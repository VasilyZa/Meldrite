package dev.meldrite.plugin.classloading;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import javax.management.*;

/** Local JDK management bridge to the launcher agent; no launcher classes cross the boundary. */
public final class RuntimeInstrumentation {
    private RuntimeInstrumentation() {}
    public static Instrumentation require() throws IOException {
        try {
            Object value = ManagementFactory.getPlatformMBeanServer().getAttribute(
                    new ObjectName("dev.meldrite:type=RuntimeAgent"), "Instrumentation");
            if (value instanceof Instrumentation instrumentation) return instrumentation;
            throw new IOException("RUNTIME_INSTRUMENTATION_TYPE_MISMATCH");
        } catch (JMException | RuntimeException failure) {
            throw new IOException("RUNTIME_INSTRUMENTATION_UNAVAILABLE: use the executable server launcher", failure);
        }
    }
}
