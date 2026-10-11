package dev.meldrite.paper.facade;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.bukkit.profile.PlayerTextures;

/** A detached Paper profile that retains every authlib property and signature. */
public class PlayerProfileAdapter implements PlayerProfile {
    public interface ProfileBridge {
        boolean onlineMode();
        GameProfile completeFromCache(GameProfile profile, boolean lookupUUID, boolean onlineMode);
        GameProfile complete(GameProfile profile, boolean textures, boolean onlineMode);
        CompletableFuture<GameProfile> update(GameProfile profile);
        boolean isSigned(Property property);
    }
    private static volatile ProfileBridge bridge;
    public static void bind(ProfileBridge value) { bridge = Objects.requireNonNull(value, "bridge"); }
    public static boolean onlineMode() { return bridge().onlineMode(); }
    public static boolean isSigned(Property property) {
        return property != null && property.hasSignature() && bridge().isSigned(property);
    }
    private static ProfileBridge bridge() { ProfileBridge value = bridge; if (value == null) throw new IllegalStateException("PROFILE_BRIDGE_NOT_BOUND"); return value; }
    private GameProfile profile;
    public PlayerProfileAdapter(GameProfile value) { this(value, true); }
    protected PlayerProfileAdapter(GameProfile value, boolean detached) {
        GameProfile supplied = Objects.requireNonNull(value);
        profile = detached ? copy(supplied) : supplied;
    }
    public PlayerProfileAdapter(UUID id, String name) { this(new GameProfile(id, name)); }
    public GameProfile gameProfile() { return copy(profile); }
    public static GameProfile copy(GameProfile value) {
        if (value == null) return null;
        GameProfile result = new GameProfile(value.getId(), value.getName());
        result.getProperties().putAll(value.getProperties());
        return result;
    }
    public static GameProfile from(org.bukkit.profile.PlayerProfile value) {
        if (value instanceof PlayerProfileAdapter own) return own.gameProfile();
        GameProfile result = new GameProfile(value.getUniqueId(), value.getName());
        if (value instanceof PlayerProfile paper) {
            for (ProfileProperty property : paper.getProperties()) result.getProperties().put(property.getName(), new Property(property.getName(), property.getValue(), property.getSignature()));
        } else {
            PlayerProfileAdapter copy = new PlayerProfileAdapter(result);
            copy.setTextures(value.getTextures());
            return copy.gameProfile();
        }
        return result;
    }
    @Override public UUID getId() { return profile.getId(); }
    @Override public UUID getUniqueId() { return getId(); }
    @Override public String getName() { return profile.getName(); }
    @Override public UUID setId(UUID id) { UUID old = getId(); GameProfile value = new GameProfile(id, getName()); value.getProperties().putAll(profile.getProperties()); profile = value; return old; }
    @Override public String setName(String name) { String old = getName(); GameProfile value = new GameProfile(getId(), name); value.getProperties().putAll(profile.getProperties()); profile = value; return old; }
    @Override public PlayerTextures getTextures() { return new Textures(); }
    @Override public void setTextures(PlayerTextures value) {
        if (value == null) { removeProperty("textures"); return; }
        if (value instanceof PlayerProfileAdapter.Textures own) {
            Property property = own.property();
            removeProperty("textures");
            if (property != null) profile.getProperties().put("textures", property);
        } else {
            Textures textures = new Textures();
            textures.replace(value.getSkin(), value.getSkinModel(), value.getCape());
        }
    }
    @Override public Set<ProfileProperty> getProperties() {
        return new AbstractSet<>() {
            @Override public Iterator<ProfileProperty> iterator() {
                Iterator<Property> iterator = profile.getProperties().values().iterator();
                return new Iterator<>() {
                    @Override public boolean hasNext() { return iterator.hasNext(); }
                    @Override public ProfileProperty next() { Property value = iterator.next(); return new ProfileProperty(value.getName(), value.getValue(), value.getSignature()); }
                    @Override public void remove() { iterator.remove(); }
                };
            }
            @Override public int size() { return profile.getProperties().size(); }
            @Override public boolean add(ProfileProperty value) { boolean present = contains(value); setProperty(value); return !present; }
            @Override public void clear() { clearProperties(); }
        };
    }
    @Override public boolean hasProperty(String name) { return profile.getProperties().containsKey(name); }
    @Override public void setProperty(ProfileProperty value) { Objects.requireNonNull(value); profile.getProperties().removeAll(value.getName()); profile.getProperties().put(value.getName(), new Property(value.getName(), value.getValue(), value.getSignature())); }
    @Override public void setProperties(Collection<ProfileProperty> values) { values.forEach(this::setProperty); }
    @Override public boolean removeProperty(String name) { return !profile.getProperties().removeAll(name).isEmpty(); }
    @Override public void clearProperties() { profile.getProperties().clear(); }
    @Override public boolean isComplete() { return profile.isComplete(); }
    @Override public boolean completeFromCache() { return completeFromCache(false, bridge().onlineMode()); }
    @Override public boolean completeFromCache(boolean onlineMode) { return completeFromCache(false, onlineMode); }
    @Override public boolean completeFromCache(boolean lookupUUID, boolean onlineMode) { profile = copy(Objects.requireNonNull(bridge().completeFromCache(gameProfile(), lookupUUID, onlineMode), "resolved profile")); return profile.isComplete(); }
    @Override public boolean complete(boolean textures) { return complete(textures, bridge().onlineMode()); }
    @Override public boolean complete(boolean textures, boolean onlineMode) { profile = copy(Objects.requireNonNull(bridge().complete(gameProfile(), textures, onlineMode), "resolved profile")); return profile.isComplete() && (!onlineMode || !textures || hasTextures()); }
    @Override public CompletableFuture<PlayerProfile> update() {
        try { return Objects.requireNonNull(bridge().update(gameProfile()), "update future").thenApply(PlayerProfileAdapter::new); }
        catch (RuntimeException failure) { return CompletableFuture.failedFuture(failure); }
    }
    @Override public PlayerProfileAdapter clone() { return new PlayerProfileAdapter(profile); }
    @Override public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (getId() != null) result.put("uniqueId", getId().toString());
        if (getName() != null) result.put("name", getName());
        if (!profile.getProperties().isEmpty()) {
            List<Map<String, String>> properties = new ArrayList<>();
            for (Property property : profile.getProperties().values()) {
                Map<String, String> value = new LinkedHashMap<>(); value.put("name", property.getName()); value.put("value", property.getValue());
                if (property.hasSignature()) value.put("signature", property.getSignature()); properties.add(value);
            }
            result.put("properties", properties);
        }
        return result;
    }
    @Override public boolean equals(Object value) { return value instanceof PlayerProfileAdapter other && serialize().equals(other.serialize()); }
    @Override public int hashCode() { return serialize().hashCode(); }

    private final class Textures implements PlayerTextures {
        private Property property() { return profile.getProperties().get("textures").stream().findFirst().orElse(null); }
        private JsonObject data() {
            Property property = property();
            if (property == null) return new JsonObject();
            try { return JsonParser.parseString(new String(Base64.getDecoder().decode(property.getValue()), StandardCharsets.UTF_8)).getAsJsonObject(); }
            catch (IllegalArgumentException | IllegalStateException | com.google.gson.JsonParseException failure) { return new JsonObject(); }
        }
        private JsonObject texture(String name) { JsonObject textures = data().getAsJsonObject("textures"); return textures == null ? null : textures.getAsJsonObject(name); }
        private URL url(String name) {
            JsonObject texture = texture(name);
            if (texture == null || !texture.has("url")) return null;
            try { return new URL(texture.get("url").getAsString()); } catch (MalformedURLException | IllegalStateException failure) { return null; }
        }
        @Override public boolean isEmpty() { return getSkin() == null && getCape() == null; }
        @Override public void clear() { removeProperty("textures"); }
        @Override public URL getSkin() { return url("SKIN"); }
        @Override public void setSkin(URL value) { setSkin(value, SkinModel.CLASSIC); }
        @Override public void setSkin(URL value, SkinModel model) { replace(value, model, getCape()); }
        @Override public SkinModel getSkinModel() {
            JsonObject skin = texture("SKIN");
            if (skin != null && skin.has("metadata")) {
                JsonObject metadata = skin.getAsJsonObject("metadata");
                if (metadata.has("model") && "slim".equalsIgnoreCase(metadata.get("model").getAsString())) return SkinModel.SLIM;
            }
            return SkinModel.CLASSIC;
        }
        @Override public URL getCape() { return url("CAPE"); }
        @Override public void setCape(URL value) { replace(getSkin(), getSkinModel(), value); }
        @Override public long getTimestamp() { JsonObject data = data(); if (!data.has("timestamp")) return 0; try { return data.get("timestamp").getAsLong(); } catch (IllegalStateException | NumberFormatException failure) { return 0; } }
        @Override public boolean isSigned() { Property value = property(); return value != null && value.hasSignature() && bridge().isSigned(value); }
        private void replace(URL skin, SkinModel model, URL cape) {
            validate(skin); validate(cape);
            if (skin == null && cape == null) { clear(); return; }
            JsonObject data = new JsonObject(); JsonObject textures = new JsonObject(); data.add("textures", textures);
            if (skin != null) { JsonObject value = new JsonObject(); value.addProperty("url", skin.toString()); if (model == SkinModel.SLIM) { JsonObject metadata = new JsonObject(); metadata.addProperty("model", "slim"); value.add("metadata", metadata); } textures.add("SKIN", value); }
            if (cape != null) { JsonObject value = new JsonObject(); value.addProperty("url", cape.toString()); textures.add("CAPE", value); }
            setProperty(new ProfileProperty("textures", Base64.getEncoder().encodeToString(data.toString().getBytes(StandardCharsets.UTF_8))));
        }
        private void validate(URL value) { if (value != null && (!"textures.minecraft.net".equals(value.getHost()) || !value.getPath().startsWith("/texture/"))) throw new IllegalArgumentException("INVALID_MINECRAFT_TEXTURE_URL: " + value); }
    }
}
