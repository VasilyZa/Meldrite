package dev.meldrite.v1201.hook;

import net.minecraft.entity.passive.SchoolingFishEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SchoolingFishEntity.class)
public interface SchoolingFishEntityPaperAccessMixin {
    @Accessor("leader") SchoolingFishEntity meldrite$getLeader();
    @Accessor("groupSize") int meldrite$getGroupSize();
}
