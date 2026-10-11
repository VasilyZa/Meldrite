package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.meldrite.v1201.console.DedicatedConsole;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftDedicatedServer.class)
public abstract class DedicatedConsoleMixin {
    @Unique private DedicatedConsole meldrite$console;

    @WrapOperation(method = "setupServer", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;start()V", ordinal = 0))
    private void meldrite$startConsole(Thread input, Operation<Void> original) {
        meldrite$console = new DedicatedConsole((MinecraftDedicatedServer) (Object) this);
        meldrite$console.start(input, () -> original.call(input));
    }

    @Inject(method = "executeQueuedCommands", at = @At("HEAD"))
    private void meldrite$tickConsole(CallbackInfo callback) {
        if (meldrite$console != null) meldrite$console.tick();
    }

    @Inject(method = "shutdown", at = @At("RETURN"))
    private void meldrite$closeConsole(CallbackInfo callback) {
        if (meldrite$console != null) meldrite$console.close();
    }
}
