package org.bukkit.craftbukkit.v1_20_R1;

import java.util.Objects;
import net.minecraft.registry.Registries;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.util.Identifier;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;

/** Bukkit's historical statistic names mapped onto the server's actual registries. */
public enum CraftStatistic {
    DAMAGE_DEALT("damage_dealt"),
    DAMAGE_TAKEN("damage_taken"),
    DEATHS("deaths"),
    MOB_KILLS("mob_kills"),
    PLAYER_KILLS("player_kills"),
    FISH_CAUGHT("fish_caught"),
    ANIMALS_BRED("animals_bred"),
    LEAVE_GAME("leave_game"),
    JUMP("jump"),
    DROP_COUNT("drop"),
    DROP("dropped"),
    PICKUP("picked_up"),
    PLAY_ONE_MINUTE("play_time"),
    TOTAL_WORLD_TIME("total_world_time"),
    WALK_ONE_CM("walk_one_cm"),
    WALK_ON_WATER_ONE_CM("walk_on_water_one_cm"),
    FALL_ONE_CM("fall_one_cm"),
    SNEAK_TIME("sneak_time"),
    CLIMB_ONE_CM("climb_one_cm"),
    FLY_ONE_CM("fly_one_cm"),
    WALK_UNDER_WATER_ONE_CM("walk_under_water_one_cm"),
    MINECART_ONE_CM("minecart_one_cm"),
    BOAT_ONE_CM("boat_one_cm"),
    PIG_ONE_CM("pig_one_cm"),
    HORSE_ONE_CM("horse_one_cm"),
    SPRINT_ONE_CM("sprint_one_cm"),
    CROUCH_ONE_CM("crouch_one_cm"),
    AVIATE_ONE_CM("aviate_one_cm"),
    MINE_BLOCK("mined"),
    USE_ITEM("used"),
    BREAK_ITEM("broken"),
    CRAFT_ITEM("crafted"),
    KILL_ENTITY("killed"),
    ENTITY_KILLED_BY("killed_by"),
    TIME_SINCE_DEATH("time_since_death"),
    TALKED_TO_VILLAGER("talked_to_villager"),
    TRADED_WITH_VILLAGER("traded_with_villager"),
    CAKE_SLICES_EATEN("eat_cake_slice"),
    CAULDRON_FILLED("fill_cauldron"),
    CAULDRON_USED("use_cauldron"),
    ARMOR_CLEANED("clean_armor"),
    BANNER_CLEANED("clean_banner"),
    BREWINGSTAND_INTERACTION("interact_with_brewingstand"),
    BEACON_INTERACTION("interact_with_beacon"),
    DROPPER_INSPECTED("inspect_dropper"),
    HOPPER_INSPECTED("inspect_hopper"),
    DISPENSER_INSPECTED("inspect_dispenser"),
    NOTEBLOCK_PLAYED("play_noteblock"),
    NOTEBLOCK_TUNED("tune_noteblock"),
    FLOWER_POTTED("pot_flower"),
    TRAPPED_CHEST_TRIGGERED("trigger_trapped_chest"),
    ENDERCHEST_OPENED("open_enderchest"),
    ITEM_ENCHANTED("enchant_item"),
    RECORD_PLAYED("play_record"),
    FURNACE_INTERACTION("interact_with_furnace"),
    CRAFTING_TABLE_INTERACTION("interact_with_crafting_table"),
    CHEST_OPENED("open_chest"),
    SLEEP_IN_BED("sleep_in_bed"),
    SHULKER_BOX_OPENED("open_shulker_box"),
    TIME_SINCE_REST("time_since_rest"),
    SWIM_ONE_CM("swim_one_cm"),
    DAMAGE_DEALT_ABSORBED("damage_dealt_absorbed"),
    DAMAGE_DEALT_RESISTED("damage_dealt_resisted"),
    DAMAGE_BLOCKED_BY_SHIELD("damage_blocked_by_shield"),
    DAMAGE_ABSORBED("damage_absorbed"),
    DAMAGE_RESISTED("damage_resisted"),
    CLEAN_SHULKER_BOX("clean_shulker_box"),
    OPEN_BARREL("open_barrel"),
    INTERACT_WITH_BLAST_FURNACE("interact_with_blast_furnace"),
    INTERACT_WITH_SMOKER("interact_with_smoker"),
    INTERACT_WITH_LECTERN("interact_with_lectern"),
    INTERACT_WITH_CAMPFIRE("interact_with_campfire"),
    INTERACT_WITH_CARTOGRAPHY_TABLE("interact_with_cartography_table"),
    INTERACT_WITH_LOOM("interact_with_loom"),
    INTERACT_WITH_STONECUTTER("interact_with_stonecutter"),
    BELL_RING("bell_ring"),
    RAID_TRIGGER("raid_trigger"),
    RAID_WIN("raid_win"),
    INTERACT_WITH_ANVIL("interact_with_anvil"),
    INTERACT_WITH_GRINDSTONE("interact_with_grindstone"),
    TARGET_HIT("target_hit"),
    INTERACT_WITH_SMITHING_TABLE("interact_with_smithing_table"),
    STRIDER_ONE_CM("strider_one_cm");

    private final Identifier key;
    private final Statistic bukkit;
    CraftStatistic(String key) { this.key = new Identifier("minecraft", key); this.bukkit = Statistic.valueOf(name()); }

    public static Statistic getBukkitStatistic(Stat<?> stat) {
        Objects.requireNonNull(stat, "statistic");
        var key = stat.getType() == Stats.CUSTOM ? (Identifier) stat.getValue() : Registries.STAT_TYPE.getId(stat.getType());
        for (var mapping : values()) if (mapping.key.equals(key)) return mapping.bukkit;
        return null;
    }
    public static Stat<Identifier> getNMSStatistic(Statistic statistic) {
        if (Objects.requireNonNull(statistic, "statistic").getType() != Statistic.Type.UNTYPED) throw new IllegalArgumentException("Statistic requires a material or entity");
        var key = valueOf(statistic.name()).key;
        if (!Registries.CUSTOM_STAT.containsId(key)) throw new IllegalArgumentException("Unknown native statistic: " + key);
        return Stats.CUSTOM.getOrCreateStat(Registries.CUSTOM_STAT.get(key));
    }
    public static Stat<?> getMaterialStatistic(Statistic statistic, Material material) {
        Objects.requireNonNull(statistic, "statistic"); Objects.requireNonNull(material, "material");
        var item = CraftMagicNumbers.getItem(material);
        return switch (statistic) {
            case MINE_BLOCK -> { var block = CraftMagicNumbers.getBlock(material); if (block == null) throw new IllegalArgumentException("Material is not a block"); yield Stats.MINED.getOrCreateStat(block); }
            case CRAFT_ITEM -> Stats.CRAFTED.getOrCreateStat(Objects.requireNonNull(item, "Material is not an item"));
            case USE_ITEM -> Stats.USED.getOrCreateStat(Objects.requireNonNull(item, "Material is not an item"));
            case BREAK_ITEM -> Stats.BROKEN.getOrCreateStat(Objects.requireNonNull(item, "Material is not an item"));
            case PICKUP -> Stats.PICKED_UP.getOrCreateStat(Objects.requireNonNull(item, "Material is not an item"));
            case DROP -> Stats.DROPPED.getOrCreateStat(Objects.requireNonNull(item, "Material is not an item"));
            default -> throw new IllegalArgumentException("Statistic does not accept a material");
        };
    }
    public static Stat<?> getEntityStatistic(Statistic statistic, EntityType entity) {
        Objects.requireNonNull(statistic, "statistic"); Objects.requireNonNull(entity, "entityType");
        var key = new Identifier(entity.getKey().getNamespace(), entity.getKey().getKey());
        if (!Registries.ENTITY_TYPE.containsId(key)) throw new IllegalArgumentException("Unknown entity type: " + entity);
        var nativeType = Registries.ENTITY_TYPE.get(key);
        return switch (statistic) {
            case KILL_ENTITY -> Stats.KILLED.getOrCreateStat(nativeType);
            case ENTITY_KILLED_BY -> Stats.KILLED_BY.getOrCreateStat(nativeType);
            default -> throw new IllegalArgumentException("Statistic does not accept an entity");
        };
    }
    public static Material getMaterialFromStatistic(Stat<?> stat) {
        return stat.getValue() instanceof net.minecraft.item.Item item ? CraftMagicNumbers.getMaterial(item)
                : stat.getValue() instanceof net.minecraft.block.Block block ? CraftMagicNumbers.getMaterial(block) : null;
    }
    public static EntityType getEntityTypeFromStatistic(Stat<?> stat) {
        if (!(stat.getValue() instanceof net.minecraft.entity.EntityType<?> entity)) return null;
        var key = Registries.ENTITY_TYPE.getId(entity);
        return org.bukkit.Registry.ENTITY_TYPE.get(new org.bukkit.NamespacedKey(key.getNamespace(), key.getPath()));
    }
    private static void set(ServerStatHandler manager, Stat<?> stat, int value) {
        if (value < 0) throw new IllegalArgumentException("Statistic must be nonnegative"); manager.setStat(null, stat, value);
    }
    private static int amount(int value) { if (value <= 0) throw new IllegalArgumentException("Amount must be positive"); return value; }
    public static int getStatistic(ServerStatHandler manager, Statistic statistic) { return manager.getStat(getNMSStatistic(statistic)); }
    public static void setStatistic(ServerStatHandler manager, Statistic statistic, int value) { set(manager, getNMSStatistic(statistic), value); }
    public static void incrementStatistic(ServerStatHandler manager, Statistic statistic) { incrementStatistic(manager, statistic, 1); }
    public static void incrementStatistic(ServerStatHandler manager, Statistic statistic, int value) { set(manager, getNMSStatistic(statistic), Math.addExact(manager.getStat(getNMSStatistic(statistic)), amount(value))); }
    public static void decrementStatistic(ServerStatHandler manager, Statistic statistic) { decrementStatistic(manager, statistic, 1); }
    public static void decrementStatistic(ServerStatHandler manager, Statistic statistic, int value) { set(manager, getNMSStatistic(statistic), Math.subtractExact(manager.getStat(getNMSStatistic(statistic)), amount(value))); }
    public static int getStatistic(ServerStatHandler manager, Statistic statistic, Material material) { return manager.getStat(getMaterialStatistic(statistic, material)); }
    public static void setStatistic(ServerStatHandler manager, Statistic statistic, Material material, int value) { set(manager, getMaterialStatistic(statistic, material), value); }
    public static void incrementStatistic(ServerStatHandler manager, Statistic statistic, Material material) { incrementStatistic(manager, statistic, material, 1); }
    public static void incrementStatistic(ServerStatHandler manager, Statistic statistic, Material material, int value) { set(manager, getMaterialStatistic(statistic, material), Math.addExact(manager.getStat(getMaterialStatistic(statistic, material)), amount(value))); }
    public static void decrementStatistic(ServerStatHandler manager, Statistic statistic, Material material) { decrementStatistic(manager, statistic, material, 1); }
    public static void decrementStatistic(ServerStatHandler manager, Statistic statistic, Material material, int value) { set(manager, getMaterialStatistic(statistic, material), Math.subtractExact(manager.getStat(getMaterialStatistic(statistic, material)), amount(value))); }
    public static int getStatistic(ServerStatHandler manager, Statistic statistic, EntityType entityType) { return manager.getStat(getEntityStatistic(statistic, entityType)); }
    public static void setStatistic(ServerStatHandler manager, Statistic statistic, EntityType entityType, int value) { set(manager, getEntityStatistic(statistic, entityType), value); }
    public static void incrementStatistic(ServerStatHandler manager, Statistic statistic, EntityType entityType) { incrementStatistic(manager, statistic, entityType, 1); }
    public static void incrementStatistic(ServerStatHandler manager, Statistic statistic, EntityType entityType, int value) { set(manager, getEntityStatistic(statistic, entityType), Math.addExact(manager.getStat(getEntityStatistic(statistic, entityType)), amount(value))); }
    public static void decrementStatistic(ServerStatHandler manager, Statistic statistic, EntityType entityType) { decrementStatistic(manager, statistic, entityType, 1); }
    public static void decrementStatistic(ServerStatHandler manager, Statistic statistic, EntityType entityType, int value) { set(manager, getEntityStatistic(statistic, entityType), Math.subtractExact(manager.getStat(getEntityStatistic(statistic, entityType)), amount(value))); }
}
