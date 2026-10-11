package dev.meldrite.v1201.compat;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.item.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import io.papermc.paper.potion.PotionMix;
import io.papermc.paper.potion.PaperPotionMix;

public final class NativeBrewing {
    public static final class Results {
        public final java.util.List<org.bukkit.inventory.ItemStack> items;
        public int index;
        public Results(java.util.List<org.bukkit.inventory.ItemStack> items) { this.items = items; }
    }
    private static final Map<NamespacedKey, PaperPotionMix> MIXES = new LinkedHashMap<>();
    private NativeBrewing() {}
    public static void add(PotionMix mix) {
        NativeRecipes.requireThread(); java.util.Objects.requireNonNull(mix);
        if (MIXES.containsKey(mix.getKey())) throw new IllegalArgumentException("Duplicate potion mix: " + mix.getKey());
        var reordered = new LinkedHashMap<NamespacedKey, PaperPotionMix>();
        reordered.put(mix.getKey(), new PaperPotionMix(mix));
        reordered.putAll(MIXES); MIXES.clear(); MIXES.putAll(reordered);
    }
    public static boolean remove(NamespacedKey key) { NativeRecipes.requireThread(); return MIXES.remove(java.util.Objects.requireNonNull(key)) != null; }
    public static void reset() {
        NativeRecipes.requireThread(); MIXES.clear();
        dev.meldrite.v1201.hook.BrewingRecipeListsAccessMixin.meldrite$potionRecipes().clear();
        dev.meldrite.v1201.hook.BrewingRecipeListsAccessMixin.meldrite$itemRecipes().clear();
        dev.meldrite.v1201.hook.BrewingRecipeListsAccessMixin.meldrite$potionTypes().clear();
        net.minecraft.recipe.BrewingRecipeRegistry.registerDefaults();
    }
    public static boolean ingredient(ItemStack stack) { return MIXES.values().stream().anyMatch(mix -> mix.ingredient().test(stack)); }
    public static boolean input(ItemStack stack) { return MIXES.values().stream().anyMatch(mix -> mix.input().test(stack)); }
    public static boolean matches(ItemStack input, ItemStack ingredient) {
        return !input.isEmpty() && MIXES.values().stream().anyMatch(mix -> mix.input().test(input) && mix.ingredient().test(ingredient));
    }
    public static ItemStack craft(ItemStack input, ItemStack ingredient) {
        if (input.isEmpty()) return null;
        for (PaperPotionMix mix : MIXES.values()) if (mix.input().test(input) && mix.ingredient().test(ingredient)) return mix.result().copy();
        return null;
    }
}
