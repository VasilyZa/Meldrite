package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.mob.AmbientEntity;
public class CraftAmbient extends CraftMob implements org.bukkit.entity.Ambient {
    public CraftAmbient(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AmbientEntity getHandle() { return (AmbientEntity) super.getHandle(); }
}
