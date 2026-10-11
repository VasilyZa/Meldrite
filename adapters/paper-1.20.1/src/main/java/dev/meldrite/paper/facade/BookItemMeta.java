package dev.meldrite.paper.facade;

import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.inventory.meta.BookMeta;

public class BookItemMeta extends org.bukkit.craftbukkit.inventory.CraftMetaItem implements BookMeta {
    private static final GsonComponentSerializer JSON = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private final boolean written;
    protected BookItemMeta(boolean written) { this.written = written; }
    protected BookItemMeta(BookItemMeta source) { super(source); written = source.written; }
    BookItemMeta forWritten(boolean value) {
        if (written == value) return this;
        BookItemMeta result = value ? new org.bukkit.craftbukkit.inventory.CraftMetaBookSigned() : new org.bukkit.craftbukkit.inventory.CraftMetaBook(); result.nativeMetadata(nativeMetadata());
        result.setPages(getPages()); return result;
    }
    @Override public boolean hasTitle() { return mutableTag().get("title") instanceof String; }
    @Override public String getTitle() { return hasTitle() ? (String) mutableTag().get("title") : null; }
    @Override public boolean setTitle(String title) { if (title != null && title.length() > 32) return false; value("title", title); return true; }
    @Override public boolean hasAuthor() { return mutableTag().get("author") instanceof String; }
    @Override public String getAuthor() { return hasAuthor() ? (String) mutableTag().get("author") : null; }
    @Override public void setAuthor(String author) { value("author", author); }
    @Override public boolean hasGeneration() { return mutableTag().get("generation") instanceof Number; }
    @Override public Generation getGeneration() { if (!hasGeneration()) return null; int value = ((Number) mutableTag().get("generation")).intValue(); return value >= 0 && value < Generation.values().length ? Generation.values()[value] : null; }
    @Override public void setGeneration(Generation generation) { value("generation", generation == null ? null : generation.ordinal()); }
    private List<String> rawPages() { if (!(mutableTag().get("pages") instanceof List<?> pages)) return List.of(); return pages.stream().map(String.class::cast).toList(); }
    private void rawPages(List<String> pages) { value("pages", new ArrayList<>(pages)); }
    private String encode(Component value) { return written ? JSON.serialize(value) : LEGACY.serialize(value); }
    private Component decode(String value) { return written ? JSON.deserialize(value) : LEGACY.deserialize(value); }
    private static String truncate(String value) { return value.length() <= 320 ? value : value.substring(0, 320); }
    @Override public boolean hasPages() { return !rawPages().isEmpty(); }
    @Override public int getPageCount() { return rawPages().size(); }
    private void checkPage(int page) { if (page < 1 || page > getPageCount()) throw new IllegalArgumentException("Invalid page number " + page + "/" + getPageCount()); }
    @Override public String getPage(int page) { return LEGACY.serialize(page(page)); }
    @Override public void setPage(int page, String data) { page(page, LEGACY.deserialize(truncate(data == null ? "" : data))); }
    @Override public List<String> getPages() { return new ArrayList<>(rawPages().stream().map(value -> LEGACY.serialize(decode(value))).toList()); }
    @Override public void setPages(List<String> pages) { rawPages(List.of()); addPage(Objects.requireNonNull(pages).toArray(String[]::new)); }
    @Override public void setPages(String... pages) { setPages(Arrays.asList(Objects.requireNonNull(pages))); }
    @Override public void addPage(String... pages) { for (String page : Objects.requireNonNull(pages)) addPages(LEGACY.deserialize(truncate(page == null ? "" : page))); }
    @Override public Component title() { return getTitle() == null ? null : LEGACY.deserialize(getTitle()); }
    @Override public BookItemMeta title(Component title) { setTitle(title == null ? null : LEGACY.serialize(title)); return this; }
    @Override public Component author() { return getAuthor() == null ? null : LEGACY.deserialize(getAuthor()); }
    @Override public BookItemMeta author(Component author) { setAuthor(author == null ? null : LEGACY.serialize(author)); return this; }
    @Override public Component page(int page) { checkPage(page); return decode(rawPages().get(page - 1)); }
    @Override public void page(int page, Component data) { checkPage(page); List<String> pages = new ArrayList<>(rawPages()); pages.set(page - 1, encode(data == null ? Component.empty() : data)); rawPages(pages); }
    @Override public void addPages(Component... additions) { List<String> pages = new ArrayList<>(rawPages()); for (Component page : Objects.requireNonNull(additions)) { if (pages.size() >= 100) break; pages.add(encode(page == null ? Component.empty() : page)); } rawPages(pages); }
    @Override public List<Component> pages() { return rawPages().stream().map(this::decode).toList(); }
    @Override public BookItemMeta pages(List<Component> pages) { rawPages(List.of()); addPages(Objects.requireNonNull(pages).toArray(Component[]::new)); return this; }
    @Override public BookItemMeta pages(Component... pages) { rawPages(List.of()); addPages(pages); return this; }
    @Override public BookMetaBuilder toBuilder() {
        return new BookMetaBuilder() {
            private final BookItemMeta value = BookItemMeta.this.clone();
            @Override public BookMetaBuilder title(Component title) { value.title(title); return this; }
            @Override public BookMetaBuilder author(Component author) { value.author(author); return this; }
            @Override public BookMetaBuilder addPage(Component page) { value.addPages(page); return this; }
            @Override public BookMetaBuilder pages(Component... pages) { value.rawPages(List.of()); value.addPages(pages); return this; }
            @Override public BookMetaBuilder pages(Collection<Component> pages) { return pages(pages.toArray(Component[]::new)); }
            @Override public BookMeta build() { return value.clone(); }
        };
    }
    @Override public Spigot spigot() {
        return new Spigot() {
            @Override public BaseComponent[] getPage(int page) { return ComponentSerializer.parse(JSON.serialize(BookItemMeta.this.page(page))); }
            @Override public void setPage(int page, BaseComponent... data) { BookItemMeta.this.page(page, JSON.deserialize(ComponentSerializer.toString(data))); }
            @Override public List<BaseComponent[]> getPages() { return BookItemMeta.this.pages().stream().map(value -> ComponentSerializer.parse(JSON.serialize(value))).toList(); }
            @Override public void setPages(List<BaseComponent[]> pages) { rawPages(List.of()); for (BaseComponent[] page : pages) addPage(page); }
            @Override public void setPages(BaseComponent[]... pages) { setPages(Arrays.asList(pages)); }
            @Override public void addPage(BaseComponent[]... pages) { for (BaseComponent[] page : pages) BookItemMeta.this.addPages(JSON.deserialize(ComponentSerializer.toString(page))); }
        };
    }
    @Override public BookItemMeta clone() { return new BookItemMeta(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = super.serialize(); result.put("meta-type", written ? "BOOK_SIGNED" : "BOOK"); if (hasTitle()) result.put("title", getTitle()); if (hasAuthor()) result.put("author", getAuthor()); if (hasGeneration() && getGeneration() != null) result.put("generation", getGeneration().name()); if (hasPages()) result.put("pages", new ArrayList<>(rawPages())); return result; }
}
