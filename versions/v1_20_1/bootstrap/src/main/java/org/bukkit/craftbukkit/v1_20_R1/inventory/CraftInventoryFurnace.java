package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Furnace;
import org.bukkit.block.Smoker;
import org.bukkit.inventory.FurnaceInventory;

public final class CraftInventoryFurnace extends CraftInventory implements FurnaceInventory {
    private final Furnace holder;

    public CraftInventoryFurnace(Inventory inventory, Furnace holder) {
        super(inventory, holder);
        this.holder = holder;
    }

    @Override public org.bukkit.inventory.ItemStack getResult() { return getItem(2); }
    @Override public org.bukkit.inventory.ItemStack getFuel() { return getItem(1); }
    @Override public org.bukkit.inventory.ItemStack getSmelting() { return getItem(0); }
    @Override public void setFuel(org.bukkit.inventory.ItemStack item) { setItem(1, item); }
    @Override public void setResult(org.bukkit.inventory.ItemStack item) { setItem(2, item); }
    @Override public void setSmelting(org.bukkit.inventory.ItemStack item) { setItem(0, item); }
    @Override public Furnace getHolder() { return holder; }

    @Override public void setItem(int slot, org.bukkit.inventory.ItemStack item) {
        if (!(inventory instanceof AbstractFurnaceBlockEntity furnace) || furnace.getWorld() != null) {
            super.setItem(slot, item);
            return;
        }
        if (slot < 0 || slot >= getSize()) throw new IndexOutOfBoundsException("slot: " + slot);
        ItemStack replacement = CraftItemStack.asNMSCopy(item);
        boolean unchanged = !replacement.isEmpty() && ItemStack.canCombine(furnace.getStack(slot), replacement);
        if (replacement.getCount() > furnace.getMaxCountPerStack()) replacement.setCount(furnace.getMaxCountPerStack());
        var nbt = furnace.createNbt();
        var contents = net.minecraft.util.collection.DefaultedList.ofSize(getSize(), ItemStack.EMPTY);
        net.minecraft.inventory.Inventories.readNbt(nbt, contents);
        contents.set(slot, replacement);
        net.minecraft.inventory.Inventories.writeNbt(nbt, contents);
        furnace.readNbt(nbt);
        if (slot == 0 && !unchanged) {
            furnace.cookTime = 0;
            if (holder.isPlaced()) {
                var world = ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) holder.getWorld()).getHandle();
                furnace.cookTimeTotal = AbstractFurnaceBlockEntity.getCookTime(world, furnace);
            }
        }
    }

    @Override public boolean isFuel(org.bukkit.inventory.ItemStack item) {
        return item != null && AbstractFurnaceBlockEntity.canUseAsFuel(CraftItemStack.asNMSCopy(item));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public boolean canSmelt(org.bukkit.inventory.ItemStack item) {
        if (item == null || item.getAmount() <= 0 || !(inventory instanceof AbstractFurnaceBlockEntity furnace)) return false;
        ServerWorld world = furnace.getWorld() instanceof ServerWorld live ? live
                : holder.isPlaced() ? ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) holder.getWorld()).getHandle() : null;
        if (world == null) return false;
        net.minecraft.inventory.SimpleInventory candidate = new net.minecraft.inventory.SimpleInventory(3);
        ItemStack input = CraftItemStack.asNMSCopy(item);
        candidate.setStack(0, input);
        candidate.setStack(2, furnace.getStack(2).copy());
        RecipeType<? extends AbstractCookingRecipe> recipeType = holder instanceof BlastFurnace
                ? RecipeType.BLASTING : holder instanceof Smoker ? RecipeType.SMOKING : RecipeType.SMELTING;
        return world.getRecipeManager().getFirstMatch((RecipeType) recipeType, candidate, world).isPresent();
    }
}
