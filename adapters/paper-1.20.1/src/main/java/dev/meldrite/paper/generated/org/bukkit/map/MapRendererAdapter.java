package dev.meldrite.paper.generated.org.bukkit.map;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.plugin.spi.PluginScope;
import java.util.*;
import org.bukkit.map.*;
import org.bukkit.entity.Player;
/** Actual plugin callback execution only; automatic native renderer registration remains pending. */
public final class MapRendererAdapter extends MapRenderer {
    private final PluginLoaderEngine engine;private final PluginScope owner;private final MapRenderer delegate;private final PluginRealm realm;
    public MapRendererAdapter(PluginLoaderEngine engine,PluginScope owner,MapRenderer delegate){super(Objects.requireNonNull(delegate).isContextual());this.engine=Objects.requireNonNull(engine);this.owner=Objects.requireNonNull(owner);this.delegate=delegate;if(!(delegate.getClass().getClassLoader() instanceof PluginRealm pluginRealm))throw new UnsupportedOperationException("UNOWNED_MAP_RENDERER");realm=pluginRealm;}
    @Override public void render(MapView view,MapCanvas canvas,Player player){if(owner.isClosed())throw new IllegalStateException("MAP_RENDERER_OWNER_CLOSED");Objects.requireNonNull(view);Objects.requireNonNull(canvas);Objects.requireNonNull(player);try{engine.withContext(realm,()->{delegate.render(view,canvas,player);return true;});}catch(RuntimeException|Error failure){throw failure;}catch(Exception failure){throw new IllegalStateException("MAP_RENDERER_FAILED",failure);}}
}
