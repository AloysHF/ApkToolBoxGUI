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
import javax.swing.border.EmptyBorder;
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
        setLayout(new BorderLayout(0, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        createOptionPanel();
        createOperationPanel();

        JPanel main = new JPanel(new BorderLayout(0, 12));
        main.setOpaque(false);
        main.add(optionPanel, BorderLayout.NORTH);
        main.add(operationPanel, BorderLayout.SOUTH);
        add(main, BorderLayout.NORTH);

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
        optionPanel = new JPanel(new GridBagLayout());
        optionPanel.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.LINE_END;
        gc.insets = new Insets(4, 8, 4, 8);

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

        recoveryFilePanel = new FilePanel("Browse...");
        recoveryFilePanel.initialize();
        recoveryFilePanel.setDescriptionAndFileExtensions(fileChecker.getFileDescription(), fileChecker.getFileExtensions());

        checkerTypeComboBox.setPreferredSize(new Dimension(420, UiKit.FIELD_HEIGHT));
        checkerTypeComboBox.setMinimumSize(new Dimension(260, UiKit.FIELD_HEIGHT));
        checkerTypeComboBox.setMaximumSize(new Dimension(520, UiKit.FIELD_HEIGHT));

        JLabel archiveLabel = mutedRowLabel("Archive type");
        gc.gridy = 0;
        gc.gridx = 0;
        optionPanel.add(archiveLabel, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.LINE_START;
        optionPanel.add(checkerTypeComboBox, gc);

        JLabel fileLabel = mutedRowLabel("Encrypted file");
        gc.gridy = 1;
        gc.gridx = 0;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.LINE_END;
        optionPanel.add(fileLabel, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.LINE_START;
        optionPanel.add(recoveryFilePanel, gc);

        categoryTabbedPane = new JTabbedPane();
        createBruteForcePanel();
        categoryTabbedPane.addTab("Brute Force", null, bruteForceCategoryPanel, "Brute Force");
        categoryTabbedPane.setSelectedIndex(0);
        createDictionaryPanel();
        categoryTabbedPane.addTab("Dictionary", null, dictionaryCategoryPanel, "Dictionary");

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(numberFormat.format(0));
        progressBar.setPreferredSize(new Dimension(0, 18));

        gc.gridy = 2;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(10, 8, 4, 8);
        optionPanel.add(categoryTabbedPane, gc);

        gc.gridy = 3;
        gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(4, 8, 0, 8);
        optionPanel.add(progressBar, gc);
    }

    private JLabel mutedRowLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UiKit.MUTED);
        label.setPreferredSize(new Dimension(110, UiKit.FIELD_HEIGHT));
        label.setMinimumSize(new Dimension(110, UiKit.FIELD_HEIGHT));
        label.setMaximumSize(new Dimension(110, UiKit.FIELD_HEIGHT));
        return label;
    }

    private void createBruteForcePanel() {
        bruteForceCategoryPanel = new JPanel();
        bruteForceCategoryPanel.setLayout(new BoxLayout(bruteForceCategoryPanel, BoxLayout.Y_AXIS));
        bruteForceCategoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bruteForceCategoryPanel.setBorder(new EmptyBorder(UiKit.GAP_SM, 20, UiKit.GAP_XS, 20));

        numberCheckBox = new JCheckBox("Number");
        numberCheckBox.setSelected(true);
        lowercaseLetterCheckBox = new JCheckBox("Lowercase letters");
        uppercaseLetterCheckBox = new JCheckBox("Uppercase letters");

        JPanel charsetRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UiKit.GAP_LG, UiKit.GAP_XS));
        charsetRow.setOpaque(false);
        charsetRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        charsetRow.add(numberCheckBox);
        charsetRow.add(lowercaseLetterCheckBox);
        charsetRow.add(uppercaseLetterCheckBox);
        bruteForceCategoryPanel.add(charsetRow);
        bruteForceCategoryPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        userIncludedCheckBox = new JCheckBox("Include");
        userIncludedTextField = UiKit.field(new JTextField());
        userExcludedCheckBox = new JCheckBox("Exclude");
        userExcludedTextField = UiKit.field(new JTextField());

        JPanel customRow = new JPanel(new GridBagLayout());
        customRow.setOpaque(false);
        customRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, UiKit.GAP_MD);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0;
        gc.weightx = 0;
        customRow.add(userIncludedCheckBox, gc);
        gc.gridx = 1;
        gc.weightx = 1;
        customRow.add(userIncludedTextField, gc);
        gc.gridx = 2;
        gc.weightx = 0;
        customRow.add(userExcludedCheckBox, gc);
        gc.gridx = 3;
        gc.weightx = 1;
        customRow.add(userExcludedTextField, gc);

        JPanel customLine = new JPanel(new BorderLayout(UiKit.GAP_MD, 0));
        customLine.setOpaque(false);
        customLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        customLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT + 4));
        JLabel customLabel = new JLabel("Custom");
        customLabel.setForeground(UiKit.MUTED);
        customLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        customLabel.setPreferredSize(new Dimension(120, UiKit.FIELD_HEIGHT));
        customLabel.setMinimumSize(new Dimension(120, UiKit.FIELD_HEIGHT));
        customLine.add(customLabel, BorderLayout.WEST);
        customLine.add(customRow, BorderLayout.CENTER);
        bruteForceCategoryPanel.add(customLine);
        bruteForceCategoryPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        minSpinner = new JSpinner();
        minSpinner.setModel(new SpinnerNumberModel(1, 1, 9, 1));
        minSpinner.setToolTipText("Minimum password length");
        minSpinner.setPreferredSize(new Dimension(64, UiKit.FIELD_HEIGHT));
        minSpinner.setMinimumSize(new Dimension(64, UiKit.FIELD_HEIGHT));
        minSpinner.setMaximumSize(new Dimension(64, UiKit.FIELD_HEIGHT));

        maxSpinner = new JSpinner();
        maxSpinner.setModel(new SpinnerNumberModel(6, 1, 9, 1));
        maxSpinner.setToolTipText("Maximum password length");
        maxSpinner.setPreferredSize(new Dimension(64, UiKit.FIELD_HEIGHT));
        maxSpinner.setMinimumSize(new Dimension(64, UiKit.FIELD_HEIGHT));
        maxSpinner.setMaximumSize(new Dimension(64, UiKit.FIELD_HEIGHT));

        JPanel lengthControls = new JPanel(new FlowLayout(FlowLayout.LEFT, UiKit.GAP_SM, 0));
        lengthControls.setOpaque(false);
        lengthControls.add(new JLabel("Min"));
        lengthControls.add(minSpinner);
        lengthControls.add(Box.createHorizontalStrut(UiKit.GAP_MD));
        lengthControls.add(new JLabel("Max"));
        lengthControls.add(maxSpinner);

        JPanel lengthLine = new JPanel(new BorderLayout(UiKit.GAP_MD, 0));
        lengthLine.setOpaque(false);
        lengthLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        lengthLine.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT + 4));
        JLabel lengthLabel = new JLabel("Length");
        lengthLabel.setForeground(UiKit.MUTED);
        lengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        lengthLabel.setPreferredSize(new Dimension(120, UiKit.FIELD_HEIGHT));
        lengthLabel.setMinimumSize(new Dimension(120, UiKit.FIELD_HEIGHT));
        lengthLine.add(lengthLabel, BorderLayout.WEST);
        lengthLine.add(lengthControls, BorderLayout.CENTER);
        bruteForceCategoryPanel.add(lengthLine);
    }

    private void createDictionaryPanel() {
        dictionaryCategoryPanel = new JPanel(new GridBagLayout());
        dictionaryCategoryPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        dictionaryFilePanel = new FilePanel("Browse...");
        dictionaryFilePanel.initialize();
        dictionaryFilePanel.setDescriptionAndFileExtensions("*.dic;*.txt", new String[]{"dic", "txt"});

        isUseMultiThreadCheckBox = new JCheckBox("Use multi-thread");
        isUseMultiThreadCheckBox.setSelected(true);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.gridx = 0;
        gc.insets = new Insets(4, 4, 4, 8);
        gc.anchor = GridBagConstraints.LINE_END;
        dictionaryCategoryPanel.add(mutedRowLabel("Wordlist"), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.LINE_START;
        dictionaryCategoryPanel.add(dictionaryFilePanel, gc);

        gc.gridy = 1;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.LINE_START;
        gc.insets = new Insets(8, 4, 4, 8);
        dictionaryCategoryPanel.add(isUseMultiThreadCheckBox, gc);
    }

    private void createOperationPanel() {
        startButton = UiKit.primaryButton("Start");
        stopButton = UiKit.secondaryButton("Stop");
        stopButton.setEnabled(false);
        currentStateLabel = UiKit.mutedLabel("State: IDLE");
        currentPasswordLabel = UiKit.mutedLabel("Trying: —");
        currentSpeedLabel = UiKit.mutedLabel("Speed: 0 passwords/s");

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, UiKit.GAP_MD, 4));
        statusPanel.setOpaque(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.add(currentStateLabel);
        statusPanel.add(currentPasswordLabel);
        statusPanel.add(currentSpeedLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiKit.GAP_SM, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.add(stopButton);
        actions.add(startButton);

        JPanel bar = new JPanel(new BorderLayout(UiKit.GAP_MD, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setPreferredSize(new Dimension(900, 40));
        bar.setMinimumSize(new Dimension(400, 40));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        bar.add(statusPanel, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);

        operationPanel = new JPanel();
        operationPanel.setLayout(new BoxLayout(operationPanel, BoxLayout.Y_AXIS));
        operationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        operationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        operationPanel.add(bar);

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
