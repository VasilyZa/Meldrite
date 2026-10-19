package dev.meldrite.paper.map;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import dev.meldrite.scheduler.ParallelRangeExecutor;
import dev.meldrite.scheduler.WorkerSettings;
import dev.meldrite.scheduler.ComputeService;

/** Private pixel snapshots can use bounded parallelism and optional SIMD without game callbacks. */
public final class VectorMapColors {
    public interface Converter {
        int vectorBits();
        void convert(int[] pixels, Color[] palette, byte[] result, int from, int to);
    }

    public interface MatchingConverter extends Converter {
        int matchColor(int red, int green, int blue, int[] paletteRed, int[] paletteGreen, int[] paletteBlue);
    }

    /** Standard Color instances are immutable; replacements in the API array are checked on every use. */
    static final class PaletteSnapshot {
        final Color[] entries;
        final int[] red, green, blue;

        PaletteSnapshot(Color[] entries, boolean standard) {
            this.entries = entries;
            if (!standard) { red = null; green = null; blue = null; return; }
            red = new int[entries.length]; green = new int[entries.length]; blue = new int[entries.length];
            for (int i = 4; i < entries.length; i++) {
                int rgb = entries[i].getRGB();
                red[i] = (rgb >>> 16) & 255; green[i] = (rgb >>> 8) & 255; blue[i] = rgb & 255;
            }
        }

        boolean matches(Color[] palette) {
            if (entries.length != palette.length) return false;
            for (int i = 4; i < entries.length; i++) if (entries[i] != palette[i]) return false;
            return true;
        }
    }

    private static final Converter CONVERTER = load();
    private static final Converter SCALAR = new ScalarMapColorConverter();
    private static volatile PaletteSnapshot lastPalette;
    private VectorMapColors() { }

    private static Converter load() {
        if (Boolean.getBoolean("meldrite.vector.disabled")
                || ModuleLayer.boot().findModule("jdk.incubator.vector").isEmpty()) return null;
        try {
            var type = Class.forName("dev.meldrite.paper.map.VectorMapColorConverter", true,
                    VectorMapColors.class.getClassLoader());
            var converter = (Converter) type.getConstructor().newInstance();
            return converter.vectorBits() >= 128 ? converter : null;
        } catch (ReflectiveOperationException | LinkageError unavailable) {
            return null;
        }
    }

    public static boolean enabled() { return CONVERTER != null; }
    public static int vectorBits() { return enabled() ? CONVERTER.vectorBits() : 0; }

    static PaletteSnapshot paletteSnapshot(Color[] palette) {
        if (palette == null) return null;
        PaletteSnapshot previous = lastPalette;
        if (previous != null && previous.matches(palette)) return previous.red == null ? null : previous;
        Color[] entries = palette.clone();
        boolean standard = true;
        for (int i = 4; i < entries.length; i++)
            if (entries[i] == null || entries[i].getClass() != Color.class) { standard = false; break; }
        PaletteSnapshot snapshot = new PaletteSnapshot(entries, standard);
        lastPalette = snapshot;
        return standard ? snapshot : null;
    }

    /** A negative result delegates to Paper, including custom Color implementations. */
    public static int matchColor(Color color, Color[] palette) {
        if (!(CONVERTER instanceof MatchingConverter matching) || color.getClass() != Color.class) return -1;
        int rgb = color.getRGB();
        if ((rgb >>> 24) < 128) return 0;
        PaletteSnapshot colors = paletteSnapshot(palette);
        if (colors == null) return -1;
        return matching.matchColor((rgb >>> 16) & 255, (rgb >>> 8) & 255, rgb & 255,
                colors.red, colors.green, colors.blue) & 255;
    }

    public static synchronized void configureWorkers(WorkerSettings settings) {
        ComputeService.configure(settings);
    }
    public static void closeWorkers() { ComputeService.close(); }
    public static ParallelRangeExecutor.Statistics workerStatistics() { return ComputeService.statistics(); }

    /** Null tells the API projection to use its original scalar implementation. */
    public static byte[] imageToBytes(Image image, Color[] palette) {
        int minimumBatch = ComputeService.minimumBatch();
        int width = image.getWidth(null), height = image.getHeight(null);
        if (!enabled() && (ComputeService.statistics().parallelism() == 1
                || (long) width * height < 2L * minimumBatch)) return null;
        // Image/Graphics access stays on the caller; workers only see this finished snapshot.
        var temporary = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = temporary.createGraphics();
        try { graphics.drawImage(image, 0, 0, null); }
        finally { graphics.dispose(); }
        int[] pixels = temporary.getRGB(0, 0, width, height, null, 0, width);
        byte[] result = new byte[pixels.length];
        Color[] colors = palette.clone();
        Converter converter = enabled() ? CONVERTER : SCALAR;
        ComputeService.forEach(pixels.length, minimumBatch,
                (from, to) -> converter.convert(pixels, colors, result, from, to));
        return result;
    }
}
