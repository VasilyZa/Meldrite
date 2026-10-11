package org.bukkit.craftbukkit.inventory;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.meldrite.paper.facade.ItemMetaState;
import dev.meldrite.paper.facade.ItemNbtData;
import dev.meldrite.paper.facade.PlayerProfileAdapter;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;

/** Concrete 1.20.1 skull ABI, including the authlib field used by texture libraries. */
@org.bukkit.configuration.serialization.DelegateDeserialization(CraftMetaItem.SerializableMeta.class)
public final class CraftMetaSkull extends CraftMetaItem implements SkullMeta {
    private GameProfile profile;
    private GameProfile importedProfile;
    private boolean profileEdited;
    private boolean soundEdited;
    private NamespacedKey noteBlockSound;

    public CraftMetaSkull() { }
    private CraftMetaSkull(CraftMetaSkull source) {
        super(source);
        profile = PlayerProfileAdapter.copy(source.profile);
        importedProfile = PlayerProfileAdapter.copy(source.importedProfile);
        profileEdited = source.profileEdited;
        soundEdited = source.soundEdited;
        noteBlockSound = source.noteBlockSound;
    }
    @Override public CraftMetaSkull clone() { return new CraftMetaSkull(this); }
    @Override public boolean hasOwner() { return profile != null && profile.getName() != null; }
    @Override public String getOwner() { return hasOwner() ? profile.getName() : null; }
    @Override public boolean setOwner(String name) { if (name != null && name.length() > 16) return false; profile = name == null ? null : new GameProfile(null, name); profileEdited = true; return true; }
    @Override public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile value) { profile = value == null ? null : PlayerProfileAdapter.from(value); profileEdited = true; }
    @Override public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() { return profile == null ? null : new PlayerProfileAdapter(profile); }
    @Override public OfflinePlayer getOwningPlayer() { if (!hasOwner()) return null; return profile.getId() == null ? Bukkit.getOfflinePlayer(profile.getName()) : Bukkit.getOfflinePlayer(profile.getId()); }
    @Override public boolean setOwningPlayer(OfflinePlayer owner) {
        if (owner == null) profile = null;
        else profile = new GameProfile(owner.getUniqueId(), owner.getName());
        profileEdited = true;
        return true;
    }
    @Override public org.bukkit.profile.PlayerProfile getOwnerProfile() { return hasOwner() ? new PlayerProfileAdapter(profile) : null; }
    @Override public void setOwnerProfile(org.bukkit.profile.PlayerProfile value) {
        GameProfile next = value == null ? null : PlayerProfileAdapter.from(value);
        if (next != null && next.getName() == null && (next.getId() == null || !next.getProperties().containsKey("textures"))) throw new IllegalArgumentException("Skull profile requires a name or UUID and textures");
        profile = next;
        profileEdited = true;
    }
    @Override public void setNoteBlockSound(NamespacedKey value) { noteBlockSound = value; soundEdited = true; }
    @Override public NamespacedKey getNoteBlockSound() { return noteBlockSound; }
    @Override public Map<String, Object> nativeMetadata() {
        Map<String, Object> result = super.nativeMetadata();
        boolean unchangedProfile = !profileEdited && (profile == null ? importedProfile == null
                : importedProfile != null && new PlayerProfileAdapter(profile).equals(new PlayerProfileAdapter(importedProfile)));
        Map<String, Object> originalOwner = ItemNbtData.compound(result.get("SkullOwner"));
        if (!unchangedProfile) result.remove("SkullOwner");
        if (!unchangedProfile && profile != null) {
            Map<String, Object> owner = new LinkedHashMap<>(originalOwner);
            owner.remove("Id"); owner.remove("Name"); owner.remove("Properties");
            if (profile.getId() != null) owner.put("Id", profile.getId());
            if (profile.getName() != null) owner.put("Name", profile.getName());
            if (!profile.getProperties().isEmpty()) {
                Map<String, Object> properties = new LinkedHashMap<>();
                for (String key : profile.getProperties().keySet()) {
                    List<Object> values = new ArrayList<>();
                    for (Property property : profile.getProperties().get(key)) {
                        Map<String, Object> value = new LinkedHashMap<>();
                        Object originals = ItemNbtData.compound(originalOwner.get("Properties")).get(key);
                        if (originals instanceof List<?> list) for (Object original : list) {
                            Map<String, Object> fields = ItemNbtData.compound(original);
                            if (Objects.equals(fields.get("Value"), property.getValue()) && Objects.equals(fields.get("Signature"), property.getSignature())) { value.putAll(fields); break; }
                        }
                        value.put("Value", property.getValue());
                        if (property.hasSignature()) value.put("Signature", property.getSignature()); values.add(value);
                    }
                    properties.put(key, values);
                }
                owner.put("Properties", properties);
            }
            result.put("SkullOwner", owner);
        }
        Map<String, Object> blockEntity = ItemNbtData.compound(result.get("BlockEntityTag"));
        if (noteBlockSound == null && soundEdited) {
            if (!blockEntity.isEmpty()) { blockEntity.remove("note_block_sound"); if (blockEntity.isEmpty()) result.remove("BlockEntityTag"); }
        } else if (noteBlockSound != null) {
            if (blockEntity.isEmpty()) { blockEntity = new LinkedHashMap<>(blockEntity); result.put("BlockEntityTag", blockEntity); }
            blockEntity.put("note_block_sound", noteBlockSound.toString());
        }
        return result;
    }
    @Override public void nativeMetadata(Map<String, Object> metadata) {
        super.nativeMetadata(metadata);
        Object owner = metadata.get("SkullOwner");
        if (owner instanceof String name && !name.isEmpty()) profile = new GameProfile(null, name);
        else if (owner instanceof Map<?, ?>) {
            Map<String, Object> value = ItemNbtData.compound(owner);
            UUID id = ItemNbtData.uuid(value.get("Id"));
            String name = value.get("Name") instanceof String text ? text : null;
            profile = id == null && (name == null || name.isEmpty()) ? null : new GameProfile(id, name);
            if (profile != null) for (var entry : ItemNbtData.compound(value.get("Properties")).entrySet()) {
                if (entry.getValue() instanceof List<?> list) for (Object property : list) {
                    Map<String, Object> fields = ItemNbtData.compound(property);
                    if (fields.get("Value") instanceof String text) profile.getProperties().put(entry.getKey(), new Property(entry.getKey(), text, fields.get("Signature") instanceof String signature ? signature : null));
                }
            }
        } else profile = null;
        importedProfile = PlayerProfileAdapter.copy(profile);
        profileEdited = false;
        soundEdited = false;
        Object sound = ItemNbtData.compound(metadata.get("BlockEntityTag")).get("note_block_sound");
        noteBlockSound = sound instanceof String text ? NamespacedKey.fromString(text) : null;
    }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = super.serialize(); result.put("meta-type", "SKULL"); if (profile != null) result.put("skull-owner", new PlayerProfileAdapter(profile).serialize()); if (noteBlockSound != null) result.put("note_block_sound", noteBlockSound.toString()); return result; }
}
