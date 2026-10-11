package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeWitherState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WitherEntity.class)
public abstract class WitherEntityPortalStateMixin extends HostileEntity implements NativeWitherState {
    @Unique private boolean meldrite$canTravelThroughPortals;
    protected WitherEntityPortalStateMixin(EntityType<? extends HostileEntity> type, World world) { super(type, world); }
    @Override public void meldrite$setCanTravelThroughPortals(boolean value) { meldrite$canTravelThroughPortals = value; }
    @Inject(method = "canUsePortals", at = @At("HEAD"), cancellable = true)
    private void meldrite$portalState(CallbackInfoReturnable<Boolean> callback) {
        callback.setReturnValue(meldrite$canTravelThroughPortals && super.canUsePortals());
    }
}
