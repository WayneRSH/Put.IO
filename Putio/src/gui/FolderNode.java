package gui;

import javax.swing.tree.DefaultMutableTreeNode;

import util.PrefObj;
import util.TreePathDir;
import util.UserPreferences;
import api.Item;

public class FolderNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 5522674899094035461L;

    private boolean isAutoDL = false;
    private Item item;

    public FolderNode( Object obj ) {
        super( obj );
        if ( obj instanceof Item ) {
            this.item = (Item) obj;
            if ( ItemPanel.prefsAutoDLFolder.contains( item.getId() ) )
                this.isAutoDL = true;
            else
                this.isAutoDL = false;
        }
    }

    public void setAutoDL( boolean isAutoDL ) {
        this.isAutoDL = isAutoDL;
        if ( isAutoDL ) {
            if ( ! ItemPanel.prefsAutoDLFolder.contains( this.item.getId() ) ) {
                ItemPanel.prefsAutoDLFolder.add( this.item.getId() );
            }
        }
        else {
            if (  ItemPanel.prefsAutoDLFolder.contains( this.item.getId() ) ) {
                ItemPanel.prefsAutoDLFolder.remove( this.item.getId() );
            }
        }

        if ( UserPreferences.PREFS != null ) {
            try {
                PrefObj.putObject( UserPreferences.PREFS, "AUTODL_FOLDERS", ItemPanel.prefsAutoDLFolder );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
    }

    public boolean isAutoDL() {
        return isAutoDL;
    }

    public void setAutoDLVar( boolean isAutoDL ) {
        this.isAutoDL = isAutoDL;
    }
    
    public TreePathDir getPathDir() {
        TreePathDir path = new TreePathDir( this.getPath() );
        return path;
    }

    public String getInfos() {
        return ( "Name : " + item.getName() + "\nId : " + item.getId() + "\nPid : " + item.getParentId()
                + "\nCreatedAt : " + item.getCreatedAt() + "\nPath : " + getPathDir().toString() );
    }
}