package edu.jiangxin.apktoolbox.file.batchrename;

import edu.jiangxin.apktoolbox.swing.extend.listener.SelectDirectoryListener;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

//https://www.zhihu.com/question/50890909
public class BatchRenamePanel extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String STRING_TYPE_PARENT_NAME = "使用父目录名";

    private static final String STRING_TYPE_PARENT_PATH = "使用目录路径";

    private static final String STRING_TYPE_SPECIALISE_STRING = "使用指定字符";

    private JPanel warningPanel;

    private JPanel sourcePanel;

    private JPanel rulePanel;

    private JPanel operationPanel;

    private JPanel statusPanel;

    private JTextField sourceTextField;
    private JTextField suffixTextField;
    private JCheckBox recursiveCheckBox;
    private JRadioButton ruleRadioButton1;
    private JRadioButton ruleRadioButton2;
    private JRadioButton ruleRadioButton3;
    private JRadioButton ruleRadioButton4;
    private JRadioButton ruleRadioButton5;
    private JRadioButton ruleRadioButton6;
    private JTextField textField21;
    private JSpinner spinner21;
    private JCheckBox checkBox31;
    private JCheckBox checkBox32;
    private JCheckBox checkBox33;
    private JTextField textField31;
    private JTextField textField32;
    private JSpinner spinner31;
    private JSpinner spinner32;
    private JSpinner spinner41;
    private JComboBox<String> comboBoxStringType;
    private JTextField textField43;
    private JSpinner spinner51;
    private JSpinner spinner52;
    private JTextField textField61;
    private JTextField textField62;
    private JTextField currentTextField;

    public BatchRenamePanel() throws HeadlessException {
        super();
    }

    @Override
    public void initUI() {
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(boxLayout);

        createWarningPanel();
        add(warningPanel);
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createSourcePanel();
        add(sourcePanel);
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createRulePanel();
        add(rulePanel);
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createOperationPanel();
        add(operationPanel);
        add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));

        createStatusPanel();
        add(statusPanel);
    }

    private void createWarningPanel() {
        warningPanel = new JPanel();
        warningPanel.setLayout(new BoxLayout(warningPanel, BoxLayout.X_AXIS));
        warningPanel.setBorder(UiKit.sectionBorder("Warning"));

        JTextArea warningTextArea = new JTextArea();
        warningTextArea.setText("It is dangerous to operate on the original files!\nPlease back up first and check the result carefully at the end!");
        warningTextArea.setForeground(UiKit.DANGER);
        warningTextArea.setFont(getFont().deriveFont(Font.BOLD, 14f));
        warningTextArea.setOpaque(false);
        warningTextArea.setBorder(null);
        warningTextArea.setEditable(false);
        warningTextArea.setLineWrap(true);
        warningTextArea.setWrapStyleWord(true);
        warningTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        warningPanel.add(warningTextArea);
        warningPanel.add(Box.createHorizontalGlue());
    }

    private void createSourcePanel() {
        sourcePanel = new JPanel();
        sourcePanel.setBorder(BorderFactory.createTitledBorder("1. 源文件"));
        sourcePanel.setLayout(new BorderLayout());

        JPanel secondLevelPanel = new JPanel();
        secondLevelPanel.setLayout(new BoxLayout(secondLevelPanel, BoxLayout.Y_AXIS));
        sourcePanel.add(secondLevelPanel);

        JPanel thirdLevelPanel1 = new JPanel();
        thirdLevelPanel1.setLayout(new BoxLayout(thirdLevelPanel1, BoxLayout.X_AXIS));

        JPanel thirdLevelPanel2 = new JPanel();
        thirdLevelPanel2.setLayout(new BoxLayout(thirdLevelPanel2, BoxLayout.X_AXIS));

        secondLevelPanel.add(thirdLevelPanel1);
        secondLevelPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        secondLevelPanel.add(thirdLevelPanel2);

        JLabel label = new JLabel("源文件所在目录：");
        sourceTextField = new JTextField();
        JButton button = new JButton("选择");
        button.addActionListener(new SelectDirectoryListener("Select a directory", sourceTextField));
        thirdLevelPanel1.add(label);
        thirdLevelPanel1.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        thirdLevelPanel1.add(sourceTextField);
        thirdLevelPanel1.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        thirdLevelPanel1.add(button);

        JLabel label2 = new JLabel("后缀：");
        suffixTextField = new JTextField();
        suffixTextField.setToolTipText("an array of extensions, ex. java,xml,mp4. If this parameter is empty, all files are returned.");
        recursiveCheckBox = new JCheckBox("包括子目录");

        thirdLevelPanel2.add(label2);
        thirdLevelPanel2.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        thirdLevelPanel2.add(suffixTextField);
        thirdLevelPanel2.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        thirdLevelPanel2.add(recursiveCheckBox);
    }

    private void createRulePanel() {
        rulePanel = new JPanel();
        rulePanel.setBorder(BorderFactory.createTitledBorder("3. 更名规则"));
        rulePanel.setLayout(new BorderLayout());

        JPanel secondLevelPanel = new JPanel();
        secondLevelPanel.setLayout(new BoxLayout(secondLevelPanel, BoxLayout.Y_AXIS));
        rulePanel.add(secondLevelPanel);

        JPanel thirdLevelPanel1 = new JPanel();
        thirdLevelPanel1.setLayout(new FlowLayout(FlowLayout.LEFT,10,3));

        JPanel thirdLevelPanel2 = new JPanel();
        thirdLevelPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,10,3));

        JPanel thirdLevelPanel3 = new JPanel();
        thirdLevelPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,10,3));

        JPanel thirdLevelPanel4 = new JPanel();
        thirdLevelPanel4.setLayout(new FlowLayout(FlowLayout.LEFT,10,3));

        JPanel thirdLevelPanel5 = new JPanel();
        thirdLevelPanel5.setLayout(new FlowLayout(FlowLayout.LEFT,10,3));

        JPanel thirdLevelPanel6 = new JPanel();
        thirdLevelPanel6.setLayout(new FlowLayout(FlowLayout.LEFT,10,3));

        secondLevelPanel.add(thirdLevelPanel1);
        secondLevelPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        secondLevelPanel.add(thirdLevelPanel2);
        secondLevelPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        secondLevelPanel.add(thirdLevelPanel3);
        secondLevelPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        secondLevelPanel.add(thirdLevelPanel4);
        secondLevelPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        secondLevelPanel.add(thirdLevelPanel5);
        secondLevelPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        secondLevelPanel.add(thirdLevelPanel6);

        ButtonGroup buttonGroup = new ButtonGroup();

        {
            ruleRadioButton1 = new JRadioButton("只在文件重名时才更名");
            ruleRadioButton1.setSelected(true);
            thirdLevelPanel1.add(ruleRadioButton1);
            buttonGroup.add(ruleRadioButton1);
        }

        {
            ruleRadioButton2 = new JRadioButton("将前缀{String}后的数字改为{Int}位");
            thirdLevelPanel2.add(ruleRadioButton2);
            buttonGroup.add(ruleRadioButton2);

            JLabel label21 = new JLabel("String");
            textField21 = new JTextField("");
            JLabel label22 = new JLabel("Int");
            spinner21 = new JSpinner();
            spinner21.setValue(4);
            thirdLevelPanel2.add(label21);
            thirdLevelPanel2.add(textField21);
            thirdLevelPanel2.add(label22);
            thirdLevelPanel2.add(spinner21);
        }

        {
            ruleRadioButton3 = new JRadioButton("重编号所有文件");
            thirdLevelPanel3.add(ruleRadioButton3);
            buttonGroup.add(ruleRadioButton3);

            checkBox31 = new JCheckBox("编号先按原文件名排序");
            checkBox32 = new JCheckBox("按末尾数字排序");
            checkBox33 = new JCheckBox("用目录名为前缀");
            JLabel label31 = new JLabel("分隔字符");
            textField31 = new JTextField("_");
            JLabel label32 = new JLabel("前缀");
            textField32 = new JTextField("File");
            JLabel label33 = new JLabel("起始号码");
            spinner31 = new JSpinner();
            spinner31.setValue(1);
            JLabel label34 = new JLabel("号码位数");
            spinner32 = new JSpinner();
            spinner32.setValue(3);
            thirdLevelPanel3.add(checkBox31);
            thirdLevelPanel3.add(checkBox32);
            thirdLevelPanel3.add(checkBox33);
            thirdLevelPanel3.add(label31);
            thirdLevelPanel3.add(textField31);
            thirdLevelPanel3.add(label32);
            thirdLevelPanel3.add(textField32);
            thirdLevelPanel3.add(label33);
            thirdLevelPanel3.add(spinner31);
            thirdLevelPanel3.add(label34);
            thirdLevelPanel3.add(spinner32);
        }

        {
            ruleRadioButton4 = new JRadioButton("在第{Int}个字符后添加{String}");
            thirdLevelPanel4.add(ruleRadioButton4);
            buttonGroup.add(ruleRadioButton4);

            JLabel stringType = new JLabel("字符串类型");

            comboBoxStringType = new JComboBox<>();
            comboBoxStringType.addItem(STRING_TYPE_PARENT_NAME);
            comboBoxStringType.addItem(STRING_TYPE_PARENT_PATH);
            comboBoxStringType.addItem(STRING_TYPE_SPECIALISE_STRING);
            comboBoxStringType.setSelectedItem(STRING_TYPE_SPECIALISE_STRING);
            comboBoxStringType.addItemListener(e -> textField43.setEditable(e.getItem().equals(STRING_TYPE_SPECIALISE_STRING)));

            JLabel label41 = new JLabel("Int");
            spinner41 = new JSpinner();
            spinner41.setValue(0);

            JLabel label43 = new JLabel("String");
            textField43 = new JTextField("");

            thirdLevelPanel4.add(label41);
            thirdLevelPanel4.add(spinner41);
            thirdLevelPanel4.add(label43);
            thirdLevelPanel4.add(textField43);
            thirdLevelPanel4.add(stringType);
            thirdLevelPanel4.add(comboBoxStringType);
        }

        {
            ruleRadioButton5 = new JRadioButton("删除自第{Int:1}个字符开始的{Int:2}个字符");
            thirdLevelPanel5.add(ruleRadioButton5);
            buttonGroup.add(ruleRadioButton5);

            JLabel label51 = new JLabel("Int:1");
            spinner51 = new JSpinner();
            spinner51.setValue(1);
            JLabel label52 = new JLabel("Int:2");
            spinner52 = new JSpinner();
            spinner52.setValue(1);
            thirdLevelPanel5.add(label51);
            thirdLevelPanel5.add(spinner51);
            thirdLevelPanel5.add(label52);
            thirdLevelPanel5.add(spinner52);
        }

        {
            ruleRadioButton6 = new JRadioButton("将文件名中出现的字符串{String:1}替换为{String:2}");
            thirdLevelPanel6.add(ruleRadioButton6);
            buttonGroup.add(ruleRadioButton6);

            JLabel label61 = new JLabel("String:1");
            textField61 = new JTextField("");
            JLabel label62 = new JLabel("String:2");
            textField62 = new JTextField("");
            thirdLevelPanel6.add(label61);
            thirdLevelPanel6.add(textField61);
            thirdLevelPanel6.add(label62);
            thirdLevelPanel6.add(textField62);
        }
    }

    private void createOperationPanel() {
        operationPanel = new JPanel();
        operationPanel.setBorder(UiKit.sectionBorder("6. 操作"));
        operationPanel.setLayout(new BorderLayout());

        JButton button = UiKit.primaryButton("Start");
        button.addActionListener(new StartButtonActionListener());
        operationPanel.add(UiKit.actionRow(button), BorderLayout.CENTER);
    }

    private void createStatusPanel() {
        statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createTitledBorder("正在处理"));
        statusPanel.setLayout(new BorderLayout());

        JPanel secondLevelPanel = new JPanel();
        secondLevelPanel.setLayout(new BoxLayout(secondLevelPanel, BoxLayout.Y_AXIS));
        statusPanel.add(secondLevelPanel);

        currentTextField = new JTextField();
        secondLevelPanel.add(currentTextField);
    }

    private final class StartButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String sourceString = sourceTextField.getText();
            if (StringUtils.isEmpty(sourceString)) {
                JOptionPane.showMessageDialog(BatchRenamePanel.this, "sourceString is empty", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            File sourceFile = new File(sourceString);
            if (!sourceFile.exists() || !sourceFile.isDirectory()) {
                JOptionPane.showMessageDialog(BatchRenamePanel.this, "sourceFile does not exist or not directory", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] extensions = null;
            if (StringUtils.isNotEmpty(suffixTextField.getText())) {
                extensions = suffixTextField.getText().split(",");
            }
            Collection<File> fileCollection = FileUtils.listFiles(sourceFile, extensions, recursiveCheckBox.isSelected());
            Set<File> fileSet = new TreeSet<>(fileCollection);
            if (fileSet.isEmpty()) {
                JOptionPane.showMessageDialog(BatchRenamePanel.this, "fileSet is empty", "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (File file : fileSet) {
                renameSingleFile(file);
            }
        }
    }

    private void renameSingleFile(File file) {
        String sourceFile = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath());
        String sourceFileName = FilenameUtils.getName(sourceFile);
        String currentDir = FilenameUtils.getPath(sourceFile);
        String targetFileName = "";
        if (ruleRadioButton1.isSelected()) {
            logger.info("unimplemented");
        } else if (ruleRadioButton2.isSelected()) {
            targetFileName = getNewFileNameWhen2Selected(sourceFileName);
        } else if (ruleRadioButton3.isSelected()) {
            targetFileName = getNewFileNameWhen3Selected();
        } else if (ruleRadioButton4.isSelected()) {
            targetFileName = getNewFileNameWhen4Selected(file);
        } else if (ruleRadioButton5.isSelected()) {
            targetFileName = getNewFileNameWhen5Selected(sourceFileName);
        } else if (ruleRadioButton6.isSelected()) {
            targetFileName = getNewFileNameWhen6Selected(sourceFileName);
        } else {
            logger.error("ruleRadioButton select status error");
            return;
        }

        try {
            Path sourcePath = new File(sourceFile).toPath();
            Files.move(sourcePath, sourcePath.resolveSibling(targetFileName));
            StringBuilder sb = new StringBuilder();
            sb.append(currentDir).append(" [").append(sourceFileName).append("]->[").append(targetFileName).append("]");
            currentTextField.setText(sb.toString());
            logger.info(sb.toString());
        } catch (IOException e) {
            logger.error("copy file failed: IOException. current: " + sourceFile);
        }
    }

    private String getNewFileNameWhen2Selected(String oldFileName) {
        String prefix = textField21.getText();
        String sourceFileNameWithoutSuffix = FilenameUtils.getBaseName(oldFileName);
        String serialNumberStr = StringUtils.substringAfter(sourceFileNameWithoutSuffix, prefix);
        int serialNumber = Integer.parseInt(serialNumberStr);
        int digit = (Integer) spinner21.getValue();
        String format = "%0" + digit + "d";
        return oldFileName.replaceAll(serialNumberStr, String.format(format, serialNumber));
    }

    private String getNewFileNameWhen3Selected() {
        String separator = textField31.getText();
        String prefix = textField32.getText();
        int start = (Integer) spinner31.getValue();
        int digit = (Integer) spinner32.getValue();
        boolean isCheckBox31Selected = checkBox31.isSelected();
        boolean isCheckBox32Selected = checkBox32.isSelected();
        boolean isCheckBox33Selected = checkBox33.isSelected();
        logger.info("unimplemented: separator: {}, prefix: {}, start: {}, digit: {}", separator, prefix, start, digit);
        logger.info("unimplemented: isCheckBox31Selected: {}, isCheckBox32Selected: {}, isCheckBox33Selected: {}",
                isCheckBox31Selected, isCheckBox32Selected, isCheckBox33Selected);
        return "";
    }

    private String getNewFileNameWhen4Selected(File file) {
        String sourceFile = FilenameUtils.normalizeNoEndSeparator(file.getAbsolutePath());
        String sourceFileName = FilenameUtils.getName(sourceFile);
        int index = (Integer) spinner41.getValue();
        String insertString = "";
        switch ((String) comboBoxStringType.getSelectedItem()) {
            case STRING_TYPE_PARENT_PATH:
                break;
            case STRING_TYPE_PARENT_NAME:
                String parentFile = FilenameUtils.normalizeNoEndSeparator(file.getParentFile().getAbsolutePath());
                insertString = FilenameUtils.getName(parentFile);
                break;
            case STRING_TYPE_SPECIALISE_STRING:
                insertString = textField43.getText();
                break;
        }
        return StringUtils.substring(sourceFileName, 0, index) + insertString + StringUtils.substring(sourceFileName, index);
    }

    private String getNewFileNameWhen5Selected(String oldFileName) {
        int start = (Integer) spinner51.getValue();
        int length = (Integer) spinner52.getValue();
        return StringUtils.substring(oldFileName, 0, start - 1) + StringUtils.substring(oldFileName, start + length - 1);
    }

    private String getNewFileNameWhen6Selected(String oldFileName) {
        return Strings.CS.replace(oldFileName, textField61.getText(), textField62.getText());
    }
}
