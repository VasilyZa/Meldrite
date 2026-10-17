import com.fasterxml.jackson.databind.ObjectMapper;
import dev.meldrite.paper.compat.EntityAbiTransform;
import dev.meldrite.paper.compat.InternalAccessDetector;
import dev.meldrite.paper.compat.LegacyMappings;
import dev.meldrite.paper.compat.LegacyNameTransform;
import dev.meldrite.plugin.spi.TransformContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Static descriptor/access audit only. Does not certify behavior, reflection, or branch reachability. */
public final class NativeExtensionAudit {
    private record Member(boolean method, String name, String descriptor, int access, String source, boolean unsupported) {}
    private record Declaration(String owner, Member member) {}
    private record Use(String plugin, String caller, String method, String original, String transformed, String status,
                       String declaration, String branchScope, boolean runtimeFailureProven) {}
    private record RuntimeRequestKey(String plugin, String pluginSha256, String callerClass, String callerMethod,
                                     String kind, String owner, String name, String descriptor, boolean isStatic) {}
    private static final class NativeType {
        final String name, parent; final List<String> interfaces; final int access;
        final Map<String, Member> fields = new TreeMap<>(), methods = new TreeMap<>();
        NativeType(ClassNode type) { name = type.name; parent = type.superName; interfaces = List.copyOf(type.interfaces); access = type.access; }
    }
    private final Map<String, NativeType> types = new TreeMap<>();
    private final List<Map<String, Object>> inputs = new ArrayList<>();
    private final List<Map<String, Object>> mixins = new ArrayList<>();
    private final List<Map<String, Object>> preApplyModels = new ArrayList<>();
    private final List<Map<String, Object>> reflection = new ArrayList<>();
    private final Map<String, Use> uses = new TreeMap<>();
    private final Map<String, Map<String, Object>> runtimeRequests = new TreeMap<>();
    private final Map<String, Set<String>> runtimeRequestSources = new TreeMap<>();
    private final Set<String> indexedClasspathResources = new HashSet<>();
    private final Set<Path> indexedClasspathJars = new HashSet<>();
    private int scannedClasses, nativeClasses;
    private MemoryMappingTree mappings;
    private int intermediaryNamespace, namedNamespace;

    public static void main(String[] args) throws Exception {
        Path runtimeRequests = null;
        var positional = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--runtime-requests")) {
                if (runtimeRequests != null || i + 1 >= args.length) {
                    throw new IllegalArgumentException("--runtime-requests must be provided once with an output path");
                }
                runtimeRequests = Path.of(args[++i]);
            } else {
                positional.add(args[i]);
            }
        }
        if (positional.size() < 9) throw new IllegalArgumentException("Usage: NativeExtensionAudit [--runtime-requests requests.json] report.json native.jar yarn.tiny compiledRoots(path-separated) mixins.json accesswidener paper-api.jar fabric-api.jar plugin.jar...");
        var audit = new NativeExtensionAudit();
        var parser = Path.of(ClassReader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (Files.isRegularFile(parser)) audit.input(parser);
        var aliases = LegacyMappings.class.getResource("/meldrite/mappings/1.20.1/legacy-names.bin.gz");
        if (aliases != null && aliases.getProtocol().equals("file")) audit.input(Path.of(aliases.toURI()));
        audit.bindMappings(Path.of(positional.get(2)));
        audit.indexJar(Path.of(positional.get(1))); audit.indexJar(Path.of(positional.get(6))); audit.indexJar(Path.of(positional.get(7)));
        audit.indexCompiled(Arrays.stream(positional.get(3).split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))).map(Path::of).toList(), Path.of(positional.get(4)));
        audit.widen(Path.of(positional.get(5)));
        for (int i = 8; i < positional.size(); i++) audit.plugin(Path.of(positional.get(i)));
        audit.write(Path.of(positional.get(0)));
        if (runtimeRequests != null) audit.writeRuntimeRequests(runtimeRequests);
    }
    private void bindMappings(Path path) throws Exception {
        input(path); mappings = new MemoryMappingTree(true); MappingReader.read(path, mappings);
        intermediaryNamespace = mappings.getNamespaceId("intermediary");
        namedNamespace = mappings.getNamespaceId("named");
        LegacyMappings.bindRuntime(new LegacyMappings.RuntimeNamespace() {
            @Override public String className(String name) { var type = mappings.getClass(name, intermediaryNamespace); return type == null ? name : type.getName(namedNamespace); }
            @Override public String intermediaryClassName(String name) { var type = mappings.getClass(name, namedNamespace); return type == null ? name : type.getName(intermediaryNamespace); }
            @Override public String fieldName(String owner, String name, String descriptor) {
                var type = mappings.getClass(owner, intermediaryNamespace); var field = type == null ? null : type.getField(name, descriptor, intermediaryNamespace);
                return field == null ? name : field.getName(namedNamespace);
            }
            @Override public String methodName(String owner, String name, String descriptor) {
                var type = mappings.getClass(owner, intermediaryNamespace); var method = type == null ? null : type.getMethod(name, descriptor, intermediaryNamespace);
                return method == null ? name : method.getName(namedNamespace);
            }
        });
    }
    private void input(Path path) throws Exception {
        inputs.add(Map.of("path", path.toAbsolutePath().toString(), "sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))),
                "modified", Files.getLastModifiedTime(path).toString()));
    }
    private static ClassNode node(byte[] bytes, boolean code) {
        var node = new ClassNode(); new ClassReader(bytes).accept(node, code ? ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES : ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES); return node;
    }
    private void indexJar(Path path) throws Exception {
        input(path);
        try (var jar = new JarFile(path.toFile())) {
            for (var entry : jar.stream().toList()) {
                if (entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/versions/")) {
                    index(node(jar.getInputStream(entry).readAllBytes(), false), path + "!/" + entry.getName());
                } else if (entry.getName().startsWith("META-INF/jars/") && entry.getName().endsWith(".jar")) {
                    try (var nested = new JarInputStream(jar.getInputStream(entry))) {
                        JarEntry nestedEntry;
                        while ((nestedEntry = nested.getNextJarEntry()) != null) {
                            if (nestedEntry.getName().endsWith(".class")
                                    && !nestedEntry.getName().startsWith("META-INF/versions/")) {
                                index(node(nested.readAllBytes(), false), path + "!/" + entry.getName()
                                        + "!/" + nestedEntry.getName());
                            }
                        }
                    }
                }
            }
        }
    }
    private void index(ClassNode node, String source) {
        var type = new NativeType(node); types.put(node.name, type);
        for (var field : node.fields) type.fields.put(field.name + field.desc, new Member(false, field.name, field.desc, field.access, source, false));
        for (var method : node.methods) type.methods.put(method.name + method.desc, new Member(true, method.name, method.desc, method.access, source, explicitUnsupported(method)));
    }
    private void indexClasspathTarget(String target) throws Exception {
        String resourceName = target + ".class";
        var loader = NativeExtensionAudit.class.getClassLoader();
        var resources = new TreeMap<String, URL>();
        var candidates = loader.getResources(resourceName);
        while (candidates.hasMoreElements()) {
            URL resource = candidates.nextElement();
            resources.put(resource.toExternalForm(), resource);
        }
        if (resources.size() != 1) {
            throw new IllegalStateException("Mixin target is absent from native index and classpath resolution found "
                    + resources.size() + " resources for " + target + ": " + resources.keySet());
        }
        URL resource = resources.firstEntry().getValue();
        byte[] bytes;
        try (var stream = resource.openStream()) { bytes = stream.readAllBytes(); }
        ClassNode metadata = node(bytes, false);
        if (!metadata.name.equals(target)) {
            throw new IllegalStateException("Classpath resource " + resource + " contains " + metadata.name
                    + " instead of requested Mixin target " + target);
        }
        if (indexedClasspathResources.add(resource.toExternalForm())) {
            if (resource.getProtocol().equals("jar")) {
                var connection = resource.openConnection();
                if (!(connection instanceof JarURLConnection jarConnection)) {
                    throw new IllegalStateException("Cannot establish JAR provenance for Mixin target " + target + " at " + resource);
                }
                URL jarUrl = jarConnection.getJarFileURL();
                if (!jarUrl.getProtocol().equals("file")) {
                    throw new IllegalStateException("Cannot hash classpath JAR for Mixin target " + target + ": " + jarUrl);
                }
                Path jarPath = Path.of(jarUrl.toURI()).toAbsolutePath().normalize();
                if (indexedClasspathJars.add(jarPath)) input(jarPath);
            } else if (resource.getProtocol().equals("file")) {
                input(Path.of(resource.toURI()));
            }
            inputs.add(Map.of("path", resource.toExternalForm(),
                    "sha256", HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)),
                    "kind", "classpath-resource"));
        }
        index(metadata, "classpath:" + resource.toExternalForm());
    }
    private void indexCompiled(List<Path> roots, Path mixinConfig) throws Exception {
        input(mixinConfig);
        var config = new ObjectMapper().readTree(mixinConfig.toFile()); String pkg = config.path("package").asText().replace('.', '/');
        var enabled = new HashSet<String>(); config.path("server").forEach(name -> enabled.add(pkg + "/" + name.asText())); config.path("mixins").forEach(name -> enabled.add(pkg + "/" + name.asText()));
        var mixinNodes = new ArrayList<ClassNode>();
        for (Path root : roots) try (var files = Files.walk(root)) {
            for (Path file : files.filter(file -> file.toString().endsWith(".class")).sorted().toList()) {
                input(file); var node = node(Files.readAllBytes(file), true);
                if (annotation(node.visibleAnnotations, node.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Mixin;") != null) mixinNodes.add(node);
                else index(node, file.toString());
            }
        }
        for (ClassNode node : mixinNodes) {
            AnnotationNode mixin = annotation(node.visibleAnnotations, node.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Mixin;");
            var targets = new ArrayList<String>();
            for (int i = 0; i < mixin.values.size(); i += 2) {
                if (mixin.values.get(i).equals("value")) for (Object target : (List<?>) mixin.values.get(i + 1)) targets.add(((Type) target).getInternalName());
                if (mixin.values.get(i).equals("targets")) for (Object target : (List<?>) mixin.values.get(i + 1)) targets.add(target.toString().replace('.', '/'));
            }
            boolean installed = enabled.contains(node.name); var additions = new ArrayList<String>();
            for (String target : targets) {
                NativeType type = types.get(target);
                if (type == null) {
                    indexClasspathTarget(target);
                    type = types.get(target);
                }
                if (type == null) throw new IllegalStateException("Mixin target is absent from native index and classpath: " + target);
                if (installed && config.path("plugin").asText().equals("dev.meldrite.v1201.hook.MeldriteMixinPlugin")) {
                    modelPreApply(node.name, type, additions);
                }
                for (FieldNode field : node.fields) {
                    if (annotation(field.visibleAnnotations, field.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;") != null) continue;
                    if ((field.access & Opcodes.ACC_PUBLIC) == 0) continue;
                    additions.add("F " + field.name + field.desc);
                    if (installed) type.fields.put(field.name + field.desc, new Member(false, field.name, field.desc, field.access, node.name, false));
                }
                for (MethodNode method : node.methods) {
                    if (method.name.startsWith("<") || (method.access & Opcodes.ACC_PUBLIC) == 0 || method.name.startsWith("meldrite$") ||
                            annotation(method.visibleAnnotations, method.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/Shadow;") != null ||
                            annotation(method.visibleAnnotations, method.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/gen/Accessor;") != null ||
                            annotation(method.visibleAnnotations, method.invisibleAnnotations, "Lorg/spongepowered/asm/mixin/gen/Invoker;") != null) continue;
                    additions.add("M " + method.name + method.desc);
                    if (installed) type.methods.put(method.name + method.desc, new Member(true, method.name, method.desc, method.access, node.name, explicitUnsupported(method)));
                }
            }
            mixins.add(Map.of("class", node.name, "installed", installed, "targets", targets, "publicAdditions", additions));
        }
    }
    /** Explicit models of installed generation hooks; these do not execute Mixin or certify behavior. */
    private void modelPreApply(String mixin, NativeType target, List<String> additions) {
        String descriptor;
        String name;
        int access = Opcodes.ACC_PUBLIC;
        if (mixin.equals("dev/meldrite/v1201/hook/ServerSingletonMixin")) {
            name = "getServer"; descriptor = "()L" + target.name + ";"; access |= Opcodes.ACC_STATIC;
        } else if (mixin.equals("dev/meldrite/v1201/hook/LegacyPlayerListPacketAliasesMixin")) {
            name = "<init>";
            descriptor = "(L" + namedClass("net/minecraft/class_2703$class_5893") + ";[L" + namedClass("net/minecraft/class_3222") + ";)V";
        } else if (mixin.equals("dev/meldrite/v1201/hook/LegacyLiteralTextAliasesMixin")) {
            name = "<init>"; descriptor = "(Ljava/lang/String;)V";
        } else return;
        String source = mixin + ":MeldriteMixinPlugin.preApply-model";
        additions.add("M " + name + descriptor + " [preApply model]");
        target.methods.putIfAbsent(name + descriptor, new Member(true, name, descriptor, access, source, false));
        preApplyModels.add(Map.of("mixin", mixin, "target", target.name, "symbol", name + descriptor,
                "status", "INSTALLED_GENERATION_HOOK_MODELED_NOT_EXECUTED"));
    }
    private String namedClass(String intermediary) {
        var type = mappings.getClass(intermediary, intermediaryNamespace);
        if (type == null) throw new IllegalStateException("Missing generation-hook type mapping: " + intermediary);
        return type.getName(namedNamespace);
    }
    private static AnnotationNode annotation(List<AnnotationNode> first, List<AnnotationNode> second, String name) {
        if (first != null) for (var annotation : first) if (annotation.desc.equals(name)) return annotation;
        if (second != null) for (var annotation : second) if (annotation.desc.equals(name)) return annotation;
        return null;
    }
    private static boolean explicitUnsupported(MethodNode method) {
        for (var instruction : method.instructions) {
            if (instruction instanceof org.objectweb.asm.tree.TypeInsnNode type && type.desc.equals("java/lang/UnsupportedOperationException")) return true;
            if (instruction instanceof MethodInsnNode call && call.name.equals("unsupported")) return true;
        }
        return false;
    }
    private void widen(Path path) throws Exception {
        input(path);
        for (String line : Files.readAllLines(path)) {
            String[] pieces = line.trim().split("\\s+");
            if (pieces.length != 5 || !pieces[0].equals("accessible")) continue;
            // The merged Yarn jar is indexed in the named namespace, which is
            // also the namespace used by this access widener.  Runtime Loom
            // remaps both together; converting this line to intermediary here
            // would make a static audit report members that are present.
            String owner = pieces[2], name = pieces[3], descriptor = pieces[4];
            var type = types.get(owner); if (type == null) throw new IllegalStateException("Missing access widener owner: " + pieces[2]);
            var members = pieces[1].equals("method") ? type.methods : type.fields; String key = name + descriptor;
            var member = members.get(key); if (member == null) throw new IllegalStateException("Missing access widener member: " + line);
            members.put(key, new Member(member.method, member.name, member.descriptor, member.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC, member.source + ":accesswidener", member.unsupported));
        }
    }
    private void plugin(Path path) throws Exception {
        input(path); String id = pluginIdentity(path); String pluginSha256 = sha256(path);
        System.out.println("Auditing " + id + " from " + path.getFileName());
        InternalAccessDetector.prepareMappings(id, path, List.of());
        var context = new TransformContext(id, TransformContext.Access.INTERNAL); var nativeTransform = new LegacyNameTransform(); var entityTransform = new EntityAbiTransform();
        var originals = new TreeMap<String, ClassNode>(); var transformed = new TreeMap<String, ClassNode>();
        try (var jar = new JarFile(path.toFile())) {
            for (var entry : jar.stream().filter(entry -> entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/versions/")).toList()) {
                byte[] bytes = jar.getInputStream(entry).readAllBytes(); var original = node(bytes, true);
                var target = referencesInternal(original, id) ? node(nativeTransform.apply(entityTransform.apply(bytes, context), context), true) : original;
                transformed.put(target.name, target); originals.put(target.name, original);
            }
        }
        for (var node : transformed.values()) index(node, path.toString());
        for (var entry : transformed.entrySet()) {
            scannedClasses++; var original = originals.get(entry.getKey()); var target = entry.getValue(); boolean nativeUse = false;
            for (int i = 0; i < target.methods.size(); i++) {
                var method = target.methods.get(i); var originalMethod = original.methods.get(i);
                var originalInstructions = new ArrayList<String>();
                for (var instruction : originalMethod.instructions) {
                    if (instruction instanceof MethodInsnNode call) originalInstructions.add(call.owner + "." + call.name + call.desc);
                    if (instruction instanceof FieldInsnNode field) originalInstructions.add(field.owner + "." + field.name + ":" + field.desc);
                }
                int callIndex = 0; var literals = new ArrayList<String>(); var reflectionCalls = new ArrayList<String>();
                for (var instruction : method.instructions) {
                    String source = null, owner = null, name = null, descriptor = null; int opcode = instruction.getOpcode(); boolean isMethod = false;
                    if (instruction instanceof MethodInsnNode call) {
                        source = callIndex < originalInstructions.size() ? originalInstructions.get(callIndex) : "TRANSFORM_INSERTED"; callIndex++;
                        owner = call.owner; name = call.name; descriptor = call.desc; isMethod = true;
                        if (call.owner.equals("dev/meldrite/plugin/classloading/LegacyClassLookup") || call.owner.startsWith("java/lang/reflect/") || call.owner.equals("java/lang/Class") && call.name.startsWith("get")) reflectionCalls.add(call.name + call.desc);
                    }
                    if (instruction instanceof FieldInsnNode field) {
                        source = callIndex < originalInstructions.size() ? originalInstructions.get(callIndex) : "TRANSFORM_INSERTED"; callIndex++;
                        owner = field.owner; name = field.name; descriptor = field.desc;
                    }
                    if (instruction instanceof LdcInsnNode constant && constant.cst instanceof String text && (text.contains("net.minecraft") || text.contains("net/minecraft") || text.contains("craftbukkit") || text.equals("getBukkitEntity") || text.equals("getHandle"))) literals.add(text);
                    if (owner == null || !nativeOwner(owner)) continue;
                    Declaration declaration = resolve(owner, name, descriptor, isMethod);
                    if (!internal(owner) && declaration != null && !internal(declaration.owner)) continue;
                    nativeUse = true;
                    String status = declaration == null ? types.containsKey(owner) ? "MISSING_MEMBER" : "MISSING_CLASS" : declaration.member.unsupported ? "EXPLICIT_UNSUPPORTED" :
                            (declaration.member.access & Opcodes.ACC_PUBLIC) == 0 ? "ACCESS_NOT_PUBLIC" : staticUse(opcode) != ((declaration.member.access & Opcodes.ACC_STATIC) != 0) ? "STATIC_KIND_MISMATCH" : "SYMBOL_RESOLVED_BEHAVIOR_UNVERIFIED";
                    String result = owner + "." + name + (isMethod ? descriptor : ":" + descriptor);
                    uses.putIfAbsent(id + ":" + target.name + ":" + method.name + method.desc + ":" + result,
                            new Use(id, target.name, method.name + method.desc, source, result, status,
                                    declaration == null ? null : declaration.owner + ":" + declaration.member.source,
                                    branchScope(target.name, source), false));
                    addRuntimeRequest(id, pluginSha256, target.name, method.name, method.desc,
                            source, owner, name, descriptor, isMethod, opcode);
                }
                if (!literals.isEmpty() && !reflectionCalls.isEmpty()) reflection.add(Map.of("plugin", id, "caller", target.name, "method", method.name + method.desc, "literals", literals, "operations", reflectionCalls, "status", "REFLECTION_NOT_RESOLVED_STATICALLY"));
            }
            if (nativeUse) nativeClasses++;
        }
        System.out.println("Audited " + id + ": " + transformed.size() + " classes");
    }
    private static boolean referencesInternal(ClassNode type, String plugin) {
        boolean[] referenced = {false};
        type.accept(new ClassRemapper(new ClassWriter(0), new Remapper() {
            @Override public String map(String name) {
                if (internal(name) || !LegacyMappings.className(name).equals(name)) referenced[0] = true;
                return name;
            }
            @Override public Object mapValue(Object value) {
                if (value instanceof String text && (internal(text.replace('.', '/')) || text.equals("getBukkitEntity") || text.equals("getHandle") || !LegacyNameTransform.reflectionClassName(text).equals(text))) referenced[0] = true;
                return super.mapValue(value);
            }
        }));
        return referenced[0] || LegacyMappings.isSubclass(plugin, type.name, "net/minecraft/world/entity/Entity");
    }
    private boolean nativeOwner(String owner) {
        if (internal(owner)) return true;
        var pending = new ArrayDeque<String>(); var seen = new HashSet<String>(); pending.add(owner);
        while (!pending.isEmpty()) {
            String current = pending.remove(); if (!seen.add(current)) continue;
            if (internal(current)) return true;
            var type = types.get(current); if (type == null) continue;
            if (type.parent != null) pending.add(type.parent); pending.addAll(type.interfaces);
        }
        return false;
    }
    private static String pluginIdentity(Path path) throws Exception {
        try (var jar = new JarFile(path.toFile())) {
            var descriptor = jar.getJarEntry("paper-plugin.yml");
            if (descriptor == null) descriptor = jar.getJarEntry("plugin.yml");
            if (descriptor == null) throw new IllegalArgumentException("Plugin descriptor is absent: " + path);
            try (var input = jar.getInputStream(descriptor)) {
                Object values = new Yaml(new SafeConstructor(new LoaderOptions())).load(input);
                if (!(values instanceof Map<?, ?> map) || !(map.get("name") instanceof String name)) throw new IllegalArgumentException("Invalid plugin descriptor: " + path);
                return name;
            }
        }
    }
    private static boolean internal(String name) { return name.startsWith("net/minecraft/") || name.startsWith("org/bukkit/craftbukkit/") || name.startsWith("io/papermc/paper/") || name.startsWith("com/destroystokyo/paper/"); }
    private static String branchScope(String caller, String originalSymbol) {
        var matcher = java.util.regex.Pattern.compile("(?:v)?1[_\\.]([0-9]+)(?:[_\\.]R([0-9]+))?").matcher(caller + " " + originalSymbol);
        while (matcher.find()) {
            if (!matcher.group(1).equals("20") || matcher.group(2) != null && !matcher.group(2).equals("1")) return "OTHER_VERSION_NAMED_BRANCH_REACHABILITY_UNVERIFIED";
        }
        return "REACHABILITY_UNVERIFIED_FOR_1_20_1";
    }
    private static boolean staticUse(int opcode) { return opcode == Opcodes.INVOKESTATIC || opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC; }

    private void addRuntimeRequest(String plugin, String pluginSha256, String callerClass, String callerMethodName,
                                   String callerMethodDescriptor, String source, String owner, String name,
                                   String descriptor, boolean method, int opcode) throws Exception {
        String kind = method ? name.equals("<init>") ? "constructor" : "method" : "field";
        String intermediaryOwner = intermediaryName(owner);
        String intermediaryName = intermediaryMemberName(owner, name, descriptor, method);
        String intermediaryDescriptor = intermediaryDescriptor(descriptor);
        String callerMethod = callerMethodName + intermediaryDescriptor(callerMethodDescriptor);
        boolean isStatic = staticUse(opcode);
        var key = new RuntimeRequestKey(plugin, pluginSha256, callerClass, callerMethod, kind,
                intermediaryOwner, intermediaryName, intermediaryDescriptor, isStatic);
        String id = runtimeRequestId(key);
        if (!runtimeRequests.containsKey(id)) {
            var request = new LinkedHashMap<String, Object>();
            request.put("id", id);
            request.put("kind", kind);
            request.put("owner", intermediaryOwner.replace('/', '.'));
            request.put("name", intermediaryName);
            request.put("descriptor", intermediaryDescriptor);
            request.put("static", isStatic);
            request.put("callerClass", callerClass.replace('/', '.'));
            request.put("callerMethod", callerMethod);
            request.put("plugin", plugin);
            request.put("pluginSha256", pluginSha256);
            request.put("scope", "LOADED_CALLER_CLASS");
            runtimeRequests.put(id, request);
            runtimeRequestSources.put(id, new java.util.TreeSet<>());
        }
        if (source != null && !source.isBlank()) runtimeRequestSources.get(id).add(source);
    }

    private static String runtimeRequestId(RuntimeRequestKey key) throws Exception {
        String material = String.join("\u0000", key.plugin(), key.pluginSha256(), key.callerClass(), key.callerMethod(),
                key.kind(), key.owner(), key.name(), key.descriptor(), Boolean.toString(key.isStatic()));
        return "abi-" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(material.getBytes(StandardCharsets.UTF_8)));
    }

    private String intermediaryName(String owner) {
        var type = mappings.getClass(owner, namedNamespace);
        if (type != null) return type.getName(intermediaryNamespace);
        type = mappings.getClass(owner, intermediaryNamespace);
        return type == null ? owner : type.getName(intermediaryNamespace);
    }

    private String intermediaryMemberName(String owner, String name, String descriptor, boolean method) {
        if (name.equals("<init>")) return name;
        String namedOwner = owner;
        var mappedOwner = mappings.getClass(owner, intermediaryNamespace);
        if (mappedOwner != null) namedOwner = mappedOwner.getName(namedNamespace);
        var pending = new ArrayDeque<String>();
        var visited = new HashSet<String>();
        pending.add(namedOwner);
        while (!pending.isEmpty()) {
            String current = pending.removeFirst();
            if (!visited.add(current)) continue;
            var mappingType = mappings.getClass(current, namedNamespace);
            if (mappingType != null) {
                var member = method ? mappingType.getMethod(name, descriptor, namedNamespace)
                        : mappingType.getField(name, descriptor, namedNamespace);
                if (member != null) return member.getName(intermediaryNamespace);
            }
            NativeType nativeType = types.get(current);
            if (nativeType != null) {
                if (nativeType.parent != null) pending.addLast(nativeType.parent);
                pending.addAll(nativeType.interfaces);
            }
        }
        // Paper/Craft additions and unmapped upstream members keep their runtime name.
        return name;
    }

    private String intermediaryDescriptor(String descriptor) {
        Type type = Type.getType(descriptor);
        if (type.getSort() == Type.METHOD) {
            var arguments = Arrays.stream(type.getArgumentTypes()).map(this::intermediaryType).toArray(Type[]::new);
            return Type.getMethodDescriptor(intermediaryType(type.getReturnType()), arguments);
        }
        return intermediaryType(type).getDescriptor();
    }

    private Type intermediaryType(Type type) {
        if (type.getSort() == Type.OBJECT) {
            var mapped = mappings.getClass(type.getInternalName(), namedNamespace);
            if (mapped != null) return Type.getObjectType(mapped.getName(intermediaryNamespace));
            mapped = mappings.getClass(type.getInternalName(), intermediaryNamespace);
            return mapped == null ? type : Type.getObjectType(mapped.getName(intermediaryNamespace));
        }
        if (type.getSort() == Type.ARRAY) {
            return Type.getType("[".repeat(type.getDimensions()) + intermediaryType(type.getElementType()).getDescriptor());
        }
        return type;
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }

    private Declaration resolve(String owner, String name, String descriptor, boolean method) {
        var pending = new ArrayDeque<String>(); var visited = new HashSet<String>(); pending.add(owner);
        while (!pending.isEmpty()) {
            String current = pending.remove(); if (!visited.add(current)) continue; var type = supportType(current); if (type == null) continue;
            Member member = (method ? type.methods : type.fields).get(name + descriptor); if (member != null) return new Declaration(current, member);
            if (name.equals("<init>")) continue;
            if (type.parent != null) pending.add(type.parent); pending.addAll(type.interfaces);
        }
        return null;
    }
    private NativeType supportType(String name) {
        var known = types.get(name); if (known != null) return known;
        if (!(name.startsWith("java/") || name.startsWith("javax/") || name.startsWith("net/kyori/") || name.startsWith("com/google/") || name.startsWith("io/netty/"))) return null;
        try (var input = NativeExtensionAudit.class.getClassLoader().getResourceAsStream(name + ".class")) {
            if (input == null) return null;
            index(node(input.readAllBytes(), false), "support:" + NativeExtensionAudit.class.getClassLoader().getResource(name + ".class"));
            return types.get(name);
        } catch (java.io.IOException failure) { throw new java.io.UncheckedIOException(failure); }
    }
    private void write(Path output) throws Exception {
        var counts = new TreeMap<String, Long>(); uses.values().forEach(use -> counts.merge(use.status, 1L, Long::sum));
        var unique = new TreeMap<String, String>(); uses.values().forEach(use -> unique.put(use.transformed, use.status));
        var uniqueCounts = new TreeMap<String, Long>(); unique.values().forEach(status -> uniqueCounts.merge(status, 1L, Long::sum));
        var report = new LinkedHashMap<String, Object>(); report.put("schema", 1);
        report.put("scope", "Static actual plugin bytecode after EntityAbiTransform + LegacyNameTransform, named native jar, compiled mixin declarations, explicit installed preApply generation models and accesswidener. Generation hooks are modeled, not executed. Multi-version and optional dependency paths are counted; reachability, reflection, source/artifact staleness and behavior remain unverified. ACCESS_NOT_PUBLIC needs caller/subclass/reflection analysis.");
        report.put("asmParser", ClassReader.class.getProtectionDomain().getCodeSource().getLocation().toString());
        report.put("inputs", inputs); report.put("pluginClassesScanned", scannedClasses); report.put("classesWithDirectNativeReferences", nativeClasses);
        report.put("distinctCallerUses", uses.size()); report.put("distinctSymbols", unique.size()); report.put("callerUseStatuses", counts); report.put("symbolStatuses", uniqueCounts);
        report.put("installedMixinDeclarations", mixins); report.put("installedPreApplyModels", preApplyModels); report.put("uses", uses.values()); report.put("reflectionCandidates", reflection);
        Files.createDirectories(output.toAbsolutePath().getParent()); new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
        System.out.println("Native ABI audit: " + scannedClasses + " plugin classes, " + unique.size() + " distinct symbols: " + uniqueCounts + ". Reflection candidates: " + reflection.size() + ". No behavior completeness certification. " + output.toAbsolutePath());
    }

    private void writeRuntimeRequests(Path output) throws Exception {
        runtimeRequestSources.forEach((id, sources) -> {
            var request = runtimeRequests.get(id);
            request.put("source", sources.isEmpty() ? "UNKNOWN_SOURCE" : sources.iterator().next());
            if (sources.size() > 1) request.put("sources", List.copyOf(sources));
        });
        var result = new LinkedHashMap<String, Object>();
        result.put("schemaVersion", 1);
        result.put("minecraft", "1.20.1");
        result.put("namespace", "intermediary");
        result.put("requests", runtimeRequests.values());
        Files.createDirectories(output.toAbsolutePath().getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), result);
        System.out.println("Runtime ABI requests: " + runtimeRequests.size() + " caller-scoped checks; loading a caller class does not prove branch execution or behavior. " + output.toAbsolutePath());
    }
}
