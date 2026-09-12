package edu.jiangxin.apktoolbox.file;

import edu.jiangxin.apktoolbox.file.core.EncoderConvert;
import edu.jiangxin.apktoolbox.file.core.EncoderDetector;
import edu.jiangxin.apktoolbox.swing.extend.autocomplete.AutoCompleteComboBox;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.FileListPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class EncodeConvertPanel extends EasyPanel {
    private static final long serialVersionUID = 1L;

    private FileListPanel srcPanel;

    private JPanel optionPanel;

    private JTextField suffixTextField;

    private JCheckBox autoDetectCheckBox;

    private AutoCompleteComboBox<String> fromComboBox;

    private JCheckBox recursiveCheckBox;

    private AutoCompleteComboBox<String> toComboBox;

    private JPanel operationPanel;

    public EncodeConvertPanel() throws HeadlessException {
        super();
    }

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        createSrcPanel();
        add(srcPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOptionPanel();
        add(optionPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
        add(operationPanel);
    }

    private void createSrcPanel() {
        srcPanel = new FileListPanel();
        srcPanel.initialize();
    }

    private void createOptionPanel() {
        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.setBorder(UiKit.sectionBorder("Options"));

        suffixTextField = UiKit.field(new JTextField());
        suffixTextField.setToolTipText("an array of extensions, ex. {\"java\",\"xml\"}. If this parameter is empty, all files are returned.");
        suffixTextField.setText(conf.getString("encodeconvert.suffix"));

        recursiveCheckBox = new JCheckBox("Recursive");
        recursiveCheckBox.setSelected(true);

        fromComboBox = new AutoCompleteComboBox<>();
        fromComboBox.initialize();
        fromComboBox.setEnabled(false);

        autoDetectCheckBox = new JCheckBox("Auto Detect");
        autoDetectCheckBox.setSelected(true);
        autoDetectCheckBox.addItemListener(e -> fromComboBox.setEnabled(!(e.getStateChange() == ItemEvent.SELECTED)));

        toComboBox = new AutoCompleteComboBox<>();
        toComboBox.initialize();
        toComboBox.setSelectedItem(conf.getString("encodeconvert.to"));

        for (String charset : Charset.availableCharsets().keySet()) {
            fromComboBox.addItem(charset);
            toComboBox.addItem(charset);
        }

        optionPanel.add(UiKit.formRow("Suffix", suffixTextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(UiKit.checkRow(recursiveCheckBox, autoDetectCheckBox));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(UiKit.formRow("From", fromComboBox));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(UiKit.formRow("To", toComboBox));
    }

    private void createOperationPanel() {
        JButton convertButton = UiKit.primaryButton("Convert");
        convertButton.addActionListener(new ConvertButtonActionListener());
        operationPanel = UiKit.actionRow(convertButton);
    }

    private class ConvertButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            conf.setProperty("encodeconvert.suffix", suffixTextField.getText());
            conf.setProperty("encodeconvert.to", toComboBox.getSelectedItem().toString());

            try {
                List<File> fileList = new ArrayList<>();
                for (File file : srcPanel.getFileList()) {
                    String[] extensions = null;
                    if (StringUtils.isNotEmpty(suffixTextField.getText())) {
                        extensions = suffixTextField.getText().split(",");
                    }
                    fileList.addAll(FileUtils.listFiles(file, extensions, recursiveCheckBox.isSelected()));
                }
                Set<File> fileSet = new TreeSet<>(fileList);
                fileList.clear();
                fileList.addAll(fileSet);

                for (File file : fileList) {
                    String fromEncoder;
                    if (autoDetectCheckBox.isSelected()) {
                        fromEncoder = EncoderDetector.judgeFile(file.getCanonicalPath());
                    } else {
                        fromEncoder = fromComboBox.getSelectedItem().toString();
                    }
                    String toEncoder = toComboBox.getSelectedItem().toString();
                    logger.info("processing: {} from {} to {}", file.getCanonicalPath(), fromEncoder, toEncoder);
                    EncoderConvert.encodeFile(file.getCanonicalPath(), fromEncoder, toEncoder);
                    logger.info("processed");
                }

                logger.info("convert finish");
            } catch (IOException e1) {
                logger.error("convert fail", e1);
            }
        }
    }
}
