package runnersim.physics;

import runnersim.model.Runner;

/**
 * One timed sprint from a standstill, analyzed with constant acceleration.
 *
 * Starting from rest with steady acceleration a over distance d in time t:
 *   d = ½at²            so  a = 2d / t²
 *   v_final = at        so  v_final = 2d / t   (twice the average speed)
 *   W = ΔKE = ½m·v_final²
 *   P_average = W / t = 2md² / t³
 *   P_peak = F · v_final = m·a·v_final   (exactly twice the average)
 *
 * Because power depends on t³, a small timing error becomes a large power error.
 */
public record SprintTrial(
        double timeS,
        double accelerationMps2,
        double averageSpeedMps,
        double finalSpeedMps,
        double forceN,
        double workJ,
        double averagePowerW,
        double peakPowerW,
        double metabolicJ) {

    public static SprintTrial of(Runner runner, double distanceM, double timeS) {
        if (distanceM <= 0) throw new IllegalArgumentException("Distance must be positive.");
        if (timeS <= 0) throw new IllegalArgumentException("Time must be positive.");
        double m = runner.getMassKg();
        double a = 2 * distanceM / (timeS * timeS);
        double vFinal = a * timeS;
        double work = 0.5 * m * vFinal * vFinal;
        return new SprintTrial(
                timeS,
                a,
                distanceM / timeS,
                vFinal,
                m * a,
                work,
                work / timeS,
                m * a * vFinal,
                work / PhysicsConstants.MUSCLE_EFFICIENCY);
    }

    public double heatJ() {
        return metabolicJ - workJ;
    }

    public double averageHorsepower() {
        return averagePowerW / PhysicsConstants.WATTS_PER_HORSEPOWER;
    }

    public double peakHorsepower() {
        return peakPowerW / PhysicsConstants.WATTS_PER_HORSEPOWER;
    }

    /** Acceleration as a multiple of gravity. */
    public double accelerationG() {
        return accelerationMps2 / PhysicsConstants.G;
    }
}
