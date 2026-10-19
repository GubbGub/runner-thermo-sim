package runnersim.physics;

import java.util.List;

import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.model.Split;

/**
 * Bottom-up model from Newtonian mechanics. Treats the runner as a point mass and adds up:
 *   1. Vertical work: lifting the body by its bounce height on every step, W = mgΔh.
 *   2. Kinetic work: speeding up from rest and between splits, W = ΔKE = ½m(v₂² − v₁²).
 * It leaves out limb swing and braking, and ignores energy stored in tendons, so it is a lower bound.
 */
public final class CenterOfMassModel implements WorkModel {

    @Override
    public EnergyBreakdown estimate(Runner runner, Run run, Gait gait) {
        double m = runner.getMassKg();

        double steps = gait.totalSteps(run.getDistanceM());
        double verticalWork = steps * m * PhysicsConstants.G * gait.verticalOscillationM();

        double kineticWork = positiveKineticWork(m, run.segments());

        double mechanical = verticalWork + kineticWork;
        double metabolic = mechanical / PhysicsConstants.MUSCLE_EFFICIENCY;

        return new EnergyBreakdown(
                "Center of mass",
                "Lifting your body on every step plus speeding up, ÷ 25% muscle efficiency",
                mechanical,
                metabolic,
                List.of(
                        new EnergyBreakdown.Part("Vertical lift (mgΔh per step)", verticalWork),
                        new EnergyBreakdown.Part("Speeding up (ΔKE)", kineticWork)));
    }

    /**
     * Sums ½m(v₂² − v₁²) over each change in speed, starting from rest.
     * Only speed-ups count as work done by the runner; slowing down is treated as free.
     */
    static double positiveKineticWork(double massKg, List<Split> segments) {
        double previousSpeed = 0.0;
        double total = 0.0;
        for (Split s : segments) {
            double v = s.speedMps();
            double deltaKe = 0.5 * massKg * (v * v - previousSpeed * previousSpeed);
            if (deltaKe > 0) total += deltaKe;
            previousSpeed = v;
        }
        return total;
    }
}
