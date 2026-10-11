package org.bukkit.craftbukkit.v1_20_R1.structure;

import dev.meldrite.v1201.hook.StructureTemplateManagerAccessMixin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.structure.Structure;

public class CraftStructureManager implements org.bukkit.structure.StructureManager {
    private final StructureTemplateManager handle;
    public CraftStructureManager(StructureTemplateManager handle) { this.handle = Objects.requireNonNull(handle); }
    public StructureTemplateManager getHandle() { return handle; }
    private StructureTemplateManagerAccessMixin access() { return (StructureTemplateManagerAccessMixin) handle; }
    private Identifier id(NamespacedKey key) {
        var id = CraftNamespacedKey.toMinecraft(Objects.requireNonNull(key));
        if (id.getPath().contains("//")) throw new IllegalArgumentException("Invalid structure key");
        handle.getTemplatePath(id, ".nbt"); return id;
    }
    private static CraftStructure craft(Structure structure) { if (!(Objects.requireNonNull(structure) instanceof CraftStructure craft)) throw new IllegalArgumentException("Native structure required"); return craft; }
    private static Structure wrap(Optional<StructureTemplate> template) { return template == null ? null : template.map(CraftStructure::new).orElse(null); }
    private void writable() { dev.meldrite.v1201.compat.NativeRecipes.requireThread(); }
    @Override public Map<NamespacedKey, Structure> getStructures() {
        var result = new java.util.HashMap<NamespacedKey, Structure>();
        access().meldrite$templates().forEach((key, template) -> template.ifPresent(value -> result.put(CraftNamespacedKey.fromMinecraft(key), new CraftStructure(value))));
        return Map.copyOf(result);
    }
    @Override public Structure getStructure(NamespacedKey key) { return wrap(access().meldrite$templates().get(id(key))); }
    @Override public Structure registerStructure(NamespacedKey key, Structure structure) { writable(); return wrap(access().meldrite$templates().put(id(key), Optional.of(craft(structure).getHandle()))); }
    @Override public Structure unregisterStructure(NamespacedKey key) { writable(); return wrap(access().meldrite$templates().remove(id(key))); }
    @Override public Structure loadStructure(NamespacedKey key) { return loadStructure(key, true); }
    @Override public Structure loadStructure(NamespacedKey key, boolean register) {
        writable(); var id = id(key); var value = access().meldrite$templates().getOrDefault(id, Optional.empty());
        if (value.isEmpty()) value = access().meldrite$load(id);
        if (register) access().meldrite$templates().put(id, value);
        return wrap(value);
    }
    @Override public void saveStructure(NamespacedKey key) { writable(); if (!handle.saveTemplate(id(key))) throw new IllegalStateException("Cannot save structure: " + key); }
    @Override public void saveStructure(NamespacedKey key, Structure structure) throws IOException {
        var file = getStructureFile(key); Files.createDirectories(file.toPath().getParent()); saveStructure(file, structure);
    }
    @Override public void deleteStructure(NamespacedKey key) throws IOException { deleteStructure(key, true); }
    @Override public void deleteStructure(NamespacedKey key, boolean unregister) throws IOException {
        writable(); var id = id(key); if (unregister) access().meldrite$templates().remove(id); Files.deleteIfExists(handle.getTemplatePath(id, ".nbt"));
    }
    @Override public File getStructureFile(NamespacedKey key) { return handle.getTemplatePath(id(key), ".nbt").toFile(); }
    @Override public Structure loadStructure(File file) throws IOException { try (var input = Files.newInputStream(Objects.requireNonNull(file).toPath())) { return loadStructure(input); } }
    @Override public Structure loadStructure(InputStream input) throws IOException { return new CraftStructure(access().meldrite$read(Objects.requireNonNull(input))); }
    @Override public void saveStructure(File file, Structure structure) throws IOException { try (var output = Files.newOutputStream(Objects.requireNonNull(file).toPath())) { saveStructure(output, structure); } }
    @Override public void saveStructure(OutputStream output, Structure structure) throws IOException { NbtIo.writeCompressed(craft(structure).getHandle().writeNbt(new NbtCompound()), Objects.requireNonNull(output)); }
    @Override public Structure createStructure() { return new CraftStructure(new StructureTemplate()); }
    @Override public Structure copy(Structure structure) { return new CraftStructure(handle.createTemplate(craft(structure).getHandle().writeNbt(new NbtCompound()))); }
}
