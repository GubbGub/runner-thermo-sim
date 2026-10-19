package runnersim.physics;

import java.util.ArrayList;
import java.util.List;

import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.model.Split;

/**
 * Runs every work model on a run and collects the results into one report.
 */
public final class RunAnalyzer {

    private final List<WorkModel> models;

    /** The default setup: cost of transport as the main estimate, center of mass as the comparison. */
    public RunAnalyzer() {
        this(List.of(new CostOfTransportModel(), new CenterOfMassModel()));
    }

    public RunAnalyzer(List<WorkModel> models) {
        if (models.isEmpty()) throw new IllegalArgumentException("Need at least one work model.");
        this.models = List.copyOf(models);
    }

    public RunReport analyze(Runner runner, Run run) {
        Gait gait = Gait.of(runner, run);

        List<EnergyBreakdown> results = new ArrayList<>();
        for (WorkModel model : models) {
            results.add(model.estimate(runner, run, gait));
        }

        List<RunReport.SplitResult> splitResults = new ArrayList<>();
        double previousSpeed = 0.0;
        double elapsed = 0.0;
        int i = 1;
        for (Split s : run.getSplits()) {
            double v = s.speedMps();
            elapsed += s.timeS();
            double deltaKe = 0.5 * runner.getMassKg() * (v * v - previousSpeed * previousSpeed);
            splitResults.add(new RunReport.SplitResult(i++, s.distanceM(), s.timeS(), elapsed, v, deltaKe));
            previousSpeed = v;
        }

        return new RunReport(runner, run, gait, results, splitResults);
    }
}
