package io.papermc.paper.plugin.manager;

import io.papermc.paper.plugin.PermissionManager;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.UnknownDependencyException;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import dev.meldrite.paper.event.EventDispatcherAdapter;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paper's 1.20.1 plugin-manager entry point backed by Meldrite's live Bukkit
 * services.  The class intentionally keeps Leaf/Paper's public name and the
 * reflective state shape used by modern plugin tooling while delegating
 * lifecycle and event operations to the profile's authoritative manager.
 */
public final class PaperPluginManagerImpl implements PluginManager, io.papermc.paper.plugin.provider.entrypoint.DependencyContext {
    /** Paper exposes these fields on the manager instance for reflective API access. */
    public final InstanceManager instanceManager;
    public final PaperEventManager paperEventManager;
    public volatile PermissionManager permissionManager;

    private final Server server;
    private final PluginManager delegate;
    private final PermissionStore permissions = new PermissionStore();

    public PaperPluginManagerImpl(Server server, CommandMap commandMap, SimplePluginManager ignored) {
        this(server, commandMap, server == null ? null : server.getPluginManager());
    }

    /** Meldrite constructor: accepts the live profile manager, including its non-Simple implementation. */
    public PaperPluginManagerImpl(Server server, CommandMap ignored, PluginManager delegate) {
        this.server = java.util.Objects.requireNonNull(server, "server");
        this.delegate = java.util.Objects.requireNonNull(delegate, "delegate");
        this.instanceManager = new InstanceManager();
        this.paperEventManager = new PaperEventManager();
        this.permissionManager = permissions;
        refresh();
    }

    /** Paper plugin manager entry point. */
    public static PaperPluginManagerImpl getInstance() {
        Server current = Bukkit.getServer();
        if (current == null) throw new IllegalStateException("Bukkit server is not bound");
        try {
            Field field = current.getClass().getField("paperPluginManager");
            Object value = field.get(current);
            if (value instanceof PaperPluginManagerImpl manager) return manager;
            throw new IllegalStateException("CraftServer.paperPluginManager has unexpected type: " + value);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("CraftServer.paperPluginManager is unavailable", failure);
        }
    }

    private void refresh() {
        Plugin[] current;
        try {
            current = delegate.getPlugins();
        } catch (RuntimeException unsupported) {
            current = new Plugin[0];
        }
        instanceManager.plugins.clear();
        instanceManager.lookupNames.clear();
        for (Plugin plugin : current) {
            if (plugin == null) continue;
            instanceManager.plugins.add(plugin);
            instanceManager.lookupNames.put(normalize(plugin.getName()), plugin);
            if (plugin.getDescription() != null && plugin.getDescription().getProvides() != null) {
                for (String provided : plugin.getDescription().getProvides()) {
                    instanceManager.lookupNames.putIfAbsent(normalize(provided), plugin);
                }
            }
        }
        instanceManager.rebuildGraph();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace(' ', '_').toLowerCase(Locale.ENGLISH);
    }

    @Override public Plugin getPlugin(String name) { refresh(); return name == null ? null : instanceManager.lookupNames.get(normalize(name)); }
    @Override public Plugin[] getPlugins() { refresh(); return instanceManager.plugins.toArray(Plugin[]::new); }
    @Override public boolean isPluginEnabled(String name) { Plugin plugin = getPlugin(name); return plugin != null && isPluginEnabled(plugin); }
    @Override public boolean isPluginEnabled(Plugin plugin) {
        return plugin != null && plugin.isEnabled();
    }
    @Override public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return delegate.loadPlugin(file);
    }
    @Override public Plugin[] loadPlugins(File directory) { return delegate.loadPlugins(directory); }
    @Override public void disablePlugins() { delegate.disablePlugins(); refresh(); }
    @Override public synchronized void clearPlugins() {
        try { delegate.clearPlugins(); }
        finally {
            // A lifecycle precondition rejection retains the live generation.
            // Completed cleanup still removes registrations when onDisable failed.
            if (delegate.getPlugins().length == 0) {
                permissions.clearPermissions();
                paperEventManager.clearEvents();
            }
            refresh();
        }
    }
    @Override public void enablePlugin(Plugin plugin) { delegate.enablePlugin(plugin); refresh(); }
    @Override public void disablePlugin(Plugin plugin) { delegate.disablePlugin(plugin); refresh(); }
    @Override public void callEvent(Event event) { delegate.callEvent(event); }
    @Override public void registerEvents(Listener listener, Plugin plugin) { delegate.registerEvents(listener, plugin); }
    @Override public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
                                        EventExecutor executor, Plugin plugin) {
        delegate.registerEvent(event, listener, priority, executor, plugin);
    }
    @Override public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
                                        EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
        delegate.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
    }
    @Override public void registerInterface(Class<? extends PluginLoader> loader) { delegate.registerInterface(loader); }

    @Override public Permission getPermission(String name) {
        Permission result = permissions.getPermission(name);
        if (result != null) return result;
        try { return delegate.getPermission(name); } catch (RuntimeException ignored) { return null; }
    }
    @Override public void addPermission(Permission permission) { permissions.addPermission(permission); }
    @Override public void removePermission(Permission permission) { permissions.removePermission(permission); }
    @Override public void removePermission(String name) { permissions.removePermission(name); }
    @Override public Set<Permission> getDefaultPermissions(boolean op) { return permissions.getDefaultPermissions(op); }
    @Override public void recalculatePermissionDefaults(Permission permission) { permissions.recalculatePermissionDefaults(permission); }
    @Override public void subscribeToPermission(String permission, Permissible permissible) { permissions.subscribeToPermission(permission, permissible); }
    @Override public void unsubscribeFromPermission(String permission, Permissible permissible) { permissions.unsubscribeFromPermission(permission, permissible); }
    @Override public Set<Permissible> getPermissionSubscriptions(String permission) { return permissions.getPermissionSubscriptions(permission); }
    @Override public void subscribeToDefaultPerms(boolean op, Permissible permissible) { permissions.subscribeToDefaultPerms(op, permissible); }
    @Override public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) { permissions.unsubscribeFromDefaultPerms(op, permissible); }
    @Override public Set<Permissible> getDefaultPermSubscriptions(boolean op) { return permissions.getDefaultPermSubscriptions(op); }
    @Override public Set<Permission> getPermissions() { return permissions.getPermissions(); }
    @Override public void addPermissions(List<Permission> values) { if (values != null) values.forEach(this::addPermission); }
    @Override public void clearPermissions() { permissions.clearPermissions(); }
    @Override public void overridePermissionManager(Plugin plugin, PermissionManager replacement) {
        permissionManager = replacement == null ? permissions : replacement;
    }
    @Override public boolean useTimings() { return false; }

    @Override public boolean isTransitiveDependency(PluginMeta pluginMeta, PluginMeta dependencyConfig) {
        if (pluginMeta == null || dependencyConfig == null) return false;
        String target = dependencyConfig.getName();
        return pluginMeta.getPluginDependencies().stream().anyMatch(target::equalsIgnoreCase)
                || pluginMeta.getPluginSoftDependencies().stream().anyMatch(target::equalsIgnoreCase);
    }
    @Override public boolean hasDependency(String pluginIdentifier) { return getPlugin(pluginIdentifier) != null; }

    /** Paper's provider API exposes this graph for diagnostics and cleanup. */
    public MutableGraph<String> getInstanceManagerGraph() { refresh(); return instanceManager.dependencyGraph; }

    /** Live view matching PaperPluginInstanceManager's reflective field names. */
    public final class InstanceManager {
        public final Map<String, Plugin> lookupNames = new LinkedHashMap<>();
        public final List<Plugin> plugins = new ArrayList<>();
        public volatile MutableGraph<String> dependencyGraph = GraphBuilder.directed().allowsSelfLoops(false).build();

        /** Paper's reflective manager contract. */
        public Plugin getPlugin(String name) {
            return name == null ? null : lookupNames.get(normalize(name));
        }

        public Plugin[] getPlugins() { return plugins.toArray(Plugin[]::new); }

        public boolean isPluginEnabled(String name) {
            return isPluginEnabled(getPlugin(name));
        }

        public synchronized boolean isPluginEnabled(Plugin plugin) {
            return plugin != null && plugins.contains(plugin) && plugin.isEnabled();
        }

        public synchronized void loadPlugin(Plugin plugin) {
            if (plugin == null || plugins.contains(plugin)) return;
            plugins.add(plugin);
            lookupNames.put(normalize(plugin.getName()), plugin);
            if (plugin.getDescription() != null && plugin.getDescription().getProvides() != null) {
                for (String provided : plugin.getDescription().getProvides()) lookupNames.putIfAbsent(normalize(provided), plugin);
            }
            rebuildGraph();
        }

        public Plugin loadPlugin(Path path) throws InvalidPluginException, UnknownDependencyException {
            try {
                return PaperPluginManagerImpl.this.loadPlugin(path.toFile());
            } catch (InvalidDescriptionException invalid) {
                throw new InvalidPluginException(invalid);
            }
        }

        public Plugin[] loadPlugins(Path directory) {
            return PaperPluginManagerImpl.this.loadPlugins(directory.toFile());
        }

        public void disablePlugins() { PaperPluginManagerImpl.this.disablePlugins(); }

        public synchronized void clearPlugins() { PaperPluginManagerImpl.this.clearPlugins(); }

        public void enablePlugin(Plugin plugin) { PaperPluginManagerImpl.this.enablePlugin(plugin); }

        public void disablePlugin(Plugin plugin) { PaperPluginManagerImpl.this.disablePlugin(plugin); }

        public boolean isTransitiveDepend(PluginMeta plugin, PluginMeta depend) {
            return PaperPluginManagerImpl.this.isTransitiveDependency(plugin, depend);
        }

        public boolean hasDependency(String pluginIdentifier) {
            return PaperPluginManagerImpl.this.hasDependency(pluginIdentifier);
        }

        public MutableGraph<String> getDependencyGraph() { refresh(); return dependencyGraph; }

        private void rebuildGraph() {
            MutableGraph<String> graph = GraphBuilder.directed().allowsSelfLoops(false).build();
            for (Plugin plugin : plugins) {
                String name = plugin.getName(); graph.addNode(name);
                if (plugin.getDescription() == null) continue;
                for (String dependency : plugin.getDescription().getDepend()) {
                    graph.putEdge(dependency, name);
                }
            }
            dependencyGraph = graph;
        }
    }

    /** Adapter exposed under Paper's field name for BukkitPluginLoader/reflective callers. */
    public final class PaperEventManager {
        public Map<Class<? extends Event>, Set<org.bukkit.plugin.RegisteredListener>> createRegisteredListeners(
                Listener listener, Plugin plugin) {
            return EventDispatcherAdapter.createRegisteredListeners(listener, plugin);
        }
        public void registerEvents(Listener listener, Plugin plugin) { delegate.registerEvents(listener, plugin); }
        public void registerEvent(Class<? extends Event> event, Listener listener, EventPriority priority,
                                   EventExecutor executor, Plugin plugin, boolean ignoreCancelled) {
            delegate.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
        }
        public void callEvent(Event event) { delegate.callEvent(event); }
        public void clearEvents() { }
    }

    private static final class PermissionStore implements PermissionManager {
        private final Map<String, Permission> permissions = new ConcurrentHashMap<>();
        private final Map<Boolean, Set<Permission>> defaults = Map.of(true, ConcurrentHashMap.newKeySet(), false, ConcurrentHashMap.newKeySet());
        private final Map<String, Set<Permissible>> subscriptions = new ConcurrentHashMap<>();
        private final Map<Boolean, Set<Permissible>> defaultSubscriptions = Map.of(true, ConcurrentHashMap.newKeySet(), false, ConcurrentHashMap.newKeySet());

        @Override public Permission getPermission(String name) { return name == null ? null : permissions.get(name.toLowerCase(Locale.ROOT)); }
        @Override public void addPermission(Permission permission) {
            if (permission == null) throw new IllegalArgumentException("permission");
            String name = permission.getName().toLowerCase(Locale.ROOT);
            if (permissions.putIfAbsent(name, permission) != null) throw new IllegalArgumentException("Permission " + name + " is already defined");
            updateDefaults(permission);
            refreshPermissibles(name);
        }
        @Override public void removePermission(Permission permission) { if (permission != null) removePermission(permission.getName()); }
        @Override public void removePermission(String name) {
            if (name == null) return;
            String key = name.toLowerCase(Locale.ROOT);
            Permission removed = permissions.remove(key);
            if (removed == null) return;
            defaults.values().forEach(values -> values.remove(removed));
            refreshPermissibles(key);
        }
        @Override public Set<Permission> getDefaultPermissions(boolean op) { return Collections.unmodifiableSet(new LinkedHashSet<>(defaults.get(op))); }
        @Override public void recalculatePermissionDefaults(Permission permission) {
            if (permission != null && getPermission(permission.getName()) == permission) {
                updateDefaults(permission);
                refreshPermissibles(permission.getName());
            }
        }
        private void updateDefaults(Permission permission) {
            for (boolean op : new boolean[]{false, true}) {
                Set<Permission> values = defaults.get(op);
                if (permission.getDefault().getValue(op)) values.add(permission);
                else values.remove(permission);
            }
        }
        private void refreshPermissibles(String name) {
            Set<Permissible> affected = new LinkedHashSet<>(getPermissionSubscriptions(name));
            affected.addAll(getDefaultPermSubscriptions(false));
            affected.addAll(getDefaultPermSubscriptions(true));
            affected.forEach(Permissible::recalculatePermissions);
        }
        @Override public void subscribeToPermission(String permission, Permissible permissible) { if (permission != null && permissible != null) subscriptions.computeIfAbsent(permission.toLowerCase(Locale.ROOT), key -> ConcurrentHashMap.newKeySet()).add(permissible); }
        @Override public void unsubscribeFromPermission(String permission, Permissible permissible) { Set<Permissible> values = permission == null ? null : subscriptions.get(permission.toLowerCase(Locale.ROOT)); if (values != null) values.remove(permissible); }
        @Override public Set<Permissible> getPermissionSubscriptions(String permission) { Set<Permissible> values = permission == null ? null : subscriptions.get(permission.toLowerCase(Locale.ROOT)); return values == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(values)); }
        @Override public void subscribeToDefaultPerms(boolean op, Permissible permissible) { if (permissible != null) defaultSubscriptions.get(op).add(permissible); }
        @Override public void unsubscribeFromDefaultPerms(boolean op, Permissible permissible) { defaultSubscriptions.get(op).remove(permissible); }
        @Override public Set<Permissible> getDefaultPermSubscriptions(boolean op) { return Collections.unmodifiableSet(new LinkedHashSet<>(defaultSubscriptions.get(op))); }
        @Override public Set<Permission> getPermissions() { return Collections.unmodifiableSet(new LinkedHashSet<>(permissions.values())); }
        @Override public void addPermissions(List<Permission> values) { if (values != null) values.forEach(this::addPermission); }
        @Override public void clearPermissions() { permissions.clear(); defaults.values().forEach(Set::clear); subscriptions.clear(); defaultSubscriptions.values().forEach(Set::clear); }
    }
}
