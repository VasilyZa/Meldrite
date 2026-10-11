package dev.meldrite.service;
import dev.meldrite.bridge.ExecutionPort;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
/** Detached palette-index layers and map metadata; native map persistence/packet delivery remain ports. */
public final class MapService implements AutoCloseable {
    public record Snapshot(int id,int centerX,int centerZ,int scale,byte[] base,byte[] overlay){public Snapshot{base=base.clone();overlay=overlay.clone();}@Override public byte[] base(){return base.clone();}@Override public byte[] overlay(){return overlay.clone();}}
    private final ExecutionPort execution;private final WorldService worlds;private final Set<Canvas> active=Collections.newSetFromMap(new IdentityHashMap<>());private boolean closed;
    public MapService(ExecutionPort execution,WorldService worlds){this.execution=Objects.requireNonNull(execution);this.worlds=Objects.requireNonNull(worlds);}
    private void primary(){if(!execution.isPrimaryThread())throw new IllegalStateException("PRIMARY_THREAD_REQUIRED");}
    public synchronized Canvas create(PluginScope owner,WorldService.Reference world,int id,int width,int height,int paletteSize,byte[] base){primary();if(closed)throw new IllegalStateException("MAP_SERVICE_CLOSED");worlds.requireWritable(world);if(id<0||width<=0||height<=0||paletteSize<1||paletteSize>255||base.length!=Math.multiplyExact(width,height))throw new IllegalArgumentException("Invalid map shape/palette/id");for(byte color:base)if(Byte.toUnsignedInt(color)>=paletteSize)throw new IllegalArgumentException("Invalid base palette index");var canvas=new Canvas(Objects.requireNonNull(owner),world,id,width,height,paletteSize,base);owner.register(canvas);active.add(canvas);return canvas;}
    public synchronized int ownedCanvases(){return active.size();}
    public final class Canvas implements AutoCloseable {
        private final PluginScope owner;private final WorldService.Reference world;private final int id,width,height,paletteSize;private final byte[] base,overlay;private int centerX,centerZ,scale=2;private boolean removed;
        private Canvas(PluginScope owner,WorldService.Reference world,int id,int width,int height,int paletteSize,byte[] base){this.owner=owner;this.world=world;this.id=id;this.width=width;this.height=height;this.paletteSize=paletteSize;this.base=base.clone();overlay=new byte[base.length];Arrays.fill(overlay,(byte)-1);}
        private void alive(){primary();if(closed||removed||owner.isClosed())throw new IllegalStateException("MAP_CANVAS_CLOSED");if(!worlds.valid(world))throw new IllegalStateException("STALE_HANDLE");}
        private int index(int x,int y){if(x<0||y<0||x>=width||y>=height)throw new IndexOutOfBoundsException("Map pixel: "+x+","+y);return y*width+x;}
        public void requireLive(){synchronized(MapService.this){alive();}}
        public int id(){synchronized(MapService.this){alive();return id;}}
        public WorldService.Reference world(){synchronized(MapService.this){alive();return world;}}
        public int centerX(){synchronized(MapService.this){alive();return centerX;}}
        public int centerZ(){synchronized(MapService.this){alive();return centerZ;}}
        public void centerX(int value){synchronized(MapService.this){alive();centerX=value;}}
        public void centerZ(int value){synchronized(MapService.this){alive();centerZ=value;}}
        public int scale(){synchronized(MapService.this){alive();return scale;}}
        public void scale(int value){synchronized(MapService.this){alive();if(value<0||value>4)throw new IllegalArgumentException("Invalid map scale");scale=value;}}
        public byte pixel(int x,int y){synchronized(MapService.this){alive();return overlay[index(x,y)];}}
        public byte basePixel(int x,int y){synchronized(MapService.this){alive();return base[index(x,y)];}}
        public void pixel(int x,int y,byte color){synchronized(MapService.this){alive();int index=index(x,y);if(color!=-1&&Byte.toUnsignedInt(color)>=paletteSize)throw new IllegalArgumentException("Invalid map palette index");overlay[index]=color;}}
        public Snapshot snapshot(){synchronized(MapService.this){alive();return new Snapshot(id,centerX,centerZ,scale,base,overlay);}}
        public void close(){synchronized(MapService.this){if(removed)return;removed=true;Arrays.fill(base,(byte)0);Arrays.fill(overlay,(byte)-1);active.remove(this);}}
    }
    public void close(){synchronized(this){if(closed)return;closed=true;List.copyOf(active).forEach(Canvas::close);}}
}
