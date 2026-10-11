package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.EnumMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;

public class CraftDecoratedPot extends CraftBlockEntityState<DecoratedPotBlockEntity> implements org.bukkit.block.DecoratedPot {
    public CraftDecoratedPot(ServerWorld world, BlockPos pos, BlockState state, DecoratedPotBlockEntity tile, boolean snapshot) { super(world, pos, state, tile, snapshot); }
    @Override public void setSherd(Side side, Material material) {
        java.util.Objects.requireNonNull(side, "side");
        var item = material == null ? net.minecraft.item.Items.BRICK : CraftMagicNumbers.getItem(material);
        if (item == null || (item != net.minecraft.item.Items.BRICK && !item.getRegistryEntry().isIn(net.minecraft.registry.tag.ItemTags.DECORATED_POT_SHERDS))) throw new IllegalArgumentException("Invalid pottery sherd: " + material);
        var sherds = snapshot.getSherds();
        snapshot.sherds = new DecoratedPotBlockEntity.Sherds(side == Side.BACK ? item : sherds.back(), side == Side.LEFT ? item : sherds.left(), side == Side.RIGHT ? item : sherds.right(), side == Side.FRONT ? item : sherds.front());
    }
    @Override public Material getSherd(Side side) {
        var s = snapshot.getSherds(); return CraftMagicNumbers.getMaterial(switch (java.util.Objects.requireNonNull(side, "side")) { case BACK -> s.back(); case LEFT -> s.left(); case RIGHT -> s.right(); case FRONT -> s.front(); });
    }
    @Override public java.util.Map<Side, Material> getSherds() { var result = new EnumMap<Side, Material>(Side.class); for (var side : Side.values()) result.put(side, getSherd(side)); return result; }
    @Override public java.util.List<Material> getShards() { return snapshot.getSherds().stream().map(CraftMagicNumbers::getMaterial).toList(); }
}
