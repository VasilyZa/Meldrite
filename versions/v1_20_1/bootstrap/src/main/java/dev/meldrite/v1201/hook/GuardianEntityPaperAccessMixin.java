package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.GuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuardianEntity.class)
public interface GuardianEntityPaperAccessMixin {
    @Invoker("setBeamTarget") void meldrite$setBeamTarget(int entityId);
}
