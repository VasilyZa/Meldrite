import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Trees;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.tools.ToolProvider;

/** Regenerates CraftPlayer API forwarding while retaining its native business methods. */
public final class GenerateCraftPlayer {
    private record Source(String text, CompilationUnitTree unit, ClassTree type, Trees trees) {
        List<MethodTree> methods() {
            return type.getMembers().stream().filter(MethodTree.class::isInstance).map(MethodTree.class::cast).toList();
        }
        String method(MethodTree method) {
            var positions = trees.getSourcePositions();
            return text.substring((int) positions.getStartPosition(unit, method), (int) positions.getEndPosition(unit, method));
        }
        String declaration(MethodTree method) {
            var positions = trees.getSourcePositions();
            return text.substring((int) positions.getStartPosition(unit, method), (int) positions.getStartPosition(unit, method.getBody()));
        }
    }
    public static void main(String[] args) throws Exception {
        Path root = args.length == 0 ? Path.of(".") : Path.of(args[0]);
        Path nativeDirectory = root.resolve("versions/v1_20_1/bootstrap/src/main/java/org/bukkit/craftbukkit/v1_20_R1/entity");
        Path target = nativeDirectory.resolve("CraftPlayer.java");
        Source previous = parse(target);
        Source entity = parse(nativeDirectory.resolve("CraftEntity.java"));
        Source api = parse(root.resolve("adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/facade/PlayerAdapter.java"));
        Source entityApi = parse(root.resolve("adapters/paper-1.20.1/src/main/java/dev/meldrite/paper/facade/EntityAdapter.java"));
        Set<String> inherited = new HashSet<>();
        Map<String, String> inheritedReturns = new java.util.HashMap<>();
        entity.methods().forEach(method -> inherited.add(key(method)));
        entityApi.methods().forEach(method -> inherited.add(key(method)));
        entity.methods().stream().filter(method -> method.getReturnType() != null)
                .forEach(method -> inheritedReturns.put(key(method), type(method.getReturnType().toString())));
        entityApi.methods().stream().filter(method -> method.getReturnType() != null)
                .forEach(method -> inheritedReturns.putIfAbsent(key(method), type(method.getReturnType().toString())));
        for (String name : List.of("CraftLivingEntity.java", "CraftHumanEntity.java")) {
            Source parent = parse(nativeDirectory.resolve(name));
            parent.methods().forEach(method -> inherited.add(key(method)));
            parent.methods().stream().filter(method -> method.getReturnType() != null)
                    .forEach(method -> inheritedReturns.put(key(method), type(method.getReturnType().toString())));
        }
        Set<String> nativeMethods = Set.of("getHandle", "meldrite$rebindNative", "getPing", "isOnline", "getScoreboard", "setScoreboard", "getPlayerProfile", "getPersistentDataContainer",
                "applyMending", "getExp", "setExp", "getLevel", "setLevel", "getTotalExperience", "setTotalExperience",
                "giveExp", "giveExpLevels", "getExpCooldown", "setExpCooldown", "sendExperienceChange", "displayName", "getDisplayName", "setDisplayName", "getStatistic", "setStatistic", "incrementStatistic", "decrementStatistic", "getName", "getProfile",
                "getAllowFlight", "setAllowFlight", "isFlying", "setFlying", "getFlySpeed", "setFlySpeed", "getWalkSpeed", "setWalkSpeed", "validateSpeed", "getAdvancementProgress",
                "getBedSpawnLocation", "setBedSpawnLocation", "getSpectatorTarget", "setSpectatorTarget", "getWorldBorder", "setWorldBorder", "borderListener", "meldrite$restoreClientState", "isSprinting", "setSprinting", "getCooldownPeriod", "getCooledAttackStrength",
                "sendMessage", "sendRawMessage", "sendActionBar", "setTitleTimes", "sendTitle", "showTitle", "setSubtitle", "hideTitle", "resetTitle", "updateTitle",
                "sendHealthUpdate", "playSound", "stopSound", "stopAllSounds", "playerListHeader", "playerListFooter", "getPlayerListHeader", "getPlayerListFooter",
                "setPlayerListHeader", "setPlayerListFooter", "setPlayerListHeaderFooter", "getResourcePackHash", "hasResourcePack", "setResourcePack",
                "getPlayerListName", "setPlayerListName", "playerListName", "updatePlayerListName",
                "sendPlayerListHeader", "sendPlayerListFooter", "sendPlayerListHeaderAndFooter",
                "sendPlayerListHeaderPacket", "sendPlayerPacket", "legacyText", "componentText", "nativeComponent", "nativeCategory", "soundEvent",
                "sendTitlePackets", "sendLegacyTitle", "sendMessageComponent", "sendResourcePackUrl", "playPlayerSound", "playPlayerEntitySound", "performCommand", "updateCommands",
                "playerState", "getAddress", "getPlayer", "name", "getLocale", "locale", "getClientBrandName", "getClientOption", "getClientViewDistance", "getViewDistance", "getSimulationDistance", "setViewDistance", "setSimulationDistance",
                "getCompassTarget", "setCompassTarget", "getFirstPlayed", "getLastPlayed", "getLastLogin", "getLastSeen", "hasPlayedBefore", "getListeningPluginChannels",
                "getPlayerTime", "getPlayerTimeOffset", "isPlayerTimeRelative", "setPlayerTime", "resetPlayerTime", "sendPlayerTime", "getPlayerWeather", "setPlayerWeather",
                "resetPlayerWeather", "sendPlayerWeather", "isSleepingIgnored", "setSleepingIgnored", "canSee", "hidePlayer", "showPlayer", "hideEntity", "showEntity",
                "changeEntityVisibility", "isConnected", "isAllowingServerListings", "kick", "kickPlayer", "loadData", "saveData", "serialize", "spigot",
                "openBook", "playEffect", "sendBlockChange", "sendBlockChanges", "sendPluginMessage", "sendSignChange", "sendSignText", "updateInventory", "setPlayerProfile", "chat");
        List<MethodTree> retained = previous.methods().stream().filter(method -> nativeMethods.contains(method.getName().toString())).toList();
        retained.forEach(method -> inherited.add(key(method)));
        StringBuilder output = new StringBuilder("""
                package org.bukkit.craftbukkit.v1_20_R1.entity;

                import dev.meldrite.craft.EntityDelegate;
                import dev.meldrite.paper.facade.FacadeContext;
                import dev.meldrite.v1201.bridge.NativeEntityPort;
                import net.minecraft.server.network.ServerPlayerEntity;

                /** Player API forwarding is generated by tools/GenerateCraftPlayer.java. */
                public class CraftPlayer extends CraftHumanEntity implements org.bukkit.entity.Player {
                private final dev.meldrite.paper.facade.PlayerAdapter playerApi;
                private volatile ServerPlayerEntity nativePlayerHandle;
                private final net.minecraft.stat.ServerStatHandler statistics;
                private final com.mojang.authlib.GameProfile profile;
                private volatile org.bukkit.craftbukkit.v1_20_R1.CraftWorldBorder playerWorldBorder;
                private net.minecraft.world.border.WorldBorderListener playerWorldBorderListener;
                private volatile net.kyori.adventure.text.Component playerListHeader = net.kyori.adventure.text.Component.empty();
                private volatile net.kyori.adventure.text.Component playerListFooter = net.kyori.adventure.text.Component.empty();
                private net.minecraft.text.Text pendingSubtitle = net.minecraft.text.Text.empty();
                private String resourcePackUrl;
                private String resourcePackHash;
                private boolean resourcePackRequired;
                private int titleFadeIn = 10;
                private int titleStay = 70;
                private int titleFadeOut = 20;
                    public CraftPlayer(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
                        super(delegate, context, entities);
                    this.playerApi = new dev.meldrite.paper.facade.PlayerAdapter(delegate, context);
                    this.nativePlayerHandle = (ServerPlayerEntity) entities.require(delegate.identity().handle());
                    this.statistics = nativePlayerHandle.getStatHandler();
                    this.profile = nativePlayerHandle.getGameProfile();
                    }
                """);
        for (MethodTree method : retained) output.append("    ").append(previous.method(method)).append('\n');
        int forwarded = 0;
        for (MethodTree method : api.methods()) {
            if (method.getReturnType() == null || nativeMethods.contains(method.getName().toString())
                    || inherited.contains(key(method)) && type(method.getReturnType().toString()).equals(inheritedReturns.get(key(method)))) continue;
            output.append("    ").append(api.declaration(method).trim()).append(" { ");
            if (!method.getReturnType().toString().equals("void")) output.append("return ");
            output.append("playerApi.").append(method.getName()).append('(');
            output.append(String.join(", ", method.getParameters().stream().map(parameter -> parameter.getName().toString()).toList()));
            output.append("); }\n");
            forwarded++;
        }
        output.append("}\n");
        Files.writeString(target, output);
        System.out.println("Generated CraftPlayer with " + forwarded + " forwarding methods and " + retained.size() + " native methods");
    }
    private static String key(MethodTree method) {
        return method.getName() + "(" + String.join(",", method.getParameters().stream().map(parameter -> type(parameter.getType().toString())).toList()) + ")";
    }
    private static String type(String value) { return value.replaceAll("(?<![\\w.])String(?![\\w])", "java.lang.String"); }
    private static Source parse(Path path) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var manager = compiler.getStandardFileManager(null, null, null);
        var task = (JavacTask) compiler.getTask(null, manager, null, List.of("-proc:none"), null, manager.getJavaFileObjects(path));
        CompilationUnitTree unit = task.parse().iterator().next();
        ClassTree type = (ClassTree) unit.getTypeDecls().stream().filter(ClassTree.class::isInstance).findFirst().orElseThrow();
        return new Source(Files.readString(path), unit, type, Trees.instance(task));
    }
}
