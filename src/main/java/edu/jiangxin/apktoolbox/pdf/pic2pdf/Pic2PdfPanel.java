package edu.jiangxin.apktoolbox.pdf.pic2pdf;

import edu.jiangxin.apktoolbox.pdf.PdfUtils;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.FileListPanel;
import edu.jiangxin.apktoolbox.swing.extend.filepanel.FilePanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serial;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pic2PdfPanel  extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private FileListPanel fileListPanel;

    private JCheckBox isRecursiveSearched;

    private FilePanel targetDirPanel;

    private JButton startButton;
    private JButton cancelButton;

    private transient WorkderThread convertThread;

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        createInputPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOutputPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOptionPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
    }

    private void createInputPanel() {
        fileListPanel = new FileListPanel();
        fileListPanel.initialize();
        add(fileListPanel);
    }

    private void createOutputPanel() {
        JPanel outputPanel = new JPanel();
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.Y_AXIS));
        outputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputPanel.setBorder(UiKit.sectionBorder("Output"));

        targetDirPanel = new FilePanel("Target Directory");
        targetDirPanel.initialize();
        targetDirPanel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        outputPanel.add(UiKit.formRow("Target", targetDirPanel));
        add(outputPanel);
    }

    private void createOptionPanel() {
        JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.setBorder(UiKit.sectionBorder("Options"));

        isRecursiveSearched = new JCheckBox("Recursive Search");
        isRecursiveSearched.setSelected(true);
        optionPanel.add(UiKit.checkRow(isRecursiveSearched));

        add(optionPanel);
    }

    private void createOperationPanel() {
        startButton = UiKit.primaryButton("Start");
        cancelButton = UiKit.secondaryButton("Cancel");
        cancelButton.setEnabled(false);
        startButton.addActionListener(new OperationButtonActionListener());
        cancelButton.addActionListener(new OperationButtonActionListener());
        add(UiKit.actionRow(cancelButton, startButton));
    }

    private void processFile(File encryptedFile, File targetDir) {
        try {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            PdfUtils.removePasswordWithIText(encryptedFile, targetDir);
        } catch (Throwable t) {
            logger.error("Process file failed: {}", encryptedFile.getAbsolutePath(), t);
        }
    }

    class OperationButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source.equals(startButton)) {
                startButton.setEnabled(false);
                cancelButton.setEnabled(true);
                convertThread = new WorkderThread(isRecursiveSearched.isSelected());
                convertThread.start();
            } else if (source.equals(cancelButton)) {
                startButton.setEnabled(true);
                cancelButton.setEnabled(false);
                if (convertThread.isAlive()) {
                    convertThread.interrupt();
                    convertThread.executorService.shutdownNow();
                }
            }

        }
    }

    class WorkderThread extends Thread {
        public final ExecutorService executorService;
        private final boolean isRecursiveSearched;

        public WorkderThread(boolean isRecursiveSearched) {
            super();
            this.isRecursiveSearched = isRecursiveSearched;
            this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        }

        @Override
        public void run() {
            try {
                List<File> fileList = fileListPanel.getFileList();
                Set<File> fileSet = new TreeSet<>();
                String[] extensions = new String[]{"jpg", "jpeg", "png"};
                for (File file : fileList) {
                    fileSet.addAll(FileUtils.listFiles(file, extensions, isRecursiveSearched));
                }

                PdfUtils.imagesToPdf(fileSet, new File(targetDirPanel.getFile(), "result.pdf"));
            } catch (Exception e) {
                logger.error("Remove process failed", e);
            } finally {
                executorService.shutdown();
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                });
            }
        }
    }
}
