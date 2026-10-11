package dev.meldrite.paper.facade;
public class BlockAdapter implements org.bukkit.block.Block {
  protected final dev.meldrite.craft.BlockDelegate delegate; protected final FacadeContext context; private final dev.meldrite.paper.registry.MaterialProjection materials;
  public BlockAdapter(dev.meldrite.craft.BlockDelegate delegate,FacadeContext context,dev.meldrite.paper.registry.MaterialProjection materials){this.delegate=java.util.Objects.requireNonNull(delegate);this.context=java.util.Objects.requireNonNull(context);this.materials=java.util.Objects.requireNonNull(materials);}
  private org.bukkit.Material material(){var result=materials.block(delegate.type());if(result instanceof dev.meldrite.registry.ProjectionResult.Exact<org.bukkit.Material> exact)return exact.value();throw new UnsupportedOperationException("UNREPRESENTABLE_BLOCK_TYPE: "+result);}
  @Override public boolean applyBoneMeal(org.bukkit.block.BlockFace p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#applyBoneMeal(Lorg/bukkit/block/BlockFace;)Z#METHOD"); }
  @Override public boolean breakNaturally() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#breakNaturally()Z#METHOD"); }
  @Override public boolean breakNaturally(org.bukkit.inventory.ItemStack p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#breakNaturally(Lorg/bukkit/inventory/ItemStack;)Z#METHOD"); }
  @Override public boolean breakNaturally(org.bukkit.inventory.ItemStack p0, boolean p1, boolean p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#breakNaturally(Lorg/bukkit/inventory/ItemStack;ZZ)Z#METHOD"); }
  @Override public boolean breakNaturally(boolean p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#breakNaturally(ZZ)Z#METHOD"); }
  @Override public boolean canPlace(org.bukkit.block.data.BlockData p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#canPlace(Lorg/bukkit/block/data/BlockData;)Z#METHOD"); }
  @Override public org.bukkit.block.Biome getBiome() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBiome()Lorg/bukkit/block/Biome;#METHOD"); }
  @Override public org.bukkit.block.data.BlockData getBlockData() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBlockData()Lorg/bukkit/block/data/BlockData;#METHOD"); }
  @Override public int getBlockPower() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBlockPower()I#METHOD"); }
  @Override public int getBlockPower(org.bukkit.block.BlockFace p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBlockPower(Lorg/bukkit/block/BlockFace;)I#METHOD"); }
  @Override public org.bukkit.SoundGroup getBlockSoundGroup() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBlockSoundGroup()Lorg/bukkit/SoundGroup;#METHOD"); }
  @Override public org.bukkit.util.BoundingBox getBoundingBox() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBoundingBox()Lorg/bukkit/util/BoundingBox;#METHOD"); }
  @Override public float getBreakSpeed(org.bukkit.entity.Player p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getBreakSpeed(Lorg/bukkit/entity/Player;)F#METHOD"); }
  @Override public org.bukkit.Chunk getChunk() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getChunk()Lorg/bukkit/Chunk;#METHOD"); }
  @Override public org.bukkit.util.VoxelShape getCollisionShape() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getCollisionShape()Lorg/bukkit/util/VoxelShape;#METHOD"); }
  @Override public org.bukkit.block.Biome getComputedBiome() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getComputedBiome()Lorg/bukkit/block/Biome;#METHOD"); }
  @Override public byte getData() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getData()B#METHOD"); }
  @Override public java.util.Collection<org.bukkit.inventory.ItemStack> getDrops() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getDrops()Ljava/util/Collection;#METHOD"); }
  @Override public java.util.Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getDrops(Lorg/bukkit/inventory/ItemStack;)Ljava/util/Collection;#METHOD"); }
  @Override public java.util.Collection<org.bukkit.inventory.ItemStack> getDrops(org.bukkit.inventory.ItemStack p0, org.bukkit.entity.Entity p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getDrops(Lorg/bukkit/inventory/ItemStack;Lorg/bukkit/entity/Entity;)Ljava/util/Collection;#METHOD"); }
  @Override public org.bukkit.block.BlockFace getFace(org.bukkit.block.Block other) { java.util.Objects.requireNonNull(other,"other"); if (!getWorld().getUID().equals(other.getWorld().getUID())) return null; int dx=other.getX()-getX(),dy=other.getY()-getY(),dz=other.getZ()-getZ(); for (var face:org.bukkit.block.BlockFace.values()) if (face.getModX()==dx&&face.getModY()==dy&&face.getModZ()==dz) return face; return null; }
  @Override public double getHumidity() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getHumidity()D#METHOD"); }
  @Override public byte getLightFromBlocks() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getLightFromBlocks()B#METHOD"); }
  @Override public byte getLightFromSky() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getLightFromSky()B#METHOD"); }
  @Override public byte getLightLevel() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getLightLevel()B#METHOD"); }
  @Override public org.bukkit.Location getLocation() { return new org.bukkit.Location(context.world(delegate.reference()),delegate.position().x(),delegate.position().y(),delegate.position().z()); }
  @Override public org.bukkit.Location getLocation(org.bukkit.Location destination) { if(destination==null)return null; destination.setWorld(getWorld()); destination.setX(getX()); destination.setY(getY()); destination.setZ(getZ()); destination.setYaw(0); destination.setPitch(0); return destination; }
  @Override public java.util.List<org.bukkit.metadata.MetadataValue> getMetadata(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getMetadata(Ljava/lang/String;)Ljava/util/List;#INHERITED_METHOD"); }
  @Override public org.bukkit.block.PistonMoveReaction getPistonMoveReaction() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getPistonMoveReaction()Lorg/bukkit/block/PistonMoveReaction;#METHOD"); }
  @Override public org.bukkit.block.Block getRelative(int x, int y, int z) { var point=delegate.position(); return context.block(new dev.meldrite.foundation.BlockPosition(point.world(),point.x()+x,point.y()+y,point.z()+z)); }
  @Override public org.bukkit.block.Block getRelative(org.bukkit.block.BlockFace face) { return getRelative(face,1); }
  @Override public org.bukkit.block.Block getRelative(org.bukkit.block.BlockFace face, int distance) { java.util.Objects.requireNonNull(face,"face"); return getRelative(face.getModX()*distance,face.getModY()*distance,face.getModZ()*distance); }
  @Override public com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getSoundGroup()Lcom/destroystokyo/paper/block/BlockSoundGroup;#METHOD"); }
  @Override public org.bukkit.block.BlockState getState() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getState()Lorg/bukkit/block/BlockState;#METHOD"); }
  @Override public org.bukkit.block.BlockState getState(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getState(Z)Lorg/bukkit/block/BlockState;#METHOD"); }
  @Override public double getTemperature() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#getTemperature()D#METHOD"); }
  @Override public java.lang.String getTranslationKey() { var key=material().getKey(); return "block."+key.getNamespace()+"."+key.getKey(); }
  @Override public org.bukkit.Material getType() { return material(); }
  @Override public org.bukkit.World getWorld() { return context.world(delegate.reference()); }
  @Override public int getX() { return delegate.position().x(); }
  @Override public int getY() { return delegate.position().y(); }
  @Override public int getZ() { return delegate.position().z(); }
  @Override public boolean hasMetadata(java.lang.String p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#hasMetadata(Ljava/lang/String;)Z#INHERITED_METHOD"); }
  @Override public boolean isBlockFaceIndirectlyPowered(org.bukkit.block.BlockFace p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isBlockFaceIndirectlyPowered(Lorg/bukkit/block/BlockFace;)Z#METHOD"); }
  @Override public boolean isBlockFacePowered(org.bukkit.block.BlockFace p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isBlockFacePowered(Lorg/bukkit/block/BlockFace;)Z#METHOD"); }
  @Override public boolean isBlockIndirectlyPowered() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isBlockIndirectlyPowered()Z#METHOD"); }
  @Override public boolean isBlockPowered() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isBlockPowered()Z#METHOD"); }
  @Override public boolean isBuildable() { return material().isSolid(); }
  @Override public boolean isBurnable() { return material().isBurnable(); }
  @Override public boolean isCollidable() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isCollidable()Z#METHOD"); }
  @Override public boolean isEmpty() { return material().isAir(); }
  @Override public boolean isLiquid() { return material()==org.bukkit.Material.WATER||material()==org.bukkit.Material.LAVA; }
  @Override public boolean isPassable() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isPassable()Z#METHOD"); }
  @Override public boolean isPreferredTool(org.bukkit.inventory.ItemStack p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isPreferredTool(Lorg/bukkit/inventory/ItemStack;)Z#METHOD"); }
  @Override public boolean isReplaceable() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isReplaceable()Z#METHOD"); }
  @Override public boolean isSolid() { return material().isSolid(); }
  @Override public boolean isValidTool(org.bukkit.inventory.ItemStack p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#isValidTool(Lorg/bukkit/inventory/ItemStack;)Z#METHOD"); }
  @Override public void randomTick() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#randomTick()V#METHOD"); }
  @Override public org.bukkit.util.RayTraceResult rayTrace(org.bukkit.Location p0, org.bukkit.util.Vector p1, double p2, org.bukkit.FluidCollisionMode p3) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#rayTrace(Lorg/bukkit/Location;Lorg/bukkit/util/Vector;DLorg/bukkit/FluidCollisionMode;)Lorg/bukkit/util/RayTraceResult;#METHOD"); }
  @Override public void removeMetadata(java.lang.String p0, org.bukkit.plugin.Plugin p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#removeMetadata(Ljava/lang/String;Lorg/bukkit/plugin/Plugin;)V#INHERITED_METHOD"); }
  @Override public void setBiome(org.bukkit.block.Biome p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#setBiome(Lorg/bukkit/block/Biome;)V#METHOD"); }
  @Override public void setBlockData(org.bukkit.block.data.BlockData p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#setBlockData(Lorg/bukkit/block/data/BlockData;)V#METHOD"); }
  @Override public void setBlockData(org.bukkit.block.data.BlockData p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#setBlockData(Lorg/bukkit/block/data/BlockData;Z)V#METHOD"); }
  @Override public void setMetadata(java.lang.String p0, org.bukkit.metadata.MetadataValue p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#setMetadata(Ljava/lang/String;Lorg/bukkit/metadata/MetadataValue;)V#INHERITED_METHOD"); }
  @Override public void setType(org.bukkit.Material p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#setType(Lorg/bukkit/Material;)V#METHOD"); }
  @Override public void setType(org.bukkit.Material p0, boolean p1) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#setType(Lorg/bukkit/Material;Z)V#METHOD"); }
  @Override public void tick() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/block/Block#tick()V#METHOD"); }
  @Override public java.lang.String translationKey() { return getTranslationKey(); }
}
