package dev.meldrite.paper;
import dev.meldrite.plugin.classloading.*;
import dev.meldrite.plugin.runtime.PluginDiscovery.DiscoveredPlugin;
import dev.meldrite.plugin.spi.*;
import io.papermc.paper.plugin.loader.*;
import io.papermc.paper.plugin.loader.library.ClassPathLibrary;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Server;
public final class PaperProviderAdapter {
    private final PluginLoaderEngine engine;private final Server server;private final Path dataRoot;private final LibraryResolver resolver;
    public PaperProviderAdapter(PluginLoaderEngine engine,Server server,Path dataRoot,LibraryResolver resolver){this.engine=engine;this.server=server;this.dataRoot=dataRoot.toAbsolutePath().normalize();this.resolver=Objects.requireNonNull(resolver);}
    private final Map<String,MeldriteConfiguredClassLoader> registered=new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<String,PhaseVisibilityPolicy> policies=new LinkedHashMap<>();
    public List<PluginHostAdapter> prepare(List<DiscoveredPlugin> discovered,LoadPlan plan)throws Exception {
        List<PluginHostAdapter> hosts=new ArrayList<>();
        String current="unknown";List<String> failures=new ArrayList<>();List<Throwable> causes=new ArrayList<>();try{for(var found:discovered){
            PluginDescriptor descriptor=found.descriptor();String id=descriptor.identity().id();current=id;Path data=dataRoot.resolve(id).normalize();if(!data.startsWith(dataRoot)||data.equals(dataRoot))throw new IllegalArgumentException("INVALID_PLUGIN_DATA_PATH: "+id);Files.createDirectories(data);
            var metadata=PluginMetaAdapter.create(descriptor);var logger=Logger.getLogger("Meldrite.Plugin."+id);var context=new BootstrapContextAdapter(metadata.meta(),data,ComponentLogger.logger(logger.getName()),found.jar());List<Path> libraries=new ArrayList<>();
            try {
            if(descriptor.loader().isPresent())try(var initial=engine.open(id+"/loader",found.jar(),List.of(),TransformContext.Access.API_ONLY,new PaperVisibilityPolicy(Map.of()))){
                PluginClasspathBuilder builder=new PluginClasspathBuilder(){
                    public PluginClasspathBuilder addLibrary(ClassPathLibrary library){
                        try {
                            library.register(path->{try{libraries.addAll(resolver.acceptJar(path));}catch(java.io.IOException failure){throw new IllegalStateException("PLUGIN_LIBRARY_FAILED: "+id+" "+path+": "+failure.getMessage(),failure);}});
                        } catch (RuntimeException failure) {
                            List<Path> fallback = localLibraryFallback(library);
                            if (fallback.isEmpty()) throw failure;
                            Logger.getLogger("Meldrite.Plugin." + id).warning("Paper library resolver failed; using verified local cache for " + id + ": " + failure.getMessage());
                            for (Path path : fallback) try { libraries.addAll(resolver.acceptJar(path)); }
                            catch (java.io.IOException localFailure) { throw new IllegalStateException("PLUGIN_LIBRARY_FAILED: " + id + " " + path + ": " + localFailure.getMessage(), localFailure); }
                        }
                        return this;
                    }
                    public BootstrapContextAdapter getContext(){return context;}
                };
                engine.withContext(initial,()->{var loader=initial.loadClass(descriptor.loader().orElseThrow()).asSubclass(PluginLoader.class).getConstructor().newInstance();loader.classloader(builder);return loader;});
            }
            if(descriptor.kind()==PluginDescriptor.Kind.LEGACY)libraries.addAll(resolver.resolve(metadata.description().getLibraries()));
            var policy=new PhaseVisibilityPolicy(new PaperVisibilityPolicy(plan.bootstrapVisibility()));policies.put(id,policy);
                var access = dev.meldrite.paper.compat.InternalAccessDetector.requiresRemapping(id, found.jar(), libraries)
                        ? TransformContext.Access.INTERNAL : TransformContext.Access.API_ONLY;
                ClassTransform preparation=metadata.meta().getAPIVersion()==null?(bytes,c)->bytes:new dev.meldrite.paper.compat.ModernMaterialTransform();
                var configured=(MeldriteConfiguredClassLoader)engine.open(id,found.jar(),libraries,access,policy,preparation,(e,p,l,a,c,t,v)->new MeldriteConfiguredClassLoader(e,p,l,a,c,t,v,server,metadata,data,logger,registered));registered.put(id,configured);
                hosts.add(new PluginHostAdapter(descriptor,new PluginScope(),engine,configured,context));
            } catch (Throwable failure) {
                failures.add(id + ": " + failure.getMessage());
                causes.add(new IllegalStateException("Plugin provider failed: " + id, failure));
            }
        }
        if (!failures.isEmpty()) {
            var failure = new IllegalStateException("PLUGIN_PROVIDER_FAILURES: " + failures);
            causes.forEach(failure::addSuppressed);
            throw failure;
        }
        return List.copyOf(hosts);
        }catch(Exception|Error failure){
            List<PluginHostAdapter> reverse=new ArrayList<>(hosts);Collections.reverse(reverse);for(var host:reverse){try{host.scope().close();}catch(Throwable cleanup){if(cleanup!=failure)failure.addSuppressed(cleanup);}policies.remove(host.identity().id());}
            policies.remove(current);throw new IllegalStateException("PLUGIN_PROVIDER_FAILED: "+current+": "+failure.getMessage(),failure);
        }
    }
    public void bootstrap(List<PluginHostAdapter> hosts,LoadPlan plan)throws Exception {
        Map<String,PluginHostAdapter> byId=new LinkedHashMap<>();hosts.forEach(host->byId.put(host.identity().id(),host));
        Map<String,String> aliases=new HashMap<>();hosts.forEach(host->{aliases.put(host.identity().id(),host.identity().id());host.descriptor().provides().forEach(alias->aliases.put(alias,host.identity().id()));});
        for(String id:plan.bootstrapOrder()){
            var host=byId.get(id);Throwable blocked=null;
            for(DependencyEdge edge:host.descriptor().dependencies())if(edge.phase()==DependencyEdge.Phase.BOOTSTRAP&&edge.required()){
                var target=byId.get(aliases.get(edge.target()));if(target!=null&&target.bootstrapFailure().isPresent()){blocked=new IllegalStateException("BLOCKED_BOOTSTRAP_DEPENDENCY: "+id+" -> "+edge.target(),target.bootstrapFailure().get());break;}
            }
            if(blocked!=null)host.rejectBootstrap(blocked);
            else try{host.bootstrap();}catch(Throwable failure){if(failure instanceof InterruptedException)Thread.currentThread().interrupt();host.rejectBootstrap(failure);java.util.logging.Logger.getLogger("Meldrite.Plugin."+id).log(java.util.logging.Level.SEVERE,"Plugin bootstrap failed: "+id,failure);}
        }
        boolean changed;
        do{changed=false;for(PluginHostAdapter host:hosts){if(host.bootstrapFailure().isPresent())continue;
            for(DependencyEdge edge:host.descriptor().dependencies())if(edge.phase()==DependencyEdge.Phase.BOOTSTRAP&&edge.required()){
                var target=byId.get(aliases.get(edge.target()));if(target!=null&&target.bootstrapFailure().isPresent()){host.rejectBootstrap(new IllegalStateException("BLOCKED_BOOTSTRAP_DEPENDENCY: "+host.identity().id()+" -> "+edge.target(),target.bootstrapFailure().get()));changed=true;break;}
            }
        }}while(changed);
        for(PluginHostAdapter host:hosts){var policy=policies.get(host.identity().id());policy.use(host.loader().getConfiguration() instanceof org.bukkit.plugin.PluginDescriptionFile?new LegacyVisibilityPolicy(plan.visibilityGraph(),java.util.logging.Logger.getLogger("Meldrite.Plugin."+host.identity().id())::warning):new PaperVisibilityPolicy(plan.visibilityGraph()));}
    }

    /**
     * Paper's Maven resolver normally downloads loader libraries.  A server
     * may be intentionally offline (or have a broken TLS path), so recover
     * only from jars already present in the local Maven/server caches.  The
     * returned paths still pass through LibraryResolver's byte admission.
     */
    private List<Path> localLibraryFallback(ClassPathLibrary library) {
        if (!library.getClass().getName().equals("io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver")) return List.of();
        List<LibraryCoordinate> requested = new ArrayList<>();
        try {
            Field field = library.getClass().getDeclaredField("dependencies");
            if (!field.trySetAccessible()) return List.of();
            Object dependencies = field.get(library);
            if (!(dependencies instanceof Iterable<?> values)) return List.of();
            for (Object dependency : values) {
                Object artifact = dependency.getClass().getMethod("getArtifact").invoke(dependency);
                String group = String.valueOf(artifact.getClass().getMethod("getGroupId").invoke(artifact));
                String name = String.valueOf(artifact.getClass().getMethod("getArtifactId").invoke(artifact));
                String version = String.valueOf(artifact.getClass().getMethod("getVersion").invoke(artifact));
                String extension = String.valueOf(artifact.getClass().getMethod("getExtension").invoke(artifact));
                String classifier = String.valueOf(artifact.getClass().getMethod("getClassifier").invoke(artifact));
                if (!"jar".equals(extension) || !classifier.isBlank()) continue;
                requested.add(new LibraryCoordinate(group, name, version));
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return List.of();
        }
        if (requested.isEmpty()) return List.of();
        LinkedHashSet<Path> result = new LinkedHashSet<>();
        for (LibraryCoordinate coordinate : requested) {
            Path path = findLocalLibrary(coordinate);
            if (path != null) result.add(path);
            if ("com.fasterxml.jackson.core".equals(coordinate.group()) && "jackson-databind".equals(coordinate.name())) {
                for (String companion : List.of("jackson-core", "jackson-annotations")) {
                    Path companionPath = findLocalLibrary(new LibraryCoordinate(coordinate.group(), companion, coordinate.version()));
                    if (companionPath == null) companionPath = findAnyLocalLibrary(coordinate.group(), companion);
                    if (companionPath != null) result.add(companionPath);
                }
            }
        }
        return List.copyOf(result);
    }

    private Path findLocalLibrary(LibraryCoordinate coordinate) {
        List<Path> roots = localLibraryRoots();
        for (Path root : roots) {
            Path direct = root.resolve(coordinate.group().replace('.', '/')).resolve(coordinate.name()).resolve(coordinate.version())
                    .resolve(coordinate.name() + "-" + coordinate.version() + ".jar");
            if (Files.isRegularFile(direct)) return direct;
            Path gradle = root.resolve(coordinate.group()).resolve(coordinate.name()).resolve(coordinate.version());
            Path found = firstJar(gradle, coordinate.name() + "-" + coordinate.version() + ".jar");
            if (found != null) return found;
        }
        return null;
    }

    private Path findAnyLocalLibrary(String group, String name) {
        List<Path> roots = localLibraryRoots();
        for (Path root : roots) {
            Path maven = root.resolve(group.replace('.', '/')).resolve(name);
            if (Files.isDirectory(maven)) {
                try (var versions = Files.list(maven)) {
                    List<Path> candidates = versions.sorted(Comparator.reverseOrder()).toList();
                    for (Path version : candidates) {
                        Path found = firstJar(version, name + "-" + version.getFileName() + ".jar");
                        if (found != null) return found;
                    }
                } catch (java.io.IOException ignored) { }
            }
            Path gradle = root.resolve(group).resolve(name);
            if (Files.isDirectory(gradle)) {
                try (var versions = Files.list(gradle)) {
                    for (Path version : versions.sorted(Comparator.reverseOrder()).toList()) {
                        Path found = firstJar(version, name + "-" + version.getFileName() + ".jar");
                        if (found != null) return found;
                    }
                } catch (java.io.IOException ignored) { }
            }
        }
        return null;
    }

    private static Path firstJar(Path directory, String fileName) {
        if (!Files.isDirectory(directory)) return null;
        Path direct = directory.resolve(fileName);
        if (Files.isRegularFile(direct)) return direct;
        try (var hashes = Files.list(directory)) {
            for (Path hash : hashes.toList()) {
                Path candidate = hash.resolve(fileName);
                if (Files.isRegularFile(candidate)) return candidate;
            }
        } catch (java.io.IOException ignored) { }
        return null;
    }

    private List<Path> localLibraryRoots() {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        Path home = Path.of(System.getProperty("user.home", "."));
        roots.add(home.resolve(".m2/repository"));
        roots.add(home.resolve(".gradle/caches/modules-2/files-2.1"));
        roots.add(dataRoot.getParent() == null ? dataRoot : dataRoot.getParent().getParent().resolve("libraries"));
        roots.add(Path.of("libraries").toAbsolutePath().normalize());
        try (var directories = Files.list(Path.of("/tmp"))) {
            directories.filter(Files::isDirectory).filter(path -> path.getFileName().toString().startsWith("meldrite-server-"))
                    .map(path -> path.resolve("libraries")).forEach(roots::add);
        } catch (java.io.IOException ignored) { }
        return List.copyOf(roots);
    }

    private record LibraryCoordinate(String group, String name, String version) { }

}
