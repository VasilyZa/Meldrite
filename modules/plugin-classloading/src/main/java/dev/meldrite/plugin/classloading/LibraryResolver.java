package dev.meldrite.plugin.classloading;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
/** Resolves a complete, prelocked dependency graph. It never guesses versions or hashes. */
public final class LibraryResolver {
    public record Artifact(String coordinate,String sha256,Optional<URI> source,List<String> dependencies) {
        public Artifact {if(!Objects.requireNonNull(coordinate).matches("[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+:[A-Za-z0-9_.+-]+")||!Objects.requireNonNull(sha256).matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Invalid locked library: "+coordinate);source=Objects.requireNonNull(source);dependencies=List.copyOf(dependencies);}
    }
    private final Path cache;
    private final Map<String,Artifact> artifacts;
    private final List<URI> repositories;
    private final boolean offline;
    private final boolean allowDeclaredJars;
    private final Set<Path> verified=new LinkedHashSet<>();
    public LibraryResolver(Path cache,List<Artifact> artifacts,List<URI> repositories,boolean offline){
        this(cache,artifacts,repositories,offline,false);
    }
    public LibraryResolver(Path cache,List<Artifact> artifacts,List<URI> repositories,boolean offline,boolean allowDeclaredJars){
        this.cache=cache.toAbsolutePath().normalize();this.repositories=List.copyOf(repositories);this.offline=offline;Map<String,Artifact> index=new LinkedHashMap<>();
        for(Artifact artifact:artifacts)if(index.putIfAbsent(artifact.coordinate(),artifact)!=null)throw new IllegalArgumentException("DUPLICATE_LIBRARY_COORDINATE: "+artifact.coordinate());
        this.artifacts=Map.copyOf(index);this.allowDeclaredJars=allowDeclaredJars;
    }
    public synchronized List<Path> resolve(List<String> coordinates)throws IOException {
        LinkedHashSet<Path> result=new LinkedHashSet<>();Set<String> seen=new HashSet<>();List<String> active=new ArrayList<>();for(String coordinate:coordinates)resolve(coordinate,result,seen,active);return List.copyOf(result);
    }
    private void resolve(String coordinate,Set<Path> result,Set<String> seen,List<String> active)throws IOException {
        if(active.contains(coordinate))throw new IOException("LIBRARY_LOCK_CYCLE: "+String.join(" -> ",active)+" -> "+coordinate);
        if(!seen.add(coordinate))return;Artifact artifact=artifacts.get(coordinate);if(artifact==null)throw new IOException("UNLOCKED_LIBRARY: "+coordinate);
        active.add(coordinate);result.add(obtain(artifact));for(String dependency:artifact.dependencies())resolve(dependency,result,seen,active);active.remove(active.size()-1);
    }
    /** Paper JarLibrary declares paths; bytes must match a locked artifact before entering a realm. */
    public synchronized List<Path> acceptJar(Path declared)throws IOException {
        String actual=sha256(declared);List<String> coordinates=artifacts.values().stream().filter(a->a.sha256().equals(actual)).map(Artifact::coordinate).sorted().toList();
        if(coordinates.isEmpty()) {
            if (allowDeclaredJars) return acceptDeclaredJar(declared, actual);
            throw new IOException("UNLOCKED_LIBRARY_BYTES: "+declared+" sha256="+actual);
        }
        Files.createDirectories(cache);Path destination=cache.resolve(actual+".jar");
        if(Files.exists(destination))verify(destination,artifacts.get(coordinates.get(0)));else {Path temporary=Files.createTempFile(cache,"library-import-",".part");try{Files.copy(declared,temporary,StandardCopyOption.REPLACE_EXISTING);verify(temporary,artifacts.get(coordinates.get(0)));Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temporary);}}
        return resolve(coordinates);
    }
    private List<Path> acceptDeclaredJar(Path declared, String actual) throws IOException {
        if (!Files.isRegularFile(declared)) throw new IOException("DECLARED_LIBRARY_MISSING: " + declared);
        Files.createDirectories(cache);Path destination=cache.resolve(actual+".jar");
        if (!Files.exists(destination)) {Path temporary=Files.createTempFile(cache,"declared-library-",".part");try {Files.copy(declared,temporary,StandardCopyOption.REPLACE_EXISTING);if(!sha256(temporary).equals(actual))throw new IOException("DECLARED_LIBRARY_CHANGED: "+declared);Files.move(temporary,destination,StandardCopyOption.REPLACE_EXISTING);} finally {Files.deleteIfExists(temporary);}}
        verified.add(destination);return List.of(destination);
    }
    public synchronized Set<Path> verifiedPaths(){return Set.copyOf(verified);}
    private Path obtain(Artifact artifact)throws IOException {
        Path target=cache.resolve(artifact.sha256()+".jar");if(Files.exists(target)){verify(target,artifact);verified.add(target);return target;}
        if(offline)throw new IOException("OFFLINE_LIBRARY_MISSING: "+artifact.coordinate()+" sha256="+artifact.sha256());
        URI source=artifact.source().orElseThrow(()->new IOException("LIBRARY_SOURCE_MISSING: "+artifact.coordinate()));
        if(!allowed(source))throw new IOException("LIBRARY_REPOSITORY_DENIED: "+artifact.coordinate()+" "+source);
        Files.createDirectories(cache);Path temporary=Files.createTempFile(cache,"library-download-",".part");
        try{
            URLConnection connection=source.toURL().openConnection();connection.setConnectTimeout(10_000);connection.setReadTimeout(15_000);
            if(connection instanceof HttpURLConnection http){http.setInstanceFollowRedirects(false);int status=http.getResponseCode();if(status!=200){http.disconnect();throw new IOException("LIBRARY_DOWNLOAD_HTTP_"+status+": "+artifact.coordinate()+" "+source);}}
            try(InputStream input=connection.getInputStream();OutputStream output=Files.newOutputStream(temporary)){byte[] buffer=new byte[8192];long bytes=0;int count;while((count=input.read(buffer))!=-1){bytes+=count;if(bytes>268_435_456)throw new IOException("LIBRARY_SIZE_LIMIT: "+artifact.coordinate());output.write(buffer,0,count);}}
            verify(temporary,artifact);Files.move(temporary,target,StandardCopyOption.REPLACE_EXISTING);verified.add(target);return target;
        }catch(IOException failure){throw new IOException("LIBRARY_RESOLUTION_FAILED: "+artifact.coordinate()+" "+source+": "+failure.getMessage(),failure);}
        finally{Files.deleteIfExists(temporary);}
    }
    private boolean allowed(URI source){
        if(!Set.of("http","https").contains(source.getScheme())||source.getUserInfo()!=null||source.getQuery()!=null||source.getFragment()!=null||!source.equals(source.normalize())||Arrays.asList(source.getPath().split("/")).contains(".."))return false;
        return repositories.stream().anyMatch(repository->Objects.equals(repository.getScheme(),source.getScheme())&&Objects.equals(repository.getAuthority(),source.getAuthority())&&source.getPath().startsWith(repository.getPath().endsWith("/")?repository.getPath():repository.getPath()+"/"));
    }
    private static void verify(Path file,Artifact artifact)throws IOException {String actual=sha256(file);if(!actual.equals(artifact.sha256()))throw new IOException("LIBRARY_SHA_MISMATCH: "+artifact.coordinate()+" expected="+artifact.sha256()+" actual="+actual);}
    private static String sha256(Path file)throws IOException {try{MessageDigest digest=MessageDigest.getInstance("SHA-256");try(var input=Files.newInputStream(file)){byte[] bytes=new byte[8192];int count;while((count=input.read(bytes))!=-1)digest.update(bytes,0,count);}return HexFormat.of().formatHex(digest.digest());}catch(NoSuchAlgorithmException impossible){throw new IOException(impossible);}}
}
