package dev.meldrite.v1201.compat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent;
public final class NativeDecorationEvents {
    private NativeDecorationEvents() {}
    public static boolean canBreak(AbstractDecorationEntity entity, DamageSource source, HangingBreakEvent.RemoveCause cause) {
        if (!NativeEntityExtensions.eventsAvailable() || !NativeEntityExtensions.state(entity).meldrite$isTracked()) return true;
        Entity damager = source == null ? null : source.isIndirect() ? source.getAttacker() : source.getSource();
        var api = (org.bukkit.entity.Hanging) NativeEntityExtensions.bukkit(entity);
        HangingBreakEvent event = damager == null ? new HangingBreakEvent(api, cause) : new HangingBreakByEntityEvent(api, NativeEntityExtensions.bukkit(damager), cause);
        org.bukkit.Bukkit.getPluginManager().callEvent(event); return !event.isCancelled() && !entity.isRemoved();
    }
    public static PlayerItemFrameChangeEvent itemChange(ItemFrameEntity frame, PlayerEntity player, net.minecraft.item.ItemStack item, PlayerItemFrameChangeEvent.ItemFrameChangeAction action) {
        if (!NativeEntityExtensions.eventsAvailable() || !NativeEntityExtensions.state(frame).meldrite$isTracked()) return null;
        var event = new PlayerItemFrameChangeEvent((org.bukkit.entity.Player) NativeEntityExtensions.bukkit(player), (org.bukkit.entity.ItemFrame) NativeEntityExtensions.bukkit(frame), org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack.asBukkitCopy(item), action);
        org.bukkit.Bukkit.getPluginManager().callEvent(event); return event;
    }
}
