package gui;

import javax.swing.tree.DefaultMutableTreeNode;

import util.TreePathDir;

import api.Item;

import communication.Download;

public class LeafNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 6674627800156445681L;

    private Item item = null;
    private Download download = null;
    private float downloadPercentage = 0.0f;
    private String status = "Waiting";

    public LeafNode( Object obj ) {
        super( obj );
        if ( obj instanceof Item )
            item = (Item) obj;
        else if ( obj instanceof Download ) {
            download = (Download) obj;
            item = download.getItem();
        }
    }

    public LeafNode( Object obj, float dwn, String st ) {
        super( obj );
        item = (Item) obj;
        downloadPercentage = dwn;
        status = st;
    }

    public Item getItem() {
        return item;
    }

    public Download getDownload() {
        return download;
    }

    public float getDownPerc() {
        return downloadPercentage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus( String st ) {
        this.status = st;
    }

    public void setDownPerc( float dwnPerc ) {
        if ( dwnPerc > 1.0f )
            downloadPercentage = 1.0f;
        else
            downloadPercentage = dwnPerc;
    }
    
    public void setDownload( Download download ) {
        this.download = download;
        this.item = download.getItem();
    }

    public TreePathDir getPathDir() {
        TreePathDir path = new TreePathDir( this.getPath() );
        return path;
    }

    public String getInfos() {
        return ( "Name : " + item.getName() + "\nId : " + item.getId() + "\nPid : " + item.getParentId()
                + "\nCreatedAt : " + item.getCreatedAt() + "\nPath : " + getPathDir().toString()
                + "\nStatus : " + getStatus() );
    }
}
