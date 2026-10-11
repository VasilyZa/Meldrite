package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.TntMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TntMinecartEntity.class)
public interface TntMinecartEntityPaperStateMixin extends dev.meldrite.v1201.compat.TntMinecartNativeAccess {
    @Override @Accessor("fuseTicks") int meldrite$getFuseTicks();
    @Override @Accessor("fuseTicks") void meldrite$setFuseTicks(int value);
}
