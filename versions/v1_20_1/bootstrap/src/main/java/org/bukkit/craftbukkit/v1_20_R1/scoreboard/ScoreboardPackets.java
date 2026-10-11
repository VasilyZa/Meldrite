package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

final class ScoreboardPackets {
    private ScoreboardPackets() { }

    static Set<ScoreboardObjective> displayed(Scoreboard board) {
        var objectives = new LinkedHashSet<ScoreboardObjective>();
        for (int slot = 0; slot < Scoreboard.DISPLAY_SLOT_COUNT; slot++) {
            var objective = board.getObjectiveForSlot(slot);
            if (objective != null) objectives.add(objective);
        }
        return objectives;
    }

    static List<Packet<?>> clear(Scoreboard board) {
        var packets = new ArrayList<Packet<?>>();
        for (var objective : displayed(board)) packets.add(new ScoreboardObjectiveUpdateS2CPacket(objective, 1));
        for (var team : board.getTeams()) packets.add(TeamS2CPacket.updateRemovedTeam(team));
        return packets;
    }

    static List<Packet<?>> snapshot(Scoreboard board) {
        var packets = new ArrayList<Packet<?>>();
        for (var team : board.getTeams()) packets.add(TeamS2CPacket.updateTeam(team, true));
        for (var objective : displayed(board)) packets.addAll(objective(board, objective));
        return packets;
    }

    static List<Packet<?>> objective(Scoreboard board, ScoreboardObjective objective) {
        var packets = new ArrayList<Packet<?>>();
        packets.add(new ScoreboardObjectiveUpdateS2CPacket(objective, 0));
        for (int slot = 0; slot < Scoreboard.DISPLAY_SLOT_COUNT; slot++) {
            if (board.getObjectiveForSlot(slot) == objective) packets.add(new ScoreboardDisplayS2CPacket(slot, objective));
        }
        for (var score : board.getAllPlayerScores(objective)) packets.add(score(score));
        return packets;
    }

    static ScoreboardPlayerUpdateS2CPacket score(ScoreboardPlayerScore score) {
        return new ScoreboardPlayerUpdateS2CPacket(ServerScoreboard.UpdateMode.CHANGE,
                score.getObjective().getName(), score.getPlayerName(), score.getScore());
    }
}
