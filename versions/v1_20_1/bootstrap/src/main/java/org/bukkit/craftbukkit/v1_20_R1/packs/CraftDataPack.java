package org.bukkit.craftbukkit.v1_20_R1.packs;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.metadata.PackResourceMetadata;
import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.CraftFeatureFlag;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;

@SuppressWarnings("removal")
public class CraftDataPack implements org.bukkit.packs.DataPack {
    private final ResourcePackProfile handle;
    public CraftDataPack(ResourcePackProfile handle) { this.handle = java.util.Objects.requireNonNull(handle); }
    public ResourcePackProfile getHandle() { return handle; }
    public String getRawId() { return handle.getName(); }
    @Override public NamespacedKey getKey() { return NamespacedKey.fromString(getRawId()); }
    @Override public String getTitle() { return CraftChatMessage.fromComponent(handle.getDisplayName()); }
    @Override public String getDescription() { return CraftChatMessage.fromComponent(handle.getDescription()); }
    @Override public int getPackFormat() {
        try (var pack = handle.createResourcePack()) {
            var metadata = pack.parseMetadata(PackResourceMetadata.SERIALIZER);
            return metadata == null ? 0 : metadata.getPackFormat();
        } catch (java.io.IOException failure) { throw new IllegalStateException("Cannot read datapack metadata: " + getRawId(), failure); }
    }
    @Override public boolean isEnabled() { return ((CraftServer) Bukkit.getServer()).getServer().getDataPackManager().getEnabledNames().contains(getRawId()); }
    @Override public boolean isRequired() { return handle.isAlwaysEnabled(); }
    @Override public Compatibility getCompatibility() { return switch (handle.getCompatibility()) { case COMPATIBLE -> Compatibility.COMPATIBLE; case TOO_NEW -> Compatibility.NEW; case TOO_OLD -> Compatibility.OLD; }; }
    @Override public Set<FeatureFlag> getRequestedFeatures() {
        return FeatureFlags.FEATURE_MANAGER.toId(handle.getRequestedFeatures()).stream()
                .map(id -> (FeatureFlag) CraftFeatureFlag.getFromNMS(CraftNamespacedKey.fromMinecraft(id))).collect(Collectors.toUnmodifiableSet());
    }
    @Override public Source getSource() {
        var source = handle.getSource();
        if (source == ResourcePackSource.BUILTIN) return Source.BUILT_IN;
        if (source == ResourcePackSource.FEATURE) return Source.FEATURE;
        if (source == ResourcePackSource.WORLD) return Source.WORLD;
        if (source == ResourcePackSource.SERVER) return Source.SERVER;
        return Source.DEFAULT;
    }
}
