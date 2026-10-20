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
    private final Ui.Callout error;
    private final transient java.util.Map<JTextField, Help> help = new java.util.HashMap<>();
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
        error = new Ui.Callout(t, 280);
        col.add(error);

        describe(name, "Name", "Enter the runner's name or a nickname.", "M");
        describe(mass, "Mass", "Enter body weight in kilograms, or in pounds with \"lb\" after the number.", "72  or  158 lb");
        describe(height, "Height", "Enter height in centimeters, or feet and inches with an apostrophe between them.", "172  or  5'11");
        describe(sprintDistance, "Distance", "Enter how far the sprint was, in meters.", "10");
        describe(sprintTimes, "Trial times", "Enter each trial's stopwatch time in seconds, with a comma between trials.", "1.85, 1.90");
        describe(labAnswer, "Your lab's answer", "This one is optional. Enter the horsepower your class calculated as a plain number, or leave it blank.", "2.91");
        describe(distance, "Distance", "Enter how far the run was, in meters.", "5000");
        describe(time, "Time", "Enter the total time as minutes:seconds, or hours:minutes:seconds.", "21:20  or  1:05:30");
        describe(cadence, "Cadence", "This one is optional. Enter steps per minute from your watch, or leave it blank.", "172");
        describe(oscillation, "Bounce", "This one is optional. Enter vertical oscillation from your watch in centimeters, or leave it blank.", "8.5");
        describe(splitLength, "Split length", "Enter the length of each lap in meters. It's needed whenever you enter split times.", "1000");
        describe(splitTimes, "Split times", "Enter each lap's time with commas between them. You need one time per split, plus one for any leftover distance.", "4:20, 4:15, 4:18, 4:22, 4:05");

        add(col, BorderLayout.NORTH);

        calculate.addActionListener(e -> submit(onSprint, onRun));
    }

    /** Fixed width, but tall enough for whatever is showing, including an error message. */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, super.getPreferredSize().height);
    }

    private void setSprintMode(boolean sprint) {
        sprintMode = sprint;
        sprintSection.setVisible(sprint);
        distanceSection.setVisible(!sprint);
        clearError();
        revalidate();
        repaint();
    }

    /** What a field is for, used to explain problems in plain language. */
    private record Help(String label, String purpose, String example) { }

    /** A problem tied to one field, so the form can outline it in red and explain it. */
    private static final class FieldProblem extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        final transient JTextField field;
        FieldProblem(JTextField field, String message) {
            super(message);
            this.field = field;
        }
    }

    private void describe(JTextField field, String label, String purpose, String example) {
        help.put(field, new Help(label, purpose, example));
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void edited() { Ui.markInvalid(field, false); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { edited(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { edited(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { edited(); }
        });
    }

    /** Reads one field with a parser. Any problem is reported against that field. */
    private <T> T read(JTextField field, java.util.function.Function<String, T> parser) {
        String text = field.getText().trim();
        if (text.isEmpty()) throw new FieldProblem(field, "This is empty.");
        try {
            return parser.apply(text);
        } catch (FieldProblem p) {
            throw p;
        } catch (IllegalArgumentException e) {
            throw new FieldProblem(field, e.getMessage());
        }
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
            String who = read(name, s -> s);
            double kg = read(mass, s -> inRange(Units.parseMassKg(s), 20, 250,
                    "Weight should be between 20 and 250 kg (44 to 550 lb)."));
            double cm = read(height, s -> inRange(Units.parseHeightCm(s), 100, 250,
                    "Height should be between 100 and 250 cm (about 3'3 to 8'2)."));
            Runner runner = new Runner(who, kg, cm);
            if (sprintMode) {
                double d = read(sprintDistance, s -> inRange(number(s), 1, 400, "Sprint distance should be between 1 and 400 m."));
                List<Double> trials = read(sprintTimes, FormPanel::times);
                OptionalDouble lab = labAnswer.getText().isBlank() ? OptionalDouble.empty()
                        : OptionalDouble.of(read(labAnswer, s -> inRange(number(s), 0.001, 100, "Horsepower should be a positive number, like 2.91.")));
                onSprint.accept(SprintReport.analyze(runner, d, trials, lab));
            } else {
                onRun.accept(runner, buildRun());
            }
            clearError();
        } catch (FieldProblem p) {
            Help h = help.get(p.field);
            Ui.markInvalid(p.field, true);
            p.field.requestFocusInWindow();
            String heading = p.getMessage().equals("This is empty.") ? "Fill in " + h.label().toLowerCase() : "Check " + h.label().toLowerCase();
            String explanation = p.getMessage().equals("This is empty.") ? h.purpose() : p.getMessage() + " " + h.purpose();
            error.show(heading, explanation, h.example());
        } catch (IllegalArgumentException ex) {
            error.show("Something doesn't add up", ex.getMessage(), null);
        }
    }

    private Run buildRun() {
        double d = read(distance, s -> inRange(number(s), 1, 200_000, "Distance should be between 1 m and 200 km."));
        OptionalDouble cad = optional(cadence, 60, 260, "Cadence should be between 60 and 260 steps per minute.");
        OptionalDouble osc = optional(oscillation, 1, 25, "Bounce should be between 1 and 25 cm.");
        if (!splitTimes.getText().isBlank()) {
            double len = read(splitLength, s -> inRange(number(s), 1, d, "Each split has to be shorter than the whole run."));
            List<Double> laps = read(splitTimes, FormPanel::times);
            int needed = Run.splitCount(d, len);
            if (laps.size() != needed) {
                throw new FieldProblem(splitTimes, "You entered " + laps.size() + (laps.size() == 1 ? " time" : " times")
                        + ", but " + Math.round(d) + " m in " + Math.round(len) + " m splits needs " + needed + ".");
            }
            return Run.fromSplitTimes(d, len, laps, cad, osc);
        }
        return Run.of(d, read(time, Units::parseTime), cad, osc);
    }

    private static List<Double> times(String text) {
        List<Double> list = new ArrayList<>();
        for (String part : text.split("[,\\s]+")) {
            if (!part.isBlank()) list.add(Units.parseTime(part));
        }
        if (list.isEmpty()) throw new IllegalArgumentException("No times found.");
        return list;
    }

    private static double number(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("\"" + s.trim() + "\" isn't a number.");
        }
    }

    private static double inRange(double v, double min, double max, String message) {
        if (v < min || v > max) throw new IllegalArgumentException(message);
        return v;
    }

    private OptionalDouble optional(JTextField f, double min, double max, String message) {
        return f.getText().isBlank() ? OptionalDouble.empty() : OptionalDouble.of(read(f, s -> inRange(number(s), min, max, message)));
    }

    private void clearError() {
        error.clear();
        for (JTextField f : help.keySet()) Ui.markInvalid(f, false);
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
