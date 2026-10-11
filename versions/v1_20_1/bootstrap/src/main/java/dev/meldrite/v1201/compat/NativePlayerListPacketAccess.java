package dev.meldrite.v1201.compat;

import java.util.List;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public interface NativePlayerListPacketAccess {
    PlayerListS2CPacket meldrite$withEntries(List<PlayerListS2CPacket.Entry> entries);
}
