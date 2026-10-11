package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.boss.WitherEntity;

public class CraftWither extends CraftMonster implements org.bukkit.entity.Wither, com.destroystokyo.paper.entity.CraftRangedEntity<WitherEntity> {
    public CraftWither(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WitherEntity getHandle() { return (WitherEntity) super.getHandle(); }
    @Override public org.bukkit.boss.BossBar getBossBar() { return new org.bukkit.craftbukkit.v1_20_R1.boss.CraftBossBar(((dev.meldrite.v1201.hook.WitherEntityPaperAccessMixin) getHandle()).meldrite$bossBar()); }
    @Override public void setTarget(org.bukkit.entity.LivingEntity target) { super.setTarget(target); }
    @Override public void setTarget(org.bukkit.entity.Wither.Head head, org.bukkit.entity.LivingEntity target) { if (head == null) throw new IllegalArgumentException("head"); requireWritable(); getHandle().setTrackedEntityId(head.ordinal(), target == null ? 0 : target.getEntityId()); }
    @Override public org.bukkit.entity.LivingEntity getTarget(org.bukkit.entity.Wither.Head head) { if (head == null) throw new IllegalArgumentException("head"); int id = getHandle().getTrackedEntityId(head.ordinal()); if (id == 0) return null; var entity = getHandle().getWorld().getEntityById(id); return entity == null ? null : (org.bukkit.entity.LivingEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity); }
    @Override public int getInvulnerabilityTicks() { return getHandle().getInvulnerableTimer(); }
    @Override public void setInvulnerabilityTicks(int ticks) { if (ticks < 0) throw new IllegalArgumentException("ticks < 0"); requireWritable(); getHandle().setInvulTimer(ticks); }
    @Override public boolean isCharged() { return getHandle().shouldRenderOverlay(); }
    @Override public int getInvulnerableTicks() { return getInvulnerabilityTicks(); }
    @Override public void setInvulnerableTicks(int ticks) { setInvulnerabilityTicks(ticks); }
    @Override public boolean canTravelThroughPortals() { return getHandle().canUsePortals(); }
    @Override public void setCanTravelThroughPortals(boolean value) { requireWritable(); ((dev.meldrite.v1201.compat.NativeWitherState) getHandle()).meldrite$setCanTravelThroughPortals(value); }
    @Override public void enterInvulnerabilityPhase() { requireWritable(); getHandle().onSummoned(); }
}
