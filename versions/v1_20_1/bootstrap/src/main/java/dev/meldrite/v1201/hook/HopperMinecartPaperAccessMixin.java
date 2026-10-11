package dev.meldrite.v1201.hook;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets = "net.minecraft.block.entity.HopperBlockEntity")
public interface HopperMinecartPaperAccessMixin {
    @Accessor("transferCooldown") int meldrite$getCooldown();
    @Accessor("transferCooldown") void meldrite$setCooldown(int value);
}
