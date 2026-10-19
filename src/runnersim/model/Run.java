package runnersim.model;

import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

/**
 * A single run: total distance and time, optional splits, and optional watch metrics.
 * When splits are given, the totals are the sum of the splits, so the two can never disagree.
 */
public final class Run {

    private final double distanceM;
    private final double timeS;
    private final List<Split> splits;
    private final OptionalDouble cadenceSpm;
    private final OptionalDouble verticalOscillationCm;

    private Run(double distanceM, double timeS, List<Split> splits,
                OptionalDouble cadenceSpm, OptionalDouble verticalOscillationCm) {
        if (distanceM <= 0) throw new IllegalArgumentException("Distance must be positive.");
        if (timeS <= 0) throw new IllegalArgumentException("Time must be positive.");
        cadenceSpm.ifPresent(c -> {
            if (c < 60 || c > 260) throw new IllegalArgumentException("Cadence should be between 60 and 260 steps per minute.");
        });
        verticalOscillationCm.ifPresent(v -> {
            if (v < 1 || v > 25) throw new IllegalArgumentException("Vertical oscillation should be between 1 and 25 cm.");
        });
        this.distanceM = distanceM;
        this.timeS = timeS;
        this.splits = List.copyOf(splits);
        this.cadenceSpm = cadenceSpm;
        this.verticalOscillationCm = verticalOscillationCm;
    }

    /** A run with only totals. */
    public static Run of(double distanceM, double timeS, OptionalDouble cadenceSpm, OptionalDouble verticalOscillationCm) {
        return new Run(distanceM, timeS, Collections.emptyList(), cadenceSpm, verticalOscillationCm);
    }

    /** A run built from splits. Distance and time are their sums. */
    public static Run fromSplits(List<Split> splits, OptionalDouble cadenceSpm, OptionalDouble verticalOscillationCm) {
        if (splits == null || splits.isEmpty()) throw new IllegalArgumentException("Add at least one split.");
        double d = 0, t = 0;
        for (Split s : splits) { d += s.distanceM(); t += s.timeS(); }
        return new Run(d, t, splits, cadenceSpm, verticalOscillationCm);
    }

    /**
     * How many splits a run needs: one per full split length, plus one for any leftover distance.
     * A 5000 m run with 1000 m splits needs 5; a 5200 m run needs 6, the last one 200 m.
     */
    public static int splitCount(double totalDistanceM, double splitLengthM) {
        if (splitLengthM <= 0) throw new IllegalArgumentException("Split length must be positive.");
        int full = (int) Math.floor(totalDistanceM / splitLengthM + 1e-9);
        double leftover = totalDistanceM - full * splitLengthM;
        return leftover > 0.5 ? full + 1 : full;
    }

    /** Builds splits of equal length from watch lap times. The last split covers whatever distance is left. */
    public static Run fromSplitTimes(double totalDistanceM, double splitLengthM, List<Double> splitTimesS,
                                     OptionalDouble cadenceSpm, OptionalDouble verticalOscillationCm) {
        int needed = splitCount(totalDistanceM, splitLengthM);
        if (splitTimesS.size() != needed) {
            throw new IllegalArgumentException("A " + Math.round(totalDistanceM) + " m run with " + Math.round(splitLengthM)
                    + " m splits needs " + needed + " split times, but " + splitTimesS.size() + " were given.");
        }
        java.util.ArrayList<Split> splits = new java.util.ArrayList<>();
        double remaining = totalDistanceM;
        for (double t : splitTimesS) {
            double d = Math.min(splitLengthM, remaining);
            splits.add(new Split(d, t));
            remaining -= d;
        }
        return fromSplits(splits, cadenceSpm, verticalOscillationCm);
    }

    public double getDistanceM() { return distanceM; }
    public double getTimeS() { return timeS; }
    public List<Split> getSplits() { return splits; }
    public boolean hasSplits() { return !splits.isEmpty(); }
    public OptionalDouble getCadenceSpm() { return cadenceSpm; }
    public OptionalDouble getVerticalOscillationCm() { return verticalOscillationCm; }

    public double getAverageSpeedMps() {
        return distanceM / timeS;
    }

    /** The run as segments: its splits, or one segment covering the whole run. */
    public List<Split> segments() {
        return hasSplits() ? splits : List.of(new Split(distanceM, timeS));
    }
}
