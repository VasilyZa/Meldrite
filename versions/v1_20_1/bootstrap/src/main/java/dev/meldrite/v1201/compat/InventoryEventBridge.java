package dev.meldrite.v1201.compat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftContainerScreenHandler;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.SmithItemEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.SmithingInventory;

/** Dispatches Bukkit inventory lifecycle events at the native handler boundary. */
public final class InventoryEventBridge {
    private static final ThreadLocal<Deque<InventoryCloseEvent.Reason>> CLOSE_REASONS =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Integer> SUPPRESSED_CLOSE_EVENTS = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Set<ScreenHandler>> PREPARING_RESULTS = ThreadLocal.withInitial(
            () -> java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));

    private InventoryEventBridge() {}

    public static void prepareResult(PlayerEntity player, ScreenHandler handler, int slot) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || Bukkit.getServer() == null) return;
        bukkitView(serverPlayer, handler);
        prepareResult(handler, slot);
    }

    public static void prepareResult(ScreenHandler handler, int slot) {
        if (Bukkit.getServer() == null) return;
        var view = cachedView(handler);
        if (view == null) return;
        var preparing = PREPARING_RESULTS.get();
        if (!preparing.add(handler)) return;
        try {
            var nativeOutput = handler.getSlot(slot).getStack();
            var output = nativeOutput.isEmpty() ? null : CraftItemStack.asBukkitCopy(nativeOutput);
            org.bukkit.event.inventory.PrepareInventoryResultEvent event;
            if (handler instanceof net.minecraft.screen.AnvilScreenHandler) event = new org.bukkit.event.inventory.PrepareAnvilEvent(view, output);
            else if (handler instanceof net.minecraft.screen.SmithingScreenHandler) event = new org.bukkit.event.inventory.PrepareSmithingEvent(view, output);
            else if (handler instanceof net.minecraft.screen.GrindstoneScreenHandler) event = new org.bukkit.event.inventory.PrepareGrindstoneEvent(view, output);
            else event = new com.destroystokyo.paper.event.inventory.PrepareResultEvent(view, output);
            Bukkit.getPluginManager().callEvent(event);
            var resultSlot = handler.getSlot(slot);
            resultSlot.inventory.setStack(resultSlot.getIndex(), CraftItemStack.asNMSCopy(event.getResult()));
            handler.sendContentUpdates();
        } finally {
            preparing.remove(handler);
            if (preparing.isEmpty()) PREPARING_RESULTS.remove();
        }
    }

    public static net.minecraft.item.ItemStack prepareCraft(PlayerEntity player, ScreenHandler handler,
            net.minecraft.inventory.RecipeInputInventory input, net.minecraft.inventory.CraftingResultInventory result,
            net.minecraft.item.ItemStack output, net.minecraft.world.World world) {
        if (!(player instanceof ServerPlayerEntity serverPlayer) || Bukkit.getServer() == null) return output;
        var view = bukkitView(serverPlayer, handler);
        result.setStack(0, output);
        result.setLastRecipe(world.getRecipeManager().getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, input, world).orElse(null));
        var recipe = result.getLastRecipe();
        var event = new org.bukkit.event.inventory.PrepareItemCraftEvent((CraftingInventory) view.getTopInventory(),
                view, recipe instanceof net.minecraft.recipe.RepairItemRecipe);
        Bukkit.getPluginManager().callEvent(event);
        return result.getStack(0);
    }

    public static InventoryOpenEvent callOpenEvent(InventoryView view) {
        return callOpenEvent(view, event -> Bukkit.getPluginManager().callEvent(event));
    }

    public static InventoryOpenEvent callOpenEvent(InventoryView view, Consumer<InventoryOpenEvent> dispatcher) {
        InventoryOpenEvent event = new InventoryOpenEvent(java.util.Objects.requireNonNull(view, "view"));
        java.util.Objects.requireNonNull(dispatcher, "dispatcher").accept(event);
        return event;
    }

    public static InventoryCloseEvent callCloseEvent(InventoryView view, InventoryCloseEvent.Reason reason) {
        return callCloseEvent(view, reason, event -> Bukkit.getPluginManager().callEvent(event));
    }

    public static InventoryCloseEvent callCloseEvent(InventoryView view, InventoryCloseEvent.Reason reason,
            Consumer<InventoryCloseEvent> dispatcher) {
        InventoryCloseEvent event = new InventoryCloseEvent(java.util.Objects.requireNonNull(view, "view"),
                java.util.Objects.requireNonNull(reason, "reason"));
        java.util.Objects.requireNonNull(dispatcher, "dispatcher").accept(event);
        return event;
    }

    public static boolean dispatchClickEvent(ServerPlayerEntity player, ClickSlotC2SPacket packet) {
        return dispatchClickEvent(player, player.currentScreenHandler, packet,
                event -> Bukkit.getPluginManager().callEvent(event));
    }

    public static boolean dispatchClickEvent(ServerPlayerEntity player, ClickSlotC2SPacket packet,
            Consumer<InventoryClickEvent> dispatcher) {
        return dispatchClickEvent(player, player.currentScreenHandler, packet, dispatcher);
    }

    public static boolean dispatchClickEvent(ServerPlayerEntity player, ScreenHandler handler,
            ClickSlotC2SPacket packet, Consumer<InventoryClickEvent> dispatcher) {
        java.util.Objects.requireNonNull(player, "player");
        java.util.Objects.requireNonNull(handler, "handler");
        java.util.Objects.requireNonNull(packet, "packet");
        if (packet.getActionType() == SlotActionType.QUICK_CRAFT
                || packet.getSlot() < -1 && packet.getSlot() != -999
                || packet.getSyncId() != handler.syncId) return false;
        InventoryView view = bukkitView(player, handler);
        int rawSlot = packet.getSlot();
        if (rawSlot >= 0 && rawSlot >= handler.slots.size()) return false;
        ClickType click = clickType(packet.getActionType(), rawSlot, packet.getButton());
        InventoryAction action = inventoryAction(handler, player, view, packet);
        SlotType slotType = view.getSlotType(rawSlot);
        InventoryClickEvent event = click == ClickType.NUMBER_KEY
                ? new InventoryClickEvent(view, slotType, rawSlot, click, action, packet.getButton())
                : new InventoryClickEvent(view, slotType, rawSlot, click, action);
        if (rawSlot == 0 && view.getTopInventory() instanceof CraftingInventory crafting
                && crafting.getRecipe() != null) {
            event = click == ClickType.NUMBER_KEY
                    ? new CraftItemEvent(crafting.getRecipe(), view, slotType, rawSlot, click, action, packet.getButton())
                    : new CraftItemEvent(crafting.getRecipe(), view, slotType, rawSlot, click, action);
        } else if (rawSlot == 3 && view.getTopInventory() instanceof SmithingInventory smithing
                && smithing.getResult() != null) {
            event = click == ClickType.NUMBER_KEY
                    ? new SmithItemEvent(view, slotType, rawSlot, click, action, packet.getButton())
                    : new SmithItemEvent(view, slotType, rawSlot, click, action);
        }
        java.util.Objects.requireNonNull(dispatcher, "dispatcher").accept(event);
        return event.isCancelled();
    }

    /**
     * Handles the three packet stages used by the native quick-craft protocol.
     * The native algorithm is retained for slot eligibility and stack sizing;
     * only the final writes are deferred until Bukkit has seen the drag event.
     */
    public static void dispatchQuickCraft(ScreenHandler handler, int slotIndex, int button,
            ServerPlayerEntity player) {
        dispatchQuickCraft(handler, slotIndex, button, player,
                event -> Bukkit.getPluginManager().callEvent(event));
    }

    static void dispatchQuickCraft(ScreenHandler handler, int slotIndex, int button,
            ServerPlayerEntity player, Consumer<InventoryDragEvent> dispatcher) {
        dispatchQuickCraft(handler, slotIndex, button, player, dispatcher,
                stack -> player.getInventory().offerOrDrop(stack));
    }

    static void dispatchQuickCraft(ScreenHandler handler, int slotIndex, int button,
            ServerPlayerEntity player, Consumer<InventoryDragEvent> dispatcher,
            Consumer<net.minecraft.item.ItemStack> closedCursorReturn) {
        if (!(handler instanceof ScreenHandlerQuickCraftAccess state)) {
            throw new IllegalStateException("SCREEN_HANDLER_QUICK_CRAFT_MIXIN_MISSING");
        }
        int previousStage = state.meldrite$getQuickCraftStage();
        int stage = ScreenHandler.unpackQuickCraftStage(button);
        state.meldrite$setQuickCraftStage(stage);
        if ((previousStage != 1 || stage != 2) && previousStage != stage) {
            state.meldrite$endQuickCraft();
            return;
        }
        if (handler.getCursorStack().isEmpty()) {
            state.meldrite$endQuickCraft();
            return;
        }
        if (stage == 0) {
            int type = ScreenHandler.unpackQuickCraftButton(button);
            state.meldrite$setQuickCraftButton(type);
            if (ScreenHandler.shouldQuickCraftContinue(type, player)) {
                state.meldrite$setQuickCraftStage(1);
                state.meldrite$getQuickCraftSlots().clear();
            } else {
                state.meldrite$endQuickCraft();
            }
            return;
        }
        if (stage == 1) {
            if (slotIndex < 0 || slotIndex >= handler.slots.size()) return;
            Slot slot = handler.getSlot(slotIndex);
            net.minecraft.item.ItemStack cursor = handler.getCursorStack();
            Set<Slot> slots = state.meldrite$getQuickCraftSlots();
            if (ScreenHandler.canInsertItemIntoSlot(slot, cursor, true)
                    && slot.canInsert(cursor)
                    && (state.meldrite$getQuickCraftButton() == 2 || cursor.getCount() > slots.size())
                    && handler.canInsertIntoSlot(slot)) {
                slots.add(slot);
            }
            return;
        }
        if (stage != 2) {
            state.meldrite$endQuickCraft();
            return;
        }
        Set<Slot> slots = state.meldrite$getQuickCraftSlots();
        if (slots.isEmpty()) {
            state.meldrite$endQuickCraft();
            return;
        }
        net.minecraft.item.ItemStack proposed = handler.getCursorStack().copy();
        if (proposed.isEmpty()) {
            state.meldrite$endQuickCraft();
            return;
        }
        int remaining = handler.getCursorStack().getCount();
        Map<Integer, net.minecraft.item.ItemStack> draggedSlots = new HashMap<>();
        for (Slot slot : slots) {
            net.minecraft.item.ItemStack cursor = handler.getCursorStack();
            if (slot != null && ScreenHandler.canInsertItemIntoSlot(slot, cursor, true)
                    && slot.canInsert(cursor)
                    && (state.meldrite$getQuickCraftButton() == 2 || cursor.getCount() >= slots.size())
                    && handler.canInsertIntoSlot(slot)) {
                int existing = slot.hasStack() ? slot.getStack().getCount() : 0;
                int max = Math.min(proposed.getMaxCount(), slot.getMaxItemCount(proposed));
                int amount = Math.min(ScreenHandler.calculateStackSize(slots,
                        state.meldrite$getQuickCraftButton(), proposed) + existing, max);
                remaining -= amount - existing;
                draggedSlots.put(slot.id, proposed.copyWithCount(amount));
            }
        }

        CraftInventoryView view = (CraftInventoryView) bukkitView(player, handler);
        org.bukkit.inventory.ItemStack newCursor = CraftItemStack.asCraftMirror(proposed);
        newCursor.setAmount(remaining);
        Map<Integer, org.bukkit.inventory.ItemStack> eventItems = new HashMap<>();
        for (Map.Entry<Integer, net.minecraft.item.ItemStack> entry : draggedSlots.entrySet()) {
            eventItems.put(entry.getKey(), CraftItemStack.asBukkitCopy(entry.getValue()));
        }
        net.minecraft.item.ItemStack oldCursor = handler.getCursorStack();
        handler.setCursorStack(CraftItemStack.asNMSCopy(newCursor));
        InventoryDragEvent event = new InventoryDragEvent(view,
                newCursor.getType() != org.bukkit.Material.AIR ? newCursor : null,
                CraftItemStack.asBukkitCopy(oldCursor), state.meldrite$getQuickCraftButton() == 1, eventItems);
        java.util.Objects.requireNonNull(dispatcher, "dispatcher").accept(event);
        boolean handlerClosed = player.currentScreenHandler != handler && handler.getCursorStack().isEmpty();
        boolean needsUpdate = event.getResult() != org.bukkit.event.Event.Result.DEFAULT;
        if (event.getResult() != org.bukkit.event.Event.Result.DENY) {
            for (Map.Entry<Integer, net.minecraft.item.ItemStack> entry : draggedSlots.entrySet()) {
                view.setItem(entry.getKey(), CraftItemStack.asBukkitCopy(entry.getValue()));
            }
            if (!handlerClosed) {
                handler.setCursorStack(CraftItemStack.asNMSCopy(event.getCursor()));
                needsUpdate = true;
            }
        } else if (handlerClosed) {
            int stagedCount = oldCursor.getCount() - newCursor.getAmount();
            if (stagedCount > 0) {
                java.util.Objects.requireNonNull(closedCursorReturn, "closedCursorReturn")
                        .accept(oldCursor.copyWithCount(stagedCount));
            }
        } else {
            if (player.currentScreenHandler == handler) {
                handler.setCursorStack(oldCursor);
            }
        }
        if (player.currentScreenHandler == handler && needsUpdate) handler.updateToClient();
        state.meldrite$endQuickCraft();
    }

    private static ClickType clickType(SlotActionType type, int rawSlot, int button) {
        return switch (type) {
            case PICKUP -> button == 0 ? ClickType.LEFT : button == 1 ? ClickType.RIGHT : ClickType.UNKNOWN;
            case QUICK_MOVE -> button == 0 ? ClickType.SHIFT_LEFT : button == 1 ? ClickType.SHIFT_RIGHT : ClickType.UNKNOWN;
            case SWAP -> (button >= 0 && button < 9) || button == 40
                    ? button == 40 ? ClickType.SWAP_OFFHAND : ClickType.NUMBER_KEY : ClickType.UNKNOWN;
            case CLONE -> button == 2 ? ClickType.MIDDLE : ClickType.UNKNOWN;
            case THROW -> rawSlot >= 0 ? button == 0 ? ClickType.DROP : button == 1 ? ClickType.CONTROL_DROP : ClickType.UNKNOWN
                    : button == 1 ? ClickType.RIGHT : ClickType.LEFT;
            case PICKUP_ALL -> ClickType.DOUBLE_CLICK;
            default -> ClickType.UNKNOWN;
        };
    }

    private static InventoryAction inventoryAction(ScreenHandler handler, ServerPlayerEntity player,
            InventoryView view, ClickSlotC2SPacket packet) {
        int rawSlot = packet.getSlot();
        int button = packet.getButton();
        if (packet.getActionType() == SlotActionType.PICKUP) {
            if (button != 0 && button != 1) return InventoryAction.UNKNOWN;
            if (rawSlot == -999) {
                if (handler.getCursorStack().isEmpty()) return InventoryAction.NOTHING;
                return button == 1 ? InventoryAction.DROP_ONE_CURSOR : InventoryAction.DROP_ALL_CURSOR;
            }
            if (rawSlot < 0) return InventoryAction.NOTHING;
            Slot slot = handler.getSlot(rawSlot);
            net.minecraft.item.ItemStack clicked = slot.getStack();
            net.minecraft.item.ItemStack cursor = handler.getCursorStack();
            if (clicked.isEmpty()) {
                if (!cursor.isEmpty()) return button == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
            } else if (slot.canTakeItems(player)) {
                if (cursor.isEmpty()) return button == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                if (slot.canInsert(cursor)) {
                    if (net.minecraft.item.ItemStack.areItemsEqual(clicked, cursor)) {
                        int toPlace = button == 0 ? cursor.getCount() : 1;
                        toPlace = Math.min(toPlace, clicked.getMaxCount() - clicked.getCount());
                        toPlace = Math.min(toPlace, slot.getMaxItemCount(clicked) - clicked.getCount());
                        if (toPlace == 1) return InventoryAction.PLACE_ONE;
                        if (toPlace == cursor.getCount()) return InventoryAction.PLACE_ALL;
                        if (toPlace < 0) return toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                        if (toPlace != 0) return InventoryAction.PLACE_SOME;
                    } else if (cursor.getCount() <= slot.getMaxItemCount(cursor)) {
                        return InventoryAction.SWAP_WITH_CURSOR;
                    }
                } else if (net.minecraft.item.ItemStack.areItemsEqual(cursor, clicked)
                        && clicked.getCount() >= 0
                        && clicked.getCount() + cursor.getCount() <= cursor.getMaxCount()) {
                    return InventoryAction.PICKUP_ALL;
                }
            }
            return InventoryAction.NOTHING;
        }
        return switch (packet.getActionType()) {
            case QUICK_MOVE -> button != 0 && button != 1 ? InventoryAction.UNKNOWN
                    : rawSlot < 0 ? InventoryAction.NOTHING
                    : handler.getSlot(rawSlot).canTakeItems(player) && handler.getSlot(rawSlot).hasStack()
                            ? InventoryAction.MOVE_TO_OTHER_INVENTORY : InventoryAction.NOTHING;
            case SWAP -> {
                if (!((button >= 0 && button < 9) || button == 40)) yield InventoryAction.UNKNOWN;
                Slot clicked = handler.getSlot(rawSlot);
                if (!clicked.canTakeItems(player)) yield InventoryAction.NOTHING;
                net.minecraft.item.ItemStack hotbar = player.getInventory().getStack(button);
                boolean cleanSwap = hotbar.isEmpty()
                        || (clicked.inventory == player.getInventory() && clicked.canInsert(hotbar));
                if (clicked.hasStack()) yield cleanSwap
                        ? InventoryAction.HOTBAR_SWAP : InventoryAction.HOTBAR_MOVE_AND_READD;
                yield !hotbar.isEmpty() && clicked.canInsert(hotbar)
                        ? InventoryAction.HOTBAR_SWAP : InventoryAction.NOTHING;
            }
            case CLONE -> button != 2 ? InventoryAction.UNKNOWN
                    : rawSlot >= 0 && handler.getSlot(rawSlot).hasStack()
                            && player.getAbilities().creativeMode && handler.getCursorStack().isEmpty()
                                    ? InventoryAction.CLONE_STACK : InventoryAction.NOTHING;
            case THROW -> {
                if (rawSlot < 0) yield InventoryAction.NOTHING;
                if (button != 0 && button != 1) yield InventoryAction.UNKNOWN;
                Slot slot = handler.getSlot(rawSlot);
                yield slot.hasStack() && slot.canTakeItems(player) && !slot.getStack().isEmpty()
                        && !slot.getStack().isOf(net.minecraft.item.Items.AIR)
                                ? button == 1 ? InventoryAction.DROP_ALL_SLOT : InventoryAction.DROP_ONE_SLOT
                                : InventoryAction.NOTHING;
            }
            case PICKUP_ALL -> {
                if (rawSlot < 0 || handler.getCursorStack().isEmpty()) yield InventoryAction.NOTHING;
                var material = org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers
                        .getMaterial(handler.getCursorStack().getItem());
                yield view.getTopInventory().contains(material) || view.getBottomInventory().contains(material)
                        ? InventoryAction.COLLECT_TO_CURSOR : InventoryAction.NOTHING;
            }
            default -> InventoryAction.UNKNOWN;
        };
    }

    public static NamedScreenHandlerFactory wrapOpenFactory(ServerPlayerEntity player,
            NamedScreenHandlerFactory factory) {
        return wrapOpenFactory(player, factory, InventoryEventBridge::callOpenEvent);
    }

    public static NamedScreenHandlerFactory wrapOpenFactory(ServerPlayerEntity player,
            NamedScreenHandlerFactory factory, Function<InventoryView, InventoryOpenEvent> openEventDispatcher) {
        return new NamedScreenHandlerFactory() {
            private Text titleOverride;

            @Override
            public ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inventory,
                    PlayerEntity menuPlayer) {
                ScreenHandler handler = factory.createMenu(syncId, inventory, menuPlayer);
                if (handler == null || !(menuPlayer instanceof ServerPlayerEntity serverPlayer)) return handler;
                InventoryView view = view(serverPlayer, handler);
                view.setTitle(org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromComponent(factory.getDisplayName()));
                InventoryOpenEvent event = java.util.Objects.requireNonNull(openEventDispatcher, "openEventDispatcher")
                        .apply(view);
                if (event.isCancelled()) {
                    withCloseEventsSuppressed(() -> handler.onClosed(menuPlayer));
                    unregisterView(view);
                    return null;
                }
                if (event.titleOverride() != null) {
                    String json = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
                            .serialize(event.titleOverride());
                    titleOverride = Text.Serializer.fromJson(json);
                    view.setTitle(titleOverride.getString());
                }
                cacheView(handler, view);
                return handler;
            }

            @Override
            public Text getDisplayName() {
                return titleOverride == null ? factory.getDisplayName() : titleOverride;
            }
        };
    }

    public static void pushCloseReason(InventoryCloseEvent.Reason reason) {
        CLOSE_REASONS.get().push(java.util.Objects.requireNonNull(reason, "reason"));
    }

    public static void popCloseReason() {
        Deque<InventoryCloseEvent.Reason> reasons = CLOSE_REASONS.get();
        if (reasons.isEmpty()) throw new IllegalStateException("Inventory close reason stack is empty");
        reasons.pop();
        if (reasons.isEmpty()) CLOSE_REASONS.remove();
    }

    public static void withCloseEventsSuppressed(Runnable action) {
        int depth = SUPPRESSED_CLOSE_EVENTS.get();
        SUPPRESSED_CLOSE_EVENTS.set(depth + 1);
        try {
            action.run();
        } finally {
            if (depth == 0) SUPPRESSED_CLOSE_EVENTS.remove();
            else SUPPRESSED_CLOSE_EVENTS.set(depth);
        }
    }

    public static void fireCloseEvent(PlayerEntity player, ScreenHandler handler) {
        if (SUPPRESSED_CLOSE_EVENTS.get() > 0) return;
        if (!(player instanceof ServerPlayerEntity serverPlayer) || handler == serverPlayer.playerScreenHandler) return;
        InventoryView view = bukkitView(serverPlayer, handler);
        Deque<InventoryCloseEvent.Reason> reasons = CLOSE_REASONS.get();
        InventoryCloseEvent.Reason reason = reasons.isEmpty() ? InventoryCloseEvent.Reason.UNKNOWN : reasons.peek();
        try {
            callCloseEvent(view, reason);
        } finally {
            unregisterView(view);
            if (reasons.isEmpty()) CLOSE_REASONS.remove();
        }
    }

    public static InventoryView bukkitView(ScreenHandler handler) {
        InventoryView cached = cachedView(handler);
        if (cached != null) return cached;
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            if (!(player instanceof CraftHumanEntity human)
                    || !(human.getHandle() instanceof ServerPlayerEntity serverPlayer)
                    || (serverPlayer.currentScreenHandler != handler && serverPlayer.playerScreenHandler != handler)) continue;
            return bukkitView(serverPlayer, handler);
        }
        throw new IllegalStateException("SCREEN_HANDLER_VIEW_NOT_BOUND_TO_PLAYER");
    }

    public static InventoryView bukkitView(ServerPlayerEntity player, ScreenHandler handler) {
        InventoryView cached = cachedView(handler);
        if (cached != null) return cached;
        InventoryView created = createView(player, handler);
        cacheView(handler, created);
        return created;
    }

    private static InventoryView view(ServerPlayerEntity player, ScreenHandler handler) {
        InventoryView cached = cachedView(handler);
        return cached == null ? createView(player, handler) : cached;
    }

    private static InventoryView createView(ServerPlayerEntity player, ScreenHandler handler) {
        HumanEntity human = (HumanEntity) NativeEntityExtensions.bukkit(player);
        if (!(human instanceof CraftHumanEntity craftHuman)) {
            throw new IllegalStateException("Native player has no CraftHumanEntity wrapper");
        }
        if (handler instanceof CraftContainerScreenHandler custom) {
            CraftInventory top = custom.getCraftInventory();
            return new CraftInventoryView(human, top, craftHuman.getInventory(), top.getType(), custom.getTitle(), handler);
        }
        return new CraftInventoryView(human, handler, (CraftInventoryPlayer) craftHuman.getInventory());
    }

    private static InventoryView cachedView(ScreenHandler handler) {
        if (!(handler instanceof ScreenHandlerBukkitViewAccess access)) {
            throw new IllegalStateException("SCREEN_HANDLER_BUKKIT_VIEW_MIXIN_MISSING");
        }
        return access.meldrite$getBukkitView();
    }

    private static void cacheView(ScreenHandler handler, InventoryView view) {
        if (!(handler instanceof ScreenHandlerBukkitViewAccess access)) {
            throw new IllegalStateException("SCREEN_HANDLER_BUKKIT_VIEW_MIXIN_MISSING");
        }
        CraftInventoryView cached = access.meldrite$getBukkitView();
        if (cached != null && cached != view) throw new IllegalStateException("SCREEN_HANDLER_VIEW_ALREADY_BOUND");
        if (!(view instanceof CraftInventoryView craftView)) {
            throw new IllegalArgumentException("Screen handler view is not a CraftInventoryView");
        }
        access.meldrite$setBukkitView(craftView);
    }

    private static void unregisterView(InventoryView view) {
        if (view.getTopInventory() instanceof CraftInventory top) {
            CraftInventory.unregisterViewer(top.getInventory(), view.getPlayer());
        }
        if (view.getBottomInventory() instanceof CraftInventory bottom) {
            CraftInventory.unregisterViewer(bottom.getInventory(), view.getPlayer());
        }
    }
}
