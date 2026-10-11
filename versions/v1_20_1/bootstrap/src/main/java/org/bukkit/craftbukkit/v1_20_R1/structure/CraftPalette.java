package org.bukkit.craftbukkit.v1_20_R1.structure;

import java.util.List;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.structure.StructureTemplate;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState;

public class CraftPalette implements org.bukkit.structure.Palette {
    private final StructureTemplate.PalettedBlockInfoList palette;
    public CraftPalette(StructureTemplate.PalettedBlockInfoList palette) { this.palette = java.util.Objects.requireNonNull(palette); }
    @Override public int getBlockCount() { return palette.getAll().size(); }
    @Override public List<BlockState> getBlocks() {
        return palette.getAll().stream().map(info -> {
            BlockEntity tile = null;
            if (info.state().getBlock() instanceof BlockEntityProvider provider) {
                tile = provider.createBlockEntity(info.pos(), info.state());
                if (tile != null && info.nbt() != null) {
                    tile.readNbt(info.nbt().copy());
                    ((dev.meldrite.v1201.compat.NativeBlockEntityPersistentDataAccess) tile).meldrite$loadPersistentData(info.nbt().getCompound("BukkitValues").copy());
                }
            }
            return CraftBlockState.createUnplaced(info.state(), tile);
        }).toList();
    }
}
