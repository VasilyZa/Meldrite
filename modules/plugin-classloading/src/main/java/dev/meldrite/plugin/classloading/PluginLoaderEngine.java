package dev.meldrite.plugin.classloading;
import dev.meldrite.plugin.spi.*;
import java.net.*;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
/** Shared routing algorithm; profile handshakes and native game access stay outside this module. */
public final class PluginLoaderEngine implements AutoCloseable {
    private final ApiAuthority authority;
    private final ClassTransform transform;
    private final List<NativeRoute> nativeRoutes;
    private final Map<String,PluginRealm> realms=new LinkedHashMap<>();
    private boolean closed;
    private GlobalLibraryRegistry globals;
    public PluginLoaderEngine(ApiAuthority authority,ClassTransform transform){this(authority,transform,List.of());}
    public PluginLoaderEngine(ApiAuthority authority,ClassTransform transform,List<NativeRoute> nativeRoutes){this.authority=Objects.requireNonNull(authority);this.transform=Objects.requireNonNull(transform);this.nativeRoutes=List.copyOf(nativeRoutes);}
    public PluginLoaderEngine(ApiAuthority authority,ClassTransform transform,List<NativeRoute> nativeRoutes,GlobalLibraryAccess globalAccess,Path cache)throws IOException {
        this(authority,transform,nativeRoutes);
        this.globals=new GlobalLibraryRegistry(authority,transform,globalAccess,cache);
    }
    public PluginRealm open(String id,Path plugin,List<Path> libraries,TransformContext.Access access,VisibilityPolicy policy)throws IOException {return open(id,plugin,libraries,access,policy,PluginRealm::new);}
    @FunctionalInterface public interface RealmFactory {
        PluginRealm create(PluginLoaderEngine engine,Path plugin,LibraryRealm libraries,ApiAuthority authority,TransformContext context,ClassTransform transform,VisibilityPolicy policy)throws IOException;
    }
    public synchronized PluginRealm open(String id,Path plugin,List<Path> libraries,TransformContext.Access access,VisibilityPolicy policy,RealmFactory factory)throws IOException {
        return open(id,plugin,libraries,access,policy,(bytes,context)->bytes,factory);
    }
    public synchronized PluginRealm open(String id,Path plugin,List<Path> libraries,TransformContext.Access access,VisibilityPolicy policy,ClassTransform preparation,RealmFactory factory)throws IOException {
        if(closed)throw new IllegalStateException("ENGINE_CLOSED");if(realms.containsKey(id))throw new IllegalStateException("DUPLICATE_REALM: "+id);
        ClassTransform definitions=new ClassTransform(){
            @Override public void prepareLibrary(Path source,TransformContext context)throws Exception{transform.prepareLibrary(source,context);}
            @Override public byte[] prepare(byte[] bytes,TransformContext context)throws Exception{return transform.prepare(preparation.apply(bytes,context),context);}
            @Override public byte[] apply(byte[] bytes,TransformContext context)throws Exception{return transform.apply(bytes,context);}
        };
        TransformContext context=new TransformContext(id,access);LibraryRealm library=new LibraryRealm(libraries,authority,context,definitions,nativeRoutes,globals);
        try{PluginRealm realm=factory.create(this,plugin,library,authority,context,definitions,policy);realm.attachGlobals(globals);realms.put(id,realm);return realm;}
        catch(IOException|RuntimeException|Error failure){try{library.close();}catch(IOException cleanup){failure.addSuppressed(cleanup);}throw failure;}
    }
    synchronized Map<String,PluginRealm> snapshot(){return new LinkedHashMap<>(realms);}
    /** Freeze the current dependency routes before retiring a complete plugin generation. */
    public void prepareReload() throws IOException {
        ReloadRealmGroup group;
        synchronized (this) {
            if (closed) throw new IllegalStateException("ENGINE_CLOSED");
            group = new ReloadRealmGroup(realms);
            for (var realm : realms.values()) realm.prepareReload(group);
        }
        group.prepared();
    }
    Class<?> load(PluginRealm realm,String name,boolean checkGroup,boolean checkLibraries)throws ClassNotFoundException {
        if (name.equals("dev.meldrite.plugin.classloading.LegacyClassLookup")) {
            return LegacyClassLookup.class;
        }
        Optional<Class<?>> shared=realm.sharedIfPresent(name);if(shared.isPresent())return shared.get();
        // Native compatibility classes are part of the host boundary. Resolve
        // them before plugin/library visibility checks so legacy reflection sees
        // the same classes as code running in the server realm.
        // Native compatibility classes are host-owned and are required by
        // ordinary Paper plugins as well as internal bootstrap code.  Limiting
        // this lookup to INTERNAL made reflection-heavy plugins bypass the
        // route entirely and fail despite a valid runtime mapping.
        for(NativeRoute route:nativeRoutes){String target=route.target(name);if(target!=null)try{
            Class<?> resolved = route.loader().loadClass(target);
            return resolved;
        }catch(ClassNotFoundException absent){
        }}
        if (globals != null) {
            var global = globals.find(name, false);
            if (global.isPresent()) return global.get();
        }
        try{return realm.exported(name,checkLibraries);}catch(ClassNotFoundException absent){if(absent.getCause()!=null)throw absent;}
        Map<String,PluginRealm> available=realm.routingRealms();
        for(String id:checkGroup?realm.policy.routes(realm.pluginId(),available.keySet()):List.<String>of()){PluginRealm provider=available.get(id);if(provider==realm)continue;
            try{Class<?> found=provider.exported(name,realm.policy.exportLibraries(realm.pluginId(),id));realm.policy.accessed(realm.pluginId(),id,name);return found;}catch(ClassNotFoundException absent){if(absent.getCause()!=null)throw absent;}}
        // Paper plugins legitimately use Class.forName for NMS and CraftBukkit
        // symbols.  Registered native routes are the compatibility boundary for
        // those calls and must be available to plugin realms as well as internal
        // runtime code; the route still controls the exact target class.
        throw new ClassNotFoundException("CLASS_NOT_VISIBLE: "+realm.pluginId()+" "+name);
    }
    List<URL> resources(PluginRealm realm,String name)throws IOException {
        LinkedHashSet<URL> urls=new LinkedHashSet<>(authority.resources(name));
        if (!name.equals("META-INF/services/org.slf4j.spi.SLF4JServiceProvider")) urls.addAll(realm.exportedResources(name,true));
        Map<String,PluginRealm> available=realm.routingRealms();
        for(String id:realm.policy.routes(realm.pluginId(),available.keySet())){PluginRealm provider=available.get(id);if(provider==realm)continue;var exported=provider.exportedResources(name,realm.policy.exportLibraries(realm.pluginId(),id));if(!exported.isEmpty()){realm.policy.accessed(realm.pluginId(),id,name);urls.addAll(exported);}}
        if (realm.context.access() == TransformContext.Access.INTERNAL) for(NativeRoute route:nativeRoutes)if(route.resources().contains(name))urls.addAll(Collections.list(route.loader().getResources(name)));
        if (globals != null) urls.addAll(globals.resources(name));
        return List.copyOf(urls);
    }
    List<URL> globalResources(String name) throws IOException { return globals == null ? List.of() : globals.resources(name); }
    synchronized void removed(PluginRealm realm){realms.remove(realm.pluginId(),realm);}
    public <T> T withContext(PluginRealm realm, Callable<T> operation) throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        realm.retainExecution();
        Throwable original = null;
        try {
            thread.setContextClassLoader(realm);
            try { return operation.call(); }
            finally { thread.setContextClassLoader(previous); }
        } catch (Exception | Error failure) {
            original = failure;
            throw failure;
        } finally {
            try { realm.releaseExecution(); }
            catch (Exception | Error cleanup) {
                if (original == null) throw cleanup;
                original.addSuppressed(cleanup);
            }
        }
    }
    @Override public void close()throws IOException {List<PluginRealm> owned;synchronized(this){if(closed)return;closed=true;owned=new ArrayList<>(realms.values());}Collections.reverse(owned);IOException aggregate=null;for(PluginRealm realm:owned)try{realm.close();}catch(IOException failure){if(aggregate==null)aggregate=new IOException("REALM_CLOSE_FAILED");aggregate.addSuppressed(failure);}if(globals!=null)try{globals.close();}catch(IOException failure){if(aggregate==null)aggregate=new IOException("REALM_CLOSE_FAILED");aggregate.addSuppressed(failure);}if(aggregate!=null)throw aggregate;}
}
