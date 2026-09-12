package edu.jiangxin.apktoolbox.android.screenshot;

import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.DateUtils;
import edu.jiangxin.apktoolbox.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class ScreenShotPanel extends EasyPanel {
    private static final long serialVersionUID = 1L;

    private JTextField directoryTextField;

    private JTextField fileNameTextField;

    private JCheckBox openCheckBox;

    private JCheckBox copyCheckBox;

    public ScreenShotPanel() throws HeadlessException {
        super();
    }

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Screenshot"));

        createDirectoryPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createFileNamePanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createScreenshotPanel();
    }

    private void createScreenshotPanel() {
        openCheckBox = new JCheckBox("Open Directory");
        openCheckBox.setSelected(false);

        copyCheckBox = new JCheckBox("Copy to Clipboard");
        copyCheckBox.setSelected(false);

        JButton screenshotButton = UiKit.primaryButton("Screenshot");
        screenshotButton.addActionListener(new ScreenshotButtonActionListener());

        JButton getExistButton = UiKit.secondaryButton("Get Existing");
        getExistButton.addActionListener(new GetExistButtonActionListener());

        add(UiKit.checkRow(openCheckBox, copyCheckBox));
        add(Box.createVerticalStrut(UiKit.GAP_SM));
        add(UiKit.actionRow(getExistButton, screenshotButton));
    }

    private void createFileNamePanel() {
        fileNameTextField = UiKit.field(new JTextField());
        fileNameTextField.setToolTipText("timestamp default(for example: 20180101122345.png)");

        JButton fileNameButton = UiKit.secondaryButton("File Name");
        fileNameButton.addActionListener(e -> fileNameTextField.requestFocusInWindow());

        JPanel fileNamePanel = new JPanel(new BorderLayout(UiKit.GAP_MD, 0));
        fileNamePanel.setOpaque(false);
        fileNamePanel.add(UiKit.formRow("File Name", fileNameTextField), BorderLayout.CENTER);
        fileNamePanel.add(fileNameButton, BorderLayout.EAST);
        add(fileNamePanel);
    }

    private void createDirectoryPanel() {
        directoryTextField = UiKit.field(new JTextField());
        directoryTextField.setText(conf.getString("screenshot.save.dir", System.getenv("USERPROFILE")));
        directoryTextField.setTransferHandler(new DirectoryTextFieldTransferHandler());

        JButton directoryButton = UiKit.secondaryButton("Browse...");
        directoryButton.addActionListener(new DirectoryButtonActionListener());

        add(UiKit.formRow("Save Directory", directoryTextField, directoryButton));
    }

    private final class DirectoryButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser(directoryTextField.getText());
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            jfc.setDialogTitle("select a directory");
            int ret = jfc.showDialog(new JLabel(), null);
            switch (ret) {
                case JFileChooser.APPROVE_OPTION:
                    File file = jfc.getSelectedFile();
                    directoryTextField.setText(file.getAbsolutePath());
                    conf.setProperty("screenshot.save.dir", file.getAbsolutePath());
                    break;

                default:
                    break;
            }

        }
    }

    private final class GetExistButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Get Exist");
        }
    }

    private final class ScreenshotButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = Utils.getFrameTitle(ScreenShotPanel.this);
            Utils.setFrameTitle(ScreenShotPanel.this, title + "    [Processing]");
            String dirName = fileNameTextField.getText();
            if (StringUtils.isEmpty(dirName)) {
                String defaultDir = System.getenv("USERPROFILE");
                dirName = conf.getString("screenshot.save.dir", defaultDir);
                logger.info("dirName: " + dirName);
            }
            String fileName = fileNameTextField.getText();
            if (StringUtils.isEmpty(fileName)) {
                fileName = DateUtils.getCurrentDateString() + ".png";
            }
            File file = new File(dirName, fileName);
            try {
                Utils.executor("adb shell /system/bin/screencap -p /sdcard/screenshot.png", true);
                logger.info("screencap finish");
                Utils.executor("adb pull /sdcard/screenshot.png " + file.getCanonicalPath(), true);
                logger.info("pull finish");
                if (openCheckBox.isSelected()) {
                    Utils.executor("explorer /e,/select, " + file.getCanonicalPath(), true);
                    logger.info("open dir finish");
                }
                if (copyCheckBox.isSelected()) {
                    logger.info("copy the snapshot");
                    Image image = ImageIO.read(file);
                    setClipboardImage(ScreenShotPanel.this, image);
                    logger.info("copy finish");
                }
            } catch (IOException e1) {
                logger.error("screenshot fail", e1);
            } finally {
                Utils.setFrameTitle(ScreenShotPanel.this, title);
            }
        }
    }

    private final class DirectoryTextFieldTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                Object o = t.getTransferData(DataFlavor.javaFileListFlavor);
                String filepath = o.toString();
                filepath = filepath.substring(1, filepath.length() - 1);
                directoryTextField.setText(filepath);
                return true;
            } catch (Exception e) {
                logger.error("import data excetion", e);
            }
            return false;
        }

        @Override
        public boolean canImport(JComponent jComponent, DataFlavor[] dataFlavors) {
            for (int i = 0; i < dataFlavors.length; i++) {
                if (DataFlavor.javaFileListFlavor.equals(dataFlavors[i])) {
                    return true;
                }
            }
            return false;
        }
    }

    private static void setClipboardImage(JPanel frame, final Image image) {
        Transferable trans = new Transferable() {
            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                if (isDataFlavorSupported(flavor)) {
                    return image;
                }
                throw new UnsupportedFlavorException(flavor);
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[] { DataFlavor.imageFlavor };
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }
        };

        frame.getToolkit().getSystemClipboard().setContents(trans, null);
    }
}
