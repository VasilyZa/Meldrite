package dev.meldrite.v1201.hook;

import java.util.Map;
import java.util.UUID;
import net.minecraft.village.VillagerGossips;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerGossips.class)
public interface VillagerGossipsPaperAccessMixin {
    @Accessor("entityReputation") Map<UUID, ?> meldrite$reputations();
}
