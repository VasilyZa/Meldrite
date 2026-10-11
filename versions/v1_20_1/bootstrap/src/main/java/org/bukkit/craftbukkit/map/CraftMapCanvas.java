package org.bukkit.craftbukkit.map;

import java.awt.Color;
import java.awt.Image;
import java.util.Arrays;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapPalette;

/** Mutable 128x128 canvas with base-pixel snapshots and cursor state. */
public class CraftMapCanvas implements MapCanvas {
    private final byte[] buffer = new byte[128 * 128];
    private byte[] base = new byte[128 * 128];
    private final CraftMapView view;
    private MapCursorCollection cursors = new MapCursorCollection();
    protected CraftMapCanvas(CraftMapView view) { this.view = view; Arrays.fill(buffer, MapPalette.TRANSPARENT); }
    @Override public CraftMapView getMapView() { return view; }
    @Override public MapCursorCollection getCursors() { return cursors; }
    @Override public void setCursors(MapCursorCollection cursors) { this.cursors = cursors == null ? new MapCursorCollection() : cursors; }
    @Override public void setPixelColor(int x, int y, Color color) { setPixel(x, y, color == null ? MapPalette.TRANSPARENT : MapPalette.matchColor(color)); }
    @Override public Color getPixelColor(int x, int y) { byte value = getPixel(x, y); return value == MapPalette.TRANSPARENT ? null : MapPalette.getColor(value); }
    @Override public Color getBasePixelColor(int x, int y) { return MapPalette.getColor(getBasePixel(x, y)); }
    @Override public void setPixel(int x, int y, byte color) { if (inside(x, y)) buffer[y * 128 + x] = color; }
    @Override public byte getPixel(int x, int y) { return inside(x, y) ? buffer[y * 128 + x] : MapPalette.TRANSPARENT; }
    @Override public byte getBasePixel(int x, int y) { return inside(x, y) ? base[y * 128 + x] : MapPalette.TRANSPARENT; }
    protected byte[] getBuffer() { return buffer; }
    protected void setBase(byte[] source) { base = source == null ? new byte[128 * 128] : source.clone(); }
    @Override public void drawImage(int x, int y, Image image) { byte[] pixels = MapPalette.imageToBytes(image); int width = image.getWidth(null); int height = image.getHeight(null); for (int row = 0; row < height; row++) for (int column = 0; column < width; column++) setPixel(x + column, y + row, pixels[row * width + column]); }
    @Override public void drawText(int x, int y, MapFont font, String text) { if (font == null || text == null || !font.isValid(text)) throw new IllegalArgumentException("Invalid map text"); int start = x; for (int index = 0; index < text.length(); index++) { char character = text.charAt(index); if (character == '\n') { x = start; y += font.getHeight() + 1; continue; } MapFont.CharacterSprite sprite = font.getChar(character); for (int row = 0; row < font.getHeight(); row++) for (int column = 0; column < sprite.getWidth(); column++) if (sprite.get(row, column)) setPixel(x + column, y + row, MapPalette.DARK_GRAY); x += sprite.getWidth() + 1; } }
    private static boolean inside(int x, int y) { return x >= 0 && x < 128 && y >= 0 && y < 128; }
}
