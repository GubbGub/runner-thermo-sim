package runnersim.cli;

/**
 * Terminal styling with ANSI escape codes. One accent color, gray for labels, bold for numbers.
 * Turns itself off when output isn't a real terminal or when NO_COLOR is set.
 */
final class Style {

    private final boolean enabled;

    Style() {
        this.enabled = System.console() != null && System.getenv("NO_COLOR") == null;
    }

    private String wrap(String code, String text) {
        return enabled ? "\u001B[" + code + "m" + text + "\u001B[0m" : text;
    }

    String bold(String s)   { return wrap("1", s); }
    String muted(String s)  { return wrap("38;2;134;134;139", s); }
    String accent(String s) { return wrap("38;2;41;151;255", s); }
    String accentBold(String s) { return wrap("1;38;2;41;151;255", s); }
    String error(String s)  { return wrap("38;2;255;69;58", s); }

    /** Small uppercase label in gray. */
    String eyebrow(String s) {
        return muted(s.toUpperCase());
    }

    String rule(int width) {
        return muted("─".repeat(width));
    }
}
