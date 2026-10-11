package dev.meldrite.v1201.hook;

import java.util.Map;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DefaultAttributeContainer.class)
public interface DefaultAttributeContainerAccessor {
    @Accessor("instances") Map<EntityAttribute, EntityAttributeInstance> meldrite$getInstances();
}
