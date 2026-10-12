package dev.meldrite.v1201.bootstrap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

/** Fabric entrypoint; it only routes lifecycle callbacks to the version composition root. */
public final class MeldriteInitializer implements ModInitializer {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Meldrite");
    private volatile VersionComposition composition;

    @Override
    public void onInitialize() {
        dev.meldrite.v1201.console.ConsoleLogging.install();
        dev.meldrite.v1201.compat.FabricPermissionBridge.install();
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) ->
                dispatcher.register(net.minecraft.server.command.CommandManager.literal("meldrite:workers")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            VersionComposition current = composition;
                            context.getSource().sendFeedback(() -> net.minecraft.text.Text.literal(current == null
                                    ? "Meldrite workers are not initialized" : current.workerStatus()), false);
                            return current == null ? 0 : 1;
                        })));
        ServerLifecycleEvents.SERVER_STARTING.register(this::starting);
        ServerLifecycleEvents.SERVER_STARTED.register(this::started);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::stopping);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resources, success) -> {
            VersionComposition current = composition;
            if (success && current != null) current.syncCommands();
        });
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            VersionComposition current = composition;
            if (current != null) current.tick();
        });
    }

    private void starting(MinecraftServer server) {
        try {
            composition = VersionComposition.start(server);
        } catch (Throwable failure) {
            String code = failure instanceof dev.meldrite.bootstrap.DistributionVerifier.Rejected rejected
                    ? rejected.code() : "BOOTSTRAP_FAILURE";
            VersionComposition.emit("status", code);
            VersionComposition.emit("error", failure.toString());
            LOGGER.error("Meldrite bootstrap failed", failure);
            server.stop(false);
        }
    }

    private void started(MinecraftServer server) {
        VersionComposition current = composition;
        if (current == null) return;
        try {
            current.worldsReady();
        } catch (Throwable failure) {
            VersionComposition.emit("status", "STARTUP_FAILURE");
            VersionComposition.emit("error", failure.toString());
            LOGGER.error("Meldrite world startup failed", failure);
            server.stop(false);
        }
    }

    private void stopping(MinecraftServer server) {
        VersionComposition current = composition;
        if (current == null) return;
        try {
            current.close();
        } catch (Throwable failure) {
            VersionComposition.emit("error", failure.toString());
            LOGGER.error("Meldrite shutdown failed", failure);
        } finally {
            composition = null;
        }
    }
}
