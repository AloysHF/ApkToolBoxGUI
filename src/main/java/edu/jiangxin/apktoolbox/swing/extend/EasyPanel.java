package edu.jiangxin.apktoolbox.swing.extend;

import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import edu.jiangxin.apktoolbox.utils.FileUtils;
import edu.jiangxin.apktoolbox.utils.Utils;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ResourceBundle;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class EasyPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    protected transient Logger logger;
    protected transient Configuration conf;
    protected transient ResourceBundle bundle;

    protected boolean isInited = false;
    
    public EasyPanel() throws HeadlessException {
        super();
        logger = LogManager.getLogger(this.getClass().getSimpleName());
        conf = Utils.getConfiguration();
        bundle = ResourceBundle.getBundle("apktoolbox");
        // Consistent base padding so every tool panel starts from the same rhythm.
        setBorder(BorderFactory.createEmptyBorder(UiKit.GAP_SM, UiKit.GAP_SM, UiKit.GAP_SM, UiKit.GAP_SM));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        logger.info("Panel start: " + this.getClass().getSimpleName());
    }

    public boolean isNeedPreChangeMenu() {
        return false;
    }

    public void init() {
        if (!isInited) {
            initUI();
            isInited = true;
        }
    }

    public void initUI() {
        // do nothing
    }

    public void afterPainted() {
        // do nothing
    }

    protected EasyFrame getFrame() {
        //Java/Swing: Obtain Window/JFrame from inside a JPanel:
        // https://stackoverflow.com/questions/9650874/java-swing-obtain-window-jframe-from-inside-a-jpanel
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof EasyFrame) {
            return (EasyFrame) window;
        }
        return null;
    }

    protected String checkAndGetFileContent(JTextField textField, String key, String msg) {
        File file = new File(textField.getText());
        if (!file.exists() || !file.isFile()) {
            logger.error(msg);
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, msg, Constants.MESSAGE_DIALOG_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            textField.requestFocus();
            return null;
        }
        String path = FileUtils.getCanonicalPathQuiet(file);
        if (path != null) {
            conf.setProperty(key, path);
        }
        return path;
    }

    protected String checkAndGetNewFileContent(JTextField textField, String key, String msg) {
        File file = new File(textField.getText());
        File parentFile = file.getParentFile();
        if (!parentFile.exists() || !parentFile.isDirectory()) {
            logger.error(msg);
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, msg, Constants.MESSAGE_DIALOG_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            textField.requestFocus();
            return null;
        }
        String path = FileUtils.getCanonicalPathQuiet(file);
        if (path != null) {
            conf.setProperty(key, path);
        }
        return path;
    }

    protected String checkAndGetDirContent(JTextField textField, String key, String msg) {
        File file = new File(textField.getText());
        if (!file.exists() || !file.isDirectory()) {
            logger.error(msg);
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, msg, Constants.MESSAGE_DIALOG_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            textField.requestFocus();
            return null;
        }
        String path = FileUtils.getCanonicalPathQuiet(file);
        if (path != null) {
            conf.setProperty(key, path);
        }
        return path;
    }

    protected String checkAndGetStringContent(JTextField textField, String key, String msg) {
        String content = textField.getText();
        if (content == null || content.isEmpty()) {
            logger.error(msg);
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, msg, Constants.MESSAGE_DIALOG_TITLE,
                    JOptionPane.ERROR_MESSAGE);
            textField.requestFocus();
            return null;
        }
        conf.setProperty(key, content);
        return content;
    }
}
