package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.FallingBlockNativeAccess;
import net.minecraft.entity.FallingBlockEntity;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
public final class CraftFallingBlock extends CraftEntity implements org.bukkit.entity.FallingBlock {
    public CraftFallingBlock(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FallingBlockEntity getHandle() { return (FallingBlockEntity) super.getHandle(); }
    private void requireWritable() { delegate.requireWritable(); }
    private FallingBlockNativeAccess state() { return (FallingBlockNativeAccess) (Object) getHandle(); }
    @Override public org.bukkit.Material getMaterial() { return getBlockData().getMaterial(); }
    @Override public BlockData getBlockData() { return CraftBlockData.fromData(getHandle().getBlockState()); }
    @Override public boolean getDropItem() { return getHandle().dropItem; }
    @Override public void setDropItem(boolean drop) { requireWritable(); getHandle().dropItem = drop; }
    @Override public boolean getCancelDrop() { return state().meldrite$getCancelDrop(); }
    @Override public void setCancelDrop(boolean cancelDrop) { requireWritable(); state().meldrite$setCancelDrop(cancelDrop); }
    @Override public boolean canHurtEntities() { return state().meldrite$getHurtEntities(); }
    @Override public void setHurtEntities(boolean hurtEntities) { requireWritable(); state().meldrite$setHurtEntities(hurtEntities); }
    @Override public float getDamagePerBlock() { return state().meldrite$getDamagePerBlock(); }
    @Override public void setDamagePerBlock(float damage) { if (!Float.isFinite(damage) || damage < 0) throw new IllegalArgumentException("damage must be finite and nonnegative"); requireWritable(); state().meldrite$setDamagePerBlock(damage); if (damage > 0) state().meldrite$setHurtEntities(true); }
    @Override public int getMaxDamage() { return state().meldrite$getMaxDamage(); }
    @Override public void setMaxDamage(int damage) { if (damage < 0) throw new IllegalArgumentException("damage must be nonnegative"); requireWritable(); state().meldrite$setMaxDamage(damage); }
    @Override public void setTicksLived(int value) { super.setTicksLived(value); getHandle().timeFalling = value; }
    @Override public boolean doesAutoExpire() { return state().meldrite$doesAutoExpire(); }
    @Override public void shouldAutoExpire(boolean autoExpire) { requireWritable(); state().meldrite$setAutoExpire(autoExpire); }
}
