package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.PotionEntityPaperAccessMixin;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class CraftThrownPotion extends CraftThrowableProjectile implements org.bukkit.entity.ThrownPotion {
    public CraftThrownPotion(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public PotionEntity getHandle() { return (PotionEntity) super.getHandle(); }
    private static org.bukkit.potion.PotionEffect effect(StatusEffectInstance effect) {
        var id = net.minecraft.registry.Registries.STATUS_EFFECT.getId(effect.getEffectType());
        var type = org.bukkit.potion.PotionEffectType.getByKey(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_EFFECT: " + id);
        return new org.bukkit.potion.PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon());
    }
    @Override public java.util.Collection<org.bukkit.potion.PotionEffect> getEffects() { return PotionUtil.getPotionEffects(getHandle().getStack()).stream().map(CraftThrownPotion::effect).toList(); }
    @Override public org.bukkit.inventory.meta.PotionMeta getPotionMeta() {
        var meta = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.getItemMeta(getHandle().getStack());
        return meta instanceof org.bukkit.inventory.meta.PotionMeta potion ? potion : null;
    }
    @Override public void setPotionMeta(org.bukkit.inventory.meta.PotionMeta meta) {
        java.util.Objects.requireNonNull(meta, "meta");
        delegate.requireWritable();
        var item = org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asCraftCopy(getItem());
        if (item.getType() != org.bukkit.Material.SPLASH_POTION && item.getType() != org.bukkit.Material.LINGERING_POTION) item.setType(org.bukkit.Material.SPLASH_POTION);
        if (!item.setItemMeta(meta)) throw new IllegalArgumentException("Potion meta cannot be applied");
        setItem(item);
    }
    @Override public void splash() {
        delegate.requireWritable();
        var pos = getHandle().getBlockPos();
        ((PotionEntityPaperAccessMixin) (Object) getHandle()).meldrite$onCollision(new BlockHitResult(getHandle().getPos(), Direction.UP, pos, false));
    }
}
