package dev.meldrite.v1201.compat;

import java.util.List;
import net.minecraft.structure.StructureTemplate;
import org.bukkit.craftbukkit.v1_20_R1.persistence.CraftPersistentDataContainer;

public interface NativeStructureAccess {
    List<StructureTemplate.PalettedBlockInfoList> meldrite$palettes();
    List<StructureTemplate.StructureEntityInfo> meldrite$entities();
    CraftPersistentDataContainer meldrite$persistentData();
}
