package dev.meldrite.v1201.compat;

import java.util.List;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Decoration;
import net.minecraft.text.Style;
import net.minecraft.util.Identifier;

/** The native dynamic chat registry lets unchanged clients render plugin-formatted signed chat without another prefix. */
public final class NativeChatTypes {
    public static final RegistryKey<MessageType> RAW = RegistryKey.of(RegistryKeys.MESSAGE_TYPE, new Identifier("meldrite", "raw"));
    private NativeChatTypes() { }

    public static void bootstrap(Registerable<MessageType> registry) {
        var raw = new Decoration("%s", List.of(Decoration.Parameter.CONTENT), Style.EMPTY);
        registry.register(RAW, new MessageType(raw, raw));
    }
}
