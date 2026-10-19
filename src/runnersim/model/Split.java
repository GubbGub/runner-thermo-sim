package runnersim.model;

/**
 * One segment of a run, such as a 1 km lap from a watch.
 */
public record Split(double distanceM, double timeS) {

    public Split {
        if (distanceM <= 0) throw new IllegalArgumentException("Split distance must be positive.");
        if (timeS <= 0) throw new IllegalArgumentException("Split time must be positive.");
    }

    public double speedMps() {
        return distanceM / timeS;
    }
}
