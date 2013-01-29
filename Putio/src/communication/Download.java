package communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;

import javax.swing.JOptionPane;

import util.GuiOperations;
import util.UserPreferences;

import api.Item;

public class Download implements Runnable {

    /** Download id counter */
    private static int downloadIdCounter = 0;

    /** Corresponding put.io item */
    private Item item;

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

    /** Download parts */
    private DownloadPart[] parts;
    /** Download part threads */
    private Thread[] partThreads;

    /**
     * @return the item
     */
    public Item getItem() {
        return item;
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

    public void setCurrentSpeed( double currentSpeed ) {
        this.currentSpeed = currentSpeed;
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

    /**
     * @return Download parts
     */
    public DownloadPart[] getParts() {
        return parts;
    }

    public Download( String token, Item item, String path,
            Connection connection ) {
        this.token = token;
        this.item = item;
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
//            Authenticator.setDefault( new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication( username, password
//                            .toCharArray() );
//                }
//            } );
            File f = new File( path + "\\" + filename );
            if ( f.exists() && f.isFile() ) {
                if ( !UserPreferences.PREF_DONT_ASK_OVERWRITE ) {
                    switch ( JOptionPane
                            .showConfirmDialog(
                                    null,
                                    "This file already exists on disc. Would you like to overwrite it? ("
                                            + GuiOperations.getReadableSize( f
                                                    .length() )
                                            + " on disc / "
                                            + GuiOperations
                                                    .getReadableSize( totalLength )
                                            + " on server)", "Overwrite ("
                                            + filename + ")",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE ) ) {
                    case JOptionPane.NO_OPTION:
                        cancel();
                        throw new Exception( "File already exists!" );
                    }
                } else {
                    switch ( UserPreferences.PREF_BEHAVIOR_OVERWRITE ) {
                    case UserPreferences.OPTION_OVERWRITE:
                        break;
                    case UserPreferences.OPTION_SKIP:
                        cancel();
                        throw new Exception( "File already exists!" );
                    case UserPreferences.OPTION_SKIP_DELETE:
                        cancel();
                        connection.refresh();
                        delete();
                        throw new Exception( "File already exists!" );
                    case UserPreferences.OPTION_SKIP_SAME_SIZE:
                        if ( f.length() == totalLength ) {
                            cancel();
                            throw new Exception(
                                    "File already exists with same size!" );
                        }
                        break;
                    case UserPreferences.OPTION_SKIP_SAME_SIZE_DELETE:
                        if ( f.length() == totalLength ) {
                            cancel();
                            connection.refresh();
                            delete();
                            throw new Exception(
                                    "File already exists with same size!" );
                        }
                        break;
                    }
                }
            }

            partThreads = new Thread[ UserPreferences.PREF_DOWNLOAD_PART_COUNT ];
            parts = new DownloadPart[ UserPreferences.PREF_DOWNLOAD_PART_COUNT ];
            long regularSize = Math.round( (double) totalLength
                    / UserPreferences.PREF_DOWNLOAD_PART_COUNT );
            long sizeOfLastPart = ( totalLength - regularSize
                    * ( UserPreferences.PREF_DOWNLOAD_PART_COUNT - 1 ) );
            for ( int i = 0; i < partThreads.length; i++ ) {
                if ( i != partThreads.length - 1 ) {
                    parts[ i ] = new DownloadPart( i + 1, this,
                            ( i * regularSize ), regularSize );
                } else {
                    parts[ i ] = new DownloadPart( i + 1, this,
                            ( i * regularSize ), sizeOfLastPart );
                }
                partThreads[ i ] = new Thread( parts[ i ] );
                partThreads[ i ].start();
            }
            while ( loopOverAllParts() && !isFaulty ) {
                // Wait until all parts are finished
                Thread.sleep( 1000 );
            }
            isCompleted = true;
            // Merge parts
            FileOutputStream fout = new FileOutputStream( f.getPath() );
            File partFile;
            FileInputStream finp;
            int length;
            byte[] buff = new byte[ 8000 ];
            for ( int i = 0; i < parts.length; i++ ) {
                partFile = new File( parts[ i ].getPath() );
                if ( !isFaulty ) {
                    finp = new FileInputStream( partFile );
                    while ( ( length = finp.read( buff ) ) > 0 ) {
                        fout.write( buff, 0, length );
                    }
                    finp.close();
                }
                partFile.delete();
            }
            fout.close();
            // Delete file in case of error
            if ( isFaulty )
                f.delete();
            isActive = false;
        } catch ( Exception e ) {
            isFaulty = true;
            StringWriter sw = new StringWriter();
            e.printStackTrace( new PrintWriter( sw ) );
            System.out.println( sw.toString() );
        }
    }

    /**
     * Loops over all download parts and runs routine tasks
     * 
     * @return FALSE if none of the parts is running
     */
    private boolean loopOverAllParts() {
        boolean retVal = false;
        for ( int i = 0; i < parts.length; i++ ) {
            retVal |= partThreads[ i ].isAlive();
            isFaulty |= parts[ i ].isFaulty();
        }
        return retVal;
    }

    public boolean delete() {
        try {
            item.delete();
            return true;
        } catch ( Exception e ) {
            return false;
        }
    }

    public void cancel() {
        isCanceled = true;
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    @Override
    public void run() {
        try {
            startDownload();
        } catch ( Exception e ) {
        }
    }
}