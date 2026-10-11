package dev.meldrite.v1201.compat;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorldBorder;

/** World broadcasts cannot replace an explicitly assigned player border. */
public final class NativePlayerBorder {
    private NativePlayerBorder() { }
    public static Packet<?> personalize(ServerPlayerEntity player, Packet<?> packet) {
        if (!(packet instanceof WorldBorderInitializeS2CPacket || packet instanceof WorldBorderCenterChangedS2CPacket
                || packet instanceof WorldBorderInterpolateSizeS2CPacket || packet instanceof WorldBorderWarningTimeChangedS2CPacket
                || packet instanceof WorldBorderWarningBlocksChangedS2CPacket || packet instanceof WorldBorderSizeChangedS2CPacket)) return packet;
        if (!(player instanceof PaperEntityAccess access) || !(access.meldrite$getCachedBukkitEntity() instanceof org.bukkit.entity.Player facade)) return packet;
        return facade.getWorldBorder() instanceof CraftWorldBorder custom
                ? new WorldBorderInitializeS2CPacket(custom.getHandle()) : packet;
    }
}
