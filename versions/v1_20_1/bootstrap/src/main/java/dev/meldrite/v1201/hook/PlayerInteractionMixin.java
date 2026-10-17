package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.meldrite.v1201.compat.NativePlayerInteractionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Carries Bukkit's separate block/item decisions back into the native interaction path. */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class PlayerInteractionMixin {
    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;
    @Shadow private boolean mining;
    @Shadow private boolean failedToMine;
    @Shadow private BlockPos miningPos;
    @Shadow private BlockPos failedMiningPos;
    @Shadow private int blockBreakingProgress;
    @Unique private BlockPos meldrite$deniedMiningPosition;

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true, require = 1)
    private void meldrite$denyCancelledMiningFinish(BlockPos position, PlayerActionC2SPacket.Action action,
            Direction face, int worldHeight, int sequence, CallbackInfo callback) {
        // STOP packets can arrive after a cancelled START. Vanilla compares the
        // old mining position even when mining=false. Only a new active mining
        // transaction can make a later STOP valid again.
        if (action == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK
                && position.equals(meldrite$deniedMiningPosition)) {
            // The denied START never installed vanilla's miningPos. Consume its
            // matching ABORT here, retaining the denial for a delayed STOP.
            world.setBlockBreakingInfo(player.getId(), position, -1);
            NativePlayerInteractionEvents.resync(player, position, face);
            callback.cancel();
        } else if (action == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                && meldrite$deniedMiningPosition != null) {
            if (position.equals(meldrite$deniedMiningPosition) || position.equals(miningPos)) {
                NativePlayerInteractionEvents.resync(player, position, face);
            }
            callback.cancel();
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At(value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;miningPos:Lnet/minecraft/util/math/BlockPos;",
            opcode = org.objectweb.asm.Opcodes.PUTFIELD, shift = At.Shift.AFTER), require = 1)
    private void meldrite$allowNewMiningStart(BlockPos position, PlayerActionC2SPacket.Action action,
            Direction face, int worldHeight, int sequence, CallbackInfo callback) {
        meldrite$deniedMiningPosition = null;
    }

    @Inject(method = "processBlockBreakingAction", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"),
            cancellable = true, require = 1)
    private void meldrite$leftClickBlock(BlockPos position, PlayerActionC2SPacket.Action action,
            Direction face, int worldHeight, int sequence, CallbackInfo callback) {
        // Only START_DESTROY_BLOCK reaches this call site, after reach/height checks.
        // Selecting a block must work in protected areas too; uncancelled clicks
        // still pass through the original modify gate before any native mutation.
        var event = NativePlayerInteractionEvents.block(player, Hand.MAIN_HAND, position, face, null, false);
        if (event != null && (event.useInteractedBlock() == Event.Result.DENY
                || event.useItemInHand() == Event.Result.DENY)) {
            if (mining) world.setBlockBreakingInfo(player.getId(), miningPos, -1);
            if (failedToMine && (!mining || !failedMiningPos.equals(miningPos))) {
                world.setBlockBreakingInfo(player.getId(), failedMiningPos, -1);
            }
            mining = false;
            failedToMine = false;
            blockBreakingProgress = -1;
            meldrite$deniedMiningPosition = position.toImmutable();
            NativePlayerInteractionEvents.resync(player, position, face);
            callback.cancel();
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 0),
            cancellable = true, require = 1)
    private void meldrite$rightClickBlock(ServerPlayerEntity actor, World world, ItemStack stack,
            Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> callback,
            @Share("meldrite$interaction") LocalRef<PlayerInteractEvent> shared) {
        var event = NativePlayerInteractionEvents.block(actor, hand, hit.getBlockPos(), hit.getSide(), hit.getPos(), true);
        shared.set(event);
        if (event != null && event.useInteractedBlock() == Event.Result.DENY
                && event.useItemInHand() == Event.Result.DENY) {
            NativePlayerInteractionEvents.resync(actor, hit.getBlockPos(), hit.getSide());
            callback.setReturnValue(ActionResult.FAIL);
        }
    }

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"), require = 1)
    private ActionResult meldrite$useClickedBlock(BlockState state, World world,
            net.minecraft.entity.player.PlayerEntity actor, Hand hand, BlockHitResult hit,
            Operation<ActionResult> original,
            @Share("meldrite$interaction") LocalRef<PlayerInteractEvent> shared) {
        var event = shared.get();
        if (event != null && event.useInteractedBlock() == Event.Result.DENY) {
            NativePlayerInteractionEvents.resync((ServerPlayerEntity) actor, hit.getBlockPos(), hit.getSide());
            return ActionResult.PASS;
        }
        return original.call(state, world, actor, hand, hit);
    }

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;createScreenHandlerFactory(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/screen/NamedScreenHandlerFactory;"), require = 1)
    private NamedScreenHandlerFactory meldrite$spectatorBlockUse(BlockState state, World world,
            BlockPos position, Operation<NamedScreenHandlerFactory> original,
            @Share("meldrite$interaction") LocalRef<PlayerInteractEvent> shared) {
        var event = shared.get();
        return event != null && event.useInteractedBlock() == Event.Result.DENY
                ? null : original.call(state, world, position);
    }

    @WrapOperation(method = "interactBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"), require = 2)
    private ActionResult meldrite$useHeldItem(ItemStack stack, ItemUsageContext context,
            Operation<ActionResult> original,
            @Share("meldrite$interaction") LocalRef<PlayerInteractEvent> shared) {
        var event = shared.get();
        if (event != null && event.useItemInHand() == Event.Result.DENY) {
            NativePlayerInteractionEvents.resync((ServerPlayerEntity) context.getPlayer(), context.getBlockPos(), context.getSide());
            return ActionResult.FAIL;
        }
        return original.call(stack, context);
    }
}
