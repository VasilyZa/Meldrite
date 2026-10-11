package org.bukkit.craftbukkit.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;

/** Publishes Bukkit routes while retaining the existing vanilla/Fabric dispatcher. */
public final class BukkitCommandTree {
    // Brigadier has no removal API. All three indexes must be updated together;
    // removing only getChildren() leaves stale literals executable by its parser.
    private static final List<Field> CHILD_INDEXES = List.of(index("children"), index("literals"), index("arguments"));
    private final Map<String, CommandNode<ServerCommandSource>> published = new LinkedHashMap<>();
    private RootCommandNode<ServerCommandSource> root;

    public void synchronize(CraftServer server, CommandDispatcher<ServerCommandSource> dispatcher, Map<String, Command> commands) {
        RootCommandNode<ServerCommandSource> current = dispatcher.getRoot();
        if (root != current) {
            // A native datapack reload creates a new dispatcher. Old nodes belong
            // to the old tree and must never be removed from the replacement.
            published.clear();
            root = current;
        }
        published.entrySet().removeIf(entry -> {
            // Brigadier merges a late registration into the existing literal.
            // Once another executor or child owns it, leave that node intact.
            if (root.getChild(entry.getKey()) != entry.getValue() || !exclusivelyBukkit(entry.getValue())) return true;
            Command desired = commands.get(entry.getKey());
            if (desired != null && ((BukkitCommandWrapper) entry.getValue().getCommand()).wraps(desired)) return false;
            remove(root, entry.getKey());
            return true;
        });
        for (var entry : new LinkedHashMap<>(commands).entrySet()) {
            String label = entry.getKey();
            // Native commands keep their original parser, permissions and executor.
            // Bukkit's fallback namespace still exposes a colliding plugin command.
            if (root.getChild(label) != null) continue;
            published.put(label, new BukkitCommandWrapper(server, entry.getValue()).register(dispatcher, label));
        }
    }

    private static boolean exclusivelyBukkit(CommandNode<ServerCommandSource> node) {
        if (!(node.getCommand() instanceof BukkitCommandWrapper wrapper) || node.getRequirement() != wrapper
                || node.getRedirect() != null || node.getChildren().size() != 1) return false;
        CommandNode<ServerCommandSource> arguments = node.getChild("args");
        return arguments != null && arguments.getCommand() == wrapper && arguments.getChildren().isEmpty()
                && arguments.getRedirect() == null;
    }

    private static Field index(String name) {
        try {
            Field field = CommandNode.class.getDeclaredField(name);
            if (!field.trySetAccessible()) throw new IllegalStateException("Brigadier command index is inaccessible: " + name);
            return field;
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot access Brigadier command index: " + name, failure);
        }
    }

    private static void remove(CommandNode<?> root, String label) {
        try {
            for (Field index : CHILD_INDEXES) ((Map<?, ?>) index.get(root)).remove(label);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Cannot remove Bukkit command: " + label, failure);
        }
    }
}
