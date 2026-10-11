package dev.meldrite.paper.map;
import dev.meldrite.service.MapService;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.paper.generated.org.bukkit.map.*;
import java.util.*;
import java.awt.Color;
import org.bukkit.map.*;
/** Locked upstream palette conversion; common service owns all pixel/lifecycle rules. */
public final class MapCanvasView {
    public final MapService.Canvas state;private final MapView view;
    public MapCanvasView(MapService.Canvas state,FacadeContext context){this.state=Objects.requireNonNull(state);view=new MapViewAdapter(state,context);}
    public MapView view(){state.requireLive();return view;}
    public Color pixelColor(int x,int y){byte pixel=state.pixel(x,y);return pixel==-1?null:MapPalette.getColor(pixel);}
    public Color baseColor(int x,int y){return MapPalette.getColor(state.basePixel(x,y));}
    public void pixelColor(int x,int y,Color color){state.requireLive();state.pixel(x,y,color==null?(byte)-1:MapPalette.matchColor(color));}
}
