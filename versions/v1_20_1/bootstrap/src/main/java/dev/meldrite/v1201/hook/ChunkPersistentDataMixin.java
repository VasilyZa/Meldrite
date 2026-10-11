package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeChunkPersistentDataAccess;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataTypeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Chunk.class)
public abstract class ChunkPersistentDataMixin implements NativeChunkPersistentDataAccess {
    @Unique private static final CraftPersistentDataTypeRegistry MELDRITE_TYPES = new CraftPersistentDataTypeRegistry();
    @Unique private CraftPersistentDataContainer meldrite$persistentData;

    @Override public CraftPersistentDataContainer meldrite$getPersistentDataContainer() {
        if (meldrite$persistentData == null) meldrite$persistentData = new CraftPersistentDataContainer(MELDRITE_TYPES);
        return meldrite$persistentData;
    }

    @Override public void meldrite$loadPersistentData(NbtCompound values) {
        meldrite$getPersistentDataContainer().putAll(values);
    }
}
