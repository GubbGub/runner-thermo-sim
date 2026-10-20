package runnersim.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.AbstractBorder;

import runnersim.learn.Term;

/**
 * Small, reusable building blocks so every screen is put together from the same parts.
 */
final class Ui {

    private Ui() { }

    /** A panel with rounded corners and a solid fill. Used for every card. */
    static class Card extends JPanel {
        private static final long serialVersionUID = 1L;
        private final Color fill;
        private final int radius;

        Card(LayoutManager layout, Color fill, int radius, int padding) {
            super(layout);
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius * 2, radius * 2);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded outline for text fields that turns accent-colored when focused. */
    /** Client property that marks a field as holding a value that needs fixing (drawn with a red outline). */
    static final String INVALID = "runnerThermo.invalid";

    static void markInvalid(JComponent field, boolean invalid) {
        field.putClientProperty(INVALID, invalid);
        field.repaint();
    }

    static final class FieldBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final transient Theme t;

        FieldBorder(Theme t) { this.t = t; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean focused = c.hasFocus();
            boolean invalid = c instanceof JComponent jc && Boolean.TRUE.equals(jc.getClientProperty(INVALID));
            g2.setColor(invalid ? t.error : focused ? t.accent : t.separator);
            g2.setStroke(new BasicStroke(focused || invalid ? 2f : 1f));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 14, 14);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(7, 10, 7, 10); }
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    static JLabel eyebrow(Theme t, String text) {
        return label(text.toUpperCase(), t.sans(Font.BOLD, 11f), t.faint);
    }

    static JTextField field(Theme t, String placeholderTooltip) {
        JTextField f = new JTextField() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(t.field);
                g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        f.setOpaque(false);
        f.setFont(t.sans(Font.PLAIN, 14f));
        f.setForeground(t.fg);
        f.setCaretColor(t.accent);
        f.setBorder(new FieldBorder(t));
        f.setToolTipText(placeholderTooltip);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { f.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { f.repaint(); }
        });
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return f;
    }

    /** A labeled input: small gray label above, field below, optional hint to the right of the label. */
    static JPanel labeled(Theme t, String label, String hint, JComponent input) {
        return labeled(t, label, hint, input, null);
    }

    static JPanel labeled(Theme t, String label, String hint, JComponent input, Term term) {
        JPanel p = column();
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(withHelp(t, label(label, t.sans(Font.PLAIN, 12f), t.muted), term), BorderLayout.WEST);
        if (!hint.isEmpty()) top.add(label(hint, t.sans(Font.PLAIN, 11f), t.faint), BorderLayout.EAST);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(top);
        p.add(Box.createVerticalStrut(4));
        p.add(input);
        return p;
    }

    static JButton primaryButton(Theme t, String text) {
        JButton b = new JButton(text) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? t.accent.darker() : t.accent;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                if (isFocusOwner()) {
                    g2.setColor(new Color(t.accent.getRed(), t.accent.getGreen(), t.accent.getBlue(), 90));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, getHeight(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(t.sans(Font.BOLD, 14f));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setPreferredSize(new Dimension(200, 38));
        return b;
    }

    /** One line of a detail list: label on the left, value and an optional note on the right. */
    static JPanel row(Theme t, String label, String value, String note) {
        return row(t, label, value, note, null);
    }

    /** A detail row whose label has a "?" button explaining the term. */
    static JPanel row(Theme t, String label, String value, String note, Term term) {
        JPanel r = new JPanel(new BorderLayout(12, 0));
        r.setOpaque(false);
        r.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        r.add(withHelp(t, label(label, t.sans(Font.PLAIN, 13f), t.muted), term), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        if (!note.isEmpty()) right.add(label(note, t.sans(Font.PLAIN, 12f), t.faint));
        right.add(label(value, t.sans(Font.BOLD, 13f), t.fg));
        r.add(right, BorderLayout.EAST);
        r.setAlignmentX(Component.LEFT_ALIGNMENT);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, r.getPreferredSize().height));
        return r;
    }

    /** A thin horizontal line between rows. */
    static JComponent divider(Theme t) {
        JPanel d = new JPanel();
        d.setBackground(t.separator);
        d.setPreferredSize(new Dimension(1, 1));
        d.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        d.setAlignmentX(Component.LEFT_ALIGNMENT);
        return d;
    }

    static JPanel column() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }
    /** Puts a "?" button after any label. With no term, returns the label unchanged. */
    static JComponent withHelp(Theme t, JComponent label, Term term) {
        if (term == null) return label;
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        p.add(label);
        p.add(Box.createHorizontalStrut(6));
        p.add(helpButton(t, term));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height));
        return p;
    }

    /** A small circled question mark. Clicking it (or pressing Space) opens a card explaining the term. */
    static JButton helpButton(Theme t, Term term) {
        JButton b = new JButton() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean hot = getModel().isRollover() || isFocusOwner();
                int d = Math.min(getWidth(), getHeight()) - 2;
                g2.setColor(hot ? t.accent : t.faint);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawOval(1, 1, d - 1, d - 1);
                g2.setFont(t.sans(Font.BOLD, 10f));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String q = "?";
                g2.drawString(q, 1 + (d - fm.stringWidth(q)) / 2f, 1 + (d + fm.getAscent() - fm.getDescent()) / 2f);
                g2.dispose();
            }
        };
        Dimension size = new Dimension(16, 16);
        b.setPreferredSize(size);
        b.setMinimumSize(size);
        b.setMaximumSize(size);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setToolTipText("What is " + term.title().toLowerCase() + "?");
        b.getAccessibleContext().setAccessibleName("Explain " + term.title());
        b.addActionListener(e -> explain(t, term).show(b, -8, b.getHeight() + 6));
        return b;
    }

    /** The pop-up card: title, plain-language explanation, then the formula. */
    private static JPopupMenu explain(Theme t, Term term) {
        JPanel body = column();

        body.add(label(term.title(), t.serif(Font.PLAIN, 18f), t.fg));
        body.add(Box.createVerticalStrut(6));

        JTextArea text = new JTextArea(term.explanation());
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setFont(t.sans(Font.PLAIN, 13f));
        text.setForeground(t.muted);
        text.setBorder(BorderFactory.createEmptyBorder());
        text.setSize(new Dimension(300, Short.MAX_VALUE));
        text.setPreferredSize(new Dimension(300, text.getPreferredSize().height));
        text.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(text);

        body.add(Box.createVerticalStrut(10));
        Card formula = new Card(new BorderLayout(), t.field, 8, 8);
        formula.add(label(term.formula(), t.serif(Font.ITALIC, 15f), t.fg), BorderLayout.WEST);
        formula.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(formula);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(t.card);
        wrap.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        wrap.add(body, BorderLayout.CENTER);

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(t.separator));
        popup.setBackground(t.card);
        popup.add(wrap);
        return popup;
    }
    /** A two-or-more option switch, like the segmented controls in macOS. */
    static JPanel segmented(Theme t, String[] options, int selected, java.util.function.IntConsumer onChange) {
        Card track = new Card(new java.awt.GridLayout(1, options.length, 4, 0), t.field, 9, 3);
        javax.swing.ButtonGroup group = new javax.swing.ButtonGroup();
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            javax.swing.JToggleButton b = new javax.swing.JToggleButton(options[i]) {
                private static final long serialVersionUID = 1L;
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    if (isSelected()) {
                        g2.setColor(t.card);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                    }
                    if (isFocusOwner()) {
                        g2.setColor(t.accent);
                        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
                    }
                    g2.setFont(getFont().deriveFont(isSelected() ? Font.BOLD : Font.PLAIN));
                    g2.setColor(isSelected() ? t.fg : t.muted);
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    String text = getText();
                    g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2f, (getHeight() + fm.getAscent() - fm.getDescent()) / 2f);
                    g2.dispose();
                }
            };
            b.setFont(t.sans(Font.PLAIN, 13f));
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setPreferredSize(new Dimension(100, 28));
            b.setSelected(i == selected);
            b.addActionListener(e -> onChange.accept(index));
            group.add(b);
            track.add(b);
        }
        track.setAlignmentX(Component.LEFT_ALIGNMENT);
        track.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return track;
    }

    /** A short paragraph that wraps to the available width. */
    static JTextArea paragraph(Theme t, String text, java.awt.Color color, int width) {
        JTextArea a = new JTextArea(text);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setFont(t.sans(Font.PLAIN, 12f));
        a.setForeground(color);
        a.setBorder(BorderFactory.createEmptyBorder());
        a.setSize(new Dimension(width, Short.MAX_VALUE));
        a.setPreferredSize(new Dimension(width, a.getPreferredSize().height));
        a.setAlignmentX(Component.LEFT_ALIGNMENT);
        return a;
    }
    /**
     * A message box for problems with the input. It wraps to fit, so nothing is cut off:
     * a short title, a plain explanation, and an example of what works.
     */
    static final class Callout extends JPanel {
        private static final long serialVersionUID = 1L;
        private final transient Theme t;
        private final JLabel title;
        private final JTextArea body;
        private final JTextArea example;
        private final int width;

        Callout(Theme t, int width) {
            this.t = t;
            this.width = width;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            title = label(" ", t.sans(Font.BOLD, 13f), t.error);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            body = paragraph(t, " ", t.fg, width - 28);
            example = paragraph(t, " ", t.muted, width - 28);
            add(title);
            add(Box.createVerticalStrut(4));
            add(body);
            add(Box.createVerticalStrut(6));
            add(example);
            setVisible(false);
        }

        void show(String heading, String explanation, String exampleText) {
            title.setText(heading);
            setWrapped(body, explanation);
            example.setVisible(exampleText != null && !exampleText.isEmpty());
            if (example.isVisible()) setWrapped(example, "Example: " + exampleText);
            setVisible(true);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
            revalidate();
            repaint();
        }

        void clear() {
            setVisible(false);
            revalidate();
        }

        private void setWrapped(JTextArea area, String text) {
            area.setText(text);
            area.setSize(new Dimension(width - 28, Short.MAX_VALUE));
            area.setPreferredSize(null);
            area.setPreferredSize(new Dimension(width - 28, area.getPreferredSize().height));
            area.setMaximumSize(area.getPreferredSize());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.Color e = t.error;
            g2.setColor(new java.awt.Color(e.getRed(), e.getGreen(), e.getBlue(), t.dark ? 40 : 22));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new java.awt.Color(e.getRed(), e.getGreen(), e.getBlue(), 90));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
