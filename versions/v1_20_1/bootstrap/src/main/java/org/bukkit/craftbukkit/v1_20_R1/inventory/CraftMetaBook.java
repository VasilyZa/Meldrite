package org.bukkit.craftbukkit.v1_20_R1.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.inventory.meta.BookMeta;

/** Versioned writable-book metadata with the Paper builder and Spigot ABI. */
public class CraftMetaBook extends org.bukkit.craftbukkit.inventory.CraftMetaBook {
    public CraftMetaBook() { super(); }
    protected CraftMetaBook(org.bukkit.craftbukkit.inventory.CraftMetaBook source) { super(source); }
    @Override public CraftMetaBook clone() { return new CraftMetaBook(this); }
    @Override public BookMeta.BookMetaBuilder toBuilder() { return new CraftMetaBookBuilder(); }
    public static class CraftMetaBookBuilder implements BookMeta.BookMetaBuilder {
        protected Component title;
        protected Component author;
        protected final List<Component> pages = new ArrayList<>();
        @Override public CraftMetaBookBuilder title(Component value) { title = value; return this; }
        @Override public CraftMetaBookBuilder author(Component value) { author = value; return this; }
        @Override public CraftMetaBookBuilder addPage(Component value) { pages.add(value); return this; }
        @Override public CraftMetaBookBuilder pages(Component... values) { pages.clear(); java.util.Collections.addAll(pages, values); return this; }
        @Override public CraftMetaBookBuilder pages(Collection<Component> values) { pages.clear(); pages.addAll(values); return this; }
        @Override public BookMeta build() { CraftMetaBook value = new CraftMetaBook(); value.title(title); value.author(author); value.pages(pages); return value; }
    }
    public final class SpigotMeta extends BookMeta.Spigot {
        @Override public BaseComponent[] getPage(int page) { return ComponentSerializer.parse(CraftMetaBook.this.getPage(page)); }
        @Override public void setPage(int page, BaseComponent... value) { CraftMetaBook.this.setPage(page, ComponentSerializer.toString(value)); }
        @Override public List<BaseComponent[]> getPages() { return CraftMetaBook.this.getPages().stream().map(value -> ComponentSerializer.parse(value)).toList(); }
        @Override public void setPages(List<BaseComponent[]> values) { CraftMetaBook.this.setPages(values.stream().map(ComponentSerializer::toString).toList()); }
        @Override public void setPages(BaseComponent[]... values) { setPages(List.of(values)); }
        @Override public void addPage(BaseComponent[]... values) { for (BaseComponent[] value : values) CraftMetaBook.this.addPage(ComponentSerializer.toString(value)); }
    }
    @Override public BookMeta.Spigot spigot() { return new SpigotMeta(); }
}
