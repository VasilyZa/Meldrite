package dev.meldrite.v1201.perf;

/** Immutable exact nearest-point index with vanilla float distance and stable ties. */
public final class FloatSpatialIndex {
    private final int[] x, y, z, order, left, right;
    private final int root;
    private final int[] minimum = new int[3], maximum = new int[3];
    public FloatSpatialIndex(int[] x, int[] y, int[] z) {
        if (x.length == 0 || x.length != y.length || x.length != z.length)
            throw new IllegalArgumentException("Invalid point snapshot");
        this.x = x.clone(); this.y = y.clone(); this.z = z.clone();
        order = new int[x.length]; left = new int[x.length]; right = new int[x.length];
        java.util.Arrays.fill(left, -1); java.util.Arrays.fill(right, -1);
        for (int i = 0; i < order.length; i++) order[i] = i;
        java.util.Arrays.fill(minimum, Integer.MAX_VALUE);
        java.util.Arrays.fill(maximum, Integer.MIN_VALUE);
        for (int i = 0; i < order.length; i++) for (int axis = 0; axis < 3; axis++) {
            int coordinate = coordinate(i, axis);
            minimum[axis] = Math.min(minimum[axis], coordinate);
            maximum[axis] = Math.max(maximum[axis], coordinate);
        }
        root = build(0, order.length, 0);
    }
    private int coordinate(int index, int axis) { return axis == 0 ? x[index] : axis == 1 ? y[index] : z[index]; }
    private int compare(int first, int second, int axis) {
        int result = Integer.compare(coordinate(first, axis), coordinate(second, axis));
        return result == 0 ? Integer.compare(first, second) : result;
    }
    private int build(int from, int to, int axis) {
        if (from >= to) return -1;
        int middle = (from + to) >>> 1;
        int lo = from, hi = to - 1;
        while (lo < hi) {
            int pivot = order[(lo + hi) >>> 1], i = lo, j = hi;
            while (i <= j) {
                while (compare(order[i], pivot, axis) < 0) i++;
                while (compare(order[j], pivot, axis) > 0) j--;
                if (i <= j) { int value = order[i]; order[i++] = order[j]; order[j--] = value; }
            }
            if (middle <= j) hi = j;
            else if (middle >= i) lo = i;
            else break;
        }
        int node = order[middle];
        left[node] = build(from, middle, (axis + 1) % 3);
        right[node] = build(middle + 1, to, (axis + 1) % 3);
        return node;
    }
    public static float distance(int x, int y, int z, int tx, int ty, int tz) {
        float dx = tx - x, dy = ty - y, dz = tz - z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    public int nearest(int x, int y, int z) {
        for (int axis = 0; axis < 3; axis++) {
            int query = axis == 0 ? x : axis == 1 ? y : z;
            if ((long) minimum[axis] - query < Integer.MIN_VALUE
                    || (long) maximum[axis] - query > Integer.MAX_VALUE) {
                // Vanilla subtracts int coordinates before converting to float.
                // Its overflow metric cannot be pruned using spatial bounds.
                int best = 0;
                for (int i = 1; i < this.x.length; i++)
                    if (distance(x, y, z, this.x[i], this.y[i], this.z[i])
                            < distance(x, y, z, this.x[best], this.y[best], this.z[best])) best = i;
                return best;
            }
        }
        return nearest(root, x, y, z, 0, root);
    }
    public float distanceToNearest(int x, int y, int z) {
        int index = nearest(x, y, z);
        return distance(x, y, z, this.x[index], this.y[index], this.z[index]);
    }
    private int nearest(int node, int qx, int qy, int qz, int axis, int best) {
        if (node < 0) return best;
        float candidate = distance(qx, qy, qz, x[node], y[node], z[node]);
        float previous = distance(qx, qy, qz, x[best], y[best], z[best]);
        if (candidate < previous || candidate == previous && node < best) best = node;
        int query = axis == 0 ? qx : axis == 1 ? qy : qz;
        int split = coordinate(node, axis);
        boolean before = query < split;
        best = nearest(before ? left[node] : right[node], qx, qy, qz, (axis + 1) % 3, best);
        // Include equal rounded distances: the earlier input point must win ties.
        float delta = split - query;
        if ((float) Math.sqrt(delta * delta) <= distance(qx, qy, qz, x[best], y[best], z[best]))
            best = nearest(before ? right[node] : left[node], qx, qy, qz, (axis + 1) % 3, best);
        return best;
    }
}
