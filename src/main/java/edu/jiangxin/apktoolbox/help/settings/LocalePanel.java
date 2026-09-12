package edu.jiangxin.apktoolbox.help.settings;

import edu.jiangxin.apktoolbox.swing.extend.EasyChildTabbedPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Locale;

public class LocalePanel extends EasyChildTabbedPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private JPanel optionPanel;

    private JComboBox<String> typeComboBox;

    private JPanel operationPanel;

    private static final String[] SUPPORTED_LANGUAGES = {Locale.CHINESE.getLanguage(), Locale.ENGLISH.getLanguage()};

    @Override
    public void createUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Display Language"));

        createOptionPanel();
        add(optionPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
        add(operationPanel);
    }

    private void createOptionPanel() {
        typeComboBox = new JComboBox<>();
        typeComboBox.setPreferredSize(new Dimension(200, UiKit.FIELD_HEIGHT));
        typeComboBox.setMaximumSize(new Dimension(200, UiKit.FIELD_HEIGHT));

        String currentLocaleLanguage = conf.getString("locale.language");
        if (StringUtils.isEmpty(currentLocaleLanguage)) {
            currentLocaleLanguage = Locale.ENGLISH.getLanguage();
            conf.setProperty("locale.language", currentLocaleLanguage);
        }

        for (String language : SUPPORTED_LANGUAGES) {
            typeComboBox.addItem(language);
            if (Strings.CS.equals(currentLocaleLanguage, language)) {
                typeComboBox.setSelectedItem(language);
            }
        }

        optionPanel = UiKit.formRow("Locale", typeComboBox);
    }

    private void createOperationPanel() {
        JButton applyButton = UiKit.primaryButton("Apply");
        applyButton.addActionListener(new ApplyButtonActionListener());
        operationPanel = UiKit.actionRow(applyButton);
    }

    private final class ApplyButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String language = (String)typeComboBox.getSelectedItem();
            if (StringUtils.isNotEmpty(language)) {
                conf.setProperty("locale.language", language);
                JOptionPane.showMessageDialog(LocalePanel.this, "Setting locale successfully, restart the program please");
            }
        }
    }
}
