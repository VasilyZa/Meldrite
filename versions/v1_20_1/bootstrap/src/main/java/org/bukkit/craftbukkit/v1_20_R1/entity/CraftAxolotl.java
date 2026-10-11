package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.AxolotlEntity;

public class CraftAxolotl extends CraftAnimals implements org.bukkit.entity.Axolotl, io.papermc.paper.entity.PaperBucketable {
    public CraftAxolotl(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public AxolotlEntity getHandle() { return (AxolotlEntity) super.getHandle(); }
    @Override public boolean isPlayingDead() { return getHandle().isPlayingDead(); }
    @Override public void setPlayingDead(boolean value) { requireWritable(); getHandle().setPlayingDead(value); }
    @Override public Variant getVariant() { return Variant.values()[getHandle().getVariant().ordinal()]; }
    @Override public void setVariant(Variant variant) { requireWritable(); getHandle().setVariant(AxolotlEntity.Variant.values()[java.util.Objects.requireNonNull(variant, "variant").ordinal()]); }
}
