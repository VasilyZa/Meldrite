package dev.meldrite.distribution.mc1201;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

/** Installs verified libraries into the ordinary system loader before Fabric starts. */
public final class RuntimeClasspath {
    private static Instrumentation instrumentation;
    // The system loader continues using these files after the launcher main returns.
    private static final List<JarFile> libraries = new ArrayList<>();
    private RuntimeClasspath() { }

    /** The executable JAR's Launcher-Agent-Class invokes this before application main. */
    public static synchronized void agentmain(String arguments, Instrumentation value) {
        instrumentation = value;
        try {
            var server = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            var name = new javax.management.ObjectName("dev.meldrite:type=RuntimeAgent");
            if (!server.isRegistered(name)) server.registerMBean(new javax.management.StandardMBean(
                    new AgentBridge(), AgentBridgeMBean.class), name);
        } catch (javax.management.JMException failure) {
            throw new IllegalStateException("Cannot publish the local runtime instrumentation bridge", failure);
        }
    }
    public interface AgentBridgeMBean { Instrumentation getInstrumentation(); }
    public static final class AgentBridge implements AgentBridgeMBean {
        @Override public Instrumentation getInstrumentation() { return instrumentation; }
    }

    static void install(List<Path> paths) throws IOException {
        if (instrumentation == null)
            throw new IllegalStateException("Start the Meldrite distribution with java -jar to initialize its runtime classpath");
        for (Path path : paths) {
            var jar = new JarFile(path.toFile());
            try {
                instrumentation.appendToSystemClassLoaderSearch(jar);
                libraries.add(jar);
            } catch (RuntimeException failure) {
                jar.close();
                throw failure;
            }
        }
    }
}
