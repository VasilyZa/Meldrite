package dev.meldrite.v1201.hook;
import net.minecraft.entity.mob.PiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(PiglinEntity.class)
public interface PiglinEntityPaperAccessMixin {
    @Accessor("cannotHunt") boolean meldrite$cannotHunt();
    @Accessor("cannotHunt") void meldrite$cannotHunt(boolean value);
    @Invoker("isCharging") boolean meldrite$isCharging();
}
