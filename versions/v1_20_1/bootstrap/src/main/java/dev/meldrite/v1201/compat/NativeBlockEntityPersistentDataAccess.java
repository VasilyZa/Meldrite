package dev.meldrite.v1201.compat;

import net.minecraft.nbt.NbtCompound;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;

public interface NativeBlockEntityPersistentDataAccess {
    CraftPersistentDataContainer meldrite$getPersistentDataContainer();
    void meldrite$loadPersistentData(NbtCompound values);
}
