package dev.meldrite.paper.facade;

import java.util.Map;

/** CraftItemStack conversion boundary without a compile-time dependency on Minecraft. */
public final class CraftItemStackAdapter {
    private CraftItemStackAdapter() { }

    public interface Bridge {
        Object toNative(org.bukkit.inventory.ItemStack stack);
        org.bukkit.inventory.ItemStack toBukkit(Object stack);
    }

    /** Native mirrors retain items which cannot be represented by Bukkit's Material enum. */
    public interface NativeBacked {
        Object nativeCopy();
    }

    public interface NamedBridge {
        Object toNative(String namespace, String key, int amount);
        Object toBukkit(Object stack);
        void writeMetadata(Object stack, Map<String, Object> metadata);
        Map<String, Object> readMetadata(Object stack);
    }

    private static volatile Bridge bridge;

    public static void bind(Bridge value) {
        bridge = java.util.Objects.requireNonNull(value, "bridge");
    }

    public static void bind(NamedBridge value) {
        NamedBridge named = java.util.Objects.requireNonNull(value, "bridge");
        bind(new Bridge() {
            @Override public Object toNative(org.bukkit.inventory.ItemStack stack) {
                var key = stack.getType().getKey();
                Object result = named.toNative(key.getNamespace(), key.getKey(), stack.getAmount());
                named.writeMetadata(result, ItemFactoryAdapter.writeNativeMetadata(stack.getItemMeta()));
                return result;
            }
            @Override public org.bukkit.inventory.ItemStack toBukkit(Object stack) {
                var result = (org.bukkit.inventory.ItemStack) named.toBukkit(stack);
                Map<String, Object> metadata = java.util.Objects.requireNonNull(named.readMetadata(stack), "metadata");
                if (!metadata.isEmpty()) result.setItemMeta(ItemFactoryAdapter.readNativeMetadata(result.getType(), metadata));
                return result;
            }
        });
    }

    public static Object asNMSCopy(org.bukkit.inventory.ItemStack stack) {
        if (stack instanceof NativeBacked nativeStack) return nativeStack.nativeCopy();
        Bridge value = bridge;
        if (value == null) throw new IllegalStateException("CRAFT_ITEM_STACK_BRIDGE_NOT_BOUND");
        return value.toNative(stack);
    }

    public static org.bukkit.inventory.ItemStack asBukkitCopy(Object stack) {
        Bridge value = bridge;
        if (value == null) throw new IllegalStateException("CRAFT_ITEM_STACK_BRIDGE_NOT_BOUND");
        return value.toBukkit(stack);
    }

}
