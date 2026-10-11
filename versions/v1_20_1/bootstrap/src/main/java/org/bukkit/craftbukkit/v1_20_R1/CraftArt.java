package org.bukkit.craftbukkit.v1_20_R1;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.Identifier;
import org.bukkit.Art;
public final class CraftArt {
    private CraftArt() {}
    public static Art NotchToBukkit(RegistryEntry<PaintingVariant> entry) {
        var id = Registries.PAINTING_VARIANT.getId(entry.value()); return id == null ? null : org.bukkit.Registry.ART.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
    }
    public static RegistryEntry<PaintingVariant> BukkitToNotch(Art art) { var key = java.util.Objects.requireNonNull(art, "art").getKey(); return Registries.PAINTING_VARIANT.getEntry(net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.PAINTING_VARIANT, new Identifier(key.getNamespace(), key.getKey()))).orElseThrow(() -> new IllegalArgumentException("Unknown art: " + key)); }
}
