package org.bukkit.craftbukkit.v1_20_R1.entity.memory;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.registry.Registries;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.entity.memory.MemoryKey;
public final class CraftMemoryKey {
    private CraftMemoryKey() {}
    @SuppressWarnings("unchecked") public static <T, U> MemoryModuleType<U> fromMemoryKey(MemoryKey<T> key) { return (MemoryModuleType<U>) Registries.MEMORY_MODULE_TYPE.get(CraftNamespacedKey.toMinecraft(java.util.Objects.requireNonNull(key, "key").getKey())); }
    public static <T, U> MemoryKey<U> toMemoryKey(MemoryModuleType<T> type) { return MemoryKey.getByKey(CraftNamespacedKey.fromMinecraft(Registries.MEMORY_MODULE_TYPE.getId(type))); }
}
