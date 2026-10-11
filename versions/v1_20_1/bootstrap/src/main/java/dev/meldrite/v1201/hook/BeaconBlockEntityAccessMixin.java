package dev.meldrite.v1201.hook;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessMixin {
    @Accessor("level") int meldrite$level();
    @Accessor("level") void meldrite$level(int value);
    @Accessor("primary") StatusEffect meldrite$primary();
    @Accessor("primary") void meldrite$primary(StatusEffect value);
    @Accessor("secondary") StatusEffect meldrite$secondary();
    @Accessor("secondary") void meldrite$secondary(StatusEffect value);
    @Accessor("lock") ContainerLock meldrite$lock();
    @Accessor("lock") void meldrite$lock(ContainerLock value);
}
