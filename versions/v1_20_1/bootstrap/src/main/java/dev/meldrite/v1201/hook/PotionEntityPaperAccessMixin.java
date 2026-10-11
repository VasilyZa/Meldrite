package dev.meldrite.v1201.hook;

import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PotionEntity.class)
public interface PotionEntityPaperAccessMixin {
    @Invoker("onCollision")
    void meldrite$onCollision(HitResult result);
}
