package dev.meldrite.v1201.hook;

import net.minecraft.server.MinecraftServer;
import dev.meldrite.v1201.compat.LegacyTickTimes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Exposes the rolling tick samples through the Paper timing API. */
@Mixin(MinecraftServer.class)
public abstract class LegacyTickTimesMixin {
    @Unique public final LegacyTickTimes tickTimes5s = new LegacyTickTimes(100);
    @Unique public final LegacyTickTimes tickTimes10s = new LegacyTickTimes(200);
    @Unique public final LegacyTickTimes tickTimes60s = new LegacyTickTimes(1200);
    @Unique private long meldrite$lastTickNanos;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void meldrite$init(CallbackInfo callback) {
        meldrite$lastTickNanos = System.nanoTime();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void meldrite$sample(CallbackInfo callback) {
        long now = System.nanoTime();
        long elapsed = Math.max(0L, now - meldrite$lastTickNanos);
        meldrite$lastTickNanos = now;
        tickTimes5s.add(elapsed);
        tickTimes10s.add(elapsed);
        tickTimes60s.add(elapsed);
    }

}
