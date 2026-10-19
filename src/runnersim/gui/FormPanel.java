package runnersim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.physics.Units;

/**
 * The input card: runner biometrics, the run, and optional watch data.
 * Turns text into a validated Runner and Run, or shows a plain-language error.
 */
final class FormPanel extends Ui.Card {

    private static final long serialVersionUID = 1L;

    private final transient Theme t;
    private final JTextField name, mass, height, distance, time, cadence, oscillation, splitLength, splitTimes;
    private final JLabel error;
    final JButton calculate;

    FormPanel(Theme t, BiConsumer<Runner, Run> onCalculate) {
        super(new BorderLayout(), t.card, 16, 20);
        this.t = t;

        name = Ui.field(t, "Your name");
        mass = Ui.field(t, "Body mass in kilograms");
        height = Ui.field(t, "Height in centimeters");
        distance = Ui.field(t, "Distance in meters, e.g. 5000");
        time = Ui.field(t, "Total time as mm:ss or h:mm:ss");
        cadence = Ui.field(t, "Steps per minute from your watch");
        oscillation = Ui.field(t, "Vertical oscillation in cm from your watch");
        splitLength = Ui.field(t, "Lap length in meters, e.g. 1000");
        splitTimes = Ui.field(t, "Lap times separated by commas, e.g. 4:20, 4:15, 4:18");

        JPanel col = Ui.column();
        col.add(Ui.eyebrow(t, "Runner"));
        col.add(Box.createVerticalStrut(8));
        col.add(Ui.labeled(t, "Name", "", name));
        col.add(Box.createVerticalStrut(10));
        col.add(pair(Ui.labeled(t, "Mass", "kg", mass), Ui.labeled(t, "Height", "cm", height)));

        col.add(Box.createVerticalStrut(20));
        col.add(Ui.eyebrow(t, "Run"));
        col.add(Box.createVerticalStrut(8));
        col.add(pair(Ui.labeled(t, "Distance", "m", distance), Ui.labeled(t, "Time", "mm:ss", time)));

        col.add(Box.createVerticalStrut(20));
        col.add(Ui.eyebrow(t, "From your watch · optional"));
        col.add(Box.createVerticalStrut(8));
        col.add(pair(Ui.labeled(t, "Cadence", "spm", cadence), Ui.labeled(t, "Bounce", "cm", oscillation)));
        col.add(Box.createVerticalStrut(10));
        col.add(Ui.labeled(t, "Split length", "m", splitLength));
        col.add(Box.createVerticalStrut(10));
        col.add(Ui.labeled(t, "Split times", "comma separated", splitTimes));

        col.add(Box.createVerticalStrut(20));
        calculate = Ui.primaryButton(t, "Calculate");
        calculate.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(calculate);
        col.add(Box.createVerticalStrut(10));
        error = Ui.label(" ", t.sans(Font.PLAIN, 12f), t.error);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(error);

        add(col, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, getPreferredSize().height));

        calculate.addActionListener(e -> submit(onCalculate));
    }

    private JPanel pair(JPanel a, JPanel b) {
        JPanel p = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    private void submit(BiConsumer<Runner, Run> onCalculate) {
        try {
            Runner runner = new Runner(name.getText(),
                    number(mass, "Mass"), number(height, "Height"));
            onCalculate.accept(runner, buildRun());
            showError(" ");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private Run buildRun() {
        double d = number(distance, "Distance");
        OptionalDouble cad = optional(cadence, "Cadence");
        OptionalDouble osc = optional(oscillation, "Bounce");

        String timesText = splitTimes.getText().trim();
        if (!timesText.isEmpty()) {
            double len = number(splitLength, "Split length");
            List<Double> times = new ArrayList<>();
            for (String part : timesText.split("[,\\s]+")) {
                if (!part.isBlank()) times.add(Units.parseTime(part));
            }
            return Run.fromSplitTimes(d, len, times, cad, osc);
        }
        return Run.of(d, Units.parseTime(time.getText()), cad, osc);
    }

    private static double number(JTextField f, String what) {
        String s = f.getText().trim();
        if (s.isEmpty()) throw new IllegalArgumentException(what + " is required.");
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(what + " should be a number.");
        }
    }

    private static OptionalDouble optional(JTextField f, String what) {
        return f.getText().isBlank() ? OptionalDouble.empty() : OptionalDouble.of(number(f, what));
    }

    private void showError(String message) {
        error.setText(message);
    }

    /** Fills the form with a sample run so the app shows something useful on first open. */
    void fillExample() {
        name.setText("Sample runner");
        mass.setText("70");
        height.setText("178");
        distance.setText("5000");
        time.setText("21:20");
        splitLength.setText("1000");
        splitTimes.setText("4:20, 4:15, 4:18, 4:22, 4:05");
    }
}
