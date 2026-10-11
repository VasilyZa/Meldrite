package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.console.ConsoleOptions;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Main.class)
public abstract class MainConsoleOptionsMixin {
    @ModifyVariable(method = "main", at = @At("HEAD"), argsOnly = true)
    private static String[] meldrite$consoleOptions(String[] arguments) { return ConsoleOptions.configure(arguments); }
}
