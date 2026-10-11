package dev.meldrite.v1201.hook;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({net.minecraft.entity.vehicle.BoatEntity.class, net.minecraft.entity.vehicle.AbstractMinecartEntity.class})
public abstract class VehicleDamageEventsMixin {
    @WrapMethod(method = "damage")
    private boolean meldrite$damage(DamageSource source, float amount, Operation<Boolean> original) {
        Entity entity = (Entity) (Object) this;
        if (!NativeDamageEvents.available(entity) || entity.isInvulnerableTo(source)) return original.call(source, amount);
        var event = NativeDamageEvents.vehicleDamage(entity, source, amount);
        if (event.isCancelled() || entity.isRemoved()) return false;
        float edited = (float) event.getDamage();
        float wobble = entity instanceof net.minecraft.entity.vehicle.BoatEntity boat ? boat.getDamageWobbleStrength() : ((net.minecraft.entity.vehicle.AbstractMinecartEntity) entity).getDamageWobbleStrength();
        if (source.isSourceCreativePlayer() || wobble + edited * 10 > 40) {
            if (!NativeDamageEvents.vehicleDestroy(entity, source)) {
                if (entity instanceof net.minecraft.entity.vehicle.BoatEntity boat) { boat.setDamageWobbleSide(-boat.getDamageWobbleSide()); boat.setDamageWobbleTicks(10); boat.setDamageWobbleStrength(40); }
                else { var cart = (net.minecraft.entity.vehicle.AbstractMinecartEntity) entity; cart.setDamageWobbleSide(-cart.getDamageWobbleSide()); cart.setDamageWobbleTicks(10); cart.setDamageWobbleStrength(40); }
                ((EntityPaperFlagAccessMixin) entity).meldrite$scheduleVelocityUpdate(); entity.emitGameEvent(net.minecraft.world.event.GameEvent.ENTITY_DAMAGE, source.getAttacker()); return true;
            }
        }
        return !entity.isRemoved() && original.call(source, edited);
    }
}
