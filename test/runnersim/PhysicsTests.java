package runnersim;

import java.util.List;
import java.util.OptionalDouble;

import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.physics.EnergyBreakdown;
import runnersim.physics.PhysicsConstants;
import runnersim.physics.RunAnalyzer;
import runnersim.physics.RunReport;
import runnersim.physics.Units;

/**
 * Checks the physics against hand calculations. No libraries needed: run it and every line should say PASS.
 */
public final class PhysicsTests {

    private static int failures = 0;

    public static void main(String[] args) {
        Runner runner = new Runner("Test", 70, 180);
        OptionalDouble none = OptionalDouble.empty();

        // 1. Cost of transport: 4.184 J/kg/m × 70 kg × 5000 m = 1,464,400 J metabolic.
        RunReport r = new RunAnalyzer().analyze(runner, Run.of(5000, 1500, none, none));
        EnergyBreakdown cot = r.primary();
        check("metabolic energy, 70 kg × 5 km", cot.metabolicJ(), 1_464_400, 1);
        check("about 350 kcal", cot.metabolicKcal(), 350.0, 0.1);
        check("mechanical work is 25% of metabolic", cot.mechanicalJ(), 366_100, 1);
        check("heat is the other 75%", cot.heatJ(), 1_098_300, 1);
        check("mechanical power = W / t", r.mechanicalPowerW(cot), 366_100.0 / 1500, 0.01);

        // 2. Kinetic work from rest: ½ × 70 × (5000/1500)² = 388.9 J.
        EnergyBreakdown com = r.models().get(1);
        check("speeding up from rest, ½mv²", com.parts().get(1).joules(), 0.5 * 70 * Math.pow(5000.0 / 1500, 2), 0.01);

        // 3. Vertical work: steps × mgΔh, with cadence given as 180 spm.
        Run withCadence = Run.of(5000, 1500, OptionalDouble.of(180), OptionalDouble.of(10));
        RunReport r2 = new RunAnalyzer().analyze(runner, withCadence);
        double steps = 180.0 / 60 * 1500;   // 4500 steps
        check("vertical work, 4500 steps × mg × 0.10 m", r2.models().get(1).parts().get(0).joules(),
                steps * 70 * PhysicsConstants.G * 0.10, 0.01);

        // 4. Splits: slowing down costs nothing, speeding up costs ½m(v₂² − v₁²).
        Run splits = Run.fromSplits(List.of(
                new runnersim.model.Split(1000, 250),   // 4.0 m/s
                new runnersim.model.Split(1000, 400),   // 2.5 m/s (slower, free)
                new runnersim.model.Split(1000, 200)),  // 5.0 m/s
                none, none);
        double expected = 0.5 * 70 * (16) + 0.5 * 70 * (25 - 6.25);
        check("kinetic work over splits", new RunAnalyzer().analyze(runner, splits).models().get(1).parts().get(1).joules(), expected, 0.01);

        // 5. Split planning and time parsing.
        check("5200 m in 1000 m splits needs 6", Run.splitCount(5200, 1000), 6, 0);
        check("5000 m in 1000 m splits needs 5", Run.splitCount(5000, 1000), 5, 0);
        check("parse 21:30", Units.parseTime("21:30"), 1290, 0);
        check("parse 1:02:35", Units.parseTime("1:02:35"), 3755, 0);

        // 6. Sprint from rest with constant acceleration: P_avg = 2md²/t³, peak = 2 × average.
        Runner m = new Runner("M", 72, 172);
        runnersim.physics.SprintTrial tr = runnersim.physics.SprintTrial.of(m, 10, 1.85);
        check("sprint acceleration a = 2d/t²", tr.accelerationMps2(), 20 / (1.85 * 1.85), 1e-9);
        check("sprint average power = 2md²/t³", tr.averagePowerW(), 2 * 72 * 100 / Math.pow(1.85, 3), 1e-6);
        check("peak power is twice the average", tr.peakPowerW(), 2 * tr.averagePowerW(), 1e-6);
        check("M's average time 1.875 s gives about 2.93 hp",
                runnersim.physics.SprintTrial.of(m, 10, 1.875).averageHorsepower(), 2.93, 0.005);

        // 7. Units: pounds, feet and inches, decimal times.
        check("158 lb in kg", Units.parseMassKg("158 lb"), 158 * 0.45359237, 1e-9);
        check("5'11 in cm", Units.parseHeightCm("5'11"), 71 * 2.54, 1e-9);
        check("5 ft 11 in in cm", Units.parseHeightCm("5 ft 11 in"), 71 * 2.54, 1e-9);
        check("parse 1.85 s", Units.parseTime("1.85"), 1.85, 1e-9);
        check("1.85 s keeps its decimals", Units.formatDuration(1.85).equals("1.85 s") ? 1 : 0, 1, 0);

        // 8. Reality check: Bolt's first 10 m took 1.89 − 0.146 = 1.744 s from first movement.
        check("Bolt's 10 m from first movement", runnersim.physics.RealityCheck.boltTimeS(10), 1.744, 1e-9);
        check("1.50 s for 10 m is flagged", runnersim.physics.RealityCheck.fasterThanBolt(10, 1.50) ? 1 : 0, 1, 0);

        System.out.println(failures == 0 ? "\nAll tests passed." : "\n" + failures + " test(s) failed.");
        if (failures > 0) System.exit(1);
    }

    private static void check(String name, double actual, double expected, double tolerance) {
        boolean ok = Math.abs(actual - expected) <= tolerance;
        if (!ok) failures++;
        System.out.printf("%s  %s  (got %.3f, expected %.3f)%n", ok ? "PASS" : "FAIL", name, actual, expected);
    }
}
