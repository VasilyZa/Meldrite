package org.bukkit.craftbukkit.v1_20_R1.entity;
import dev.meldrite.craft.EntityDelegate;
import dev.meldrite.paper.facade.FacadeContext;
import dev.meldrite.v1201.bridge.NativeEntityPort;
public class CraftGlowItemFrame extends CraftItemFrame implements org.bukkit.entity.GlowItemFrame {
    public CraftGlowItemFrame(EntityDelegate delegate, FacadeContext context, NativeEntityPort entities) { super(delegate, context, entities); }
    @Override public net.minecraft.entity.decoration.GlowItemFrameEntity getHandle() { return (net.minecraft.entity.decoration.GlowItemFrameEntity) super.getHandle(); }
}
