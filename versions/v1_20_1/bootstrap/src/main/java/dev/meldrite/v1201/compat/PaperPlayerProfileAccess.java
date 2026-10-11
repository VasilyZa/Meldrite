package dev.meldrite.v1201.compat;

import com.mojang.authlib.GameProfile;

/** Replaces session name/properties while retaining the native entity identity. */
public interface PaperPlayerProfileAccess {
    void meldrite$setGameProfile(GameProfile profile);
}
