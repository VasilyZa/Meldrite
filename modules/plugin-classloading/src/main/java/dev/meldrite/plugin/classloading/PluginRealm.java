package dev.meldrite.plugin.classloading;
import dev.meldrite.plugin.spi.*;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.*;
public class PluginRealm extends OwnedRealm {
    static { registerAsParallelCapable(); }
    final PluginLoaderEngine engine;
    final LibraryRealm libraries;
    final VisibilityPolicy policy;
    protected PluginRealm(PluginLoaderEngine engine,Path plugin,LibraryRealm libraries,ApiAuthority authority,TransformContext context,ClassTransform transform,VisibilityPolicy policy)throws IOException {super(List.of(plugin),authority,context,transform);this.engine=engine;this.libraries=libraries;this.policy=Objects.requireNonNull(policy);}
    public String pluginId(){return context.pluginId();}
    @Override protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
        // A few legacy Paper libraries pass JVM internal names to Class.forName.
        // Normalize them before the normal visibility and native routing checks.
        String requested = name.indexOf('/') >= 0 ? name.replace('/', '.') : name;
        synchronized(getClassLoadingLock(requested)){Class<?> loaded=findLoadedClass(requested);if(loaded==null){if(closed)throw new ClassNotFoundException("REALM_CLOSED: "+pluginId());loaded=engine.load(this,requested,true,true);}if(resolve && loaded.getClassLoader()==this)resolveClass(loaded);return loaded;}
    }
    protected final Class<?> loadRoutedClass(String name,boolean resolve,boolean checkGroup,boolean checkLibraries)throws ClassNotFoundException {
        String requested = name.indexOf('/') >= 0 ? name.replace('/', '.') : name;
        synchronized(getClassLoadingLock(requested)){if(closed)throw new ClassNotFoundException("REALM_CLOSED: "+pluginId());Class<?> loaded=engine.load(this,requested,checkGroup,checkLibraries);if(resolve && loaded.getClassLoader()==this)resolveClass(loaded);return loaded;}
    }
    Class<?> exported(String name,boolean checkLibraries)throws ClassNotFoundException {
        if(closed)throw new ClassNotFoundException("REALM_CLOSED: "+pluginId());
        if(findResource(name.replace('.','/')+".class")!=null)return own(name);
        if(checkLibraries)return libraries.own(name);throw new ClassNotFoundException(name);
    }
    List<URL> exportedResources(String name,boolean checkLibraries)throws IOException {List<URL> urls=new ArrayList<>(ownResources(name));if(checkLibraries)urls.addAll(libraries.ownResources(name));return urls;}
    protected final List<URL> localResources(String name)throws IOException {
        if(closed)return List.of();
        if(name.equals("META-INF/services/org.slf4j.spi.SLF4JServiceProvider"))return authority.resources(name);
        return exportedResources(name,true);
    }
    @Override public URL getResource(String name){try{var resources=getResources(name);return resources.hasMoreElements()?resources.nextElement():null;}catch(IOException failure){throw new UncheckedIOException(failure);}}
    @Override public Enumeration<URL> getResources(String name)throws IOException {return Collections.enumeration(closed?List.of():engine.resources(this,name));}
    @Override public void close()throws IOException {if(closed)return;IOException error=null;try{super.close();}catch(IOException failure){error=failure;}try{libraries.close();}catch(IOException failure){if(error==null)error=failure;else error.addSuppressed(failure);}engine.removed(this);if(error!=null)throw error;}
}
