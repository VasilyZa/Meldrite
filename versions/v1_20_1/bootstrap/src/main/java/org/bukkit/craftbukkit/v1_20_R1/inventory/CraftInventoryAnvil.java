package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeAnvilState;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.AnvilScreenHandler;
import org.bukkit.Location;

public class CraftInventoryAnvil extends CraftResultInventory implements org.bukkit.inventory.AnvilInventory {
    private final Location location;
    public final AnvilScreenHandler container;
    public CraftInventoryAnvil(Location location, Inventory inventory, Inventory result, AnvilScreenHandler container) {
        super(inventory, result, org.bukkit.event.inventory.InventoryType.ANVIL);
        this.location = location;
        this.container = java.util.Objects.requireNonNull(container, "container");
    }
    private NativeAnvilState state() { return (NativeAnvilState) container; }
    @Override public Location getLocation() { return location == null ? null : location.clone(); }
    @Override public String getRenameText() { return state().meldrite$getRenameText(); }
    @Override public int getRepairCostAmount() { return state().meldrite$getRepairCostAmount(); }
    @Override public void setRepairCostAmount(int amount) { state().meldrite$setRepairCostAmount(amount); }
    @Override public int getRepairCost() { return state().meldrite$getRepairCost(); }
    @Override public void setRepairCost(int cost) { state().meldrite$setRepairCost(cost); }
    @Override public int getMaximumRepairCost() { return state().meldrite$getMaximumRepairCost(); }
    @Override public void setMaximumRepairCost(int cost) {
        if (cost < 0) throw new IllegalArgumentException("Maximum repair cost must not be negative");
        state().meldrite$setMaximumRepairCost(cost);
    }
}
