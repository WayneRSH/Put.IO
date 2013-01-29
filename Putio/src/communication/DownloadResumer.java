package communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

public class DownloadResumer implements Runnable {
    /** Main download which this part belongs to */
    private Download rootDownload;

    /** Status members */
    private long size;
    private long offset;
    private long downloadedAmount;

    /** Download path of this part */
    private String path;
    /** Resume info of this download */
    private String resumePath;
    /** File output stream of this part */
    private FileOutputStream fileOS = null;
    /** Indicates if this download part faulty */
    private boolean isFaulty = false;
    /** Indicates if we must resume */
    private boolean isResuming = false;

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * @return the resume info path
     */
    public String getResumePath() {
        return resumePath;
    }

    /**
     * @return the rootDownload
     */
    public Download getRootDownload() {
        return rootDownload;
    }

    /**
     * @return isFaulty
     */
    public boolean isFaulty() {
        return isFaulty;
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
    public DownloadResumer( Download download, long size ) {
        this.rootDownload = download;
        this.size = size;
        this.offset = 0;
        this.path = download.getPath() + "\\" + download.getFileName();
        this.resumePath = this.path + ".piores"; 
        this.path += ".piodm";
        // Check if we must resume
        File f = new File( this.resumePath );
        if ( f.exists() && f.isFile() ) {
            try {
                byte[] buff = new byte[ 8 ];
                FileInputStream fis = new FileInputStream( f );
                int n = 0;
                if ( ( n = fis.read( buff ) ) > 0 ) {
                    System.out.println( "Read:" + n + " Data:" + buff );
                    // buff should contain the offset
                    ByteBuffer bb = ByteBuffer.wrap( buff );
                    this.offset = bb.getLong();
                    System.out.println( "Offset:" + this.offset );
                }
                isResuming = true;
            } catch ( IOException e ) {
                isFaulty = true;
                StringWriter sw = new StringWriter();
                e.printStackTrace( new PrintWriter( sw ) );
                System.out.println( sw.toString() );
            }
        }
    }

    @Override
    public void run() {
        try {
            // For the offset
            FileOutputStream fiOff = new FileOutputStream( resumePath );
            byte[] byteOffset = new byte[ 8 ];
            
            URL url = new URL( rootDownload.getUrl().replace( " ", "%20" ) );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // Specify what part of file to download
            if (offset > 0)
                conn.setRequestProperty( "Range", "bytes=" + offset + "-" );
            else {
                byteOffset = ByteBuffer.allocate( 8 ).putLong( offset ).array();
                fiOff.write( byteOffset );
            }
            // We can then append
            fileOS = new FileOutputStream( resumePath, isResuming );
            
            conn.connect();
            int responseCode = conn.getResponseCode();
            if ( responseCode == HttpURLConnection.HTTP_OK
                    || responseCode == HttpURLConnection.HTTP_PARTIAL ) {
                byte[] tmp_buffer = new byte[ 4096 ];
                InputStream is = conn.getInputStream();
                int n;
                while ( ( n = is.read( tmp_buffer ) ) > 0
                        && downloadedAmount < size ) {
                    if ( !rootDownload.getConnection().isConnected
                            || rootDownload.isCanceled() ) {
                        isFaulty = true;
                        break;
                    }
                    if ( downloadedAmount + n > size ) {
                        n = (int) ( size - downloadedAmount );
                    }
                    fileOS.write( tmp_buffer, 0, n );
                    fileOS.flush();
                    downloadedAmount += n;
                    rootDownload.setDownloadedAmount( rootDownload
                            .getDownloadedAmount() + n );
                    // Wait if paused
                    while ( rootDownload.isPaused()
                            && !rootDownload.isCanceled() )
                        Thread.sleep( 1000 );
                }
            } else {
                throw new IllegalStateException( "HTTP response: "
                        + responseCode );
            }
        } catch ( Exception e ) {
            isFaulty = true;
            StringWriter sw = new StringWriter();
            e.printStackTrace( new PrintWriter( sw ) );
            System.out.println( sw.toString() );
        } finally {
            if ( fileOS != null )
                try {
                    fileOS.close();
                } catch ( IOException e ) {
                    // Ignore
                }
        }
    }
}