package dev.meldrite.v1201.hook;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntitySerializationAccessor {
    @Accessor("passengerList") ImmutableList<Entity> meldrite$getPassengerList();
    @Accessor("passengerList") void meldrite$setPassengerList(ImmutableList<Entity> passengers);
}
