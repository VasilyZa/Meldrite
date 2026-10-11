package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.entity.boss.dragon.EnderDragonPart;

public class CraftComplexPart extends CraftEntity implements org.bukkit.entity.ComplexEntityPart {
    private final org.bukkit.entity.ComplexLivingEntity parent;
    public CraftComplexPart(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
        super(delegate, context, entities);
        this.parent = (org.bukkit.entity.ComplexLivingEntity) NativeEntityExtensions.bukkit(getHandle().owner);
    }
    public CraftComplexPart(EnderDragonPart part, org.bukkit.entity.ComplexLivingEntity parent) {
        this(part, parent, project(part));
    }
    private CraftComplexPart(EnderDragonPart part, org.bukkit.entity.ComplexLivingEntity parent, CraftEntity projected) {
        super(projected);
        this.parent = parent;
    }
    private static CraftEntity project(EnderDragonPart part) {
        return NativeEntityExtensions.bukkit(part);
    }
    @Override public EnderDragonPart getHandle() { return (EnderDragonPart) super.getHandle(); }
    @Override public org.bukkit.entity.ComplexLivingEntity getParent() { return parent; }
    @Override public boolean isValid() { return parent.isValid() && !getHandle().isRemoved(); }
}
