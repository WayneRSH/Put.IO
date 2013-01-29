package util;

import java.awt.Desktop;
import java.io.File;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import api.Item;

import communication.Download;
import gui.ItemPanel;
import gui.MainScreen;
import gui.ItemPanel.LeafNode;

public class GuiOperations {
    public static void connect(MainScreen ms) {
        if (UserPreferences.PREF_USE_PROXY) {
            ms.getDownloadManager().getConnection().setProxyAddress(UserPreferences.PREF_PROXY_ADDRESS);
            ms.getDownloadManager().getConnection().setProxyPort(UserPreferences.PREF_PROXY_PORT);
            try {
                ms.getDownloadManager().getConnection().setProxyActive(true);
            } catch (Exception ex) {
                ms.setStatus("Proxy settings are not ok!");
            }
        } else {
            try {
                ms.getDownloadManager().getConnection().setProxyActive(false);
            } catch (Exception ex) {
                ms.setStatus("Proxy settings are not ok!");
            }
        }
        ms.getDownloadManager().connect();
    }

    public static void disconnect(MainScreen ms) {
        ms.getDownloadManager().disconnect();
    }

    public static String getReadableSize(long size) {
        String unit;
        double retVal;
        if (size < 1048576L) {
            retVal = size / 1024.0;
            unit = " KB";
        } else if (size < 1073741824L) {
            retVal = size / 1048576.0;
            unit = " MB";
        } else {
            retVal = size / 1073741824.0;
            unit = " GB";
        }
        return roundTo2Decimals(retVal) + unit;
    }

    public static double roundTo2Decimals(double value) {
        double result = value * 10;
        result = Math.round(result);
        result /= 10;
        return result;
    }

    public static int getRowNumber(MainScreen ms, int downloadId) {
        for (int i = 0; i < ms.getItemTable().getModel().getRowCount(); i++) {
            if (((Integer) ms.getItemTable().getModel().getValueAt(i, 0)) == downloadId)
                return i;
        }
        return -1;
    }
    
    public static LeafNode getLeaf(MainScreen ms, Item item) {
        DefaultMutableTreeNode node = ms.getItemsPanel().getItemInTree( item );
        if ( node != null && node instanceof LeafNode )
            return ( LeafNode ) node;
        return null;
    }

    public static void cleanDownloadedItems(ItemPanel it) {
        /*
        int downloadId;
        Download download;
        for (int i = ms.getItemTable().getModel().getRowCount() - 1; i >= 0; i--) {
            downloadId = (Integer) ms.getItemTable().getModel().getValueAt(i, 0);
            download = ms.getDownloadManager().getSessionDownloads().get(downloadId);
            if (download == null || (download != null && (download.isCompleted() || download.isCanceled()))) {
                ((DefaultTableModel) ms.getItemTable().getModel()).removeRow(i);
            }
        }
        */
    }

    public static void pauseOrResumeSelectedItem(ItemPanel it) {
        /*
        int selectedIndex = ms.getItemTable().getSelectedRow();
        if (selectedIndex >= 0) {
            Download download = ms.getDownloadManager().getSessionDownloads().get((Integer) ms.getItemTable().getValueAt(selectedIndex, 0));
            if (download != null) {
                if (download.isPaused())
                    download.resume();
                else
                    download.pause();
            }
        }
        */
    }

    public static void pauseAll(MainScreen ms, boolean isPause) {
        Iterator<Download> i = ms.getDownloadManager().getActiveDownloads().values().iterator();
        Download download;
        while (i.hasNext()) {
            download = i.next();
            if (download != null) {
                if (!isPause)
                    download.resume();
                else
                    download.pause();
            }
        }
    }

    public static void cancelSelectedItem(ItemPanel it) {
        /*
        int selectedIndex = ms.getItemTable().getSelectedRow();
        if (selectedIndex >= 0) {
            Download download = ms.getDownloadManager().getSessionDownloads().get((Integer) ms.getItemTable().getValueAt(selectedIndex, 0));
            if (download != null) {
                download.cancel();
            }
        }
        */
    }

    public static void cancelAll(ItemPanel it) {
        /*
        Iterator<Download> i = ms.getDownloadManager().getSessionDownloads().values().iterator();
        Download download;
        while (i.hasNext()) {
            download = i.next();
            if (download != null) {
                download.cancel();
            }
        }
        */
    }

    public static void removeSelectedItem(ItemPanel it) {
        /*
        int selectedIndex = ms.getItemTable().getSelectedRow();
        if (selectedIndex >= 0) {
            Download download = ms.getDownloadManager().getSessionDownloads().get((Integer) ms.getItemTable().getValueAt(selectedIndex, 0));
            if (download != null) {
                if (download.isActive()) {
                    if (JOptionPane.showConfirmDialog(null, "This is an active download. If you remove it from the list, download will be canceled. Are you sure to remove it?", "Active download", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        download.cancel();
                    }
                } else {
                    download.cancel();
                }
                ((DefaultTableModel) ms.getItemTable().getModel()).removeRow(selectedIndex);
                if (JOptionPane.showConfirmDialog(null, "Would you like to delete it also from server?", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    ms.getDownloadManager().getConnection().refresh();
                    if (download.delete())
                        JOptionPane.showMessageDialog(null, "Item deleted succesfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    else
                        JOptionPane.showMessageDialog(null, "Delete operation failed!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                ((DefaultTableModel) ms.getItemTable().getModel()).removeRow(selectedIndex);
            }
        }
        */
    }
    
    public static void changeSortOrder(MainScreen ms, String sortBy) {
        if (sortBy.equals("name")) {
            if (!ms.getItemsPanel().isSortedByName())
                ms.getItemsPanel().sortByName();
        }
        else {
            if (!ms.getItemsPanel().isSortedByDate())
                ms.getItemsPanel().sortByDate();
        }
    }

    public static void resetDownloadSession(MainScreen ms) {
        Download[] sessionDownloads = ms.getDownloadManager().getSessionDownloads().values().toArray(new Download[0]);
        for (int i = 0; i < sessionDownloads.length; i++) {
            if (!sessionDownloads[i].isActive() && (sessionDownloads[i].isFaulty() || sessionDownloads[i].isCanceled()))
                ms.getDownloadManager().getSessionDownloads().remove(sessionDownloads[i].getId());
        }
        JOptionPane.showMessageDialog(null, "Download session is clear!", "Download session", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void openDownloadFolder() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(UserPreferences.PREF_DOWNLOAD_TARGET));
            } else {
                JOptionPane.showMessageDialog(null, "Cannot open download folder!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot open download folder! (" + ex.getMessage() + ")", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}