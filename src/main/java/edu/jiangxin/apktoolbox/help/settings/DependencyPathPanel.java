package edu.jiangxin.apktoolbox.help.settings;

import edu.jiangxin.apktoolbox.swing.extend.EasyChildTabbedPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DependencyPathPanel extends EasyChildTabbedPanel {

    private static final long serialVersionUID = 1L;

    @Override
    public void createUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Third-party Dependencies"));

        createPathPanel(this, "7-Zip (e.g. C:/Program Files/7-Zip/7z.exe)", "https://www.7-zip.org/", Constants.SEVEN_ZIP_PATH_KEY);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createPathPanel(this, "RAR (e.g. C:/Program Files/WinRAR/Rar.exe)", "https://www.win-rar.com/", Constants.RAR_PATH_KEY);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createPathPanel(this, "WinRAR (e.g. C:/Program Files/WinRAR/WinRAR.exe)", "https://www.win-rar.com/", Constants.WIN_RAR_PATH_KEY);
    }

    private void createPathPanel(JPanel panel, String label, String website, String confKey) {
        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
        pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(pathPanel);

        JButton visitWebsiteButton = UiKit.secondaryButton(bundle.getString("download.button"));
        visitWebsiteButton.addActionListener(e -> {
            URI uri;
            try {
                uri = new URI(website);
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException ex) {
                logger.error("URISyntaxException", ex);
            } catch (IOException ex) {
                logger.error("IOException", ex);
            }
        });

        JTextField pathTextField = UiKit.field(new JTextField());
        pathTextField.setText(conf.getString(confKey));

        JButton pathButton = UiKit.secondaryButton(bundle.getString("choose.file.button"));
        pathButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setDialogTitle("select a file");
            int ret = jfc.showDialog(new JLabel(), null);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = jfc.getSelectedFile();
                pathTextField.setText(file.getAbsolutePath());
                conf.setProperty(confKey, pathTextField.getText());
            }
        });

        pathPanel.add(UiKit.formRow(label, visitWebsiteButton));
        pathPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        pathPanel.add(UiKit.formRow("Path", pathTextField, pathButton));
    }
}

