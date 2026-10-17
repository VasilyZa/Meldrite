package dev.meldrite.v1201.compat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.meldrite.plugin.classloading.RuntimeInstrumentation;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

/** On-demand metadata audit of actual Fabric classes. Never labels server behavior as supported. */
public final class NativeRuntimeAbiAudit {
    private static final String BUILTIN = "/meldrite/abi/1.20.1.json";
    private static final int MAX_REQUESTS = 20_000;
    private static final long MAX_INPUT_BYTES = 16L * 1024 * 1024;
    private static final Pattern OBJECT_TYPE = Pattern.compile("L([^;]+);");

    public record Report(Map<String, Object> document) {
        public boolean completeWithoutGaps() {
            return "NO_STRUCTURAL_GAPS_IN_CHECKED_SCOPE".equals(document.get("status"));
        }
    }

    private NativeRuntimeAbiAudit() { }

    public static Report inspect() throws IOException {
        long started = System.nanoTime();
        MappingResolver mappings = FabricLoader.getInstance().getMappingResolver();
        ClassLoader runtimeLoader = NativeRuntimeAbiAudit.class.getClassLoader();
        var report = new LinkedHashMap<String, Object>();
        report.put("schemaVersion", 1);
        report.put("minecraft", "1.20.1");
        report.put("meldrite", FabricLoader.getInstance().getModContainer("meldrite").orElseThrow()
                .getMetadata().getVersion().getFriendlyString());
        report.put("runtimeNamespace", mappings.getCurrentRuntimeNamespace());
        report.put("createdAt", Instant.now().toString());
        report.put("scope", "Actual class declarations and exact interface dispatch metadata; loaded caller classes do not prove branch execution");
        report.put("behaviorStatus", "NOT_EXECUTED");
        report.put("auditCodeSource", source(NativeRuntimeAbiAudit.class));
        var checks = new ArrayList<Map<String, Object>>();
        var interfaces = new ArrayList<Map<String, Object>>();
        List<Class<?>> loaded = List.of();
        boolean enumerationAvailable = true;
        try { loaded = Arrays.asList(RuntimeInstrumentation.require().getAllLoadedClasses()); }
        catch (IOException failure) {
            enumerationAvailable = false;
            report.put("classEnumerationFailure", failure.toString());
        }
        report.put("loadedClassEnumerationAvailable", enumerationAvailable);
        report.put("loadedClassesObserved", loaded.size());
        var loadedByName = new HashMap<String, List<Class<?>>>();
        var originFingerprints = new HashMap<String, Map<String, Object>>();
        for (Class<?> type : loaded) loadedByName.computeIfAbsent(type.getName(), key -> new ArrayList<>()).add(type);

        JsonObject builtin;
        try (var stream = NativeRuntimeAbiAudit.class.getResourceAsStream(BUILTIN)) {
            if (stream == null) throw new IOException("NATIVE_ABI_CONTRACTS_MISSING: " + BUILTIN);
            byte[] bytes = stream.readAllBytes();
            report.put("builtinManifestSha256", digest(bytes));
            builtin = manifest(bytes);
        }
        JsonArray builtinRequests = requests(builtin);
        for (JsonElement element : builtinRequests) {
            interrupted();
            checks.add(checkRequest(element.getAsJsonObject(), mappings, runtimeLoader));
        }
        report.put("builtinRequestsChecked", builtinRequests.size());

        int nativeClasses = 0;
        for (Class<?> type : loaded.stream().sorted(Comparator.comparing(Class::getName)).toList()) {
            interrupted();
            if (type.getClassLoader() != runtimeLoader || !type.getName().startsWith("net.minecraft.")
                    || type.isInterface() || java.lang.reflect.Modifier.isAbstract(type.getModifiers())) continue;
            ++nativeClasses;
            try {
                for (Class<?> contract : RuntimeAbiVerifier.interfaces(type)) {
                    if (!contract.getName().startsWith("dev.meldrite.v1201.")) continue;
                    for (var finding : RuntimeAbiVerifier.checkInterface(type, contract)) {
                        var entry = new LinkedHashMap<String, Object>();
                        entry.put("interface", contract.getName()); entry.put("finding", finding);
                        interfaces.add(entry);
                    }
                }
            } catch (LinkageError | SecurityException failure) {
                interfaces.add(Map.of("interface", "UNRESOLVED", "finding", new RuntimeAbiVerifier.Finding(
                        type.getName(), "interface", null, null, "METADATA_LINKAGE_ERROR", null, failure.toString())));
            }
        }
        report.put("loadedConcreteNativeClassesInspected", nativeClasses);
        report.put("interfaceMethodsChecked", interfaces.stream()
                .map(entry -> (RuntimeAbiVerifier.Finding) entry.get("finding"))
                .filter(finding -> "interface-method".equals(finding.kind())).count());
        report.put("interfaceFindings", interfaces.size());

        int externalChecked = 0, externalSkipped = 0;
        String configured = System.getProperty("meldrite.abi.requests");
        if (configured != null && !configured.isBlank()) {
            Path input = Path.of(configured).toAbsolutePath().normalize();
            if (!Files.isRegularFile(input) || Files.size(input) > MAX_INPUT_BYTES)
                throw new IOException("NATIVE_ABI_REQUEST_INPUT_INVALID: " + input);
            byte[] bytes;
            try (var stream = Files.newInputStream(input)) { bytes = stream.readNBytes((int) MAX_INPUT_BYTES + 1); }
            if (bytes.length > MAX_INPUT_BYTES) throw new IOException("NATIVE_ABI_REQUEST_INPUT_TOO_LARGE: " + input);
            report.put("externalManifest", Map.of("path", input.toString(), "sha256", digest(bytes)));
            JsonObject external = manifest(bytes);
            for (JsonElement element : requests(external)) {
                interrupted();
                JsonObject request = element.getAsJsonObject();
                String callerClass = string(request, "callerClass");
                String expectedHash = string(request, "pluginSha256");
                if (!"LOADED_CALLER_CLASS".equals(string(request, "scope")) || callerClass == null
                        || expectedHash == null || !expectedHash.matches("[0-9a-fA-F]{64}"))
                    throw new IOException("NATIVE_ABI_EXTERNAL_REQUEST_REQUIRES_CALLER_AND_JAR_IDENTITY");
                String caller = callerClass.replace('/', '.');
                List<Class<?>> callers = loadedByName.getOrDefault(caller, List.of());
                var entry = new LinkedHashMap<String, Object>();
                entry.put("request", request);
                entry.put("branchReachability", "NOT_EXECUTED");
                if (callers.isEmpty()) {
                    entry.put("status", "NOT_CHECKED_CALLER_UNLOADED"); ++externalSkipped;
                } else {
                    var origins = new ArrayList<Map<String, Object>>();
                    for (Class<?> type : callers) origins.add(callerSource(type, originFingerprints));
                    entry.put("callerOrigins", origins);
                    boolean matched = origins.stream().anyMatch(origin -> expectedHash.equalsIgnoreCase(
                            String.valueOf(origin.get("jarSha256"))));
                    if (matched) {
                        entry.put("callerIdentity", "MATCHED_JAR_SHA256_AT_AUDIT_TIME");
                        entry.putAll(checkRequest(request, mappings, runtimeLoader));
                        ++externalChecked;
                    } else {
                        boolean unavailable = origins.stream().anyMatch(origin -> !origin.containsKey("jarSha256"));
                        entry.put("status", unavailable ? "NOT_CHECKED_CALLER_IDENTITY_UNAVAILABLE"
                                : "NOT_CHECKED_CALLER_SOURCE_MISMATCH");
                        ++externalSkipped;
                    }
                }
                checks.add(entry);
            }
        }
        report.put("externalRequestsChecked", externalChecked);
        report.put("externalRequestsSkipped", externalSkipped);
        report.put("checks", checks);
        report.put("interfaceChecks", interfaces);
        long gaps = checks.stream().filter(entry -> entry.containsKey("findings"))
                .flatMap(entry -> castFindings(entry.get("findings")).stream()).filter(NativeRuntimeAbiAudit::gap).count()
                + interfaces.stream().map(entry -> (RuntimeAbiVerifier.Finding) entry.get("finding"))
                .filter(NativeRuntimeAbiAudit::gap).count();
        long unchecked = checks.stream().filter(entry -> entry.containsKey("findings"))
                .flatMap(entry -> castFindings(entry.get("findings")).stream())
                .filter(finding -> "ABSTRACT_RECEIVER_NOT_CHECKED".equals(finding.status())).count();
        report.put("uncheckedFindings", unchecked);
        report.put("structuralGaps", gaps);
        report.put("status", gaps != 0 ? "STRUCTURAL_GAPS_FOUND"
                : !enumerationAvailable || externalSkipped != 0 || unchecked != 0 ? "INCOMPLETE"
                : "NO_STRUCTURAL_GAPS_IN_CHECKED_SCOPE");
        report.put("durationMillis", (System.nanoTime() - started) / 1_000_000L);
        return new Report(java.util.Collections.unmodifiableMap(report));
    }

    public static void write(Report report, Path destination) throws IOException {
        interrupted();
        Path output = destination.toAbsolutePath().normalize();
        Files.createDirectories(output.getParent());
        Path temporary = Files.createTempFile(output.getParent(), "native-abi-", ".json");
        try {
            Files.writeString(temporary, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
                    .toJson(report.document()) + "\n", StandardCharsets.UTF_8);
            Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally { Files.deleteIfExists(temporary); }
    }

    private static Map<String, Object> checkRequest(JsonObject request, MappingResolver mappings, ClassLoader loader) {
        String ownerName = Objects.requireNonNull(string(request, "owner"), "ABI owner");
        String kind = Objects.requireNonNull(string(request, "kind"), "ABI kind");
        String name = string(request, "name");
        String descriptor = Objects.requireNonNull(string(request, "descriptor"), "ABI descriptor");
        var entry = new LinkedHashMap<String, Object>(); entry.put("request", request);
        String runtimeOwner = mappings.mapClassName("intermediary", ownerName.replace('/', '.'));
        String runtimeDescriptor = mapDescriptor(mappings, descriptor);
        try {
            Class<?> owner = Class.forName(runtimeOwner, false, loader);
            entry.put("ownerOrigin", source(owner));
            if (kind.equals("interface") || kind.equals("assignable")) {
                if (!runtimeDescriptor.startsWith("L") || !runtimeDescriptor.endsWith(";"))
                    throw new IllegalArgumentException("Expected a reference descriptor for " + kind);
                Class<?> contract = Class.forName(runtimeDescriptor.substring(1, runtimeDescriptor.length() - 1).replace('/', '.'), false, loader);
                if (kind.equals("interface")) entry.put("findings", RuntimeAbiVerifier.checkInterface(owner, contract,
                        request.has("declared") && request.get("declared").getAsBoolean()));
                else entry.put("findings", List.of(new RuntimeAbiVerifier.Finding(runtimeOwner, kind, contract.getName(),
                        runtimeDescriptor, contract.isAssignableFrom(owner) ? "STRUCTURAL_MATCH" : "HIERARCHY_MISMATCH",
                        null, "Assignability only; behavior is not executed")));
            } else {
                String runtimeName = name;
                if (kind.equals("field")) runtimeName = mappings.mapFieldName("intermediary", ownerName.replace('/', '.'), name, descriptor);
                else if (kind.equals("method")) runtimeName = mappings.mapMethodName("intermediary", ownerName.replace('/', '.'), name, descriptor);
                Boolean expectedStatic = request.has("static") ? request.get("static").getAsBoolean() : null;
                boolean declared = request.has("declared") && request.get("declared").getAsBoolean();
                entry.put("findings", List.of(RuntimeAbiVerifier.checkMember(owner, kind, runtimeName, runtimeDescriptor,
                        expectedStatic, request.has("final") ? request.get("final").getAsBoolean() : null,
                        string(request, "visibility"), declared)));
            }
        } catch (ClassNotFoundException | LinkageError | SecurityException failure) {
            entry.put("findings", List.of(new RuntimeAbiVerifier.Finding(runtimeOwner, kind, name, runtimeDescriptor,
                    "CLASS_LINKAGE_ERROR", null, failure.toString())));
        }
        return entry;
    }

    private static JsonObject manifest(byte[] bytes) throws IOException {
        try {
            JsonElement root = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
            if (!root.isJsonObject()) throw new IOException("NATIVE_ABI_REQUEST_SCHEMA_MISMATCH");
            return root.getAsJsonObject();
        } catch (com.google.gson.JsonParseException failure) {
            throw new IOException("NATIVE_ABI_REQUEST_INVALID_JSON", failure);
        }
    }
    private static JsonArray requests(JsonObject manifest) throws IOException {
        try {
            JsonElement version = manifest.get("schemaVersion");
            if (version == null || !version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber()
                    || version.getAsBigDecimal().compareTo(java.math.BigDecimal.ONE) != 0
                    || !textValue(manifest.get("minecraft"), "1.20.1")
                    || !textValue(manifest.get("namespace"), "intermediary")
                    || !manifest.has("requests") || !manifest.get("requests").isJsonArray())
                throw new IOException("NATIVE_ABI_REQUEST_SCHEMA_MISMATCH");
        } catch (NumberFormatException failure) {
            throw new IOException("NATIVE_ABI_REQUEST_SCHEMA_MISMATCH", failure);
        }
        JsonArray requests = manifest.getAsJsonArray("requests");
        if (requests.size() == 0 || requests.size() > MAX_REQUESTS)
            throw new IOException("NATIVE_ABI_REQUEST_COUNT_INVALID: " + requests.size());
        for (int index = 0; index < requests.size(); ++index) {
            interrupted();
            JsonElement element = requests.get(index);
            if (!element.isJsonObject()) throw invalid(index, "request must be an object");
            validateRequest(element.getAsJsonObject(), index);
        }
        return requests;
    }
    private static void validateRequest(JsonObject request, int index) throws IOException {
        for (String key : List.of("owner", "kind", "descriptor")) {
            JsonElement value = request.get(key);
            if (!isText(value) || value.getAsString().isBlank()) throw invalid(index, "missing string " + key);
        }
        String owner = string(request, "owner"), kind = string(request, "kind"), descriptor = string(request, "descriptor");
        if (!className(owner.replace('.', '/'))) throw invalid(index, "invalid owner");
        if (!List.of("field", "method", "constructor", "interface", "assignable").contains(kind))
            throw invalid(index, "unknown kind " + kind);
        for (String key : List.of("static", "final", "declared")) {
            JsonElement value = request.get(key);
            if (value != null && (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()))
                throw invalid(index, "expected boolean " + key);
        }
        for (String key : List.of("name", "visibility", "callerClass", "pluginSha256", "scope")) {
            JsonElement value = request.get(key);
            if (value != null && !value.isJsonNull() && !isText(value)) throw invalid(index, "expected string " + key);
        }
        if (kind.equals("interface") || kind.equals("assignable")) {
            var inapplicable = new ArrayList<>(List.of("static", "final", "visibility"));
            if (kind.equals("assignable")) inapplicable.add("declared");
            for (String key : inapplicable) if (request.has(key))
                throw invalid(index, key + " is not defined for " + kind);
        }
        String visibility = string(request, "visibility");
        if (visibility != null && !List.of("public", "protected", "private", "package").contains(visibility))
            throw invalid(index, "invalid visibility");
        if (kind.equals("method") || kind.equals("field") || kind.equals("constructor")) {
            String name = string(request, "name");
            if (name == null || name.isBlank() || name.matches(".*[.;\\[/<>].*")) {
                if (!kind.equals("constructor") || !"<init>".equals(name)) throw invalid(index, "invalid member name");
            }
            if (kind.equals("constructor") && !"<init>".equals(name)) throw invalid(index, "constructor must be <init>");
        }
        try {
            boolean method = kind.equals("method") || kind.equals("constructor");
            int end;
            if (method) {
                if (!descriptor.startsWith("(")) throw new IllegalArgumentException();
                int cursor = 1;
                while (cursor < descriptor.length() && descriptor.charAt(cursor) != ')') cursor = typeEnd(descriptor, cursor, false);
                if (cursor >= descriptor.length()) throw new IllegalArgumentException();
                end = typeEnd(descriptor, cursor + 1, true);
                if (kind.equals("constructor") && !descriptor.endsWith(")V")) throw new IllegalArgumentException();
            } else {
                end = typeEnd(descriptor, 0, false);
                if ((kind.equals("interface") || kind.equals("assignable")) && !descriptor.startsWith("L"))
                    throw new IllegalArgumentException();
            }
            if (end != descriptor.length()) throw new IllegalArgumentException();
        } catch (IllegalArgumentException failure) { throw invalid(index, "invalid JVM descriptor " + descriptor); }
    }
    private static int typeEnd(String descriptor, int cursor, boolean allowVoid) {
        if (cursor >= descriptor.length()) throw new IllegalArgumentException();
        char type = descriptor.charAt(cursor++);
        if ("BCDFIJSZ".indexOf(type) >= 0 || (type == 'V' && allowVoid)) return cursor;
        if (type == '[') {
            int dimensions = 1;
            while (cursor < descriptor.length() && descriptor.charAt(cursor) == '[') { ++cursor; ++dimensions; }
            if (dimensions > 255) throw new IllegalArgumentException();
            return typeEnd(descriptor, cursor, false);
        }
        if (type == 'L') {
            int end = descriptor.indexOf(';', cursor);
            if (end < 0 || !className(descriptor.substring(cursor, end))) throw new IllegalArgumentException();
            return end + 1;
        }
        throw new IllegalArgumentException();
    }
    private static boolean className(String name) {
        if (name.isBlank() || name.startsWith("/") || name.endsWith("/") || name.contains("//")) return false;
        for (int index = 0; index < name.length(); ++index) {
            char value = name.charAt(index);
            if (Character.isWhitespace(value) || ".;[]():".indexOf(value) >= 0) return false;
        }
        return true;
    }
    private static boolean isText(JsonElement value) {
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
    }
    private static boolean textValue(JsonElement value, String expected) {
        return isText(value) && expected.equals(value.getAsString());
    }
    private static IOException invalid(int index, String reason) {
        return new IOException("NATIVE_ABI_REQUEST_INVALID[" + index + "]: " + reason);
    }
    private static String mapDescriptor(MappingResolver mappings, String descriptor) {
        var matcher = OBJECT_TYPE.matcher(descriptor); var result = new StringBuilder();
        while (matcher.find()) matcher.appendReplacement(result, java.util.regex.Matcher.quoteReplacement("L"
                + mappings.mapClassName("intermediary", matcher.group(1).replace('/', '.')).replace('.', '/') + ";"));
        matcher.appendTail(result); return result.toString();
    }
    private static String string(JsonObject object, String key) {
        JsonElement value = object.get(key); return value == null || value.isJsonNull() ? null : value.getAsString();
    }
    private static Map<String, Object> source(Class<?> type) {
        var result = new LinkedHashMap<String, Object>(); result.put("class", type.getName());
        result.put("loader", String.valueOf(type.getClassLoader()));
        try {
            var domain = type.getProtectionDomain(); var code = domain == null ? null : domain.getCodeSource();
            result.put("location", code == null || code.getLocation() == null ? null : code.getLocation().toExternalForm());
        } catch (SecurityException failure) { result.put("sourceUnavailable", failure.toString()); }
        return result;
    }
    /** CodeSource identifies the original plugin JAR, not a same-named class in another realm. */
    private static Map<String, Object> callerSource(Class<?> type, Map<String, Map<String, Object>> cache)
            throws java.io.InterruptedIOException {
        var origin = new LinkedHashMap<>(source(type));
        Object location = origin.get("location");
        if (location == null) {
            origin.put("fingerprintStatus", "SOURCE_UNAVAILABLE");
            return origin;
        }
        String key = location.toString();
        Map<String, Object> fingerprint = cache.get(key);
        if (fingerprint == null) {
            fingerprint = new LinkedHashMap<>();
            try {
                URI uri = URI.create(key);
                if (!"file".equalsIgnoreCase(uri.getScheme())) throw new IOException("Source is not a local JAR");
                Path jar = Path.of(uri);
                if (!Files.isRegularFile(jar)) throw new IOException("Source is not a regular JAR file");
                MessageDigest digest = sha256();
                try (var input = Files.newInputStream(jar)) {
                    byte[] buffer = new byte[64 * 1024];
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        interrupted();
                        digest.update(buffer, 0, count);
                    }
                }
                fingerprint.put("jarSha256", HexFormat.of().formatHex(digest.digest()));
                fingerprint.put("fingerprintStatus", "HASHED_CODE_SOURCE_AT_AUDIT_TIME");
            } catch (java.io.InterruptedIOException failure) { throw failure; }
            catch (IOException | IllegalArgumentException | SecurityException failure) {
                fingerprint.put("fingerprintStatus", "SOURCE_UNAVAILABLE");
                fingerprint.put("fingerprintFailure", failure.toString());
            }
            cache.put(key, fingerprint);
        }
        origin.putAll(fingerprint);
        return origin;
    }
    private static boolean gap(RuntimeAbiVerifier.Finding finding) {
        return !finding.matched() && !"ABSTRACT_RECEIVER_NOT_CHECKED".equals(finding.status());
    }
    @SuppressWarnings("unchecked") private static List<RuntimeAbiVerifier.Finding> castFindings(Object value) {
        return (List<RuntimeAbiVerifier.Finding>) value;
    }
    private static String digest(byte[] bytes) {
        return HexFormat.of().formatHex(sha256().digest(bytes));
    }
    private static MessageDigest sha256() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException failure) { throw new IllegalStateException("SHA-256 unavailable", failure); }
    }
    private static void interrupted() throws java.io.InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) throw new java.io.InterruptedIOException("Native ABI audit cancelled");
    }
}
