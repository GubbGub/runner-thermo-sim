package runnersim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import runnersim.learn.Term;
import runnersim.model.Run;
import runnersim.model.Runner;
import runnersim.physics.SprintReport;
import runnersim.physics.Units;

/**
 * The input card. A switch at the top picks the kind of run:
 *   Sprint: a short dash from a standstill, timed once or several times (the AP Physics lab).
 *   Distance run: a longer run with optional watch data and splits.
 */
final class FormPanel extends Ui.Card {

    private static final long serialVersionUID = 1L;

    private final transient Theme t;
    private final JTextField name, mass, height;
    private final JTextField sprintDistance, sprintTimes, labAnswer;
    private final JTextField distance, time, cadence, oscillation, splitLength, splitTimes;
    private final JPanel sprintSection, distanceSection;
    private final JLabel error;
    private boolean sprintMode = true;
    final JButton calculate;

    FormPanel(Theme t, Consumer<SprintReport> onSprint, BiConsumer<Runner, Run> onRun) {
        super(new BorderLayout(), t.card, 16, 20);
        this.t = t;

        name = Ui.field(t, "Your name");
        mass = Ui.field(t, "Kilograms, or add lb: 158 lb");
        height = Ui.field(t, "Centimeters, or feet and inches: 5'11");

        sprintDistance = Ui.field(t, "Sprint distance in meters, e.g. 10");
        sprintTimes = Ui.field(t, "One or more times in seconds, e.g. 1.85, 1.90");
        labAnswer = Ui.field(t, "The horsepower your class calculated, to compare");

        distance = Ui.field(t, "Distance in meters, e.g. 5000");
        time = Ui.field(t, "Total time as mm:ss or h:mm:ss");
        cadence = Ui.field(t, "Steps per minute from your watch");
        oscillation = Ui.field(t, "Vertical oscillation in cm from your watch");
        splitLength = Ui.field(t, "Lap length in meters, e.g. 1000");
        splitTimes = Ui.field(t, "Lap times separated by commas, e.g. 4:20, 4:15, 4:18");

        JPanel col = Ui.column();
        col.add(Ui.segmented(t, new String[] {"Sprint", "Distance run"}, 0, i -> setSprintMode(i == 0)));
        col.add(Box.createVerticalStrut(18));

        col.add(Ui.eyebrow(t, "Runner"));
        col.add(Box.createVerticalStrut(8));
        col.add(Ui.labeled(t, "Name", "", name));
        col.add(Box.createVerticalStrut(10));
        col.add(pair(Ui.labeled(t, "Mass", "kg or lb", mass), Ui.labeled(t, "Height", "cm or ft'in", height)));

        sprintSection = Ui.column();
        sprintSection.add(Box.createVerticalStrut(20));
        sprintSection.add(Ui.withHelp(t, Ui.eyebrow(t, "Sprint from a standstill"), Term.CONSTANT_ACCELERATION));
        sprintSection.add(Box.createVerticalStrut(8));
        sprintSection.add(Ui.labeled(t, "Distance", "m", sprintDistance));
        sprintSection.add(Box.createVerticalStrut(10));
        sprintSection.add(Ui.labeled(t, "Trial times", "seconds, comma separated", sprintTimes, Term.TIMING_ERROR));
        sprintSection.add(Box.createVerticalStrut(10));
        sprintSection.add(Ui.labeled(t, "Your lab's answer", "hp · optional", labAnswer, Term.PERCENT_DIFFERENCE));
        col.add(sprintSection);

        distanceSection = Ui.column();
        distanceSection.add(Box.createVerticalStrut(20));
        distanceSection.add(Ui.eyebrow(t, "Run"));
        distanceSection.add(Box.createVerticalStrut(8));
        distanceSection.add(pair(Ui.labeled(t, "Distance", "m", distance), Ui.labeled(t, "Time", "mm:ss", time)));
        distanceSection.add(Box.createVerticalStrut(20));
        distanceSection.add(Ui.eyebrow(t, "From your watch · optional"));
        distanceSection.add(Box.createVerticalStrut(8));
        distanceSection.add(pair(Ui.labeled(t, "Cadence", "spm", cadence, Term.CADENCE),
                Ui.labeled(t, "Bounce", "cm", oscillation, Term.VERTICAL_OSCILLATION)));
        distanceSection.add(Box.createVerticalStrut(10));
        distanceSection.add(Ui.labeled(t, "Split length", "m", splitLength));
        distanceSection.add(Box.createVerticalStrut(10));
        distanceSection.add(Ui.labeled(t, "Split times", "comma separated", splitTimes));
        distanceSection.setVisible(false);
        col.add(distanceSection);

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

        calculate.addActionListener(e -> submit(onSprint, onRun));
    }

    private void setSprintMode(boolean sprint) {
        sprintMode = sprint;
        sprintSection.setVisible(sprint);
        distanceSection.setVisible(!sprint);
        showError(" ");
        revalidate();
        repaint();
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

    private void submit(Consumer<SprintReport> onSprint, BiConsumer<Runner, Run> onRun) {
        try {
            Runner runner = new Runner(name.getText(), Units.parseMassKg(mass.getText()), Units.parseHeightCm(height.getText()));
            if (sprintMode) {
                onSprint.accept(SprintReport.analyze(runner, number(sprintDistance, "Distance"),
                        times(sprintTimes, "Trial times"), optional(labAnswer, "Your lab's answer")));
            } else {
                onRun.accept(runner, buildRun());
            }
            showError(" ");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private Run buildRun() {
        double d = number(distance, "Distance");
        OptionalDouble cad = optional(cadence, "Cadence");
        OptionalDouble osc = optional(oscillation, "Bounce");
        if (!splitTimes.getText().isBlank()) {
            return Run.fromSplitTimes(d, number(splitLength, "Split length"), times(splitTimes, "Split times"), cad, osc);
        }
        return Run.of(d, Units.parseTime(time.getText()), cad, osc);
    }

    private static List<Double> times(JTextField f, String what) {
        List<Double> list = new ArrayList<>();
        for (String part : f.getText().trim().split("[,\\s]+")) {
            if (!part.isBlank()) list.add(Units.parseTime(part));
        }
        if (list.isEmpty()) throw new IllegalArgumentException(what + " are required.");
        return list;
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

    void focusFirstField() {
        name.requestFocusInWindow();
    }

    /** Fills the form with a sample sprint so the app shows something useful on first open. */
    void fillExample() {
        name.setText("Sample sprinter");
        mass.setText("160 lb");
        height.setText("5'10");
        sprintDistance.setText("10");
        sprintTimes.setText("1.85, 1.90");
        distance.setText("5000");
        time.setText("21:20");
        splitLength.setText("1000");
        splitTimes.setText("4:20, 4:15, 4:18, 4:22, 4:05");
    }
}
