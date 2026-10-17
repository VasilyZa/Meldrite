package dev.meldrite.v1201.hook;

import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

/** Mixin-config anchor for the Paper constructor transform applied by MeldriteMixinPlugin. */
@Mixin(ChunkDeltaUpdateS2CPacket.class)
public abstract class NativeSectionBlockPacketMixin { }
