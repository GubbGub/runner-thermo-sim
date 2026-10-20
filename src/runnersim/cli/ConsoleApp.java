package runnersim.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.Scanner;

import runnersim.learn.Term;
import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.physics.EnergyBreakdown;
import runnersim.physics.Gait;
import runnersim.physics.RunAnalyzer;
import runnersim.physics.RealityCheck;
import runnersim.physics.RunReport;
import runnersim.physics.SprintReport;
import runnersim.physics.SprintTrial;
import runnersim.physics.Units;

/**
 * The terminal version: asks for a runner and a run with a Scanner, then prints a formatted lab report.
 */
public final class ConsoleApp {

    private static final int WIDTH = 58;

    private final Style style = new Style();
    private final RunAnalyzer analyzer = new RunAnalyzer();

    public static void main(String[] args) {
        new ConsoleApp().start();
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            Prompt prompt = new Prompt(scanner, style);
            printHeader();

            Runner runner = askRunner(prompt);
            while (true) {
                if (prompt.choice("Sprint from a standstill, or a distance run?", "sprint", "distance") == 0) {
                    printSprint(askSprint(prompt, runner));
                } else {
                    printReport(analyzer.analyze(runner, askRun(prompt)));
                }
                learn(prompt);
                int next = prompt.choice("Next: another run for " + runner.getName() + ", a new runner, or quit?", "same", "new", "quit");
                System.out.println();
                if (next == 2) break;
                if (next == 1) runner = askRunner(prompt);
            }
        } catch (Prompt.InputClosedException e) {
            System.out.println();
        }
        System.out.println("  " + style.muted("Done. Keep running."));
    }

    // ── Input ────────────────────────────────────────────────────────────

    private Runner askRunner(Prompt p) {
        section("Runner");
        String name = p.text("Name");
        double mass = p.parsed("Mass", "kg, or add lb like 158 lb", Units::parseMassKg);
        double height = p.parsed("Height", "cm, or feet like 5'11", Units::parseHeightCm);
        return new Runner(name, mass, height);
    }

    private Run askRun(Prompt p) {
        section("Run");
        double distance = p.number("Distance", "m", 1, 200_000);
        OptionalDouble cadence = p.optionalNumber("Cadence from your watch", "steps/min", 60, 260);
        OptionalDouble bounce = p.optionalNumber("Vertical oscillation", "cm", 1, 25);

        if (p.yesNo("Enter splits from your watch?", false)) {
            double splitLength = p.number("Split length", "m", 1, distance);
            int count = Run.splitCount(distance, splitLength);
            List<Double> times = new ArrayList<>();
            double remaining = distance;
            for (int i = 1; i <= count; i++) {
                double d = Math.min(splitLength, remaining);
                remaining -= d;
                times.add(p.time("Split " + i + " " + style.muted("(" + Units.formatDistance(d) + ")")));
            }
            return Run.fromSplitTimes(distance, splitLength, times, cadence, bounce);
        }

        double time = p.time("Total time");
        return Run.of(distance, time, cadence, bounce);
    }

    private SprintReport askSprint(Prompt p, Runner runner) {
        section("Sprint from a standstill");
        double distance = p.number("Distance", "m", 1, 400);
        List<Double> times = p.times("Trial times", "seconds, separated by commas, like 1.85, 1.90");
        OptionalDouble lab = p.optionalNumber("Your lab's answer", "hp", 0.001, 100);
        return SprintReport.analyze(runner, distance, times, lab);
    }

    private void printSprint(SprintReport r) {
        SprintTrial avg = r.average();
        int n = r.trials().size();

        System.out.println();
        System.out.println("  " + style.rule(WIDTH));
        System.out.println("  " + style.bold(r.runner().getName()) + style.muted("  ·  ")
                + f("%.0f m sprint", r.distanceM()) + style.muted("  ·  ")
                + (n > 1 ? "average of " + n + " trials " : "") + Units.formatDuration(avg.timeS()));
        System.out.println("  " + style.muted(f("%.1f kg  ·  %.0f cm  ·  steady acceleration from rest", r.runner().getMassKg(), r.runner().getHeightCm())));
        System.out.println("  " + style.rule(WIDTH));

        section("Power");
        row("Average power", Units.formatHorsepower(avg.averagePowerW()), Units.formatWatts(avg.averagePowerW()), Term.HORSEPOWER);
        row("Peak power at the line", Units.formatHorsepower(avg.peakPowerW()), Units.formatWatts(avg.peakPowerW()) + ", twice the average", Term.PEAK_POWER);

        section("Motion");
        row("Acceleration", f("%.2f m/s²", avg.accelerationMps2()), f("%.2f g  ·  %.0f N of force", avg.accelerationG(), avg.forceN()), Term.ACCELERATION);
        row("Speed at the line", f("%.1f m/s", avg.finalSpeedMps()), f("average %.2f m/s", avg.averageSpeedMps()), Term.CONSTANT_ACCELERATION);

        section("Energy");
        row("Work done", Units.formatEnergy(avg.workJ()), "kinetic energy at the line", Term.KINETIC_ENERGY);
        row("Energy burned", Units.formatEnergy(avg.metabolicJ()), f("%.1f kcal", avg.metabolicJ() / 4184), Term.ENERGY_BURNED);
        row("Released as heat", Units.formatEnergy(avg.heatJ()), "75% of it", Term.HEAT);

        if (r.labHorsepower().isPresent()) {
            double diff = r.labDifferencePercent().getAsDouble();
            section("Your lab vs. the model");
            row("Your class's answer", f("%.2f hp", r.labHorsepower().getAsDouble()), "");
            row("This app", Units.formatHorsepower(avg.averagePowerW()), "average power");
            row("Difference", f("%+.1f%%", diff), Math.abs(diff) < 5 ? "same method, nice work" : "check units and formulas", Term.PERCENT_DIFFERENCE);
        }

        if (n > 1) {
            section("Each trial");
            for (int i = 0; i < n; i++) {
                SprintTrial tr = r.trials().get(i);
                row("Trial " + (i + 1) + "  " + Units.formatDuration(tr.timeS()), Units.formatHorsepower(tr.averagePowerW()),
                        f("%.1f m/s at the line", tr.finalSpeedMps()), i == 0 ? Term.TIMING_ERROR : null);
            }
        }

        section("Reality check");
        if (r.warnings().isEmpty()) {
            for (String line : wrap(f("These times are humanly possible. Usain Bolt's first %.0f m took about %.2f s from his first movement.",
                    r.distanceM(), RealityCheck.boltTimeS(r.distanceM())), WIDTH)) System.out.println("  " + style.muted(line));
        } else {
            for (String w : r.warnings()) {
                for (String line : wrap(w, WIDTH)) System.out.println("  " + style.error(line));
            }
        }
        System.out.println();
    }

    // ── Output ───────────────────────────────────────────────────────────

    private void printHeader() {
        System.out.println();
        System.out.println("  " + style.eyebrow("Runner Thermo Sim"));
        System.out.println("  " + style.bold("Where the energy of a run goes"));
        System.out.println("  " + style.muted("Mechanical work, power and heat from your own run."));
        System.out.println();
    }

    private void section(String title) {
        System.out.println();
        System.out.println("  " + style.eyebrow(title));
    }

    private void row(String label, String value, String note) {
        row(label, value, note, null);
    }

    /** A result line. Terms with an explanation get a blue "?" after the label. */
    private void row(String label, String value, String note, Term term) {
        int pad = Math.max(1, 28 - label.length() - (term == null ? 0 : 2));
        String name = style.muted(label) + (term == null ? "" : " " + style.accent("?")) + " ".repeat(pad);
        System.out.println("  " + name + style.bold(value) + (note.isEmpty() ? "" : "  " + style.muted(note)));
    }

    /** After a report: type any "?" term to read what it means. Enter moves on. */
    private void learn(Prompt p) {
        System.out.println("  " + style.muted("Terms marked ") + style.accent("?") + style.muted(" have explanations. Type one, like ")
                + style.accent("froude") + style.muted(", or press Enter to continue."));
        while (true) {
            String query = p.optionalText("Learn");
            if (query.isEmpty()) return;
            Term term = Term.find(query);
            if (term == null) {
                System.out.println("    " + style.error("No term matches \"" + query + "\". Try cadence, heat or froude."));
                continue;
            }
            System.out.println();
            System.out.println("    " + style.bold(term.title()));
            for (String line : wrap(term.explanation(), 60)) System.out.println("    " + line);
            System.out.println("    " + style.accent(term.formula()));
            System.out.println();
        }
    }

    private static java.util.List<String> wrap(String text, int width) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() + word.length() + 1 > width && line.length() > 0) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) line.append(' ');
            line.append(word);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private void printReport(RunReport r) {
        EnergyBreakdown main = r.primary();
        Gait g = r.gait();

        System.out.println();
        System.out.println("  " + style.rule(WIDTH));
        System.out.println("  " + style.bold(r.runner().getName()) + style.muted("  ·  ")
                + Units.formatDistance(r.run().getDistanceM()) + " in " + Units.formatDuration(r.run().getTimeS()));
        System.out.println("  " + style.rule(WIDTH));

        section("Pace");
        row("Average pace", Units.formatTime(r.paceSecondsPerKm()) + " /km", Units.formatTime(r.paceSecondsPerMile()) + " /mi");
        row("Average speed", f("%.2f m/s", r.averageSpeedMps()), f("%.1f km/h", r.averageSpeedMps() * 3.6));

        section("Energy");
        row("Mechanical work", Units.formatEnergy(main.mechanicalJ()), "what moved you", Term.MECHANICAL_WORK);
        row("Energy burned", Units.formatEnergy(main.metabolicJ()), f("%.0f kcal", main.metabolicKcal()), Term.ENERGY_BURNED);
        row("Released as heat", Units.formatEnergy(main.heatJ()), f("%.0f%% of the total", main.heatJ() / main.metabolicJ() * 100), Term.HEAT);
        row("If none escaped", f("+%.1f °C", r.temperatureRiseIfNoCoolingK(main)), "body temperature rise");

        section("Power");
        row("Mechanical power", f("%.0f W", r.mechanicalPowerW(main)), Units.formatHorsepower(r.mechanicalPowerW(main)) + f("  ·  %.2f W/kg", r.mechanicalPowerW(main) / r.runner().getMassKg()), Term.POWER);
        row("Metabolic power", f("%.0f W", r.metabolicPowerW(main)), "including heat");

        section("Stride");
        row("Cadence", f("%.0f steps/min", g.cadenceSpm()), g.cadenceEstimated() ? "estimated" : "from your watch", Term.CADENCE);
        row("Step length", f("%.2f m", g.stepLengthM()), "", Term.STEP_LENGTH);
        row("Vertical oscillation", f("%.1f cm", g.verticalOscillationM() * 100), g.oscillationEstimated() ? "estimated" : "from your watch", Term.VERTICAL_OSCILLATION);
        row("Froude number", f("%.2f", g.froudeNumber()), g.froudeNumber() > 0.5 ? "running gait (> 0.5)" : "walking range (< 0.5)", Term.FROUDE_NUMBER);

        section("Two ways to estimate the work");
        System.out.println("  " + style.muted("Both assume 25% muscle efficiency ") + style.accent("?") + style.muted(" (type \"efficiency\" below)"));
        for (EnergyBreakdown b : r.models()) {
            row(b.modelName(), Units.formatEnergy(b.mechanicalJ()), f("%.0f W", r.mechanicalPowerW(b)), Term.find(b.modelName()));
            for (EnergyBreakdown.Part part : b.parts()) {
                if (b.parts().size() > 1) System.out.println("    " + style.muted("· " + part.label() + "  " + Units.formatEnergy(part.joules())));
            }
        }
        if (r.models().size() > 1) {
            double ratio = r.models().get(1).mechanicalJ() / main.mechanicalJ();
            System.out.println("    " + style.muted(f("The center-of-mass model accounts for %.0f%% of the measured cost.", ratio * 100)));
            System.out.println("    " + style.muted("The rest is swinging your limbs and braking each step, minus what your tendons return."));
        }

        if (!r.splits().isEmpty()) {
            section("Splits");
            System.out.println("  " + style.muted("ΔKE is the change in kinetic energy ") + style.accent("?") + style.muted(" (type \"kinetic\" below)"));
            System.out.println("  " + style.muted(String.format("%-4s %-10s %-10s %-10s %s", "#", "Distance", "Time", "Pace /km", "ΔKE")));
            for (RunReport.SplitResult s : r.splits()) {
                String ke = s.deltaKineticJ() >= 0 ? "+" + Units.formatEnergy(s.deltaKineticJ()) : Units.formatEnergy(s.deltaKineticJ());
                System.out.println("  " + String.format("%-4d %-10s %-10s %-10s ", s.number(), Units.formatDistance(s.distanceM()),
                        Units.formatDuration(s.timeS()), Units.formatTime(1000 / s.speedMps())) + (s.deltaKineticJ() > 0 ? style.accent(ke) : style.muted(ke)));
            }
        }
        System.out.println();
    }

    private static String f(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }
}
