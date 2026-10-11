package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(DisplayEntity.ItemDisplayEntity.class)
public interface ItemDisplayEntityPaperAccessMixin {
    @Invoker("getItemStack") ItemStack meldrite$item();
    @Invoker("setItemStack") void meldrite$item(ItemStack value);
    @Invoker("getTransformationMode") ModelTransformationMode meldrite$mode();
    @Invoker("setTransformationMode") void meldrite$mode(ModelTransformationMode value);
}
