package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScoreboardPlayerUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;

/** Native state with the same packet events as ServerScoreboard, scoped to its assigned players. */
final class AssignedScoreboard extends Scoreboard {
    private final CraftScoreboardManager manager;
    private final Set<ScoreboardObjective> displayed = new HashSet<>();

    AssignedScoreboard(CraftScoreboardManager manager) { this.manager = manager; }
    private void send(Packet<?> packet) { manager.broadcast(this, packet); }

    @Override public void updateScore(ScoreboardPlayerScore score) {
        super.updateScore(score);
        if (displayed.contains(score.getObjective())) send(ScoreboardPackets.score(score));
    }

    @Override public void updatePlayerScore(String player) {
        super.updatePlayerScore(player);
        send(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, null, player, 0));
    }

    @Override public void updatePlayerScore(String player, ScoreboardObjective objective) {
        super.updatePlayerScore(player, objective);
        if (displayed.contains(objective)) {
            send(new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.REMOVE, objective.getName(), player, 0));
        }
    }

    @Override public void setObjectiveSlot(int slot, ScoreboardObjective objective) {
        var previous = getObjectiveForSlot(slot);
        super.setObjectiveSlot(slot, objective);
        if (previous == objective) return;
        if (previous != null && !ScoreboardPackets.displayed(this).contains(previous) && displayed.remove(previous)) {
            send(new ScoreboardObjectiveUpdateS2CPacket(previous, 1));
        }
        if (objective != null && displayed.add(objective)) {
            ScoreboardPackets.objective(this, objective).forEach(this::send);
        } else {
            send(new ScoreboardDisplayS2CPacket(slot, objective));
        }
    }

    @Override public void updateExistingObjective(ScoreboardObjective objective) {
        super.updateExistingObjective(objective);
        if (displayed.contains(objective)) send(new ScoreboardObjectiveUpdateS2CPacket(objective, 2));
    }

    @Override public void updateRemovedObjective(ScoreboardObjective objective) {
        super.updateRemovedObjective(objective);
        if (displayed.remove(objective)) send(new ScoreboardObjectiveUpdateS2CPacket(objective, 1));
    }

    @Override public boolean addPlayerToTeam(String player, Team team) {
        if (!super.addPlayerToTeam(player, team)) return false;
        send(TeamS2CPacket.changePlayerTeam(team, player, TeamS2CPacket.Operation.ADD));
        return true;
    }

    @Override public void removePlayerFromTeam(String player, Team team) {
        super.removePlayerFromTeam(player, team);
        send(TeamS2CPacket.changePlayerTeam(team, player, TeamS2CPacket.Operation.REMOVE));
    }

    @Override public void updateScoreboardTeamAndPlayers(Team team) {
        super.updateScoreboardTeamAndPlayers(team);
        send(TeamS2CPacket.updateTeam(team, true));
    }

    @Override public void updateScoreboardTeam(Team team) {
        super.updateScoreboardTeam(team);
        send(TeamS2CPacket.updateTeam(team, false));
    }

    @Override public void updateRemovedTeam(Team team) {
        super.updateRemovedTeam(team);
        send(TeamS2CPacket.updateRemovedTeam(team));
    }
}
