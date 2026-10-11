package dev.meldrite.v1201.console;

import java.util.regex.Pattern;

/** Converts Minecraft text formatting for terminals, and strips it for plain output. */
public final class ConsoleText {
    private static final Pattern ANSI = Pattern.compile("\u001b(?:\\[[0-?]*[ -/]*[@-~]|\\][^\u0007\u001b]*(?:\u0007|\u001b\\\\))");
    private static final int[] COLORS = {30, 34, 32, 36, 31, 35, 33, 37, 90, 94, 92, 96, 91, 95, 93, 97};
    private ConsoleText() {}

    public static String format(String input, boolean ansi) {
        String text = ansi ? input : ANSI.matcher(input).replaceAll("");
        var result = new StringBuilder(text.length());
        boolean styled = false;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current != '§' || i + 1 == text.length()) { result.append(current); continue; }
            char code = Character.toLowerCase(text.charAt(i + 1));
            if (code == 'x' && i + 13 < text.length()) {
                int rgb = 0; boolean valid = true;
                for (int digit = 0; digit < 6; digit++) {
                    int position = i + 2 + digit * 2;
                    int value = Character.digit(text.charAt(position + 1), 16);
                    if (text.charAt(position) != '§' || value < 0) { valid = false; break; }
                    rgb = rgb * 16 + value;
                }
                if (valid) {
                    if (ansi) result.append("\u001b[0;38;2;").append(rgb >> 16).append(';').append((rgb >> 8) & 255).append(';').append(rgb & 255).append('m');
                    styled |= ansi; i += 13; continue;
                }
            }
            int color = Character.digit(code, 16);
            String style = switch (code) {
                case 'k' -> ""; case 'l' -> "1"; case 'm' -> "9"; case 'n' -> "4"; case 'o' -> "3"; case 'r' -> "0";
                default -> null;
            };
            if (color >= 0 || style != null) {
                if (ansi && (color >= 0 || !style.isEmpty())) {
                    result.append("\u001b[").append(color >= 0 ? "0;" + COLORS[color] : style).append('m');
                    styled = true;
                }
                i++; continue;
            }
            result.append(current);
        }
        if (ansi && (styled || text.indexOf('\u001b') >= 0)) {
            int end = result.length();
            while (end > 0 && (result.charAt(end - 1) == '\n' || result.charAt(end - 1) == '\r')) end--;
            result.insert(end, "\u001b[0m");
        }
        return result.toString();
    }
}
