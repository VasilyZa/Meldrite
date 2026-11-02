package dev.meldrite.v1201.compat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import dev.meldrite.paper.bootstrap.ProfileBootstrap.ProfileServer;
import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.command.BukkitCommandTree;
import org.bukkit.craftbukkit.command.BukkitCommandWrapper;
import org.bukkit.plugin.Plugin;

/** Completion-only routes from a player's advertised tree; never used to execute commands. */
public final class NativeCommandSuggestions {
    private record Route(CommandNode<ServerCommandSource> authority, BukkitCommandWrapper wrapper,
            CommandDispatcher<ServerCommandSource> view, Set<SuggestionProvider<?>> providers) { }

    private final CommandDispatcher<ServerCommandSource> authority;
    private final Map<String, Route> routes = new LinkedHashMap<>();
    private final Set<SuggestionProvider<?>> unavailable = Collections.synchronizedSet(
            Collections.newSetFromMap(new IdentityHashMap<>()));

    private NativeCommandSuggestions(CommandDispatcher<ServerCommandSource> authority) { this.authority = authority; }

    static Map<CommandNode<?>, SuggestionProvider<?>> providers(CommandNode<?> root) {
        var result = new IdentityHashMap<CommandNode<?>, SuggestionProvider<?>>();
        collect(root, result, Collections.newSetFromMap(new IdentityHashMap<>()));
        return result;
    }

    private static void collect(CommandNode<?> node, Map<CommandNode<?>, SuggestionProvider<?>> result,
            Set<CommandNode<?>> visited) {
        if (!visited.add(node)) return;
        if (node instanceof ArgumentCommandNode<?, ?> argument) result.put(node, argument.getCustomSuggestions());
        for (var child : node.getChildren()) collect(child, result, visited);
        if (node.getRedirect() != null) collect(node.getRedirect(), result, visited);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void capture(ServerPlayerEntity player, RootCommandNode<?> root,
            Map<CommandNode<?>, SuggestionProvider<?>> before) {
        var dispatcher = player.server.getCommandManager().getDispatcher();
        var view = new NativeCommandSuggestions(dispatcher);
        var candidates = new LinkedHashMap<CommandNode<?>, Set<SuggestionProvider<?>>>();
        for (var node : root.getChildren()) {
            Set<SuggestionProvider<?>> changed = Collections.newSetFromMap(new IdentityHashMap<>());
            providers(node).forEach((argument, provider) -> {
                if (provider != null && before.get(argument) != provider) changed.add(provider);
            });
            if (!changed.isEmpty()) candidates.put(node, changed);
        }
        for (var visible : root.getChildren()) {
            var nativeNode = dispatcher.getRoot().getChild(visible.getName());
            // A greedy Bukkit route has only the root permission gate. Native commands retain
            // their own parsers and subcommand permissions, so their providers stay authoritative.
            if (nativeNode == null || !BukkitCommandTree.exclusivelyBukkit(nativeNode)) continue;
            var wrapper = (BukkitCommandWrapper) nativeNode.getCommand();
            var selected = visible;
            var added = candidates.get(selected);
            if (added == null) for (var candidate : candidates.entrySet()) {
                var original = dispatcher.getRoot().getChild(candidate.getKey().getName());
                if (original != null && BukkitCommandTree.exclusivelyBukkit(original)
                        && wrapper.sameCommand((BukkitCommandWrapper) original.getCommand())) {
                    selected = candidate.getKey(); added = candidate.getValue(); break;
                }
            }
            if (added == null) continue;
            var alias = new LiteralCommandNode<ServerCommandSource>(visible.getName(), null,
                    (java.util.function.Predicate) selected.getRequirement(), (CommandNode) selected.getRedirect(),
                    (com.mojang.brigadier.RedirectModifier) selected.getRedirectModifier(), selected.isFork());
            for (var child : selected.getChildren()) alias.addChild((CommandNode) child);
            var routeRoot = new RootCommandNode<ServerCommandSource>();
            routeRoot.addChild(alias);
            view.routes.put(visible.getName(), new Route(nativeNode, wrapper, new CommandDispatcher<>(routeRoot), added));
        }
        ((PaperPlayerAccess) player).meldrite$playerState().commandSuggestions = view.routes.isEmpty() ? null : view;
    }

    public static CompletableFuture<Suggestions> complete(ServerPlayerEntity player,
            CommandDispatcher<ServerCommandSource> dispatcher, ParseResults<ServerCommandSource> parsed,
            CompletableFuture<Suggestions> ordinary) {
        var view = ((PaperPlayerAccess) player).meldrite$playerState().commandSuggestions;
        if (view == null || view.authority != dispatcher) return ordinary;
        return view.complete(player, parsed, ordinary);
    }

    private CompletableFuture<Suggestions> complete(ServerPlayerEntity player, ParseResults<ServerCommandSource> parsed,
            CompletableFuture<Suggestions> ordinary) {
        var reader = new StringReader(parsed.getReader().getString());
        if (reader.canRead() && reader.peek() == '/') reader.skip();
        int start = reader.getCursor();
        while (reader.canRead() && reader.peek() != ' ') reader.skip();
        String label = reader.getString().substring(start, reader.getCursor());
        var route = routes.get(label);
        if (route == null || !reader.canRead() || authority.getRoot().getChild(label) != route.authority()
                || !route.authority().canUse(parsed.getContext().getSource())
                || route.wrapper().owner() != null && !route.wrapper().owner().isEnabled()) return ordinary;
        reader.setCursor(start);
        var results = new ArrayList<CompletableFuture<Suggestions>>();
        try {
            var context = call(route.wrapper().owner(), () -> route.view().parse(reader, parsed.getContext().getSource()));
            var suggestionContext = context.getContext().findSuggestionContext(reader.getTotalLength());
            var commandContext = context.getContext().build(reader.getString());
            for (var child : suggestionContext.parent.getChildren()) {
                if (!(child instanceof ArgumentCommandNode<ServerCommandSource, ?> argument)) continue;
                var provider = argument.getCustomSuggestions();
                if (provider == null || !route.providers().contains(provider) || unavailable.contains(provider)) continue;
                Plugin owner = provider.getClass().getClassLoader() instanceof ConfiguredPluginClassLoader loader
                        ? loader.getPlugin() : route.wrapper().owner();
                if (owner == null && provider.getClass().getClassLoader() instanceof ConfiguredPluginClassLoader
                        || owner != null && !owner.isEnabled()) continue;
                try {
                    var future = call(owner, () -> child.canUse(commandContext.getSource())
                            ? provider.getSuggestions(commandContext, new SuggestionsBuilder(reader.getString(),
                                    Math.min(suggestionContext.startPos, reader.getTotalLength()))) : Suggestions.empty());
                    results.add(java.util.Objects.requireNonNull(future, "Suggestion provider returned null").handle((value, failure) -> {
                        if (failure == null && value != null) return value;
                        providerFailed(provider, failure == null
                                ? new IllegalStateException("Suggestion provider returned null") : failure);
                        return null;
                    }));
                } catch (CommandSyntaxException ignored) {
                    // Brigadier treats an argument syntax rejection as no suggestions from that child.
                } catch (Exception failure) { providerFailed(provider, failure); }
            }
        } catch (Exception failure) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot parse player command completion view; using Bukkit completion", failure);
            return ordinary;
        }
        if (results.isEmpty()) return ordinary;
        return CompletableFuture.allOf(results.toArray(CompletableFuture[]::new)).thenCompose(ignored -> {
            var suggestions = results.stream().map(CompletableFuture::join).filter(java.util.Objects::nonNull).toList();
            return suggestions.isEmpty() ? ordinary
                    : CompletableFuture.completedFuture(Suggestions.merge(reader.getString(), suggestions));
        });
    }

    private <T> T call(Plugin owner, Callable<T> callback) throws Exception {
        return owner == null ? callback.call() : ((ProfileServer) Bukkit.getServer()).callPluginCallback(owner, callback);
    }

    private void providerFailed(SuggestionProvider<?> provider, Throwable failure) {
        while (failure instanceof CompletionException && failure.getCause() != null) failure = failure.getCause();
        synchronized (unavailable) {
            if (!unavailable.add(provider)) return;
        }
        // Client-only trees may deliberately contain placeholder providers. They retain the
        // real Bukkit TabCompleter; unexpected failures still receive an actionable diagnostic.
        Bukkit.getLogger().log(failure instanceof UnsupportedOperationException ? Level.FINE : Level.WARNING,
                "Player command suggestion provider unavailable; using Bukkit completion: " + provider.getClass().getName(), failure);
    }
}
