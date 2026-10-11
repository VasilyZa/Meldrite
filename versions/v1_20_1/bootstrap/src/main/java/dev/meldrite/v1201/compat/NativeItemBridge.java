package dev.meldrite.v1201.compat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import dev.meldrite.paper.facade.CraftItemStackAdapter;
import dev.meldrite.paper.facade.ItemFactoryAdapter;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.minecraft.command.argument.ItemStringReader;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

/** Native item parser, enchantment table, presentation and conversion binding. */
public final class NativeItemBridge implements CraftItemStackAdapter.Bridge, ItemFactoryAdapter.NativeBridge {
    public static void bind() {
        NativeItemBridge bridge = new NativeItemBridge();
        CraftItemStackAdapter.bind((CraftItemStackAdapter.Bridge) bridge);
        ItemFactoryAdapter.bind(bridge);
    }

    @Override public ItemStack toNative(org.bukkit.inventory.ItemStack stack) {
        if (stack instanceof CraftItemStackAdapter.NativeBacked nativeStack) return (ItemStack) nativeStack.nativeCopy();
        if (stack == null || stack.getType() == Material.LEGACY_AIR || stack.getAmount() <= 0) return ItemStack.EMPTY;
        Material material = stack.getType();
        Item item = material.isLegacy() ? null : item(material);
        if (item == net.minecraft.item.Items.AIR) return ItemStack.EMPTY;
        short damage = stack.getDurability();
        NbtCompound metadata = NativeItemMetadata.encode(ItemFactoryAdapter.writeNativeMetadata(stack.getItemMeta()));
        if (material.isLegacy()) return legacyStack(material, damage, stack.getAmount(), metadata);
        ItemStack result = new ItemStack(item, stack.getAmount());
        if (!metadata.isEmpty()) result.setNbt(metadata);
        return result;
    }

    @Override public org.bukkit.inventory.ItemStack toBukkit(Object source) {
        ItemStack stack = source == null ? ItemStack.EMPTY : ItemStack.class.cast(source);
        if (stack.isEmpty()) return new org.bukkit.inventory.ItemStack(Material.AIR);
        Material material = material(stack.getItem());
        if (material == null) return CraftItemStack.asCraftMirror(stack.copy());
        org.bukkit.inventory.ItemStack result = new org.bukkit.inventory.ItemStack(material, stack.getCount());
        if (stack.hasNbt()) result.setItemMeta(ItemFactoryAdapter.readNativeMetadata(material, NativeItemMetadata.decode(stack.getNbt())));
        return result;
    }

    @Override public CraftItemStack createItemStack(String input) {
        try {
            var parsed = ItemStringReader.item(Registries.ITEM.getReadOnlyWrapper(), new StringReader(input));
            ItemStack stack = new ItemStack(parsed.item());
            if (parsed.nbt() != null) stack.setNbt(parsed.nbt());
            return CraftItemStack.asCraftMirror(stack);
        } catch (CommandSyntaxException failure) {
            throw new IllegalArgumentException("Could not parse ItemStack: " + input, failure);
        }
    }

    @Override public CraftItemStack enchantWithLevels(org.bukkit.inventory.ItemStack original, int levels, boolean treasure, java.util.Random random) {
        ItemStack stack = CraftItemStack.asNMSCopy(original);
        if (stack.getNbt() != null) stack.getNbt().remove(ItemStack.ENCHANTMENTS_KEY);
        return CraftItemStack.asCraftMirror(EnchantmentHelper.enchant(new JavaRandom(random), stack, levels, treasure));
    }
    @Override public CraftItemStack ensureServerConversions(org.bukkit.inventory.ItemStack stack) { return CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(stack)); }
    @Override public java.util.Map<String, Object> itemData(org.bukkit.inventory.ItemStack stack) {
        return NativeItemMetadata.decode(toNative(stack).writeNbt(new NbtCompound()));
    }
    @Override public org.bukkit.inventory.ItemStack itemFromData(java.util.Map<String, Object> data) {
        NbtCompound tag = NativeItemMetadata.encode(data);
        Identifier id = Identifier.tryParse(tag.getString("id"));
        if (id == null || !Registries.ITEM.containsId(id)) throw new IllegalArgumentException("UNKNOWN_NESTED_NATIVE_ITEM: " + tag.getString("id"));
        return CraftItemStack.asCraftMirror(ItemStack.fromNbt(tag));
    }
    @Override public boolean isCrossbowProjectile(org.bukkit.inventory.ItemStack stack) {
        var item = toNative(stack).getItem();
        return item instanceof net.minecraft.item.ArrowItem || item instanceof net.minecraft.item.FireworkRocketItem;
    }
    @Override public org.bukkit.block.BlockState blockState(Material material, java.util.Map<String, Object> data) {
        if (material == Material.SHIELD) {
            Object base = data.get("Base");
            var color = base instanceof Number number ? org.bukkit.DyeColor.getByWoolData(number.byteValue()) : org.bukkit.DyeColor.WHITE;
            if (color == null) throw new IllegalArgumentException("Unknown shield base color: " + base);
            material = Material.valueOf(color.name() + "_BANNER");
        }
        var state = NativeBlockData.block(material).getDefaultState();
        if (!(state.getBlock() instanceof net.minecraft.block.BlockEntityProvider provider)) throw new IllegalArgumentException("Material has no block entity: " + material);
        var entity = provider.createBlockEntity(net.minecraft.util.math.BlockPos.ORIGIN, state);
        if (entity == null) throw new IllegalStateException("Could not create block entity for " + material);
        NbtCompound original = NativeItemMetadata.encode(data);
        entity.readNbt(original);
        if (entity instanceof NativeBlockEntityPersistentDataAccess access && data.containsKey("BukkitValues")) access.meldrite$loadPersistentData(NativeItemMetadata.encode(dev.meldrite.paper.facade.ItemNbtData.compound(data.get("BukkitValues"))));
        var result = org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState.createUnplaced(state, entity);
        if (result instanceof org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState<?> craft) craft.preserveUnhandledItemData(original);
        return result;
    }
    @Override public java.util.Map<String, Object> blockStateData(Material material, org.bukkit.block.BlockState state) {
        if (material == Material.SHIELD && state instanceof org.bukkit.craftbukkit.v1_20_R1.block.CraftBanner banner) {
            var data = NativeItemMetadata.decode(banner.getSnapshotNBT());
            data.put("Base", (int) banner.getBaseColor().getWoolData());
            return data;
        }
        if (!(state instanceof org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState<?> craft) || craft.getType() != material) throw new IllegalArgumentException("Block state does not match item material " + material);
        return NativeItemMetadata.decode(craft.getSnapshotNBT());
    }
    @Override public ItemFactoryAdapter.Presentation presentation(org.bukkit.inventory.ItemStack original) {
        ItemStack stack = CraftItemStack.asNMSCopy(original);
        Identifier id = Registries.ITEM.getId(stack.getItem());
        return new ItemFactoryAdapter.Presentation(id.getNamespace(), id.getPath(), stack.getCount(),
                stack.getNbt() == null ? null : stack.getNbt().toString(), Text.Serializer.toJson(stack.toHoverableText()),
                Language.getInstance().get(stack.getItem().getTranslationKey(stack)));
    }
    @Override public Material getSpawnEgg(EntityType type) {
        if (type == EntityType.UNKNOWN) return null;
        Identifier key = new Identifier(type.getKey().getNamespace(), type.getKey().getKey());
        var nativeType = Registries.ENTITY_TYPE.getOrEmpty(key).orElseThrow(() -> new IllegalArgumentException("UNKNOWN_ENTITY_TYPE: " + key));
        SpawnEggItem egg = SpawnEggItem.forEntity(nativeType);
        if (egg == null) return null;
        Material result = material(egg);
        if (result == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_SPAWN_EGG: " + Registries.ITEM.getId(egg));
        return result;
    }
    @Override public Material fromLegacy(Material material, short damage) {
        if (!material.isLegacy()) return material;
        if (material == Material.LEGACY_AIR) return Material.AIR;
        ItemStack stack = legacyStack(material, damage, 1, new NbtCompound());
        Material result = material(stack.getItem());
        if (result == null) throw new UnsupportedOperationException("UNREPRESENTABLE_LEGACY_MATERIAL: " + material + ":" + damage);
        return result;
    }

    private static ItemStack legacyStack(Material material, short damage, int count, NbtCompound metadata) {
        // CraftLegacy skips numeric spawn eggs: the vanilla fixer cannot read their old entity choice.
        if (material == Material.LEGACY_MONSTER_EGG) {
            EntityType type = damage == 0 ? EntityType.PIG : EntityType.fromId(damage & 0xff);
            if (type == null || type == EntityType.UNKNOWN) throw new IllegalArgumentException("UNKNOWN_LEGACY_SPAWN_EGG: " + damage);
            var key = type.getKey();
            var nativeType = Registries.ENTITY_TYPE.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("UNKNOWN_LEGACY_ENTITY: " + type));
            SpawnEggItem egg = SpawnEggItem.forEntity(nativeType);
            if (egg == null) throw new IllegalArgumentException("LEGACY_ENTITY_HAS_NO_SPAWN_EGG: " + type);
            ItemStack result = new ItemStack(egg, count);
            if (!metadata.isEmpty()) result.setNbt(metadata.copy());
            return result;
        }
        NbtCompound old = new NbtCompound();
        old.putInt("id", material.getId()); old.putShort("Damage", damage); old.putByte("Count", (byte) 1);
        old.put("tag", metadata.copy());
        NbtCompound updated = (NbtCompound) Schemas.getFixer().update(TypeReferences.ITEM_STACK,
                new Dynamic<NbtElement>(NbtOps.INSTANCE, old), -1, SharedConstants.getGameVersion().getSaveVersion().getId()).getValue();
        Identifier id = Identifier.tryParse(updated.getString("id"));
        if (id == null || !Registries.ITEM.containsId(id) || id.toString().equals("minecraft:air")) {
            if (!material.isBlock()) throw new IllegalArgumentException("UNMAPPABLE_LEGACY_ITEM: " + material + ":" + damage);
            var state = Schemas.getFixer().update(TypeReferences.BLOCK_STATE, BlockStateFlattening.lookupState(material.getId() << 4 | damage & 15),
                    100, SharedConstants.getGameVersion().getSaveVersion().getId());
            id = Identifier.tryParse(state.get("Name").asString(""));
            if (id == null || !Registries.BLOCK.containsId(id)) throw new IllegalArgumentException("UNMAPPABLE_LEGACY_BLOCK: " + material + ":" + damage);
            Item item = Registries.BLOCK.get(id).asItem();
            if (item == net.minecraft.item.Items.AIR) throw new IllegalArgumentException("LEGACY_BLOCK_HAS_NO_ITEM: " + material);
            updated.putString("id", Registries.ITEM.getId(item).toString());
        }
        ItemStack result = ItemStack.fromNbt(updated);
        if (result.isEmpty()) throw new IllegalArgumentException("UNMAPPABLE_LEGACY_ITEM: " + material + ":" + damage);
        result.setCount(count);
        return result;
    }
    private static Item requireItem(Identifier id) { return Registries.ITEM.getOrEmpty(id).orElseThrow(() -> new IllegalArgumentException("UNKNOWN_NATIVE_ITEM: " + id)); }
    public static Item item(Material material) {
        var key = material.getKey();
        Identifier id = new Identifier(key.getNamespace(), key.getKey());
        return Registries.ITEM.getOrEmpty(id).orElseGet(() -> Registries.BLOCK.getOrEmpty(id)
                .orElseThrow(() -> new IllegalArgumentException("UNKNOWN_NATIVE_MATERIAL: " + id)).asItem());
    }
    public static Material material(Item item) {
        return NativeMaterialProjection.material(item);
    }

    static final class JavaRandom implements net.minecraft.util.math.random.Random {
        private final java.util.Random random;
        JavaRandom(java.util.Random random) { this.random = Objects.requireNonNull(random); }
        @Override public net.minecraft.util.math.random.Random split() { throw new UnsupportedOperationException("Java Random cannot split"); }
        @Override public net.minecraft.util.math.random.RandomSplitter nextSplitter() { throw new UnsupportedOperationException("Java Random cannot provide a splitter"); }
        @Override public void setSeed(long seed) { random.setSeed(seed); }
        @Override public int nextInt() { return random.nextInt(); }
        @Override public int nextInt(int bound) { return random.nextInt(bound); }
        @Override public long nextLong() { return random.nextLong(); }
        @Override public boolean nextBoolean() { return random.nextBoolean(); }
        @Override public float nextFloat() { return random.nextFloat(); }
        @Override public double nextDouble() { return random.nextDouble(); }
        @Override public double nextGaussian() { return random.nextGaussian(); }
    }
}
