package edu.jiangxin.apktoolbox.file.duplicate;

import edu.jiangxin.apktoolbox.utils.DateUtils;
import edu.jiangxin.apktoolbox.swing.extend.FileListPanel;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import edu.jiangxin.apktoolbox.utils.ExcelExporter;
import edu.jiangxin.apktoolbox.utils.FileUtils;
import edu.jiangxin.apktoolbox.utils.RevealFileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class DuplicateSearchPanel extends EasyPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private JTabbedPane tabbedPane;

    private JPanel optionPanel;

    private FileListPanel fileListPanel;

    private JCheckBox isFileNameChecked;
    private JCheckBox isMD5Checked;
    private JCheckBox isModifiedTimeChecked;

    private JCheckBox isHiddenFileSearched;
    private JCheckBox isRecursiveSearched;
    private JTextField suffixTextField;

    private JPanel resultPanel;

    private JTable resultTable;

    private DefaultTableModel resultTableModel;

    private JButton searchButton;
    private JButton cancelButton;

    private JProgressBar progressBar;

    private JMenuItem openDirMenuItem;
    private JMenuItem deleteFileMenuItem;
    private JMenuItem deleteFilesInSameDirMenuItem;
    private JMenuItem deleteFilesInSameDirRecursiveMenuItem;

    private transient SearchThread searchThread;

    private transient final Map<String, List<File>> duplicateFileGroupMap = new HashMap<>();

    @Override
    public void initUI() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        createOptionPanel();
        tabbedPane.addTab("Options", null, optionPanel, "Show Search Options");

        createResultPanel();
        tabbedPane.addTab("Result", null, resultPanel, "Show Search Result");
    }

    private void createOptionPanel() {
        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileListPanel = new FileListPanel();
        fileListPanel.initialize();

        JCheckBox isSizeChecked = new JCheckBox("Size");
        isSizeChecked.setSelected(true);
        isSizeChecked.setEnabled(false);
        isFileNameChecked = new JCheckBox("Filename");
        isMD5Checked = new JCheckBox("MD5");
        isModifiedTimeChecked = new JCheckBox("Last Modified Time");

        JPanel checkOptionPanel = new JPanel();
        checkOptionPanel.setLayout(new BoxLayout(checkOptionPanel, BoxLayout.Y_AXIS));
        checkOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkOptionPanel.setBorder(UiKit.sectionBorder("Check Options"));
        checkOptionPanel.add(UiKit.checkRow(isSizeChecked, isFileNameChecked, isMD5Checked, isModifiedTimeChecked));

        isHiddenFileSearched = new JCheckBox("Hidden Files");
        isRecursiveSearched = new JCheckBox("Recursive");
        isRecursiveSearched.setSelected(true);
        suffixTextField = UiKit.field(new JTextField());
        suffixTextField.setToolTipText("an array of extensions, ex. {\"java\",\"xml\"}. If this parameter is empty, all files are returned.");

        JPanel searchOptionPanel = new JPanel();
        searchOptionPanel.setLayout(new BoxLayout(searchOptionPanel, BoxLayout.Y_AXIS));
        searchOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchOptionPanel.setBorder(UiKit.sectionBorder("Search Options"));
        searchOptionPanel.add(UiKit.checkRow(isHiddenFileSearched, isRecursiveSearched));
        searchOptionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        searchOptionPanel.add(UiKit.formRow("Suffix", suffixTextField));

        searchButton = UiKit.primaryButton("Search");
        cancelButton = UiKit.secondaryButton("Cancel");
        cancelButton.setEnabled(false);
        searchButton.addActionListener(new OperationButtonActionListener());
        cancelButton.addActionListener(new OperationButtonActionListener());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setPreferredSize(new Dimension(Constants.DEFAULT_SCROLL_PANEL_WIDTH, 22));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        optionPanel.add(fileListPanel);
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        optionPanel.add(checkOptionPanel);
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        optionPanel.add(searchOptionPanel);
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        optionPanel.add(UiKit.actionRow(cancelButton, searchButton));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(progressBar);
    }

    private void createResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(new EmptyBorder(UiKit.GAP_XS, 0, 0, 0));

        resultTableModel = new DuplicateFilesTableModel(new Vector<>(), DuplicateFilesConstants.COLUMN_NAMES);
        resultTable = new JTable(resultTableModel);
        resultTable.setRowHeight(28);
        resultTable.setShowGrid(false);
        resultTable.setIntercellSpacing(new Dimension(0, 0));

        resultTable.setDefaultRenderer(Vector.class, new DuplicateFilesTableCellRenderer());

        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            resultTable.getColumn(resultTable.getColumnName(i)).setCellRenderer(new DuplicateFilesTableCellRenderer());
        }

        resultTable.addMouseListener(new MyMouseListener());

        resultTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private String getComparedKey(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Size][");
        sb.append(DigestUtils.md5Hex(String.valueOf(file.length())));
        sb.append("]");
        
        if (isFileNameChecked.isSelected()) {
            sb.append("[Filename][");
            sb.append(DigestUtils.md5Hex(file.getName()));
            sb.append("]");
        }
        if (isMD5Checked.isSelected()) {
            sb.append("[MD5][");
            try (InputStream is = new FileInputStream(file)) {
                sb.append(DigestUtils.md5Hex(is));
            } catch (FileNotFoundException e) {
                logger.error("getComparedKey FileNotFoundException");
            } catch (IOException e) {
                logger.error("getComparedKey IOException");
            }
            sb.append("]");
        }
        if (isModifiedTimeChecked.isSelected()) {
            sb.append("[ModifiedTime][");
            sb.append(DigestUtils.md5Hex(String.valueOf(file.lastModified())));
            sb.append("]");
        }
        logger.info("path: " + file.getAbsolutePath() + ", key: " + sb);
        return sb.toString();
    }

    class MyMouseListener extends MouseAdapter {
        @Override
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            int r = resultTable.rowAtPoint(e.getPoint());
            if (r >= 0 && r < resultTable.getRowCount()) {
                resultTable.setRowSelectionInterval(r, r);
            } else {
                resultTable.clearSelection();
            }
            int rowIndex = resultTable.getSelectedRow();
            if (rowIndex < 0) {
                return;
            }
            if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                JPopupMenu popupmenu = new JPopupMenu();
                MyMenuActionListener menuActionListener = new MyMenuActionListener();

                openDirMenuItem = new JMenuItem("Open parent folder of this file");
                openDirMenuItem.addActionListener(menuActionListener);
                popupmenu.add(openDirMenuItem);

                deleteFileMenuItem = new JMenuItem("Delete this duplicate file");
                deleteFileMenuItem.addActionListener(menuActionListener);
                popupmenu.add(deleteFileMenuItem);

                deleteFilesInSameDirMenuItem = new JMenuItem("Delete these duplicate files in the same directory");
                deleteFilesInSameDirMenuItem.addActionListener(menuActionListener);
                popupmenu.add(deleteFilesInSameDirMenuItem);

                deleteFilesInSameDirRecursiveMenuItem = new JMenuItem("Delete these duplicate files in the same directory(Recursive)");
                deleteFilesInSameDirRecursiveMenuItem.addActionListener(menuActionListener);
                popupmenu.add(deleteFilesInSameDirRecursiveMenuItem);

                JMenuItem exportMenuItem = new JMenuItem("导出到 Excel");
                exportMenuItem.addActionListener(ev ->
                    ExcelExporter.export(resultTableModel, "duplicate_files_export.xlsx", DuplicateSearchPanel.this));
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
            } else if (source.equals(deleteFileMenuItem)) {
                onDeleteFile();
            } else if (source.equals(deleteFilesInSameDirMenuItem)) {
                onDeleteFilesInSameDir();
            } else if (source.equals(deleteFilesInSameDirRecursiveMenuItem)) {
                onDeleteFilesInSameDirRecursive();
            } else {
                logger.error("invalid source");
            }
        }

        private void onOpenDir() {
            int rowIndex = resultTable.getSelectedRow();
            String parentPath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(DuplicateFilesConstants.COLUMN_NAME_FILE_PARENT).getModelIndex()).toString();
            File parent = new File(parentPath);
            RevealFileUtils.revealDirectory(parent);
        }

        private void onDeleteFile() {
            int rowIndex = resultTable.getSelectedRow();
            String parentPath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(DuplicateFilesConstants.COLUMN_NAME_FILE_PARENT).getModelIndex()).toString();
            String name = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(DuplicateFilesConstants.COLUMN_NAME_FILE_NAME).getModelIndex()).toString();
            File selectedFile = new File(parentPath, name);
            String key = getComparedKey(selectedFile);
            List<File> files = duplicateFileGroupMap.get(key);
            for (File file : files) {
                if (selectedFile.equals(file)) {
                    files.remove(file);
                    boolean isSuccessful = file.delete();
                    logger.info("delete file: " + file.getAbsolutePath() + ", result: " + isSuccessful);
                    break;
                }
            }
            resultTableModel.setRowCount(0);
            showResult();
        }

        private void onDeleteFilesInSameDir() {
            int rowIndex = resultTable.getSelectedRow();
            String parentPath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(DuplicateFilesConstants.COLUMN_NAME_FILE_PARENT).getModelIndex()).toString();
            for (Map.Entry<String, List<File>> entry : duplicateFileGroupMap.entrySet()) {
                List<File> duplicateFileGroup = entry.getValue();
                for (File duplicateFile : duplicateFileGroup) {
                    String parentPathTmp = duplicateFile.getParent();
                    if (Objects.equals(parentPath, parentPathTmp)) {
                        duplicateFileGroup.remove(duplicateFile);
                        boolean isSuccessful = duplicateFile.delete();
                        logger.info("delete file: " + duplicateFile.getAbsolutePath() + ", result: " + isSuccessful);
                        break;
                    }
                }
            }
            resultTableModel.setRowCount(0);
            showResult();
        }

        private void onDeleteFilesInSameDirRecursive() {
            int rowIndex = resultTable.getSelectedRow();
            String parentPath = resultTableModel.getValueAt(rowIndex, resultTable.getColumn(DuplicateFilesConstants.COLUMN_NAME_FILE_PARENT).getModelIndex()).toString();
            for (Map.Entry<String, List<File>> entry : duplicateFileGroupMap.entrySet()) {
                List<File> duplicateFileGroup = entry.getValue();
                for (File duplicateFile : duplicateFileGroup) {
                    String parentPathTmp = duplicateFile.getParent();
                    if (Objects.equals(parentPath, parentPathTmp) || FilenameUtils.directoryContains(parentPath, parentPathTmp)) {
                        duplicateFileGroup.remove(duplicateFile);
                        boolean isSuccessful = duplicateFile.delete();
                        logger.info("delete file: " + duplicateFile.getAbsolutePath() + ", result: " + isSuccessful);
                        break;
                    }
                }
            }
            resultTableModel.setRowCount(0);
            showResult();
        }
    }

    class OperationButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source.equals(searchButton)) {
                searchButton.setEnabled(false);
                cancelButton.setEnabled(true);
                String[] extensions = null;
                if (StringUtils.isNotEmpty(suffixTextField.getText())) {
                    extensions = suffixTextField.getText().split(",");
                }
                searchThread = new SearchThread(extensions, isRecursiveSearched.isSelected(), isHiddenFileSearched.isSelected(), duplicateFileGroupMap);
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
            int groupIndex = 0;
            for (Map.Entry<String, List<File>> entry : duplicateFileGroupMap.entrySet()) {
                List<File> duplicateFileGroup = entry.getValue();
                if (duplicateFileGroup.size() < 2) {
                    continue;
                }
                groupIndex++;
                for (File duplicateFile : duplicateFileGroup) {
                    Vector<Object> rowData = getRowVector(groupIndex, duplicateFile);
                    resultTableModel.addRow(rowData);
                }
            }
            tabbedPane.setSelectedIndex(1);
        });
    }

    private Vector<Object> getRowVector(int groupIndex, File file) {
        Vector<Object> rowData = new Vector<>();
        rowData.add(groupIndex);
        rowData.add(file.getParent());
        rowData.add(file.getName());
        rowData.add(FilenameUtils.getExtension(file.getName()));
        rowData.add(FileUtils.sizeOfInHumanFormat(file));
        rowData.add(DateUtils.millisecondToHumanFormat(file.lastModified()));
        return rowData;
    }

    class SearchThread extends Thread {
        private final ExecutorService executorService;
        private final AtomicInteger processedFiles = new AtomicInteger(0);
        private int totalFiles = 0;
        private final String[] extensions;
        private final boolean isRecursiveSearched;
        private final boolean isHiddenFileSearched;
        private final Map<String, List<File>> duplicateFileGroupMap;

        public SearchThread(String[] extensions, boolean isRecursiveSearched, boolean isHiddenFileSearched, Map<String, List<File>> duplicateFileGroupMap) {
            super();
            this.extensions = extensions;
            this.isRecursiveSearched = isRecursiveSearched;
            this.isHiddenFileSearched = isHiddenFileSearched;
            this.duplicateFileGroupMap = duplicateFileGroupMap;
            this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(0);
                progressBar.setString("Starting search...");
            });
        }

        @Override
        public void run() {
            try {
                duplicateFileGroupMap.clear();
                SwingUtilities.invokeLater(() -> resultTableModel.setRowCount(0));

                List<File> fileList = fileListPanel.getFileList();
                Set<File> fileSet = new TreeSet<>(fileList);
                for (File file : fileList) {
                    fileSet.addAll(org.apache.commons.io.FileUtils.listFiles(file, extensions, isRecursiveSearched));
                }

                // 1. Group files by size first
                Map<Long, List<File>> sizeGroups = new HashMap<>();
                for (File file : fileSet) {
                    if (currentThread().isInterrupted()) {
                        return;
                    }
                    if (file.isHidden() && !isHiddenFileSearched) {
                        continue;
                    }
                    sizeGroups.computeIfAbsent(file.length(), k -> new ArrayList<>()).add(file);
                }

                // 2. Only process groups with duplicate sizes
                List<Future<?>> futures = new ArrayList<>();
                totalFiles = fileSet.size();
                updateProgress();

                for (Map.Entry<Long, List<File>> entry : sizeGroups.entrySet()) {
                    if (entry.getValue().size() > 1) { // Only process groups with duplicates
                        futures.add(executorService.submit(() -> {
                            processFileGroup(entry.getValue());
                            return null;
                        }));
                    } else {
                        // Count single files directly
                        incrementProcessedFiles();
                    }
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

        private void processFileGroup(List<File> files) {
            Map<String, List<File>> groupMap = new HashMap<>();
            for (File file : files) {
                if (currentThread().isInterrupted()) {
                    return;
                }
                String key = getComparedKey(file);
                groupMap.computeIfAbsent(key, k -> new ArrayList<>()).add(file);
                incrementProcessedFiles();
            }

            // Merge results to main map
            synchronized (duplicateFileGroupMap) {
                for (Map.Entry<String, List<File>> entry : groupMap.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        duplicateFileGroupMap.put(entry.getKey(), entry.getValue());
                    }
                }
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
                    progressBar.setString(String.format("Processing: %d/%d files (%d%%)", 
                        processed, totalFiles, percentage));
                });
            }
        }
    }
}
