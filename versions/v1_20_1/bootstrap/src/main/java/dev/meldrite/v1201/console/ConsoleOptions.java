package dev.meldrite.v1201.console;

import java.util.ArrayList;

/** Removes only our options before vanilla parses its own command line. */
public final class ConsoleOptions {
    private ConsoleOptions() {}
    public static String[] configure(String[] arguments) {
        var game = new ArrayList<String>();
        for (String argument : arguments) {
            if (argument.equals("--nojline") || argument.equals("-nojline")) {
                System.setProperty("meldrite.console.jline", "false");
            } else if (argument.equals("--noconsole") || argument.equals("-noconsole")) {
                System.setProperty("meldrite.console.enabled", "false");
                System.setProperty("meldrite.console.jline", "false");
            } else game.add(argument);
        }
        return game.toArray(String[]::new);
    }
    public static boolean enabled() { return Boolean.parseBoolean(System.getProperty("meldrite.console.enabled", "true")); }
    public static boolean interactive() { return enabled() && Boolean.parseBoolean(System.getProperty("meldrite.console.jline", "true")); }
}
