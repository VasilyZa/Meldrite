package org.bukkit.craftbukkit.v1_20_R1.advancement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.text.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.AdvancementDisplayType;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class CraftAdvancementDisplay implements org.bukkit.advancement.AdvancementDisplay, io.papermc.paper.advancement.AdvancementDisplay {
    private final AdvancementDisplay handle;
    public CraftAdvancementDisplay(AdvancementDisplay handle) { this.handle = java.util.Objects.requireNonNull(handle); }
    public AdvancementDisplay getHandle() { return handle; }
    private static Component component(Text text) { return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text)); }
    @Override public String getTitle() { return CraftChatMessage.fromComponent(handle.getTitle()); }
    @Override public String getDescription() { return CraftChatMessage.fromComponent(handle.getDescription()); }
    @Override public ItemStack getIcon() { return CraftItemStack.asBukkitCopy(handle.getIcon()); }
    @Override public boolean shouldShowToast() { return handle.shouldShowToast(); }
    @Override public boolean shouldAnnounceChat() { return handle.shouldAnnounceToChat(); }
    @Override public boolean isHidden() { return handle.isHidden(); }
    @Override public float getX() { return handle.getX(); }
    @Override public float getY() { return handle.getY(); }
    @Override public AdvancementDisplayType getType() { return AdvancementDisplayType.valueOf(handle.getFrame().name()); }
    @Override public Frame frame() { return Frame.valueOf(handle.getFrame().name()); }
    @Override public Component title() { return component(handle.getTitle()); }
    @Override public Component description() { return component(handle.getDescription()); }
    @Override public ItemStack icon() { return getIcon(); }
    @Override public boolean doesShowToast() { return shouldShowToast(); }
    @Override public boolean doesAnnounceToChat() { return shouldAnnounceChat(); }
    @Override public NamespacedKey backgroundPath() { return handle.getBackground() == null ? null : CraftNamespacedKey.fromMinecraft(handle.getBackground()); }
    @Override public Component displayName() {
        return Component.text().append(Component.text("[")).append(title()).append(Component.text("]")).build()
                .color(frame().color()).hoverEvent(title().append(Component.newline()).append(description()));
    }
}
