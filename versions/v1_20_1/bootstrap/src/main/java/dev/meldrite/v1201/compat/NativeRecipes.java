package dev.meldrite.v1201.compat;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.inventory.*;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.*;

/** Bukkit recipes use the same registry and matching engines as Fabric and vanilla recipes. */
public final class NativeRecipes {
    private NativeRecipes() { }
    public static MinecraftServer server() { return ((CraftServer) Bukkit.getServer()).getServer(); }
    public static void requireThread() {
        if (!server().isOnThread()) throw new IllegalStateException("Recipe changes require the server thread");
    }

    public static void add(RecipeManager manager, net.minecraft.recipe.Recipe<?> recipe) {
        requireThread();
        Objects.requireNonNull(recipe, "recipe");
        NativeRecipeManagerAccess access = (NativeRecipeManagerAccess) manager;
        if (access.meldrite$getRecipesById().containsKey(recipe.getId())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
        }
        // New plugin recipes have priority, while all untouched Mod recipes retain their native instances.
        Map<RecipeType<?>, Map<Identifier, net.minecraft.recipe.Recipe<?>>> types = new LinkedHashMap<>(access.meldrite$getRecipes());
        Map<Identifier, net.minecraft.recipe.Recipe<?>> type = new LinkedHashMap<>();
        type.put(recipe.getId(), recipe);
        type.putAll(types.getOrDefault(recipe.getType(), Map.of()));
        types.put(recipe.getType(), Collections.unmodifiableMap(type));
        Map<Identifier, net.minecraft.recipe.Recipe<?>> ids = new LinkedHashMap<>(access.meldrite$getRecipesById());
        ids.put(recipe.getId(), recipe);
        access.meldrite$setRecipes(Collections.unmodifiableMap(types));
        access.meldrite$setRecipesById(Collections.unmodifiableMap(ids));
    }

    public static boolean remove(RecipeManager manager, Identifier id) {
        requireThread();
        NativeRecipeManagerAccess access = (NativeRecipeManagerAccess) manager;
        var removed = access.meldrite$getRecipesById().get(Objects.requireNonNull(id, "id"));
        if (removed == null) return false;
        Map<RecipeType<?>, Map<Identifier, net.minecraft.recipe.Recipe<?>>> types = new LinkedHashMap<>(access.meldrite$getRecipes());
        Map<Identifier, net.minecraft.recipe.Recipe<?>> type = new LinkedHashMap<>(types.get(removed.getType()));
        type.remove(id);
        types.put(removed.getType(), Collections.unmodifiableMap(type));
        Map<Identifier, net.minecraft.recipe.Recipe<?>> ids = new LinkedHashMap<>(access.meldrite$getRecipesById());
        ids.remove(id);
        access.meldrite$setRecipes(Collections.unmodifiableMap(types));
        access.meldrite$setRecipesById(Collections.unmodifiableMap(ids));
        return true;
    }

    public static void clear(RecipeManager manager) {
        requireThread();
        var access = (NativeRecipeManagerAccess) manager;
        access.meldrite$setRecipes(Map.of());
        access.meldrite$setRecipesById(Map.of());
    }

    public static void synchronize() {
        requireThread();
        for (var player : server().getPlayerManager().getPlayerList()) {
            player.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket(server().getRecipeManager().values()));
            player.getRecipeBook().sendInitRecipesPacket(player);
        }
    }

    public static Recipe copyBukkit(Recipe recipe) {
        Recipe copy;
        if (recipe instanceof ShapedRecipe shaped) {
            var result = new CraftShapedRecipe(shaped.getKey(), shaped.getResult());
            result.shape(shaped.getShape());
            shaped.getChoiceMap().forEach((character, choice) -> { if (choice != null) result.setIngredient(character, choice); });
            copy = result;
        } else if (recipe instanceof ShapelessRecipe shapeless) {
            var result = new CraftShapelessRecipe(shapeless.getKey(), shapeless.getResult());
            shapeless.getChoiceList().forEach(result::addIngredient);
            copy = result;
        } else if (recipe instanceof FurnaceRecipe r) {
            copy = new CraftFurnaceRecipe(r.getKey(), r.getResult(), r.getInputChoice(), r.getExperience(), r.getCookingTime());
        } else if (recipe instanceof BlastingRecipe r) {
            copy = new CraftBlastingRecipe(r.getKey(), r.getResult(), r.getInputChoice(), r.getExperience(), r.getCookingTime());
        } else if (recipe instanceof SmokingRecipe r) {
            copy = new CraftSmokingRecipe(r.getKey(), r.getResult(), r.getInputChoice(), r.getExperience(), r.getCookingTime());
        } else if (recipe instanceof CampfireRecipe r) {
            copy = new CraftCampfireRecipe(r.getKey(), r.getResult(), r.getInputChoice(), r.getExperience(), r.getCookingTime());
        } else if (recipe instanceof StonecuttingRecipe r) {
            var result = new CraftStonecuttingRecipe(r.getKey(), r.getResult(), r.getInputChoice());
            result.setGroup(r.getGroup());
            copy = result;
        } else if (recipe instanceof SmithingTransformRecipe r) {
            copy = new CraftSmithingTransformRecipe(r.getKey(), r.getResult(), r.getTemplate(), r.getBase(), r.getAddition(), r.willCopyNbt());
        } else if (recipe instanceof SmithingTrimRecipe r) {
            copy = new CraftSmithingTrimRecipe(r.getKey(), r.getTemplate(), r.getBase(), r.getAddition(), r.willCopyNbt());
        } else throw new IllegalArgumentException("Unmapped Bukkit recipe: " + recipe.getClass().getName());
        if (recipe instanceof org.bukkit.inventory.CraftingRecipe source && copy instanceof org.bukkit.inventory.CraftingRecipe target) {
            target.setGroup(source.getGroup());
            target.setCategory(source.getCategory());
        }
        if (recipe instanceof CookingRecipe<?> source && copy instanceof CookingRecipe<?> target) {
            target.setGroup(source.getGroup());
            target.setCategory(source.getCategory());
        }
        return copy;
    }

    public static net.minecraft.recipe.Recipe<?> toMinecraft(Recipe recipe) {
        if (recipe instanceof CraftComplexRecipe complex) return complex.getHandle();
        Identifier id = CraftNamespacedKey.toMinecraft(((Keyed) recipe).getKey());
        ItemStack output = CraftItemStack.asNMSCopy(recipe.getResult());
        if (recipe instanceof ShapedRecipe r) {
            String[] shape = r.getShape();
            if (shape.length == 0) throw new IllegalArgumentException("Shaped recipe has no shape");
            int width = shape[0].length();
            DefaultedList<Ingredient> inputs = DefaultedList.ofSize(width * shape.length, Ingredient.EMPTY);
            var choices = r.getChoiceMap();
            for (int y = 0; y < shape.length; y++) for (int x = 0; x < width; x++) {
                inputs.set(y * width + x, CraftRecipe.toIngredient(choices.get(shape[y].charAt(x)), false));
            }
            if (inputs.stream().allMatch(Ingredient::isEmpty)) throw new IllegalArgumentException("Shaped recipe has no ingredients");
            return new net.minecraft.recipe.ShapedRecipe(id, r.getGroup(), CraftRecipe.getCategory(r.getCategory()), width, shape.length, inputs, output);
        }
        if (recipe instanceof ShapelessRecipe r) {
            DefaultedList<Ingredient> inputs = DefaultedList.of();
            r.getChoiceList().forEach(choice -> inputs.add(CraftRecipe.toIngredient(choice, true)));
            if (inputs.isEmpty()) throw new IllegalArgumentException("Shapeless recipe has no ingredients");
            return new net.minecraft.recipe.ShapelessRecipe(id, r.getGroup(), CraftRecipe.getCategory(r.getCategory()), output, inputs);
        }
        if (recipe instanceof CookingRecipe<?> r) {
            Ingredient input = CraftRecipe.toIngredient(r.getInputChoice(), true);
            var category = CraftRecipe.getCategory(r.getCategory());
            if (r instanceof FurnaceRecipe) return new net.minecraft.recipe.SmeltingRecipe(id, r.getGroup(), category, input, output, r.getExperience(), r.getCookingTime());
            if (r instanceof BlastingRecipe) return new net.minecraft.recipe.BlastingRecipe(id, r.getGroup(), category, input, output, r.getExperience(), r.getCookingTime());
            if (r instanceof SmokingRecipe) return new net.minecraft.recipe.SmokingRecipe(id, r.getGroup(), category, input, output, r.getExperience(), r.getCookingTime());
            if (r instanceof CampfireRecipe) return new net.minecraft.recipe.CampfireCookingRecipe(id, r.getGroup(), category, input, output, r.getExperience(), r.getCookingTime());
        }
        if (recipe instanceof StonecuttingRecipe r) return new net.minecraft.recipe.StonecuttingRecipe(id, r.getGroup(), CraftRecipe.toIngredient(r.getInputChoice(), true), output);
        if (recipe instanceof SmithingTransformRecipe r) {
            return new Transform(id, CraftRecipe.toIngredient(r.getTemplate(), true), CraftRecipe.toIngredient(r.getBase(), true),
                    CraftRecipe.toIngredient(r.getAddition(), true), output, r.willCopyNbt());
        }
        if (recipe instanceof SmithingTrimRecipe r) {
            return new Trim(id, CraftRecipe.toIngredient(r.getTemplate(), true), CraftRecipe.toIngredient(r.getBase(), true),
                    CraftRecipe.toIngredient(r.getAddition(), true), r.willCopyNbt());
        }
        throw new IllegalArgumentException("Unmapped Bukkit recipe: " + recipe.getClass().getName());
    }

    public static Recipe fromMinecraft(net.minecraft.recipe.Recipe<?> recipe) {
        var type = recipe.getClass();
        // Custom recipes may need crafting context even for output lookup; keep their native handles.
        if (!List.of(net.minecraft.recipe.ShapedRecipe.class, net.minecraft.recipe.ShapelessRecipe.class,
                net.minecraft.recipe.SmeltingRecipe.class, net.minecraft.recipe.BlastingRecipe.class,
                net.minecraft.recipe.SmokingRecipe.class, net.minecraft.recipe.CampfireCookingRecipe.class,
                net.minecraft.recipe.StonecuttingRecipe.class, net.minecraft.recipe.SmithingTransformRecipe.class,
                net.minecraft.recipe.SmithingTrimRecipe.class, Transform.class, Trim.class).contains(type)) {
            return new CraftComplexRecipe(recipe);
        }
        var key = CraftNamespacedKey.fromMinecraft(recipe.getId());
        var output = CraftItemStack.asBukkitCopy(outputForBukkit(recipe));
        if (output.getType().isAir() || recipe.isEmpty()
                || recipe instanceof net.minecraft.recipe.ShapedRecipe shaped && (shaped.getWidth() > 3 || shaped.getHeight() > 3)
                || recipe instanceof net.minecraft.recipe.ShapelessRecipe shapeless && shapeless.getIngredients().size() > 9) {
            return new CraftComplexRecipe(recipe);
        }
        Recipe result;
        // Subclasses may override matches/craft/remainders; preserve those recipes intact.
        if (recipe.getClass() == net.minecraft.recipe.ShapedRecipe.class) {
            result = new CraftShapedRecipe(output, (net.minecraft.recipe.ShapedRecipe) recipe);
        } else if (recipe.getClass() == net.minecraft.recipe.ShapelessRecipe.class) {
            result = new CraftShapelessRecipe(output, (net.minecraft.recipe.ShapelessRecipe) recipe);
        } else if (List.of(net.minecraft.recipe.SmeltingRecipe.class, net.minecraft.recipe.BlastingRecipe.class,
                net.minecraft.recipe.SmokingRecipe.class, net.minecraft.recipe.CampfireCookingRecipe.class).contains(recipe.getClass())) {
            result = CraftCookingRecipe.fromMinecraft((net.minecraft.recipe.AbstractCookingRecipe) recipe, server().getRegistryManager());
        } else if (recipe.getClass() == net.minecraft.recipe.StonecuttingRecipe.class) {
            var stone = new CraftStonecuttingRecipe(key, output, CraftRecipe.toBukkit(recipe.getIngredients().get(0)));
            stone.setGroup(recipe.getGroup());
            result = stone;
        } else if (recipe.getClass() == net.minecraft.recipe.SmithingTransformRecipe.class || recipe instanceof Transform) {
            Ingredient template = recipe instanceof Transform transform ? transform.templateIngredient() : ((SmithingRecipeAccess) recipe).meldrite$getTemplate();
            Ingredient base = recipe instanceof Transform transform ? transform.baseIngredient() : ((SmithingRecipeAccess) recipe).meldrite$getBase();
            Ingredient addition = recipe instanceof Transform transform ? transform.additionIngredient() : ((SmithingRecipeAccess) recipe).meldrite$getAddition();
            result = new CraftSmithingTransformRecipe(key, output, CraftRecipe.toBukkit(template),
                    CraftRecipe.toBukkit(base), CraftRecipe.toBukkit(addition),
                    !(recipe instanceof Transform transform) || transform.copyNbt);
        } else if (recipe.getClass() == net.minecraft.recipe.SmithingTrimRecipe.class || recipe instanceof Trim) {
            Ingredient template = recipe instanceof Trim trim ? trim.templateIngredient() : ((SmithingRecipeAccess) recipe).meldrite$getTemplate();
            Ingredient base = recipe instanceof Trim trim ? trim.baseIngredient() : ((SmithingRecipeAccess) recipe).meldrite$getBase();
            Ingredient addition = recipe instanceof Trim trim ? trim.additionIngredient() : ((SmithingRecipeAccess) recipe).meldrite$getAddition();
            result = new CraftSmithingTrimRecipe(key, CraftRecipe.toBukkit(template),
                    CraftRecipe.toBukkit(base), CraftRecipe.toBukkit(addition),
                    !(recipe instanceof Trim trim) || trim.copyNbt);
        } else result = new CraftComplexRecipe(recipe);
        return result;
    }

    /** Exposes the original recipe's output contract without inspecting its Mod identity. */
    public static ItemStack outputForBukkit(net.minecraft.recipe.Recipe<?> recipe) {
        return recipe.getOutput(server().getRegistryManager());
    }

    public static void fillShaped(ShapedRecipe target, net.minecraft.recipe.ShapedRecipe source) {
        String[] shape = new String[source.getHeight()];
        for (int y = 0; y < source.getHeight(); y++) {
            StringBuilder row = new StringBuilder();
            for (int x = 0; x < source.getWidth(); x++) row.append((char) ('a' + y * source.getWidth() + x));
            shape[y] = row.toString();
        }
        target.shape(shape);
        for (int i = 0; i < source.getIngredients().size(); i++) {
            RecipeChoice choice = CraftRecipe.toBukkit(source.getIngredients().get(i));
            if (choice != null) target.setIngredient((char) ('a' + i), choice);
        }
        target.setGroup(source.getGroup());
        target.setCategory(CraftRecipe.getCategory(source.getCategory()));
    }

    public static void fillShapeless(ShapelessRecipe target, net.minecraft.recipe.ShapelessRecipe source) {
        for (Ingredient ingredient : source.getIngredients()) {
            RecipeChoice choice = CraftRecipe.toBukkit(ingredient);
            if (choice != null) target.addIngredient(choice);
        }
        target.setGroup(source.getGroup());
        target.setCategory(CraftRecipe.getCategory(source.getCategory()));
    }

    public static final class Transform extends net.minecraft.recipe.SmithingTransformRecipe {
        private final boolean copyNbt;
        private final Ingredient template;
        private final Ingredient base;
        private final Ingredient addition;
        public Transform(Identifier id, Ingredient template, Ingredient base, Ingredient addition, ItemStack output, boolean copyNbt) {
            super(id, template, base, addition, output);
            this.template = template;
            this.base = base;
            this.addition = addition;
            this.copyNbt = copyNbt;
        }
        Ingredient templateIngredient() { return template; }
        Ingredient baseIngredient() { return base; }
        Ingredient additionIngredient() { return addition; }
        @Override public ItemStack craft(Inventory inventory, DynamicRegistryManager registries) {
            return copyNbt ? super.craft(inventory, registries) : getOutput(registries).copy();
        }
    }

    public static final class Trim extends net.minecraft.recipe.SmithingTrimRecipe {
        private final boolean copyNbt;
        private final Ingredient template;
        private final Ingredient base;
        private final Ingredient addition;
        public Trim(Identifier id, Ingredient template, Ingredient base, Ingredient addition, boolean copyNbt) {
            super(id, template, base, addition);
            this.template = template;
            this.base = base;
            this.addition = addition;
            this.copyNbt = copyNbt;
        }
        Ingredient templateIngredient() { return template; }
        Ingredient baseIngredient() { return base; }
        Ingredient additionIngredient() { return addition; }
        @Override public ItemStack craft(Inventory inventory, DynamicRegistryManager registries) {
            if (copyNbt) return super.craft(inventory, registries);
            ItemStack result = super.craft(inventory, registries);
            if (result.isEmpty()) return result;
            var trim = result.getNbt().get("Trim").copy();
            result.setNbt(new net.minecraft.nbt.NbtCompound());
            result.getNbt().put("Trim", trim);
            return result;
        }
    }
}
