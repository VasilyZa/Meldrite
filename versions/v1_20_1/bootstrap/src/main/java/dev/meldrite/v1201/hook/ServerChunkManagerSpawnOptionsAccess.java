package dev.meldrite.v1201.hook;

import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the native spawn toggles used by Bukkit's world spawn flags. */
@Mixin(ServerChunkManager.class)
public interface ServerChunkManagerSpawnOptionsAccess {
    @Accessor("spawnAnimals") boolean meldrite$spawnAnimals();
    @Accessor("spawnMonsters") boolean meldrite$spawnMonsters();
}
