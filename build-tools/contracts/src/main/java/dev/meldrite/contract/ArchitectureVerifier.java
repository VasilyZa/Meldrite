package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;

/** Fails closed when compiled modules leak restricted symbols, strings, resources, or dependencies. */
public final class ArchitectureVerifier {
    private static final ObjectMapper JSON = new ObjectMapper();
    private ArchitectureVerifier() {}

    public static List<String> scanClassDirectory(Path directory, Set<String> forbiddenPrefixes) throws IOException {
        return scanClassDirectory(directory, forbiddenPrefixes, Map.of());
    }

    public static List<String> scanClassDirectory(Path directory, Set<String> forbiddenPrefixes,
            Map<String, Set<String>> allowedReferencePrefixes) throws IOException {
        return scanClassDirectory(directory, forbiddenPrefixes, allowedReferencePrefixes, 61);
    }

    public static List<String> scanClassDirectory(Path directory, Set<String> forbiddenPrefixes,
            Map<String, Set<String>> allowedReferencePrefixes, int maxJavaMajor) throws IOException {
        if (!Files.isDirectory(directory)) throw new IllegalStateException("MISSING_CLASSES: " + directory);
        List<Path> classes;
        try (var paths = Files.walk(directory)) { classes = paths.filter(p -> p.toString().endsWith(".class")).sorted().toList(); }
        if (classes.isEmpty()) throw new IllegalStateException("MISSING_CLASSES: " + directory);
        List<String> violations = new ArrayList<>();
        for (Path file : classes) {
            byte[] bytes = Files.readAllBytes(file);
            if (bytes.length < 10 || bytes[0] != (byte) 0xca || bytes[1] != (byte) 0xfe || bytes[2] != (byte) 0xba || bytes[3] != (byte) 0xbe)
                throw new IllegalStateException("INVALID_CLASSFILE: " + file);
            int major = ((bytes[6] & 255) << 8) | (bytes[7] & 255);
            if (major > maxJavaMajor) throw new IllegalStateException("JAVA_TARGET: " + file + " major=" + major + " exceeds configured maximum " + maxJavaMajor);
            String relativePath = directory.relativize(file).toString().replace('\\', '/');
            Set<String> allowed = allowedReferencePrefixesFor(relativePath, allowedReferencePrefixes);
            scanConstantPool(bytes, forbiddenPrefixes, allowed, file.toString(), violations);
            ClassReader reader;
            try { reader = new ClassReader(bytes); }
            catch (IllegalArgumentException malformed) { throw new IllegalStateException("INVALID_CLASSFILE: " + file, malformed); }
            ClassVisitor visitor = new ReferenceVisitor(violations, forbiddenPrefixes, allowed, file.toString());
            try { reader.accept(visitor, 0); }
            catch (RuntimeException malformed) { throw new IllegalStateException("INVALID_CLASSFILE: " + file, malformed); }
        }
        return List.copyOf(violations);
    }

    private static Set<String> allowedReferencePrefixesFor(String relativePath,
            Map<String, Set<String>> allowedReferencePrefixes) {
        Set<String> allowed = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : allowedReferencePrefixes.entrySet())
            if (relativePath.startsWith(entry.getKey())) allowed.addAll(entry.getValue());
        return allowed;
    }

    private static void scanConstantPool(byte[] bytes, Set<String> forbidden, Set<String> allowed,
            String source,
            List<String> violations) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            input.skipNBytes(8);
            int count = input.readUnsignedShort();
            for (int index = 1; index < count; index++) {
                int tag = input.readUnsignedByte();
                switch (tag) {
                    case 1 -> {
                        String value = input.readUTF();
                        for (String prefix : forbidden) if (!allowed.contains(prefix) && value.contains(prefix))
                            violations.add(source + " contains restricted constant-pool reference " + prefix);
                    }
                    case 3, 4, 9, 10, 11, 12, 17, 18 -> input.skipNBytes(4);
                    case 5, 6 -> { input.skipNBytes(8); index++; }
                    case 7, 8, 16, 19, 20 -> input.skipNBytes(2);
                    case 15 -> input.skipNBytes(3);
                    default -> throw new IOException("INVALID_CLASSFILE: " + source + " constant-pool tag=" + tag);
                }
            }
        }
    }

    public static void verifyDependencyGraph(Map<String, Set<String>> graph, Map<String, Set<String>> allowedEdges) {
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            Set<String> allowed = allowedEdges.getOrDefault(entry.getKey(), Set.of());
            for (String dependency : entry.getValue()) {
                if (!allowed.contains(dependency)) throw new IllegalStateException("ILLEGAL_PROJECT_EDGE: " + entry.getKey() + " -> " + dependency);
            }
        }
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String node : graph.keySet()) visit(node, graph, visiting, visited);
    }

    private static void visit(String node, Map<String, Set<String>> graph, Set<String> visiting, Set<String> visited) {
        if (visited.contains(node)) return;
        if (!visiting.add(node)) throw new IllegalStateException("PROJECT_CYCLE: " + node);
        for (String dependency : graph.getOrDefault(node, Set.of())) if (graph.containsKey(dependency)) visit(dependency, graph, visiting, visited);
        visiting.remove(node);
        visited.add(node);
    }

    public static void verify(Path root, Path graphFile) throws IOException {
        JsonNode rules = JSON.readTree(root.resolve("contracts/architecture-rules.json").toFile());
        JsonNode graphDocument = JSON.readTree(graphFile.toFile());
        if (rules == null || !rules.isObject() || graphDocument == null || !graphDocument.isObject())
            throw new IllegalStateException("INVALID_ARCHITECTURE_MODEL");
        JsonNode modules = rules.path("modules");
        if (!modules.isObject() || modules.isEmpty()) throw new IllegalStateException("EMPTY_ARCHITECTURE_MODEL");
        for (String field : List.of("projectEdges", "directExternalCompileDependencies", "resolvedExternalCompileDependencies",
                "runtimeProjectEdges", "directExternalRuntimeDependencies", "resolvedExternalRuntimeDependencies")) {
            if (!graphDocument.path(field).isObject()) throw new IllegalStateException("MISSING_GRAPH_SCOPE: " + field);
        }
        if(graphDocument.path("schemaVersion").asInt()==2)for(String field:List.of("resolvedFileCompileDependencies","resolvedFileRuntimeDependencies"))if(!graphDocument.path(field).isObject())throw new IllegalStateException("MISSING_GRAPH_SCOPE: "+field);
        Map<String, Set<String>> scopedExceptions = new HashMap<>();
        for (JsonNode exception : rules.path("exceptions")) {
            String moduleName = exception.path("module").asText();
            String dependency = exception.path("dependency").asText();
            String purpose = exception.path("purpose").asText();
            if (!modules.has(moduleName) || !dependency.matches("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+") || purpose.isBlank())
                throw new IllegalStateException("INVALID_SCOPED_EXCEPTION: " + exception);
            scopedExceptions.computeIfAbsent(moduleName, ignored -> new HashSet<>()).add(dependency);
        }
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        Map<String, Set<String>> allowedGraph = new LinkedHashMap<>();
        Set<String> ruledProjectPaths = new HashSet<>();
        for (var module = modules.fields(); module.hasNext();) {
            var entry = module.next();
            String name = entry.getKey();
            JsonNode rule = entry.getValue();
            String projectPath = rule.path("gradlePath").asText();
            ruledProjectPaths.add(projectPath);
            Path project = root.resolve(rule.path("path").asText());
            Path classes = project.resolve("build/classes/java/main");
            Set<String> forbidden = strings(rule.path("forbiddenPrefixes"));
            if (forbidden.isEmpty()) throw new IllegalStateException("MISSING_REFERENCE_POLICY in " + name);
            int maxJavaMajor = rule.path("maxJavaMajor").asInt(61);
            if (maxJavaMajor < 61) throw new IllegalStateException("INVALID_JAVA_TARGET in " + name);
            List<String> violations = new ArrayList<>(scanClassDirectory(classes, forbidden,
                    referenceAllowances(rule.path("allowedReferencePrefixes"), name), maxJavaMajor));
            scanResources(project.resolve("build/resources/main"), forbidden, strings(rule.path("allowedResourcePaths")), violations);
            if (!violations.isEmpty()) throw new IllegalStateException("FORBIDDEN_REFERENCE in " + name + ": " + String.join("; ", violations));
            Set<String> dependencies = new HashSet<>(strings(graphDocument.path("projectEdges").path(projectPath)));
            dependencies.addAll(strings(graphDocument.path("runtimeProjectEdges").path(projectPath)));
            graph.put(projectPath, dependencies);
            allowedGraph.put(projectPath, strings(rule.path("allowedProjectDependencies")));
            Set<String> allowedExternal = strings(rule.path("allowedExternalCompileDependencies"));
            Set<String> allowedExternalPrefixes = strings(rule.path("allowedExternalCompilePrefixes"));
            Set<String> actualExternal = strings(graphDocument.path("directExternalCompileDependencies").path(projectPath));
            Set<String> unexpected = new HashSet<>(actualExternal);
            unexpected.removeAll(allowedExternal);
            unexpected.removeIf(dependency -> allowedExternalPrefixes.stream().anyMatch(dependency::startsWith));
            if (!unexpected.isEmpty()) throw new IllegalStateException("ILLEGAL_EXTERNAL_COMPILE_DEPENDENCY in " + name + ": " + unexpected);
            Set<String> allowedRuntime = strings(rule.has("allowedExternalRuntimeDependencies")
                    ? rule.path("allowedExternalRuntimeDependencies") : rule.path("allowedExternalCompileDependencies"));
            Set<String> allowedRuntimePrefixes = strings(rule.path("allowedExternalRuntimePrefixes"));
            Set<String> runtimeDirect = strings(graphDocument.path("directExternalRuntimeDependencies").path(projectPath));
            Set<String> unexpectedRuntime = new HashSet<>(runtimeDirect);
            unexpectedRuntime.removeAll(allowedRuntime);
            unexpectedRuntime.removeIf(dependency -> allowedRuntimePrefixes.stream().anyMatch(dependency::startsWith));
            if (!unexpectedRuntime.isEmpty()) throw new IllegalStateException("ILLEGAL_EXTERNAL_RUNTIME_DEPENDENCY in " + name + ": " + unexpectedRuntime);
            Map<String, String> purposes = new HashMap<>();
            rule.path("dependencyPurposes").fields().forEachRemaining(purpose -> purposes.put(purpose.getKey(), purpose.getValue().asText()));
            if (!purposes.keySet().containsAll(allowedExternal)) throw new IllegalStateException("MISSING_DEPENDENCY_PURPOSE in " + name);
            if (!allowedExternalPrefixes.stream().allMatch(purposes::containsKey)) throw new IllegalStateException("MISSING_DEPENDENCY_PURPOSE in " + name);
            if (!allowedRuntimePrefixes.stream().allMatch(purposes::containsKey)) throw new IllegalStateException("MISSING_DEPENDENCY_PURPOSE in " + name);
            if (!purposes.keySet().containsAll(allowedRuntime) || purposes.values().stream().anyMatch(String::isBlank))
                throw new IllegalStateException("MISSING_DEPENDENCY_PURPOSE in " + name);
            Set<String> resolvedExternal = strings(graphDocument.path("resolvedExternalCompileDependencies").path(projectPath));
            resolvedExternal.addAll(strings(graphDocument.path("resolvedExternalRuntimeDependencies").path(projectPath)));
            Set<String> fileDependencies=strings(graphDocument.path("resolvedFileCompileDependencies").path(projectPath));
            fileDependencies.addAll(strings(graphDocument.path("resolvedFileRuntimeDependencies").path(projectPath)));
            Map<Path,String> allowedFiles=new HashMap<>();
            List<String> fileDependencyContracts = new ArrayList<>();
            if (rule.has("fileDependencyContract")) {
                fileDependencyContracts.add(rule.path("fileDependencyContract").asText());
            }
            rule.path("additionalFileDependencyContracts").forEach(node -> fileDependencyContracts.add(node.asText()));
            if (!fileDependencyContracts.isEmpty()) {
                if(rule.path("fileDependencyPurpose").asText().isBlank())throw new IllegalStateException("MISSING_DEPENDENCY_PURPOSE in "+name);
                for (String contractPath : fileDependencyContracts) {
                    JsonNode manifest=JSON.readTree(root.resolve(contractPath).toFile());
                    for(JsonNode artifact:manifest.path("artifacts"))allowedFiles.put(root.resolve(artifact.path("cachePath").asText()).toAbsolutePath().normalize(),artifact.path("sha256").asText());
                }
            }
            Map<String, String> allowedFileSuffixes = new HashMap<>();
            rule.path("allowedFileDependencyHashes").fields().forEachRemaining(fileEntry ->
                    allowedFileSuffixes.put(fileEntry.getKey(), fileEntry.getValue().asText()));
            for(String dependency:fileDependencies){
                Path path=Path.of(dependency).toAbsolutePath().normalize();String expected=allowedFiles.get(path);
                if(expected==null)for(var fileEntry:allowedFileSuffixes.entrySet())if(path.toString().endsWith(fileEntry.getKey())){expected=fileEntry.getValue();break;}
                if(expected==null)throw new IllegalStateException("ILLEGAL_FILE_DEPENDENCY in "+name+": "+path);
                if(!Files.isRegularFile(path)||!LockVerifier.sha256(Files.readAllBytes(path)).equals(expected))throw new IllegalStateException("FILE_DEPENDENCY_SHA_MISMATCH in "+name+": "+path);
            }
            for (String coordinate : resolvedExternal) {
                String[] parts = coordinate.split(":", 3);
                String group = parts[0];
                for (String forbiddenGroup : strings(rules.path("forbiddenExternalGroups"))) {
                    if (group.equals(forbiddenGroup) || group.startsWith(forbiddenGroup + ".")) {
                        String artifact = parts[0] + ":" + parts[1];
                        Set<String> allowedGroups = strings(rule.path("allowedForbiddenExternalGroups"));
                        boolean allowedGroup = allowedGroups.stream()
                                .anyMatch(allowed -> group.equals(allowed) || group.startsWith(allowed + "."));
                        if (!scopedExceptions.getOrDefault(name, Set.of()).contains(artifact)
                                && !allowedGroup)
                            throw new IllegalStateException("FORBIDDEN_EXTERNAL_DEPENDENCY in " + name + ": " + coordinate);
                    }
                }
            }
        }
        Set<String> actualProjectPaths = new HashSet<>();
        graphDocument.path("projectEdges").fieldNames().forEachRemaining(actualProjectPaths::add);
        graphDocument.path("runtimeProjectEdges").fieldNames().forEachRemaining(actualProjectPaths::add);
        if (!actualProjectPaths.equals(ruledProjectPaths)) throw new IllegalStateException("UNRULED_OR_MISSING_PROJECT: actual=" + actualProjectPaths + " rules=" + ruledProjectPaths);
        verifyDependencyGraph(graph, allowedGraph);
        Path report = root.resolve("build/reports/meldrite/verifyArchitecture/report.json");
        Files.createDirectories(report.getParent());
        JSON.writerWithDefaultPrettyPrinter().writeValue(report.toFile(), Map.of("modules", modules.size(), "projectEdges", graph,
                "directExternalCompileDependencies", graphDocument.path("directExternalCompileDependencies"),
                "resolvedExternalCompileDependencies", graphDocument.path("resolvedExternalCompileDependencies"),
                "directExternalRuntimeDependencies", graphDocument.path("directExternalRuntimeDependencies"),
                "resolvedExternalRuntimeDependencies", graphDocument.path("resolvedExternalRuntimeDependencies")));
        System.out.println("Architecture rules verified for " + modules.size() + " compiled modules and " + graph.size() + " Gradle projects.");
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) throw new IllegalArgumentException("Usage: ArchitectureVerifier <project-root> <gradle-graph.json>");
        verify(Path.of(args[0]), Path.of(args[1]));
    }

    private static Set<String> strings(JsonNode node) {
        Set<String> values = new HashSet<>();
        if (node != null && node.isArray()) node.forEach(value -> values.add(value.asText()));
        return values;
    }

    private static Map<String, Set<String>> referenceAllowances(JsonNode node, String moduleName) {
        if (node == null || node.isMissingNode() || node.isNull()) return Map.of();
        if (!node.isObject()) throw new IllegalStateException("INVALID_REFERENCE_ALLOWANCE in " + moduleName);
        Map<String, Set<String>> allowances = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> {
            String path = entry.getKey();
            if (path.isBlank() || !path.endsWith("/") || !entry.getValue().isArray())
                throw new IllegalStateException("INVALID_REFERENCE_ALLOWANCE in " + moduleName + ": " + path);
            Set<String> prefixes = strings(entry.getValue());
            if (prefixes.isEmpty())
                throw new IllegalStateException("INVALID_REFERENCE_ALLOWANCE in " + moduleName + ": " + path);
            allowances.put(path, prefixes);
        });
        return Map.copyOf(allowances);
    }

    private static void scanResources(Path directory, Set<String> forbidden, Set<String> allowedPaths,
                                      List<String> violations) throws IOException {
        if (!Files.isDirectory(directory)) return;
        try (var files = Files.walk(directory)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String path = directory.relativize(file).toString().replace('\\', '/');
                if (allowedPaths.contains(path)) continue;
                String extension = path.toLowerCase(java.util.Locale.ROOT);
                if (!(extension.endsWith(".json") || extension.endsWith(".properties") || extension.endsWith(".xml")
                        || extension.endsWith(".txt") || extension.endsWith(".yml") || extension.endsWith(".yaml")
                        || extension.endsWith(".toml") || extension.endsWith(".mf") || path.startsWith("META-INF/services/"))) continue;
                String text = Files.readString(file, StandardCharsets.UTF_8);
                for (String prefix : forbidden) if (text.contains(prefix)) violations.add(path + " contains restricted resource text " + prefix);
            }
        }
    }

    private static final class ReferenceVisitor extends ClassVisitor {
        private final List<String> violations;
        private final Set<String> forbidden;
        private final Set<String> allowed;
        private final String source;
        ReferenceVisitor(List<String> violations, Set<String> forbidden, Set<String> allowed, String source) {
            super(Opcodes.ASM9);
            this.violations = violations;
            this.forbidden = forbidden;
            this.allowed = allowed;
            this.source = source;
        }
        private void text(String value) {
            if (value == null) return;
            for (String prefix : forbidden)
                if (!allowed.contains(prefix) && value.contains(prefix)) violations.add(source + " contains restricted reference " + prefix);
        }
        private void descriptor(String value) {
            if (value == null) return;
            try { type(Type.getType(value)); } catch (IllegalArgumentException ignored) { text(value); }
        }
        private void type(Type type) {
            switch (type.getSort()) {
                case Type.ARRAY -> type(type.getElementType());
                case Type.METHOD -> { for (Type argument : type.getArgumentTypes()) type(argument); type(type.getReturnType()); }
                case Type.OBJECT -> text(type.getInternalName());
                default -> { }
            }
        }
        private void constant(Object value) {
            if (value instanceof Type t) type(t);
            else if (value instanceof Handle h) { text(h.getOwner()); descriptor(h.getDesc()); }
            else if (value instanceof ConstantDynamic d) { descriptor(d.getDescriptor()); constant(d.getBootstrapMethod()); for (int i=0;i<d.getBootstrapMethodArgumentCount();i++) constant(d.getBootstrapMethodArgument(i)); }
            else if (value instanceof String s) text(s);
        }
        private AnnotationVisitor annotationVisitor() {
            return new AnnotationVisitor(Opcodes.ASM9) {
                @Override public void visit(String name, Object value) { constant(value); }
                @Override public void visitEnum(String name, String descriptor, String value) { ReferenceVisitor.this.descriptor(descriptor); text(value); }
                @Override public AnnotationVisitor visitAnnotation(String name, String descriptor) { ReferenceVisitor.this.descriptor(descriptor); return annotationVisitor(); }
                @Override public AnnotationVisitor visitArray(String name) { return annotationVisitor(); }
            };
        }
        @Override public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            text(name); text(superName); if (interfaces != null) for (String value : interfaces) text(value); text(signature);
        }
        @Override public void visitOuterClass(String owner, String name, String descriptor) { text(owner); descriptor(descriptor); }
        @Override public void visitInnerClass(String name, String outerName, String innerName, int access) { text(name); text(outerName); }
        @Override public void visitNestHost(String nestHost) { text(nestHost); }
        @Override public void visitNestMember(String nestMember) { text(nestMember); }
        @Override public void visitPermittedSubclass(String permittedSubclass) { text(permittedSubclass); }
        @Override public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
            descriptor(descriptor); text(signature);
            return new RecordComponentVisitor(Opcodes.ASM9) {
                @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref, org.objectweb.asm.TypePath path, String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
            };
        }
        @Override public ModuleVisitor visitModule(String name, int access, String version) {
            return new ModuleVisitor(Opcodes.ASM9) {
                @Override public void visitUse(String service) { text(service); }
                @Override public void visitProvide(String service, String... providers) { text(service); for (String provider : providers) text(provider); }
            };
        }
        @Override public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) { descriptor(descriptor); return annotationVisitor(); }
        @Override public AnnotationVisitor visitTypeAnnotation(int typeRef, org.objectweb.asm.TypePath typePath, String descriptor, boolean visible) { descriptor(descriptor); return annotationVisitor(); }
        @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            descriptor(descriptor); text(signature); constant(value);
            return new FieldVisitor(Opcodes.ASM9) {
                @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref, org.objectweb.asm.TypePath path, String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
            };
        }
        @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            descriptor(descriptor); text(signature); if (exceptions != null) for (String exception : exceptions) text(exception);
            return new MethodVisitor(Opcodes.ASM9) {
                @Override public AnnotationVisitor visitAnnotationDefault() { return annotationVisitor(); }
                @Override public AnnotationVisitor visitAnnotation(String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
                @Override public AnnotationVisitor visitTypeAnnotation(int ref, org.objectweb.asm.TypePath path, String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
                @Override public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
                @Override public void visitTypeInsn(int opcode, String type) { text(type); }
                @Override public void visitFieldInsn(int opcode, String owner, String name, String desc) { text(owner); ReferenceVisitor.this.descriptor(desc); }
                @Override public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) { text(owner); ReferenceVisitor.this.descriptor(desc); }
                @Override public void visitInvokeDynamicInsn(String name, String desc, Handle bootstrap, Object... args) { ReferenceVisitor.this.descriptor(desc); constant(bootstrap); for (Object arg : args) constant(arg); }
                @Override public void visitLdcInsn(Object value) { constant(value); }
                @Override public void visitMultiANewArrayInsn(String desc, int dimensions) { ReferenceVisitor.this.descriptor(desc); }
                @Override public void visitTryCatchBlock(org.objectweb.asm.Label start, org.objectweb.asm.Label end, org.objectweb.asm.Label handler, String type) { text(type); }
                @Override public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
                    for (int i = 0; i < numLocal; i++) if (local[i] instanceof String name) text(name);
                    for (int i = 0; i < numStack; i++) if (stack[i] instanceof String name) text(name);
                }
                @Override public void visitLocalVariable(String name, String desc, String signature, org.objectweb.asm.Label start, org.objectweb.asm.Label end, int index) { ReferenceVisitor.this.descriptor(desc); text(signature); }
                @Override public AnnotationVisitor visitInsnAnnotation(int ref, org.objectweb.asm.TypePath path, String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
                @Override public AnnotationVisitor visitLocalVariableAnnotation(int ref, org.objectweb.asm.TypePath path, org.objectweb.asm.Label[] start, org.objectweb.asm.Label[] end, int[] index, String desc, boolean visible) { ReferenceVisitor.this.descriptor(desc); return annotationVisitor(); }
            };
        }
    }
}
