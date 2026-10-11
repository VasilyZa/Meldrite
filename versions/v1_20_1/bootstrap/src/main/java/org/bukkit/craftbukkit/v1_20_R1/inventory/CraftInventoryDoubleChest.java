package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.Objects;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.bukkit.Location;
import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;

/** Live wrapper around the native two-sided chest inventory. */
public class CraftInventoryDoubleChest extends CraftInventory implements DoubleChestInventory {
    private final CraftInventory left;
    private final CraftInventory right;

    public CraftInventoryDoubleChest(DoubleInventory inventory) {
        super(Objects.requireNonNull(inventory, "inventory"));
        var access = (dev.meldrite.v1201.hook.DoubleInventoryAccessMixin) (Object) inventory;
        this.left = new CraftInventory(access.meldrite$first());
        this.right = new CraftInventory(access.meldrite$second());
    }

    public CraftInventoryDoubleChest(Inventory left, Inventory right) {
        this(new DoubleInventory(Objects.requireNonNull(left, "left"), Objects.requireNonNull(right, "right")));
    }

    @Override public org.bukkit.inventory.Inventory getLeftSide() { return left; }
    @Override public org.bukkit.inventory.Inventory getRightSide() { return right; }
    @Override public DoubleChest getHolder() { return new DoubleChest(this); }
    @Override public Location getLocation() {
        Location first = left.getLocation();
        Location second = right.getLocation();
        if (first == null) return second;
        if (second == null) return first;
        return first.clone().add(second).multiply(0.5);
    }
}
