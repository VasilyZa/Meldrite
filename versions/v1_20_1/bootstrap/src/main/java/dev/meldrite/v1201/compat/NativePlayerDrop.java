package dev.meldrite.v1201.compat;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.PlayerInventory;

/** Implements Paper's detached player drop behavior for native players. */
public final class NativePlayerDrop {
    private NativePlayerDrop() { }

    public static ItemEntity drop(PlayerEntity player, ItemStack suppliedStack, boolean throwRandomly,
            boolean retainOwnership, boolean callEvent, Random random) {
        java.util.Objects.requireNonNull(player, "player");
        java.util.Objects.requireNonNull(suppliedStack, "stack");
        java.util.Objects.requireNonNull(random, "random");
        NativeEntityExtensions.requireThread(player);
        if (suppliedStack.isEmpty()) return null;

        World world = player.getWorld();
        ItemStack stack = suppliedStack.copy();
        suppliedStack.setCount(0);
        ItemEntity dropped = new ItemEntity(world, player.getX(), player.getEyeY() - 0.30000001192092896D,
                player.getZ(), stack);
        dropped.setPickupDelay(40);
        if (retainOwnership) dropped.setThrower(player.getUuid());
        setVelocity(player, dropped, throwRandomly, random);

        if (!callEvent) return dropped;

        Player bukkitPlayer = (Player) NativeEntityExtensions.bukkit(player);
        Item bukkitItem = (Item) NativeEntityExtensions.bukkit(dropped);
        PlayerDropItemEvent event = new PlayerDropItemEvent(bukkitPlayer, bukkitItem);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            restoreCancelledDrop(bukkitPlayer.getInventory(), retainOwnership, bukkitItem);
            return null;
        }

        // Paper removes a dropped filled map from the player's map tracker only
        // after listeners accept the drop. This is independent of insertion.
        if (stack.isOf(Items.FILLED_MAP)) {
            MapState mapState = FilledMapItem.getMapState(stack, world);
            if (mapState != null) mapState.update(player, stack);
        }
        return dropped;
    }

    private static void restoreCancelledDrop(PlayerInventory inventory, boolean retainOwnership, Item dropped) {
        org.bukkit.inventory.ItemStack droppedStack = dropped.getItemStack();
        org.bukkit.inventory.ItemStack heldStack = inventory.getItemInHand();
        if (retainOwnership && (heldStack == null || heldStack.getAmount() == 0)) {
            inventory.setItemInHand(droppedStack);
        } else if (retainOwnership && heldStack.isSimilar(droppedStack)
                && heldStack.getAmount() < heldStack.getMaxStackSize() && droppedStack.getAmount() == 1) {
            heldStack.setAmount(heldStack.getAmount() + 1);
            inventory.setItemInHand(heldStack);
        } else {
            inventory.addItem(droppedStack);
        }
    }

    private static void setVelocity(PlayerEntity player, ItemEntity item, boolean throwRandomly, Random random) {
        if (throwRandomly) {
            float speed = random.nextFloat() * 0.5F;
            float angle = random.nextFloat() * 6.2831855F;
            item.setVelocity(-MathHelper.sin(angle) * speed, 0.20000000298023224D,
                    MathHelper.cos(angle) * speed);
            return;
        }

        float speed = 0.3F;
        float pitchSin = MathHelper.sin(player.getPitch() * 0.017453292F);
        float pitchCos = MathHelper.cos(player.getPitch() * 0.017453292F);
        float yawSin = MathHelper.sin(player.getYaw() * 0.017453292F);
        float yawCos = MathHelper.cos(player.getYaw() * 0.017453292F);
        float angle = random.nextFloat() * 6.2831855F;
        float jitter = 0.02F * random.nextFloat();
        item.setVelocity(-yawSin * pitchCos * speed + Math.cos(angle) * jitter,
                -pitchSin * speed + 0.1F + (random.nextFloat() - random.nextFloat()) * 0.1F,
                yawCos * pitchCos * speed + Math.sin(angle) * jitter);
    }
}
