package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeChunkPersistentDataAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerPersistentDataMixin {
    @Inject(method = "serialize", at = @At("RETURN"))
    private static void meldrite$savePersistentData(ServerWorld world, Chunk chunk, CallbackInfoReturnable<NbtCompound> callback) {
        CraftPersistentDataContainer values = ((NativeChunkPersistentDataAccess) chunk).meldrite$getPersistentDataContainer();
        if (!values.isEmpty()) callback.getReturnValue().put("BukkitValues", values.toTagCompound());
    }

    @Inject(method = "deserialize", at = @At("RETURN"))
    private static void meldrite$loadPersistentData(ServerWorld world, PointOfInterestStorage poi, ChunkPos position,
            NbtCompound nbt, CallbackInfoReturnable<ProtoChunk> callback) {
        if (nbt.contains("BukkitValues", NbtElement.COMPOUND_TYPE)) {
            ProtoChunk loaded = callback.getReturnValue();
            // Full chunks leave deserialization inside a temporary generation
            // wrapper. The world later unwraps it and keeps the WorldChunk.
            Chunk owner = loaded instanceof WrapperProtoChunk wrapper ? wrapper.getWrappedChunk() : loaded;
            ((NativeChunkPersistentDataAccess) owner).meldrite$loadPersistentData(nbt.getCompound("BukkitValues"));
        }
    }
}
