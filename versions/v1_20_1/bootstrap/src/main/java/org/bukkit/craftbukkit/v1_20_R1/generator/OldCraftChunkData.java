package org.bukkit.craftbukkit.v1_20_R1.generator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.material.MaterialData;

/** In-memory implementation of the pre-1.17 generator ChunkData contract. */
public final class OldCraftChunkData implements ChunkGenerator.ChunkData {
    private final int minHeight;
    private final int maxHeight;
    private final Registry<net.minecraft.world.biome.Biome> biomeRegistry;
    private final ChunkSection[] sections;
    private final Set<BlockPos> tiles = new HashSet<>();
    private final World world;

    public OldCraftChunkData(World world) {
        this(Objects.requireNonNull(world, "world").getMinHeight(), world.getMaxHeight(),
                world instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld craft
                        ? craft.getHandle().getRegistryManager().get(RegistryKeys.BIOME)
                        : null, world);
    }
    @Deprecated
    @io.papermc.paper.annotation.DoNotUse
    public OldCraftChunkData(int minHeight, int maxHeight, Registry<net.minecraft.world.biome.Biome> biomeRegistry) {
        this(minHeight, maxHeight, biomeRegistry, null);
    }
    public OldCraftChunkData(int minHeight, int maxHeight, Registry<net.minecraft.world.biome.Biome> biomeRegistry, World world) {
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.biomeRegistry = biomeRegistry;
        this.world = world;
        int count = Math.max(1, (maxHeight - minHeight + 15) >> 4);
        this.sections = new ChunkSection[count];
    }
    @Override public int getMinHeight() { return minHeight; }
    @Override public int getMaxHeight() { return maxHeight; }
    private int sectionIndex(int y) { return (y - minHeight) >> 4; }
    private ChunkSection section(int y, boolean create) {
        if (y < minHeight || y >= maxHeight) return null;
        int index = sectionIndex(y);
        ChunkSection section = sections[index];
        if (section == null && create) {
            if (biomeRegistry == null) throw new IllegalStateException("Old generator data requires a native CraftWorld");
            section = sections[index] = new ChunkSection(biomeRegistry);
        }
        return section;
    }
    private BlockState state(int x, int y, int z) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16) return net.minecraft.block.Blocks.AIR.getDefaultState();
        ChunkSection section = section(y, false);
        return section == null ? net.minecraft.block.Blocks.AIR.getDefaultState() : section.getBlockState(x, y & 15, z);
    }
    @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) {
        ChunkSection section = section(y, false);
        if (section == null) return org.bukkit.block.Biome.CUSTOM;
        var key = section.getBiome((x >> 2) & 3, (y >> 2) & 3, (z >> 2) & 3).getKey().orElse(null);
        if (key == null) return org.bukkit.block.Biome.CUSTOM;
        var result = org.bukkit.Registry.BIOME.get(new org.bukkit.NamespacedKey(key.getValue().getNamespace(), key.getValue().getPath()));
        return result == null ? org.bukkit.block.Biome.CUSTOM : result;
    }
    @Override public void setBlock(int x, int y, int z, Material material) { setBlock(x, y, z, material.createBlockData()); }
    @Override public void setBlock(int x, int y, int z, MaterialData material) { setBlock(x, y, z, CraftMagicNumbers.getBlock(material)); }
    @Override public void setBlock(int x, int y, int z, BlockData data) { setBlock(x, y, z, dev.meldrite.v1201.compat.NativeBlockData.require(data)); }
    private void setBlock(int x, int y, int z, BlockState state) {
        if (x < 0 || x >= 16 || z < 0 || z >= 16 || y < minHeight || y >= maxHeight) return;
        ChunkSection section = section(y, true);
        section.setBlockState(x, y & 15, z, state);
        BlockPos pos = new BlockPos(x, y, z);
        if (state.getBlock() instanceof BlockEntityProvider) tiles.add(pos); else tiles.remove(pos);
    }
    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, Material material) { setRegion(xMin,yMin,zMin,xMax,yMax,zMax,material.createBlockData()); }
    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, MaterialData material) { setRegion(xMin,yMin,zMin,xMax,yMax,zMax,CraftBlockData.fromData(CraftMagicNumbers.getBlock(material))); }
    @Override public void setRegion(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax, BlockData data) {
        BlockState state = dev.meldrite.v1201.compat.NativeBlockData.require(data);
        for (int y=Math.max(minHeight,yMin); y<Math.min(maxHeight,yMax); y++) for (int x=Math.max(0,xMin); x<Math.min(16,xMax); x++) for (int z=Math.max(0,zMin); z<Math.min(16,zMax); z++) setBlock(x,y,z,state);
    }
    @Override public Material getType(int x, int y, int z) { return CraftMagicNumbers.getMaterial(state(x,y,z).getBlock()); }
    @Override public MaterialData getTypeAndData(int x, int y, int z) { return CraftMagicNumbers.getMaterial(state(x,y,z)); }
    @Override public BlockData getBlockData(int x, int y, int z) { return CraftBlockData.fromData(state(x,y,z)); }
    @Override public byte getData(int x, int y, int z) { return CraftMagicNumbers.toLegacyData(state(x,y,z)); }
    public ChunkSection[] getRawChunkData() { return sections; }
    public Set<BlockPos> getTiles() { return tiles; }
}
