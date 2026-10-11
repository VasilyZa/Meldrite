package dev.meldrite.v1201.compat;
import dev.meldrite.v1201.hook.AllayEntityPaperAccessMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AllayEntity;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.CreatureSpawnEvent;
public final class NativeAllayBehavior {
    private NativeAllayBehavior() {}
    public static AllayEntity duplicate(AllayEntity original) {
        NativeEntityExtensions.requireThread(original);
        var duplicate = EntityType.ALLAY.create(original.getWorld()); if (duplicate == null) return null;
        duplicate.refreshPositionAfterTeleport(original.getPos()); duplicate.setPersistent();
        ((AllayEntityPaperAccessMixin) duplicate).meldrite$resetCooldown(); ((AllayEntityPaperAccessMixin) original).meldrite$resetCooldown();
        if (NativeEntityExtensions.eventsAvailable()) {
            var event = new CreatureSpawnEvent((org.bukkit.entity.LivingEntity) NativeEntityExtensions.bukkit(duplicate), CreatureSpawnEvent.SpawnReason.DUPLICATION);
            Bukkit.getPluginManager().callEvent(event); if (event.isCancelled()) return null;
        }
        if (!original.getWorld().spawnEntity(duplicate)) return null;
        return duplicate;
    }
}
