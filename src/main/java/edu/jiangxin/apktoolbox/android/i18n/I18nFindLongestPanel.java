package edu.jiangxin.apktoolbox.android.i18n;

import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.listener.SelectDirectoryListener;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.SAXBuilderHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class I18nFindLongestPanel extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient List<I18nInfo> infos = new ArrayList<>();

    private JTextField srcTextField;

    private JTextField itemTextField;

    public I18nFindLongestPanel() throws HeadlessException {
        super();
    }

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Internationalization Resources"));

        createSourcePanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createItemPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createOperationPanel();
    }

    private void createOperationPanel() {
        JButton findButton = UiKit.primaryButton(bundle.getString("android.i18n.longest.find"));
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infos.clear();

                String srcPath = checkAndGetDirContent(srcTextField, "android.i18n.longest.src.dir", "Source directory is invalid");
                if (StringUtils.isEmpty(srcPath)) {
                    return;
                }

                String item = checkAndGetStringContent(itemTextField, "android.i18n.longest.items", "Item is empty");
                if (StringUtils.isEmpty(item)) {
                    return;
                }

                sort(srcPath, itemTextField.getText());
                if (CollectionUtils.isEmpty(infos)) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18nFindLongestPanel.this, "Failed, please see the log", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    I18nInfo info = infos.get(0);
                    StringBuilder sb = new StringBuilder();
                    sb.append("length: ").append(info.length).append(System.lineSeparator())
                            .append("text: ").append(info.text).append(System.lineSeparator())
                            .append("path: ").append(info.path);
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(I18nFindLongestPanel.this, sb.toString(), "INFO",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        add(UiKit.actionRow(findButton));
    }

    private void createItemPanel() {
        itemTextField = UiKit.field(new JTextField());
        itemTextField.setText(conf.getString("android.i18n.longest.items"));
        add(UiKit.formRow("Items", itemTextField));
    }

    private void createSourcePanel() {
        srcTextField = UiKit.field(new JTextField());
        srcTextField.setText(conf.getString("android.i18n.longest.src.dir"));

        JButton srcButton = UiKit.secondaryButton("Browse...");
        srcButton.addActionListener(new SelectDirectoryListener("select a directory", srcTextField));

        add(UiKit.formRow("Source Directory", srcTextField, srcButton));
    }

    private String getCanonicalPath(File file) {
        if (file == null) {
            logger.error("file is null");
            return null;
        }
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            logger.error("getCanonicalPath failed: {}", file.getAbsolutePath());
            return null;
        }
    }

    private void sort(String sourceBaseStr, String itemName) {
        File[] sourceParentFiles = new File(sourceBaseStr).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("values");
            }
        });
        if (sourceParentFiles == null) {
            logger.error("None valid directory found");
            return;
        }
        for (File sourceParentFile : sourceParentFiles) {
            File sourceFile = new File(sourceParentFile, "strings.xml");
            if (sourceFile.exists()) {
                SAXBuilder builder = new SAXBuilder();
                SAXBuilderHelper.setSecurityFeatures(builder);
                Document sourceDoc;
                try {
                    sourceDoc = builder.build(sourceFile);
                } catch (JDOMException | IOException e) {
                    logger.error("build failed: {}", sourceFile);
                    continue;
                }
                Element sourceRoot = sourceDoc.getRootElement();
                for (Element child : sourceRoot.getChildren()) {
                    String value = child.getAttributeValue("name");
                    if (value != null && value.equals(itemName)) {
                        String text = child.getText();
                        if (text != null) {
                            I18nInfo info = new I18nInfo(getCanonicalPath(sourceFile), text, text.length());
                            infos.add(info);
                            break;
                        }
                    }
                }

            }

        }
        infos.sort((o1, o2) -> o2.length - o1.length);

        logger.info(infos);
    }

    static class I18nInfo {
        String path;
        String text;
        int length;

        public I18nInfo(String path, String text, int length) {
            this.path = path;
            this.text = text;
            this.length = length;
        }

        @Override
        public String toString() {
            return "I18NInfo [path=" + path + ", text=" + text + ", length=" + length + "]";
        }
    }
}
