package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.Objects;
import java.util.function.BiFunction;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Material;

/** Central block-state factory entry points retained for Paper/CraftBukkit ABI. */
public final class CraftBlockStates {
    private CraftBlockStates() { }
    public abstract static class BlockStateFactory<B extends CraftBlockState> {
        public final Class<B> blockStateType;
        protected BlockStateFactory(Class<B> blockStateType) { this.blockStateType = Objects.requireNonNull(blockStateType); }
        public abstract B createBlockState(ServerWorld world, BlockPos position, net.minecraft.block.BlockState state, BlockEntity tileEntity);
        public final B createBlockState(org.bukkit.World world, BlockPos position, net.minecraft.block.BlockState state, BlockEntity tileEntity) {
            ServerWorld nativeWorld = world == null ? null : ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) world).getHandle();
            return createBlockState(nativeWorld, position, state, tileEntity);
        }
    }
    public static class BlockEntityStateFactory<T extends BlockEntity, B extends CraftBlockEntityState<T>> extends BlockStateFactory<B> {
        private final BiFunction<ServerWorld, T, B> constructor;
        private final BiFunction<BlockPos, net.minecraft.block.BlockState, T> tileEntityConstructor;
        public BlockEntityStateFactory(Class<B> type, BiFunction<ServerWorld, T, B> constructor) {
            super(type);
            this.constructor = Objects.requireNonNull(constructor, "constructor");
            this.tileEntityConstructor = null;
        }
        public BlockEntityStateFactory(Class<B> type, BiFunction<org.bukkit.World, T, B> constructor,
                BiFunction<BlockPos, net.minecraft.block.BlockState, T> tileEntityConstructor) {
            super(type);
            this.constructor = (world, tile) -> constructor.apply(world == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.world(world), tile);
            this.tileEntityConstructor = Objects.requireNonNull(tileEntityConstructor, "tileEntityConstructor");
        }
        @SuppressWarnings("unchecked") @Override public B createBlockState(ServerWorld world, BlockPos position, net.minecraft.block.BlockState state, BlockEntity tileEntity) {
            T resolved = (T) tileEntity;
            if (resolved == null && world == null && tileEntityConstructor != null) resolved = tileEntityConstructor.apply(position, state);
            return constructor.apply(world, Objects.requireNonNull(resolved, "tileEntity"));
        }
    }
    public static Class<? extends CraftBlockState> getBlockStateType(Material material) {
        net.minecraft.block.BlockState state = org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.getBlock(Objects.requireNonNull(material)).getDefaultState();
        BlockEntity entity = state.getBlock() instanceof BlockEntityProvider provider ? provider.createBlockEntity(BlockPos.ORIGIN, state) : null;
        return CraftBlockState.typeOf(state, entity);
    }
    public static Class<? extends CraftBlockState> getBlockStateType(BlockEntityType<?> type) {
        Objects.requireNonNull(type, "blockEntityType");
        if (type == BlockEntityType.SIGN) return CraftSign.class;
        if (type == BlockEntityType.HANGING_SIGN) return CraftHangingSign.class;
        if (type == BlockEntityType.SKULL) return CraftSkull.class;
        if (type == BlockEntityType.COMMAND_BLOCK) return CraftCommandBlock.class;
        if (type == BlockEntityType.BANNER) return CraftBanner.class;
        if (type == BlockEntityType.SHULKER_BOX) return CraftShulkerBox.class;
        if (type == BlockEntityType.BED) return CraftBed.class;
        if (type == BlockEntityType.BEEHIVE) return CraftBeehive.class;
        if (type == BlockEntityType.CAMPFIRE) return CraftCampfire.class;
        if (type == BlockEntityType.BARREL) return CraftBarrel.class;
        if (type == BlockEntityType.BEACON) return CraftBeacon.class;
        if (type == BlockEntityType.BELL) return CraftBell.class;
        if (type == BlockEntityType.BLAST_FURNACE) return CraftBlastFurnace.class;
        if (type == BlockEntityType.BREWING_STAND) return CraftBrewingStand.class;
        if (type == BlockEntityType.CHEST || type == BlockEntityType.TRAPPED_CHEST) return CraftChest.class;
        if (type == BlockEntityType.CHISELED_BOOKSHELF) return CraftChiseledBookshelf.class;
        if (type == BlockEntityType.COMPARATOR) return CraftComparator.class;
        if (type == BlockEntityType.CONDUIT) return CraftConduit.class;
        if (type == BlockEntityType.DAYLIGHT_DETECTOR) return CraftDaylightDetector.class;
        if (type == BlockEntityType.DECORATED_POT) return CraftDecoratedPot.class;
        if (type == BlockEntityType.DISPENSER) return CraftDispenser.class;
        if (type == BlockEntityType.DROPPER) return CraftDropper.class;
        if (type == BlockEntityType.ENCHANTING_TABLE) return CraftEnchantingTable.class;
        if (type == BlockEntityType.ENDER_CHEST) return CraftEnderChest.class;
        if (type == BlockEntityType.END_GATEWAY) return CraftEndGateway.class;
        if (type == BlockEntityType.END_PORTAL) return CraftEndPortal.class;
        if (type == BlockEntityType.FURNACE) return CraftFurnaceFurnace.class;
        if (type == BlockEntityType.HOPPER) return CraftHopper.class;
        if (type == BlockEntityType.JIGSAW) return CraftJigsaw.class;
        if (type == BlockEntityType.JUKEBOX) return CraftJukebox.class;
        if (type == BlockEntityType.LECTERN) return CraftLectern.class;
        if (type == BlockEntityType.PISTON) return CraftMovingPiston.class;
        if (type == BlockEntityType.SCULK_CATALYST) return CraftSculkCatalyst.class;
        if (type == BlockEntityType.SCULK_SENSOR) return CraftSculkSensor.class;
        if (type == BlockEntityType.SCULK_SHRIEKER) return CraftSculkShrieker.class;
        if (type == BlockEntityType.CALIBRATED_SCULK_SENSOR) return CraftCalibratedSculkSensor.class;
        if (type == BlockEntityType.SMOKER) return CraftSmoker.class;
        if (type == BlockEntityType.MOB_SPAWNER) return CraftCreatureSpawner.class;
        if (type == BlockEntityType.STRUCTURE_BLOCK) return CraftStructureBlock.class;
        if (type == BlockEntityType.BRUSHABLE_BLOCK) return CraftBrushableBlock.class;
        return CraftBlockEntityState.class;
    }
    public static BlockEntity createNewTileEntity(Material material) {
        net.minecraft.block.BlockState state = org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.getBlock(Objects.requireNonNull(material)).getDefaultState();
        return state.getBlock() instanceof BlockEntityProvider provider ? provider.createBlockEntity(BlockPos.ORIGIN, state) : null;
    }
    public static org.bukkit.block.BlockState getBlockState(org.bukkit.block.Block block) { return getBlockState(block, true); }
    public static org.bukkit.block.BlockState getBlockState(org.bukkit.block.Block block, boolean useSnapshot) {
        CraftBlock craft = (CraftBlock) Objects.requireNonNull(block, "block");
        return CraftBlockState.create(craft.getCraftWorld().getHandle(), craft.getPosition(), useSnapshot);
    }
    public static org.bukkit.block.BlockState getBlockState(Material material, NbtCompound tag) { return getBlockState(BlockPos.ORIGIN, material, tag); }
    public static org.bukkit.block.BlockState getBlockState(BlockPos position, Material material, NbtCompound tag) {
        net.minecraft.block.BlockState state = org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.getBlock(Objects.requireNonNull(material)).getDefaultState();
        BlockEntity entity = blockEntity(position, state, tag);
        return CraftBlockState.createUnplaced(state, entity);
    }
    public static org.bukkit.block.BlockState getBlockState(net.minecraft.block.BlockState state, NbtCompound tag) {
        return getBlockState(BlockPos.ORIGIN, state, tag);
    }
    public static org.bukkit.block.BlockState getBlockState(BlockPos position, net.minecraft.block.BlockState state, NbtCompound tag) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(state, "state");
        BlockEntity entity = blockEntity(position, state, tag);
        return CraftBlockState.createUnplaced(state, entity);
    }
    public static org.bukkit.block.BlockState getUnplacedBlockState(ServerWorld world, BlockPos position, net.minecraft.block.BlockState state) {
        return getUnplacedBlockState((net.minecraft.world.BlockView) world, position, state);
    }
    public static org.bukkit.block.BlockState getUnplacedBlockState(net.minecraft.world.BlockView world, BlockPos position, net.minecraft.block.BlockState state) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(state, "state");
        BlockEntity entity = world.getBlockEntity(position);
        if (entity == null) entity = blockEntity(position, state, null);
        return CraftBlockState.createUnplaced(state, entity);
    }
    private static BlockEntity blockEntity(BlockPos position, net.minecraft.block.BlockState state, NbtCompound tag) {
        if (tag != null) return BlockEntity.createFromNbt(position, state, tag);
        return state.getBlock() instanceof BlockEntityProvider provider ? provider.createBlockEntity(position, state) : null;
    }
}
