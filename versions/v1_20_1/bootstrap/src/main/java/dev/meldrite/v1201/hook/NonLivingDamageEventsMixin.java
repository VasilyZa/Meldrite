package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeDamageEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({net.minecraft.entity.decoration.ArmorStandEntity.class, net.minecraft.entity.decoration.ItemFrameEntity.class, net.minecraft.entity.decoration.EndCrystalEntity.class, net.minecraft.entity.ItemEntity.class, net.minecraft.entity.projectile.ShulkerBulletEntity.class, net.minecraft.entity.projectile.ExplosiveProjectileEntity.class})
public abstract class NonLivingDamageEventsMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void meldrite$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
        Entity entity = (Entity) (Object) this;
        if (entity.isRemoved() || entity.getWorld().isClient || entity.isInvulnerableTo(source)) return;
        if (entity instanceof net.minecraft.entity.decoration.ArmorStandEntity stand && stand.isMarker()) return;
        if (entity instanceof net.minecraft.entity.decoration.ItemFrameEntity frame && (((ItemFrameEntityPaperAccessMixin) frame).meldrite$isFixed() || frame.getHeldItemStack().isEmpty() || source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_EXPLOSION))) return;
        if (entity instanceof net.minecraft.entity.decoration.EndCrystalEntity && source.getAttacker() instanceof net.minecraft.entity.boss.dragon.EnderDragonEntity) return;
        if (entity instanceof net.minecraft.entity.ItemEntity item && (item.getStack().isOf(net.minecraft.item.Items.NETHER_STAR) && source.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_EXPLOSION) || !item.getStack().getItem().damage(source))) return;
        if (entity instanceof net.minecraft.entity.projectile.ExplosiveProjectileEntity && source.getAttacker() == null) return;
        boolean zeroCancels = entity instanceof net.minecraft.entity.ItemEntity || entity instanceof net.minecraft.entity.decoration.ArmorStandEntity;
        if (NativeDamageEvents.nonLiving(entity, source, amount, zeroCancels, entity instanceof net.minecraft.entity.decoration.ArmorStandEntity && entity.isInvisible())) callback.setReturnValue(false);
    }
}
