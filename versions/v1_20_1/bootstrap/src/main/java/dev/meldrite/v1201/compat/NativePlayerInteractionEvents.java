package dev.meldrite.v1201.compat;

import java.util.Objects;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

/** Projects native block clicks before either the block or held item can change the world. */
public final class NativePlayerInteractionEvents {
    private NativePlayerInteractionEvents() { }

    /** Returns null only while the Bukkit facade/event environment is not available. */
    public static PlayerInteractEvent block(ServerPlayerEntity player, Hand hand, BlockPos position,
            Direction face, Vec3d hit, boolean rightClick) {
        if (!NativeEntityExtensions.eventsAvailable()) return null;
        Objects.requireNonNull(player);
        Objects.requireNonNull(hand);
        Objects.requireNonNull(position);
        Objects.requireNonNull(face);
        NativeEntityExtensions.requireThread(player);
        var facade = (Player) NativeEntityExtensions.bukkit(player);
        var world = NativeEntityExtensions.world(player.getServerWorld());
        var block = world.getBlockAt(position.getX(), position.getY(), position.getZ());
        var stack = player.getStackInHand(hand);
        // The API event owns a snapshot; a listener may replace the live hand slot.
        var item = stack.isEmpty() ? null : CraftItemStack.asBukkitCopy(stack);
        var relativeHit = hit == null ? null : new Vector(hit.x - position.getX(),
                hit.y - position.getY(), hit.z - position.getZ());
        var event = new PlayerInteractEvent(facade,
                rightClick ? Action.RIGHT_CLICK_BLOCK : Action.LEFT_CLICK_BLOCK,
                item, block, BlockFace.valueOf(face.name()),
                hand == Hand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND, relativeHit);
        facade.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /** Undo client prediction without applying the cancelled block/item operation. */
    public static void resync(ServerPlayerEntity player, BlockPos position, Direction face) {
        NativeEntityExtensions.requireThread(player);
        var world = player.getServerWorld();
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(position, world.getBlockState(position)));
        // Placement and tools can predict a change on the adjacent face as well.
        var adjacent = position.offset(face);
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(adjacent, world.getBlockState(adjacent)));
        player.currentScreenHandler.syncState();
    }
}
