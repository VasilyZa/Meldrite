package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.DisplayEntityPaperAccessMixin;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.AffineTransformation;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
public class CraftDisplay extends CraftEntity implements Display {
    public CraftDisplay(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public DisplayEntity getHandle() { return (DisplayEntity) super.getHandle(); }
    private DisplayEntityPaperAccessMixin access() { return (DisplayEntityPaperAccessMixin) getHandle(); }
    @Override public Transformation getTransformation() { var value = DisplayEntityPaperAccessMixin.meldrite$transformation(getHandle().getDataTracker()); return new Transformation(value.getTranslation(), value.getLeftRotation(), value.getScale(), value.getRightRotation()); }
    @Override public void setTransformation(Transformation value) { java.util.Objects.requireNonNull(value, "transformation"); delegate.requireWritable(); access().meldrite$transformation(new AffineTransformation(value.getTranslation(), value.getLeftRotation(), value.getScale(), value.getRightRotation())); }
    @Override public void setTransformationMatrix(org.joml.Matrix4f value) { java.util.Objects.requireNonNull(value, "matrix"); delegate.requireWritable(); access().meldrite$transformation(new AffineTransformation(value)); }
    @Override public int getInterpolationDuration() { return access().meldrite$duration(); }
    @Override public void setInterpolationDuration(int value) { delegate.requireWritable(); access().meldrite$duration(value); }
    @Override public int getInterpolationDelay() { return access().meldrite$delay(); }
    @Override public void setInterpolationDelay(int value) { delegate.requireWritable(); access().meldrite$delay(value); }
    @Override public float getViewRange() { return access().meldrite$viewRange(); }
    @Override public void setViewRange(float value) { delegate.requireWritable(); access().meldrite$viewRange(value); }
    @Override public float getShadowRadius() { return access().meldrite$shadowRadius(); }
    @Override public void setShadowRadius(float value) { delegate.requireWritable(); access().meldrite$shadowRadius(value); }
    @Override public float getShadowStrength() { return access().meldrite$shadowStrength(); }
    @Override public void setShadowStrength(float value) { delegate.requireWritable(); access().meldrite$shadowStrength(value); }
    @Override public float getDisplayWidth() { return access().meldrite$width(); }
    @Override public void setDisplayWidth(float value) { delegate.requireWritable(); access().meldrite$width(value); }
    @Override public float getDisplayHeight() { return access().meldrite$height(); }
    @Override public void setDisplayHeight(float value) { delegate.requireWritable(); access().meldrite$height(value); }
    @Override public Billboard getBillboard() { return Billboard.valueOf(access().meldrite$billboard().name()); }
    @Override public void setBillboard(Billboard value) { java.util.Objects.requireNonNull(value, "billboard"); delegate.requireWritable(); access().meldrite$billboard(DisplayEntity.BillboardMode.valueOf(value.name())); }
    @Override public Color getGlowColorOverride() { int value = access().meldrite$glow(); return value == -1 ? null : Color.fromARGB(value); }
    @Override public void setGlowColorOverride(Color value) { delegate.requireWritable(); access().meldrite$glow(value == null ? -1 : value.asARGB()); }
    @Override public Brightness getBrightness() { var value = access().meldrite$brightness(); return value == null ? null : new Brightness(value.block(), value.sky()); }
    @Override public void setBrightness(Brightness value) { delegate.requireWritable(); access().meldrite$brightness(value == null ? null : new net.minecraft.entity.decoration.Brightness(value.getBlockLight(), value.getSkyLight())); }
}
