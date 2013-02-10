package communication;

import gui.LeafNode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import util.GuiOperations;
import util.UserPreferences;

import api.Item;

public class Download implements Runnable {

    /** Download id counter */
    private static int downloadIdCounter = 0;

    /** Corresponding put.io item */
    private Item item;

    /** Corresponding leaf in tree */
    private LeafNode leaf;

    /** Global connection */
    private Connection connection;

    /** Download related members */
    private String filename;
    private String path;
    private String url;
    private String token;
    private String contentType;
    private String putioId;

    /** Status members */
    private long totalLength;
    private long downloadedAmount;

    /** Current download speed in MB */
    private double currentSpeed;
    private ArrayList<Double> avgSpeedList = new ArrayList<Double>( 1 );
    private double currentAvgSpeed;

    /** Download id */
    private int id;
    /** Indicates if download runs */
    private boolean isActive;
    /** Indicates if download is completed */
    private boolean isCompleted;
    /** Indicates if there is an error */
    private boolean isFaulty;
    /** Indicates if download is canceled */
    private boolean isCanceled = false;
    /** Indicates if download is paused */
    private boolean isPaused = false;

    /** Download resume */
    private DownloadResumer downResume;
    /** Download resume thread */
    private Thread downResThread;
    
    private String errorMsg;

    /**
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the leaf
     */
    public LeafNode getLeaf() {
        return leaf;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return filename;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName( String fileName ) {
        this.filename = fileName;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }
    
    public String getItemPath() {
        return (path + File.separator + filename);
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath( String path ) {
        this.path = path;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            the url to set
     */
    public void setUrl( String url ) {
        this.url = url;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setToken( String token ) {
        this.token = token;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType
     *            the contentType to set
     */
    public void setContentType( String contentType ) {
        this.contentType = contentType;
    }

    /**
     * @return the putioId
     */
    public String getPutioId() {
        return putioId;
    }

    /**
     * @return the totalLength
     */
    public long getTotalLength() {
        return totalLength;
    }

    /**
     * @return the downloadedAmount
     */
    public long getDownloadedAmount() {
        return downloadedAmount;
    }

    /**
     * @param downloadedAmount
     *            the downloadedAmount to set
     */
    public void setDownloadedAmount( long downloadedAmount ) {
        this.downloadedAmount = downloadedAmount;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public synchronized double getCurrentAvgSpeed() {
        return this.currentAvgSpeed;
    }

    public synchronized void setCurrentSpeed( double currentSpeed ) {
        this.currentSpeed = currentSpeed;
        
        if ( !isPaused ) {
            if ( this.avgSpeedList.size() == 39
                    || ( this.avgSpeedList.size() == 2 && ( this.avgSpeedList.get( 0 ) - this.avgSpeedList
                            .get( 1 ) ) > 100 ) )
                this.avgSpeedList.remove( 0 );
            this.avgSpeedList.add( currentSpeed );
            double avgSpeed = 0D;
            for ( int i = 0; i < this.avgSpeedList.size(); i++ ) {
                // Weighted average (the older the lesser)
                avgSpeed += this.avgSpeedList.get( i )
                        * ( (double) ( i + 1 ) / ( (double) ( this.avgSpeedList.size() + 1 ) / 2 ) );
            }
            avgSpeed /= this.avgSpeedList.size();
            this.currentAvgSpeed = avgSpeed;
        }
        else
            this.currentAvgSpeed = 0;
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @return the isActive
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * @return the isCompleted
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * @return the isFaulty
     */
    public boolean isFaulty() {
        return isFaulty;
    }

    /**
     * @return the isCanceled
     */
    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * @return the isCanceled
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    public boolean isResuming() {
        return this.downResume.isResuming();
    }
    
    public String getErrorMsg() {
        return this.errorMsg;
    }

    public Download( String token, Item item, LeafNode leaf, String path, Connection connection ) {
        this.token = token;
        this.item = item;
        this.leaf = leaf;
        this.filename = item.getName();
        this.path = path;
        this.url = item.getDownloadUrl( token );
        this.contentType = item.getContentType();
        this.totalLength = item.getSize();
        this.putioId = item.getId();
        this.downloadedAmount = 0;
        this.id = getNextFreeId();
        this.isActive = false;
        this.isCompleted = false;
        this.isFaulty = false;
        this.connection = connection;
    }

    private static int getNextFreeId() {
        return downloadIdCounter++;
    }

    private void startDownload() throws MalformedURLException, IOException {
        try {
            isActive = true;
            // Creating directories
            String dirs = leaf.getPathDir().getDirs();
            if ( dirs != null ) {
                File fDirs = new File( path + File.separator + dirs );
                if ( !fDirs.exists() ) {
                    if ( !fDirs.mkdirs() )
                        throw new Exception( "Unable to create dirs " + dirs );
                }
                path += File.separator + dirs;
            }

            File f = new File( path + File.separator + filename );
            if ( f.exists() && f.isFile() ) {
                if ( !UserPreferences.PREF_DONT_ASK_OVERWRITE ) {
                    switch ( JOptionPane.showConfirmDialog( null,
                            "This file already exists on disc. Would you like to overwrite it? ("
                                    + GuiOperations.getReadableSize( f.length() ) + " on disc / "
                                    + GuiOperations.getReadableSize( totalLength ) + " on server)",
                            "Overwrite (" + filename + ")", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE ) ) {
                    case JOptionPane.NO_OPTION:
                        isCompleted = true;
                        leaf.setDownPerc( 1.0f );
                        throw new Exception( "File already exists!" );
                    }
                }
                else {
                    switch ( UserPreferences.PREF_BEHAVIOR_OVERWRITE ) {
                    case UserPreferences.OPTION_OVERWRITE:
                        break;
                    case UserPreferences.OPTION_SKIP:
                        isCompleted = true;
                        leaf.setDownPerc( 1.0f );
                        throw new Exception( "File already exists!" );
                    case UserPreferences.OPTION_SKIP_DELETE:
                        isCompleted = true;
                        connection.refresh();
                        delete();
                        throw new Exception( "File already exists!" );
                    case UserPreferences.OPTION_SKIP_SAME_SIZE:
                        if ( f.length() == totalLength ) {
                            isCompleted = true;
                            leaf.setDownPerc( 1.0f );
                            throw new Exception( "File already exists with same size!" );
                        }
                        break;
                    case UserPreferences.OPTION_SKIP_SAME_SIZE_DELETE:
                        if ( f.length() == totalLength ) {
                            isCompleted = true;
                            connection.refresh();
                            delete();
                            throw new Exception( "File already exists with same size!" );
                        }
                        break;
                    }
                }
            }

            downResume = new DownloadResumer( this, totalLength, UserPreferences.PREF_DOWNLOAD_PART_COUNT );
            downResThread = new Thread( downResume );
            downResThread.start();

            while ( loopResume() && !isFaulty ) {
                Thread.sleep( 1000 );
            }
            isCompleted = downResume.isCompleted();

            // Delete file in case of error
            if ( isFaulty ) {
                f.delete();
                this.errorMsg = downResume.getErrorMsg();
            }
            isActive = false;
        }
        catch ( Exception e ) {
            isActive = false;
            //System.out.println( e.toString() );
        }
    }

    private boolean loopResume() {
        boolean retVal = false;
        retVal |= downResThread.isAlive();
        isFaulty |= downResume.isFaulty();
        return retVal;
    }

    public boolean delete() {
        try {
            item.delete();
            return true;
        }
        catch ( Exception e ) {
            return false;
        }
    }

    public void cancel() {
        isCanceled = true;
        isPaused = false;
    }
    
    public void retry() {
        isCanceled = false;
        isPaused = false;
        isFaulty = false;
        isCompleted = false;
    }

    public void pause() {
        if (!isCanceled)
            isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    @Override
    public void run() {
        try {
            startDownload();
        }
        catch ( Exception e ) {
        }
    }
    
    @Override
    public boolean equals( Object object ) {
        boolean same = false;
        if ( object != null && object instanceof Download ) {
            same = this.item.getId().equals( ( (Download) object ).getItem().getId() );
        }

        return same;
    }
}
