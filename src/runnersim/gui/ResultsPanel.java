package runnersim.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import runnersim.learn.Term;
import runnersim.physics.EnergyBreakdown;
import runnersim.physics.Gait;
import runnersim.physics.RunReport;
import runnersim.physics.Units;

/**
 * The results side: headline numbers as tiles, then stride, model comparison and splits as cards.
 */
final class ResultsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final transient Theme t;

    ResultsPanel(Theme t) {
        super();
        this.t = t;
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setOpaque(false);
        showEmpty();
    }

    private void showEmpty() {
        removeAll();
        JLabel a = Ui.label("Enter a run to see where its energy goes.", t.serif(Font.PLAIN, 20f), t.fg);
        JLabel b = Ui.label("Work, heat and power appear here.", t.sans(Font.PLAIN, 13f), t.muted);
        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createVerticalStrut(8));
        add(a);
        add(Box.createVerticalStrut(4));
        add(b);
        revalidate();
        repaint();
    }

    void show(RunReport r) {
        removeAll();
        EnergyBreakdown main = r.primary();
        Gait g = r.gait();

        JLabel title = Ui.label(r.runner().getName() + " · " + Units.formatDistance(r.run().getDistanceM())
                + " in " + Units.formatTime(r.run().getTimeS()), t.serif(Font.PLAIN, 26f), t.fg);
        JLabel sub = Ui.label(Units.formatTime(r.paceSecondsPerKm()) + " /km  ·  " + Units.formatTime(r.paceSecondsPerMile())
                + " /mi  ·  " + f("%.2f m/s", r.averageSpeedMps()), t.sans(Font.PLAIN, 13f), t.muted);
        add(left(title));
        add(Box.createVerticalStrut(2));
        add(left(sub));
        add(Box.createVerticalStrut(16));

        JPanel tiles = new JPanel(new GridLayout(2, 2, 12, 12));
        tiles.setOpaque(false);
        tiles.add(tile("Mechanical work", Units.formatEnergy(main.mechanicalJ()), "what actually moved you", Term.MECHANICAL_WORK));
        tiles.add(tile("Energy burned", f("%.0f kcal", main.metabolicKcal()), Units.formatEnergy(main.metabolicJ()) + " metabolic", Term.ENERGY_BURNED));
        tiles.add(tile("Released as heat", Units.formatEnergy(main.heatJ()),
                f("+%.1f °C if none escaped", r.temperatureRiseIfNoCoolingK(main)), Term.HEAT));
        tiles.add(tile("Mechanical power", f("%.0f W", r.mechanicalPowerW(main)),
                f("%.2f W/kg", r.mechanicalPowerW(main) / r.runner().getMassKg()), Term.POWER));
        add(left(tiles));
        add(Box.createVerticalStrut(16));

        JPanel stride = Ui.column();
        stride.add(Ui.eyebrow(t, "Stride"));
        stride.add(Box.createVerticalStrut(4));
        stride.add(Ui.row(t, "Cadence", f("%.0f steps/min", g.cadenceSpm()), g.cadenceEstimated() ? "estimated" : "watch", Term.CADENCE));
        stride.add(Ui.divider(t));
        stride.add(Ui.row(t, "Step length", f("%.2f m", g.stepLengthM()), "", Term.STEP_LENGTH));
        stride.add(Ui.divider(t));
        stride.add(Ui.row(t, "Vertical oscillation", f("%.1f cm", g.verticalOscillationM() * 100), g.oscillationEstimated() ? "estimated" : "watch", Term.VERTICAL_OSCILLATION));
        stride.add(Ui.divider(t));
        stride.add(Ui.row(t, "Froude number", f("%.2f", g.froudeNumber()), g.froudeNumber() > 0.5 ? "running gait" : "walking range", Term.FROUDE_NUMBER));
        add(card(stride));
        add(Box.createVerticalStrut(12));

        JPanel models = Ui.column();
        models.add(Ui.withHelp(t, Ui.eyebrow(t, "Two ways to estimate the work"), Term.MUSCLE_EFFICIENCY));
        models.add(Box.createVerticalStrut(4));
        for (int i = 0; i < r.models().size(); i++) {
            EnergyBreakdown b = r.models().get(i);
            if (i > 0) models.add(Ui.divider(t));
            models.add(Ui.row(t, b.modelName(), Units.formatEnergy(b.mechanicalJ()), f("%.0f W", r.mechanicalPowerW(b)), Term.find(b.modelName())));
            if (b.parts().size() > 1) {
                for (EnergyBreakdown.Part p : b.parts()) {
                    JLabel l = Ui.label("   " + p.label() + "  ·  " + Units.formatEnergy(p.joules()), t.sans(Font.PLAIN, 12f), t.faint);
                    models.add(left(l));
                }
            }
        }
        if (r.models().size() > 1) {
            double ratio = r.models().get(1).mechanicalJ() / main.mechanicalJ();
            models.add(Box.createVerticalStrut(10));
            models.add(left(Ui.label(f("Lifting and speeding up your body explains %.0f%% of the measured cost.", ratio * 100),
                    t.sans(Font.PLAIN, 12f), t.muted)));
            models.add(left(Ui.label("The rest goes to swinging your limbs and braking each step, minus what your tendons return.",
                    t.sans(Font.PLAIN, 12f), t.muted)));
        }
        add(card(models));

        if (!r.splits().isEmpty()) {
            add(Box.createVerticalStrut(12));
            JPanel splits = Ui.column();
            splits.add(Ui.withHelp(t, Ui.eyebrow(t, "Splits and kinetic energy"), Term.KINETIC_ENERGY));
            splits.add(Box.createVerticalStrut(4));
            for (int i = 0; i < r.splits().size(); i++) {
                RunReport.SplitResult s = r.splits().get(i);
                if (i > 0) splits.add(Ui.divider(t));
                String ke = (s.deltaKineticJ() >= 0 ? "+" : "") + Units.formatEnergy(s.deltaKineticJ()) + " KE";
                splits.add(Ui.row(t, s.number() + "   " + Units.formatDistance(s.distanceM()),
                        Units.formatTime(s.timeS()), Units.formatTime(1000 / s.speedMps()) + " /km   " + ke));
            }
            add(card(splits));
        }

        add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }

    private JPanel tile(String label, String value, String note, Term term) {
        JPanel c = Ui.column();
        c.add(Ui.withHelp(t, Ui.label(label, t.sans(Font.PLAIN, 12f), t.muted), term));
        c.add(Box.createVerticalStrut(4));
        c.add(Ui.label(value, t.sans(Font.BOLD, 26f), t.fg));
        c.add(Box.createVerticalStrut(2));
        c.add(Ui.label(note, t.sans(Font.PLAIN, 12f), t.faint));
        Ui.Card card = new Ui.Card(new BorderLayout(), t.card, 14, 16);
        card.add(c, BorderLayout.NORTH);
        return card;
    }

    private Component card(JPanel content) {
        Ui.Card c = new Ui.Card(new BorderLayout(), t.card, 14, 16);
        c.add(content, BorderLayout.NORTH);
        return left(c);
    }

    private static <T extends java.awt.Component> T left(T c) {
        if (c instanceof javax.swing.JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, jc.getPreferredSize().height));
        }
        return c;
    }

    private static String f(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }
}
