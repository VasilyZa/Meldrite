package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
public final class CraftMinecartCommand extends CraftMinecart implements org.bukkit.entity.minecart.CommandMinecart {
    public CraftMinecartCommand(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public CommandBlockMinecartEntity getHandle() { return (CommandBlockMinecartEntity) super.getHandle(); }
    private net.minecraft.world.CommandBlockExecutor executor() { return getHandle().getCommandExecutor(); }
    @Override public String getCommand() { return executor().getCommand(); }
    @Override public void setCommand(String command) { requireWritable(); executor().setCommand(java.util.Objects.requireNonNull(command)); }
    @Override public void setName(String name) { requireWritable(); executor().setCustomName(name == null ? null : net.minecraft.text.Text.literal(name)); }
    @Override public net.kyori.adventure.text.Component lastOutput() { var value = executor().getLastOutput(); return value == null ? null : net.kyori.adventure.text.Component.text(value.getString()); }
    @Override public void lastOutput(net.kyori.adventure.text.Component output) { requireWritable(); executor().setLastOutput(output == null ? null : net.minecraft.text.Text.literal(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(output))); }
    @Override public int getSuccessCount() { return executor().getSuccessCount(); }
    @Override public void setSuccessCount(int count) { requireWritable(); executor().setSuccessCount(count); }
}
