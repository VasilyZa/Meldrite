package org.bukkit.craftbukkit.v1_20_R1;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;

/** Uses the loaded datapack table and native loot generator. */
public final class CraftLootTable implements org.bukkit.loot.LootTable {
    private final NamespacedKey key;
    private final net.minecraft.loot.LootTable handle;
    public CraftLootTable(NamespacedKey key, net.minecraft.loot.LootTable handle) { this.key = Objects.requireNonNull(key); this.handle = Objects.requireNonNull(handle); }
    public net.minecraft.loot.LootTable getHandle() { return handle; }
    @Override public NamespacedKey getKey() { return key; }
    @Override public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        var parameters = parameters(context);
        var stacks = random == null ? handle.generateLoot(parameters) : handle.generateLoot(parameters, random.nextLong());
        return stacks.stream().filter(stack -> !stack.isEmpty()).map(CraftItemStack::asBukkitCopy).toList();
    }
    @Override public void fillInventory(Inventory inventory, Random random, LootContext context) {
        Objects.requireNonNull(inventory); Objects.requireNonNull(random);
        var parameters = parameters(context);
        var nativeInventory = new SimpleInventory(inventory.getSize()) {
            @Override public int getMaxCountPerStack() { return inventory.getMaxStackSize(); }
        };
        for (int slot = 0; slot < inventory.getSize(); slot++) nativeInventory.setStack(slot, CraftItemStack.asNMSCopy(inventory.getItem(slot)));
        handle.supplyInventory(nativeInventory, parameters, random.nextLong());
        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, CraftItemStack.asBukkitCopy(nativeInventory.getStack(slot)));
    }
    private <T> void allowed(LootContextParameterSet.Builder builder, LootContextParameter<T> parameter, T value) { if (handle.getType().isAllowed(parameter)) builder.add(parameter, value); }
    private LootContextParameterSet parameters(LootContext context) {
        Objects.requireNonNull(context);
        var location = context.getLocation(); location.checkFinite();
        if (!(location.getWorld() instanceof CraftWorld craftWorld)) throw new IllegalArgumentException("FOREIGN_LOOT_WORLD");
        var world = craftWorld.getHandle(); dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(world);
        if (context.getLootingModifier() != LootContext.DEFAULT_LOOT_MODIFIER) throw new UnsupportedOperationException("EXPLICIT_LOOTING_MODIFIER_REQUIRES_NATIVE_LOOT_CONTEXT_EXTENSION");
        var builder = new LootContextParameterSet.Builder(world).luck(context.getLuck());
        allowed(builder, LootContextParameters.ORIGIN, new net.minecraft.util.math.Vec3d(location.getX(), location.getY(), location.getZ()));
        if (context.getLootedEntity() != null) {
            if (!(context.getLootedEntity() instanceof CraftEntity entity) || entity.getHandle().getWorld() != world) throw new IllegalArgumentException("FOREIGN_LOOTED_ENTITY");
            allowed(builder, LootContextParameters.THIS_ENTITY, entity.getHandle());
            allowed(builder, LootContextParameters.DAMAGE_SOURCE, world.getDamageSources().generic());
            allowed(builder, LootContextParameters.ORIGIN, entity.getHandle().getPos());
        }
        if (context.getKiller() != null) {
            if (!(context.getKiller() instanceof CraftHumanEntity killer) || killer.getHandle().getWorld() != world) throw new IllegalArgumentException("FOREIGN_LOOT_KILLER");
            var player = killer.getHandle();
            allowed(builder, LootContextParameters.KILLER_ENTITY, player);
            allowed(builder, LootContextParameters.DIRECT_KILLER_ENTITY, player);
            allowed(builder, LootContextParameters.DAMAGE_SOURCE, world.getDamageSources().playerAttack(player));
            allowed(builder, LootContextParameters.LAST_DAMAGE_PLAYER, player);
            allowed(builder, LootContextParameters.TOOL, player.getActiveItem());
        }
        return builder.build(handle.getType());
    }
    @Override public boolean equals(Object object) { return object instanceof org.bukkit.loot.LootTable table && key.equals(table.getKey()); }
    @Override public int hashCode() { return key.hashCode(); }
    @Override public String toString() { return key.toString(); }
}
