package dev.meldrite.v1201.hook;
import net.minecraft.entity.decoration.InteractionEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(InteractionEntity.class)
public interface InteractionEntityPaperAccessMixin {
    @Invoker("setInteractionWidth") void meldrite$setWidth(float value);
    @Invoker("getInteractionWidth") float meldrite$getWidth();
    @Invoker("setInteractionHeight") void meldrite$setHeight(float value);
    @Invoker("getInteractionHeight") float meldrite$getHeight();
    @Invoker("setResponse") void meldrite$setResponse(boolean value);
    @Invoker("shouldRespond") boolean meldrite$getResponse();
    @Accessor("attack") InteractionEntity.Interaction meldrite$attack();
    @Accessor("interaction") InteractionEntity.Interaction meldrite$interaction();
}
