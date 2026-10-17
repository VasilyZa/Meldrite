package org.bukkit.craftbukkit.v1_20_R1.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import dev.meldrite.v1201.compat.NativeLegacyConversion;
import dev.meldrite.v1201.compat.NativeMaterialProjection;
import dev.meldrite.v1201.compat.NativeEntityExtensions;
import dev.meldrite.v1201.hook.AbstractBlockPaperAccessMixin;
import dev.meldrite.v1201.hook.DefaultAttributePaperAccessMixin;
import dev.meldrite.v1201.hook.EntityIdPaperAccessMixin;
import dev.meldrite.v1201.hook.EntitySerializationPaperAccessMixin;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_20_R1.CraftFeatureFlag;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

/** Craft's UnsafeValues ABI backed by Minecraft's registries, item NBT and data fixer. */
@SuppressWarnings({"deprecation", "removal"})
public final class CraftMagicNumbers implements UnsafeValues {
    public static final UnsafeValues INSTANCE = new CraftMagicNumbers();
    private static final Map<Block, Material> BLOCK_MATERIAL = NativeMaterialProjection.blockMaterialMap();
    private static final List<String> SUPPORTED_API = List.of("1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20");
    private static final ComponentFlattener FLATTENER = ComponentFlattener.basic().toBuilder()
            .mapper(TranslatableComponent.class, component -> {
                Text nativeText = Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component.children(List.of())));
                if (nativeText == null) throw new IllegalArgumentException("Invalid translatable component");
                return nativeText.getString();
            }).build();
    private CraftMagicNumbers() {}
    @Override public ComponentFlattener componentFlattener() { return FLATTENER; }
    @Override public PlainComponentSerializer plainComponentSerializer() { return PlainComponentSerializer.builder().flattener(FLATTENER).build(); }
    @Override public PlainTextComponentSerializer plainTextSerializer() { return PlainTextComponentSerializer.plainText(); }
    @Override public GsonComponentSerializer gsonComponentSerializer() { return GsonComponentSerializer.gson(); }
    @Override public GsonComponentSerializer colorDownsamplingGsonComponentSerializer() { return GsonComponentSerializer.colorDownsamplingGson(); }
    @Override public LegacyComponentSerializer legacyComponentSerializer() { return LegacyComponentSerializer.legacySection(); }

    public static Identifier key(Material material) {
        var key = Objects.requireNonNull(material).getKey(); return new Identifier(key.getNamespace(), key.getKey());
    }
    public static Item getItem(Material material) {
        if (material == null) return null;
        return material.isLegacy() ? NativeLegacyConversion.item(material, (short) 0) : NativeMaterialProjection.item(material);
    }
    public static Item getItem(Material material, short data) { return NativeLegacyConversion.item(Objects.requireNonNull(material), data); }
    public static Block getBlock(Material material) {
        if (material == null) return null;
        return material.isLegacy() ? NativeLegacyConversion.block(material, (byte) 0).getBlock() : NativeMaterialProjection.block(material);
    }
    public static BlockState getBlock(Material material, byte data) { return NativeLegacyConversion.block(Objects.requireNonNull(material), data); }
    public static BlockState getBlock(MaterialData material) { return getBlock(material.getItemType(), material.getData()); }
    public static Material getMaterial(Item item) { return NativeMaterialProjection.material(item); }
    public static Material getMaterial(Block block) { return NativeMaterialProjection.material(block); }
    public static MaterialData getMaterial(BlockState state) { return NativeLegacyConversion.toLegacyData(state); }
    public static MaterialData getMaterialData(Item item) { return NativeLegacyConversion.toLegacyData(getMaterial(item)); }
    public static byte toLegacyData(BlockState state) { return NativeLegacyConversion.toLegacyData(state).getData(); }
    public static net.minecraft.entity.EntityType<?> getEntityTypes(EntityType type) {
        if (type == null || type == EntityType.UNKNOWN) return null;
        return Registries.ENTITY_TYPE.getOrEmpty(identifier(type.getKey())).orElse(null);
    }
    public static EntityType getEntityType(net.minecraft.entity.EntityType<?> type) {
        Identifier key = Registries.ENTITY_TYPE.getId(type);
        return key == null ? null : org.bukkit.Registry.ENTITY_TYPE.get(namespaced(key));
    }
    @Override public Material toLegacy(Material material) { return material == null || material.isLegacy() ? material : NativeLegacyConversion.toLegacyData(material).getItemType(); }
    @Override public Material fromLegacy(Material material) { return material == null || !material.isLegacy() ? material : fromLegacy(new MaterialData(material)); }
    @Override public Material fromLegacy(MaterialData material) { return fromLegacy(material, false); }
    @Override public Material fromLegacy(MaterialData material, boolean itemPriority) { return NativeLegacyConversion.fromLegacy(material, itemPriority); }
    @Override public org.bukkit.block.data.BlockData fromLegacy(Material material, byte data) {
        return org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData.fromData(NativeLegacyConversion.block(Objects.requireNonNull(material), data));
    }
    @Override public Material getMaterial(String material, int version) {
        Objects.requireNonNull(material, "material"); requireVersion(version);
        if (version == getDataVersion()) {
            Material direct = Material.getMaterial(material); return direct == null ? NativeMaterialProjection.match(material, false) : direct;
        }
        String id = material.indexOf(':') < 0 ? "minecraft:" + material.toLowerCase(Locale.ROOT) : material.toLowerCase(Locale.ROOT);
        var original = new Dynamic<NbtElement>(NbtOps.INSTANCE, NbtString.of(id));
        var converted = Schemas.getFixer().update(TypeReferences.ITEM_NAME, original, version, getDataVersion());
        if (original.equals(converted)) converted = Schemas.getFixer().update(TypeReferences.BLOCK_NAME, original, version, getDataVersion());
        String updated = converted.asString("");
        Material projection = NativeMaterialProjection.match(updated, false); return projection == null ? Material.matchMaterial(updated) : projection;
    }
    @Override public int getDataVersion() { return SharedConstants.getGameVersion().getSaveVersion().getId(); }
    public String getMappingsVersion() { return "yarn-1.20.1+build.10-v2"; }
    @Override public ItemStack modifyItemStack(ItemStack original, String arguments) {
        Objects.requireNonNull(original); Objects.requireNonNull(arguments);
        NbtCompound parsed;
        try { parsed = StringNbtReader.parse(arguments); }
        catch (CommandSyntaxException failure) { throw new IllegalArgumentException("Could not parse ItemStack NBT", failure); }
        var stack = CraftItemStack.asNMSCopy(original);
        if (stack.isEmpty()) throw new IllegalArgumentException("Cannot apply NBT to an empty item");
        stack.setNbt(parsed);
        var meta = CraftItemStack.getItemMeta(stack);
        if (!original.setItemMeta(meta)) throw new IllegalArgumentException("Item rejected its native metadata");
        return original;
    }
    @Override public byte[] serializeItem(ItemStack item) {
        var stack = CraftItemStack.asNMSCopy(Objects.requireNonNull(item, "item"));
        if (stack.isEmpty()) throw new IllegalArgumentException("air cannot be serialized");
        return serializeNbtToBytes(stack.writeNbt(new NbtCompound()));
    }
    @Override public ItemStack deserializeItem(byte[] data) {
        var encoded = deserializeNbtFromBytes(data);
        int version = encoded.getInt("DataVersion");
        var upgraded = (NbtCompound) Schemas.getFixer().update(TypeReferences.ITEM_STACK,
                new Dynamic<NbtElement>(NbtOps.INSTANCE, encoded), version, getDataVersion()).getValue();
        Identifier id = Identifier.tryParse(upgraded.getString("id"));
        if (id == null || !Registries.ITEM.containsId(id)) throw new IllegalArgumentException("UNKNOWN_SERIALIZED_ITEM: " + upgraded.getString("id"));
        return CraftItemStack.asCraftMirror(net.minecraft.item.ItemStack.fromNbt(upgraded));
    }
    private byte[] serializeNbtToBytes(NbtCompound compound) {
        compound.putInt("DataVersion", getDataVersion());
        var output = new ByteArrayOutputStream();
        try { NbtIo.writeCompressed(compound, output); } catch (IOException failure) { throw new IllegalArgumentException("Could not encode native NBT", failure); }
        return output.toByteArray();
    }
    private NbtCompound deserializeNbtFromBytes(byte[] data) {
        Objects.requireNonNull(data, "data");
        if (data.length == 0) throw new IllegalArgumentException("cannot deserialize nothing");
        try { var result = NbtIo.readCompressed(new ByteArrayInputStream(data)); requireVersion(result.getInt("DataVersion")); return result; }
        catch (IOException failure) { throw new IllegalArgumentException("Could not decode native NBT", failure); }
    }
    private void requireVersion(int version) { if (version > getDataVersion()) throw new IllegalArgumentException("Newer version! Server downgrades are not supported!"); }
    @Override public String getBlockTranslationKey(Material material) { Block block = getBlock(material); return block == null ? null : block.getTranslationKey(); }
    @Override public String getItemTranslationKey(Material material) { Item item = getItem(material); return item == null ? null : item.getTranslationKey(); }
    @Override public String getTranslationKey(EntityType type) {
        var handle = getEntityTypes(Objects.requireNonNull(type));
        if (handle == null) throw new IllegalArgumentException("Invalid EntityType: " + type); return handle.getTranslationKey();
    }
    @Override public String getTranslationKey(ItemStack item) { return CraftItemStack.asNMSCopy(Objects.requireNonNull(item)).getTranslationKey(); }
    @Override public io.papermc.paper.inventory.ItemRarity getItemRarity(Material material) {
        Item item = requireItem(material); return io.papermc.paper.inventory.ItemRarity.valueOf(item.getRarity(item.getDefaultStack()).name());
    }
    @Override public io.papermc.paper.inventory.ItemRarity getItemStackRarity(ItemStack item) { return io.papermc.paper.inventory.ItemRarity.valueOf(CraftItemStack.asNMSCopy(Objects.requireNonNull(item)).getRarity().name()); }
    @Override public boolean isValidRepairItemStack(ItemStack repaired, ItemStack material) {
        var item = CraftItemStack.asNMSCopy(Objects.requireNonNull(repaired)); return item.getItem().canRepair(item, CraftItemStack.asNMSCopy(Objects.requireNonNull(material)));
    }
    @Override public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(Material material, EquipmentSlot slot) {
        var attributes = ImmutableMultimap.<Attribute, AttributeModifier>builder();
        requireItem(material).getAttributeModifiers(nativeSlot(slot)).forEach((nativeAttribute, modifier) -> {
            Identifier key = Registries.ATTRIBUTE.getId(nativeAttribute);
            Attribute attribute = org.bukkit.Registry.ATTRIBUTE.get(namespaced(key));
            if (attribute == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ATTRIBUTE: " + key);
            attributes.put(attribute, new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getValue(), AttributeModifier.Operation.values()[modifier.getOperation().ordinal()], slot));
        });
        return attributes.build();
    }
    @Override public Multimap<Attribute, AttributeModifier> getItemAttributes(Material material, EquipmentSlot slot) { return getDefaultAttributeModifiers(material, slot); }
    @Override public FeatureFlag getFeatureFlag(NamespacedKey key) { return CraftFeatureFlag.getFromNMS(Objects.requireNonNull(key)); }
    @Override public boolean isCollidable(Material material) {
        Block block = getBlock(Objects.requireNonNull(material));
        if (block == null) throw new IllegalArgumentException(material + " is not a block"); return ((AbstractBlockPaperAccessMixin) block).meldrite$isCollidable();
    }
    @Override public boolean isSupportedApiVersion(String version) { return version != null && SUPPORTED_API.contains(version); }
    @Override public void checkSupported(PluginDescriptionFile description) throws InvalidPluginException {
        Objects.requireNonNull(description);
        if (description.getAPIVersion() != null && !isSupportedApiVersion(description.getAPIVersion())) throw new InvalidPluginException("Unsupported API version " + description.getAPIVersion());
    }
    public static boolean isLegacy(PluginDescriptionFile description) { return Objects.requireNonNull(description).getAPIVersion() == null; }
    @Override public int getProtocolVersion() { return SharedConstants.getProtocolVersion(); }
    @Override public String getMainLevelName() { return ((CraftServer) Bukkit.getServer()).getServer().getProperties().levelName; }
    @Override public boolean hasDefaultEntityAttributes(NamespacedKey key) {
        var type = Registries.ENTITY_TYPE.getOrEmpty(identifier(Objects.requireNonNull(key))).orElse(null); return type != null && DefaultAttributeRegistry.hasDefinitionFor(type);
    }
    @Override public Attributable getDefaultEntityAttributes(NamespacedKey key) {
        var type = Registries.ENTITY_TYPE.getOrEmpty(identifier(Objects.requireNonNull(key))).orElseThrow(() -> new IllegalArgumentException("Unknown EntityType: " + key));
        if (!DefaultAttributeRegistry.hasDefinitionFor(type)) throw new IllegalArgumentException("EntityType has no default attributes: " + key);
        @SuppressWarnings("unchecked") var livingType = (net.minecraft.entity.EntityType<? extends net.minecraft.entity.LivingEntity>) type;
        DefaultAttributeContainer container = DefaultAttributeRegistry.get(livingType);
        return new Attributable() {
            @Override public AttributeInstance getAttribute(Attribute attribute) {
                var nativeAttribute = Registries.ATTRIBUTE.getOrEmpty(identifier(Objects.requireNonNull(attribute).getKey())).orElse(null);
                if (nativeAttribute == null || !container.has(nativeAttribute)) return null;
                return new ReadOnlyAttribute(attribute, nativeAttribute, container);
            }
            @Override public void registerAttribute(Attribute attribute) { throw new UnsupportedOperationException("Default entity attributes are immutable"); }
        };
    }
    private record ReadOnlyAttribute(Attribute attribute, EntityAttribute nativeAttribute, DefaultAttributeContainer container) implements AttributeInstance {
        @Override public Attribute getAttribute() { return attribute; }
        @Override public double getBaseValue() { return container.getBaseValue(nativeAttribute); }
        @Override public double getValue() { return container.getValue(nativeAttribute); }
        @Override public double getDefaultValue() { return nativeAttribute.getDefaultValue(); }
        @Override public java.util.Collection<AttributeModifier> getModifiers() {
            return ((DefaultAttributePaperAccessMixin) container).meldrite$getInstances().get(nativeAttribute).getModifiers().stream()
                    .map(modifier -> new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getValue(), AttributeModifier.Operation.values()[modifier.getOperation().ordinal()])).toList();
        }
        @Override public void setBaseValue(double value) { throw new UnsupportedOperationException("Default entity attributes are immutable"); }
        @Override public void addModifier(AttributeModifier modifier) { throw new UnsupportedOperationException("Default entity attributes are immutable"); }
        @Override public void addTransientModifier(AttributeModifier modifier) { throw new UnsupportedOperationException("Default entity attributes are immutable"); }
        @Override public void removeModifier(AttributeModifier modifier) { throw new UnsupportedOperationException("Default entity attributes are immutable"); }
    }
    private static Item requireItem(Material material) { Item item = getItem(Objects.requireNonNull(material)); if (item == null) throw new IllegalArgumentException(material + " is not an item"); return item; }
    private static Identifier identifier(NamespacedKey key) { return new Identifier(key.getNamespace(), key.getKey()); }
    private static NamespacedKey namespaced(Identifier key) { return new NamespacedKey(key.getNamespace(), key.getPath()); }
    private static net.minecraft.entity.EquipmentSlot nativeSlot(EquipmentSlot slot) {
        return switch (Objects.requireNonNull(slot)) {
            case HAND -> net.minecraft.entity.EquipmentSlot.MAINHAND;
            case OFF_HAND -> net.minecraft.entity.EquipmentSlot.OFFHAND;
            case FEET -> net.minecraft.entity.EquipmentSlot.FEET;
            case LEGS -> net.minecraft.entity.EquipmentSlot.LEGS;
            case CHEST -> net.minecraft.entity.EquipmentSlot.CHEST;
            case HEAD -> net.minecraft.entity.EquipmentSlot.HEAD;
        };
    }
    private static UnsupportedOperationException unsupported(String method) { return new UnsupportedOperationException("UNSUPPORTED_NATIVE_UNSAFE_VALUES: " + method); }
    @Override public Component resolveWithContext(Component component, org.bukkit.command.CommandSender context, org.bukkit.entity.Entity subject, boolean bypass) throws IOException { throw unsupported("resolveWithContext"); }
    @Override public void reportTimings() { throw unsupported("reportTimings"); }
    @Override public String getTimingsServerName() { throw unsupported("getTimingsServerName"); }
    @Override public com.destroystokyo.paper.util.VersionFetcher getVersionFetcher() {
        return new com.destroystokyo.paper.util.VersionFetcher.DummyVersionFetcher();
    }
    @Override public byte[] processClass(PluginDescriptionFile description, String path, byte[] bytes) {
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(bytes, "bytes");
        try {
            return org.bukkit.craftbukkit.util.Commodore.convert(bytes, !isLegacy(description));
        } catch (RuntimeException failure) {
            Bukkit.getLogger().log(java.util.logging.Level.SEVERE,
                    "Fatal error converting " + description.getFullName() + ":" + path, failure);
            return bytes;
        }
    }
    @Override public org.bukkit.advancement.Advancement loadAdvancement(NamespacedKey key, String json) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(json, "json");
        if (!(Bukkit.getServer() instanceof CraftServer craft)) throw new IllegalStateException("Bukkit server is not a native CraftServer");
        var id = identifier(key);
        var loader = craft.getServer().getAdvancementLoader();
        var manager = ((dev.meldrite.v1201.hook.ServerAdvancementLoaderAccessMixin) (Object) loader).meldrite$getManager();
        if (manager.get(id) != null) throw new IllegalArgumentException("Advancement already exists: " + key);
        final com.google.gson.JsonObject document;
        try {
            document = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException("Invalid advancement JSON: " + key, failure);
        }
        var builder = net.minecraft.advancement.Advancement.Builder.fromJson(document,
                new net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer(id, craft.getServer().getLootManager()));
        if (builder == null) throw new IllegalArgumentException("Advancement JSON produced no builder: " + key);
        if (!builder.findParent(manager::get)) throw new IllegalArgumentException("Unknown advancement parent for " + key);
        manager.load(java.util.Map.of(id, builder));
        var loaded = manager.get(id);
        if (loaded == null) throw new IllegalStateException("Native advancement manager rejected " + key);
        return new org.bukkit.craftbukkit.v1_20_R1.advancement.CraftAdvancement(loaded);
    }
    @Override public boolean removeAdvancement(NamespacedKey key) {
        Objects.requireNonNull(key, "key");
        if (!(Bukkit.getServer() instanceof CraftServer craft)) throw new IllegalStateException("Bukkit server is not a native CraftServer");
        var manager = ((dev.meldrite.v1201.hook.ServerAdvancementLoaderAccessMixin) (Object) craft.getServer().getAdvancementLoader()).meldrite$getManager();
        var id = identifier(key);
        if (manager.get(id) == null) return false;
        manager.removeAll(java.util.Set.of(id));
        return manager.get(id) == null;
    }
    @Override public org.bukkit.inventory.CreativeCategory getCreativeCategory(Material material) {
        Objects.requireNonNull(material, "material");
        if (!(Bukkit.getServer() instanceof CraftServer craft)) throw new IllegalStateException("Bukkit server is not a native CraftServer");
        return NativeMaterialProjection.creativeCategory(material, craft.getServer().getRegistryManager());
    }
    @Override public byte[] serializeEntity(org.bukkit.entity.Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity instanceof org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity craft)) throw new IllegalArgumentException("Only native CraftEntities can be serialized");
        var handle = craft.getHandle();
        if (!(handle.getWorld() instanceof net.minecraft.server.world.ServerWorld nativeWorld) || !nativeWorld.getServer().isOnThread()) throw new IllegalStateException("ASYNC_ENTITY_SERIALIZATION");
        var accessor = (EntitySerializationPaperAccessMixin) handle;
        var passengers = accessor.meldrite$getPassengerList();
        var compound = new NbtCompound();
        accessor.meldrite$setPassengerList(com.google.common.collect.ImmutableList.of());
        try {
            if (!handle.saveNbt(compound)) throw new IllegalArgumentException("Entity cannot be serialized: " + Registries.ENTITY_TYPE.getId(handle.getType()));
        } finally { accessor.meldrite$setPassengerList(passengers); }
        return serializeNbtToBytes(compound);
    }
    @Override public org.bukkit.entity.Entity deserializeEntity(byte[] data, org.bukkit.World world) { return deserializeEntity(data, world, false); }
    @Override public org.bukkit.entity.Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID) {
        var compound = deserializeNbtFromBytes(data);
        Objects.requireNonNull(world, "world");
        if (!(world instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld craft)) throw new IllegalArgumentException("Only native CraftWorlds can restore entities");
        var nativeWorld = craft.getHandle();
        if (!nativeWorld.getServer().isOnThread()) throw new IllegalStateException("ASYNC_ENTITY_DESERIALIZATION");
        compound = upgradeEntityNbt(compound, preserveUUID);
        Identifier id = Identifier.tryParse(compound.getString("id"));
        var type = id == null ? null : Registries.ENTITY_TYPE.getOrEmpty(id).orElse(null);
        if (type == null || !type.isSaveable()) throw new IllegalArgumentException("UNRESTORABLE_SERIALIZED_ENTITY: " + compound.getString("id"));
        var handle = net.minecraft.entity.EntityType.getEntityFromNbt(compound, nativeWorld)
                .orElseThrow(() -> new IllegalArgumentException("The native entity could not be restored: " + id));
        return NativeEntityExtensions.bukkit(handle);
    }
    private NbtCompound upgradeEntityNbt(NbtCompound compound, boolean preserveUUID) {
        var upgraded = (NbtCompound) Schemas.getFixer().update(TypeReferences.ENTITY,
                new Dynamic<NbtElement>(NbtOps.INSTANCE, compound), compound.getInt("DataVersion"), getDataVersion()).getValue();
        if (!preserveUUID) upgraded.remove("UUID");
        return upgraded;
    }
    @Override public int nextEntityId() { return EntityIdPaperAccessMixin.meldrite$getCurrentId().incrementAndGet(); }
    @Override public NamespacedKey getBiomeKey(org.bukkit.RegionAccessor region, int x, int y, int z) {
        Objects.requireNonNull(region, "region");
        org.bukkit.block.Biome biome = region.getBiome(x, y, z);
        return biome == null ? null : biome.getKey();
    }
    @Override public void setBiomeKey(org.bukkit.RegionAccessor region, int x, int y, int z, NamespacedKey key) {
        Objects.requireNonNull(region, "region");
        Objects.requireNonNull(key, "key");
        org.bukkit.block.Biome biome = org.bukkit.Registry.BIOME.get(key);
        if (biome == null) throw new IllegalArgumentException("Unknown biome: " + key);
        region.setBiome(x, y, z, biome);
    }
    @Override public String getStatisticCriteriaKey(org.bukkit.Statistic statistic) {
        Objects.requireNonNull(statistic, "statistic");
        if (statistic.getType() != org.bukkit.Statistic.Type.UNTYPED) return "minecraft.custom:minecraft." + statistic.getKey().getKey();
        return org.bukkit.craftbukkit.v1_20_R1.CraftStatistic.getNMSStatistic(statistic).getName();
    }
    public static final class NBT {
        public static final int TAG_END = NbtElement.END_TYPE;
        public static final int TAG_BYTE = NbtElement.BYTE_TYPE;
        public static final int TAG_SHORT = NbtElement.SHORT_TYPE;
        public static final int TAG_INT = NbtElement.INT_TYPE;
        public static final int TAG_LONG = NbtElement.LONG_TYPE;
        public static final int TAG_FLOAT = NbtElement.FLOAT_TYPE;
        public static final int TAG_DOUBLE = NbtElement.DOUBLE_TYPE;
        public static final int TAG_BYTE_ARRAY = NbtElement.BYTE_ARRAY_TYPE;
        public static final int TAG_STRING = NbtElement.STRING_TYPE;
        public static final int TAG_LIST = NbtElement.LIST_TYPE;
        public static final int TAG_COMPOUND = NbtElement.COMPOUND_TYPE;
        public static final int TAG_INT_ARRAY = NbtElement.INT_ARRAY_TYPE;
        public static final int TAG_LONG_ARRAY = NbtElement.LONG_ARRAY_TYPE;
        public static final int TAG_ANY_NUMBER = NbtElement.NUMBER_TYPE;
        private NBT() {}
    }
}
