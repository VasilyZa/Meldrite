package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(DisplayEntity.TextDisplayEntity.class)
public interface TextDisplayEntityPaperAccessMixin {
    @Invoker("getText") Text meldrite$text();
    @Invoker("setText") void meldrite$text(Text value);
    @Invoker("getLineWidth") int meldrite$lineWidth();
    @Invoker("setLineWidth") void meldrite$lineWidth(int value);
    @Invoker("getTextOpacity") byte meldrite$opacity();
    @Invoker("setTextOpacity") void meldrite$opacity(byte value);
    @Invoker("getBackground") int meldrite$background();
    @Invoker("setBackground") void meldrite$background(int value);
    @Invoker("getDisplayFlags") byte meldrite$flags();
    @Invoker("setDisplayFlags") void meldrite$flags(byte value);
}
