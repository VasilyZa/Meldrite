package dev.meldrite.bridge;
import dev.meldrite.foundation.EntityHandle;
import dev.meldrite.foundation.ItemSnapshot;
import java.util.List;
public interface InventoryPort {
    List<ItemSnapshot> snapshot(EntityHandle entity);
    void replace(EntityHandle entity, int slot, ItemSnapshot item);
    /** All-or-nothing compare-and-replace. Failure must leave the slot unchanged. */
    default boolean compareAndReplace(EntityHandle entity, int slot, ItemSnapshot expected, ItemSnapshot replacement) {
        throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: inventory.atomic-replace");
    }
    /** Whole-container transaction, including the result slot; false must change no slot. */
    default boolean compareAndReplaceAll(EntityHandle entity,List<ItemSnapshot> expected,List<ItemSnapshot> replacement) {
        throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: inventory.atomic-container-replace");
    }
}
