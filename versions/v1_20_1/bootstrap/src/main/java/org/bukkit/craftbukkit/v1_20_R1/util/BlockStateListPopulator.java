package org.bukkit.craftbukkit.v1_20_R1.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.StructureWorldAccess;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState;

/** Captures native block writes and applies them as a single generation batch. */
public final class BlockStateListPopulator implements StructureWorldAccess {
    private final StructureWorldAccess world;
    private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
    private final Map<BlockPos, BlockEntity> entities = new LinkedHashMap<>();
    private final Map<BlockPos, Integer> flags = new LinkedHashMap<>();
    public BlockStateListPopulator(StructureWorldAccess world) { this.world = java.util.Objects.requireNonNull(world, "world"); }
    public BlockState getBlockState(BlockPos pos) { return blocks.containsKey(pos) ? blocks.get(pos) : world.getBlockState(pos); }
    public BlockEntity getBlockEntity(BlockPos pos) { return entities.containsKey(pos) ? entities.get(pos) : world.getBlockEntity(pos); }
    public boolean setBlock(BlockPos pos, BlockState state, int flags) {
        BlockPos immutable = pos.toImmutable();
        blocks.put(immutable, state);
        this.flags.put(immutable, flags);
        entities.put(immutable, state.getBlock() instanceof net.minecraft.block.BlockEntityProvider provider ? provider.createBlockEntity(immutable, state) : null);
        return true;
    }
    public List<CraftBlockState> getList() {
        List<CraftBlockState> result = new ArrayList<>();
        for (var entry : blocks.entrySet()) {
            var snapshot = world instanceof net.minecraft.server.world.ServerWorld serverWorld
                    ? CraftBlockState.createCaptured(serverWorld, entry.getKey(), entry.getValue(), entities.get(entry.getKey()))
                    : CraftBlockState.createUnplaced(entry.getValue(), entities.get(entry.getKey()));
            ((CraftBlockState) snapshot).setFlag(flags.get(entry.getKey()));
            result.add((CraftBlockState) snapshot);
        }
        return result;
    }
    public void updateList() { for (var entry : blocks.entrySet()) world.setBlockState(entry.getKey(), entry.getValue(), flags.get(entry.getKey())); }
    public void refreshTiles() { }
    public Map<BlockPos, BlockState> getBlocks() { return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(blocks)); }
    public StructureWorldAccess getWorld() { return world; }
    @Override public net.minecraft.fluid.FluidState getFluidState(BlockPos pos) { return getBlockState(pos).getFluidState(); }
    @Override public boolean testBlockState(BlockPos pos, java.util.function.Predicate<BlockState> predicate) { return predicate.test(getBlockState(pos)); }
    @Override public boolean testFluidState(BlockPos pos, java.util.function.Predicate<net.minecraft.fluid.FluidState> predicate) { return predicate.test(getFluidState(pos)); }
    @Override public boolean setBlockState(BlockPos pos, BlockState state, int flags, int depth) { return setBlock(pos, state, flags); }
    @Override public boolean removeBlock(BlockPos pos, boolean moved) { return setBlock(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3); }
    @Override public boolean breakBlock(BlockPos pos, boolean drop, net.minecraft.entity.Entity source, int depth) {
        return !getBlockState(pos).isAir() && setBlock(pos, net.minecraft.block.Blocks.AIR.getDefaultState(), 3);
    }
    @Override public boolean spawnEntity(net.minecraft.entity.Entity entity) { return world.spawnEntity(entity); }
    @Override public int getBottomY() { return world.getBottomY(); }
    @Override public int getHeight() { return world.getHeight(); }
    @Override public net.minecraft.world.BlockView getChunkAsView(int x, int z) { return world.getChunkAsView(x, z); }
    @Override public int getColor(BlockPos pos, net.minecraft.world.biome.ColorResolver resolver) { return world.getColor(pos, resolver); }
    @Override public long getSeed() { return world.getSeed(); }
    @Override public net.minecraft.server.world.ServerWorld toServerWorld() { return world.toServerWorld(); }
    @Override public net.minecraft.world.WorldProperties getLevelProperties() { return world.getLevelProperties(); }
    @Override public long getTickOrder() { return world.getTickOrder(); }
    @Override public net.minecraft.world.tick.QueryableTickScheduler<net.minecraft.block.Block> getBlockTickScheduler() { return world.getBlockTickScheduler(); }
    @Override public net.minecraft.world.tick.QueryableTickScheduler<net.minecraft.fluid.Fluid> getFluidTickScheduler() { return world.getFluidTickScheduler(); }
    @Override public net.minecraft.world.LocalDifficulty getLocalDifficulty(net.minecraft.util.math.BlockPos p0) { return world.getLocalDifficulty(p0); }
    @Override public net.minecraft.server.MinecraftServer getServer() { return world.getServer(); }
    @Override public net.minecraft.world.chunk.ChunkManager getChunkManager() { return world.getChunkManager(); }
    @Override public net.minecraft.util.math.random.Random getRandom() { return world.getRandom(); }
    @Override public void playSound(net.minecraft.entity.player.PlayerEntity p0, net.minecraft.util.math.BlockPos p1, net.minecraft.sound.SoundEvent p2, net.minecraft.sound.SoundCategory p3, float p4, float p5) { world.playSound(p0, p1, p2, p3, p4, p5); }
    @Override public void addParticle(net.minecraft.particle.ParticleEffect p0, double p1, double p2, double p3, double p4, double p5, double p6) { world.addParticle(p0, p1, p2, p3, p4, p5, p6); }
    @Override public void syncWorldEvent(net.minecraft.entity.player.PlayerEntity p0, int p1, net.minecraft.util.math.BlockPos p2, int p3) { world.syncWorldEvent(p0, p1, p2, p3); }
    @Override public void emitGameEvent(net.minecraft.world.event.GameEvent p0, net.minecraft.util.math.Vec3d p1, net.minecraft.world.event.GameEvent.Emitter p2) { world.emitGameEvent(p0, p1, p2); }
    @Override public java.util.List<? extends net.minecraft.entity.player.PlayerEntity> getPlayers() { return world.getPlayers(); }
    @Override public java.util.List<net.minecraft.entity.Entity> getOtherEntities(net.minecraft.entity.Entity p0, net.minecraft.util.math.Box p1, java.util.function.Predicate<? super net.minecraft.entity.Entity> p2) { return world.getOtherEntities(p0, p1, p2); }
    @Override public <T extends net.minecraft.entity.Entity> java.util.List<T> getEntitiesByType(net.minecraft.util.TypeFilter<net.minecraft.entity.Entity, T> p0, net.minecraft.util.math.Box p1, java.util.function.Predicate<? super T> p2) { return world.getEntitiesByType(p0, p1, p2); }
    @Override public net.minecraft.world.chunk.Chunk getChunk(int p0, int p1, net.minecraft.world.chunk.ChunkStatus p2, boolean p3) { return world.getChunk(p0, p1, p2, p3); }
    @Override public int getTopY(net.minecraft.world.Heightmap.Type p0, int p1, int p2) { return world.getTopY(p0, p1, p2); }
    @Override public int getAmbientDarkness() { return world.getAmbientDarkness(); }
    @Override public net.minecraft.world.biome.source.BiomeAccess getBiomeAccess() { return world.getBiomeAccess(); }
    @Override public net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> getGeneratorStoredBiome(int p0, int p1, int p2) { return world.getGeneratorStoredBiome(p0, p1, p2); }
    @Override public boolean isClient() { return world.isClient(); }
    @Override public int getSeaLevel() { return world.getSeaLevel(); }
    @Override public net.minecraft.world.dimension.DimensionType getDimension() { return world.getDimension(); }
    @Override public net.minecraft.registry.DynamicRegistryManager getRegistryManager() { return world.getRegistryManager(); }
    @Override public net.minecraft.resource.featuretoggle.FeatureSet getEnabledFeatures() { return world.getEnabledFeatures(); }
    @Override public float getBrightness(net.minecraft.util.math.Direction p0, boolean p1) { return world.getBrightness(p0, p1); }
    @Override public net.minecraft.world.chunk.light.LightingProvider getLightingProvider() { return world.getLightingProvider(); }
    @Override public net.minecraft.world.border.WorldBorder getWorldBorder() { return world.getWorldBorder(); }
}
