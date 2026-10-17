package dev.meldrite.v1201.hook;

import net.minecraft.entity.Entity;
import net.minecraft.world.ModifiableWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Adds Paper's entity-reason insertion contract to every native world writer. */
@Mixin(ModifiableWorld.class)
public interface ModifiableWorldSpawnReasonMixin {
    /** Unsupported writers reject insertion; server and generation worlds override this method. */
    default boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }
}
