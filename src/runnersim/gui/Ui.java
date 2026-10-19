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
import javax.swing.border.AbstractBorder;

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
    static final class FieldBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final transient Theme t;

        FieldBorder(Theme t) { this.t = t; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean focused = c.hasFocus();
            g2.setColor(focused ? t.accent : t.separator);
            g2.setStroke(new BasicStroke(focused ? 2f : 1f));
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
        JPanel p = column();
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(label(label, t.sans(Font.PLAIN, 12f), t.muted), BorderLayout.WEST);
        if (!hint.isEmpty()) top.add(label(hint, t.sans(Font.PLAIN, 11f), t.faint), BorderLayout.EAST);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
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
        JPanel r = new JPanel(new BorderLayout(12, 0));
        r.setOpaque(false);
        r.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        r.add(label(label, t.sans(Font.PLAIN, 13f), t.muted), BorderLayout.WEST);
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
}
