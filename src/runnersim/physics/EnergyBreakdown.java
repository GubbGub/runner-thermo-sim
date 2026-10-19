package runnersim.physics;

import java.util.List;

/**
 * The result of one work model: mechanical work, metabolic energy, and the parts that add up to the work.
 */
public record EnergyBreakdown(
        String modelName,
        String summary,
        double mechanicalJ,
        double metabolicJ,
        List<Part> parts) {

    /** One named contribution to the mechanical work. */
    public record Part(String label, double joules) { }

    public EnergyBreakdown {
        parts = List.copyOf(parts);
    }

    /** First law of thermodynamics: energy in = work out + heat. Whatever isn't work becomes heat. */
    public double heatJ() {
        return metabolicJ - mechanicalJ;
    }

    public double metabolicKcal() {
        return metabolicJ / PhysicsConstants.JOULES_PER_KCAL;
    }
}
