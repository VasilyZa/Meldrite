package dev.meldrite.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import org.objectweb.asm.*;

/** Classfile-derived ABI. Never loads upstream classes or infers behavior from method presence. */
public final class ApiInventory {
    static final ObjectMapper JSON = new ObjectMapper();
    private ApiInventory() {}
    public static JsonNode extract(Path jar, String coordinate, String provenance) throws IOException {
        return extract(jar,coordinate,provenance,List.of());
    }
    public static JsonNode extract(Path jar,String coordinate,String provenance,List<Path> dependencyClasspath) throws IOException {
        if (!Set.of("API_PUBLIC", "API_INTERNAL_SUPPORT", "TRANSITIVE_API", "SERVER_IMPLEMENTATION").contains(provenance))
            throw new IllegalArgumentException("Unknown provenance: " + provenance);
        ObjectNode document=JSON.createObjectNode().put("schemaVersion",1).put("coordinate",coordinate)
                .put("sha256",LockVerifier.sha256(java.nio.file.Files.readAllBytes(jar))).put("provenance",provenance);
        TreeMap<String,ObjectNode> classes=new TreeMap<>();
        ArrayNode resources=document.putArray("resources");
        try(JarFile archive=new JarFile(jar.toFile(),false,java.util.zip.ZipFile.OPEN_READ,Runtime.Version.parse("17"))){
            for(var entry:archive.stream().filter(e->!e.isDirectory()).sorted(Comparator.comparing(java.util.jar.JarEntry::getName)).toList()){
                byte[] bytes; try(var input=archive.getInputStream(entry)){bytes=input.readAllBytes();}
                if(entry.getName().startsWith("META-INF/versions/")){
                    resources.addObject().put("path",entry.getName()).put("sha256",LockVerifier.sha256(bytes)).put("role","multi-release-alternative");
                    continue;
                }
                if(!entry.getName().endsWith(".class")){
                    resources.addObject().put("path",entry.getName()).put("sha256",LockVerifier.sha256(bytes));continue;
                }
            }
            for(var entry:archive.versionedStream().filter(e->!e.isDirectory()&&e.getName().endsWith(".class")).sorted(Comparator.comparing(java.util.jar.JarEntry::getName)).toList()){
                byte[] bytes;try(var input=archive.getInputStream(entry)){bytes=input.readAllBytes();}
                ObjectNode model=readClass(bytes,provenance);model.put("selectedEntry",entry.getRealName());
                if(classes.put(model.path("owner").asText(),model)!=null)throw new IOException("Duplicate class identity in "+coordinate);
            }
        }
        List<ObjectNode> declared=List.copyOf(classes.values());
        ArrayNode classList=document.putArray("classes");declared.forEach(classList::add);
        ArrayNode unresolved=document.putArray("unresolvedHierarchy");
        resolveParents(classes,dependencyClasspath,provenance.equals("TRANSITIVE_API"),unresolved);
        TreeMap<String,ObjectNode> symbols=new TreeMap<>();
        for(ObjectNode model:declared){
            if(!model.path("exposed").asBoolean())continue;
            add(symbols,symbol(model,"CLASS",model.path("owner").asText(),"",""));
            for(JsonNode field:model.path("fields"))add(symbols,symbol((ObjectNode)field,"FIELD",model.path("owner").asText(),field.path("name").asText(),field.path("descriptor").asText()));
            for(JsonNode method:model.path("methods")){
                ObjectNode declaredMethod=symbol((ObjectNode)method,"METHOD",model.path("owner").asText(),method.path("name").asText(),method.path("descriptor").asText());
                if(model.path("classKind").asText().equals("ANNOTATION"))declaredMethod.put("requiresImplementation",false).put("providerRole","JVM annotation member");
                add(symbols,declaredMethod);
            }
            LinkedHashMap<String,ObjectNode> effective=effectiveMethods(model.path("owner").asText(),classes,new HashSet<>());
            for(ObjectNode method:effective.values()){
                String owner=model.path("owner").asText();
                if(owner.equals(method.path("declaringOwner").asText()))continue;
                ObjectNode inherited=symbol(method,"INHERITED_METHOD",owner,method.path("name").asText(),method.path("descriptor").asText());
                inherited.put("declarationProvenance",method.path("provenance").asText()).put("provenance",model.path("provenance").asText());
                if(model.path("classKind").asText().equals("ANNOTATION"))inherited.put("requiresImplementation",false).put("providerRole","JVM annotation member");
                add(symbols,inherited);
            }
        }
        ArrayNode symbolList=document.putArray("symbols");symbols.values().forEach(symbolList::add);
        document.put("classCount",declared.size()).put("symbolCount",symbols.size());
        return document;
    }
    private static void resolveParents(Map<String,ObjectNode> classes,List<Path> paths,boolean conditional,ArrayNode unresolved)throws IOException{
        List<JarFile> archives=new ArrayList<>();
        try{
            for(Path path:paths)archives.add(new JarFile(path.toFile(),false,java.util.zip.ZipFile.OPEN_READ,Runtime.Version.parse("17")));
            Deque<ObjectNode> pending=new ArrayDeque<>(classes.values());
            while(!pending.isEmpty()){
                ObjectNode model=pending.removeFirst();List<String> parents=new ArrayList<>();if((model.path("access").asInt()&Opcodes.ACC_INTERFACE)==0)parents.add(model.path("superName").asText());model.path("interfaces").forEach(p->parents.add(p.asText()));
                for(String parent:parents){
                    if(parent.isBlank()||classes.containsKey(parent))continue;
                    ObjectNode found=null;
                    try(var platform=ClassLoader.getPlatformClassLoader().getResourceAsStream(parent+".class")){
                        if(platform!=null)found=readClass(platform.readAllBytes(),"TRANSITIVE_API");
                    }
                    for(JarFile archive:archives){if(found!=null)break;var entry=archive.getJarEntry(parent+".class");if(entry==null)continue;
                        try(var input=archive.getInputStream(entry)){found=readClass(input.readAllBytes(),"TRANSITIVE_API");}break;
                    }
                    if(found==null){
                        if(!conditional)throw new IOException("MISSING_INHERITANCE_PROVIDER: "+parent);
                        unresolved.addObject().put("owner",model.path("owner").asText()).put("parent",parent)
                                .put("reason","Optional/provider-specific parent is absent from the locked public classpath; no inherited members are invented.")
                                .put("failureContract","CLASSPATH_UNAVAILABLE");continue;
                    }
                    classes.put(parent,found);pending.addLast(found);
                }
            }
        }finally{
            IOException failure=null;for(JarFile archive:archives)try{archive.close();}catch(IOException error){if(failure==null)failure=error;else failure.addSuppressed(error);}if(failure!=null)throw failure;
        }
    }
    private static void add(Map<String,ObjectNode> symbols,ObjectNode symbol){
        if(symbols.put(symbol.path("id").asText(),symbol)!=null)throw new IllegalStateException("Duplicate symbol: "+symbol.path("id"));
    }
    private static ObjectNode symbol(ObjectNode metadata,String kind,String owner,String name,String descriptor){
        ObjectNode result=metadata.deepCopy(); result.remove(List.of("fields","methods"));
        result.put("id",owner+"#"+name+descriptor+"#"+kind).put("owner",owner).put("name",name).put("descriptor",descriptor).put("kind",kind);
        boolean requires=kind.endsWith("METHOD")&&(metadata.path("access").asInt()&Opcodes.ACC_ABSTRACT)!=0;
        result.put("requiresImplementation",requires);return result;
    }
    private static LinkedHashMap<String,ObjectNode> effectiveMethods(String owner,Map<String,ObjectNode> classes,Set<String> active){
        LinkedHashMap<String,ObjectNode> methods=new LinkedHashMap<>();ObjectNode model=classes.get(owner);
        if(model==null)return methods;
        if(!active.add(owner))throw new IllegalStateException("INHERITANCE_CYCLE: "+owner);
        for(JsonNode parent:model.path("interfaces"))effectiveMethods(parent.asText(),classes,active).forEach((id,method)->{
            ObjectNode old=methods.get(id);
            if(old==null||((old.path("access").asInt()&Opcodes.ACC_ABSTRACT)!=0&&(method.path("access").asInt()&Opcodes.ACC_ABSTRACT)==0))methods.put(id,method);
        });
        if((model.path("access").asInt()&Opcodes.ACC_INTERFACE)==0)methods.putAll(effectiveMethods(model.path("superName").asText(),classes,active));
        for(JsonNode member:model.path("methods")){
            if(member.path("name").asText().startsWith("<")||(member.path("access").asInt()&Opcodes.ACC_STATIC)!=0)continue;
            methods.put(member.path("name").asText()+member.path("descriptor").asText(),(ObjectNode)member);
        }
        active.remove(owner);return methods;
    }
    static ObjectNode readClass(byte[] bytes,String provenance) throws IOException {
        ObjectNode model=JSON.createObjectNode().put("bytecodeSha256",LockVerifier.sha256(bytes)).put("provenance",provenance);
        ArrayNode fields=model.putArray("fields"),methods=model.putArray("methods"),annotations=model.putArray("annotations");
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9){
            String owner;
            @Override public void visit(int version,int access,String name,String signature,String superName,String[] interfaces){
                owner=name;model.put("owner",name).put("access",access).put("classVersion",version).put("signature",Objects.toString(signature,""))
                    .put("superName",Objects.toString(superName,"")).put("exposed",visible(access)).put("enum",(access&Opcodes.ACC_ENUM)!=0)
                    .put("classKind",(access&Opcodes.ACC_ENUM)!=0?"ENUM":(access&Opcodes.ACC_ANNOTATION)!=0?"ANNOTATION":(access&Opcodes.ACC_INTERFACE)!=0?"INTERFACE":"CLASS");
                ArrayNode parents=model.putArray("interfaces");if(interfaces!=null)for(String parent:interfaces)parents.add(parent);
            }
            @Override public void visitInnerClass(String name,String outerName,String innerName,int access){
                if(name.equals(owner))model.put("access",access).put("exposed",visible(access)).put("outerName",Objects.toString(outerName,"")).put("innerName",Objects.toString(innerName,""));
            }
            @Override public AnnotationVisitor visitAnnotation(String descriptor,boolean visible){
                if(descriptor.contains("ApiStatus$Internal")&&provenance.equals("API_PUBLIC"))model.put("provenance","API_INTERNAL_SUPPORT");
                return annotation(annotations,descriptor,visible);
            }
            @Override public AnnotationVisitor visitTypeAnnotation(int ref,TypePath path,String desc,boolean visible){return typeAnnotation(annotations,ref,path,desc,visible);}
            @Override public void visitPermittedSubclass(String name){if(!model.has("permittedSubclasses"))model.putArray("permittedSubclasses");((ArrayNode)model.get("permittedSubclasses")).add(name);}
            @Override public RecordComponentVisitor visitRecordComponent(String name,String descriptor,String signature){
                if(!model.has("recordComponents"))model.putArray("recordComponents");
                ObjectNode component=((ArrayNode)model.get("recordComponents")).addObject().put("name",name).put("descriptor",descriptor).put("signature",Objects.toString(signature,""));
                return new RecordComponentVisitor(Opcodes.ASM9){@Override public AnnotationVisitor visitAnnotation(String desc,boolean visible){return annotation(array(component,"annotations"),desc,visible);}};
            }
            @Override public FieldVisitor visitField(int access,String name,String descriptor,String signature,Object value){
                if(!visible(access))return null;
                ObjectNode field=member(access,name,descriptor,signature,owner,provenance);field.put("enumConstant",(access&Opcodes.ACC_ENUM)!=0);if(value!=null)field.set("constant",value(value));fields.add(field);
                return new FieldVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String desc,boolean visible){return memberAnnotation(field,desc,visible);}
                    @Override public AnnotationVisitor visitTypeAnnotation(int ref,TypePath path,String desc,boolean visible){return typeAnnotation(array(field,"annotations"),ref,path,desc,visible);}
                };
            }
            @Override public MethodVisitor visitMethod(int access,String name,String descriptor,String signature,String[] exceptions){
                if(!visible(access))return null;
                ObjectNode method=member(access,name,descriptor,signature,owner,provenance);ArrayNode throwsTypes=method.putArray("exceptions");if(exceptions!=null)for(String exception:exceptions)throwsTypes.add(exception);methods.add(method);
                return new MethodVisitor(Opcodes.ASM9){
                    @Override public AnnotationVisitor visitAnnotation(String desc,boolean visible){return memberAnnotation(method,desc,visible);}
                    @Override public AnnotationVisitor visitTypeAnnotation(int ref,TypePath path,String desc,boolean visible){return typeAnnotation(array(method,"annotations"),ref,path,desc,visible);}
                    @Override public AnnotationVisitor visitParameterAnnotation(int parameter,String desc,boolean visible){
                        ObjectNode parameterNode=JSON.createObjectNode().put("parameter",parameter);array(method,"parameterAnnotations").add(parameterNode);return annotation(array(parameterNode,"annotations"),desc,visible);
                    }
                    @Override public AnnotationVisitor visitAnnotationDefault(){ObjectNode defaults=method.putObject("annotationDefault");return values(defaults);}
                    @Override public void visitParameter(String name,int access){array(method,"parameters").addObject().put("name",Objects.toString(name,"")).put("access",access);}
                };
            }
        },ClassReader.SKIP_CODE|ClassReader.SKIP_FRAMES);
        String effectiveProvenance=model.path("provenance").asText();
        if(effectiveProvenance.equals("API_INTERNAL_SUPPORT")){ fields.forEach(f->((ObjectNode)f).put("provenance",effectiveProvenance));methods.forEach(m->((ObjectNode)m).put("provenance",effectiveProvenance)); }
        return model;
    }
    private static boolean visible(int access){return(access&(Opcodes.ACC_PUBLIC|Opcodes.ACC_PROTECTED))!=0;}
    private static ObjectNode member(int access,String name,String descriptor,String signature,String owner,String provenance){return JSON.createObjectNode().put("name",name).put("descriptor",descriptor).put("access",access).put("signature",Objects.toString(signature,"")).put("declaringOwner",owner).put("provenance",provenance);}
    private static ArrayNode array(ObjectNode node,String name){return node.has(name)?(ArrayNode)node.get(name):node.putArray(name);}
    private static AnnotationVisitor memberAnnotation(ObjectNode member,String desc,boolean visible){if(desc.contains("ApiStatus$Internal")&&member.path("provenance").asText().equals("API_PUBLIC"))member.put("provenance","API_INTERNAL_SUPPORT");return annotation(array(member,"annotations"),desc,visible);}
    private static AnnotationVisitor typeAnnotation(ArrayNode list,int ref,TypePath path,String desc,boolean visible){ObjectNode meta=JSON.createObjectNode().put("typeRef",ref).put("typePath",Objects.toString(path,""));list.add(meta);return annotation(array(meta,"annotations"),desc,visible);}
    private static AnnotationVisitor annotation(ArrayNode list,String desc,boolean visible){ObjectNode node=list.addObject().put("descriptor",desc).put("visible",visible);return values(node.putObject("values"));}
    private static JsonNode value(Object value){
        if(value instanceof Type type)return JSON.createObjectNode().put("classDescriptor",type.getDescriptor());
        if(value instanceof Number||value instanceof Character)return JSON.createObjectNode().put("valueType",value.getClass().getName()).put("value",value.toString());
        if(value!=null&&value.getClass().isArray()){
            ArrayNode values=JSON.createArrayNode();for(int i=0;i<java.lang.reflect.Array.getLength(value);i++)values.add(value(java.lang.reflect.Array.get(value,i)));
            return JSON.createObjectNode().put("valueType",value.getClass().getName()).set("values",values);
        }
        return JSON.valueToTree(value);
    }
    private static AnnotationVisitor values(ObjectNode target){return new AnnotationVisitor(Opcodes.ASM9){
        private String key(String name){return Objects.toString(name,"value");}
        @Override public void visit(String name,Object value){target.set(key(name),value(value));}
        @Override public void visitEnum(String name,String desc,String value){target.putObject(key(name)).put("enumDescriptor",desc).put("value",value);}
        @Override public AnnotationVisitor visitAnnotation(String name,String desc){ObjectNode child=target.putObject(key(name)).put("descriptor",desc);return values(child.putObject("values"));}
        @Override public AnnotationVisitor visitArray(String name){ArrayNode items=target.putArray(key(name));return new AnnotationVisitor(Opcodes.ASM9){
            @Override public void visit(String unused,Object value){items.add(value(value));}
            @Override public void visitEnum(String unused,String desc,String value){items.addObject().put("enumDescriptor",desc).put("value",value);}
            @Override public AnnotationVisitor visitAnnotation(String unused,String desc){return values(items.addObject().put("descriptor",desc).putObject("values"));}
            @Override public AnnotationVisitor visitArray(String unused){ObjectNode nested=items.addObject();return values(nested);}
        };}
    };}
}
