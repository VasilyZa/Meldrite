package dev.meldrite.v1201.hook;
import java.util.Map;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(FeatureManager.class)
public interface FeatureManagerPaperAccessMixin { @Accessor("featureFlags") Map<Identifier, FeatureFlag> meldrite$getFeatureFlags(); }
