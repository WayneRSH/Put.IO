package communication;

import java.util.List;

import javax.swing.JOptionPane;

import api.Item;
import api.User;

public class Connection {
    /** User information */
    protected String oauth_token;

    /** Proxy related members */
    protected boolean proxyActive;
    protected String proxyAddress;
    protected String proxyPort;

    /** Connection related members */
    protected boolean isConnected;

    /** User related members */
    protected User user;

    /**
     * @return the mProxyActive
     */
    public boolean isProxyActive() {
        return proxyActive;
    }

    /**
     * 
     * @param proxyActive the mProxyActive to set
     * @throws Exception if proxy settings are not ok
     */
    public void setProxyActive(boolean proxyActive) throws Exception {
        this.proxyActive = proxyActive;
        if (proxyActive) {
            if (proxyAddress == null || proxyPort == null)
                throw new Exception("Proxy settings are not ok!");
            System.setProperty("http.proxySet", "true");
            System.setProperty("http.proxyHost", proxyAddress);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxySet", "true");
            System.setProperty("https.proxyHost", proxyAddress);
            System.setProperty("https.proxyPort", proxyPort);
        } else {
            System.setProperty("http.proxySet", "false");
            System.setProperty("http.proxyHost", "");
            System.setProperty("http.proxyPort", "");
            System.setProperty("https.proxySet", "false");
            System.setProperty("https.proxyHost", "");
            System.setProperty("https.proxyPort", "");
        }
    }

    /**
     * @return the mProxyAddress
     */
    public String getProxyAddress() {
        return proxyAddress;
    }

    /**
     * @param mProxyAddress the mProxyAddress to set
     */
    public void setProxyAddress(String mProxyAddress) {
        this.proxyAddress = mProxyAddress;
    }

    /**
     * @return the mProxyPort
     */
    public String getProxyPort() {
        return proxyPort;
    }

    /**
     * @param proxyPort the mProxyPort to set
     */
    public void setProxyPort(String proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @return mUser
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the isConnected
     */
    public boolean isConnected() {
        return isConnected;
    }

    public boolean connect() {
        if (oauth_token != null) {
            try {
                RequestorHolder.getRequestor().setThreadCredentials(oauth_token);
                user = new User().info();
                isConnected = true;
            } catch (Exception e) {
                disconnect();
                JOptionPane.showMessageDialog(null, "Connection error !\nTry again later.", "Error", JOptionPane.ERROR_MESSAGE);
                System.out.println(e.toString());
            }
        }
        return isConnected;
    }

    public boolean refresh() {
        if (oauth_token != null) {
            try {
                RequestorHolder.getRequestor().setThreadCredentials(oauth_token);
                user = new User().info();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        return isConnected;
    }

    public void disconnect() {
        RequestorHolder.getRequestor().setThreadCredentials(null);
        isConnected = false;
    }

    public List<Item> getRootItems() throws Exception {
        try {
            return new Item().listAll();
        } catch (Exception e) {
            throw e;
        }
    }
    
    public List<Item> getChildren(Item item) throws Exception {
        try {
            return item.listChildren();
        } catch (Exception e) {
            throw e;
        }
    }
}