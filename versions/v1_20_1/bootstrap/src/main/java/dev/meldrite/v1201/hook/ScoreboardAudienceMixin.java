package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeScoreboardBinding;
import java.util.List;
import net.minecraft.network.packet.Packet;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerScoreboard.class)
abstract class ScoreboardAudienceMixin {
    @Redirect(method = {
            "updateScore(Lnet/minecraft/scoreboard/ScoreboardPlayerScore;)V",
            "updatePlayerScore(Ljava/lang/String;)V",
            "updatePlayerScore(Ljava/lang/String;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            "setObjectiveSlot(ILnet/minecraft/scoreboard/ScoreboardObjective;)V",
            "addPlayerToTeam(Ljava/lang/String;Lnet/minecraft/scoreboard/Team;)Z",
            "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/scoreboard/Team;)V",
            "updateExistingObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            "updateScoreboardTeamAndPlayers(Lnet/minecraft/scoreboard/Team;)V",
            "updateScoreboardTeam(Lnet/minecraft/scoreboard/Team;)V",
            "updateRemovedTeam(Lnet/minecraft/scoreboard/Team;)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"))
    private void meldrite$sendAssigned(PlayerManager players, Packet<?> packet) {
        NativeScoreboardBinding.broadcast((ServerScoreboard) (Object) this, players, packet);
    }

    @Redirect(method = {"addScoreboardObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            "removeScoreboardObjective(Lnet/minecraft/scoreboard/ScoreboardObjective;)V"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;getPlayerList()Ljava/util/List;"))
    private List<ServerPlayerEntity> meldrite$assignedPlayers(PlayerManager players) {
        return NativeScoreboardBinding.audience((ServerScoreboard) (Object) this, players);
    }
}
