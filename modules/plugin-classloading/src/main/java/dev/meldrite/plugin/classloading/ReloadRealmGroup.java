package dev.meldrite.plugin.classloading;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Holds one reload's dependency definitions until disabling and all old callbacks finish. */
final class ReloadRealmGroup {
    final Map<String, PluginRealm> routes;
    private final Set<PluginRealm> members = new LinkedHashSet<>();
    private final Set<PluginRealm> pending = new LinkedHashSet<>();
    private int executions;
    private boolean prepared;

    ReloadRealmGroup(Map<String, PluginRealm> routes) {
        this.routes = Collections.unmodifiableMap(new LinkedHashMap<>(routes));
    }
    synchronized void add(PluginRealm realm, int active, boolean retired) {
        members.add(realm);
        executions += active;
        if (retired) pending.add(realm);
    }
    synchronized void retain() { executions++; }
    void release() throws IOException {
        List<PluginRealm> finish;
        synchronized (this) { executions--; finish = ready(); }
        closeResources(finish);
    }
    void closeWhenIdle(PluginRealm realm) throws IOException {
        List<PluginRealm> finish;
        synchronized (this) { pending.add(realm); finish = ready(); }
        closeResources(finish);
    }
    void prepared() throws IOException {
        List<PluginRealm> finish;
        synchronized (this) { prepared = true; finish = ready(); }
        closeResources(finish);
    }
    private List<PluginRealm> ready() {
        // Later onDisable callbacks may still need a previously disabled dependency.
        if (!prepared || executions != 0 || pending.size() != members.size()) return List.of();
        var finish = new ArrayList<>(pending);
        pending.clear();
        return finish;
    }
    private static void closeResources(List<PluginRealm> realms) throws IOException {
        IOException aggregate = null;
        Collections.reverse(realms);
        for (var realm : realms) try { realm.closeResources(); }
        catch (IOException | RuntimeException | Error failure) {
            if (aggregate == null) aggregate = new IOException("RELOAD_REALM_CLOSE_FAILED");
            aggregate.addSuppressed(failure);
        }
        if (aggregate != null) throw aggregate;
    }
}
