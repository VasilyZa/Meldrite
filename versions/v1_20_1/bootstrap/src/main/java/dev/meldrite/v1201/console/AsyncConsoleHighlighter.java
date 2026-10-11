package dev.meldrite.v1201.console;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.utils.AttributedString;

/** Coalesces redraws; neither the input nor logging thread enters game command parsers. */
public final class AsyncConsoleHighlighter extends DefaultHighlighter {
    private final Highlighter delegate;
    private final AtomicReference<Request> requested = new AtomicReference<>();
    private volatile Result result;
    public AsyncConsoleHighlighter(Highlighter delegate) { this.delegate = delegate; }

    @Override public AttributedString highlight(LineReader reader, String buffer) {
        if (reader != null && (reader.getSearchTerm() != null || reader.getRegionActive() != LineReader.RegionType.NONE))
            return super.highlight(reader, buffer);
        if (buffer.isEmpty()) return AttributedString.EMPTY;
        var cached = result;
        if (cached != null && cached.buffer.equals(buffer)) return cached.text;
        requested.set(new Request(reader, buffer));
        return new AttributedString(buffer);
    }

    public void update() {
        var request = requested.getAndSet(null);
        if (request == null) return;
        var cached = result;
        if (cached != null && cached.buffer.equals(request.buffer)) return;
        AttributedString text;
        try { text = delegate.highlight(null, request.buffer); }
        catch (RuntimeException unavailable) { text = new AttributedString(request.buffer); }
        result = new Result(request.buffer, text);
        if (request.reader != null) ForkJoinPool.commonPool().execute(() -> {
            try {
                if (request.reader.isReading()) request.reader.callWidget(LineReader.REDISPLAY);
            } catch (IllegalStateException finished) { }
        });
    }

    private record Request(LineReader reader, String buffer) {}
    private record Result(String buffer, AttributedString text) {}
}
