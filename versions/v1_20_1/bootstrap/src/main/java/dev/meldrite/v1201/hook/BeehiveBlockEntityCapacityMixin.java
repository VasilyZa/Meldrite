package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeBeehiveCapacity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the CraftBukkit/Purpur configurable bee capacity to native hives. */
@Mixin(BeehiveBlockEntity.class)
public abstract class BeehiveBlockEntityCapacityMixin implements NativeBeehiveCapacity {
    @Unique private static final String MELDRITE_MAX_BEES_TAG = "Bukkit.MaxEntities";
    @Unique private int meldrite$maxBees = BeehiveBlockEntity.MAX_BEE_COUNT;
    @Unique private static final ThreadLocal<Boolean> MELDRITE_FORCE_RELEASE = new ThreadLocal<>();

    @Override public java.util.List<net.minecraft.entity.Entity> meldrite$releaseBees(net.minecraft.server.world.ServerWorld world,
            net.minecraft.block.BlockState state, boolean force) {
        var hive = (BeehiveBlockEntity) (Object) this;
        var previousWorld = hive.getWorld();
        Boolean previousForce = MELDRITE_FORCE_RELEASE.get();
        hive.setWorld(world);
        MELDRITE_FORCE_RELEASE.set(force);
        try {
            return ((BeehiveBlockEntityAccessMixin) (Object) this).meldrite$tryReleaseBee(state, BeehiveBlockEntity.BeeState.BEE_RELEASED);
        } finally {
            hive.setWorld(previousWorld);
            if (previousForce == null) MELDRITE_FORCE_RELEASE.remove(); else MELDRITE_FORCE_RELEASE.set(previousForce);
        }
    }

    @Redirect(method = "releaseBee", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isNight()Z"))
    private static boolean meldrite$night(net.minecraft.world.World world) {
        return !Boolean.TRUE.equals(MELDRITE_FORCE_RELEASE.get()) && world.isNight();
    }

    @Redirect(method = "releaseBee", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isRaining()Z"))
    private static boolean meldrite$rain(net.minecraft.world.World world) {
        return !Boolean.TRUE.equals(MELDRITE_FORCE_RELEASE.get()) && world.isRaining();
    }

    @Override public int meldrite$getMaxBees() { return meldrite$maxBees; }
    @Override public void meldrite$setMaxBees(int maxBees) {
        if (maxBees < 1) throw new IllegalArgumentException("Beehive capacity must be positive");
        meldrite$maxBees = maxBees;
        ((BeehiveBlockEntity) (Object) this).markDirty();
    }

    @Inject(method = "isFullOfBees", at = @At("HEAD"), cancellable = true)
    private void meldrite$isFull(CallbackInfoReturnable<Boolean> callback) {
        if (meldrite$maxBees != BeehiveBlockEntity.MAX_BEE_COUNT) callback.setReturnValue(((BeehiveBlockEntity) (Object) this).getBeeCount() >= meldrite$maxBees);
    }

    @Inject(method = "tryEnterHive(Lnet/minecraft/entity/Entity;ZI)V", at = @At("HEAD"), cancellable = true)
    private void meldrite$capacity(net.minecraft.entity.Entity entity, boolean hasNectar, int ticksInHive, CallbackInfo callback) {
        if (meldrite$maxBees != BeehiveBlockEntity.MAX_BEE_COUNT && ((BeehiveBlockEntity) (Object) this).getBeeCount() >= meldrite$maxBees) callback.cancel();
    }

    @ModifyConstant(method = "tryEnterHive(Lnet/minecraft/entity/Entity;ZI)V", constant = @Constant(intValue = 3))
    private int meldrite$nativeCapacity(int vanillaCapacity) { return meldrite$maxBees; }

    @Inject(method = "addBee", at = @At("HEAD"), cancellable = true)
    private void meldrite$add(NbtCompound entityData, int ticksInHive, boolean hasNectar, CallbackInfo callback) {
        if (meldrite$maxBees != BeehiveBlockEntity.MAX_BEE_COUNT && ((BeehiveBlockEntity) (Object) this).getBeeCount() >= meldrite$maxBees) callback.cancel();
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo callback) {
        meldrite$maxBees = nbt.contains(MELDRITE_MAX_BEES_TAG, 3) ? Math.max(1, nbt.getInt(MELDRITE_MAX_BEES_TAG)) : BeehiveBlockEntity.MAX_BEE_COUNT;
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo callback) {
        if (meldrite$maxBees != BeehiveBlockEntity.MAX_BEE_COUNT) nbt.putInt(MELDRITE_MAX_BEES_TAG, meldrite$maxBees);
    }
}
