package dev.meldrite.v1201.bootstrap;

import com.mojang.brigadier.CommandDispatcher;
import dev.meldrite.v1201.compat.NativeRuntimeAbiAudit;
import dev.meldrite.v1201.compat.NativeAbiAuditExecutor;
import java.io.IOException;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import static net.minecraft.server.command.CommandManager.literal;

/** Native server commands; diagnostics require administrator access. */
final class MeldriteCommands {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("Meldrite.NativeAbi");
    private MeldriteCommands() { }

    static void register(CommandDispatcher<ServerCommandSource> dispatcher, Supplier<String> workerStatus,
                         Supplier<NativeAbiAuditExecutor> audits) {
        dispatcher.register(literal("meldrite")
                .executes(context -> {
                    var source = context.getSource();
                    String usage = "用法：/meldrite version"
                            + (source.hasPermissionLevel(2) ? " | /meldrite workers | /meldrite audit" : "");
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
                        }))
                .then(literal("audit")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> audit(context.getSource(), audits.get()))));
    }

    private static int audit(ServerCommandSource source, NativeAbiAuditExecutor executor) {
        if (executor == null || executor.isClosed()) {
            source.sendError(Text.literal("ABI 审计服务尚未启动或已关闭"));
            return 0;
        }
        var submission = executor.submit(() -> {
            try {
                var report = NativeRuntimeAbiAudit.inspect();
                var output = source.getServer().getRunDirectory().toPath().resolve("reports/meldrite/native-abi.json");
                NativeRuntimeAbiAudit.write(report, output);
                var data = report.document();
                String result = "ABI 结构审计：" + data.get("builtinRequestsChecked") + " 项内置契约，"
                        + data.get("interfaceMethodsChecked") + " 项接口义务，" + data.get("structuralGaps") + " 项结构缺口"
                        + "\n外部请求：已检查 " + data.get("externalRequestsChecked") + "，未检查 " + data.get("externalRequestsSkipped")
                        + "\n状态：" + data.get("status") + "\n行为验证：本次未执行\n报告：" + output.toAbsolutePath();
                if (!executor.isClosed()) source.getServer().execute(() -> {
                    if (executor.isClosed()) return;
                    source.sendFeedback(() -> Text.literal(result), false);
                    if (!report.completeWithoutGaps()) source.sendError(Text.literal("审计存在缺口或未完成项目，详见报告中的 status 和 findings"));
                });
            } catch (IOException failure) { throw new java.io.UncheckedIOException(failure); }
        }, failure -> {
            if (executor.isClosed()) return;
            LOGGER.error("Native ABI audit failed", failure);
            source.getServer().execute(() -> {
                if (!executor.isClosed()) source.sendError(Text.literal("ABI 审计失败：" + failure.getMessage()));
            });
        });
        if (submission == NativeAbiAuditExecutor.Submission.STARTED) {
            source.sendFeedback(() -> Text.literal("已开始 ABI 结构审计，结果将写入 reports/meldrite/native-abi.json"), false);
            return 1;
        }
        source.sendError(Text.literal(submission == NativeAbiAuditExecutor.Submission.BUSY
                ? "已有 ABI 审计正在执行，请等待完成" : "ABI 审计服务已关闭"));
        return 0;
    }

    private static String modVersion(String id) {
        return FabricLoader.getInstance().getModContainer(id)
                .orElseThrow(() -> new IllegalStateException("Missing runtime mod: " + id))
                .getMetadata().getVersion().getFriendlyString();
    }
}
