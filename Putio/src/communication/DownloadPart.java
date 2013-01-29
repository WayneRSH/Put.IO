package communication;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadPart implements Runnable {
    /** Download part id */
    private int id;
    /** Main download which this part belongs to */
    private Download rootDownload;

    /** Status members */
    private long size;
    private long offset;
    private long downloadedAmount;

    /** Download path of this part */
    private String path;
    /** File output stream of this part */
    private FileOutputStream fileOS = null;
    /** Indicates if this download part faulty */
    private boolean isFaulty = false;

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
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

    /**
     * @return the downloadedAmount
     */
    public long getDownloadedAmount() {
        return downloadedAmount;
    }

    /**
     * Constructor
     * @param size Part size
     */
    public DownloadPart(int id, Download download, long offset, long size) {
	this.id = id;
	this.rootDownload = download;
	this.offset = offset;
	this.size = size;
	this.path = download.getPath() + "\\" + download.getFileName() + ".piodm" + id;
    }

    @Override
    public void run() {
	try {
	    fileOS = new FileOutputStream(path);
	    URL url = new URL(rootDownload.getUrl().replace(" ", "%20"));
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    // Specify what part of file to download
	    conn.setRequestProperty("Range", "bytes=" + offset + "-");
	    conn.connect();
	    int responseCode = conn.getResponseCode();
	    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL) {
		byte tmp_buffer[] = new byte[4096];
		InputStream is = conn.getInputStream();
		int n;
		while ((n = is.read(tmp_buffer)) > 0 && downloadedAmount < size) {
		    if (!rootDownload.getConnection().isConnected || rootDownload.isCanceled()) {
			isFaulty = true;
			break;
		    }
		    if (downloadedAmount + n > size) {
			n = (int) (size - downloadedAmount);
		    }
		    fileOS.write(tmp_buffer, 0, n);
		    fileOS.flush();
		    downloadedAmount += n;
		    rootDownload.setDownloadedAmount(rootDownload.getDownloadedAmount() + n);
		    // Wait if paused
		    while (rootDownload.isPaused() && !rootDownload.isCanceled())
			Thread.sleep(1000);
		}
	    } else {
		throw new IllegalStateException("HTTP response: " + responseCode);
	    }
	} catch (Exception e) {
	    isFaulty = true;
	    StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));
	    System.out.println(sw.toString());
	} finally {
	    if (fileOS != null)
		try {
		    fileOS.close();
		} catch (IOException e) {
		    // Ignore
		}
	}
    }
}