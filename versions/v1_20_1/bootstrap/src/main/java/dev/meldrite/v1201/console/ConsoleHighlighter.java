package dev.meldrite.v1201.console;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.function.Function;
import org.jline.reader.LineReader;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.*;

/** Uses the live command grammar, including Mod and plugin registrations. */
public final class ConsoleHighlighter extends DefaultHighlighter {
    private static final int[] COLORS = {AttributedStyle.CYAN, AttributedStyle.YELLOW, AttributedStyle.GREEN,
            AttributedStyle.MAGENTA, AttributedStyle.BLUE};
    private final Function<String, ParseResults<?>> parser;
    public ConsoleHighlighter(Function<String, ParseResults<?>> parser) { this.parser = parser; }

    @Override public AttributedString highlight(LineReader reader, String buffer) {
        if (reader != null && (reader.getSearchTerm() != null || reader.getRegionActive() != LineReader.RegionType.NONE))
            return super.highlight(reader, buffer);
        try {
            var parsed = parser.apply(buffer);
            if (parsed == null) return new AttributedString(buffer);
            var output = new AttributedStringBuilder();
            int position = 0, color = 0;
            for (CommandContextBuilder<?> context = parsed.getContext(); context != null; context = context.getChild()) {
                for (ParsedCommandNode<?> node : context.getNodes()) {
                    int start = node.getRange().getStart(), end = node.getRange().getEnd();
                    if (start < position || end > buffer.length()) continue;
                    output.append(buffer.substring(position, start), AttributedStyle.DEFAULT);
                    var style = node.getNode() instanceof LiteralCommandNode<?> ? AttributedStyle.DEFAULT
                            : AttributedStyle.DEFAULT.foreground(COLORS[color++ % COLORS.length]);
                    output.append(buffer.substring(start, end), style);
                    position = end;
                }
            }
            output.append(buffer.substring(position), AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            return output.toAttributedString();
        } catch (RuntimeException unavailable) { return new AttributedString(buffer); }
    }
}
