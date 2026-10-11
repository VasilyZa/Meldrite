package dev.meldrite.v1201.hook;

import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.WitherEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WitherEntity.class)
public interface WitherEntityPaperAccessMixin {
    @Accessor("bossBar") ServerBossBar meldrite$bossBar();
}
