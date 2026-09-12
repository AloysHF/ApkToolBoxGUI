package edu.jiangxin.apktoolbox.reverse;

import edu.jiangxin.apktoolbox.swing.extend.listener.SelectFileListener;
import edu.jiangxin.apktoolbox.swing.extend.plugin.PluginPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Utils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author jiangxin
 * @author 2019-04-12
 *
 */
public class ApkSignerPanel extends PluginPanel {

    private static final long serialVersionUID = 1L;

    private JTextField apkPathTextField;

    private JTextField keyStorePathTextField;

    private JPasswordField keyStorePasswordField;

    private JTextField aliasTextField;

    private JPasswordField aliasPasswordField;

    public ApkSignerPanel() throws HeadlessException {
        super();
    }

    @Override
    public String getPluginFilename() {
        return "apksigner.jar";
    }

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("APK Signing"));

        createApkPathPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createKeyStorePathPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createKeyStorePasswordPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createAliasPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createAliasPasswordPanel();
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOptionPanel();
    }

    private void createOptionPanel() {
        JButton recoverButton = UiKit.secondaryButton("Recover Defaults");
        recoverButton.addActionListener(new RecoverButtonActionListener());

        JButton apkSignButton = UiKit.primaryButton("Sign APK");
        apkSignButton.addActionListener(new ApkSignButtonActionListener());

        add(UiKit.actionRow(recoverButton, apkSignButton));
    }

    private void createAliasPasswordPanel() {
        aliasPasswordField = new JPasswordField();
        aliasPasswordField.setText(conf.getString("apksigner.alias.password"));
        aliasPasswordField.setPreferredSize(new Dimension(0, UiKit.FIELD_HEIGHT));
        aliasPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT));
        add(UiKit.formRow("Alias Password", aliasPasswordField));
    }

    private void createAliasPanel() {
        aliasTextField = UiKit.field(new JTextField());
        aliasTextField.setText(conf.getString("apksigner.alias"));
        add(UiKit.formRow("Alias", aliasTextField));
    }

    private void createKeyStorePasswordPanel() {
        keyStorePasswordField = new JPasswordField();
        keyStorePasswordField.setText(conf.getString("apksigner.keystore.password"));
        keyStorePasswordField.setPreferredSize(new Dimension(0, UiKit.FIELD_HEIGHT));
        keyStorePasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT));
        add(UiKit.formRow("KeyStore Password", keyStorePasswordField));
    }

    private void createKeyStorePathPanel() {
        keyStorePathTextField = UiKit.field(new JTextField());
        keyStorePathTextField.setText(conf.getString("apksigner.keystore.path"));

        JButton keyStorePathButton = UiKit.secondaryButton("Browse...");
        keyStorePathButton.addActionListener(new SelectFileListener("select a keystore file", keyStorePathTextField));

        add(UiKit.formRow("KeyStore", keyStorePathTextField, keyStorePathButton));
    }

    private void createApkPathPanel() {
        apkPathTextField = UiKit.field(new JTextField());
        apkPathTextField.setText(conf.getString("apksigner.apk.path"));

        JButton apkPathButton = UiKit.secondaryButton("Browse...");
        apkPathButton.addActionListener(new SelectFileListener("select a APK file", apkPathTextField));

        add(UiKit.formRow("APK", apkPathTextField, apkPathButton));
    }
    
    private final class RecoverButtonActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            keyStorePathTextField.setText(conf.getString("default.apksigner.keystore.path"));
            keyStorePasswordField.setText(conf.getString("default.apksigner.keystore.password"));
            aliasTextField.setText(conf.getString("default.apksigner.alias"));
            aliasPasswordField.setText("default.apksigner.alias.password");
        }
    }

    private final class ApkSignButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String apkPath = checkAndGetFileContent(apkPathTextField, "apksigner.apk.path", "apk file is invalid");
            if (StringUtils.isEmpty(apkPath)) {
                return;
            }

            String keystorePath = checkAndGetFileContent(keyStorePathTextField, "apksigner.keystore.path", "keystore file is invalid");
            if (StringUtils.isEmpty(keystorePath)) {
                return;
            }

            String keystorePassword = checkAndGetStringContent(keyStorePasswordField, "apksigner.keystore.password", "keystorePassword is invalid");
            if (StringUtils.isEmpty(keystorePassword)) {
                return;
            }

            String alias = checkAndGetStringContent(aliasTextField, "apksigner.alias", "alias is invalid");
            if (StringUtils.isEmpty(alias)) {
                return;
            }

            String aliasPassword = checkAndGetStringContent(aliasPasswordField, "apksigner.alias.password", "aliasPassword is invalid");
            if (StringUtils.isEmpty(aliasPassword)) {
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(getPluginStartupCmd())
                    .append(" -keystore ").append(keystorePath).append(" -pswd ").append(keystorePassword)
                    .append(" -alias ").append(alias).append(" -aliaspswd ").append(aliasPassword).append(" ")
                    .append(apkPath);

            Utils.executor(sb.toString(), true);
        }
    }
}
