package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import dev.meldrite.v1201.hook.TextDisplayEntityPaperAccessMixin;
import net.minecraft.text.Text;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Color;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;
public class CraftTextDisplay extends CraftDisplay implements org.bukkit.entity.TextDisplay {
    public CraftTextDisplay(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity getHandle() { return (net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity) super.getHandle(); }
    private TextDisplayEntityPaperAccessMixin access() { return (TextDisplayEntityPaperAccessMixin) getHandle(); }
    @Override public String getText() { return CraftChatMessage.fromComponent(access().meldrite$text()); }
    @Override public void setText(String value) { delegate.requireWritable(); access().meldrite$text(CraftChatMessage.fromString(value, true)[0]); }
    @Override public net.kyori.adventure.text.Component text() { return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(access().meldrite$text())); }
    @Override public void text(net.kyori.adventure.text.Component value) { delegate.requireWritable(); access().meldrite$text(value == null ? Text.empty() : Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(value))); }
    @Override public int getLineWidth() { return access().meldrite$lineWidth(); }
    @Override public void setLineWidth(int value) { delegate.requireWritable(); access().meldrite$lineWidth(value); }
    @Override public Color getBackgroundColor() { int value = access().meldrite$background(); return value == -1 ? null : Color.fromARGB(value); }
    @Override public void setBackgroundColor(Color value) { delegate.requireWritable(); access().meldrite$background(value == null ? -1 : value.asARGB()); }
    @Override public byte getTextOpacity() { return access().meldrite$opacity(); }
    @Override public void setTextOpacity(byte value) { delegate.requireWritable(); access().meldrite$opacity(value); }
    private boolean flag(int mask) { return (access().meldrite$flags() & mask) != 0; }
    private void flag(int mask, boolean value) { delegate.requireWritable(); byte flags = access().meldrite$flags(); access().meldrite$flags((byte) (value ? flags | mask : flags & ~mask)); }
    @Override public boolean isShadowed() { return flag(1); }
    @Override public void setShadowed(boolean value) { flag(1, value); }
    @Override public boolean isSeeThrough() { return flag(2); }
    @Override public void setSeeThrough(boolean value) { flag(2, value); }
    @Override public boolean isDefaultBackground() { return flag(4); }
    @Override public void setDefaultBackground(boolean value) { flag(4, value); }
    @Override public TextAlignment getAlignment() { return TextAlignment.valueOf(net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity.getAlignment(access().meldrite$flags()).name()); }
    @Override public void setAlignment(TextAlignment value) { java.util.Objects.requireNonNull(value, "alignment"); flag(8, value == TextAlignment.LEFT); flag(16, value == TextAlignment.RIGHT); }
}
