package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.ActionDecision;
import dev.meldrite.bridge.ActionSnapshot;
import dev.meldrite.bridge.EventIngressPort;
import dev.meldrite.foundation.BlockPosition;
import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.foundation.ResourceKey;
import dev.meldrite.foundation.WorldKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** The only version-owned gateway called by the Mixin. */
public final class HookGateway implements EventIngressPort, AutoCloseable {
    private static volatile HookGateway current;
    private final MinecraftServer server;
    private final NativeWorldPort worlds;
    private final NativeEntityPort entities;
    private final AtomicReference<EventIngressPort> ingress = new AtomicReference<>();

    public HookGateway(MinecraftServer server) {
        this.server = Objects.requireNonNull(server);
        this.worlds = new NativeWorldPort(server);
        this.entities = new NativeEntityPort(server, worlds);
        current = this;
    }

    public static HookGateway current() {
        HookGateway gateway = current;
        if (gateway == null) throw new IllegalStateException("MELDRITE_HOOK_GATEWAY_MISSING");
        return gateway;
    }

    public void install(EventIngressPort target) {
        ingress.compareAndSet(null, Objects.requireNonNull(target));
        if (ingress.get() != target) throw new IllegalStateException("MELDRITE_EVENT_INGRESS_ALREADY_INSTALLED");
    }

    public boolean ready() { return ingress.get() != null; }

    public ActionDecision before(ServerPlayerEntity player, BlockPos position) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(position);
        WorldKey world = NativeWorldPort.key(player.getServerWorld());
        EntityHandle actor = new EntityHandle(world, player.getUuid(), Integer.toUnsignedLong(player.getId()) + 1);
        BlockPosition block = new BlockPosition(world, position.getX(), position.getY(), position.getZ());
        ResourceKey type = worlds.blockType(block);
        ActionSnapshot snapshot = new ActionSnapshot(UUID.randomUUID().toString(), "block-break",
                Map.of("source", "server-player-interaction-manager", "block", type.entryNamespace() + ":" + type.entryPath()),
                Optional.of(new ActionSnapshot.BlockBreakInput(actor, block, type, 0, true)));
        return before(snapshot);
    }

    @Override public ActionDecision before(ActionSnapshot action) {
        EventIngressPort target = ingress.get();
        if (target == null) throw new IllegalStateException("MELDRITE_EVENT_INGRESS_NOT_READY");
        return Objects.requireNonNull(target.before(action), "Event ingress returned null");
    }

    public MinecraftServer server() { return server; }
    public NativeWorldPort worlds() { return worlds; }
    public NativeEntityPort entities() { return entities; }

    @Override public void close() {
        ingress.set(null);
        if (current == this) current = null;
    }
}
