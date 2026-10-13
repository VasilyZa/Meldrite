package dev.meldrite.paper.bootstrap;

import dev.meldrite.bridge.*;
import dev.meldrite.paper.*;
import dev.meldrite.paper.event.EventDispatcherAdapter;
import dev.meldrite.paper.event.EventFactory;
import dev.meldrite.paper.facade.*;
import dev.meldrite.paper.generated.org.bukkit.plugin.ServicesManagerAdapter;
import dev.meldrite.paper.scheduler.SchedulerAdapter;
import dev.meldrite.paper.scheduler.PaperSchedulerAdapter;
import dev.meldrite.paper.compat.LegacyNameTransform;
import dev.meldrite.paper.compat.EntityAbiTransform;
import dev.meldrite.paper.compat.LegacyTextConstructorTransform;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.plugin.runtime.*;
import dev.meldrite.plugin.spi.*;
import dev.meldrite.scheduler.*;
import dev.meldrite.service.*;
import dev.meldrite.event.EventPolicy;
import dev.meldrite.event.ActionToken;
import dev.meldrite.event.EventCoordinator;
import dev.meldrite.adventure.ComponentCodec;
import dev.meldrite.registry.ProjectionResult;
import java.nio.file.*;
import java.time.Duration;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

/** Profile-owned API projections around the existing shared services and lifecycle algorithms. */
public final class ProfileBootstrap implements AutoCloseable {
    private final ExecutionPort execution;
    private final WorldPort worldPort;
    private final EntityPort entityPort;
    private final WorldService worlds;
    private final EntityService entities;
    private final ArrayDeque<AutoCloseable> owned = new ArrayDeque<>();
    private final AtomicInteger remaps = new AtomicInteger();
    private final PluginLoaderEngine engine;
    private final FacadeContext facade;
    private final Path pluginsDirectory;
    private final TickScheduler ticks;
    private final AsyncScheduler async;
    private final SchedulerAdapter scheduler;
    private final PaperSchedulerAdapter paperScheduler;
    private final ItemFactoryAdapter itemFactory;
    private final ServicesManagerAdapter services;
    private final EventDispatcherAdapter events;
    private final EventCoordinator eventCoordinator;
    private final EventFactory eventFactory;
    private volatile PluginManager pluginManager;
    private final CommandMap commandMap;
    private final Set<Plugin> commandsRegistered = Collections.newSetFromMap(new IdentityHashMap<>());
    private final ComponentCodec components = new ComponentCodec();
    private volatile List<PluginHostAdapter> hosts = List.of();
    private LoadPlan plan;
    private PaperProviderAdapter provider;
    private PluginController controller;
    private boolean closed;
    private boolean serverLoaded;
    private boolean reloading;

    public static ProfileBootstrap create(BridgeBundle bridges, Path directory) throws Exception {
        return create(bridges, directory, List.of());
    }
    public static ProfileBootstrap create(BridgeBundle bridges, Path directory, List<NativeRoute> nativeRoutes) throws Exception {
        return create(bridges, directory, nativeRoutes, ProfileServer::new);
    }
    public static ProfileBootstrap create(BridgeBundle bridges, Path directory, List<NativeRoute> nativeRoutes,
            java.util.function.BiFunction<FacadeContext, ProfileBootstrap, org.bukkit.Server> serverCreator) throws Exception {
        return new ProfileBootstrap(bridges, directory, nativeRoutes, serverCreator);
    }
    public FacadeContext facadeContext() { primary(); return facade; }
    private ProfileBootstrap(BridgeBundle bridges, Path directory, List<NativeRoute> nativeRoutes,
            java.util.function.BiFunction<FacadeContext, ProfileBootstrap, org.bukkit.Server> serverCreator) throws Exception {
        execution = bridges.execution().orElseThrow(() -> new IllegalStateException("MISSING_REQUIRED_CAPABILITY: execution"));
        primary();
        pluginsDirectory = Files.createDirectories(directory.resolve("plugins"));
        engine = own(new PluginLoaderEngine(new ApiAuthority(getClass().getClassLoader(), ApiProviderLocator.paths()),
                (bytes, context) -> {
                    remaps.incrementAndGet();
                    return new LegacyNameTransform().apply(new EntityAbiTransform().apply(new LegacyTextConstructorTransform().apply(bytes, context), context), context);
                }, nativeRoutes));
        try {
            // Absent game effects reject explicitly. V1 installs only the proven lifecycle/execution bridges.
            worldPort = bridges.world().orElse(world -> { throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: world.lifecycle"); });
            entityPort = bridges.entity().orElse(entity -> { throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: entity.lifecycle"); });
            worlds = new WorldService(worldPort, execution);
            entities = new EntityService(entityPort, worlds);
            facade = own(new FacadeContext(entities, worlds, execution,
                new FacadeContext.ServerInfo("Paper", "git-Paper-196 (MC: 1.20.1)", "1.20.1-R0.1-SNAPSHOT", "1.20.1", pluginsDirectory,
                            Logger.getLogger("Meldrite")), context -> serverCreator.apply(context, this)));
            // Version servers can own a concrete Craft command map for their reflective ABI.
            // Registration, lookup and dispatch must all use that authoritative instance.
            var serverCommands = facade.server().getCommandMap();
            commandMap = serverCommands == null ? new SimpleCommandMap(facade.server()) : serverCommands;
            events = new EventDispatcherAdapter(new EventPolicy(execution, 32),
                    failure -> Logger.getLogger("Meldrite.Events").log(Level.SEVERE, "Plugin listener failed", failure.cause()), engine);
            eventCoordinator = new EventCoordinator(new EventPolicy(execution, 16),
                    failure -> Logger.getLogger("Meldrite.Events").log(Level.SEVERE, "Native action failed", failure));
            eventFactory = new EventFactory(this::playerProjection,
                    point -> {
                        try { return new ProjectionResult.Exact<>(facade.block(point)); }
                        catch (RuntimeException failure) { return new ProjectionResult.Unavailable<>(failure.getMessage()); }
                    }, new dev.meldrite.paper.registry.MaterialProjection());
            services = own(new ServicesManagerAdapter(execution, this::scope, events::callEvent));
            // Close the tick scheduler before the async executor.  PluginScope
            // cancellation removes pending handles first; otherwise the
            // executor drains stale queued callbacks during server shutdown.
            var workers = WorkerSettings.system();
            dev.meldrite.paper.map.VectorMapColors.configureWorkers(workers);
            own((AutoCloseable) dev.meldrite.paper.map.VectorMapColors::closeWorkers);
            async = own(AsyncScheduler.configured(Duration.ofSeconds(10)));
            Logger.getLogger("Meldrite.Workers").info("Background workers: CPUs=" + workers.processors()
                    + ", async=" + workers.asyncThreads() + ", async queue=" + workers.asyncQueueCapacity()
                    + ", compute lanes=" + workers.computeThreads());
            ticks = own(new TickScheduler(execution, execution.currentTick(),
                    failure -> Logger.getLogger("Meldrite.Scheduler").log(Level.SEVERE, "Plugin task failed", failure.cause())));
            scheduler = new SchedulerAdapter(ticks, async, this::scope, engine);
            paperScheduler = new PaperSchedulerAdapter(scheduler);
            facade.installEntitySchedulers(own(new dev.meldrite.paper.scheduler.EntitySchedulerService(execution, this::scope, engine)));
            itemFactory = org.bukkit.craftbukkit.inventory.CraftItemFactory.instance();
            own(new ConfigurationService(directory.resolve("plugin-data")));
            own(new PersistentDataService());
            if (Bukkit.getServer() != null) throw new IllegalStateException("API_SERVER_ALREADY_BOUND");
            Bukkit.setServer(facade.server());
            org.spigotmc.SpigotConfig.init(directory.resolve("spigot.yml").toFile());
            if (components.fromJson(components.json(net.kyori.adventure.text.Component.text("Meldrite"))) == null) {
                throw new IllegalStateException("ADVENTURE_CODEC_UNAVAILABLE");
            }
            own(this::shutdownPlugins);
        } catch (Throwable failure) {
            try { close(); } catch (Throwable cleanup) { failure.addSuppressed(cleanup); }
            throw failure;
        }
    }
    private <T extends AutoCloseable> T own(T resource) { owned.push(resource); return resource; }
    private void primary() { if (!execution.isPrimaryThread()) throw new IllegalStateException("PROFILE_BOOTSTRAP_OFF_PRIMARY"); }
    private PluginScope scope(Plugin plugin) {
        for (var host : hosts) if (host.loader().getPlugin() == plugin) return host.scope();
        throw new IllegalArgumentException("UNKNOWN_PLUGIN_OWNER: " + plugin.getName());
    }
    private Plugin plugin(String name) {
        for (var host : hosts) if (host.identity().id().equals(name) || host.loader().getPlugin() != null
                && host.loader().getPlugin().getName().equals(name)) return host.loader().getPlugin();
        throw new IllegalArgumentException("UNKNOWN_PLUGIN_OWNER: " + name);
    }
    public void discover() throws Exception {
        primary();
        if (closed) throw new IllegalStateException("PROFILE_CLOSED");
        if (controller != null) throw new IllegalStateException("PLUGINS_ALREADY_DISCOVERED");
        var descriptor = new DescriptorAdapter();
        var discovered = new PluginDiscovery(descriptor).discover(pluginsDirectory);
        plan = new DependencyPlanner().plan(discovered.stream().map(PluginDiscovery.DiscoveredPlugin::descriptor).toList(), descriptor.dependencyRules());
        provider = new PaperProviderAdapter(engine, facade.server(), pluginsDirectory,
                new LibraryResolver(pluginsDirectory.resolve(".libraries"), List.of(), List.of(), true, true));
        hosts = provider.prepare(discovered, plan);
        controller = new PluginController(plan, new ArrayList<PluginHost>(hosts), Set.of(PluginController.Step.ENABLE));
    }
    private void shutdownPlugins() {
        if (controller == null) return;
        controller.disableAll();
        var failures = controller.diagnostics().stream().filter(diagnostic -> diagnostic.code().equals("PLUGIN_DISABLE_FAILED")
                || diagnostic.code().equals("PLUGIN_CLEANUP_FAILED")).toList();
        if (!failures.isEmpty()) {
            var failure = new IllegalStateException("PLUGIN_SHUTDOWN_FAILED: " + failures);
            for (var diagnostic : failures) {
                Logger.getLogger("Meldrite.Plugins").log(Level.SEVERE, diagnostic.message(), diagnostic.cause());
                if (diagnostic.cause() != null) failure.addSuppressed(diagnostic.cause());
            }
            throw failure;
        }
    }
    private void clearPluginHosts() {
        primary();
        if (controller != null) controller.checkIdle();
        try { shutdownPlugins(); }
        finally {
            hosts = List.of();
            plan = null;
            provider = null;
            controller = null;
            commandsRegistered.clear();
            org.bukkit.event.HandlerList.unregisterAll();
        }
    }
    private void reload(Runnable clearPluginState, Runnable reloadResources, Runnable publishCommands) {
        primary();
        if (closed || ((ServerAdapter) facade.server()).isStopping()) throw new IllegalStateException("PROFILE_STOPPING");
        if (!serverLoaded) throw new IllegalStateException("RELOAD_BEFORE_WORLDS_READY");
        if (reloading) throw new IllegalStateException("PLUGIN_RELOAD_REENTRANT");
        if (controller != null) controller.checkIdle();
        var manager = facade.server().getPluginManager();
        reloading = true;
        boolean pluginsChanged = false;
        try {
            var configuration = dev.meldrite.paper.configuration.ServerConfiguration.current();
            configuration.reload();
            org.spigotmc.SpigotConfig.init(configuration.directory().resolve("spigot.yml").toFile());
            pluginsChanged = true;
            engine.prepareReload();
            manager.clearPlugins();
            commandMap.clearCommands();
            clearPluginState.run();
            reloadResources.run();
            if (manager instanceof io.papermc.paper.plugin.manager.PaperPluginManagerImpl) {
                org.bukkit.util.permissions.DefaultPermissions.registerCorePermissions();
            }
            discover();
            bootstrapPlugins();
            loadPlugins();
            enable(PluginDescriptor.EnablePhase.STARTUP);
            enable(PluginDescriptor.EnablePhase.POSTWORLD);
            publishCommands.run();
            events.callEvent(new org.bukkit.event.server.ServerLoadEvent(org.bukkit.event.server.ServerLoadEvent.LoadType.RELOAD));
        } catch (Exception | Error failure) {
            if (pluginsChanged) {
                cleanupAfterReloadFailure(failure, manager::clearPlugins);
                cleanupAfterReloadFailure(failure, commandMap::clearCommands);
                cleanupAfterReloadFailure(failure, clearPluginState);
                cleanupAfterReloadFailure(failure, publishCommands);
            }
            Logger.getLogger("Meldrite.Plugins").log(Level.SEVERE, "Bukkit plugin reload failed", failure);
            if (failure instanceof Error error) throw error;
            throw new IllegalStateException("PLUGIN_RELOAD_FAILED", failure);
        } finally { reloading = false; }
    }
    private static void cleanupAfterReloadFailure(Throwable original, Runnable operation) {
        try { operation.run(); }
        catch (Throwable cleanup) { if (cleanup != original) original.addSuppressed(cleanup); }
    }
    public void bootstrapPlugins() throws Exception { primary(); provider.bootstrap(hosts, plan); }
    public void loadPlugins() { primary(); controller.loadAll(); reportFailures(); }
    public void enableStartup() { enable(PluginDescriptor.EnablePhase.STARTUP); }
    public void worldsReady() {
        primary();
        if (serverLoaded) return;
        bindLoadedWorlds();
        facade.markWorldsReady();
        enable(PluginDescriptor.EnablePhase.POSTWORLD);
        serverLoaded = true;
        events.callEvent(new org.bukkit.event.server.ServerLoadEvent(org.bukkit.event.server.ServerLoadEvent.LoadType.STARTUP));
    }

    /** Installs the profile-owned event construction and dispatch path behind the version gateway. */
    public EventIngressPort eventIngress() {
        return action -> {
            primary();
            try (var token = new ActionToken(action.token())) {
                return eventCoordinator.dispatch(token, action, false, eventFactory::create,
                        events::callEvent, eventFactory::decision);
            }
        };
    }

    private ProjectionResult<org.bukkit.entity.Player> playerProjection(dev.meldrite.foundation.EntityHandle handle) {
        try {
            var info = entityPort.describe(handle).orElse(null);
            if (info == null || !info.player()) return new ProjectionResult.Unavailable<>("player absent");
            var reference = worlds.reference(handle.world());
            var identity = new EntityService.Identity(handle, reference, 0, true, info.name());
            var entity = facade.entity(identity);
            if (!(entity instanceof org.bukkit.entity.Player player)) return new ProjectionResult.Unavailable<>("not a player");
            return new ProjectionResult.Exact<>(player);
        } catch (RuntimeException failure) {
            return new ProjectionResult.Unavailable<>(failure.getMessage());
        }
    }

    private void bindLoadedWorlds() {
        for (var info : worldPort.loadedWorlds()) {
            try { worlds.reference(info.key()); }
            catch (IllegalStateException absent) { worlds.bind(info.key(), info.uid(), info.name()); }
        }
    }
    private void enable(PluginDescriptor.EnablePhase phase) {
        primary();
        for (String id : plan.serverOrder()) {
            var host = hosts.stream().filter(value -> value.identity().id().equals(id)).findFirst().orElseThrow();
            if (host.descriptor().enablePhase() == phase && controller.state(id) == PluginState.LOADED) enable(host);
        }
        reportFailures();
    }
    private void enable(PluginHostAdapter host) {
        Plugin plugin = host.loader().getPlugin();
        if (commandsRegistered.add(plugin)) {
            // Paper plugins register their commands in code; legacy YAML must not reserve those labels.
            if (host.descriptor().kind() == PluginDescriptor.Kind.LEGACY) {
                commandMap.registerAll(plugin.getName(), org.bukkit.command.PluginCommandYamlParser.parse(plugin));
            }
            for (var permission : plugin.getDescription().getPermissions()) {
                try { facade.server().getPluginManager().addPermission(permission); }
                catch (IllegalArgumentException duplicate) {
                    plugin.getLogger().log(Level.WARNING, "Permission '" + permission.getName() + "' is already registered", duplicate);
                }
            }
        }
        controller.enable(host.identity().id());
    }
    private void reportFailures() {
        var diagnostics = controller.diagnostics();
        for (var diagnostic : diagnostics) Logger.getLogger("Meldrite.Plugins").log(Level.SEVERE, diagnostic.message(), diagnostic.cause());
        if (diagnostics.stream().anyMatch(diagnostic -> diagnostic.code().startsWith("PLUGIN_")
                && diagnostic.code().endsWith("_FAILED"))) {
            var failure = new IllegalStateException("PLUGIN_LIFECYCLE_FAILURES: " + diagnostics);
            for (var diagnostic : diagnostics) if (diagnostic.cause() != null) failure.addSuppressed(diagnostic.cause());
            throw failure;
        }
    }
    public void tick() { primary(); if (!closed) { facade.tickEntitySchedulers(); ticks.tick(execution.currentTick()); } }
    public String workerStatus() {
        var background = async.statistics();
        var compute = dev.meldrite.paper.map.VectorMapColors.workerStatistics();
        return "Async: limit=" + background.threads() + ", active=" + background.active()
                + ", queued=" + background.queued() + "/" + background.queueCapacity()
                + ", completed=" + background.completed() + ", rejected=" + background.rejected()
                + " | Compute: lanes=" + compute.parallelism() + ", active=" + compute.active()
                + ", queued=" + compute.queued() + "/" + compute.queueCapacity()
                + ", completed=" + compute.completed() + ", caller fallbacks=" + compute.callerFallbacks()
                + ", vector=" + dev.meldrite.paper.map.VectorMapColors.vectorBits() + " bits";
    }
    public int remapCalls() { return remaps.get(); }
    public long enabledPlugins() { primary(); return plan.serverOrder().stream().filter(id -> controller.state(id) == PluginState.ENABLED).count(); }
    private PluginManager pluginManager() {
        var existing = pluginManager;
        if (existing != null) return existing;
        synchronized (this) {
            if (pluginManager == null) {
                pluginManager = (PluginManager) Proxy.newProxyInstance(
                        PluginManager.class.getClassLoader(), new Class<?>[]{PluginManager.class}, (proxy, method, args) -> {
                            if (method.getName().equals("getPlugin") && args != null && args.length == 1
                                    && args[0] instanceof String name) {
                                return pluginByName(name);
                            }
                            if (method.getName().equals("getPlugins")) {
                                return hosts.stream().map(host -> host.loader().getPlugin())
                                        .filter(Objects::nonNull).toArray(Plugin[]::new);
                            }
                            if (method.getName().equals("isPluginEnabled")) {
                                Plugin target = args != null && args.length == 1 && args[0] instanceof Plugin plugin
                                        ? plugin : args != null && args.length == 1 && args[0] instanceof String name
                                        ? pluginByName(name) : null;
                                return target != null && hosts.stream().anyMatch(host -> host.loader().getPlugin() == target
                                        && controller != null && controller.state(host.identity().id()) == PluginState.ENABLED);
                            }
                            if (method.getName().equals("disablePlugin") && args != null && args.length == 1
                                    && args[0] instanceof Plugin target) {
                                hosts.stream().filter(host -> host.loader().getPlugin() == target).findFirst()
                                        .ifPresent(host -> controller.disableFromPlugin(host.identity().id()));
                                return null;
                            }
                            if (method.getName().equals("enablePlugin") && args != null && args.length == 1
                                    && args[0] instanceof Plugin target) {
                                hosts.stream().filter(host -> host.loader().getPlugin() == target).findFirst()
                                        .ifPresent(this::enable);
                                return null;
                            }
                            if (method.getName().equals("disablePlugins")) {
                                shutdownPlugins();
                                return null;
                            }
                            if (method.getName().equals("clearPlugins")) {
                                clearPluginHosts();
                                return null;
                            }
                            if (method.getName().equals("loadPlugin") && args != null && args.length == 1
                                    && args[0] instanceof java.io.File file) {
                                throw new UnsupportedOperationException("DYNAMIC_PLUGIN_LOAD_REQUIRES_RESTART: " + file);
                            }
                            if (method.getName().equals("loadPlugins") && args != null && args.length == 1
                                    && args[0] instanceof java.io.File directory) {
                                throw new UnsupportedOperationException("DYNAMIC_PLUGIN_LOAD_REQUIRES_RESTART: " + directory);
                            }
                            if (method.getName().equals("registerEvents") && args != null && args.length == 2
                                    && args[0] instanceof org.bukkit.event.Listener listener
                                    && args[1] instanceof Plugin plugin) {
                                events.registerEvents(listener, plugin, scope(plugin));
                                return null;
                            }
                            if (method.getName().equals("registerEvent") && args != null
                                    && (args.length == 5 || args.length == 6)
                                    && args[0] instanceof Class<?> eventType
                                    && org.bukkit.event.Event.class.isAssignableFrom(eventType)
                                    && args[1] instanceof org.bukkit.event.Listener listener
                                    && args[2] instanceof org.bukkit.event.EventPriority priority
                                    && args[3] instanceof org.bukkit.plugin.EventExecutor executor
                                    && args[4] instanceof Plugin plugin) {
                                boolean ignoreCancelled = args.length == 6 && Boolean.TRUE.equals(args[5]);
                                events.registerEvent(eventType.asSubclass(org.bukkit.event.Event.class), listener, priority,
                                        executor, plugin, ignoreCancelled, scope(plugin));
                                return null;
                            }
                            if (method.getName().equals("callEvent") && args != null && args.length == 1
                                    && args[0] instanceof org.bukkit.event.Event event) {
                                events.callEvent(event);
                                return null;
                            }
                            if (method.getDeclaringClass() == Object.class) {
                                return switch (method.getName()) {
                                    case "toString" -> "MeldritePluginManager";
                                    case "hashCode" -> System.identityHashCode(proxy);
                                    case "equals" -> proxy == args[0];
                                    default -> null;
                                };
                            }
                            throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/plugin/PluginManager#"
                                    + method.getName());
                        });
            }
            return pluginManager;
        }
    }
    private Plugin pluginByName(String name) {
        if (name == null) return null;
        return hosts.stream().map(host -> host.loader().getPlugin())
                .filter(Objects::nonNull)
                .filter(plugin -> plugin.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
    private synchronized PluginCommand pluginCommand(String name) {
        if (name == null || name.isBlank()) return null;
        var command = commandMap.getCommand(name);
        return command instanceof PluginCommand pluginCommand ? pluginCommand : null;
    }
    private CommandMap commandMap() { return commandMap; }
    private org.bukkit.inventory.ItemFactory itemFactory() { return itemFactory; }
    @Override public void close() throws Exception {
        primary(); if (closed) return; closed = true;
        ((ServerAdapter) facade.server()).markStopping();
        var failure = new Exception("PROFILE_CLEANUP_FAILED");
        while (!owned.isEmpty()) try { owned.pop().close(); } catch (Throwable cause) { failure.addSuppressed(cause); }
        if (failure.getSuppressed().length != 0) throw failure;
    }
    /** Public concrete server type exposes the Paper server API to reflection. */
    public static class ProfileServer extends ServerAdapter {
        private final ProfileBootstrap runtime;
        public ProfileServer(FacadeContext context, ProfileBootstrap runtime) { super(context); this.runtime = runtime; }
        @Override public org.bukkit.plugin.PluginManager getPluginManager() { return runtime.pluginManager(); }
        @Override public org.bukkit.command.CommandMap getCommandMap() { return runtime.commandMap(); }
        @Override public org.bukkit.command.PluginCommand getPluginCommand(String name) { return runtime.pluginCommand(name); }
        @Override public boolean dispatchCommand(org.bukkit.command.CommandSender sender, String command) throws org.bukkit.command.CommandException {
            Objects.requireNonNull(command, "command");
            int separator = command.indexOf(' ');
            String label = (separator < 0 ? command : command.substring(0, separator)).toLowerCase(Locale.ENGLISH);
            var target = runtime.commandMap.getCommand(label);
            ClassLoader loader = target instanceof org.bukkit.command.PluginIdentifiableCommand owned
                    ? owned.getPlugin().getClass().getClassLoader() : target == null ? null : target.getClass().getClassLoader();
            if (!(loader instanceof PluginRealm realm)) return runtime.commandMap.dispatch(sender, command);
            try { return runtime.engine.withContext(realm, () -> runtime.commandMap.dispatch(sender, command)); }
            catch (RuntimeException | Error failure) { throw failure; }
            catch (Exception failure) { throw new org.bukkit.command.CommandException("Plugin command failed: " + label, failure); }
        }
        @Override public org.bukkit.scheduler.BukkitScheduler getScheduler() { return runtime.scheduler; }
        @Override public io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() { return runtime.paperScheduler; }
        @Override public io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() { return runtime.paperScheduler; }
        @Override public io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() { return runtime.paperScheduler; }
        @Override public org.bukkit.inventory.ItemFactory getItemFactory() { return runtime.itemFactory(); }
        @Override public org.bukkit.plugin.ServicesManager getServicesManager() { return runtime.services; }
        @Override public void reload() { runtime.reload(this::clearPluginState, this::reloadServerResources, this::publishReloadedPlugins); }
        /** Version-owned resources stay outside the shared plugin lifecycle. */
        protected void clearPluginState() { }
        protected void reloadServerResources() { }
        protected void publishReloadedPlugins() { syncCommands(); }
        /** Version-specific servers publish the command map to their native dispatcher. */
        public void syncCommands() { }
    }
}
