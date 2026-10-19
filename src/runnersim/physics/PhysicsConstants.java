package runnersim.physics;

/**
 * Every physical constant and modeling assumption in one place, so each one is easy to find and question.
 */
public final class PhysicsConstants {

    private PhysicsConstants() { }

    /** Gravitational acceleration at Earth's surface, m/s². */
    public static final double G = 9.81;

    /** Joules in one kilocalorie (one food Calorie). */
    public static final double JOULES_PER_KCAL = 4184.0;

    /** Fraction of metabolic energy that muscles turn into mechanical work. The rest leaves as heat. */
    public static final double MUSCLE_EFFICIENCY = 0.25;

    /**
     * Metabolic cost of level running per kilogram per meter, J/(kg·m).
     * Roughly 1 kcal per kg per km and nearly independent of speed (Margaria et al., 1963).
     */
    public static final double COST_OF_TRANSPORT = 4.184;

    /** Specific heat of the human body, J/(kg·K). Used for the "no cooling" temperature rise. */
    public static final double BODY_SPECIFIC_HEAT = 3470.0;

    /** Leg length as a fraction of standing height (standard anthropometric ratio). */
    public static final double LEG_LENGTH_TO_HEIGHT = 0.53;

    /** Typical vertical bounce of the center of mass per step, used when no watch data is given, m. */
    public static final double DEFAULT_VERTICAL_OSCILLATION_M = 0.08;

    /** Cadence estimate when none is given: steps/min = base + slope × speed (m/s), clamped. */
    public static final double CADENCE_BASE_SPM = 150.0;
    public static final double CADENCE_SLOPE = 6.0;
    public static final double CADENCE_MIN_SPM = 150.0;
    public static final double CADENCE_MAX_SPM = 210.0;
}
