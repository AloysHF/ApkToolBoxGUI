package edu.jiangxin.apktoolbox.word.stat;

import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.FileListPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.DateUtils;
import edu.jiangxin.apktoolbox.utils.ExcelExporter;
import edu.jiangxin.apktoolbox.utils.FileUtils;
import edu.jiangxin.apktoolbox.word.WordUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serial;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WordStatPanel extends EasyPanel {

    @Serial
    private static final long serialVersionUID = 1L;

    private JTabbedPane tabbedPane;

    private JPanel mainPanel;

    private FileListPanel fileListPanel;

    private JCheckBox isRecursiveSearched;

    private JPanel resultPanel;

    private JTable resultTable;

    private DefaultTableModel resultTableModel;

    private JLabel statInfoLabel;

    private JButton statButton;
    private JButton cancelButton;

    private JProgressBar progressBar;

    private transient SearchThread searchThread;

    private final AtomicLong totalFileSize = new AtomicLong(0);

    private final AtomicInteger totalPageCount = new AtomicInteger(0);

    private transient final List<Vector<Object>> resultFileList = new CopyOnWriteArrayList<>();

    @Override
    public void initUI() {
        tabbedPane = new JTabbedPane();
        add(tabbedPane);

        createMainPanel();
        tabbedPane.addTab("Options", null, mainPanel, "Show Stat Options");

        createResultPanel();
        tabbedPane.addTab("Result", null, resultPanel, "Show Stat Result");
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileListPanel = new FileListPanel();
        fileListPanel.initialize();

        isRecursiveSearched = new JCheckBox("Recursive");
        isRecursiveSearched.setSelected(true);

        JPanel searchOptionPanel = new JPanel();
        searchOptionPanel.setLayout(new BoxLayout(searchOptionPanel, BoxLayout.Y_AXIS));
        searchOptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchOptionPanel.setBorder(UiKit.sectionBorder("Stat Options"));
        searchOptionPanel.add(UiKit.checkRow(isRecursiveSearched));

        statButton = UiKit.primaryButton("Stat");
        cancelButton = UiKit.secondaryButton("Cancel");
        cancelButton.setEnabled(false);
        statButton.addActionListener(new OperationButtonActionListener());
        cancelButton.addActionListener(new OperationButtonActionListener());

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        mainPanel.add(fileListPanel);
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        mainPanel.add(searchOptionPanel);
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_MD));
        mainPanel.add(UiKit.actionRow(cancelButton, statButton));
        mainPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        mainPanel.add(progressBar);
    }

    private void createResultPanel() {
        resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBorder(new EmptyBorder(UiKit.GAP_XS, 0, 0, 0));

        resultTableModel = new WordFilesTableModel(new Vector<>(), WordFilesConstants.COLUMN_NAMES);
        resultTable = new JTable(resultTableModel);
        resultTable.setRowHeight(28);
        resultTable.setShowGrid(false);
        resultTable.setIntercellSpacing(new Dimension(0, 0));
        resultTable.setDefaultRenderer(Vector.class, new WordFilesTableCellRenderer());
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            resultTable.getColumn(resultTable.getColumnName(i)).setCellRenderer(new WordFilesTableCellRenderer());
        }
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        resultTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    JPopupMenu popupmenu = new JPopupMenu();
                    JMenuItem exportMenuItem = new JMenuItem("Export to Excel");
                    exportMenuItem.addActionListener(ev ->
                        ExcelExporter.export(resultTableModel, "word_stat_export.xlsx", WordStatPanel.this));
                    popupmenu.add(exportMenuItem);
                    popupmenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane(resultTable);

        JPanel statInfoPanel = new JPanel();
        statInfoPanel.setLayout(new BoxLayout(statInfoPanel, BoxLayout.X_AXIS));
        statInfoLabel = new JLabel("");
        statInfoPanel.add(statInfoLabel);
        statInfoPanel.add(Box.createHorizontalGlue());

        resultPanel.add(scrollPane, BorderLayout.CENTER);
        resultPanel.add(statInfoPanel, BorderLayout.SOUTH);
    }

    private void processFile(File file) {
        Vector<Object> fileVector = getRowVector(file);
        resultFileList.add(fileVector);
    }

    class OperationButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source.equals(statButton)) {
                if (fileListPanel.getFileList().isEmpty()) {
                    return;
                }
                statButton.setEnabled(false);
                cancelButton.setEnabled(true);
                searchThread = new SearchThread(isRecursiveSearched.isSelected());
                searchThread.start();
            } else if (source.equals(cancelButton)) {
                statButton.setEnabled(true);
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
            for (Vector<Object> file : resultFileList) {
                file.add(0, ++index);
                resultTableModel.addRow(file);
            }
            tabbedPane.setSelectedIndex(1);
            statInfoLabel.setText("Page Count: " + totalPageCount.get() + ", Total Size: " + FileUtils.sizeOfInHumanFormat(totalFileSize.get()));
        });
    }

    private Vector<Object> getRowVector(File file) {
        Vector<Object> rowData = new Vector<>();
        rowData.add(file.getParent());
        rowData.add(file.getName());
        totalFileSize.addAndGet(file.length());
        rowData.add(FileUtils.sizeOfInHumanFormat(file));
        rowData.add(DateUtils.millisecondToHumanFormat(file.lastModified()));
        int pageCount = WordUtils.getPageCount(file);
        totalPageCount.addAndGet(pageCount);
        rowData.add(pageCount);
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

            resultFileList.clear();
            totalFileSize.set(0);
            totalPageCount.set(0);

            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(0);
                progressBar.setString("Starting search...");
                resultTableModel.setRowCount(0);
                statInfoLabel.setText("");
            });
        }

        @Override
        public void run() {
            try {
                List<File> fileList = fileListPanel.getFileList();
                Set<File> fileSet = new TreeSet<>();
                String[] extensions = new String[]{"doc", "DOC", "docx", "DOCX"};
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
                        currentThread().interrupt();
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
                    statButton.setEnabled(true);
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
}
