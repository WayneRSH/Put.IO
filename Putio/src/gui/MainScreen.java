package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import communication.DownloadManager;
import util.GuiOperations;
import util.UserPreferences;

public class MainScreen extends JFrame {
    private static final long serialVersionUID = 2020155738164521721L;

    // Own reference
    private MainScreen mainScreen;
    // Download Manager
    protected DownloadManager downloadManager;
    protected Thread downloadManagerThread;

    // Menu
    protected JMenuBar mainMenuBar;
    protected JMenu fileMenu;
    protected JMenuItem connectMenuItem;
    protected JMenuItem disconnectMenuItem;
    protected JMenuItem preferencesMenuItem;
    protected JMenuItem exitMenuItem;
    protected JMenu downloadMenu;
    protected JMenuItem openDownloadFolderMenuItem;
    protected JMenuItem resetSessionMenuItem;
    protected JMenu aboutMenu;
    protected JMenuItem aboutProgramMenuItem;
    // Content area
    protected JPanel contentPanel;
    // Status bar
    protected JPanel statusBarPanel;
    protected JLabel statusLabel;
    protected JLabel updateTimeLabel;
    protected JLabel currentDownloadSpeedLabel;
    // Items
    //protected JPanel itemsPanel;
    protected ItemPanel itemsPanel;
    protected JTable itemTable;
    protected DefaultTableModel itemTableModel;
    protected JScrollPane itemScrollPane;
    // Screens
    protected PreferencesScreen preferencesScreen;
    protected AboutScreen aboutScreen;
    // Icons of the program
    protected Image programIcon;
    protected Image downloadActiveIcon;
    // System tray
    protected TrayIcon trayIcon;
    protected SystemTray systemTray;
    // Size
    protected int width = 900;
    protected int height = 700;

    public MainScreen() {
        // Get user preferences
        UserPreferences.loadUserPreferences();
        // Initialize program
        mainScreen = this;
        this.initWindow();
        downloadManager = new DownloadManager(this);
        downloadManagerThread = new Thread(downloadManager);
        downloadManagerThread.start();
        // Auto connect
        if (UserPreferences.PREF_AUTO_CONNECT) {
            GuiOperations.connect(this);
        }
    }

    protected void initWindow() {
        // Get icon image
        programIcon = Toolkit.getDefaultToolkit().getImage("res/putio.gif");

        // Menu
        mainMenuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        connectMenuItem = new JMenuItem("Connect");
        connectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiOperations.connect(mainScreen);
            }
        });
        disconnectMenuItem = new JMenuItem("Disconnect");
        disconnectMenuItem.setEnabled(false);
        disconnectMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiOperations.disconnect(mainScreen);
            }
        });
        preferencesMenuItem = new JMenuItem("Preferences");
        preferencesMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (preferencesScreen == null)
                    preferencesScreen = new PreferencesScreen(mainScreen);
                preferencesScreen.loadSettings();
                preferencesScreen.setLocationRelativeTo(mainScreen);
                preferencesScreen.setIconImage(programIcon);
                preferencesScreen.setVisible(true);
            }
        });
        downloadMenu = new JMenu("Download");
        openDownloadFolderMenuItem = new JMenuItem("Open download folder");
        openDownloadFolderMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiOperations.openDownloadFolder();
            }
        });
        resetSessionMenuItem = new JMenuItem("Reset download session");
        resetSessionMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GuiOperations.resetDownloadSession(mainScreen);
            }
        });
        aboutMenu = new JMenu("About");
        aboutProgramMenuItem = new JMenuItem("Program");
        aboutProgramMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (aboutScreen == null)
                    aboutScreen = new AboutScreen();
                aboutScreen.setLocationRelativeTo(mainScreen);
                aboutScreen.setIconImage(programIcon);
                aboutScreen.setVisible(true);
            }
        });
        aboutMenu.add(aboutProgramMenuItem);
        mainMenuBar.add(fileMenu);
        fileMenu.add(connectMenuItem);
        fileMenu.add(disconnectMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(preferencesMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        mainMenuBar.add(downloadMenu);
        downloadMenu.add(openDownloadFolderMenuItem);
        downloadMenu.addSeparator();
        downloadMenu.add(resetSessionMenuItem);
        mainMenuBar.add(aboutMenu);

        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Status bar
        statusBarPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Not connected");
        updateTimeLabel = new JLabel();
        currentDownloadSpeedLabel = new JLabel("Current download speed: 0.0 MB/s");
        statusBarPanel.add(statusLabel, BorderLayout.WEST);
        statusBarPanel.add(updateTimeLabel, BorderLayout.PAGE_END);
        statusBarPanel.add(currentDownloadSpeedLabel, BorderLayout.EAST);
        statusBarPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        // Item table
        /*
        itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(new TitledBorder("Items"));
        */
        
        /*
        itemTableModel = new DefaultTableModel(new String[] {"Id", "File", "Type", "Size", "Link", "Status"}, 0) {
            private static final long serialVersionUID = 585424215608387056L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        itemTable = new JTable(itemTableModel);
        itemTable.getColumn("Id").setMinWidth(0);
        itemTable.getColumn("Id").setMaxWidth(0);
        itemTable.setFillsViewportHeight(true);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int selectedRowIndex = itemTable.getSelectedRow();
                    if (selectedRowIndex >= 0) {
                        pauseOrResumeDownloadMenuItem.setEnabled(true);
                        cancelDownloadMenuItem.setEnabled(true);
                        removeDownloadMenuItem.setEnabled(true);
                    } else {
                        pauseOrResumeDownloadMenuItem.setEnabled(false);
                        cancelDownloadMenuItem.setEnabled(false);
                        removeDownloadMenuItem.setEnabled(false);
                    }
                    itemMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseClicked(MouseEvent e) {
            }
        });
        itemScrollPane = new JScrollPane(itemTable);
        itemsPanel.add(itemScrollPane); 
        */

        itemsPanel = new ItemPanel(mainScreen);

        // Add to content pane
        contentPanel.add(itemsPanel);

        this.getContentPane().add(mainMenuBar, BorderLayout.NORTH);
        this.getContentPane().add(contentPanel, BorderLayout.CENTER);
        this.getContentPane().add(statusBarPanel, BorderLayout.SOUTH);

        // SystemTray
        if (SystemTray.isSupported()) {
            trayIcon = new TrayIcon(programIcon.getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING), "put.io Download Manager");
            systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added");
            }
            trayIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mainScreen.setVisible(true);
                    mainScreen.setState(Frame.NORMAL);
                    mainScreen.toFront();
                }
            });
            PopupMenu trayMenu = new PopupMenu();
            MenuItem exitMenuItem = new MenuItem("Exit");
            exitMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            trayMenu.add(exitMenuItem);
            trayIcon.setPopupMenu(trayMenu);
        }

        // Main screen
        this.setIconImage(programIcon);
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }
            @Override
            public void windowIconified(WindowEvent e) {
            }
            @Override
            public void windowDeiconified(WindowEvent e) {
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
            }
            @Override
            public void windowClosing(WindowEvent e) {
                mainScreen.setVisible(false);
            }
            @Override
            public void windowClosed(WindowEvent e) {
            }
            @Override
            public void windowActivated(WindowEvent e) {
            }
        });
        this.setTitle("put.io Download Manager");
        this.setSize(new Dimension(width, height));
        this.setMinimumSize(new Dimension(700, 350));
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setVisible(!UserPreferences.PREF_START_IN_TRAY);
    }

    /**
     * @return the downloadManager
     */
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    /**
     * @return the itemTable
     */
    public JTable getItemTable() {
        return itemTable;
    }

    /**
     * @return the itemsPanel
     */
    public ItemPanel getItemsPanel() {
        return itemsPanel;
    }

    /**
     * Sets status text on main screen
     * @param status Status text to be displayed
     */
    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    /**
     * Sets remaining update time
     * @param time
     */
    public void setUpdateTime(String time) {
        updateTimeLabel.setText(time);
    }

    /**
     * Enabled/Disabled status of connection menu
     * @param isConnected True if connected
     */
    public void displayAsConnected(boolean isConnected) {
        connectMenuItem.setEnabled(!isConnected);
        disconnectMenuItem.setEnabled(isConnected);
        if (!isConnected)
            setUpdateTime("");
    }

    /**
     * Cleans items panel
     */
    public void cleanItemsPanel() {
        itemsPanel.initTree();
    }

    /**
     * Changes system tray icon
     * @param isDownloadActive TRUE if there is currently an active download
     */
    public void changeTrayIcon(boolean isDownloadActive) {
        if (isDownloadActive) {
            // Get icon image
            if (downloadActiveIcon == null)
                downloadActiveIcon = Toolkit.getDefaultToolkit().getImage("res/putio_active.gif");
            trayIcon.setImage(downloadActiveIcon.getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
        } else {

            trayIcon.setImage(programIcon.getScaledInstance(16, 16, Image.SCALE_AREA_AVERAGING));
        }
    }

    /**
     * Sets system tray icon text
     * @param text Text to be written
     */
    public void updateCurrentDownloadSpeed(String text) {
        trayIcon.setToolTip("put.io Download Manager (" + text + ")");
        currentDownloadSpeedLabel.setText("Current download speed: " + text);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
