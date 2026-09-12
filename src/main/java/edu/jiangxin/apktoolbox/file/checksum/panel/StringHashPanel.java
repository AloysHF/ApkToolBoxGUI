package edu.jiangxin.apktoolbox.file.checksum.panel;

import edu.jiangxin.apktoolbox.file.checksum.CalculateType;
import edu.jiangxin.apktoolbox.swing.extend.EasyChildTabbedPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class StringHashPanel extends EasyChildTabbedPanel {
    private static final long serialVersionUID = 63924900336217723L;

    private JPanel stringInputPanel;
    private JTextArea stringInputTextArea;

    private JPanel optionPanel;

    private JTextField stringLengthTextField;
    private JCheckBox md5CheckBox;
    private JTextField md5TextField;
    private JCheckBox sha1CheckBox;
    private JTextField sha1TextField;
    private JCheckBox sha256CheckBox;
    private JTextField sha256TextField;
    private JCheckBox sha384CheckBox;
    private JTextField sha384TextField;
    private JCheckBox sha512CheckBox;
    private JTextField sha512TextField;
    private JCheckBox crc32CheckBox;
    private JTextField crc32TextField;

    private JPanel operationPanel;
    private JProgressBar progressBar;

    @Override
    public void createUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        createStringInputPanel();
        add(stringInputPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOptionPanel();
        add(optionPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
        add(operationPanel);
    }

    private void createStringInputPanel() {
        stringInputPanel = new JPanel();
        stringInputPanel.setLayout(new BoxLayout(stringInputPanel, BoxLayout.Y_AXIS));
        stringInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stringInputPanel.setBorder(UiKit.sectionBorder("Input"));

        stringInputTextArea = new JTextArea(5, 4);
        stringInputTextArea.setLineWrap(true);
        stringInputTextArea.setMargin(new Insets(UiKit.GAP_SM, UiKit.GAP_SM, UiKit.GAP_SM, UiKit.GAP_SM));
        JScrollPane stringInputScrollPanel = new JScrollPane(stringInputTextArea);

        stringInputPanel.add(UiKit.formRow("String", stringInputScrollPanel));
    }

    private void createOptionPanel() {
        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.setBorder(UiKit.sectionBorder("Results"));

        stringLengthTextField = UiKit.field(new JTextField());
        stringLengthTextField.setEditable(false);
        optionPanel.add(UiKit.formRow("Length", stringLengthTextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        md5CheckBox = new JCheckBox("MD5");
        md5CheckBox.setSelected(true);
        md5TextField = UiKit.field(new JTextField());
        md5TextField.setEditable(false);
        optionPanel.add(UiKit.formRow("MD5", md5CheckBox, md5TextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        sha1CheckBox = new JCheckBox("SHA1");
        sha1TextField = UiKit.field(new JTextField());
        sha1TextField.setEditable(false);
        optionPanel.add(UiKit.formRow("SHA1", sha1CheckBox, sha1TextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        sha256CheckBox = new JCheckBox("SHA256");
        sha256TextField = UiKit.field(new JTextField());
        sha256TextField.setEditable(false);
        optionPanel.add(UiKit.formRow("SHA256", sha256CheckBox, sha256TextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        sha384CheckBox = new JCheckBox("SHA384");
        sha384TextField = UiKit.field(new JTextField());
        sha384TextField.setEditable(false);
        optionPanel.add(UiKit.formRow("SHA384", sha384CheckBox, sha384TextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        sha512CheckBox = new JCheckBox("SHA512");
        sha512TextField = UiKit.field(new JTextField());
        sha512TextField.setEditable(false);
        optionPanel.add(UiKit.formRow("SHA512", sha512CheckBox, sha512TextField));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        crc32CheckBox = new JCheckBox("CRC32");
        crc32TextField = UiKit.field(new JTextField());
        crc32TextField.setEditable(false);
        optionPanel.add(UiKit.formRow("CRC32", crc32CheckBox, crc32TextField));
    }

    private void createOperationPanel() {
        JButton compareButton = UiKit.primaryButton("Calculate");
        compareButton.addActionListener(arg0 -> {
            String text = stringInputTextArea.getText();
            if (text == null) {
                logger.error("text is null");
                return;
            }
            calculate(text);
        });

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setMaximum(100);
        progressBar.setPreferredSize(new Dimension(200, 22));

        JPanel actions = UiKit.actionRow(compareButton);
        JPanel bar = new JPanel(new BorderLayout(UiKit.GAP_MD, 0));
        bar.setOpaque(false);
        bar.add(progressBar, BorderLayout.CENTER);
        bar.add(actions, BorderLayout.EAST);

        operationPanel = new JPanel();
        operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.Y_AXIS));
        operationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPanel.add(bar);
    }

    private void calculate(String text) {
        progressBar.setValue(0);
        new Thread(() -> {
            stringLengthTextField.setText(String.valueOf(text.length()));
            progressBar.setValue(progressBar.getValue() + 10);
        }).start();

        new Thread(() -> {
            if (md5CheckBox.isSelected()) {
                md5TextField.setText(calculate(CalculateType.Md5, text));
            } else {
                md5TextField.setText("");
            }
            progressBar.setValue(progressBar.getValue() + 15);
        }).start();

        new Thread(() -> {
            if (sha1CheckBox.isSelected()) {
                sha1TextField.setText(calculate(CalculateType.Sha1, text));
            } else {
                sha1TextField.setText("");
            }
            progressBar.setValue(progressBar.getValue() + 15);
        }).start();

        new Thread(() -> {
            if (sha256CheckBox.isSelected()) {
                sha256TextField.setText(calculate(CalculateType.Sha256, text));
            } else {
                sha256TextField.setText("");
            }
            progressBar.setValue(progressBar.getValue() + 15);
        }).start();

        new Thread(() -> {
            if (sha384CheckBox.isSelected()) {
                sha384TextField.setText(calculate(CalculateType.Sha384, text));
            } else {
                sha384TextField.setText("");
            }
            progressBar.setValue(progressBar.getValue() + 15);
        }).start();

        new Thread(() -> {
            if (sha512CheckBox.isSelected()) {
                sha512TextField.setText(calculate(CalculateType.Sha512, text));
            } else {
                sha512TextField.setText("");
            }
            progressBar.setValue(progressBar.getValue() + 15);
        }).start();

        new Thread(() -> {
            if (crc32CheckBox.isSelected()) {
                crc32TextField.setText(calculate(CalculateType.Crc32, text));
            } else {
                crc32TextField.setText("");
            }
            progressBar.setValue(progressBar.getValue() + 15);
        }).start();
    }

    private String calculate(final CalculateType selectedHash, final String text) {
        String result = "";
        try (ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))) {
            switch (selectedHash) {
                case Md5: {
                    result = DigestUtils.md5Hex(bais);
                    break;
                }
                case Sha1: {
                    result = DigestUtils.sha1Hex(bais);
                    break;
                }
                case Sha256: {
                    result = DigestUtils.sha256Hex(bais);
                    break;
                }
                case Sha384: {
                    result = DigestUtils.sha384Hex(bais);
                    break;
                }
                case Sha512: {
                    result = DigestUtils.sha512Hex(bais);
                    break;
                }
                case Crc32: {
                    CRC32 crc32 = new CRC32();
                    crc32.update(text.getBytes());
                    result = Long.toHexString(crc32.getValue());
                    break;
                }
                default: {
                    result = "Not support";
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("calculate, IOException");
        }
        return result;
    }
}

