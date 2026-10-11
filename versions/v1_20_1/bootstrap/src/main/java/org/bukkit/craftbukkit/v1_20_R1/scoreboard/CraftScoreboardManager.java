package org.bukkit.craftbukkit.v1_20_R1.scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.network.packet.Packet;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bukkit.scoreboard.ScoreboardManager;

public final class CraftScoreboardManager extends org.bukkit.craftbukkit.scoreboard.CraftScoreboardManager {
    private final BooleanSupplier primary;
    private final Supplier<List<Viewer>> viewers;
    private final CraftScoreboard main;
    private final Map<UUID, CraftScoreboard> assignments = new HashMap<>();
    private final Set<CraftScoreboard> boards = Collections.newSetFromMap(new WeakHashMap<>());

    public CraftScoreboardManager(MinecraftServer server) {
        this(Objects.requireNonNull(server).getScoreboard(), server::isOnThread,
                () -> server.getPlayerManager().getPlayerList().stream()
                        .map(player -> new Viewer(player.getUuid(), player.networkHandler::sendPacket)).toList());
    }

    CraftScoreboardManager(Scoreboard mainBoard, BooleanSupplier primary, Supplier<List<Viewer>> viewers) {
        this.primary = Objects.requireNonNull(primary);
        this.viewers = Objects.requireNonNull(viewers);
        main = new CraftScoreboard(mainBoard, primary);
        boards.add(main);
    }

    @Override public CraftScoreboard getMainScoreboard() { return main; }
    @Override public CraftScoreboard getNewScoreboard() {
        mutation();
        var board = new CraftScoreboard(new AssignedScoreboard(this), primary);
        boards.add(board);
        return board;
    }

    public CraftScoreboard getPlayerBoard(ServerPlayerEntity player) { return getPlayerBoard(player.getUuid()); }
    CraftScoreboard getPlayerBoard(UUID player) { return assignments.getOrDefault(player, main); }

    public void setPlayerBoard(ServerPlayerEntity player, org.bukkit.scoreboard.Scoreboard board) {
        if (player.networkHandler == null) throw new IllegalStateException("Player has no active network connection");
        setPlayerBoard(player.getUuid(), board, player.networkHandler::sendPacket);
    }

    void setPlayerBoard(UUID player, org.bukkit.scoreboard.Scoreboard board, Consumer<Packet<?>> send) {
        mutation();
        if (!(board instanceof CraftScoreboard next) || !boards.contains(next)) {
            throw new IllegalArgumentException("Scoreboard was not created by this server's scoreboard manager");
        }
        var previous = getPlayerBoard(player);
        if (previous.getHandle() == next.getHandle()) return;
        var packets = new ArrayList<Packet<?>>();
        packets.addAll(ScoreboardPackets.clear(previous.getHandle()));
        packets.addAll(ScoreboardPackets.snapshot(next.getHandle()));
        if (next == main) assignments.remove(player); else assignments.put(player, next);
        packets.forEach(send);
    }

    public boolean isAssigned(Scoreboard board, ServerPlayerEntity player) {
        return getPlayerBoard(player).getHandle() == board;
    }

    public void broadcast(Scoreboard board, Packet<?> packet) {
        for (var viewer : viewers.get()) {
            if (getPlayerBoard(viewer.id()).getHandle() == board) viewer.send().accept(packet);
        }
    }

    public void updateCustomCriteria(Scoreboard source, ScoreboardCriterion criteria, String name, Consumer<ScoreboardPlayerScore> update) {
        if (source != main.getHandle()) return;
        for (var board : List.copyOf(boards)) {
            if (board != main) board.getHandle().forEachScore(criteria, name, update);
        }
    }

    public void synchronizePlayer(ServerPlayerEntity player) {
        mutation();
        var board = getPlayerBoard(player).getHandle();
        ScoreboardPackets.clear(board).forEach(player.networkHandler::sendPacket);
        ScoreboardPackets.snapshot(board).forEach(player.networkHandler::sendPacket);
    }

    public void removePlayer(ServerPlayerEntity player) { assignments.remove(player.getUuid()); }
    @Override public void removePlayer(org.bukkit.entity.Player player) {
        if (player instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer craft) {
            removePlayer(craft.getHandle());
        }
    }

    private void mutation() {
        if (!primary.getAsBoolean()) throw new IllegalStateException("Scoreboard mutation requires the server thread");
    }

    record Viewer(UUID id, Consumer<Packet<?>> send) { }
}
