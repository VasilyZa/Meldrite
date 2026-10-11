package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.passive.TurtleEntity;

public class CraftTurtle extends CraftAnimals implements org.bukkit.entity.Turtle {
    public CraftTurtle(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public TurtleEntity getHandle() { return (TurtleEntity) super.getHandle(); }
    private dev.meldrite.v1201.hook.TurtleEntityPaperAccessMixin turtleState() { return (dev.meldrite.v1201.hook.TurtleEntityPaperAccessMixin) getHandle(); }
    @Override public boolean hasEgg() { return getHandle().hasEgg(); }
    @Override public boolean isLayingEgg() { return getHandle().isDiggingSand(); }
    @Override public org.bukkit.Location getHome() { var pos = turtleState().meldrite$home(); return new org.bukkit.Location(getWorld(), pos.getX(), pos.getY(), pos.getZ()); }
    @Override public void setHome(org.bukkit.Location location) { java.util.Objects.requireNonNull(location, "home").checkFinite(); requireWritable(); getHandle().setHomePos(net.minecraft.util.math.BlockPos.ofFloored(location.getX(), location.getY(), location.getZ())); }
    @Override public boolean isGoingHome() { return turtleState().meldrite$isGoingHome(); }
    @Override public boolean isDigging() { return getHandle().isDiggingSand(); }
    @Override public void setHasEgg(boolean value) { requireWritable(); turtleState().meldrite$hasEgg(value); }
}
