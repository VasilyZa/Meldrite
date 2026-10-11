package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.PaperItemAccess;
import dev.meldrite.v1201.hook.ItemEntityPaperAccessMixin;
import net.kyori.adventure.util.TriState;
import net.minecraft.entity.ItemEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.UUID;

/** Live Bukkit Item projection backed by a native ItemEntity. */
public final class CraftItem extends CraftEntity implements org.bukkit.entity.Item {
    private static final int NO_AGE_TIME = Short.MIN_VALUE;
    private static final int NO_PICKUP_TIME = Short.MAX_VALUE;
    private volatile ItemEntity item;

    public CraftItem(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
        super(delegate, context, entities);
        this.item = (ItemEntity) super.getHandle();
    }

    @Override public ItemEntity getHandle() { return item; }
    @Override public void meldrite$rebindNative(net.minecraft.entity.Entity replacement) {
        if (!(replacement instanceof ItemEntity next)) throw new IllegalArgumentException("ITEM_REBIND_REQUIRES_ITEM");
        super.meldrite$rebindNative(replacement);
        item = next;
    }

    private ItemEntityPaperAccessMixin state() { return (ItemEntityPaperAccessMixin) (Object) item; }
    private PaperItemAccess paperState() { return (PaperItemAccess) (Object) item; }

    @Override public ItemStack getItemStack() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftMirror(item.getStack()); }
    @Override public void setItemStack(ItemStack stack) { delegate.requireWritable(); item.setStack(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(Objects.requireNonNull(stack, "stack"))); }
    @Override public int getPickupDelay() { return state().meldrite$getPickupDelay(); }
    @Override public void setPickupDelay(int delay) { if (delay < 0) throw new IllegalArgumentException("delay must be nonnegative"); delegate.requireWritable(); state().meldrite$setPickupDelay(Math.min(delay, NO_PICKUP_TIME)); }
    @Override public void setUnlimitedLifetime(boolean unlimited) { delegate.requireWritable(); state().meldrite$setItemAge(unlimited ? NO_AGE_TIME : getTicksLived()); }
    @Override public boolean isUnlimitedLifetime() { return state().meldrite$getItemAge() == NO_AGE_TIME; }
    @Override public void setTicksLived(int ticks) {
        super.setTicksLived(ticks);
        if (!isUnlimitedLifetime()) state().meldrite$setItemAge(ticks);
    }
    @Override public void setOwner(UUID uuid) { delegate.requireWritable(); state().meldrite$setOwner(uuid); }
    @Override public UUID getOwner() { return state().meldrite$getOwner(); }
    @Override public void setThrower(UUID uuid) { delegate.requireWritable(); state().meldrite$setThrower(uuid); }
    @Override public UUID getThrower() { return state().meldrite$getThrower(); }
    @Override public boolean canMobPickup() { return paperState().meldrite$canMobPickup(); }
    @Override public void setCanMobPickup(boolean value) { delegate.requireWritable(); paperState().meldrite$setCanMobPickup(value); }
    @Override public boolean canPlayerPickup() { return state().meldrite$getPickupDelay() != NO_PICKUP_TIME; }
    @Override public void setCanPlayerPickup(boolean value) { delegate.requireWritable(); state().meldrite$setPickupDelay(value ? 0 : NO_PICKUP_TIME); }
    @Override public boolean willAge() { return state().meldrite$getItemAge() != NO_AGE_TIME; }
    @Override public void setWillAge(boolean value) { delegate.requireWritable(); state().meldrite$setItemAge(value ? 0 : NO_AGE_TIME); }
    @Override public int getHealth() { return state().meldrite$getHealth(); }
    @Override public void setHealth(int health) {
        delegate.requireWritable();
        if (health <= 0) { item.discard(); return; }
        state().meldrite$setHealth(health);
    }
    @Override public TriState getFrictionState() { return paperState().meldrite$getFrictionState(); }
    @Override public void setFrictionState(TriState state) { delegate.requireWritable(); paperState().meldrite$setFrictionState(state); }
}
