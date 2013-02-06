package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import communication.Download;

public class JTreeDownload extends JTree {
    private static final long serialVersionUID = 6328897696354901796L;
    private ArrayList<Download> downloads;
    private DefaultTreeModel treeModelDL;
    private JTreeDownload downloadTree;
    private MainScreen ms;

    public JTreeDownload(MainScreen ms) {
        super( new DefaultTreeModel( new FolderNode( "Active downloads" ) ) );
        downloadTree = this;
        this.ms = ms;
        treeModelDL = (DefaultTreeModel) getModel();
        setEditable( false );
        getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        setRowHeight( 0 );
        setRootVisible( false );
        setCellRenderer( new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = -7105201164017761653L;

            public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel,
                    boolean expanded, boolean leaf, int row, boolean hasFocus ) {

                if ( value instanceof LeafNode ) {
                    JPanel panel = new JPanel();
                    String text = ( ( (LeafNode) value ).getDownload() ).getItem().getName();
                    panel.add( new JLabel( text ) );
                    panel.setBackground( new Color( 0, 0, 0, 0 ) );
                    panel.setEnabled( tree.isEnabled() );
                    return panel;
                }
                else {
                    super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
                    return this;
                }
            }
        } );
        MouseMotionListener mml = new MouseMotionAdapter() {
            @Override
            public void mouseMoved( MouseEvent e ) {
                int selRow = getRowForLocation( 0, e.getY() );
                if ( selRow > -1 ) {
                    TreePath selTmp = getPathForRow( selRow );
                    setSelectionPath( selTmp );
                }
                else
                    setSelectionRow( -1 );
                super.mouseMoved( e );
            }
        };
        addMouseMotionListener( mml );
        MouseListener ml = new MouseAdapter() {
            @Override
            public void mouseExited( MouseEvent e ) {
                setSelectionRow( -1 );
                super.mouseExited( e );
            }

            @Override
            public void mouseReleased( MouseEvent e ) {
                TreePath path = getSelectionPath();
                if ( path != null ) {
                    Object obj = path.getLastPathComponent();
                    if ( obj instanceof LeafNode ) {
                        DefaultMutableTreeNode node = downloadTree.ms.getItemPanel().getItemInTree( ( (LeafNode) obj ).getDownload()
                                .getItem() );
                        if ( node != null && node instanceof LeafNode )
                            downloadTree.ms.getItemPanel().focus( (LeafNode) node );
                    }
                }
                super.mouseReleased( e );
            }
        };
        addMouseListener( ml );
        setUI( new BasicTreeUI() {
            @Override
            protected void paintRow( java.awt.Graphics g, java.awt.Rectangle clipBounds,
                    java.awt.Insets insets, java.awt.Rectangle bounds, TreePath path, int row,
                    boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf ) {
                AnimatedTreeUI.drawCell( g, bounds, downloadTree, path, row );
                super.paintRow( g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf );
            };
        } );
        downloads = new ArrayList<Download>( 1 );
    }

    public void addDL( Download download ) {
        if ( !downloads.contains( download ) ) {
            // System.out.println( "add dl " + download.getItem().getName() );
            downloads.add( download );
            if ( downloads.size() == 1 ) {
                setRootVisible( true );
            }
            treeModelDL.insertNodeInto( new LeafNode( download ),
                    ( (DefaultMutableTreeNode) treeModelDL.getRoot() ),
                    ( (DefaultMutableTreeNode) treeModelDL.getRoot() ).getChildCount() );
            if ( downloads.size() == 1 ) {
                expandRow( 0 );
                setRootVisible( false );
            }
        }
        else {
            LeafNode node = getNode( download );
            if ( node != null ) {
                node.setDownPerc( (float) download.getDownloadedAmount() / (float) download.getTotalLength() );
                repaint();
            }
        }
        if ( downloads.size() >= 1 )
            this.expandRow( 0 );
    }

    public void removeDL( Download download ) {
        if ( downloads.contains( download ) ) {
            downloads.remove( download );
            LeafNode node = getNode( download );
            if ( node != null ) {
                treeModelDL.removeNodeFromParent( node );
                repaint();
            }
        }
    }

    public void updateDL() {
        Iterator<Download> dls = ms.getDownloadManager().getActiveDownloads().values().iterator();
        while ( dls.hasNext() ) {
            Download dl = dls.next();
            addDL( dl );
        }
        List<Download> delList = new ArrayList<Download>( 1 );
        ListIterator<Download> li = downloads.listIterator();
        while ( li.hasNext() ) {
            Download dl = li.next();
            if ( !dl.isActive() )
                delList.add( dl );
        }
        li = delList.listIterator();
        while ( li.hasNext() ) {
            Download dl = li.next();
            removeDL( dl );
        }
        repaint();
    }

    public LeafNode getNode( Download download ) {
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModelDL.getRoot() ).children(); e.hasMoreElements(); ) {
            LeafNode curNode = (LeafNode) e.nextElement();
            Object curObj = curNode.getUserObject();
            if ( curObj instanceof Download ) {
                Download dl = (Download) curObj;
                if ( dl.getItem().getId().equals( download.getItem().getId() ) ) {
                    return curNode;
                }
            }
        }
        return null;
    }
}
