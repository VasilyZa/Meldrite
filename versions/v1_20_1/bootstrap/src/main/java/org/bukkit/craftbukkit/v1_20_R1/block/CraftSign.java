package org.bukkit.craftbukkit.v1_20_R1.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;

public class CraftSign extends CraftBlockEntityState<SignBlockEntity> implements Sign {
    private final CraftSignSide front = new CraftSignSide(true);
    private final CraftSignSide back = new CraftSignSide(false);

    public CraftSign(ServerWorld world, BlockPos position, BlockState state, SignBlockEntity sign, boolean useSnapshot) {
        super(world, position, state, sign, useSnapshot);
    }

    @Override public List<Component> lines() { return front.lines(); }
    @Override public Component line(int index) { return front.line(index); }
    @Override public void line(int index, Component line) { front.line(index, line); }
    @Override public String[] getLines() { return front.getLines(); }
    @Override public String getLine(int index) { return front.getLine(index); }
    @Override public void setLine(int index, String line) { front.setLine(index, line); }
    @Override public boolean isEditable() { return !isWaxed(); }
    @Override public void setEditable(boolean editable) { setWaxed(!editable); }
    @Override public boolean isWaxed() { return snapshot.isWaxed(); }
    @Override public void setWaxed(boolean waxed) { snapshot.setWaxed(waxed); }
    @Override public boolean isGlowingText() { return front.isGlowingText(); }
    @Override public void setGlowingText(boolean glowing) { front.setGlowingText(glowing); }
    @Override public DyeColor getColor() { return front.getColor(); }
    @Override public void setColor(DyeColor color) { front.setColor(color); }

    @Override public SignSide getSide(Side side) {
        Objects.requireNonNull(side, "side");
        return side == Side.FRONT ? front : back;
    }

    @Override public Side getInteractableSideFor(double x, double z) {
        org.bukkit.block.data.BlockData data = getBlockData();
        BlockFace face = data instanceof org.bukkit.block.data.Rotatable rotatable
                ? rotatable.getRotation()
                : data instanceof org.bukkit.block.data.Directional directional ? directional.getFacing() : BlockFace.SOUTH;
        double dot = (x - (getX() + 0.5)) * face.getModX() + (z - (getZ() + 0.5)) * face.getModZ();
        return dot >= 0 ? Side.FRONT : Side.BACK;
    }

    private final class CraftSignSide implements SignSide {
        private final boolean isFront;

        private CraftSignSide(boolean isFront) { this.isFront = isFront; }
        private SignText text() { return isFront ? snapshot.getFrontText() : snapshot.getBackText(); }
        private void update(java.util.function.UnaryOperator<SignText> transform) {
            snapshot.setText(transform.apply(text()), isFront);
        }

        @Override public List<Component> lines() {
            List<Component> result = new ArrayList<>(4);
            for (int index = 0; index < 4; index++) result.add(line(index));
            return List.copyOf(result);
        }
        @Override public Component line(int index) {
            checkLine(index);
            return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text().getMessage(index, false)));
        }
        @Override public void line(int index, Component component) {
            checkLine(index);
            Text nativeText = Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(Objects.requireNonNull(component, "component")));
            update(current -> current.withMessage(index, nativeText));
        }
        @Override public String[] getLines() {
            String[] result = new String[4];
            for (int index = 0; index < result.length; index++) result[index] = getLine(index);
            return result;
        }
        @Override public String getLine(int index) {
            checkLine(index);
            return CraftChatMessage.fromComponent(text().getMessage(index, false));
        }
        @Override public void setLine(int index, String line) {
            checkLine(index);
            Text nativeText = CraftChatMessage.fromStringOrNull(line);
            update(current -> current.withMessage(index, nativeText == null ? Text.empty() : nativeText));
        }
        @Override public boolean isGlowingText() { return text().isGlowing(); }
        @Override public void setGlowingText(boolean glowing) { update(current -> current.withGlowing(glowing)); }
        @Override public DyeColor getColor() { return DyeColor.getByWoolData((byte) text().getColor().getId()); }
        @Override public void setColor(DyeColor color) {
            Objects.requireNonNull(color, "color");
            update(current -> current.withColor(net.minecraft.util.DyeColor.byId(color.getWoolData())));
        }
        private void checkLine(int index) {
            if (index < 0 || index >= 4) throw new IndexOutOfBoundsException("Sign line index " + index + " is outside 0..3");
        }
    }
}
