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

        createPathPanel(this, "7-Zip", "https://www.7-zip.org/", Constants.SEVEN_ZIP_PATH_KEY);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createPathPanel(this, "RAR", "https://www.win-rar.com/", Constants.RAR_PATH_KEY);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createPathPanel(this, "WinRAR", "https://www.win-rar.com/", Constants.WIN_RAR_PATH_KEY);
    }

    private void createPathPanel(JPanel panel, String name, String website, String confKey) {
        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));
        pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(pathPanel);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setPreferredSize(new Dimension(UiKit.LABEL_WIDTH, UiKit.FIELD_HEIGHT));

        JButton visitWebsiteButton = UiKit.secondaryButton(bundle.getString("download.button"));
        visitWebsiteButton.setToolTipText(website);
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

        JPanel header = new JPanel(new BorderLayout(UiKit.GAP_SM, 0));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT));
        header.add(nameLabel, BorderLayout.WEST);
        header.add(visitWebsiteButton, BorderLayout.EAST);
        pathPanel.add(header);
        pathPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        JTextField pathTextField = UiKit.field(new JTextField());
        pathTextField.setText(conf.getString(confKey));
        pathTextField.setToolTipText("Executable path, e.g. C:/Program Files/" + name + "/");

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

        pathPanel.add(UiKit.formRow("Path", pathTextField, pathButton));
    }
}

