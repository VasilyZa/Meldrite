package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EvokerEntity.class)
public interface EvokerEntityPaperAccessMixin {
    @Invoker("getWololoTarget") SheepEntity meldrite$getWololoTarget();
    @Invoker("setWololoTarget") void meldrite$setWololoTarget(SheepEntity sheep);
}
