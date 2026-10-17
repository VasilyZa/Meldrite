package dev.meldrite.v1201.bootstrap;

import com.mojang.brigadier.CommandDispatcher;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

/** Native server commands; only worker diagnostics require administrator access. */
final class MeldriteCommands {
    private MeldriteCommands() { }

    static void register(CommandDispatcher<ServerCommandSource> dispatcher, Supplier<String> workerStatus) {
        dispatcher.register(literal("meldrite")
                .executes(context -> {
                    var source = context.getSource();
                    String usage = "用法：/meldrite version"
                            + (source.hasPermissionLevel(2) ? " | /meldrite workers" : "");
                    source.sendFeedback(() -> Text.literal(usage), false);
                    return 1;
                })
                .then(literal("version").executes(context -> {
                    String version = "Meldrite: " + modVersion("meldrite")
                            + "\nMinecraft: " + modVersion("minecraft")
                            + "\nFabric Loader: " + modVersion("fabricloader")
                            + "\nJava: " + Runtime.version();
                    context.getSource().sendFeedback(() -> Text.literal(version), false);
                    return 1;
                }))
                .then(literal("workers")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            String status = workerStatus.get();
                            context.getSource().sendFeedback(() -> Text.literal(status == null
                                    ? "Meldrite workers are not initialized" : status), false);
                            return status == null ? 0 : 1;
                        })));
    }

    private static String modVersion(String id) {
        return FabricLoader.getInstance().getModContainer(id)
                .orElseThrow(() -> new IllegalStateException("Missing runtime mod: " + id))
                .getMetadata().getVersion().getFriendlyString();
    }
}
