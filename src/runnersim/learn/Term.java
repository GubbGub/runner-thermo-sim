package runnersim.learn;

/**
 * Plain-language explanations of every physics term the app shows.
 * Both interfaces read from here, so each explanation is written once.
 */
public enum Term {

    MECHANICAL_WORK("Mechanical work",
            "Energy your muscles transfer into motion: lifting your body on each step and pushing it forward. "
            + "In physics, work is a force applied over a distance.",
            "W = F · d"),

    ENERGY_BURNED("Energy burned",
            "All the chemical energy your body releases from food and oxygen during the run. "
            + "This is what a watch's calorie count estimates. One food Calorie (kcal) is 4,184 joules.",
            "E = W / η"),

    MUSCLE_EFFICIENCY("Muscle efficiency",
            "The fraction of energy burned that becomes useful work. Human muscle manages about 25%, "
            + "similar to a car engine. The other 75% becomes heat.",
            "η = W / E ≈ 0.25"),

    HEAT("Released as heat",
            "By the first law of thermodynamics, energy can't disappear, so whatever doesn't become work becomes heat. "
            + "That's why you sweat. The temperature figure shows how much warmer you'd get if your body couldn't shed any of it.",
            "Q = E − W,   ΔT = Q / (m · c)"),

    POWER("Mechanical power",
            "How fast you do work, in joules per second (watts). Dividing by body mass gives watts per kilogram, "
            + "which lets runners of different sizes compare fairly.",
            "P = W / t"),

    CADENCE("Cadence",
            "Steps per minute, counting both feet. Most distance runners land between 160 and 190. "
            + "Many watches measure it; if you skip it, the app estimates it from your speed.",
            "f = steps / minute"),

    STEP_LENGTH("Step length",
            "Distance covered in one step. To go faster you take longer steps, quicker steps, or both.",
            "L = v / f"),

    VERTICAL_OSCILLATION("Vertical oscillation",
            "How far your body bounces up and down on each step, usually 6 to 10 cm. "
            + "Every bounce means lifting your whole body, so a smaller bounce wastes less energy.",
            "W = m · g · Δh  (per step)"),

    FROUDE_NUMBER("Froude number",
            "A number with no units that compares your speed with your leg length. "
            + "Animals of very different sizes switch from walking to running near the same value, about 0.5. "
            + "Biologists have even used it to estimate how fast dinosaurs moved from their footprints.",
            "Fr = v² / (g · L)"),

    COST_OF_TRANSPORT("Cost of transport",
            "The energy it takes to move one kilogram one meter. For running it's about 4 J/(kg·m), "
            + "and it barely changes with speed: a fast mile and a slow mile cost about the same energy, the fast one just spends it sooner.",
            "C = E / (m · d)"),

    CENTER_OF_MASS("Center of mass",
            "The point where your body's mass balances. Physics can treat your whole body as if all its mass sat there. "
            + "This model adds up the work of lifting and speeding up that point, but ignores swinging arms and legs.",
            "W = N · m·g·Δh + Σ ½m(v₂² − v₁²)"),

    KINETIC_ENERGY("Kinetic energy (KE)",
            "The energy of motion. Speeding up between splits adds kinetic energy, and that takes work. "
            + "Slowing down releases it, mostly as heat in your muscles, so the app doesn't count it as extra work.",
            "KE = ½ · m · v²");

    private final String title;
    private final String explanation;
    private final String formula;

    Term(String title, String explanation, String formula) {
        this.title = title;
        this.explanation = explanation;
        this.formula = formula;
    }

    public String title() { return title; }
    public String explanation() { return explanation; }
    public String formula() { return formula; }

    /** Finds a term by any part of its name, ignoring case: "froude" finds FROUDE_NUMBER. */
    public static Term find(String query) {
        String q = query.trim().toLowerCase();
        if (q.isEmpty()) return null;
        for (Term term : values()) {
            if (term.title.toLowerCase().contains(q)) return term;
        }
        return null;
    }
}
