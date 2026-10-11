package dev.meldrite.v1211.bootstrap;

import dev.meldrite.v1211.bridge.VersionBridge;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/** Fabric entrypoint for the 1.21.1 composition root. */
public final class MeldriteInitializer implements ModInitializer {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Meldrite");
    private volatile boolean started;

    @Override
    public void onInitialize() {
        dev.meldrite.bootstrap.StartupBanner.print(net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer("meldrite").orElseThrow().getMetadata().getVersion().getFriendlyString(), LOGGER::info);
        ServerLifecycleEvents.SERVER_STARTED.register(this::started);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::stopping);
    }

    private void started(MinecraftServer server) {
        started = true;
        System.out.println("{\"meldrite\":\"runtime\",\"profile\":\"" + profile()
                + "\",\"minecraftVersion\":\"" + new VersionBridge().minecraftVersion()
                + "\",\"javaFeature\":" + Runtime.version().feature() + "}");
    }

    private void stopping(MinecraftServer server) {
        if (started) System.out.println("{\"meldrite\":\"runtime\",\"status\":\"STOPPED\"}");
        started = false;
    }

    public String profile() { return "paper-1.21.1"; }
    public String bridgeVersion() { return new VersionBridge().minecraftVersion(); }
}
