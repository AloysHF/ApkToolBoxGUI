package edu.jiangxin.apktoolbox.file.password.recovery;

import edu.jiangxin.apktoolbox.file.password.recovery.category.CategoryFactory;
import edu.jiangxin.apktoolbox.file.password.recovery.category.CategoryType;
import edu.jiangxin.apktoolbox.file.password.recovery.category.ICategory;
import edu.jiangxin.apktoolbox.file.password.recovery.checker.*;
import edu.jiangxin.apktoolbox.file.password.recovery.checker.thirdparty.ThirdParty7ZipChecker;
import edu.jiangxin.apktoolbox.file.password.recovery.checker.thirdparty.ThirdPartyRarChecker;
import edu.jiangxin.apktoolbox.file.password.recovery.checker.thirdparty.ThirdPartyWinRarChecker;
import edu.jiangxin.apktoolbox.swing.extend.EasyPanel;
import edu.jiangxin.apktoolbox.swing.extend.filepanel.FilePanel;
import edu.jiangxin.apktoolbox.swing.extend.ui.UiKit;
import edu.jiangxin.apktoolbox.utils.Constants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.Serial;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RecoveryPanel extends EasyPanel {
    @Serial
    private static final long serialVersionUID = 1L;

    private JPanel optionPanel;

    private FilePanel recoveryFilePanel;

    private JTabbedPane categoryTabbedPane;

    private JPanel bruteForceCategoryPanel;

    private JPanel dictionaryCategoryPanel;

    private FilePanel dictionaryFilePanel;

    private CategoryType currentCategoryType = CategoryType.UNKNOWN;

    private JPanel operationPanel;

    private JCheckBox numberCheckBox;
    private JCheckBox lowercaseLetterCheckBox;
    private JCheckBox uppercaseLetterCheckBox;

    private JCheckBox userIncludedCheckBox;

    private JTextField userIncludedTextField;

    private JCheckBox userExcludedCheckBox;

    private JTextField userExcludedTextField;

    private JSpinner minSpinner;
    private JSpinner maxSpinner;

    private JCheckBox isUseMultiThreadCheckBox;

    private JProgressBar progressBar;

    private JComboBox<FileChecker> checkerTypeComboBox;

    private transient FileChecker currentFileChecker;

    private JButton startButton;
    private JButton stopButton;

    private JLabel currentStateLabel;

    private JLabel currentPasswordLabel;

    private JLabel currentSpeedLabel;

    private int passwordTryCount = 0;

    private NumberFormat numberFormat;

    private State currentState = State.IDLE;

    private Timer timer;

    private final transient ExecutorService startExecutorService = Executors.newFixedThreadPool(1);

    private final transient ExecutorService stopExecutorService = Executors.newFixedThreadPool(1);

    public RecoveryPanel() {
        super();
        initBase();
    }

    private void initBase() {
        numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(3);
    }

    @Override
    public void initUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setBorder(UiKit.sectionBorder("Password Recovery"));

        createOptionPanel();
        add(optionPanel);
        add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
        add(operationPanel);

        timer = new Timer(1000, e -> {
            if (currentState == State.WORKING) {
                int currentValue = progressBar.getValue();
                int speed = currentValue - passwordTryCount;
                passwordTryCount = currentValue;
                currentSpeedLabel.setText("Speed: " + speed + " passwords/s");
            }
        });
    }

    private void createOptionPanel() {
        optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));

        checkerTypeComboBox = new JComboBox<>();
        checkerTypeComboBox.addItem(new ThirdParty7ZipChecker());
        checkerTypeComboBox.addItem(new ThirdPartyWinRarChecker());
        checkerTypeComboBox.addItem(new ThirdPartyRarChecker());
        checkerTypeComboBox.addItem(new ZipChecker());
        checkerTypeComboBox.addItem(new RarChecker());
        checkerTypeComboBox.addItem(new SevenZipChecker());
        checkerTypeComboBox.addItem(new PdfChecker());
        checkerTypeComboBox.addItem(new XmlBasedOfficeChecker());
        checkerTypeComboBox.addItem(new BinaryOfficeChecker());
        checkerTypeComboBox.setSelectedIndex(0);
        checkerTypeComboBox.addItemListener(e -> {
            FileChecker fileChecker = (FileChecker) e.getItem();
            if (fileChecker == null) {
                logger.error("fileChecker is null");
                return;
            }
            recoveryFilePanel.setDescriptionAndFileExtensions(
                    fileChecker.getFileDescription(), fileChecker.getFileExtensions());
        });

        FileChecker fileChecker = (FileChecker) checkerTypeComboBox.getSelectedItem();
        if (fileChecker == null) {
            logger.error("fileChecker is null");
            return;
        }

        recoveryFilePanel = new FilePanel("Choose Recovery File");
        recoveryFilePanel.initialize();
        recoveryFilePanel.setDescriptionAndFileExtensions(fileChecker.getFileDescription(), fileChecker.getFileExtensions());

        categoryTabbedPane = new JTabbedPane();

        createBruteForcePanel();
        categoryTabbedPane.addTab("Brute Force", null, bruteForceCategoryPanel, "Brute Force");
        categoryTabbedPane.setSelectedIndex(0);

        createDictionaryPanel();
        categoryTabbedPane.addTab("Dictionary", null, dictionaryCategoryPanel, "Dictionary");

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        String text = numberFormat.format(0);
        progressBar.setString(text);
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        checkerTypeComboBox.setPreferredSize(new Dimension(320, UiKit.FIELD_HEIGHT));
        checkerTypeComboBox.setMaximumSize(new Dimension(320, UiKit.FIELD_HEIGHT));

        optionPanel.add(UiKit.formRow("Archive Type", checkerTypeComboBox));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(UiKit.formRow("File", recoveryFilePanel));
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(categoryTabbedPane);
        optionPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        optionPanel.add(progressBar);
    }

    private void createBruteForcePanel() {
        bruteForceCategoryPanel = new JPanel();

        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.Y_AXIS));
        bruteForceCategoryPanel.add(topLevelPanel);

        JPanel charsetPanel = new JPanel();
        charsetPanel.setLayout(new BoxLayout(charsetPanel, BoxLayout.X_AXIS));

        JPanel passwordLengthPanel = new JPanel();
        passwordLengthPanel.setLayout(new BoxLayout(passwordLengthPanel, BoxLayout.X_AXIS));

        topLevelPanel.add(charsetPanel);
        topLevelPanel.add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));
        topLevelPanel.add(passwordLengthPanel);

        numberCheckBox = new JCheckBox("Number");
        numberCheckBox.setSelected(true);
        lowercaseLetterCheckBox = new JCheckBox("Lowercase Letter");
        uppercaseLetterCheckBox = new JCheckBox("Uppercase Letter");
        userIncludedCheckBox = new JCheckBox("User-defined");
        userIncludedTextField = new JTextField(10);
        userExcludedCheckBox = new JCheckBox("User-excluded");
        userExcludedTextField = new JTextField(10);


        charsetPanel.add(numberCheckBox);
        charsetPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        charsetPanel.add(lowercaseLetterCheckBox);
        charsetPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        charsetPanel.add(uppercaseLetterCheckBox);
        charsetPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        charsetPanel.add(userIncludedCheckBox);
        charsetPanel.add(userIncludedTextField);
        charsetPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        charsetPanel.add(userExcludedCheckBox);
        charsetPanel.add(userExcludedTextField);

        JLabel minLabel = new JLabel("Minimum Length: ");
        JLabel maxLabel = new JLabel("Maximum Length: ");

        minSpinner = new JSpinner();
        minSpinner.setModel(new SpinnerNumberModel(1, 1, 9, 1));
        minSpinner.setToolTipText("Minimum Length");

        maxSpinner = new JSpinner();
        maxSpinner.setModel(new SpinnerNumberModel(6, 1, 9, 1));
        maxSpinner.setToolTipText("Maximum Length");

        passwordLengthPanel.add(minLabel);
        passwordLengthPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        passwordLengthPanel.add(minSpinner);
        passwordLengthPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        passwordLengthPanel.add(maxLabel);
        passwordLengthPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        passwordLengthPanel.add(maxSpinner);
    }

    private void createDictionaryPanel() {
        dictionaryCategoryPanel = new JPanel();

        JPanel topLevelPanel = new JPanel();
        topLevelPanel.setLayout(new BoxLayout(topLevelPanel, BoxLayout.Y_AXIS));
        dictionaryCategoryPanel.add(topLevelPanel);

        dictionaryFilePanel = new FilePanel("Choose Dictionary File");
        dictionaryFilePanel.initialize();
        dictionaryFilePanel.setDescriptionAndFileExtensions("*.dic;*.txt", new String[]{"dic", "txt"});

        JPanel threadPanel = new JPanel();
        threadPanel.setLayout(new BoxLayout(threadPanel, BoxLayout.X_AXIS));

        topLevelPanel.add(dictionaryFilePanel);
        topLevelPanel.add(Box.createVerticalStrut(Constants.DEFAULT_Y_BORDER));
        topLevelPanel.add(threadPanel);

        isUseMultiThreadCheckBox = new JCheckBox("Use Multi-thread");
        isUseMultiThreadCheckBox.setSelected(true);

        threadPanel.add(isUseMultiThreadCheckBox);
    }

    private void createOperationPanel() {
        operationPanel = new JPanel();
        operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.X_AXIS));

        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        currentStateLabel = new JLabel("State: IDLE");
        currentPasswordLabel = new JLabel("Trying: ");
        currentSpeedLabel = new JLabel("Speed: 0 passwords/s");

        operationPanel.add(startButton);
        operationPanel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        operationPanel.add(stopButton);
        operationPanel.add(Box.createHorizontalStrut(2 * Constants.DEFAULT_X_BORDER));
        operationPanel.add(currentStateLabel);
        operationPanel.add(Box.createHorizontalStrut(2 * Constants.DEFAULT_X_BORDER));
        operationPanel.add(currentPasswordLabel);
        operationPanel.add(Box.createHorizontalStrut(2 * Constants.DEFAULT_X_BORDER));
        operationPanel.add(currentSpeedLabel);
        operationPanel.add(Box.createHorizontalGlue());

        startButton.addActionListener(e -> startExecutorService.submit(this::onStart));
        stopButton.addActionListener(e -> stopExecutorService.submit(this::onStop));

        setCurrentState(State.IDLE);
    }

    private void onStart() {
        FileChecker fileChecker = (FileChecker) checkerTypeComboBox.getSelectedItem();
        if (fileChecker == null) {
            JOptionPane.showMessageDialog(this, "fileChecker is null!");
            return;
        }
        if (!fileChecker.prepareChecker()) {
            JOptionPane.showMessageDialog(this, "onStart failed: Checker condition does not been prepared!");
            return;
        }

        File selectedFile = recoveryFilePanel.getFile();
        if (!selectedFile.isFile()) {
            JOptionPane.showMessageDialog(this, "file is not a file!");
            return;
        }

        String extension = FilenameUtils.getExtension(recoveryFilePanel.getFile().getName());
        if (!ArrayUtils.contains(fileChecker.getFileExtensions(), StringUtils.toRootLowerCase(extension))) {
            JOptionPane.showMessageDialog(this, "invalid file!");
            return;
        }

        fileChecker.attachFile(selectedFile);
        currentFileChecker = fileChecker;

        Component selectedPanel = categoryTabbedPane.getSelectedComponent();
        if (selectedPanel.equals(bruteForceCategoryPanel)) {
            currentCategoryType = CategoryType.BRUTE_FORCE;
        } else if (selectedPanel.equals(dictionaryCategoryPanel)) {
            if (isUseMultiThreadCheckBox.isSelected()) {
                currentCategoryType = CategoryType.DICTIONARY_MULTI_THREAD;
            } else {
                currentCategoryType = CategoryType.DICTIONARY_SINGLE_THREAD;
            }
        } else {
            currentCategoryType = CategoryType.UNKNOWN;
        }
        logger.info("onStart: {}", currentCategoryType);
        if (currentCategoryType == CategoryType.UNKNOWN) {
            JOptionPane.showMessageDialog(this, "onStart failed: Invalid category!");
            return;
        }
        setCurrentState(State.WORKING);
        passwordTryCount = 0;
        timer.start();
        ICategory category = CategoryFactory.getCategoryInstance(currentCategoryType);
        category.start(this);
        if (currentState == State.WORKING) {
            onStop();
        }
    }

    private void onStop() {
        logger.info("onStop: currentState: {}, currentCategoryType: {}", currentState, currentCategoryType);
        if (currentState != State.WORKING) {
            logger.error("onStop failed: Not in working state!");
            return;
        }
        if (currentCategoryType == CategoryType.UNKNOWN) {
            logger.error("onStop failed: Invalid category!");
            return;
        }
        timer.stop();
        setCurrentState(State.STOPPING);
        ICategory category = CategoryFactory.getCategoryInstance(currentCategoryType);
        category.cancel();
        setCurrentState(State.IDLE);
        currentCategoryType = CategoryType.UNKNOWN;
    }

    public void showResultWithDialog(String password) {
        if (password == null) {
            logger.error("Can not find password");
            JOptionPane.showMessageDialog(this, "Can not find password");
        } else {
            logger.info("Find out the password: {}", password);
            JPanel panel = createPasswordShowPanel(password);
            JOptionPane.showMessageDialog(this, panel, "Find out the password", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static JPanel createPasswordShowPanel(String password) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        JTextField textField = new JTextField(password);
        textField.setEditable(false);
        textField.setColumns(20);
        JButton button = new JButton("Copy");
        button.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(password);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        panel.add(textField);
        panel.add(Box.createHorizontalStrut(Constants.DEFAULT_X_BORDER));
        panel.add(button);
        return panel;
    }

    public void setCurrentState(State currentState) {
        SwingUtilities.invokeLater(() -> {
            this.currentState = currentState;
            if (currentStateLabel != null) {
                currentStateLabel.setText("State: " + currentState.toString());
            }
            if (currentState == State.WORKING) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
                updateUiComponent(false);
            } else if (currentState == State.STOPPING) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                startButton.setEnabled(false);
                stopButton.setEnabled(false);
                updateUiComponent(false);
            } else if (currentState == State.IDLE) {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                updateUiComponent(true);
            }
        });
    }

    private void updateUiComponent(boolean enable) {
        for (Component component : getComponents(optionPanel)) {
            component.setEnabled(enable);
        }
    }

    private Component[] getComponents(Component container) {
        List<Component> list;

        try {
            list = new ArrayList<>(Arrays.asList(
                    ((Container) container).getComponents()));
            for (int index = 0; index < list.size(); index++) {
                list.addAll(Arrays.asList(getComponents(list.get(index))));
            }
        } catch (ClassCastException e) {
            list = new ArrayList<>();
        }

        return list.toArray(new Component[0]);
    }

    public State getCurrentState() {
        return currentState;
    }

    public void resetProgressMaxValue(int maxValue) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(maxValue);
            setProgressBarValue(0);
        });
    }

    public void increaseProgressBarValue() {
        SwingUtilities.invokeLater(() -> setProgressBarValue(progressBar.getValue() + 1));
    }

    private void setProgressBarValue(int value) {
        int properValue = Math.min(value, progressBar.getMaximum());
        progressBar.setValue(properValue);
        String text = numberFormat.format(((double) properValue) / progressBar.getMaximum());
        progressBar.setString(text);
    }

    public void setCurrentPassword(String password) {
        SwingUtilities.invokeLater(() -> currentPasswordLabel.setText("Trying: " + password));
    }

    public String getCharset() {
        Set<Character> charsetSet = new HashSet<>();
        if (numberCheckBox.isSelected()) {
            CollectionUtils.addAll(charsetSet, ArrayUtils.toObject("0123456789".toCharArray()));
        }
        if (lowercaseLetterCheckBox.isSelected()) {
            CollectionUtils.addAll(charsetSet, ArrayUtils.toObject("abcdefghijklmnopqrstuvwxyz".toCharArray()));
        }
        if (uppercaseLetterCheckBox.isSelected()) {
            CollectionUtils.addAll(charsetSet, ArrayUtils.toObject("ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()));
        }
        if (userIncludedCheckBox.isSelected()) {
            CollectionUtils.addAll(charsetSet, ArrayUtils.toObject(userIncludedTextField.getText().toCharArray()));
        }
        if (userExcludedCheckBox.isSelected()) {
            for (char ch : userExcludedTextField.getText().toCharArray()) {
                charsetSet.remove(ch);
            }
        }
        return String.valueOf(ArrayUtils.toPrimitive(charsetSet.toArray(new Character[0])));
    }

    public File getDictionaryFile() {
        return dictionaryFilePanel.getFile();
    }

    public int getMinLength() {
        return (Integer) minSpinner.getValue();
    }

    public int getMaxLength() {
        return (Integer) maxSpinner.getValue();
    }

    public FileChecker getCurrentFileChecker() {
        return currentFileChecker;
    }
}
