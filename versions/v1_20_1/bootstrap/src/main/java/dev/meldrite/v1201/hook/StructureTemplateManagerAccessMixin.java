package dev.meldrite.v1201.hook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructureTemplateManager.class)
public interface StructureTemplateManagerAccessMixin {
    @Accessor("templates") Map<Identifier, Optional<StructureTemplate>> meldrite$templates();
    @Invoker("loadTemplate") Optional<StructureTemplate> meldrite$load(Identifier id);
    @Invoker("readTemplate") StructureTemplate meldrite$read(InputStream input) throws IOException;
}
