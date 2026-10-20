package runnersim.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Colors and fonts for the window. Light and dark palettes follow macOS appearance,
 * matching the Day Counter app: system gray background, white cards, one blue accent.
 */
final class Theme {

    final boolean dark;
    final Color bg, card, fg, muted, faint, separator, field, accent, good, error;
    final String serifFamily, sansFamily;

    private Theme(boolean dark) {
        this.dark = dark;
        if (dark) {
            bg = new Color(0x000000); card = new Color(0x1C1C1E); fg = new Color(0xF5F5F7);
            muted = new Color(0x98989D); faint = new Color(0x6E6E73); separator = new Color(0x38383A);
            field = new Color(0x2C2C2E); accent = new Color(0x2997FF); good = new Color(0x30D158); error = new Color(0xFF453A);
        } else {
            bg = new Color(0xF5F5F7); card = new Color(0xFFFFFF); fg = new Color(0x1D1D1F);
            muted = new Color(0x6E6E73); faint = new Color(0x86868B); separator = new Color(0xE5E5EA);
            field = new Color(0xF5F5F7); accent = new Color(0x0071E3); good = new Color(0x248A3D); error = new Color(0xD70015);
        }
        Set<String> installed = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        serifFamily = firstInstalled(installed, "New York", "New York Medium", "Iowan Old Style", "Charter", "Georgia", "Cambria", Font.SERIF);
        sansFamily = firstInstalled(installed, "SF Pro Text", "SF Pro", "Helvetica Neue", "Inter", "Segoe UI Variable Text", "Segoe UI", Font.SANS_SERIF);
    }

    static Theme detect() {
        String override = System.getenv("RUNNER_THEME");
        if (override != null) return new Theme(override.equalsIgnoreCase("dark"));
        return new Theme(systemDarkMode());
    }

    /** Asks the operating system whether dark mode is on. Anything else gets the light theme. */
    private static boolean systemDarkMode() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) return macDarkMode();
        if (os.contains("win")) return windowsDarkMode();
        return false;
    }

    /**
     * Windows stores the app theme in the registry: AppsUseLightTheme = 0 means dark.
     * The output line looks like "    AppsUseLightTheme    REG_DWORD    0x0".
     */
    private static boolean windowsDarkMode() {
        try {
            Process p = new ProcessBuilder("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                    "/v", "AppsUseLightTheme").redirectErrorStream(true).start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                boolean dark = false;
                while ((line = r.readLine()) != null) {
                    if (line.contains("AppsUseLightTheme")) dark = line.trim().endsWith("0x0");
                }
                p.waitFor();
                return dark;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /** macOS stores dark mode as AppleInterfaceStyle = Dark. The key is missing in light mode. */
    private static boolean macDarkMode() {
        try {
            Process p = new ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").redirectErrorStream(true).start();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = r.readLine();
                p.waitFor();
                return line != null && line.trim().equalsIgnoreCase("Dark");
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static String firstInstalled(Set<String> installed, String... families) {
        for (String f : families) if (installed.contains(f)) return f;
        return families[families.length - 1];
    }

    Font serif(int style, float size) { return new Font(serifFamily, style, 1).deriveFont(size); }
    Font sans(int style, float size)  { return new Font(sansFamily, style, 1).deriveFont(size); }
}
