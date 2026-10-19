package runnersim.model;

import runnersim.physics.PhysicsConstants;

/**
 * A runner's biometrics. Immutable once created.
 */
public final class Runner {

    private final String name;
    private final double massKg;
    private final double heightCm;

    public Runner(String name, double massKg, double heightCm) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name can't be empty.");
        }
        if (massKg < 20 || massKg > 250) {
            throw new IllegalArgumentException("Mass should be between 20 and 250 kg.");
        }
        if (heightCm < 100 || heightCm > 250) {
            throw new IllegalArgumentException("Height should be between 100 and 250 cm.");
        }
        this.name = name.trim();
        this.massKg = massKg;
        this.heightCm = heightCm;
    }

    public String getName() { return name; }
    public double getMassKg() { return massKg; }
    public double getHeightCm() { return heightCm; }
    public double getHeightM() { return heightCm / 100.0; }

    /** Leg length (hip to floor), estimated as a fixed fraction of height. */
    public double getLegLengthM() {
        return getHeightM() * PhysicsConstants.LEG_LENGTH_TO_HEIGHT;
    }

    /** Body weight as a force, W = mg, in newtons. */
    public double getWeightN() {
        return massKg * PhysicsConstants.G;
    }
}
