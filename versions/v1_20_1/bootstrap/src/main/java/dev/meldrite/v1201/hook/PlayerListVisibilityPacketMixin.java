package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativePlayerListPacketAccess;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/** Makes a detached copy: broadcast packets are shared by several viewers. */
@Mixin(PlayerListS2CPacket.class)
public abstract class PlayerListVisibilityPacketMixin implements NativePlayerListPacketAccess {
    @Shadow @Final private java.util.EnumSet<PlayerListS2CPacket.Action> actions;
    @Shadow @Final @Mutable private java.util.List<PlayerListS2CPacket.Entry> entries;
    @Override public PlayerListS2CPacket meldrite$withEntries(java.util.List<PlayerListS2CPacket.Entry> selected) {
        var copy = new PlayerListS2CPacket(actions.clone(), java.util.List.of());
        ((PlayerListVisibilityPacketMixin) (Object) copy).entries = java.util.List.copyOf(selected);
        return copy;
    }
}
