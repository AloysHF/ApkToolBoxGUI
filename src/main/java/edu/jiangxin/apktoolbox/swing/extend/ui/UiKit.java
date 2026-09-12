package edu.jiangxin.apktoolbox.swing.extend.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
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
import java.awt.FontMetrics;
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
    public static final int BUTTON_WIDTH = 120;
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
     * Text fields/combos expand; trailing action buttons keep text-based fixed width.
     */
    public static JPanel formRow(String labelText, JComponent... fields) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(GAP_XS, GAP_XS, GAP_XS, GAP_XS);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        label.setMinimumSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        label.setMaximumSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        row.add(label, gc);

        for (int i = 0; i < fields.length; i++) {
            JComponent field = fields[i];
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            boolean isAction = field instanceof JButton;
            boolean expand = !isAction && !(field instanceof JSpinner);
            gc.gridx = 1 + i;
            gc.weightx = expand ? 1.0 : 0.0;
            gc.fill = expand ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
            if (isAction) {
                styleSecondaryButton((JButton) field);
            } else if (field instanceof JSpinner spinner) {
                spinner.setPreferredSize(new Dimension(90, FIELD_HEIGHT));
                spinner.setMaximumSize(new Dimension(90, FIELD_HEIGHT));
                spinner.setMinimumSize(new Dimension(70, FIELD_HEIGHT));
            } else if (expand) {
                field.setPreferredSize(new Dimension(Math.max(field.getPreferredSize().width, 180), FIELD_HEIGHT));
                field.setMinimumSize(new Dimension(120, FIELD_HEIGHT));
                field.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
            }
            row.add(field, gc);
        }

        int rowHeight = FIELD_HEIGHT + GAP_XS * 2;
        row.setPreferredSize(new Dimension(Math.max(row.getPreferredSize().width, 360), rowHeight));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        return row;
    }

    private static boolean isExpandable(JComponent c) {
        return !(c instanceof JButton);
    }

    /** Checkbox / radio row with fixed height so BoxLayout does not stretch it. */
    public static JPanel checkRow(JComponent... fields) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP_MD, GAP_XS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        for (JComponent field : fields) {
            field.setAlignmentY(Component.CENTER_ALIGNMENT);
            row.add(field);
        }
        int rowHeight = FIELD_HEIGHT + GAP_XS * 2;
        row.setPreferredSize(new Dimension(Math.max(row.getPreferredSize().width, 240), rowHeight));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        return row;
    }

    /** Right-aligned action bar with primary last (or first if only one). */
    public static JPanel actionRow(JComponent... buttons) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_SM, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(Math.max(row.getPreferredSize().width, 200), BUTTON_HEIGHT_PRIMARY + GAP_XS));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTON_HEIGHT_PRIMARY + GAP_XS));
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
        int w = Math.max(BUTTON_WIDTH_PRIMARY, textWidth(button, GAP_LG));
        button.setPreferredSize(new Dimension(w, BUTTON_HEIGHT_PRIMARY));
        button.setMinimumSize(new Dimension(w, BUTTON_HEIGHT_PRIMARY));
        // Keep width fixed so BoxLayout parents cannot stretch the accent button.
        button.setMaximumSize(new Dimension(w, BUTTON_HEIGHT_PRIMARY));
        button.setMargin(new Insets(0, GAP_MD, 0, GAP_MD));
        button.setBackground(ACCENT);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    public static void styleSecondaryButton(JButton button) {
        int w = Math.max(BUTTON_WIDTH, textWidth(button, GAP_MD));
        button.setPreferredSize(new Dimension(w, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(w, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(w, BUTTON_HEIGHT));
        button.setFocusPainted(false);
    }

    private static int textWidth(JButton button, int pad) {
        FontMetrics fm = button.getFontMetrics(button.getFont());
        int text = fm.stringWidth(button.getText() == null ? "" : button.getText());
        return text + pad * 2 + 8;
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
