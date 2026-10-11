package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.NativeCampfireCookingAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public final class CraftCampfire extends CraftBlockEntityState<CampfireBlockEntity> implements org.bukkit.block.Campfire {
    public CraftCampfire(ServerWorld world, BlockPos position, BlockState state, CampfireBlockEntity tile, boolean useSnapshot) {
        super(world, position, state, tile, useSnapshot);
    }

    private void checkSlot(int slot) { if (slot < 0 || slot >= getSize()) throw new IllegalArgumentException("Invalid campfire slot: " + slot); }
    @Override public int getSize() { return snapshot.getItemsBeingCooked().size(); }
    @Override public ItemStack getItem(int slot) { checkSlot(slot); var item = snapshot.getItemsBeingCooked().get(slot); return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item); }
    @Override public void setItem(int slot, ItemStack item) { checkSlot(slot); snapshot.getItemsBeingCooked().set(slot, CraftItemStack.asNMSCopy(item)); }
    @Override public int getCookTime(int slot) { checkSlot(slot); return snapshot.cookingTimes[slot]; }
    @Override public void setCookTime(int slot, int time) { checkSlot(slot); snapshot.cookingTimes[slot] = time; }
    @Override public int getCookTimeTotal(int slot) { checkSlot(slot); return snapshot.cookingTotalTimes[slot]; }
    @Override public void setCookTimeTotal(int slot, int time) { checkSlot(slot); snapshot.cookingTotalTimes[slot] = time; }
    @Override public void stopCooking() { for (int slot = 0; slot < getSize(); slot++) stopCooking(slot); }
    @Override public void startCooking() { for (int slot = 0; slot < getSize(); slot++) startCooking(slot); }
    @Override public boolean stopCooking(int slot) { boolean previous = isCookingDisabled(slot); ((NativeCampfireCookingAccess) snapshot).meldrite$setCookingDisabled(slot, true); return previous; }
    @Override public boolean startCooking(int slot) { boolean previous = isCookingDisabled(slot); ((NativeCampfireCookingAccess) snapshot).meldrite$setCookingDisabled(slot, false); return previous; }
    @Override public boolean isCookingDisabled(int slot) { checkSlot(slot); return ((NativeCampfireCookingAccess) snapshot).meldrite$isCookingDisabled(slot); }
}
