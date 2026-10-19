package runnersim.physics;

import runnersim.model.Run;
import runnersim.model.Runner;

/**
 * How the runner moved: cadence, step length, bounce and Froude number.
 * Values flagged as estimated came from the model, not from the runner's watch.
 */
public record Gait(
        double cadenceSpm,
        boolean cadenceEstimated,
        double stepLengthM,
        double verticalOscillationM,
        boolean oscillationEstimated,
        double froudeNumber) {

    public static Gait of(Runner runner, Run run) {
        double v = run.getAverageSpeedMps();

        boolean cadenceEstimated = run.getCadenceSpm().isEmpty();
        double cadence = run.getCadenceSpm().orElseGet(() -> estimateCadence(v));

        boolean oscEstimated = run.getVerticalOscillationCm().isEmpty();
        double oscM = run.getVerticalOscillationCm().isPresent()
                ? run.getVerticalOscillationCm().getAsDouble() / 100.0
                : PhysicsConstants.DEFAULT_VERTICAL_OSCILLATION_M;

        double stepsPerSecond = cadence / 60.0;
        double stepLength = v / stepsPerSecond;

        // Froude number Fr = v² / (gL): dimensionless speed scaled to leg length.
        // Above about 0.5, walking stops being efficient and people switch to running.
        double froude = (v * v) / (PhysicsConstants.G * runner.getLegLengthM());

        return new Gait(cadence, cadenceEstimated, stepLength, oscM, oscEstimated, froude);
    }

    /** Cadence rises gently with speed. A rough linear fit, clamped to realistic limits. */
    static double estimateCadence(double speedMps) {
        double c = PhysicsConstants.CADENCE_BASE_SPM + PhysicsConstants.CADENCE_SLOPE * speedMps;
        return Math.max(PhysicsConstants.CADENCE_MIN_SPM, Math.min(PhysicsConstants.CADENCE_MAX_SPM, c));
    }

    public double totalSteps(double distanceM) {
        return distanceM / stepLengthM;
    }
}
