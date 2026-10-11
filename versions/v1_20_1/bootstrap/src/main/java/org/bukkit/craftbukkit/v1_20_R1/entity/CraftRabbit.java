package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.RabbitEntity;

public class CraftRabbit extends CraftAnimals implements org.bukkit.entity.Rabbit {
    public CraftRabbit(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public RabbitEntity getHandle() { return (RabbitEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.RabbitEntityPaperAccessMixin rabbitState() { return (dev.meldrite.v1201.hook.RabbitEntityPaperAccessMixin) getHandle(); }
    @Override public Type getRabbitType() { return Type.values()[getHandle().getVariant().ordinal()]; }
    @Override public void setRabbitType(Type type) { requireWritable(); getHandle().setVariant(RabbitEntity.RabbitType.values()[java.util.Objects.requireNonNull(type, "type").ordinal()]); }
    @Override public void setMoreCarrotTicks(int ticks) { requireWritable(); rabbitState().meldrite$carrotTicks(ticks); }
    @Override public int getMoreCarrotTicks() { return rabbitState().meldrite$carrotTicks(); }
}
