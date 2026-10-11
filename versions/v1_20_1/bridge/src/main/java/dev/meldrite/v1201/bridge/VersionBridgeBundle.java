package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.BridgeBundle;
import dev.meldrite.foundation.CapabilitySet;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.Optional;

/** The V1 composition exposes only the ports with native evidence in this slice. */
public final class VersionBridgeBundle {
    private VersionBridgeBundle() {}

    public static Composition create(MinecraftServer server) {
        var execution = new ExecutionBridge(server);
        var lifecycle = new LifecycleBridge(server);
        var worlds = new NativeWorldPort(server);
        var entities = new NativeEntityPort(server, worlds);
        var gateway = new HookGateway(server);
        var registry = new RegistryBridge(Map.of());
        var itemCodec = new ItemNbtCodec();
        var dataFix = new NativeDataFixBridge();
        var audience = new AudienceNetworkBridge(message -> {
            throw new UnsupportedOperationException("UNSUPPORTED_NATIVE_AUDIENCE_TRANSPORT");
        });
        var capabilities = new CapabilitySet(disableLifecycle()
                ? java.util.Set.of("execution.primary", "events.block-break", "hook.block-break.pre-commit", "registry.snapshot", "item.nbt-opaque", "datafix.identity")
                : java.util.Set.of("execution.primary", "server.lifecycle.start-stop", "events.block-break", "hook.block-break.pre-commit", "registry.snapshot", "item.nbt-opaque", "datafix.identity"));
        var bundle = new BridgeBundle(capabilities, Optional.of(execution), Optional.of(registry), Optional.of(gateway),
                Optional.of(itemCodec), Optional.of(audience), Optional.of(worlds), Optional.of(entities), Optional.empty(),
                Optional.of(dataFix), disableLifecycle() ? Optional.empty() : Optional.of(lifecycle), Optional.empty());
        return new Composition(bundle, execution, lifecycle, gateway, worlds, entities, registry, itemCodec, dataFix, audience);
    }

    private static boolean disableLifecycle() {
        String property = System.getProperty("MELDRITE_DISABLE_LIFECYCLE");
        if (property != null) return Boolean.parseBoolean(property);
        return Boolean.parseBoolean(System.getenv().getOrDefault("MELDRITE_DISABLE_LIFECYCLE", "false"));
    }

    public record Composition(BridgeBundle bundle, ExecutionBridge execution, LifecycleBridge lifecycle,
                              HookGateway gateway, NativeWorldPort worlds, NativeEntityPort entities,
                              RegistryBridge registry, ItemNbtCodec itemCodec, NativeDataFixBridge dataFix,
                              AudienceNetworkBridge audience)
            implements AutoCloseable {
        @Override
        public void close() {
            gateway.close();
            lifecycle.close();
        }
    }
}
