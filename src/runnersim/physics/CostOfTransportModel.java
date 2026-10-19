package runnersim.physics;

import java.util.List;

import runnersim.model.Run;
import runnersim.model.Runner;

/**
 * Top-down model from exercise physiology. Measured oxygen use shows level running costs about
 * 1 kcal per kilogram per kilometer at almost any speed. Mechanical work is the efficient fraction of that.
 */
public final class CostOfTransportModel implements WorkModel {

    @Override
    public EnergyBreakdown estimate(Runner runner, Run run, Gait gait) {
        double metabolic = PhysicsConstants.COST_OF_TRANSPORT * runner.getMassKg() * run.getDistanceM();
        double mechanical = metabolic * PhysicsConstants.MUSCLE_EFFICIENCY;

        return new EnergyBreakdown(
                "Cost of transport",
                "Measured metabolic cost of running (≈1 kcal/kg/km) × 25% muscle efficiency",
                mechanical,
                metabolic,
                List.of(new EnergyBreakdown.Part("Work from metabolic cost", mechanical)));
    }
}
