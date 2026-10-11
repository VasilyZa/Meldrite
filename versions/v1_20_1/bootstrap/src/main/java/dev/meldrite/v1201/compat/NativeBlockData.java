package dev.meldrite.v1201.compat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.LinkedHashMap;
import java.util.Objects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;

/** Uses the game's registry-aware parser, including properties registered by Fabric mods. */
public final class NativeBlockData {
    private NativeBlockData() { }

    public static CraftBlockData parse(Material material, String data) {
        Block block = material == null ? null : block(material);
        if (data == null && block == null) throw new IllegalArgumentException("A block material or block data string is required");
        String input = block == null ? data : Registries.BLOCK.getId(block) + (data == null ? "" : data);
        try {
            StringReader reader = new StringReader(input);
            var result = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), reader, false);
            if (reader.canRead()) throw new IllegalArgumentException("Spurious trailing block data: " + input);
            return CraftBlockData.createData(result.blockState(), new LinkedHashMap<>(result.properties()));
        } catch (CommandSyntaxException failure) {
            throw new IllegalArgumentException("Could not parse block data: " + input, failure);
        }
    }

    public static BlockState require(BlockData data) {
        Objects.requireNonNull(data, "blockData");
        if (!(data instanceof CraftBlockData craft)) throw new IllegalArgumentException("BlockData has no native state: " + data.getClass().getName());
        return craft.getState();
    }

    public static Block block(Material material) {
        Objects.requireNonNull(material, "material");
        if (!material.isBlock()) throw new IllegalArgumentException("Material is not a block: " + material);
        if (material.isLegacy()) return NativeLegacyConversion.block(material, (byte) 0).getBlock();
        var key = material.getKey();
        Identifier id = new Identifier(key.getNamespace(), key.getKey());
        return Registries.BLOCK.getOrEmpty(id).orElseThrow(() -> new IllegalArgumentException("Material has no native block: " + id));
    }
}
