package dev.meldrite.v1201.hook;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FoxEntity.class)
public interface FoxEntityPaperAccessMixin {
    @Accessor("OWNER") static TrackedData<Optional<UUID>> meldrite$firstTrusted() { throw new AssertionError(); }
    @Accessor("OTHER_TRUSTED") static TrackedData<Optional<UUID>> meldrite$secondTrusted() { throw new AssertionError(); }
    @Invoker("setSleeping") void meldrite$sleeping(boolean value);
    @Invoker("isAggressive") boolean meldrite$isDefending();
    @Invoker("setAggressive") void meldrite$defending(boolean value);
    @Invoker("setWalking") void meldrite$faceplanted(boolean value);
}
