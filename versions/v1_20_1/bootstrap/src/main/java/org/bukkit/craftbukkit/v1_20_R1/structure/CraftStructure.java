package org.bukkit.craftbukkit.v1_20_R1.structure;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.compat.NativeStructureAccess;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.util.RandomSourceWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.structure.Palette;
import org.bukkit.util.BlockVector;

public class CraftStructure implements org.bukkit.structure.Structure {
    private final StructureTemplate structure;
    public CraftStructure(StructureTemplate structure) { this.structure = Objects.requireNonNull(structure); }
    public StructureTemplate getHandle() { return structure; }
    private NativeStructureAccess access() { return (NativeStructureAccess) structure; }
    @Override public BlockVector getSize() { var size = structure.getSize(); return new BlockVector(size.getX(), size.getY(), size.getZ()); }
    @Override public List<Palette> getPalettes() { return access().meldrite$palettes().stream().map(palette -> (Palette) new CraftPalette(palette)).toList(); }
    @Override public int getPaletteCount() { return access().meldrite$palettes().size(); }
    @Override public int getEntityCount() { return access().meldrite$entities().size(); }
    @Override public List<Entity> getEntities() {
        var world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        var result = new java.util.ArrayList<Entity>();
        for (var info : access().meldrite$entities()) EntityType.getEntityFromNbt(info.nbt.copy(), world).ifPresent(entity -> {
            entity.setPosition(info.pos); result.add(NativeEntityExtensions.bukkit(entity));
        });
        return List.copyOf(result);
    }
    @Override public PersistentDataContainer getPersistentDataContainer() { return access().meldrite$persistentData(); }
    private static CraftWorld world(RegionAccessor region) { if (!(region instanceof CraftWorld world)) throw new IllegalArgumentException("Native world required"); world.requireWritable(); return world; }
    private static BlockPos pos(Location location) { Objects.requireNonNull(location).checkFinite(); return BlockPos.ofFloored(location.getX(), location.getY(), location.getZ()); }
    @Override public void place(Location location, boolean entities, StructureRotation rotation, Mirror mirror, int palette, float integrity, Random random) {
        pos(location); place(Objects.requireNonNull(location.getWorld()), new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), entities, rotation, mirror, palette, integrity, random);
    }
    @Override public void place(RegionAccessor region, BlockVector location, boolean entities, StructureRotation rotation, Mirror mirror, int palette, float integrity, Random random) {
        Objects.requireNonNull(location).checkFinite(); Objects.requireNonNull(rotation); Objects.requireNonNull(mirror); Objects.requireNonNull(random);
        if (!Float.isFinite(integrity) || integrity < 0 || integrity > 1 || palette < -1 || palette >= getPaletteCount()) throw new IllegalArgumentException("Invalid integrity or palette");
        var nativeWorld = world(region).getHandle(); var position = BlockPos.ofFloored(location.getX(), location.getY(), location.getZ());
        var source = new RandomSourceWrapper(random);
        var settings = new StructurePlacementData() {
            @Override public StructureTemplate.PalettedBlockInfoList getRandomBlockInfos(List<StructureTemplate.PalettedBlockInfoList> palettes, BlockPos origin) {
                return palette < 0 ? super.getRandomBlockInfos(palettes, origin) : palettes.get(palette);
            }
        }.setMirror(BlockMirror.valueOf(mirror.name())).setRotation(BlockRotation.valueOf(rotation.name()))
                .setIgnoreEntities(!entities).setRandom(source).addProcessor(new BlockRotStructureProcessor(integrity));
        structure.place(nativeWorld, position, position, settings, source, 2);
    }
    @Override public void fill(Location first, Location second, boolean entities) {
        pos(first); pos(second);
        if (!Objects.equals(first.getWorld(), second.getWorld())) throw new IllegalArgumentException("Corners must be in the same world");
        var origin = new Location(first.getWorld(), Math.min(first.getBlockX(), second.getBlockX()), Math.min(first.getBlockY(), second.getBlockY()), Math.min(first.getBlockZ(), second.getBlockZ()));
        fill(origin, new BlockVector(Math.abs(first.getBlockX() - second.getBlockX()), Math.abs(first.getBlockY() - second.getBlockY()), Math.abs(first.getBlockZ() - second.getBlockZ())), entities);
    }
    @Override public void fill(Location origin, BlockVector size, boolean entities) {
        var position = pos(origin); Objects.requireNonNull(size).checkFinite();
        if (size.getBlockX() < 1 || size.getBlockY() < 1 || size.getBlockZ() < 1) throw new IllegalArgumentException("Size must be at least 1x1x1");
        structure.saveFromWorld(world(origin.getWorld()).getHandle(), position, new BlockPos(size.getBlockX(), size.getBlockY(), size.getBlockZ()), entities, Blocks.STRUCTURE_VOID);
    }
}
