package gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import communication.DownloadManager;
import util.GuiOperations;
import util.StaticIcon;
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
    protected ItemPanel itemPanel;
    protected JScrollPane itemScrollPane;
    // Screens
    protected PreferencesScreen preferencesScreen;
    protected AboutScreen aboutScreen;
    // Icons of the program
    protected StaticIcon programIcon = new StaticIcon( StaticIcon.putioIcon );
    protected StaticIcon downloadActiveIcon = new StaticIcon( StaticIcon.putioActiveIcon );
    // Tool bar
    protected JToolBar toolBar;
    protected JButton connectButton;
    protected JButton refreshButton;
    protected JButton downloadButton;
    protected JButton settingsButton;
    protected JButton stopButton;
    protected StaticIcon connectIcon = new StaticIcon( StaticIcon.connectIcon );
    protected StaticIcon disconnectIcon = new StaticIcon( StaticIcon.disconnectIcon );
    protected StaticIcon refreshIcon = new StaticIcon( StaticIcon.refreshIcon );
    protected StaticIcon pauseIcon = new StaticIcon( StaticIcon.pauseIcon );
    protected StaticIcon playIcon = new StaticIcon( StaticIcon.playIcon );
    protected StaticIcon settingsIcon = new StaticIcon( StaticIcon.settingsIcon );
    protected StaticIcon stopIcon = new StaticIcon( StaticIcon.stopIcon );
    // System tray
    protected TrayIcon trayIcon;
    protected SystemTray systemTray;
    // Size
    protected int width = 800;
    protected int height = 600;

    public MainScreen() {
        // Get user preferences
        UserPreferences.loadUserPreferences();
        // Initialize program
        mainScreen = this;
        this.initWindow();
        downloadManager = new DownloadManager( this );
        downloadManagerThread = new Thread( downloadManager );
        downloadManagerThread.start();
        // Auto connect
        if ( UserPreferences.PREF_AUTO_CONNECT ) {
            GuiOperations.connect( this );
        }
    }

    protected void initWindow() {
        // Menu
        mainMenuBar = new JMenuBar();
        fileMenu = new JMenu( "File" );
        exitMenuItem = new JMenuItem( "Exit" );
        exitMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                closeProgram();
            }
        } );
        connectMenuItem = new JMenuItem( "Connect" );
        connectMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.connect( mainScreen );
            }
        } );
        disconnectMenuItem = new JMenuItem( "Disconnect" );
        disconnectMenuItem.setEnabled( false );
        disconnectMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.disconnect( mainScreen );
            }
        } );
        preferencesMenuItem = new JMenuItem( "Preferences" );
        preferencesMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                if ( preferencesScreen == null )
                    preferencesScreen = new PreferencesScreen( mainScreen );
                preferencesScreen.loadSettings();
                preferencesScreen.setLocationRelativeTo( mainScreen );
                preferencesScreen.setIconImage( programIcon.getImage() );
                preferencesScreen.setVisible( true );
            }
        } );
        downloadMenu = new JMenu( "Download" );
        openDownloadFolderMenuItem = new JMenuItem( "Open download folder" );
        openDownloadFolderMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.openDownloadFolder();
            }
        } );
        resetSessionMenuItem = new JMenuItem( "Reset download session" );
        resetSessionMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.resetDownloadSession( mainScreen );
            }
        } );
        aboutMenu = new JMenu( "About" );
        aboutProgramMenuItem = new JMenuItem( "Program" );
        aboutProgramMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                if ( aboutScreen == null )
                    aboutScreen = new AboutScreen();
                aboutScreen.setLocationRelativeTo( mainScreen );
                aboutScreen.setIconImage( programIcon.getImage() );
                aboutScreen.setVisible( true );
            }
        } );
        aboutMenu.add( aboutProgramMenuItem );
        mainMenuBar.add( fileMenu );
        fileMenu.add( connectMenuItem );
        fileMenu.add( disconnectMenuItem );
        fileMenu.addSeparator();
        fileMenu.add( preferencesMenuItem );
        fileMenu.addSeparator();
        fileMenu.add( exitMenuItem );
        mainMenuBar.add( downloadMenu );
        downloadMenu.add( openDownloadFolderMenuItem );
        downloadMenu.addSeparator();
        downloadMenu.add( resetSessionMenuItem );
        mainMenuBar.add( aboutMenu );

        // Content
        contentPanel = new JPanel();
        contentPanel.setLayout( new BorderLayout() );
        contentPanel.setBorder( new EmptyBorder( 0, 5, 5, 5 ) );

        // Button bar
        toolBar = new JToolBar();
        toolBar.setFloatable( false );
        toolBar.setRollover( true );
        connectButton = new JButton( connectIcon ) {
            private static final long serialVersionUID = -2470428648196485552L;

            @Override
            public void paintComponent( Graphics g ) {
                if ( downloadManager.getConnection().isConnected() ) {
                    connectButton.setIcon( disconnectIcon );
                    connectButton.setToolTipText( "Disconnect" );
                    refreshButton.setEnabled( true );
                }
                else {
                    connectButton.setIcon( connectIcon );
                    connectButton.setToolTipText( "Connect" );
                    refreshButton.setEnabled( false );
                }
                super.paintComponent( g );
            }
        };
        connectButton.setFocusPainted( false );
        connectButton.setFocusable( false );
        connectButton.setToolTipText( "Connect" );
        connectButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                if ( downloadManager.getConnection().isConnected() )
                    GuiOperations.disconnect( mainScreen );
                else
                    GuiOperations.connect( mainScreen );
            }
        } );
        refreshButton = new JButton( refreshIcon );
        refreshButton.setFocusPainted( false );
        refreshButton.setFocusable( false );
        refreshButton.setToolTipText( "Refresh" );
        refreshButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                if ( downloadManagerThread.isAlive() )
                    downloadManager.refresh();
            }
        } );
        if ( !UserPreferences.PREF_AUTO_DOWNLOAD ) {
            downloadButton = new JButton( playIcon );
            downloadButton.setToolTipText( "Download" );
        }
        else {
            downloadButton = new JButton( pauseIcon );
            downloadButton.setToolTipText( "Pause" );
        }
        downloadButton.setFocusPainted( false );
        downloadButton.setFocusable( false );
        downloadButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.toggleDownload( mainScreen );
                if ( downloadManager.isDownloading() ) {
                    downloadButton.setIcon( pauseIcon );
                    downloadButton.setToolTipText( "Pause" );
                }
                else {
                    downloadButton.setIcon( playIcon );
                    downloadButton.setToolTipText( "Download" );
                }
            }
        } );
        settingsButton = new JButton( settingsIcon );
        settingsButton.setFocusPainted( false );
        settingsButton.setFocusable( false );
        settingsButton.setToolTipText( "Preferences" );
        settingsButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                if ( preferencesScreen == null )
                    preferencesScreen = new PreferencesScreen( mainScreen );
                preferencesScreen.loadSettings();
                preferencesScreen.setLocationRelativeTo( mainScreen );
                preferencesScreen.setIconImage( programIcon.getImage() );
                preferencesScreen.setVisible( true );
            }
        } );
        stopButton = new JButton( stopIcon );
        stopButton.setFocusPainted( false );
        stopButton.setFocusable( false );
        stopButton.setToolTipText( "Exit program" );
        stopButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                closeProgram();
            }
        } );
        toolBar.add( connectButton );
        toolBar.add( refreshButton );
        toolBar.addSeparator();
        toolBar.add( downloadButton );
        toolBar.addSeparator();
        toolBar.add( settingsButton );
        toolBar.addSeparator();
        toolBar.add( stopButton );

        // Item panel
        try {
            itemPanel = new ItemPanel( mainScreen );
        }
        catch ( Exception e ) {
            e.printStackTrace();
            System.exit( 1 );
        }

        // Add to content pane
        contentPanel.add( toolBar, BorderLayout.PAGE_START );
        contentPanel.add( itemPanel, BorderLayout.CENTER );

        // Status bar
        statusBarPanel = new JPanel( new BorderLayout() );
        statusLabel = new JLabel( "Not connected" );
        updateTimeLabel = new JLabel();
        currentDownloadSpeedLabel = new JLabel( "Current download speed: 0.0 MB/s" );
        statusBarPanel.add( statusLabel, BorderLayout.WEST );
        statusBarPanel.add( updateTimeLabel, BorderLayout.PAGE_END );
        statusBarPanel.add( currentDownloadSpeedLabel, BorderLayout.EAST );
        statusBarPanel.setBorder( new EmptyBorder( 5, 10, 5, 10 ) );

        this.getContentPane().add( mainMenuBar, BorderLayout.NORTH );
        this.getContentPane().add( contentPanel, BorderLayout.CENTER );
        this.getContentPane().add( statusBarPanel, BorderLayout.SOUTH );

        // SystemTray
        if ( SystemTray.isSupported() ) {
            trayIcon = new TrayIcon( programIcon.getImage().getScaledInstance( 16, 16,
                    Image.SCALE_AREA_AVERAGING ), "put.io Download Manager" );
            systemTray = SystemTray.getSystemTray();
            try {
                systemTray.add( trayIcon );
            }
            catch ( AWTException e ) {
                System.out.println( "TrayIcon could not be added" );
            }
            trayIcon.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    mainScreen.setVisible( true );
                    mainScreen.setState( Frame.NORMAL );
                    mainScreen.toFront();
                }
            } );
            PopupMenu trayMenu = new PopupMenu();
            MenuItem exitMenuItem = new MenuItem( "Exit" );
            exitMenuItem.addActionListener( new ActionListener() {
                @Override
                public void actionPerformed( ActionEvent e ) {
                    closeProgram();
                }
            } );
            trayMenu.add( exitMenuItem );
            trayIcon.setPopupMenu( trayMenu );
        }

        // Main screen
        this.setIconImage( programIcon.getImage() );

        this.addComponentListener( new ComponentListener() {
            public void componentResized( ComponentEvent e ) {
                mainScreen.width = e.getComponent().getSize().width;
                mainScreen.height = e.getComponent().getSize().height;
            }

            public void componentHidden( ComponentEvent e ) {
            }

            public void componentMoved( ComponentEvent e ) {
            }

            public void componentShown( ComponentEvent e ) {
            }
        } );

        this.addWindowListener( new WindowListener() {
            @Override
            public void windowOpened( WindowEvent e ) {
            }

            @Override
            public void windowIconified( WindowEvent e ) {
            }

            @Override
            public void windowDeiconified( WindowEvent e ) {
            }

            @Override
            public void windowDeactivated( WindowEvent e ) {
            }

            @Override
            public void windowClosing( WindowEvent e ) {
                mainScreen.setVisible( false );
            }

            @Override
            public void windowClosed( WindowEvent e ) {
            }

            @Override
            public void windowActivated( WindowEvent e ) {
            }
        } );
        this.setTitle( "put.io Download Manager" );
        int frameWidth = UserPreferences.PREFS.getInt( "FRAME_WIDTH", this.width );
        int frameHeight = UserPreferences.PREFS.getInt( "FRAME_HEIGHT", this.height );
        this.setSize( new Dimension( frameWidth, frameHeight ) );
        int framePosX = UserPreferences.PREFS.getInt( "FRAME_POSX",
                (int) Math.floor( ( Toolkit.getDefaultToolkit().getScreenSize().width - this.width ) / 2 ) );
        int framePosY = UserPreferences.PREFS.getInt( "FRAME_POSY",
                (int) Math.floor( ( Toolkit.getDefaultToolkit().getScreenSize().height - this.height ) / 2 ) );
        this.setLocation( framePosX, framePosY );
        this.setMinimumSize( new Dimension( 700, 350 ) );
        this.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        this.setVisible( !UserPreferences.PREF_START_IN_TRAY );
    }

    /**
     * @return the downloadManager
     */
    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    /**
     * @return the itemPanel
     */
    public ItemPanel getItemPanel() {
        return itemPanel;
    }

    /**
     * Sets status text on main screen
     * 
     * @param status
     *            Status text to be displayed
     */
    public void setStatus( String status ) {
        statusLabel.setText( status );
    }

    /**
     * Sets remaining update time
     * 
     * @param time
     */
    public void setUpdateTime( String time ) {
        updateTimeLabel.setText( time );
    }

    /**
     * Enabled/Disabled status of connection menu
     * 
     * @param isConnected
     *            True if connected
     */
    public void displayAsConnected( boolean isConnected ) {
        connectMenuItem.setEnabled( !isConnected );
        disconnectMenuItem.setEnabled( isConnected );
        if ( !isConnected )
            setUpdateTime( "" );
    }

    /**
     * Cleans items panel
     */
    public void cleanItemPanel() {
        itemPanel.initTree();
    }

    /**
     * Changes system tray icon
     * 
     * @param isDownloadActive
     *            TRUE if there is currently an active download
     */
    public void changeTrayIcon( boolean isDownloadActive ) {
        if ( isDownloadActive ) {
            trayIcon.setImage( downloadActiveIcon.getImage().getScaledInstance( 16, 16,
                    Image.SCALE_AREA_AVERAGING ) );
        }
        else {
            trayIcon.setImage( programIcon.getImage().getScaledInstance( 16, 16, Image.SCALE_AREA_AVERAGING ) );
        }
    }

    /**
     * Sets system tray icon text
     * 
     * @param text
     *            Text to be written
     */
    public void updateCurrentDownloadSpeed( String text ) {
        trayIcon.setToolTip( "put.io Download Manager (" + text + ")" );
        currentDownloadSpeedLabel.setText( "Current download speed: " + text );
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void closeProgram() {
        UserPreferences.PREFS.putInt( "FRAME_WIDTH", this.width );
        UserPreferences.PREFS.putInt( "FRAME_HEIGHT", this.height );
        UserPreferences.PREFS.putInt( "FRAME_POSX", this.getX() );
        UserPreferences.PREFS.putInt( "FRAME_POSY", this.getY() );
        UserPreferences.PREFS.putInt( "FRAME_DIV_POS", itemPanel.getDivPos() );
        System.exit( 0 );
    }
}
