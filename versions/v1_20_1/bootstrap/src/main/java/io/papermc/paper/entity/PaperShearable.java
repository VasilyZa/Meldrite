package io.papermc.paper.entity;

import dev.meldrite.v1201.compat.NativeEntityExtensions;

public interface PaperShearable extends Shearable {
    net.minecraft.entity.Shearable getHandle();
    @Override default boolean readyToBeSheared() { return getHandle().isShearable(); }
    @Override default void shear(net.kyori.adventure.sound.Sound.Source source) {
        NativeEntityExtensions.requireThread((net.minecraft.entity.Entity) getHandle());
        var category = switch (java.util.Objects.requireNonNull(source, "source")) {
            case RECORD -> net.minecraft.sound.SoundCategory.RECORDS;
            case BLOCK -> net.minecraft.sound.SoundCategory.BLOCKS;
            case PLAYER -> net.minecraft.sound.SoundCategory.PLAYERS;
            default -> net.minecraft.sound.SoundCategory.valueOf(source.name());
        };
        getHandle().sheared(category);
    }
}
