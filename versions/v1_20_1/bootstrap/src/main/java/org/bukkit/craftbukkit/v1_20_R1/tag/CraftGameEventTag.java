package org.bukkit.craftbukkit.v1_20_R1.tag;

import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;

public final class CraftGameEventTag extends CraftTag<GameEvent, org.bukkit.GameEvent> {
    public CraftGameEventTag(Registry<GameEvent> registry, TagKey<GameEvent> tag) { super(registry, tag); }
    @Override protected GameEvent toNative(org.bukkit.GameEvent event) {
        return registry.getOrEmpty(new Identifier(event.getKey().getNamespace(), event.getKey().getKey())).orElse(null);
    }
    @Override protected org.bukkit.GameEvent toBukkit(GameEvent event) {
        var id = registry.getId(event);
        return org.bukkit.Registry.GAME_EVENT.get(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
    }
}
