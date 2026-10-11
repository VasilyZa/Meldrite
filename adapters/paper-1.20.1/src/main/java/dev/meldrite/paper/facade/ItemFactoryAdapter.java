package dev.meldrite.paper.facade;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.craftbukkit.inventory.CraftMetaSkull;
import org.bukkit.craftbukkit.inventory.*;

/** Bukkit metadata plus the bound Minecraft item operations. */
public class ItemFactoryAdapter implements ItemFactory {
    public record Presentation(String namespace, String key, int count, String metadataSnbt,
                               String displayNameJson, String localizedName) {
        public Presentation { Objects.requireNonNull(namespace); Objects.requireNonNull(key); Objects.requireNonNull(displayNameJson); Objects.requireNonNull(localizedName); }
    }
    public interface NativeBridge {
        ItemStack createItemStack(String input);
        ItemStack enchantWithLevels(ItemStack stack, int levels, boolean treasure, Random random);
        ItemStack ensureServerConversions(ItemStack stack);
        Presentation presentation(ItemStack stack);
        Material getSpawnEgg(EntityType type);
        Material fromLegacy(Material material, short data);
        default Map<String, Object> itemData(ItemStack item) { throw new IllegalStateException("ITEM_NBT_BRIDGE_NOT_BOUND"); }
        default ItemStack itemFromData(Map<String, Object> data) { throw new IllegalStateException("ITEM_NBT_BRIDGE_NOT_BOUND"); }
        default boolean isCrossbowProjectile(ItemStack item) { return Set.of(Material.ARROW, Material.SPECTRAL_ARROW, Material.TIPPED_ARROW, Material.FIREWORK_ROCKET).contains(item.getType()); }
        default org.bukkit.block.BlockState blockState(Material material, Map<String, Object> data) { throw new IllegalStateException("ITEM_BLOCK_STATE_BRIDGE_NOT_BOUND"); }
        default Map<String, Object> blockStateData(Material material, org.bukkit.block.BlockState state) { throw new IllegalStateException("ITEM_BLOCK_STATE_BRIDGE_NOT_BOUND"); }
    }
    private static volatile NativeBridge nativeBridge;
    public static void bind(NativeBridge bridge) { nativeBridge = Objects.requireNonNull(bridge); }
    private static NativeBridge nativeBridge() {
        NativeBridge bridge = nativeBridge;
        if (bridge == null) throw new IllegalStateException("ITEM_FACTORY_NATIVE_BRIDGE_NOT_BOUND");
        return bridge;
    }
    public static Material modernMaterial(Material material, short data) {
        return material != null && material.isLegacy() ? nativeBridge().fromLegacy(material, data) : material;
    }
    public static Map<String, Object> itemData(ItemStack item) { return nativeBridge().itemData(item); }
    public static ItemStack itemFromData(Map<String, Object> data) { return nativeBridge().itemFromData(data); }
    public static boolean isCrossbowProjectile(ItemStack item) { return nativeBridge().isCrossbowProjectile(item); }
    public static org.bukkit.block.BlockState blockState(Material material, Map<String, Object> data) { return nativeBridge().blockState(material, data); }
    public static Map<String, Object> blockStateData(Material material, org.bukkit.block.BlockState state) { return nativeBridge().blockStateData(material, state); }
    /*
     * Paper's concrete meta implementations often implement Damageable in
     * addition to their specialised interface.  A JDK proxy cannot combine
     * sibling clone() declarations (for example SkullMeta.clone(): SkullMeta
     * and Damageable.clone(): Damageable) unless a single interface redeclares
     * the covariant return.  These marker interfaces keep the proxy ABI
     * compatible with both call sites.
     */
    private interface BaseDamageable extends Damageable { @Override BaseDamageable clone(); }
    private interface FireworkDamageable extends FireworkMeta, Damageable { @Override FireworkDamageable clone(); }
    private interface MapDamageable extends MapMeta, Damageable { @Override MapDamageable clone(); }
    private interface BannerDamageable extends BannerMeta, Damageable { @Override BannerDamageable clone(); }
    private interface CrossbowDamageable extends CrossbowMeta, Damageable { @Override CrossbowDamageable clone(); }

    @Override public ItemMeta getItemMeta(Material material) {
        if (material == null) throw new IllegalArgumentException("Material cannot be null");
        material = modernMaterial(material, (short) 0);
        if (material.isAir()) return null;
        Class<? extends ItemMeta> type = metaType(material);
        if (type == SkullMeta.class) return new CraftMetaSkull();
        if (type == BookMeta.class) return material == Material.WRITTEN_BOOK ? new CraftMetaBookSigned() : new CraftMetaBook();
        if (type == PotionMeta.class) return new CraftMetaPotion();
        if (type == org.bukkit.inventory.meta.ColorableArmorMeta.class) return new CraftMetaColorableArmor();
        if (type == LeatherArmorMeta.class) return new CraftMetaLeatherArmor();
        if (type == org.bukkit.inventory.meta.ArmorMeta.class) return new CraftMetaArmor();
        if (type == EnchantmentStorageMeta.class) return new CraftMetaEnchantedBook();
        if (type == SpawnEggMeta.class) return new CraftMetaSpawnEgg(spawnEggType(material));
        if (type == FireworkMeta.class) return new CraftMetaFirework();
        if (type == org.bukkit.inventory.meta.FireworkEffectMeta.class) return new CraftMetaCharge();
        if (type == MapMeta.class) return new CraftMetaMap();
        if (type == BannerMeta.class) return new CraftMetaBanner();
        if (type == CrossbowMeta.class) return new CraftMetaCrossbow();
        if (type == org.bukkit.inventory.meta.CompassMeta.class) return new CraftMetaCompass();
        if (type == org.bukkit.inventory.meta.BundleMeta.class) return new CraftMetaBundle();
        if (type == org.bukkit.inventory.meta.AxolotlBucketMeta.class) return new CraftMetaAxolotlBucket();
        if (type == org.bukkit.inventory.meta.TropicalFishBucketMeta.class) return new CraftMetaTropicalFishBucket();
        if (type == org.bukkit.inventory.meta.KnowledgeBookMeta.class) return new CraftMetaKnowledgeBook();
        if (type == org.bukkit.inventory.meta.MusicInstrumentMeta.class) return new CraftMetaMusicInstrument();
        if (type == org.bukkit.inventory.meta.SuspiciousStewMeta.class) return new CraftMetaSuspiciousStew();
        if (type == com.destroystokyo.paper.inventory.meta.ArmorStandMeta.class) return new CraftMetaArmorStand();
        if (type == org.bukkit.inventory.meta.BlockStateMeta.class) return new CraftMetaBlockState(material);
        if (material == Material.COD_BUCKET || material == Material.SALMON_BUCKET || material == Material.PUFFERFISH_BUCKET) return new CraftMetaEntityTag();
        return new CraftMetaItem();
    }

    public static Map<String, Object> writeNativeMetadata(ItemMeta meta) {
        if (meta == null) return Map.of();
        if (meta instanceof ItemMetaState concrete) return concrete.nativeMetadata();
        MetaHandler handler = MetaHandler.handler(meta);
        if (handler != null) return handler.common.nativeMetadata();
        throw new UnsupportedOperationException("FOREIGN_ITEM_META_NATIVE_CONVERSION: " + meta.getClass().getName());
    }

    public static ItemMeta readNativeMetadata(Material material, Map<String, Object> metadata) {
        ItemMeta result = new ItemFactoryAdapter().getItemMeta(material);
        // A nonzero Damage on a spawn egg is the pre-flattening entity id.
        // Keep the material-derived type only for modern, damage-free eggs;
        // CraftBukkit's legacy conversion must still be able to remap it.
        if (result instanceof SpawnEggItemMeta && metadata != null
                && metadata.get("Damage") instanceof Number damage && damage.intValue() != 0) {
            result = new CraftMetaSpawnEgg();
        }
        if (result == null) return null;
        return readInto(result, metadata);
    }

    public static ItemMeta readUnmappedNativeMetadata(Map<String, Object> metadata) {
        return readInto(new CraftMetaItem(), metadata);
    }

    private static ItemMeta readInto(ItemMeta result, Map<String, Object> metadata) {
        if (result instanceof ItemMetaState concrete) concrete.nativeMetadata(metadata);
        else MetaHandler.handler(result).common.nativeMetadata(metadata);
        return result;
    }

    private static Class<? extends ItemMeta> metaType(Material material) {
        if (material == null) return ItemMeta.class;
        String name = material.name();
        if (name.endsWith("_HEAD") || name.endsWith("_SKULL") || name.endsWith("_WALL_HEAD") || name.endsWith("_WALL_SKULL")) return SkullMeta.class;
        if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION || material == Material.TIPPED_ARROW) return PotionMeta.class;
        if (material == Material.LEATHER_HORSE_ARMOR) return LeatherArmorMeta.class;
        if (name.startsWith("LEATHER_")) return org.bukkit.inventory.meta.ColorableArmorMeta.class;
        if (name.matches("(?:CHAINMAIL|DIAMOND|GOLDEN|IRON|NETHERITE|TURTLE)_(?:HELMET|CHESTPLATE|LEGGINGS|BOOTS)")) return org.bukkit.inventory.meta.ArmorMeta.class;
        if (material == Material.WRITTEN_BOOK || material == Material.WRITABLE_BOOK) return BookMeta.class;
        if (material == Material.ENCHANTED_BOOK) return EnchantmentStorageMeta.class;
        if (name.endsWith("_SPAWN_EGG")) return SpawnEggMeta.class;
        if (material == Material.FIREWORK_ROCKET) return FireworkMeta.class;
        if (material == Material.FIREWORK_STAR) return org.bukkit.inventory.meta.FireworkEffectMeta.class;
        if (material == Material.FILLED_MAP) return MapMeta.class;
        if (name.endsWith("BANNER")) return BannerMeta.class;
        if (material == Material.CROSSBOW) return CrossbowMeta.class;
        if (material == Material.COMPASS) return org.bukkit.inventory.meta.CompassMeta.class;
        if (material == Material.BUNDLE) return org.bukkit.inventory.meta.BundleMeta.class;
        if (material == Material.AXOLOTL_BUCKET) return org.bukkit.inventory.meta.AxolotlBucketMeta.class;
        if (material == Material.TROPICAL_FISH_BUCKET) return org.bukkit.inventory.meta.TropicalFishBucketMeta.class;
        if (material == Material.KNOWLEDGE_BOOK) return org.bukkit.inventory.meta.KnowledgeBookMeta.class;
        if (material == Material.GOAT_HORN) return org.bukkit.inventory.meta.MusicInstrumentMeta.class;
        if (material == Material.SUSPICIOUS_STEW) return org.bukkit.inventory.meta.SuspiciousStewMeta.class;
        if (material == Material.ARMOR_STAND) return com.destroystokyo.paper.inventory.meta.ArmorStandMeta.class;
        if (material == Material.SHIELD || material == Material.COMPARATOR) return org.bukkit.inventory.meta.BlockStateMeta.class;
        if (name.endsWith("SHULKER_BOX") || Set.of("CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "BARREL", "HOPPER", "DISPENSER", "DROPPER", "FURNACE", "BLAST_FURNACE", "SMOKER", "BREWING_STAND", "BEACON", "BEE_NEST", "BEEHIVE", "SPAWNER", "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK", "REPEATING_COMMAND_BLOCK", "CHISELED_BOOKSHELF", "DECORATED_POT", "SUSPICIOUS_SAND", "SUSPICIOUS_GRAVEL", "JUKEBOX", "LECTERN", "CAMPFIRE", "SOUL_CAMPFIRE", "ENCHANTING_TABLE", "STRUCTURE_BLOCK", "JIGSAW", "SCULK_SENSOR", "CALIBRATED_SCULK_SENSOR", "SCULK_SHRIEKER", "SCULK_CATALYST", "CONDUIT", "BELL", "DAYLIGHT_DETECTOR").contains(name) || name.endsWith("_SIGN")) return org.bukkit.inventory.meta.BlockStateMeta.class;
        return ItemMeta.class;
    }
    private static EntityType spawnEggType(Material material) {
        if (material == null || !material.name().endsWith("_SPAWN_EGG")) return null;
        String name = material.name().substring(0, material.name().length() - "_SPAWN_EGG".length());
        // Bukkit keeps two historical entity names while the item ids use
        // the modern names.  Keep the mapping here so every spawn-egg meta
        // created from a material has the same type as vanilla.
        name = switch (name) {
            case "MOOSHROOM" -> "MUSHROOM_COW";
            case "SNOW_GOLEM" -> "SNOWMAN";
            default -> name;
        };
        try { return EntityType.valueOf(name); }
        catch (IllegalArgumentException ignored) { return null; }
    }
    @Override public boolean isApplicable(ItemMeta meta, ItemStack stack) { return stack != null && isApplicable(meta, stack.getType()); }
    @Override public boolean isApplicable(ItemMeta meta, Material material) {
        material = modernMaterial(material, (short) 0);
        if (meta == null || material == null || material.isAir()) return false;
        requireOwnMeta(meta);
        Class<?> target = metaType(material);
        if (meta.getClass() == CraftMetaItem.class || meta.getClass() == ItemMetaState.class) return true;
        if (meta instanceof CraftMetaEntityTag) return material == Material.COD_BUCKET || material == Material.SALMON_BUCKET || material == Material.PUFFERFISH_BUCKET;
        if (meta instanceof CraftMetaBlockState block) return block.material() == material;
        if (meta instanceof org.bukkit.inventory.meta.ColorableArmorMeta) return target == org.bukkit.inventory.meta.ColorableArmorMeta.class;
        if (meta instanceof LeatherArmorMeta) return target == LeatherArmorMeta.class || target == org.bukkit.inventory.meta.ColorableArmorMeta.class;
        return target != ItemMeta.class && target.isInstance(meta);
    }
    private static void requireOwnMeta(ItemMeta meta) {
        if (!(meta instanceof ItemMetaState) && MetaHandler.handler(meta) == null)
            throw new IllegalArgumentException("ItemMeta was not created by this item factory: " + meta.getClass().getName());
    }
    @Override public boolean equals(ItemMeta first, ItemMeta second) {
        if (first == second) return true;
        if (first == null || second == null) {
            ItemMeta value = first == null ? second : first;
            MetaHandler handler = MetaHandler.handler(value);
            return handler == null ? writeNativeMetadata(value).isEmpty() : handler.isEmpty();
        }
        return Objects.equals(first, second);
    }
    @Override public ItemMeta asMetaFor(ItemMeta meta, ItemStack stack) {
        if (meta == null || stack == null) throw new IllegalArgumentException("Item meta and stack are required");
        return asMetaFor(meta, stack.getType());
    }
    @Override public ItemMeta asMetaFor(ItemMeta meta, Material material) {
        if (meta == null || material == null) throw new IllegalArgumentException("Item meta and material are required");
        material = modernMaterial(material, (short) 0);
        requireOwnMeta(meta);
        if (meta instanceof BookItemMeta book && metaType(material) == BookMeta.class) return book.forWritten(material == Material.WRITTEN_BOOK);
        if (material.isAir()) return null;
        if (isApplicable(meta, material) && metaType(material).isInstance(meta)) return meta;
        Map<String, Object> values = writeNativeMetadata(meta);
        removeSpecialTags(meta, material, values);
        if (meta instanceof SkullMeta && metaType(material) != SkullMeta.class) { values.remove("SkullOwner"); values.remove("SkullProfile"); }
        if (meta instanceof BookMeta && metaType(material) != BookMeta.class) for (String key : List.of("title", "author", "pages", "resolved", "generation", "filtered_title", "filtered_pages")) values.remove(key);
        if (meta instanceof PotionMeta && metaType(material) != PotionMeta.class) for (String key : List.of("Potion", "CustomPotionEffects", "CustomPotionColor")) values.remove(key);
        if (meta instanceof EnchantmentStorageMeta && metaType(material) != EnchantmentStorageMeta.class) values.remove("StoredEnchantments");
        if (meta instanceof SpawnEggMeta && metaType(material) != SpawnEggMeta.class) values.remove("EntityTag");
        if (meta instanceof LeatherArmorMeta && !LeatherArmorMeta.class.isAssignableFrom(metaType(material)) && values.get("display") instanceof Map<?, ?> display) {
            Map<String, Object> copy = ItemNbtData.copy(ItemNbtData.compound(display)); copy.remove("color");
            if (copy.isEmpty()) values.remove("display"); else values.put("display", copy);
        }
        return readNativeMetadata(material, values);
    }
    private static void removeSpecialTags(ItemMeta meta, Material material, Map<String, Object> values) {
        Class<?> target = metaType(material);
        if (meta instanceof CrossbowMeta && target != CrossbowMeta.class) { values.remove("Charged"); values.remove("ChargedProjectiles"); }
        if (meta instanceof FireworkMeta && target != FireworkMeta.class) values.remove("Fireworks");
        if (meta instanceof org.bukkit.inventory.meta.FireworkEffectMeta && target != org.bukkit.inventory.meta.FireworkEffectMeta.class) values.remove("Explosion");
        if (meta instanceof org.bukkit.inventory.meta.BundleMeta && target != org.bukkit.inventory.meta.BundleMeta.class) values.remove("Items");
        if (meta instanceof org.bukkit.inventory.meta.ArmorMeta && !org.bukkit.inventory.meta.ArmorMeta.class.isAssignableFrom(target)) values.remove("Trim");
        if (meta instanceof org.bukkit.inventory.meta.CompassMeta && target != org.bukkit.inventory.meta.CompassMeta.class) for (String key : List.of("LodestoneDimension", "LodestonePos", "LodestoneTracked")) values.remove(key);
        if (meta instanceof org.bukkit.inventory.meta.AxolotlBucketMeta && target != org.bukkit.inventory.meta.AxolotlBucketMeta.class) values.remove("Variant");
        if (meta instanceof org.bukkit.inventory.meta.TropicalFishBucketMeta && target != org.bukkit.inventory.meta.TropicalFishBucketMeta.class) values.remove("BucketVariantTag");
        if (meta instanceof org.bukkit.inventory.meta.KnowledgeBookMeta && target != org.bukkit.inventory.meta.KnowledgeBookMeta.class) values.remove("Recipes");
        if (meta instanceof org.bukkit.inventory.meta.MusicInstrumentMeta && target != org.bukkit.inventory.meta.MusicInstrumentMeta.class) values.remove("instrument");
        if (meta instanceof org.bukkit.inventory.meta.SuspiciousStewMeta && target != org.bukkit.inventory.meta.SuspiciousStewMeta.class) values.remove("Effects");
        if (meta instanceof MapMeta && target != MapMeta.class) {
            values.remove("map"); values.remove("map_is_scaling");
            removeCompoundFields(values, "display", "MapColor", "LocName");
        }
        if (meta instanceof BannerMeta && target != BannerMeta.class) removeCompoundFields(values, "BlockEntityTag", "Patterns", "Base");
        if (meta instanceof com.destroystokyo.paper.inventory.meta.ArmorStandMeta && target != com.destroystokyo.paper.inventory.meta.ArmorStandMeta.class) removeCompoundFields(values, "EntityTag", "Invisible", "NoBasePlate", "ShowArms", "Small", "Marker");
    }
    private static void removeCompoundFields(Map<String, Object> values, String key, String... fields) {
        if (!(values.get(key) instanceof Map<?, ?>)) return;
        Map<String, Object> compound = ItemNbtData.copy(ItemNbtData.compound(values.get(key)));
        for (String field : fields) compound.remove(field);
        if (compound.isEmpty()) values.remove(key); else values.put(key, compound);
    }
    @Override public Color getDefaultLeatherColor() { return Color.fromRGB(0xA06540); }
    @Override public ItemStack createItemStack(String input) {
        return nativeBridge().createItemStack(Objects.requireNonNull(input, "input"));
    }
    @Override public Material updateMaterial(ItemMeta meta, Material material) {
        requireOwnMeta(Objects.requireNonNull(meta, "meta"));
        return meta instanceof SpawnEggItemMeta egg ? egg.updateMaterial(material) : material;
    }
    @Override public Material getSpawnEgg(EntityType type) {
        return nativeBridge().getSpawnEgg(Objects.requireNonNull(type, "type"));
    }
    @Override public ItemStack enchantWithLevels(ItemStack stack, int levels, boolean allowTreasure, Random random) {
        if (stack == null) throw new IllegalArgumentException("stack");
        if (stack.isEmpty() || stack.getAmount() <= 0) throw new IllegalArgumentException("ItemStack must not be empty");
        if (levels < 1 || levels > 30) throw new IllegalArgumentException("levels must be in range [1, 30]");
        if (random == null) throw new IllegalArgumentException("random");
        return nativeBridge().enchantWithLevels(stack, levels, allowTreasure, random);
    }
    @Override public net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowItem> asHoverEvent(ItemStack stack, java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowItem> renderer) {
        if (stack == null) throw new IllegalArgumentException("stack");
        Presentation item = nativeBridge().presentation(stack);
        var tag = item.metadataSnbt() == null ? null : net.kyori.adventure.nbt.api.BinaryTagHolder.binaryTagHolder(item.metadataSnbt());
        var value = net.kyori.adventure.text.event.HoverEvent.ShowItem.showItem(net.kyori.adventure.key.Key.key(item.namespace(), item.key()), item.count(), tag);
        return net.kyori.adventure.text.event.HoverEvent.showItem(Objects.requireNonNull(renderer, "renderer").apply(value));
    }
    @Override public Component displayName(ItemStack stack) {
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(nativeBridge().presentation(Objects.requireNonNull(stack)).displayNameJson());
    }
    @Override public String getI18NDisplayName(ItemStack stack) { return nativeBridge().presentation(Objects.requireNonNull(stack)).localizedName(); }
    @Override public ItemStack ensureServerConversions(ItemStack stack) {
        if (stack == null) throw new IllegalArgumentException("stack");
        return nativeBridge().ensureServerConversions(stack);
    }
    @Override public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(ItemStack stack) {
        if (stack == null) throw new IllegalArgumentException("stack");
        Presentation item = nativeBridge().presentation(stack);
        return new net.md_5.bungee.api.chat.hover.content.Item(item.namespace() + ":" + item.key(), item.count(),
                net.md_5.bungee.api.chat.ItemTag.ofNbt(item.metadataSnbt() == null ? "{}" : item.metadataSnbt()));
    }
    @Override public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity) {
        if (entity == null) throw new IllegalArgumentException("entity");
        return hoverContentOf(entity, entity.getCustomName());
    }
    @Override public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity, String text) {
        if (entity == null) throw new IllegalArgumentException("entity");
        return hoverContentOf(entity, text == null || text.isBlank() ? null : new net.md_5.bungee.api.chat.TextComponent(text));
    }
    @Override public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity, net.md_5.bungee.api.chat.BaseComponent component) {
        Objects.requireNonNull(entity, "entity");
        return new net.md_5.bungee.api.chat.hover.content.Entity(entity.getType().getKey().toString(), entity.getUniqueId().toString(), component);
    }
    @Override public net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity entity, net.md_5.bungee.api.chat.BaseComponent[] components) {
        return hoverContentOf(entity, components == null ? null : new net.md_5.bungee.api.chat.TextComponent(components));
    }

    private static final class MetaHandler implements InvocationHandler {
        private ItemMetaState common = new ItemMetaState();
        private Class<?> metaType = ItemMeta.class;

        static ItemMeta create(Class<?> type) { return create(type, new MetaHandler()); }
        static ItemMeta create(Class<?> type, MetaHandler handler) {
            handler.metaType = type;
            // A large number of plugins cast every ItemMeta to Damageable (and
            // occasionally Repairable) before checking the material.  Bukkit's
            // concrete metas implement these interfaces selectively; exposing
            // them on the lightweight proxy is harmless and keeps those casts
            // ABI-compatible for the facade.
            return (ItemMeta) Proxy.newProxyInstance(
                    ItemMeta.class.getClassLoader(),
                    new Class<?>[]{proxyType(type)}, handler);
        }
        private static Class<?> proxyType(Class<?> type) {
            if (type == FireworkMeta.class) return FireworkDamageable.class;
            if (type == MapMeta.class) return MapDamageable.class;
            if (type == BannerMeta.class) return BannerDamageable.class;
            if (type == CrossbowMeta.class) return CrossbowDamageable.class;
            return BaseDamageable.class;
        }
        private static MetaHandler handler(Object proxy) {
            if (proxy == null || !Proxy.isProxyClass(proxy.getClass())) return null;
            return Proxy.getInvocationHandler(proxy) instanceof MetaHandler value ? value : null;
        }
        private MetaHandler copy() {
            MetaHandler copy = new MetaHandler();
            copy.common = common.clone();
            copy.metaType = metaType;
            return copy;
        }
        private boolean isEmpty() {
            return common.nativeMetadata().isEmpty();
        }
        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (!Set.of("clone", "serialize", "equals", "hashCode", "toString").contains(name)) {
                try {
                    Method commonMethod = ItemMetaState.class.getMethod(name, method.getParameterTypes());
                    try { return commonMethod.invoke(common, args); }
                    catch (java.lang.reflect.InvocationTargetException failure) { throw failure.getCause(); }
                } catch (NoSuchMethodException ignored) { }
            }
            switch (name) {
                case "toString" -> { return "MeldriteItemMeta" + serialize(); }
                case "hashCode" -> { return serialize().hashCode(); }
                case "equals" -> {
                    if (args == null || args.length != 1 || !(args[0] instanceof ItemMeta other)) return false;
                    MetaHandler otherHandler = handler(other);
                    return otherHandler != null && serialize().equals(otherHandler.serialize());
                }
                case "clone" -> { return create(metaType, copy()); }
                case "serialize" -> { return serialize(); }
                default -> { throw new UnsupportedOperationException("UNSUPPORTED_ITEM_META_OPERATION: " + method.toGenericString()); }
            }
        }
        private Map<String, Object> serialize() {
            Map<String, Object> result = new LinkedHashMap<>(common.serialize());
            return result;
        }
    }
}
