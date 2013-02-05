/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;

import javax.swing.JTree;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;

import communication.Connection;
import communication.Download;
import util.GuiOperations;
import util.PrefObj;
import util.UserPreferences;

import api.Item;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.prefs.Preferences;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.File;

public class ItemPanel extends JPanel implements TreeSelectionListener {

    private static final long serialVersionUID = -6845029704959817538L;
    private JTree tree;
    private JPanel rightPanel;
    private JEditorPane infoPane;
    private JTreeDownload downloadTree;
    private DefaultMutableTreeNode waitNode;
    private DefaultMutableTreeNode rootNode;
    private DefaultMutableTreeNode loadNode;
    private String waitNodeTxt = "Connect your account";
    private String rootNodeTxt = "Your files";
    private String loadNodeTxt = "Loading...";
    private DefaultTreeModel waitTreeModel;
    private SortTreeModel treeModel;
    private Boolean treeLoaded = false;
    private Connection conn;
    private Boolean stopOperation = false;
    private MainScreen ms;
    protected JPopupMenu itemMenu;
    protected JMenuItem autoDownloadEveryMenuItem;
    protected JMenuItem addToAutomaticDownloadMenuItem;
    protected JMenuItem pauseOrResumeDownloadMenuItem;
    protected JMenuItem cancelDownloadMenuItem;
    protected JMenuItem removeDownloadMenuItem;
    protected JMenuItem pauseAllMenuItem;
    protected JMenuItem resumeAllMenuItem;
    protected JMenuItem cancelAllMenuItem;
    protected JMenuItem cleanDownloadedMenuItem;
    protected JMenuItem sortByNameMenuItem;
    protected JMenuItem sortByDateMenuItem;
    private Boolean sortedByName = true;
    private StaticIcon openFolderIcon = new StaticIcon( StaticIcon.openFolderIcon );
    private StaticIcon openFolderSelIcon = new StaticIcon( StaticIcon.openFolderSelIcon );
    private StaticIcon closedFolderIcon = new StaticIcon( StaticIcon.closedFolderIcon );
    private StaticIcon closedFolderSelIcon = new StaticIcon( StaticIcon.closedFolderSelIcon );
    private StaticIcon putioIcon = new StaticIcon( StaticIcon.putioTreeIcon );
    private Color colorSel = new Color( 180, 180, 180 );
    private Preferences prefs = null;
    private List<String> prefsAutoDLFolder;
    private JSplitPane splitPane;

    private class NodeRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 3297546151711973066L;

        public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel,
                boolean expanded, boolean leaf, int row, boolean hasFocus ) {
            
            if ( value instanceof LeafNode ) {
                JPanel panel = new JPanel();
                String text = ( ( (LeafNode) value ).getItem() ).toString();
                panel.add( new JLabel( text ) );
                panel.setBackground( new Color( 0, 0, 0, 0 ) );
                panel.setEnabled( tree.isEnabled() );
                return panel;
            }
            else {
                super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );

                // Custom folder icon
                if ( value instanceof FolderNode ) {
                    if ( expanded ) {
                        if ( ( (FolderNode) value ).isAutoDL() )
                            setIcon( openFolderSelIcon );
                        else
                            setIcon( openFolderIcon );
                    }
                    else {
                        if ( ( (FolderNode) value ).isAutoDL() )
                            setIcon( closedFolderSelIcon );
                        else
                            setIcon( closedFolderIcon );
                    }
                }
                // Root icon
                else if ( ( (DefaultMutableTreeNode) value ).isRoot() )
                    setIcon( putioIcon );

                setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );

                if ( sel ) {
                    if ( ( (DefaultMutableTreeNode) value ).isRoot() ) {
                        setBackgroundSelectionColor( null );
                        setForeground( Color.BLACK );
                    }
                    else {
                        setBackgroundSelectionColor( colorSel );
                    }
                    setBorderSelectionColor( null );
                }

                return this;
            }
        }
    }

    public class FolderNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 5522674899094035461L;

        private boolean isAutoDL = false;
        private Item item;

        public FolderNode( Object obj ) {
            super( obj );
            if ( obj instanceof Item ) {
                this.item = (Item) obj;
                if ( prefsAutoDLFolder.contains( item.getId() ) )
                    this.isAutoDL = true;
                else
                    this.isAutoDL = false;
            }
        }

        public void setAutoDL( boolean isAutoDL ) {
            this.isAutoDL = isAutoDL;
            if ( isAutoDL ) {
                if ( !prefsAutoDLFolder.contains( this.item.getId() ) ) {
                    prefsAutoDLFolder.add( this.item.getId() );
                }
            }
            else {
                if ( prefsAutoDLFolder.contains( this.item.getId() ) ) {
                    prefsAutoDLFolder.remove( this.item.getId() );
                }
            }

            try {
                PrefObj.putObject( prefs, "AUTODL_FOLDERS", prefsAutoDLFolder );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }

            tree.repaint();
        }

        public boolean isAutoDL() {
            return isAutoDL;
        }

        public void setAutoDLVar( boolean isAutoDL ) {
            this.isAutoDL = isAutoDL;
            tree.repaint();
        }
    }

    public class LeafNode extends DefaultMutableTreeNode {
        private static final long serialVersionUID = 6674627800156445681L;

        private Item item = null;
        private Download download = null;
        private float downloadPercentage = 0.0f;
        private String status = "Unknown";

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
            printInfo();
        }

        public void setDownPerc( float dwnPerc ) {
            if ( dwnPerc > 1.0f )
                downloadPercentage = 1.0f;
            else
                downloadPercentage = dwnPerc;
            if (download == null)
                tree.repaint();
        }

        public TreePathDir getPathDir() {
            TreePathDir path = new TreePathDir( this.getPath() );
            return path;
        }

        private void printInfo() {
            TreePath p = tree.getSelectionPath();
            if ( p != null && p.getLastPathComponent() instanceof LeafNode
                    && (LeafNode) p.getLastPathComponent() == this )
                infoPane.setText( "Name : " + item.getName() + "\nId : " + item.getId() + "\nPid : "
                        + item.getParentId() + "\nCreatedAt : " + item.getCreatedAt() + "\nPath : "
                        + getPathDir().toString() + "\nStatus : " + getStatus() );
        }

        public void focus() {
            expandSingleNode( this );
        }
    }

    public class TreePathDir extends TreePath {
        private static final long serialVersionUID = 7977104688434134727L;

        public TreePathDir( Object[] path ) {
            super( path );
        }

        @Override
        public String toString() {
            StringBuffer tempSpot = new StringBuffer( "" );

            for ( int counter = 1, maxCounter = getPathCount(); counter < maxCounter; counter++ ) {
                if ( counter > 1 )
                    tempSpot.append( File.separator );
                tempSpot.append( getPathComponent( counter ) );
            }
            return tempSpot.toString();
        }

        public String getDirs() {
            Object obj[] = getPath();
            String dirs = null;
            for ( int i = 1; i < getPathCount() - 1; i++ ) {
                if ( i > 1 )
                    dirs += File.separator;
                else
                    dirs = "";
                dirs += obj[ i ].toString();
            }
            return dirs;
        }
    }
    
    private class JTreeDownload extends JTree {
        private static final long serialVersionUID = 6328897696354901796L;
        private ArrayList<Download> downloads;
        private DefaultTreeModel treeModelDL;
        
        public JTreeDownload() {
            super( new DefaultTreeModel( new FolderNode( "Active downloads" ) ) );
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
                    int selRow = downloadTree.getRowForLocation( 0, e.getY() );
                    if ( selRow > -1 ) {
                        TreePath selTmp = downloadTree.getPathForRow( selRow );
                        downloadTree.setSelectionPath( selTmp );
                    }
                    else
                        downloadTree.setSelectionRow( -1 );
                    super.mouseMoved( e );
                }
            };
            addMouseMotionListener( mml );
            MouseListener ml = new MouseAdapter() {
                @Override
                public void mouseExited( MouseEvent e ) {
                    downloadTree.setSelectionRow( -1 );
                    super.mouseExited( e );
                }
                
                @Override
                public void mouseReleased( MouseEvent e ) {
                    TreePath path = downloadTree.getSelectionPath();
                    if ( path != null ) {
                        Object obj = path.getLastPathComponent();
                        if ( obj instanceof LeafNode ) {
                            DefaultMutableTreeNode node = getItemInTree( ( (LeafNode) obj ).getDownload().getItem() );
                            if ( node != null && node instanceof LeafNode )
                                ( (LeafNode) node ).focus();
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
                    super.paintRow( g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded,
                            isLeaf );
                };
            } );
            downloads = new ArrayList<Download>( 1 );
        }
        public void addDL( Download download ) {
            if ( !downloads.contains( download ) ) {
                //System.out.println( "add dl " + download.getItem().getName() );
                downloads.add( download );
                if (downloads.size() == 1) {
                    setRootVisible( true );
                }
                treeModelDL.insertNodeInto( new LeafNode( download ),
                        ( (DefaultMutableTreeNode) treeModelDL.getRoot() ),
                        ( (DefaultMutableTreeNode) treeModelDL.getRoot() ).getChildCount() );
                if (downloads.size() == 1) {
                    expandRow( 0 );
                    setRootVisible( false );
                }
            }
            else {
                LeafNode node = getNode( download );
                if ( node != null ) {
                    node.setDownPerc( (float) download.getDownloadedAmount() / (float) download.getTotalLength() );
                }
            }
            if (downloads.size() >= 1)
                this.expandRow( 0 );
        }
        public void removeDL( Download download ) {
            if ( downloads.contains( download ) ) {
                downloads.remove( download );
                LeafNode node = getNode( download );
                if ( node != null ) {
                    treeModelDL.removeNodeFromParent( node );
                    downloadTree.repaint();
                }
            }
        }
        public void updateDL() {
            Iterator<Download> dls = ms.getDownloadManager().getActiveDownloads().values().iterator();
            while ( dls.hasNext() ) {
                Download dl = dls.next();
                addDL( dl );
            }
            List<Download> delList = new ArrayList<Download>(1);
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
            downloadTree.repaint();
        }
        public LeafNode getNode( Download download ) {
            for ( @SuppressWarnings( "rawtypes" )
            Enumeration e = ( (DefaultMutableTreeNode) treeModelDL.getRoot() ).children(); e
                    .hasMoreElements(); ) {
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

    // Optionally play with line styles. Possible values are
    // "Angled" (the default), "Horizontal", and "None".
    private static boolean playWithLineStyle = false;
    private static String lineStyle = "Horizontal";

    @SuppressWarnings( "unchecked" )
    public ItemPanel( MainScreen mainScreen ) throws Exception {
        super( new GridLayout( 1, 0 ) );

        ms = mainScreen;

        itemMenu = new JPopupMenu();
        autoDownloadEveryMenuItem = new JMenuItem( "Automatically download everything" );
        autoDownloadEveryMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                ListIterator<String> prefIterator = prefsAutoDLFolder.listIterator();
                while ( prefIterator.hasNext() ) {
                    String id = prefIterator.next();
                    if ( id == null )
                        continue;
                    DefaultMutableTreeNode node = getItemInTree( id );
                    if ( node != null && node instanceof FolderNode )
                        ( (FolderNode) node ).setAutoDLVar( false );
                }
                try {
                    prefsAutoDLFolder = new ArrayList<String>( 1 );
                    PrefObj.putObject( prefs, "AUTODL_FOLDERS", prefsAutoDLFolder );
                }
                catch ( Exception e1 ) {
                    e1.printStackTrace();
                }
            }
        } );
        addToAutomaticDownloadMenuItem = new JMenuItem( "Add to automatic download" );
        addToAutomaticDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                TreePath treePath = tree.getSelectionPath();
                Object node = treePath.getLastPathComponent();
                if ( node instanceof FolderNode )
                    ( (FolderNode) node ).setAutoDL( !( (FolderNode) node ).isAutoDL );
            }
        } );
        pauseOrResumeDownloadMenuItem = new JMenuItem( "Pause/resume selected" );
        pauseOrResumeDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.pauseOrResumeSelectedItem( getItemPanel() );
            }
        } );
        cancelDownloadMenuItem = new JMenuItem( "Cancel selected" );
        cancelDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.cancelSelectedItem( getItemPanel() );
            }
        } );
        removeDownloadMenuItem = new JMenuItem( "Remove selected" );
        removeDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.removeSelectedItem( getItemPanel() );
            }
        } );
        pauseAllMenuItem = new JMenuItem( "Pause all" );
        pauseAllMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.pauseAll( ms, true );
            }
        } );
        resumeAllMenuItem = new JMenuItem( "Resume all" );
        resumeAllMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.pauseAll( ms, false );
            }
        } );
        cancelAllMenuItem = new JMenuItem( "Cancel all" );
        cancelAllMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.cancelAll( getItemPanel() );
            }
        } );
        cleanDownloadedMenuItem = new JMenuItem( "Clean downloaded/canceled files" );
        cleanDownloadedMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.cleanDownloadedItems( getItemPanel() );
            }
        } );
        sortByNameMenuItem = new JMenuItem( "Sort by name" );
        sortByNameMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.changeSortOrder( ms, "name" );
            }
        } );
        sortByDateMenuItem = new JMenuItem( "Sort by date" );
        sortByDateMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                GuiOperations.changeSortOrder( ms, "date" );
            }
        } );
        itemMenu.add( autoDownloadEveryMenuItem );
        itemMenu.add( addToAutomaticDownloadMenuItem );
        itemMenu.addSeparator();
        itemMenu.add( pauseOrResumeDownloadMenuItem );
        itemMenu.add( cancelDownloadMenuItem );
        itemMenu.add( removeDownloadMenuItem );
        itemMenu.addSeparator();
        itemMenu.add( pauseAllMenuItem );
        itemMenu.add( resumeAllMenuItem );
        itemMenu.add( cancelAllMenuItem );
        itemMenu.addSeparator();
        itemMenu.add( sortByNameMenuItem );
        itemMenu.add( sortByDateMenuItem );
        itemMenu.addSeparator();
        itemMenu.add( cleanDownloadedMenuItem );

        // Create a tree
        waitNode = new DefaultMutableTreeNode( waitNodeTxt );
        rootNode = new DefaultMutableTreeNode( rootNodeTxt );
        loadNode = new DefaultMutableTreeNode( loadNodeTxt );
        waitTreeModel = new DefaultTreeModel( waitNode );
        tree = new JTree( waitTreeModel );
        tree.setEditable( false );
        tree.getSelectionModel().setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
        // Enable tool tips
        ToolTipManager.sharedInstance().registerComponent( tree );
        tree.setRowHeight( 0 );
        tree.setCellRenderer( new NodeRenderer() );
        tree.setUI( new AnimatedTreeUI() );

        // Listen for when the selection changes.
        tree.addTreeSelectionListener( this );

        MouseListener ml = new MouseAdapter() {
            @Override
            public void mouseReleased( MouseEvent e ) {
                int selRow = tree.getClosestRowForLocation( e.getX(), e.getY() );
                if ( selRow > 0 ) {
                    if ( e.getClickCount() == 1 ) {
                        pauseOrResumeDownloadMenuItem.setEnabled( true );
                        cancelDownloadMenuItem.setEnabled( true );
                        removeDownloadMenuItem.setEnabled( true );
                    }
                    else if ( e.getClickCount() == 2 ) {
                        // System.out.println( "2 clic " + e.getButton() + " : "
                        // + selRow + " "
                        // + selPath.toString() );
                    }
                }
                else {
                    pauseOrResumeDownloadMenuItem.setEnabled( false );
                    cancelDownloadMenuItem.setEnabled( false );
                    removeDownloadMenuItem.setEnabled( false );
                }

                TreePath selTmp = tree.getPathForRow( selRow );
                tree.setSelectionPath( selTmp );
                Object obj = selTmp.getLastPathComponent();
                if ( obj instanceof FolderNode ) {
                    if ( ( (FolderNode) obj ).isAutoDL() )
                        addToAutomaticDownloadMenuItem.setText( "Remove from automatic download" );
                    else
                        addToAutomaticDownloadMenuItem.setText( "Add to automatic download" );
                }
                addToAutomaticDownloadMenuItem.setVisible( obj instanceof FolderNode );
                // Hide first separator if necessary
                for ( int i = 0; i < itemMenu.getComponentCount() - 1; i++ ) {
                    if ( itemMenu.getComponent( i ) instanceof JSeparator ) {
                        ( (JSeparator) itemMenu.getComponent( i ) ).setVisible( obj instanceof FolderNode );
                        break;
                    }
                }

                if ( e.getButton() == MouseEvent.BUTTON3 )
                    itemMenu.show( e.getComponent(), e.getX(), e.getY() );
            }
        };

        tree.addMouseListener( ml );

        tree.addTreeWillExpandListener( new TreeWillExpandListener() {
            @Override
            public void treeWillExpand( TreeExpansionEvent event ) throws ExpandVetoException {
            }

            @Override
            public void treeWillCollapse( TreeExpansionEvent event ) throws ExpandVetoException {
                TreePath path = event.getPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                if ( node.equals( (DefaultMutableTreeNode) treeModel.getRoot() ) )
                    throw new ExpandVetoException( event );
            }
        } );

        if ( playWithLineStyle ) {
            System.out.println( "line style = " + lineStyle );
            tree.putClientProperty( "JTree.lineStyle", lineStyle );
        }

        // Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane( tree );
        treeView.getHorizontalScrollBar().addAdjustmentListener( new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged( AdjustmentEvent e ) {
                tree.repaint();
            }
        } );

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
        
        // Create the HTML viewing pane
        infoPane = new JEditorPane();
        infoPane.setEditable( false );
        JScrollPane infoView = new JScrollPane( infoPane );
        infoView.setPreferredSize( new Dimension( 0, 0 ) );
        
        downloadTree = new JTreeDownload();
        JScrollPane downView = new JScrollPane( downloadTree );
        downView.setPreferredSize( new Dimension( 0, 0 ) );
        downView.setBorder( new TitledBorder( "Active downloads" ) );
        
        rightPanel.add( infoView );
        rightPanel.add( downView );

        // Add the scroll panes to a split pane
        splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
        splitPane.setLeftComponent( treeView );
        splitPane.setRightComponent( rightPanel );

        splitPane.setDividerLocation( UserPreferences.PREFS.getInt( "FRAME_DIV_POS",
                Math.round( ms.getWidth() / 2 ) ) );
        splitPane.setPreferredSize( new Dimension( 500, 300 ) );

        // Add the split pane to this panel
        add( splitPane );

        if ( prefs == null && UserPreferences.PREFS != null )
            prefs = UserPreferences.PREFS;

        try {
            prefsAutoDLFolder = (List<String>) PrefObj.getObject( prefs, "AUTODL_FOLDERS" );
        }
        catch ( Exception e ) {
            prefsAutoDLFolder = new ArrayList<String>( 1 );
            PrefObj.putObject( prefs, "AUTODL_FOLDERS", prefsAutoDLFolder );
        }
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged( TreeSelectionEvent e ) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if ( node == null )
            return;

        Object obj = node.getUserObject();
        if ( obj instanceof Item ) {

            Item nodeInfo = (Item) obj;
            TreePathDir path = null;
            String status = "";

            if ( node instanceof LeafNode ) {
                path = ( (LeafNode) node ).getPathDir();
                status = ( (LeafNode) node ).getStatus();
            }
            else
                path = new TreePathDir( node.getPath() );

            infoPane.setText( "Name : " + nodeInfo.getName() + "\nId : " + nodeInfo.getId() + "\nPid : "
                    + nodeInfo.getParentId() + "\nCreatedAt : " + nodeInfo.getCreatedAt() + "\nPath : "
                    + path.toString() + "\nStatus : " + status );
        }
    }

    public void initTree() {
        switch ( UserPreferences.PREF_BEHAVIOR_SORT_BY ) {
        case UserPreferences.OPTION_SORT_BY_DATE:
            treeModel = new SortTreeModel( rootNode, new TreeStringComparatorDate() );
            sortedByName = false;
            sortByNameMenuItem.setEnabled( true );
            sortByDateMenuItem.setEnabled( false );
            break;
        default:
            treeModel = new SortTreeModel( rootNode, new TreeStringComparatorName() );
            sortedByName = true;
            sortByNameMenuItem.setEnabled( false );
            sortByDateMenuItem.setEnabled( true );
            break;
        }
        treeModel.addTreeModelListener( new MyTreeModelListener() );

        waitTreeModel.setRoot( waitNode );
        tree.setModel( waitTreeModel );
        tree.setEditable( false );
        treeLoaded = false;
        infoPane.setText( "" );
    }

    public void populateTree( Connection connection ) {
        this.stopOperation = false;

        conn = connection;
        if ( !treeLoaded )
            waitTreeModel.setRoot( loadNode );

        try {
            List<Item> rootItems = conn.getRootItems();
            fillTree( rootItems );
            reorderTree( rootItems );

            if ( !treeLoaded ) {
                tree.setModel( treeModel );
                tree.expandRow( 0 );
            }

            // The list of all the items from the API V2 doesn't return the
            // shared items...
            if ( UserPreferences.PREF_LOAD_SHARED )
                fillSharedTree();
            else
                removeSharedTree();

            treeLoaded = true;
        }
        catch ( Exception e ) {
            System.out.println( e.toString() );
        }
    }

    private void fillTree( List<Item> rootItems ) {
        ListIterator<Item> i = rootItems.listIterator();
        DefaultMutableTreeNode itemNode = null;
        while ( i.hasNext() ) {
            Item currentItem = i.next();
            if ( currentItem.isDir() )
                itemNode = new FolderNode( currentItem );
            else
                itemNode = new LeafNode( currentItem );

            // If it doesn't exist
            DefaultMutableTreeNode searchNode = null;
            if ( ( searchNode = getItemInTree( currentItem ) ) == null ) {

                // If the item is shared and we don't want to add shared items
                if ( ( isParentShared( currentItem, rootItems ) || currentItem.isShared() )
                        && !UserPreferences.PREF_LOAD_SHARED ) {
                    currentItem.setShared( true );
                    continue;
                }

                // Check if the item is a child
                for ( @SuppressWarnings( "rawtypes" )
                Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).breadthFirstEnumeration(); e
                        .hasMoreElements() && searchNode == null; ) {
                    DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
                    Object nodeInfo = curNode.getUserObject();
                    if ( nodeInfo instanceof Item ) {
                        Item itTmp = (Item) nodeInfo;
                        // If it's a child
                        if ( itTmp.getId().equals( currentItem.getParentId() ) ) {
                            if ( itTmp.isShared() )
                                currentItem.setShared( true );
                            searchNode = curNode;
                            treeModel.insertNodeInto( itemNode, searchNode );
                        }
                    }
                }

                // If it's not a child or we didn't find the parent in the
                // nodes, we put it in the root
                if ( searchNode == null ) {
                    treeModel.insertNodeInto( itemNode, (DefaultMutableTreeNode) treeModel.getRoot() );
                }

                // System.out.println( "Add: " + currentItem.getName() + " id:"
                // + currentItem.getId() );
            }
        }
    }

    private void reorderTree( List<Item> rootItems ) {
        // We must stock the nodes in a map. If we change the tree, the
        // enumeration could be messed up
        HashMap<DefaultMutableTreeNode, DefaultMutableTreeNode> nodeMap = new HashMap<DefaultMutableTreeNode, DefaultMutableTreeNode>();

        // Reorder the nodes, the lone children should be on the root,
        // so we just check the root children
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).children(); e.hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object nodeInfo = curNode.getUserObject();
            if ( nodeInfo instanceof Item ) {
                Item itTmp = (Item) nodeInfo;
                // System.out.println(itTmp.getName());
                // If the current root-child node should not be on the root
                // we look for its parent node
                if ( !itTmp.getParentId().equals( "0" ) ) {
                    DefaultMutableTreeNode searchNode = null;
                    for ( @SuppressWarnings( "rawtypes" )
                    Enumeration ePar = ( (DefaultMutableTreeNode) treeModel.getRoot() )
                            .breadthFirstEnumeration(); ePar.hasMoreElements() && searchNode == null; ) {
                        DefaultMutableTreeNode curParNode = (DefaultMutableTreeNode) ePar.nextElement();
                        Object nodeParInfo = curParNode.getUserObject();
                        if ( nodeParInfo instanceof Item ) {
                            Item itParTmp = (Item) nodeParInfo;
                            if ( itParTmp.getId().equals( itTmp.getParentId() ) ) {
                                searchNode = curParNode;
                                nodeMap.put( curNode, searchNode );
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Reorder
        Set<DefaultMutableTreeNode> keys = nodeMap.keySet();
        Iterator<DefaultMutableTreeNode> it = keys.iterator();
        while ( it.hasNext() ) {
            DefaultMutableTreeNode key = it.next();
            if ( nodeMap.get( key ) != null ) {
                // Set shared if it belongs to a shared directory
                if ( ( (Item) nodeMap.get( key ).getUserObject() ).isShared() )
                    ( (Item) key.getUserObject() ).setShared( true );
                treeModel.removeNodeFromParent( key );
                treeModel.insertNodeInto( key, nodeMap.get( key ) );
            }
        }

        // If the tree is loaded, we should clean items deleted on the server
        if ( treeLoaded ) {
            // Same as before, we stock nodes to delete in a list
            List<DefaultMutableTreeNode> delList = new ArrayList<DefaultMutableTreeNode>( 1 );

            for ( @SuppressWarnings( "rawtypes" )
            Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).depthFirstEnumeration(); e
                    .hasMoreElements(); ) {
                DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
                Object curNodeItem = curNode.getUserObject();
                if ( curNodeItem instanceof Item ) {
                    Item curItem = (Item) curNodeItem;
                    // We don't check shared Items, which are loaded in
                    // fillSharedTree separately
                    if ( curItem.isShared() )
                        continue;
                    else if ( !isItemInList( curItem, rootItems ) ) {
                        // System.out.println("Del : " + curItem.getName());
                        delList.add( curNode );
                    }
                }
            }

            ListIterator<DefaultMutableTreeNode> itDel = delList.listIterator();

            while ( itDel.hasNext() ) {
                DefaultMutableTreeNode delNode = itDel.next();
                treeModel.removeNodeFromParent( delNode );
            }
        }
    }

    private void fillSharedTree() {
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).children(); e.hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object nodeInfo = curNode.getUserObject();
            if ( nodeInfo instanceof Item ) {
                Item itTmp = (Item) nodeInfo;
                if ( itTmp.getName().contains( "shared" ) ) {
                    addChildren( curNode );
                    break;
                }
            }
        }
    }

    private void removeSharedTree() {
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).children(); e.hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object nodeInfo = curNode.getUserObject();
            if ( nodeInfo instanceof Item ) {
                Item itTmp = (Item) nodeInfo;
                if ( itTmp.getName().contains( "shared" ) ) {
                    treeModel.removeNodeFromParent( curNode );
                    break;
                }
            }
        }
    }

    private void addChildren( DefaultMutableTreeNode node ) {
        if ( stopOperation )
            return;
        List<Item> li = null;
        try {
            li = ( (Item) node.getUserObject() ).listChildren();
        }
        catch ( Exception e ) {
            li = null;
            System.out.println( e.toString() );
        }

        if ( li != null ) {

            // First, we should clean deleted items
            List<DefaultMutableTreeNode> delList = new ArrayList<DefaultMutableTreeNode>( 1 );

            for ( @SuppressWarnings( "rawtypes" )
            Enumeration e = node.children(); e.hasMoreElements(); ) {
                DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
                Object curNodeItem = curNode.getUserObject();
                if ( curNodeItem instanceof Item ) {
                    Item curItem = (Item) curNodeItem;
                    if ( !isItemInList( curItem, li ) )
                        delList.add( curNode );
                }
            }

            ListIterator<DefaultMutableTreeNode> itDel = delList.listIterator();

            while ( itDel.hasNext() ) {
                DefaultMutableTreeNode delNode = itDel.next();
                treeModel.removeNodeFromParent( delNode );
            }

            ListIterator<Item> i = li.listIterator();
            DefaultMutableTreeNode childNode = null;

            while ( i.hasNext() ) {
                Item childItem = i.next();
                // They're all shared here, so we flag them shared
                childItem.setShared( true );

                if ( ( childNode = getItemInTree( childItem ) ) == null ) {
                    if ( childItem.isDir() )
                        childNode = new FolderNode( childItem );
                    else
                        childNode = new LeafNode( childItem );

                    treeModel.insertNodeInto( childNode, node );
                    // System.out.println( "Add: " + childItem.getName() +
                    // " id:" + childItem.getId() );
                    // System.out.println( "Parent: " +
                    // ((Item)node.getUserObject()).getName() + " id:" +
                    // ((Item)node.getUserObject()).getId() );
                    if ( childItem.isDir() )
                        addChildren( childNode );
                }
                else if ( childItem.isDir() ) {
                    addChildren( childNode );
                }
            }
        }
        else {
            List<DefaultMutableTreeNode> delList = new ArrayList<DefaultMutableTreeNode>( 1 );

            for ( @SuppressWarnings( "rawtypes" )
            Enumeration e = node.children(); e.hasMoreElements(); ) {
                DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
                delList.add( curNode );
            }

            ListIterator<DefaultMutableTreeNode> itDel = delList.listIterator();

            while ( itDel.hasNext() ) {
                DefaultMutableTreeNode delNode = itDel.next();
                treeModel.removeNodeFromParent( delNode );
            }
        }
    }

    public DefaultMutableTreeNode getItemInTree( Item item ) {
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).breadthFirstEnumeration(); e
                .hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object nodeInfo = curNode.getUserObject();
            if ( nodeInfo instanceof Item ) {
                Item itTmp = (Item) nodeInfo;
                if ( itTmp.getId().equals( item.getId() ) ) {
                    return curNode;
                }
            }
        }
        return null;
    }

    public DefaultMutableTreeNode getItemInTree( String id ) {
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).breadthFirstEnumeration(); e
                .hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object nodeInfo = curNode.getUserObject();
            if ( nodeInfo instanceof Item ) {
                Item itTmp = (Item) nodeInfo;
                if ( itTmp.getId().equals( id ) ) {
                    return curNode;
                }
            }
        }
        return null;
    }

    private DefaultMutableTreeNode getItemInTree( Item item, SortTreeModel treeMod ) {
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeMod.getRoot() ).breadthFirstEnumeration(); e
                .hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object nodeInfo = curNode.getUserObject();
            if ( nodeInfo instanceof Item ) {
                Item itTmp = (Item) nodeInfo;
                if ( itTmp.getId().equals( item.getId() ) ) {
                    return curNode;
                }
            }
        }
        return null;
    }

    private Boolean isItemInList( Item item, List<Item> list ) {
        ListIterator<Item> itList = list.listIterator();
        while ( itList.hasNext() ) {
            Item curItem = itList.next();
            if ( curItem.getId().equals( item.getId() ) )
                return true;
        }
        return false;
    }

    private Boolean isParentShared( Item item, List<Item> list ) {
        ListIterator<Item> itList = list.listIterator();
        while ( itList.hasNext() ) {
            Item curItem = itList.next();
            if ( curItem.getId().equals( item.getParentId() ) && curItem.isShared() )
                return true;
        }
        return false;
    }

    public void stopOp() {
        this.stopOperation = true;
    }

    public ItemPanel getItemPanel() {
        return this;
    }

    public List<Item> getItems() {
        List<Item> list = new ArrayList<Item>( 1 );
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).depthFirstEnumeration(); e
                .hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            if ( curNode instanceof LeafNode ) {
                list.add( ( (LeafNode) curNode ).getItem() );
            }
        }
        if ( list.isEmpty() )
            return null;
        else
            return list;
    }

    public void sortByName() {
        sortBy( "name" );
    }

    public void sortByDate() {
        sortBy( "date" );
    }

    public void sortBy( String order ) {
        DefaultMutableTreeNode tmpNode = null;
        DefaultMutableTreeNode curNodeCopy = null;
        SortTreeModel treeModelTmp = null;
        DefaultMutableTreeNode rootTmp = new DefaultMutableTreeNode( rootNodeTxt );
        if ( order.equals( "name" ) ) {
            treeModelTmp = new SortTreeModel( rootTmp, new TreeStringComparatorName() );
            sortedByName = true;
        }
        else {
            treeModelTmp = new SortTreeModel( rootTmp, new TreeStringComparatorDate() );
            sortedByName = false;
        }
        sortByNameMenuItem.setEnabled( !sortedByName );
        sortByDateMenuItem.setEnabled( sortedByName );
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).breadthFirstEnumeration(); e
                .hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            Object obj = curNode.getUserObject();
            if ( obj instanceof Item ) {
                Item currentItem = (Item) obj;
                if ( currentItem.isDir() )
                    curNodeCopy = new FolderNode( currentItem );
                else
                    curNodeCopy = new LeafNode( currentItem, ( (LeafNode) curNode ).getDownPerc(),
                            ( (LeafNode) curNode ).getStatus() );

                Object objPar = ( (DefaultMutableTreeNode) curNode.getParent() ).getUserObject();
                if ( objPar instanceof Item ) {
                    if ( ( tmpNode = getItemInTree( (Item) objPar, treeModelTmp ) ) != null ) {
                        treeModelTmp.insertNodeInto( curNodeCopy, tmpNode );
                    }
                }
                else {
                    treeModelTmp.insertNodeInto( curNodeCopy, rootTmp );
                }
            }
        }
        rootNode = rootTmp;
        treeModel = treeModelTmp;
        tree.setModel( treeModel );
    }

    public Boolean isSortedByName() {
        return sortedByName;
    }

    public Boolean isSortedByDate() {
        return !sortedByName;
    }

    public void setDownPerc( Item item, float down ) {
        LeafNode leaf = (LeafNode) getItemInTree( item );
        leaf = (LeafNode) getItemInTree( item );
        leaf.setDownPerc( down );
    }

    private void expandSingleNode( DefaultMutableTreeNode node ) {
        if ( node == null )
            return;
        
        if ( node.isLeaf() && node != (DefaultMutableTreeNode) treeModel.getRoot() ) {
            TreePath pathNode = new TreePath( node.getPath() );
            node = (DefaultMutableTreeNode) node.getParent();
            TreePath path = new TreePath( node.getPath() );
            tree.expandPath( path );
            tree.scrollPathToVisible( path );
            tree.setSelectionPath( pathNode );
        }
    }

    public int getDivPos() {
        return splitPane.getDividerLocation();
    }

    public void updateDownload() {
        downloadTree.updateDL();
    }
}
