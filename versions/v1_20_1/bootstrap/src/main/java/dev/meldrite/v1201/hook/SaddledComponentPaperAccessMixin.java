package dev.meldrite.v1201.hook;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(SaddledComponent.class)
public interface SaddledComponentPaperAccessMixin {
    @Accessor("boosted") boolean meldrite$isBoosting();
    @Accessor("boosted") void meldrite$setBoosting(boolean value);
    @Accessor("boostedTime") int meldrite$currentBoostTime();
    @Accessor("boostedTime") void meldrite$currentBoostTime(int value);
    @Accessor("dataTracker") DataTracker meldrite$tracker();
    @Accessor("boostTime") TrackedData<Integer> meldrite$boostTime();
    @Invoker("getBoostTime") int meldrite$maximumBoostTime();
}
