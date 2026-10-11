package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.LightningEntityPaperAccessMixin;
public class CraftLightningStrike extends CraftEntity implements org.bukkit.entity.LightningStrike {
    public CraftLightningStrike(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.LightningEntity getHandle() { return (net.minecraft.entity.LightningEntity) super.getHandle(); }
    private LightningEntityPaperAccessMixin access() { return (LightningEntityPaperAccessMixin) getHandle(); }
    @Override public boolean isEffect() { return access().meldrite$isCosmetic(); }
    @Override public int getFlashCount() { return access().meldrite$getRemainingActions(); }
    @Override public void setFlashCount(int value) { if (value < 0) throw new IllegalArgumentException("Negative flash count"); delegate.requireWritable(); access().meldrite$setRemainingActions(value); }
    @Override public int getLifeTicks() { return access().meldrite$getAmbientTick(); }
    @Override public void setLifeTicks(int value) { delegate.requireWritable(); access().meldrite$setAmbientTick(value); }
    @Override public org.bukkit.entity.Entity getCausingEntity() { var owner = getHandle().getChanneler(); return owner == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(owner); }
    @Override public void setCausingPlayer(org.bukkit.entity.Player value) { delegate.requireWritable(); getHandle().setChanneler(value == null ? null : ((CraftPlayer) value).getHandle()); }
    private final org.bukkit.entity.LightningStrike.Spigot spigot = new org.bukkit.entity.LightningStrike.Spigot() { @Override public boolean isSilent() { return CraftLightningStrike.this.getHandle().isSilent(); } };
    @Override public org.bukkit.entity.LightningStrike.Spigot spigot() { return spigot; }
}
