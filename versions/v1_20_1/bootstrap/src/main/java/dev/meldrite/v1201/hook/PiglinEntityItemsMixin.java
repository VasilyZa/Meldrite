package dev.meldrite.v1201.hook;
import dev.meldrite.v1201.compat.NativePiglinItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PiglinEntity.class)
public abstract class PiglinEntityItemsMixin implements NativePiglinItems {
    @Unique private final Set<Item> meldrite$barter = new HashSet<>();
    @Unique private final Set<Item> meldrite$interest = new HashSet<>();
    @Override public Set<Item> meldrite$barterItems() { return meldrite$barter; }
    @Override public Set<Item> meldrite$interestItems() { return meldrite$interest; }
    @WrapOperation(method = "equipToOffHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean meldrite$paymentDrop(net.minecraft.item.ItemStack stack, Item item, Operation<Boolean> original) { return original.call(stack, item) || meldrite$barter.contains(stack.getItem()); }
    @WrapOperation(method = "prefersNewEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PiglinBrain;isGoldenItem(Lnet/minecraft/item/ItemStack;)Z"))
    private boolean meldrite$interestEquipment(net.minecraft.item.ItemStack stack, Operation<Boolean> original) { return original.call(stack) || meldrite$interest.contains(stack.getItem()) || meldrite$barter.contains(stack.getItem()); }
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void meldrite$write(NbtCompound nbt, CallbackInfo ci) { if (!meldrite$barter.isEmpty()) nbt.put("Bukkit.BarterList", meldrite$encode(meldrite$barter)); if (!meldrite$interest.isEmpty()) nbt.put("Bukkit.InterestList", meldrite$encode(meldrite$interest)); }
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void meldrite$read(NbtCompound nbt, CallbackInfo ci) { meldrite$decode(nbt.getList("Bukkit.BarterList", 8), meldrite$barter); meldrite$decode(nbt.getList("Bukkit.InterestList", 8), meldrite$interest); }
    @Unique private static NbtList meldrite$encode(Set<Item> items) { var list = new NbtList(); items.forEach(item -> list.add(NbtString.of(Registries.ITEM.getId(item).toString()))); return list; }
    @Unique private static void meldrite$decode(NbtList list, Set<Item> items) { items.clear(); list.forEach(element -> { var id = Identifier.tryParse(element.asString()); if (id != null && Registries.ITEM.containsId(id)) items.add(Registries.ITEM.get(id)); }); }
}
