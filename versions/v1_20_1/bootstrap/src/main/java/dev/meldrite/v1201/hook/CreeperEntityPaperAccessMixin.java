package dev.meldrite.v1201.hook;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreeperEntity.class)
public interface CreeperEntityPaperAccessMixin {
    @Accessor("CHARGED") static TrackedData<Boolean> meldrite$charged() { throw new AssertionError(); }
    @Accessor("IGNITED") static TrackedData<Boolean> meldrite$ignited() { throw new AssertionError(); }
    @Accessor("currentFuseTime") int meldrite$getCurrentFuseTime();
    @Accessor("currentFuseTime") void meldrite$setCurrentFuseTime(int value);
    @Accessor("fuseTime") int meldrite$getFuseTime();
    @Accessor("fuseTime") void meldrite$setFuseTime(int value);
    @Accessor("explosionRadius") int meldrite$getExplosionRadius();
    @Accessor("explosionRadius") void meldrite$setExplosionRadius(int value);
    @Invoker("explode") void meldrite$explode();
}
