package org.bukkit.craftbukkit.v1_20_R1.block.data.type;

import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.ChestType;
import net.minecraft.state.property.Property;
import org.bukkit.block.data.type.Chest;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftDirectional;

/** Chest type, direction and waterlogging use the exact native properties. */
public abstract class CraftChest extends CraftDirectional implements Chest {
    public CraftChest(BlockState state) { super(state); }
    public CraftChest(BlockState state, Map<Property<?>, Comparable<?>> parsedStates) { super(state, parsedStates); }
    @Override public Type getType() { return Type.valueOf(this.<ChestType>propertyValue("type").name()); }
    @Override public void setType(Type type) { propertyValue("type", ChestType.valueOf(java.util.Objects.requireNonNull(type).name())); }
    @Override public boolean isWaterlogged() { return propertyValue("waterlogged"); }
    @Override public void setWaterlogged(boolean waterlogged) { propertyValue("waterlogged", waterlogged); }
}
