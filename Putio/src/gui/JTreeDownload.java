package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ListIterator;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import util.GuiOperations;

import communication.Download;

public class JTreeDownload extends JTree {
    private static final long serialVersionUID = 6328897696354901796L;
    private DefaultTreeModel treeModelDL;
    private JTreeDownload downloadTree;
    private MainScreen ms;
    private JPopupMenu downloadMenu;
    private JMenuItem clearMenuItem;
    private int[] mousePos = { 0, 0 };

    public JTreeDownload( MainScreen _ms ) {
        super( new DefaultTreeModel( new FolderNode( "Downloads" ) ) );
        downloadTree = this;
        this.ms = _ms;

        downloadMenu = new JPopupMenu();
        clearMenuItem = new JMenuItem( "Clear completed downloads" );
        clearMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                cleanDownloads();
            }
        } );
        downloadMenu.add( clearMenuItem );

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
                    panel.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
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
                int selRow = getRowForLocation( 10, e.getY() );
                if ( selRow > -1 ) {
                    TreePath selTmp = getPathForRow( selRow );
                    setSelectionPath( selTmp );
                    mousePos[ 0 ] = e.getX();
                    mousePos[ 1 ] = e.getY();
                    repaint();
                }
                else {
                    setSelectionRow( -1 );
                    downloadTree.setToolTipText( null );
                }
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
                if ( e.getButton() == MouseEvent.BUTTON3 )
                    downloadMenu.show( e.getComponent(), e.getX(), e.getY() );
                else {
                    TreePath path = getSelectionPath();
                    if ( path != null ) {
                        Object obj = path.getLastPathComponent();
                        if ( obj instanceof LeafNode ) {
                            LeafNode node = (LeafNode) obj;

                            // Controls
                            int xPos = ( downloadTree.getVisibleRect().width + downloadTree.getVisibleRect().x ) - 43;

                            int selection = 0;
                            // Mouse over control
                            if ( mousePos[ 0 ] >= xPos && mousePos[ 0 ] < ( xPos + 19 ) )
                                selection = 1;
                            else if ( mousePos[ 0 ] >= ( xPos + 19 ) && mousePos[ 0 ] < ( xPos + 37 ) )
                                selection = 2;

                            if ( node.getDownload().isCompleted() && !node.getDownload().isFaulty() ) {
                                if ( selection == 1 ) {
                                    // Launch
                                    GuiOperations.launchItem( ms, node.getItem() );
                                }

                                if ( selection == 2 ) {
                                    // Remove
                                    treeModelDL.removeNodeFromParent( node );
                                }
                            }
                            else {
                                if ( selection == 1 ) {
                                    if ( node.getDownload().isCanceled() || node.getDownload().isFaulty() ) {
                                        // Retry
                                        GuiOperations.retrySelectedItem( ms, node.getItem() );
                                    }
                                    else {
                                        // Pause/Resume
                                        GuiOperations.pauseOrResumeSelectedItem( ms, node.getItem() );
                                    }
                                }

                                if ( selection == 2 ) {
                                    if ( node.getDownload().isCanceled() || node.getDownload().isFaulty() ) {
                                        // Remove
                                        treeModelDL.removeNodeFromParent( node );
                                    }
                                    else {
                                        // Cancel
                                        GuiOperations.cancelSelectedItem( ms, node.getItem() );
                                    }
                                }
                            }

                            if ( selection == 0 ) {
                                DefaultMutableTreeNode leftNode = downloadTree.ms.getItemPanel()
                                        .getItemInTree( ( (LeafNode) obj ).getDownload().getItem() );
                                if ( leftNode != null && leftNode instanceof LeafNode )
                                    downloadTree.ms.getItemPanel().focus( (LeafNode) leftNode );
                            }
                        }
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
                AnimatedTreeUI.drawControls( g, bounds, downloadTree, path, row, mousePos );
            };
        } );
    }

    public synchronized void updateDownload( Download download ) {
        LeafNode node = getNode( download );

        if ( download.isActive() && node == null ) {
            treeModelDL.insertNodeInto( new LeafNode( download ),
                    ( (DefaultMutableTreeNode) treeModelDL.getRoot() ),
                    ( (DefaultMutableTreeNode) treeModelDL.getRoot() ).getChildCount() );
            if ( treeModelDL.getChildCount( ( (DefaultMutableTreeNode) treeModelDL.getRoot() ) ) >= 1
                    && !isExpanded( 0 ) ) {
                setRootVisible( true );
                expandRow( 0 );
                setRootVisible( false );
            }
        }

        if ( node != null ) {
            if ( node.getDownload() != download )
                node.setDownload( download );
            node.setDownPerc( (float) download.getDownloadedAmount() / (float) download.getTotalLength() );
        }
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

    private void cleanDownloads() {
        ArrayList<LeafNode> list = new ArrayList<LeafNode>( 1 );
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModelDL.getRoot() ).children(); e.hasMoreElements(); ) {
            LeafNode curNode = (LeafNode) e.nextElement();
            Object curObj = curNode.getUserObject();
            if ( curObj instanceof Download ) {
                Download dl = (Download) curObj;
                if ( dl.isCompleted() ) {
                    list.add( curNode );
                }
            }
        }
        ListIterator<LeafNode> it = list.listIterator();
        while ( it.hasNext() ) {
            treeModelDL.removeNodeFromParent( it.next() );
        }
    }
}
