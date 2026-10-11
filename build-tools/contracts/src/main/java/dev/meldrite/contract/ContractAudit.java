package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/** Locked, offline ABI and binding audit. Generation is separate from verification. */
public final class ContractAudit {
    private ContractAudit() {}
    public static List<ArtifactLock> canonicalArtifacts(ArtifactLock root){
        LinkedHashMap<String,ArtifactLock> selected=new LinkedHashMap<>();Deque<ArtifactLock> pending=new ArrayDeque<>();pending.add(root);
        while(!pending.isEmpty()){
            ArtifactLock lock=pending.removeFirst();String[] parts=lock.coordinate().split(":");
            String identity=parts[0]+":"+parts[1]+":"+(parts.length>3?parts[3]:"");
            if(selected.putIfAbsent(identity,lock)!=null)continue;
            pending.addAll(lock.dependencies());
        }
        return selected.values().stream().filter(a->a.cachePath().endsWith(".jar")).toList();
    }
    public static void main(String[] args)throws Exception{
        if(args.length<2||args.length>4||!Set.of("generate","verify").contains(args[0]))throw new IllegalArgumentException("Usage: ContractAudit <generate|verify> <project-root> [--profile <profile>]");
        Path root=Path.of(args[1]).toAbsolutePath().normalize();
        String profile = "paper-1.20.1";
        if (args.length == 4 && args[2].equals("--profile")) profile = args[3];
        if (!profile.matches("paper-[0-9]+\\.[0-9]+(?:\\.[0-9]+)?")) throw new IllegalArgumentException("Invalid profile: " + profile);
        ArtifactLockFile paper=ArtifactLockFile.read(root.resolve("contracts/locks/" + profile + ".lock.json"));
        for(ArtifactLock lock:paper.roots())LockVerifier.verify(lock,root);
        ArtifactLockFile tools=ArtifactLockFile.read(root.resolve("contracts/locks/tools.lock.json"));for(ArtifactLock lock:tools.roots())LockVerifier.verify(lock,root);
        if(paper.roots().size()!=1)throw new IOException("One API root is required");
        List<ArtifactLock> selected=canonicalArtifacts(paper.roots().get(0));
        verifyClassIdentity(selected,root);
        Path profileDirectory=root.resolve("contracts/profiles/" + profile);
        if(args[0].equals("generate"))generate(root,profileDirectory,profile,selected);else verify(root,profileDirectory,profile,selected);
    }
    private static void generate(Path root,Path profileDirectory,String profileName,List<ArtifactLock> selected)throws Exception{
        if(Files.exists(profileDirectory.resolve("bindings.json")))requireInitialBindings(read(profileDirectory.resolve("bindings.json")));
        Files.createDirectories(profileDirectory.resolve("inventory"));Files.createDirectories(profileDirectory.resolve("vendor-bindings"));
        ObjectNode manifest=ApiInventory.JSON.createObjectNode().put("schemaVersion",1).put("profile",profileName)
                .put("mediation","nearest definition; declared order breaks depth ties; artifact/classifier identity");
        ArrayNode artifacts=manifest.putArray("artifacts");List<Path> classpath=selected.stream().map(a->root.resolve(a.cachePath())).toList();
        int count=0;
        for(int i=0;i<selected.size();i++){
            ArtifactLock lock=selected.get(i);boolean api=i==0;
            JsonNode inventory=ApiInventory.extract(root.resolve(lock.cachePath()),lock.coordinate(),api?"API_PUBLIC":"TRANSITIVE_API",classpath);
            String shard="inventory/"+lock.sha256()+".json.gz";writeGzip(profileDirectory.resolve(shard),inventory);
            ObjectNode artifact=artifacts.addObject().put("coordinate",lock.coordinate()).put("resolvedVersion",lock.resolvedVersion()).put("sha256",lock.sha256())
                    .put("cachePath",lock.cachePath()).put("sourceUrl",lock.sourceUrl()).put("provenance",api?"API_PUBLIC":"TRANSITIVE_API")
                    .put("inventoryFile",shard).put("inventorySha256",LockVerifier.sha256(Files.readAllBytes(profileDirectory.resolve(shard))))
                    .put("classCount",inventory.path("classCount").asInt()).put("symbolCount",inventory.path("symbolCount").asInt());
            ObjectNode bindings=BindingVerifier.initialBindings(inventory);
            if(api){
                write(profileDirectory.resolve("bindings.json"),bindings);write(profileDirectory.resolve("domain-batches.json"),CapabilityWorklist.create(inventory,bindings));
                ObjectNode capabilities=ApiInventory.JSON.createObjectNode().put("schemaVersion",1);ObjectNode entries=capabilities.putObject("entries");
                for(JsonNode binding:bindings.path("entries")){
                    String name=binding.path("capability").asText();if(!entries.has(name))entries.putObject(name).put("status","UNSUPPORTED").put("reason","No delivered server behavior or regression evidence.").putArray("evidenceIds");
                }
                write(profileDirectory.resolve("capabilities.json"),capabilities);
            }else{
                String bindingFile="vendor-bindings/"+lock.sha256()+".json.gz";writeGzip(profileDirectory.resolve(bindingFile),bindings);
                artifact.put("bindingsFile",bindingFile).put("bindingsSha256",LockVerifier.sha256(Files.readAllBytes(profileDirectory.resolve(bindingFile))));
            }
            count+=inventory.path("symbolCount").asInt();System.out.println("Inventoried "+lock.coordinate()+" classes="+inventory.path("classCount")+" symbols="+inventory.path("symbolCount"));
        }
        manifest.put("symbolCount",count);manifest.putObject("serverImplementation").put("status","NOT_EXTRACTED")
                .put("prerequisite","C1/C3: independently locked server/Craft artifacts; never treat their symbols as public API.");
        write(profileDirectory.resolve("api-index.json"),manifest);
        if(!Files.exists(profileDirectory.resolve("evidence.json")))write(profileDirectory.resolve("evidence.json"),ApiInventory.JSON.createObjectNode().put("schemaVersion",1).set("scenarios",ApiInventory.JSON.createArrayNode()));
    }
    private static void verify(Path root,Path profile,String profileName,List<ArtifactLock> selected)throws Exception{
        JsonNode manifest=read(profile.resolve("api-index.json"));
        if(manifest.path("schemaVersion").asInt()!=1||!manifest.path("profile").asText().equals(profileName)||manifest.path("artifacts").size()!=selected.size())BindingVerifier.fail("INVALID_INVENTORY","profile/artifact set");
        JsonNode evidence=read(profile.resolve("evidence.json"));Map<String,Integer> total=new TreeMap<>();List<Path> classpath=selected.stream().map(a->root.resolve(a.cachePath())).toList();
        verifyLifecycle(read(profile.resolve("lifecycle.json")));
        String workspaceHash=BindingVerifier.behaviorFingerprint(root);
        Map<String,JsonNode> scenarioMap=new HashMap<>();evidence.path("scenarios").forEach(s->scenarioMap.put(s.path("id").asText(),s));
        int symbols=0,classes=0;
        for(int i=0;i<selected.size();i++){
            ArtifactLock lock=selected.get(i);JsonNode artifact=manifest.path("artifacts").get(i);boolean api=i==0;
            if(!artifact.path("coordinate").asText().equals(lock.coordinate())||!artifact.path("sha256").asText().equals(lock.sha256())||!artifact.path("cachePath").asText().equals(lock.cachePath())||!artifact.path("provenance").asText().equals(api?"API_PUBLIC":"TRANSITIVE_API"))BindingVerifier.fail("ARTIFACT_IDENTITY_CHANGED",lock.coordinate());
            Path shard=safeProfilePath(profile,artifact.path("inventoryFile").asText());checkHash(shard,artifact.path("inventorySha256").asText());
            JsonNode recorded=readGzip(shard),actual=ApiInventory.extract(root.resolve(lock.cachePath()),lock.coordinate(),api?"API_PUBLIC":"TRANSITIVE_API",classpath);
            if(!recorded.path("coordinate").equals(actual.path("coordinate"))||!recorded.path("sha256").equals(actual.path("sha256")))BindingVerifier.fail("ARTIFACT_IDENTITY_CHANGED",lock.coordinate());
            JsonNode bindings;
            if(api)bindings=read(profile.resolve("bindings.json"));else{
                Path path=safeProfilePath(profile,artifact.path("bindingsFile").asText());checkHash(path,artifact.path("bindingsSha256").asText());bindings=readGzip(path);
            }
            Map<String,Integer> counts=BindingVerifier.verify(recorded,actual,bindings,evidence);counts.forEach((state,count)->total.merge(state,count,Integer::sum));
            if(api){
                for(JsonNode binding:bindings.path("entries"))if(Set.of("SUPPORTED","PARTIAL").contains(binding.path("status").asText())){
                    List<JsonNode> scenarios=new ArrayList<>();binding.path("evidenceIds").forEach(id->scenarios.add(scenarioMap.get(id.asText())));BindingVerifier.verifyImplementation(root,binding,scenarios);BindingVerifier.verifyLinkage(root,binding,classpath);
                    for(JsonNode scenario:scenarios)if(!workspaceHash.equals(scenario.path("workspaceSha256").asText()))BindingVerifier.fail("STALE_EVIDENCE",binding.path("symbolId").asText()+" workspace changed");
                }
                verifyWorklist(actual,bindings,read(profile.resolve("domain-batches.json")));
                verifyCapabilities(bindings,read(profile.resolve("capabilities.json")));
            }
            symbols+=actual.path("symbolCount").asInt();classes+=actual.path("classCount").asInt();
            if(artifact.path("symbolCount").asInt()!=actual.path("symbolCount").asInt()||artifact.path("classCount").asInt()!=actual.path("classCount").asInt())BindingVerifier.fail("INVALID_INVENTORY","counts "+lock.coordinate());
        }
        if(symbols!=manifest.path("symbolCount").asInt())BindingVerifier.fail("INVALID_INVENTORY","total symbol count");
        Path report=root.resolve("build/reports/meldrite/verifyContracts/api-report.json");
        write(report,ApiInventory.JSON.createObjectNode().put("profile",profileName).put("artifacts",selected.size()).put("classes",classes).put("symbols",symbols)
                .put("missingBindings",0).put("unclassifiedSymbols",0).put("surfaceDifferences",0).put("serverImplementation","NOT_EXTRACTED: C1/C3 prerequisite").set("statuses",ApiInventory.JSON.valueToTree(total)));
        System.out.println("Offline locks and complete API inventory verified: "+selected.size()+" artifacts, "+classes+" classes, "+symbols+" symbols; "+total);
    }
    private static void verifyWorklist(JsonNode inventory,JsonNode bindings,JsonNode worklist){
        Set<String> required=new HashSet<>();inventory.path("symbols").forEach(s->{if(s.path("requiresImplementation").asBoolean())required.add(s.path("id").asText());});
        Set<String> actual=new HashSet<>();worklist.path("batches").fields().forEachRemaining(batch->batch.getValue().path("symbols").forEach(s->{String id=s.path("symbolId").asText();if(!actual.add(id)||s.path("scenarioId").asText().isBlank()||!s.path("adapterFile").equals(bindings.path("entries").path(id).path("adapterFile")))BindingVerifier.fail("INVALID_WORKLIST",id);}));
        if(!required.equals(actual))BindingVerifier.fail("INCOMPLETE_WORKLIST","expected="+required.size()+" actual="+actual.size());
    }
    public static void requireInitialBindings(JsonNode bindings){
        for(JsonNode binding:bindings.path("entries"))if(Set.of("SUPPORTED","PARTIAL").contains(binding.path("status").asText()))BindingVerifier.fail("IMPLEMENTED_BINDINGS_EXIST","Inventory changes require explicit reconciliation; support statuses must not be reset.");
    }
    public static void verifyLifecycle(JsonNode lifecycle){
        if(lifecycle.path("schemaVersion").asInt()!=1||!lifecycle.path("stages").isArray()||lifecycle.path("stages").isEmpty()||!lifecycle.path("constraints").isArray()||lifecycle.path("baselineStatus").asText().isBlank())BindingVerifier.fail("INVALID_LIFECYCLE","schema/stages/constraints/baseline status");
        Map<String,Set<String>> graph=new LinkedHashMap<>();
        for(JsonNode stage:lifecycle.path("stages"))if(stage.asText().isBlank()||graph.put(stage.asText(),new HashSet<>())!=null)BindingVerifier.fail("INVALID_LIFECYCLE","duplicate/empty stage");
        for(JsonNode edge:lifecycle.path("constraints")){
            if(!edge.isArray()||edge.size()!=2||!graph.containsKey(edge.get(0).asText())||!graph.containsKey(edge.get(1).asText()))BindingVerifier.fail("INVALID_LIFECYCLE","unknown stage");
            graph.get(edge.get(0).asText()).add(edge.get(1).asText());
        }
        ArchitectureVerifier.verifyDependencyGraph(graph,graph);
    }
    public static void verifyCapabilities(JsonNode bindings,JsonNode capabilities){
        Map<String,List<JsonNode>> grouped=new HashMap<>();bindings.path("entries").forEach(b->grouped.computeIfAbsent(b.path("capability").asText(),ignored->new ArrayList<>()).add(b));
        Set<String> listed=new HashSet<>();capabilities.path("entries").fieldNames().forEachRemaining(listed::add);
        if(capabilities.path("schemaVersion").asInt()!=1||!listed.equals(grouped.keySet()))BindingVerifier.fail("INVALID_CAPABILITIES","missing/unknown domain");
        for(var group:grouped.entrySet()){
            JsonNode capability=capabilities.path("entries").path(group.getKey());String status=capability.path("status").asText();
            if(!Set.of("SUPPORTED","PARTIAL","UNSUPPORTED","NOT_APPLICABLE").contains(status))BindingVerifier.fail("INVALID_STATUS",group.getKey());
            if(!capability.path("evidenceIds").isArray())BindingVerifier.fail("INVALID_CAPABILITIES",group.getKey());
            if(Set.of("SUPPORTED","PARTIAL").contains(status)&&capability.path("evidenceIds").isEmpty())BindingVerifier.fail("MISSING_EVIDENCE",group.getKey());
            if(Set.of("SUPPORTED","PARTIAL").contains(status)){
                Set<String> boundEvidence=new HashSet<>();group.getValue().forEach(b->b.path("evidenceIds").forEach(id->boundEvidence.add(id.asText())));
                for(JsonNode id:capability.path("evidenceIds"))if(!boundEvidence.contains(id.asText()))BindingVerifier.fail("MISSING_EVIDENCE",group.getKey()+" -> "+id.asText());
            }
            if(status.equals("SUPPORTED")&&group.getValue().stream().anyMatch(b->!Set.of("SUPPORTED","NOT_APPLICABLE").contains(b.path("status").asText())))BindingVerifier.fail("FALSE_CAPABILITY_SUPPORT",group.getKey());
            if(Set.of("SUPPORTED","PARTIAL").contains(status)&&group.getValue().stream().noneMatch(b->Set.of("SUPPORTED","PARTIAL").contains(b.path("status").asText())))BindingVerifier.fail("FALSE_CAPABILITY_SUPPORT",group.getKey());
            if(status.equals("NOT_APPLICABLE")&&(capability.path("reason").asText().isBlank()||group.getValue().stream().anyMatch(b->!b.path("status").asText().equals("NOT_APPLICABLE"))))BindingVerifier.fail("INVALID_STATUS",group.getKey());
            if(status.equals("PARTIAL")&&(!capability.path("limits").isArray()||capability.path("limits").isEmpty()))BindingVerifier.fail("INVALID_STATUS",group.getKey());
            if(status.equals("UNSUPPORTED")&&capability.path("reason").asText().isBlank())BindingVerifier.fail("INVALID_STATUS",group.getKey());
        }
    }
    private static void verifyClassIdentity(List<ArtifactLock> artifacts,Path root)throws IOException{
        Map<String,String> providers=new HashMap<>();
        for(ArtifactLock artifact:artifacts)try(JarFile jar=new JarFile(root.resolve(artifact.cachePath()).toFile(),false,java.util.zip.ZipFile.OPEN_READ,Runtime.Version.parse("17"))){
            for(var entry:jar.versionedStream().filter(e->e.getName().endsWith(".class")&&!e.getName().startsWith("META-INF/")&&!e.getName().equals("module-info.class")).toList()){
                String prior=providers.putIfAbsent(entry.getName(),artifact.coordinate());if(prior!=null)throw new IOException("DUPLICATE_API_CLASS_IDENTITY: "+entry.getName()+" in "+prior+" and "+artifact.coordinate());
            }
        }
    }
    private static Path safeProfilePath(Path profile,String relative){Path input=Path.of(relative),target=profile.resolve(input).normalize();if(relative.isBlank()||input.isAbsolute()||!target.startsWith(profile))BindingVerifier.fail("INVALID_INVENTORY_PATH",relative);return target;}
    private static void checkHash(Path path,String hash)throws IOException{if(!Files.isRegularFile(path)||!LockVerifier.sha256(Files.readAllBytes(path)).equals(hash))BindingVerifier.fail("INVENTORY_HASH_MISMATCH",path.toString());}
    private static JsonNode read(Path path)throws IOException{return ApiInventory.JSON.readTree(path.toFile());}
    private static JsonNode readGzip(Path path)throws IOException{try(var input=new GZIPInputStream(Files.newInputStream(path))){return ApiInventory.JSON.readTree(input);}}
    private static void writeGzip(Path path,JsonNode document)throws IOException{try(var output=new GZIPOutputStream(Files.newOutputStream(path))){ApiInventory.JSON.writeValue(output,document);}}
    private static void write(Path path,JsonNode document)throws IOException{Files.createDirectories(path.getParent());ApiInventory.JSON.writerWithDefaultPrettyPrinter().writeValue(path.toFile(),document);}
}
