package dev.meldrite.v1201.bridge;

import dev.meldrite.bridge.ItemCodecPort;
import dev.meldrite.foundation.ItemSnapshot;
import dev.meldrite.foundation.ResourceKey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Lossless item payload codec used at the neutral/native boundary.
 * The opaque bytes are retained verbatim, so editing a known count cannot
 * discard fields introduced by a newer native item implementation.
 */
public final class ItemNbtCodec implements ItemCodecPort {
    public static final String CODEC_ID = "minecraft-nbt-opaque-v1";

    @Override public ItemSnapshot decode(byte[] encoded) throws IOException {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encoded))) {
            if (input.readInt() != 0x4D4E4254) throw new IOException("UNSUPPORTED_ITEM_PAYLOAD");
            ResourceKey type = key(input.readUTF());
            int count = input.readInt();
            String codec = input.readUTF();
            int length = input.readInt();
            if (length < 0 || length > input.available()) throw new EOFException("Invalid item payload length");
            byte[] payload = input.readNBytes(length);
            return new ItemSnapshot(type, count, codec, payload);
        }
    }

    @Override public byte[] encode(ItemSnapshot item) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(0x4D4E4254);
            output.writeUTF(item.type().entryNamespace() + ":" + item.type().entryPath());
            output.writeInt(item.count());
            output.writeUTF(item.codecId());
            output.writeInt(item.opaquePayload().length);
            output.write(item.opaquePayload());
            return bytes.toByteArray();
        }
    }

    @Override public ItemSnapshot mergeCountLosslessly(ItemSnapshot original, int count) {
        return new ItemSnapshot(original.type(), count, original.codecId(), original.opaquePayload());
    }

    private static ResourceKey key(String id) {
        int colon = id.indexOf(':');
        if (colon <= 0 || colon == id.length() - 1) throw new IllegalArgumentException("Invalid native item id: " + id);
        return new ResourceKey("minecraft", "item", id.substring(0, colon), id.substring(colon + 1));
    }
}
