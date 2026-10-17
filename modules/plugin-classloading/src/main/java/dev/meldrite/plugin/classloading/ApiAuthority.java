package dev.meldrite.plugin.classloading;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
/** Fixed definition authority derived from the complete locked provider classpath. */
public final class ApiAuthority {
    private final ClassLoader loader;
    private final ClassLoader realmParent;
    private final Set<String> packages;
    private final Set<String> resources;
    public ApiAuthority(ClassLoader loader, List<Path> jars) throws IOException {
        this.loader=Objects.requireNonNull(loader);this.realmParent=new RealmParent(loader);Set<String> packages=new HashSet<>(),resources=new HashSet<>();
        for(Path path:jars)try(JarFile jar=new JarFile(path.toFile(),true,JarFile.OPEN_READ,Runtime.version())){
            jar.versionedStream().filter(e->!e.isDirectory()).forEach(e->{String name=e.getName();resources.add(name);if(name.endsWith(".class")&&!name.equals("module-info.class")){int slash=name.lastIndexOf('/');packages.add(slash<0?"":name.substring(0,slash).replace('/','.'));}});
        }
        this.packages=Set.copyOf(packages);this.resources=Set.copyOf(resources);
    }
    public boolean owns(String name){int dot=name.lastIndexOf('.');return packages.contains(dot<0?"":name.substring(0,dot));}
    Class<?> load(String name)throws ClassNotFoundException{return loader.loadClass(name);}
    ClassLoader realmParent(){return realmParent;}
    List<URL> resources(String name)throws IOException{return resources.contains(name)?Collections.list(loader.getResources(name)):List.of();}
    /** Service contexts can inherit the host ancestry without adding an implicit host visibility route. */
    private static final class RealmParent extends ClassLoader {
        static { registerAsParallelCapable(); }
        RealmParent(ClassLoader host){super(host);}
        @Override protected Class<?> loadClass(String name,boolean resolve)throws ClassNotFoundException {
            return ClassLoader.getPlatformClassLoader().loadClass(name);
        }
        @Override public URL getResource(String name){return ClassLoader.getPlatformClassLoader().getResource(name);}
        @Override public Enumeration<URL> getResources(String name)throws IOException{return ClassLoader.getPlatformClassLoader().getResources(name);}
    }
    void validate(Path path)throws IOException { validate(path, false); }
    /** A host-defined global JAR cannot rely on plugin-domain API delegation. */
    void validateGlobal(Path path)throws IOException { validate(path, true); }
    private void validate(Path path,boolean global)throws IOException {
        try(JarFile jar=new JarFile(path.toFile(),true,JarFile.OPEN_READ,Runtime.version())){
            var manifest=jar.getManifest();
            if(manifest!=null){String classpath=manifest.getMainAttributes().getValue(java.util.jar.Attributes.Name.CLASS_PATH);if(classpath!=null&&!classpath.isBlank())throw new IllegalStateException("UNDECLARED_MANIFEST_CLASSPATH: "+path);}
            for(var iterator=jar.versionedStream().iterator();iterator.hasNext();){var entry=iterator.next();String name=entry.getName();if(!name.endsWith(".class")||name.equals("module-info.class"))continue;
                String symbol=name.substring(0,name.length()-6).replace('/','.');
                // Some Bukkit plugins bundle a few unmodified org.bukkit API classes. They must
                // resolve to the platform authority; rejecting the whole plugin prevents those
                // plugins from loading even though the duplicate bytes are never defined.
                // Bukkit plugins often shade the API and its transitive libraries. The
                // authority still wins for those symbols, while the Adventure API remains
                // strict because two Component identities cannot safely coexist.
                boolean sharedIdentity = symbol.startsWith("net.kyori.") || symbol.startsWith("dev.meldrite.");
                if((owns(symbol)&&(global||sharedIdentity))||symbol.startsWith("java.")||symbol.startsWith("jdk.")||symbol.startsWith("sun."))throw new IllegalStateException("DUPLICATE_API_DEFINITION: "+path+"!"+symbol);
            }
        }
    }
}
