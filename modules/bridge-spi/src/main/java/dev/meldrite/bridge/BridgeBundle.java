package dev.meldrite.bridge;

import dev.meldrite.foundation.CapabilitySet;
import dev.meldrite.foundation.Diagnostic;
import java.util.Objects;
import java.util.Optional;

/** Immutable, typed inventory of bridge capabilities installed by one version composition root. */
public record BridgeBundle(CapabilitySet capabilities,
        Optional<ExecutionPort> execution, Optional<RegistryPort> registry,
        Optional<EventIngressPort> eventIngress, Optional<ItemCodecPort> itemCodec,
        Optional<NetworkAudiencePort> networkAudience, Optional<WorldPort> world,
        Optional<EntityPort> entity, Optional<CommandTreePort> commandTree,
        Optional<DataFixPort> dataFix, Optional<ServerLifecyclePort> serverLifecycle,
        Optional<InventoryPort> inventory) {
    public BridgeBundle {
        Objects.requireNonNull(capabilities, "capabilities"); Objects.requireNonNull(execution, "execution");
        Objects.requireNonNull(registry, "registry"); Objects.requireNonNull(eventIngress, "eventIngress");
        Objects.requireNonNull(itemCodec, "itemCodec"); Objects.requireNonNull(networkAudience, "networkAudience");
        Objects.requireNonNull(world, "world"); Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(commandTree, "commandTree"); Objects.requireNonNull(dataFix, "dataFix");
        Objects.requireNonNull(serverLifecycle, "serverLifecycle"); Objects.requireNonNull(inventory, "inventory");
    }

    public Optional<Diagnostic> missingCapability(String name) {
        Objects.requireNonNull(name, "name");
        return capabilities.supports(name) ? Optional.empty() : Optional.of(new Diagnostic(
                "UNSUPPORTED_CAPABILITY", Optional.empty(), "bridge-bind", "Bridge does not support capability: " + name, null));
    }
}
