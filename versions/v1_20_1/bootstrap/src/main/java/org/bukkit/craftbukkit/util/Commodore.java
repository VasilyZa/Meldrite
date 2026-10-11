package org.bukkit.craftbukkit.util;

import dev.meldrite.paper.compat.LegacyTextConstructorTransform;
import dev.meldrite.paper.compat.EntityAbiTransform;
import dev.meldrite.paper.compat.LegacyNameTransform;
import dev.meldrite.plugin.spi.TransformContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import org.bukkit.Material;
import org.bukkit.plugin.AuthorNagException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * CraftBukkit's plugin bytecode compatibility pass.
 *
 * <p>The loader normally applies the same passes through the plugin classloader. This
 * facade is kept for plugins and tools which call the historical Commodore entry point
 * directly, so the result is the real transformed class rather than the input bytes.</p>
 */
public final class Commodore {
    private static final Set<String> EVIL = Set.of(
            "org/bukkit/World (III)I getBlockTypeIdAt",
            "org/bukkit/World (Lorg/bukkit/Location;)I getBlockTypeIdAt",
            "org/bukkit/block/Block ()I getTypeId",
            "org/bukkit/block/Block (I)Z setTypeId",
            "org/bukkit/block/Block (IZ)Z setTypeId",
            "org/bukkit/block/Block (IBZ)Z setTypeIdAndData",
            "org/bukkit/block/Block (B)V setData",
            "org/bukkit/block/Block (BZ)V setData",
            "org/bukkit/inventory/ItemStack ()I getTypeId",
            "org/bukkit/inventory/ItemStack (I)V setTypeId");
    private static final TransformContext CONTEXT = new TransformContext("commodore", TransformContext.Access.INTERNAL);

    private Commodore() { }

    public static void main(String[] args) {
        File input = null;
        File output = null;
        for (int i = 0; i < args.length; i++) {
            if (("-i".equals(args[i]) || "--input".equals(args[i])) && i + 1 < args.length) input = new File(args[++i]);
            else if (("-o".equals(args[i]) || "--output".equals(args[i])) && i + 1 < args.length) output = new File(args[++i]);
        }
        if (input == null || output == null) throw new IllegalArgumentException("Usage: Commodore --input <jar|directory> --output <jar|directory>");
        if (input.isDirectory()) {
            if (!output.isDirectory() && !output.mkdirs()) throw new IllegalArgumentException("Output directory cannot be created: " + output);
            File[] files = input.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null) for (File jar : files) convert(jar, new File(output, jar.getName()));
        } else convert(input, output);
    }

    public static void convert(File input, File output) {
        if (input.equals(output)) throw new IllegalArgumentException("Commodore input and output must differ");
        try (JarFile source = new JarFile(input, false); JarOutputStream target = new JarOutputStream(new FileOutputStream(output))) {
            if (source.getJarEntry(".commodore") != null) {
                copyJar(source, target);
                return;
            }
            Enumeration<JarEntry> entries = source.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                byte[] bytes;
                try (InputStream stream = source.getInputStream(entry)) { bytes = stream.readAllBytes(); }
                if (entry.getName().endsWith(".class")) bytes = convert(bytes, false);
                JarEntry copy = new JarEntry(entry.getName());
                copy.setTime(entry.getTime());
                target.putNextEntry(copy);
                target.write(bytes);
                target.closeEntry();
            }
            target.putNextEntry(new ZipEntry(".commodore"));
            target.closeEntry();
        } catch (IOException failure) {
            throw new IllegalStateException("Unable to convert plugin " + input, failure);
        }
    }

    private static void copyJar(JarFile source, JarOutputStream target) throws IOException {
        Enumeration<JarEntry> entries = source.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            target.putNextEntry(new JarEntry(entry.getName()));
            try (InputStream stream = source.getInputStream(entry)) { stream.transferTo(target); }
            target.closeEntry();
        }
    }

    /** Applies legacy Bukkit rewriting and the server's native class mapping. */
    public static byte[] convert(byte[] input, boolean modern) {
        if (input == null || input.length < 4 || input[0] != (byte) 0xCA || input[1] != (byte) 0xFE
                || input[2] != (byte) 0xBA || input[3] != (byte) 0xBE) return input;
        byte[] mapped = new LegacyTextConstructorTransform().apply(input, CONTEXT);
        mapped = new EntityAbiTransform().apply(mapped, CONTEXT);
        mapped = new LegacyNameTransform().apply(mapped, CONTEXT);
        ClassReader reader = new ClassReader(mapped);
        ClassWriter writer = new ClassWriter(reader, 0);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                return super.visitField(access, name, rewrite(descriptor), rewrite(signature), value);
            }
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor delegate = super.visitMethod(access, name, rewrite(descriptor), rewrite(signature), exceptions);
                return new MethodVisitor(Opcodes.ASM9, delegate) {
                    @Override public void visitInvokeDynamicInsn(String name, String descriptor, Handle handle, Object... args) {
                        super.visitInvokeDynamicInsn(name, rewrite(descriptor), handle, args);
                    }
                    @Override public void visitTypeInsn(int opcode, String type) { super.visitTypeInsn(opcode, rewrite(type)); }
                    @Override public void visitFrame(int type, int localsCount, Object[] locals, int stackCount, Object[] stack) {
                        rewriteFrame(locals);
                        rewriteFrame(stack);
                        super.visitFrame(type, localsCount, locals, stackCount, stack);
                    }
                    @Override public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
                        super.visitLocalVariable(name, rewrite(descriptor), rewrite(signature), start, end, index);
                    }
                    @Override public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        owner = rewrite(owner);
                        descriptor = rewrite(descriptor);
                        if (owner.equals("org/bukkit/scoreboard/DisplaySlot") && name.startsWith("SIDEBAR_") && !name.startsWith("SIDEBAR_TEAM_")) name = name.replace("SIDEBAR_", "SIDEBAR_TEAM_");
                        if (owner.equals("org/bukkit/block/Biome")) name = biomeField(name);
                        if (owner.equals("org/bukkit/entity/EntityType") && name.equals("PIG_ZOMBIE")) name = "ZOMBIFIED_PIGLIN";
                        if (owner.equals("org/bukkit/loot/LootTables") && name.equals("ZOMBIE_PIGMAN")) name = "ZOMBIFIED_PIGLIN";
                        if (owner.equals("org/bukkit/entity/TextDisplay$TextAligment")) owner = "org/bukkit/entity/TextDisplay$TextAlignment";
                        if (modern && owner.equals("org/bukkit/Material")) name = modernMaterial(name);
                        else if (!modern && owner.equals("org/bukkit/Material")) {
                            try { Material.valueOf("LEGACY_" + name); } catch (IllegalArgumentException failure) { throw new AuthorNagException("No legacy enum constant for " + name + ". Did you forget to define a modern (1.13+) api-version in your plugin.yml?"); }
                            name = "LEGACY_" + name;
                        }
                        super.visitFieldInsn(opcode, owner, name, descriptor);
                    }
                    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean itf) {
                        owner = rewrite(owner);
                        descriptor = rewrite(descriptor);
                        if (owner.equals("org/bukkit/map/MapView") && name.equals("getId") && descriptor.equals("()S")) descriptor = "()I";
                        if ((owner.equals("org/bukkit/Bukkit") || owner.equals("org/bukkit/Server")) && name.equals("getMap") && descriptor.equals("(S)Lorg/bukkit/map/MapView;")) descriptor = "(I)Lorg/bukkit/map/MapView;";
                        if (owner.equals("org/bukkit/entity/TextDisplay$TextAligment")) owner = "org/bukkit/entity/TextDisplay$TextAlignment";
                        if (descriptor.contains("TextDisplay$TextAligment")) descriptor = descriptor.replace("TextDisplay$TextAligment", "TextDisplay$TextAlignment");
                        if (!modern && EVIL.contains(owner + " " + descriptor + " " + name)) {
                            Type returnType = Type.getReturnType(descriptor);
                            Type[] old = Type.getArgumentTypes(descriptor);
                            Type[] args = new Type[old.length + 1];
                            args[0] = Type.getObjectType(owner);
                            System.arraycopy(old, 0, args, 1, old.length);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/bukkit/craftbukkit/legacy/CraftEvil", name, Type.getMethodDescriptor(returnType, args), false);
                            return;
                        }
                        super.visitMethodInsn(opcode, owner, name, descriptor, itf);
                    }
                    @Override public void visitLdcInsn(Object value) {
                        if (value instanceof Type type && (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY)) value = Type.getType(rewrite(type.getDescriptor()));
                        if ("com.mysql.jdbc.Driver".equals(value)) value = "com.mysql.cj.jdbc.Driver";
                        super.visitLdcInsn(value);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }

    private static void rewriteFrame(Object[] frame) {
        if (frame == null) return;
        for (int i = 0; i < frame.length; i++) if (frame[i] instanceof String value) frame[i] = rewrite(value);
    }
    private static String rewrite(String value) {
        if (value == null) return null;
        return value.replace("org/bukkit/craftbukkit/v1_20_R3/", "org/bukkit/craftbukkit/v1_20_R1/")
                .replace("org.bukkit.craftbukkit.v1_20_R3.", "org.bukkit.craftbukkit.v1_20_R1.");
    }
    private static String biomeField(String name) {
        return switch (name) {
            case "NETHER" -> "NETHER_WASTES";
            case "TALL_BIRCH_FOREST" -> "OLD_GROWTH_BIRCH_FOREST";
            case "GIANT_TREE_TAIGA" -> "OLD_GROWTH_PINE_TAIGA";
            case "GIANT_SPRUCE_TAIGA" -> "OLD_GROWTH_SPRUCE_TAIGA";
            case "SNOWY_TUNDRA" -> "SNOWY_PLAINS";
            case "JUNGLE_EDGE" -> "SPARSE_JUNGLE";
            case "STONE_SHORE" -> "STONY_SHORE";
            case "MOUNTAINS" -> "WINDSWEPT_HILLS";
            case "WOODED_MOUNTAINS" -> "WINDSWEPT_FOREST";
            case "GRAVELLY_MOUNTAINS" -> "WINDSWEPT_GRAVELLY_HILLS";
            case "SHATTERED_SAVANNA" -> "WINDSWEPT_SAVANNA";
            case "WOODED_BADLANDS_PLATEAU" -> "WOODED_BADLANDS";
            default -> name;
        };
    }
    private static String modernMaterial(String name) {
        return switch (name) {
            case "CACTUS_GREEN" -> "GREEN_DYE";
            case "DANDELION_YELLOW" -> "YELLOW_DYE";
            case "ROSE_RED" -> "RED_DYE";
            case "SIGN" -> "OAK_SIGN";
            case "WALL_SIGN" -> "OAK_WALL_SIGN";
            case "ZOMBIE_PIGMAN_SPAWN_EGG" -> "ZOMBIFIED_PIGLIN_SPAWN_EGG";
            case "GRASS_PATH" -> "DIRT_PATH";
            default -> name;
        };
    }
}
