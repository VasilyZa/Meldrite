package dev.meldrite.v1201.hook;

import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FurnaceMinecartEntity.class)
public interface FurnaceMinecartEntityPaperStateMixin extends dev.meldrite.v1201.compat.FurnaceMinecartNativeAccess {
    @Override @Accessor("fuel") int meldrite$getFuel();
    @Override @Accessor("fuel") void meldrite$setFuel(int value);
}
