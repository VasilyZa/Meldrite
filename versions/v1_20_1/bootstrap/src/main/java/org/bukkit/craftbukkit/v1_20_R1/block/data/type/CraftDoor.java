package org.bukkit.craftbukkit.v1_20_R1.block.data.type;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.state.property.Property;
import org.bukkit.block.data.type.Door;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftDirectional;

/** Both door halves, hinge, power and openness retain their native values. */
public abstract class CraftDoor extends CraftDirectional implements Door {
    public CraftDoor(BlockState state) { super(state); }
    public CraftDoor(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public Hinge getHinge() { return Hinge.valueOf(this.<DoorHinge>propertyValue("hinge").name()); }
    @Override public void setHinge(Hinge hinge) { propertyValue("hinge", DoorHinge.valueOf(java.util.Objects.requireNonNull(hinge).name())); }
    @Override public Half getHalf() { return this.<DoubleBlockHalf>propertyValue("half") == DoubleBlockHalf.UPPER ? Half.TOP : Half.BOTTOM; }
    @Override public void setHalf(Half half) { propertyValue("half", java.util.Objects.requireNonNull(half) == Half.TOP ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER); }
    @Override public boolean isOpen() { return propertyValue("open"); }
    @Override public void setOpen(boolean open) { propertyValue("open", open); }
    @Override public boolean isPowered() { return propertyValue("powered"); }
    @Override public void setPowered(boolean powered) { propertyValue("powered", powered); }
}
