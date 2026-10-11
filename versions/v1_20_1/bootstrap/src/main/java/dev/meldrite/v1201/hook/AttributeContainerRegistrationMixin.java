package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeAttributeRegistration;
import java.util.Map;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AttributeContainer.class)
public abstract class AttributeContainerRegistrationMixin implements NativeAttributeRegistration {
    @Shadow @Final private Map<EntityAttribute, EntityAttributeInstance> custom;
    @Shadow protected abstract void updateTrackedStatus(EntityAttributeInstance instance);
    @Override public void registerAttribute(EntityAttribute attribute) {
        custom.computeIfAbsent(attribute, key -> {
            var instance = new EntityAttributeInstance(key, this::updateTrackedStatus);
            updateTrackedStatus(instance); return instance;
        });
    }
}
