package com.destroystokyo.paper.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.meldrite.paper.facade.PlayerProfileAdapter;
import java.util.UUID;
import org.bukkit.profile.PlayerProfile;

/** Paper's Authlib conversion facade backed by the shared profile adapter. */
public class CraftPlayerProfile extends org.bukkit.craftbukkit.profile.CraftPlayerProfile {
    public CraftPlayerProfile(UUID id, String name) { super(id, name); }
    public CraftPlayerProfile(GameProfile profile) { super(profile); }
    private CraftPlayerProfile(GameProfile profile, boolean mirror) { super(profile, mirror); }

    public GameProfile getGameProfile() { return buildGameProfile(); }

    public static PlayerProfile asBukkitCopy(GameProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null");
        return new CraftPlayerProfile(profile);
    }

    public static PlayerProfile asBukkitMirror(GameProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null");
        return new CraftPlayerProfile(profile, false);
    }

    public static Property asAuthlib(ProfileProperty property) {
        if (property == null) throw new IllegalArgumentException("property cannot be null");
        return new Property(property.getName(), property.getValue(), property.getSignature());
    }

    public static GameProfile asAuthlibCopy(PlayerProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null");
        return PlayerProfileAdapter.from(profile);
    }

    public static GameProfile asAuthlib(PlayerProfile profile) {
        if (profile == null) throw new IllegalArgumentException("profile cannot be null");
        if (profile instanceof CraftPlayerProfile craft) return craft.getGameProfile();
        if (profile instanceof org.bukkit.craftbukkit.profile.CraftPlayerProfile craft) return craft.gameProfile();
        return PlayerProfileAdapter.from(profile);
    }

    @Override public CraftPlayerProfile clone() { return new CraftPlayerProfile(gameProfile()); }
}
