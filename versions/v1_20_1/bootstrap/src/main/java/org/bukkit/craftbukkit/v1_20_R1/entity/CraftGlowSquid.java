package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.GlowSquidEntityPaperAccessMixin;
import net.minecraft.entity.passive.GlowSquidEntity;

public class CraftGlowSquid extends CraftSquid implements org.bukkit.entity.GlowSquid {
    public CraftGlowSquid(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public GlowSquidEntity getHandle() { return (GlowSquidEntity) super.getHandle(); }
    @Override public int getDarkTicksRemaining() { return getHandle().getDarkTicksRemaining(); }
    @Override public void setDarkTicksRemaining(int ticks) { if (ticks < 0) throw new IllegalArgumentException("Dark ticks must be nonnegative"); requireWritable(); ((GlowSquidEntityPaperAccessMixin) getHandle()).meldrite$setDarkTicksRemaining(ticks); }
}
