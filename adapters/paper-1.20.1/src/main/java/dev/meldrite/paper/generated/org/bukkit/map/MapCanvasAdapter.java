package dev.meldrite.paper.generated.org.bukkit.map;
public final class MapCanvasAdapter implements org.bukkit.map.MapCanvas {
private final dev.meldrite.paper.map.MapCanvasView view;public MapCanvasAdapter(dev.meldrite.paper.map.MapCanvasView view){this.view=java.util.Objects.requireNonNull(view);}
  @Override public void drawImage(int p0, int p1, java.awt.Image p2) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapCanvas#drawImage(IILjava/awt/Image;)V#METHOD"); }
  @Override public void drawText(int p0, int p1, org.bukkit.map.MapFont p2, java.lang.String p3) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapCanvas#drawText(IILorg/bukkit/map/MapFont;Ljava/lang/String;)V#METHOD"); }
  @Override public byte getBasePixel(int p0, int p1) { return view.state.basePixel(p0,p1); }
  @Override public java.awt.Color getBasePixelColor(int p0, int p1) { return view.baseColor(p0,p1); }
  @Override public org.bukkit.map.MapCursorCollection getCursors() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapCanvas#getCursors()Lorg/bukkit/map/MapCursorCollection;#METHOD"); }
  @Override public org.bukkit.map.MapView getMapView() { return view.view(); }
  @Override public byte getPixel(int p0, int p1) { return view.state.pixel(p0,p1); }
  @Override public java.awt.Color getPixelColor(int p0, int p1) { return view.pixelColor(p0,p1); }
  @Override public void setCursors(org.bukkit.map.MapCursorCollection p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapCanvas#setCursors(Lorg/bukkit/map/MapCursorCollection;)V#METHOD"); }
  @Override public void setPixel(int p0, int p1, byte p2) { view.state.pixel(p0,p1,p2); }
  @Override public void setPixelColor(int p0, int p1, java.awt.Color p2) { view.pixelColor(p0,p1,p2); }
}
