package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.EvokerEntityPaperAccessMixin;
import net.minecraft.entity.mob.EvokerEntity;

public class CraftEvoker extends CraftSpellcaster implements org.bukkit.entity.Evoker {
    public CraftEvoker(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public EvokerEntity getHandle() { return (EvokerEntity) super.getHandle(); }
    @Override public org.bukkit.entity.Evoker.Spell getCurrentSpell() { return org.bukkit.entity.Evoker.Spell.valueOf(getSpell().name()); }
    @Override public void setCurrentSpell(org.bukkit.entity.Evoker.Spell spell) { requireWritable(); getHandle().setSpell(spell == null ? net.minecraft.entity.mob.SpellcastingIllagerEntity.Spell.NONE : net.minecraft.entity.mob.SpellcastingIllagerEntity.Spell.byId(spell.ordinal())); }
    @Override public org.bukkit.entity.Sheep getWololoTarget() { var sheep = ((EvokerEntityPaperAccessMixin) getHandle()).meldrite$getWololoTarget(); return sheep == null ? null : (org.bukkit.entity.Sheep) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(sheep); }
    @Override public void setWololoTarget(org.bukkit.entity.Sheep sheep) { if (sheep != null && !(sheep instanceof CraftSheep)) throw new IllegalArgumentException("Sheep must be native-backed"); requireWritable(); ((EvokerEntityPaperAccessMixin) getHandle()).meldrite$setWololoTarget(sheep == null ? null : ((CraftSheep) sheep).getHandle()); }
}
