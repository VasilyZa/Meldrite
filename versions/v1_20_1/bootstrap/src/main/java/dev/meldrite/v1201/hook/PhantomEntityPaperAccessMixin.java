package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.PhantomEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PhantomEntity.class)
public interface PhantomEntityPaperAccessMixin {
    @Invoker("setPhantomSize") void meldrite$setPhantomSize(int size);
    @Invoker("getPhantomSize") int meldrite$getPhantomSize();
}
