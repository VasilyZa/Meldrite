package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.ActionDecision;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/** Applies the cancellation side of a pre-commit block transaction atomically. */
public final class BlockBreakBridge {
    private BlockBreakBridge() {}

    public static boolean apply(ServerPlayerEntity player, BlockPos position, ActionDecision decision) {
        if (!decision.cancelled()) return false;
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(position, player.getServerWorld().getBlockState(position)));
        return true;
    }
}
