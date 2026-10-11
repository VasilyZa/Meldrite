package dev.meldrite.v1201.hook;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/** DataWatcher short names retained by older Bukkit NMS adapters. */
@Mixin(DataTracker.class)
public abstract class LegacyDataTrackerAliasesMixin {
    public <T> T a(TrackedData<T> key) {
        return ((DataTracker) (Object) this).get(key);
    }

    public <T> void a(TrackedData<T> key, T value) {
        ((DataTracker) (Object) this).set(key, value);
    }

    public List<?> packAll() {
        return ((DataTracker) (Object) this).getChangedEntries();
    }
}
