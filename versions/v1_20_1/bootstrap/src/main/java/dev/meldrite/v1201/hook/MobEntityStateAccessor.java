package dev.meldrite.v1201.hook;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntityStateAccessor {
    @Accessor("persistent") void meldrite$setPersistenceRequired(boolean persistent);
    @Accessor("handDropChances") float[] meldrite$getHandDropChances();
    @Accessor("armorDropChances") float[] meldrite$getArmorDropChances();
    @Accessor("lootTable") void meldrite$setLootTable(net.minecraft.util.Identifier table);
    @Accessor("lootTableSeed") void meldrite$setLootTableSeed(long seed);
    @Accessor("goalSelector") net.minecraft.entity.ai.goal.GoalSelector meldrite$goalSelector();
    @Invoker("getAmbientSound") net.minecraft.sound.SoundEvent meldrite$ambientSound();
    @Invoker("isAffectedByDaylight") boolean meldrite$isInDaylight();
    @Invoker("tickNewAi") void meldrite$tickNewAi();
}
