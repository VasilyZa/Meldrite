package dev.meldrite.v1201.hook;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ThreadedAnvilChunkStorage.class)
public interface PlayerEntityTrackersAccess {
    @Accessor("entityTrackers") it.unimi.dsi.fastutil.ints.Int2ObjectMap<?> meldrite$entityTrackers();
}
