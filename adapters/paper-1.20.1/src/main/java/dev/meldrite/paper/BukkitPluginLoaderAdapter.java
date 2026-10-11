package dev.meldrite.paper;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
/** The exact JavaPlugin init SPI; behavior delegates to bound services or fails explicitly. */
public final class BukkitPluginLoaderAdapter implements PluginLoader {
    public Plugin loadPlugin(File file)throws InvalidPluginException,UnknownDependencyException {try{return Bukkit.getPluginManager().loadPlugin(file);}catch(InvalidDescriptionException invalid){throw new InvalidPluginException(invalid);}}
    public PluginDescriptionFile getPluginDescription(File file)throws InvalidDescriptionException {
        try(JarFile jar=new JarFile(file)){
            if(jar.getJarEntry("paper-plugin.yml")!=null)throw new InvalidDescriptionException("Paper provider does not expose a legacy file description");
            var entry=jar.getJarEntry("plugin.yml");if(entry==null)throw new FileNotFoundException(file+"!plugin.yml");
            try(var input=jar.getInputStream(entry)){return new PluginDescriptionFile(input);}
        }catch(IOException failure){throw new InvalidDescriptionException(failure);}
    }
    public Pattern[] getPluginFileFilters(){return new Pattern[0];}
    public Map<Class<? extends Event>,Set<RegisteredListener>> createRegisteredListeners(Listener listener,Plugin plugin){return dev.meldrite.paper.event.EventDispatcherAdapter.createRegisteredListeners(listener,plugin);}
    public void enablePlugin(Plugin plugin){Bukkit.getPluginManager().enablePlugin(plugin);}
    public void disablePlugin(Plugin plugin){Bukkit.getPluginManager().disablePlugin(plugin);}
}
