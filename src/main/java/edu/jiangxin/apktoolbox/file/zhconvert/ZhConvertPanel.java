package edu.jiangxin.apktoolbox.file.zhconvert;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import edu.jiangxin.apktoolbox.swing.extend.FileListPanel;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import edu.jiangxin.apktoolbox.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.*;

public class ZhConvertPanel extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private JPanel northPanel;

    private JSplitPane centerPanel;

    private FileListPanel fileListPanel;

    private JTextField suffixTextField;

    private JCheckBox recursiveCheckBox;

    private JComboBox<String> comboBox;

    private JTextField keyText;

    private JTextField valueText;

    private JTextArea textArea;

    private JList<Object> transformList;

    private static ZHConverterUtils myZHConverterUtils = new ZHConverterUtils();

    public ZhConvertPanel() throws HeadlessException {
        super();
    }

    @Override
    public void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UiKit.GAP_SM, UiKit.GAP_SM, UiKit.GAP_SM, UiKit.GAP_SM));

        createNorthPanel();
        add(northPanel, BorderLayout.NORTH);

        createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
    }

    private void createNorthPanel() {
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileListPanel = new FileListPanel();
        fileListPanel.initialize();
        northPanel.add(fileListPanel);
        northPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));

        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.setBorder(UiKit.sectionBorder("Options"));
        northPanel.add(optionPanel);

        suffixTextField = UiKit.field(new JTextField());
        suffixTextField.setToolTipText("an array of extensions, ex. {\"java\",\"xml\"}. If this parameter is empty, all files are returned.");
        suffixTextField.setText(conf.getString("osconvert.suffix"));

        recursiveCheckBox = new JCheckBox("Recursive");
        recursiveCheckBox.setSelected(true);

        comboBox = new JComboBox<>();
        comboBox.addItem(Constants.zhSimple2zhTw);
        comboBox.addItem(Constants.zhTw2zhSimple);
        comboBox.setPreferredSize(new Dimension(180, UiKit.FIELD_HEIGHT));
        comboBox.setMaximumSize(new Dimension(180, UiKit.FIELD_HEIGHT));

        optionPanel.add(UiKit.formRow("Suffix", suffixTextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(UiKit.formRow("Mode", comboBox));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(UiKit.checkRow(recursiveCheckBox));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        JButton convertBtn = UiKit.primaryButton("Convert");
        convertBtn.addActionListener(new ConvertBtnActionListener());
        optionPanel.add(UiKit.actionRow(convertBtn));
    }

    private void createCenterPanel() {
        JPanel centerLeftTopPanel = new JPanel();
        centerLeftTopPanel.setLayout(new BoxLayout(centerLeftTopPanel, BoxLayout.Y_AXIS));
        centerLeftTopPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerLeftTopPanel.setBorder(UiKit.sectionBorder("Custom Phrase Rules"));

        JPanel keyValuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        keyValuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        keyValuePanel.setOpaque(false);

        keyText = UiKit.field(new JTextField(12));
        keyValuePanel.add(keyText);
        keyValuePanel.add(Box.createHorizontalStrut(UiKit.GAP_SM));

        valueText = UiKit.field(new JTextField(12));
        keyValuePanel.add(valueText);
        keyValuePanel.add(Box.createHorizontalStrut(UiKit.GAP_SM));

        JButton saveBtn = UiKit.secondaryButton("Add Rule");
        saveBtn.addActionListener(new SaveBtnActionListener());
        keyValuePanel.add(saveBtn);
        centerLeftTopPanel.add(keyValuePanel);

        JScrollPane centerLeftBottomPanel = new JScrollPane();
        textArea = new JTextArea();
        textArea.setMargin(new Insets(UiKit.GAP_MD, UiKit.GAP_MD, UiKit.GAP_MD, UiKit.GAP_MD));
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        centerLeftBottomPanel.setViewportView(textArea);
        centerLeftBottomPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerLeftBottomPanel.setBorder(UiKit.sectionBorder("Conversion Log"));
        JSplitPane centerLeftSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerLeftTopPanel, centerLeftBottomPanel);
        centerLeftSplitPanel.setResizeWeight(0.15);
        centerLeftSplitPanel.setContinuousLayout(true);

        transformList = new JList<>();
        transformList.setFixedCellHeight(26);
        refreshListData();
        transformList.setBorder(UiKit.sectionBorder("Phrase Dictionary"));
        JScrollPane centerRightScrollPanel = new JScrollPane(transformList);

        centerPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerLeftSplitPanel, centerRightScrollPanel);
        centerPanel.setDividerLocation(0.7f);
        centerPanel.setContinuousLayout(true);
        centerPanel.setBorder(BorderFactory.createEmptyBorder());
    }

    private final class ConvertBtnActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new Thread(() -> {
                String converType = comboBox.getSelectedItem().toString();
                logger.info("convert type: {}", converType);

                List<File> fileList = new ArrayList<>();
                for (File file : fileListPanel.getFileList()) {
                    String[] extensions = null;
                    if (StringUtils.isNotEmpty(suffixTextField.getText())) {
                        extensions = suffixTextField.getText().split(",");
                    }
                    fileList.addAll(FileUtils.listFiles(file, extensions, recursiveCheckBox.isSelected()));
                }
                Set<File> fileSet = new TreeSet<>(fileList);
                fileList.clear();
                fileList.addAll(fileSet);

                textArea.setCaretPosition(textArea.getText().length());
                try {
                    scanFolderAndConver(fileList, converType, textArea);
                    JOptionPane.showMessageDialog(getFrame(), "Convert finished", "Info", JOptionPane.INFORMATION_MESSAGE);
                    textArea.append("done...\n");
                    textArea.setCaretPosition(textArea.getText().length());
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(getFrame(), "Error: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    textArea.append("convert failed\n");
                    textArea.setCaretPosition(textArea.getText().length());
                }
            }).start();
        }
    }

    private final class SaveBtnActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String key = keyText.getText();
            String value = valueText.getText();

            if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                myZHConverterUtils.storeDataToProperties(key, value);
                JOptionPane.showMessageDialog(ZhConvertPanel.this, "Phrase rule added", "Info", JOptionPane.INFORMATION_MESSAGE);
                refreshListData();
                textArea.append("Added phrase rule: " + key + " <===> " + value + "\n");
            } else {
                JOptionPane.showMessageDialog(ZhConvertPanel.this, "Key and value must not be empty", "Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void refreshListData() {
        List<String> listModel = new ArrayList<>();
        Properties properties = myZHConverterUtils.getCharMap();
        for (String key2 : properties.stringPropertyNames()) {
            listModel.add(key2 + " <===> " + properties.getProperty(key2));
        }
        transformList.setListData(listModel.toArray());
    }

    private static void scanFolderAndConver(List<File> fileList, String converType, JTextArea jTextArea) throws IOException {
        jTextArea.append("Start converting files:\n");
        for (File file : fileList) {
            jTextArea.append("Converting: " + file + "\n");
            String content = org.apache.commons.io.FileUtils.readFileToString(file, "utf-8");

            if (converType.equals(Constants.zhSimple2zhTw)) {
                String str = myZHConverterUtils.myConvertToTW(content);
                String result = ZhConverterUtil.toTraditional(str);
                org.apache.commons.io.FileUtils.write(file, result, "UTF-8");
            } else {
                String str = myZHConverterUtils.myConvertToSimple(content);
                String result = ZhConverterUtil.toSimple(str);
                org.apache.commons.io.FileUtils.write(file, result, "UTF-8");
            }
            jTextArea.append("Done: " + file + "\n");
            jTextArea.setCaretPosition(jTextArea.getText().length());
        }
        jTextArea.append("All conversions finished.\n");
        jTextArea.append("==========================================================================\n");
    }
}
