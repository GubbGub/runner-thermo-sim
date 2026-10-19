// Runner.java - Holds biometric data and calculates specific physical traits
public class Runner {
    private String name;
    private double weightKg; // Mass moves mass!
    private double heightCm;

    public Runner(String name, double weightKg, double heightCm) {
        this.name = name;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
    }

    public double getWeightKg() { return weightKg; }
    public String getName() { return name; }
    
    // Estimate stride length based on height (approx. 41.4% of height)
    public double getEstimatedStrideLengthMeters() {
        return (heightCm / 100.0) * 0.414;
    }
}
