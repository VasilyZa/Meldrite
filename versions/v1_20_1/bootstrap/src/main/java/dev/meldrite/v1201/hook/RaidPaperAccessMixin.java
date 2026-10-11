package dev.meldrite.v1201.hook;

import java.util.Set;
import java.util.UUID;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Raid.class)
public interface RaidPaperAccessMixin {
    @Accessor("ticksActive") long meldrite$activeTicks();
    @Accessor("waveCount") int meldrite$waveCount();
    @Accessor("heroesOfTheVillage") Set<UUID> meldrite$heroes();
}
