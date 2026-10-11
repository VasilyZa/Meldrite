package dev.meldrite.plugin.runtime;

import dev.meldrite.plugin.spi.DescriptorReader;
import dev.meldrite.plugin.spi.PluginDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.jar.JarFile;

/** Descriptor-only discovery with bounded reads; no ClassLoader is created here. */
public final class PluginDiscovery {
    public record DiscoveredPlugin(Path jar,String sha256,PluginDescriptor descriptor) {}
    private final DescriptorReader reader;
    public PluginDiscovery(DescriptorReader reader){this.reader=Objects.requireNonNull(reader);}
    public List<DiscoveredPlugin> discover(Path directory)throws IOException{
        if(!Files.isDirectory(directory))throw new IOException("PLUGIN_DIRECTORY_UNAVAILABLE: "+directory);
        List<Path> jars;try(var files=Files.list(directory)){jars=files.filter(Files::isRegularFile).filter(p->p.getFileName().toString().endsWith(".jar")&&!p.getFileName().toString().startsWith(".")).sorted().toList();}
        List<DiscoveredPlugin> result=new ArrayList<>();int index=0;
        for(Path path:jars){
            try(JarFile jar=new JarFile(path.toFile(),true,java.util.zip.ZipFile.OPEN_READ,JarFile.runtimeVersion())){
                Map<String,String> documents=new LinkedHashMap<>();
                for(String name:reader.descriptorNames()){
                    var entry=jar.getJarEntry(name);if(entry==null)continue;
                    try(var input=jar.getInputStream(entry)){
                        byte[] bytes=input.readNBytes(1_048_577);if(bytes.length>1_048_576)throw new IOException("DESCRIPTOR_TOO_LARGE: "+path+"!"+name);
                        String text=StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();documents.put(name,text);
                    }
                }
                if(documents.isEmpty())throw new IOException("PLUGIN_DESCRIPTOR_MISSING: "+path);
                result.add(new DiscoveredPlugin(path.toAbsolutePath().normalize(),sha256(path),reader.read(path,Collections.unmodifiableMap(documents),index++)));
            }
        }
        return List.copyOf(result);
    }
    private static String sha256(Path file)throws IOException{
        try{MessageDigest digest=MessageDigest.getInstance("SHA-256");try(var stream=Files.newInputStream(file)){byte[] bytes=new byte[8192];int count;while((count=stream.read(bytes))!=-1)digest.update(bytes,0,count);}return HexFormat.of().formatHex(digest.digest());}
        catch(NoSuchAlgorithmException impossible){throw new IOException("SHA-256 unavailable",impossible);}
    }
}
