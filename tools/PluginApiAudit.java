import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/** Read-only bytecode caller inventory. No plugin-specific runtime compatibility rules. */
public final class PluginApiAudit {
    private record Indexed(ClassNode node, String source, String sha256, long modified, boolean local) {}
    private record Ref(String owner, String name, String descriptor, int opcode) {
        String symbol() { return owner + "." + name + descriptor; }
    }
    private record Decl(String owner, MethodNode method, Indexed indexed) {
        String symbol() { return owner + "." + method.name + method.desc; }
    }
    private record UnsupportedPath(String kind, List<String> path, List<String> messages) {}
    private final Map<String, Indexed> types = new TreeMap<>();
    private final Map<String, ClassNode> apiBaseline = new TreeMap<>();
    private final Map<String, List<String>> mappings = new TreeMap<>();
    private final Map<String, List<UnsupportedPath>> cache = new HashMap<>();
    private final List<Map<String, Object>> inputs = new ArrayList<>();
    private final List<Map<String, Object>> uses = new ArrayList<>();
    private final List<Map<String, Object>> reflection = new ArrayList<>();
    private final List<Map<String, Object>> staleSources = new ArrayList<>();
    private final Map<String, Map<String, Object>> findings = new TreeMap<>();
    private final String craft;
    private int classes, directCalls, methodHandles;

    private PluginApiAudit(String craft) { this.craft = craft; }

    public static void main(String[] args) throws Exception {
        if (args.length < 5) throw new IllegalArgumentException("Usage: PluginApiAudit <report.json> <craft package internal name> <class roots separated by path separator> <API jars separated by path separator> <plugin.jar...>");
        var audit = new PluginApiAudit(args[1]);
        audit.input(Path.of(ClassReader.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "ASM parser");
        for (String jar : args[3].split(Pattern.quote(File.pathSeparator))) if (!jar.isBlank()) audit.indexJar(Path.of(jar));
        for (String root : args[2].split(Pattern.quote(File.pathSeparator))) if (!root.isBlank()) audit.indexRoot(Path.of(root));
        audit.runtimeMappings();
        for (int i = 4; i < args.length; i++) audit.plugin(Path.of(args[i]));
        audit.write(Path.of(args[0]));
    }

    private void input(Path path, String role) throws Exception {
        var row = new LinkedHashMap<String, Object>();
        row.put("path", path.toAbsolutePath().toString()); row.put("role", role);
        row.put("modified", Files.getLastModifiedTime(path).toMillis());
        if (Files.isRegularFile(path)) row.put("sha256", hash(Files.readAllBytes(path)));
        inputs.add(row);
    }

    private void indexJar(Path path) throws Exception {
        input(path, "API");
        try (var jar = new JarFile(path.toFile())) {
            for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class") && !e.getName().startsWith("META-INF/versions/")).toList()) {
                byte[] bytes = jar.getInputStream(entry).readAllBytes();
                var node = node(bytes); apiBaseline.putIfAbsent(node.name, node); types.putIfAbsent(node.name, new Indexed(node, path.toString(), hash(bytes), entry.getTime(), false));
            }
        }
    }

    private void indexRoot(Path root) throws Exception {
        input(root, "compiled implementation");
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".class")).sorted().toList()) {
                byte[] bytes = Files.readAllBytes(file); var node = node(bytes);
                types.put(node.name, new Indexed(node, file.toString(), hash(bytes), Files.getLastModifiedTime(file).toMillis(), true));
                if (node.sourceFile != null && root.toString().replace('\\', '/').endsWith("/build/classes/java/main")) {
                    Path source = root.getParent().getParent().getParent().getParent().resolve("src/main/java").resolve(node.name.substring(0, Math.max(0, node.name.lastIndexOf('/') + 1))).resolve(node.sourceFile);
                    if (Files.isRegularFile(source) && Files.getLastModifiedTime(source).toMillis() > Files.getLastModifiedTime(file).toMillis()) {
                        staleSources.add(Map.of("class", node.name, "source", source.toString(), "sourceModified", Files.getLastModifiedTime(source).toMillis(), "classModified", Files.getLastModifiedTime(file).toMillis()));
                    }
                }
            }
        }
    }

    private void runtimeMappings() {
        map("org/bukkit/Server", "CraftServer");
        map("org/bukkit/World", "CraftWorld"); map("org/bukkit/RegionAccessor", "CraftWorld");
        map("org/bukkit/OfflinePlayer", "CraftOfflinePlayer", "entity/CraftPlayer");
        map("org/bukkit/entity/Player", "entity/CraftPlayer");
        map("org/bukkit/entity/HumanEntity", "entity/CraftHumanEntity", "entity/CraftPlayer");
        map("org/bukkit/entity/LivingEntity", "entity/CraftLivingEntity", "entity/CraftPlayer");
        map("org/bukkit/entity/Entity", "entity/CraftEntity", "entity/CraftPlayer");
        map("org/bukkit/command/CommandSender", "entity/CraftPlayer", "command/CraftConsoleCommandSender", "command/CraftEntityCommandSender");
        map("org/bukkit/inventory/Inventory", "inventory/CraftInventory", "inventory/CraftInventoryPlayer");
        map("org/bukkit/inventory/PlayerInventory", "inventory/CraftInventoryPlayer");
        map("org/bukkit/inventory/EntityEquipment", "inventory/CraftEntityEquipment", "inventory/CraftInventoryPlayer");
        map("org/bukkit/block/Block", "block/CraftBlock"); map("org/bukkit/block/BlockState", "block/CraftBlockState");
        map("org/bukkit/Chunk", "CraftChunk"); map("org/bukkit/WorldBorder", "CraftWorldBorder");
        // Discover additional concrete wrappers by their directly implemented API contracts.
        // Facade fallbacks are deliberately excluded where a concrete Craft wrapper exists.
        for (var indexed : types.values()) {
            var type = indexed.node;
            if (!indexed.local || (type.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0 || !type.name.startsWith("org/bukkit/craftbukkit/")) continue;
            for (String api : type.interfaces) if (api.startsWith("org/bukkit/") && !mappings.containsKey(api)) {
                mappings.computeIfAbsent(api, ignored -> new ArrayList<>()).add(type.name);
            }
        }
        // Nested Spigot APIs are concrete API classes with throwing defaults.
        // The runtime supplies owned anonymous subclasses, not those defaults.
        for (var indexed : types.values()) {
            var type = indexed.node;
            if (!indexed.local || (type.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0) continue;
            if (type.superName != null && type.superName.startsWith("org/bukkit/") &&
                    (type.superName.endsWith("$Spigot") || type.name.startsWith("dev/meldrite/v1201/compat/") && type.superName.startsWith("org/bukkit/event/"))) {
                mappings.computeIfAbsent(type.superName, ignored -> new ArrayList<>()).add(type.name);
            }
        }
        for (var indexed : types.values()) {
            var type = indexed.node;
            if (!indexed.local || (type.access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_INTERFACE)) != 0) continue;
            for (String contract : type.interfaces) if (contract.startsWith("dev/meldrite/bridge/")) {
                mappings.computeIfAbsent(contract, ignored -> new ArrayList<>()).add(type.name);
            }
        }
        // Resolve nested Spigot receivers from the selected facade's actual supplier.
        // A neutral adapter can also own a Spigot subclass, but an overridden Craft
        // spigot() never returns it. Anonymous class numbers have no meaning here.
        for (String contract : new ArrayList<>(mappings.keySet())) {
            if (!contract.endsWith("$Spigot")) continue;
            String outerContract = contract.substring(0, contract.length() - "$Spigot".length());
            var selected = new TreeSet<String>();
            for (String facade : mappings.getOrDefault(outerContract, List.of())) {
                Decl supplier = resolve(facade, "spigot", "()L" + contract + ";");
                if (supplier == null) continue;
                for (var instruction : supplier.method.instructions) {
                    collectSpigotAllocation(instruction, contract, selected);
                    if (instruction instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETFIELD && field.desc.equals("L" + contract + ";")) {
                        var owner = types.get(field.owner);
                        if (owner != null) for (var constructor : owner.node.methods) if (constructor.name.equals("<init>")) {
                            for (var initialization : constructor.instructions) collectSpigotAllocation(initialization, contract, selected);
                        }
                    }
                }
            }
            if (!selected.isEmpty()) mappings.put(contract, new ArrayList<>(selected));
        }
        mappings.replaceAll((owner, values) -> values.stream().filter(types::containsKey).distinct().sorted().toList());
    }

    private void collectSpigotAllocation(AbstractInsnNode instruction, String contract, Set<String> selected) {
        if (instruction instanceof TypeInsnNode allocation && allocation.getOpcode() == Opcodes.NEW && subtype(allocation.desc, contract)) selected.add(allocation.desc);
    }

    private void map(String api, String... suffixes) { mappings.put(api, Arrays.stream(suffixes).map(s -> craft + "/" + s).toList()); }

    private Decl resolve(String owner, String name, String descriptor) { return resolve(owner, name, descriptor, new HashSet<>()); }
    private Decl resolve(String owner, String name, String descriptor, Set<String> seen) {
        if (owner == null || !seen.add(owner)) return null;
        var indexed = types.get(owner);
        if (indexed == null && owner.startsWith("java/")) {
            try (var stream = ClassLoader.getSystemResourceAsStream(owner + ".class")) {
                if (stream != null) { byte[] bytes = stream.readAllBytes(); indexed = new Indexed(node(bytes), "JDK:" + owner, hash(bytes), 0, false); types.put(owner, indexed); }
            } catch (Exception e) { throw new IllegalStateException("Cannot index JDK declaration: " + owner, e); }
        }
        if (indexed == null) return null;
        for (var method : indexed.node.methods) if (method.name.equals(name) && method.desc.equals(descriptor)) return new Decl(owner, method, indexed);
        Decl inherited = resolve(indexed.node.superName, name, descriptor, seen); if (inherited != null) return inherited;
        for (String iface : indexed.node.interfaces) { inherited = resolve(iface, name, descriptor, seen); if (inherited != null) return inherited; }
        return null;
    }

    private boolean subtype(String child, String parent) { return subtype(child, parent, new HashSet<>()); }
    private boolean subtype(String child, String parent, Set<String> seen) {
        if (Objects.equals(child, parent)) return true;
        if (child == null || !seen.add(child)) return false;
        var indexed = types.get(child); if (indexed == null) return false;
        if (subtype(indexed.node.superName, parent, seen)) return true;
        for (String iface : indexed.node.interfaces) if (subtype(iface, parent, seen)) return true;
        return false;
    }

    private List<UnsupportedPath> trace(Decl declaration, String receiver, Set<String> seen, int depth) {
        if (declaration == null || depth > 9 || (declaration.method.access & Opcodes.ACC_ABSTRACT) != 0) return List.of();
        String key = receiver + ":" + declaration.symbol();
        if (!seen.add(key)) return List.of();
        if (depth == 0 && cache.containsKey(key)) return cache.get(key);
        var method = declaration.method;
        var messages = new ArrayList<String>();
        boolean throwsUnsupported = false, branches = false, returns = false;
        for (var insn : method.instructions) {
            if (insn instanceof TypeInsnNode type && type.getOpcode() == Opcodes.NEW && type.desc.equals("java/lang/UnsupportedOperationException")) throwsUnsupported = true;
            if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String text) messages.add(text);
            if (insn instanceof JumpInsnNode || insn instanceof TableSwitchInsnNode || insn instanceof LookupSwitchInsnNode) branches = true;
            if (insn.getOpcode() >= Opcodes.IRETURN && insn.getOpcode() <= Opcodes.RETURN) returns = true;
        }
        var result = new ArrayList<UnsupportedPath>();
        if (throwsUnsupported) {
            boolean factory = Type.getReturnType(method.desc).getInternalName() != null && Type.getReturnType(method.desc).getInternalName().equals("java/lang/UnsupportedOperationException");
            result.add(new UnsupportedPath(!branches && !returns ? "ALWAYS_UNSUPPORTED" : factory ? "UNSUPPORTED_EXCEPTION_FACTORY" : "CONDITIONAL_UNSUPPORTED_PATH", List.of(declaration.symbol()), List.copyOf(messages)));
        }
        // Trace only owned implementation/API code, never arbitrary plugin code or JDK/library code.
        for (var insn : method.instructions) {
            if (!(insn instanceof MethodInsnNode call) || call.name.equals("<init>") && call.owner.equals("java/lang/UnsupportedOperationException")) continue;
            if (!owned(call.owner) && !mappings.containsKey(call.owner)) continue;
            String target = normalizeCraft(call.owner);
            if (call.getOpcode() != Opcodes.INVOKESPECIAL && call.getOpcode() != Opcodes.INVOKESTATIC && subtype(receiver, target) && usesThisReceiver(method, call)) target = receiver;
            List<String> targets = mappings.getOrDefault(target, List.of(target));
            for (String nestedReceiver : targets) {
            Decl nested = resolve(nestedReceiver, call.name, normalizeDescriptor(call.desc));
            var nestedSeen = new HashSet<>(seen);
            for (var nestedPath : trace(nested, nestedReceiver, nestedSeen, depth + 1)) {
                var path = new ArrayList<String>(); path.add(declaration.symbol()); path.addAll(nestedPath.path);
                String kind = nestedPath.kind;
                if (kind.equals("UNSUPPORTED_EXCEPTION_FACTORY")) {
                    if (nextReal(call).getOpcode() != Opcodes.ATHROW) continue;
                    kind = branches || returns ? "CONDITIONAL_UNSUPPORTED_PATH" : "ALWAYS_UNSUPPORTED";
                } else if (branches || !method.tryCatchBlocks.isEmpty()) kind = "CONDITIONAL_UNSUPPORTED_PATH";
                result.add(new UnsupportedPath(kind, path, nestedPath.messages));
            }
            }
        }
        seen.remove(key);
        var unique = result.stream().distinct().limit(12).toList();
        if (depth == 0) cache.put(key, unique);
        return unique;
    }

    private static boolean usesThisReceiver(MethodNode method, MethodInsnNode call) {
        // javac forwarding methods use ALOAD 0 followed by argument loads. A field
        // receiver breaks this pattern, preserving PlayerAdapter's actual owner.
        AbstractInsnNode cursor = call.getPrevious();
        int argumentCount = Type.getArgumentTypes(call.desc).length;
        for (int i = 0; cursor != null && i < argumentCount; ) {
            if (cursor.getOpcode() < 0) { cursor = cursor.getPrevious(); continue; }
            if (!(cursor instanceof VarInsnNode)) return false;
            i++; cursor = cursor.getPrevious();
        }
        while (cursor != null && cursor.getOpcode() < 0) cursor = cursor.getPrevious();
        return cursor instanceof VarInsnNode var && var.getOpcode() == Opcodes.ALOAD && var.var == 0;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode instruction) {
        var result = instruction.getNext(); while (result != null && result.getOpcode() < 0) result = result.getNext();
        return result == null ? instruction : result;
    }
    private static boolean owned(String owner) { return owner.startsWith("dev/meldrite/") || owner.startsWith("org/bukkit/craftbukkit/") || owner.equals("org/bukkit/Bukkit"); }
    private String normalizeCraft(String owner) {
        if (!owner.startsWith("org/bukkit/craftbukkit/")) return owner;
        if (types.containsKey(owner)) return owner;
        return owner.replaceFirst("org/bukkit/craftbukkit/(?:v1_\\d+_R\\d+/)?", craft + "/");
    }
    private String normalizeDescriptor(String descriptor) {
        return descriptor.replaceAll("Lorg/bukkit/craftbukkit/(?:v1_\\d+_R\\d+/)?", "L" + craft + "/");
    }

    private void plugin(Path path) throws Exception {
        input(path, "plugin"); String plugin = path.getFileName().toString();
        try (var jar = new JarFile(path.toFile())) {
            var descriptor = jar.getJarEntry("plugin.yml");
            if (descriptor != null) {
                String yaml = new String(jar.getInputStream(descriptor).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                var name = Pattern.compile("(?m)^name:\\s*['\"]?([^'\"\\r\\n]+)").matcher(yaml);
                if (name.find()) plugin = name.group(1).trim();
            }
            for (var entry : jar.stream().filter(e -> e.getName().endsWith(".class") && !e.getName().startsWith("META-INF/versions/")).toList()) {
                classes++; byte[] bytes = jar.getInputStream(entry).readAllBytes(); var node = node(bytes);
                for (var method : node.methods) {
                    int line = -1, offset = 0; var literals = new ArrayList<String>(); var reflectOps = new ArrayList<String>();
                    for (var insn : method.instructions) {
                        if (insn instanceof LineNumberNode number) line = number.line;
                        if (insn instanceof MethodInsnNode call) {
                            if (apiOwner(call.owner)) { directCalls++; use(plugin, path, node, method, new Ref(call.owner, call.name, call.desc, call.getOpcode()), "INVOKE", line, offset, hash(bytes)); }
                            if (call.owner.startsWith("java/lang/reflect/") || call.owner.equals("java/lang/Class") && (call.name.contains("Method") || call.name.equals("forName"))) reflectOps.add(call.owner + "." + call.name + call.desc);
                        }
                        if (insn instanceof InvokeDynamicInsnNode dynamic) {
                            for (Object arg : dynamic.bsmArgs) if (arg instanceof Handle handle && handle.getTag() >= Opcodes.H_INVOKEVIRTUAL && apiOwner(handle.getOwner())) {
                                methodHandles++; use(plugin, path, node, method, new Ref(handle.getOwner(), handle.getName(), handle.getDesc(), handleOpcode(handle.getTag())), "INVOKEDYNAMIC_METHOD_HANDLE", line, offset, hash(bytes));
                            }
                        }
                        if (insn instanceof LdcInsnNode ldc) {
                            if (ldc.cst instanceof Handle handle && handle.getTag() >= Opcodes.H_INVOKEVIRTUAL && apiOwner(handle.getOwner())) {
                                methodHandles++; use(plugin, path, node, method, new Ref(handle.getOwner(), handle.getName(), handle.getDesc(), handleOpcode(handle.getTag())), "LDC_METHOD_HANDLE", line, offset, hash(bytes));
                            }
                            if (ldc.cst instanceof String text && (text.contains("bukkit") || text.startsWith("get") || text.startsWith("set"))) literals.add(text);
                        }
                        if (insn.getOpcode() >= 0) offset++;
                    }
                    if (!reflectOps.isEmpty()) reflection.add(Map.of("plugin", plugin, "caller", node.name, "method", method.name + method.desc, "operations", reflectOps, "candidateLiterals", literals, "status", "REFLECTION_NOT_RESOLVED"));
                }
            }
        }
        System.out.println("Audited " + plugin + " from " + path.toAbsolutePath());
    }

    private void use(String plugin, Path path, ClassNode node, MethodNode method, Ref original, String kind, int line, int instruction, String classHash) {
        String owner = normalizeCraft(original.owner), descriptor = normalizeDescriptor(original.descriptor);
        List<String> receivers = original.name.equals("<init>") || original.opcode == Opcodes.INVOKESTATIC && !owner.equals("org/bukkit/Bukkit") ? null : mappings.get(owner);
        if (receivers == null || receivers.isEmpty()) receivers = List.of(owner);
        var receiverResults = new ArrayList<Map<String, Object>>();
        String status = "NO_EXPLICIT_UNSUPPORTED_FOUND";
        for (String receiver : receivers) {
            Decl declaration = resolve(receiver, original.name, descriptor);
            var result = new LinkedHashMap<String, Object>(); result.put("receiver", receiver);
            result.put("declaration", declaration == null ? null : declaration.symbol());
            List<UnsupportedPath> paths = trace(declaration, receiver, new HashSet<>(), 0);
            result.put("unsupportedPaths", paths);
            String receiverStatus = declaration == null ? "MISSING_MEMBER" : (declaration.method.access & Opcodes.ACC_ABSTRACT) != 0 ? "ABSTRACT_RUNTIME_TARGET_NOT_MAPPED" : paths.stream().anyMatch(p -> p.kind.equals("ALWAYS_UNSUPPORTED")) ? "ALWAYS_UNSUPPORTED" : paths.isEmpty() ? "NO_EXPLICIT_UNSUPPORTED_FOUND" : "CONDITIONAL_UNSUPPORTED_PATH";
            result.put("status", receiverStatus);
            result.put("operationAssessment", operationAssessment(original, receiver, paths));
            if (declaration != null) {
                result.put("implementationClass", declaration.indexed.source); result.put("implementationClassSha256", declaration.indexed.sha256);
            }
            receiverResults.add(result);
            if (receiverStatus.equals("ALWAYS_UNSUPPORTED")) status = receiverStatus;
            else if (receiverStatus.equals("CONDITIONAL_UNSUPPORTED_PATH") && !status.equals("ALWAYS_UNSUPPORTED")) status = receiverStatus;
            else if (receiverStatus.equals("MISSING_MEMBER") && status.equals("NO_EXPLICIT_UNSUPPORTED_FOUND")) status = types.containsKey(receiver) ? "MISSING_MEMBER" : "CLASS_NOT_INDEXED";
        }
        var use = new LinkedHashMap<String, Object>();
        use.put("plugin", plugin); use.put("pluginJar", path.toAbsolutePath().toString()); use.put("caller", node.name);
        use.put("callerClassSha256", classHash); use.put("callerMethod", method.name + method.desc); use.put("line", line); use.put("instruction", instruction);
        use.put("kind", kind); use.put("module", module(node.name)); use.put("branchScope", branch(node.name, original.owner));
        use.put("platformScope", platformScope(node.name, original));
        use.put("runtimeFailureProven", false);
        use.put("api", original.symbol()); use.put("normalizedApi", owner + "." + original.name + descriptor);
        use.put("apiBaseline", original.owner.startsWith("org/bukkit/craftbukkit/") ? "CRAFT_NATIVE_DESCRIPTOR_REMAP_NOT_APPLIED_SEE_NATIVE_AUDIT" :
                declaredInApi(original.owner, original.name, original.descriptor, new HashSet<>()) ? "DECLARED_IN_INDEXED_1_20_1_API_BASELINE" : "NOT_DECLARED_IN_INDEXED_1_20_1_API_BASELINE_OR_DEPENDENCY_UNINDEXED");
        use.put("status", status); use.put("receiverResults", receiverResults); uses.add(use);
        if (status.equals("ALWAYS_UNSUPPORTED") || status.equals("CONDITIONAL_UNSUPPORTED_PATH")) {
            String key = plugin + ":" + original.symbol();
            var finding = findings.computeIfAbsent(key, ignored -> {
                var row = new LinkedHashMap<String, Object>(); row.put("plugin", plugin); row.put("api", original.symbol()); row.put("status", statusValue(receiverResults));
                row.put("classification", classification(receiverResults));
                row.put("receiverResults", receiverResults); row.put("callers", new ArrayList<Map<String, Object>>()); return row;
            });
            @SuppressWarnings("unchecked") var callers = (List<Map<String, Object>>) finding.get("callers");
            callers.add(Map.of("class", node.name, "method", method.name + method.desc, "module", module(node.name), "branchScope", branch(node.name, original.owner), "platformScope", platformScope(node.name, original), "line", line, "kind", kind));
        }
    }

    private static String statusValue(List<Map<String, Object>> results) { return results.stream().anyMatch(r -> "ALWAYS_UNSUPPORTED".equals(r.get("status"))) ? "ALWAYS_UNSUPPORTED_FOR_RECORDED_RECEIVER" : "CONDITIONAL_UNSUPPORTED_PATH"; }
    private boolean declaredInApi(String owner, String name, String descriptor, Set<String> seen) {
        if (owner == null || !seen.add(owner)) return false;
        ClassNode type = apiBaseline.get(owner); if (type == null) return false;
        for (var method : type.methods) if (method.name.equals(name) && method.desc.equals(descriptor)) return true;
        if (declaredInApi(type.superName, name, descriptor, seen)) return true;
        for (String contract : type.interfaces) if (declaredInApi(contract, name, descriptor, seen)) return true;
        return false;
    }
    private static String classification(List<Map<String, Object>> results) {
        boolean contract = false, apiDefault = false, versionBaseline = false;
        for (var result : results) {
            contract |= result.get("operationAssessment").equals("DEPRECATED_ENTITY_TYPE_CONTRACT") || result.get("operationAssessment").equals("PLAYER_INVENTORY_DROP_CHANCE_CONTRACT");
            apiDefault |= result.get("operationAssessment").equals("DEFAULT_API_PLACEHOLDER_CANDIDATE");
            versionBaseline |= result.get("operationAssessment").equals("VERSION_BASELINE_REGENERATION_RESTRICTION");
            @SuppressWarnings("unchecked") var paths = (List<UnsupportedPath>) result.get("unsupportedPaths");
            for (var path : paths) if (!versionBaseline && path.messages.stream().anyMatch(message -> message.startsWith("UNSUPPORTED_OPERATION:"))) return "FACADE_PLACEHOLDER";
            for (var path : paths) if (path.messages.stream().anyMatch(message -> message.startsWith("UNSUPPORTED_CAPABILITY:"))) return "CAPABILITY_PLACEHOLDER";
        }
        if (versionBaseline) return "VERSION_BASELINE_RESTRICTION";
        if (apiDefault) return "DEFAULT_API_PLACEHOLDER";
        if (contract) return "DOCUMENTED_RECEIVER_OR_DEPRECATED_OPERATION_CONTRACT";
        return "EXPLICIT_OPERATION_OR_REPRESENTATION_RESTRICTION";
    }
    private static String operationAssessment(Ref api, String receiver, List<UnsupportedPath> paths) {
        if (paths.isEmpty()) return "NO_EXPLICIT_UNSUPPORTED_PATH";
        if (api.owner.equals("org/bukkit/World") && api.name.equals("regenerateChunk") && api.descriptor.equals("(II)Z")) return "VERSION_BASELINE_REGENERATION_RESTRICTION";
        if (api.owner.endsWith("$Spigot") && paths.stream().anyMatch(path -> path.messages.contains("Not supported yet."))) return "DEFAULT_API_PLACEHOLDER_CANDIDATE";
        if (api.owner.equals("org/bukkit/entity/Ocelot") && api.name.equals("setCatType") || api.owner.equals("org/bukkit/entity/Horse") && api.name.equals("setVariant")) return "DEPRECATED_ENTITY_TYPE_CONTRACT";
        if (receiver.endsWith("/inventory/CraftInventoryPlayer") && api.name.startsWith("set") && api.name.endsWith("DropChance")) return "PLAYER_INVENTORY_DROP_CHANCE_CONTRACT";
        if (paths.stream().anyMatch(path -> path.messages.stream().anyMatch(message -> message.startsWith("UNSUPPORTED_OPERATION:") || message.startsWith("UNSUPPORTED_CAPABILITY:")))) return "IMPLEMENTATION_PLACEHOLDER_CANDIDATE";
        return "CONDITIONAL_OR_EXPLICIT_RESTRICTION_REQUIRES_CONTRACT_REVIEW";
    }
    private static String platformScope(String caller, Ref api) {
        if (api.owner.contains("/threadedregions/") || api.descriptor.contains("/threadedregions/") || caller.toLowerCase(Locale.ROOT).contains("folia")) return "FOLIA_RELATED_API_GUARD_AND_REACHABILITY_UNVERIFIED";
        return "BUKKIT_PAPER_API_REACHABILITY_UNVERIFIED";
    }
    private static boolean apiOwner(String owner) { return owner.startsWith("org/bukkit/") || owner.startsWith("com/destroystokyo/paper/") || owner.startsWith("io/papermc/paper/"); }
    private static int handleOpcode(int tag) { return switch (tag) { case Opcodes.H_INVOKESTATIC -> Opcodes.INVOKESTATIC; case Opcodes.H_INVOKESPECIAL, Opcodes.H_NEWINVOKESPECIAL -> Opcodes.INVOKESPECIAL; case Opcodes.H_INVOKEINTERFACE -> Opcodes.INVOKEINTERFACE; default -> Opcodes.INVOKEVIRTUAL; }; }
    private static String module(String caller) {
        var matcher = Pattern.compile("/(?:Modules|modules)/([^/]+)").matcher(caller); if (matcher.find()) return matcher.group(1);
        matcher = Pattern.compile("/(?:commands|Commands)/(?:list/)?([^/$]+)").matcher(caller); if (matcher.find()) return "commands/" + matcher.group(1);
        String[] pieces = caller.split("/"); return pieces.length > 3 ? pieces[3] : "root";
    }
    private String branch(String caller, String owner) {
        var matcher = Pattern.compile("(?:v)?1[_\\.]([0-9]+)(?:[_\\.]R([0-9]+))?").matcher(caller + " " + owner);
        while (matcher.find()) if (!matcher.group(1).equals("20") || matcher.group(2) != null && !matcher.group(2).equals("1")) return "OTHER_VERSION_NAMED_BRANCH_REACHABILITY_UNVERIFIED";
        return "REACHABILITY_UNVERIFIED_FOR_1_20_1";
    }

    private void write(Path output) throws Exception {
        var counts = new TreeMap<String, Long>(); for (var use : uses) counts.merge(use.get("status").toString(), 1L, Long::sum);
        var modules = new TreeMap<String, Map<String, Object>>();
        for (var use : uses) if (Set.of("ALWAYS_UNSUPPORTED", "CONDITIONAL_UNSUPPORTED_PATH").contains(use.get("status"))) {
            String key = use.get("plugin") + "/" + use.get("module");
            var summary = modules.computeIfAbsent(key, ignored -> { var row = new LinkedHashMap<String, Object>(); row.put("apis", new TreeSet<String>()); row.put("callerMethods", new TreeSet<String>()); row.put("uses", 0L); return row; });
            @SuppressWarnings("unchecked") var apis = (Set<String>) summary.get("apis"); apis.add(use.get("api").toString());
            @SuppressWarnings("unchecked") var callers = (Set<String>) summary.get("callerMethods"); callers.add(use.get("caller") + "." + use.get("callerMethod"));
            summary.put("uses", (Long) summary.get("uses") + 1L);
        }
        var report = new LinkedHashMap<String, Object>(); report.put("schema", 1); report.put("generatedAt", Instant.now().toString());
        report.put("scope", "Actual invoke instructions and invokedynamic/LDC method handles in supplied plugin jars; inherited declarations and owned forwarding paths in already compiled implementation. No server launch, Gradle invocation, production plugin special cases, branch reachability or behavior certification.");
        report.put("limitations", List.of("ALWAYS_UNSUPPORTED applies to the recorded receiver and forwarding path; interface/subtype receiver choice remains an assumption.", "Conditional paths can be normal unsupported operations or guards and are not proven runtime failures.", "Reflection, dynamic proxy implementations, native methods and unresolved interface receivers are not certified.", "Class version branches are inventoried, never claimed to execute on Minecraft 1.20.1.", "Only compiled classes are audited; source files newer than compiled classes are listed separately.", "Owned forwarding traversal is bounded to 9 levels and 12 distinct unsupported paths per call."));
        report.put("versionBaselineRestrictions", List.of(Map.of("api", "org/bukkit/World.regenerateChunk(II)Z", "assessment", "Deprecated 1.20.1 version-baseline restriction; do not infer a required native regeneration feature from the local placeholder message.",
                "officialApiDocumentation", "https://jd.papermc.io/paper/1.20.1/org/bukkit/World.html#regenerateChunk(int,int)",
                "implementationEvidence", "Local Taiyitist 1.20.1 CraftWorld.java:341 throws the same operation unconditionally. Direct pinned official CraftBukkit source fetch was HTTP 403, so its 1.20.1 implementation is not independently verified.",
                "officialBuildReferences", "https://hub.spigotmc.org/versions/1.20.1.json", "officialCraftBukkitCommit", "3f9263ba3a726846a9466e12da95d73229af4ad9")));
        report.put("inputs", inputs); report.put("runtimeReceiverMappings", mappings); report.put("sourceNewerThanClass", staleSources);
        report.put("scannedClasses", classes); report.put("directApiInvocations", directCalls); report.put("apiMethodHandles", methodHandles); report.put("useCounts", counts);
        var unresolvedBaseline = new TreeMap<String, Long>();
        var unsupportedScope = new TreeMap<String, Long>();
        for (var use : uses) {
            if (Set.of("MISSING_MEMBER", "CLASS_NOT_INDEXED").contains(use.get("status"))) unresolvedBaseline.merge(use.get("apiBaseline").toString(), 1L, Long::sum);
            if (Set.of("ALWAYS_UNSUPPORTED", "CONDITIONAL_UNSUPPORTED_PATH").contains(use.get("status"))) unsupportedScope.merge(use.get("platformScope").toString(), 1L, Long::sum);
        }
        report.put("unresolvedUseBaselineCounts", unresolvedBaseline);
        report.put("unsupportedUsePlatformCounts", unsupportedScope);
        var assessed = new TreeMap<String, Long>();
        for (var finding : findings.values()) assessed.merge(finding.get("classification").toString(), 1L, Long::sum);
        report.put("unsupportedClassifications", assessed);
        report.put("unsupportedSymbolCount", findings.size()); report.put("modules", modules); report.put("unsupportedSymbols", findings.values()); report.put("uses", uses); report.put("reflectionCandidates", reflection);
        Files.createDirectories(output.toAbsolutePath().getParent()); new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
        var summary = new StringBuilder("# Plugin API caller audit\n\nGenerated from actual invocation bytecode; no server launch or runtime reachability claim.\n\n");
        summary.append("Scanned ").append(classes).append(" classes, ").append(directCalls).append(" direct API calls and ").append(methodHandles).append(" method handles. Found ").append(findings.size()).append(" distinct plugin/API symbols with an explicit unsupported path.\n\n");
        summary.append("Source files newer than audited compiled classes: ").append(staleSources.size()).append(". Recompile normally and rerun when this is nonzero.\n\n");
        summary.append("Unsupported classifications: ").append(assessed).append(". Documented receiver/deprecated contract restrictions are kept separate from implementation placeholders; Folia and other-version call-site scope is recorded in JSON.\n\n");
        summary.append("Unresolved call-site baseline counts: ").append(unresolvedBaseline).append(". Craft/native descriptor differences require the native audit; APIs absent from the locked 1.20.1 baseline and unindexed dependencies are not established implementation failures.\n\n");
        summary.append("| Plugin | API | Classification | Status | Calling modules |\n| --- | --- | --- | --- | --- |\n");
        for (var finding : findings.values()) {
            @SuppressWarnings("unchecked") var callers = (List<Map<String, Object>>) finding.get("callers");
            var moduleNames = callers.stream().map(row -> row.get("module").toString()).distinct().sorted().toList();
            summary.append("| ").append(finding.get("plugin")).append(" | `").append(finding.get("api")).append("` | ").append(finding.get("classification")).append(" | ").append(finding.get("status")).append(" | ").append(String.join(", ", moduleNames)).append(" |\n");
        }
        summary.append("\nEvery exact caller class/method, line, invocation kind, receiver assumption, declaration, forwarding path and implementation hash is recorded in the JSON report.\n");
        Files.writeString(output.resolveSibling(output.getFileName().toString().replaceFirst("\\.json$", "") + ".md"), summary);
        System.out.println("Classes=" + classes + ", direct calls=" + directCalls + ", method handles=" + methodHandles + ", unsupported symbols=" + findings.size() + ", uses=" + counts + ", sources newer than classes=" + staleSources.size());
        System.out.println("Report: " + output.toAbsolutePath());
    }

    private static ClassNode node(byte[] bytes) { var node = new ClassNode(); new ClassReader(bytes).accept(node, ClassReader.SKIP_FRAMES); return node; }
    private static String hash(byte[] bytes) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); } catch (Exception e) { throw new IllegalStateException(e); } }
}
