package dev.meldrite.paper.generated.org.bukkit.inventory;
public final class RecipeAdapter implements org.bukkit.inventory.Recipe, org.bukkit.Keyed {
private final dev.meldrite.service.RecipeService.Registration registration; public RecipeAdapter(dev.meldrite.service.RecipeService.Registration registration){this.registration=java.util.Objects.requireNonNull(registration);} public org.bukkit.NamespacedKey getKey(){var key=registration.definition().key();return new org.bukkit.NamespacedKey(key.entryNamespace(),key.entryPath());}
  @Override public org.bukkit.inventory.ItemStack getResult() { return dev.meldrite.paper.inventory.CraftPreviewView.project(registration.definition().result()); }
}
