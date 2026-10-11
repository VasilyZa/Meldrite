package org.bukkit.craftbukkit.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import java.net.URL;
import java.util.Objects;
import org.bukkit.profile.PlayerTextures;

/** Live texture view over the owning Paper profile. */
public final class CraftPlayerTextures implements PlayerTextures {
    private final PlayerProfile profile;
    public CraftPlayerTextures(PlayerProfile profile) { this.profile = Objects.requireNonNull(profile, "profile"); }
    @Override public boolean isEmpty() { return getSkin() == null && getCape() == null; }
    @Override public void clear() { profile.getProperties().removeIf(value -> "textures".equals(value.getName())); }
    @Override public URL getSkin() { return profile.getTextures().getSkin(); }
    @Override public void setSkin(URL value) { profile.getTextures().setSkin(value); }
    @Override public void setSkin(URL value, SkinModel model) { profile.getTextures().setSkin(value, model); }
    @Override public SkinModel getSkinModel() { return profile.getTextures().getSkinModel(); }
    @Override public URL getCape() { return profile.getTextures().getCape(); }
    @Override public void setCape(URL value) { profile.getTextures().setCape(value); }
    @Override public long getTimestamp() { return profile.getTextures().getTimestamp(); }
    @Override public boolean isSigned() { return profile.getTextures().isSigned(); }
}
