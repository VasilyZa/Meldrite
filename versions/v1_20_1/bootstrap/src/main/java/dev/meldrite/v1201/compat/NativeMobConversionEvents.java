package dev.meldrite.v1201.compat;

import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public final class NativeMobConversionEvents {
    private record Context(MobEntity original, EntityTransformEvent.TransformReason transform, CreatureSpawnEvent.SpawnReason spawn) {}
    private static final ThreadLocal<Context> ACTIVE = new ThreadLocal<>();
    private NativeMobConversionEvents() {}
    public static <T extends MobEntity> T convert(MobEntity original, EntityType<T> type, boolean equipment, EntityTransformEvent.TransformReason transform, CreatureSpawnEvent.SpawnReason spawn) {
        NativeEntityExtensions.requireThread(original); var previous = ACTIVE.get(); ACTIVE.set(new Context(original, transform, spawn));
        try { return original.convertTo(type, equipment); }
        finally { if (previous == null) ACTIVE.remove(); else ACTIVE.set(previous); }
    }
    public static boolean allow(MobEntity original, MobEntity converted) {
        var context = ACTIVE.get(); if (context == null || context.original() != original || !NativeEntityExtensions.eventsAvailable()) return true;
        var api = NativeEntityExtensions.bukkit(converted);
        var transform = new EntityTransformEvent(NativeEntityExtensions.bukkit(original), List.of(api), context.transform());
        Bukkit.getPluginManager().callEvent(transform); if (transform.isCancelled()) return false;
        var spawn = new CreatureSpawnEvent((org.bukkit.entity.LivingEntity) api, context.spawn()); Bukkit.getPluginManager().callEvent(spawn);
        return !spawn.isCancelled();
    }
}
