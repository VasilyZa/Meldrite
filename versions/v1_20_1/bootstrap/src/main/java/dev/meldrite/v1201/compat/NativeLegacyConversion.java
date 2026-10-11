package dev.meldrite.v1201.compat;

import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.BlockStateFlattening;
import net.minecraft.datafixer.fix.ItemIdFix;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;

/** CraftLegacy's numeric conversion tables, populated through Minecraft's actual data fixer. */
@SuppressWarnings("deprecation")
public final class NativeLegacyConversion {
    private record LegacyKey(Material material, byte data) {}
    private record Tables(Map<LegacyKey, Item> items, Map<LegacyKey, BlockState> states,
                          Map<Item, LegacyKey> reverseItems, Map<Block, LegacyKey> reverseBlocks,
                          Map<BlockState, LegacyKey> reverseStates) {}
    private static final class Holder { private static final Tables TABLES = build(); }
    private NativeLegacyConversion() {}
    public static Material fromLegacy(MaterialData data, boolean itemPriority) {
        Material material = java.util.Objects.requireNonNull(data).getItemType();
        if (!material.isLegacy()) return material;
        if (material == Material.LEGACY_AIR) return Material.AIR;
        var key = new LegacyKey(material, data.getData()); var tables = Holder.TABLES;
        Item item = tables.items.get(key); BlockState state = tables.states.get(key);
        if (itemPriority && item != null) return NativeMaterialProjection.material(item);
        if (state != null) return NativeMaterialProjection.material(state.getBlock());
        if (!itemPriority && item != null) return NativeMaterialProjection.material(item);
        throw new IllegalArgumentException("UNMAPPABLE_LEGACY_MATERIAL: " + material + ":" + (data.getData() & 255));
    }
    public static Item item(Material material, short data) {
        if (!material.isLegacy()) return NativeMaterialProjection.item(material);
        if (material == Material.LEGACY_AIR) return Items.AIR;
        var key = new LegacyKey(material, (byte) data); var tables = Holder.TABLES;
        Item item = tables.items.get(key);
        if (item != null) return item;
        BlockState state = tables.states.get(key);
        if (state != null && state.getBlock().asItem() != Items.AIR) return state.getBlock().asItem();
        throw new IllegalArgumentException("UNMAPPABLE_LEGACY_ITEM: " + material + ":" + (data & 255));
    }
    public static BlockState block(Material material, byte data) {
        if (!material.isLegacy()) { MaterialData legacy = toLegacyData(material); return block(legacy.getItemType(), data); }
        if (material == Material.LEGACY_AIR) return Blocks.AIR.getDefaultState();
        var result = Holder.TABLES.states.get(new LegacyKey(material, data));
        if (result == null) throw new IllegalArgumentException("UNMAPPABLE_LEGACY_BLOCK: " + material + ":" + (data & 255));
        return result;
    }
    public static MaterialData toLegacyData(Material material) {
        java.util.Objects.requireNonNull(material);
        if (material.isLegacy()) return new MaterialData(material);
        if (material == Material.AIR) return new MaterialData(Material.LEGACY_AIR);
        var tables = Holder.TABLES; LegacyKey key = null;
        Block block = NativeMaterialProjection.block(material);
        if (block != null) {
            key = tables.reverseStates.get(block.getDefaultState());
            if (key == null) key = tables.reverseBlocks.get(block);
            if (key == null) key = tables.reverseItems.get(block.asItem());
        } else key = tables.reverseItems.get(NativeMaterialProjection.item(material));
        return requireLegacy(key, material.toString());
    }
    public static MaterialData toLegacyData(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
            return new MaterialData(Material.LEGACY_AIR);
        }
        var tables = Holder.TABLES;
        LegacyKey key = tables.reverseStates.get(state);
        if (key == null) key = tables.reverseBlocks.get(state.getBlock());
        return requireLegacy(key, state.toString());
    }
    private static MaterialData requireLegacy(LegacyKey key, String description) {
        if (key == null) throw new IllegalArgumentException("MATERIAL_HAS_NO_LEGACY_REPRESENTATION: " + description);
        return new MaterialData(key.material, key.data);
    }
    private static Tables build() {
        var items = new HashMap<LegacyKey, Item>(); var states = new HashMap<LegacyKey, BlockState>();
        var reverseItems = new HashMap<Item, LegacyKey>(); var reverseBlocks = new HashMap<Block, LegacyKey>();
        var reverseStates = new HashMap<BlockState, LegacyKey>();
        int version = SharedConstants.getGameVersion().getSaveVersion().getId();
        for (Material material : Material.values()) {
            if (!material.isLegacy() || material == Material.LEGACY_AIR) continue;
            if (material.isBlock()) for (byte data = 0; data < 16; data++) {
                var converted = Schemas.getFixer().update(TypeReferences.BLOCK_STATE,
                        BlockStateFlattening.lookupState(material.getId() << 4 | data), 100, version);
                Identifier id = Identifier.tryParse(converted.get("Name").asString(""));
                if (id == null || !Registries.BLOCK.containsId(id) || Registries.BLOCK.get(id) == Blocks.AIR) continue;
                NbtCompound tag = (NbtCompound) converted.convert(NbtOps.INSTANCE).getValue();
                BlockState state = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), tag);
                var key = new LegacyKey(material, data);
                states.put(key, state); reverseStates.putIfAbsent(state, key); reverseBlocks.putIfAbsent(state.getBlock(), key);
            }
            if (material == Material.LEGACY_MONSTER_EGG) continue;
            if (ItemIdFix.fromId(material.getId()) == null) continue;
            int max = material.getMaxDurability() == 0 ? 16 : 1;
            for (byte data = 0; data < max; data++) {
                var old = new NbtCompound(); old.putInt("id", material.getId()); old.putShort("Damage", data);
                old.put("tag", new NbtCompound());
                var converted = Schemas.getFixer().update(TypeReferences.ITEM_STACK, new Dynamic<NbtElement>(NbtOps.INSTANCE, old), -1, version);
                Identifier id = Identifier.tryParse(converted.get("id").asString(""));
                if (id == null) continue;
                Item item = Registries.ITEM.getOrEmpty(id).orElse(null);
                if (item == null || item == Items.AIR) continue;
                var key = new LegacyKey(material, data); items.put(key, item); reverseItems.putIfAbsent(item, key);
            }
        }
        for (EntityType type : EntityType.values()) {
            if (type == EntityType.UNKNOWN || type.getTypeId() < 0 || type.getTypeId() > 120) continue;
            var nativeType = Registries.ENTITY_TYPE.getOrEmpty(new Identifier(type.getKey().getNamespace(), type.getKey().getKey())).orElse(null);
            SpawnEggItem egg = nativeType == null ? null : SpawnEggItem.forEntity(nativeType);
            if (egg == null) continue;
            var key = new LegacyKey(Material.LEGACY_MONSTER_EGG, (byte) type.getTypeId()); items.put(key, egg); reverseItems.put(egg, key);
        }
        items.put(new LegacyKey(Material.LEGACY_MONSTER_EGG, (byte) 0), Items.PIG_SPAWN_EGG);
        return new Tables(Map.copyOf(items), Map.copyOf(states), Map.copyOf(reverseItems), Map.copyOf(reverseBlocks), Map.copyOf(reverseStates));
    }
}
