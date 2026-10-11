package dev.meldrite.paper.facade;

import java.util.Map;
import org.bukkit.Color;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class LeatherItemMeta extends org.bukkit.craftbukkit.inventory.CraftMetaItem implements LeatherArmorMeta {
    private static final Color DEFAULT_COLOR = Color.fromRGB(0xA06540);
    protected LeatherItemMeta() { }
    protected LeatherItemMeta(LeatherItemMeta source) { super(source); }
    @Override public Color getColor() { Object value = ItemNbtData.compound(mutableTag().get("display")).get("color"); return value instanceof Number color ? Color.fromRGB(color.intValue()) : DEFAULT_COLOR; }
    @Override public void setColor(Color value) {
        if (value == null || DEFAULT_COLOR.equals(value)) {
            Map<String, Object> display = ItemNbtData.compound(mutableTag().get("display"));
            if (!display.isEmpty()) { display.remove("color"); if (display.isEmpty()) mutableTag().remove("display"); }
        } else mutableCompound("display").put("color", value.asRGB());
    }
    @Override public LeatherItemMeta clone() { return new LeatherItemMeta(this); }
    @Override public Map<String, Object> serialize() { Map<String, Object> result = super.serialize(); result.put("meta-type", "LEATHER_ARMOR"); if (!DEFAULT_COLOR.equals(getColor())) result.put("color", getColor()); return result; }
}
