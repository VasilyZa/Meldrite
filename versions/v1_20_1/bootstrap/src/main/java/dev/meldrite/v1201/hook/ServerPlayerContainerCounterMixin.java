package dev.meldrite.v1201.hook;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerContainerCounterMixin {
    @Shadow private int screenHandlerSyncId;
    @Shadow protected abstract void incrementScreenHandlerSyncId();

    public int nextContainerCounter() {
        dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread((ServerPlayerEntity) (Object) this);
        incrementScreenHandlerSyncId();
        return screenHandlerSyncId;
    }
}
