package dev.meldrite.v1201.compat;

import java.util.Locale;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;

/** Optional Fabric Permissions API boundary, independent of any permission provider. */
public final class FabricPermissionBridge {
    private static final boolean AVAILABLE = available();
    private FabricPermissionBridge() { }
    private static boolean available() {
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions", false, FabricPermissionBridge.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException absent) { return false; }
    }
    public static void install() { if (AVAILABLE) Api.install(); }
    static boolean isAvailable() { return AVAILABLE; }
    static TriState query(ServerCommandSource source, String node) {
        return !AVAILABLE || source == null ? TriState.DEFAULT : Api.query(source, node);
    }
    private static final class Api {
        private static final ThreadLocal<Boolean> QUERYING = new ThreadLocal<>();
        private static boolean installed;
        static synchronized void install() {
            if (installed) return;
            var event = me.lucko.fabric.api.permissions.v0.PermissionCheckEvent.EVENT;
            var fallback = new net.minecraft.util.Identifier("meldrite", "bukkit_permissions");
            event.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, fallback);
            event.register(fallback, (source, node) -> {
                if (Boolean.TRUE.equals(QUERYING.get()) || !(source instanceof ServerCommandSource command)
                        || !(command.getEntity() instanceof ServerPlayerEntity player) || Bukkit.getServer() == null)
                    return TriState.DEFAULT;
                var facade = Bukkit.getPlayer(player.getUuid());
                return facade instanceof CraftHumanEntity human
                        ? human.meldrite$bukkitPermissionValue(node.toLowerCase(Locale.ENGLISH)) : TriState.DEFAULT;
            });
            installed = true;
        }
        static TriState query(ServerCommandSource source, String node) {
            // A provider may delegate to Bukkit; it must reach the local fallback without recursion.
            if (Boolean.TRUE.equals(QUERYING.get())) return TriState.DEFAULT;
            QUERYING.set(true);
            try { return me.lucko.fabric.api.permissions.v0.Permissions.getPermissionValue(source, node); }
            finally { QUERYING.remove(); }
        }
    }
}
