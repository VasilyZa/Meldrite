package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public class CraftMarker extends CraftEntity implements org.bukkit.entity.Marker {
    public CraftMarker(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.MarkerEntity getHandle() { return (net.minecraft.entity.MarkerEntity) super.getHandle(); }
}
