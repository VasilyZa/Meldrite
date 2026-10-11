package org.bukkit.craftbukkit.v1_20_R1.block.sign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;
import org.bukkit.DyeColor;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftChatMessage;

/** Mutable view over one native sign side, retained for legacy reflective callers. */
public class CraftSignSide implements SignSide {
    private SignText signText;
    public CraftSignSide(SignText signText) { this.signText = Objects.requireNonNull(signText, "signText"); }
    public SignText getSignText() { return signText; }
    @Override public List<net.kyori.adventure.text.Component> lines() {
        List<net.kyori.adventure.text.Component> result = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) result.add(line(index));
        return List.copyOf(result);
    }
    @Override public net.kyori.adventure.text.Component line(int index) {
        check(index);
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(signText.getMessage(index, false)));
    }
    @Override public void line(int index, net.kyori.adventure.text.Component value) {
        check(index);
        Text nativeText = Text.Serializer.fromJson(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(Objects.requireNonNull(value, "value")));
        signText = signText.withMessage(index, nativeText);
    }
    @Override public String[] getLines() { String[] result = new String[4]; for (int i = 0; i < 4; i++) result[i] = getLine(i); return result; }
    @Override public String getLine(int index) { check(index); return CraftChatMessage.fromComponent(signText.getMessage(index, false)); }
    @Override public void setLine(int index, String line) { check(index); Text value = CraftChatMessage.fromStringOrNull(line); signText = signText.withMessage(index, value == null ? Text.empty() : value); }
    @Override public boolean isGlowingText() { return signText.isGlowing(); }
    @Override public void setGlowingText(boolean glowing) { signText = signText.withGlowing(glowing); }
    @Override public DyeColor getColor() { return DyeColor.getByWoolData((byte) signText.getColor().getId()); }
    @Override public void setColor(DyeColor color) { signText = signText.withColor(net.minecraft.util.DyeColor.byId(Objects.requireNonNull(color).getWoolData())); }
    private static void check(int index) { if (index < 0 || index >= 4) throw new IndexOutOfBoundsException("Sign line index " + index); }
}
