package dev.meldrite.paper.generated.org.bukkit.map;
public final class MapViewAdapter implements org.bukkit.map.MapView {
private final dev.meldrite.service.MapService.Canvas state;private final dev.meldrite.paper.facade.FacadeContext context;public MapViewAdapter(dev.meldrite.service.MapService.Canvas state,dev.meldrite.paper.facade.FacadeContext context){this.state=java.util.Objects.requireNonNull(state);this.context=java.util.Objects.requireNonNull(context);}
  @Override public void addRenderer(org.bukkit.map.MapRenderer p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#addRenderer(Lorg/bukkit/map/MapRenderer;)V#METHOD"); }
  @Override public int getCenterX() { return state.centerX(); }
  @Override public int getCenterZ() { return state.centerZ(); }
  @Override public int getId() { return state.id(); }
  @Override public java.util.List<org.bukkit.map.MapRenderer> getRenderers() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#getRenderers()Ljava/util/List;#METHOD"); }
  @Override public org.bukkit.map.MapView.Scale getScale() { return org.bukkit.map.MapView.Scale.valueOf((byte)state.scale()); }
  @Override public org.bukkit.World getWorld() { return context.world(state.world()); }
  @Override public boolean isLocked() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#isLocked()Z#METHOD"); }
  @Override public boolean isTrackingPosition() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#isTrackingPosition()Z#METHOD"); }
  @Override public boolean isUnlimitedTracking() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#isUnlimitedTracking()Z#METHOD"); }
  @Override public boolean isVirtual() { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#isVirtual()Z#METHOD"); }
  @Override public boolean removeRenderer(org.bukkit.map.MapRenderer p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#removeRenderer(Lorg/bukkit/map/MapRenderer;)Z#METHOD"); }
  @Override public void setCenterX(int p0) { state.centerX(p0); }
  @Override public void setCenterZ(int p0) { state.centerZ(p0); }
  @Override public void setLocked(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#setLocked(Z)V#METHOD"); }
  @Override public void setScale(org.bukkit.map.MapView.Scale p0) { state.scale(java.util.Objects.requireNonNull(p0).getValue()); }
  @Override public void setTrackingPosition(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#setTrackingPosition(Z)V#METHOD"); }
  @Override public void setUnlimitedTracking(boolean p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#setUnlimitedTracking(Z)V#METHOD"); }
  @Override public void setWorld(org.bukkit.World p0) { throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/map/MapView#setWorld(Lorg/bukkit/World;)V#METHOD"); }
}
