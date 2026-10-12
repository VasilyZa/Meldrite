package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.v1201.hook.ForgingScreenHandlerPaperAccessMixin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

/** The vanilla anvil algorithm operating on a Bukkit-created inventory. */
public final class CraftAnvilScreenHandler extends AnvilScreenHandler {
    private final CraftInventoryCustomAnvil inventory;
    private final String title;
    private final InventoryChangedListener inputListener = this::onContentChanged;

    public CraftAnvilScreenHandler(int syncId, PlayerInventory playerInventory,
            CraftInventoryCustomAnvil inventory, String title) {
        super(syncId, playerInventory, ScreenHandlerContext.EMPTY);
        this.inventory = java.util.Objects.requireNonNull(inventory, "inventory");
        this.title = java.util.Objects.requireNonNull(title, "title");
        var access = (ForgingScreenHandlerPaperAccessMixin) (Object) this;
        access.meldrite$setInput(inventory.getIngredientsInventory());
        access.meldrite$setOutput((CraftingResultInventory) inventory.getResultInventory());
        for (int index = 0; index < 2; index++) {
            Slot original = getSlot(index);
            replaceSlot(index, new Slot(inventory.getIngredientsInventory(), index, original.x, original.y));
        }
        int resultIndex = getResultSlotIndex();
        Slot result = getSlot(resultIndex);
        replaceSlot(resultIndex, new Slot(inventory.getResultInventory(), result.getIndex(), result.x, result.y) {
            @Override public boolean canInsert(ItemStack stack) { return false; }
            @Override public boolean canTakeItems(PlayerEntity player) {
                return CraftAnvilScreenHandler.this.canTakeOutput(player, hasStack());
            }
            @Override public void onTakeItem(PlayerEntity player, ItemStack stack) {
                CraftAnvilScreenHandler.this.onTakeOutput(player, stack);
            }
        });
        inventory.bind(this);
        ((SimpleInventory) inventory.getIngredientsInventory()).addListener(inputListener);
        inventory.getIngredientsInventory().onOpen(playerInventory.player);
    }

    private void replaceSlot(int index, Slot replacement) {
        replacement.id = index;
        slots.set(index, replacement);
    }

    public CraftInventoryCustomAnvil getCraftInventory() { return inventory; }
    public String getTitle() { return title; }

    @Override public void onClosed(PlayerEntity player) {
        try {
            super.onClosed(player);
        } finally {
            ((SimpleInventory) inventory.getIngredientsInventory()).removeListener(inputListener);
            inventory.getIngredientsInventory().onClose(player);
            inventory.unbind(this);
        }
    }
}
