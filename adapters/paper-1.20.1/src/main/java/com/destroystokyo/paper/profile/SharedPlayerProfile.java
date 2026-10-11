package com.destroystokyo.paper.profile;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;

/**
 * Paper's internal bridge used by CraftBukkit skull and profile code.
 * Implementations expose the Authlib snapshot without sharing mutable state.
 */
public interface SharedPlayerProfile {
    UUID getUniqueId();
    String getName();
    boolean removeProperty(String property);
    Property getProperty(String propertyName);
    void setProperty(String propertyName, Property property);
    GameProfile buildGameProfile();
}
