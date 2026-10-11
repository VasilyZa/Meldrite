package org.bukkit.craftbukkit.profile;

import com.mojang.authlib.GameProfile;
import com.destroystokyo.paper.profile.SharedPlayerProfile;
import com.mojang.authlib.properties.Property;
import dev.meldrite.paper.facade.PlayerProfileAdapter;
import java.util.Map;
import java.util.UUID;

/**
 * CraftBukkit's concrete profile entry point.  The state and completion
 * operations are shared with the server profile bridge so reflective plugin
 * casts observe the same live profile semantics as Bukkit.createPlayerProfile.
 */
public class CraftPlayerProfile extends PlayerProfileAdapter implements SharedPlayerProfile {
    public CraftPlayerProfile(UUID uniqueId, String name) { super(uniqueId, name); }
    public CraftPlayerProfile(GameProfile profile) { super(profile); }
    protected CraftPlayerProfile(GameProfile profile, boolean detached) { super(profile, detached); }

    @Override public Property getProperty(String propertyName) {
        return gameProfile().getProperties().get(propertyName).stream().findFirst().orElse(null);
    }

    @Override public void setProperty(String propertyName, Property property) {
        removeProperty(propertyName);
        if (property != null) {
            if (!propertyName.equals(property.getName())) {
                throw new IllegalArgumentException("property name does not match key: " + propertyName);
            }
            super.setProperty(new com.destroystokyo.paper.profile.ProfileProperty(
                    property.getName(), property.getValue(), property.getSignature()));
        }
    }

    @Override public GameProfile buildGameProfile() { return gameProfile(); }

    /** Validates the profile shape accepted by skull and profile serialization. */
    public static GameProfile validateSkullProfile(GameProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null");
        if (profile.getName() == null && !profile.getProperties().containsKey("textures")) {
            throw new IllegalArgumentException("The skull profile is missing a name or textures!");
        }
        return profile;
    }

    public static Property getProperty(GameProfile profile, String propertyName) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null");
        return profile.getProperties().get(propertyName).stream().findFirst().orElse(null);
    }

    @Override public CraftPlayerProfile clone() { return new CraftPlayerProfile(gameProfile()); }

    public static CraftPlayerProfile deserialize(Map<String, Object> values) {
        Object idValue = values.get("uniqueId");
        UUID id = idValue == null ? null : UUID.fromString(String.valueOf(idValue));
        String name = values.get("name") == null ? null : String.valueOf(values.get("name"));
        CraftPlayerProfile result = new CraftPlayerProfile(id, name);
        Object properties = values.get("properties");
        if (properties instanceof Iterable<?> entries) {
            for (Object entry : entries) {
                if (!(entry instanceof Map<?, ?> property)) throw new IllegalArgumentException("profile property must be a map");
                String propertyName = String.valueOf(property.get("name"));
                String value = String.valueOf(property.get("value"));
                Object signature = property.get("signature");
                result.setProperty(new com.destroystokyo.paper.profile.ProfileProperty(propertyName, value,
                        signature == null ? null : String.valueOf(signature)));
            }
        }
        return result;
    }
}
