package dev.meldrite.v1201.hook;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(ServerWorld.class)
public interface ServerWorldPropertiesPaperAccessMixin { @Accessor("worldProperties") ServerWorldProperties meldrite$properties(); }
