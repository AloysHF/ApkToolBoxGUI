package edu.jiangxin.apktoolbox.swing.extend.plugin;

import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class ProgressBarDialog extends JDialog {
    @Serial
    private static final long serialVersionUID = 1L;
    final JProgressBar progressBar = new JProgressBar();
    final JLabel progressLabel = new JLabel("");

    public ProgressBarDialog() {
    }

    // in case of escape of "this"
    public void initialize(String title) {
        setTitle(title);
        setSize(440, 110);
        setLayout(new BorderLayout());
        //Swing Worker can't update GUI components in modal jdialog
        //https://stackoverflow.com/questions/54496606/swing-worker-cant-update-gui-components-in-modal-jdialog
        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(null);

        JPanel contentPanel = new JPanel(new BorderLayout(UiKit.GAP_MD, 0));
        contentPanel.setBorder(new javax.swing.border.EmptyBorder(UiKit.PAD, UiKit.PAD, UiKit.PAD, UiKit.PAD));
        contentPanel.add(progressBar, BorderLayout.CENTER);
        contentPanel.add(progressLabel, BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);

        progressBar.setMaximum(100);
        progressBar.setValue(0);
    }

    public void setValue(int value) {
        progressBar.setValue(value);
        progressLabel.setText(value + "%");
    }
}
