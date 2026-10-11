package dev.meldrite.v1201.hook;

import java.util.Map;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RaidManager.class)
public interface RaidManagerPaperAccessMixin {
    @Accessor("raids") Map<Integer, Raid> meldrite$raids();
}
