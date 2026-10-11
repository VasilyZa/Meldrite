import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import javax.tools.*;

/** Source inventory only; presence and declaration counts do not certify ABI or behavior. */
public final class CraftSourceAudit {
    private static final String PREFIX = "org.bukkit.craftbukkit.";
    private record Method(String signature, long line, boolean explicitUnsupported) {}
    private record SourceType(String name, String kind, String file, String sha256, long line,
                              String superclass, List<String> interfaces, List<String> fields, List<Method> methods) {}

    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException("Usage: CraftSourceAudit <report.json> <Leaf source root> <Meldrite source roots...>");
        var upstream = parse(List.of(Path.of(args[1])));
        var roots = Arrays.stream(args).skip(2).map(Path::of).toList();
        var local = parse(roots);
        var rows = new ArrayList<Map<String, Object>>();
        int missing = 0;
        for (var entry : upstream.entrySet()) {
            var implementation = local.get(entry.getKey());
            var row = new LinkedHashMap<String, Object>();
            row.put("name", entry.getKey());
            row.put("status", implementation == null ? "MISSING_SOURCE_TYPE" : "SOURCE_PRESENT_UNVERIFIED");
            row.put("leaf", entry.getValue());
            row.put("meldrite", implementation);
            rows.add(row);
            if (implementation == null) missing++;
        }
        var report = new LinkedHashMap<String, Object>();
        report.put("schema", 1);
        report.put("scope", "Named Craft source types; Java parser, without type resolution. Does not certify method descriptors, inherited implementations, NMS mapping or runtime behavior.");
        report.put("leafRoots", List.of(Path.of(args[1]).toAbsolutePath().toString()));
        report.put("meldriteRoots", roots.stream().map(path -> path.toAbsolutePath().toString()).toList());
        report.put("leafTypeCount", upstream.size());
        report.put("meldriteTypeCount", local.size());
        report.put("missingLeafTypes", missing);
        report.put("matchedSourceTypes", upstream.size() - missing);
        report.put("explicitUnsupportedMethods", local.values().stream().flatMap(type -> type.methods.stream()).filter(Method::explicitUnsupported).count());
        report.put("types", rows);
        report.put("meldriteOnlyTypes", local.values().stream().filter(type -> !upstream.containsKey(type.name)).toList());
        Path output = Path.of(args[0]).toAbsolutePath();
        Files.createDirectories(output.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), report);
        System.out.println("Craft source audit: " + upstream.size() + " Leaf types, " + (upstream.size() - missing)
                + " matched source types, " + missing + " missing; no completeness certification. Report: " + output);
    }

    private static Map<String, SourceType> parse(List<Path> roots) throws Exception {
        var files = new TreeSet<Path>();
        for (Path root : roots) {
            try (var paths = Files.walk(root)) { paths.filter(path -> path.toString().endsWith(".java")).forEach(files::add); }
        }
        var compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("A JDK with jdk.compiler is required");
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var result = new TreeMap<String, SourceType>();
        try (var manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, java.nio.charset.StandardCharsets.UTF_8)) {
            var task = (JavacTask) compiler.getTask(null, manager, diagnostics, List.of("-proc:none"), null,
                    manager.getJavaFileObjectsFromPaths(files));
            var positions = Trees.instance(task).getSourcePositions();
            for (var unit : task.parse()) {
                if (unit.getPackageName() == null) continue;
                String pkg = unit.getPackageName().toString();
                if (!pkg.startsWith(PREFIX.substring(0, PREFIX.length() - 1))) continue;
                String canonicalPackage = pkg.replace("org.bukkit.craftbukkit.v1_20_R1", "org.bukkit.craftbukkit");
                Path file = Path.of(unit.getSourceFile().toUri()).toAbsolutePath();
                String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
                new TreeScanner<Void, String>() {
                    @Override public Void visitClass(ClassTree type, String enclosing) {
                        if (type.getSimpleName().length() == 0) return null;
                        String name = enclosing + type.getSimpleName();
                        var fields = new ArrayList<String>();
                        var methods = new ArrayList<Method>();
                        for (Tree member : type.getMembers()) {
                            if (member instanceof VariableTree field) fields.add(field.getType() + " " + field.getName());
                            if (member instanceof MethodTree method) {
                                String signature = method.getName() + "(" + String.join(",", method.getParameters().stream()
                                        .map(parameter -> parameter.getType().toString()).toList()) + "):" + method.getReturnType();
                                boolean[] unsupported = {false};
                                if (method.getBody() != null) method.getBody().accept(new TreeScanner<Void, Void>() {
                                    @Override public Void visitNewClass(NewClassTree expression, Void ignored) {
                                        if (expression.getIdentifier().toString().endsWith("UnsupportedOperationException")) unsupported[0] = true;
                                        return super.visitNewClass(expression, ignored);
                                    }
                                    @Override public Void visitMethodInvocation(MethodInvocationTree expression, Void ignored) {
                                        if (expression.getMethodSelect().toString().equals("unsupported")) unsupported[0] = true;
                                        return super.visitMethodInvocation(expression, ignored);
                                    }
                                }, null);
                                methods.add(new Method(signature, unit.getLineMap().getLineNumber(positions.getStartPosition(unit, method)), unsupported[0]));
                            }
                        }
                        var source = new SourceType(name, type.getKind().name(), file.toString(), hash,
                                unit.getLineMap().getLineNumber(positions.getStartPosition(unit, type)),
                                Objects.toString(type.getExtendsClause(), null), type.getImplementsClause().stream().map(Object::toString).toList(), fields, methods);
                        // The runtime keeps a legacy package shim beside the versioned implementation.
                        // Compare the real versioned source when both canonicalize to one Leaf type.
                        var existing = result.get(name);
                        if (existing == null) {
                            result.put(name, source);
                        } else {
                            boolean sourceVersioned = isVersionedCraftSource(file);
                            boolean existingVersioned = isVersionedCraftSource(Path.of(existing.file));
                            if (sourceVersioned && !existingVersioned) result.put(name, source);
                            else if (sourceVersioned == existingVersioned) {
                                throw new IllegalStateException("Duplicate Craft source type at same ABI priority: " + name);
                            }
                        }
                        for (Tree member : type.getMembers()) if (member instanceof ClassTree) scan(member, name + "$");
                        return null;
                    }
                }.scan(unit, canonicalPackage + ".");
            }
        }
        for (var diagnostic : diagnostics.getDiagnostics()) if (diagnostic.getKind() == Diagnostic.Kind.ERROR)
            throw new IllegalStateException("Cannot parse Craft source: " + diagnostic);
        return result;
    }

    private static boolean isVersionedCraftSource(Path file) {
        return file.toString().contains("/org/bukkit/craftbukkit/v1_20_R1/");
    }
}
