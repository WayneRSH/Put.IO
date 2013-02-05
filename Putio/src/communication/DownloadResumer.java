package communication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class DownloadResumer implements Runnable {
    /** Main download which this part belongs to */
    private Download rootDownload;

    /** Status members */
    private long size;
    private long downloadedAmount;
    private boolean resuming;

    /** Download path of this part */
    private String path;
    /** Indicates if this download part faulty */
    private boolean isFaulty = false;

    private Thread[] partThreads;
    private DownloadPart[] parts;
    private int numberOfParts;
    private boolean isCompleted = false;

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

    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * @return the downloadedAmount
     */
    public long getDownloadedAmount() {
        return downloadedAmount;
    }

    public void addDownloadedAmount( long partDownloadedAmount ) {
        this.downloadedAmount += partDownloadedAmount;
        this.rootDownload.setDownloadedAmount( this.downloadedAmount );
    }

    public boolean isResuming() {
        return this.resuming;
    }

    /**
     * Constructor
     * 
     * @param size
     *            Part size
     */
    public DownloadResumer( Download download, long size, int defaultParts ) throws Exception {
        this.rootDownload = download;
        this.size = size;
        this.path = download.getPath() + File.separator + download.getFileName();
        this.downloadedAmount = 0L;
        this.isCompleted = false;

        // Checking existing parts
        int i = 0;
        while ( true ) {
            File fDown = new File( this.path + ".pio" + i );
            if ( !fDown.exists() )
                break;
            else
                this.resuming = true;
            i++;
        }

        // Parts found, we use the initial number of parts for resuming the
        // download
        if ( i > 0 )
            this.numberOfParts = i;
        else
            this.numberOfParts = defaultParts;
    }

    @Override
    public void run() {
        try {
            int i;
            this.partThreads = new Thread[ this.numberOfParts ];
            this.parts = new DownloadPart[ this.numberOfParts ];
            long regularSize = Math.round( (double) this.size / this.numberOfParts );
            long sizeOfLastPart = ( this.size - regularSize * ( this.numberOfParts - 1 ) );
            for ( i = 0; i < this.partThreads.length; i++ ) {
                if ( i != this.partThreads.length - 1 ) {
                    this.parts[ i ] = new DownloadPart( this, new File( path + ".pio" + i ),
                            ( i * regularSize ), regularSize );
                }
                else {
                    this.parts[ i ] = new DownloadPart( this, new File( path + ".pio" + i ),
                            ( i * regularSize ), sizeOfLastPart );
                }
                this.partThreads[ i ] = new Thread( this.parts[ i ] );
                this.partThreads[ i ].start();
            }
            while ( loopOverAllParts() && !this.isFaulty ) {
                Thread.sleep( 1000 );
            }

            // Merge parts
            if ( !isFaulty && isCompleted ) {
                FileOutputStream fout = new FileOutputStream( this.path );
                File partFile;
                FileInputStream finp;
                int length;
                byte[] buff = new byte[ 8000 ];
                for ( i = 0; i < this.parts.length; i++ ) {
                    partFile = new File( this.path + ".pio" + i );
                    if ( !this.isFaulty ) {
                        finp = new FileInputStream( partFile );
                        while ( ( length = finp.read( buff ) ) > 0 ) {
                            fout.write( buff, 0, length );
                        }
                        finp.close();
                    }
                    partFile.delete();
                }
                fout.close();
            }
        }
        catch ( Exception e ) {
            this.isFaulty = true;
            StringWriter sw = new StringWriter();
            e.printStackTrace( new PrintWriter( sw ) );
            System.out.println( sw.toString() );
        }
    }

    private boolean loopOverAllParts() {
        boolean retVal = false;
        boolean complete = true;
        for ( int i = 0; i < this.parts.length; i++ ) {
            retVal |= this.partThreads[ i ].isAlive();
            this.isFaulty |= this.parts[ i ].isFaulty();
            complete &= this.parts[ i ].isCompleted();
        }
        this.isCompleted = complete;
        return retVal;
    }
}
