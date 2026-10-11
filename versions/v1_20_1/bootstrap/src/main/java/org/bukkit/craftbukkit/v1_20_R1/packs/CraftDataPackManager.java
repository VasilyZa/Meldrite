package org.bukkit.craftbukkit.v1_20_R1.packs;

import java.util.Collection;
import java.util.Objects;
import net.minecraft.resource.ResourcePackManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.EntityType;
import org.bukkit.packs.DataPack;

@SuppressWarnings("removal")
public class CraftDataPackManager implements org.bukkit.packs.DataPackManager {
    private final ResourcePackManager handle;
    public CraftDataPackManager(ResourcePackManager handle) { this.handle = Objects.requireNonNull(handle); }
    public ResourcePackManager getHandle() { return handle; }
    @Override public Collection<DataPack> getDataPacks() {
        dev.meldrite.v1201.compat.NativeRecipes.requireThread(); handle.scanPacks();
        return handle.getProfiles().stream().map(profile -> (DataPack) new CraftDataPack(profile)).toList();
    }
    @Override public DataPack getDataPack(NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        var profile = handle.getProfile(key.toString());
        if (profile == null && key.getNamespace().equals("minecraft")) profile = handle.getProfile(key.getKey());
        return profile == null ? null : new CraftDataPack(profile);
    }
    private Collection<DataPack> packs(Collection<String> names) { return names.stream().map(handle::getProfile).filter(Objects::nonNull).map(profile -> (DataPack) new CraftDataPack(profile)).toList(); }
    private CraftWorld world(World world) { if (!(Objects.requireNonNull(world) instanceof CraftWorld craft)) throw new IllegalArgumentException("Native world required"); return craft; }
    @Override public Collection<DataPack> getEnabledDataPacks(World world) { return packs(world(world).getHandle().getServer().getSaveProperties().getDataConfiguration().dataPacks().getEnabled()); }
    @Override public Collection<DataPack> getDisabledDataPacks(World world) { return packs(world(world).getHandle().getServer().getSaveProperties().getDataConfiguration().dataPacks().getDisabled()); }
    @Override public boolean isEnabledByFeature(Material material, World world) {
        Objects.requireNonNull(material); var features = world(world).getHandle().getEnabledFeatures();
        if (material.isItem()) return CraftMagicNumbers.getItem(material).isEnabled(features);
        if (material.isBlock()) return CraftMagicNumbers.getBlock(material).isEnabled(features);
        throw new IllegalArgumentException("Material must be an item or block");
    }
    @Override public boolean isEnabledByFeature(EntityType type, World world) {
        Objects.requireNonNull(type); if (type == EntityType.UNKNOWN) throw new IllegalArgumentException("Unknown entity type");
        var nativeType = net.minecraft.entity.EntityType.get(type.getKey().toString()).orElseThrow(() -> new IllegalArgumentException("Unknown native entity type"));
        return nativeType.isEnabled(world(world).getHandle().getEnabledFeatures());
    }
}
