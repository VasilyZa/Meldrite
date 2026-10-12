package dev.meldrite.bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Consumer;

/** Prints startup identity once during the version's bootstrap. */
public final class StartupBanner {
    // A JVM property is shared even when the launcher and Fabric use different class loaders.
    private static final String PRINTED_PROPERTY = "meldrite.startupBannerPrinted";

    private StartupBanner() { }

    public static void print(String version, Consumer<String> output) {
        if (Boolean.getBoolean(PRINTED_PROPERTY)) return;
        String art = "Meldrite";
        try (var input = StartupBanner.class.getResourceAsStream("/meldrite/ascii-art.txt")) {
            if (input != null) art = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException unavailable) {
            // A decorative resource must not prevent the server from initializing.
        }
        art.lines().forEach(output);
        boolean chinese = Locale.getDefault(Locale.Category.DISPLAY).getLanguage().equals("zh");
        output.accept((chinese ? "Meldrite 版本: " : "Meldrite Version: ") + version);
        output.accept((chinese ? "JDK 版本: " : "JDK Version: ") + Runtime.version());
        output.accept(chinese ? "作者: Xiaobai（VasilyZa）" : "Author: Xiaobai (VasilyZa)");
        output.accept("\u001b[31m战无不胜的毛泽东思想万岁！\u001b[0m");
        System.setProperty(PRINTED_PROPERTY, "true");
    }
}
