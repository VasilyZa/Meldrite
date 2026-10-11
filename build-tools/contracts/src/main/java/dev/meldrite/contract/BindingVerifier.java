package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import java.nio.file.*;
import org.objectweb.asm.Opcodes;

/** Separates exact surface, ownership, and measured behavior. Failures never become support claims. */
public final class BindingVerifier {
    private BindingVerifier() {}
    public static ObjectNode initialBindings(JsonNode inventory) {
        ObjectNode document=ApiInventory.JSON.createObjectNode().put("schemaVersion",1);
        ObjectNode entries=document.putObject("entries");
        for(JsonNode symbol:inventory.path("symbols")){
            String id=symbol.path("id").asText();boolean vendor=symbol.path("provenance").asText().equals("TRANSITIVE_API");
            boolean required=symbol.path("requiresImplementation").asBoolean()&&!vendor;
            CapabilityWorklist.Assignment assignment=CapabilityWorklist.assign(symbol);
            ObjectNode binding=entries.putObject(id).put("symbolId",id).put("status",required?"UNSUPPORTED":"NOT_APPLICABLE")
                    .put("adapterFile",assignment.adapterFile()).put("serviceOwner",assignment.owner()).put("capability",assignment.capability());
            binding.put("reason",required?"Server implementation has not been delivered or behavior-verified.":
                    "Provided verbatim by locked artifact "+inventory.path("coordinate").asText()+" ("+inventory.path("sha256").asText()+"); this disposition makes no server behavior claim.");
            binding.put("failureContract",required?"UNSUPPORTED_OPERATION with symbol ID; never return a fabricated value.":"Upstream provider owns this symbol; downstream server obligations are independently audited.");
            binding.putArray("evidenceIds");
        }
        return document;
    }
    public static Map<String,Integer> verify(JsonNode expected,JsonNode actual,JsonNode bindings,JsonNode evidence){
        Map<String,JsonNode> before=index(expected.path("symbols")),after=index(actual.path("symbols"));
        for(var entry:before.entrySet()){
            JsonNode changed=after.get(entry.getKey());
            if(changed==null){
                JsonNode original=entry.getValue();boolean descriptorChange=after.values().stream().anyMatch(s->s.path("owner").equals(original.path("owner"))&&s.path("name").equals(original.path("name"))&&s.path("kind").equals(original.path("kind")));
                fail(descriptorChange?"DESCRIPTOR_CHANGED":"MISSING_SYMBOL",entry.getKey());
            }
        }
        for(var entry:before.entrySet())if(!entry.getValue().equals(after.get(entry.getKey())))fail("SURFACE_CHANGED",entry.getKey()+" expected="+entry.getValue()+" actual="+after.get(entry.getKey()));
        if(!before.keySet().equals(after.keySet()))fail("UNEXPECTED_SYMBOL",difference(after.keySet(),before.keySet()).toString());
        if(!expected.path("classes").equals(actual.path("classes"))||!expected.path("resources").equals(actual.path("resources"))||!expected.path("unresolvedHierarchy").equals(actual.path("unresolvedHierarchy")))fail("SURFACE_CHANGED","class/source metadata or resources");
        if(bindings.path("schemaVersion").asInt()!=1||!bindings.path("entries").isObject())fail("INVALID_BINDINGS","schemaVersion/entries");
        Map<String,JsonNode> entries=new TreeMap<>();bindings.path("entries").fields().forEachRemaining(e->entries.put(e.getKey(),e.getValue()));
        if(!before.keySet().equals(entries.keySet())){
            Set<String> missing=difference(before.keySet(),entries.keySet());fail(missing.isEmpty()?"UNKNOWN_BINDING":"MISSING_BINDING",missing.isEmpty()?difference(entries.keySet(),before.keySet()).toString():missing.toString());
        }
        Map<String,JsonNode> scenarios=new HashMap<>();
        if(!evidence.path("scenarios").isArray())fail("INVALID_EVIDENCE","scenarios array required");
        for(JsonNode scenario:evidence.path("scenarios"))if(scenarios.put(scenario.path("id").asText(),scenario)!=null)fail("INVALID_EVIDENCE","duplicate scenario "+scenario.path("id"));
        Map<String,Integer> counts=new TreeMap<>();for(String state:List.of("SUPPORTED","PARTIAL","UNSUPPORTED","NOT_APPLICABLE"))counts.put(state,0);
        for(var entry:entries.entrySet()){
            String id=entry.getKey();JsonNode binding=entry.getValue(),symbol=before.get(id);String status=binding.path("status").asText();
            if(!counts.containsKey(status)||!id.equals(binding.path("symbolId").asText()))fail("INVALID_STATUS",id);
            for(String field:List.of("adapterFile","serviceOwner","capability"))if(!binding.path(field).isTextual()||binding.path(field).asText().isBlank())fail("INVALID_BINDING",id+" missing "+field);
            if(!binding.path("evidenceIds").isArray())fail("INVALID_BINDING",id+" evidenceIds");
            if(status.equals("SUPPORTED")||status.equals("PARTIAL")){
                if(binding.path("evidenceIds").isEmpty())fail("MISSING_EVIDENCE",id);
                for(JsonNode evidenceId:binding.path("evidenceIds")){
                    JsonNode scenario=scenarios.get(evidenceId.asText());
                    if(scenario==null||!scenario.path("result").asText().equals("PASS")||!scenario.path("behavior").asBoolean()||!scenario.path("regression").asBoolean())fail("MISSING_BEHAVIOR_EVIDENCE",id+" -> "+evidenceId.asText());
                    if(!scenario.path("symbolIds").isArray()||!contains(scenario.path("symbolIds"),id))fail("MISSING_BEHAVIOR_EVIDENCE",id+" scenario does not exercise symbol");
                }
                if(!binding.path("implementation").isObject())fail("MISSING_SYMBOL_BINDING",id);
                if(status.equals("PARTIAL")&&(!binding.path("limits").isArray()||binding.path("limits").isEmpty()))fail("INVALID_STATUS",id+" needs explicit limits");
            } else {
                if(binding.path("reason").asText().isBlank()||binding.path("failureContract").asText().isBlank())fail("INVALID_STATUS",id+" needs reason/failure contract");
                if(status.equals("NOT_APPLICABLE")&&symbol.path("requiresImplementation").asBoolean()&&!symbol.path("provenance").asText().equals("TRANSITIVE_API"))fail("INVALID_STATUS",id+" abstract operation needs server binding");
            }
            counts.compute(status,(key,count)->count+1);
        }
        return Collections.unmodifiableMap(counts);
    }
    private static boolean contains(JsonNode array,String value){for(JsonNode item:array)if(item.asText().equals(value))return true;return false;}
    public static void verifyImplementation(Path root,JsonNode binding,List<? extends JsonNode> scenarios)throws Exception{
        JsonNode target=binding.path("implementation");String id=binding.path("symbolId").asText();
        Path classFile=safePath(root,target.path("classFile").asText());Path sourceFile=safePath(root,binding.path("adapterFile").asText());
        if(!Files.isRegularFile(classFile)||!Files.isRegularFile(sourceFile))fail("MISSING_SYMBOL_BINDING",id);
        String classHash=LockVerifier.sha256(Files.readAllBytes(classFile));
        if(!classHash.equals(target.path("classSha256").asText())||!LockVerifier.sha256(Files.readAllBytes(sourceFile)).equals(target.path("sourceSha256").asText()))fail("STALE_EVIDENCE",id+" implementation changed");
        JsonNode actual=ApiInventory.readClass(Files.readAllBytes(classFile),"API_PUBLIC");
        if(!actual.path("owner").asText().equals(target.path("owner").asText()))fail("MISSING_SYMBOL_BINDING",id+" target owner");
        boolean found=false;
        for(JsonNode method:actual.path("methods"))if(method.path("name").equals(target.path("name"))&&method.path("descriptor").equals(target.path("descriptor"))&&(method.path("access").asInt()&Opcodes.ACC_ABSTRACT)==0)found=true;
        if(!found)fail("MISSING_SYMBOL_BINDING",id+" target method");
        if(scenarios.isEmpty())fail("MISSING_BEHAVIOR_EVIDENCE",id);
        for(JsonNode scenario:scenarios){
            if(!classHash.equals(scenario.path("subjectSha256").asText()))fail("STALE_EVIDENCE",id+" scenario subject");
            Path report=safePath(root,scenario.path("reportFile").asText());
            if(!Files.isRegularFile(report))fail("MISSING_BEHAVIOR_EVIDENCE",id+" report unavailable");
            if(!LockVerifier.sha256(Files.readAllBytes(report)).equals(scenario.path("reportSha256").asText()))fail("STALE_EVIDENCE",id+" report changed");
            javax.xml.parsers.DocumentBuilderFactory factory=javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);factory.setFeature("http://xml.org/sax/features/external-general-entities",false);factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD,"");factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
            var document=factory.newDocumentBuilder().parse(report.toFile());var cases=document.getElementsByTagName("testcase");boolean passed=false;
            for(int i=0;i<cases.getLength();i++){
                var test=(org.w3c.dom.Element)cases.item(i);
                if(test.getAttribute("classname").equals(scenario.path("testClass").asText())&&test.getAttribute("name").equals(scenario.path("testName").asText())){
                    if(test.getElementsByTagName("failure").getLength()!=0||test.getElementsByTagName("error").getLength()!=0||test.getElementsByTagName("skipped").getLength()!=0)fail("MISSING_BEHAVIOR_EVIDENCE",id+" testcase did not pass");
                    passed=true;
                }
            }
            if(!passed)fail("MISSING_BEHAVIOR_EVIDENCE",id+" testcase missing");
        }
    }
    private static Path safePath(Path root,String relative){
        Path base=root.toAbsolutePath().normalize();Path input=Path.of(relative);Path result=base.resolve(input).normalize();
        if(relative.isBlank()||input.isAbsolute()||!result.startsWith(base))fail("INVALID_EVIDENCE_PATH",relative);
        return result;
    }
    public static void verifyLinkage(Path root,JsonNode binding,List<Path> canonicalClasspath)throws Exception{
        JsonNode target=binding.path("implementation");String id=binding.path("symbolId").asText();int separator=id.indexOf('#'),last=id.lastIndexOf('#');
        if(separator<1||last<=separator||!id.substring(separator+1,last).equals(target.path("name").asText()+target.path("descriptor").asText()))fail("INVALID_SYMBOL_LINKAGE",id+" target signature differs");
        String apiName=id.substring(0,separator).replace('/','.');Path classFile=safePath(root,target.path("classFile").asText());
        Path classRoot=classFile;for(String part:(target.path("owner").asText()+".class").split("/"))classRoot=classRoot.getParent();
        List<java.net.URL> urls=new ArrayList<>();for(Path path:canonicalClasspath)urls.add(path.toUri().toURL());urls.add(classRoot.toUri().toURL());
        for(String directory:List.of("modules","adapters","versions")){
            Path parent=root.resolve(directory);if(!Files.isDirectory(parent))continue;
            try(var paths=Files.walk(parent)){
                for(Path path:paths.filter(Files::isDirectory).filter(p->p.endsWith(Path.of("build/classes/java/main"))).sorted().toList())urls.add(path.toUri().toURL());
            }
        }
        try(var loader=new java.net.URLClassLoader(urls.toArray(java.net.URL[]::new),ClassLoader.getPlatformClassLoader())){
            Class<?> api=Class.forName(apiName,false,loader),implementation=Class.forName(target.path("owner").asText().replace('/','.'),false,loader);
            if(!api.isAssignableFrom(implementation))fail("INVALID_SYMBOL_LINKAGE",id+" target is not assignable to API owner");
            boolean found=false;for(var method:implementation.getDeclaredMethods())if(method.getName().equals(target.path("name").asText())&&org.objectweb.asm.Type.getMethodDescriptor(method).equals(target.path("descriptor").asText())&&!java.lang.reflect.Modifier.isAbstract(method.getModifiers()))found=true;
            if(!found)fail("INVALID_SYMBOL_LINKAGE",id+" target descriptor is unavailable");
        }catch(ClassNotFoundException|LinkageError unavailable){throw new IllegalStateException("INVALID_SYMBOL_LINKAGE: "+id,unavailable);}
    }
    public static String behaviorFingerprint(Path root)throws java.io.IOException{
        TreeMap<String,String> hashes=new TreeMap<>();
        for(String directory:List.of("modules","adapters","versions","compat-packs","testkit/fixtures","testkit/integration")){
            Path parent=root.resolve(directory);if(!Files.isDirectory(parent))continue;
            Files.walkFileTree(parent,new SimpleFileVisitor<Path>(){
                @Override public FileVisitResult preVisitDirectory(Path directory,java.nio.file.attribute.BasicFileAttributes attributes){return Set.of("build",".gradle",".git").contains(directory.getFileName().toString())?FileVisitResult.SKIP_SUBTREE:FileVisitResult.CONTINUE;}
                @Override public FileVisitResult visitFile(Path file,java.nio.file.attribute.BasicFileAttributes attributes)throws java.io.IOException{
                    if(attributes.isRegularFile())hashes.put(root.relativize(file).toString().replace('\\','/'),LockVerifier.sha256(Files.readAllBytes(file)));return FileVisitResult.CONTINUE;
                }
            });
        }
        for(String file:List.of("settings.gradle.kts","build.gradle.kts","gradle/libs.versions.toml","contracts/locks/paper-1.20.1.lock.json","contracts/profiles/paper-1.20.1/api-index.json","contracts/profiles/paper-1.20.1/lifecycle.json")){
            Path path=root.resolve(file);if(Files.isRegularFile(path))hashes.put(file,LockVerifier.sha256(Files.readAllBytes(path)));
        }
        return LockVerifier.sha256(ApiInventory.JSON.writeValueAsBytes(hashes));
    }
    static Map<String,JsonNode> index(JsonNode symbols){
        if(!symbols.isArray())fail("INVALID_INVENTORY","symbols array required");
        Map<String,JsonNode> result=new TreeMap<>();for(JsonNode symbol:symbols){String id=symbol.path("id").asText();if(id.isBlank()||result.put(id,symbol)!=null)fail("INVALID_INVENTORY","blank/duplicate symbol");}return result;
    }
    private static Set<String> difference(Set<String> a,Set<String> b){Set<String> copy=new TreeSet<>(a);copy.removeAll(b);return copy;}
    static void fail(String code,String detail){throw new IllegalStateException(code+": "+detail);}
}
