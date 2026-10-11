package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.NativeBlockData;
import dev.meldrite.v1201.compat.NativeLegacyConversion;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.entity.SmokerBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.BrushableBlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.block.entity.SculkShriekerBlockEntity;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EndPortalBlockEntity;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

/** A detached Bukkit snapshot retains the actual native block state, without a world or position. */
public class CraftBlockState implements org.bukkit.block.BlockState {
    protected BlockState state;
    protected final ServerWorld world;
    protected final BlockPos position;
    private final org.bukkit.craftbukkit.v1_20_R1.CraftWorld bukkitWorld;
    protected int flag = 3;
    public CraftBlockState(BlockState state) { this.world = null; this.position = null; this.bukkitWorld = null; this.state = Objects.requireNonNull(state); }
    protected CraftBlockState(org.bukkit.block.Block block) {
        this(((CraftBlock) Objects.requireNonNull(block, "block")).getCraftWorld().getHandle(),
                ((CraftBlock) block).getPosition(), ((CraftBlock) block).getNMS());
    }
    protected CraftBlockState(org.bukkit.block.Block block, int flag) {
        this(block);
        this.flag = flag;
    }
    public CraftBlockState(ServerWorld world, BlockPos position, BlockState state) {
        this.world = world;
        this.position = Objects.requireNonNull(position).toImmutable();
        this.state = Objects.requireNonNull(state);
        this.bukkitWorld = world == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.world(world);
    }
    public BlockState getHandle() { return state; }
    public void setFlag(int flag) { this.flag = flag; }
    public int getFlag() { return flag; }
    public static org.bukkit.block.BlockState create(ServerWorld world, BlockPos position, boolean useSnapshot) {
        BlockState state = world.getBlockState(position);
        BlockEntity blockEntity = world.getBlockEntity(position);
        return create(world, position, state, blockEntity, useSnapshot);
    }
    public static org.bukkit.block.BlockState createUnplaced(BlockState state, BlockEntity blockEntity) {
        return create(null, BlockPos.ORIGIN, state, blockEntity, true);
    }
    public static org.bukkit.block.BlockState createCaptured(ServerWorld world, BlockPos position, BlockState state, BlockEntity blockEntity) {
        return create(Objects.requireNonNull(world), position, state, blockEntity, true);
    }
    public static Class<? extends CraftBlockState> typeOf(BlockState state, BlockEntity blockEntity) {
        return createUnplaced(Objects.requireNonNull(state, "state"), blockEntity).getClass().asSubclass(CraftBlockState.class);
    }
    private static org.bukkit.block.BlockState create(ServerWorld world, BlockPos position, BlockState state, BlockEntity blockEntity, boolean useSnapshot) {
        if (blockEntity instanceof HangingSignBlockEntity sign) return new CraftHangingSign(world, position, state, sign, useSnapshot);
        if (blockEntity instanceof SignBlockEntity sign) return new CraftSign(world, position, state, sign, useSnapshot);
        if (blockEntity instanceof BlastFurnaceBlockEntity furnace) return new CraftBlastFurnace(world, position, state, furnace, useSnapshot);
        if (blockEntity instanceof SmokerBlockEntity furnace) return new CraftSmoker(world, position, state, furnace, useSnapshot);
        if (blockEntity instanceof FurnaceBlockEntity furnace) return new CraftFurnaceFurnace(world, position, state, furnace, useSnapshot);
        if (blockEntity instanceof BrewingStandBlockEntity brewing) return new CraftBrewingStand(world, position, state, brewing, useSnapshot);
        if (blockEntity instanceof BeaconBlockEntity beacon) return new CraftBeacon(world, position, state, beacon, useSnapshot);
        if (blockEntity instanceof BeehiveBlockEntity beehive) return new CraftBeehive(world, position, state, beehive, useSnapshot);
        if (blockEntity instanceof MobSpawnerBlockEntity spawner) return new CraftCreatureSpawner(world, position, state, spawner, useSnapshot);
        if (blockEntity instanceof BannerBlockEntity banner) return new CraftBanner(world, position, state, banner, useSnapshot);
        if (blockEntity instanceof BedBlockEntity bed) return new CraftBed(world, position, state, bed, useSnapshot);
        if (blockEntity instanceof CampfireBlockEntity campfire) return new CraftCampfire(world, position, state, campfire, useSnapshot);
        if (blockEntity instanceof EnchantingTableBlockEntity enchantingTable) return new CraftEnchantingTable(world, position, state, enchantingTable, useSnapshot);
        if (blockEntity instanceof JukeboxBlockEntity jukebox) return new CraftJukebox(world, position, state, jukebox, useSnapshot);
        if (blockEntity instanceof LecternBlockEntity lectern) return new CraftLectern(world, position, state, lectern, useSnapshot);
        if (blockEntity instanceof ChestBlockEntity chest) return new CraftChest(world, position, state, chest, useSnapshot);
        if (blockEntity instanceof BarrelBlockEntity barrel) return new CraftBarrel(world, position, state, barrel, useSnapshot);
        if (blockEntity instanceof ShulkerBoxBlockEntity shulker) return new CraftShulkerBox(world, position, state, shulker, useSnapshot);
        if (blockEntity instanceof EnderChestBlockEntity ender) return new CraftEnderChest(world, position, state, ender, useSnapshot);
        if (blockEntity instanceof DispenserBlockEntity dispenser && !(dispenser instanceof DropperBlockEntity)) return new CraftDispenser(world, position, state, dispenser, useSnapshot);
        if (blockEntity instanceof DropperBlockEntity dropper) return new CraftDropper(world, position, state, dropper, useSnapshot);
        if (blockEntity instanceof HopperBlockEntity hopper) return new CraftHopper(world, position, state, hopper, useSnapshot);
        if (blockEntity instanceof ChiseledBookshelfBlockEntity bookshelf) return new CraftChiseledBookshelf(world, position, state, bookshelf, useSnapshot);
        if (blockEntity instanceof DecoratedPotBlockEntity pot) return new CraftDecoratedPot(world, position, state, pot, useSnapshot);
        if (blockEntity instanceof BrushableBlockEntity brushable) return state.getBlock() == net.minecraft.block.Blocks.SUSPICIOUS_SAND
                ? new CraftSuspiciousSand(world, position, state, brushable, useSnapshot) : new CraftBrushableBlock(world, position, state, brushable, useSnapshot);
        if (blockEntity instanceof CommandBlockBlockEntity command) return new CraftCommandBlock(world, position, state, command, useSnapshot);
        if (blockEntity instanceof BellBlockEntity bell) return new CraftBell(world, position, state, bell, useSnapshot);
        if (blockEntity instanceof CalibratedSculkSensorBlockEntity calibrated) return new CraftCalibratedSculkSensor(world, position, state, calibrated, useSnapshot);
        if (blockEntity instanceof SculkSensorBlockEntity sensor) return new CraftSculkSensor<>(world, position, state, sensor, useSnapshot);
        if (blockEntity instanceof SculkShriekerBlockEntity shrieker) return new CraftSculkShrieker(world, position, state, shrieker, useSnapshot);
        if (blockEntity instanceof SculkCatalystBlockEntity catalyst) return new CraftSculkCatalyst(world, position, state, catalyst, useSnapshot);
        if (blockEntity instanceof EndGatewayBlockEntity gateway) return new CraftEndGateway(world, position, state, gateway, useSnapshot);
        if (blockEntity instanceof EndPortalBlockEntity portal) return new CraftEndPortal(world, position, state, portal, useSnapshot);
        if (blockEntity instanceof ComparatorBlockEntity comparator) return new CraftComparator(world, position, state, comparator, useSnapshot);
        if (blockEntity instanceof ConduitBlockEntity conduit) return new CraftConduit(world, position, state, conduit, useSnapshot);
        if (blockEntity instanceof DaylightDetectorBlockEntity daylight) return new CraftDaylightDetector(world, position, state, daylight, useSnapshot);
        if (blockEntity instanceof PistonBlockEntity moving) return new CraftMovingPiston(world, position, state, moving, useSnapshot);
        if (blockEntity instanceof JigsawBlockEntity jigsaw) return new CraftJigsaw(world, position, state, jigsaw, useSnapshot);
        if (blockEntity instanceof StructureBlockBlockEntity structure) return new CraftStructureBlock(world, position, state, structure, useSnapshot);
        if (blockEntity instanceof LockableContainerBlockEntity container) {
            return new CraftContainer(world, position, state, container, useSnapshot);
        }
        if (blockEntity instanceof SkullBlockEntity skull) return new CraftSkull(world, position, state, skull, useSnapshot);
        if (blockEntity != null) return new CraftBlockEntityState<>(world, position, state, blockEntity, useSnapshot);
        return new CraftBlockState(world, position, state);
    }
    @Override public org.bukkit.block.Block getBlock() { if (!placed()) throw unplaced(); return CraftBlock.at(world, position); }
    @Override public org.bukkit.World getWorld() { if (!placed()) throw unplaced(); return bukkitWorld; }
    @Override public org.bukkit.Chunk getChunk() {
        if (!placed()) throw unplaced();
        return dev.meldrite.v1201.compat.NativeEntityExtensions.world(world).getChunkAt(position.getX() >> 4, position.getZ() >> 4);
    }
    @Override public byte getLightLevel() { return placed() ? getBlock().getLightLevel() : (byte) unplacedInt(); }
    @Override public int getX() { return placed() ? position.getX() : 0; }
    @Override public int getY() { return placed() ? position.getY() : 0; }
    @Override public int getZ() { return placed() ? position.getZ() : 0; }
    @Override public Location getLocation() { return placed() ? new Location(getWorld(), getX(), getY(), getZ()) : new Location(null, 0, 0, 0); }
    @Override public Location getLocation(Location location) {
        if (location == null) return null;
        location.setWorld(placed() ? getWorld() : null); location.setX(getX()); location.setY(getY()); location.setZ(getZ()); location.setYaw(0); location.setPitch(0);
        return location;
    }
    @Override public BlockData getBlockData() { return CraftBlockData.fromData(state); }
    @Override public Material getType() { return getBlockData().getMaterial(); }
    @Override public void setBlockData(BlockData data) { state = NativeBlockData.require(data); }
    @Override public void setType(Material material) {
        Objects.requireNonNull(material, "material");
        if (getType() != material) state = NativeBlockData.block(material).getDefaultState();
    }
    @Override public MaterialData getData() { return NativeLegacyConversion.toLegacyData(state); }
    @Override public void setData(MaterialData data) { state = NativeLegacyConversion.block(Objects.requireNonNull(data).getItemType(), data.getData()); }
    @Override public byte getRawData() { return getData().getData(); }
    @Override public void setRawData(byte data) { state = NativeLegacyConversion.block(getData().getItemType(), data); }
    @Override public boolean isPlaced() { return placed(); }
    @Override public boolean update() { return update(false); }
    @Override public boolean update(boolean force) { return update(force, true); }
    @Override public boolean update(boolean force, boolean applyPhysics) {
        if (!placed()) return true;
        if (!world.getServer().isOnThread()) throw new IllegalStateException("ASYNC_BLOCK_WRITE");
        var current = world.getBlockState(position);
        if (!force && current.getBlock() != state.getBlock()) return false;
        if (current.equals(state)) return true;
        return world.setBlockState(position, state, applyPhysics ? flag : 18 | dev.meldrite.v1201.compat.NativeBlockPlacement.NO_PLACE);
    }
    @Override public boolean isCollidable() { return (boolean) dev.meldrite.v1201.compat.NativeMaterialProjection.property(getType(), "isCollidable", null); }
    @Override public Collection<ItemStack> getDrops() { return placed() ? getBlock().getDrops() : throwUnplaced(); }
    @Override public Collection<ItemStack> getDrops(ItemStack tool) { return placed() ? getBlock().getDrops(tool) : throwUnplaced(); }
    @Override public Collection<ItemStack> getDrops(ItemStack tool, org.bukkit.entity.Entity entity) { return placed() ? getBlock().getDrops(tool, entity) : throwUnplaced(); }
    @Override public void setMetadata(String key, MetadataValue value) { throw unplaced(); }
    @Override public List<MetadataValue> getMetadata(String key) { throw unplaced(); }
    @Override public boolean hasMetadata(String key) { throw unplaced(); }
    @Override public void removeMetadata(String key, Plugin plugin) { throw unplaced(); }
    @Override public boolean equals(Object other) {
        return other instanceof CraftBlockState that && state.equals(that.state);
    }
    @Override public int hashCode() { return state.hashCode(); }
    private boolean placed() { return world != null; }
    private static IllegalStateException unplaced() { return new IllegalStateException("BlockState is not placed in a world"); }
    private static int unplacedInt() { throw unplaced(); }
    private static <T> T throwUnplaced() { throw unplaced(); }
}
