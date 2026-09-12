package edu.jiangxin.apktoolbox.swing.extend;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;

import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.jiangxin.apktoolbox.utils.Utils;

/**
 * @author jiangxin
 * @author 2018-09-09
 *
 */
public class EasyFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    protected transient Logger logger;
    protected transient Configuration conf;
    protected transient ResourceBundle bundle;
    protected transient Image image;

    public EasyFrame() throws HeadlessException {
        super();
        logger = LogManager.getLogger(this.getClass().getSimpleName());
        conf = Utils.getConfiguration();
        bundle = ResourceBundle.getBundle("apktoolbox");
    }

    // in case of escape of "this"
    public void initialize() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                onWindowClosing(e);
                Utils.saveConfiguration();
                logger.info("windowClosing: " + EasyFrame.this.getClass().getSimpleName());
            }
            @Override
            public void windowIconified(WindowEvent e) {
                super.windowIconified(e);
                onWindowIconified(e);
                setVisible(false);
                dispose();
                Utils.saveConfiguration();
                logger.info("windowIconified: " + EasyFrame.this.getClass().getSimpleName());
            }
        });
        Toolkit tk = Toolkit.getDefaultToolkit();
        image = tk.createImage(this.getClass().getResource("/icon.jpg"));
        setIconImage(image);
        logger.info("Frame start: " + this.getClass().getSimpleName());
    }

    public void refreshSizeAndLocation() {
        // use pack to resize the child component
        pack();
        setMinimumSize(new Dimension(880, 120));
        setResizable(false);

        // relocation this JFrame
        int windowWidth = getWidth();
        int windowHeight = getHeight();
        Toolkit kit = Toolkit.getDefaultToolkit();
        if (kit == null) {
            logger.error("kit is null");
            return;
        }
        Dimension screenSize = kit.getScreenSize();
        if (screenSize == null) {
            logger.error("screenSize is null");
            return;
        }
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
    }

    protected void onWindowClosing(WindowEvent e) {}

    protected void onWindowIconified(WindowEvent e) {}
}
