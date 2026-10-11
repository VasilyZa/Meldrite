package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeChunkPersistentDataAccess;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldChunk.class)
public abstract class WorldChunkPersistentDataMixin {
    @Inject(method = "<init>(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/ProtoChunk;Lnet/minecraft/world/chunk/WorldChunk$EntityLoader;)V", at = @At("RETURN"))
    private void meldrite$copyPersistentData(ServerWorld world, ProtoChunk proto, WorldChunk.EntityLoader entityLoader, CallbackInfo callback) {
        var source = ((NativeChunkPersistentDataAccess) proto).meldrite$getPersistentDataContainer();
        ((NativeChunkPersistentDataAccess) this).meldrite$getPersistentDataContainer().putAll(source.getTagsCloned());
    }
}
