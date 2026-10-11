package dev.meldrite.service;
import dev.meldrite.bridge.*;
import dev.meldrite.foundation.*;
import java.util.*;
public final class InventoryService {
    private final EntityService entities;private final InventoryPort inventories;private final ItemCodecPort codec;
    public InventoryService(EntityService entities,InventoryPort inventories,ItemCodecPort codec){this.entities=Objects.requireNonNull(entities);this.inventories=Objects.requireNonNull(inventories);this.codec=Objects.requireNonNull(codec);}
    public List<ItemSnapshot> snapshot(EntityService.Identity identity){if(!entities.valid(identity))throw new IllegalStateException("STALE_HANDLE");return List.copyOf(inventories.snapshot(identity.handle()));}
    public ItemSnapshot resize(ItemSnapshot before,int count)throws Exception {
        if(count<0)throw new IllegalArgumentException("Negative item count");
        var merged=Objects.requireNonNull(codec.mergeCountLosslessly(before,count),"Codec returned null");
        if(!merged.type().equals(before.type())||!merged.codecId().equals(before.codecId())||merged.count()!=count)throw new IllegalStateException("LOSSY_ITEM_MERGE");
        if(!merged.equals(codec.decode(codec.encode(merged))))throw new IllegalStateException("ITEM_CODEC_ROUNDTRIP_FAILED");
        return merged;
    }
    public void requireWritable(EntityService.Identity identity){entities.requireWritable(identity);}
    public void commit(EntityService.Identity identity,List<ItemSnapshot> expected,List<ItemSnapshot> replacement)throws Exception {
        entities.requireWritable(identity);expected=List.copyOf(expected);replacement=List.copyOf(replacement);
        if(expected.size()!=replacement.size())throw new IllegalArgumentException("Inventory shape changed");
        for(var item:replacement)if(!item.equals(codec.decode(codec.encode(item))))throw new IllegalStateException("ITEM_CODEC_ROUNDTRIP_FAILED");
        entities.requireWritable(identity);
        if(!inventories.compareAndReplaceAll(identity.handle(),expected,replacement))throw new IllegalStateException("INVENTORY_CHANGED");
    }
    public ItemSnapshot mutate(EntityService.Identity identity,int slot,int count)throws Exception {
        entities.requireWritable(identity);
        if(count<0)throw new IllegalArgumentException("Negative item count");
        var slots=snapshot(identity);
        if(slot<0||slot>=slots.size())throw new IndexOutOfBoundsException("Inventory slot: "+slot);
        ItemSnapshot before=slots.get(slot);
        if(before.count()==count)return before;
        ItemSnapshot merged=resize(before,count);
        entities.requireWritable(identity);
        if(!inventories.compareAndReplace(identity.handle(),slot,before,merged))throw new IllegalStateException("INVENTORY_CHANGED");
        return merged;
    }
}
