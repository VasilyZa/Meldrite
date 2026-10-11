package org.bukkit.craftbukkit.v1_20_R1.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;
import org.bukkit.util.CachedServerIcon;

public final class CraftIconCache implements CachedServerIcon {
    public final byte[] value;
    public CraftIconCache(byte[] value) { this.value = value == null ? null : value.clone(); }
    @Override public String getData() {
        return value == null ? null : "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(value);
    }
    public static CraftIconCache load(File file) throws java.io.IOException {
        if (file == null || !file.isFile()) throw new IllegalArgumentException("Icon must be a valid file: " + file);
        BufferedImage image = ImageIO.read(file);
        if (image == null) throw new IllegalArgumentException("Icon is not a readable image: " + file);
        return load(image);
    }
    public static CraftIconCache load(BufferedImage image) throws java.io.IOException {
        if (image == null || image.getWidth() != 64 || image.getHeight() != 64) {
            throw new IllegalArgumentException("Server icon must be a 64 by 64 pixel image");
        }
        var bytes = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "PNG", bytes)) throw new java.io.IOException("PNG image encoder unavailable");
        return new CraftIconCache(bytes.toByteArray());
    }
}
