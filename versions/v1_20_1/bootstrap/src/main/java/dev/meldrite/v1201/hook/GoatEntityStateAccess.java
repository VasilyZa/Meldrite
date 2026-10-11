package dev.meldrite.v1201.hook;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.GoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GoatEntity.class)
public interface GoatEntityStateAccess {
    @Accessor("LEFT_HORN") static TrackedData<Boolean> meldrite$leftHorn() { throw new AssertionError("Mixin not applied"); }
    @Accessor("RIGHT_HORN") static TrackedData<Boolean> meldrite$rightHorn() { throw new AssertionError("Mixin not applied"); }
}
