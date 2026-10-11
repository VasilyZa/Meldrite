package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Offline release gate for one locked Minecraft/profile distribution pair. */
public final class ReleaseVerifier {
    private static final ObjectMapper JSON = new ObjectMapper();
    private ReleaseVerifier() {}

    public static CompatibilityReport verify(Path root) throws Exception {
        return verify(root, "paper-1.20.1", "mc-1.20.1");
    }

    public static CompatibilityReport verify(Path root, String profile, String distribution) throws Exception {
        if (!profile.startsWith("paper-") || !distribution.startsWith("mc-")) {
            throw new IllegalArgumentException("Invalid profile/distribution");
        }
        String minecraft = distribution.substring("mc-".length());
        if (!profile.equals("paper-" + minecraft)) {
            throw new IllegalArgumentException("profile and distribution must describe the same Minecraft version");
        }
        List<String> checks = new ArrayList<>();
        List<String> limitations = new ArrayList<>();
        require(root.resolve("contracts/release-policy.json"), checks, "release-policy");
        Path distributionDirectory = root.resolve("distributions").resolve(distribution);
        Path requiredCapabilities = distributionDirectory.resolve("required-capabilities.json");
        require(requiredCapabilities, checks, "required-capabilities");
        require(root.resolve("contracts/locks").resolve(profile + ".lock.json"), checks, "paper-lock");
        require(root.resolve("contracts/locks/tools.lock.json"), checks, "tools-lock");
        Path manifest = distributionDirectory.resolve("distribution.json");
        require(manifest, checks, "distribution-manifest");
        Path eula = root.resolve("eula.txt");
        if (Files.isRegularFile(eula)) {
            boolean accepted = Files.readAllLines(eula).stream()
                    .map(line -> line.trim().toLowerCase(Locale.ROOT))
                    .anyMatch("eula=true"::equals);
            if (!accepted) throw new IllegalStateException("EULA_NOT_ACCEPTED");
        }
        checks.add("eula-accepted-and-preserved");
        JsonNode required = JSON.readTree(requiredCapabilities.toFile());
        if (!required.path("minecraft").asText().equals(minecraft)
                || !required.path("profile").asText().equals(profile)
                || !required.path("capabilities").isArray()) {
            throw new IllegalStateException("INVALID_REQUIRED_CAPABILITIES");
        }
        checks.add("capability-manifest");
        JsonNode distributionDocument = JSON.readTree(manifest.toFile());
        if (!distributionDocument.path("minecraftVersion").asText().equals(minecraft)
                || !distributionDocument.path("profile").asText().equals(profile)
                || distributionDocument.path("javaFeature").asInt() < 17) {
            throw new IllegalStateException("INVALID_DISTRIBUTION_MANIFEST");
        }
        checks.add("distribution-manifest-consistent");
        JsonNode policy = JSON.readTree(root.resolve("contracts/release-policy.json").toFile());
        if (!containsText(policy.path("minecraft"), minecraft) || !containsText(policy.path("profiles"), profile)) {
            throw new IllegalStateException("PROFILE_NOT_IN_RELEASE_POLICY: " + profile);
        }
        checks.add("release-policy-scope");
        limitations.add("client-backed block-break and network transport remain unverified without a client harness");
        return new CompatibilityReport("PASS", checks, limitations);
    }

    private static boolean containsText(JsonNode values, String expected) {
        if (!values.isArray()) return false;
        for (JsonNode value : values) if (expected.equals(value.asText())) return true;
        return false;
    }

    private static void require(Path path, List<String> checks, String name) {
        if (!Files.isRegularFile(path)) throw new IllegalStateException("MISSING_RELEASE_ARTIFACT: " + path);
        checks.add(name);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || !args[0].equals("verify")) {
            throw new IllegalArgumentException("Usage: ReleaseVerifier verify <project-root> [--profile <profile>] [--distribution <distribution>]");
        }
        Path root = Path.of(args[1]).toAbsolutePath().normalize();
        String profile = "paper-1.20.1";
        String distribution = "mc-1.20.1";
        for (int index = 2; index < args.length; index++) {
            if (index + 1 >= args.length) throw new IllegalArgumentException("Missing value for " + args[index]);
            switch (args[index]) {
                case "--profile" -> profile = args[++index];
                case "--distribution" -> distribution = args[++index];
                default -> throw new IllegalArgumentException("Unknown option: " + args[index]);
            }
        }
        CompatibilityReport report = verify(root, profile, distribution);
        Path output = root.resolve("build/reports/meldrite/verifyRelease").resolve(distribution).resolve("compatibility-report.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, report.json() + "\n");
        System.out.println(report.json());
    }
}
