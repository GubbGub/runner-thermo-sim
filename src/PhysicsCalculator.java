// PhysicsCalculator.java - Processes the thermodynamics and mechanical work
public class PhysicsCalculator {
    // Human metabolic efficiency is roughly 25% (0.25)
    private static final double HUMAN_EFFICIENCY = 0.25; 

    /**
     * Calculates mechanical work done against gravity/acceleration in Joules.
     * Simplified: Work = Force * Distance. (Force = mass * acceleration)
     * For a runner, we can approximate horizontal work per stride.
     */
    public static double calculateMechanicalWorkJoules(Runner runner, double distanceMeters, double timeSeconds) {
        double velocity = distanceMeters / timeSeconds;
        // Rough biomechanical estimate: energy cost of horizontal running is ~1 Joule per kg per meter
        return runner.getWeightKg() * distanceMeters;
    }

    /**
     * Total Metabolic Energy (what your body actually burns, including heat loss)
     */
    public static double calculateTotalMetabolicJoules(double mechanicalWork) {
        return mechanicalWork / HUMAN_EFFICIENCY;
    }

    /**
     * Power output in Watts (Joules per second)
     */
    public static double calculatePowerWatts(double joules, double timeSeconds) {
        return joules / timeSeconds;
    }
}
