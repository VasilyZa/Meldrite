package dev.meldrite.v1201.compat;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import dev.meldrite.paper.facade.PlayerProfileAdapter;
import dev.meldrite.v1201.hook.MinecraftServerPaperAccessMixin;
import dev.meldrite.v1201.hook.UserCachePaperAccessMixin;
import dev.meldrite.v1201.hook.UserCacheEntryPaperAccessMixin;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;

/** Resolves Paper profiles against the server's actual cache and authentication services. */
public final class NativeProfileBridge implements PlayerProfileAdapter.ProfileBridge {
    private final UserCache cache;
    private final MinecraftSessionService sessions;
    private final ServicesKeySet keys;
    private final BooleanSupplier onlineMode;
    private final Executor executor;
    private final Function<String, GameProfile> cachedByName;

    public NativeProfileBridge(MinecraftServer server) {
        this(server.getUserCache(), server.getSessionService(),
                ((MinecraftServerPaperAccessMixin) server).meldrite$apiServices().servicesKeySet(),
                server::isOnlineMode, Util.getIoWorkerExecutor(), name -> cachedByName(server.getUserCache(), name));
    }

    NativeProfileBridge(UserCache cache, MinecraftSessionService sessions, ServicesKeySet keys,
                        BooleanSupplier onlineMode, Executor executor, Function<String, GameProfile> cachedByName) {
        this.cache = Objects.requireNonNull(cache); this.sessions = Objects.requireNonNull(sessions);
        this.keys = Objects.requireNonNull(keys); this.onlineMode = Objects.requireNonNull(onlineMode);
        this.executor = Objects.requireNonNull(executor); this.cachedByName = Objects.requireNonNull(cachedByName);
    }

    private static GameProfile cachedByName(UserCache cache, String name) {
        UserCachePaperAccessMixin access = (UserCachePaperAccessMixin) cache;
        Object value = access.meldrite$profilesByName().get(name.toLowerCase(Locale.ROOT));
        if (value == null) return null;
        UserCacheEntryPaperAccessMixin entry = (UserCacheEntryPaperAccessMixin) value;
        entry.meldrite$lastAccessed(access.meldrite$nextProfileAccess());
        return PlayerProfileAdapter.copy(entry.meldrite$profile());
    }

    @Override public boolean onlineMode() { return onlineMode.getAsBoolean(); }
    @Override public GameProfile completeFromCache(GameProfile value, boolean lookupUUID, boolean onlineMode) {
        GameProfile source = PlayerProfileAdapter.copy(Objects.requireNonNull(value, "profile"));
        GameProfile resolved = source;
        if (source.getId() == null) {
            if (!onlineMode) {
                String name = source.getName();
                UUID id = name == null ? null : UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                resolved = new GameProfile(id, name);
                copyProperties(source, resolved, false);
            } else if (source.getName() != null) {
                GameProfile cached = lookupUUID ? cache.findByName(source.getName()).orElse(null) : cachedByName.apply(source.getName());
                if (cached != null) {
                    resolved = PlayerProfileAdapter.copy(cached);
                    copyProperties(source, resolved, false);
                }
            }
        }
        if (resolved.getId() != null && (resolved.getName() == null || !resolved.getProperties().containsKey("textures"))) {
            GameProfile cached = cache.getByUuid(resolved.getId()).orElse(null);
            if (cached != null) {
                if (resolved.getName() == null) {
                    GameProfile replacement = PlayerProfileAdapter.copy(cached);
                    copyProperties(resolved, replacement, false);
                    resolved = replacement;
                } else {
                    copyProperties(cached, resolved, false);
                }
            }
        }
        return resolved;
    }
    @Override public GameProfile complete(GameProfile value, boolean textures, boolean onlineMode) {
        GameProfile profile = completeFromCache(value, true, onlineMode);
        if (onlineMode && (profile.getId() == null || !profile.isComplete() || textures && !profile.getProperties().containsKey("textures"))) {
            if (profile.getId() != null) {
                GameProfile result = sessions.fillProfileProperties(profile, true);
                if (result != null) {
                    GameProfile replacement = PlayerProfileAdapter.copy(result);
                    GameProfile filled = new GameProfile(profile.getId(), profile.getName() == null ? replacement.getName() : profile.getName());
                    filled.getProperties().putAll(replacement.getProperties());
                    profile = filled;
                }
                if (profile.isComplete()) cache.add(PlayerProfileAdapter.copy(profile));
            }
        }
        return profile;
    }
    @Override public CompletableFuture<GameProfile> update(GameProfile value) {
        GameProfile source = PlayerProfileAdapter.copy(Objects.requireNonNull(value, "profile"));
        return CompletableFuture.supplyAsync(() -> complete(source, true, onlineMode()), executor)
                .thenApply(PlayerProfileAdapter::copy);
    }
    @Override public boolean isSigned(Property property) {
        return property.hasSignature() && keys.keys(ServicesKeyType.PROFILE_PROPERTY).stream().anyMatch(key -> key.validateProperty(property));
    }

    private static void copyProperties(GameProfile source, GameProfile target, boolean clearTarget) {
        if (clearTarget) target.getProperties().clear();
        source.getProperties().keySet().forEach(name -> {
            target.getProperties().removeAll(name);
            target.getProperties().putAll(name, source.getProperties().get(name));
        });
    }
}
