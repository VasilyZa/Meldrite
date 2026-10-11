package org.bukkit.craftbukkit.v1_20_R1.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.util.BlockVector;
import org.bukkit.block.structure.*;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftBlockVector;

public class CraftStructureBlock extends CraftBlockEntityState<StructureBlockBlockEntity> implements org.bukkit.block.Structure {
    public CraftStructureBlock(ServerWorld world, BlockPos pos, BlockState state, StructureBlockBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public String getStructureName() { return snapshot.getTemplateName(); }
    @Override public void setStructureName(String value) { snapshot.setTemplateName(java.util.Objects.requireNonNull(value, "name")); }
    @Override public String getAuthor() { return snapshot.author; }
    @Override public void setAuthor(String value) { if (value == null || value.isEmpty()) throw new IllegalArgumentException("Author must not be empty"); snapshot.author = value; }
    @Override public void setAuthor(org.bukkit.entity.LivingEntity entity) { snapshot.setAuthor(((org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity) java.util.Objects.requireNonNull(entity)).getHandle()); }
    @Override public BlockVector getRelativePosition() { return CraftBlockVector.toBukkit(snapshot.getOffset()); }
    @Override public void setRelativePosition(BlockVector vector) { validate(vector, -48, 48); snapshot.setOffset(CraftBlockVector.toBlockPosition(vector)); }
    @Override public BlockVector getStructureSize() { return CraftBlockVector.toBukkit(snapshot.getSize()); }
    @Override public void setStructureSize(BlockVector vector) { validate(vector, 0, 48); snapshot.setSize(CraftBlockVector.toBlockPosition(vector)); }
    @Override public Mirror getMirror() { return Mirror.valueOf(snapshot.getMirror().name()); }
    @Override public void setMirror(Mirror value) { snapshot.setMirror(net.minecraft.util.BlockMirror.valueOf(java.util.Objects.requireNonNull(value).name())); }
    @Override public StructureRotation getRotation() { return StructureRotation.valueOf(snapshot.getRotation().name()); }
    @Override public void setRotation(StructureRotation value) { snapshot.setRotation(net.minecraft.util.BlockRotation.valueOf(java.util.Objects.requireNonNull(value).name())); }
    @Override public UsageMode getUsageMode() { return UsageMode.valueOf(snapshot.getMode().name()); }
    @Override public void setUsageMode(UsageMode value) {
        var mode = net.minecraft.block.enums.StructureBlockMode.valueOf(java.util.Objects.requireNonNull(value).name());
        if (isSnapshot()) {
            var nbt = snapshot.createNbt();
            nbt.putString("mode", mode.name());
            snapshot.readNbt(nbt);
        } else snapshot.setMode(mode);
        state = state.with(net.minecraft.block.StructureBlock.MODE, mode);
    }
    @Override public boolean isIgnoreEntities() { return snapshot.shouldIgnoreEntities(); }
    @Override public void setIgnoreEntities(boolean value) { snapshot.setIgnoreEntities(value); }
    @Override public boolean isShowAir() { return snapshot.shouldShowAir(); }
    @Override public void setShowAir(boolean value) { snapshot.setShowAir(value); }
    @Override public boolean isBoundingBoxVisible() { return snapshot.shouldShowBoundingBox(); }
    @Override public void setBoundingBoxVisible(boolean value) { snapshot.setShowBoundingBox(value); }
    @Override public float getIntegrity() { return snapshot.getIntegrity(); }
    @Override public void setIntegrity(float value) { if (!Float.isFinite(value) || value < 0 || value > 1) throw new IllegalArgumentException("Integrity must be between 0 and 1"); snapshot.setIntegrity(value); }
    @Override public long getSeed() { return snapshot.getSeed(); }
    @Override public void setSeed(long value) { snapshot.setSeed(value); }
    @Override public String getMetadata() { return snapshot.getMetadata(); }
    @Override public void setMetadata(String value) { java.util.Objects.requireNonNull(value, "metadata"); if (getUsageMode() == UsageMode.DATA) snapshot.setMetadata(value); }
    private static void validate(BlockVector vector, int min, int max) {
        java.util.Objects.requireNonNull(vector, "vector");
        if (vector.getBlockX() < min || vector.getBlockX() > max || vector.getBlockY() < min || vector.getBlockY() > max || vector.getBlockZ() < min || vector.getBlockZ() > max) throw new IllegalArgumentException("Structure vector must be between " + min + " and " + max);
    }
}
