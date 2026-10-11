package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.FireballNativeAccess;
import net.minecraft.entity.projectile.FireballEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FireballEntity.class)
public interface FireballEntityPaperAccessMixin extends FireballNativeAccess {
    @Override @Accessor("explosionPower") int meldrite$getExplosionPower();
    @Override @Accessor("explosionPower") void meldrite$setExplosionPower(int value);
}
