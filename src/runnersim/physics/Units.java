package runnersim.physics;

import java.util.Locale;

/**
 * Parsing and formatting for times, paces and energies, shared by both interfaces.
 */
public final class Units {

    private Units() { }

    /**
     * Parses "95", "1:35", or "1:02:35" into seconds. Seconds may have decimals ("1:35.4").
     */
    public static double parseTime(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Enter a time like 21:30.");
        String[] parts = text.trim().split(":");
        if (parts.length > 3) throw new IllegalArgumentException("Use h:mm:ss, mm:ss, or seconds.");
        double total = 0;
        try {
            for (int i = 0; i < parts.length; i++) {
                double v = Double.parseDouble(parts[i]);
                if (v < 0) throw new IllegalArgumentException("Time can't be negative.");
                if (i > 0 && v >= 60) throw new IllegalArgumentException("Minutes and seconds should be under 60.");
                total = total * 60 + v;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + text.trim() + "\" isn't a time. Use mm:ss, like 21:30.");
        }
        if (total <= 0) throw new IllegalArgumentException("Time must be more than zero.");
        return total;
    }

    /** 1290 → "21:30", 3755 → "1:02:35". */
    public static String formatTime(double seconds) {
        long s = Math.round(seconds);
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        return h > 0 ? String.format("%d:%02d:%02d", h, m, sec) : String.format("%d:%02d", m, sec);
    }

    /** Formats joules with a sensible unit: J, kJ or MJ. */
    public static String formatEnergy(double joules) {
        double a = Math.abs(joules);
        if (a >= 1_000_000) return String.format(Locale.US, "%.2f MJ", joules / 1_000_000);
        if (a >= 1_000) return String.format(Locale.US, "%.1f kJ", joules / 1_000);
        return String.format(Locale.US, "%.0f J", joules);
    }

    public static String formatDistance(double meters) {
        return meters >= 1000
                ? String.format(Locale.US, "%.2f km", meters / 1000)
                : String.format(Locale.US, "%.0f m", meters);
    }
}
