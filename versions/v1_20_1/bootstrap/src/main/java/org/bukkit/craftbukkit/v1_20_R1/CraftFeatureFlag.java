package org.bukkit.craftbukkit.v1_20_R1;

import dev.meldrite.v1201.hook.FeatureManagerPaperAccessMixin;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;

public final class CraftFeatureFlag implements FeatureFlag {
    private final NamespacedKey key;
    private final net.minecraft.resource.featuretoggle.FeatureFlag handle;
    public CraftFeatureFlag(NamespacedKey key, net.minecraft.resource.featuretoggle.FeatureFlag handle) {
        this.key = java.util.Objects.requireNonNull(key); this.handle = java.util.Objects.requireNonNull(handle);
    }
    public static CraftFeatureFlag getFromNMS(NamespacedKey key) {
        java.util.Objects.requireNonNull(key);
        var flag = ((FeatureManagerPaperAccessMixin) FeatureFlags.FEATURE_MANAGER).meldrite$getFeatureFlags().get(new Identifier(key.getNamespace(), key.getKey()));
        return flag == null ? null : new CraftFeatureFlag(key, flag);
    }
    @Override public NamespacedKey getKey() { return key; }
    public net.minecraft.resource.featuretoggle.FeatureFlag getHandle() { return handle; }
    @Override public boolean equals(Object other) { return other instanceof CraftFeatureFlag flag && flag.handle == handle; }
    @Override public int hashCode() { return System.identityHashCode(handle); }
}
