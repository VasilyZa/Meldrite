package dev.meldrite.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;

/** Initial build boundary; F2 adds the neutral module dependency/reference policy. */
public final class FoundationArchitectureCheck {
    private FoundationArchitectureCheck() {}

    public static int verifyClasses(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) throw new IllegalStateException("MISSING_CLASSES: " + directory);
        List<Path> classes;
        try (var files = Files.walk(directory)) {
            classes = files.filter(p -> p.toString().endsWith(".class")).sorted().toList();
        }
        if (classes.isEmpty()) throw new IllegalStateException("MISSING_CLASSES: " + directory);
        for (Path file : classes) {
            byte[] bytes = Files.readAllBytes(file);
            if (bytes.length < 10 || bytes[0] != (byte)0xca || bytes[1] != (byte)0xfe
                    || bytes[2] != (byte)0xba || bytes[3] != (byte)0xbe) {
                throw new IllegalStateException("INVALID_CLASSFILE: " + file);
            }
            int major = ((bytes[6] & 255) << 8) | (bytes[7] & 255);
            if (major > 61) throw new IllegalStateException("JAVA_TARGET: " + file + " major=" + major + " exceeds Java 17");
            try {
                new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {}, 0);
            } catch (IllegalArgumentException | IndexOutOfBoundsException malformed) {
                throw new IllegalStateException("INVALID_CLASSFILE: " + file, malformed);
            }
        }
        return classes.size();
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: FoundationArchitectureCheck <project-root>");
        Path root = Path.of(args[0]);
        Map<String, Integer> checked = new TreeMap<>();
        for (String module : List.of("testkit/runtime", "testkit/contract")) {
            checked.put(module, verifyClasses(root.resolve(module).resolve("build/classes/java/main")));
        }
        Path report = root.resolve("build/reports/meldrite/verifyArchitecture/report.json");
        Files.createDirectories(report.getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(report.toFile(), Map.of(
            "scope", "F1 tooling bytecode; neutral dependency policy is added by F2",
            "javaRelease", 17, "classes", checked));
        System.out.println("Architecture foundation verified: " + checked);
    }
}
