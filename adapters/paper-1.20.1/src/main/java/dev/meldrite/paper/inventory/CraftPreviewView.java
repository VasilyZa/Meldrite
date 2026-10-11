package dev.meldrite.paper.inventory;
import dev.meldrite.foundation.ItemSnapshot;
import dev.meldrite.service.RecipeService;
import java.util.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
/** Strict plain-item preview subset. Unknown or metadata-bearing native payloads reject before exposure. */
public final class CraftPreviewView {
    private final RecipeService.Preview preview;
    public CraftPreviewView(RecipeService.Preview preview){this.preview=Objects.requireNonNull(preview);for(var item:preview.contents())project(item);}
    public static ItemStack project(ItemSnapshot snapshot){
        var key=snapshot.type();if(!key.registryNamespace().equals("minecraft")||!key.registryPath().equals("item")||!key.entryNamespace().equals("minecraft")||!snapshot.codecId().equals("meldrite:plain-item-v1")||snapshot.opaquePayload().length!=0)throw new UnsupportedOperationException("UNREPRESENTABLE_RECIPE_ITEM: "+key);
        Material type=Material.matchMaterial(key.entryNamespace()+":"+key.entryPath());if(type==null||!type.isItem())throw new UnsupportedOperationException("UNREPRESENTABLE_RECIPE_ITEM: "+key);return new ItemStack(type,snapshot.count());
    }
    public int size(){return preview.contents().size();}
    public ItemStack[] contents(){return preview.contents().stream().map(CraftPreviewView::project).toArray(ItemStack[]::new);}
    public ItemStack item(int slot){return project(preview.contents().get(slot));}
    public boolean isEmpty(){return preview.contents().stream().allMatch(item->item.count()==0);}
    public ItemStack[] matrix(){return preview.matrix().stream().map(CraftPreviewView::project).toArray(ItemStack[]::new);}
    public ItemStack result(){return project(preview.result());}
    public Recipe recipe(){return new dev.meldrite.paper.generated.org.bukkit.inventory.RecipeAdapter(preview.recipe());}
    public org.bukkit.event.inventory.InventoryType type(){preview.recipe();return org.bukkit.event.inventory.InventoryType.CRAFTING;}
}
