package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.FishEntity;

public class CraftFish extends CraftWaterMob implements org.bukkit.entity.Fish, io.papermc.paper.entity.PaperBucketable {
    public CraftFish(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public FishEntity getHandle() { return (FishEntity) super.getHandle(); }
    @Override public void setFromBucket(boolean value) { requireWritable(); getHandle().setFromBucket(value); }
}
