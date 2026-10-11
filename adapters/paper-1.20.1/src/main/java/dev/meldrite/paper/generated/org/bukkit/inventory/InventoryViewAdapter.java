package dev.meldrite.paper.generated.org.bukkit.inventory;
import java.util.Objects;
import org.bukkit.inventory.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
/** Detached preview view; native cursor/slot/title effects remain unsupported. */
public final class InventoryViewAdapter extends InventoryView {
    private final Inventory top,bottom;private final HumanEntity player;
    public InventoryViewAdapter(Inventory top,Inventory bottom,HumanEntity player){this.top=Objects.requireNonNull(top);this.bottom=Objects.requireNonNull(bottom);this.player=Objects.requireNonNull(player);}
    public Inventory getTopInventory(){return top;}
    public Inventory getBottomInventory(){return bottom;}
    public HumanEntity getPlayer(){return player;}
    public InventoryType getType(){return top.getType();}
    public String getTitle(){throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/inventory/InventoryView#getTitle()Ljava/lang/String;#METHOD");}
    public String getOriginalTitle(){throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/inventory/InventoryView#getOriginalTitle()Ljava/lang/String;#METHOD");}
    public void setTitle(String title){throw new UnsupportedOperationException("UNSUPPORTED_OPERATION: org/bukkit/inventory/InventoryView#setTitle(Ljava/lang/String;)V#METHOD");}
}
