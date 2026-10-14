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

    private static final Converter CONVERTER = load();
    private static final Converter SCALAR = new ScalarMapColorConverter();
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
