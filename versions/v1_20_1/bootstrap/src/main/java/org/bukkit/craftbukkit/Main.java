package org.bukkit.craftbukkit;

/** CraftBukkit launcher facade for environments that invoke the historical main class. */
public final class Main {
    public static boolean useJline = true;
    public static boolean useConsole = true;
    private Main() { }
    public static void main(String[] args) {
        try {
            Class<?> launcher = Class.forName("net.fabricmc.loader.impl.launch.server.FabricServerLauncher");
            launcher.getMethod("main", String[].class).invoke(null, (Object) args);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Fabric server launcher is unavailable", failure);
        }
    }
    public static void printlnStartupInfoToSystemOut(String line) { org.apache.logging.log4j.LogManager.getLogger("Meldrite").info(line); }
    public static void printlnStartupWarningToSystemOut(String line) { org.apache.logging.log4j.LogManager.getLogger("Meldrite").warn(line); }
    public static void printlnStartupErrorToSystemOut(String line) { org.apache.logging.log4j.LogManager.getLogger("Meldrite").error(line); }
}
