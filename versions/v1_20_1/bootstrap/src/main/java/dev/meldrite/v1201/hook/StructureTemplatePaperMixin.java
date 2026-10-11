package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeStructureAccess;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.structure.StructureTemplate;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplatePaperMixin implements NativeStructureAccess {
    @Shadow @Final private List<StructureTemplate.PalettedBlockInfoList> blockInfoLists;
    @Shadow @Final private List<StructureTemplate.StructureEntityInfo> entities;
    public final CraftPersistentDataContainer persistentDataContainer = new CraftPersistentDataContainer(new CraftPersistentDataTypeRegistry());
    @Override public List<StructureTemplate.PalettedBlockInfoList> meldrite$palettes() { return blockInfoLists; }
    @Override public List<StructureTemplate.StructureEntityInfo> meldrite$entities() { return entities; }
    @Override public CraftPersistentDataContainer meldrite$persistentData() { return persistentDataContainer; }
    @Inject(method = "writeNbt", at = @At("RETURN"))
    private void meldrite$write(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (!persistentDataContainer.isEmpty()) cir.getReturnValue().put("BukkitValues", persistentDataContainer.toTagCompound());
    }
    @Inject(method = "readNbt", at = @At("TAIL"))
    private void meldrite$read(RegistryEntryLookup<Block> lookup, NbtCompound nbt, CallbackInfo ci) {
        persistentDataContainer.clear(); persistentDataContainer.putAll(nbt.getCompound("BukkitValues").copy());
    }
}
