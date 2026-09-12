package edu.jiangxin.apktoolbox.android.dumpsys;

import edu.jiangxin.apktoolbox.android.dumpsys.tojson.IDumpsys2Json;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.filepanel.FilePanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DumpsysPanel extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private JPanel dumpsysPanel;

    private FilePanel dumpsysTargetDirPanel;

    private JPanel dumpsysOptionPanel;
    private JPanel dumpsysTypeChoosePanel;

    private JPanel dumpsysOperationPanel;
    private JButton dumpsysStartButton;

    private JPanel analysisPanel;
    private FilePanel analysisFilePanel;
    private RSyntaxTextArea analysisOutputTextArea;
    private RTextScrollPane analysisOutputScrollPane;

    private JPanel analysisOperationPanel;

    public DumpsysPanel() {
        super();
    }

    @Override
    public void initUI() {
        setPreferredSize(new Dimension(760, 420));
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        createDumpsysPanel();
        tabbedPane.addTab("Dumpsys", null, dumpsysPanel, "Dumpsys");

        createAnalysisPanel();
        tabbedPane.addTab("Analysis", null, analysisPanel, "Analysis");
    }

    @Override
    public void afterPainted() {
        super.afterPainted();
        dumpsysTargetDirPanel.setPersistentKey(Constants.KEY_PREFIX + "dumpsysTargetDirPanel");
        analysisFilePanel.setPersistentKey(Constants.KEY_PREFIX + "dumpsysAnalysisFilePanel");
    }

    private void createDumpsysPanel() {
        dumpsysPanel = new JPanel();
        dumpsysPanel.setLayout(new BoxLayout(dumpsysPanel, BoxLayout.Y_AXIS));
        dumpsysPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        createDumpsysTargetDirPanel();
        dumpsysPanel.add(UiKit.formRow("Save Directory", dumpsysTargetDirPanel));
        dumpsysPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));

        createDumpsysOptionPanel();
        dumpsysPanel.add(dumpsysOptionPanel);
        dumpsysPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));

        createDumpsysOperationPanel();
        dumpsysPanel.add(dumpsysOperationPanel);
    }

    private void createDumpsysOptionPanel() {
        dumpsysOptionPanel = new JPanel();
        dumpsysOptionPanel.setLayout(new BoxLayout(dumpsysOptionPanel, BoxLayout.Y_AXIS));
        dumpsysOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dumpsysOptionPanel.setBorder(UiKit.sectionBorder("Dumpsys Types"));

        JCheckBox allSelectedCheckBox = new JCheckBox("Select All");
        allSelectedCheckBox.setSelected(false);
        allSelectedCheckBox.addActionListener(e -> {
            boolean selected = allSelectedCheckBox.isSelected();
            for (Component component : dumpsysTypeChoosePanel.getComponents()) {
                if (component instanceof JCheckBox checkBox) {
                    checkBox.setSelected(selected);
                    checkBox.setEnabled(!selected);
                }
            }
        });

        dumpsysTypeChoosePanel = new JPanel();
        dumpsysTypeChoosePanel.setLayout(new GridLayout(0, 4, UiKit.GAP_SM, UiKit.GAP_SM));
        dumpsysTypeChoosePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dumpsysTypeChoosePanel.setBorder(new EmptyBorder(UiKit.GAP_XS, 0, 0, 0));

        String[] types = {
                "input", "window", "SurfaceFlinger", "activity",
                "accessibility", "package", "alarm", "input_method",
                "sensorservice", "account", "display", "power", "battery"
        };
        for (String type : types) {
            JCheckBox checkBox = new JCheckBox(type);
            if ("input".equals(type)) {
                checkBox.setSelected(true);
            }
            dumpsysTypeChoosePanel.add(checkBox);
        }

        dumpsysOptionPanel.add(UiKit.checkRow(allSelectedCheckBox));
        dumpsysOptionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        dumpsysOptionPanel.add(dumpsysTypeChoosePanel);
    }

    private void createDumpsysOperationPanel() {
        dumpsysStartButton = UiKit.primaryButton("Start");
        dumpsysStartButton.setEnabled(true);
        dumpsysStartButton.addActionListener(new DumpsysStartButtonActionListener());
        dumpsysOperationPanel = UiKit.actionRow(dumpsysStartButton);
    }

    private void createAnalysisPanel() {
        analysisPanel = new JPanel();
        analysisPanel.setLayout(new BoxLayout(analysisPanel, BoxLayout.Y_AXIS));
        analysisPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        createAnalysisTargetFilePanel();
        analysisPanel.add(UiKit.formRow("Source File", analysisFilePanel));
        analysisPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));

        createAnalysisOutputPanel();
        analysisPanel.add(analysisOutputScrollPane);
        analysisPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));

        createAnalysisOperationPanel();
        analysisPanel.add(analysisOperationPanel);
    }

    private void createAnalysisTargetFilePanel() {
        analysisFilePanel = new FilePanel("Analysis File");
        analysisFilePanel.initialize();
        analysisFilePanel.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }

    private void createAnalysisOutputPanel() {
        analysisOutputTextArea = new RSyntaxTextArea();
        analysisOutputTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        analysisOutputTextArea.setCodeFoldingEnabled(true);
        analysisOutputTextArea.setEditable(false);

        analysisOutputScrollPane = new RTextScrollPane(analysisOutputTextArea);
        analysisOutputScrollPane.setPreferredSize(new Dimension(400, 250));
    }

    private void createAnalysisOperationPanel() {
        JButton analysisStartButton = UiKit.primaryButton("Start");
        analysisStartButton.setEnabled(true);
        analysisStartButton.addActionListener(new AnalysisStartButtonActionListener());
        analysisOperationPanel = UiKit.actionRow(analysisStartButton);
    }

    private void createDumpsysTargetDirPanel() {
        dumpsysTargetDirPanel = new FilePanel("Browse...");
        dumpsysTargetDirPanel.initialize();
        dumpsysTargetDirPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    }

    class DumpsysStartButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dumpsysStartButton.setEnabled(false);
            File targetDirFile = dumpsysTargetDirPanel.getFile();
            if (!targetDirFile.exists()) {
                JOptionPane.showMessageDialog(getFrame(), "Target directory does not exist!", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date(System.currentTimeMillis()));
            File targetDirWithTimestamp = new File(targetDirFile, timeStamp);

            if (!targetDirWithTimestamp.exists()) {
                boolean success = targetDirWithTimestamp.mkdirs();
                if (!success) {
                    JOptionPane.showMessageDialog(getFrame(), "Create target directory failed!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Component[] components = dumpsysTypeChoosePanel.getComponents();
            for (Component component : components) {
                if (component instanceof JCheckBox checkBox) {
                    String dumpsysType = checkBox.getText();
                    if (checkBox.isSelected()) {
                        dumpsysAndSaveToFile(dumpsysType, targetDirWithTimestamp);
                    }
                }
            }
            dumpsysStartButton.setEnabled(true);
        }

        private void dumpsysAndSaveToFile(String dumpsysType, File targetDir) {
            String command = "adb shell dumpsys " + dumpsysType;
            CommandLine cmdLine = CommandLine.parse(command);
            DefaultExecutor executor = DefaultExecutor.builder().get();
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 ByteArrayOutputStream errorStream = new ByteArrayOutputStream();) {
                PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
                executor.setStreamHandler(streamHandler);
                int exitValue = executor.execute(cmdLine);
                if (exitValue == 0) {
                    String result = outputStream.toString().trim();
                    if (StringUtils.isNotEmpty(result)) {
                        File outputFile = new File(targetDir, dumpsysType + ".dumpsys");
                        FileUtils.writeStringToFile(outputFile, result, "UTF-8");
                    }
                } else {
                    String errorResult = errorStream.toString().trim();
                    if (StringUtils.isNotEmpty(errorResult)) {
                        File errorFile = new File(targetDir, dumpsysType + ".error");
                        FileUtils.writeStringToFile(errorFile, errorResult, "UTF-8");
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(getFrame(), "Execute command failed:\n" + ex.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class AnalysisStartButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            File analysisFile = analysisFilePanel.getFile();
            if (!analysisFile.exists()) {
                JOptionPane.showMessageDialog(getFrame(), "Analysis file does not exist!", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String fileNameWithoutExtension = FilenameUtils.getBaseName(analysisFile.getName());
            String className = StringUtils.capitalize(fileNameWithoutExtension) + "2Json";
            IDumpsys2Json dumpsys2Json = getDumpsys2JsonInstance(className);
            if (dumpsys2Json != null) {
                String jsonResult = dumpsys2Json.dumpsys2Json(analysisFile);
                analysisOutputTextArea.setText(jsonResult);
                analysisOutputTextArea.setCaretPosition(0);
            }
        }

        private IDumpsys2Json getDumpsys2JsonInstance(String className) {
            try {
                String fullClassName = "edu.jiangxin.apktoolbox.android.dumpsys.tojson." + className;
                Class<?> clazz = Class.forName(fullClassName);
                if (IDumpsys2Json.class.isAssignableFrom(clazz)) {
                    return (IDumpsys2Json) clazz.getDeclaredConstructor().newInstance();
                } else {
                    JOptionPane.showMessageDialog(getFrame(), "Class " + fullClassName + " does not implement IDumpsys2Json!", "ERROR", JOptionPane.ERROR_MESSAGE);
                    return null;
                }
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(getFrame(), "Class not found: " + className, "ERROR", JOptionPane.ERROR_MESSAGE);
                return null;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(getFrame(), "Error loading class: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
    }
}
