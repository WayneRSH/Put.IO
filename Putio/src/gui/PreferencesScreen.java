package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.UserPreferences;

import api.GetToken;

public class PreferencesScreen extends JFrame {
    private static final long serialVersionUID = -4807862182563388227L;

    // Own reference
    protected PreferencesScreen preferencesScreen;
    // Main screen reference
    protected MainScreen mainScreen;
    // Content area
    protected JPanel contentPanel;
    protected JTabbedPane tabbedPanel;
    // Connection tab
    protected JPanel connectionTabPanel;
    // User settings
    protected JPanel userSettingsPanel;
    protected JPanel userSettingsV1Panel;
    protected JLabel usernameLabel;
    protected JTextField usernameText;
    protected JLabel apiSecretLabel;
    protected JPasswordField apiSecretText;
    protected JLabel passwordLabel;
    protected JPasswordField passwordText;
    protected JPanel userTokenPanel;
    protected JLabel userTokenLabel;
    protected JTextField userTokenText;
    protected JButton userTokenButton;
    // Proxy settings
    protected JPanel proxySettingsPanel;
    protected JCheckBox proxyUseCheck;
    protected JLabel proxyAddressLabel;
    protected JTextField proxyAddressText;
    protected JLabel proxyPortLabel;
    protected JTextField proxyPortText;
    // Server settings
    protected JPanel serverSettingsPanel;
    protected JPanel serverCheckPanel;
    protected JLabel serverCheckIntervalLabel;
    protected JTextField serverCheckIntervalText;
    protected JCheckBox autoCleanCheck;
    protected JPanel serverSortByPanel;
    protected JLabel serverSortByLabel;
    protected JComboBox<String> serverSortByCombo;
    protected JPanel serverFriendPanel;
    protected JCheckBox loadSharedCheck;
    // Startup settings
    protected JPanel startupSettingsPanel;
    protected JCheckBox autoConnectCheck;
    protected JCheckBox startInTrayCheck;
    // Download settings
    protected JPanel downloadSettingsPanel;
    protected JPanel autoDownloadPanel;
    protected JCheckBox autoDownloadCheck;
    protected JPanel downloadTargetPanel;
    protected JLabel downloadTargetLabel;
    protected JTextField downloadTargetText;
    protected JButton fileChooserButton;
    protected JFileChooser fileChooser;
    protected JPanel downloadWhatPanel;
    protected JLabel downloadWhatLabel;
    protected JRadioButton everythingRadio;
    protected JRadioButton selFoldersRadio;
    protected ButtonGroup downloadWhatGroup;
    protected JPanel downloadPartsPanel;
    protected JLabel maxParallelDownloadsLabel;
    protected JTextField maxParallelDownloadsText;
    protected JLabel downloadPartsLabel;
    protected JTextField downloadPartsText;
    protected JPanel fileSizeCheckPanel;
    protected JCheckBox fileSizeCheckCheck;
    protected JTextField fileSizeCheckText;
    protected JPanel fileSizeDeletePanel;
    protected JCheckBox fileSizeDeleteCheck;
    // Conflict settings
    protected JPanel conflictSettingsPanel;
    protected JPanel redownloadPanel;
    protected JCheckBox redownloadCheck;
    protected JComboBox<String> redownloadCombo;
    protected JPanel overwritePanel;
    protected JCheckBox overwriteCheck;
    protected JPanel overwriteComboPanel;
    protected JComboBox<String> overwriteCombo;
    // Buttons
    protected JPanel buttonsPanel;
    protected JButton saveAndCloseButton;
    protected JButton closeButton;

    /**
     * Constructor
     * @param ms Reference of MainScreen
     */
    public PreferencesScreen(MainScreen ms) {
        preferencesScreen = this;
        mainScreen = ms;
        initWindow();
    }

    /**
     * Initializes components of the window
     */
    private void initWindow() {
        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        tabbedPanel = new JTabbedPane();
        tabbedPanel.setPreferredSize( new Dimension( 500, 800 ) );
        connectionTabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // User settings
        userSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userSettingsPanel.setLayout(new BoxLayout(userSettingsPanel, BoxLayout.PAGE_AXIS));
        userSettingsPanel.setBorder(new TitledBorder("User Settings"));
        userSettingsV1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernameLabel = new JLabel("Username:");
        usernameText = new JTextField(UserPreferences.PREF_USERNAME, 6);
        apiSecretLabel = new JLabel("API Secret:");
        apiSecretText = new JPasswordField(UserPreferences.PREF_API_SECRET, 6);
        passwordLabel = new JLabel("Password:");
        passwordText = new JPasswordField(UserPreferences.PREF_PASSWORD, 6);
        userSettingsV1Panel.add(usernameLabel);
        userSettingsV1Panel.add(usernameText);
        userSettingsV1Panel.add(apiSecretLabel);
        userSettingsV1Panel.add(apiSecretText);
        userSettingsV1Panel.add(passwordLabel);
        userSettingsV1Panel.add(passwordText);
        userTokenPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userTokenPanel.setPreferredSize( new Dimension( 443, 40 ) );
        userTokenLabel = new JLabel("Token:");
        userTokenText = new JTextField(UserPreferences.PREF_USERTOKEN, 10);
        userTokenButton = new JButton("Get your token");
        userTokenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GetToken.browse();
            }
        });
        userTokenPanel.add(userTokenLabel);
        userTokenPanel.add(userTokenText);
        userTokenPanel.add(userTokenButton);
        //userSettingsPanel.add(userSettingsV1Panel);
        userSettingsPanel.add(userTokenPanel);

        // Proxy settings
        proxySettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        proxyUseCheck = new JCheckBox("Use Proxy", UserPreferences.PREF_USE_PROXY);
        proxyUseCheck.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (proxyUseCheck.isSelected()) {
                    proxyAddressText.setEnabled(true);
                    proxyPortText.setEnabled(true);
                } else {
                    proxyAddressText.setEnabled(false);
                    proxyPortText.setEnabled(false);
                }
            }
        });
        proxySettingsPanel.setBorder(new TitledBorder("Proxy Settings"));
        proxyAddressLabel = new JLabel("Proxy:");
        proxyAddressText = new JTextField(UserPreferences.PREF_PROXY_ADDRESS, 15);
        proxyPortLabel = new JLabel("Port:");
        proxyPortText = new JTextField(UserPreferences.PREF_PROXY_PORT, 3);
        proxySettingsPanel.add(proxyUseCheck);
        proxySettingsPanel.add(proxyAddressLabel);
        proxySettingsPanel.add(proxyAddressText);
        proxySettingsPanel.add(proxyPortLabel);
        proxySettingsPanel.add(proxyPortText);
        proxySettingsPanel.setPreferredSize( new Dimension( 455, 60 ) );
        
        // Server settings
        serverSettingsPanel = new JPanel();
        serverSettingsPanel.setLayout(new BoxLayout(serverSettingsPanel, BoxLayout.PAGE_AXIS));
        serverSettingsPanel.setBorder(new TitledBorder("Server Settings"));
        serverCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverCheckIntervalLabel = new JLabel("Server check interval (sec):");
        serverCheckIntervalText = new JTextField(String.valueOf(UserPreferences.PREF_SERVER_CHECK_INTERVAL), 3);
        autoCleanCheck = new JCheckBox("Delete empty folders (keeps account clean)", UserPreferences.PREF_AUTO_CLEAN);
        serverCheckPanel.add(serverCheckIntervalLabel);
        serverCheckPanel.add(serverCheckIntervalText);
        serverCheckPanel.add(autoCleanCheck);
        serverSortByPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serverSortByLabel = new JLabel("Sort by default:");
        serverSortByCombo = new JComboBox<String>(new String[] {"Name", "Date"});
        serverSortByCombo.setSelectedIndex(UserPreferences.PREF_BEHAVIOR_SORT_BY);
        serverSortByPanel.add(serverSortByLabel);
        serverSortByPanel.add(serverSortByCombo);
        serverFriendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loadSharedCheck = new JCheckBox("Load friends' files (this could take some time to load)", UserPreferences.PREF_LOAD_SHARED);
        serverFriendPanel.add(loadSharedCheck);
        serverSettingsPanel.add(serverCheckPanel);
        serverSettingsPanel.add(serverSortByPanel);
        serverSettingsPanel.add(serverFriendPanel);
        serverSettingsPanel.setPreferredSize( new Dimension( 455, 120 ) );
        
        connectionTabPanel.add(userSettingsPanel);
        connectionTabPanel.add(proxySettingsPanel);
        connectionTabPanel.add(serverSettingsPanel);
        
        tabbedPanel.addTab("Connection", null, connectionTabPanel);

        // Startup settings
        startupSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        autoConnectCheck = new JCheckBox("Connect on start automatically", UserPreferences.PREF_AUTO_CONNECT);
        startInTrayCheck = new JCheckBox("Start in system tray", UserPreferences.PREF_START_IN_TRAY);
        startupSettingsPanel.setBorder(new TitledBorder("Startup Settings"));
        startupSettingsPanel.add(autoConnectCheck);
        startupSettingsPanel.add(startInTrayCheck);
        
        tabbedPanel.addTab("Startup", null, startupSettingsPanel);

        // Download Settings
        downloadSettingsPanel = new JPanel();
        downloadSettingsPanel.setLayout(new BoxLayout(downloadSettingsPanel, BoxLayout.PAGE_AXIS));
        downloadSettingsPanel.setBorder(new TitledBorder("Download Settings"));
        autoDownloadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        autoDownloadCheck = new JCheckBox("Download on connect", UserPreferences.PREF_AUTO_DOWNLOAD);
        autoDownloadPanel.add(autoDownloadCheck);
        downloadTargetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        downloadTargetLabel = new JLabel("Target:");
        downloadTargetText = new JTextField(UserPreferences.PREF_DOWNLOAD_TARGET, 20);
        downloadTargetText.setEditable(false);
        fileChooserButton = new JButton("Browse");
        fileChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fileChooser.showOpenDialog(preferencesScreen) == JFileChooser.APPROVE_OPTION) {
                    downloadTargetText.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        fileChooser = new JFileChooser(downloadTargetText.getText());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select download directory");
        fileChooser.setAcceptAllFileFilterUsed(false);
        downloadTargetPanel.add(downloadTargetLabel);
        downloadTargetPanel.add(downloadTargetText);
        downloadTargetPanel.add(fileChooserButton);
        downloadWhatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        downloadWhatLabel = new JLabel("Download:");
        everythingRadio = new JRadioButton("everything");
        selFoldersRadio = new JRadioButton("selected folders");
        selFoldersRadio.setToolTipText( "Right clic folder -> Add to automatic download" );
        downloadWhatGroup = new ButtonGroup();
        downloadWhatGroup.add( everythingRadio );
        downloadWhatGroup.add( selFoldersRadio );
        downloadWhatPanel.add( downloadWhatLabel );
        downloadWhatPanel.add( everythingRadio );
        downloadWhatPanel.add( selFoldersRadio );
        downloadPartsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maxParallelDownloadsLabel = new JLabel("Max parallel downloads:");
        maxParallelDownloadsText = new JTextField(String.valueOf(UserPreferences.PREF_MAX_DOWNLOADS), 2);
        downloadPartsLabel = new JLabel("Parts for each download:");
        downloadPartsText = new JTextField(String.valueOf(UserPreferences.PREF_DOWNLOAD_PART_COUNT), 2);
        downloadPartsPanel.add(maxParallelDownloadsLabel);
        downloadPartsPanel.add(maxParallelDownloadsText);
        downloadPartsPanel.add(downloadPartsLabel);
        downloadPartsPanel.add(downloadPartsText);
        fileSizeCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fileSizeCheckCheck = new JCheckBox("Skip download if size of the file is smaller than (MB):", UserPreferences.PREF_FILE_SIZE_CHECK);
        fileSizeCheckCheck.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                fileSizeCheckText.setEnabled(fileSizeCheckCheck.isSelected());
                fileSizeDeleteCheck.setEnabled(fileSizeCheckCheck.isSelected());
            }
        });
        fileSizeCheckText = new JTextField(String.valueOf(UserPreferences.PREF_FILE_SIZE_FOR_CHECK), 4);
        fileSizeCheckText.setEnabled(fileSizeCheckCheck.isSelected());
        fileSizeCheckPanel.add(fileSizeCheckCheck);
        fileSizeCheckPanel.add(fileSizeCheckText);
        fileSizeDeletePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        fileSizeDeleteCheck = new JCheckBox("Also delete it from server automatically", UserPreferences.PREF_FILE_SIZE_DELETE);
        fileSizeDeleteCheck.setEnabled(UserPreferences.PREF_FILE_SIZE_CHECK);
        fileSizeDeletePanel.add(fileSizeDeleteCheck);
        downloadSettingsPanel.add(autoDownloadPanel);
        downloadSettingsPanel.add(downloadTargetPanel);
        downloadSettingsPanel.add(downloadWhatPanel);
        downloadSettingsPanel.add(downloadPartsPanel);
        downloadSettingsPanel.add(fileSizeCheckPanel);
        downloadSettingsPanel.add(fileSizeDeletePanel);
        
        tabbedPanel.addTab("Download", null, downloadSettingsPanel);

        // Conflict settings
        conflictSettingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        conflictSettingsPanel.setBorder(new TitledBorder("Conflict Settings"));
        redownloadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        redownloadCheck = new JCheckBox("Do not ask if file already downloaded. Apply following: ", UserPreferences.PREF_DONT_ASK_DOWNLOAD_AGAIN);
        redownloadCheck.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                redownloadCombo.setEnabled(redownloadCheck.isSelected());
            }
        });
        redownloadCombo = new JComboBox<String>(new String[] {"Download again", "Skip", "Skip and delete"});
        redownloadCombo.setSelectedIndex(UserPreferences.PREF_BEHAVIOR_DOWNLOAD_AGAIN);
        redownloadCombo.setEnabled(UserPreferences.PREF_DONT_ASK_DOWNLOAD_AGAIN);
        redownloadPanel.add(redownloadCheck);
        redownloadPanel.add(redownloadCombo);
        overwritePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        overwriteCheck = new JCheckBox("Do not ask if file already exits on disc. Apply following: ", UserPreferences.PREF_DONT_ASK_OVERWRITE);
        overwriteCheck.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                overwriteCombo.setEnabled(overwriteCheck.isSelected());
            }
        });
        overwriteComboPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        overwriteCombo = new JComboBox<String>(new String[] {"Overwrite", "Skip", "Skip and delete from server", "Skip in case of same size", "Skip in case of same size and delete from server"});
        overwriteCombo.setSelectedIndex(UserPreferences.PREF_BEHAVIOR_OVERWRITE);
        overwriteCombo.setEnabled(UserPreferences.PREF_DONT_ASK_OVERWRITE);
        overwritePanel.add(overwriteCheck);
        overwriteComboPanel.add(overwriteCombo);
        conflictSettingsPanel.add(redownloadPanel);
        conflictSettingsPanel.add(overwritePanel);
        conflictSettingsPanel.add(overwriteComboPanel);
        
        tabbedPanel.addTab("Conflict", null, conflictSettingsPanel);

        // Buttons
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        saveAndCloseButton = new JButton("Save & Close");
        saveAndCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (saveSettings()) {
                    preferencesScreen.setVisible(false);
                }
            }
        });
        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                preferencesScreen.setVisible(false);
            }
        });
        buttonsPanel.add(saveAndCloseButton);
        buttonsPanel.add(closeButton);

        // Add to content pane
        contentPanel.add(tabbedPanel);
        contentPanel.add(buttonsPanel);

        this.getContentPane().add(contentPanel, BorderLayout.CENTER);

        this.setTitle("Preferences");
        this.setSize(new Dimension(490, 370));
        this.setResizable(false);
    }

    /**
     * Saves all user preferences
     * @return True if save operation was successful
     */
    private boolean saveSettings() {
        int maxDownloads, downloadPartCount, serverCheckInterval;
        float deleteLimit;
        try {
            maxDownloads = Integer.parseInt(maxParallelDownloadsText.getText());
            if (maxDownloads < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number as maximum parallel downloads!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            downloadPartCount = Integer.parseInt(downloadPartsText.getText());
            if (downloadPartCount < 1)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number as download part count!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            serverCheckInterval = Integer.parseInt(serverCheckIntervalText.getText());
            if (serverCheckInterval < 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number as server check interval!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            deleteLimit = Float.parseFloat(fileSizeCheckText.getText());
            if (deleteLimit < 0.0f)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number as the limit for deleting small files!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        UserPreferences.PREF_USERNAME = usernameText.getText();
        UserPreferences.PREF_API_SECRET = String.valueOf(apiSecretText.getPassword());
        UserPreferences.PREF_PASSWORD = String.valueOf(passwordText.getPassword());
        UserPreferences.PREF_USERTOKEN = userTokenText.getText();
        UserPreferences.PREF_USE_PROXY = proxyUseCheck.isSelected();
        UserPreferences.PREF_PROXY_ADDRESS = proxyAddressText.getText();
        UserPreferences.PREF_PROXY_PORT = proxyPortText.getText();
        UserPreferences.PREF_AUTO_CONNECT = autoConnectCheck.isSelected();
        UserPreferences.PREF_START_IN_TRAY = startInTrayCheck.isSelected();
        UserPreferences.PREF_AUTO_DOWNLOAD = autoDownloadCheck.isSelected();
        UserPreferences.PREF_DOWNLOAD_TARGET = downloadTargetText.getText();
        UserPreferences.PREF_MAX_DOWNLOADS = maxDownloads;
        UserPreferences.PREF_DOWNLOAD_PART_COUNT = downloadPartCount;
        UserPreferences.PREF_FILE_SIZE_CHECK = fileSizeCheckCheck.isSelected();
        UserPreferences.PREF_FILE_SIZE_FOR_CHECK = deleteLimit;
        UserPreferences.PREF_FILE_SIZE_DELETE = fileSizeDeleteCheck.isSelected();
        UserPreferences.PREF_AUTO_CLEAN = autoCleanCheck.isSelected();
        UserPreferences.PREF_LOAD_SHARED = loadSharedCheck.isSelected();
        UserPreferences.PREF_SERVER_CHECK_INTERVAL = serverCheckInterval;
        UserPreferences.PREF_DONT_ASK_DOWNLOAD_AGAIN = redownloadCheck.isSelected();
        UserPreferences.PREF_BEHAVIOR_DOWNLOAD_AGAIN = redownloadCombo.getSelectedIndex();
        UserPreferences.PREF_DONT_ASK_OVERWRITE = overwriteCheck.isSelected();
        UserPreferences.PREF_BEHAVIOR_OVERWRITE = overwriteCombo.getSelectedIndex();
        UserPreferences.PREF_BEHAVIOR_SORT_BY = serverSortByCombo.getSelectedIndex();
        UserPreferences.PREF_BEHAVIOR_DOWNLOAD_EVERYTHING = everythingRadio.isSelected();
        UserPreferences.saveUserPreferences();
        return true;
    }

    /**
     * Loads all user preferences
     */
    protected void loadSettings() {
        usernameText.setText(UserPreferences.PREF_USERNAME);
        apiSecretText.setText(UserPreferences.PREF_API_SECRET);
        passwordText.setText(UserPreferences.PREF_PASSWORD);
        userTokenText.setText(UserPreferences.PREF_USERTOKEN);
        proxyUseCheck.setSelected(UserPreferences.PREF_USE_PROXY);
        proxyAddressText.setText(UserPreferences.PREF_PROXY_ADDRESS);
        proxyPortText.setText(UserPreferences.PREF_PROXY_PORT);
        autoDownloadCheck.setSelected(UserPreferences.PREF_AUTO_DOWNLOAD);
        downloadTargetText.setText(UserPreferences.PREF_DOWNLOAD_TARGET);
        maxParallelDownloadsText.setText(String.valueOf(UserPreferences.PREF_MAX_DOWNLOADS));
        downloadPartsText.setText(String.valueOf(UserPreferences.PREF_DOWNLOAD_PART_COUNT));
        fileSizeCheckCheck.setSelected(UserPreferences.PREF_FILE_SIZE_CHECK);
        fileSizeCheckText.setText(String.valueOf(UserPreferences.PREF_FILE_SIZE_FOR_CHECK));
        fileSizeDeleteCheck.setSelected(UserPreferences.PREF_FILE_SIZE_DELETE);
        autoCleanCheck.setSelected(UserPreferences.PREF_AUTO_CLEAN);
        loadSharedCheck.setSelected(UserPreferences.PREF_LOAD_SHARED);
        serverCheckIntervalText.setText(String.valueOf(UserPreferences.PREF_SERVER_CHECK_INTERVAL));
        redownloadCheck.setSelected(UserPreferences.PREF_DONT_ASK_DOWNLOAD_AGAIN);
        redownloadCombo.setSelectedIndex(UserPreferences.PREF_BEHAVIOR_DOWNLOAD_AGAIN);
        overwriteCheck.setSelected(UserPreferences.PREF_DONT_ASK_OVERWRITE);
        overwriteCombo.setSelectedIndex(UserPreferences.PREF_BEHAVIOR_OVERWRITE);
        serverSortByCombo.setSelectedIndex(UserPreferences.PREF_BEHAVIOR_SORT_BY);
        if (UserPreferences.PREF_BEHAVIOR_DOWNLOAD_EVERYTHING) {
            everythingRadio.setSelected( true );
            selFoldersRadio.setSelected( false );
        }
        else {
            everythingRadio.setSelected( false );
            selFoldersRadio.setSelected( true );
        }

        usernameText.setEnabled(!mainScreen.getDownloadManager().getConnection().isConnected());
        apiSecretText.setEnabled(!mainScreen.getDownloadManager().getConnection().isConnected());
        passwordText.setEnabled(!mainScreen.getDownloadManager().getConnection().isConnected());
        proxyUseCheck.setEnabled(!mainScreen.getDownloadManager().getConnection().isConnected());
        proxyAddressText.setEnabled(!mainScreen.getDownloadManager().getConnection().isConnected() && proxyUseCheck.isSelected());
        proxyPortText.setEnabled(!mainScreen.getDownloadManager().getConnection().isConnected() && proxyUseCheck.isSelected());
    }
}
