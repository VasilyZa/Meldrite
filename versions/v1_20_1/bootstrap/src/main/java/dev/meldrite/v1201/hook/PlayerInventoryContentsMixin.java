package dev.meldrite.v1201.hook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import dev.meldrite.v1201.bridge.PlayerInventoryViewersAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** CraftBukkit container access: snapshot the container while retaining native item references. */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryContentsMixin implements PlayerInventoryViewersAccess {
    @Shadow @Final public DefaultedList<ItemStack> main;
    @Shadow @Final public DefaultedList<ItemStack> armor;
    @Shadow @Final public DefaultedList<ItemStack> offHand;
    @Shadow @Final private List<DefaultedList<ItemStack>> combinedInventory;
    private final List<PlayerEntity> meldrite$viewers = new ArrayList<>();

    public List<ItemStack> getContents() {
        var contents = new ArrayList<ItemStack>(main.size() + armor.size() + offHand.size());
        for (var compartment : combinedInventory) contents.addAll(compartment);
        return contents;
    }

    public List<ItemStack> getArmorContents() { return armor; }

    @Override
    public List<PlayerEntity> meldrite$getViewers() { return List.copyOf(meldrite$viewers); }

    public void onOpen(PlayerEntity player) {
        if (player != null && !meldrite$viewers.contains(player)) meldrite$viewers.add(player);
    }

    public void onClose(PlayerEntity player) { meldrite$viewers.remove(player); }
}
