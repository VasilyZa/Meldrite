package dev.meldrite.v1201.hook;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

import java.util.EnumSet;
import java.util.List;

/** Legacy packet accessors for native player-list data. */
@Mixin(PlayerListS2CPacket.class)
public abstract class LegacyPlayerListPacketAliasesMixin {
    public EnumSet<PlayerListS2CPacket.Action> b() {
        return ((PlayerListS2CPacket) (Object) this).getActions();
    }

    public List<PlayerListS2CPacket.Entry> e() {
        return ((PlayerListS2CPacket) (Object) this).getEntries();
    }
}
