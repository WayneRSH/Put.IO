package communication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadPart implements Runnable {

    /** Status members */
    private long size;
    private long offset;
    private long downloadedAmount;
    private File file;
    private DownloadResumer downloadResumer;

    /** File output stream of this part */
    private FileOutputStream fileOS = null;
    /** Indicates if this download part faulty */
    private boolean isFaulty = false;

    private boolean isResuming = false;
    private boolean isCompleted = false;

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return isFaulty
     */
    public boolean isFaulty() {
        return isFaulty;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * @return the downloadedAmount
     */
    public long getDownloadedAmount() {
        return downloadedAmount;
    }

    /**
     * Constructor
     * 
     * @param size
     *            Part size
     */
    public DownloadPart( DownloadResumer downloadResumer, File file, long offset, long size ) {
        this.downloadResumer = downloadResumer;
        this.file = file;
        this.offset = offset;
        this.size = size;
        this.isCompleted = false;
    }

    @Override
    public void run() {
        try {
            // Check resuming
            if ( this.file.exists() ) {
                Long off = this.file.length();
                if ( off <= this.size ) {
                    this.isResuming = true;
                    this.offset += off;
                    this.downloadedAmount = off;
                    this.downloadResumer.addDownloadedAmount( this.downloadedAmount );
                }
            }

            if ( downloadedAmount == size )
                isCompleted = true;
            else {
                this.fileOS = new FileOutputStream( this.file, isResuming );

                URL url = new URL( downloadResumer.getRootDownload().getUrl().replace( " ", "%20" ) );
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                // Specify what part of file to download
                if ( this.offset > 0 )
                    conn.setRequestProperty( "Range", "bytes=" + offset + "-" );

                conn.connect();
                int responseCode = conn.getResponseCode();
                if ( responseCode == HttpURLConnection.HTTP_OK
                        || responseCode == HttpURLConnection.HTTP_PARTIAL ) {
                    byte[] tmp_buffer = new byte[ 4096 ];
                    InputStream is = conn.getInputStream();
                    int n;
                    while ( ( n = is.read( tmp_buffer ) ) > 0 && downloadedAmount < size ) {
                        if ( !downloadResumer.getRootDownload().getConnection().isConnected )
                            break;
                        if ( downloadResumer.getRootDownload().isCanceled() ) {
                            isFaulty = true;
                            break;
                        }
                        if ( downloadedAmount + n > size ) {
                            n = (int) ( size - downloadedAmount );
                        }
                        fileOS.write( tmp_buffer, 0, n );
                        fileOS.flush();
                        downloadedAmount += n;
                        downloadResumer.addDownloadedAmount( n );

                        // Wait if paused
                        while ( downloadResumer.getRootDownload().isPaused()
                                && !downloadResumer.getRootDownload().isCanceled() )
                            Thread.sleep( 1000 );
                    }
                    fileOS.close();
                    if ( downloadedAmount == size )
                        isCompleted = true;
                }
                else {
                    throw new IllegalStateException( "HTTP response: " + responseCode );
                }
            }
        }
        catch ( Exception e ) {
            isFaulty = true;
            StringWriter sw = new StringWriter();
            e.printStackTrace( new PrintWriter( sw ) );
            System.out.println( sw.toString() );
        }
        finally {
            if ( fileOS != null ) {
                try {
                    fileOS.close();
                }
                catch ( IOException e ) {
                    // Ignore
                }
            }
            if ( isFaulty ) {
                this.file.delete();
            }
        }
    }
}
