package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.compat.NativeAnvilState;
import dev.meldrite.v1201.compat.NativeAnvilContext;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.AnvilScreenHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryHolder;

/** A plugin-owned anvil whose identity and contents survive native menu binding. */
public final class CraftInventoryCustomAnvil extends CraftResultInventory implements AnvilInventory {
    private final java.util.Deque<AnvilScreenHandler> containers = new java.util.ArrayDeque<>();
    private String renameText;
    private int repairCost;
    private int repairCostAmount;
    private int maximumRepairCost = 40;

    public CraftInventoryCustomAnvil(InventoryHolder holder, String title) {
        super(new SimpleInventory(2), new CraftingResultInventory(), holder, InventoryType.ANVIL, title);
    }

    void bind(AnvilScreenHandler handler) {
        java.util.Objects.requireNonNull(handler, "handler");
        int cost = getRepairCost();
        int amount = getRepairCostAmount();
        int maximum = getMaximumRepairCost();
        containers.addLast(handler);
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
    }

    private NativeAnvilState state() {
        AnvilScreenHandler current = NativeAnvilContext.current();
        if (current instanceof CraftAnvilScreenHandler custom && custom.getCraftInventory() == this) {
            return (NativeAnvilState) current;
        }
        return (NativeAnvilState) containers.peekLast();
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
