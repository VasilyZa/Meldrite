package dev.meldrite.v1201.hook;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Entity.class)
public interface EntityPaperFlagAccessMixin { @Invoker("setFlag") void meldrite$setFlag(int flag, boolean enabled); @Invoker("setRotation") void meldrite$setRotation(float yaw, float pitch); @Invoker("isBeingRainedOn") boolean meldrite$isBeingRainedOn(); @Invoker("isInsideBubbleColumn") boolean meldrite$isInsideBubbleColumn(); @Invoker("scheduleVelocityUpdate") void meldrite$scheduleVelocityUpdate(); }
