package dev.meldrite.v1201.compat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.registry.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;

public final class NativeEnchantmentProjection {
    private NativeEnchantmentProjection() {}
    public static void install() {
        var pending = new java.util.ArrayList<CraftEnchantment>();
        for (var nativeEnchantment : Registries.ENCHANTMENT) {
            CraftEnchantment projected = new CraftEnchantment(nativeEnchantment);
            Enchantment current = Enchantment.getByKey(projected.getKey());
            if (current != null) {
                if (current instanceof CraftEnchantment craft && craft.getHandle() == nativeEnchantment) continue;
                throw new IllegalStateException("NATIVE_ENCHANTMENT_ALREADY_REGISTERED: " + projected.getKey());
            }
            Enchantment named = Enchantment.getByName(projected.getName());
            if (named != null) throw new IllegalStateException("NATIVE_ENCHANTMENT_NAME_ALREADY_REGISTERED: " + projected.getName());
            pending.add(projected);
        }
        pending.forEach(Enchantment::registerEnchantment);
    }
    public static org.bukkit.Registry<Enchantment> registry() {
        return new org.bukkit.Registry<>() {
            @Override public Enchantment get(NamespacedKey key) { return Enchantment.getByKey(Objects.requireNonNull(key)); }
            @Override public Iterator<Enchantment> iterator() { return stream().iterator(); }
            @Override public java.util.stream.Stream<Enchantment> stream() { return Arrays.stream(Enchantment.values()); }
        };
    }
}
