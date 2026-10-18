package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.Objects;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

/** Live view of a player's current native screen handler. */
public final class CraftInventoryView extends InventoryView {
    private final HumanEntity player;
    private final Inventory top;
    private final Inventory bottom;
    private final InventoryType type;
    private final ScreenHandler handler;
    private String title;

    public CraftInventoryView(HumanEntity player, Inventory top, Inventory bottom, InventoryType type, String title) {
        this(player, top, bottom, type, title, null);
    }

    public CraftInventoryView(HumanEntity player, Inventory top, ScreenHandler handler) {
        this(player, top, player.getInventory(), inventoryType(handler), title(top, inventoryType(handler)), handler);
    }

    public CraftInventoryView(HumanEntity player, Inventory top, Inventory bottom, InventoryType type,
            String title, ScreenHandler handler) {
        this(player, top, bottom, type, title, handler, true);
    }

    public CraftInventoryView(HumanEntity player, Inventory top, Inventory bottom, InventoryType type,
            String title, ScreenHandler handler, boolean registerViewers) {
        this.player = Objects.requireNonNull(player, "player");
        this.top = Objects.requireNonNull(top, "top");
        this.bottom = Objects.requireNonNull(bottom, "bottom");
        this.type = Objects.requireNonNull(type, "type");
        this.handler = handler;
        this.title = title == null ? type.getDefaultTitle() : title;
        if (registerViewers) {
            CraftInventory.registerViewer(top instanceof CraftInventory craft ? craft.getInventory() : null, player);
            CraftInventory.registerViewer(bottom instanceof CraftInventory craft ? craft.getInventory() : null, player);
        }
    }

    public ScreenHandler getHandle() { return handler; }

    public CraftInventoryView(HumanEntity player, ScreenHandler handler, CraftInventoryPlayer bottom) {
        this(player, topInventory(player, handler, bottom), handler);
    }

    @Override public Inventory getTopInventory() { return top; }
    @Override public Inventory getBottomInventory() { return bottom; }
    @Override public HumanEntity getPlayer() { return player; }
    @Override public InventoryType getType() { return type; }
    @Override public String getTitle() { return title; }
    @Override public String getOriginalTitle() { return title; }
    @Override public void setTitle(String title) { this.title = Objects.requireNonNull(title, "title"); }

    @Override public void setItem(int rawSlot, org.bukkit.inventory.ItemStack stack) {
        if (handler != null && rawSlot >= 0 && rawSlot < handler.slots.size()) {
            handler.getSlot(rawSlot).setStack(CraftItemStack.asNMSCopy(stack));
            return;
        }
        super.setItem(rawSlot, stack);
    }

    @Override public org.bukkit.inventory.ItemStack getItem(int rawSlot) {
        if (handler != null && rawSlot >= 0 && rawSlot < handler.slots.size()) {
            return CraftItemStack.asCraftMirror(handler.getSlot(rawSlot).getStack());
        }
        return super.getItem(rawSlot);
    }

    private static Inventory topInventory(HumanEntity player, ScreenHandler handler, CraftInventoryPlayer bottom) {
        if (handler instanceof net.minecraft.screen.PlayerScreenHandler playerHandler) {
            return new CraftInventoryCrafting(playerHandler.getCraftingInput(),
                    ((dev.meldrite.v1201.hook.PlayerCraftingResultAccess) playerHandler).meldrite$getResult(), player);
        }
        if (handler instanceof net.minecraft.screen.CraftingScreenHandler) {
            var crafting = (dev.meldrite.v1201.hook.CraftingScreenHandlerAccess) handler;
            return new CraftInventoryCrafting(crafting.meldrite$getInput(), crafting.meldrite$getResult(), player);
        }
        var input = handler.slots.isEmpty() ? null : handler.getSlot(0).inventory;
        if (handler instanceof net.minecraft.screen.ForgingScreenHandler forging) {
            var context = ((dev.meldrite.v1201.hook.ForgingScreenHandlerPaperAccessMixin) handler).meldrite$getContext();
            var location = context.get((world, pos) -> world instanceof net.minecraft.server.world.ServerWorld serverWorld
                    ? new org.bukkit.Location(dev.meldrite.v1201.compat.NativeEntityExtensions.world(serverWorld), pos.getX(), pos.getY(), pos.getZ()) : null).orElse(null);
            var result = handler.getSlot(forging.getResultSlotIndex()).inventory;
            if (handler instanceof net.minecraft.screen.AnvilScreenHandler anvil) return new CraftInventoryAnvil(location, input, result, anvil);
            if (handler instanceof net.minecraft.screen.SmithingScreenHandler) return new CraftInventorySmithing(location, input, (net.minecraft.inventory.CraftingResultInventory) result);
        }
        if (handler instanceof net.minecraft.screen.StonecutterScreenHandler) return new CraftInventoryStonecutter(input, handler.getSlot(1).inventory);
        if (handler instanceof net.minecraft.screen.CartographyTableScreenHandler) return new CraftInventoryCartography(input, handler.getSlot(2).inventory);
        if (handler instanceof net.minecraft.screen.GrindstoneScreenHandler) return new CraftInventoryGrindstone(input, handler.getSlot(2).inventory);
        if (handler instanceof net.minecraft.screen.LoomScreenHandler) return new CraftInventoryLoom(input, handler.getSlot(3).inventory);
        if (handler instanceof net.minecraft.screen.EnchantmentScreenHandler) return new CraftInventoryEnchanting(input);
        if (handler instanceof net.minecraft.screen.BeaconScreenHandler) return new CraftInventoryBeacon(input);
        if (handler instanceof net.minecraft.screen.MerchantScreenHandler) return new CraftInventoryMerchant(
                ((dev.meldrite.v1201.hook.MerchantScreenHandlerAccess) handler).meldrite$getMerchant(), (net.minecraft.village.MerchantInventory) input);
        net.minecraft.inventory.Inventory nativeInventory = firstNonPlayerInventory(handler, bottom.getInventory());
        return nativeInventory == null ? bottom : new CraftInventory(nativeInventory, player, inventoryType(handler));
    }

    private static net.minecraft.inventory.Inventory firstNonPlayerInventory(ScreenHandler handler, net.minecraft.entity.player.PlayerInventory playerInventory) {
        for (net.minecraft.screen.slot.Slot slot : handler.slots) {
            if (slot.inventory != playerInventory) return slot.inventory;
        }
        return null;
    }

    public static InventoryType inventoryType(ScreenHandler handler) {
        if (handler instanceof net.minecraft.screen.PlayerScreenHandler) return InventoryType.CRAFTING;
        // getType() throws for valid menus constructed without a registered factory type.
        ScreenHandlerType<?> type = ((dev.meldrite.v1201.compat.ScreenHandlerBukkitViewAccess) handler)
                .meldrite$getNullableType();
        if (type == ScreenHandlerType.CRAFTING) return InventoryType.WORKBENCH;
        if (type == ScreenHandlerType.ANVIL) return InventoryType.ANVIL;
        if (type == ScreenHandlerType.BEACON) return InventoryType.BEACON;
        if (type == ScreenHandlerType.BLAST_FURNACE) return InventoryType.BLAST_FURNACE;
        if (type == ScreenHandlerType.BREWING_STAND) return InventoryType.BREWING;
        if (type == ScreenHandlerType.CARTOGRAPHY_TABLE) return InventoryType.CARTOGRAPHY;
        if (type == ScreenHandlerType.ENCHANTMENT) return InventoryType.ENCHANTING;
        if (type == ScreenHandlerType.FURNACE) return InventoryType.FURNACE;
        if (type == ScreenHandlerType.GRINDSTONE) return InventoryType.GRINDSTONE;
        if (type == ScreenHandlerType.HOPPER) return InventoryType.HOPPER;
        if (type == ScreenHandlerType.LECTERN) return InventoryType.LECTERN;
        if (type == ScreenHandlerType.LOOM) return InventoryType.LOOM;
        if (type == ScreenHandlerType.MERCHANT) return InventoryType.MERCHANT;
        if (type == ScreenHandlerType.SHULKER_BOX) return InventoryType.SHULKER_BOX;
        if (type == ScreenHandlerType.SMITHING) return InventoryType.SMITHING;
        if (type == ScreenHandlerType.SMOKER) return InventoryType.SMOKER;
        if (type == ScreenHandlerType.STONECUTTER) return InventoryType.STONECUTTER;
        if (type == ScreenHandlerType.GENERIC_3X3) return InventoryType.DISPENSER;
        if (type == ScreenHandlerType.GENERIC_9X1 || type == ScreenHandlerType.GENERIC_9X2
                || type == ScreenHandlerType.GENERIC_9X3 || type == ScreenHandlerType.GENERIC_9X4
                || type == ScreenHandlerType.GENERIC_9X5 || type == ScreenHandlerType.GENERIC_9X6) {
            return InventoryType.CHEST;
        }
        return InventoryType.CHEST;
    }

    private static String title(Inventory top, InventoryType fallback) {
        return top instanceof CraftInventory craft ? craft.getTitle() : fallback.getDefaultTitle();
    }
}
