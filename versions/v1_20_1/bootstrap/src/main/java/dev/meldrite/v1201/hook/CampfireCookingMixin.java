package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeCampfireCookingAccess;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireCookingMixin implements NativeCampfireCookingAccess {
    @Unique private final boolean[] meldrite$cookingDisabled = new boolean[4];

    @Override public boolean meldrite$isCookingDisabled(int slot) { return meldrite$cookingDisabled[slot]; }
    @Override public void meldrite$setCookingDisabled(int slot, boolean disabled) { meldrite$cookingDisabled[slot] = disabled; }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void meldrite$writeCookingState(NbtCompound nbt, CallbackInfo callback) {
        byte[] slots = new byte[meldrite$cookingDisabled.length];
        boolean disabled = false;
        for (int index = 0; index < slots.length; index++) {
            slots[index] = (byte) (meldrite$cookingDisabled[index] ? 1 : 0);
            disabled |= meldrite$cookingDisabled[index];
        }
        if (disabled) nbt.putByteArray("Paper.CookingDisabled", slots);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$readCookingState(NbtCompound nbt, CallbackInfo callback) {
        byte[] slots = nbt.getByteArray("Paper.CookingDisabled");
        for (int index = 0; index < meldrite$cookingDisabled.length; index++) {
            meldrite$cookingDisabled[index] = index < slots.length && slots[index] != 0;
        }
    }

    @Redirect(method = "litServerTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/collection/DefaultedList;get(I)Ljava/lang/Object;", ordinal = 0))
    private static Object meldrite$skipDisabledSlot(DefaultedList<ItemStack> items, int slot,
                                                   World world, BlockPos position, BlockState state, CampfireBlockEntity campfire) {
        return ((NativeCampfireCookingAccess) campfire).meldrite$isCookingDisabled(slot) ? ItemStack.EMPTY : items.get(slot);
    }
}
