package runnersim.physics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;

import runnersim.model.Runner;

/**
 * A sprint lab: one runner, one distance, one or more timed trials, and optionally the class's own answer.
 * The headline numbers use the average time, the way a lab usually combines trials.
 */
public record SprintReport(
        Runner runner,
        double distanceM,
        List<SprintTrial> trials,
        SprintTrial average,
        OptionalDouble labHorsepower,
        List<String> warnings) {

    public SprintReport {
        trials = List.copyOf(trials);
        warnings = List.copyOf(warnings);
    }

    public static SprintReport analyze(Runner runner, double distanceM, List<Double> trialTimesS, OptionalDouble labHorsepower) {
        if (trialTimesS.isEmpty()) throw new IllegalArgumentException("Enter at least one trial time.");
        if (distanceM < 1 || distanceM > 400) throw new IllegalArgumentException("Sprint distance should be between 1 and 400 m.");
        labHorsepower.ifPresent(hp -> {
            if (hp <= 0) throw new IllegalArgumentException("Your lab's horsepower should be a positive number.");
        });

        List<SprintTrial> trials = new ArrayList<>();
        double sum = 0;
        for (double t : trialTimesS) {
            trials.add(SprintTrial.of(runner, distanceM, t));
            sum += t;
        }
        SprintTrial average = SprintTrial.of(runner, distanceM, sum / trialTimesS.size());

        List<String> warnings = new ArrayList<>();
        double bolt = RealityCheck.boltTimeS(distanceM);
        for (int i = 0; i < trials.size(); i++) {
            SprintTrial tr = trials.get(i);
            String which = trials.size() > 1 ? "Trial " + (i + 1) : "This time";
            if (RealityCheck.fasterThanBolt(distanceM, tr.timeS())) {
                warnings.add(String.format(Locale.US,
                        "%s (%.2f s) is faster than Usain Bolt's first %.0f m in his world record (about %.2f s). The timing is probably off.",
                        which, tr.timeS(), distanceM, bolt));
            } else if (tr.finalSpeedMps() > RealityCheck.FASTEST_HUMAN_SPEED_MPS) {
                warnings.add(String.format(Locale.US,
                        "%s implies %.1f m/s at the line, faster than any human has run (about 12.4 m/s). Real sprinters don't accelerate evenly, so this model overestimates final speed.",
                        which, tr.finalSpeedMps()));
            }
        }
        if (trials.size() > 1) {
            double min = Double.MAX_VALUE, max = 0;
            for (SprintTrial tr : trials) { min = Math.min(min, tr.averagePowerW()); max = Math.max(max, tr.averagePowerW()); }
            double spread = (max - min) / average.averagePowerW() * 100;
            if (spread > 10) {
                warnings.add(String.format(Locale.US,
                        "The trials differ by %.0f%% in power. Power depends on time cubed, so a small stopwatch difference becomes a big power difference.",
                        spread));
            }
        }
        return new SprintReport(runner, distanceM, trials, average, labHorsepower, warnings);
    }

    /** How far the class's answer is from the model, as a percent of the model (+ means the lab is higher). */
    public OptionalDouble labDifferencePercent() {
        if (labHorsepower.isEmpty()) return OptionalDouble.empty();
        double model = average.averageHorsepower();
        return OptionalDouble.of((labHorsepower.getAsDouble() - model) / model * 100);
    }
}
