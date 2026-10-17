package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeAnvilContext;
import dev.meldrite.v1201.compat.NativeAnvilState;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.AnvilScreenHandler;
import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

/** Native and plugin-owned anvils share CraftBukkit's reflective anvil ABI. */
public class CraftInventoryAnvil extends CraftResultInventory implements org.bukkit.inventory.AnvilInventory {
    private final Location location;
    private final boolean custom;
    /** The event's live handler; null only while a plugin-owned inventory is unopened. */
    public AnvilScreenHandler container;
    private final java.util.Deque<AnvilScreenHandler> containers = new java.util.ArrayDeque<>();
    private String renameText;
    private int repairCost;
    private int repairCostAmount;
    private int maximumRepairCost = 40;

    public CraftInventoryAnvil(Location location, Inventory inventory, Inventory result, AnvilScreenHandler container) {
        super(inventory, result, InventoryType.ANVIL);
        this.location = location;
        this.custom = false;
        this.container = java.util.Objects.requireNonNull(container, "container");
    }

    public CraftInventoryAnvil(InventoryHolder holder, String title) {
        super(new SimpleInventory(2), new CraftingResultInventory(), holder, InventoryType.ANVIL, title);
        this.location = null;
        this.custom = true;
    }

    boolean isCustom() { return custom; }
    @Override public Location getLocation() { return location == null ? null : location.clone(); }

    /** Refreshes the reflective field after event entry/exit or a binding change. */
    public void refreshContainer() {
        if (!custom) return;
        AnvilScreenHandler current = NativeAnvilContext.current();
        container = current instanceof CraftAnvilScreenHandler handler && handler.getCraftInventory() == this
                ? current : containers.peekLast();
    }

    void bind(AnvilScreenHandler handler) {
        java.util.Objects.requireNonNull(handler, "handler");
        int cost = getRepairCost();
        int amount = getRepairCostAmount();
        int maximum = getMaximumRepairCost();
        containers.addLast(handler);
        refreshContainer();
        NativeAnvilState state = (NativeAnvilState) handler;
        state.meldrite$setRepairCost(cost);
        state.meldrite$setRepairCostAmount(amount);
        state.meldrite$setMaximumRepairCost(maximum);
    }

    void unbind(AnvilScreenHandler handler) {
        if (!containers.contains(handler)) return;
        if (containers.peekLast() == handler) {
            NativeAnvilState state = (NativeAnvilState) handler;
            renameText = state.meldrite$getRenameText();
            repairCost = state.meldrite$getRepairCost();
            repairCostAmount = state.meldrite$getRepairCostAmount();
            maximumRepairCost = state.meldrite$getMaximumRepairCost();
        }
        containers.remove(handler);
        refreshContainer();
    }

    private NativeAnvilState state() {
        refreshContainer();
        return (NativeAnvilState) container;
    }
    @Override public String getRenameText() {
        NativeAnvilState state = state();
        return state == null ? renameText : state.meldrite$getRenameText();
    }
    @Override public int getRepairCostAmount() {
        NativeAnvilState state = state();
        return state == null ? repairCostAmount : state.meldrite$getRepairCostAmount();
    }
    @Override public void setRepairCostAmount(int amount) {
        repairCostAmount = amount;
        NativeAnvilState state = state();
        if (state != null) state.meldrite$setRepairCostAmount(amount);
    }
    @Override public int getRepairCost() {
        NativeAnvilState state = state();
        return state == null ? repairCost : state.meldrite$getRepairCost();
    }
    @Override public void setRepairCost(int cost) {
        repairCost = cost;
        NativeAnvilState state = state();
        if (state != null) state.meldrite$setRepairCost(cost);
    }
    @Override public int getMaximumRepairCost() {
        NativeAnvilState state = state();
        return state == null ? maximumRepairCost : state.meldrite$getMaximumRepairCost();
    }
    @Override public void setMaximumRepairCost(int cost) {
        if (cost < 0) throw new IllegalArgumentException("Maximum repair cost must not be negative");
        maximumRepairCost = cost;
        NativeAnvilState state = state();
        if (state != null) state.meldrite$setMaximumRepairCost(cost);
    }
}
