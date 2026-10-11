package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.craft.BlockDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeWorldPort;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;

/** A cached Bukkit block is a coordinate view; every state read uses its live native world. */
public class CraftBlock extends dev.meldrite.paper.facade.BlockAdapter {
    private static final dev.meldrite.paper.registry.MaterialProjection MATERIALS = new dev.meldrite.paper.registry.MaterialProjection();
    private final BlockPos position;
    public CraftBlock(BlockDelegate delegate, FacadeContext context, NativeWorldPort worlds) {
        super(delegate, context, MATERIALS);
        java.util.Objects.requireNonNull(worlds);
        this.position = new BlockPos(delegate.position().x(), delegate.position().y(), delegate.position().z());
    }
    public WorldAccess getHandle() { return getWorld().getHandle(); }
    private ServerWorld world() { return (ServerWorld) getHandle(); }
    public BlockPos getPosition() { return position; }
    public BlockState getNMS() { return getHandle().getBlockState(position); }
    public static CraftBlock at(WorldAccess world, BlockPos position) {
        if (!(world instanceof ServerWorld serverWorld)) throw new IllegalArgumentException("Block requires a live server world");
        return (CraftBlock) dev.meldrite.v1201.compat.NativeEntityExtensions.world(serverWorld)
                .getBlockAt(position.getX(), position.getY(), position.getZ());
    }
    @Override public org.bukkit.craftbukkit.v1_20_R1.CraftWorld getWorld() {
        return (org.bukkit.craftbukkit.v1_20_R1.CraftWorld) super.getWorld();
    }
    @Override public org.bukkit.Chunk getChunk() { return getWorld().getChunkAt(getX() >> 4, getZ() >> 4); }
    public org.bukkit.craftbukkit.v1_20_R1.CraftWorld getCraftWorld() { return getWorld(); }
    public org.bukkit.util.BlockVector getVector() { return new org.bukkit.util.BlockVector(getX(), getY(), getZ()); }
    @Override public Material getType() { return CraftMagicNumbers.getMaterial(getNMS().getBlock()); }
    @Override public byte getData() { return CraftMagicNumbers.toLegacyData(getNMS()); }
    @Override public Location getLocation(Location destination) {
        if (destination == null) return null;
        destination.setWorld(getWorld()); destination.setX(getX()); destination.setY(getY()); destination.setZ(getZ());
        destination.setYaw(0); destination.setPitch(0); return destination;
    }
    @Override public CraftBlock getRelative(int x, int y, int z) { return (CraftBlock) getWorld().getBlockAt(getX() + x, getY() + y, getZ() + z); }
    @Override public CraftBlock getRelative(BlockFace face) { return getRelative(face, 1); }
    @Override public CraftBlock getRelative(BlockFace face, int distance) {
        java.util.Objects.requireNonNull(face); return getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }
    @Override public org.bukkit.block.BlockState getState() { return CraftBlockState.create(world(), position, true); }
    @Override public org.bukkit.block.BlockState getState(boolean useSnapshot) { return CraftBlockState.create(world(), position, useSnapshot); }
    @Override public org.bukkit.block.Biome getBiome() {
        var entry = world().getBiome(position);
        var key = entry.getKey().orElse(null);
        if (key == null) return org.bukkit.block.Biome.CUSTOM;
        var biome = org.bukkit.Registry.BIOME.get(new org.bukkit.NamespacedKey(key.getValue().getNamespace(), key.getValue().getPath()));
        return biome == null ? org.bukkit.block.Biome.CUSTOM : biome;
    }
    @Override public org.bukkit.block.Biome getComputedBiome() { return getBiome(); }
    @Override public double getTemperature() { return world().getBiome(position).value().getTemperature(); }
    @Override public double getHumidity() {
        return dev.meldrite.v1201.compat.NativeBiomeValues.downfall(world().getBiome(position).value());
    }
    @Override public org.bukkit.util.VoxelShape getCollisionShape() { return new org.bukkit.craftbukkit.v1_20_R1.util.CraftVoxelShape(getNMS().getCollisionShape(world(), position)); }
    @Override public org.bukkit.SoundGroup getBlockSoundGroup() { return org.bukkit.craftbukkit.v1_20_R1.CraftSoundGroup.getSoundGroup(getNMS().getBlock().getSoundGroup(getNMS())); }
    @Override public com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup() { return new org.bukkit.craftbukkit.v1_20_R1.CraftBlockSoundGroup(getNMS().getBlock().getSoundGroup(getNMS())); }
    @Override public BlockFace getFace(org.bukkit.block.Block other) {
        java.util.Objects.requireNonNull(other);
        if (other.getWorld() != getWorld()) return null;
        for (BlockFace face : BlockFace.values()) if (getX() + face.getModX() == other.getX()
                && getY() + face.getModY() == other.getY() && getZ() + face.getModZ() == other.getZ()) return face;
        return null;
    }
    @Override public void setType(Material material) { setType(material, true); }
    @Override public void setType(Material material, boolean physics) {
        var nativeBlock = CraftMagicNumbers.getBlock(java.util.Objects.requireNonNull(material));
        if (nativeBlock == null) throw new IllegalArgumentException("Material is not a block: " + material);
        requireWritable();
        var old = getNMS();
        if (old.hasBlockEntity() && old.getBlock() != nativeBlock) world().removeBlockEntity(position);
        world().setBlockState(position, nativeBlock.getDefaultState(), physics ? 3 : 18 | dev.meldrite.v1201.compat.NativeBlockPlacement.NO_PLACE);
    }
    @Override public org.bukkit.block.data.BlockData getBlockData() {
        return org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData.fromData(getNMS());
    }
    @Override public void setBlockData(org.bukkit.block.data.BlockData data) { setBlockData(data, true); }
    @Override public void setBlockData(org.bukkit.block.data.BlockData data, boolean physics) {
        java.util.Objects.requireNonNull(data, "data");
        requireWritable();
        var old = getNMS();
        var next = dev.meldrite.v1201.compat.NativeBlockData.require(data);
        if (old.hasBlockEntity() && old.getBlock() != next.getBlock()) world().removeBlockEntity(position);
        world().setBlockState(position, next, physics ? 3 : 18 | dev.meldrite.v1201.compat.NativeBlockPlacement.NO_PLACE);
    }
    @Override public boolean canPlace(org.bukkit.block.data.BlockData data) {
        java.util.Objects.requireNonNull(data, "data");
        return dev.meldrite.v1201.compat.NativeBlockData.require(data).canPlaceAt(world(), position);
    }
    private void requireWritable() { if (!context.primaryThread()) throw new IllegalStateException("ASYNC_BLOCK_WRITE"); }
    @Override public boolean isEmpty() { return getNMS().isAir(); }
    @Override public boolean isLiquid() { return getNMS().isLiquid(); }
    @Override public boolean isBuildable() { return getNMS().isSolid(); }
    @Override public boolean isSolid() { return getNMS().blocksMovement(); }
    @Override public boolean isBurnable() { return getNMS().isBurnable(); }
    @Override public boolean isReplaceable() { return getNMS().isReplaceable(); }
    @Override public boolean isCollidable() { return ((dev.meldrite.v1201.hook.AbstractBlockPaperAccessMixin) getNMS().getBlock()).meldrite$isCollidable(); }
    @Override public boolean isPassable() { return getNMS().getCollisionShape(world(), position).isEmpty(); }
    @Override public byte getLightLevel() { return (byte) world().getLightLevel(position); }
    @Override public byte getLightFromSky() { return (byte) world().getLightLevel(net.minecraft.world.LightType.SKY, position); }
    @Override public byte getLightFromBlocks() { return (byte) world().getLightLevel(net.minecraft.world.LightType.BLOCK, position); }
    @Override public String getTranslationKey() { return getNMS().getBlock().getTranslationKey(); }
    @Override public String translationKey() { return getTranslationKey(); }
    @Override public org.bukkit.block.PistonMoveReaction getPistonMoveReaction() { return org.bukkit.block.PistonMoveReaction.getById(getNMS().getPistonBehavior().ordinal()); }
    @Override public org.bukkit.util.BoundingBox getBoundingBox() {
        var shape = getNMS().getOutlineShape(world(), position);
        if (shape.isEmpty()) return new org.bukkit.util.BoundingBox();
        var box = shape.getBoundingBox();
        return new org.bukkit.util.BoundingBox(getX() + box.minX, getY() + box.minY, getZ() + box.minZ,
                getX() + box.maxX, getY() + box.maxY, getZ() + box.maxZ);
    }
    @Override public boolean isPreferredTool(org.bukkit.inventory.ItemStack item) {
        var tool = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item);
        return !getNMS().isToolRequired() || tool.isSuitableFor(getNMS());
    }
    @Override public java.util.Collection<org.bukkit.inventory.ItemStack> getDrops() { return getDrops(null); }
    @Override public java.util.Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack tool) { return getDrops(tool, null); }
    @Override public java.util.Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack tool, org.bukkit.entity.Entity entity) {
        var nativeTool = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(tool);
        var nativeEntity = entity == null ? null : ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity) entity).getHandle();
        if (tool != null && !isPreferredTool(tool)) return java.util.List.of();
        return net.minecraft.block.Block.getDroppedStacks(getNMS(), world(), position, world().getBlockEntity(position), nativeEntity, nativeTool)
                .stream().map(stack -> (org.bukkit.inventory.ItemStack) org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(stack)).toList();
    }
    @Override public boolean breakNaturally() { return breakNaturally(null, true, false); }
    @Override public boolean breakNaturally(boolean triggerEffect, boolean force) { return breakNaturally(null, triggerEffect, force); }
    @Override public boolean breakNaturally(org.bukkit.inventory.ItemStack tool) { return breakNaturally(tool, true, false); }
    @Override public boolean breakNaturally(org.bukkit.inventory.ItemStack tool, boolean triggerEffect, boolean force) {
        requireWritable();
        var state = getNMS();
        if (state.isAir()) return false;
        if (tool == null || tool.getType().isAir()) return world().breakBlock(position, triggerEffect, null, 512);
        var nativeTool = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(tool);
        if (!force && !nativeTool.isSuitableFor(state)) return false;
        var blockEntity = world().getBlockEntity(position);
        var drops = net.minecraft.block.Block.getDroppedStacks(state, world(), position, blockEntity, null, nativeTool);
        if (!world().breakBlock(position, false, null, 512)) return false;
        if (triggerEffect) for (var drop : drops) net.minecraft.block.Block.dropStack(world(), position, drop);
        return true;
    }
    @Override public boolean applyBoneMeal(BlockFace face) {
        java.util.Objects.requireNonNull(face, "face");
        requireWritable();
        var direction = direction(face);
        var hit = new net.minecraft.util.hit.BlockHitResult(
                net.minecraft.util.math.Vec3d.ofCenter(position), direction, position, false);
        var context = dev.meldrite.v1201.hook.ItemUsageContextPaperAccessMixin.meldrite$create(
                world(), null, net.minecraft.util.Hand.MAIN_HAND,
                net.minecraft.item.Items.BONE_MEAL.getDefaultStack(), hit);
        return ((net.minecraft.item.BoneMealItem) net.minecraft.item.Items.BONE_MEAL).useOnBlock(context).isAccepted();
    }
    @Override public org.bukkit.util.RayTraceResult rayTrace(org.bukkit.Location start, org.bukkit.util.Vector direction, double maxDistance, org.bukkit.FluidCollisionMode fluid) {
        if (start == null || start.getWorld() != getWorld()) throw new IllegalArgumentException("Ray start must be in this world");
        return getWorld().rayTraceBlocks(start, direction, maxDistance, fluid, false);
    }
    @Override public boolean isValidTool(org.bukkit.inventory.ItemStack tool) { return !getDrops(tool).isEmpty(); }
    @Override public boolean isBlockPowered() { return world().getReceivedStrongRedstonePower(position) > 0; }
    @Override public boolean isBlockIndirectlyPowered() { return world().isReceivingRedstonePower(position); }
    @Override public boolean isBlockFacePowered(BlockFace face) { return world().isEmittingRedstonePower(position, direction(face)); }
    @Override public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
        var side = direction(face);
        int power = world().getEmittedRedstonePower(position, side);
        var neighbor = world().getBlockState(position.offset(side));
        if (neighbor.isOf(net.minecraft.block.Blocks.REDSTONE_WIRE)) power = Math.max(power, neighbor.get(net.minecraft.block.RedstoneWireBlock.POWER));
        return power > 0;
    }
    @Override public int getBlockPower() { return getBlockPower(BlockFace.SELF); }
    @Override public int getBlockPower(BlockFace face) {
        java.util.Objects.requireNonNull(face);
        int power = 0;
        for (Direction direction : Direction.values()) {
            if (face != BlockFace.SELF && direction != direction(face)) continue;
            BlockPos neighbor = position.offset(direction);
            if (!world().isEmittingRedstonePower(neighbor, direction)) continue;
            var state = world().getBlockState(neighbor);
            if (state.isOf(net.minecraft.block.Blocks.REDSTONE_WIRE)) power = Math.max(power, state.get(net.minecraft.block.RedstoneWireBlock.POWER));
        }
        return power > 0 ? power : (face == BlockFace.SELF ? isBlockIndirectlyPowered() : isBlockFaceIndirectlyPowered(face)) ? 15 : 0;
    }
    public static Direction blockFaceToNotch(BlockFace face) { return direction(face); }
    public static BlockFace notchToBlockFace(Direction direction) { return BlockFace.valueOf(java.util.Objects.requireNonNull(direction).name()); }
    private static Direction direction(BlockFace face) {
        return switch (java.util.Objects.requireNonNull(face)) {
            case NORTH -> Direction.NORTH; case SOUTH -> Direction.SOUTH; case EAST -> Direction.EAST;
            case WEST -> Direction.WEST; case UP -> Direction.UP; case DOWN -> Direction.DOWN;
            default -> throw new IllegalArgumentException("Not a native block face: " + face);
        };
    }
    @Override public void tick() { requireWritable(); getNMS().scheduledTick(world(), position, world().random); }
    @Override public void randomTick() { requireWritable(); getNMS().randomTick(world(), position, world().random); }
}
