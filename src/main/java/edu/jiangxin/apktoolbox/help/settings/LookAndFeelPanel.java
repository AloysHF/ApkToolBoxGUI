package edu.jiangxin.apktoolbox.help.settings;

import edu.jiangxin.apktoolbox.swing.extend.EasyChildTabbedPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

public class LookAndFeelPanel extends EasyChildTabbedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private JPanel optionPanel;

    private JComboBox<String> typeComboBox;

    private JPanel operationPanel;

    static {
        // Avoid install duplicated Look And Feel, we install them in static block
        UIManager.installLookAndFeel("Flat Light", "com.formdev.flatlaf.FlatLightLaf");
        UIManager.installLookAndFeel("Flat Dark", "com.formdev.flatlaf.FlatDarkLaf");
        UIManager.installLookAndFeel("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        UIManager.installLookAndFeel("Flat Darcula", "com.formdev.flatlaf.FlatDarculaLaf");
    }

    @Override
    public void createUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Look and Feel"));

        createOptionPanel();
        add(optionPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
        add(operationPanel);
    }

    private void createOptionPanel() {
        typeComboBox = new JComboBox<>();
        typeComboBox.setPreferredSize(new Dimension(260, UiKit.FIELD_HEIGHT));
        typeComboBox.setMaximumSize(new Dimension(260, UiKit.FIELD_HEIGHT));

        UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
        if (ArrayUtils.isEmpty(lookAndFeelInfos)) {
            typeComboBox.setEnabled(false);
        } else {
            typeComboBox.setEnabled(true);
            String className = conf.getString("look.and.feel.class.name");
            for (UIManager.LookAndFeelInfo info : lookAndFeelInfos) {
                typeComboBox.addItem(info.getName());
                if (Strings.CS.equals(className, info.getClassName())) {
                    typeComboBox.setSelectedItem(info.getName());
                }
            }
        }

        optionPanel = UiKit.formRow("Type", typeComboBox);
    }

    private void createOperationPanel() {
        JButton applyButton = UiKit.primaryButton("Apply");
        applyButton.addActionListener(new ApplyButtonActionListener());
        operationPanel = UiKit.actionRow(applyButton);
    }

    private final class ApplyButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String name = (String) typeComboBox.getSelectedItem();
            String className = getLookAndFeelClassNameFromName(name);
            if (className == null) {
                logger.warn("className is null");
                return;
            }
            conf.setProperty("look.and.feel.class.name", className);
            try {
                UIManager.setLookAndFeel(className);
            } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                logger.error("setLookAndFeel failed, use default instead", e);
            }
            SwingUtilities.updateComponentTreeUI(getFrame());
            getFrame().refreshSizeAndLocation();
        }
    }

    private String getLookAndFeelClassNameFromName(String name) {
        UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo info : lookAndFeelInfos) {
            if (Strings.CS.equals(name, info.getName())) {
                return info.getClassName();
            }
        }
        return null;
    }
}