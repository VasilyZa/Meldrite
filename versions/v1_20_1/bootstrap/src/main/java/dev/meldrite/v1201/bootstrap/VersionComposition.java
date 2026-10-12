package dev.meldrite.v1201.bootstrap;

import dev.meldrite.bootstrap.BootstrapCoordinator;
import dev.meldrite.bootstrap.DistributionVerifier;
import dev.meldrite.bridge.BridgeBundle;
import dev.meldrite.paper.bootstrap.ProfileBootstrap;
import dev.meldrite.plugin.spi.NativeRoute;
import dev.meldrite.v1201.bridge.VersionBridgeBundle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** V1 composition root. All native objects enter the neutral runtime through this class. */
public final class VersionComposition implements AutoCloseable {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Meldrite");
    private static final DistributionVerifier VERIFIER = new DistributionVerifier();
    private final MinecraftServer server;
    private final VersionBridgeBundle.Composition bridges;
    private final BootstrapCoordinator coordinator;
    private final ProfileBootstrap profile;
    private boolean started;

    private VersionComposition(MinecraftServer server, VersionBridgeBundle.Composition bridges,
                               BootstrapCoordinator coordinator, ProfileBootstrap profile) {
        this.server = server;
        this.bridges = bridges;
        this.coordinator = coordinator;
        this.profile = profile;
    }

    public static VersionComposition start(MinecraftServer server) {
        dev.meldrite.paper.compat.DedicatedServerFacade.bind(server);
        dev.meldrite.v1201.compat.NativeMaterialProjection.bind();
        dev.meldrite.paper.facade.PlayerProfileAdapter.bind(new dev.meldrite.v1201.compat.NativeProfileBridge(server));
        dev.meldrite.v1201.compat.NativeRegistryBinding.bind(server);
        dev.meldrite.v1201.compat.NativeItemBridge.bind();
        var bridges = VersionBridgeBundle.create(server);
        var execution = bridges.execution();
        var loader = FabricLoader.getInstance();
        String namespace = loader.getMappingResolver().getCurrentRuntimeNamespace();
        LOGGER.info("Meldrite runtime: namespace={} minecraftVersion={} javaFeature={} loaderVersion={} fabricApiVersion={}",
                namespace, server.getVersion(), Runtime.version().feature(),
                loader.getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString(),
                loader.getModContainer("fabric-api").orElseThrow().getMetadata().getVersion().getFriendlyString());
        var verifier = new DistributionVerifier.Manifest("mc-1.20.1", "1.20.1", "paper-1.20.1",
                17, loader.isDevelopmentEnvironment() ? "named" : "intermediary",
                Set.of("execution.primary", "server.lifecycle.start-stop", "events.block-break", "hook.block-break.pre-commit"),
                Set.of("execution", "serverLifecycle", "eventIngress", "world", "entity"));
        var environment = environment(bridges.bundle(), server, namespace);
        VERIFIER.verify(verifier, environment);

        var coordinator = new BootstrapCoordinator(execution, phase -> { });
        coordinator.own(bridges);
        coordinator.distributionVerified();
        coordinator.transformsRegistered();
        coordinator.servicesBound();
        try {
            Path serverDirectory = Path.of(".").toAbsolutePath().normalize();
            ProfileBootstrap profile = ProfileBootstrap.create(bridges.bundle(), serverDirectory, List.of(nativeRoute(loader)),
                    (context, runtime) -> new org.bukkit.craftbukkit.v1_20_R1.CraftServer(context, runtime,
                            (net.minecraft.server.dedicated.MinecraftDedicatedServer) server));
            dev.meldrite.v1201.compat.NativeCraftFacades.install(profile.facadeContext(), bridges.entities(), bridges.worlds());
            dev.meldrite.v1201.compat.NativePotionProjection.install();
            dev.meldrite.v1201.compat.NativeEnchantmentProjection.install();
            dev.meldrite.v1201.compat.NativeScoreboardBinding.bind(server);
            coordinator.own(profile);
            org.bukkit.util.permissions.DefaultPermissions.registerCorePermissions();
            bridges.gateway().install(profile.eventIngress());
            coordinator.pluginsDiscovered();
            profile.discover();
            coordinator.pluginBootstrap();
            profile.bootstrapPlugins();
            coordinator.pluginsLoaded();
            profile.loadPlugins();
            coordinator.startupEnabled();
            profile.enableStartup();
            if (profile.facadeContext().server() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftServer craftServer) {
                craftServer.refreshHelpMap();
            }
            emit("status", "STARTING");
            return new VersionComposition(server, bridges, coordinator, profile);
        } catch (Throwable failure) {
            try {
                coordinator.close();
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            if (failure instanceof RuntimeException runtime) throw runtime;
            if (failure instanceof Error error) throw error;
            throw new IllegalStateException("PROFILE_BOOTSTRAP_FAILED", failure);
        }
    }

    public void worldsReady() {
        if (started) return;
        dev.meldrite.v1201.bridge.HookHealthVerifier.verify(bridges.gateway());
        profile.worldsReady();
        if (profile.facadeContext().server() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftServer craftServer) {
            craftServer.refreshHelpMap();
            craftServer.syncCommands();
        }
        coordinator.worldsReady();
        coordinator.ready();
        started = true;
        emit("status", "READY");
    }

    public BootstrapCoordinator.Phase phase() {
        return coordinator.phase();
    }

    public void syncCommands() {
        if (started && profile.facadeContext().server() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftServer craftServer) {
            craftServer.syncCommands();
        }
    }

    public void tick() {
        if (started && coordinator.phase() == BootstrapCoordinator.Phase.READY) profile.tick();
    }

    public String workerStatus() { return profile.workerStatus(); }

    @Override
    public void close() {
        try {
            coordinator.close();
        } finally {
            emit("status", coordinator.phase() == BootstrapCoordinator.Phase.STOPPED ? "STOPPED" : "FAILED");
        }
    }

    private static DistributionVerifier.Environment environment(BridgeBundle bundle, MinecraftServer server, String namespace) {
        Set<String> capabilities = bundle.capabilities().names();
        Set<String> ports = new HashSet<>();
        if (bundle.execution().isPresent()) ports.add("execution");
        if (bundle.eventIngress().isPresent()) ports.add("eventIngress");
        if (bundle.world().isPresent()) ports.add("world");
        if (bundle.entity().isPresent()) ports.add("entity");
        if (bundle.serverLifecycle().isPresent()) ports.add("serverLifecycle");
        return new DistributionVerifier.Environment(server.getVersion(), configured("paper-1.20.1", "MELDRITE_PROFILE"),
                runtimeJava(), namespace, duplicateSharedApis(), capabilities, ports);
    }

    private static Set<String> duplicateSharedApis() {
        try {
            Enumeration<URL> resources = VersionComposition.class.getClassLoader()
                    .getResources("net/kyori/adventure/text/Component.class");
            int count = 0;
            while (resources.hasMoreElements()) {
                resources.nextElement();
                count++;
            }
            return count > 1 ? Set.of("adventure") : Set.of();
        } catch (IOException failure) {
            throw new IllegalStateException("SHARED_API_SCAN_FAILED", failure);
        }
    }

    private static String configured(String fallback, String environmentName) {
        String property = System.getProperty(environmentName);
        if (property != null && !property.isBlank()) return property;
        String environment = System.getenv(environmentName);
        return environment == null || environment.isBlank() ? fallback : environment;
    }

    private static int runtimeJava() {
        String version = System.getProperty("java.specification.version", "17");
        if (version.startsWith("1.")) version = version.substring(2);
        int separator = version.indexOf('.');
        if (separator >= 0) version = version.substring(0, separator);
        return Integer.parseInt(version);
    }

    static void emit(String kind, String value) {
        if ("error".equals(kind)) LOGGER.error("Meldrite error: {}", value);
        else LOGGER.info("Meldrite {}: {}", kind, value);
    }

    public MinecraftServer server() {
        return server;
    }

    /**
     * Legacy Paper plugins use Mojang's readable NMS names while a production Fabric
     * server exposes intermediary names.  Keep the route explicit and resolve only
     * game/CraftBukkit namespaces; Bukkit API classes remain governed by ApiAuthority.
     */
    private static NativeRoute nativeRoute(FabricLoader loader) {
        ClassLoader runtime = VersionComposition.class.getClassLoader();
        var mappings = loader.getMappingResolver();
        dev.meldrite.paper.compat.LegacyMappings.bindRuntime(new dev.meldrite.paper.compat.LegacyMappings.RuntimeNamespace() {
            @Override public String className(String name) {
                return mappings.mapClassName("intermediary", name.replace('/', '.')).replace('.', '/');
            }
            @Override public String intermediaryClassName(String name) {
                return mappings.unmapClassName("intermediary", name.replace('/', '.')).replace('.', '/');
            }
            @Override public String fieldName(String owner, String name, String descriptor) {
                return mappings.mapFieldName("intermediary", owner.replace('/', '.'), name, descriptor);
            }
            @Override public String methodName(String owner, String name, String descriptor) {
                return mappings.mapMethodName("intermediary", owner.replace('/', '.'), name, descriptor);
            }
        });
        return new NativeRoute(runtime, Set.of(), Set.of(), requestedName -> {
            // Reflection callers commonly pass JVM internal names even though
            // Class.forName expects dotted names. Normalize before routing.
            String name = requestedName.replace('/', '.');
            String verified = dev.meldrite.paper.compat.LegacyNameTransform.reflectionClassName(name);
            if (!verified.equals(name)) {
                try { Class.forName(verified, false, runtime); return verified; }
                catch (ClassNotFoundException ignored) { return null; }
            }
            // Class.forName is also used with array descriptors by legacy
            // reflection helpers for native array types.
            if (name.equals("[Lnet.minecraft.server.MinecraftServer$TickTimes;")
                    || name.equals("[Lnet.minecraft.server.v1_20_R1.MinecraftServer$TickTimes;")
                    || name.equals("[Lnet.minecraft.server.v1_20_R3.MinecraftServer$TickTimes;")) {
                return "[Ldev.meldrite.v1201.compat.LegacyTickTimes;";
            }
            if (name.equals("[dev.meldrite.v1201.compat.LegacyTickTimes]")) {
                return "[dev.meldrite.v1201.compat.LegacyTickTimes]";
            }
            if (name.equals("dev.meldrite.v1201.compat.LegacyTickTimes")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            if (name.equals("dev.meldrite.v1201.compat.NativeEntityExtensions")
                    || name.equals("io.papermc.paper.chunk.system.entity.EntityLookup")
                    || name.equals("dev.meldrite.paper.compat.DedicatedServerFacade")
                    || name.equals("dev.meldrite.paper.facade.CraftItemStackAdapter")
                    || name.equals("dev.meldrite.paper.facade.EntityAdapter")
                    || name.equals("dev.meldrite.paper.facade.PlayerAdapter")
                    || name.equals("dev.meldrite.paper.facade.WorldAdapter")
                    || name.equals("io.papermc.paper.plugin.manager.PaperPluginManagerImpl")
                    || name.equals("dev.meldrite.paper.bootstrap.ProfileBootstrap$ProfileServer")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            // Legacy libraries may use the runtime's remapped NBT
            // name directly after their constant-pool transform.  The aliases
            // mixin exposes the old reflective method names on this class.
            if (name.equals("net.minecraft.class_2487")
                    || name.equals("net.minecraft.nbt.NBTTagCompound")
                    || name.equals("net.minecraft.nbt.CompoundTag")) {
                try { runtime.loadClass("net.minecraft.class_2487"); return "net.minecraft.class_2487"; }
                catch (ClassNotFoundException ignored) { return null; }
            }
            if (name.equals("net.minecraft.world.entity.monster.EntitySlime")
                    || name.equals("net.minecraft.world.entity.monster.Slime")) {
                try { runtime.loadClass("net.minecraft.class_1621"); return "net.minecraft.class_1621"; }
                catch (ClassNotFoundException ignored) { return null; }
            }
            if (name.equals("net.minecraft.server.v1_20_R3.MinecraftServer$TickTimes")
                    || name.equals("net.minecraft.server.v1_20_R1.MinecraftServer$TickTimes")
                    || name.equals("net.minecraft.server.MinecraftServer$TickTimes")) {
                return "dev.meldrite.v1201.compat.LegacyTickTimes";
            }
            if (name.startsWith("com.mojang.brigadier.")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            if (name.startsWith("org.bukkit.craftbukkit.v1_20_R1.")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            if (name.equals("org.bukkit.craftbukkit.util.CraftMagicNumbers")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.util.CraftMagicNumbers")) {
                return "org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers";
            }
            if (name.equals("org.bukkit.craftbukkit.util.CraftNamespacedKey")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey")) {
                return "org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey";
            }
            if (name.equals("org.bukkit.craftbukkit.util.CraftChatMessage")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.util.CraftChatMessage")) {
                return "org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage";
            }
            if (name.equals("org.bukkit.craftbukkit.util.CraftRayTraceResult")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.util.CraftRayTraceResult")) {
                return "org.bukkit.craftbukkit.v1_20_R1.util.CraftRayTraceResult";
            }
            if (name.equals("org.bukkit.craftbukkit.CraftParticle")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.CraftParticle")) {
                return "org.bukkit.craftbukkit.v1_20_R1.CraftParticle";
            }
            if (name.equals("org.bukkit.craftbukkit.CraftFeatureFlag")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.CraftFeatureFlag")) {
                return "org.bukkit.craftbukkit.v1_20_R1.CraftFeatureFlag";
            }
            if (name.equals("org.bukkit.craftbukkit.CraftServer")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R1.CraftServer")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.CraftServer")) {
                return "org.bukkit.craftbukkit.v1_20_R1.CraftServer";
            }
            if (name.equals("org.bukkit.craftbukkit.CraftWorld")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R1.CraftWorld")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.CraftWorld")) {
                return "org.bukkit.craftbukkit.v1_20_R1.CraftWorld";
            }
            if (name.equals("org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity")) {
                return name.endsWith("CraftItemStack")
                        ? "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack"
                        : "org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity";
            }
            if (name.equals("org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer")) return "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer";
            if (name.equals("org.bukkit.craftbukkit.entity.CraftEntity")) return "org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity";
            if (name.equals("org.bukkit.craftbukkit.entity.CraftPlayer")) return "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer";
            if (name.equals("org.bukkit.craftbukkit.CraftItemStack")
                    || name.equals("org.bukkit.craftbukkit.inventory.CraftItemStack")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack")
                    || name.equals("org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack")) {
                return "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack";
            }
            if (name.equals("org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity")) return "org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity";
            if (name.equals("org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer")) return "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer";
            if (name.startsWith("org.bukkit.craftbukkit.v1_20_R1.")
                    || name.startsWith("org.bukkit.craftbukkit.v1_20_R3.")) {
                int prefixLength = name.startsWith("org.bukkit.craftbukkit.v1_20_R1.")
                        ? "org.bukkit.craftbukkit.v1_20_R1.".length()
                        : "org.bukkit.craftbukkit.v1_20_R3.".length();
                String suffix = name.substring(prefixLength);
                return switch (suffix) {
                    case "inventory.CraftItemStack" -> "org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack";
                    case "entity.CraftEntity" -> "org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity";
                    case "entity.CraftPlayer" -> "org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer";
                    case "CraftWorld" -> "org.bukkit.craftbukkit.v1_20_R1.CraftWorld";
                    default -> null;
                };
            }
            if (name.startsWith("net.minecraft.server.v1_20_R1.")
                    || name.startsWith("net.minecraft.server.v1_20_R3.")) {
                String suffix = name.substring("net.minecraft.server.v1_20_R1.".length());
                if (name.startsWith("net.minecraft.server.v1_20_R3.")) {
                    suffix = name.substring("net.minecraft.server.v1_20_R3.".length());
                }
                if (suffix.equals("MinecraftServer")) return "net.minecraft.server.MinecraftServer";
            }
            if (name.startsWith("org.slf4j.") || name.startsWith("io.netty.") || name.startsWith("org.apache.commons.lang.")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            if (name.startsWith("com.mojang.authlib.")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            // Older NMS callers use pre-Mojang package names.
            // Resolve the stable 1.20.1 concepts to Fabric's intermediary classes.
            String lookup = name.startsWith("net.minecraft.") ? name.substring("net.minecraft.".length()) : name;
            String legacy = switch (lookup) {
                case "network.EnumProtocol", "network.ConnectionProtocol" -> "net.minecraft.class_2539";
                case "network.Connection", "network.NetworkManager" -> "net.minecraft.class_2535";
                case "server.network.ServerConnection", "server.network.ServerConnectionListener",
                     "ServerConnection", "network.ServerConnection" -> "net.minecraft.class_3242";
                case "network.protocol.Packet", "network.Packet" -> "net.minecraft.class_2596";
                case "network.protocol.EnumProtocolDirection", "network.protocol.PacketFlow" -> "net.minecraft.class_2598";
                case "network.chat.IChatBaseComponent", "network.chat.IChatbaseComponent", "network.chat.Component" -> "net.minecraft.class_2561";
                case "server.v1_20_R3.IChatBaseComponent", "server.v1_20_R1.IChatBaseComponent" -> "net.minecraft.class_2561";
                case "server.v1_20_R3.ChatMessageType", "network.chat.ChatMessageType", "network.chat.ChatType" -> "net.minecraft.class_2556";
                case "network.chat.ChatComponentText", "network.chat.TextComponent" -> "net.minecraft.class_2561";
                case "network.protocol.game.PacketPlayOutEntityTeleport",
                     "network.protocol.game.ClientboundTeleportEntityPacket" -> "net.minecraft.class_2777";
                case "network.protocol.game.PacketPlayOutSpawnEntity",
                     "network.protocol.game.ClientboundAddEntityPacket" -> "net.minecraft.class_2604";
                case "world.level.IBlockAccess", "world.level.BlockGetter" -> "net.minecraft.class_1922";
                case "world.entity.Entity" -> "net.minecraft.class_1297";
                case "world.entity.EntityLiving" -> "net.minecraft.class_1309";
                case "world.entity.monster.EntitySlime", "world.entity.monster.Slime" -> "net.minecraft.class_1621";
                case "world.entity.player.EntityHuman", "world.entity.player.Player" -> "net.minecraft.class_1657";
                case "world.entity.LivingEntity" -> "net.minecraft.class_1309";
                case "world.entity.AreaEffectCloud", "world.entity.EntityAreaEffectCloud" -> "net.minecraft.class_1295";
                case "server.level.EntityPlayer", "server.level.ServerPlayer", "server.network.ServerPlayer" -> "net.minecraft.class_3222";
                case "world.entity.decoration.EntityArmorStand", "world.entity.decoration.ArmorStand" -> "net.minecraft.class_1531";
                case "server.level.ServerLevel", "world.level.WorldServer" -> "net.minecraft.class_3218";
                case "server.MinecraftServer" -> "net.minecraft.server.MinecraftServer";
                case "server.network.PlayerConnection" -> "net.minecraft.class_3244";
                case "server.v1_20_R3.Entity", "server.v1_20_R1.Entity" -> "net.minecraft.class_1297";
                case "server.v1_20_R3.Packet", "server.v1_20_R1.Packet" -> "net.minecraft.class_2596";
                case "server.v1_20_R3.IRegistry", "core.IRegistry", "core.Registry" -> "net.minecraft.class_2378";
                case "server.v1_20_R3.IRegistryWritable", "core.IRegistryWritable", "core.WritableRegistry" -> "net.minecraft.class_2385";
                case "core.IRegistryCustom", "core.RegistryAccess" -> "net.minecraft.class_5455";
                case "core.registries.BuiltInRegistries" -> "net.minecraft.class_7923";
                case "core.Holder" -> "net.minecraft.class_6880";
                case "resources.ResourceKey" -> "net.minecraft.class_5321";
                case "server.v1_20_R3.MinecraftKey", "resources.MinecraftKey", "resources.ResourceLocation" -> "net.minecraft.class_2960";
                case "server.v1_20_R3.WorldServer", "server.level.WorldServer" -> "net.minecraft.class_3218";
                case "world.level.Level" -> "net.minecraft.class_1937";
                case "server.v1_20_R3.Vec3D", "world.phys.Vec3D", "world.phys.Vec3" -> "net.minecraft.class_243";
                case "server.v1_20_R3.NBTTagCompound", "nbt.CompoundTag", "nbt.NBTTagCompound" -> "net.minecraft.class_2487";
                case "server.v1_20_R3.NBTTagList", "nbt.ListTag", "nbt.NBTTagList" -> "net.minecraft.class_2499";
                case "server.v1_20_R3.ItemStack", "world.item.ItemStack" -> "net.minecraft.class_1799";
                case "server.v1_20_R3.EnumHand", "world.EnumHand", "world.InteractionHand" -> "net.minecraft.class_1268";
                case "network.PacketDataSerializer" -> "net.minecraft.class_2540";
                case "network.protocol.game.ClientboundSetActionBarTextPacket" -> "net.minecraft.class_5894";
                case "network.protocol.game.ClientboundSetTitleTextPacket" -> "net.minecraft.class_5888";
                case "network.protocol.game.ClientboundSetSubtitleTextPacket" -> "net.minecraft.class_5889";
                case "network.protocol.game.ClientboundClearTitlesPacket" -> "net.minecraft.class_5890";
                case "network.protocol.game.ClientboundSetTitlesAnimationPacket" -> "net.minecraft.class_5891";
                case "network.protocol.game.ClientboundTabListPacket" -> "net.minecraft.class_5892";
                case "server.v1_20_R3.PacketPlayOutEntitySound", "network.protocol.game.PacketPlayOutEntitySound", "network.protocol.game.ClientboundSoundEntityPacket" -> "net.minecraft.class_2675";
                case "server.v1_20_R3.PacketPlayOutOpenBook", "network.protocol.game.PacketPlayOutOpenBook", "network.protocol.game.ClientboundOpenBookPacket" -> "net.minecraft.class_2793";
                case "server.dedicated.DedicatedServer", "server.dedicated.MinecraftDedicatedServer" -> "net.minecraft.server.MinecraftServer";
                case "nbt.MojangsonParser" -> "net.minecraft.class_2522";
                case "nbt.NbtIo", "nbt.NBTCompressedStreamTools" -> "net.minecraft.class_2507";
                case "world.level.World" -> "net.minecraft.class_1937";
                case "nbt.NBTBase" -> "net.minecraft.class_2520";
                case "nbt.NBTTagString" -> "net.minecraft.class_2519";
                case "world.level.block.entity.BlockEntityType" -> "net.minecraft.class_2591";
                default -> null;
            };
            if (legacy != null) {
                try { runtime.loadClass(legacy); return legacy; } catch (ClassNotFoundException ignored) { return null; }
            }
            if (!name.startsWith("net.minecraft.")) return null;
            // A transformed plugin may already refer to intermediary names.
            // Never feed those names into unmapClassName: that converts a valid
            // intermediary class (class_3244) into an official short name (aiy).
            if (name.matches("net\\.minecraft\\.class_[0-9]+(?:\\$class_[0-9]+)*")) {
                try { runtime.loadClass(name); return name; } catch (ClassNotFoundException ignored) { return null; }
            }
            for (String source : List.of("named", "official")) {
                if (!mappings.getNamespaces().contains(source)) continue;
                try {
                    String mapped = mappings.mapClassName(source, name);
                    if (mapped != null && !mapped.isBlank()) {
                        try { runtime.loadClass(mapped); return mapped; } catch (ClassNotFoundException ignored) { }
                    }
                } catch (RuntimeException ignored) {
                    // The current loader may not publish every optional namespace.
                }
            }
            return null;
        });
    }
}
