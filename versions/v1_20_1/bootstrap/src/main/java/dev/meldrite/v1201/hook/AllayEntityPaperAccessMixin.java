package dev.meldrite.v1201.hook;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(AllayEntity.class)
public interface AllayEntityPaperAccessMixin {
    @Accessor("duplicationCooldown") long meldrite$cooldown();
    @Accessor("duplicationCooldown") void meldrite$cooldown(long ticks);
    @Accessor("jukeboxPos") BlockPos meldrite$jukebox();
    @Accessor("jukeboxPos") void meldrite$jukebox(BlockPos pos);
    @Accessor("CAN_DUPLICATE") static TrackedData<Boolean> meldrite$canDuplicate() { throw new AssertionError(); }
    @Invoker("canDuplicate") boolean meldrite$canDuplicateNow();
    @Invoker("startDuplicationCooldown") void meldrite$resetCooldown();
    @Invoker("duplicate") void meldrite$duplicate();
    @Invoker("interactMob") net.minecraft.util.ActionResult meldrite$interact(net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.Hand hand);
}
