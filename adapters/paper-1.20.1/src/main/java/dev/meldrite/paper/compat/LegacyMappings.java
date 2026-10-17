package dev.meldrite.paper.compat;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.Opcodes;

/** Verified 1.20.1 Spigot/Mojang members, expressed in Fabric intermediary names. */
public final class LegacyMappings {
    public interface RuntimeNamespace {
        String className(String intermediaryName);
        String intermediaryClassName(String runtimeName);
        String fieldName(String intermediaryOwner, String intermediaryName, String intermediaryDescriptor);
        String methodName(String intermediaryOwner, String intermediaryName, String intermediaryDescriptor);
    }

    private record Symbol(boolean method, String owner, String name, String descriptor) {}
    private record Target(String owner, String name, int access) {}
    private record LineageKey(String owner, boolean declared) {}
    private static final String RESOURCE = "/meldrite/mappings/1.20.1/legacy-names.bin.gz";
    private static final Pattern INTERMEDIARY_CLASS =
            Pattern.compile("net/minecraft/(?:server/)?class_[0-9]+(?:\\$class_[0-9]+)*");
    private static final LegacyMappings INSTANCE = read();
    private static volatile RuntimeNamespace runtime = new RuntimeNamespace() {
        @Override public String className(String name) { return name; }
        @Override public String intermediaryClassName(String name) { return name; }
        @Override public String fieldName(String owner, String name, String descriptor) { return name; }
        @Override public String methodName(String owner, String name, String descriptor) { return name; }
    };
    private final Map<String, String> classes;
    private final Map<String, List<String>> parents;
    private final Map<Symbol, Target> members;
    private final Map<String, Map<String, List<String>>> pluginParents = new ConcurrentHashMap<>();
    private final Map<String, Map<String, java.util.Set<String>>> pluginChildren = new ConcurrentHashMap<>();
    private final Map<String, java.util.Set<String>> pluginInterfaces = new ConcurrentHashMap<>();
    // Group declarations by class so lazy class loads never scan every member of a plugin.
    private final Map<String, Map<String, Map<Symbol, Integer>>> pluginDeclarations = new ConcurrentHashMap<>();
    private final Map<String, Map<Symbol, List<Target>>> pluginContracts = new ConcurrentHashMap<>();
    private final Map<String, Map<LineageKey, List<String>>> pluginLineages = new ConcurrentHashMap<>();
    /** Interfaces with at least one implementation that inherits a native class. */
    private final Map<String, java.util.Set<String>> pluginNativeContracts = new ConcurrentHashMap<>();
    private final Map<String, List<Map.Entry<Symbol, Target>>> reflectionIndex;

    private LegacyMappings(Map<String, String> classes, Map<String, List<String>> parents, Map<Symbol, Target> members) {
        this.classes = Map.copyOf(classes);
        this.parents = Map.copyOf(parents);
        this.members = Map.copyOf(members);
        Map<String, List<Map.Entry<Symbol, Target>>> index = new HashMap<>();
        members.forEach((symbol, target) -> index.computeIfAbsent(reflectionKey(symbol.method, symbol.owner, symbol.name),
                ignored -> new ArrayList<>()).add(Map.entry(symbol, target)));
        index.replaceAll((key, values) -> List.copyOf(values));
        reflectionIndex = Map.copyOf(index);
    }

    public static void bindRuntime(RuntimeNamespace namespace) {
        runtime = Objects.requireNonNull(namespace);
        INSTANCE.pluginLineages.clear();
        INSTANCE.pluginNativeContracts.clear();
        INSTANCE.pluginContracts.clear();
    }

    public static boolean isSubclass(String plugin, String owner, String parent) {
        return INSTANCE.lineage(plugin, owner, false).contains(INSTANCE.canonicalClass(parent));
    }

    static void clearPlugin(String plugin) {
        INSTANCE.pluginParents.remove(plugin);
        INSTANCE.pluginChildren.remove(plugin);
        INSTANCE.pluginInterfaces.remove(plugin);
        INSTANCE.pluginDeclarations.remove(plugin);
        INSTANCE.pluginContracts.remove(plugin);
        INSTANCE.pluginLineages.remove(plugin);
        INSTANCE.pluginNativeContracts.remove(plugin);
    }

    static void replaceDeclarations(String plugin, String owner) {
        Map<String, Map<Symbol, Integer>> declarations = INSTANCE.pluginDeclarations.get(plugin);
        if (declarations != null) declarations.remove(owner);
    }

    public static String className(String internalName) {
        String target = INSTANCE.classes.get(internalName);
        if (target != null || INSTANCE.parents.containsKey(internalName)) {
            return runtime.className(target == null ? internalName : target);
        }
        return internalName;
    }

    public static String reflectionClassName(String name) {
        if (name.startsWith("[")) {
            if (!name.matches("\\[+(?:L[^;]+;|[ZBCSIJFD])")) return name;
            return new Remapper() {
                @Override public String map(String internalName) { return className(internalName); }
            }.mapDesc(name.replace('.', '/')).replace('/', '.');
        }
        String internal = name.replace('.', '/');
        String mapped = className(internal);
        return mapped.equals(internal) ? name : mapped.replace('/', '.');
    }

    private static final class AmbiguousReflectionNames {
        static final java.util.Set<String> CLASSES = read();
        private static java.util.Set<String> read() {
            String resource = "/meldrite/mappings/1.20.1/reflection-names.tiny.gz";
            try (var input = LegacyMappings.class.getResourceAsStream(resource)) {
                if (input == null) throw new IOException("REFLECTION_MAPPINGS_MISSING: " + resource);
                try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                        new GZIPInputStream(input), java.nio.charset.StandardCharsets.UTF_8))) {
                    var result = new HashSet<String>();
                    for (String line; (line = reader.readLine()) != null;) {
                        String[] columns = line.split("\t", -1);
                        if (columns.length == 3 && columns[0].equals("c")) {
                            String legacy = INSTANCE.classes.get(columns[2]);
                            if (legacy != null && !legacy.equals(columns[1])) result.add(columns[2]);
                        }
                    }
                    return java.util.Set.copyOf(result);
                }
            } catch (IOException failure) { throw new ExceptionInInitializerError(failure); }
        }
    }

    static boolean isAmbiguousReflectionClass(String text) {
        String name = text.replace('.', '/');
        if (name.startsWith("[")) {
            int type = name.lastIndexOf('[') + 1;
            if (type >= name.length() || name.charAt(type) != 'L' || !name.endsWith(";")) return false;
            name = name.substring(type + 1, name.length() - 1);
        }
        return AmbiguousReflectionNames.CLASSES.contains(name);
    }

    /**
     * Paper's reobf.tiny protocol calls its server-runtime column "spigot".
     * Export the real active Fabric symbols in that column, without changing
     * reflection objects or inferring which input namespace a caller intended.
     */
    public static void writePaperReflectionMappings(java.io.OutputStream destination) throws IOException {
        RuntimeNamespace namespace = runtime;
        String resource = "/meldrite/mappings/1.20.1/reflection-names.tiny.gz";
        try (var input = LegacyMappings.class.getResourceAsStream(resource)) {
            if (input == null) throw new IOException("REFLECTION_MAPPINGS_MISSING: " + resource);
            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(
                    new GZIPInputStream(input), java.nio.charset.StandardCharsets.UTF_8))) {
                if (!"tiny\t2\t0\tintermediary\tmojang".equals(reader.readLine()))
                    throw new IOException("REFLECTION_MAPPINGS_FORMAT_INVALID");
                List<String[]> rows = reader.lines().map(line -> line.split("\t", -1)).toList();
                Map<String, String> readableClasses = new HashMap<>();
                for (String[] row : rows) if (row.length == 3 && row[0].equals("c"))
                    readableClasses.put(row[1], row[2]);
                Remapper readable = new Remapper() {
                    @Override public String map(String name) { return readableClasses.getOrDefault(name, name); }
                };
                StringBuilder tiny = new StringBuilder("tiny\t2\t0\tmojang\tspigot\n");
                String owner = null;
                for (String[] columns : rows) {
                    if (columns.length == 3 && columns[0].equals("c")) {
                        owner = columns[1];
                        tiny.append("c\t").append(columns[2]).append('\t').append(namespace.className(owner)).append('\n');
                    } else if (owner != null && columns.length == 5 && columns[0].isEmpty()
                            && (columns[1].equals("f") || columns[1].equals("m"))) {
                        String name = columns[3];
                        boolean method = columns[1].equals("m");
                        String mapped = name.startsWith("<") ? name : method
                                ? namespace.methodName(owner, name, columns[2])
                                : namespace.fieldName(owner, name, columns[2]);
                        String descriptor = method ? readable.mapMethodDesc(columns[2]) : readable.mapDesc(columns[2]);
                        tiny.append('\t').append(columns[1]).append('\t').append(descriptor).append('\t')
                                .append(columns[4]).append('\t').append(mapped).append('\n');
                    } else throw new IOException("REFLECTION_MAPPINGS_ENTRY_INVALID");
                }
                destination.write(tiny.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        }
    }

    static void registerHierarchy(String plugin, String owner, String superclass, String[] interfaces, int access) {
        var inheritance = new ArrayList<String>();
        if (superclass != null) inheritance.add(superclass);
        inheritance.addAll(List.of(interfaces));
        var local = INSTANCE.pluginParents.computeIfAbsent(plugin, ignored -> new ConcurrentHashMap<>());
        List<String> previous = local.put(owner, List.copyOf(inheritance));
        var children = INSTANCE.pluginChildren.computeIfAbsent(plugin, ignored -> new ConcurrentHashMap<>());
        if (previous != null && !previous.equals(inheritance)) {
            for (String parent : previous) {
                var descendants = children.get(parent);
                if (descendants != null) {
                    descendants.remove(owner);
                    if (descendants.isEmpty()) children.remove(parent, descendants);
                }
            }
        }
        for (String parent : inheritance) children.computeIfAbsent(parent, ignored -> ConcurrentHashMap.newKeySet()).add(owner);
        var contracts = INSTANCE.pluginInterfaces.computeIfAbsent(plugin, ignored -> ConcurrentHashMap.newKeySet());
        boolean wasInterface = contracts.contains(owner);
        boolean isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
        if (isInterface) contracts.add(owner); else contracts.remove(owner);
        if (previous == null || !previous.equals(inheritance) || wasInterface != isInterface) {
            INSTANCE.pluginContracts.remove(plugin);
            INSTANCE.pluginLineages.remove(plugin);
            INSTANCE.pluginNativeContracts.remove(plugin);
        }
    }

    static void registerDeclaration(String plugin, boolean method, String owner, String name, String descriptor, int access) {
        INSTANCE.pluginDeclarations.computeIfAbsent(plugin, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(owner, ignored -> new ConcurrentHashMap<>())
                .put(new Symbol(method, owner, name, descriptor), access);
    }

    static String fieldName(String plugin, String owner, String name, String descriptor) {
        return INSTANCE.memberName(plugin, false, owner, name, descriptor);
    }

    static String methodName(String plugin, String owner, String name, String descriptor) {
        if (name.startsWith("<")) return name;
        return INSTANCE.memberName(plugin, true, owner, name, descriptor);
    }

    static List<String> reflectionMemberNames(Class<?> owner, String name, boolean method, String parameterDescriptor, boolean declared) {
        String canonical = INSTANCE.canonicalClass(owner.getName().replace('.', '/'));
        var names = new ArrayList<String>();
        List<String> inheritance = INSTANCE.lineage("", canonical, declared);
        if (!INSTANCE.parents.containsKey(canonical)) {
            // Plugin overrides have already been renamed. Resolve their native
            // signature through the real class hierarchy; the reflection helper
            // still enforces getDeclaredMethod/getMethod visibility itself.
            var pending = new ArrayDeque<Class<?>>();
            var visited = new HashSet<Class<?>>();
            pending.add(owner);
            var nativeAncestors = new ArrayList<String>();
            while (!pending.isEmpty()) {
                Class<?> next = pending.removeFirst();
                if (!visited.add(next)) continue;
                String nextName = INSTANCE.canonicalClass(next.getName().replace('.', '/'));
                if (INSTANCE.parents.containsKey(nextName)) nativeAncestors.add(nextName);
                else if (method && (!declared || next == owner)) {
                    for (var declaration : next.getDeclaredMethods()) {
                        String descriptor = canonicalDescriptor(org.objectweb.asm.Type.getMethodDescriptor(declaration));
                        if (parameterDescriptor != null && !descriptor.startsWith(parameterDescriptor)) continue;
                        Symbol symbol = new Symbol(true, nextName, name, descriptor);
                        INSTANCE.pluginDeclarations.forEach((plugin, owners) -> {
                            if (owners.getOrDefault(nextName, Map.of()).containsKey(symbol)) {
                                String mapped = INSTANCE.memberName(plugin, true, nextName, name, descriptor);
                                if (!mapped.equals(name) && !names.contains(mapped)) names.add(mapped);
                            }
                        });
                    }
                }
                if (next.getSuperclass() != null) pending.add(next.getSuperclass());
                pending.addAll(List.of(next.getInterfaces()));
            }
            inheritance = nativeAncestors;
        }
        for (String ancestor : inheritance) {
            for (var entry : INSTANCE.reflectionIndex.getOrDefault(reflectionKey(method, ancestor, name), List.of())) {
                Symbol key = entry.getKey();
                if (parameterDescriptor == null || key.descriptor.startsWith(parameterDescriptor)) {
                    Target target = entry.getValue();
                    String mapped = method ? runtime.methodName(target.owner, target.name, key.descriptor)
                            : runtime.fieldName(target.owner, target.name, key.descriptor);
                    if (!names.contains(mapped)) names.add(mapped);
                }
            }
            if (!names.isEmpty()) break;
        }
        names.sort(String::compareTo);
        return List.copyOf(names);
    }

    static String canonicalDescriptor(String descriptor) {
        Remapper remapper = new Remapper() {
            @Override public String map(String name) { return INSTANCE.canonicalClass(name); }
        };
        return descriptor.startsWith("(") ? remapper.mapMethodDesc(descriptor) : remapper.mapDesc(descriptor);
    }

    private String memberName(String plugin, boolean method, String owner, String name, String descriptor) {
        String canonicalDescriptor = canonicalDescriptor(descriptor);
        Map<String, Map<Symbol, Integer>> declarations = pluginDeclarations.getOrDefault(plugin, Map.of());
        for (String ancestor : lineage(plugin, owner, false)) {
            Symbol symbol = new Symbol(method, ancestor, name, canonicalDescriptor);
            Integer declaration = declarations.getOrDefault(ancestor, Map.of()).get(symbol);
            if (declaration != null) {
                if (!method || (declaration & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) != 0) return name;
                return overrideName(plugin, ancestor, name, canonicalDescriptor);
            }
            Target target = members.get(symbol);
            if (target != null) {
                return method ? runtime.methodName(target.owner, target.name, canonicalDescriptor)
                        : runtime.fieldName(target.owner, target.name, canonicalDescriptor);
            }
        }
        return name;
    }

    private String overrideName(String plugin, String owner, String name, String descriptor) {
        Target direct = overrideTarget(plugin, owner, name, descriptor);
        if (direct != null) return runtime.methodName(direct.owner, direct.name, descriptor);
        var targets = new HashSet<Target>();
        var interfaces = pluginInterfaces.getOrDefault(plugin, java.util.Set.of());
        for (String ancestor : lineage(plugin, owner, false)) {
            if (interfaces.contains(ancestor)) targets.addAll(interfaceTargets(plugin, ancestor, name, descriptor));
        }
        if (targets.isEmpty()) return name;
        String mapped = null;
        for (Target target : targets) {
            String candidate = runtime.methodName(target.owner, target.name, descriptor);
            if (mapped != null && !mapped.equals(candidate)) {
                throw new IllegalStateException("Conflicting native names for plugin interface method " + owner + "." + name + descriptor);
            }
            mapped = candidate;
        }
        return mapped;
    }

    private Target overrideTarget(String plugin, String owner, String name, String descriptor) {
        for (String ancestor : lineage(plugin, owner, false)) {
            Target target = members.get(new Symbol(true, ancestor, name, descriptor));
            if (target != null && (target.access & (Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC)) == 0
                    && (target.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) {
                return target;
            }
        }
        return null;
    }

    private List<Target> interfaceTargets(String plugin, String owner, String name, String descriptor) {
        // A library can contain thousands of unrelated interfaces. Only an
        // interface implemented below a native class can contribute a legacy
        // contract. Checking this cached
        // fact avoids walking the entire descendant graph for every ordinary
        // Java method while preserving propagation for genuine native bridges.
        if (!nativeContractInterfaces(plugin).contains(owner)) return List.of();
        return pluginContracts.computeIfAbsent(plugin, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(new Symbol(true, owner, name, descriptor), ignored -> {
                    var result = new HashSet<Target>();
                    var pending = new ArrayDeque<String>();
                    var visited = new HashSet<String>();
                    var children = pluginChildren.getOrDefault(plugin, Map.of());
                    pending.add(owner);
                    while (!pending.isEmpty()) {
                        String next = pending.removeFirst();
                        if (!visited.add(next)) continue;
                        Target target = overrideTarget(plugin, next, name, descriptor);
                        if (target != null) result.add(target);
                        pending.addAll(children.getOrDefault(next, java.util.Set.of()));
                    }
                    return List.copyOf(result);
                });
    }

    private static String reflectionKey(boolean method, String owner, String name) {
        return (method ? "M" : "F") + owner + ":" + name;
    }

    private String canonicalClass(String name) {
        String alias = classes.get(name);
        if (alias != null) return alias;
        // Intermediary names are already canonical. Calling unmapClassName on
        // them would turn them back into official names and break descriptors.
        if (INTERMEDIARY_CLASS.matcher(name).matches()) return name;
        return runtime.intermediaryClassName(name);
    }

    private List<String> lineage(String plugin, String owner, boolean declared) {
        Map<LineageKey, List<String>> cache = pluginLineages.computeIfAbsent(plugin,
                ignored -> new ConcurrentHashMap<>());
        return cache.computeIfAbsent(new LineageKey(owner, declared), ignored -> computeLineage(plugin, owner, declared));
    }

    private List<String> computeLineage(String plugin, String owner, boolean declared) {
        var result = new ArrayList<String>();
        var pending = new ArrayDeque<String>();
        var visited = new HashSet<String>();
        pending.add(owner);
        Map<String, List<String>> local = pluginParents.getOrDefault(plugin, Map.of());
        while (!pending.isEmpty()) {
            String source = pending.removeFirst();
            String canonical = canonicalClass(source);
            if (!visited.add(canonical)) continue;
            result.add(canonical);
            if (!declared) {
                pending.addAll(local.getOrDefault(source, List.of()));
                pending.addAll(parents.getOrDefault(canonical, List.of()));
            }
        }
        return List.copyOf(result);
    }

    private java.util.Set<String> nativeContractInterfaces(String plugin) {
        return pluginNativeContracts.computeIfAbsent(plugin, INSTANCE::computeNativeContractInterfaces);
    }

    private java.util.Set<String> computeNativeContractInterfaces(String plugin) {
        var nativeOwners = new HashSet<String>();
        var local = pluginParents.getOrDefault(plugin, Map.of());
        // Find plugin classes that inherit a real native class. This fixed
        // point follows only the declared superclass/interface edges; it does
        // not scan every descendant of every interface.
        boolean changed;
        do {
            changed = false;
            for (var entry : local.entrySet()) {
                if (nativeOwners.contains(entry.getKey())) continue;
                for (String parent : entry.getValue()) {
                    String canonical = canonicalClass(parent);
                    if (parents.containsKey(canonical) || nativeOwners.contains(parent) || nativeOwners.contains(canonical)) {
                        if (nativeOwners.add(entry.getKey())) changed = true;
                        break;
                    }
                }
            }
        } while (changed);
        var result = new HashSet<String>();
        var interfaces = pluginInterfaces.getOrDefault(plugin, java.util.Set.of());
        for (String nativeOwner : nativeOwners) {
            for (String ancestor : lineage(plugin, nativeOwner, false)) {
                if (interfaces.contains(ancestor)) result.add(ancestor);
            }
        }
        return java.util.Set.copyOf(result);
    }

    private static LegacyMappings read() {
        try (var resource = LegacyMappings.class.getResourceAsStream(RESOURCE)) {
            if (resource == null) throw new IllegalStateException("Missing version mapping resource " + RESOURCE);
            try (var input = new DataInputStream(new GZIPInputStream(resource))) {
                if (input.readInt() != 0x4D4C4D33 || !input.readUTF().equals("1.20.1")) {
                    throw new IOException("Unsupported legacy mapping format");
                }
                Map<String, String> classes = new HashMap<>();
                for (int count = input.readInt(); count > 0; count--) classes.put(input.readUTF(), input.readUTF());
                Map<String, List<String>> parents = new HashMap<>();
                for (int count = input.readInt(); count > 0; count--) {
                    String name = input.readUTF();
                    var inheritance = new ArrayList<String>();
                    for (int parentCount = input.readInt(); parentCount > 0; parentCount--) inheritance.add(input.readUTF());
                    parents.put(name, List.copyOf(inheritance));
                }
                Map<Symbol, Target> members = new HashMap<>();
                for (int count = input.readInt(); count > 0; count--) {
                    var symbol = new Symbol(input.readBoolean(), input.readUTF(), input.readUTF(), input.readUTF());
                    members.put(symbol, new Target(input.readUTF(), input.readUTF(), input.readInt()));
                }
                if (input.read() != -1) throw new IOException("Trailing legacy mapping bytes");
                return new LegacyMappings(classes, parents, members);
            }
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot read verified legacy mappings", failure);
        }
    }
}
