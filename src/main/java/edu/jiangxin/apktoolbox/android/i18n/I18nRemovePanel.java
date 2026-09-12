package edu.jiangxin.apktoolbox.android.i18n;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.jiangxin.apktoolbox.swing.extend.listener.SelectDirectoryListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import java.awt.Component;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class I18nRemovePanel extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient List<I18nFindLongestPanel.I18nInfo> infos = new ArrayList<>();

    private static final String CHARSET = "UTF-8";

    private JTextField srcTextField;

    private JTextField itemTextField;

    public I18nRemovePanel() throws HeadlessException {
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
        JButton removeButton = UiKit.primaryButton(bundle.getString("android.i18n.remove.title"));
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                infos.clear();

                String srcPath = checkAndGetDirContent(srcTextField, "android.i18n.remove.src.dir", "Source directory is invalid");
                if (StringUtils.isEmpty(srcPath)) {
                    return;
                }

                String item = checkAndGetStringContent(itemTextField, "android.i18n.remove.items", "Items is empty");
                if (StringUtils.isEmpty(item)) {
                    return;
                }

                remove(srcPath, itemTextField.getText());
            }
        });

        add(UiKit.actionRow(removeButton));
    }

    private void createItemPanel() {
        itemTextField = UiKit.field(new JTextField());
        itemTextField.setText(conf.getString("android.i18n.remove.items"));
        add(UiKit.formRow("Items", itemTextField));
    }

    private void createSourcePanel() {
        srcTextField = UiKit.field(new JTextField());
        srcTextField.setText(conf.getString("android.i18n.remove.src.dir"));

        JButton srcButton = UiKit.secondaryButton("Browse...");
        srcButton.addActionListener(new SelectDirectoryListener("select a directory", srcTextField));

        add(UiKit.formRow("Source Directory", srcTextField, srcButton));
    }

    private void remove(String sourceBaseStr, String itemName) {
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
        int count = 0;
        for (File sourceParentFile : sourceParentFiles) {
            File sourceFile = new File(sourceParentFile, "strings.xml");
            if (sourceFile.exists()) {
                try {
                    System.out.println("read from: " + sourceFile.getCanonicalPath());
                    String content = FileUtils.readFileToString(sourceFile, CHARSET);
                    Pattern pattern = Pattern.compile("\\s*<string name=\"" + itemName + "\".*>.*</string>");
                    Matcher matcher = pattern.matcher(content);
                    String resultString = matcher.replaceAll("");
                    FileUtils.writeStringToFile(sourceFile, resultString, CHARSET);
                    logger.info("remove success, count: {}, and file: {}", ++count, sourceFile);
                } catch (IOException e) {
                    logger.error("remove exception: {}", sourceFile);
                }
            }
        }
    }
}
