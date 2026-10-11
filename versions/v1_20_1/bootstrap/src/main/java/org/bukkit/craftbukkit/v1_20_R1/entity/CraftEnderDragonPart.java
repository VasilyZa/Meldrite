package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public class CraftEnderDragonPart extends CraftComplexPart implements org.bukkit.entity.EnderDragonPart {
    public CraftEnderDragonPart(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public org.bukkit.entity.EnderDragon getParent() { return (org.bukkit.entity.EnderDragon) super.getParent(); }
    @Override public void damage(double amount) { getParent().damage(amount); }
    @Override public void damage(double amount, org.bukkit.entity.Entity source) { getParent().damage(amount, source); }
    @Override public double getHealth() { return getParent().getHealth(); }
    @Override public void setHealth(double value) { getParent().setHealth(value); }
    @Override public double getAbsorptionAmount() { return getParent().getAbsorptionAmount(); }
    @Override public void setAbsorptionAmount(double value) { getParent().setAbsorptionAmount(value); }
    @Override public double getMaxHealth() { return getParent().getMaxHealth(); }
    @Override public void setMaxHealth(double value) { getParent().setMaxHealth(value); }
    @Override public void resetMaxHealth() { getParent().resetMaxHealth(); }
}
