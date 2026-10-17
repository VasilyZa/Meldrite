import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipFile;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.mappingio.tree.MappingTreeView.ClassMappingView;
import net.fabricmc.mappingio.tree.MappingTreeView.MemberMappingView;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Composes official upstream mappings and verifies every emitted member against the native jar. */
public final class GenerateLegacyMappings {
    private record Symbol(boolean method, String owner, String name, String descriptor) implements Comparable<Symbol> {
        @Override public int compareTo(Symbol other) {
            int order = Boolean.compare(method, other.method);
            if (order == 0) order = owner.compareTo(other.owner);
            if (order == 0) order = name.compareTo(other.name);
            if (order == 0) order = descriptor.compareTo(other.descriptor);
            return order;
        }
        @Override public String toString() { return owner + "." + name + descriptor; }
    }
    private record NativeClass(List<String> parents, Map<Symbol, Integer> members) {}
    private record Target(String owner, String name, int access) {}
    private record Member(Symbol source, Target target) {}

    public static void main(String[] args) throws Exception {
        if (args.length != 6) throw new IllegalArgumentException(
                "official-spigot.tiny, intermediary.jar, server_mappings.txt, native-intermediary.jar, official-server.jar, output directory required");
        Path spigotPath = Path.of(args[0]);
        Path intermediaryPath = Path.of(args[1]);
        Path mojangPath = Path.of(args[2]);
        Path nativePath = Path.of(args[3]);
        Path serverPath = Path.of(args[4]);
        Path output = Path.of(args[5]);
        var spigot = new MemoryMappingTree();
        var intermediary = new MemoryMappingTree();
        var mojang = new MemoryMappingTree();
        MappingReader.read(spigotPath, spigot);
        try (var zip = new ZipFile(intermediaryPath.toFile());
             var reader = new InputStreamReader(zip.getInputStream(zip.getEntry("mappings/mappings.tiny")), StandardCharsets.UTF_8)) {
            MappingReader.read(reader, intermediary);
        }
        MappingReader.read(mojangPath, mojang);
        if (!"official".equals(spigot.getSrcNamespace()) || !"official".equals(intermediary.getSrcNamespace())) {
            throw new IllegalArgumentException("Both Tiny inputs must originate in the same official namespace");
        }
        int targetNamespace = intermediary.getNamespaceId("intermediary");
        int spigotNamespace = spigot.getNamespaceId("spigot");
        Map<String, NativeClass> natives = readNativeJar(nativePath);
        Map<String, NativeClass> officialServer = readNativeJar(serverPath);
        var serverClasses = new HashSet<String>();
        for (String name : officialServer.keySet()) serverClasses.add(intermediary.mapClassName(name, targetNamespace));
        natives.keySet().retainAll(serverClasses);
        Map<String, ClassMappingView> byNativeOwner = new HashMap<>();
        Map<String, String> classes = new TreeMap<>();
        List<String> rejected = new ArrayList<>();
        for (ClassMappingView source : intermediary.getClasses()) {
            String target = source.getName(targetNamespace);
            if (!natives.containsKey(target)) continue;
            byNativeOwner.put(target, source);
            ClassMappingView spigotClass = spigot.getClass(source.getSrcName());
            if (spigotClass != null) addClass(classes, spigotClass.getName(spigotNamespace), target, rejected);
        }
        Map<String, ClassMappingView> mojangByOfficial = new HashMap<>();
        for (ClassMappingView source : mojang.getClasses()) {
            String official = source.getDstName(0);
            mojangByOfficial.put(official, source);
            ClassMappingView fabric = intermediary.getClass(official);
            if (fabric != null && natives.containsKey(fabric.getName(targetNamespace))) {
                addClass(classes, source.getSrcName(), fabric.getName(targetNamespace), rejected);
            }
        }
        Map<String, String> reflectionClasses = new TreeMap<>();
        var reflectionMembers = new TreeMap<Symbol, String>();
        List<Member> raw = new ArrayList<>();
        List<Member> aliases = new ArrayList<>();
        int validDeclarations = 0;
        for (ClassMappingView source : spigot.getClasses()) {
            ClassMappingView fabric = intermediary.getClass(source.getSrcName());
            if (fabric == null || !natives.containsKey(fabric.getName(targetNamespace))) continue;
            String owner = fabric.getName(targetNamespace);
            for (boolean method : List.of(false, true)) {
                var sourceMembers = method ? source.getMethods() : source.getFields();
                for (MemberMappingView member : sourceMembers) {
                    if (!exists(officialServer, new Symbol(method, source.getSrcName(), member.getSrcName(), member.getSrcDesc()))) {
                        rejected.add("missing-server\t" + source.getName(spigotNamespace) + "." + member.getSrcName() + member.getSrcDesc());
                        continue;
                    }
                    String descriptor = intermediary.mapDesc(member.getSrcDesc(), targetNamespace);
                    Target target = targetMember(byNativeOwner, natives, owner,
                            member.getSrcName(), member.getSrcDesc(), method, targetNamespace);
                    Symbol mapped = new Symbol(method, owner, target.name, descriptor);
                    if (!exists(natives, mapped)) {
                        rejected.add("missing-native\t" + source.getName(spigotNamespace) + "." + member.getSrcName()
                                + member.getSrcDesc() + "\t" + mapped);
                        continue;
                    }
                    target = withAccess(natives, target, method, descriptor);
                    validDeclarations++;
                    raw.add(new Member(new Symbol(method, owner, member.getSrcName(), descriptor), target));
                    String developmentName = member.getName(spigotNamespace);
                    if (developmentName != null) aliases.add(new Member(new Symbol(method, owner, developmentName, descriptor), target));
                }
            }
        }
        // Mojang names are an additional readable input ABI, composed through official names.
        for (var entry : mojangByOfficial.entrySet()) {
            ClassMappingView fabric = intermediary.getClass(entry.getKey());
            if (fabric == null || !natives.containsKey(fabric.getName(targetNamespace))) continue;
            String owner = fabric.getName(targetNamespace);
            ClassMappingView source = entry.getValue();
            reflectionClasses.put(owner, source.getSrcName());
            for (boolean method : List.of(false, true)) {
                var sourceMembers = method ? source.getMethods() : source.getFields();
                for (MemberMappingView member : sourceMembers) {
                    String officialDescriptor = mojang.mapDesc(member.getSrcDesc(), 0);
                    if (!exists(officialServer, new Symbol(method, entry.getKey(), member.getDstName(0), officialDescriptor))) continue;
                    String descriptor = intermediary.mapDesc(officialDescriptor, targetNamespace);
                    Target target = targetMember(byNativeOwner, natives, owner,
                            member.getDstName(0), officialDescriptor, method, targetNamespace);
                    if (!exists(natives, new Symbol(method, owner, target.name, descriptor))) continue;
                    target = withAccess(natives, target, method, descriptor);
                    aliases.add(new Member(new Symbol(method, owner, member.getSrcName(), descriptor), target));
                    Symbol runtimeSymbol = new Symbol(method, owner, target.name, descriptor);
                    String previous = reflectionMembers.putIfAbsent(runtimeSymbol, member.getSrcName());
                    if (previous != null && !previous.equals(member.getSrcName()))
                        throw new IllegalStateException("Ambiguous Mojang reflection member: " + runtimeSymbol);
                }
            }
        }
        Map<Symbol, Target> members = new TreeMap<>();
        raw.stream().sorted((left, right) -> left.source.compareTo(right.source)).forEach(member -> putMember(members, member, rejected));
        aliases.stream().sorted((left, right) -> left.source.compareTo(right.source)).forEach(member -> putMember(members, member, rejected));
        for (Member member : raw) {
            Symbol source = member.source;
            members.putIfAbsent(new Symbol(source.method, source.owner, member.target.name, source.descriptor), member.target);
        }
        for (var entry : members.entrySet()) {
            Symbol key = entry.getKey();
            if (!exists(natives, new Symbol(key.method, key.owner, entry.getValue().name, key.descriptor))) {
                throw new IllegalStateException("Unverified member emitted: " + entry);
            }
        }
        Files.createDirectories(output);
        Path binary = output.resolve("legacy-names.bin.gz");
        try (var stream = new DataOutputStream(new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(binary))))) {
            stream.writeInt(0x4D4C4D33);
            stream.writeUTF("1.20.1");
            stream.writeInt(classes.size());
            for (var entry : classes.entrySet()) {
                stream.writeUTF(entry.getKey());
                stream.writeUTF(entry.getValue());
            }
            stream.writeInt(natives.size());
            for (var entry : new TreeMap<>(natives).entrySet()) {
                stream.writeUTF(entry.getKey());
                stream.writeInt(entry.getValue().parents.size());
                for (String parent : entry.getValue().parents) stream.writeUTF(parent);
            }
            stream.writeInt(members.size());
            for (var entry : members.entrySet()) {
                Symbol key = entry.getKey();
                stream.writeBoolean(key.method);
                stream.writeUTF(key.owner);
                stream.writeUTF(key.name);
                stream.writeUTF(key.descriptor);
                stream.writeUTF(entry.getValue().owner);
                stream.writeUTF(entry.getValue().name);
                stream.writeInt(entry.getValue().access);
            }
        }
        Path reflection = output.resolve("reflection-names.tiny.gz");
        try (var stream = new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(reflection)))) {
            StringBuilder tiny = new StringBuilder("tiny\t2\t0\tintermediary\tmojang\n");
            for (var type : reflectionClasses.entrySet()) {
                tiny.append("c\t").append(type.getKey()).append('\t').append(type.getValue()).append('\n');
                for (var member : reflectionMembers.subMap(
                        new Symbol(false, type.getKey(), "", ""), true,
                        new Symbol(false, type.getKey() + "\u0000", "", ""), false).entrySet()) {
                    Symbol key = member.getKey();
                    tiny.append("\tf\t").append(key.descriptor).append('\t').append(key.name).append('\t').append(member.getValue()).append('\n');
                }
                for (var member : reflectionMembers.subMap(
                        new Symbol(true, type.getKey(), "", ""), true,
                        new Symbol(true, type.getKey() + "\u0000", "", ""), false).entrySet()) {
                    Symbol key = member.getKey();
                    tiny.append("\tm\t").append(key.descriptor).append('\t').append(key.name).append('\t').append(member.getValue()).append('\n');
                }
            }
            stream.write(tiny.toString().getBytes(StandardCharsets.UTF_8));
        }
        rejected.sort(String::compareTo);
        Files.write(output.resolve("rejected-members.tsv"), rejected, StandardCharsets.UTF_8);
        long fields = members.keySet().stream().filter(key -> !key.method).count();
        String provenance = """
                version=1.20.1
                format=MLM3-gzip
                generator=GenerateLegacyMappings.java
                spigot.source=https://hub.spigotmc.org/stash/projects/SPIGOT/repos/builddata/browse?at=faff587dcbe915bc565bf01f2d54c6af86039414
                spigot.buildDataCommit=faff587dcbe915bc565bf01f2d54c6af86039414
                spigot.namespace=official-to-spigot-development
                fabric.source=https://maven.fabricmc.net/net/fabricmc/intermediary/1.20.1/intermediary-1.20.1-v2.jar
                mojang.source=https://piston-data.mojang.com/v1/objects/0b4dba049482496c507b2387a73a913230ebbd76/server.txt
                inputs=Spigot-class/raw-official-member,Spigot-development-member,Mojang-class/member,intermediary
                conflicts=raw-official-member-takes-precedence;see-rejected-members.tsv
                verification=all-emitted-members-exist-in-official-server-and-native-intermediary-with-descriptor-hierarchy-and-target-access
                """
                + "spigot.sha256=" + sha256(spigotPath) + "\n"
                + "fabric.sha256=" + sha256(intermediaryPath) + "\n"
                + "mojang.sha256=" + sha256(mojangPath) + "\n"
                + "native.sha256=" + sha256(nativePath) + "\n"
                + "officialServer.sha256=" + sha256(serverPath) + "\n"
                + "output.sha256=" + sha256(binary) + "\n"
                + "reflection.sha256=" + sha256(reflection) + "\n"
                + "reflectionMembers=" + reflectionMembers.size() + "\n"
                + "classes=" + classes.size() + "\n"
                + "nativeClasses=" + natives.size() + "\n"
                + "verifiedDeclarations=" + validDeclarations + "\n"
                + "fieldAliases=" + fields + "\n"
                + "methodAliases=" + (members.size() - fields) + "\n"
                + "rejected=" + rejected.size() + "\n";
        Files.writeString(output.resolve("provenance.properties"), provenance, StandardCharsets.UTF_8);
        System.out.print(provenance);
    }

    private static void addClass(Map<String, String> classes, String source, String target, List<String> rejected) {
        if (source == null || !source.contains("/")) return;
        String previous = classes.putIfAbsent(source, target);
        if (previous != null && !previous.equals(target)) {
            rejected.add("class-alias-conflict\t" + source + "\tretained=" + previous + "\trejected=" + target);
        }
    }

    private static void putMember(Map<Symbol, Target> members, Member member, List<String> rejected) {
        Target previous = members.putIfAbsent(member.source, member.target);
        if (previous != null && !previous.equals(member.target)) {
            rejected.add("alias-conflict\t" + member.source + "\tretained=" + previous + "\trejected=" + member.target);
        }
    }

    private static Target targetMember(Map<String, ClassMappingView> byNativeOwner,
            Map<String, NativeClass> natives, String owner, String name, String descriptor, boolean method, int namespace) {
        var pending = new ArrayDeque<String>();
        var visited = new HashSet<String>();
        pending.add(owner);
        while (!pending.isEmpty()) {
            String next = pending.removeFirst();
            if (!visited.add(next)) continue;
            ClassMappingView source = byNativeOwner.get(next);
            if (source != null) {
                MemberMappingView member = method ? source.getMethod(name, descriptor) : source.getField(name, descriptor);
                if (member != null && member.getName(namespace) != null) return new Target(next, member.getName(namespace), 0);
            }
            NativeClass nativeClass = natives.get(next);
            if (nativeClass != null) pending.addAll(nativeClass.parents);
        }
        return new Target(owner, name, 0);
    }

    private static Target withAccess(Map<String, NativeClass> natives, Target target, boolean method, String descriptor) {
        var pending = new ArrayDeque<String>();
        var visited = new HashSet<String>();
        pending.add(target.owner);
        while (!pending.isEmpty()) {
            String owner = pending.removeFirst();
            if (!visited.add(owner)) continue;
            var type = natives.get(owner);
            if (type == null) continue;
            Integer access = type.members.get(new Symbol(method, owner, target.name, descriptor));
            if (access != null) return new Target(target.owner, target.name, access);
            if (!target.name.startsWith("<")) pending.addAll(type.parents);
        }
        throw new IllegalStateException("Verified member access not found: " + target.owner + "." + target.name + descriptor);
    }

    private static boolean exists(Map<String, NativeClass> natives, Symbol symbol) {
        var pending = new ArrayDeque<String>();
        var visited = new HashSet<String>();
        pending.add(symbol.owner);
        while (!pending.isEmpty()) {
            String owner = pending.removeFirst();
            if (!visited.add(owner)) continue;
            NativeClass type = natives.get(owner);
            if (type == null) continue;
            if (type.members.containsKey(new Symbol(symbol.method, owner, symbol.name, symbol.descriptor))) return true;
            if (!symbol.name.equals("<init>") && !symbol.name.equals("<clinit>")) pending.addAll(type.parents);
        }
        return false;
    }

    private static Map<String, NativeClass> readNativeJar(Path jar) throws Exception {
        Map<String, NativeClass> result = new HashMap<>();
        try (var zip = new ZipFile(jar.toFile())) {
            for (var entry : zip.stream().filter(entry -> entry.getName().endsWith(".class")).toList()) {
                var members = new HashMap<Symbol, Integer>();
                var parents = new ArrayList<String>();
                var reader = new ClassReader(zip.getInputStream(entry));
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override public void visit(int version, int access, String name, String signature, String superclass, String[] interfaces) {
                        if (superclass != null) parents.add(superclass);
                        parents.addAll(List.of(interfaces));
                    }
                    @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                        members.put(new Symbol(false, reader.getClassName(), name, descriptor), access);
                        return null;
                    }
                    @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        members.put(new Symbol(true, reader.getClassName(), name, descriptor), access);
                        return null;
                    }
                }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                result.put(reader.getClassName(), new NativeClass(List.copyOf(parents), Map.copyOf(members)));
            }
        }
        return result;
    }

    private static String sha256(Path path) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(path)) {
            byte[] buffer = new byte[65536];
            for (int count; (count = input.read(buffer)) >= 0;) digest.update(buffer, 0, count);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
