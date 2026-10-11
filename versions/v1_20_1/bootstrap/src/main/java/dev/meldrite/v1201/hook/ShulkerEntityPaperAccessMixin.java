package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerEntity.class)
public interface ShulkerEntityPaperAccessMixin {
    @Invoker("getPeekAmount") int meldrite$getPeekAmount();
    @Invoker("setPeekAmount") void meldrite$setPeekAmount(int amount);
    @Invoker("setAttachedFace") void meldrite$setAttachedFace(Direction face);
}
