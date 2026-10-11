package dev.meldrite.v1201.hook;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(AbstractPiglinEntity.class)
public interface AbstractPiglinEntityPaperAccessMixin {
    @Accessor("timeInOverworld") int meldrite$timeInOverworld();
    @Accessor("timeInOverworld") void meldrite$timeInOverworld(int ticks);
    @Invoker("isImmuneToZombification") boolean meldrite$isImmune();
}
