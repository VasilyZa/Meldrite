package org.bukkit.craftbukkit.v1_20_R1.inventory;

import dev.meldrite.paper.facade.CraftItemStackAdapter;
import dev.meldrite.paper.facade.ItemFactoryAdapter;
import dev.meldrite.v1201.compat.NativeItemMetadata;
import dev.meldrite.v1201.compat.NativeItemBridge;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

/** Typed CraftBukkit conversion ABI backed by the actual Fabric item implementation. */
@org.bukkit.configuration.serialization.DelegateDeserialization(org.bukkit.inventory.ItemStack.class)
public final class CraftItemStack extends org.bukkit.inventory.ItemStack implements CraftItemStackAdapter.NativeBacked {
    public ItemStack handle;
    private CraftItemStack(ItemStack handle) { this.handle = handle == null || handle.isEmpty() ? null : handle; }
    public static ItemStack asNMSCopy(org.bukkit.inventory.ItemStack stack) {
        if (stack instanceof CraftItemStack craft) return craft.handle == null ? ItemStack.EMPTY : craft.handle.copy();
        if (stack == null || stack.getAmount() <= 0) return ItemStack.EMPTY;
        return (ItemStack) CraftItemStackAdapter.asNMSCopy(stack);
    }
    public static org.bukkit.inventory.ItemStack asBukkitCopy(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new org.bukkit.inventory.ItemStack(Material.AIR);
        return CraftItemStackAdapter.asBukkitCopy(stack);
    }
    public static CraftItemStack asCraftMirror(ItemStack stack) { return new CraftItemStack(stack); }
    public static CraftItemStack asCraftCopy(org.bukkit.inventory.ItemStack stack) { return asCraftMirror(asNMSCopy(stack)); }
    public static ItemStack unwrap(org.bukkit.inventory.ItemStack stack) { return stack instanceof CraftItemStack craft ? craft.handle == null ? ItemStack.EMPTY : craft.handle : asNMSCopy(stack); }
    public static CraftItemStack asNewCraftStack(net.minecraft.item.Item item) { return asNewCraftStack(item, 1); }
    public static CraftItemStack asNewCraftStack(net.minecraft.item.Item item, int amount) { return asCraftMirror(new ItemStack(item, amount)); }
    @Override public ItemStack nativeCopy() { return handle == null ? ItemStack.EMPTY : handle.copy(); }
    @Override public boolean isEmpty() { return handle == null || handle.isEmpty(); }
    public static ItemStack copyNMSStack(ItemStack stack, int amount) {
        ItemStack result = stack.copy();
        result.setCount(amount);
        return result;
    }
    @Override public Material getType() {
        if (handle == null) return Material.AIR;
        Identifier id = Registries.ITEM.getId(handle.getItem());
        Material material = NativeItemBridge.material(handle.getItem());
        if (material == null) throw new UnsupportedOperationException("UNREPRESENTABLE_NATIVE_ITEM: " + id);
        return material;
    }
    @Override public void setType(Material material) {
        java.util.Objects.requireNonNull(material);
        material = ItemFactoryAdapter.modernMaterial(material, (short) 0);
        if (handle != null && NativeItemBridge.material(handle.getItem()) == material) return;
        ItemMeta metadata = handle == null ? null : getItemMeta();
        int amount = handle == null ? 1 : getAmount();
        var item = NativeItemBridge.item(material);
        if (item == net.minecraft.item.Items.AIR) { handle = null; return; }
        if (handle == null) handle = new ItemStack(item, amount);
        else ((dev.meldrite.v1201.hook.ItemStackPaperAccessMixin) (Object) handle).meldrite$setItem(item);
        if (metadata != null) {
            ItemFactoryAdapter factory = new ItemFactoryAdapter();
            setItemMeta(handle, factory.asMetaFor(metadata, material));
        }
    }
    @Override public int getAmount() { return handle == null ? 0 : handle.getCount(); }
    @Override public void setAmount(int amount) { if (handle != null) handle.setCount(amount); }
    @Override public int getMaxStackSize() { return handle == null ? Material.AIR.getMaxStackSize() : handle.getMaxCount(); }
    @Override public int getMaxItemUseDuration() { return handle == null ? 0 : handle.getMaxUseTime(); }
    @Override public short getDurability() { return handle == null ? -1 : (short) handle.getDamage(); }
    @Override public void setDurability(short damage) { if (handle != null) handle.setDamage(damage); }
    @Override public ItemMeta getItemMeta() {
        return getItemMeta(handle);
    }
    public static ItemMeta getItemMeta(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Material material = NativeItemBridge.material(stack.getItem());
        return material == null ? ItemFactoryAdapter.readUnmappedNativeMetadata(NativeItemMetadata.decode(stack.getNbt())) : getItemMeta(stack, material);
    }
    public static ItemMeta getItemMeta(ItemStack stack, Material material) { return ItemFactoryAdapter.readNativeMetadata(material, NativeItemMetadata.decode(stack == null ? null : stack.getNbt())); }
    @Override public boolean hasItemMeta() {
        return hasItemMeta(handle);
    }
    public static boolean hasItemMeta(ItemStack stack) {
        return stack != null && !stack.isEmpty() && !new ItemFactoryAdapter().equals(getItemMeta(stack), null);
    }
    @Override public boolean setItemMeta(ItemMeta metadata) {
        return setItemMeta(handle, metadata);
    }
    public static boolean setItemMeta(ItemStack stack, ItemMeta metadata) {
        if (stack == null || stack.isEmpty()) return false;
        Material material = NativeItemBridge.material(stack.getItem());
        if (metadata != null && material != null) {
            ItemFactoryAdapter factory = new ItemFactoryAdapter();
            if (!factory.isApplicable(metadata, material)) return false;
            metadata = factory.asMetaFor(metadata, material);
            Material updated = factory.updateMaterial(metadata, material);
            if (updated != material) {
                var key = updated.getKey();
                var item = Registries.ITEM.getOrEmpty(new Identifier(key.getNamespace(), key.getKey())).orElseThrow(() -> new IllegalArgumentException("UNKNOWN_NATIVE_ITEM: " + key));
                ((dev.meldrite.v1201.hook.ItemStackPaperAccessMixin) (Object) stack).meldrite$setItem(item);
            }
        }
        var values = metadata == null ? java.util.Map.<String, Object>of() : ItemFactoryAdapter.writeNativeMetadata(metadata);
        stack.setNbt(values.isEmpty() ? null : NativeItemMetadata.encode(values));
        return true;
    }
    public static void applyMetaToItem(ItemStack stack, ItemMeta metadata) {
        stack.getOrCreateNbt().copyFrom(NativeItemMetadata.encode(ItemFactoryAdapter.writeNativeMetadata(java.util.Objects.requireNonNull(metadata))));
    }
    @Override public CraftItemStack clone() { return asCraftMirror(handle == null ? null : handle.copy()); }
    @Override public boolean isSimilar(org.bukkit.inventory.ItemStack other) {
        return other != null && ItemStack.canCombine(unwrap(this), unwrap(other));
    }
    @Override public boolean equals(Object other) {
        return other instanceof org.bukkit.inventory.ItemStack stack && getAmount() == stack.getAmount() && isSimilar(stack);
    }
    @Override public int hashCode() {
        if (handle == null || NativeItemBridge.material(handle.getItem()) != null) return super.hashCode();
        return java.util.Objects.hash(Registries.ITEM.getId(handle.getItem()), handle.getCount(), handle.getNbt());
    }
    @Override public java.util.Map<String, Object> serialize() {
        if (handle != null && NativeItemBridge.material(handle.getItem()) == null)
            throw new UnsupportedOperationException("Bukkit configuration serialization cannot represent native item " + Registries.ITEM.getId(handle.getItem()) + "; use serializeAsBytes");
        return super.serialize();
    }
    @Override public byte[] serializeAsBytes() { return org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.INSTANCE.serializeItem(this); }
    public static CraftItemStack deserializeBytes(byte[] bytes) { return (CraftItemStack) org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers.INSTANCE.deserializeItem(bytes); }
    @Override public String toString() { return "CraftItemStack{" + (handle == null ? "empty" : handle.writeNbt(new net.minecraft.nbt.NbtCompound())) + "}"; }
    @Override public String getTranslationKey() { return translationKey(); }
    @Override public String translationKey() { return unwrap(this).getTranslationKey(); }
    @Override public io.papermc.paper.inventory.ItemRarity getRarity() { return io.papermc.paper.inventory.ItemRarity.valueOf(unwrap(this).getRarity().name()); }
    @Override public boolean isRepairableBy(org.bukkit.inventory.ItemStack repairMaterial) {
        ItemStack item = unwrap(this); ItemStack repair = unwrap(java.util.Objects.requireNonNull(repairMaterial));
        return !item.isEmpty() && !repair.isEmpty() && item.getItem().canRepair(item, repair);
    }
    @Override public boolean canRepair(org.bukkit.inventory.ItemStack item) { return asCraftCopy(java.util.Objects.requireNonNull(item)).isRepairableBy(this); }
    @Override public java.util.Map<org.bukkit.enchantments.Enchantment, Integer> getEnchantments() {
        ItemMeta metadata = getItemMeta();
        return metadata == null ? java.util.Map.of() : metadata.getEnchants();
    }
    @Override public boolean containsEnchantment(org.bukkit.enchantments.Enchantment enchantment) { return getEnchantmentLevel(enchantment) > 0; }
    @Override public int getEnchantmentLevel(org.bukkit.enchantments.Enchantment enchantment) {
        java.util.Objects.requireNonNull(enchantment, "enchantment");
        ItemMeta metadata = getItemMeta();
        return metadata == null ? 0 : metadata.getEnchantLevel(enchantment);
    }
    @Override public void addUnsafeEnchantment(org.bukkit.enchantments.Enchantment enchantment, int level) {
        java.util.Objects.requireNonNull(enchantment, "enchantment");
        ItemMeta metadata = getItemMeta();
        if (metadata == null) return;
        metadata.addEnchant(enchantment, level, true);
        if (!setItemMeta(metadata)) throw new IllegalArgumentException("Cannot apply item enchantment metadata");
    }
    @Override public int removeEnchantment(org.bukkit.enchantments.Enchantment enchantment) {
        java.util.Objects.requireNonNull(enchantment, "enchantment");
        ItemMeta metadata = getItemMeta();
        if (metadata == null) return 0;
        int previous = metadata.getEnchantLevel(enchantment);
        if (metadata.removeEnchant(enchantment)) setItemMeta(metadata);
        return previous;
    }
}
