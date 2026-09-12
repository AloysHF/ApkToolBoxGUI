package edu.jiangxin.apktoolbox.swing.extend;

import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class FileListPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LogManager.getLogger(FileListPanel.class.getSimpleName());

    private JPanel leftPanel;

    private JPanel rightPanel;

    private JList<File> fileList;

    private DefaultListModel<File> fileListModel;

    public FileListPanel() {
        super();
    }

    // in case of escape of "this"
    public void initialize() {
        initUI();
    }

    public List<File> getFileList() {
        List<File> fileList = new ArrayList<>();
        Object[] objectArray = fileListModel.toArray();
        for (Object obj : objectArray) {
            if (obj instanceof File) {
                fileList.add((File) obj);
            }
        }
        return fileList;
    }

    private void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        createLeftPanel();
        add(leftPanel);

        add(Box.createHorizontalStrut(UiKit.GAP_MD));

        createRightPanel();
        add(rightPanel);
    }

    private void createLeftPanel() {
        fileList = new JList<>();
        fileListModel = new DefaultListModel<>();
        fileList.setModel(fileListModel);
        fileList.setFixedCellHeight(26);

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(Constants.DEFAULT_SCROLL_PANEL_WIDTH, Constants.DEFAULT_SCROLL_PANEL_HEIGHT));

        leftPanel = new JPanel();
        leftPanel.setBorder(UiKit.sectionBorder("File List"));
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setTransferHandler(new FileListTransferHandler());
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void createRightPanel() {
        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rightContentPanel = new JPanel();
        rightContentPanel.setLayout(new GridLayout(6, 1, 0, UiKit.GAP_SM));
        rightContentPanel.setBorder(new EmptyBorder(0, UiKit.GAP_SM, 0, 0));
        rightContentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightContentPanel.add(createSideButton("Add File", new AddFileButtonActionListener()));
        rightContentPanel.add(createSideButton("Add Directory", new AddDirectoryButtonActionListener()));
        rightContentPanel.add(createSideButton("Remove Selected", new RemoveSelectedButtonActionListener()));
        rightContentPanel.add(createSideButton("Clear All", new ClearButtonActionListener()));
        rightContentPanel.add(createSideButton("Select All", new SelectAllButtonActionListener()));
        rightContentPanel.add(createSideButton("Inverse Selected", new InverseSelectedButtonActionListener()));

        rightPanel.add(Box.createVerticalGlue(), BorderLayout.NORTH);
        rightPanel.add(rightContentPanel, BorderLayout.CENTER);
        rightPanel.add(Box.createVerticalGlue(), BorderLayout.SOUTH);
        rightPanel.setPreferredSize(new Dimension(UiKit.BUTTON_WIDTH + UiKit.GAP_SM * 2, Constants.DEFAULT_SCROLL_PANEL_HEIGHT));
        rightPanel.setMaximumSize(rightPanel.getPreferredSize());
    }

    private JButton createSideButton(String text, ActionListener listener) {
        JButton button = UiKit.secondaryButton(text);
        button.addActionListener(listener);
        return button;
    }

    private final class FileListTransferHandler extends TransferHandler {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                @SuppressWarnings("unchecked")
                List<File> files = (List<File>) (t.getTransferData(DataFlavor.javaFileListFlavor));
                for (File file : files) {
                    fileListModel.addElement(file);
                }
                return true;
            } catch (IOException e) {
                LOGGER.error("importData failed: IOException");
            } catch (UnsupportedFlavorException e) {
                LOGGER.error("importData failed: UnsupportedFlavorException");
            }
            return false;
        }

        @Override
        public boolean canImport(JComponent jComponent, DataFlavor[] dataFlavors) {
            for (DataFlavor dataFlavor : dataFlavors) {
                if (DataFlavor.javaFileListFlavor.equals(dataFlavor)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final class AddFileButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setDialogTitle("Select A File");
            int ret = jfc.showDialog(new JLabel(), null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                fileListModel.addElement(file);
            }
        }
    }

    private final class AddDirectoryButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setDialogTitle("Select A Directory");
            int ret = jfc.showDialog(new JLabel(), null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                fileListModel.addElement(file);
            }

        }
    }

    private final class RemoveSelectedButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<File> files = fileList.getSelectedValuesList();
            for (File file : files) {
                fileListModel.removeElement(file);
            }
        }
    }

    private final class ClearButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            fileListModel.removeAllElements();
        }
    }

    private final class SelectAllButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectedIndices = new int[fileListModel.getSize()];
            for (int i = 0; i < selectedIndices.length; i++) {
                selectedIndices[i] = i;
            }
            fileList.clearSelection();
            fileList.setSelectedIndices(selectedIndices);
        }
    }

    private final class InverseSelectedButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] newIndices = new int[fileListModel.getSize() - fileList.getSelectedIndices().length];
            for (int i = 0, j = 0; i < fileListModel.getSize(); ++i) {
                if (!fileList.isSelectedIndex(i)) {
                    newIndices[j++] = i;
                }
            }
            fileList.clearSelection();
            fileList.setSelectedIndices(newIndices);
        }
    }

}
