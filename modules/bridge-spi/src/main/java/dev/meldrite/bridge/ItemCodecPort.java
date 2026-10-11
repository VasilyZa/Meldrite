package dev.meldrite.bridge;
import dev.meldrite.foundation.ItemSnapshot;
public interface ItemCodecPort {
    ItemSnapshot decode(byte[] encoded) throws Exception;
    byte[] encode(ItemSnapshot item) throws Exception;
    /** Version codec must merge the count while preserving every unknown field, or reject. */
    default ItemSnapshot mergeCountLosslessly(ItemSnapshot original, int count) throws Exception {
        throw new UnsupportedOperationException("UNSUPPORTED_CAPABILITY: item.lossless-count-merge");
    }
}
