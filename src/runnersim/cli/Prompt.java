package runnersim.cli;

import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.Scanner;

import runnersim.physics.Units;

/**
 * Reads validated input from a Scanner. Every method re-asks until the answer is usable,
 * so the rest of the program never sees bad data.
 */
final class Prompt {

    /** Thrown when input ends (for example Ctrl+D), so the app can exit cleanly. */
    static final class InputClosedException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private final Scanner in;
    private final Style style;

    Prompt(Scanner in, Style style) {
        this.in = in;
        this.style = style;
    }

    private String ask(String label, String hint) {
        System.out.print("  " + label + (hint.isEmpty() ? "" : " " + style.muted(hint)) + style.accent(" › "));
        try {
            return in.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new InputClosedException();
        }
    }

    private void complain(String message) {
        System.out.println("    " + style.error(message));
    }

    String text(String label) {
        while (true) {
            String s = ask(label, "");
            if (!s.isEmpty()) return s;
            complain("This one's required.");
        }
    }

    /** Free text that may be empty. */
    String optionalText(String label) {
        return ask(label, "");
    }

    double number(String label, String unit, double min, double max) {
        while (true) {
            String s = ask(label, "(" + unit + ")");
            try {
                double v = Double.parseDouble(s);
                if (v >= min && v <= max) return v;
                complain("Enter a value between " + fmt(min) + " and " + fmt(max) + " " + unit + ".");
            } catch (NumberFormatException e) {
                complain("Enter a number, like " + fmt((min + max) / 4) + ".");
            }
        }
    }

    /** Like number(), but pressing Enter skips it. */
    OptionalDouble optionalNumber(String label, String unit, double min, double max) {
        while (true) {
            String s = ask(label, "(" + unit + ", Enter to skip)");
            if (s.isEmpty()) return OptionalDouble.empty();
            try {
                double v = Double.parseDouble(s);
                if (v >= min && v <= max) return OptionalDouble.of(v);
                complain("Enter a value between " + fmt(min) + " and " + fmt(max) + " " + unit + ", or press Enter to skip.");
            } catch (NumberFormatException e) {
                complain("Enter a number, or press Enter to skip.");
            }
        }
    }

    double time(String label) {
        while (true) {
            String s = ask(label, "(mm:ss or h:mm:ss)");
            try {
                return Units.parseTime(s);
            } catch (IllegalArgumentException e) {
                complain(e.getMessage());
            }
        }
    }

    boolean yesNo(String label, boolean defaultYes) {
        while (true) {
            String s = ask(label, defaultYes ? "(Y/n)" : "(y/N)").toLowerCase();
            if (s.isEmpty()) return defaultYes;
            if (s.startsWith("y")) return true;
            if (s.startsWith("n")) return false;
            complain("Type y or n.");
        }
    }

    private static String fmt(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
