package dev.meldrite.v1201.hook;

import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkTicketManager.class)
public interface ChunkTicketManagerPaperAccessMixin {
    @Accessor("simulationDistance") int meldrite$simulationDistance();
}
