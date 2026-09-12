package edu.jiangxin.apktoolbox.pdf.finder;

import edu.jiangxin.apktoolbox.pdf.PdfUtils;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.FileListPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.DateUtils;
import edu.jiangxin.apktoolbox.utils.ExcelExporter;
import edu.jiangxin.apktoolbox.utils.FileUtils;
import edu.jiangxin.apktoolbox.utils.RevealFileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.Serial;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class PdfFinderPanel extends EasyPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private JTabbedPane tabbedPane;

    private JPanel mainPanel;

    private FileListPanel fileListPanel;

    private JRadioButton scannedRadioButton;

    private JRadioButton encryptedRadioButton;

    private JRadioButton nonOutlineRadioButton;

    private JRadioButton hasAnnotationsRadioButton;

    private JSpinner thresholdSpinner;

    private JCheckBox isRecursiveSearched;

    private JPanel resultPanel;

    private JTable resultTable;

    private DefaultTableModel resultTableModel;

    private JButton searchButton;
    private JButton cancelButton;

    private JProgressBar progressBar;

    private JMenuItem openDirMenuItem;

    private JMenuItem copyFilesMenuItem;

    private transient SearchThread searchThread;

    private transient final List<File> resultFileList = new ArrayList<>();


    @Override
    public void initUI() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        createMainPanel();
        tabbedPane.addTab("Options", null, mainPanel, "Show Search Options");

        createResultPanel();
        tabbedPane.addTab("Result", null, resultPanel, "Show Search Result");
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileListPanel = new FileListPanel();
        fileListPanel.initialize();

        ButtonGroup buttonGroup = new ButtonGroup();
        ItemListener itemListener = new RadioButtonItemListener();

        scannedRadioButton = new JRadioButton("Scanned PDF files");
        scannedRadioButton.setSelected(true);
        scannedRadioButton.addItemListener(itemListener);
        buttonGroup.add(scannedRadioButton);

        encryptedRadioButton = new JRadioButton("Encrypted PDF files");
        encryptedRadioButton.addItemListener(itemListener);
        buttonGroup.add(encryptedRadioButton);

        nonOutlineRadioButton = new JRadioButton("PDF files without outline");
        nonOutlineRadioButton.addItemListener(itemListener);
        buttonGroup.add(nonOutlineRadioButton);

        hasAnnotationsRadioButton = new JRadioButton("PDF files with annotations");
        hasAnnotationsRadioButton.addItemListener(itemListener);
        buttonGroup.add(hasAnnotationsRadioButton);

        thresholdSpinner = new JSpinner();
        thresholdSpinner.setModel(new SpinnerNumberModel(1, 0, 100, 1));
        thresholdSpinner.setPreferredSize(new Dimension(80, UiKit.FIELD_HEIGHT));
        thresholdSpinner.setMaximumSize(new Dimension(80, UiKit.FIELD_HEIGHT));

        JPanel checkOptionPanel = new JPanel();
        checkOptionPanel.setLayout(new BoxLayout(checkOptionPanel, BoxLayout.Y_AXIS));
        checkOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkOptionPanel.setBorder(UiKit.sectionBorder("Check Options"));
        checkOptionPanel.add(UiKit.checkRow(scannedRadioButton, encryptedRadioButton, nonOutlineRadioButton, hasAnnotationsRadioButton));
        checkOptionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        checkOptionPanel.add(UiKit.formRow("Threshold", thresholdSpinner));

        isRecursiveSearched = new JCheckBox("Recursive");
        isRecursiveSearched.setSelected(true);

        JPanel searchOptionPanel = new JPanel();
        searchOptionPanel.setLayout(new BoxLayout(searchOptionPanel, BoxLayout.Y_AXIS));
        searchOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchOptionPanel.setBorder(UiKit.sectionBorder("Search Options"));
        searchOptionPanel.add(UiKit.checkRow(isRecursiveSearched));

        searchButton = UiKit.primaryButton("Search");
        cancelButton = UiKit.secondaryButton("Cancel");
        cancelButton.setEnabled(false);
        searchButton.addActionListener(new OperationButtonActionListener());
        cancelButton.addActionListener(new OperationButtonActionListener());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        mainPanel.add(fileListPanel);
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        mainPanel.add(checkOptionPanel);
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        mainPanel.add(searchOptionPanel);
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        mainPanel.add(UiKit.actionRow(cancelButton, searchButton));
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        mainPanel.add(progressBar);
    }

    private void createResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(new EmptyBorder(UiKit.GAP_XS, 0, 0, 0));

        resultTableModel = new PdfFilesTableModel(new Vector<>(), PdfFilesConstants.COLUMN_NAMES);
        resultTable = new JTable(resultTableModel);
        resultTable.setRowHeight(28);
        resultTable.setShowGrid(false);
        resultTable.setIntercellSpacing(new Dimension(0, 0));

        resultTable.setDefaultRenderer(Vector.class, new PdfFilesTableCellRenderer());

        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            resultTable.getColumn(resultTable.getColumnName(i)).setCellRenderer(new PdfFilesTableCellRenderer());
        }

        resultTable.addMouseListener(new MyMouseListener());

        resultTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void processFile(File file) {
        if (scannedRadioButton.isSelected()) {
            int threshold = (Integer) thresholdSpinner.getValue();
            if (PdfUtils.isScannedPdf(file, threshold)) {
                resultFileList.add(file);
            }
        } else if (encryptedRadioButton.isSelected()) {
            if (PdfUtils.isEncryptedPdf(file)) {
                resultFileList.add(file);
            }
        } else if (nonOutlineRadioButton.isSelected()) {
            if (PdfUtils.isNonOutlinePdf(file)) {
                resultFileList.add(file);
            }
        } else if (hasAnnotationsRadioButton.isSelected()) {
            if (PdfUtils.hasAnnotations(file)) {
                resultFileList.add(file);
            }
        } else {
            logger.error("Invalid option selected");
        }
    }

    class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            int r = resultTable.rowAtPoint(e.getPoint());
            if (r >= 0 && r < resultTable.getRowCount()) {
                if (!resultTable.isRowSelected(r)) {
                    resultTable.setRowSelectionInterval(r, r);
                }
            } else {
                resultTable.clearSelection();
            }
            int[] rowsIndex = resultTable.getSelectedRows();
            if (rowsIndex == null || rowsIndex.length == 0) {
                return;
            }
            if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                JPopupMenu popupmenu = new JPopupMenu();
                MyMenuActionListener menuActionListener = new MyMenuActionListener();

                if (rowsIndex.length == 1) {
                    openDirMenuItem = new JMenuItem("Open parent folder of this file");
                    openDirMenuItem.addActionListener(menuActionListener);
                    popupmenu.add(openDirMenuItem);
                    popupmenu.addSeparator();
                }

                copyFilesMenuItem = new JMenuItem("Copy selected files to...");
                copyFilesMenuItem.addActionListener(menuActionListener);
                popupmenu.add(copyFilesMenuItem);

                JMenuItem exportMenuItem = new JMenuItem("导出到 Excel");
                exportMenuItem.addActionListener(ev ->
                    ExcelExporter.export(resultTableModel, "pdf_finder_export.xlsx", PdfFinderPanel.this));
                popupmenu.add(exportMenuItem);

                popupmenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class MyMenuActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            Object source = actionEvent.getSource();
            if (source.equals(openDirMenuItem)) {
                onOpenDir();
            } else if (source.equals(copyFilesMenuItem)) {
                onCopyFiles();
            } else {
                logger.error("invalid source");
            }
        }

        private void onOpenDir() {
            int rowIndex = resultTable.getSelectedRow();
            String parentPath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(PdfFilesConstants.COLUMN_NAME_FILE_PARENT).getModelIndex()).toString();
            File parent = new File(parentPath);
            RevealFileUtils.revealDirectory(parent);
        }

        private void onCopyFiles() {
            int[] selectedRows = resultTable.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(PdfFinderPanel.this, "No rows selected", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<File> filesToCopy = new ArrayList<>();
            for (int rowIndex : selectedRows) {
                String filePath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(PdfFilesConstants.COLUMN_NAME_FILE_NAME).getModelIndex()).toString();
                String parentPath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(PdfFilesConstants.COLUMN_NAME_FILE_PARENT).getModelIndex()).toString();
                File file = new File(parentPath, filePath);
                filesToCopy.add(file);
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Target Directory");
            int returnValue = fileChooser.showOpenDialog(PdfFinderPanel.this);
            if (returnValue != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File targetDir = fileChooser.getSelectedFile();
            for (File file : filesToCopy) {
                try {
                    org.apache.commons.io.FileUtils.copyFileToDirectory(file, targetDir);
                } catch (Exception e) {
                    logger.error("Copy file failed: " + file.getAbsolutePath(), e);
                }
            }
        }


    }

    class OperationButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source.equals(searchButton)) {
                searchButton.setEnabled(false);
                cancelButton.setEnabled(true);
                searchThread = new SearchThread(isRecursiveSearched.isSelected());
                searchThread.start();
            } else if (source.equals(cancelButton)) {
                searchButton.setEnabled(true);
                cancelButton.setEnabled(false);
                if (searchThread.isAlive()) {
                    searchThread.interrupt();
                    searchThread.executorService.shutdownNow();
                }
            }

        }
    }

    private void showResult() {
        SwingUtilities.invokeLater(() -> {
            int index = 0;
            for (File file : resultFileList) {
                index++;
                Vector<Object> rowData = getRowVector(index, file);
                resultTableModel.addRow(rowData);
            }
            tabbedPane.setSelectedIndex(1);
        });
    }

    private Vector<Object> getRowVector(int index, File file) {
        Vector<Object> rowData = new Vector<>();
        rowData.add(index);
        rowData.add(file.getParent());
        rowData.add(file.getName());
        rowData.add(FileUtils.sizeOfInHumanFormat(file));
        rowData.add(DateUtils.millisecondToHumanFormat(file.lastModified()));
        return rowData;
    }

    class SearchThread extends Thread {
        public final ExecutorService executorService;
        private final AtomicInteger processedFiles = new AtomicInteger(0);
        private int totalFiles = 0;
        private final boolean isRecursiveSearched;

        public SearchThread(boolean isRecursiveSearched) {
            super();
            this.isRecursiveSearched = isRecursiveSearched;
            this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(0);
                progressBar.setString("Starting search...");
            });
        }

        @Override
        public void run() {
            try {
                resultFileList.clear();
                SwingUtilities.invokeLater(() -> resultTableModel.setRowCount(0));

                List<File> fileList = fileListPanel.getFileList();
                Set<File> fileSet = new TreeSet<>();
                String[] extensions = new String[]{"pdf", "PDF"};
                for (File file : fileList) {
                    fileSet.addAll(FileUtils.listFiles(file, extensions, isRecursiveSearched));
                }

                List<Future<?>> futures = new ArrayList<>();
                totalFiles = fileSet.size();
                updateProgress();

                for (File file : fileSet) {
                    if (currentThread().isInterrupted()) {
                        return;
                    }
                    futures.add(executorService.submit(() -> {
                        if (currentThread().isInterrupted()) {
                            return null;
                        }
                        processFile(file);
                        incrementProcessedFiles();
                        return null;
                    }));
                }

                // Wait for all tasks to complete
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (InterruptedException e) {
                        logger.error("Search interrupted", e);
                        currentThread().interrupt(); // Restore interrupted status
                        return;
                    }
                }

                showResult();
            } catch (Exception e) {
                logger.error("Search failed", e);
                SwingUtilities.invokeLater(() -> progressBar.setString("Search failed"));
            } finally {
                executorService.shutdown();
                SwingUtilities.invokeLater(() -> {
                    searchButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                });
            }
        }

        private void incrementProcessedFiles() {
            processedFiles.incrementAndGet();
            updateProgress();
        }

        private void updateProgress() {
            if (totalFiles > 0) {
                SwingUtilities.invokeLater(() -> {
                    int processed = processedFiles.get();
                    int percentage = (int) (processed * 100.0 / totalFiles);
                    progressBar.setValue(percentage);
                    progressBar.setString(String.format("Processing: %d/%d files (%d%%)", processed, totalFiles, percentage));
                });
            }
        }
    }

    class RadioButtonItemListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            thresholdSpinner.setEnabled(scannedRadioButton.isSelected());
        }
    }
}
