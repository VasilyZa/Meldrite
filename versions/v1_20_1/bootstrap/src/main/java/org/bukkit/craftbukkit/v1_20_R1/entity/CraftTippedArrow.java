package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public class CraftTippedArrow extends CraftArrow implements org.bukkit.entity.TippedArrow {
    public CraftTippedArrow(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
}
