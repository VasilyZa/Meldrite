package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.MooshroomEntityPaperAccessMixin;
import net.minecraft.entity.passive.MooshroomEntity;
public class CraftMushroomCow extends CraftCow implements org.bukkit.entity.MushroomCow, io.papermc.paper.entity.PaperShearable {
    public CraftMushroomCow(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public MooshroomEntity getHandle() { return (MooshroomEntity) super.getHandle(); }
    private MooshroomEntityPaperAccessMixin access() { return (MooshroomEntityPaperAccessMixin) getHandle(); }
    @Override public Variant getVariant() { return Variant.valueOf(getHandle().getVariant().name()); }
    @Override public void setVariant(Variant variant) { java.util.Objects.requireNonNull(variant, "variant"); requireWritable(); getHandle().setVariant(MooshroomEntity.Type.valueOf(variant.name())); }
    @Override public int getStewEffectDuration() { return access().meldrite$duration(); }
    @Override public void setStewEffectDuration(int ticks) { requireWritable(); access().meldrite$duration(ticks); }
    @Override public org.bukkit.potion.PotionEffectType getStewEffectType() { var effect = access().meldrite$effect(); return effect == null ? null : org.bukkit.potion.PotionEffectType.getById(net.minecraft.entity.effect.StatusEffect.getRawId(effect)); }
    @Override public void setStewEffect(org.bukkit.potion.PotionEffectType type) { requireWritable(); access().meldrite$effect(type == null ? null : net.minecraft.entity.effect.StatusEffect.byRawId(type.getId())); }
}
