package dev.meldrite.v1201.hook;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
@Mixin(AreaEffectCloudEntity.class)
public abstract class AreaEffectCloudApplyEventMixin {
    @Shadow @Final private Map<Entity, Integer> affectedEntities;
    @Shadow private int reapplicationDelay;
    @Shadow private int duration;
    @Shadow private int durationOnUse;
    @Shadow private float radiusOnUse;
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getNonSpectatingEntities(Ljava/lang/Class;Lnet/minecraft/util/math/Box;)Ljava/util/List;"))
    private List<LivingEntity> meldrite$apply(World world, Class<LivingEntity> type, Box box, Operation<List<LivingEntity>> original, @Local(ordinal = 0) List<StatusEffectInstance> appliedEffects) {
        List<LivingEntity> nearby = original.call(world, type, box); var cloud = (AreaEffectCloudEntity) (Object) this;
        if (nearby.isEmpty() || !dev.meldrite.v1201.compat.NativeEntityExtensions.eventsAvailable()) return nearby;
        float radius = cloud.getRadius(); var eligible = new ArrayList<org.bukkit.entity.LivingEntity>();
        for (var entity : nearby) {
            double x = entity.getX() - cloud.getX(), z = entity.getZ() - cloud.getZ();
            if (!affectedEntities.containsKey(entity) && entity.isAffectedBySplashPotions() && x * x + z * z <= radius * radius) eligible.add((org.bukkit.entity.LivingEntity) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity));
        }
        var event = new org.bukkit.event.entity.AreaEffectCloudApplyEvent((org.bukkit.entity.AreaEffectCloud) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(cloud), eligible);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return List.of();
        for (var api : event.getAffectedEntities()) {
            if (!(api instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity craft)) continue;
            var entity = craft.getHandle(); affectedEntities.put(entity, cloud.age + reapplicationDelay);
            for (var effect : appliedEffects) {
                if (effect.getEffectType().isInstant()) effect.getEffectType().applyInstantEffect(cloud, cloud.getOwner(), entity, effect.getAmplifier(), .5);
                else entity.addStatusEffect(new StatusEffectInstance(effect), cloud);
            }
            if (radiusOnUse != 0) { radius += radiusOnUse; if (radius < .5f) { cloud.discard(); break; } cloud.setRadius(radius); }
            if (durationOnUse != 0) { duration += durationOnUse; if (duration <= 0) { cloud.discard(); break; } }
        }
        return List.of();
    }
}
