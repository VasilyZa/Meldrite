package org.bukkit.craftbukkit.v1_20_R1.entity;

import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.WardenAngerManager;
import net.minecraft.entity.mob.Angriness;
import net.minecraft.entity.mob.WardenEntity;
import org.bukkit.Location;

public final class CraftWarden extends CraftMonster implements org.bukkit.entity.Warden {
    public CraftWarden(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public WardenEntity getHandle() { return (WardenEntity) super.getHandle(); }
    private WardenAngerManager anger() { return getHandle().getAngerManager(); }
    private Entity nativeEntity(org.bukkit.entity.Entity entity) {
        if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("FOREIGN_ENTITY_FACADE");
        if (craft.getWorld() != getWorld()) throw new IllegalArgumentException("ENTITY_IN_DIFFERENT_WORLD");
        return craft.getHandle();
    }
    @Override public int getAnger() {
        var target = getHandle().getTarget();
        return target == null ? getHighestAnger() : anger().getAngerFor(target);
    }
    @Override public int getAnger(org.bukkit.entity.Entity entity) { return anger().getAngerFor(nativeEntity(entity)); }
    @Override public int getHighestAnger() { return getHandle().getAnger(); }
    @Override public void increaseAnger(org.bukkit.entity.Entity entity, int increase) {
        if (increase < 0) throw new IllegalArgumentException("increase must be nonnegative");
        requireWritable();
        anger().increaseAngerAt(nativeEntity(entity), increase);
    }
    @Override public void setAnger(org.bukkit.entity.Entity entity, int value) {
        if (value < 0) throw new IllegalArgumentException("anger must be nonnegative");
        requireWritable();
        Entity target = nativeEntity(entity);
        getHandle().removeSuspect(target);
        if (value > 0) anger().increaseAngerAt(target, value);
    }
    @Override public void clearAnger(org.bukkit.entity.Entity entity) { requireWritable(); getHandle().removeSuspect(nativeEntity(entity)); }
    @Override public org.bukkit.entity.LivingEntity getEntityAngryAt() {
        return getHandle().getPrimeSuspect().map(NativeEntityExtensions::bukkit)
                .map(entity -> (org.bukkit.entity.LivingEntity) entity).orElse(null);
    }
    @Override public void setDisturbanceLocation(Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != getWorld()) throw new IllegalArgumentException("location is in a different world");
        requireWritable();
        getHandle().getBrain().remember(net.minecraft.entity.ai.brain.MemoryModuleType.DISTURBANCE_LOCATION,
                new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
    @Override public org.bukkit.entity.Warden.AngerLevel getAngerLevel() {
        return switch (getHandle().getAngriness()) {
            case CALM -> org.bukkit.entity.Warden.AngerLevel.CALM;
            case AGITATED -> org.bukkit.entity.Warden.AngerLevel.AGITATED;
            case ANGRY -> org.bukkit.entity.Warden.AngerLevel.ANGRY;
        };
    }
}
