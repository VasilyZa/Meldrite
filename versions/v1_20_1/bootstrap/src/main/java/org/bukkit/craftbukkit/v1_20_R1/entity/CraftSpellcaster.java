package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.SpellcastingIllagerPaperAccessMixin;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;

public class CraftSpellcaster extends CraftIllager implements org.bukkit.entity.Spellcaster {
    public CraftSpellcaster(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public SpellcastingIllagerEntity getHandle() { return (SpellcastingIllagerEntity) super.getHandle(); }
    @Override public Spell getSpell() { return toBukkitSpell(((SpellcastingIllagerPaperAccessMixin) getHandle()).meldrite$getSpell()); }
    @Override public void setSpell(Spell spell) { java.util.Objects.requireNonNull(spell, "spell"); requireWritable(); getHandle().setSpell(toNMSSpell(spell)); }
    public static Spell toBukkitSpell(SpellcastingIllagerEntity.Spell spell) { return Spell.valueOf(spell.name()); }
    public static SpellcastingIllagerEntity.Spell toNMSSpell(Spell spell) { return SpellcastingIllagerEntity.Spell.byId(spell.ordinal()); }
}
