package dev.meldrite.v1201.hook;

import net.minecraft.entity.Entity;
import net.minecraft.world.ChunkRegion;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Preserves world-generation region insertion without dispatching server plugin spawn events. */
@Mixin(ChunkRegion.class)
public abstract class ChunkRegionSpawnReasonMixin {
    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return ((ChunkRegion) (Object) this).spawnEntity(entity);
    }
}
