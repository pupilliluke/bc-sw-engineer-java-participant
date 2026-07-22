public class SolidDemo {
    // Business calculation: returns data; does not print.
    static double calculateInterest(
            double balance, double ratePercent) {
        return balance * ratePercent / 100.0;
    }

    // Presentation: formats a value; does not calculate it.
    static void printInterest(double interest) {
        System.out.printf(
                "Interest earned: %.2f%n", interest);
    }

    public static void main(String[] args) {
        double interest =
                calculateInterest(10_000, 5);
        printInterest(interest);

        if (interest != 500.00) {
            throw new AssertionError(
                    "Expected 500.00, got " + interest);
        }
    }
}