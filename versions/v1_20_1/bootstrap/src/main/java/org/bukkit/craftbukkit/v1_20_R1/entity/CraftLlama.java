package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.LlamaEntityPaperAccessMixin;
import net.minecraft.entity.passive.LlamaEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryLlama;
import org.bukkit.entity.Llama;

public class CraftLlama extends CraftChestedHorse implements Llama, com.destroystokyo.paper.entity.CraftRangedEntity<LlamaEntity> {
    public CraftLlama(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public LlamaEntity getHandle() { return (LlamaEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Horse.Variant getVariant() { return org.bukkit.entity.Horse.Variant.LLAMA; }
    @Override public Color getColor() { return Color.values()[getHandle().getVariant().ordinal()]; }
    @Override public void setColor(Color color) { java.util.Objects.requireNonNull(color, "color"); requireWritable(); getHandle().setVariant(LlamaEntity.Variant.values()[color.ordinal()]); }
    @Override public int getStrength() { return getHandle().getStrength(); }
    @Override public void setStrength(int value) { if (value < 1 || value > 5) throw new IllegalArgumentException("Llama strength must be between 1 and 5"); requireWritable(); if (value != getStrength()) { ((LlamaEntityPaperAccessMixin) getHandle()).meldrite$setStrength(value); horseAccess().meldrite$resizeInventory(); } }
    @Override public org.bukkit.inventory.LlamaInventory getInventory() { return new CraftInventoryLlama(horseAccess().meldrite$getInventory(), this); }
    @Override public boolean inCaravan() { return getHandle().isFollowing(); }
    @Override public void joinCaravan(Llama llama) {
        java.util.Objects.requireNonNull(llama, "llama");
        if (!(llama instanceof CraftLlama head)) throw new IllegalArgumentException("Llama must be native-backed");
        requireWritable();
        if (head == this || head.getHandle().getWorld() != getHandle().getWorld()) throw new IllegalArgumentException("Caravan head must be another llama in the same world");
        if (inCaravan()) leaveCaravan();
        getHandle().follow(head.getHandle());
    }
    @Override public void leaveCaravan() { requireWritable(); getHandle().stopFollowing(); }
    @Override public Llama getCaravanHead() { var head = getHandle().getFollowing(); return head == null ? null : (Llama) NativeEntityExtensions.bukkit(head); }
    @Override public boolean hasCaravanTail() { return getHandle().hasFollower(); }
    @Override public Llama getCaravanTail() { var tail = ((LlamaEntityPaperAccessMixin) getHandle()).meldrite$getFollower(); return tail == null ? null : (Llama) NativeEntityExtensions.bukkit(tail); }
}
