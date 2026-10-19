package runnersim.physics;

import java.util.List;

import runnersim.model.Run;
import runnersim.model.Runner;

/**
 * Everything the app knows about one analyzed run. Read-only.
 * The first model's breakdown is the primary estimate; the rest are comparisons.
 */
public record RunReport(
        Runner runner,
        Run run,
        Gait gait,
        List<EnergyBreakdown> models,
        List<SplitResult> splits) {

    /** One split with its speed and the change in kinetic energy going into it. */
    public record SplitResult(int number, double distanceM, double timeS, double elapsedS,
                              double speedMps, double deltaKineticJ) { }

    public RunReport {
        models = List.copyOf(models);
        splits = List.copyOf(splits);
    }

    public EnergyBreakdown primary() {
        return models.get(0);
    }

    public double averageSpeedMps() {
        return run.getAverageSpeedMps();
    }

    /** Seconds per kilometer. */
    public double paceSecondsPerKm() {
        return run.getTimeS() / (run.getDistanceM() / 1000.0);
    }

    /** Seconds per mile. */
    public double paceSecondsPerMile() {
        return run.getTimeS() / (run.getDistanceM() / 1609.344);
    }

    /** Average mechanical power, P = W / t. */
    public double mechanicalPowerW(EnergyBreakdown b) {
        return b.mechanicalJ() / run.getTimeS();
    }

    /** Average rate of metabolic energy use, including heat. */
    public double metabolicPowerW(EnergyBreakdown b) {
        return b.metabolicJ() / run.getTimeS();
    }

    /**
     * How much body temperature would rise if none of the heat escaped: ΔT = Q / (m·c).
     * Sweat and breathing remove almost all of it, which is the point of the number.
     */
    public double temperatureRiseIfNoCoolingK(EnergyBreakdown b) {
        return b.heatJ() / (runner.getMassKg() * PhysicsConstants.BODY_SPECIFIC_HEAT);
    }
}
