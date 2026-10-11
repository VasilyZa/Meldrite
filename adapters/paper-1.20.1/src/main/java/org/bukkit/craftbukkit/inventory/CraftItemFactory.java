package org.bukkit.craftbukkit.inventory;

/** Craft entry point uses the profile's single native metadata implementation. */
public final class CraftItemFactory extends dev.meldrite.paper.facade.ItemFactoryAdapter {
    private static final CraftItemFactory INSTANCE = new CraftItemFactory();
    private CraftItemFactory() { }
    public static CraftItemFactory instance() { return INSTANCE; }
}
