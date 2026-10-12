package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.HungerManagerPaperAccess;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameMode;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;

public class CraftHumanEntity extends CraftLivingEntity implements org.bukkit.entity.HumanEntity {
    private volatile PlayerEntity nativePlayer;
    protected final org.bukkit.permissions.PermissibleBase perm;
    private boolean op;
    private final CraftInventoryPlayer inventory;
    private final CraftInventory enderChest;
    private ScreenHandler openedInventoryHandler;
    private CraftInventoryView openedInventoryView;
    public CraftHumanEntity(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) {
        super(delegate, context, entities);
        // Do not call the overridable CraftPlayer#getHandle() while the
        // subclass is being constructed.  The entity bridge may still be
        // publishing this facade, so the player lookup is not yet guaranteed
        // to be visible through the facade cache.
        this.nativePlayer = (PlayerEntity) entities.require(delegate.identity().handle());
        this.perm = new dev.meldrite.v1201.compat.FabricPermissible(this,
                () -> nativePlayer instanceof ServerPlayerEntity player ? player.getCommandSource() : null);
        this.inventory = new CraftInventoryPlayer(nativePlayer.getInventory());
        this.enderChest = new CraftInventory(nativePlayer.getEnderChestInventory(), this, org.bukkit.event.inventory.InventoryType.ENDER_CHEST);
    }
    @Override public PlayerEntity getHandle() { return (PlayerEntity) super.getHandle(); }
    @Override public void meldrite$rebindNative(net.minecraft.entity.Entity replacement) {
        if (!(replacement instanceof PlayerEntity next)) throw new IllegalArgumentException("HUMAN_REBIND_REQUIRES_PLAYER");
        super.meldrite$rebindNative(replacement);
        nativePlayer = next;
        inventory.meldrite$rebindInventory(next.getInventory());
        enderChest.meldrite$rebindInventory(next.getEnderChestInventory());
        openedInventoryHandler = null;
        openedInventoryView = null;
    }
    @Override public boolean isOp() {
        return nativePlayer instanceof ServerPlayerEntity player ? player.server.getPlayerManager().isOperator(player.getGameProfile()) : op;
    }
    @Override public void setOp(boolean value) {
        requireWritable();
        if (isOp() == value) return;
        if (nativePlayer instanceof ServerPlayerEntity player) {
            if (value) player.server.getPlayerManager().addToOperators(player.getGameProfile());
            else player.server.getPlayerManager().removeFromOperators(player.getGameProfile());
        } else op = value;
        perm.recalculatePermissions();
    }
    @Override public boolean isPermissionSet(String name) { return perm.isPermissionSet(name); }
    @Override public boolean isPermissionSet(org.bukkit.permissions.Permission permission) { return perm.isPermissionSet(permission); }
    @Override public boolean hasPermission(String name) { return perm.hasPermission(name); }
    @Override public boolean hasPermission(org.bukkit.permissions.Permission permission) { return perm.hasPermission(permission); }
    public net.fabricmc.fabric.api.util.TriState meldrite$bukkitPermissionValue(String name) {
        return ((dev.meldrite.v1201.compat.FabricPermissible) perm).bukkitValue(name);
    }
    @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin) { return perm.addAttachment(plugin); }
    @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, String name, boolean value) { return perm.addAttachment(plugin, name, value); }
    @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, int ticks) { return perm.addAttachment(plugin, ticks); }
    @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin plugin, String name, boolean value, int ticks) { return perm.addAttachment(plugin, name, value, ticks); }
    @Override public void removeAttachment(org.bukkit.permissions.PermissionAttachment attachment) { perm.removeAttachment(attachment); }
    @Override public void recalculatePermissions() { perm.recalculatePermissions(); }
    @Override public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { return perm.getEffectivePermissions(); }
    @Override public org.bukkit.inventory.MainHand getMainHand() { return getHandle().getMainArm() == net.minecraft.util.Arm.RIGHT ? org.bukkit.inventory.MainHand.RIGHT : org.bukkit.inventory.MainHand.LEFT; }
    @Override public int getEnchantmentSeed() { return getHandle().getEnchantmentTableSeed(); }
    @Override public org.bukkit.inventory.ItemStack getItemInHand() { return getEquipment().getItemInMainHand(); }
    @Override public void setItemInHand(org.bukkit.inventory.ItemStack item) { getEquipment().setItemInMainHand(item); }
    @Override public org.bukkit.inventory.EntityEquipment getEquipment() { return inventory; }
    @Override public boolean isDeeplySleeping() { return getHandle().canResetTimeBySleeping(); }
    @Override public int getSleepTicks() { return getHandle().getSleepTimer(); }
    @Override public boolean isBlocking() { return getHandle().isBlocking(); }
    @Override public org.bukkit.inventory.ItemStack getItemInUse() { return getActiveItem(); }
    @Override public int getExpToLevel() { return getHandle().getNextLevelExperience(); }
    @Override public float getAttackCooldown() { return getHandle().getAttackCooldownProgress(0); }
    @Override public void setHurtDirection(float direction) { if (!Float.isFinite(direction)) throw new IllegalArgumentException("nonfinite direction"); requireWritable(); ((dev.meldrite.v1201.hook.PlayerDamageTiltPaperAccessMixin) getHandle()).meldrite$setDamageTiltYaw(direction); }
    @Override public float getExhaustion() { return getHandle().getHungerManager().getExhaustion(); }
    @Override public void setExhaustion(float exhaustion) { if (!Float.isFinite(exhaustion) || exhaustion < 0) throw new IllegalArgumentException("Invalid exhaustion"); requireWritable(); getHandle().getHungerManager().setExhaustion(exhaustion); }
    @Override public float getSaturation() { return getHandle().getHungerManager().getSaturationLevel(); }
    @Override public void setSaturation(float saturation) { if (!Float.isFinite(saturation) || saturation < 0) throw new IllegalArgumentException("Invalid saturation"); requireWritable(); getHandle().getHungerManager().setSaturationLevel(saturation); }
    @Override public int getFoodLevel() { return getHandle().getHungerManager().getFoodLevel(); }
    @Override public void setFoodLevel(int food) { if (food < 0 || food > 20) throw new IllegalArgumentException("Food outside [0,20]"); requireWritable(); getHandle().getHungerManager().setFoodLevel(food); }
    // Remaining API gaps generated by tools/GenerateEntityApi.java.
    @Override public void closeInventory() { closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.PLUGIN); }
    @Override public void closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason reason) {
        ScreenHandler handler = getHandle().currentScreenHandler;
        dev.meldrite.v1201.compat.InventoryEventBridge.pushCloseReason(reason);
        try {
            if (getHandle() instanceof ServerPlayerEntity serverPlayer) serverPlayer.closeHandledScreen();
            else if (handler != null && handler != getHandle().playerScreenHandler) handler.onClosed(getHandle());
        } finally {
            dev.meldrite.v1201.compat.InventoryEventBridge.popCloseReason();
        }
        CraftInventory.unregisterViewer(inventory.getInventory(), this);
        if (handler != null) {
            for (net.minecraft.screen.slot.Slot slot : handler.slots) CraftInventory.unregisterViewer(slot.inventory, this);
        }
    }
    @Override public boolean discoverRecipe(org.bukkit.NamespacedKey recipe) { return discoverRecipes(java.util.List.of(java.util.Objects.requireNonNull(recipe))) > 0; }
    @Override public int discoverRecipes(java.util.Collection<org.bukkit.NamespacedKey> recipes) {
        requireWritable();
        return getHandle().unlockRecipes(resolveRecipes(recipes));
    }
    @Override public boolean dropItem(boolean all) { return getHandle() instanceof ServerPlayerEntity player && player.dropSelectedItem(all); }
    @Override public org.bukkit.entity.Firework fireworkBoost(org.bukkit.inventory.ItemStack fireworkItem) {
        java.util.Objects.requireNonNull(fireworkItem, "fireworkItemStack");
        if (fireworkItem.getType() != org.bukkit.Material.FIREWORK_ROCKET) throw new IllegalArgumentException("fireworkItemStack must be a firework rocket");
        requireWritable();
        var nativeWorld = getHandle().getWorld();
        var rocket = new net.minecraft.entity.projectile.FireworkRocketEntity(nativeWorld,
                org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(fireworkItem), getHandle());
        return nativeWorld.spawnEntity(rocket)
                ? (org.bukkit.entity.Firework) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(rocket)
                : null;
    }
    @Override public org.bukkit.Location getBedLocation() {
        if (!getHandle().isSleeping()) throw new IllegalStateException("Not sleeping");
        BlockPos position = getHandle().getSleepingPosition().orElseThrow(() -> new IllegalStateException("Sleeping position missing"));
        return new org.bukkit.Location(getWorld(), position.getX(), position.getY(), position.getZ());
    }
    @Override public int getCooldown(org.bukkit.Material material) {
        Item item = item(material);
        var manager = cooldownManager();
        var entry = manager.entries.get(item);
        return entry == null ? 0 : Math.max(0, entry.endTick - manager.tick);
    }
    @Override public java.util.Set<org.bukkit.NamespacedKey> getDiscoveredRecipes() {
        if (!(getHandle() instanceof ServerPlayerEntity player)) return java.util.Set.of();
        var book = player.getRecipeBook();
        var manager = player.server.getRecipeManager();
        var discovered = new java.util.LinkedHashSet<org.bukkit.NamespacedKey>();
        for (Identifier id : manager.keys().toList()) if (book.contains(id)) discovered.add(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.fromMinecraft(id));
        return java.util.Set.copyOf(discovered);
    }
    @Override public org.bukkit.inventory.Inventory getEnderChest() { return enderChest; }
    @Override public org.bukkit.entity.FishHook getFishHook() {
        var hook = getHandle().fishHook;
        return hook == null || hook.isRemoved() ? null : (org.bukkit.entity.FishHook) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(hook);
    }
    @Override public org.bukkit.GameMode getGameMode() {
        if (getHandle() instanceof ServerPlayerEntity player) return bukkitGameMode(player.interactionManager.getGameMode());
        return org.bukkit.GameMode.SURVIVAL;
    }
    @Override public org.bukkit.inventory.PlayerInventory getInventory() { return inventory; }
    @Override public org.bukkit.inventory.ItemStack getItemOnCursor() { return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftMirror(getHandle().currentScreenHandler.getCursorStack()); }
    @Override public org.bukkit.Location getLastDeathLocation() { return getHandle().getLastDeathPos().map(this::location).orElse(null); }
    @Override public org.bukkit.inventory.InventoryView getOpenInventory() {
        if (getHandle() instanceof ServerPlayerEntity player) {
            return dev.meldrite.v1201.compat.InventoryEventBridge.bukkitView(player, player.currentScreenHandler);
        }
        return new CraftInventoryView(this, getHandle().currentScreenHandler, inventory);
    }
    @Override public org.bukkit.Location getPotentialBedLocation() {
        if (!(getHandle() instanceof ServerPlayerEntity player)) return null;
        BlockPos position = player.getSpawnPointPosition();
        if (position == null) return null;
        ServerWorld world = player.server.getWorld(player.getSpawnPointDimension());
        return world == null ? null : location(world, position);
    }
    @Override public int getSaturatedRegenRate() { return hungerAccess().meldrite$getSaturatedRegenRate(); }
    @Override public org.bukkit.entity.Entity getShoulderEntityLeft() { return shoulderEntity(getHandle().getShoulderEntityLeft()); }
    @Override public org.bukkit.entity.Entity getShoulderEntityRight() { return shoulderEntity(getHandle().getShoulderEntityRight()); }
    @Override public int getStarvationRate() { return hungerAccess().meldrite$getStarvationRate(); }
    @Override public int getUnsaturatedRegenRate() { return hungerAccess().meldrite$getUnsaturatedRegenRate(); }
    @Override public boolean hasCooldown(org.bukkit.Material material) { return cooldownManager().isCoolingDown(item(material)); }
    @Override public boolean hasDiscoveredRecipe(org.bukkit.NamespacedKey recipe) {
        java.util.Objects.requireNonNull(recipe, "recipe");
        return getHandle() instanceof ServerPlayerEntity player
                && player.getRecipeBook().contains(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(recipe));
    }
    @Override public org.bukkit.inventory.InventoryView openAnvil(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.ANVIL); }
    @Override public org.bukkit.inventory.InventoryView openCartographyTable(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.CARTOGRAPHY_TABLE); }
    @Override public org.bukkit.inventory.InventoryView openEnchanting(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.ENCHANTING_TABLE); }
    @Override public org.bukkit.inventory.InventoryView openGrindstone(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.GRINDSTONE); }
    @Override public org.bukkit.inventory.InventoryView openInventory(org.bukkit.inventory.Inventory inventory) {
        java.util.Objects.requireNonNull(inventory, "inventory");
        if (!(getHandle() instanceof ServerPlayerEntity player)) return null;
        if (!(inventory instanceof CraftInventory craft)) throw new IllegalArgumentException("Inventory is not a native CraftInventory");
        return openInventory(player, craft, craft.getTitle());
    }
    private org.bukkit.inventory.InventoryView openInventory(ServerPlayerEntity player, CraftInventory craft, String title) {
        var factory = new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, ignored) -> org.bukkit.craftbukkit.v1_20_R1.inventory.CraftContainerScreenHandler.create(
                        syncId, playerInventory, craft, player, title),
                org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(title)[0]);
        if (player.openHandledScreen(factory).isEmpty()) return null;
        openedInventoryHandler = player.currentScreenHandler;
        openedInventoryView = (CraftInventoryView) dev.meldrite.v1201.compat.InventoryEventBridge
                .bukkitView(player, player.currentScreenHandler);
        return openedInventoryView;
    }
    @Override public void openInventory(org.bukkit.inventory.InventoryView view) {
        java.util.Objects.requireNonNull(view, "view");
        if (view.getPlayer() != this) throw new IllegalArgumentException("InventoryView belongs to another player");
        if (!(getHandle() instanceof ServerPlayerEntity player)) return;
        if (!(view.getTopInventory() instanceof CraftInventory craft)) throw new IllegalArgumentException("Inventory is not a native CraftInventory");
        openInventory(player, craft, view.getTitle());
    }
    @Override public org.bukkit.inventory.InventoryView openLoom(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.LOOM); }
    @Override public org.bukkit.inventory.InventoryView openMerchant(org.bukkit.entity.Villager villager, boolean force) {
        java.util.Objects.requireNonNull(villager, "villager");
        return openMerchant((org.bukkit.inventory.Merchant) villager, force);
    }
    @Override public org.bukkit.inventory.InventoryView openMerchant(org.bukkit.inventory.Merchant merchant, boolean force) {
        java.util.Objects.requireNonNull(merchant, "merchant");
        if (!(getHandle() instanceof ServerPlayerEntity player)) return null;
        if (merchant.isTrading()) {
            if (!force) return null;
            org.bukkit.entity.HumanEntity trader = merchant.getTrader();
            if (trader != null) trader.closeInventory();
        }
        net.minecraft.village.Merchant nativeMerchant;
        net.minecraft.text.Text displayName;
        if (merchant instanceof org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchant craft) {
            nativeMerchant = craft.getMerchant();
            displayName = nativeMerchant instanceof org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchantCustom.MinecraftMerchant custom
                    ? custom.getTitle() : ((net.minecraft.entity.Entity) nativeMerchant).getDisplayName();
        } else if (merchant instanceof CraftEntity craft && craft.getHandle() instanceof net.minecraft.village.Merchant nativeHandle) {
            nativeMerchant = nativeHandle;
            displayName = craft.getHandle().getDisplayName();
        } else {
            throw new IllegalArgumentException("Unsupported merchant implementation: " + merchant.getClass().getName());
        }
        int level = nativeMerchant instanceof net.minecraft.entity.passive.VillagerEntity villager
                ? villager.getVillagerData().getLevel() : 1;
        requireWritable();
        nativeMerchant.setCustomer(player);
        nativeMerchant.sendOffers(player, displayName, level);
        if (!(player.currentScreenHandler instanceof net.minecraft.screen.MerchantScreenHandler)
                || ((dev.meldrite.v1201.hook.MerchantScreenHandlerAccess) player.currentScreenHandler).meldrite$getMerchant() != nativeMerchant) {
            nativeMerchant.setCustomer(null);
            return null;
        }
        return getOpenInventory();
    }
    @Override public void openSign(org.bukkit.block.Sign sign, org.bukkit.block.sign.Side side) {
        java.util.Objects.requireNonNull(sign, "sign");
        java.util.Objects.requireNonNull(side, "side");
        if (!(getHandle() instanceof ServerPlayerEntity player)) return;
        var block = sign.getBlock();
        if (!(block.getWorld() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld world)) throw new IllegalArgumentException("Sign requires a native world");
        var position = new BlockPos(block.getX(), block.getY(), block.getZ());
        if (!(world.getHandle().getBlockEntity(position) instanceof net.minecraft.block.entity.SignBlockEntity signEntity)) return;
        requireWritable();
        player.openEditSignScreen(signEntity, side == org.bukkit.block.sign.Side.FRONT);
    }
    @Override public org.bukkit.inventory.InventoryView openSmithingTable(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.SMITHING_TABLE); }
    @Override public org.bukkit.inventory.InventoryView openStonecutter(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.STONECUTTER); }
    @Override public org.bukkit.inventory.InventoryView openWorkbench(org.bukkit.Location location, boolean force) { return openBlockMenu(location, force, org.bukkit.Material.CRAFTING_TABLE); }
    @Override public org.bukkit.entity.Entity releaseLeftShoulderEntity() { return releaseShoulder(true); }
    @Override public org.bukkit.entity.Entity releaseRightShoulderEntity() { return releaseShoulder(false); }
    @Override public void setCooldown(org.bukkit.Material material, int ticks) { if (ticks < 0) throw new IllegalArgumentException("Cannot have negative cooldown"); cooldownManager().set(item(material), ticks); }
    @Override public void setEnchantmentSeed(int seed) { requireWritable(); getHandle().enchantmentTableSeed = seed; }
    @Override public void setGameMode(org.bukkit.GameMode mode) {
        java.util.Objects.requireNonNull(mode, "mode");
        if (getHandle() instanceof ServerPlayerEntity player) player.changeGameMode(net.minecraft.world.GameMode.byId(mode.getValue()));
    }
    @Override public void setItemOnCursor(org.bukkit.inventory.ItemStack stack) { getHandle().currentScreenHandler.setCursorStack(org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(stack)); }
    @Override public void setLastDeathLocation(org.bukkit.Location location) {
        requireWritable();
        if (location == null) { getHandle().setLastDeathPos(java.util.Optional.empty()); return; }
        location.checkFinite();
        if (!(location.getWorld() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld world)) throw new IllegalArgumentException("Location requires a native world");
        getHandle().setLastDeathPos(java.util.Optional.of(GlobalPos.create(world.getHandle().getRegistryKey(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()))));
    }
    @Override public void setSaturatedRegenRate(int ticks) { requireWritable(); hungerAccess().meldrite$setSaturatedRegenRate(ticks); }
    @Override public void setShoulderEntityLeft(org.bukkit.entity.Entity entity) { setShoulderEntity(entity, true); }
    @Override public void setShoulderEntityRight(org.bukkit.entity.Entity entity) { setShoulderEntity(entity, false); }
    @Override public void setStarvationRate(int ticks) { requireWritable(); hungerAccess().meldrite$setStarvationRate(ticks); }
    @Override public void setUnsaturatedRegenRate(int ticks) { requireWritable(); hungerAccess().meldrite$setUnsaturatedRegenRate(ticks); }
    @Override public boolean setWindowProperty(org.bukkit.inventory.InventoryView.Property property, int value) {
        java.util.Objects.requireNonNull(property, "property");
        var view = getOpenInventory();
        if (property.getType() != view.getType()) return false;
        getHandle().currentScreenHandler.setProperty(property.getId(), value);
        return true;
    }
    @Override public boolean sleep(org.bukkit.Location location, boolean force) {
        java.util.Objects.requireNonNull(location, "location");
        location.checkFinite();
        if (location.getWorld() != getWorld()) throw new IllegalArgumentException("Cannot sleep across worlds");
        return getHandle().trySleep(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ())).right().isPresent();
    }
    @Override public boolean undiscoverRecipe(org.bukkit.NamespacedKey recipe) { return undiscoverRecipes(java.util.List.of(java.util.Objects.requireNonNull(recipe))) > 0; }
    @Override public int undiscoverRecipes(java.util.Collection<org.bukkit.NamespacedKey> recipes) {
        requireWritable();
        return getHandle().lockRecipes(resolveRecipes(recipes));
    }
    @Override public void wakeup(boolean setSpawnLocation) {
        if (!getHandle().isSleeping()) throw new IllegalStateException("Cannot wakeup if not sleeping");
        requireWritable();
        getHandle().wakeUp(true, setSpawnLocation);
    }

    private org.bukkit.inventory.InventoryView openBlockMenu(org.bukkit.Location location, boolean force, org.bukkit.Material material) {
        if (!(getHandle() instanceof ServerPlayerEntity player)) return null;
        if (location == null) location = getLocation();
        location.checkFinite();
        if (!(location.getWorld() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld world)) throw new IllegalArgumentException("Location requires a native world");
        if (world.getHandle() != player.getServerWorld()) return null;
        BlockPos position = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.block.Block block = block(material);
        var state = world.getHandle().getBlockState(position);
        boolean matchingBlock = state.isOf(block);
        if (!force && !matchingBlock) return null;
        net.minecraft.screen.NamedScreenHandlerFactory factory = matchingBlock
                ? state.createScreenHandlerFactory(world.getHandle(), position) : null;
        if (factory == null && force) factory = forcedBlockMenu(material);
        if (factory == null) return null;
        requireWritable();
        if (player.openHandledScreen(factory).isEmpty()) return null;
        openedInventoryHandler = null;
        openedInventoryView = null;
        return getOpenInventory();
    }

    private net.minecraft.block.Block block(org.bukkit.Material material) {
        return switch (material) {
            case ANVIL -> net.minecraft.block.Blocks.ANVIL;
            case CARTOGRAPHY_TABLE -> net.minecraft.block.Blocks.CARTOGRAPHY_TABLE;
            case ENCHANTING_TABLE -> net.minecraft.block.Blocks.ENCHANTING_TABLE;
            case GRINDSTONE -> net.minecraft.block.Blocks.GRINDSTONE;
            case LOOM -> net.minecraft.block.Blocks.LOOM;
            case SMITHING_TABLE -> net.minecraft.block.Blocks.SMITHING_TABLE;
            case STONECUTTER -> net.minecraft.block.Blocks.STONECUTTER;
            case CRAFTING_TABLE -> net.minecraft.block.Blocks.CRAFTING_TABLE;
            default -> throw new IllegalArgumentException("Unsupported workbench material: " + material);
        };
    }

    private net.minecraft.screen.NamedScreenHandlerFactory forcedBlockMenu(org.bukkit.Material material) {
        String title = switch (material) {
            case ANVIL -> "container.repair";
            case CARTOGRAPHY_TABLE -> "container.cartography_table";
            case ENCHANTING_TABLE -> "container.enchant";
            case GRINDSTONE -> "container.grindstone";
            case LOOM -> "container.loom";
            case SMITHING_TABLE -> "upgrade.item";
            case STONECUTTER -> "container.stonecutter";
            case CRAFTING_TABLE -> "container.crafting";
            default -> throw new IllegalArgumentException("Unsupported workbench material: " + material);
        };
        return new net.minecraft.screen.SimpleNamedScreenHandlerFactory((syncId, playerInventory, player) -> {
            var context = net.minecraft.screen.ScreenHandlerContext.EMPTY;
            return switch (material) {
                case ANVIL -> new net.minecraft.screen.AnvilScreenHandler(syncId, playerInventory, context);
                case CARTOGRAPHY_TABLE -> new net.minecraft.screen.CartographyTableScreenHandler(syncId, playerInventory, context);
                case ENCHANTING_TABLE -> new net.minecraft.screen.EnchantmentScreenHandler(syncId, playerInventory, context);
                case GRINDSTONE -> new net.minecraft.screen.GrindstoneScreenHandler(syncId, playerInventory, context);
                case LOOM -> new net.minecraft.screen.LoomScreenHandler(syncId, playerInventory, context);
                case SMITHING_TABLE -> new net.minecraft.screen.SmithingScreenHandler(syncId, playerInventory, context);
                case STONECUTTER -> new net.minecraft.screen.StonecutterScreenHandler(syncId, playerInventory, context);
                case CRAFTING_TABLE -> new net.minecraft.screen.CraftingScreenHandler(syncId, playerInventory, context);
                default -> throw new IllegalArgumentException("Unsupported workbench material: " + material);
            };
        }, net.minecraft.text.Text.translatable(title));
    }

    private org.bukkit.entity.Entity shoulderEntity(net.minecraft.nbt.NbtCompound data) {
        if (data == null || data.isEmpty()) return null;
        var entity = net.minecraft.entity.EntityType.getEntityFromNbt(data.copy(), getHandle().getWorld()).orElse(null);
        return entity == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity);
    }

    private void setShoulderEntity(org.bukkit.entity.Entity entity, boolean left) {
        requireWritable();
        var player = getHandle();
        net.minecraft.nbt.NbtCompound data = new net.minecraft.nbt.NbtCompound();
        CraftEntity craft = null;
        if (entity != null) {
            if (!(entity instanceof CraftEntity wrapper)) throw new IllegalArgumentException("Shoulder entity is not a native CraftEntity");
            craft = wrapper;
            var nativeEntity = wrapper.getHandle();
            if (nativeEntity.getWorld() != player.getWorld()) throw new IllegalArgumentException("Shoulder entity must be in the player's world");
            if (!nativeEntity.saveNbt(data)) throw new IllegalArgumentException("Shoulder entity cannot be serialized");
        }
        if (left) player.setShoulderEntityLeft(data);
        else player.setShoulderEntityRight(data);
        if (craft != null) craft.remove();
    }

    private org.bukkit.entity.Entity releaseShoulder(boolean left) {
        requireWritable();
        var player = getHandle();
        var data = left ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        if (data == null || data.isEmpty()) return null;
        var entity = net.minecraft.entity.EntityType.getEntityFromNbt(data.copy(), player.getWorld()).orElse(null);
        if (entity == null) return null;
        double side = left ? -0.5 : 0.5;
        entity.refreshPositionAndAngles(player.getX() + side, player.getY() + 1.2, player.getZ(), player.getYaw(), player.getPitch());
        if (!player.getWorld().spawnEntity(entity)) return null;
        if (left) player.setShoulderEntityLeft(new net.minecraft.nbt.NbtCompound());
        else player.setShoulderEntityRight(new net.minecraft.nbt.NbtCompound());
        return dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity);
    }

    private net.minecraft.entity.player.ItemCooldownManager cooldownManager() { return getHandle().getItemCooldownManager(); }
    private HungerManagerPaperAccess hungerAccess() { return (HungerManagerPaperAccess) getHandle().getHungerManager(); }
    private java.util.Collection<Recipe<?>> resolveRecipes(java.util.Collection<org.bukkit.NamespacedKey> keys) {
        java.util.Objects.requireNonNull(keys, "recipes");
        var manager = getHandle().getWorld().getServer().getRecipeManager();
        var recipes = new java.util.ArrayList<Recipe<?>>();
        for (var key : keys) {
            var id = org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(java.util.Objects.requireNonNull(key, "recipe"));
            manager.get(id).ifPresent(recipes::add);
        }
        return recipes;
    }
    private org.bukkit.Location location(GlobalPos position) {
        ServerWorld world = getHandle().getWorld().getServer().getWorld(position.getDimension());
        return world == null ? null : location(world, position.getPos());
    }
    private static org.bukkit.Location location(ServerWorld world, BlockPos position) {
        var craftWorld = dev.meldrite.v1201.compat.NativeEntityExtensions.world(world);
        return new org.bukkit.Location(craftWorld, position.getX(), position.getY(), position.getZ());
    }
    private static Item item(org.bukkit.Material material) {
        java.util.Objects.requireNonNull(material, "material");
        if (!material.isItem()) throw new IllegalArgumentException("Material is not an item: " + material);
        return dev.meldrite.v1201.compat.NativeItemBridge.item(material);
    }
    private static org.bukkit.GameMode bukkitGameMode(GameMode mode) {
        return switch (mode) { case CREATIVE -> org.bukkit.GameMode.CREATIVE; case ADVENTURE -> org.bukkit.GameMode.ADVENTURE; case SPECTATOR -> org.bukkit.GameMode.SPECTATOR; default -> org.bukkit.GameMode.SURVIVAL; };
    }
}
