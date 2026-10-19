package runnersim;

import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import runnersim.cli.ConsoleApp;
import runnersim.gui.DesktopApp;

/**
 * Entry point. Opens the window by default; pass --cli (or run without a display) for the terminal version.
 */
public final class App {

    private App() { }

    public static void main(String[] args) {
        boolean cli = Arrays.asList(args).contains("--cli") || GraphicsEnvironment.isHeadless();
        if (cli) {
            ConsoleApp.main(args);
        } else {
            DesktopApp.main(args);
        }
    }
}
