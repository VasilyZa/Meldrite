package dev.meldrite.v1201.hook;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ThreadedAnvilChunkStorage.class)
public interface ThreadedAnvilChunkStoragePaperAccessMixin {
    @Accessor("chunkHolders") Long2ObjectLinkedOpenHashMap<ChunkHolder> meldrite$getChunkHolders();
    @Accessor("watchDistance") int meldrite$watchDistance();
}
