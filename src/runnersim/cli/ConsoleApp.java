package runnersim.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.Scanner;

import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.physics.EnergyBreakdown;
import runnersim.physics.Gait;
import runnersim.physics.RunAnalyzer;
import runnersim.physics.RunReport;
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
            boolean again = true;
            while (again) {
                Run run = askRun(prompt);
                RunReport report = analyzer.analyze(runner, run);
                printReport(report);
                again = prompt.yesNo("Analyze another run?", false);
                System.out.println();
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
        double mass = p.number("Mass", "kg", 20, 250);
        double height = p.number("Height", "cm", 100, 250);
        return new Runner(name, mass, height);
    }

    private Run askRun(Prompt p) {
        section("Run");
        double distance = p.number("Distance", "m", 50, 200_000);
        OptionalDouble cadence = p.optionalNumber("Cadence from your watch", "steps/min", 60, 260);
        OptionalDouble bounce = p.optionalNumber("Vertical oscillation", "cm", 1, 25);

        if (p.yesNo("Enter splits from your watch?", false)) {
            double splitLength = p.number("Split length", "m", 50, distance);
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
        String padded = String.format("%-26s", label);
        System.out.println("  " + style.muted(padded) + style.bold(value) + (note.isEmpty() ? "" : "  " + style.muted(note)));
    }

    private void printReport(RunReport r) {
        EnergyBreakdown main = r.primary();
        Gait g = r.gait();

        System.out.println();
        System.out.println("  " + style.rule(WIDTH));
        System.out.println("  " + style.bold(r.runner().getName()) + style.muted("  ·  ")
                + Units.formatDistance(r.run().getDistanceM()) + " in " + Units.formatTime(r.run().getTimeS()));
        System.out.println("  " + style.rule(WIDTH));

        section("Pace");
        row("Average pace", Units.formatTime(r.paceSecondsPerKm()) + " /km", Units.formatTime(r.paceSecondsPerMile()) + " /mi");
        row("Average speed", f("%.2f m/s", r.averageSpeedMps()), f("%.1f km/h", r.averageSpeedMps() * 3.6));

        section("Energy");
        row("Mechanical work", Units.formatEnergy(main.mechanicalJ()), "what moved you");
        row("Metabolic energy", Units.formatEnergy(main.metabolicJ()), f("%.0f kcal", main.metabolicKcal()));
        row("Released as heat", Units.formatEnergy(main.heatJ()), f("%.0f%% of the total", main.heatJ() / main.metabolicJ() * 100));
        row("If none escaped", f("+%.1f °C", r.temperatureRiseIfNoCoolingK(main)), "body temperature rise");

        section("Power");
        row("Mechanical power", f("%.0f W", r.mechanicalPowerW(main)), f("%.2f W/kg", r.mechanicalPowerW(main) / r.runner().getMassKg()));
        row("Metabolic power", f("%.0f W", r.metabolicPowerW(main)), "including heat");

        section("Stride");
        row("Cadence", f("%.0f steps/min", g.cadenceSpm()), g.cadenceEstimated() ? "estimated" : "from your watch");
        row("Step length", f("%.2f m", g.stepLengthM()), "");
        row("Vertical oscillation", f("%.1f cm", g.verticalOscillationM() * 100), g.oscillationEstimated() ? "estimated" : "from your watch");
        row("Froude number", f("%.2f", g.froudeNumber()), g.froudeNumber() > 0.5 ? "running gait (> 0.5)" : "walking range (< 0.5)");

        section("Two ways to estimate the work");
        for (EnergyBreakdown b : r.models()) {
            row(b.modelName(), Units.formatEnergy(b.mechanicalJ()), f("%.0f W", r.mechanicalPowerW(b)));
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
            System.out.println("  " + style.muted(String.format("%-4s %-10s %-10s %-10s %s", "#", "Distance", "Time", "Pace /km", "ΔKE")));
            for (RunReport.SplitResult s : r.splits()) {
                String ke = s.deltaKineticJ() >= 0 ? "+" + Units.formatEnergy(s.deltaKineticJ()) : Units.formatEnergy(s.deltaKineticJ());
                System.out.println("  " + String.format("%-4d %-10s %-10s %-10s ", s.number(), Units.formatDistance(s.distanceM()),
                        Units.formatTime(s.timeS()), Units.formatTime(1000 / s.speedMps())) + (s.deltaKineticJ() > 0 ? style.accent(ke) : style.muted(ke)));
            }
        }
        System.out.println();
    }

    private static String f(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }
}
