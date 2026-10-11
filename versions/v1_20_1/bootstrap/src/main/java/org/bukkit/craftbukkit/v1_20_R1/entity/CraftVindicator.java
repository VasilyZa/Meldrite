package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.VindicatorEntityPaperAccessMixin;
import net.minecraft.entity.mob.VindicatorEntity;

public class CraftVindicator extends CraftIllager implements org.bukkit.entity.Vindicator {
    public CraftVindicator(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public VindicatorEntity getHandle() { return (VindicatorEntity) super.getHandle(); }
    @Override public boolean isJohnny() { return ((VindicatorEntityPaperAccessMixin) getHandle()).meldrite$isJohnny(); }
    @Override public void setJohnny(boolean value) { requireWritable(); ((VindicatorEntityPaperAccessMixin) getHandle()).meldrite$setJohnny(value); }
}
