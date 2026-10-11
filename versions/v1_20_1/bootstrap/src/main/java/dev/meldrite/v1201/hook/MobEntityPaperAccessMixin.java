package dev.meldrite.v1201.hook;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(MobEntity.class)
public interface MobEntityPaperAccessMixin {
 @Accessor("persistent") void meldrite$setPersistenceRequired(boolean value);
 @Accessor("handDropChances") float[] meldrite$getHandDropChances();
 @Accessor("armorDropChances") float[] meldrite$getArmorDropChances();
 @Accessor("lootTable") void meldrite$setLootTable(Identifier value);
 @Accessor("lootTableSeed") void meldrite$setLootTableSeed(long value);
 @Accessor("goalSelector") GoalSelector meldrite$goalSelector();
 @Invoker("getAmbientSound") SoundEvent meldrite$ambientSound();
 @Invoker("isAffectedByDaylight") boolean meldrite$isInDaylight();
 @Invoker("tickNewAi") void meldrite$tickNewAi();
}
