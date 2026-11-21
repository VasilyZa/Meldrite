package dev.meldrite.paper.compat;

import org.bukkit.Color;

/** Converts native color fields whose consumers use only the three RGB channels. */
public final class PackedRgb {
    private PackedRgb() { }

    public static int normalize(int packed) {
        return packed & 0x00FFFFFF;
    }

    public static Color toBukkit(int packed) {
        return Color.fromRGB(normalize(packed));
    }
}
