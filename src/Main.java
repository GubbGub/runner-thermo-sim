// Main.java - Runs the lab simulation and prints the results
public class Main {
    public static void main(String[] args) {
        // Mocking the two runners from your example to test "mass moves mass"
        Runner runnerA = new Runner("Alice", 50.0, 150.0);  // ~4'11"
        Runner runnerB = new Runner("Bob", 100.0, 205.0);   // ~6'9"

        double testDistance = 400.0; // 1 lap around the track (meters)
        double testTime = 90.0;      // 1 minute and 30 seconds

        printLabResults(runnerA, testDistance, testTime);
        System.out.println("------------------------------------");
        printLabResults(runnerB, testDistance, testTime);
    }

    private static void printLabResults(Runner runner, double distance, double time) {
        double mechWork = PhysicsCalculator.calculateMechanicalWorkJoules(runner, distance, time);
        double totalEnergy = PhysicsCalculator.calculateTotalMetabolicJoules(mechWork);
        double power = PhysicsCalculator.calculatePowerWatts(mechWork, time);

        System.out.println("=== LAB RESULTS FOR: " + runner.getName() + " ===");
        System.out.printf("Mechanical Work Done: %.2f Joules\n", mechWork);
        System.out.printf("Total Metabolic Energy Expended: %.2f Joules (Calories burned: %.2f)\n", 
                totalEnergy, totalEnergy / 4184.0); // 1 Calorie = 4184 Joules
        System.out.printf("Average Mechanical Power Output: %.2f Watts\n", power);
    }
}
