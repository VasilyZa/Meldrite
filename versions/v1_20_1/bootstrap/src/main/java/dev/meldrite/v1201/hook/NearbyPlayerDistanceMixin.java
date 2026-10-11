package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeNearbyPlayerSource;
import dev.meldrite.v1201.compat.NativePlayerDistanceAccess;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.world.ChunkTicketManager$NearbyChunkTicketUpdater")
public abstract class NearbyPlayerDistanceMixin extends ChunkPosDistanceLevelPropagator implements NativeNearbyPlayerSource {
    protected NearbyPlayerDistanceMixin(int levels, int expected, int queue) { super(levels, expected, queue); }
    @Shadow public abstract void setWatchDistance(int radius);
    @Shadow public abstract void updateLevels();
    @Inject(method = "<init>", at = @At("RETURN"))
    private void meldrite$bind(ChunkTicketManager manager, int maximum, CallbackInfo ci) {
        ((NativePlayerDistanceAccess) manager).meldrite$bindNearbySource(this);
    }
    @Override public void meldrite$setRadius(int radius) { setWatchDistance(radius); }
    @Override public void meldrite$source(long chunk, int level) { updateLevel(chunk, level, false); }
    @Override public void meldrite$flushSources() { updateLevels(); }
    @Override public int meldrite$sourceLevel(long chunk) { return getLevel(chunk); }
}
