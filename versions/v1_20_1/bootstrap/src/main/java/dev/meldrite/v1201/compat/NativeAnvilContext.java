package dev.meldrite.v1201.compat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandler;

/** Selects the native anvil associated with the synchronous Bukkit event. */
public final class NativeAnvilContext {
    private static final ThreadLocal<Deque<AnvilScreenHandler>> HANDLERS = new ThreadLocal<>();
    private NativeAnvilContext() {}

    public static AnvilScreenHandler current() {
        Deque<AnvilScreenHandler> handlers = HANDLERS.get();
        return handlers == null ? null : handlers.peek();
    }

    public static <T> T withHandlerResult(ScreenHandler handler, Supplier<T> action) {
        if (!(handler instanceof AnvilScreenHandler anvil)) return action.get();
        Deque<AnvilScreenHandler> handlers = HANDLERS.get();
        if (handlers == null) {
            handlers = new ArrayDeque<>();
            HANDLERS.set(handlers);
        }
        handlers.push(anvil);
        try {
            return action.get();
        } finally {
            handlers.pop();
            if (handlers.isEmpty()) HANDLERS.remove();
        }
    }

    public static void withHandler(ScreenHandler handler, Runnable action) {
        withHandlerResult(handler, () -> { action.run(); return null; });
    }
}
