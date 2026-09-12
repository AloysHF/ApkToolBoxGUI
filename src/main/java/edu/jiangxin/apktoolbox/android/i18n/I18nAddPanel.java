/**
 * 
 */
package edu.jiangxin.apktoolbox.android.i18n;

import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.listener.SelectDirectoryListener;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.SAXBuilderHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class I18nAddPanel extends EasyPanel {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private static final String CHARSET = "UTF-8";

    private static final boolean REMOVE_LAST_LF_OPEN = true;

    private static final Map<String, String> replace = new HashedMap<>();

    private JTextField srcTextField;

    private JTextField targetTextField;

    private JTextField itemTextField;

    private int operationCount = 0;

    static {
        replace.put("&quot;", "jiangxin001");
        replace.put("&#160;", "jiangxin002");
    }
    
    public I18nAddPanel() throws HeadlessException {
        super();
    }

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Internationalization Resources"));

        createSourcePanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createTargetPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createItemPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));
        createOperationPanel();
    }

    private void createOperationPanel() {
        JButton addButton = UiKit.primaryButton(bundle.getString("android.i18n.add.title"));
        addButton.addActionListener(e -> {
            String srcPath = checkAndGetDirContent(srcTextField, "android.i18n.add.src.dir", "Source directory is invalid");
            if (srcPath == null) {
                return;
            }

            String targetPath = checkAndGetDirContent(targetTextField, "android.i18n.add.target.dir", "Target directory is invalid");
            if (targetPath == null) {
                return;
            }

            String itemStr = checkAndGetStringContent(itemTextField, "android.i18n.add.items", "Items is empty");
            if (itemStr == null) {
                return;
            }

            List<String> items = new ArrayList<>(Arrays.asList(itemStr.split(";")));
            operationCount = 0;

            for (String item : items) {
                int ret = innerProcessor(srcPath, targetPath, item);
                if (ret != 0) {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(this, "Failed, please see the log", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            String message = String.format("Success: items: %d", operationCount);
            JOptionPane.showMessageDialog(this, message, "INFO", JOptionPane.INFORMATION_MESSAGE);
        });

        add(UiKit.actionRow(addButton));
    }

    private void createItemPanel() {
        itemTextField = UiKit.field(new JTextField());
        itemTextField.setText(conf.getString("android.i18n.add.items"));
        add(UiKit.formRow("Items", itemTextField));
    }

    private void createTargetPanel() {
        targetTextField = UiKit.field(new JTextField());
        targetTextField.setText(conf.getString("android.i18n.add.target.dir"));

        JButton targetButton = UiKit.secondaryButton("Browse...");
        targetButton.addActionListener(new SelectDirectoryListener("save to", targetTextField));

        add(UiKit.formRow("Target Directory", targetTextField, targetButton));
    }

    private void createSourcePanel() {
        srcTextField = UiKit.field(new JTextField());
        srcTextField.setText(conf.getString("android.i18n.add.src.dir"));

        JButton srcButton = UiKit.secondaryButton("Browse...");
        srcButton.addActionListener(new SelectDirectoryListener("select a directory", srcTextField));

        add(UiKit.formRow("Source Directory", srcTextField, srcButton));
    }

    private int innerProcessor(String sourceBaseStr, String targetBaseStr, String itemName) {
        if (StringUtils.isAnyEmpty(sourceBaseStr, targetBaseStr, itemName)) {
            logger.error("params are invalid: sourceBaseStr: {}, targetBaseStr: {}, itemName: {}", sourceBaseStr, targetBaseStr, itemName);
            return -1;
        }
        File sourceBaseFile = new File(sourceBaseStr);
        File targetBaseFile = new File(targetBaseStr);
        int count = 0;

        File[] sourceParentFiles = sourceBaseFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("values");
            }
        });
        if (sourceParentFiles == null) {
            logger.error("sourceParentFiles is null");
            return -1;
        }
        for (File sourceParentFile : sourceParentFiles) {
            File sourceFile = new File(sourceParentFile, "strings.xml");

            Element sourceElement = getSourceElement(sourceFile, itemName);
            if (sourceElement == null) {
                logger.warn("sourceElement is null: {}", sourceFile);
                continue;
            }

            File targetFile = new File(new File(targetBaseFile, sourceParentFile.getName()), "strings.xml");
            if (!targetFile.exists()) {
                logger.warn("targetFile does not exist: {}", sourceFile);
                continue;
            }
            try {
                preProcess(targetFile);
            } catch (IOException e) {
                logger.error("preProcess failed.", e);
                return -1;
            }
            boolean res = setTargetElement(targetFile, sourceElement, itemName);
            if (!res) {
                logger.error("setTargetElement failed.");
                return -1;
            }
            try {
                postProcess(targetFile);
            } catch (IOException e) {
                logger.error("postProcess failed.", e);
                return -1;
            }
            logger.info("count: {}, in path: {}, out path: {}", ++count, sourceFile, targetFile);
        }
        operationCount += count;
        logger.info("finish one cycle");
        return 0;
    }

    private Element getSourceElement(File sourceFile, String itemName) {
        if (!sourceFile.exists()) {
            logger.warn("sourceFile does not exist: {}", sourceFile);
            return null;
        }
        SAXBuilder builder = new SAXBuilder();
        SAXBuilderHelper.setSecurityFeatures(builder);
        Document sourceDoc = null;
        try (InputStream in = new FileInputStream(sourceFile)) {
            sourceDoc = builder.build(in);
            logger.info("build source document: {}", sourceFile);
        } catch (JDOMException | IOException e) {
            logger.error("build source document failed: {}", sourceFile);
            return null;
        }
        if (sourceDoc == null) {
            logger.error("sourceDoc is null");
            return null;
        }
        Element sourceElement = null;
        for (Element sourceChild : sourceDoc.getRootElement().getChildren()) {
            String sourceValue = sourceChild.getAttributeValue("name");
            if (sourceValue != null && sourceValue.equals(itemName)) {
                sourceElement = sourceChild.clone();
                break;
            }
        }
        return sourceElement;
    }

    private boolean setTargetElement(File targetFile, Element sourceElement, String itemName) {
        SAXBuilder builder = new SAXBuilder();
        SAXBuilderHelper.setSecurityFeatures(builder);
        Document targetDoc;
        try {
            targetDoc = builder.build(targetFile);
            logger.info("build target document: {}", targetFile);
        } catch (JDOMException | IOException e) {
            logger.error("build target document failed: {}", targetFile);
            return false;
        }
        Element targetRoot = targetDoc.getRootElement();
        boolean isFinished = false;
        for (Element targetChild : targetRoot.getChildren()) {
            String targetValue = targetChild.getAttributeValue("name");
            if (targetValue != null && targetValue.equals(itemName)) {
                targetChild.setText(sourceElement.getText());
                isFinished = true;
                break;
            }
        }
        if (!isFinished) {
            targetRoot.addContent("    ");
            targetRoot.addContent(sourceElement);
            targetRoot.addContent("\n");
        }
        XMLOutputter out = new XMLOutputter();
        Format format = Format.getRawFormat();
        format.setEncoding("UTF-8");
        format.setLineSeparator("\n");
        out.setFormat(format);
        OutputStream os = null;
        try {
            os = new FileOutputStream(targetFile);
            out.output(targetDoc, os);
        } catch (IOException e) {
            logger.error("output fail", e);
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error("close output stream exception", e);
                }
            }
        }
        return true;
    }

    private static void preProcess(File file) throws IOException {
        String content = FileUtils.readFileToString(file, CHARSET);
        for (Map.Entry<String, String> entry : replace.entrySet()) {
            content = content.replaceAll(entry.getKey(), entry.getValue());
        }
        FileUtils.writeStringToFile(file, content, CHARSET);
    }

    private static void postProcess(File file) throws IOException {
        String content = FileUtils.readFileToString(file, CHARSET);
        for (Map.Entry<String, String> entry : replace.entrySet()) {
            content = content.replaceAll(entry.getValue(), entry.getKey());
        }
        if (REMOVE_LAST_LF_OPEN) {
            content = Strings.CS.removeEnd(content, "\n");
        }
        FileUtils.writeStringToFile(file, content, CHARSET);
    }

}
