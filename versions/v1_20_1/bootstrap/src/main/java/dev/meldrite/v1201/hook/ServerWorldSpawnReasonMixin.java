package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntitySpawner;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;

/** Routes server-world insertion through native Bukkit event and cancellation semantics. */
@Mixin(ServerWorld.class)
public abstract class ServerWorldSpawnReasonMixin {
    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return NativeEntitySpawner.addNative((ServerWorld) (Object) this, entity, reason);
    }
}
