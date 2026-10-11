package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.EndermanEntityPaperAccessMixin;
import net.minecraft.entity.mob.EndermanEntity;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.material.MaterialData;

public final class CraftEnderman extends CraftMonster implements org.bukkit.entity.Enderman {
    private boolean screaming;
    private boolean staredAt;
    public CraftEnderman(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EndermanEntity getHandle() { return (EndermanEntity) super.getHandle(); }
    private EndermanEntityPaperAccessMixin access() { return (EndermanEntityPaperAccessMixin) (Object) getHandle(); }
    @Override public MaterialData getCarriedMaterial() {
        var state = getHandle().getCarriedBlock();
        return state == null ? Material.AIR.getNewData((byte) 0) : CraftMagicNumbers.getMaterial(state);
    }
    @Override public void setCarriedMaterial(MaterialData data) { requireWritable(); getHandle().setCarriedBlock(data == null ? null : CraftMagicNumbers.getBlock(data)); }
    @Override public BlockData getCarriedBlock() {
        var state = getHandle().getCarriedBlock();
        return state == null ? null : CraftBlockData.fromData(state);
    }
    @Override public void setCarriedBlock(BlockData data) {
        requireWritable();
        if (data != null && !(data instanceof CraftBlockData)) throw new IllegalArgumentException("FOREIGN_BLOCK_DATA");
        getHandle().setCarriedBlock(data == null ? null : ((CraftBlockData) data).getState());
    }
    @Override public boolean teleportRandomly() { requireWritable(); return access().meldrite$teleportRandomly(); }
    @Override public boolean teleport() { return teleportRandomly(); }
    @Override public boolean teleportTowards(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE");
        requireWritable();
        return access().meldrite$teleportTo(craft.getHandle());
    }
    @Override public boolean isScreaming() { return screaming || getHandle().isAngry(); }
    @Override public void setScreaming(boolean value) { requireWritable(); screaming = value; if (value) getHandle().setProvoked(); }
    @Override public boolean hasBeenStaredAt() { return staredAt; }
    @Override public void setHasBeenStaredAt(boolean value) { requireWritable(); staredAt = value; }
}
