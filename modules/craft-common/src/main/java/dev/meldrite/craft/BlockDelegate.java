package dev.meldrite.craft;
import dev.meldrite.foundation.*;
import dev.meldrite.service.WorldService;
import java.util.Objects;
/** Coordinates are identity snapshots; live state reads always validate the world lease/thread. */
public final class BlockDelegate {
    private final WorldService worlds;private final WorldService.Reference reference;private final BlockPosition position;
    public BlockDelegate(WorldService worlds,WorldService.Reference reference,BlockPosition position){this.worlds=Objects.requireNonNull(worlds);this.reference=Objects.requireNonNull(reference);this.position=Objects.requireNonNull(position);if(!reference.key().equals(position.world()))throw new IllegalArgumentException("Block/world identity mismatch");}
    public WorldService.Reference reference(){return reference;}public BlockPosition position(){return position;}
    public ResourceKey type(){return worlds.blockType(reference,position);}
}
