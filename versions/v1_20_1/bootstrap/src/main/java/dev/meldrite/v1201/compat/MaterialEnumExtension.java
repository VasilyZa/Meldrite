package dev.meldrite.v1201.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.material.MaterialData;
import sun.misc.Unsafe;

/** Publishes genuine constants once, before plugin enum collections and switch tables exist. */
final class MaterialEnumExtension {
    private static final Unsafe UNSAFE = unsafe();
    private static final MethodHandle CONSTRUCTOR = constructor();

    private MaterialEnumExtension() { }

    static Material create(String name, int ordinal, int nativeId, int stack, int damage, NamespacedKey key, boolean block) {
        try {
            Material material = (Material) CONSTRUCTOR.invoke(name, ordinal, nativeId, stack, damage, MaterialData.class);
            field(Material.class, "key").set(material, key);
            field(Material.class, "isBlock").setBoolean(material, block);
            return material;
        } catch (Throwable failure) {
            throw new IllegalStateException("Could not construct native Material " + key, failure);
        }
    }

    @SuppressWarnings("unchecked")
    static void publish(List<Material> additions) {
        if (additions.isEmpty()) return;
        try {
            Material[] old = Material.values();
            Material[] values = Arrays.copyOf(old, old.length + additions.size());
            for (int index = 0; index < additions.size(); index++) {
                Material material = additions.get(index);
                if (material.ordinal() != old.length + index) throw new IllegalStateException("Non-contiguous native Material ordinal");
                values[material.ordinal()] = material;
            }
            Map<String, Material> names = (Map<String, Material>) field(Material.class, "BY_NAME").get(null);
            for (Material material : additions) {
                if (names.putIfAbsent(material.name(), material) != null) throw new IllegalStateException("Duplicate native Material name " + material.name());
            }
            Field storage = field(Material.class, "$VALUES");
            if (storage.getType() != Material[].class || !storage.isSynthetic()) throw new IllegalStateException("Unexpected Paper Material enum layout");
            UNSAFE.putObjectVolatile(UNSAFE.staticFieldBase(storage), UNSAFE.staticFieldOffset(storage), values);
            // Class caches a separate universe for valueOf, EnumSet and EnumMap.
            UNSAFE.putObjectVolatile(Material.class, UNSAFE.objectFieldOffset(Class.class.getDeclaredField("enumConstants")), null);
            UNSAFE.putObjectVolatile(Material.class, UNSAFE.objectFieldOffset(Class.class.getDeclaredField("enumConstantDirectory")), null);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Could not publish the native Material enum", failure);
        }
    }

    private static MethodHandle constructor() {
        try {
            return MethodHandles.privateLookupIn(Material.class, MethodHandles.lookup()).findConstructor(Material.class,
                    MethodType.methodType(void.class, String.class, int.class, int.class, int.class, int.class, Class.class));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Unsupported Paper Material constructor", failure);
        }
    }

    private static Field field(Class<?> owner, String name) throws ReflectiveOperationException {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Unsafe unsafe() {
        try {
            return (Unsafe) field(Unsafe.class, "theUnsafe").get(null);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Cannot publish native Material constants on this JVM", failure);
        }
    }
}
