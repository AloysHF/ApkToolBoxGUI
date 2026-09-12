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
        setLayout(new BorderLayout(UiKit.GAP_MD, UiKit.GAP_MD));
        setBorder(UiKit.sectionBorder("Password Recovery"));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setMaximumSize(new Dimension(980, Integer.MAX_VALUE));

        createOptionPanel();
        form.add(optionPanel);
        form.add(Box.createVerticalStrut(UiKit.GAP_MD));

        createOperationPanel();
        form.add(operationPanel);

        add(form, BorderLayout.NORTH);

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
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.setBorder(UiKit.sectionBorder("Input"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(2, 8, 2, 8);

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

        categoryTabbedPane = new JTabbedPane();

        createBruteForcePanel();
        categoryTabbedPane.addTab("Brute Force", null, bruteForceCategoryPanel, "Brute Force");
        categoryTabbedPane.setSelectedIndex(0);

        createDictionaryPanel();
        categoryTabbedPane.addTab("Dictionary", null, dictionaryCategoryPanel, "Dictionary");

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString(numberFormat.format(0));
        progressBar.setPreferredSize(new Dimension(0, 22));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        optionPanel.add(gridRow("Archive Type", checkerTypeComboBox), gc);
        gc.gridy++;
        optionPanel.add(gridRow("File", recoveryFilePanel), gc);
        gc.gridy++;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1;
        optionPanel.add(categoryTabbedPane, gc);
        gc.gridy++;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weighty = 0;
        optionPanel.add(progressBar, gc);
    }

    private JPanel gridRow(String label, JComponent field) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(2, 0, 2, 0);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;

        JLabel jlabel = new JLabel(label);
        jlabel.setPreferredSize(new Dimension(UiKit.LABEL_WIDTH, UiKit.FIELD_HEIGHT));
        c.gridx = 0;
        row.add(jlabel, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        if (field instanceof JComboBox<?> combo) {
            combo.setPreferredSize(new Dimension(Math.max(combo.getPreferredSize().width, 280), UiKit.FIELD_HEIGHT));
            combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT));
        }
        row.add(field, c);
        return row;
    }

    private void createBruteForcePanel() {
        bruteForceCategoryPanel = new JPanel();
        bruteForceCategoryPanel.setLayout(new BoxLayout(bruteForceCategoryPanel, BoxLayout.Y_AXIS));
        bruteForceCategoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bruteForceCategoryPanel.setBorder(UiKit.sectionBorder("Charset & Length"));

        numberCheckBox = new JCheckBox("Number");
        numberCheckBox.setSelected(true);
        lowercaseLetterCheckBox = new JCheckBox("Lowercase");
        uppercaseLetterCheckBox = new JCheckBox("Uppercase");
        userIncludedCheckBox = new JCheckBox("Include");
        userIncludedTextField = UiKit.field(new JTextField());
        userIncludedTextField.setColumns(8);
        userExcludedCheckBox = new JCheckBox("Exclude");
        userExcludedTextField = UiKit.field(new JTextField());
        userExcludedTextField.setColumns(8);

        bruteForceCategoryPanel.add(UiKit.checkRow(
                numberCheckBox, lowercaseLetterCheckBox, uppercaseLetterCheckBox));
        bruteForceCategoryPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        bruteForceCategoryPanel.add(UiKit.checkRow(
                userIncludedCheckBox, userIncludedTextField,
                userExcludedCheckBox, userExcludedTextField));
        bruteForceCategoryPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));

        minSpinner = new JSpinner();
        minSpinner.setModel(new SpinnerNumberModel(1, 1, 9, 1));
        minSpinner.setToolTipText("Minimum password length");
        minSpinner.setPreferredSize(new Dimension(72, UiKit.FIELD_HEIGHT));
        minSpinner.setMaximumSize(new Dimension(72, UiKit.FIELD_HEIGHT));

        maxSpinner = new JSpinner();
        maxSpinner.setModel(new SpinnerNumberModel(6, 1, 9, 1));
        maxSpinner.setToolTipText("Maximum password length");
        maxSpinner.setPreferredSize(new Dimension(72, UiKit.FIELD_HEIGHT));
        maxSpinner.setMaximumSize(new Dimension(72, UiKit.FIELD_HEIGHT));

        JPanel lengthRow = new JPanel();
        lengthRow.setLayout(new BoxLayout(lengthRow, BoxLayout.X_AXIS));
        lengthRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lengthRow.setOpaque(false);
        lengthRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiKit.FIELD_HEIGHT + GAP_XS_PAD()));
        lengthRow.add(UiKit.mutedLabel("Length"));
        lengthRow.add(Box.createHorizontalStrut(UiKit.GAP_SM));
        lengthRow.add(UiKit.mutedLabel("Min"));
        lengthRow.add(Box.createHorizontalStrut(UiKit.GAP_XS));
        lengthRow.add(minSpinner);
        lengthRow.add(Box.createHorizontalStrut(UiKit.GAP_MD));
        lengthRow.add(UiKit.mutedLabel("Max"));
        lengthRow.add(Box.createHorizontalStrut(UiKit.GAP_XS));
        lengthRow.add(maxSpinner);
        lengthRow.add(Box.createHorizontalGlue());
        bruteForceCategoryPanel.add(lengthRow);
    }

    private int GAP_XS_PAD() {
        return UiKit.GAP_XS * 2;
    }

    private void createDictionaryPanel() {
        dictionaryCategoryPanel = new JPanel();
        dictionaryCategoryPanel.setLayout(new BoxLayout(dictionaryCategoryPanel, BoxLayout.Y_AXIS));
        dictionaryCategoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dictionaryCategoryPanel.setBorder(UiKit.sectionBorder("Dictionary"));

        dictionaryFilePanel = new FilePanel("Browse...");
        dictionaryFilePanel.initialize();
        dictionaryFilePanel.setDescriptionAndFileExtensions("*.dic;*.txt", new String[]{"dic", "txt"});

        isUseMultiThreadCheckBox = new JCheckBox("Use multi-thread");
        isUseMultiThreadCheckBox.setSelected(true);

        dictionaryCategoryPanel.add(UiKit.formRow("Dictionary", dictionaryFilePanel));
        dictionaryCategoryPanel.add(Box.createVerticalStrut(UiKit.GAP_SM));
        dictionaryCategoryPanel.add(UiKit.checkRow(isUseMultiThreadCheckBox));
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
