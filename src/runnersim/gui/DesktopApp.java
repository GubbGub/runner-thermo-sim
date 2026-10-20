package runnersim.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import runnersim.physics.RunAnalyzer;

/**
 * The window version. Same physics engine as the terminal app, laid out like a native Mac app.
 */
public final class DesktopApp {

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Runner Thermo");
        System.setProperty("apple.awt.application.appearance", "system");
        SwingUtilities.invokeLater(DesktopApp::open);
    }

    static JFrame build() {
        Theme t = Theme.detect();
        RunAnalyzer analyzer = new RunAnalyzer();

        JPanel root = new JPanel(new BorderLayout(0, 24));
        root.setBackground(t.bg);
        root.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JPanel header = Ui.column();
        header.add(Ui.eyebrow(t, "AP Physics II · Thermodynamics lab"));
        header.add(Box.createVerticalStrut(6));
        header.add(Ui.label("Runner Thermo", t.serif(Font.PLAIN, 38f), t.fg));
        header.add(Box.createVerticalStrut(4));
        header.add(Ui.label("Where the energy of a run goes: work, heat and horsepower from your own data.", t.serif(Font.PLAIN, 17f), t.muted));
        root.add(header, BorderLayout.NORTH);

        ResultsPanel results = new ResultsPanel(t);
        FormPanel form = new FormPanel(t, results::showSprint, (runner, run) -> results.show(analyzer.analyze(runner, run)));

        JPanel formHolder = new JPanel(new BorderLayout());
        formHolder.setOpaque(false);
        formHolder.add(form, BorderLayout.NORTH);

        JPanel resultsHolder = new JPanel(new BorderLayout());
        resultsHolder.setOpaque(false);
        resultsHolder.add(results, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(resultsHolder,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel body = new JPanel(new BorderLayout(24, 0));
        body.setOpaque(false);
        body.add(formHolder, BorderLayout.WEST);
        body.add(scroll, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        JFrame frame = new JFrame("Runner Thermo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.getRootPane().setDefaultButton(form.calculate);
        frame.setMinimumSize(new Dimension(860, 640));
        frame.setSize(1060, 820);
        frame.setLocationRelativeTo(null);

        form.fillExample();
        form.calculate.doClick();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) { form.focusFirstField(); }
        });
        return frame;
    }

    private static void open() {
        build().setVisible(true);
    }
}
