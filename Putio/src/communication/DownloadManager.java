package communication;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import javax.swing.JOptionPane;

import gui.ItemPanel.LeafNode;
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
    private Boolean mustRefresh = false;

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
                    if ( Math.abs( timeSinceUpdate ) >= UserPreferences.PREF_SERVER_CHECK_INTERVAL * 1000 || mustRefresh ) {
                        connection.refresh();
                        ms.setStatus( "Connected as "
                                + connection.getUser().getName()
                                + " - Available Quota: "
                                + GuiOperations.getReadableSize( connection
                                        .getUser().getDiskQuotaAvailable() ) );
                        refreshQueue();
                        lastUpdateTime = System.currentTimeMillis();
                        mustRefresh = false;
                    }
                    if ( UserPreferences.PREF_AUTO_DOWNLOAD ) {
                        while ( activeDownloads.size() < UserPreferences.PREF_MAX_DOWNLOADS
                                && downloadQueue.size() > 0 ) {
                            Download d = downloadQueue.poll();
                            if ( !UserPreferences.PREF_FILE_SIZE_CHECK ) {
                                activeDownloads.put( d.getId(), d );
                                startDownload( d );
                            }
                            else {
                                if ( d.getTotalLength() >= ( (long) ( UserPreferences.PREF_FILE_SIZE_FOR_CHECK * 1048576 ) ) ) {
                                    activeDownloads.put( d.getId(), d );
                                    startDownload( d );
                                }
                                else {
//                                    ms.getItemTable()
//                                            .getModel()
//                                            .setValueAt(
//                                                    "Skipped",
//                                                    GuiOperations.getRowNumber(
//                                                            ms, d.getId() ), 5 );
//                                    if ( UserPreferences.PREF_FILE_SIZE_DELETE ) {
//                                        connection.refresh();
//                                        if ( d.delete() ) {
//                                            ms.getItemTable()
//                                                    .getModel()
//                                                    .setValueAt(
//                                                            "Skipped (Deleted)",
//                                                            GuiOperations
//                                                                    .getRowNumber(
//                                                                            ms,
//                                                                            d.getId() ),
//                                                            5 );
//                                        }
//                                        else {
//                                            ms.getItemTable()
//                                                    .getModel()
//                                                    .setValueAt(
//                                                            "Skipped (Delete failed!)",
//                                                            GuiOperations
//                                                                    .getRowNumber(
//                                                                            ms,
//                                                                            d.getId() ),
//                                                            5 );
//                                        }
//                                    }
                                }
                            }
                        }
                    }
                    // Update total download speed displays in GUI
                    totalSpeed = 0.0;
                    Iterator<Download> downloads = activeDownloads.values()
                            .iterator();
                    while ( downloads.hasNext() ) {
                        totalSpeed += downloads.next().getCurrentAvgSpeed();
                    }
                    ms.updateCurrentDownloadSpeed( GuiOperations
                            .getReadableFromMBSize( totalSpeed ) + "/s" );
                }
                else {
                    ms.updateCurrentDownloadSpeed( "0.0 MB/s" );
                }
                Thread.sleep( 200L );
                ms.getItemPanel().updateDownload();
                Thread.sleep( 200L );
            }
            catch ( InterruptedException e ) {
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
                        ms.cleanItemPanel();
                        refreshQueue();
                        lastUpdateTime = System.currentTimeMillis();
                    }
                }
                else {
                    ms.setStatus( "Connection Error" );
                    ms.displayAsConnected( false );
                    ms.cleanItemPanel();
                }
            }
        } );
        connectionThread.start();
    }

    public void disconnect() {
        ms.getItemPanel().stopOp();
        connection.disconnect();
        ms.setStatus( "Not connected" );
        ms.displayAsConnected( false );
        ms.cleanItemPanel();
    }

    public void refreshQueue() {
        try {
            ms.getItemPanel().populateTree( connection );
            List<Item> items = ms.getItemPanel().getItems();
            //System.out.println( items );
            if ( items != null ) {
                ListIterator<Item> i = items.listIterator();
                while ( i.hasNext() ) {
                    Item currentItem = i.next();
                    addItemsToQueue( currentItem );
                }
            }
        }
        catch ( Exception e ) {
            System.out.println( e.toString() );
        }
    }
    
    public void refresh() {
        ms.getItemPanel().stopOp();
        this.mustRefresh = true;
    }

    private void addItemsToQueue( Item item ) {
        if ( isQueueNecessary( item ) ) {
            LeafNode leaf = (LeafNode) ms.getItemPanel().getItemInTree( item );
            Download d = new Download( UserPreferences.PREF_USERTOKEN, item,
                    leaf, UserPreferences.PREF_DOWNLOAD_TARGET, connection );
            downloadQueue.add( d );
            sessionDownloads.put( d.getId(), d );
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
                                                        "File was deleted successfully!",
                                                        "Success",
                                                        JOptionPane.INFORMATION_MESSAGE );
                                    }
                                    else {
                                        JOptionPane.showMessageDialog( null,
                                                "File could not be deleted!",
                                                "Failure",
                                                JOptionPane.ERROR_MESSAGE );
                                    }
                                    break;
                                }
                                break;
                            }
                        }
                        else {
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
                    }
                    else {
                        queueNecessary = false;
                        break;
                    }
                }
                // Resume
                else if ( !download.isCompleted() && !download.isFaulty() && !download.isActive() ) {
                    queueNecessary = true;
                    sessionDownloads.remove( download.getId() );
                    break;
                }
                else {
                    queueNecessary = false;
                    break;
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
                    float downloadPercent = 0.0f;
                    long prevSystemTime = System.currentTimeMillis(), currentSystemTime;
                    long prevDownloadedAmount = download.getDownloadedAmount();
                    long currentDownloadedAmount = download.getDownloadedAmount();
                    LeafNode leaf;
                    while ( t.isAlive() ) {
                        leaf = GuiOperations.getLeaf( ms, download.getItem() );
                        if ( leaf != null ) {
                            if ( download.getDownloadedAmount() == 0L ) {
                                status = "Starting download";
                                prevSystemTime = System.currentTimeMillis();
                            }
                            else if ( download.isCanceled() ) {
                                status = "Canceling download";
                            }
                            else if ( download.isCompleted() ) {
                                status = "Completing download";
                            }
                            else if ( download.isPaused() ) {
                                status = GuiOperations
                                        .getReadableSize( currentDownloadedAmount )
                                        + " / "
                                        + GuiOperations
                                                .getReadableSize( download
                                                        .getTotalLength() )
                                        + " (Paused)";
                                download.setCurrentSpeed( 0.0 );
                            }
                            else {
                                currentSystemTime = System.currentTimeMillis();
                                currentDownloadedAmount = download.getDownloadedAmount();
                                if (currentSystemTime == prevSystemTime)
                                    currentSystemTime++;
                                double downloadSpeed = ( ( currentDownloadedAmount - prevDownloadedAmount ) / 1048576.0 )
                                        / ( ( currentSystemTime - prevSystemTime ) / 1000.0 );
                                // Update current speed of download
                                download.setCurrentSpeed( downloadSpeed );
                                double timeRemaining = Math.round( ( download.getTotalLength() - currentDownloadedAmount ) / ( download.getCurrentAvgSpeed() * 1048.576 ) );
                                int seconds = (int) (timeRemaining / 1000) % 60 ;
                                int minutes = (int) ((timeRemaining / (1000*60)) % 60);
                                int hours   = (int) ((timeRemaining / (1000*60*60)) % 24);
                                String timeRemainingTxt = String.format("%02d:%02d:%02d",
                                        hours,
                                        minutes,
                                        seconds
                                    );
                                String readableDownloadSpeed = GuiOperations
                                        .getReadableFromMBSize( download.getCurrentAvgSpeed() );
                                prevSystemTime = currentSystemTime;
                                prevDownloadedAmount = currentDownloadedAmount;
                                downloadPercent = (float) currentDownloadedAmount
                                        / (float) download.getTotalLength();
                                status = GuiOperations
                                        .getReadableSize( currentDownloadedAmount )
                                        + " / "
                                        + GuiOperations
                                                .getReadableSize( download
                                                        .getTotalLength() )
                                        + " (" + readableDownloadSpeed + "/s)"
                                        + " ETA : " + timeRemainingTxt;
                            }
                            leaf.setDownPerc( downloadPercent );
                            leaf.setStatus( status );
                        }
                        Thread.sleep( 1000L );
                    }
                }
                catch ( Exception e ) {
                    // ignore
                    StringWriter sw = new StringWriter();
                    e.printStackTrace( new PrintWriter( sw ) );
                    System.out.println( sw.toString() );
                }
                finally {
                    activeDownloads.remove( download.getId() );
                    LeafNode leaf = GuiOperations.getLeaf( ms,
                            download.getItem() );
                    if ( leaf != null ) {
                        if ( !download.isFaulty() ) {
                            leaf.setStatus( "Completed" );
                            leaf.setDownPerc( 1.0f );
                            if ( !connection.refresh() || !download.delete() ) {
                                leaf.setStatus( "Completed (Couldn't be deleted)" );
                            }
                        }
                        else {
                            if ( !download.isCanceled() ) {
                                leaf.setStatus( "Error" );
                            }
                            else {
                                leaf.setStatus( "Canceled/Skipped" );
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
