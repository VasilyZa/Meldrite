package org.bukkit.craftbukkit.v1_20_R1;

import dev.meldrite.craft.WorldDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeWorldPort;
import dev.meldrite.v1201.compat.WorldBukkitAccess;
import net.minecraft.server.world.ServerWorld;

public class CraftWorld extends dev.meldrite.paper.facade.WorldAdapter {
    private final NativeWorldPort worlds;
    private final java.util.concurrent.ConcurrentMap<Long, CraftChunk> chunks = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile ServerWorld world;
    private volatile org.bukkit.WorldBorder worldBorder;
    public CraftWorld(WorldDelegate delegate, FacadeContext context, NativeWorldPort worlds) {
        super(delegate, context);
        this.worlds = java.util.Objects.requireNonNull(worlds);
        this.world = this.worlds.require(delegate.reference().key());
        ((WorldBukkitAccess) this.world).meldrite$setBukkitWorld(this);
    }
    public ServerWorld getHandle() {
        var reference = delegate.reference();
        if (!delegate.valid()) throw new IllegalStateException("STALE_CRAFT_WORLD");
        ServerWorld current = world;
        if (current == null) throw new IllegalStateException("STALE_CRAFT_WORLD");
        if (!NativeWorldPort.key(current).equals(reference.key())) {
            throw new IllegalStateException("UNSUPPORTED_CRAFT_WORLD_REBIND");
        }
        ServerWorld canonical;
        try {
            canonical = worlds.require(reference.key());
        } catch (IllegalStateException unloaded) {
            throw new IllegalStateException("STALE_CRAFT_WORLD", unloaded);
        }
        if (canonical != current) throw new IllegalStateException("UNSUPPORTED_CRAFT_WORLD_REBIND");
        return current;
    }
    @Override public java.io.File getWorldFolder() {
        var world = getHandle();
        return net.minecraft.world.dimension.DimensionType.getSaveDirectory(world.getRegistryKey(),
                world.getServer().getSavePath(net.minecraft.util.WorldSavePath.ROOT)).toFile();
    }
    @Override public int getPlayerCount() { return getHandle().getPlayers().size(); }
    @Override public int getViewDistance() {
        return dev.meldrite.v1201.compat.NativePlayerDistances.worldView(getHandle().getChunkManager().threadedAnvilChunkStorage);
    }
    @Override public void setViewDistance(int distance) {
        if (distance < 2 || distance > 32) throw new IllegalArgumentException("View distance must be between 2 and 32");
        requireWritable();
        var world = getHandle();
        world.getChunkManager().applyViewDistance(distance);
        var packet = new net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket(distance);
        for (var player : world.getPlayers()) if (player.networkHandler != null) player.networkHandler.sendPacket(packet);
    }
    @Override public int getSimulationDistance() {
        return ((dev.meldrite.v1201.hook.ChunkTicketManagerPaperAccessMixin) getHandle().getChunkManager().threadedAnvilChunkStorage.getTicketManager()).meldrite$simulationDistance();
    }
    @Override public void setSimulationDistance(int distance) {
        if (distance < 2 || distance > 32) throw new IllegalArgumentException("Simulation distance must be between 2 and 32");
        requireWritable();
        var world = getHandle();
        world.getChunkManager().applySimulationDistance(distance);
        var packet = new net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket(distance);
        for (var player : world.getPlayers()) if (player.networkHandler != null) player.networkHandler.sendPacket(packet);
    }
    @Override public org.bukkit.WorldBorder getWorldBorder() {
        getHandle();
        if (worldBorder == null) worldBorder = new CraftWorldBorder(this);
        return worldBorder;
    }
    @Override public org.bukkit.boss.DragonBattle getEnderDragonBattle() {
        var fight = getHandle().getEnderDragonFight();
        return fight == null ? null : new org.bukkit.craftbukkit.v1_20_R1.boss.CraftDragonBattle(fight);
    }
    @Override public boolean hasRaids() { return getHandle().getDimension().hasRaids(); }
    @Override public java.util.List<org.bukkit.Raid> getRaids() {
        return ((dev.meldrite.v1201.hook.RaidManagerPaperAccessMixin) getHandle().getRaidManager()).meldrite$raids().values().stream()
                .map(raid -> (org.bukkit.Raid) new CraftRaid(raid)).toList();
    }
    @Override public org.bukkit.Raid locateNearestRaid(org.bukkit.Location location, int radius) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (radius < 0) throw new IllegalArgumentException("radius < 0");
        var raid = getHandle().getRaidManager().getRaidAt(new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), radius * radius);
        return raid == null ? null : new CraftRaid(raid);
    }
    private CraftChunk chunk(int x, int z) {
        long key = net.minecraft.util.math.ChunkPos.toLong(x, z);
        return chunks.computeIfAbsent(key, ignored -> new CraftChunk(this, x, z));
    }
    @Override public org.bukkit.Chunk getChunkAt(int x, int z) { getHandle().getChunkManager().getWorldChunk(x, z); return chunk(x, z); }
    @Override public org.bukkit.Chunk getChunkAt(int x, int z, boolean generate) {
        var nativeChunk = getHandle().getChunkManager().getChunk(x, z, net.minecraft.world.chunk.ChunkStatus.FULL, generate);
        return nativeChunk instanceof net.minecraft.world.chunk.WorldChunk ? chunk(x, z) : null;
    }
    @Override public org.bukkit.Chunk getChunkAt(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }
    @Override public org.bukkit.Chunk getChunkAt(org.bukkit.block.Block block) {
        java.util.Objects.requireNonNull(block, "block");
        if (block.getWorld() != this) throw new IllegalArgumentException("Block is in a different world");
        return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
    }
    @Override public java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean generate, boolean urgent) {
        var manager = getHandle().getChunkManager();
        return manager.getChunkFutureSyncOnMainThread(x, z, net.minecraft.world.chunk.ChunkStatus.FULL, generate)
                .thenApply(result -> result.map(loaded -> chunk(x, z), unloaded -> null));
    }
    @Override public int getChunkCount() { return getHandle().getChunkManager().getTotalChunksLoadedCount(); }
    @Override public org.bukkit.Chunk[] getLoadedChunks() {
        var storage = getHandle().getChunkManager().threadedAnvilChunkStorage;
        return ((dev.meldrite.v1201.hook.ThreadedAnvilChunkStoragePaperAccessMixin) storage).meldrite$getChunkHolders().values().stream()
                .map(net.minecraft.server.world.ChunkHolder::getWorldChunk).filter(java.util.Objects::nonNull)
                .map(nativeChunk -> chunk(nativeChunk.getPos().x, nativeChunk.getPos().z)).toArray(org.bukkit.Chunk[]::new);
    }
    @Override public org.bukkit.ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
        getHandle();
        return CraftChunkSnapshot.empty(this, x, z, includeBiome, includeBiomeTempRain);
    }
    @Override public boolean isChunkGenerated(int x, int z) {
        return getHandle().getChunkManager().getChunk(x, z, net.minecraft.world.chunk.ChunkStatus.FULL, false) instanceof net.minecraft.world.chunk.WorldChunk;
    }
    @Override public boolean isChunkInUse(int x, int z) { return getHandle().isChunkLoaded(net.minecraft.util.math.ChunkPos.toLong(x, z)); }
    @Override public boolean isChunkLoaded(int x, int z) { return getHandle().getChunkManager().isChunkLoaded(x, z); }
    @Override public boolean isChunkLoaded(org.bukkit.Chunk chunk) {
        return chunk != null && chunk.getWorld() == this && isChunkLoaded(chunk.getX(), chunk.getZ());
    }
    @Override public boolean refreshChunk(int x, int z) {
        requireWritable();
        var manager = getHandle().getChunkManager();
        var chunk = manager.getWorldChunk(x, z);
        if (chunk == null) return false;
        var packet = new net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket(chunk, manager.getLightingProvider(), null, null);
        for (var player : manager.threadedAnvilChunkStorage.getPlayersWatchingChunk(chunk.getPos())) {
            if (player.networkHandler != null) player.networkHandler.sendPacket(packet);
        }
        return true;
    }
    @Override public void loadChunk(int x, int z) { loadChunk(x, z, true); }
    @Override public boolean loadChunk(int x, int z, boolean generate) {
        var nativeChunk = getHandle().getChunkManager().getChunk(x, z, net.minecraft.world.chunk.ChunkStatus.FULL, generate);
        if (!(nativeChunk instanceof net.minecraft.world.chunk.WorldChunk)) return false;
        dev.meldrite.v1201.compat.NativeChunkTickets.addLoad(getHandle(), x, z);
        return true;
    }
    @Override public void loadChunk(org.bukkit.Chunk chunk) {
        if (chunk == null || chunk.getWorld() != this) throw new IllegalArgumentException("Chunk is in a different world");
        loadChunk(chunk.getX(), chunk.getZ());
    }
    @Override public boolean unloadChunk(int x, int z) { return unloadChunk(x, z, true); }
    @Override public boolean unloadChunk(int x, int z, boolean save) {
        var nativeWorld = getHandle();
        long key = net.minecraft.util.math.ChunkPos.toLong(x, z);
        if (nativeWorld.getForcedChunks().contains(key)
                || nativeWorld.getPlayers().stream().anyMatch(player -> player.getChunkPos().x == x && player.getChunkPos().z == z)) return false;
        return dev.meldrite.v1201.compat.NativeChunkTickets.removeLoad(nativeWorld, x, z);
    }
    @Override public boolean unloadChunk(org.bukkit.Chunk chunk) {
        return chunk != null && chunk.getWorld() == this && unloadChunk(chunk.getX(), chunk.getZ());
    }
    @Override public boolean unloadChunkRequest(int x, int z) { return unloadChunk(x, z); }
    @Override public boolean isChunkForceLoaded(int x, int z) { return getHandle().getForcedChunks().contains(net.minecraft.util.math.ChunkPos.toLong(x, z)); }
    @Override public void setChunkForceLoaded(int x, int z, boolean forced) { requireWritable(); getHandle().setChunkForced(x, z, forced); }
    @Override public java.util.Collection<org.bukkit.Chunk> getForceLoadedChunks() {
        return getHandle().getForcedChunks().longStream().mapToObj(key -> (org.bukkit.Chunk) chunk(
                net.minecraft.util.math.ChunkPos.getPackedX(key), net.minecraft.util.math.ChunkPos.getPackedZ(key))).toList();
    }
    @Override public boolean addPluginChunkTicket(int x, int z, org.bukkit.plugin.Plugin plugin) { return dev.meldrite.v1201.compat.NativeChunkTickets.addPlugin(getHandle(), x, z, plugin); }
    @Override public boolean removePluginChunkTicket(int x, int z, org.bukkit.plugin.Plugin plugin) { return dev.meldrite.v1201.compat.NativeChunkTickets.removePlugin(getHandle(), x, z, plugin); }
    @Override public void removePluginChunkTickets(org.bukkit.plugin.Plugin plugin) {
        for (var entry : dev.meldrite.v1201.compat.NativeChunkTickets.allPlugins(getHandle(), this::chunk).entrySet()) {
            if (entry.getKey() == plugin) for (var chunk : entry.getValue()) dev.meldrite.v1201.compat.NativeChunkTickets.removePlugin(getHandle(), chunk.getX(), chunk.getZ(), plugin);
        }
    }
    @Override public java.util.Collection<org.bukkit.plugin.Plugin> getPluginChunkTickets(int x, int z) { return dev.meldrite.v1201.compat.NativeChunkTickets.plugins(getHandle(), x, z); }
    @Override public java.util.Map<org.bukkit.plugin.Plugin, java.util.Collection<org.bukkit.Chunk>> getPluginChunkTickets() {
        return dev.meldrite.v1201.compat.NativeChunkTickets.allPlugins(getHandle(), this::chunk);
    }
    @Override public org.bukkit.block.Block getBlockAt(int x, int y, int z) {
        getHandle();
        return context.block(new dev.meldrite.foundation.BlockPosition(delegate.reference().key(), x, y, z));
    }
    @Override public org.bukkit.block.Block getBlockAt(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location).checkFinite();
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    @Override public org.bukkit.block.BlockState getBlockState(int x, int y, int z) { return getBlockAt(x, y, z).getState(); }
    @Override public org.bukkit.block.BlockState getBlockState(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        return getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    @Override public boolean generateTree(org.bukkit.Location location, org.bukkit.TreeType type) {
        requireWritable();
        return generateTree(location, new org.bukkit.craftbukkit.v1_20_R1.util.RandomSourceWrapper.RandomWrapper(getHandle().getRandom()), type);
    }
    @Override public boolean generateTree(org.bukkit.Location location, java.util.Random random, org.bukkit.TreeType type) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        java.util.Objects.requireNonNull(random, "random"); java.util.Objects.requireNonNull(type, "type");
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        requireWritable();
        var world = getHandle();
        return generateTree(world, location, random, type);
    }
    @Override public boolean generateTree(org.bukkit.Location location, java.util.Random random, org.bukkit.TreeType type,
            org.bukkit.util.Consumer<org.bukkit.block.BlockState> consumer) {
        return generateTree(location, random, type, (java.util.function.Predicate<org.bukkit.block.BlockState>) state -> {
            if (consumer != null) consumer.accept(state);
            return true;
        });
    }
    @Override public boolean generateTree(org.bukkit.Location location, java.util.Random random, org.bukkit.TreeType type,
            java.util.function.Predicate<org.bukkit.block.BlockState> predicate) {
        var captured = captureTree(location, random, type);
        for (var state : captured.blocks().getList()) if (predicate == null || predicate.test(state)) state.update(true);
        return captured.generated();
    }
    @Override public boolean generateTree(org.bukkit.Location location, org.bukkit.TreeType type, org.bukkit.BlockChangeDelegate changes) {
        if (changes == null) throw new IllegalArgumentException("Block change delegate cannot be null");
        requireWritable();
        var captured = captureTree(location,
                new org.bukkit.craftbukkit.v1_20_R1.util.RandomSourceWrapper.RandomWrapper(getHandle().getRandom()), type);
        if (captured.generated()) for (var entry : captured.blocks().getBlocks().entrySet()) {
            var pos = entry.getKey();
            changes.setBlockData(pos.getX(), pos.getY(), pos.getZ(), org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData.fromData(entry.getValue()));
        }
        return captured.generated();
    }
    private record CapturedTree(org.bukkit.craftbukkit.v1_20_R1.util.BlockStateListPopulator blocks, boolean generated) {}
    private CapturedTree captureTree(org.bukkit.Location location, java.util.Random random, org.bukkit.TreeType type) {
        validateSpawnLocation(location);
        if (random == null || type == null) throw new IllegalArgumentException("Random and tree type cannot be null");
        requireWritable();
        var blocks = new org.bukkit.craftbukkit.v1_20_R1.util.BlockStateListPopulator(getHandle());
        return new CapturedTree(blocks, generateTree(blocks, location, random, type));
    }
    private boolean generateTree(net.minecraft.world.StructureWorldAccess access, org.bukkit.Location location,
            java.util.Random random, org.bukkit.TreeType type) {
        var origin = new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        var source = new org.bukkit.craftbukkit.v1_20_R1.util.RandomSourceWrapper(random);
        if (type == org.bukkit.TreeType.CHORUS_PLANT) {
            net.minecraft.block.ChorusFlowerBlock.generate(access, origin, source, 8);
            return true;
        }
        var key = switch (type) {
            case TREE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.OAK;
            case BIG_TREE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.FANCY_OAK;
            case REDWOOD -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.SPRUCE;
            case TALL_REDWOOD -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.PINE;
            case BIRCH -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.BIRCH;
            case JUNGLE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.MEGA_JUNGLE_TREE;
            case SMALL_JUNGLE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.JUNGLE_TREE_NO_VINE;
            case COCOA_TREE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.JUNGLE_TREE;
            case JUNGLE_BUSH -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.JUNGLE_BUSH;
            case RED_MUSHROOM -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.HUGE_RED_MUSHROOM;
            case BROWN_MUSHROOM -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.HUGE_BROWN_MUSHROOM;
            case SWAMP -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.SWAMP_OAK;
            case ACACIA -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.ACACIA;
            case DARK_OAK -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.DARK_OAK;
            case MEGA_REDWOOD -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.MEGA_SPRUCE;
            case TALL_BIRCH -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.SUPER_BIRCH_BEES_0002;
            case CRIMSON_FUNGUS -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.CRIMSON_FUNGUS_PLANTED;
            case WARPED_FUNGUS -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.WARPED_FUNGUS_PLANTED;
            case AZALEA -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.AZALEA_TREE;
            case MANGROVE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.MANGROVE;
            case TALL_MANGROVE -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.TALL_MANGROVE;
            case CHERRY -> net.minecraft.world.gen.feature.TreeConfiguredFeatures.CHERRY;
            case CHORUS_PLANT -> throw new AssertionError("Handled above");
        };
        var feature = access.getRegistryManager().get(net.minecraft.registry.RegistryKeys.CONFIGURED_FEATURE).get(key);
        if (feature == null) throw new IllegalStateException("Configured tree feature is missing: " + key.getValue());
        return feature.generate(access, getHandle().getChunkManager().getChunkGenerator(), source, origin);
    }
    @Override public void setBiome(org.bukkit.Location location, org.bukkit.block.Biome biome) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        setBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), biome);
    }

    private void validateSpawnLocation(org.bukkit.Location location) {
        if (location == null) throw new IllegalArgumentException("Location cannot be null");
        location.checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
    }
    @Override public org.bukkit.entity.Entity spawnEntity(org.bukkit.Location location, org.bukkit.entity.EntityType type) {
        return spawnEntity(location, type, true);
    }
    @Override public org.bukkit.entity.Entity spawnEntity(org.bukkit.Location location, org.bukkit.entity.EntityType type, boolean randomizeData) {
        dev.meldrite.v1201.compat.NativeEntitySpawner.validateType(type);
        return spawnNative(location, type, null, randomizeData, null, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
    @Override public <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location location, Class<T> type) {
        return spawn(location, type, true, null);
    }
    @Override public <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location location, Class<T> type,
            boolean randomizeData, org.bukkit.util.Consumer<T> consumer) {
        return spawnNative(location, dev.meldrite.v1201.compat.NativeEntitySpawner.type(type), type, randomizeData,
                consumer, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);
    }
    @Override public <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location location, Class<T> type,
            org.bukkit.util.Consumer<T> consumer, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return spawnNative(location, dev.meldrite.v1201.compat.NativeEntitySpawner.type(type), type, true, consumer, reason);
    }
    private <T extends org.bukkit.entity.Entity> T spawnNative(org.bukkit.Location location, org.bukkit.entity.EntityType type,
            Class<T> apiType, boolean randomizeData, org.bukkit.util.Consumer<T> consumer,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        validateSpawnLocation(location);
        if (reason == null) throw new IllegalArgumentException("Spawn reason cannot be null");
        requireWritable();
        var world = getHandle();
        var entity = dev.meldrite.v1201.compat.NativeEntitySpawner.create(world, location, type, apiType, randomizeData);
        @SuppressWarnings("unchecked") T result = apiType == null ? (T) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity)
                : apiType.cast(dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity));
        return dev.meldrite.v1201.compat.NativeEntitySpawner.add(world, entity, result, reason, randomizeData, consumer,
                context.server().getPluginManager());
    }
    @Override public org.bukkit.entity.Arrow spawnArrow(org.bukkit.Location location, org.bukkit.util.Vector direction, float speed, float spread) {
        return spawnArrow(location, direction, speed, spread, org.bukkit.entity.Arrow.class);
    }
    @Override public <T extends org.bukkit.entity.AbstractArrow> T spawnArrow(org.bukkit.Location location,
            org.bukkit.util.Vector direction, float speed, float spread, Class<T> type) {
        validateSpawnLocation(location);
        if (direction == null || type == null) throw new IllegalArgumentException("Direction and arrow class cannot be null");
        direction.checkFinite();
        if (!Float.isFinite(speed) || !Float.isFinite(spread)) throw new IllegalArgumentException("Arrow speed and spread must be finite");
        var entityType = dev.meldrite.v1201.compat.NativeEntitySpawner.type(type);
        if (entityType != org.bukkit.entity.EntityType.ARROW && entityType != org.bukkit.entity.EntityType.SPECTRAL_ARROW
                && entityType != org.bukkit.entity.EntityType.TRIDENT) throw new IllegalArgumentException("Class is not an arrow: " + type.getName());
        requireWritable();
        var world = getHandle();
        var entity = (net.minecraft.entity.projectile.PersistentProjectileEntity) dev.meldrite.v1201.compat.NativeEntitySpawner.create(world, location, entityType, type, true);
        entity.setVelocity(direction.getX(), direction.getY(), direction.getZ(), speed, spread);
        entity.pickupType = net.minecraft.entity.projectile.PersistentProjectileEntity.PickupPermission.ALLOWED;
        var result = type.cast(dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity));
        return dev.meldrite.v1201.compat.NativeEntitySpawner.add(world, entity, result,
                org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM, false, null, context.server().getPluginManager());
    }
    @Override public org.bukkit.entity.FallingBlock spawnFallingBlock(org.bukkit.Location location, org.bukkit.block.data.BlockData data) {
        validateSpawnLocation(location);
        if (!(data instanceof org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData nativeData)
                || data.getMaterial().isAir()) throw new IllegalArgumentException("Falling block data must be a non-air native block");
        requireWritable();
        var world = getHandle();
        var entity = dev.meldrite.v1201.compat.NativeEntitySpawner.fallingBlock(world, location, nativeData.getState());
        var result = (org.bukkit.entity.FallingBlock) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity);
        return dev.meldrite.v1201.compat.NativeEntitySpawner.add(world, entity, result,
                org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM, false, null, context.server().getPluginManager());
    }
    @Override public org.bukkit.entity.FallingBlock spawnFallingBlock(org.bukkit.Location location, org.bukkit.material.MaterialData data) {
        if (data == null) throw new IllegalArgumentException("Falling block data cannot be null");
        return spawnFallingBlock(location, data.getItemType(), data.getData());
    }
    @Override public org.bukkit.entity.FallingBlock spawnFallingBlock(org.bukkit.Location location, org.bukkit.Material material, byte data) {
        if (material == null || !material.isBlock() || material.isAir()) throw new IllegalArgumentException("Material must be a non-air block");
        return spawnFallingBlock(location, org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData.fromData(
                org.bukkit.craftbukkit.legacy.CraftLegacy.fromLegacyData(material, data)));
    }

    @Override public org.bukkit.entity.Item dropItem(org.bukkit.Location location, org.bukkit.inventory.ItemStack item) {
        return dropItem(location, item, null);
    }
    @Override public org.bukkit.entity.Item dropItem(org.bukkit.Location location, org.bukkit.inventory.ItemStack item,
            org.bukkit.util.Consumer<org.bukkit.entity.Item> consumer) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        java.util.Objects.requireNonNull(item, "item");
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        if (item.getType().isAir() || item.getAmount() <= 0) throw new IllegalArgumentException("Cannot drop an empty item stack");
        requireWritable();
        var world = getHandle();
        var nativeItem = new net.minecraft.entity.ItemEntity(world, location.getX(), location.getY(), location.getZ(),
                org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asNMSCopy(item));
        nativeItem.setPickupDelay(10);
        var result = (org.bukkit.entity.Item) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(nativeItem);
        return dev.meldrite.v1201.compat.NativeEntitySpawner.add(world, nativeItem, result,
                org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM, false, consumer, context.server().getPluginManager());
    }
    @Override public org.bukkit.entity.Item dropItemNaturally(org.bukkit.Location location, org.bukkit.inventory.ItemStack item) {
        return dropItemNaturally(location, item, null);
    }
    @Override public org.bukkit.entity.Item dropItemNaturally(org.bukkit.Location location, org.bukkit.inventory.ItemStack item,
            org.bukkit.util.Consumer<org.bukkit.entity.Item> consumer) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        var random = getHandle().getRandom();
        var natural = location.clone().add(random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.5 - 0.25, random.nextDouble() * 0.5 - 0.25);
        return dropItem(natural, item, consumer);
    }

    private org.bukkit.entity.LightningStrike strikeLightning(org.bukkit.Location location, boolean cosmetic) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        requireWritable();
        var world = getHandle();
        var lightning = new net.minecraft.entity.LightningEntity(net.minecraft.entity.EntityType.LIGHTNING_BOLT, world);
        lightning.refreshPositionAndAngles(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        lightning.setCosmetic(cosmetic);
        if (!world.spawnEntity(lightning)) throw new IllegalStateException("NATIVE_LIGHTNING_SPAWN_REJECTED");
        var result = (org.bukkit.entity.LightningStrike) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(lightning);
        var event = new org.bukkit.event.weather.LightningStrikeEvent(this, result, org.bukkit.event.weather.LightningStrikeEvent.Cause.CUSTOM);
        context.server().getPluginManager().callEvent(event);
        if (event.isCancelled()) lightning.discard();
        return result;
    }
    @Override public org.bukkit.entity.LightningStrike strikeLightning(org.bukkit.Location location) { return strikeLightning(location, false); }
    @Override public org.bukkit.entity.LightningStrike strikeLightningEffect(org.bukkit.Location location) { return strikeLightning(location, true); }

    private boolean explode(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, org.bukkit.entity.Entity source) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || !Float.isFinite(power) || power < 0) {
            throw new IllegalArgumentException("Explosion coordinates and power must be finite and nonnegative");
        }
        requireWritable();
        net.minecraft.entity.Entity nativeSource = null;
        if (source != null) {
            if (!(source instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity craft) || craft.getWorld() != this) {
                throw new IllegalArgumentException("Explosion source is not in this world");
            }
            nativeSource = craft.getHandle();
        }
        var sourceType = !breakBlocks ? net.minecraft.world.World.ExplosionSourceType.NONE
                : nativeSource == null ? net.minecraft.world.World.ExplosionSourceType.BLOCK
                : net.minecraft.world.World.ExplosionSourceType.MOB;
        var explosion = getHandle().createExplosion(nativeSource, null, new net.minecraft.world.explosion.ExplosionBehavior(),
                x, y, z, power, setFire, sourceType);
        return explosion != null;
    }
    @Override public boolean createExplosion(double x, double y, double z, float power) { return explode(x, y, z, power, false, true, null); }
    @Override public boolean createExplosion(double x, double y, double z, float power, boolean setFire) { return explode(x, y, z, power, setFire, true, null); }
    @Override public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) { return explode(x, y, z, power, setFire, breakBlocks, null); }
    @Override public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, org.bukkit.entity.Entity source) { return explode(x, y, z, power, setFire, breakBlocks, source); }
    @Override public boolean createExplosion(org.bukkit.Location location, float power) { return createExplosion(location, power, false, true); }
    @Override public boolean createExplosion(org.bukkit.Location location, float power, boolean setFire) { return createExplosion(location, power, setFire, true); }
    @Override public boolean createExplosion(org.bukkit.Location location, float power, boolean setFire, boolean breakBlocks) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        return explode(location.getX(), location.getY(), location.getZ(), power, setFire, breakBlocks, null);
    }
    @Override public boolean createExplosion(org.bukkit.Location location, float power, boolean setFire, boolean breakBlocks, org.bukkit.entity.Entity source) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        return explode(location.getX(), location.getY(), location.getZ(), power, setFire, breakBlocks, source);
    }
    @Override public boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location location, float power, boolean setFire, boolean breakBlocks) {
        return createExplosion(location, power, setFire, breakBlocks, source);
    }

    private static net.minecraft.sound.SoundCategory soundCategory(org.bukkit.SoundCategory category) {
        return net.minecraft.sound.SoundCategory.valueOf(java.util.Objects.requireNonNull(category, "category").name());
    }
    @Override public void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, int data) { playEffect(location, effect, data, 64); }
    @Override public void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, int data, int radius) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        java.util.Objects.requireNonNull(effect, "effect");
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        if (radius < 0) throw new IllegalArgumentException("radius must be nonnegative");
        var packet = new net.minecraft.network.packet.s2c.play.WorldEventS2CPacket(effect.getId(),
                new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
        double squaredRadius = (double) radius * radius;
        for (var player : getHandle().getPlayers()) {
            if (player.networkHandler != null && player.squaredDistanceTo(location.getX(), location.getY(), location.getZ()) <= squaredRadius) {
                player.networkHandler.sendPacket(packet);
            }
        }
    }
    @Override public <T> void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, T data) { playEffect(location, effect, data, 64); }
    @Override public <T> void playEffect(org.bukkit.Location location, org.bukkit.Effect effect, T data, int radius) {
        java.util.Objects.requireNonNull(effect, "effect");
        if (data != null && (effect.getData() == null || !effect.getData().isInstance(data))) throw new IllegalArgumentException("Invalid effect data for " + effect);
        if (data == null && effect.getData() != null) throw new IllegalArgumentException("Effect requires data: " + effect);
        playEffect(location, effect, CraftEffect.getDataValue(effect, data), radius);
    }
    private void playSound(double x, double y, double z, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        playSound(x, y, z, org.bukkit.craftbukkit.v1_20_R1.CraftSound.getSoundEffect(sound), category, volume, pitch);
    }
    private void playSound(double x, double y, double z, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        playSound(x, y, z, org.bukkit.craftbukkit.v1_20_R1.CraftSound.getSoundEffect(sound), category, volume, pitch);
    }
    private void playSound(double x, double y, double z, net.minecraft.sound.SoundEvent sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        getHandle().playSound(null, x, y, z, net.minecraft.registry.Registries.SOUND_EVENT.getEntry(sound), soundCategory(category), volume, pitch, getHandle().getRandom().nextLong());
    }
    @Override public void playSound(org.bukkit.Location location, org.bukkit.Sound sound, float volume, float pitch) { playSound(location, sound, org.bukkit.SoundCategory.MASTER, volume, pitch); }
    @Override public void playSound(org.bukkit.Location location, String sound, float volume, float pitch) { playSound(location, sound, org.bukkit.SoundCategory.MASTER, volume, pitch); }
    @Override public void playSound(org.bukkit.Location location, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        java.util.Objects.requireNonNull(location, "location").checkFinite(); if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        playSound(location.getX(), location.getY(), location.getZ(), sound, category, volume, pitch);
    }
    @Override public void playSound(org.bukkit.Location location, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        java.util.Objects.requireNonNull(location, "location").checkFinite(); if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        playSound(location.getX(), location.getY(), location.getZ(), sound, category, volume, pitch);
    }
    private void playSound(org.bukkit.entity.Entity entity, net.minecraft.sound.SoundEvent sound, org.bukkit.SoundCategory category, float volume, float pitch) {
        if (!(entity instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity craft) || craft.getWorld() != this) throw new IllegalArgumentException("Sound source is not in this world");
        getHandle().playSoundFromEntity(null, craft.getHandle(), net.minecraft.registry.Registries.SOUND_EVENT.getEntry(sound), soundCategory(category), volume, pitch, getHandle().getRandom().nextLong());
    }
    @Override public void playSound(org.bukkit.entity.Entity entity, org.bukkit.Sound sound, float volume, float pitch) { playSound(entity, sound, org.bukkit.SoundCategory.MASTER, volume, pitch); }
    @Override public void playSound(org.bukkit.entity.Entity entity, String sound, float volume, float pitch) { playSound(entity, sound, org.bukkit.SoundCategory.MASTER, volume, pitch); }
    @Override public void playSound(org.bukkit.entity.Entity entity, org.bukkit.Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) { playSound(entity, org.bukkit.craftbukkit.v1_20_R1.CraftSound.getSoundEffect(sound), category, volume, pitch); }
    @Override public void playSound(org.bukkit.entity.Entity entity, String sound, org.bukkit.SoundCategory category, float volume, float pitch) { playSound(entity, org.bukkit.craftbukkit.v1_20_R1.CraftSound.getSoundEffect(sound), category, volume, pitch); }

    private <T> void spawnParticleNative(org.bukkit.Particle particle, double x, double y, double z, int count,
            double offsetX, double offsetY, double offsetZ, double extra, T data) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || count < 0) throw new IllegalArgumentException("Invalid particle parameters");
        getHandle().spawnParticles(org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toNMS(particle, data), x, y, z, count, offsetX, offsetY, offsetZ, extra);
    }
    @Override public void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count) { spawnParticle(particle, x, y, z, count, 0, 0, 0, 0, null); }
    @Override public void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) { spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 0, null); }
    @Override public void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) { spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) { spawnParticleNative(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, T data) { spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 0, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, T data) { spawnParticle(particle, x, y, z, count, 0, 0, 0, 0, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) { spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, java.util.List<org.bukkit.entity.Player> receivers, org.bukkit.entity.Player sender,
            double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
        java.util.Objects.requireNonNull(receivers, "receivers");
        var effect = org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toNMS(particle, data);
        for (var receiver : receivers) {
            if (!(receiver instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer player) || player.getWorld() != this) continue;
            getHandle().spawnParticles(player.getHandle(), effect, force, x, y, z, count, offsetX, offsetY, offsetZ, extra);
        }
    }
    @Override public void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count) { spawnParticle(particle, location, count, 0, 0, 0, 0, null); }
    @Override public void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ) { spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, 0, null); }
    @Override public void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) { spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, null); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) { java.util.Objects.requireNonNull(location, "location").checkFinite(); if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world"); spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ, extra, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) { spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, double offsetX, double offsetY, double offsetZ, T data) { spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, 0, data); }
    @Override public <T> void spawnParticle(org.bukkit.Particle particle, org.bukkit.Location location, int count, T data) { spawnParticle(particle, location, count, 0, 0, 0, 0, data); }

    private org.bukkit.generator.BiomeProvider biomeProvider() {
        return new org.bukkit.generator.BiomeProvider() {
            @Override public org.bukkit.block.Biome getBiome(org.bukkit.generator.WorldInfo info, int x, int y, int z) { return CraftWorld.this.getBiome(x, y, z); }
            @Override public java.util.List<org.bukkit.block.Biome> getBiomes(org.bukkit.generator.WorldInfo info) {
                return java.util.stream.StreamSupport.stream(org.bukkit.Registry.BIOME.spliterator(), false).toList();
            }
        };
    }
    @Override public org.bukkit.generator.BiomeProvider getBiomeProvider() { return biomeProvider(); }
    @Override public org.bukkit.generator.BiomeProvider vanillaBiomeProvider() { return biomeProvider(); }

    // World queries stay backed by the live ServerWorld so plugins observe the
    // same dimension, biome and heightmap state as Fabric code.
    private org.bukkit.block.Biome biome(net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> entry) {
        var key = entry == null ? null : entry.getKey().orElse(null);
        if (key == null) return org.bukkit.block.Biome.CUSTOM;
        var projected = org.bukkit.Registry.BIOME.get(org.bukkit.NamespacedKey.fromString(key.getValue().toString()));
        return projected == null ? org.bukkit.block.Biome.CUSTOM : projected;
    }

    private net.minecraft.registry.entry.RegistryEntry<net.minecraft.world.biome.Biome> nativeBiome(int x, int y, int z) {
        return getHandle().getBiomeAccess().getBiome(new net.minecraft.util.math.BlockPos(x, y, z));
    }

    @Override public org.bukkit.block.Biome getBiome(int x, int z) { return biome(nativeBiome(x, 0, z)); }
    @Override public org.bukkit.block.Biome getBiome(int x, int y, int z) { return biome(nativeBiome(x, y, z)); }
    @Override public org.bukkit.block.Biome getComputedBiome(int x, int y, int z) { return getBiome(x, y, z); }
    @Override public org.bukkit.block.Biome getBiome(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        return getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    @Override public double getTemperature(int x, int z) { return getTemperature(x, 0, z); }
    @Override public double getTemperature(int x, int y, int z) {
        return nativeBiome(x, y, z).value().getTemperature();
    }
    @Override public double getHumidity(int x, int z) { return getHumidity(x, 0, z); }
    @Override public double getHumidity(int x, int y, int z) {
        return dev.meldrite.v1201.compat.NativeBiomeValues.downfall(nativeBiome(x, y, z).value());
    }
    @Override public void setBiome(int x, int z, org.bukkit.block.Biome biome) {
        java.util.Objects.requireNonNull(biome, "biome");
        for (int y = getMinHeight(); y < getMaxHeight(); y++) setBiome(x, y, z, biome);
    }
    @Override public void setBiome(int x, int y, int z, org.bukkit.block.Biome biome) {
        java.util.Objects.requireNonNull(biome, "biome");
        var id = new net.minecraft.util.Identifier(biome.getKey().getNamespace(), biome.getKey().getKey());
        var key = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.BIOME, id);
        var entry = getHandle().getRegistryManager().get(net.minecraft.registry.RegistryKeys.BIOME).getEntry(key).orElse(null);
        if (entry == null) throw new IllegalArgumentException("Unknown native biome: " + biome.getKey());
        requireWritable();
        var chunk = getHandle().getChunkManager().getChunk(x >> 4, z >> 4, net.minecraft.world.chunk.ChunkStatus.FULL, true);
        if (!(chunk instanceof net.minecraft.world.chunk.WorldChunk worldChunk)) return;
        int section = getHandle().getSectionIndex(y);
        if (section < 0 || section >= worldChunk.getSectionArray().length) return;
        var container = worldChunk.getSection(section).getBiomeContainer();
        if (!(container instanceof net.minecraft.world.chunk.PalettedContainer<?>)) return;
        @SuppressWarnings("rawtypes") var paletted = (net.minecraft.world.chunk.PalettedContainer) container;
        paletted.set((x >> 2) & 3, (y >> 2) & 3, (z >> 2) & 3, entry);
        worldChunk.setNeedsSaving(true);
    }

    private int height(net.minecraft.world.Heightmap.Type type, int x, int z) {
        var chunk = getHandle().getChunkManager().getChunk(x >> 4, z >> 4, net.minecraft.world.chunk.ChunkStatus.FULL, true);
        return chunk.sampleHeightmap(type, x & 15, z & 15);
    }
    @Override public int getHighestBlockYAt(int x, int z) { return getHighestBlockYAt(x, z, org.bukkit.HeightMap.MOTION_BLOCKING); }
    @Override public int getHighestBlockYAt(int x, int z, org.bukkit.HeightMap heightMap) {
        return height(CraftHeightMap.toNMS(java.util.Objects.requireNonNull(heightMap, "heightMap")), x, z);
    }
    @Override public int getHighestBlockYAt(int x, int z, com.destroystokyo.paper.HeightmapType heightMap) {
        java.util.Objects.requireNonNull(heightMap, "heightMap");
        return switch (heightMap) {
            case LIGHT_BLOCKING -> height(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z);
            case ANY -> height(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, x, z);
            case SOLID -> height(net.minecraft.world.Heightmap.Type.OCEAN_FLOOR, x, z);
            case SOLID_OR_LIQUID -> height(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, x, z);
            case SOLID_OR_LIQUID_NO_LEAVES -> height(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
        };
    }
    @Override public org.bukkit.block.Block getHighestBlockAt(int x, int z) { return getBlockAt(x, getHighestBlockYAt(x, z), z); }
    @Override public org.bukkit.block.Block getHighestBlockAt(int x, int z, org.bukkit.HeightMap heightMap) { return getBlockAt(x, getHighestBlockYAt(x, z, heightMap), z); }
    @Override public org.bukkit.block.Block getHighestBlockAt(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }
    @Override public org.bukkit.block.Block getHighestBlockAt(org.bukkit.Location location, org.bukkit.HeightMap heightMap) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ(), heightMap);
    }

    @Override public int getMinHeight() { return getHandle().getBottomY(); }
    @Override public int getMaxHeight() { return getHandle().getTopY(); }
    @Override public int getLogicalHeight() { return getHandle().getLogicalHeight(); }
    @Override public boolean isNatural() { return getHandle().getDimension().natural(); }
    @Override public boolean isBedWorks() { return getHandle().getDimension().bedWorks(); }
    @Override public boolean hasSkyLight() { return getHandle().getDimension().hasSkyLight(); }
    @Override public boolean hasSkylight() { return getHandle().getDimension().hasSkyLight(); }
    @Override public boolean hasCeiling() { return getHandle().getDimension().hasCeiling(); }
    @Override public boolean hasBedrockCeiling() { return getHandle().getDimension().hasCeiling(); }
    @Override public boolean isPiglinSafe() { return getHandle().getDimension().piglinSafe(); }
    @Override public boolean isRespawnAnchorWorks() { return getHandle().getDimension().respawnAnchorWorks(); }
    @Override public boolean doesBedWork() { return getHandle().getDimension().bedWorks(); }
    @Override public boolean doesRespawnAnchorWork() { return getHandle().getDimension().respawnAnchorWorks(); }
    @Override public boolean isUltrawarm() { return getHandle().getDimension().ultrawarm(); }
    @Override public double getCoordinateScale() { return getHandle().getDimension().coordinateScale(); }
    @Override public boolean isFixedTime() { return getHandle().getDimension().hasFixedTime(); }
    @Override public boolean canGenerateStructures() { return getHandle().getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures(); }
    @Override public org.bukkit.WorldType getWorldType() { return getHandle().isFlat() ? org.bukkit.WorldType.FLAT : org.bukkit.WorldType.NORMAL; }
    @Override public boolean isAutoSave() { return !getHandle().savingDisabled; }
    @Override public void setAutoSave(boolean value) { requireWritable(); getHandle().savingDisabled = !value; }
    @Override public void save() {
        requireWritable();
        var world = getHandle();
        boolean disabled = world.savingDisabled;
        world.savingDisabled = false;
        try { world.save(null, false, false); }
        finally { world.savingDisabled = disabled; }
    }
    @Override public org.bukkit.Difficulty getDifficulty() {
        return org.bukkit.Difficulty.getByValue(getHandle().getDifficulty().getId());
    }
    @Override public void setDifficulty(org.bukkit.Difficulty difficulty) {
        java.util.Objects.requireNonNull(difficulty, "difficulty");
        requireWritable();
        getHandle().getServer().setDifficulty(net.minecraft.world.Difficulty.byId(difficulty.getValue()), true);
    }
    @Override public boolean getPVP() { return getHandle().getServer().isPvpEnabled(); }
    @Override public void setPVP(boolean pvp) { requireWritable(); getHandle().getServer().setPvpEnabled(pvp); }
    @Override public boolean isHardcore() { return getHandle().getLevelProperties().isHardcore(); }
    @Override public void setHardcore(boolean hardcore) {
        requireWritable();
        var properties = (dev.meldrite.v1201.hook.LevelPropertiesAccessMixin) getHandle().getLevelProperties();
        var info = properties.meldrite$levelInfo();
        properties.meldrite$levelInfo(new net.minecraft.world.level.LevelInfo(
                info.getLevelName(), info.getGameMode(), hardcore, info.getDifficulty(),
                info.areCommandsAllowed(), info.getGameRules(), info.getDataConfiguration()));
    }
    @Override public boolean getKeepSpawnInMemory() { return dev.meldrite.v1201.compat.NativeWorldSettings.getKeepSpawnInMemory(getHandle()); }
    @Override public void setKeepSpawnInMemory(boolean keepLoaded) { requireWritable(); dev.meldrite.v1201.compat.NativeWorldSettings.setKeepSpawnInMemory(getHandle(), keepLoaded); }
    @Override public int getSpawnLimit(org.bukkit.entity.SpawnCategory category) { return dev.meldrite.v1201.compat.NativeWorldSettings.getSpawnLimit(getHandle(), category); }
    @Override public void setSpawnLimit(org.bukkit.entity.SpawnCategory category, int limit) { requireWritable(); dev.meldrite.v1201.compat.NativeWorldSettings.setSpawnLimit(getHandle(), category, limit); }
    @Override public long getTicksPerSpawns(org.bukkit.entity.SpawnCategory category) { return dev.meldrite.v1201.compat.NativeWorldSettings.getTicksPerSpawns(getHandle(), category); }
    @Override public void setTicksPerSpawns(org.bukkit.entity.SpawnCategory category, int ticks) { requireWritable(); dev.meldrite.v1201.compat.NativeWorldSettings.setTicksPerSpawns(getHandle(), category, ticks); }
    @Override public int getMonsterSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.MONSTER); }
    @Override public void setMonsterSpawnLimit(int limit) { setSpawnLimit(org.bukkit.entity.SpawnCategory.MONSTER, limit); }
    @Override public int getAnimalSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.ANIMAL); }
    @Override public void setAnimalSpawnLimit(int limit) { setSpawnLimit(org.bukkit.entity.SpawnCategory.ANIMAL, limit); }
    @Override public int getWaterAnimalSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_ANIMAL); }
    @Override public void setWaterAnimalSpawnLimit(int limit) { setSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_ANIMAL, limit); }
    @Override public int getWaterAmbientSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_AMBIENT); }
    @Override public void setWaterAmbientSpawnLimit(int limit) { setSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_AMBIENT, limit); }
    @Override public int getWaterUndergroundCreatureSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE); }
    @Override public void setWaterUndergroundCreatureSpawnLimit(int limit) { setSpawnLimit(org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE, limit); }
    @Override public int getAmbientSpawnLimit() { return getSpawnLimit(org.bukkit.entity.SpawnCategory.AMBIENT); }
    @Override public void setAmbientSpawnLimit(int limit) { setSpawnLimit(org.bukkit.entity.SpawnCategory.AMBIENT, limit); }
    @Override public long getTicksPerAnimalSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.ANIMAL); }
    @Override public void setTicksPerAnimalSpawns(int ticks) { setTicksPerSpawns(org.bukkit.entity.SpawnCategory.ANIMAL, ticks); }
    @Override public long getTicksPerMonsterSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.MONSTER); }
    @Override public void setTicksPerMonsterSpawns(int ticks) { setTicksPerSpawns(org.bukkit.entity.SpawnCategory.MONSTER, ticks); }
    @Override public long getTicksPerWaterSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_ANIMAL); }
    @Override public void setTicksPerWaterSpawns(int ticks) { setTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_ANIMAL, ticks); }
    @Override public long getTicksPerWaterAmbientSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_AMBIENT); }
    @Override public void setTicksPerWaterAmbientSpawns(int ticks) { setTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_AMBIENT, ticks); }
    @Override public long getTicksPerWaterUndergroundCreatureSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE); }
    @Override public void setTicksPerWaterUndergroundCreatureSpawns(int ticks) { setTicksPerSpawns(org.bukkit.entity.SpawnCategory.WATER_UNDERGROUND_CREATURE, ticks); }
    @Override public long getTicksPerAmbientSpawns() { return getTicksPerSpawns(org.bukkit.entity.SpawnCategory.AMBIENT); }
    @Override public void setTicksPerAmbientSpawns(int ticks) { setTicksPerSpawns(org.bukkit.entity.SpawnCategory.AMBIENT, ticks); }
    @Override public boolean getAllowAnimals() {
        return ((dev.meldrite.v1201.hook.ServerChunkManagerSpawnOptionsAccess) getHandle().getChunkManager()).meldrite$spawnAnimals();
    }
    @Override public boolean getAllowMonsters() {
        return ((dev.meldrite.v1201.hook.ServerChunkManagerSpawnOptionsAccess) getHandle().getChunkManager()).meldrite$spawnMonsters();
    }
    @Override public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
        requireWritable(); getHandle().getChunkManager().setMobSpawnOptions(allowMonsters, allowAnimals);
    }
    @Override public java.util.Collection<org.bukkit.Material> getInfiniburn() {
        var tag = getHandle().getDimension().infiniburn();
        var blocks = net.minecraft.registry.Registries.BLOCK.iterateEntries(tag);
        var result = new java.util.LinkedHashSet<org.bukkit.Material>();
        blocks.forEach(entry -> result.add(org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.getMaterial(entry.value())));
        return java.util.Collections.unmodifiableSet(result);
    }
    @Override public java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() {
        var enabled = getHandle().getEnabledFeatures();
        var result = new java.util.LinkedHashSet<org.bukkit.FeatureFlag>();
        for (var id : net.minecraft.resource.featuretoggle.FeatureFlags.FEATURE_MANAGER.toId(enabled)) {
            var flag = org.bukkit.craftbukkit.v1_20_R1.CraftFeatureFlag.getFromNMS(new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()));
            if (flag != null && enabled.contains(flag.getHandle())) result.add(flag);
        }
        return java.util.Collections.unmodifiableSet(result);
    }

    @Override public long getTime() {
        return Math.floorMod(getHandle().getTimeOfDay(), 24000L);
    }

    @Override public void setTime(long time) {
        long current = getFullTime();
        long margin = Math.floorMod(time - current, 24000L);
        setFullTime(current + margin);
    }

    @Override public long getFullTime() { return getHandle().getTimeOfDay(); }

    @Override public void setFullTime(long time) {
        requireWritable();
        var world = getHandle();
        var event = new org.bukkit.event.world.TimeSkipEvent(this, org.bukkit.event.world.TimeSkipEvent.SkipReason.CUSTOM, time - world.getTimeOfDay());
        context.server().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        world.setTimeOfDay(world.getTimeOfDay() + event.getSkipAmount());
        for (var player : world.getPlayers()) {
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket(
                    world.getTime(), world.getTimeOfDay(), world.getGameRules().getBoolean(net.minecraft.world.GameRules.DO_DAYLIGHT_CYCLE)));
        }
    }

    @Override public long getGameTime() { return getHandle().getTime(); }

    @Override public org.bukkit.Location getSpawnLocation() {
        var pos = getHandle().getSpawnPos();
        return new org.bukkit.Location(this, pos.getX(), pos.getY(), pos.getZ(), getHandle().getSpawnAngle(), 0);
    }

    @Override public boolean setSpawnLocation(org.bukkit.Location location) {
        java.util.Objects.requireNonNull(location).checkFinite();
        if (location.getWorld() != this) return false;
        requireWritable();
        var previous = getSpawnLocation();
        getHandle().setSpawnPos(new net.minecraft.util.math.BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()), location.getYaw());
        context.server().getPluginManager().callEvent(new org.bukkit.event.world.SpawnChangeEvent(this, previous));
        return true;
    }
    @Override public boolean setSpawnLocation(int x, int y, int z, float angle) { return setSpawnLocation(new org.bukkit.Location(this, x, y, z, angle, 0)); }
    @Override public boolean setSpawnLocation(int x, int y, int z) { return setSpawnLocation(x, y, z, 0); }

    @Override public java.util.List<org.bukkit.entity.Player> getPlayers() {
        return getHandle().getPlayers().stream()
                .map(entity -> (org.bukkit.entity.Player) dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }

    public Iterable<net.minecraft.entity.Entity> getNMSEntities() { return getHandle().iterateEntities(); }
    @Override public org.bukkit.util.RayTraceResult rayTraceEntities(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance) {
        return rayTraceEntities(start, direction, distance, 0, null);
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceEntities(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance,
            double raySize) {
        return rayTraceEntities(start, direction, distance, raySize, null);
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceEntities(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance,
            java.util.function.Predicate<org.bukkit.entity.Entity> filter) {
        return rayTraceEntities(start, direction, distance, 0, filter);
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceEntities(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance,
            double raySize, java.util.function.Predicate<org.bukkit.entity.Entity> filter) {
        if (start == null || start.getWorld() != this) throw new IllegalArgumentException("Ray start must be in this world");
        start.checkFinite();
        if (direction == null) throw new IllegalArgumentException("Ray direction is required");
        direction.checkFinite();
        if (!(direction.lengthSquared() > 0)) throw new IllegalArgumentException("Ray direction must be nonzero");
        if (!Double.isFinite(distance) || !Double.isFinite(raySize)) throw new IllegalArgumentException("Ray distance and size must be finite");
        if (distance < 0) return null;
        var from = start.toVector();
        var offset = direction.clone().normalize().multiply(distance);
        var bounds = org.bukkit.util.BoundingBox.of(from, from).expandDirectional(offset).expand(raySize);
        org.bukkit.util.RayTraceResult nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (var entity : getNearbyEntities(bounds, filter)) {
            var hit = entity.getBoundingBox().expand(raySize).rayTrace(from, direction, distance);
            if (hit == null) continue;
            double hitDistance = from.distanceSquared(hit.getHitPosition());
            if (hitDistance < nearestDistance) {
                nearestDistance = hitDistance;
                nearest = new org.bukkit.util.RayTraceResult(hit.getHitPosition(), entity, hit.getHitBlockFace());
            }
        }
        return nearest;
    }
    @Override public org.bukkit.util.RayTraceResult rayTrace(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance,
            org.bukkit.FluidCollisionMode fluid, boolean ignorePassableBlocks, double raySize,
            java.util.function.Predicate<org.bukkit.entity.Entity> filter) {
        var blockHit = rayTraceBlocks(start, direction, distance, fluid, ignorePassableBlocks);
        double limit = blockHit == null ? distance : start.toVector().distance(blockHit.getHitPosition());
        var entityHit = rayTraceEntities(start, direction, limit, raySize, filter);
        if (blockHit == null) return entityHit;
        if (entityHit == null) return blockHit;
        return start.toVector().distanceSquared(entityHit.getHitPosition()) < limit * limit ? entityHit : blockHit;
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceBlocks(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance) {
        return rayTraceBlocks(start, direction, distance, org.bukkit.FluidCollisionMode.NEVER, false);
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceBlocks(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance,
            org.bukkit.FluidCollisionMode fluid) {
        return rayTraceBlocks(start, direction, distance, fluid, false);
    }
    @Override public org.bukkit.util.RayTraceResult rayTraceBlocks(org.bukkit.Location start, org.bukkit.util.Vector direction, double distance,
            org.bukkit.FluidCollisionMode fluid, boolean ignorePassableBlocks) {
        if (start == null || start.getWorld() != this) throw new IllegalArgumentException("Ray start must be in this world");
        start.checkFinite();
        if (direction == null) throw new IllegalArgumentException("Ray direction is required");
        direction.checkFinite();
        if (!(direction.lengthSquared() > 0)) throw new IllegalArgumentException("Ray direction must be nonzero");
        if (fluid == null) throw new IllegalArgumentException("Fluid mode is required");
        if (!Double.isFinite(distance)) throw new IllegalArgumentException("Ray distance must be finite");
        if (distance < 0) return null;
        var world = getHandle();
        dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(world);
        var offset = direction.clone().normalize().multiply(distance);
        var from = new net.minecraft.util.math.Vec3d(start.getX(), start.getY(), start.getZ());
        var to = from.add(offset.getX(), offset.getY(), offset.getZ());
        var handling = switch (fluid) {
            case NEVER -> net.minecraft.world.RaycastContext.FluidHandling.NONE;
            case SOURCE_ONLY -> net.minecraft.world.RaycastContext.FluidHandling.SOURCE_ONLY;
            case ALWAYS -> net.minecraft.world.RaycastContext.FluidHandling.ANY;
        };
        var shape = ignorePassableBlocks ? net.minecraft.world.RaycastContext.ShapeType.COLLIDER : net.minecraft.world.RaycastContext.ShapeType.OUTLINE;
        var hit = world.raycast(new net.minecraft.world.RaycastContext(from, to, shape, handling, null));
        if (hit.getType() == net.minecraft.util.hit.HitResult.Type.MISS) return null;
        var pos = hit.getBlockPos(); var point = hit.getPos();
        return new org.bukkit.util.RayTraceResult(new org.bukkit.util.Vector(point.x, point.y, point.z),
                getBlockAt(pos.getX(), pos.getY(), pos.getZ()), org.bukkit.block.BlockFace.valueOf(hit.getSide().name()));
    }
    @Override public java.util.List<org.bukkit.entity.Entity> getEntities() {
        var result = new java.util.ArrayList<org.bukkit.entity.Entity>();
        for (var nativeEntity : getNMSEntities()) {
            var entity = dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(nativeEntity);
            if (entity.isValid()) result.add(entity);
        }
        return result;
    }
    @Override public java.util.List<org.bukkit.entity.LivingEntity> getLivingEntities() {
        return getEntities().stream().filter(org.bukkit.entity.LivingEntity.class::isInstance)
                .map(org.bukkit.entity.LivingEntity.class::cast).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
    @Override public org.bukkit.entity.Entity getEntity(java.util.UUID uuid) {
        java.util.Objects.requireNonNull(uuid, "uuid");
        var entity = getHandle().getEntity(uuid);
        return entity == null ? null : dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(entity);
    }
    @Override public int getEntityCount() {
        int count = 0;
        for (var entity : getNMSEntities()) if (!entity.isRemoved()) count++;
        return count;
    }
    @Override public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getEntitiesByClass(Class<T> type) {
        java.util.Objects.requireNonNull(type, "type");
        return getEntities().stream().filter(type::isInstance).map(type::cast).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
    @Override public <T extends org.bukkit.entity.Entity> java.util.Collection<T> getEntitiesByClass(Class<T>... types) {
        java.util.Objects.requireNonNull(types, "types");
        var result = new java.util.ArrayList<T>();
        for (var entity : getEntities()) {
            for (var type : types) if (java.util.Objects.requireNonNull(type).isInstance(entity)) { result.add(type.cast(entity)); break; }
        }
        return result;
    }
    @Override public java.util.Collection<org.bukkit.entity.Entity> getEntitiesByClasses(Class<?>... types) {
        java.util.Objects.requireNonNull(types, "types");
        for (var type : types) java.util.Objects.requireNonNull(type, "type");
        return getEntities().stream().filter(entity -> java.util.Arrays.stream(types).anyMatch(type -> type.isInstance(entity)))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
    @Override public java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(org.bukkit.Location location, double x, double y, double z) {
        return getNearbyEntities(location, x, y, z, null);
    }
    @Override public java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(org.bukkit.Location location, double x, double y, double z,
            java.util.function.Predicate<org.bukkit.entity.Entity> filter) {
        java.util.Objects.requireNonNull(location, "location").checkFinite();
        if (location.getWorld() != this) throw new IllegalArgumentException("Location is in a different world");
        return getNearbyEntities(org.bukkit.util.BoundingBox.of(location, x, y, z), filter);
    }
    @Override public java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(org.bukkit.util.BoundingBox box) { return getNearbyEntities(box, null); }
    @Override public java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(org.bukkit.util.BoundingBox box,
            java.util.function.Predicate<org.bukkit.entity.Entity> filter) {
        java.util.Objects.requireNonNull(box, "box");
        var world = getHandle();
        dev.meldrite.v1201.compat.NativeEntityExtensions.requireThread(world);
        var bounds = new net.minecraft.util.math.Box(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ());
        var result = new java.util.ArrayList<org.bukkit.entity.Entity>();
        for (var nativeEntity : world.getOtherEntities(null, bounds, entity -> true)) {
            var entity = dev.meldrite.v1201.compat.NativeEntityExtensions.bukkit(nativeEntity);
            if (filter == null || filter.test(entity)) result.add(entity);
        }
        return result;
    }

    private net.minecraft.world.level.ServerWorldProperties properties() {
        return ((dev.meldrite.v1201.hook.ServerWorldPropertiesPaperAccessMixin) getHandle()).meldrite$properties();
    }
    @Override public boolean hasStorm() { return properties().isRaining(); }
    @Override public void setStorm(boolean storm) {
        requireWritable();
        if (hasStorm() != storm) {
            var event = new org.bukkit.event.weather.WeatherChangeEvent(this, storm, org.bukkit.event.weather.WeatherChangeEvent.Cause.PLUGIN);
            context.server().getPluginManager().callEvent(event);
            if (!event.isCancelled()) properties().setRaining(storm);
        }
        setWeatherDuration(0); setClearWeatherDuration(0);
    }
    @Override public boolean isThundering() { return properties().isThundering(); }
    @Override public void setThundering(boolean thunder) {
        requireWritable();
        if (isThundering() != thunder) {
            var event = new org.bukkit.event.weather.ThunderChangeEvent(this, thunder, org.bukkit.event.weather.ThunderChangeEvent.Cause.PLUGIN);
            context.server().getPluginManager().callEvent(event);
            if (!event.isCancelled()) properties().setThundering(thunder);
        }
        setThunderDuration(0); setClearWeatherDuration(0);
    }
    @Override public int getWeatherDuration() { return properties().getRainTime(); }
    @Override public void setWeatherDuration(int duration) { requireWritable(); properties().setRainTime(duration); }
    @Override public int getThunderDuration() { return properties().getThunderTime(); }
    @Override public void setThunderDuration(int duration) { requireWritable(); properties().setThunderTime(duration); }
    @Override public int getClearWeatherDuration() { return properties().getClearWeatherTime(); }
    @Override public void setClearWeatherDuration(int duration) { requireWritable(); properties().setClearWeatherTime(duration); }
    @Override public boolean isClearWeather() { return !hasStorm() && !isThundering(); }
    @Override public long getSeed() { return getHandle().getSeed(); }
    @Override public int getSeaLevel() { return getHandle().getSeaLevel(); }

    public void requireWritable() { if (!getHandle().getServer().isOnThread()) throw new IllegalStateException("ASYNC_WORLD_WRITE"); }

    private record RuleDefinition(net.minecraft.world.GameRules.Key<?> key, net.minecraft.world.GameRules.Type<?> type) { }
    private static java.util.Map<String, RuleDefinition> gameRules() {
        var result = new java.util.LinkedHashMap<String, RuleDefinition>();
        net.minecraft.world.GameRules.accept(new net.minecraft.world.GameRules.Visitor() {
            @Override public <T extends net.minecraft.world.GameRules.Rule<T>> void visit(net.minecraft.world.GameRules.Key<T> key, net.minecraft.world.GameRules.Type<T> type) { result.put(key.getName(), new RuleDefinition(key, type)); }
        });
        return result;
    }

    @Override public String[] getGameRules() { return gameRules().keySet().toArray(String[]::new); }
    @Override public boolean isGameRule(String rule) {
        if (rule == null || rule.isEmpty()) throw new IllegalArgumentException("Game rule cannot be null or empty");
        return gameRules().containsKey(rule);
    }
    @Override public String getGameRuleValue(String rule) {
        if (rule == null) return null;
        var key = gameRules().get(rule); if (key == null) return "";
        return getHandle().getGameRules().get(key.key()).serialize();
    }
    @Override public boolean setGameRuleValue(String rule, String value) {
        if (rule == null || value == null) return false;
        var key = gameRules().get(rule); if (key == null) return false;
        requireWritable();
        var bukkitRule = org.bukkit.GameRule.getByName(rule);
        if (bukkitRule != null) {
            var event = new io.papermc.paper.event.world.WorldGameRuleChangeEvent(this, null, bukkitRule, value);
            context.server().getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            value = event.getValue();
        }
        var nativeRule = (dev.meldrite.v1201.hook.GameRulePaperAccessMixin) getHandle().getGameRules().get(key.key());
        nativeRule.meldrite$deserialize(value);
        nativeRule.meldrite$changed(getHandle().getServer());
        return true;
    }
    @Override public <T> T getGameRuleValue(org.bukkit.GameRule<T> rule) {
        java.util.Objects.requireNonNull(rule);
        var key = gameRules().get(rule.getName()); if (key == null) return null;
        var value = getHandle().getGameRules().get(key.key());
        if (value instanceof net.minecraft.world.GameRules.BooleanRule bool) return rule.getType().cast(bool.get());
        if (value instanceof net.minecraft.world.GameRules.IntRule integer) return rule.getType().cast(integer.get());
        throw new IllegalArgumentException("Unsupported native game rule: " + rule.getName());
    }
    @Override public <T> T getGameRuleDefault(org.bukkit.GameRule<T> rule) {
        java.util.Objects.requireNonNull(rule);
        var key = gameRules().get(rule.getName()); if (key == null) return null;
        var value = key.type().createRule();
        if (value instanceof net.minecraft.world.GameRules.BooleanRule bool) return rule.getType().cast(bool.get());
        if (value instanceof net.minecraft.world.GameRules.IntRule integer) return rule.getType().cast(integer.get());
        throw new IllegalArgumentException("Unsupported native game rule: " + rule.getName());
    }
    @Override public <T> boolean setGameRule(org.bukkit.GameRule<T> rule, T value) {
        java.util.Objects.requireNonNull(rule); java.util.Objects.requireNonNull(value);
        rule.getType().cast(value);
        return setGameRuleValue(rule.getName(), String.valueOf(value));
    }
}
