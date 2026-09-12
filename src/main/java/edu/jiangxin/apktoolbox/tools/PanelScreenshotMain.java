package edu.jiangxin.apktoolbox.tools;

import edu.jiangxin.apktoolbox.android.dumpsys.DumpsysPanel;
import edu.jiangxin.apktoolbox.android.i18n.I18nAddPanel;
import edu.jiangxin.apktoolbox.android.i18n.I18nFindLongestPanel;
import edu.jiangxin.apktoolbox.android.i18n.I18nRemovePanel;
import edu.jiangxin.apktoolbox.android.screenshot.ScreenShotPanel;
import edu.jiangxin.apktoolbox.convert.color.ColorPickerPanel;
import edu.jiangxin.apktoolbox.file.EncodeConvertPanel;
import edu.jiangxin.apktoolbox.file.OsConvertPanel;
import edu.jiangxin.apktoolbox.file.batchrename.BatchRenamePanel;
import edu.jiangxin.apktoolbox.file.checksum.panel.FileChecksumPanel;
import edu.jiangxin.apktoolbox.file.checksum.panel.StringHashPanel;
import edu.jiangxin.apktoolbox.file.duplicate.DuplicateSearchPanel;
import edu.jiangxin.apktoolbox.file.password.recovery.RecoveryPanel;
import edu.jiangxin.apktoolbox.file.zhconvert.ZhConvertPanel;
import edu.jiangxin.apktoolbox.help.AboutPanel;
import edu.jiangxin.apktoolbox.help.settings.DependencyPathPanel;
import edu.jiangxin.apktoolbox.help.settings.LocalePanel;
import edu.jiangxin.apktoolbox.help.settings.LookAndFeelPanel;
import edu.jiangxin.apktoolbox.pdf.finder.PdfFinderPanel;
import edu.jiangxin.apktoolbox.pdf.passwordremover.PdfPasswordRemoverPanel;
import edu.jiangxin.apktoolbox.pdf.pic2pdf.Pic2PdfPanel;
import edu.jiangxin.apktoolbox.pdf.stat.PdfStatPanel;
import edu.jiangxin.apktoolbox.reverse.AxmlPrinterPanel;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.EasyChildTabbedPanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Utils;
import edu.jiangxin.apktoolbox.word.stat.WordStatPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Offline panel renderer for visual QA screenshots. */
public final class PanelScreenshotMain {

    private PanelScreenshotMain() {
    }

    public static void main(String[] args) throws Exception {
        if (!Utils.checkAndInitEnvironment()) {
            System.err.println("Environment init failed");
            System.exit(1);
        }
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf");
        UiKit.applyGlobalDefaults();

        File outDir = new File(args.length > 0 ? args[0] : "ui-shots");
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IllegalStateException("Cannot create " + outDir);
        }

        Map<String, Supplier<EasyPanel>> panels = new LinkedHashMap<>();
        panels.put("about", AboutPanel::new);
        panels.put("settings-lookandfeel", LookAndFeelPanel::new);
        panels.put("settings-locale", LocalePanel::new);
        panels.put("settings-dependency", DependencyPathPanel::new);
        panels.put("encode-convert", EncodeConvertPanel::new);
        panels.put("os-convert", OsConvertPanel::new);
        panels.put("zh-convert", ZhConvertPanel::new);
        panels.put("duplicate", DuplicateSearchPanel::new);
        panels.put("batch-rename", BatchRenamePanel::new);
        panels.put("checksum-file", FileChecksumPanel::new);
        panels.put("checksum-string", StringHashPanel::new);
        panels.put("recovery", RecoveryPanel::new);
        panels.put("pdf-stat", PdfStatPanel::new);
        panels.put("pdf-finder", PdfFinderPanel::new);
        panels.put("pdf-password-remover", PdfPasswordRemoverPanel::new);
        panels.put("pic2pdf", Pic2PdfPanel::new);
        panels.put("word-stat", WordStatPanel::new);
        panels.put("i18n-add", I18nAddPanel::new);
        panels.put("i18n-remove", I18nRemovePanel::new);
        panels.put("i18n-find-longest", I18nFindLongestPanel::new);
        panels.put("screenshot", ScreenShotPanel::new);
        panels.put("dumpsys", DumpsysPanel::new);
        panels.put("apksigner-placeholder", null);
        panels.put("axmlprinter", AxmlPrinterPanel::new);
        panels.put("color-picker", ColorPickerPanel::new);

        // ApkSigner needs plugin base; still can init UI
        panels.put("apksigner", edu.jiangxin.apktoolbox.reverse.ApkSignerPanel::new);
        panels.remove("apksigner-placeholder");

        List<String> failures = new ArrayList<>();
        for (Map.Entry<String, Supplier<EasyPanel>> entry : panels.entrySet()) {
            String name = entry.getKey();
            try {
                EasyPanel panel = entry.getValue().get();
                if (panel instanceof EasyChildTabbedPanel child) {
                    child.onTabSelected();
                } else {
                    panel.init();
                }
                File file = renderPanel(panel, new File(outDir, name + ".png"));
                System.out.println("OK  " + name + " -> " + file.getAbsolutePath());
            } catch (Throwable t) {
                failures.add(name + ": " + t);
                System.err.println("FAIL " + name + " -> " + t);
            }
        }

        if (!failures.isEmpty()) {
            System.err.println("Failures: " + failures);
            System.exit(2);
        }
        System.exit(0);
    }

    private static File renderPanel(JPanel panel, File outFile) throws Exception {
        panel.setSize(panel.getPreferredSize());
        Dimension pref = panel.getPreferredSize();
        int w = Math.max(640, Math.min(1100, Math.max(pref.width, 640)));
        int h = Math.max(360, Math.min(900, Math.max(pref.height, 360)));

        // Prefer a live frame pack when possible for more realistic layout.
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        int fw = Math.max(720, frame.getWidth());
        int fh = Math.max(400, frame.getHeight());
        frame.setSize(fw, fh);
        frame.setVisible(true);
        // Let layout/paint settle.
        for (int i = 0; i < 12; i++) {
            frame.validate();
            frame.paintAll(frame.getGraphics());
            Thread.sleep(20);
        }
        BufferedImage image = new BufferedImage(fw, fh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paintAll(g);
        g.dispose();
        frame.dispose();

        // If panel painted empty/black, fall back to whole frame snapshot.
        if (isNearlyUniform(image)) {
            frame = new JFrame();
            frame.setUndecorated(true);
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.pack();
            frame.setSize(fw, fh);
            frame.setVisible(true);
            frame.validate();
            SwingUtilities.invokeAndWait(() -> {
            });
            Thread.sleep(50);
            image = new Robot().createScreenCapture(new Rectangle(0, 0, fw, fh));
            // Position at origin for robot capture reliability
            frame.setLocation(0, 0);
            frame.toFront();
            Thread.sleep(80);
            image = new Robot().createScreenCapture(new Rectangle(0, 0, Math.min(fw, 1200), Math.min(fh, 900)));
            frame.dispose();
        }

        ImageIO.write(image, "png", outFile);
        return outFile;
    }

    private static boolean isNearlyUniform(BufferedImage image) {
        int sample = image.getRGB(10, 10);
        int same = 0;
        int total = 0;
        for (int y = 0; y < image.getHeight(); y += 40) {
            for (int x = 0; x < image.getWidth(); x += 40) {
                total++;
                if (image.getRGB(x, y) == sample) {
                    same++;
                }
            }
        }
        return total > 0 && same * 1.0 / total > 0.97;
    }
}
