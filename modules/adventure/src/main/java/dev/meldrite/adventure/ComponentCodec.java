package dev.meldrite.adventure;
import java.util.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
public final class ComponentCodec {
    public record LegacyResult(String text,boolean potentiallyLossy){}
    public String json(Component component){return GsonComponentSerializer.gson().serialize(Objects.requireNonNull(component));}
    public Component fromJson(String json){return GsonComponentSerializer.gson().deserialize(Objects.requireNonNull(json));}
    public LegacyResult legacy(Component component){return new LegacyResult(LegacyComponentSerializer.legacySection().serialize(component),true);}
    public Component fromLegacy(String text){return LegacyComponentSerializer.legacySection().deserialize(text);}
    public Component template(String trustedTemplate,Map<String,Component> parameters){
        TagResolver.Builder resolver=TagResolver.builder();parameters.forEach((name,value)->resolver.resolver(Placeholder.component(name,value)));
        return MiniMessage.miniMessage().deserialize(trustedTemplate,resolver.build());
    }
}
