package org.bukkit.craftbukkit.v1_20_R1;

import dev.meldrite.paper.bootstrap.ProfileBootstrap;
import dev.meldrite.paper.facade.FacadeContext;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.PlayerManager;
import net.minecraft.inventory.SimpleInventory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.PluginManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.NamespacedKey;
import org.bukkit.help.HelpMap;
import org.bukkit.craftbukkit.help.SimpleHelpMap;

/** CraftBukkit's native server ABI and the profile's Bukkit services share one server. */
public class CraftServer extends org.bukkit.craftbukkit.CraftServerBase {
    private final MinecraftDedicatedServer console;
    // Fabric creates this facade before the native player manager. The native
    // setPlayerManager hook publishes the actual manager once setup reaches it.
    protected volatile DedicatedPlayerManager playerList;
    /** CraftBukkit's reflective command ABI owns the same map used by the profile. */
    private final org.bukkit.craftbukkit.command.CraftCommandMap commandMap = new org.bukkit.craftbukkit.command.CraftCommandMap(this);
    private final java.util.Map<java.util.UUID, CraftOfflinePlayer> offlinePlayers = new java.util.HashMap<>();
    private final SimpleHelpMap helpMap;
    private final org.bukkit.craftbukkit.command.BukkitCommandTree bukkitCommandTree = new org.bukkit.craftbukkit.command.BukkitCommandTree();
    /** Leaf/Paper-compatible manager exposed to Paper plugins and reflective tooling. */
    public final io.papermc.paper.plugin.manager.PaperPluginManagerImpl paperPluginManager;
    public CraftServer(FacadeContext context, ProfileBootstrap runtime, MinecraftDedicatedServer console) {
        super(context, runtime);
        this.console = java.util.Objects.requireNonNull(console);
        meldrite$bindPlayerList(console.getPlayerManager());
        this.paperPluginManager = new io.papermc.paper.plugin.manager.PaperPluginManagerImpl(
                this, commandMap, super.getPluginManager());
        this.helpMap = new SimpleHelpMap(this);
    }
    public MinecraftDedicatedServer getServer() { return console; }
    @Override public HelpMap getHelpMap() { return helpMap; }
    public void refreshHelpMap() { helpMap.initializeGeneralTopics(); helpMap.initializeCommands(); }
    @Override public CraftWorldBorder createWorldBorder() { return new CraftWorldBorder(new net.minecraft.world.border.WorldBorder()); }
    @Override public org.bukkit.advancement.Advancement getAdvancement(NamespacedKey key) {
        var advancement = console.getAdvancementLoader().get(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(java.util.Objects.requireNonNull(key)));
        return advancement == null ? null : new org.bukkit.craftbukkit.v1_20_R1.advancement.CraftAdvancement(advancement);
    }
    @Override public org.bukkit.loot.LootTable getLootTable(NamespacedKey key) {
        java.util.Objects.requireNonNull(key, "key");
        var identifier = org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(key);
        var table = console.getLootManager().getElement(new net.minecraft.loot.LootDataKey<>(
                net.minecraft.loot.LootDataType.LOOT_TABLES, identifier));
        return table == null ? null : new org.bukkit.craftbukkit.v1_20_R1.CraftLootTable(key, table);
    }
    @Override public java.util.Iterator<org.bukkit.advancement.Advancement> advancementIterator() {
        return console.getAdvancementLoader().getAdvancements().stream().map(advancement -> (org.bukkit.advancement.Advancement) new org.bukkit.craftbukkit.v1_20_R1.advancement.CraftAdvancement(advancement)).toList().iterator();
    }
    @Override public org.bukkit.packs.DataPackManager getDataPackManager() { return new org.bukkit.craftbukkit.v1_20_R1.packs.CraftDataPackManager(console.getDataPackManager()); }
    @Override public org.bukkit.potion.PotionBrewer getPotionBrewer() { return new org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionBrewer(); }
    @Override public org.bukkit.structure.StructureManager getStructureManager() { return new org.bukkit.craftbukkit.v1_20_R1.structure.CraftStructureManager(console.getStructureTemplateManager()); }
    @Override public org.bukkit.boss.BossBar createBossBar(String title, org.bukkit.boss.BarColor color, org.bukkit.boss.BarStyle style, org.bukkit.boss.BarFlag... flags) {
        if (!console.isOnThread()) throw new IllegalStateException("ASYNC_BOSS_BAR_WRITE");
        return new org.bukkit.craftbukkit.v1_20_R1.boss.CraftBossBar(title, color, style, flags);
    }
    private net.minecraft.util.Identifier bossBarId(NamespacedKey key) {
        java.util.Objects.requireNonNull(key, "key");
        return new net.minecraft.util.Identifier(key.getNamespace(), key.getKey());
    }
    @Override public org.bukkit.boss.KeyedBossBar createBossBar(NamespacedKey key, String title, org.bukkit.boss.BarColor color, org.bukkit.boss.BarStyle style, org.bukkit.boss.BarFlag... flags) {
        if (!console.isOnThread()) throw new IllegalStateException("ASYNC_BOSS_BAR_WRITE");
        var id = bossBarId(key);
        java.util.Objects.requireNonNull(color, "color"); java.util.Objects.requireNonNull(style, "style"); java.util.Objects.requireNonNull(flags, "flags");
        var bar = new org.bukkit.craftbukkit.v1_20_R1.boss.CraftKeyedBossbar(console.getBossBarManager().add(id, org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage.fromString(title, true)[0]));
        bar.setColor(color); bar.setStyle(style);
        for (var flag : flags) if (flag != null) bar.addFlag(flag);
        return bar;
    }
    @Override public org.bukkit.boss.KeyedBossBar getBossBar(NamespacedKey key) {
        var bar = console.getBossBarManager().get(bossBarId(key));
        return bar == null ? null : new org.bukkit.craftbukkit.v1_20_R1.boss.CraftKeyedBossbar(bar);
    }
    @Override public java.util.Iterator<org.bukkit.boss.KeyedBossBar> getBossBars() {
        return console.getBossBarManager().getAll().stream().map(bar -> (org.bukkit.boss.KeyedBossBar) new org.bukkit.craftbukkit.v1_20_R1.boss.CraftKeyedBossbar(bar)).toList().iterator();
    }
    @Override public boolean removeBossBar(NamespacedKey key) {
        if (!console.isOnThread()) throw new IllegalStateException("ASYNC_BOSS_BAR_WRITE");
        var bar = console.getBossBarManager().get(bossBarId(key));
        if (bar == null) return false;
        bar.clearPlayers(); console.getBossBarManager().remove(bar); return true;
    }
    /** Mirrors the native setter, including the real uninitialized/cleared state. */
    public void meldrite$bindPlayerList(PlayerManager manager) {
        if (manager != console.getPlayerManager()) throw new IllegalStateException("CRAFT_SERVER_PLAYER_MANAGER_MISMATCH");
        if (manager != null && !(manager instanceof DedicatedPlayerManager)) {
            throw new IllegalStateException("CRAFT_SERVER_REQUIRES_DEDICATED_PLAYER_MANAGER");
        }
        playerList = (DedicatedPlayerManager) manager;
    }
    /** Null only while native setup has not installed a player manager, or after its explicit removal. */
    public DedicatedPlayerManager getHandle() {
        DedicatedPlayerManager current = playerList;
        if (current != console.getPlayerManager()) throw new IllegalStateException("CRAFT_SERVER_PLAYER_MANAGER_MISMATCH");
        return current;
    }
    @Override public org.bukkit.entity.Player getPlayer(java.util.UUID uuid) {
        java.util.Objects.requireNonNull(uuid, "uuid");
        var manager = getHandle(); if (manager == null) return null;
        var player = manager.getPlayer(uuid);
        return player == null ? null : (org.bukkit.entity.Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player);
    }
    @Override public org.bukkit.entity.Player getPlayerExact(String name) {
        java.util.Objects.requireNonNull(name, "name");
        var manager = getHandle(); if (manager == null) return null;
        for (var player : manager.getPlayerList()) if (player.getGameProfile().getName().equalsIgnoreCase(name)) return (org.bukkit.entity.Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player);
        return null;
    }
    @Override public org.bukkit.entity.Player getPlayer(String name) {
        java.util.Objects.requireNonNull(name, "name");
        var exact = getPlayerExact(name); if (exact != null) return exact;
        org.bukkit.entity.Player closest = null; int delta = Integer.MAX_VALUE;
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        for (var player : getOnlinePlayers()) {
            String candidate = player.getName();
            if (candidate.toLowerCase(java.util.Locale.ROOT).startsWith(lower) && candidate.length() - name.length() < delta) { closest = player; delta = candidate.length() - name.length(); }
        }
        return closest;
    }
    @Override public java.util.Collection<? extends org.bukkit.entity.Player> getOnlinePlayers() {
        var manager = getHandle();
        return manager == null ? java.util.List.of() : manager.getPlayerList().stream().map(player -> (org.bukkit.entity.Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(player)).toList();
    }
    @Override public org.bukkit.OfflinePlayer getOfflinePlayer(java.util.UUID uuid) {
        java.util.Objects.requireNonNull(uuid, "uuid");
        var online = getPlayer(uuid); if (online != null) { synchronized (offlinePlayers) { offlinePlayers.remove(uuid); } return online; }
        synchronized (offlinePlayers) { return offlinePlayers.computeIfAbsent(uuid, id -> new CraftOfflinePlayer(this, console.getUserCache().getByUuid(id).orElseGet(() -> new com.mojang.authlib.GameProfile(id, null)))); }
    }
    @Override public org.bukkit.OfflinePlayer getOfflinePlayer(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Player name required");
        var online = getPlayerExact(name); if (online != null) return online;
        var profile = getOnlineMode() ? console.getUserCache().findByName(name).orElse(null) : null;
        if (profile == null) profile = new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(java.nio.charset.StandardCharsets.UTF_8)), name);
        return getOfflinePlayer(profile);
    }
    public org.bukkit.OfflinePlayer getOfflinePlayer(com.mojang.authlib.GameProfile profile) {
        synchronized (offlinePlayers) { var result = new CraftOfflinePlayer(this, profile); offlinePlayers.put(profile.getId(), result); return result; }
    }
    @Override public org.bukkit.OfflinePlayer getOfflinePlayerIfCached(String name) {
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("Player name required");
        var online = getPlayerExact(name); if (online != null) return online;
        var entry = ((dev.meldrite.v1201.hook.UserCachePaperAccessMixin) console.getUserCache()).meldrite$profilesByName().get(name.toLowerCase(java.util.Locale.ROOT));
        if (entry == null) return null;
        var profile = ((dev.meldrite.v1201.hook.UserCacheEntryPaperAccessMixin) entry).meldrite$profile();
        return getOfflinePlayer(profile);
    }
    @Override public org.bukkit.OfflinePlayer[] getOfflinePlayers() {
        var result = new java.util.LinkedHashMap<java.util.UUID, org.bukkit.OfflinePlayer>();
        var folder = console.getSavePath(net.minecraft.util.WorldSavePath.PLAYERDATA);
        if (java.nio.file.Files.isDirectory(folder)) try (var files = java.nio.file.Files.list(folder)) {
            for (var path : files.filter(file -> file.getFileName().toString().endsWith(".dat")).toList()) {
                var name = path.getFileName().toString();
                try { var uuid = java.util.UUID.fromString(name.substring(0, name.length() - 4)); result.put(uuid, getOfflinePlayer(uuid)); }
                catch (IllegalArgumentException ignored) { /* Non-player files are not UUID records. */ }
            }
        } catch (java.io.IOException failure) { throw new IllegalStateException("Cannot enumerate player data", failure); }
        for (var player : getOnlinePlayers()) result.put(player.getUniqueId(), player);
        return result.values().toArray(org.bukkit.OfflinePlayer[]::new);
    }
    private org.bukkit.ban.IpBanList ipBanList() {
        return new org.bukkit.craftbukkit.v1_20_R1.ban.CraftIpBanList(getHandle().getIpBanList());
    }
    private org.bukkit.ban.ProfileBanList profileBanList() {
        return new org.bukkit.craftbukkit.v1_20_R1.ban.CraftProfileBanList(this, getHandle().getUserBanList());
    }
    @Override @SuppressWarnings("unchecked") public <T extends org.bukkit.BanList<?>> T getBanList(org.bukkit.BanList.Type type) {
        java.util.Objects.requireNonNull(type, "type");
        return (T) (type == org.bukkit.BanList.Type.IP ? ipBanList() : profileBanList());
    }
    @Override public java.util.Set<org.bukkit.OfflinePlayer> getBannedPlayers() {
        java.util.Set<org.bukkit.OfflinePlayer> result = new java.util.LinkedHashSet<>();
        for (String name : getHandle().getUserBanList().getNames()) result.add(getOfflinePlayer(name));
        return result;
    }
    @Override public java.util.Set<String> getIPBans() {
        return new java.util.LinkedHashSet<>(java.util.Arrays.asList(getHandle().getIpBanList().getNames()));
    }
    @Override public void banIP(String address) {
        java.util.Objects.requireNonNull(address, "address");
        requireServerThread();
        getHandle().getIpBanList().add(new net.minecraft.server.BannedIpEntry(address));
    }
    @Override public void banIP(java.net.InetAddress address) {
        java.util.Objects.requireNonNull(address, "address");
        banIP(com.google.common.net.InetAddresses.toAddrString(address));
    }
    @Override public void unbanIP(String address) {
        java.util.Objects.requireNonNull(address, "address");
        requireServerThread();
        getHandle().getIpBanList().remove(address);
    }
    @Override public void unbanIP(java.net.InetAddress address) {
        java.util.Objects.requireNonNull(address, "address");
        unbanIP(com.google.common.net.InetAddresses.toAddrString(address));
    }
    @Override public java.util.Set<org.bukkit.OfflinePlayer> getOperators() {
        java.util.Set<org.bukkit.OfflinePlayer> result = new java.util.LinkedHashSet<>();
        for (String name : getHandle().getOpNames()) result.add(getOfflinePlayer(name));
        return result;
    }
    @Override public java.util.Set<org.bukkit.OfflinePlayer> getWhitelistedPlayers() {
        java.util.Set<org.bukkit.OfflinePlayer> result = new java.util.LinkedHashSet<>();
        for (String name : getHandle().getWhitelistedNames()) result.add(getOfflinePlayer(name));
        return result;
    }
    @Override public void reloadWhitelist() { getHandle().reloadWhitelist(); }
    @Override public java.util.UUID getPlayerUniqueId(String name) {
        if (name == null || name.isBlank()) return null;
        var online = getPlayerExact(name);
        if (online != null) return online.getUniqueId();
        var cached = getOfflinePlayerIfCached(name);
        return cached == null ? getOfflinePlayer(name).getUniqueId() : cached.getUniqueId();
    }
    @Override public java.util.List<org.bukkit.entity.Player> matchPlayer(String name) {
        if (name == null) return java.util.List.of();
        String lower = name.toLowerCase(java.util.Locale.ROOT);
        return getOnlinePlayers().stream().filter(player -> player.getName().toLowerCase(java.util.Locale.ROOT).startsWith(lower)).map(player -> (org.bukkit.entity.Player) player).toList();
    }
    private void requireServerThread() {
        if (!console.isOnThread()) throw new IllegalStateException("ASYNC_SERVER_WRITE");
    }
    private static org.bukkit.GameMode gameMode(net.minecraft.world.GameMode mode) {
        return mode == null ? null : org.bukkit.GameMode.getByValue(mode.getId());
    }
    @Override public org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboardManager getScoreboardManager() {
        return dev.meldrite.v1201.compat.NativeScoreboardBinding.current(console);
    }
    @Override public org.bukkit.entity.Entity getEntity(java.util.UUID uuid) {
        java.util.Objects.requireNonNull(uuid, "uuid");
        for (var world : console.getWorlds()) {
            var entity = world.getEntity(uuid);
            if (entity != null) return dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity);
        }
        return null;
    }
    @Override public java.util.List<org.bukkit.entity.Entity> selectEntities(org.bukkit.command.CommandSender sender, String selector) {
        if (sender == null || selector == null) throw new IllegalArgumentException("Sender and selector are required");
        var reader = new com.mojang.brigadier.StringReader(selector);
        try {
            var parsed = new net.minecraft.command.EntitySelectorReader(reader).read();
            if (reader.canRead()) throw new IllegalArgumentException("Spurious trailing data in selector: " + selector);
            var source = sender instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity entity
                    ? entity.getHandle().getCommandSource()
                    : org.bukkit.craftbukkit.command.VanillaCommandWrapper.getListener(sender);
            // Bukkit's selector API accepts selectors independently of command permission levels.
            return parsed.getEntities(source.withLevel(4)).stream().map(dev.meldrite.v1201.compat.NativeEntityExtensions::bukkit)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) {
            throw new IllegalArgumentException("Could not parse selector: " + selector, failure);
        }
    }
    @Override public double[] getTPS() {
        long[] samples = console.lastTickLengths;
        long total = 0L; int count = 0;
        for (long sample : samples) if (sample > 0L) { total += sample; count++; }
        if (count == 0) return new double[]{0.0, 0.0, 0.0};
        double millis = (total / (double) count) / 1_000_000.0;
        double tps = millis <= 0.0 ? 0.0 : Math.min(20.0, 1000.0 / millis);
        return new double[]{tps, tps, tps};
    }
    @Override public long[] getTickTimes() { return java.util.Arrays.copyOf(console.lastTickLengths, console.lastTickLengths.length); }
    @Override public void savePlayers() {
        requireServerThread();
        getHandle().saveAllPlayerData();
    }
    @Override public java.io.File getWorldContainer() { return console.getRunDirectory(); }
    @Override public java.util.List<org.bukkit.World> getWorlds() { return facadeContext().worlds(); }
    @Override public org.bukkit.World getWorld(String name) { return name == null ? null : facadeContext().worldByName(name); }
    @Override public org.bukkit.World getWorld(java.util.UUID uid) { return uid == null ? null : facadeContext().worldByUid(uid); }
    @Override public org.bukkit.World getWorld(org.bukkit.NamespacedKey key) { return key == null ? null : facadeContext().worldByKey(key); }
    @Override public org.bukkit.UnsafeValues getUnsafe() { return org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.INSTANCE; }
    @Override public <T extends org.bukkit.Keyed> org.bukkit.Tag<T> getTag(String registry, org.bukkit.NamespacedKey key, Class<T> type) {
        return dev.meldrite.v1201.compat.NativeTags.get(registry, key, type);
    }
    @Override public <T extends org.bukkit.Keyed> Iterable<org.bukkit.Tag<T>> getTags(String registry, Class<T> type) {
        return dev.meldrite.v1201.compat.NativeTags.all(registry, type);
    }
    @Override public int getMaxPlayers() { return getHandle().getMaxPlayerCount(); }
    @Override public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers < 0) throw new IllegalArgumentException("maxPlayers must be >= 0");
        getHandle().maxPlayers = maxPlayers;
    }
    @Override public int getPort() { return console.getServerPort(); }
    @Override public String getIp() { return console.getServerIp(); }
    private dev.meldrite.paper.configuration.ServerConfiguration configuration() {
        return dev.meldrite.paper.configuration.ServerConfiguration.current();
    }
    @Override public String getUpdateFolder() {
        return configuration().spigot().getString("settings.update-folder", "update");
    }
    @Override public java.io.File getUpdateFolderFile() {
        return new java.io.File(getPluginsFolder(), getUpdateFolder());
    }
    @Override public long getConnectionThrottle() {
        if (org.spigotmc.SpigotConfig.bungee) return -1L;
        return configuration().bukkit().getLong("settings.connection-throttle", -1L);
    }
    @Override public int getViewDistance() { return console.getProperties().viewDistance; }
    @Override public int getSimulationDistance() { return console.getProperties().simulationDistance; }
    @Override public int getMaxWorldSize() { return console.getProperties().maxWorldSize; }
    @Override public int getSpawnRadius() { return console.getSpawnProtectionRadius(); }
    private net.minecraft.server.world.ServerWorld overworld() {
        var world = console.getOverworld();
        if (world == null) throw new IllegalStateException("WORLD_NOT_READY");
        return world;
    }
    @Override public int getSpawnLimit(org.bukkit.entity.SpawnCategory category) {
        return dev.meldrite.v1201.compat.NativeWorldSettings.getSpawnLimit(overworld(), category);
    }
    @Override public int getMonsterSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.MONSTER); }
    @Override public int getAnimalSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.ANIMAL); }
    @Override public int getWaterAnimalSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_ANIMAL); }
    @Override public int getWaterAmbientSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_AMBIENT); }
    @Override public int getWaterUndergroundCreatureSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE); }
    @Override public int getAmbientSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.AMBIENT); }
    @Override public int getTicksPerSpawns(org.bukkit.entity.SpawnCategory category) {
        return Math.toIntExact(dev.meldrite.v1201.compat.NativeWorldSettings.getTicksPerSpawns(overworld(), category));
    }
    @Override public int getTicksPerAnimalSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.ANIMAL); }
    @Override public int getTicksPerMonsterSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.MONSTER); }
    @Override public int getTicksPerWaterSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_ANIMAL); }
    @Override public int getTicksPerWaterAmbientSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_AMBIENT); }
    @Override public int getTicksPerWaterUndergroundCreatureSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE); }
    @Override public int getTicksPerAmbientSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.AMBIENT); }
    @Override public String getWorldType() { return console.getProperties().generatorOptions.isLegacyCustomizedType() ? "CUSTOMIZED" : "NORMAL"; }
    @Override public java.util.List<String> getInitialEnabledPacks() { return console.getProperties().dataPackSettings.getEnabled(); }
    @Override public java.util.List<String> getInitialDisabledPacks() { return console.getProperties().dataPackSettings.getDisabled(); }
    @Override public String getResourcePack() { return console.getResourcePackProperties().map(net.minecraft.server.MinecraftServer.ServerResourcePackProperties::url).orElse(""); }
    @Override public String getResourcePackHash() { return console.getResourcePackProperties().map(net.minecraft.server.MinecraftServer.ServerResourcePackProperties::hash).orElse(""); }
    @Override public String getResourcePackPrompt() { return console.getResourcePackProperties().map(value -> value.prompt() == null ? null : value.prompt().getString()).orElse(null); }
    @Override public boolean isResourcePackRequired() { return console.getResourcePackProperties().map(net.minecraft.server.MinecraftServer.ServerResourcePackProperties::isRequired).orElse(false); }
    @Override public int getMaxChainedNeighborUpdates() { return console.getMaxChainedNeighborUpdates(); }
    @Override public boolean isEnforcingSecureProfiles() { return console.shouldEnforceSecureProfile(); }
    @Override public boolean getHideOnlinePlayers() { return console.hideOnlinePlayers(); }
    @Override public int getCurrentTick() { return console.getTicks(); }
    @Override public double getAverageTickTime() { return console.getTickTime(); }
    @Override public boolean isTickingWorlds() { return console.isRunning(); }
    @Override public void shutdown() {
        markStopping();
        // The native loop owns cleanup after the current tick has returned.
        console.stop(false);
    }
    @Override public boolean getOnlineMode() { return console.isOnlineMode(); }
    @Override public boolean getAllowEnd() { return console.getWorldRegistryKeys().contains(net.minecraft.world.World.END); }
    @Override public boolean getAllowNether() { return console.isNetherAllowed(); }
    @Override public boolean getAllowFlight() { return console.isFlightEnabled(); }
    @Override public boolean getGenerateStructures() { return console.getProperties().generatorOptions.shouldGenerateStructures(); }
    @Override public org.bukkit.GameMode getDefaultGameMode() { return gameMode(console.getDefaultGameMode()); }
    @Override public void setDefaultGameMode(org.bukkit.GameMode mode) {
        java.util.Objects.requireNonNull(mode, "mode");
        requireServerThread();
        console.setDefaultGameMode(net.minecraft.world.GameMode.byId(mode.getValue()));
    }
    @Override public boolean isHardcore() { return console.isHardcore(); }
    @Override public boolean hasWhitelist() { return getHandle().isWhitelistEnabled(); }
    @Override public void setWhitelist(boolean value) { console.setUseWhitelist(value); }
    @Override public boolean isWhitelistEnforced() { return console.isEnforceWhitelist(); }
    @Override public void setWhitelistEnforced(boolean value) { console.setEnforceWhitelist(value); }
    @Override public int getIdleTimeout() { return console.getPlayerIdleTimeout(); }
    @Override public void setIdleTimeout(int timeout) { console.setPlayerIdleTimeout(timeout); }
    @Override public String getMotd() { return console.getServerMotd(); }
    @Override public void setMotd(String motd) { console.setMotd(java.util.Objects.requireNonNull(motd)); }
    @Override public String getShutdownMessage() {
        return configuration().bukkit().getString("settings.shutdown-message");
    }
    @Override public net.kyori.adventure.text.Component shutdownMessage() {
        String message = getShutdownMessage();
        return message == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(message);
    }
    @Override public boolean shouldSendChatPreviews() { return false; }
    @Override public org.bukkit.Warning.WarningState getWarningState() {
        return org.bukkit.Warning.WarningState.value(configuration().bukkit().getString("settings.deprecated-verbose", "default"));
    }
    private String configuredPermissionMessage() {
        String value = configuration().paper().getString("messages.no-permission");
        if (value == null) value = configuration().bukkit().getString("settings.permission-message");
        if (value == null) value = "I'm sorry, but you do not have permission to perform this command.";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', value);
    }
    @Override public String getPermissionMessage() { return configuredPermissionMessage(); }
    @Override public net.kyori.adventure.text.Component permissionMessage() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(configuredPermissionMessage());
    }
    @Override public boolean suggestPlayerNamesWhenNullTabCompletions() {
        return configuration().paper().getBoolean("commands.suggest-player-names-when-null-tab-completions", false);
    }
    @Override public net.kyori.adventure.text.Component motd() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(getMotd());
    }
    @Override public void motd(net.kyori.adventure.text.Component motd) {
        setMotd(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(java.util.Objects.requireNonNull(motd)));
    }
    @Override public SimpleCommandMap getCommandMap() { return commandMap; }
    @Override public PluginManager getPluginManager() { return paperPluginManager; }
    @Override protected void clearPluginState() { helpMap.resetForReload(); }
    @Override protected void reloadServerResources() {
        dev.meldrite.v1201.compat.NativeServerProperties.reload(console);
        reloadWhitelist();
        reloadData();
    }
    @Override protected void publishReloadedPlugins() {
        refreshHelpMap();
        getConsoleSender().recalculatePermissions();
        for (var player : getOnlinePlayers()) player.recalculatePermissions();
        syncCommands();
    }
    @Override public void syncCommands() {
        requireServerThread();
        var commands = console.getCommandManager();
        bukkitCommandTree.synchronize(this, commands.getDispatcher(), getCommandMap().getKnownCommands());
        var players = console.getPlayerManager();
        if (players != null) for (var player : players.getPlayerList()) commands.sendCommandTree(player);
    }
    @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder holder, int size) {
        return createInventory(holder, size, (String) null);
    }
    @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder holder, int size, String title) {
        if (size < 9 || size > 54 || size % 9 != 0) throw new IllegalArgumentException("Inventory size must be a multiple of 9 between 9 and 54");
        return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCustom(holder, size, title);
    }
    @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder holder, int size, net.kyori.adventure.text.Component title) {
        return createInventory(holder, size, title == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title));
    }
    @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder holder, InventoryType type) {
        return createInventory(holder, type, (String) null);
    }
    @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder holder, InventoryType type, String title) {
        java.util.Objects.requireNonNull(type, "type");
        if (!type.isCreatable()) throw new IllegalArgumentException("InventoryType cannot be used to create an inventory: " + type);
        if (type == InventoryType.ANVIL) {
            return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryAnvil(holder, title);
        }
        if (type == InventoryType.WORKBENCH) {
            return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCrafting(
                    new dev.meldrite.v1201.compat.NativeCraftingInput(3, 3), new net.minecraft.inventory.CraftingResultInventory(), holder, title);
        }
        return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCustom(holder, type, title);
    }
    @Override public org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder holder, InventoryType type, net.kyori.adventure.text.Component title) {
        return createInventory(holder, type, title == null ? null : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title));
    }

    @Override public org.bukkit.inventory.Merchant createMerchant(String title) {
        return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchantCustom(title == null ? InventoryType.MERCHANT.getDefaultTitle() : title);
    }
    @Override public org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component title) {
        return new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftMerchantCustom(java.util.Objects.requireNonNull(title, "title"));
    }

    @Override public boolean addRecipe(Recipe recipe) { return addRecipe(recipe, false); }
    @Override public boolean addRecipe(Recipe recipe, boolean resendRecipes) {
        dev.meldrite.v1201.compat.NativeRecipes.requireThread();
        if (recipe == null) throw new IllegalArgumentException("recipe");
        if (recipe instanceof org.bukkit.craftbukkit.v1_20_R1.inventory.CraftRecipe craft) craft.addToCraftingManager();
        else if (recipe instanceof org.bukkit.inventory.ShapedRecipe || recipe instanceof org.bukkit.inventory.ShapelessRecipe
                || recipe instanceof org.bukkit.inventory.CookingRecipe<?> || recipe instanceof org.bukkit.inventory.StonecuttingRecipe
                || recipe instanceof org.bukkit.inventory.SmithingTransformRecipe || recipe instanceof org.bukkit.inventory.SmithingTrimRecipe) {
            var converted = dev.meldrite.v1201.compat.NativeRecipes.toMinecraft(recipe);
            dev.meldrite.v1201.compat.NativeRecipes.add(console.getRecipeManager(), converted);
        } else if (recipe instanceof org.bukkit.inventory.ComplexRecipe) throw new UnsupportedOperationException("Cannot add custom complex recipe");
        else return false;
        if (resendRecipes) dev.meldrite.v1201.compat.NativeRecipes.synchronize();
        return true;
    }
    @Override public Recipe getRecipe(NamespacedKey key) {
        if (key == null) throw new IllegalArgumentException("recipeKey");
        return console.getRecipeManager().get(org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(key))
                .map(dev.meldrite.v1201.compat.NativeRecipes::fromMinecraft).orElse(null);
    }
    @Override public java.util.List<Recipe> getRecipesFor(ItemStack result) {
        if (result == null) throw new IllegalArgumentException("ItemStack");
        var matches = new java.util.ArrayList<Recipe>();
        for (var nativeRecipe : console.getRecipeManager().values()) {
            var projected = dev.meldrite.v1201.compat.NativeRecipes.fromMinecraft(nativeRecipe);
            var candidate = projected.getResult();
            if (candidate.getType() == result.getType() && (result.getDurability() == -1 || candidate.getDurability() == result.getDurability())) matches.add(projected);
        }
        return matches;
    }
    @Override public Recipe getCraftingRecipe(ItemStack[] matrix, org.bukkit.World world) {
        if (matrix == null || matrix.length != 9 || world == null) throw new IllegalArgumentException("A 3x3 matrix and world are required");
        var input = new dev.meldrite.v1201.compat.NativeCraftingInput(3, 3);
        for (int i = 0; i < matrix.length; i++) input.setStack(i, org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(matrix[i]));
        return console.getRecipeManager().getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, input,
                ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) world).getHandle()).map(dev.meldrite.v1201.compat.NativeRecipes::fromMinecraft).orElse(null);
    }
    @Override public ItemStack craftItem(ItemStack[] matrix, org.bukkit.World world, org.bukkit.entity.Player player) {
        if (matrix == null || matrix.length != 9 || world == null || player == null) throw new IllegalArgumentException("A 3x3 matrix, world, and player are required");
        dev.meldrite.v1201.compat.NativeRecipes.requireThread();
        var nativePlayer = ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer) player).getHandle();
        var nativeWorld = ((org.bukkit.craftbukkit.v1_20_R1.CraftWorld) world).getHandle();
        var handler = new net.minecraft.screen.CraftingScreenHandler(-1, nativePlayer.getInventory());
        var access = (dev.meldrite.v1201.hook.CraftingScreenHandlerAccess) handler;
        var input = access.meldrite$getInput();
        var resultInventory = access.meldrite$getResult();
        var human = (org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity) player;
        var previewView = new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView(human,
                new org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCrafting(input, resultInventory, human),
                human.getInventory(), InventoryType.WORKBENCH, InventoryType.WORKBENCH.getDefaultTitle(), handler, false);
        ((dev.meldrite.v1201.compat.ScreenHandlerBukkitViewAccess) handler).meldrite$setBukkitView(previewView);
        for (int i = 0; i < matrix.length; i++) input.setStack(i, org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(matrix[i]));
        var nativeRecipe = console.getRecipeManager().getFirstMatch(net.minecraft.recipe.RecipeType.CRAFTING, input, nativeWorld);
        var output = net.minecraft.item.ItemStack.EMPTY;
        if (nativeRecipe.isPresent() && resultInventory.shouldCraftRecipe(nativeWorld, nativePlayer, nativeRecipe.get())) {
            var crafted = nativeRecipe.get().craft(input, console.getRegistryManager());
            if (crafted.isItemEnabled(nativeWorld.getEnabledFeatures())) output = crafted;
        }
        output = dev.meldrite.v1201.compat.InventoryEventBridge.prepareCraft(nativePlayer, handler, input, resultInventory, output, nativeWorld);
        var remainders = nativeRecipe.isPresent() ? nativeRecipe.get().getRemainder(input) : null;
        for (int i = 0; i < matrix.length; i++) {
            var remaining = remainders == null ? net.minecraft.item.ItemStack.EMPTY : remainders.get(i);
            matrix[i] = remaining.isEmpty() ? null : org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(remaining);
        }
        return org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(output);
    }
    @Override public java.util.Iterator<Recipe> recipeIterator() { return new org.bukkit.craftbukkit.v1_20_R1.inventory.RecipeIterator(console.getRecipeManager()); }
    @Override public void clearRecipes() { dev.meldrite.v1201.compat.NativeRecipes.clear(console.getRecipeManager()); }
    @Override public boolean removeRecipe(NamespacedKey key) { return removeRecipe(key, false); }
    @Override public boolean removeRecipe(NamespacedKey key, boolean resendRecipes) {
        if (key == null) throw new IllegalArgumentException("recipeKey");
        boolean removed = dev.meldrite.v1201.compat.NativeRecipes.remove(console.getRecipeManager(), org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey.toMinecraft(key));
        if (removed && resendRecipes) dev.meldrite.v1201.compat.NativeRecipes.synchronize();
        return removed;
    }
    @Override public void resetRecipes() { reloadData(); }
    @Override public void reloadData() {
        dev.meldrite.v1201.compat.NativeRecipes.requireThread();
        reloadResourcesAndWait();
    }
    @Override public void updateResources() {
        dev.meldrite.v1201.compat.NativeRecipes.requireThread();
        reloadResourcesAndWait();
    }
    private void reloadResourcesAndWait() {
        var reload = console.reloadResources(console.getDataPackManager().getEnabledNames());
        console.runTasks(reload::isDone);
        reload.join();
    }
    @Override public void updateRecipes() { dev.meldrite.v1201.compat.NativeRecipes.synchronize(); }
}
