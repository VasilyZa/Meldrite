package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeEntityExtensions;
import io.papermc.paper.chunk.system.entity.EntityLookup;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper's reflective lookup entry exposes the same live native index for this world.
 * Access retains the native backend's primary-thread requirement; this does not
 * provide Paper's independently synchronized asynchronous entity index.
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldPaperLookupMixin {
    @Unique private EntityLookup meldrite$paperEntityLookup;

    public EntityLookup getEntityLookup() {
        var world = (ServerWorld) (Object) this;
        NativeEntityExtensions.requireThread(world);
        if (meldrite$paperEntityLookup == null)
            meldrite$paperEntityLookup = (EntityLookup) NativeEntityExtensions.lookup(world);
        return meldrite$paperEntityLookup;
    }
}
