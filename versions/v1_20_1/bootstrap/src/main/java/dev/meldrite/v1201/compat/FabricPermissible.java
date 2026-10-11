package dev.meldrite.v1201.compat;

import java.util.*;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.command.ServerCommandSource;
import org.bukkit.Bukkit;
import org.bukkit.permissions.*;

/** Provider decisions take precedence; undefined nodes retain Bukkit's existing semantics. */
public final class FabricPermissible extends PermissibleBase {
    private final Supplier<ServerCommandSource> source;
    private final Permissible owner;
    public FabricPermissible(ServerOperator operator, Supplier<ServerCommandSource> source) {
        super(operator);
        this.owner = operator instanceof Permissible permissible ? permissible : this;
        this.source = Objects.requireNonNull(source);
    }
    private static String normalize(String name) {
        if (name == null) throw new IllegalArgumentException("Permission name cannot be null");
        return name.toLowerCase(Locale.ENGLISH);
    }
    private static Permission validate(Permission permission) {
        if (permission == null) throw new IllegalArgumentException("Permission cannot be null");
        return permission;
    }
    private TriState value(String name) {
        if (!FabricPermissionBridge.isAvailable()) return TriState.DEFAULT;
        var nativeSource = source.get();
        var direct = FabricPermissionBridge.query(nativeSource, name);
        if (direct != TriState.DEFAULT || nativeSource == null) return direct;
        var query = new Query(nativeSource, Bukkit.getPluginManager().getPermissions());
        query.direct.put(name, direct);
        return query.value(name);
    }
    private record Parent(String name, boolean edge) { }
    /** Results live only for one query/snapshot, so provider changes are immediately visible. */
    private static final class Query {
        final ServerCommandSource source;
        final Map<String, List<Parent>> parents = new HashMap<>();
        final Set<String> known = new HashSet<>(), path = new HashSet<>();
        final Map<String, TriState> direct = new HashMap<>(), resolved = new HashMap<>();
        int cutoffs;
        Query(ServerCommandSource source, Set<Permission> definitions) {
            this.source = source;
            for (var permission : definitions) {
                String parent = normalize(permission.getName());
                known.add(parent);
                permission.getChildren().forEach((child, edge) -> {
                    String node = normalize(child); known.add(node);
                    parents.computeIfAbsent(node, unused -> new ArrayList<>()).add(new Parent(parent, edge));
                });
            }
        }
        TriState value(String node) {
            if (source == null) return TriState.DEFAULT;
            var cached = resolved.get(node);
            if (cached != null) return cached;
            if (path.size() >= 512 || !path.add(node)) { cutoffs++; return TriState.DEFAULT; }
            int before = cutoffs;
            TriState result = TriState.DEFAULT;
            try {
                result = direct.computeIfAbsent(node, key -> FabricPermissionBridge.query(source, key));
                if (result == TriState.DEFAULT) for (var parent : parents.getOrDefault(node, List.of())) {
                    var value = value(parent.name());
                    if (value == TriState.DEFAULT) continue;
                    if ((value == TriState.TRUE) != parent.edge()) { result = TriState.FALSE; break; }
                    result = TriState.TRUE;
                }
                // A cycle/depth cutoff depends on this root's path, not on the permission itself.
                if (cutoffs == before) resolved.put(node, result);
                return result;
            } finally { path.remove(node); }
        }
    }
    @Override public boolean hasPermission(String name) {
        String node = normalize(name);
        var provided = value(node);
        if (provided != TriState.DEFAULT) return provided == TriState.TRUE;
        synchronized (this) { return super.hasPermission(node); }
    }
    @Override public boolean hasPermission(Permission permission) {
        validate(permission);
        var provided = value(normalize(permission.getName()));
        if (provided != TriState.DEFAULT) return provided == TriState.TRUE;
        synchronized (this) { return super.hasPermission(permission); }
    }
    @Override public boolean isPermissionSet(String name) {
        String node = normalize(name);
        if (value(node) != TriState.DEFAULT) return true;
        synchronized (this) { return super.isPermissionSet(node); }
    }
    @Override public boolean isPermissionSet(Permission permission) { return isPermissionSet(validate(permission).getName()); }
    @Override public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        if (!FabricPermissionBridge.isAvailable()) return super.getEffectivePermissions();
        var effective = new LinkedHashMap<String, PermissionAttachmentInfo>();
        for (var info : super.getEffectivePermissions()) effective.put(info.getPermission(), info);
        var query = new Query(source.get(), Bukkit.getPluginManager().getPermissions());
        var candidates = new HashSet<>(query.known); candidates.addAll(effective.keySet());
        for (String name : candidates) {
            var provided = query.value(name);
            if (provided != TriState.DEFAULT)
                effective.put(name, new PermissionAttachmentInfo(owner, name, null, provided == TriState.TRUE));
        }
        return new HashSet<>(effective.values());
    }
    /** Only registered Bukkit nodes/attachments are supplied as a Mod query's fallback. */
    public TriState bukkitValue(String name) {
        String node = normalize(name);
        boolean known;
        synchronized (this) { known = super.isPermissionSet(node); }
        if (!known) known = new Query(null, Bukkit.getPluginManager().getPermissions()).known.contains(node);
        if (!known) return TriState.DEFAULT;
        return TriState.of(hasPermission(node));
    }
}
