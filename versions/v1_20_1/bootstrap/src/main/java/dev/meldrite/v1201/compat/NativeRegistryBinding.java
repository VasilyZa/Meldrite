package dev.meldrite.v1201.compat;

import dev.meldrite.paper.registry.RegistryProjection;
import java.util.Map;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R1.CraftGameEvent;
import org.bukkit.craftbukkit.v1_20_R1.CraftMusicInstrument;
import org.bukkit.craftbukkit.v1_20_R1.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R1.generator.structure.CraftStructure;
import org.bukkit.craftbukkit.v1_20_R1.generator.structure.CraftStructureType;
import org.bukkit.craftbukkit.v1_20_R1.inventory.trim.CraftTrimMaterial;
import org.bukkit.craftbukkit.v1_20_R1.inventory.trim.CraftTrimPattern;

public final class NativeRegistryBinding {
    private NativeRegistryBinding() { }
    public static void bind(MinecraftServer server) {
        Map<Class<?>, Registry<?>> projections = new java.util.concurrent.ConcurrentHashMap<>();
        RegistryProjection.bind(type -> projections.computeIfAbsent(type, key -> create(server, key)));
    }
    private static Registry<?> create(MinecraftServer server, Class<?> type) {
        var registries = server.getRegistryManager();
        if (type == org.bukkit.GameEvent.class) return new CraftRegistry<>(Registries.GAME_EVENT, CraftGameEvent::new);
        if (type == org.bukkit.MusicInstrument.class) return new CraftRegistry<>(registries.get(RegistryKeys.INSTRUMENT), CraftMusicInstrument::new);
        if (type == org.bukkit.generator.structure.Structure.class) return new CraftRegistry<>(registries.get(RegistryKeys.STRUCTURE), CraftStructure::new);
        if (type == org.bukkit.generator.structure.StructureType.class) return new CraftRegistry<>(Registries.STRUCTURE_TYPE, CraftStructureType::new);
        if (type == org.bukkit.inventory.meta.trim.TrimMaterial.class) return new CraftRegistry<>(registries.get(RegistryKeys.TRIM_MATERIAL), CraftTrimMaterial::new);
        if (type == org.bukkit.inventory.meta.trim.TrimPattern.class) return new CraftRegistry<>(registries.get(RegistryKeys.TRIM_PATTERN), CraftTrimPattern::new);
        if (type == org.bukkit.potion.PotionEffectType.class) return dev.meldrite.paper.registry.PotionEffectProjection.registry();
        if (type == org.bukkit.enchantments.Enchantment.class) return NativeEnchantmentProjection.registry();
        return null;
    }
}
