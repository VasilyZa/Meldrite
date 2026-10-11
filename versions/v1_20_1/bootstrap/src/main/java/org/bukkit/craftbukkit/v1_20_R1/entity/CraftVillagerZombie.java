package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeVillagerConversionState;
import dev.meldrite.v1201.hook.ZombieVillagerEntityPaperAccessMixin;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Villager;

public class CraftVillagerZombie extends CraftZombie implements org.bukkit.entity.ZombieVillager {
    public CraftVillagerZombie(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public ZombieVillagerEntity getHandle() { return (ZombieVillagerEntity) super.getHandle(); }
    private ZombieVillagerEntityPaperAccessMixin conversion() { return (ZombieVillagerEntityPaperAccessMixin) getHandle(); }
    @Override public Villager.Profession getVillagerProfession() { return Villager.Profession.valueOf(Registries.VILLAGER_PROFESSION.getId(getHandle().getVillagerData().getProfession()).getPath().toUpperCase(java.util.Locale.ROOT)); }
    @Override public void setVillagerProfession(Villager.Profession profession) { java.util.Objects.requireNonNull(profession, "profession"); requireWritable(); var id = new Identifier("minecraft", profession.name().toLowerCase(java.util.Locale.ROOT)); getHandle().setVillagerData(getHandle().getVillagerData().withProfession(Registries.VILLAGER_PROFESSION.get(id))); }
    @Override public Villager.Type getVillagerType() { return Villager.Type.valueOf(Registries.VILLAGER_TYPE.getId(getHandle().getVillagerData().getType()).getPath().toUpperCase(java.util.Locale.ROOT)); }
    @Override public void setVillagerType(Villager.Type type) { java.util.Objects.requireNonNull(type, "type"); requireWritable(); var key = type.getKey(); getHandle().setVillagerData(getHandle().getVillagerData().withType(Registries.VILLAGER_TYPE.get(new Identifier(key.getNamespace(), key.getKey())))); }
    @Override public boolean isConverting() { return getHandle().isConverting(); }
    @Override public int getConversionTime() { if (!isConverting()) throw new IllegalStateException("Entity is not converting"); return conversion().meldrite$getConversionTimer(); }
    @Override public void setConversionTime(int ticks) { setConversionTime(ticks, true); }
    @Override public void setConversionTime(int ticks, boolean broadcast) {
        requireWritable();
        if (ticks < 0) { conversion().meldrite$setConversionTimer(-1); getHandle().getDataTracker().set(ZombieVillagerEntityPaperAccessMixin.meldrite$converting(), false); conversion().meldrite$setConverter(null); getHandle().removeStatusEffect(StatusEffects.STRENGTH); return; }
        var state = (NativeVillagerConversionState) getHandle();
        state.meldrite$setBroadcastConversion(broadcast);
        try { conversion().meldrite$startConversion(null, ticks); } finally { state.meldrite$setBroadcastConversion(true); }
    }
    @Override public OfflinePlayer getConversionPlayer() { var uuid = conversion().meldrite$getConverter(); return uuid == null ? null : getServer().getOfflinePlayer(uuid); }
    @Override public void setConversionPlayer(OfflinePlayer player) { requireWritable(); if (isConverting()) conversion().meldrite$setConverter(player == null ? null : player.getUniqueId()); }
}
