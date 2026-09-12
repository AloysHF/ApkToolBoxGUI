package edu.jiangxin.apktoolbox.swing.extend.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Shared visual language for all tool panels.
 * Spacing, form layout, buttons and section cards stay consistent app-wide.
 */
public final class UiKit {

    public static final int GAP_XS = 4;
    public static final int GAP_SM = 8;
    public static final int GAP_MD = 12;
    public static final int GAP_LG = 16;
    public static final int PAD = 16;

    public static final int LABEL_WIDTH = 140;
    public static final int FIELD_HEIGHT = 28;
    public static final int BUTTON_HEIGHT = 28;
    public static final int BUTTON_WIDTH = 110;
    public static final int BUTTON_HEIGHT_PRIMARY = 30;
    public static final int BUTTON_WIDTH_PRIMARY = 120;
    public static final int CONTENT_MAX_WIDTH = 920;

    /** Android-inspired accent used only for primary actions / focus accents. */
    public static final Color ACCENT = new Color(0x3D, 0xDC, 0x84);
    public static final Color ACCENT_DARK = new Color(0x2E, 0xB8, 0x6B);
    public static final Color DANGER = new Color(0xFF, 0x6B, 0x6B);
    public static final Color MUTED = new Color(0x9E, 0xA3, 0xB0);

    private UiKit() {
    }

    /** Apply global UI defaults after FlatLaf has been installed. */
    public static void applyGlobalDefaults() {
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Button.arc", 6);
        UIManager.put("Component.arc", 6);
        UIManager.put("TextComponent.arc", 4);
        UIManager.put("TabbedPane.tabArc", 4);
        UIManager.put("TabbedPane.tabHeight", 30);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ToolPanel.borderConsumesSpace", false);
    }

    /** Section container with standard padding and optional titled border. */
    public static JPanel card(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(sectionBorder(title));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    public static Border sectionBorder(String title) {
        if (title == null || title.isEmpty()) {
            return new EmptyBorder(GAP_SM, GAP_SM, GAP_SM, GAP_SM);
        }
        Color lineColor = UIManager.getColor("Component.borderColor") != null
                ? UIManager.getColor("Component.borderColor")
                : new Color(0x4E, 0x52, 0x54);
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(lineColor, 1), title),
                new EmptyBorder(GAP_SM, GAP_XS, GAP_XS, GAP_XS));
    }

    /** Vertical stack with standard gaps. */
    public static JPanel stack(JComponent... children) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        boolean first = true;
        for (JComponent child : children) {
            if (child == null) {
                continue;
            }
            child.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (!first) {
                panel.add(Box.createVerticalStrut(GAP_MD));
            }
            panel.add(child);
            first = false;
        }
        return panel;
    }

    /**
     * Form row: fixed-width right-aligned label + flexible fields.
     * Accepts text fields, combos, checkboxes, buttons, etc.
     */
    public static JPanel formRow(String labelText, JComponent... fields) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(GAP_XS, GAP_XS, GAP_XS, GAP_XS);
        gc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        label.setMaximumSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        row.add(label, gc);

        gc.gridx = 1;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        if (fields.length == 0) {
            row.add(Box.createHorizontalGlue(), gc);
        } else {
            for (int i = 0; i < fields.length; i++) {
                gc.gridx = 1 + i;
                gc.weightx = i == fields.length - 1 ? 1.0 : 0.0;
                gc.fill = i == fields.length - 1 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
                JComponent field = fields[i];
                field.setAlignmentX(Component.LEFT_ALIGNMENT);
                if (field instanceof JTextField || field instanceof JButton) {
                    field.setPreferredSize(preferredFieldSize(field));
                    field.setMaximumSize(preferredFieldSize(field));
                }
                row.add(field, gc);
            }
        }
        return row;
    }

    private static Dimension preferredFieldSize(JComponent c) {
        int h = c instanceof JButton ? BUTTON_HEIGHT : FIELD_HEIGHT;
        int w = c instanceof JButton ? BUTTON_WIDTH : Math.max(c.getPreferredSize().width, 120);
        if (c.getPreferredSize().width > 0) {
            w = Math.max(w, c.getPreferredSize().width);
        }
        return new Dimension(w, h);
    }

    /** Checkbox row with standard height. */
    public static JPanel checkRow(JComponent... fields) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP_MD, GAP_XS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        for (JComponent field : fields) {
            field.setAlignmentY(Component.CENTER_ALIGNMENT);
            row.add(field);
        }
        return row;
    }

    /** Right-aligned action bar with primary last (or first if only one). */
    public static JPanel actionRow(JComponent... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_SM, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        for (JComponent button : buttons) {
            if (button instanceof JButton jButton) {
                if (jButton.getClientProperty("ui.primary") != null
                        && Boolean.TRUE.equals(jButton.getClientProperty("ui.primary"))) {
                    stylePrimaryButton(jButton);
                } else {
                    styleSecondaryButton(jButton);
                }
            }
            row.add(button);
        }
        return row;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.putClientProperty("ui.primary", Boolean.TRUE);
        stylePrimaryButton(button);
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        styleSecondaryButton(button);
        return button;
    }

    public static void stylePrimaryButton(JButton button) {
        button.setPreferredSize(new Dimension(BUTTON_WIDTH_PRIMARY, BUTTON_HEIGHT_PRIMARY));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH_PRIMARY, BUTTON_HEIGHT_PRIMARY));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT_PRIMARY));
        button.setMargin(new Insets(0, GAP_MD, 0, GAP_MD));
        button.setBackground(ACCENT);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    public static void styleSecondaryButton(JButton button) {
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT));
        button.setFocusPainted(false);
    }

    /** Uniform text field sizing. */
    public static JTextField field(JTextField field) {
        field.setPreferredSize(new Dimension(Math.max(field.getPreferredSize().width, 220), FIELD_HEIGHT));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        field.setMinimumSize(new Dimension(120, FIELD_HEIGHT));
        return field;
    }

    public static JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel dangerLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(DANGER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }

    /** Content panel constrained to a reasonable max width for readability. */
    public static JPanel contentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));
        return panel;
    }

    public static Dimension contentPreferred(int width, int height) {
        return new Dimension(Math.min(width, CONTENT_MAX_WIDTH), height);
    }
}
