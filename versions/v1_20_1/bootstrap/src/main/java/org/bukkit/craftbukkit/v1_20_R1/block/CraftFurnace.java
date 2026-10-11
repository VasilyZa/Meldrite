package org.bukkit.craftbukkit.v1_20_R1.block;

import dev.meldrite.v1201.compat.NativeFurnaceCookSpeedAccess;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftCookingRecipe;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceInventory;

public class CraftFurnace extends CraftContainer implements Furnace {
    public CraftFurnace(ServerWorld world, BlockPos position, BlockState state,
                        AbstractFurnaceBlockEntity furnace, boolean useSnapshot) {
        super(world, position, state, furnace, useSnapshot);
    }

    protected AbstractFurnaceBlockEntity furnace() { return (AbstractFurnaceBlockEntity) snapshot; }
    @Override public AbstractFurnaceBlockEntity getTileEntity() { return (AbstractFurnaceBlockEntity) tileEntity; }
    @Override public AbstractFurnaceBlockEntity getSnapshot() { return furnace(); }

    @Override public FurnaceInventory getInventory() {
        CraftInventory inventory = (CraftInventory) super.getInventory();
        return new CraftInventoryFurnace(inventory.getInventory(), this);
    }

    @Override public FurnaceInventory getSnapshotInventory() {
        return new CraftInventoryFurnace(snapshot, this);
    }

    @Override public short getBurnTime() { return (short) furnace().burnTime; }
    @Override public void setBurnTime(short burnTime) {
        furnace().burnTime = burnTime;
        state = state.with(net.minecraft.block.AbstractFurnaceBlock.LIT, burnTime > 0);
    }
    @Override public short getCookTime() { return (short) furnace().cookTime; }
    @Override public void setCookTime(short cookTime) { furnace().cookTime = cookTime; }
    @Override public int getCookTimeTotal() { return furnace().cookTimeTotal; }
    @Override public void setCookTimeTotal(int cookTimeTotal) {
        furnace().cookTimeTotal = cookTimeTotal;
    }

    @Override public double getCookSpeedMultiplier() {
        return ((NativeFurnaceCookSpeedAccess) furnace()).meldrite$getCookSpeedMultiplier();
    }

    @Override public void setCookSpeedMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier) || multiplier < 0.0D || multiplier > 200.0D) {
            throw new IllegalArgumentException("cookSpeedMultiplier must be between 0 and 200");
        }
        ((NativeFurnaceCookSpeedAccess) furnace()).meldrite$setCookSpeedMultiplier(multiplier);
        furnace().cookTimeTotal = AbstractFurnaceBlockEntity.getCookTime(world, furnace());
    }

    @Override public Map<CookingRecipe<?>, Integer> getRecipesUsed() {
        Map<CookingRecipe<?>, Integer> result = new HashMap<>();
        furnace().recipesUsed.forEach((key, count) -> world.getRecipeManager().get(key).ifPresent(recipe -> {
            if (recipe instanceof net.minecraft.recipe.AbstractCookingRecipe cookingRecipe) {
                result.put(CraftCookingRecipe.fromMinecraft(cookingRecipe, world.getRegistryManager()), count);
            }
        }));
        return Map.copyOf(result);
    }

    @Override public int getRecipeUsedCount(NamespacedKey recipe) {
        Objects.requireNonNull(recipe, "recipe");
        return furnace().recipesUsed.getInt(CraftNamespacedKey.toMinecraft(recipe));
    }

    @Override public boolean hasRecipeUsedCount(NamespacedKey recipe) {
        Objects.requireNonNull(recipe, "recipe");
        return furnace().recipesUsed.containsKey(CraftNamespacedKey.toMinecraft(recipe));
    }

    @Override public void setRecipeUsedCount(CookingRecipe<?> recipe, int count) {
        Objects.requireNonNull(recipe, "recipe");
        var key = CraftNamespacedKey.toMinecraft(recipe.getKey());
        var nativeRecipe = world.getRecipeManager().get(key);
        if (nativeRecipe.isEmpty() || !(nativeRecipe.get() instanceof net.minecraft.recipe.AbstractCookingRecipe)) {
            throw new IllegalArgumentException(recipe.getKey() + " is not a registered cooking recipe");
        }
        if (count <= 0) furnace().recipesUsed.removeInt(key);
        else furnace().recipesUsed.put(key, count);
    }

    @Override public void setRecipesUsed(Map<CookingRecipe<?>, Integer> recipesUsed) {
        Objects.requireNonNull(recipesUsed, "recipesUsed");
        furnace().recipesUsed.clear();
        recipesUsed.forEach((recipe, count) -> {
            Objects.requireNonNull(recipe, "recipe");
            if (count != null) setRecipeUsedCount(recipe, count);
        });
    }
}
