package runnersim.physics;

import runnersim.model.Run;
import runnersim.model.Runner;

/**
 * A way of estimating the work a runner does. Each implementation is one physical model,
 * so new models can be added and compared without changing anything else.
 */
public interface WorkModel {

    EnergyBreakdown estimate(Runner runner, Run run, Gait gait);
}
