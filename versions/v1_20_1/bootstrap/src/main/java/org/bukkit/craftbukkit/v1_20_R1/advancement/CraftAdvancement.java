package org.bukkit.craftbukkit.v1_20_R1.advancement;

import java.util.Collection;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;

public class CraftAdvancement implements org.bukkit.advancement.Advancement {
    private final net.minecraft.advancement.Advancement handle;
    public CraftAdvancement(net.minecraft.advancement.Advancement handle) { this.handle = Objects.requireNonNull(handle); }
    public net.minecraft.advancement.Advancement getHandle() { return handle; }
    @Override public NamespacedKey getKey() { return CraftNamespacedKey.fromMinecraft(handle.getId()); }
    @Override public Collection<String> getCriteria() { return java.util.Collections.unmodifiableSet(handle.getCriteria().keySet()); }
    @Override public CraftAdvancementDisplay getDisplay() { return handle.getDisplay() == null ? null : new CraftAdvancementDisplay(handle.getDisplay()); }
    public org.bukkit.advancement.AdvancementDisplay getDisplay0() { return getDisplay(); }
    @Override public Component displayName() { return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(handle.toHoverableText())); }
    @Override public CraftAdvancement getParent() { return handle.getParent() == null ? null : new CraftAdvancement(handle.getParent()); }
    @Override public Collection<org.bukkit.advancement.Advancement> getChildren() {
        var result = new java.util.ArrayList<org.bukkit.advancement.Advancement>();
        handle.getChildren().forEach(child -> result.add(new CraftAdvancement(child)));
        return java.util.List.copyOf(result);
    }
    @Override public CraftAdvancement getRoot() { return new CraftAdvancement(handle.getRoot()); }
    @Override public boolean equals(Object other) { return other instanceof CraftAdvancement craft && handle.equals(craft.handle); }
    @Override public int hashCode() { return handle.hashCode(); }
}
