package runnersim.physics;

import java.util.Locale;

/**
 * Parsing and formatting for times, paces and energies, shared by both interfaces.
 */
public final class Units {

    private Units() { }

    /**
     * Parses "95", "1.85", "1:35", or "1:02:35" into seconds. Seconds may have decimals ("1:35.4").
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

    /** Short durations keep their decimals: 1.85 → "1.85 s". Longer ones use clock format: 1290 → "21:30". */
    public static String formatDuration(double seconds) {
        return seconds < 60 ? String.format(Locale.US, "%.2f s", seconds) : formatTime(seconds);
    }

    /** "2,185 W" style power. */
    public static String formatWatts(double watts) {
        return String.format(Locale.US, "%,.0f W", watts);
    }

    /** Power in horsepower, two decimals: "2.93 hp". */
    public static String formatHorsepower(double watts) {
        return String.format(Locale.US, "%.2f hp", watts / PhysicsConstants.WATTS_PER_HORSEPOWER);
    }

    /**
     * Reads a body mass in kilograms or pounds: "72", "72 kg", "158 lb", "158lbs".
     * A bare number is kilograms.
     */
    public static double parseMassKg(String text) {
        String s = text == null ? "" : text.trim().toLowerCase(Locale.US);
        if (s.isEmpty()) throw new IllegalArgumentException("Mass is required.");
        boolean pounds = s.endsWith("lb") || s.endsWith("lbs") || s.endsWith("pounds") || s.endsWith("pound");
        String number = s.replaceAll("[a-z\\s]+$", "").trim();
        try {
            double v = Double.parseDouble(number);
            return pounds ? v * PhysicsConstants.KG_PER_POUND : v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Mass should look like 72 or 158 lb.");
        }
    }

    /**
     * Reads a height in centimeters, inches, or feet and inches:
     * "172", "172 cm", "71 in", "5'11", "5' 11\"", "5 ft 11".
     * A bare number is centimeters.
     */
    public static double parseHeightCm(String text) {
        String s = text == null ? "" : text.trim().toLowerCase(Locale.US);
        if (s.isEmpty()) throw new IllegalArgumentException("Height is required.");
        try {
            java.util.regex.Matcher feet = java.util.regex.Pattern
                    .compile("^(\\d+(?:\\.\\d+)?)\\s*(?:'|ft|feet|foot)\\s*(\\d+(?:\\.\\d+)?)?\\s*(?:\"|in|inches|'')?$")
                    .matcher(s);
            if (feet.matches()) {
                double ft = Double.parseDouble(feet.group(1));
                double in = feet.group(2) == null ? 0 : Double.parseDouble(feet.group(2));
                return (ft * 12 + in) * 2.54;
            }
            if (s.endsWith("in") || s.endsWith("inches") || s.endsWith("\"")) {
                return Double.parseDouble(s.replaceAll("[a-z\"\\s]+$", "").trim()) * 2.54;
            }
            return Double.parseDouble(s.replaceAll("cm$", "").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Height should look like 180, 180 cm, or 5'11.");
        }
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
