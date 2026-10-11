package dev.meldrite.contract;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Resolves one fixed Maven coordinate and stores its bytes in the project artifact cache. */
public final class ArtifactLocker {
    private static final ThreadLocal<URI> CENTRAL_MIRROR = new ThreadLocal<>();
    private static final Pattern GROUP_ID = Pattern.compile("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*");
    private static final Pattern ARTIFACT_ID = Pattern.compile("[A-Za-z0-9_.+-]+");
    private static final Pattern VERSION = Pattern.compile("[A-Za-z0-9_.+-]+");
    private static final Pattern CLASSIFIER = Pattern.compile("[A-Za-z0-9_.+-]+");
    private ArtifactLocker() {}

    public static ArtifactLock resolve(String coordinate, URI repository, Path projectRoot) throws IOException {
        return resolve(coordinate, List.of(repository), projectRoot.toAbsolutePath().normalize(), new HashSet<>());
    }

    public static ArtifactLock resolve(String coordinate, List<URI> repositories, Path projectRoot) throws IOException {
        return resolve(coordinate, List.copyOf(repositories), projectRoot.toAbsolutePath().normalize(), new HashSet<>());
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0 || !args[0].equals("lock") || args.length < 2) {
            throw new IllegalArgumentException("Usage: ArtifactLocker lock <lock-input.json> [--project-root <path>|--cache <path>] [--central-repository <url>]");
        }
        Path inputPath = Path.of(args[1]);
        Path projectRoot = Path.of(".").toAbsolutePath().normalize();
        for (int i = 2; i < args.length; i++) {
            if ((args[i].equals("--project-root") || args[i].equals("--cache")) && i + 1 < args.length) projectRoot = Path.of(args[++i]).toAbsolutePath().normalize();
            else if (args[i].equals("--central-repository") && i + 1 < args.length) CENTRAL_MIRROR.set(withTrailingSlash(URI.create(args[++i])));
        }
        try {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        var input = mapper.readTree(inputPath.toFile());
        var roots = new ArrayList<ArtifactLock>();
        var rootInputs = input.path("roots");
        if (rootInputs.isArray()) {
            for (var rootInput : rootInputs) roots.addAll(resolveInput(rootInput, input, projectRoot));
        } else {
            roots.addAll(resolveInput(input, input, projectRoot));
        }
        Path output = Path.of(input.path("output").asText("contracts/locks/generated.lock.json"));
        if (!output.isAbsolute()) output = projectRoot.resolve(output);
        if (roots.isEmpty()) throw new IOException("Lock input has no roots");
        String sourceCommit = input.path("upstreamSourceCommit").asText(null);
        new ArtifactLockFile(1, roots, sourceCommit,
                java.time.Instant.now().toString(), sourceCommit == null ? null : "REFERENCE_ONLY").write(output);
        System.out.println("Wrote " + roots.size() + " root lock(s) to " + output);
        } finally {
            CENTRAL_MIRROR.remove();
        }
    }

    private static List<ArtifactLock> resolveInput(com.fasterxml.jackson.databind.JsonNode input,
            com.fasterxml.jackson.databind.JsonNode common, Path projectRoot) throws IOException {
        var repositories = input.has("repositories") ? input.path("repositories") : common.path("repositories");
        List<URI> urls = new ArrayList<>();
        if (repositories.isArray()) for (var repo : repositories) urls.add(URI.create(repo.isTextual() ? repo.asText() : repo.path("url").asText()));
        else if (repositories.isTextual()) urls.add(URI.create(repositories.asText()));
        if (urls.isEmpty()) throw new IOException("Lock input requires repositories for " + input.path("coordinate").asText());
        String coordinate = input.path("coordinate").asText();
        String cache = common.path("cache").asText(".gradle/meldrite/artifacts");
        if (!cache.equals(".gradle/meldrite/artifacts")) throw new IOException("Unsupported artifact cache path: " + cache);
        String policy = common.path("snapshotPolicy").asText("unique-unclassified");
        if (!policy.equals("unique-unclassified")) throw new IOException("Unsupported snapshot policy: " + policy);
        return List.of(resolve(coordinate, urls, projectRoot, new HashSet<>()));
    }

    private static ArtifactLock resolve(String coordinate, List<URI> repositories, Path root, Set<String> visiting) throws IOException {
        return resolve(coordinate, repositories, root, visiting, Set.of());
    }

    private static ArtifactLock resolve(String coordinate, List<URI> repositories, Path root, Set<String> visiting,
            Set<String> exclusions) throws IOException {
        Coordinate parsed = Coordinate.parse(coordinate);
        rejectDynamic(parsed.version, coordinate);
        if (!visiting.add(coordinate)) throw new IOException("Cyclic Maven dependency while resolving " + coordinate);
        try {
            String groupPath = parsed.group.replace('.', '/');
            String relative = groupPath + "/" + parsed.artifact + "/" + parsed.version + "/";
            URI repository = null;
            URI artifactUri = null;
            URI pomUri = null;
            String uniqueVersion = null;
            String uniquePomVersion = null;
            byte[] artifact = null;
            byte[] pom = null;
            IOException last = null;
            for (URI candidate : repositories) {
                try {
                    SnapshotVersions versions = snapshotVersions(candidate, groupPath, parsed);
                    String resolvedVersion = versions.jarVersion;
                    String pomVersion = versions.pomVersion;
                    String candidateJar = parsed.artifact + "-" + resolvedVersion
                            + (parsed.classifier.isEmpty() ? "" : "-" + parsed.classifier) + ".jar";
                    String candidatePom = parsed.artifact + "-" + pomVersion + ".pom";
                    URI candidateArtifactUri = candidate.resolve(relative + candidateJar);
                    URI candidatePomUri = candidate.resolve(relative + candidatePom);
                    byte[] candidatePomBytes = download(candidatePomUri);
                    byte[] candidateArtifactBytes = download(candidateArtifactUri);
                    repository = candidate;
                    uniqueVersion = resolvedVersion;
                    uniquePomVersion = pomVersion;
                    artifactUri = candidateArtifactUri;
                    pomUri = candidatePomUri;
                    pom = candidatePomBytes;
                    artifact = candidateArtifactBytes;
                    break;
                } catch (InvalidSnapshotMetadataException ex) {
                    throw ex;
                } catch (IOException ex) { last = ex; }
            }
            if (repository == null) throw new IOException("Could not resolve " + coordinate + " from configured repositories", last);
            String jarFile = parsed.artifact + "-" + uniqueVersion + (parsed.classifier.isEmpty() ? "" : "-" + parsed.classifier) + ".jar";
            String pomVersion = uniquePomVersion;
            String pomFile = parsed.artifact + "-" + pomVersion + ".pom";
            Path cacheBase = artifactCacheBase(root, parsed);
            Path artifactCache = cacheBase.resolve(jarFile).normalize();
            Path pomCache = cacheBase.resolve(pomFile).normalize();
            ensureWithinCache(root, artifactCache);
            ensureWithinCache(root, pomCache);
            Files.createDirectories(cacheBase);
            Files.write(artifactCache, artifact);
            Files.write(pomCache, pom);
            List<ArtifactLock> dependencies = dependencies(coordinate, pom, repositories, root, visiting, exclusions);
            return new ArtifactLock(coordinate, uniqueVersion, artifactUri.toString(), pomUri.toString(),
                    root.relativize(artifactCache).toString().replace('\\', '/'), root.relativize(pomCache).toString().replace('\\', '/'),
                    LockVerifier.sha256(artifact), LockVerifier.sha256(pom), dependencies);
        } finally {
            visiting.remove(coordinate);
        }
    }

    private static List<ArtifactLock> dependencies(String coordinate, byte[] pomBytes, List<URI> repositories,
            Path root, Set<String> visiting, Set<String> exclusions) throws IOException {
        PomModel model = effectivePom(coordinate, pomBytes, repositories, root, new HashSet<>());
        List<ArtifactLock> result = new ArrayList<>(model.auxiliaryLocks);
        for (DependencySpec dependency : model.dependencies) {
            String group = expand(dependency.group, model.properties);
            String artifact = expand(dependency.artifact, model.properties);
            if (isExcluded(exclusions, group, artifact)) continue;
            DependencySpec managed = model.managed.get(key(group, artifact, dependency.type, dependency.classifier));
            String version = dependency.version.isBlank() && managed != null ? managed.version : dependency.version;
            String scope = dependency.scope.isBlank() && managed != null ? managed.scope : dependency.scope;
            String type = dependency.type.isBlank() && managed != null ? managed.type : dependency.type;
            String classifier = dependency.classifier.isBlank() && managed != null ? managed.classifier : dependency.classifier;
            String optional = dependency.optional.isBlank() && managed != null ? managed.optional : dependency.optional;
            version = expand(version, model.properties);
            scope = scope.isBlank() ? "compile" : expand(scope, model.properties);
            type = type.isBlank() ? "jar" : expand(type, model.properties);
            classifier = expand(classifier, model.properties);
            if (scope.equals("test") || scope.equals("provided") || scope.equals("system") || scope.equals("import")) continue;
            if (Boolean.parseBoolean(optional)) continue;
            if (version.isBlank()) throw new IOException("Dependency is missing a managed version: " + group + ":" + artifact);
            Set<String> childExclusions = new HashSet<>(exclusions);
            childExclusions.addAll(dependency.exclusions);
            if (managed != null) childExclusions.addAll(managed.exclusions);
            String depCoordinate = group + ":" + artifact + ":" + version + (classifier.isBlank() ? "" : ":" + classifier);
            if (type.equals("pom")) result.add(resolvePomOnly(depCoordinate, repositories, root, new HashSet<>()));
            else if (type.equals("jar")) result.add(resolve(depCoordinate, repositories, root, visiting, childExclusions));
            else throw new IOException("Unsupported Maven dependency type '" + type + "' for " + depCoordinate);
        }
        return result;
    }

    private static PomModel effectivePom(String coordinate, byte[] pomBytes, List<URI> repositories, Path root,
            Set<String> modelVisiting) throws IOException {
        if (!modelVisiting.add(coordinate)) throw new IOException("Cyclic Maven parent/BOM model at " + coordinate);
        try {
            Element project = parseXml(pomBytes).getDocumentElement();
            Coordinate self = Coordinate.parse(coordinate);
            Element parentElement = child(project, "parent");
            PomModel parent = null;
            ArtifactLock parentLock = null;
            if (parentElement != null) {
                String pg = text(parentElement, "groupId", "");
                String pa = text(parentElement, "artifactId", "");
                String pv = text(parentElement, "version", "");
                String parentCoordinate = pg + ":" + pa + ":" + pv;
                ResolvedPom parentPom = fetchPom(parentCoordinate, repositories, root);
                parent = effectivePom(parentCoordinate, parentPom.bytes, repositories, root, modelVisiting);
                parentLock = pomLock(parentCoordinate, parentPom, parent.auxiliaryLocks);
            }

            var properties = new java.util.LinkedHashMap<String, String>();
            if (parent != null) properties.putAll(parent.properties);
            String group = text(project, "groupId", parent == null ? self.group : parent.group);
            String artifact = text(project, "artifactId", self.artifact);
            String version = text(project, "version", parent == null ? self.version : parent.version);
            Element projectProps = child(project, "properties");
            if (projectProps != null) for (Element property : children(projectProps)) properties.put(property.getTagName(), property.getTextContent().trim());
            properties.put("project.groupId", group);
            properties.put("pom.groupId", group);
            properties.put("project.artifactId", artifact);
            properties.put("pom.artifactId", artifact);
            properties.put("project.version", version);
            properties.put("pom.version", version);

            Map<String, DependencySpec> managed = new java.util.LinkedHashMap<>();
            if (parent != null) managed.putAll(parent.managed);
            List<ArtifactLock> auxiliary = new ArrayList<>();
            if (parentLock != null) auxiliary.add(parentLock);

            Element management = child(project, "dependencyManagement");
            Element managedEntries = management == null ? null : child(management, "dependencies");
            if (managedEntries != null) for (Element entry : children(managedEntries)) {
                DependencySpec spec = dependencySpec(entry, properties);
                if (spec.type.equals("pom") && spec.scope.equals("import")) {
                    if (spec.version.isBlank()) throw new IOException("Imported BOM has no version: " + spec.group + ":" + spec.artifact);
                    String bomCoordinate = spec.group + ":" + spec.artifact + ":" + spec.version;
                    ResolvedPom bomPom = fetchPom(bomCoordinate, repositories, root);
                    PomModel bom = effectivePom(bomCoordinate, bomPom.bytes, repositories, root, modelVisiting);
                    auxiliary.add(pomLock(bomCoordinate, bomPom, bom.auxiliaryLocks));
                    managed.putAll(bom.managed);
                } else {
                    managed.put(key(spec.group, spec.artifact, spec.type, spec.classifier), spec);
                }
            }

            List<DependencySpec> dependencies = new ArrayList<>();
            if (parent != null) dependencies.addAll(parent.dependencies);
            Element entries = child(project, "dependencies");
            if (entries != null) for (Element entry : children(entries)) dependencies.add(dependencySpec(entry, properties));
            return new PomModel(group, artifact, version, properties, managed, dependencies, auxiliary);
        } finally {
            modelVisiting.remove(coordinate);
        }
    }

    private static DependencySpec dependencySpec(Element element, java.util.Map<String, String> properties) throws IOException {
        String group = expand(text(element, "groupId", ""), properties);
        String artifact = expand(text(element, "artifactId", ""), properties);
        String version = expand(text(element, "version", ""), properties);
        String type = expand(text(element, "type", "jar"), properties);
        String classifier = expand(text(element, "classifier", ""), properties);
        String scope = expand(text(element, "scope", ""), properties);
        String optional = expand(text(element, "optional", ""), properties);
        Set<String> exclusions = new HashSet<>();
        Element exclusionsElement = child(element, "exclusions");
        if (exclusionsElement != null) for (Element exclusion : children(exclusionsElement)) {
            exclusions.add(expand(text(exclusion, "groupId", "*"), properties) + ":" + expand(text(exclusion, "artifactId", "*"), properties));
        }
        return new DependencySpec(group, artifact, version, type, classifier, scope, optional, exclusions);
    }

    private static String key(String group, String artifact, String type, String classifier) {
        return group + ":" + artifact + ":" + (type.isBlank() ? "jar" : type) + ":" + classifier;
    }

    private static boolean isExcluded(Set<String> exclusions, String group, String artifact) {
        for (String exclusion : exclusions) {
            String[] parts = exclusion.split(":", 2);
            if (parts.length == 2 && (parts[0].equals("*") || parts[0].equals(group))
                    && (parts[1].equals("*") || parts[1].equals(artifact))) return true;
        }
        return false;
    }

    private static ArtifactLock resolvePomOnly(String coordinate, List<URI> repositories, Path root,
            Set<String> modelVisiting) throws IOException {
        ResolvedPom pom = fetchPom(coordinate, repositories, root);
        PomModel model = effectivePom(coordinate, pom.bytes, repositories, root, modelVisiting);
        return pomLock(coordinate, pom, model.auxiliaryLocks);
    }

    private static ResolvedPom fetchPom(String coordinate, List<URI> repositories, Path root) throws IOException {
        Coordinate parsed = Coordinate.parse(coordinate);
        rejectDynamic(parsed.version, coordinate);
        String groupPath = parsed.group.replace('.', '/');
        String relative = groupPath + "/" + parsed.artifact + "/" + parsed.version + "/";
        IOException last = null;
        for (URI repository : repositories) {
            try {
                String version = snapshotVersions(repository, groupPath, parsed).pomVersion;
                String file = parsed.artifact + "-" + version + ".pom";
                URI url = repository.resolve(relative + file);
                byte[] bytes = download(url);
                Path cache = artifactCacheBase(root, parsed).resolve(file).normalize();
                ensureWithinCache(root, cache);
                Files.createDirectories(cache.getParent());
                Files.write(cache, bytes);
                return new ResolvedPom(version, url, cache, bytes);
            } catch (InvalidSnapshotMetadataException ex) {
                throw ex;
            } catch (IOException ex) { last = ex; }
        }
        throw new IOException("Could not resolve POM for " + coordinate + " from configured repositories", last);
    }

    private static ArtifactLock pomLock(String coordinate, ResolvedPom pom, List<ArtifactLock> dependencies) throws IOException {
        String relative = pom.cache.toString().replace('\\', '/');
        int marker = relative.indexOf(".gradle/meldrite/artifacts/");
        relative = marker >= 0 ? relative.substring(marker) : relative;
        String hash = LockVerifier.sha256(pom.bytes);
        return new ArtifactLock(coordinate, pom.resolvedVersion, pom.url.toString(), pom.url.toString(), relative, relative,
                hash, hash, dependencies);
    }

    private static Path artifactCacheBase(Path projectRoot, Coordinate coordinate) throws IOException {
        Path cacheRoot = projectRoot.resolve(".gradle/meldrite/artifacts").toAbsolutePath().normalize();
        Path cacheBase = cacheRoot.resolve(coordinate.group).resolve(coordinate.artifact)
                .resolve(coordinate.version).normalize();
        if (!cacheBase.startsWith(cacheRoot)) {
            throw new IOException("Maven coordinate escapes artifact cache root: " + coordinate);
        }
        return cacheBase;
    }

    private static void ensureWithinCache(Path projectRoot, Path destination) throws IOException {
        Path cacheRoot = projectRoot.resolve(".gradle/meldrite/artifacts").toAbsolutePath().normalize();
        Path normalized = destination.toAbsolutePath().normalize();
        if (!normalized.startsWith(cacheRoot)) {
            throw new IOException("Artifact cache destination escapes cache root: " + destination);
        }
    }

    private record ResolvedPom(String resolvedVersion, URI url, Path cache, byte[] bytes) {}
    private record DependencySpec(String group, String artifact, String version, String type, String classifier,
            String scope, String optional, Set<String> exclusions) {}
    private record PomModel(String group, String artifact, String version, java.util.Map<String, String> properties,
            Map<String, DependencySpec> managed, List<DependencySpec> dependencies, List<ArtifactLock> auxiliaryLocks) {}

    private static SnapshotVersions snapshotVersions(URI repository, String groupPath, Coordinate coordinate) throws IOException {
        if (!coordinate.version.endsWith("-SNAPSHOT")) return new SnapshotVersions(coordinate.version, coordinate.version);
        URI metadataUri = repository.resolve(groupPath + "/" + coordinate.artifact + "/" + coordinate.version + "/maven-metadata.xml");
        Document metadata = parseXml(download(metadataUri));
        String jarVersion = snapshotVersion(metadata, "jar");
        String pomVersion = snapshotVersion(metadata, "pom");
        String timestampVersion = timestampVersion(metadata, coordinate);
        if (jarVersion == null) jarVersion = timestampVersion;
        if (pomVersion == null) pomVersion = timestampVersion;
        if (jarVersion.isBlank() || pomVersion.isBlank()) {
            throw new InvalidSnapshotMetadataException("Snapshot metadata has no unique jar/POM version for " + coordinate + " at " + metadataUri);
        }
        validateSnapshotVersion(jarVersion, coordinate, "jar");
        validateSnapshotVersion(pomVersion, coordinate, "pom");
        return new SnapshotVersions(jarVersion, pomVersion);
    }

    private static String snapshotVersion(Document metadata, String extension) {
        Element versions = first(metadata.getDocumentElement(), "snapshotVersions");
        if (versions != null) for (Element version : children(versions)) {
            if (text(version, "extension", "").equals(extension) && text(version, "classifier", "").isEmpty()) {
                return text(version, "value", "");
            }
        }
        return null;
    }

    private static String timestampVersion(Document metadata, Coordinate coordinate) {
        Element snapshot = first(metadata.getDocumentElement(), "snapshot");
        String timestamp = snapshot == null ? "" : text(snapshot, "timestamp", "");
        String build = snapshot == null ? "" : text(snapshot, "buildNumber", "");
        if (!timestamp.isBlank() && !build.isBlank()) return coordinate.version.substring(0, coordinate.version.length() - "SNAPSHOT".length()) + timestamp + "-" + build;
        return "";
    }

    private static void validateSnapshotVersion(String resolvedVersion, Coordinate requested, String extension) throws IOException {
        String base = requested.version.substring(0, requested.version.length() - "-SNAPSHOT".length());
        Pattern timestamped = Pattern.compile(Pattern.quote(base) + "-\\d{8}\\.\\d{6}-\\d+");
        if (!timestamped.matcher(resolvedVersion).matches() || !VERSION.matcher(resolvedVersion).matches()) {
            throw new InvalidSnapshotMetadataException(
                    "Invalid non-unique snapshot version for " + requested + " (" + extension + "): " + resolvedVersion);
        }
    }

    private static final class InvalidSnapshotMetadataException extends IOException {
        private InvalidSnapshotMetadataException(String message) { super(message); }
    }

    private record SnapshotVersions(String jarVersion, String pomVersion) {}

    private static byte[] download(URI uri) throws IOException {
        URI mirror = CENTRAL_MIRROR.get();
        boolean useMirror = mirror != null && uri.getHost() != null
                && uri.getHost().equalsIgnoreCase("repo.maven.apache.org")
                && uri.getPath() != null;
        if (useMirror) {
            String path = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
            if (path.startsWith("maven2/")) path = path.substring("maven2/".length());
            URI redirected = mirror.resolve(path);
            try { return downloadDirect(redirected); }
            catch (IOException ex) {
                throw new IOException("Configured Maven Central mirror failed for canonical source " + uri + " via " + redirected, ex);
            }
        }
        return downloadDirect(uri);
    }

    private static URI withTrailingSlash(URI uri) {
        return URI.create(uri.toString().endsWith("/") ? uri.toString() : uri + "/");
    }

    private static byte[] downloadDirect(URI uri) throws IOException {
        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(20_000);
        try (InputStream stream = connection.getInputStream()) { return stream.readAllBytes(); }
    }

    private static Document parseXml(byte[] bytes) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(bytes));
        } catch (Exception ex) { throw new IOException("Cannot parse Maven XML metadata/POM", ex); }
    }

    private static Element child(Element parent, String name) {
        for (Element element : children(parent)) if (element.getTagName().equals(name)) return element;
        return null;
    }
    private static Element first(Element parent, String name) {
        if (parent.getTagName().equals(name)) return parent;
        for (Element element : children(parent)) {
            Element match = first(element, name);
            if (match != null) return match;
        }
        return null;
    }
    private static List<Element> children(Element parent) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) if (nodes.item(i) instanceof Element element) result.add(element);
        return result;
    }
    private static String text(Element parent, String name, String fallback) {
        Element value = child(parent, name);
        return value == null ? fallback : value.getTextContent().trim();
    }
    private static String expand(String value, java.util.Map<String, String> properties) throws IOException {
        String expanded = value;
        for (int i = 0; i < 20 && expanded.contains("${"); i++) {
            int start = expanded.indexOf("${");
            int end = expanded.indexOf('}', start);
            if (end < 0) break;
            String key = expanded.substring(start + 2, end);
            String replacement = properties.get(key);
            if (replacement == null) throw new IOException("Unresolved Maven property ${" + key + "}");
            expanded = expanded.substring(0, start) + replacement + expanded.substring(end + 1);
        }
        return expanded;
    }
    private static void rejectDynamic(String version, String coordinate) throws IOException {
        if (version.equals("+") || version.endsWith(".+") || version.startsWith("[") || version.startsWith("(")
                || version.endsWith("]") || version.endsWith(")") || version.equalsIgnoreCase("latest")
                || version.equalsIgnoreCase("release")) {
            throw new IOException("Dynamic or ranged Maven version is forbidden: " + coordinate);
        }
    }

    private record Coordinate(String group, String artifact, String version, String classifier) {
        static Coordinate parse(String value) throws IOException {
            String[] parts = value.split(":", -1);
            if (parts.length < 3 || parts.length > 4 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
                throw new IOException("Expected Maven coordinate group:artifact:version[:classifier], got " + value);
            }
            String classifier = parts.length == 4 ? parts[3] : "";
            validateComponent("groupId", parts[0], GROUP_ID, false, value);
            validateComponent("artifactId", parts[1], ARTIFACT_ID, false, value);
            validateComponent("version", parts[2], VERSION, false, value);
            if (!classifier.isEmpty()) validateComponent("classifier", classifier, CLASSIFIER, false, value);
            return new Coordinate(parts[0], parts[1], parts[2], classifier);
        }

        private static void validateComponent(String name, String component, Pattern pattern, boolean emptyAllowed,
                String coordinate) throws IOException {
            if (component.isEmpty() && emptyAllowed) return;
            if (component.isBlank() || component.equals(".") || component.equals("..") || !pattern.matcher(component).matches()) {
                throw new IOException("Invalid Maven coordinate component " + name + " in '" + coordinate + "'");
            }
        }
    }
}
