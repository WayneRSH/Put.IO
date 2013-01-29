package communication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import javax.swing.JOptionPane;

import gui.MainScreen;
import util.GuiOperations;
import util.UserPreferences;

import api.Item;

public class DownloadManager implements Runnable {
    // Necessary components
    protected Queue<Download> downloadQueue;
    protected Map<Integer, Download> activeDownloads;
    protected Map<Integer, Download> sessionDownloads;
    protected Connection connection;
    protected Thread connectionThread;

    // Last update time
    protected long lastUpdateTime = System.currentTimeMillis();

    // GUI
    protected MainScreen ms;

    /**
     * @return the downloadQueue
     */
    public Queue<Download> getDownloadQueue() {
        return downloadQueue;
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return the activeDownloads
     */
    public Map<Integer, Download> getActiveDownloads() {
        return activeDownloads;
    }

    /**
     * @return the sessionDownloads
     */
    public Map<Integer, Download> getSessionDownloads() {
        return sessionDownloads;
    }

    /**
     * Constructor
     * 
     * @param ms
     *            Reference of MainScreen
     */
    public DownloadManager( MainScreen ms ) {
        downloadQueue = new LinkedList<Download>();
        activeDownloads = new HashMap<Integer, Download>();
        sessionDownloads = new HashMap<Integer, Download>();
        this.ms = ms;
        this.connection = new Connection();
    }

    @Override
    public void run() {
        long timeSinceUpdate, timeToNextUpdate;
        double totalSpeed;
        while ( true ) {
            try {
                if ( connection.isConnected() ) {
                    timeSinceUpdate = Math.abs( System.currentTimeMillis()
                            - lastUpdateTime );
                    timeToNextUpdate = ( ( UserPreferences.PREF_SERVER_CHECK_INTERVAL * 1000 ) - timeSinceUpdate ) / 1000L;
                    if ( timeToNextUpdate < 0 )
                        timeToNextUpdate = 0;
                    ms.setUpdateTime( "Time to next update: "
                            + String.valueOf( timeToNextUpdate ) + " seconds" );
                    if ( Math.abs( timeSinceUpdate ) >= UserPreferences.PREF_SERVER_CHECK_INTERVAL * 1000 ) {
                        connection.refresh();
                        ms.setStatus( "Connected as "
                                + connection.getUser().getName()
                                + " - Available Quota: "
                                + GuiOperations.getReadableSize( connection
                                        .getUser().getDiskQuotaAvailable() ) );
                        refreshQueue();
                        lastUpdateTime = System.currentTimeMillis();
                    }
                    if ( UserPreferences.PREF_AUTO_DOWNLOAD ) {
                        while ( activeDownloads.size() < UserPreferences.PREF_MAX_DOWNLOADS
                                && downloadQueue.size() > 0 ) {
                            Download d = downloadQueue.poll();
                            if ( !UserPreferences.PREF_FILE_SIZE_CHECK ) {
                                activeDownloads.put( d.getId(), d );
                                startDownload( d );
                            } else {
                                if ( d.getTotalLength() >= ( (long) ( UserPreferences.PREF_FILE_SIZE_FOR_CHECK * 1048576 ) ) ) {
                                    activeDownloads.put( d.getId(), d );
                                    startDownload( d );
                                } else {
                                    ms.getItemTable()
                                            .getModel()
                                            .setValueAt(
                                                    "Skipped",
                                                    GuiOperations.getRowNumber(
                                                            ms, d.getId() ), 5 );
                                    if ( UserPreferences.PREF_FILE_SIZE_DELETE ) {
                                        connection.refresh();
                                        if ( d.delete() ) {
                                            ms.getItemTable()
                                                    .getModel()
                                                    .setValueAt(
                                                            "Skipped (Deleted)",
                                                            GuiOperations
                                                                    .getRowNumber(
                                                                            ms,
                                                                            d.getId() ),
                                                            5 );
                                        } else {
                                            ms.getItemTable()
                                                    .getModel()
                                                    .setValueAt(
                                                            "Skipped (Delete failed!)",
                                                            GuiOperations
                                                                    .getRowNumber(
                                                                            ms,
                                                                            d.getId() ),
                                                            5 );
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Update total download speed displays in GUI
                    totalSpeed = 0.0;
                    Iterator<Download> downloads = activeDownloads.values()
                            .iterator();
                    while ( downloads.hasNext() ) {
                        totalSpeed += downloads.next().getCurrentSpeed();
                    }
                    ms.updateCurrentDownloadSpeed( String
                            .valueOf( GuiOperations
                                    .roundTo2Decimals( totalSpeed ) )
                            + " MB/s" );
                }
                Thread.sleep( 1000 );
            } catch ( InterruptedException e ) {
                // ignored
            }
        }
    }

    public void connect() {
        ms.setStatus( "Connecting..." );
        ms.displayAsConnected( true );
        connection.oauth_token = UserPreferences.PREF_USERTOKEN;

        connectionThread = new Thread( new Runnable() {
            @Override
            public void run() {
                if ( connection.connect() ) {
                    if ( connection.isConnected() ) {
                        ms.setStatus( "Connected as "
                                + connection.getUser().getName()
                                + " - Available Quota: "
                                + GuiOperations.getReadableSize( connection
                                        .getUser().getDiskQuotaAvailable() ) );
                        ms.cleanItemsPanel();
                        refreshQueue();
                        lastUpdateTime = System.currentTimeMillis();
                    }
                } else {
                    ms.setStatus( "Connection Error" );
                    ms.displayAsConnected( false );
                    ms.cleanItemsPanel();
                }
            }
        } );
        connectionThread.start();
    }

    public void disconnect() {
        ms.getItemsPanel().stopOp();
        connection.disconnect();
        ms.setStatus( "Not connected" );
        ms.displayAsConnected( false );
        ms.cleanItemsPanel();
    }

    public void refreshQueue() {
        try {
            ms.getItemsPanel().populateTree( connection );
            List<Item> items = ms.getItemsPanel().getItems();
            if (items != null) {
                ListIterator<Item> i = items.listIterator();
                while (i.hasNext()) {
                    Item currentItem = i.next();
                    addItemsToQueue(currentItem);
                }
            }
        } catch ( Exception e ) {
            System.out.println( e.toString() );
        }
    }

    private void addItemsToQueue( Item item ) {
        // if (!item.isDir()) {
        if ( isQueueNecessary( item ) ) {
            Download d = new Download( UserPreferences.PREF_USERNAME,
                    UserPreferences.PREF_PASSWORD, item,
                    UserPreferences.PREF_DOWNLOAD_TARGET, connection );
            downloadQueue.add( d );
            sessionDownloads.put( d.getId(), d );
            // ((DefaultTableModel) ms.getItemTable().getModel()).addRow(new
            // Object[] { d.getId(), item.getName(), item.getContentType(),
            // String.valueOf(item.getSize()), item.getDownloadUrl(), "" });
        }
        // }
        try {
            List<Item> childrenItems = item.listChildren();
            if ( childrenItems.size() == 0 && item.isDir()
                    && UserPreferences.PREF_AUTO_CLEAN ) {
                item.delete();
            } else {
                ListIterator<Item> i = childrenItems.listIterator();
                while ( i.hasNext() ) {
                    Item currentItem = i.next();
                    addItemsToQueue( currentItem );
                }
            }
        } catch ( Exception e ) {
            // No children items
        }
    }

    private boolean isQueueNecessary( Item item ) {
        boolean queueNecessary = true;
        Iterator<Download> i = sessionDownloads.values().iterator();
        while ( i.hasNext() ) {
            Download download = i.next();
            if ( download.getPutioId().equals( item.getId() ) ) {
                if ( download.isCompleted() || download.isFaulty() ) {
                    if ( !download.isCanceled() ) {
                        if ( !UserPreferences.PREF_DONT_ASK_DOWNLOAD_AGAIN ) {
                            switch ( JOptionPane
                                    .showConfirmDialog(
                                            null,
                                            "This file was already downloaded. Would you like to download it again?",
                                            "Duplicate: " + item.getName(),
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE ) ) {
                            case JOptionPane.YES_OPTION:
                                queueNecessary = true;
                                sessionDownloads.remove( download.getId() );
                                break;
                            case JOptionPane.NO_OPTION:
                                queueNecessary = false;
                                switch ( JOptionPane
                                        .showConfirmDialog(
                                                null,
                                                "Would you like to delete it from server?",
                                                "Delete",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE ) ) {
                                case JOptionPane.YES_OPTION:
                                    if ( download.delete() ) {
                                        JOptionPane
                                                .showMessageDialog(
                                                        null,
                                                        "File was deleteed successfully!",
                                                        "Success",
                                                        JOptionPane.INFORMATION_MESSAGE );
                                    } else {
                                        JOptionPane.showMessageDialog( null,
                                                "File could not be deleted!",
                                                "Failure",
                                                JOptionPane.ERROR_MESSAGE );
                                    }
                                    break;
                                }
                                break;
                            }
                        } else {
                            switch ( UserPreferences.PREF_BEHAVIOR_DOWNLOAD_AGAIN ) {
                            case UserPreferences.OPTION_DOWNLOAD_AGAIN:
                                queueNecessary = true;
                                sessionDownloads.remove( download.getId() );
                                break;
                            case UserPreferences.OPTION_SKIP_DELETE:
                                download.delete();
                            case UserPreferences.OPTION_SKIP:
                                queueNecessary = false;
                                break;
                            }
                        }
                    } else {
                        queueNecessary = false;
                    }
                } else {
                    queueNecessary = false;
                }
            }
        }
        return queueNecessary;
    }

    protected void startDownload( final Download download ) {
        // Update target of the download
        download.setPath( UserPreferences.PREF_DOWNLOAD_TARGET );
        // Start download
        final Thread t = new Thread( download );
        t.start();
        // Update system tray icon
        ms.changeTrayIcon( true );

        Runnable statusUpdater = new Runnable() {
            @Override
            public void run() {
                try {
                    String status;
                    long prevSystemTime = System.currentTimeMillis(), currentSystemTime;
                    long prevDownloadedAmount = 0L, currentDownloadedAmount = 0L;
                    int rowNumber;
                    while ( t.isAlive() ) {
                        rowNumber = GuiOperations.getRowNumber( ms,
                                download.getId() );
                        if ( rowNumber >= 0 ) {
                            if ( download.getDownloadedAmount() == 0L ) {
                                status = "Starting download";
                                prevSystemTime = System.currentTimeMillis();
                            } else if ( download.isCanceled() ) {
                                status = "Canceling download";
                            } else if ( download.isCompleted() ) {
                                status = "Completing download";
                            } else if ( download.isPaused() ) {
                                status = GuiOperations
                                        .getReadableSize( currentDownloadedAmount )
                                        + " / "
                                        + GuiOperations
                                                .getReadableSize( download
                                                        .getTotalLength() )
                                        + " (Paused)";
                                download.setCurrentSpeed( 0.0 );
                            } else {
                                currentSystemTime = System.currentTimeMillis();
                                currentDownloadedAmount = download
                                        .getDownloadedAmount();
                                double downloadSpeed = ( ( currentDownloadedAmount - prevDownloadedAmount ) / 1048576.0 )
                                        / ( ( currentSystemTime - prevSystemTime ) / 1000.0 );
                                // Update current speed of download
                                download.setCurrentSpeed( downloadSpeed );
                                prevSystemTime = currentSystemTime;
                                prevDownloadedAmount = currentDownloadedAmount;
                                status = GuiOperations
                                        .getReadableSize( currentDownloadedAmount )
                                        + " / "
                                        + GuiOperations
                                                .getReadableSize( download
                                                        .getTotalLength() )
                                        + " ("
                                        + GuiOperations
                                                .roundTo2Decimals( downloadSpeed )
                                        + "MB/sn)";
                            }
                            ms.getItemTable().getModel()
                                    .setValueAt( status, rowNumber, 5 );
                        }
                        Thread.sleep( 1000L );
                    }
                } catch ( InterruptedException e ) {
                    // ignore
                } finally {
                    activeDownloads.remove( download.getId() );
                    int rowNumber = GuiOperations.getRowNumber( ms,
                            download.getId() );
                    if ( rowNumber >= 0 ) {
                        if ( !download.isFaulty() ) {
                            ms.getItemTable().getModel()
                                    .setValueAt( "Completed", rowNumber, 5 );
                            if ( !connection.refresh() || !download.delete() ) {
                                ms.getItemTable()
                                        .getModel()
                                        .setValueAt(
                                                "Completed (Couldn't be deleted)",
                                                GuiOperations.getRowNumber( ms,
                                                        download.getId() ), 5 );
                            }
                        } else {
                            if ( !download.isCanceled() ) {
                                ms.getItemTable().getModel()
                                        .setValueAt( "Error", rowNumber, 5 );
                            } else {
                                ms.getItemTable()
                                        .getModel()
                                        .setValueAt( "Canceled/Skipped",
                                                rowNumber, 5 );
                            }
                        }
                    }
                    // Update system tray icon
                    ms.changeTrayIcon( activeDownloads.size() > 0 );
                }
            }
        };

        Thread ts = new Thread( statusUpdater );
        ts.start();
    }
}