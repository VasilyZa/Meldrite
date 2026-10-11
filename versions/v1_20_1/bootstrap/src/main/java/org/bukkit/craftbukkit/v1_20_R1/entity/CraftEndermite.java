package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEndermiteState;
import dev.meldrite.v1201.hook.EndermiteEntityPaperAccessMixin;
import net.minecraft.entity.mob.EndermiteEntity;
public class CraftEndermite extends CraftMonster implements org.bukkit.entity.Endermite {
    public CraftEndermite(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EndermiteEntity getHandle() { return (EndermiteEntity) super.getHandle(); }
    @Override public boolean isPlayerSpawned() { return ((NativeEndermiteState) getHandle()).meldrite$isPlayerSpawned(); }
    @Override public void setPlayerSpawned(boolean value) { requireWritable(); ((NativeEndermiteState) getHandle()).meldrite$playerSpawned(value); }
    @Override public int getLifetimeTicks() { return ((EndermiteEntityPaperAccessMixin) getHandle()).meldrite$lifetime(); }
    @Override public void setLifetimeTicks(int ticks) { requireWritable(); ((EndermiteEntityPaperAccessMixin) getHandle()).meldrite$lifetime(ticks); }
}
