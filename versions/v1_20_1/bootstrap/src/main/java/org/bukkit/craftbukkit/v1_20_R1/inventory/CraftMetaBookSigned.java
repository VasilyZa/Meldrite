package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.BookMeta;

/** Versioned signed-book metadata ABI. */
public final class CraftMetaBookSigned extends CraftMetaBook {
    public CraftMetaBookSigned() { super(); }
    private CraftMetaBookSigned(CraftMetaBook source) { super(source); }
    @Override public CraftMetaBookSigned clone() { return new CraftMetaBookSigned(this); }
    @Override public BookMeta.BookMetaBuilder toBuilder() { return new CraftMetaBookSignedBuilder(); }
    public static final class CraftMetaBookSignedBuilder extends CraftMetaBookBuilder {
        @Override public BookMeta build() {
            CraftMetaBookSigned value = new CraftMetaBookSigned();
            value.title(title); value.author(author); value.pages(pages);
            return value;
        }
    }
}
