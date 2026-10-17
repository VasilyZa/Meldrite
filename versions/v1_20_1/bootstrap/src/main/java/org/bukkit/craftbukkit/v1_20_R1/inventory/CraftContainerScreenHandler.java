package org.bukkit.craftbukkit.v1_20_R1.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.bukkit.event.inventory.InventoryType;

/** Screen handler for Bukkit-created inventories whose contents are plugin-owned. */
public final class CraftContainerScreenHandler extends ScreenHandler {
    private final CraftInventory inventory;
    private final PlayerInventory playerInventory;
    private final PlayerEntity player;
    private final net.minecraft.inventory.InventoryChangedListener craftingListener = this::onContentChanged;
    private final String title;

    public static ScreenHandler create(int syncId, PlayerInventory playerInventory, CraftInventory inventory,
            PlayerEntity player, String title) {
        if (inventory instanceof CraftInventoryAnvil anvil && anvil.isCustom()) {
            return new CraftAnvilScreenHandler(syncId, playerInventory, anvil, title);
        }
        return new CraftContainerScreenHandler(syncId, playerInventory, inventory, player, title);
    }

    public CraftContainerScreenHandler(int syncId, PlayerInventory playerInventory, CraftInventory inventory, PlayerEntity player) {
        this(syncId, playerInventory, inventory, player, inventory.getTitle());
    }

    public CraftContainerScreenHandler(int syncId, PlayerInventory playerInventory, CraftInventory inventory,
            PlayerEntity player, String title) {
        super(screenType(inventory), syncId);
        this.inventory = java.util.Objects.requireNonNull(inventory, "inventory");
        this.playerInventory = java.util.Objects.requireNonNull(playerInventory, "playerInventory");
        this.player = java.util.Objects.requireNonNull(player, "player");
        this.title = java.util.Objects.requireNonNull(title, "title");
        addTopSlots(inventory.getType(), inventory.getSize());
        addPlayerSlots(playerInventory);
        int propertyCount = propertyCount(inventory.getType());
        if (propertyCount > 0) addProperties(new net.minecraft.screen.ArrayPropertyDelegate(propertyCount));
        inventory.getInventory().onOpen(player);
        if (inventory instanceof CraftInventoryCrafting && inventory.getInventory() instanceof net.minecraft.inventory.SimpleInventory matrix) {
            matrix.addListener(craftingListener);
        }
    }

    public CraftInventory getCraftInventory() {
        return inventory;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.getInventory().canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int topSize = topSlotCount(inventory.getType(), inventory.getSize());
        int firstInput = inventory instanceof CraftInventoryCrafting ? 1 : 0;
        boolean moved = slotIndex < topSize
                ? insertItem(stack, topSize, slots.size(), true)
                : insertItem(stack, firstInput, topSize, false);
        if (!moved) return ItemStack.EMPTY;
        if (slotIndex == 0 && inventory instanceof CraftInventoryCrafting) slot.onQuickTransfer(stack, original);
        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTakeItem(player, stack);
        return original;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.getInventory().onClose(player);
        if (inventory instanceof CraftInventoryCrafting && inventory.getInventory() instanceof net.minecraft.inventory.SimpleInventory matrix) {
            matrix.removeListener(craftingListener);
        }
    }

    @Override public void onContentChanged(net.minecraft.inventory.Inventory changed) {
        if (inventory instanceof CraftInventoryCrafting crafting) {
            dev.meldrite.v1201.hook.CraftingScreenHandlerAccess.meldrite$updateResult(this, player.getWorld(), player,
                    (net.minecraft.inventory.RecipeInputInventory) crafting.getMatrixInventory(),
                    (net.minecraft.inventory.CraftingResultInventory) crafting.getResultInventory());
        }
        super.onContentChanged(changed);
    }

    private void addTopSlots(InventoryType type, int size) {
        if (inventory instanceof CraftInventoryCrafting crafting) {
            addSlot(new net.minecraft.screen.slot.CraftingResultSlot(player,
                    (net.minecraft.inventory.RecipeInputInventory) crafting.getMatrixInventory(), crafting.getResultInventory(), 0, 124, 35));
            for (int row = 0; row < 3; row++) for (int column = 0; column < 3; column++) {
                addSlot(new Slot(crafting.getMatrixInventory(), row * 3 + column, 30 + column * 18, 17 + row * 18));
            }
            return;
        }
        if (isGeneric(type)) {
            int rows = type == InventoryType.PLAYER ? 4 : size / 9;
            requireSize(type == InventoryType.PLAYER && size == 41
                    || size >= 9 && size <= 54 && size % 9 == 0, type, size);
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < 9; column++) addSlot(new Slot(inventory.getInventory(), column + row * 9, 8 + column * 18, 18 + row * 18));
            }
            return;
        }
        int[][] positions = positions(type, size);
        for (int index = 0; index < positions.length; index++) {
            addSlot(new Slot(inventory.getInventory(), index, positions[index][0], positions[index][1]));
        }
    }

    private void addPlayerSlots(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
    }

    private static ScreenHandlerType<?> screenType(CraftInventory inventory) {
        return switch (inventory.getType()) {
            case PLAYER, CHEST, ENDER_CHEST, BARREL -> genericType(inventory.getSize());
            case SHULKER_BOX -> ScreenHandlerType.SHULKER_BOX;
            case DISPENSER, DROPPER -> ScreenHandlerType.GENERIC_3X3;
            case FURNACE -> ScreenHandlerType.FURNACE;
            case WORKBENCH -> ScreenHandlerType.CRAFTING;
            case ENCHANTING -> ScreenHandlerType.ENCHANTMENT;
            case BREWING -> ScreenHandlerType.BREWING_STAND;
            case ANVIL -> ScreenHandlerType.ANVIL;
            case SMITHING, SMITHING_NEW -> ScreenHandlerType.SMITHING;
            case BEACON -> ScreenHandlerType.BEACON;
            case HOPPER -> ScreenHandlerType.HOPPER;
            case BLAST_FURNACE -> ScreenHandlerType.BLAST_FURNACE;
            case LECTERN -> ScreenHandlerType.LECTERN;
            case SMOKER -> ScreenHandlerType.SMOKER;
            case LOOM -> ScreenHandlerType.LOOM;
            case CARTOGRAPHY -> ScreenHandlerType.CARTOGRAPHY_TABLE;
            case GRINDSTONE -> ScreenHandlerType.GRINDSTONE;
            case STONECUTTER -> ScreenHandlerType.STONECUTTER;
            default -> throw new IllegalArgumentException("Unsupported custom inventory type: " + inventory.getType());
        };
    }

    private static ScreenHandlerType<?> genericType(int size) {
        return switch (size) {
            case 9 -> ScreenHandlerType.GENERIC_9X1;
            case 18 -> ScreenHandlerType.GENERIC_9X2;
            case 27 -> ScreenHandlerType.GENERIC_9X3;
            case 36 -> ScreenHandlerType.GENERIC_9X4;
            case 41 -> ScreenHandlerType.GENERIC_9X4;
            case 45 -> ScreenHandlerType.GENERIC_9X5;
            case 54 -> ScreenHandlerType.GENERIC_9X6;
            default -> throw new IllegalArgumentException("Unsupported generic inventory size: " + size);
        };
    }

    private static boolean isGeneric(InventoryType type) {
        return type == InventoryType.PLAYER || type == InventoryType.CHEST || type == InventoryType.ENDER_CHEST
                || type == InventoryType.BARREL || type == InventoryType.SHULKER_BOX;
    }

    private static int topSlotCount(InventoryType type, int size) {
        return type == InventoryType.PLAYER ? 36 : size;
    }

    private static int propertyCount(InventoryType type) {
        return switch (type) {
            case FURNACE, BLAST_FURNACE, SMOKER -> 4;
            case BREWING -> 2;
            case ENCHANTING -> 3;
            case ANVIL, LECTERN, LOOM, STONECUTTER -> 1;
            case BEACON -> 3;
            default -> 0;
        };
    }

    private static int[][] positions(InventoryType type, int size) {
        return switch (type) {
            case DISPENSER, DROPPER -> grid3x3(size);
            case HOPPER -> linear(size, 44, 20, 18);
            case FURNACE, BLAST_FURNACE, SMOKER -> requirePositions(size, type,
                    new int[][] {{56, 17}, {56, 53}, {116, 35}});
            case BREWING -> requirePositions(size, type,
                    new int[][] {{56, 51}, {79, 58}, {102, 51}, {79, 17}, {17, 17}});
            case WORKBENCH -> requirePositions(size, type,
                    new int[][] {{124, 35}, {30, 17}, {48, 17}, {66, 17}, {30, 35}, {48, 35}, {66, 35}, {30, 53}, {48, 53}, {66, 53}});
            case ENCHANTING -> requirePositions(size, type, new int[][] {{15, 47}, {35, 47}});
            case ANVIL -> requirePositions(size, type, new int[][] {{27, 47}, {76, 47}, {134, 47}});
            case SMITHING, SMITHING_NEW -> requirePositions(size, type, new int[][] {{8, 48}, {26, 48}, {44, 48}, {98, 48}});
            case BEACON, LECTERN -> requirePositions(size, type, new int[][] {{136, 110}});
            case LOOM -> requirePositions(size, type, new int[][] {{13, 26}, {34, 26}, {55, 26}, {143, 57}});
            case CARTOGRAPHY -> requirePositions(size, type, new int[][] {{15, 15}, {15, 52}, {145, 52}});
            case GRINDSTONE -> requirePositions(size, type, new int[][] {{49, 19}, {49, 40}, {129, 34}});
            case STONECUTTER -> requirePositions(size, type, new int[][] {{20, 33}, {143, 33}});
            default -> throw new IllegalArgumentException("Unsupported custom inventory type: " + type);
        };
    }

    private static int[][] grid3x3(int size) {
        requireSize(size == 9, InventoryType.DISPENSER, size);
        int[][] positions = new int[9][2];
        for (int index = 0; index < positions.length; index++) positions[index] = new int[] { 62 + index % 3 * 18, 17 + index / 3 * 18 };
        return positions;
    }

    private static int[][] linear(int size, int x, int y, int step) {
        requireSize(size == 5, InventoryType.HOPPER, size);
        int[][] positions = new int[size][2];
        for (int index = 0; index < size; index++) positions[index] = new int[] { x + index * step, y };
        return positions;
    }

    private static int[][] requirePositions(int size, InventoryType type, int[][] positions) {
        requireSize(size == positions.length, type, size);
        return positions;
    }

    private static void requireSize(boolean valid, InventoryType type, int size) {
        if (!valid) throw new IllegalArgumentException("Invalid size for " + type + " inventory: " + size);
    }
}
