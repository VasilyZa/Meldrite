package dev.meldrite.v1201.compat;

import com.google.common.collect.ImmutableMultimap;
import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.CreativeCategory;

/** Exact enum and identity projection of the frozen native item and block registries. */
public final class NativeMaterialProjection {
    private record Entry(Identifier key, Item item, Block block, boolean extended) { }
    private record State(Map<Material, Entry> entries, Map<Identifier, Material> keys,
                         Map<Item, Material> items, Map<Block, Material> blocks) { }
    private static volatile State state;
    private static final MutableBlockMaterialMap BLOCK_MATERIAL = new MutableBlockMaterialMap();

    private NativeMaterialProjection() { }

    /** Stable Paper-facing conversion map; obtaining the reference does not bind Material. */
    public static Map<Block, Material> blockMaterialMap() { return BLOCK_MATERIAL; }

    public static synchronized void bind() {
        if (state != null) {
            if (state.items.size() != Registries.ITEM.size() || state.blocks.size() != Registries.BLOCK.size())
                throw new IllegalStateException("Native material registries changed after Material publication");
            for (Item item : Registries.ITEM) {
                Material material = state.items.get(item);
                if (material == null || !state.entries.get(material).key.equals(Registries.ITEM.getId(item)))
                    throw new IllegalStateException("Native item identity changed after Material publication");
            }
            for (Block block : Registries.BLOCK) {
                Material material = state.blocks.get(block);
                if (material == null || !state.entries.get(material).key.equals(Registries.BLOCK.getId(block)))
                    throw new IllegalStateException("Native block identity changed after Material publication");
            }
            return;
        }
        try {
            Material.class.getDeclaredMethod("meldrite$nativeMaterials");
        } catch (NoSuchMethodException failure) {
            throw new IllegalStateException("Paper Material was loaded without the native Material API transform", failure);
        }
        var existing = new HashMap<Identifier, Material>();
        var names = new HashSet<String>();
        for (Material material : Material.values()) {
            names.add(material.name());
            if (!material.isLegacy()) existing.put(identifier(material.getKey()), material);
        }
        var keys = new TreeSet<Identifier>();
        keys.addAll(Registries.ITEM.getIds());
        keys.addAll(Registries.BLOCK.getIds());
        var entries = new IdentityHashMap<Material, Entry>();
        var itemMaterials = new IdentityHashMap<Item, Material>();
        var blockMaterials = new IdentityHashMap<Block, Material>();
        var materials = new HashMap<Identifier, Material>();
        var additions = new ArrayList<Material>();
        int ordinal = Material.values().length;
        for (Identifier key : keys) {
            Item item = Registries.ITEM.getOrEmpty(key).orElse(null);
            Block block = Registries.BLOCK.getOrEmpty(key).orElse(null);
            Material material = existing.get(key);
            boolean extended = material == null;
            if (extended) {
                String name = enumName(key);
                if (!names.add(name)) throw new IllegalStateException("Native Material enum name collision: " + key);
                material = MaterialEnumExtension.create(name, ordinal++, item == null ? -1 : Registries.ITEM.getRawId(item),
                        item == null ? 0 : item.getMaxCount(), item == null ? 0 : item.getMaxDamage(),
                        new NamespacedKey(key.getNamespace(), key.getPath()), block != null);
                additions.add(material);
            }
            entries.put(material, new Entry(key, item, block, extended));
            materials.put(key, material);
            if (item != null) itemMaterials.put(item, material);
            if (block != null) blockMaterials.put(block, material);
        }
        MaterialEnumExtension.publish(additions);
        State published = new State(Collections.unmodifiableMap(entries), Map.copyOf(materials),
                Collections.unmodifiableMap(itemMaterials), Collections.unmodifiableMap(blockMaterials));
        BLOCK_MATERIAL.bind(blockMaterials);
        state = published;
    }

    public static boolean extended(Material material) {
        State current = state;
        Entry entry = current == null ? null : current.entries.get(material);
        return entry != null && entry.extended;
    }

    public static Material material(Item item) {
        State current = state;
        if (current != null) return current.items.get(item);
        Identifier key = Registries.ITEM.getId(item);
        return key.getNamespace().equals("minecraft") ? Material.matchMaterial(key.toString()) : null;
    }

    public static Material material(Block block) {
        State current = state;
        if (current != null) return BLOCK_MATERIAL.get(block);
        Identifier key = Registries.BLOCK.getId(block);
        return key.getNamespace().equals("minecraft") ? Material.matchMaterial(key.toString()) : null;
    }

    private static final class MutableBlockMaterialMap extends AbstractMap<Block, Material> {
        private volatile Map<Block, Material> backing;

        private void bind(Map<Block, Material> canonical) {
            if (backing != null) throw new IllegalStateException("Native block material map was already bound");
            backing = Collections.synchronizedMap(new IdentityHashMap<>(canonical));
        }

        private Map<Block, Material> backing() {
            State published = state;
            Map<Block, Material> current = backing;
            if (published == null || current == null) {
                throw new IllegalStateException("NATIVE_BLOCK_MATERIAL_MAP_NOT_BOUND: NativeMaterialProjection.bind() has not completed");
            }
            return current;
        }

        @Override public Set<Map.Entry<Block, Material>> entrySet() {
            Set<Map.Entry<Block, Material>> entries = backing().entrySet();
            // IdentityHashMap's own spliterator returns immutable snapshots.
            // An iterator-backed view keeps Paper's entries writable through streams as well.
            return new AbstractSet<>() {
                @Override public Iterator<Map.Entry<Block, Material>> iterator() { return entries.iterator(); }
                @Override public int size() { return entries.size(); }
                @Override public boolean contains(Object entry) { return entries.contains(entry); }
                @Override public boolean remove(Object entry) { return entries.remove(entry); }
                @Override public void clear() { entries.clear(); }
            };
        }
        @Override public Material get(Object key) { return backing().get(key); }
        @Override public boolean containsKey(Object key) { return backing().containsKey(key); }
        @Override public Material put(Block key, Material value) { return backing().put(key, value); }
        @Override public Material remove(Object key) { return backing().remove(key); }
        @Override public void clear() { backing().clear(); }
        @Override public int size() { return backing().size(); }
    }

    /**
     * Projects the native creative tab containing an item to Bukkit's smaller
     * category enum. The native tab list is refreshed from the live registry so
     * Fabric item-group extensions participate in the lookup as well.
     */
    public static synchronized CreativeCategory creativeCategory(Material material, DynamicRegistryManager registryManager) {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(registryManager, "registryManager");
        Item item = requireItem(entry(material));
        ItemGroups.updateDisplayContext(FeatureFlags.DEFAULT_ENABLED_FEATURES, false, registryManager);
        var stack = item.getDefaultStack();
        for (ItemGroup group : ItemGroups.getGroups()) {
            if (group == ItemGroups.getSearchGroup() || !group.contains(stack)) continue;
            Identifier id = Registries.ITEM_GROUP.getId(group);
            if (id == null) continue;
            return switch (id.getPath()) {
                case "building_blocks", "colored_blocks", "natural", "natural_blocks" -> CreativeCategory.BUILDING_BLOCKS;
                case "functional" -> CreativeCategory.DECORATIONS;
                case "redstone", "redstone_blocks" -> CreativeCategory.REDSTONE;
                case "tools", "tools_and_utilities" -> CreativeCategory.TOOLS;
                case "combat" -> CreativeCategory.COMBAT;
                case "food_and_drinks" -> CreativeCategory.FOOD;
                case "ingredients" -> CreativeCategory.BREWING;
                case "spawn_eggs" -> CreativeCategory.MISC;
                default -> throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ITEM_GROUP: " + id);
            };
        }
        throw new UnsupportedOperationException("NATIVE_ITEM_HAS_NO_CREATIVE_CATEGORY: " + Registries.ITEM.getId(item));
    }

    public static Item item(Material material) { return entry(material).item; }
    public static Block block(Material material) { return entry(material).block; }

    public static Material match(String text, boolean legacy) {
        State current = state;
        if (current == null || legacy || text == null) return null;
        Identifier key = Identifier.tryParse(text.toLowerCase(Locale.ROOT));
        return key == null ? null : current.keys.get(key);
    }

    /** Called by the Material transform only for appended constants. */
    public static Object property(Material material, String method, Object argument) {
        Entry entry = entry(material);
        Block block = entry.block;
        Item item = entry.item;
        var blockState = block == null ? null : block.getDefaultState();
        return switch (method) {
            case "isBlock" -> block != null;
            case "isItem" -> item != null;
            case "isAir", "isEmpty" -> blockState != null && blockState.isAir();
            case "isEdible" -> item != null && item.isFood();
            case "isRecord" -> item instanceof MusicDiscItem;
            case "isSolid" -> blockState != null && blockState.isSolid();
            case "isTransparent" -> blockState != null && blockState.isTransparent(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            case "isOccluding" -> blockState != null && blockState.isOpaque();
            case "hasGravity" -> block instanceof FallingBlock;
            case "isFlammable" -> block != null && fireChance(block, "burnChances", "field_11095") > 0;
            case "isBurnable" -> block != null && fireChance(block, "spreadChances", "field_11091") > 0;
            case "isFuel" -> item != null && AbstractFurnaceBlockEntity.canUseAsFuel(item.getDefaultStack());
            case "isCollidable" -> nativeField(AbstractBlock.class, "collidable", "field_23159", "Z", requireBlock(entry));
            case "getHardness" -> requireBlock(entry).getHardness();
            case "getBlastResistance" -> requireBlock(entry).getBlastResistance();
            case "getSlipperiness" -> requireBlock(entry).getSlipperiness();
            case "getCraftingRemainingItem" -> material(requireItem(entry).getRecipeRemainder());
            case "getEquipmentSlot" -> equipmentSlot(LivingEntity.getPreferredEquipmentSlot(requireItem(entry).getDefaultStack()));
            case "getItemRarity" -> io.papermc.paper.inventory.ItemRarity.valueOf(requireItem(entry).getRarity(requireItem(entry).getDefaultStack()).name());
            case "getItemAttributes", "getDefaultAttributeModifiers" -> attributes(entry, (EquipmentSlot) argument);
            case "translationKey", "getTranslationKey" -> item != null ? item.getTranslationKey() : requireBlock(entry).getTranslationKey();
            case "getItemTranslationKey" -> item == null ? null : item.getTranslationKey();
            case "getBlockTranslationKey" -> block == null ? null : block.getTranslationKey();
            case "isEnabledByFeature" -> enabled(entry, (org.bukkit.World) argument);
            case "isInteractable" -> throw new UnsupportedOperationException("UNSUPPORTED_NATIVE_MATERIAL_PROPERTY: " + entry.key + "#" + method);
            case "getCreativeCategory" -> creativeCategory(material, liveRegistryManager());
            default -> throw new IllegalArgumentException("Unknown native Material property: " + method);
        };
    }

    private static Entry entry(Material material) {
        State current = state;
        Entry entry = current == null ? null : current.entries.get(material);
        if (entry == null) throw new IllegalArgumentException("Material has no native registry identity: " + material);
        return entry;
    }

    private static DynamicRegistryManager liveRegistryManager() {
        if (!(org.bukkit.Bukkit.getServer() instanceof org.bukkit.craftbukkit.v1_20_R1.CraftServer craft)) {
            throw new IllegalStateException("Bukkit server is not a native CraftServer");
        }
        return craft.getServer().getRegistryManager();
    }
    private static Item requireItem(Entry entry) {
        if (entry.item == null) throw new IllegalArgumentException(entry.key + " is not an item");
        return entry.item;
    }
    private static Block requireBlock(Entry entry) {
        if (entry.block == null) throw new IllegalArgumentException(entry.key + " is not a block");
        return entry.block;
    }
    private static Identifier identifier(NamespacedKey key) { return new Identifier(key.getNamespace(), key.getKey()); }
    private static String enumName(Identifier key) {
        // Hex-encoding separators is injective even for legal keys such as a-b and a_b.
        var name = new StringBuilder("MOD_");
        key.toString().codePoints().forEach(character -> {
            if (character >= 'a' && character <= 'z' || character >= '0' && character <= '9') name.appendCodePoint(Character.toUpperCase(character));
            else name.append('_').append(Integer.toHexString(character).toUpperCase(Locale.ROOT)).append('_');
        });
        return name.toString();
    }
    private static EquipmentSlot equipmentSlot(net.minecraft.entity.EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> EquipmentSlot.HAND;
            case OFFHAND -> EquipmentSlot.OFF_HAND;
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            case HEAD -> EquipmentSlot.HEAD;
        };
    }
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
    private static Object attributes(Entry entry, EquipmentSlot slot) {
        var result = ImmutableMultimap.<Attribute, AttributeModifier>builder();
        requireItem(entry).getAttributeModifiers(nativeSlot(slot)).forEach((nativeAttribute, modifier) -> {
            Identifier key = Registries.ATTRIBUTE.getId(nativeAttribute);
            Attribute attribute = org.bukkit.Registry.ATTRIBUTE.get(new NamespacedKey(key.getNamespace(), key.getPath()));
            if (attribute == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ATTRIBUTE: " + key);
            var operation = switch (modifier.getOperation()) {
                case ADDITION -> AttributeModifier.Operation.ADD_NUMBER;
                case MULTIPLY_BASE -> AttributeModifier.Operation.ADD_SCALAR;
                case MULTIPLY_TOTAL -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
            };
            result.put(attribute, new AttributeModifier(modifier.getId(), modifier.getName(), modifier.getValue(), operation, slot));
        });
        return result.build();
    }
    private static boolean enabled(Entry entry, org.bukkit.World world) {
        Objects.requireNonNull(world, "world");
        if (!(world instanceof org.bukkit.craftbukkit.v1_20_R1.CraftWorld nativeWorld))
            throw new IllegalArgumentException("World has no native feature set: " + world);
        var features = nativeWorld.getHandle().getEnabledFeatures();
        return entry.item != null ? entry.item.isEnabled(features) : requireBlock(entry).isEnabled(features);
    }
    private static int fireChance(Block block, String name, String intermediaryName) {
        var chances = (it.unimi.dsi.fastutil.objects.Object2IntMap<?>) nativeField(FireBlock.class, name, intermediaryName,
                "Lit/unimi/dsi/fastutil/objects/Object2IntMap;", Blocks.FIRE);
        return chances.getInt(block);
    }
    private static Object nativeField(Class<?> owner, String name, String intermediaryName, String descriptor, Object instance) {
        try {
            Field field;
            try { field = owner.getDeclaredField(name); }
            catch (NoSuchFieldException namedFailure) {
                var mappings = FabricLoader.getInstance().getMappingResolver();
                String intermediaryOwner = mappings.unmapClassName("intermediary", owner.getName());
                field = owner.getDeclaredField(mappings.mapFieldName("intermediary", intermediaryOwner, intermediaryName, descriptor));
            }
            field.setAccessible(true);
            return field.get(instance);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Native Material property cannot access " + owner.getName() + "#" + name, failure);
        }
    }
}
