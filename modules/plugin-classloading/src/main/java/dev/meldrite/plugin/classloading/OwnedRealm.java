package dev.meldrite.plugin.classloading;
import dev.meldrite.plugin.spi.*;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.*;
/** JAR definition preserves certificates, CodeSource and package sealing. No implicit host fallback. */
abstract class OwnedRealm extends URLClassLoader {
    static { registerAsParallelCapable(); }
    final ApiAuthority authority;
    final TransformContext context;
    private final ClassTransform transform;
    private final Map<String,byte[]> definitions=new HashMap<>();
    private final Set<String> validatedUrls=new HashSet<>();
    volatile boolean closed;
    OwnedRealm(List<Path> jars,ApiAuthority authority,TransformContext context,ClassTransform transform)throws IOException {
        super(urls(jars),authority.realmParent());this.authority=authority;this.context=context;this.transform=transform;
        try{for(Path path:jars){authority.validate(path);validatedUrls.add(path.toUri().toURL().toExternalForm());}}catch(IOException|RuntimeException failure){try{super.close();}catch(IOException cleanup){failure.addSuppressed(cleanup);}throw failure;}
    }
    private static URL[] urls(List<Path> jars)throws MalformedURLException {List<URL> urls=new ArrayList<>();for(Path path:jars)urls.add(path.toUri().toURL());return urls.toArray(URL[]::new);}
    /** Reflection-based appenders may mutate URLClassPath without calling addURL. */
    private synchronized void validateAppendedUrls() throws IOException {
        for (URL url : getURLs()) {
            validateUrl(url);
        }
    }
    private synchronized void validateUrl(URL url) throws IOException {
        String key = url.toExternalForm();
        if (validatedUrls.contains(key)) return;
        if (!url.getProtocol().equals("file")) throw new IOException("UNSUPPORTED_LIBRARY_URL: " + url);
        final Path path;
        try { path = Path.of(url.toURI()); }
        catch (URISyntaxException | IllegalArgumentException failure) { throw new IOException("INVALID_LIBRARY_URL: " + url, failure); }
        authority.validate(path);
        try { transform.prepareLibrary(path, context); }
        catch (IOException | RuntimeException failure) { throw failure; }
        catch (Exception failure) { throw new IOException("LIBRARY_PREPARATION_FAILED: " + path, failure); }
        validatedUrls.add(key);
    }
    /** Appenders can mutate URLClassPath between its snapshot and the actual lookup. */
    private void validateOrigin(URL resource) throws IOException {
        if (resource == null) return;
        if (!resource.getProtocol().equals("jar")) throw new IOException("UNSUPPORTED_LIBRARY_RESOURCE: " + resource);
        var connection = (JarURLConnection) resource.openConnection();
        connection.setUseCaches(false);
        URL origin = connection.getJarFileURL();
        if (Arrays.stream(getURLs()).noneMatch(url -> url.toExternalForm().equals(origin.toExternalForm())))
            throw new IOException("UNDECLARED_LIBRARY_RESOURCE: " + origin);
        validateUrl(origin);
    }
    @Override public URL findResource(String name) {
        if (closed) return null;
        try {
            validateAppendedUrls();
            URL resource = super.findResource(name);
            validateOrigin(resource);
            return resource;
        } catch (IOException failure) { throw new UncheckedIOException(failure); }
    }
    @Override public Enumeration<URL> findResources(String name) throws IOException {
        if (closed) return Collections.emptyEnumeration();
        validateAppendedUrls();
        var found = Collections.list(super.findResources(name));
        for (URL resource : found) validateOrigin(resource);
        return Collections.enumeration(found);
    }
    final Class<?> shared(String name)throws ClassNotFoundException {
        try{return ClassLoader.getPlatformClassLoader().loadClass(name);}catch(ClassNotFoundException absent){if(authority.owns(name))return authority.load(name);throw absent;}
    }
    @Override public URL getResource(String name) {
        if (name.equals("META-INF/services/org.slf4j.spi.SLF4JServiceProvider")) {
            try { return authority.resources(name).stream().findFirst().orElse(null); }
            catch (IOException failure) { throw new UncheckedIOException(failure); }
        }
        return super.getResource(name);
    }
    final Optional<Class<?>> sharedIfPresent(String name)throws ClassNotFoundException {
        try{return Optional.of(shared(name));}catch(ClassNotFoundException absent){if(authority.owns(name))throw absent;return Optional.empty();}
    }
    final Class<?> own(String name)throws ClassNotFoundException {
        if(closed)throw new ClassNotFoundException("REALM_CLOSED: "+context.pluginId()+" "+name);
        synchronized(getClassLoadingLock(name)){Class<?> loaded=findLoadedClass(name);return loaded!=null?loaded:findClass(name);}
    }
    @Override protected Class<?> findClass(String name)throws ClassNotFoundException {
        String resource=name.replace('.','/')+".class";URL url=findResource(resource);if(url==null)throw new ClassNotFoundException(name);
        try{
            JarURLConnection connection=(JarURLConnection)url.openConnection();connection.setUseCaches(false);
            byte[] source;Manifest manifest;java.security.cert.Certificate[] certificates;URL location=connection.getJarFileURL();
            try(JarFile jar=connection.getJarFile()){
                JarEntry entry=jar.getJarEntry(connection.getEntryName());
                try(InputStream input=jar.getInputStream(entry)){source=input.readAllBytes();}
                manifest=jar.getManifest();certificates=entry.getCertificates();
            }
            byte[] prepared=Objects.requireNonNull(transform.prepare(source.clone(),context),"Null prepared class bytes");
            byte[] bytes=context.access()==TransformContext.Access.API_ONLY?prepared:Objects.requireNonNull(transform.apply(prepared,context),"Null transformed class bytes");
            if(certificates!=null&&!Arrays.equals(source,bytes))throw new SecurityException("Signed class transformation requires an explicit signature policy: "+name);
            int dot=name.lastIndexOf('.');if(dot>=0)ensurePackage(name.substring(0,dot),manifest,location);
            Class<?> defined=defineClass(name,bytes,0,bytes.length,new CodeSource(location,certificates));
            synchronized(definitions){definitions.put(name,bytes.clone());}return defined;
        }catch(Exception failure){throw new ClassNotFoundException("CLASS_DEFINITION_FAILED: "+context.pluginId()+" "+name,failure);}
    }
    private synchronized void ensurePackage(String name,Manifest manifest,URL location){
        Package existing=getDefinedPackage(name);String path=name.replace('.','/')+"/";
        Attributes attributes=manifest==null?null:manifest.getAttributes(path);String sealed=attributes==null?null:attributes.getValue(Attributes.Name.SEALED);
        if(sealed==null&&manifest!=null)sealed=manifest.getMainAttributes().getValue(Attributes.Name.SEALED);
        if(existing==null){if(manifest==null)definePackage(name,null,null,null,null,null,null,null);else definePackage(name,manifest,location);}
        else if(existing.isSealed()?!existing.isSealed(location):"true".equalsIgnoreCase(sealed))throw new SecurityException("Package sealing violation: "+name);
    }
    public byte[] definitionBytes(String name){synchronized(definitions){byte[] bytes=definitions.get(name);if(bytes==null)throw new IllegalArgumentException("Class not defined here: "+name);return bytes.clone();}}
    final List<URL> ownResources(String name)throws IOException{return closed?List.of():Collections.list(findResources(name));}
    @Override public void close()throws IOException {closed=true;synchronized(definitions){definitions.clear();}super.close();}
}
