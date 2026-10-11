package dev.meldrite.v1201.hook;

import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

/** Header/footer aliases used by older packet adapters. */
@Mixin(PlayerListHeaderS2CPacket.class)
public abstract class LegacyPlayerListHeaderAliasesMixin {
    public Text header() {
        return ((PlayerListHeaderS2CPacket) (Object) this).getHeader();
    }

    public Text footer() {
        return ((PlayerListHeaderS2CPacket) (Object) this).getFooter();
    }
}
