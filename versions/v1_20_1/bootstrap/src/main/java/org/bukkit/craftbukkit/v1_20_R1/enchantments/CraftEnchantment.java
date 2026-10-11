package org.bukkit.craftbukkit.v1_20_R1.enchantments;

import dev.meldrite.v1201.hook.EnchantmentPaperSlotsMixin;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.entity.EntityGroup;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.EquipmentSlot;

/** Live 1.20.1 enchantment projection, including Fabric registry entries. */
public final class CraftEnchantment extends Enchantment {
    private final net.minecraft.enchantment.Enchantment handle;
    public CraftEnchantment(net.minecraft.enchantment.Enchantment handle) {
        super(key(handle)); this.handle = Objects.requireNonNull(handle);
    }
    private static NamespacedKey key(net.minecraft.enchantment.Enchantment handle) {
        Identifier id = Registries.ENCHANTMENT.getId(Objects.requireNonNull(handle));
        if (id == null) throw new IllegalArgumentException("UNREGISTERED_NATIVE_ENCHANTMENT: " + handle);
        return new NamespacedKey(id.getNamespace(), id.getPath());
    }
    public net.minecraft.enchantment.Enchantment getHandle() { return handle; }
    public static net.minecraft.enchantment.Enchantment getRaw(Enchantment enchantment) {
        if (enchantment instanceof EnchantmentWrapper wrapper) enchantment = wrapper.getEnchantment();
        return enchantment instanceof CraftEnchantment craft ? craft.handle : null;
    }
    @Override public int getMaxLevel() { return handle.getMaxLevel(); }
    @Override public int getStartLevel() { return handle.getMinLevel(); }
    @Override public EnchantmentTarget getItemTarget() {
        return switch (handle.target.name()) {
            case "ARMOR" -> EnchantmentTarget.ARMOR;
            case "ARMOR_FEET" -> EnchantmentTarget.ARMOR_FEET;
            case "ARMOR_HEAD" -> EnchantmentTarget.ARMOR_HEAD;
            case "ARMOR_LEGS" -> EnchantmentTarget.ARMOR_LEGS;
            case "ARMOR_CHEST" -> EnchantmentTarget.ARMOR_TORSO;
            case "DIGGER" -> EnchantmentTarget.TOOL;
            case "WEAPON" -> EnchantmentTarget.WEAPON;
            case "BOW" -> EnchantmentTarget.BOW;
            case "FISHING_ROD" -> EnchantmentTarget.FISHING_ROD;
            case "BREAKABLE" -> EnchantmentTarget.BREAKABLE;
            case "WEARABLE" -> EnchantmentTarget.WEARABLE;
            case "TRIDENT" -> EnchantmentTarget.TRIDENT;
            case "CROSSBOW" -> EnchantmentTarget.CROSSBOW;
            case "VANISHABLE" -> EnchantmentTarget.VANISHABLE;
            default -> throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ENCHANTMENT_TARGET: " + handle.target);
        };
    }
    @Override public boolean isTreasure() { return handle.isTreasure(); }
    @Override public boolean isCursed() { return handle.isCursed(); }
    @Override public boolean canEnchantItem(org.bukkit.inventory.ItemStack item) { return handle.isAcceptableItem(CraftItemStack.asNMSCopy(Objects.requireNonNull(item))); }
    @Override public boolean conflictsWith(Enchantment other) {
        net.minecraft.enchantment.Enchantment target = getRaw(Objects.requireNonNull(other));
        return target != null && !handle.canCombine(target);
    }
    @Override public String getName() {
        if (getKey().getNamespace().equals("minecraft")) {
            String legacy = switch (getKey().getKey()) {
                case "protection" -> "PROTECTION_ENVIRONMENTAL";
                case "fire_protection" -> "PROTECTION_FIRE";
                case "feather_falling" -> "PROTECTION_FALL";
                case "blast_protection" -> "PROTECTION_EXPLOSIONS";
                case "projectile_protection" -> "PROTECTION_PROJECTILE";
                case "respiration" -> "OXYGEN";
                case "aqua_affinity" -> "WATER_WORKER";
                case "sharpness" -> "DAMAGE_ALL";
                case "smite" -> "DAMAGE_UNDEAD";
                case "bane_of_arthropods" -> "DAMAGE_ARTHROPODS";
                case "looting" -> "LOOT_BONUS_MOBS";
                case "sweeping" -> "SWEEPING_EDGE";
                case "efficiency" -> "DIG_SPEED";
                case "unbreaking" -> "DURABILITY";
                case "fortune" -> "LOOT_BONUS_BLOCKS";
                case "power" -> "ARROW_DAMAGE";
                case "punch" -> "ARROW_KNOCKBACK";
                case "flame" -> "ARROW_FIRE";
                case "infinity" -> "ARROW_INFINITE";
                case "luck_of_the_sea" -> "LUCK";
                case "thorns", "depth_strider", "frost_walker", "binding_curse", "soul_speed", "swift_sneak", "knockback", "fire_aspect", "silk_touch", "lure", "loyalty", "impaling", "riptide", "channeling", "multishot", "quick_charge", "piercing", "mending", "vanishing_curse" -> getKey().getKey().toUpperCase(java.util.Locale.ROOT);
                default -> null;
            };
            if (legacy != null) return legacy;
        }
        return "UNKNOWN_ENCHANT_" + Registries.ENCHANTMENT.getRawId(handle);
    }
    @Override public Component displayName(int level) { return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(handle.getName(level))); }
    @Override public String translationKey() { return handle.getTranslationKey(); }
    @Override public boolean isTradeable() { return handle.isAvailableForEnchantedBookOffer(); }
    @Override public boolean isDiscoverable() { return handle.isAvailableForRandomSelection(); }
    @Override public io.papermc.paper.enchantments.EnchantmentRarity getRarity() { return fromNMSRarity(handle.getRarity()); }
    public static io.papermc.paper.enchantments.EnchantmentRarity fromNMSRarity(net.minecraft.enchantment.Enchantment.Rarity rarity) {
        return io.papermc.paper.enchantments.EnchantmentRarity.valueOf(Objects.requireNonNull(rarity).name());
    }
    @Override public float getDamageIncrease(int level, org.bukkit.entity.EntityCategory category) {
        EntityGroup group = switch (Objects.requireNonNull(category)) {
            case NONE -> EntityGroup.DEFAULT;
            case UNDEAD -> EntityGroup.UNDEAD;
            case ARTHROPOD -> EntityGroup.ARTHROPOD;
            case ILLAGER -> EntityGroup.ILLAGER;
            case WATER -> EntityGroup.AQUATIC;
        };
        return handle.getAttackDamage(level, group);
    }
    @Override public Set<EquipmentSlot> getActiveSlots() {
        return java.util.Arrays.stream(((EnchantmentPaperSlotsMixin) handle).meldrite$getSlotTypes()).map(slot -> switch (slot) {
            case MAINHAND -> EquipmentSlot.HAND;
            case OFFHAND -> EquipmentSlot.OFF_HAND;
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            case HEAD -> EquipmentSlot.HEAD;
        }).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
