package dev.meldrite.v1201.hook;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemUsageContext.class)
public interface ItemUsageContextAccessor {
    @Invoker("<init>")
    static ItemUsageContext meldrite$create(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockHitResult hit) {
        throw new AssertionError("Mixin invoker stub");
    }
}
