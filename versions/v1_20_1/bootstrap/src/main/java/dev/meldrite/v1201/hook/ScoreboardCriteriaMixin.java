package dev.meldrite.v1201.hook;

import dev.meldrite.v1201.compat.NativeScoreboardBinding;
import java.util.function.Consumer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
abstract class ScoreboardCriteriaMixin {
    @Inject(method = "forEachScore(Lnet/minecraft/scoreboard/ScoreboardCriterion;Ljava/lang/String;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void meldrite$customCriteria(ScoreboardCriterion criteria, String name, Consumer<ScoreboardPlayerScore> update, CallbackInfo callback) {
        NativeScoreboardBinding.updateCustomCriteria((Scoreboard) (Object) this, criteria, name, update);
    }
}
