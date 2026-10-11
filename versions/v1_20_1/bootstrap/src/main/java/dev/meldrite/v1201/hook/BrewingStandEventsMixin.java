package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.compat.NativeBrewing;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandEventsMixin {
    @Unique private static final ThreadLocal<NativeBrewing.Results> MELDRITE_RESULTS = new ThreadLocal<>();
    @Unique private static boolean meldrite$available(World world) {
        return world instanceof ServerWorld && NativeEntityExtensions.eventsAvailable();
    }
    @Unique private static org.bukkit.block.Block meldrite$block(World world, BlockPos pos) {
        return NativeEntityExtensions.world((ServerWorld) world).getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void meldrite$input(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        var stand = (BrewingStandBlockEntity) (Object) this;
        if (slot >= 0 && slot < 3 && stand.getStack(slot).isEmpty() && NativeBrewing.input(stack)) cir.setReturnValue(true);
    }
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private static void meldrite$fuel(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity stand, CallbackInfo ci) {
        var stack = stand.getStack(4);
        if (!meldrite$available(world) || stand.fuel > 0 || !stack.isOf(Items.BLAZE_POWDER)) return;
        var event = new BrewingStandFuelEvent(meldrite$block(world, pos), CraftItemStack.asCraftMirror(stack), 20);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) { ci.cancel(); return; }
        stand.fuel = event.getFuelPower();
        if (stand.fuel > 0 && event.isConsuming()) stack.decrement(1);
        stand.markDirty();
    }
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z", ordinal = 0))
    private static boolean meldrite$nativeFuel(ItemStack stack, Item item, Operation<Boolean> original,
            World world, BlockPos pos, BlockState state, BrewingStandBlockEntity stand) {
        return !meldrite$available(world) && original.call(stack, item);
    }
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 400))
    private static int meldrite$start(int time, World world, BlockPos pos, BlockState state, BrewingStandBlockEntity stand) {
        if (!meldrite$available(world)) return time;
        var event = new BrewingStartEvent(meldrite$block(world, pos), CraftItemStack.asCraftMirror(stand.getStack(3)), time);
        Bukkit.getPluginManager().callEvent(event);
        return event.getTotalBrewTime();
    }
    @WrapMethod(method = "craft")
    private static void meldrite$brew(World world, BlockPos pos, DefaultedList<ItemStack> inventory, Operation<Void> original) {
        if (!meldrite$available(world) || !(world.getBlockEntity(pos) instanceof BrewingStandBlockEntity stand)) {
            original.call(world, pos, inventory); return;
        }
        var block = meldrite$block(world, pos);
        var holder = (org.bukkit.block.BrewingStand) block.getState(false);
        var results = new java.util.ArrayList<org.bukkit.inventory.ItemStack>(3);
        for (int slot = 0; slot < 3; slot++) results.add(CraftItemStack.asCraftMirror(BrewingRecipeRegistry.craft(inventory.get(3), inventory.get(slot))));
        var event = new BrewEvent(block, holder.getInventory(), results, stand.fuel);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        var previous = MELDRITE_RESULTS.get(); MELDRITE_RESULTS.set(new NativeBrewing.Results(event.getResults()));
        try { original.call(world, pos, inventory); }
        finally { if (previous == null) MELDRITE_RESULTS.remove(); else MELDRITE_RESULTS.set(previous); }
    }
    @WrapOperation(method = "craft", at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/BrewingRecipeRegistry;craft(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack meldrite$result(ItemStack ingredient, ItemStack input, Operation<ItemStack> original) {
        var results = MELDRITE_RESULTS.get(); if (results == null) return original.call(ingredient, input);
        int index = results.index++;
        return index < results.items.size() ? CraftItemStack.asNMSCopy(results.items.get(index)) : ItemStack.EMPTY;
    }
}
