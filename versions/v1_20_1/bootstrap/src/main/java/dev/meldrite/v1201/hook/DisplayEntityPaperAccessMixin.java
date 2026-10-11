package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.math.AffineTransformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(DisplayEntity.class)
public interface DisplayEntityPaperAccessMixin {
    @Invoker("getTransformation") static AffineTransformation meldrite$transformation(DataTracker tracker) { throw new AssertionError(); }
    @Invoker("setTransformation") void meldrite$transformation(AffineTransformation value);
    @Invoker("getInterpolationDuration") int meldrite$duration();
    @Invoker("setInterpolationDuration") void meldrite$duration(int value);
    @Invoker("getStartInterpolation") int meldrite$delay();
    @Invoker("setStartInterpolation") void meldrite$delay(int value);
    @Invoker("getBillboardMode") DisplayEntity.BillboardMode meldrite$billboard();
    @Invoker("setBillboardMode") void meldrite$billboard(DisplayEntity.BillboardMode value);
    @Invoker("getBrightnessUnpacked") Brightness meldrite$brightness();
    @Invoker("setBrightness") void meldrite$brightness(Brightness value);
    @Invoker("getViewRange") float meldrite$viewRange();
    @Invoker("setViewRange") void meldrite$viewRange(float value);
    @Invoker("getShadowRadius") float meldrite$shadowRadius();
    @Invoker("setShadowRadius") void meldrite$shadowRadius(float value);
    @Invoker("getShadowStrength") float meldrite$shadowStrength();
    @Invoker("setShadowStrength") void meldrite$shadowStrength(float value);
    @Invoker("getDisplayWidth") float meldrite$width();
    @Invoker("setDisplayWidth") void meldrite$width(float value);
    @Invoker("getDisplayHeight") float meldrite$height();
    @Invoker("setDisplayHeight") void meldrite$height(float value);
    @Invoker("getGlowColorOverride") int meldrite$glow();
    @Invoker("setGlowColorOverride") void meldrite$glow(int value);
}
