package runnersim.physics;

/**
 * Compares a sprint time with the fastest one ever run, so impossible lab data stands out.
 *
 * Usain Bolt's official 10 m splits from his 9.58 s world record (Berlin, 2009),
 * minus his 0.146 s reaction time, give his time from first movement to each mark.
 * A stopwatch started when the runner moves is measuring the same thing.
 */
public final class RealityCheck {

    private RealityCheck() { }

    private static final double BOLT_REACTION_S = 0.146;
    private static final double[] SPLIT_DISTANCES_M = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
    private static final double[] BOLT_SPLITS_S = {BOLT_REACTION_S, 1.89, 2.88, 3.78, 4.64, 5.47, 6.29, 7.10, 7.92, 8.75, 9.58};

    /** The fastest human top speed on record, about 12.4 m/s (Bolt, 2009). */
    public static final double FASTEST_HUMAN_SPEED_MPS = 12.4;

    /**
     * Bolt's time from first movement to this distance, or NaN outside 0–100 m.
     * Between split marks it interpolates in a straight line.
     */
    public static double boltTimeS(double distanceM) {
        if (distanceM <= 0 || distanceM > 100) return Double.NaN;
        for (int i = 1; i < SPLIT_DISTANCES_M.length; i++) {
            if (distanceM <= SPLIT_DISTANCES_M[i]) {
                double d0 = SPLIT_DISTANCES_M[i - 1], d1 = SPLIT_DISTANCES_M[i];
                double t0 = BOLT_SPLITS_S[i - 1], t1 = BOLT_SPLITS_S[i];
                double t = t0 + (t1 - t0) * (distanceM - d0) / (d1 - d0);
                return t - BOLT_REACTION_S;
            }
        }
        return Double.NaN;
    }

    /** True when a time is faster than Bolt's for the same distance. */
    public static boolean fasterThanBolt(double distanceM, double timeS) {
        double bolt = boltTimeS(distanceM);
        return !Double.isNaN(bolt) && timeS < bolt;
    }
}
