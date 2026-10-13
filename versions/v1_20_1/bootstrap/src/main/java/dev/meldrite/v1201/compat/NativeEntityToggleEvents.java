package dev.meldrite.v1201.compat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/** Cancellable Bukkit decisions before native entity flags and client actions commit. */
public final class NativeEntityToggleEvents {
    private NativeEntityToggleEvents() { }

    public static boolean swimming(Entity entity, boolean swimming) {
        if (!(entity instanceof LivingEntity) || entity.isSwimming() == swimming
                || !NativeEntityExtensions.eventsAvailable()
                || !NativeEntityExtensions.state(entity).meldrite$isTracked()) return true;
        NativeEntityExtensions.requireThread(entity);
        var facade = (org.bukkit.entity.LivingEntity) NativeEntityExtensions.bukkit(entity);
        var event = new EntityToggleSwimEvent(facade, swimming);
        facade.getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    public static boolean clientCommand(ServerPlayerEntity player, ClientCommandC2SPacket.Mode mode) {
        if (player.isRemoved()) return false;
        if ((mode != ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY && mode != ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
                || !NativeEntityExtensions.eventsAvailable()) return true;
        NativeEntityExtensions.requireThread(player);
        var facade = (Player) NativeEntityExtensions.bukkit(player);
        var event = new PlayerToggleSneakEvent(facade, mode == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY);
        facade.getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }
}
