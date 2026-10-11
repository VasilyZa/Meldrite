package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.WorldPort;
import dev.meldrite.foundation.BlockPosition;
import dev.meldrite.foundation.ResourceKey;
import dev.meldrite.foundation.WorldKey;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Native 1.20.1 world identity and live block projection. */
public final class NativeWorldPort implements WorldPort {
    private final MinecraftServer server;

    public NativeWorldPort(MinecraftServer server) { this.server = Objects.requireNonNull(server); }

    @Override public boolean isLoaded(WorldKey key) { return find(key) != null; }
    @Override public int minHeight(WorldKey key) { return require(key).getBottomY(); }
    @Override public int maxHeight(WorldKey key) { return require(key).getTopY(); }
    @Override public String environment(WorldKey key) {
        ServerWorld world = require(key);
        if (world.getRegistryKey().equals(net.minecraft.world.World.OVERWORLD)) return "NORMAL";
        if (world.getRegistryKey().equals(net.minecraft.world.World.NETHER)) return "NETHER";
        if (world.getRegistryKey().equals(net.minecraft.world.World.END)) return "THE_END";
        return "CUSTOM";
    }

    @Override public List<WorldInfo> loadedWorlds() {
        var result = new java.util.ArrayList<WorldInfo>();
        for (ServerWorld world : server.getWorlds()) result.add(new WorldInfo(key(world), uid(world), name(world)));
        return List.copyOf(result);
    }

    @Override public ResourceKey blockType(BlockPosition position) {
        ServerWorld world = require(position.world());
        Block block = world.getBlockState(new BlockPos(position.x(), position.y(), position.z())).getBlock();
        Identifier id = Registries.BLOCK.getId(block);
        if (id == null) throw new IllegalStateException("UNKNOWN_NATIVE_BLOCK");
        return new ResourceKey("minecraft", "block", id.getNamespace(), id.getPath());
    }

    public ServerWorld require(WorldKey key) {
        ServerWorld world = find(key);
        if (world == null) throw new IllegalStateException("WORLD_NOT_LOADED: " + key);
        return world;
    }

    public static WorldKey key(ServerWorld world) {
        Identifier id = world.getRegistryKey().getValue();
        return new WorldKey(new ResourceKey("minecraft", "dimension", id.getNamespace(), id.getPath()));
    }

    private ServerWorld find(WorldKey key) {
        for (ServerWorld world : server.getWorlds()) if (key(world).equals(key)) return world;
        return null;
    }

    private static UUID uid(ServerWorld world) {
        return UUID.nameUUIDFromBytes(("meldrite:world:" + key(world).key()).getBytes(StandardCharsets.UTF_8));
    }

    private static String name(ServerWorld world) { return world.getRegistryKey().getValue().toString(); }
}
