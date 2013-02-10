package gui;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.ToolTipManager;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
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
import util.SortTreeModel;
import util.TreeStringComparatorDate;
import util.TreeStringComparatorName;
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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

public class ItemPanel extends JPanel implements TreeSelectionListener {

    private static final long serialVersionUID = -6845029704959817538L;
    private JTree tree;
    private JSplitPane splitPane;
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
    private Preferences prefs = null;
    private Connection conn;
    private MainScreen ms;
    private Boolean treeLoaded = false;
    private Boolean stopOperation = false;
    private Boolean sortedByName = true;
    private Boolean playWithLineStyle = false;
    private String lineStyle = "Horizontal";

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
    protected JMenuItem expandMenuItem;
    protected JMenuItem collapseMenuItem;

    public static List<String> prefsAutoDLFolder;

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
                tree.repaint();
            }
        } );
        addToAutomaticDownloadMenuItem = new JMenuItem( "Add to automatic download" );
        addToAutomaticDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                Object node = tree.getLastSelectedPathComponent();
                if ( node instanceof FolderNode )
                    ( (FolderNode) node ).setAutoDL( !( (FolderNode) node ).isAutoDL() );
                tree.repaint();
            }
        } );
        pauseOrResumeDownloadMenuItem = new JMenuItem( "Pause/resume selected" );
        pauseOrResumeDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                Object node = tree.getLastSelectedPathComponent();
                if ( node instanceof LeafNode ) {
                    GuiOperations.pauseOrResumeSelectedItem( ms, ( (LeafNode) node ).getItem() );
                }
            }
        } );
        cancelDownloadMenuItem = new JMenuItem( "Cancel selected" );
        cancelDownloadMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                Object node = tree.getLastSelectedPathComponent();
                if ( node instanceof LeafNode ) {
                    GuiOperations.cancelSelectedItem( ms, ( (LeafNode) node ).getItem() );
                }
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
        expandMenuItem = new JMenuItem( "Expand everything" );
        expandMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                expandEverything();
            }
        } );
        collapseMenuItem = new JMenuItem( "Collapse everything" );
        collapseMenuItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                collapseEverything();
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
        itemMenu.add( expandMenuItem );
        itemMenu.add( collapseMenuItem );
        itemMenu.addSeparator();
        itemMenu.add( cleanDownloadedMenuItem );

        // Create a tree
        waitNode = new DefaultMutableTreeNode( waitNodeTxt );
        rootNode = new DefaultMutableTreeNode( rootNodeTxt );
        loadNode = new DefaultMutableTreeNode( loadNodeTxt );
        waitTreeModel = new DefaultTreeModel( waitNode );
        tree = new JTree( waitTreeModel );
        tree.setEditable( false );
        tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
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
        rightPanel.setLayout( new BoxLayout( rightPanel, BoxLayout.PAGE_AXIS ) );

        // Create the HTML viewing pane
        infoPane = new JEditorPane();
        infoPane.setEditable( false );
        JScrollPane infoView = new JScrollPane( infoPane );
        infoView.setPreferredSize( new Dimension( 0, 0 ) );

        downloadTree = new JTreeDownload( ms );
        JScrollPane downView = new JScrollPane( downloadTree );
        downView.getHorizontalScrollBar().addAdjustmentListener( new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged( AdjustmentEvent e ) {
                downloadTree.repaint();
            }
        } );
        downView.setPreferredSize( new Dimension( 0, 0 ) );
        downView.setBorder( new TitledBorder( "Downloads" ) );

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
        updateInfo();
    }

    private void updateInfo() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if ( node == null )
            return;

        Object obj = node.getUserObject();
        if ( obj instanceof Item ) {
            if ( node instanceof LeafNode )
                infoPane.setText( ( (LeafNode) node ).getInfos() );
            else if ( node instanceof FolderNode )
                infoPane.setText( ( (FolderNode) node ).getInfos() );
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
                if ( UserPreferences.PREF_BEHAVIOR_DOWNLOAD_EVERYTHING )
                    list.add( ( (LeafNode) curNode ).getItem() );
                else {
                    TreeNode[] nodePath = curNode.getPath();
                    for ( int i = 0; i < nodePath.length; i++ ) {
                        if ( nodePath[ i ] instanceof FolderNode && ( (FolderNode) nodePath[ i ] ).isAutoDL() ) {
                            list.add( ( (LeafNode) curNode ).getItem() );
                            break;
                        }
                    }
                }
            }
        }
        if ( list.isEmpty() )
            return null;
        else
            return list;
    }
    
    public List<LeafNode> getLeaves() {
        List<LeafNode> list = new ArrayList<LeafNode>( 1 );
        for ( @SuppressWarnings( "rawtypes" )
        Enumeration e = ( (DefaultMutableTreeNode) treeModel.getRoot() ).depthFirstEnumeration(); e
                .hasMoreElements(); ) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) e.nextElement();
            if ( curNode instanceof LeafNode ) {
                list.add( (LeafNode) curNode );
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
        tree.repaint();
    }

    public void focus( DefaultMutableTreeNode node ) {
        if ( node == null )
            return;

        if ( node.isLeaf() && node != (DefaultMutableTreeNode) treeModel.getRoot() ) {
            TreePath pathNode = new TreePath( node.getPath() );
            node = (DefaultMutableTreeNode) node.getParent();
            TreePath path = new TreePath( node.getPath() );
            tree.expandPath( path );
            tree.setSelectionPath( pathNode );
            tree.scrollPathToVisible( pathNode );
        }
    }

    public int getDivPos() {
        return splitPane.getDividerLocation();
    }

    public synchronized void updateDownload( Download dl ) {
        downloadTree.updateDownload( dl );
    }

    public JTree getTree() {
        return tree;
    }
    
    public void checkFiles() {
        List<LeafNode> leaves = getLeaves();
        if ( leaves != null ) {
            ListIterator<LeafNode> it = leaves.listIterator();
            while ( it.hasNext() ) {
                LeafNode node = it.next();
                
                // Check finished files
                String filePath = UserPreferences.PREF_DOWNLOAD_TARGET;
                String dirs = node.getPathDir().getDirs();
                if ( dirs != null )
                    filePath += File.separator + dirs;
                filePath += File.separator + node.getItem().getName();
                
                File f = new File( filePath );
                if ( f.exists() && f.length() == node.getItem().getSize() ) {
                    node.setDownPerc( 1.0f );
                    node.setStatus( "Completed" );
                    continue;
                }
                // Check part files
                int i = 0;
                long dlSize = 0L;
                while ( true ) {
                    File fPart = new File( filePath + ".pio" + i );
                    if ( !fPart.exists() )
                        break;
                    else
                        dlSize += fPart.length();
                    i++;
                }
                if ( i > 0 ) {
                    node.setDownPerc( (float) dlSize / (float) node.getItem().getSize() );
                    node.setStatus( GuiOperations.getReadableSize( dlSize ) + " / "
                            + GuiOperations.getReadableSize( node.getItem().getSize() ) );
                }
            }
        }
        repaint();
    }
    
    public void expandEverything() {
        for ( int i = 0; i < tree.getRowCount(); i++ ) {
            tree.expandRow( i );
        }
    }

    public void collapseEverything() {
        for ( int i = tree.getRowCount() - 1; i >= 0; i-- ) {
            tree.collapseRow( i );
        }
    }

    @Override
    public synchronized void repaint() {
        if ( tree != null )
            tree.repaint();
        if ( downloadTree != null )
            downloadTree.repaint();
        if ( infoPane != null ) {
            updateInfo();
            infoPane.repaint();
        }
        super.repaint();
    }
}
