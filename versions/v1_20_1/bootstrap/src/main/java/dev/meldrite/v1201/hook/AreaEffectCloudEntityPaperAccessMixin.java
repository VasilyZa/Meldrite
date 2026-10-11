package dev.meldrite.v1201.hook;
import java.util.List;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(AreaEffectCloudEntity.class)
public interface AreaEffectCloudEntityPaperAccessMixin {
    @Accessor("waitTime") int meldrite$getWaitTime();
    @Accessor("waitTime") void meldrite$setWaitTime(int value);
    @Accessor("reapplicationDelay") int meldrite$getReapplicationDelay();
    @Accessor("reapplicationDelay") void meldrite$setReapplicationDelay(int value);
    @Accessor("durationOnUse") int meldrite$getDurationOnUse();
    @Accessor("durationOnUse") void meldrite$setDurationOnUse(int value);
    @Accessor("radiusOnUse") float meldrite$getRadiusOnUse();
    @Accessor("radiusOnUse") void meldrite$setRadiusOnUse(float value);
    @Accessor("radiusGrowth") float meldrite$getRadiusGrowth();
    @Accessor("radiusGrowth") void meldrite$setRadiusGrowth(float value);
    @Accessor("effects") List<StatusEffectInstance> meldrite$effects();
    @Accessor("potion") Potion meldrite$getPotion();
    @Accessor("ownerUuid") java.util.UUID meldrite$getOwnerUuid();
    @Accessor("ownerUuid") void meldrite$setOwnerUuid(java.util.UUID value);
    @Invoker("updateColor") void meldrite$updateColor();
}
