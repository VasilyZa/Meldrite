package dev.meldrite.v1201.compat;

import dev.meldrite.paper.registry.ScoreboardProjection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.packet.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftCriteria;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboard;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboardManager;

public final class NativeScoreboardBinding {
    private static volatile CraftScoreboardManager manager;
    private NativeScoreboardBinding() {}
    public static CraftScoreboardManager create(MinecraftServer server) { return new CraftScoreboardManager(server); }
    public static CraftScoreboardManager current(MinecraftServer server) {
        CraftScoreboardManager value = manager;
        return value == null ? create(server) : value;
    }
    public static void bind(MinecraftServer server) {
        var value = create(server);
        ScoreboardProjection.bind(value, CraftCriteria::fromName);
        manager = value;
    }
    public static CraftScoreboard getPlayerBoard(ServerPlayerEntity player) { return bound().getPlayerBoard(player); }
    public static void setPlayerBoard(ServerPlayerEntity player, org.bukkit.scoreboard.Scoreboard board) { bound().setPlayerBoard(player, board); }

    public static void broadcast(ServerScoreboard board, PlayerManager players, Packet<?> packet) {
        var value = manager;
        if (value == null || value.getMainScoreboard().getHandle() != board) players.sendToAll(packet);
        else value.broadcast(board, packet);
    }

    public static List<ServerPlayerEntity> audience(ServerScoreboard board, PlayerManager players) {
        var value = manager;
        if (value == null || value.getMainScoreboard().getHandle() != board) return players.getPlayerList();
        return players.getPlayerList().stream().filter(player -> value.isAssigned(board, player)).toList();
    }

    public static boolean synchronizeAssignedPlayer(ServerPlayerEntity player) {
        var value = manager;
        if (value == null || value.getPlayerBoard(player) == value.getMainScoreboard()) return false;
        value.synchronizePlayer(player);
        return true;
    }

    public static void removePlayer(ServerPlayerEntity player) {
        var value = manager;
        if (value != null) value.removePlayer(player);
    }

    public static void updateCustomCriteria(Scoreboard board, ScoreboardCriterion criteria, String name, Consumer<ScoreboardPlayerScore> update) {
        var value = manager;
        if (value != null) value.updateCustomCriteria(board, criteria, name, update);
    }

    private static CraftScoreboardManager bound() {
        var value = manager;
        if (value == null) throw new IllegalStateException("NATIVE_SCOREBOARD_NOT_BOUND");
        return value;
    }
}
