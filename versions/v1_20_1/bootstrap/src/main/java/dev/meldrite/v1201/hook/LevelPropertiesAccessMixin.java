package dev.meldrite.v1201.hook;

import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelProperties.class)
public interface LevelPropertiesAccessMixin {
    @Accessor("levelInfo") LevelInfo meldrite$levelInfo();
    @Accessor("levelInfo") void meldrite$levelInfo(LevelInfo value);
}
