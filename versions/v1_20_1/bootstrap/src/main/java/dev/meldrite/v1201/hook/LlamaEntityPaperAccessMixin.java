package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.LlamaEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LlamaEntity.class)
public interface LlamaEntityPaperAccessMixin {
    @Invoker("setStrength") void meldrite$setStrength(int strength);
    @Accessor("follower") LlamaEntity meldrite$getFollower();
}
