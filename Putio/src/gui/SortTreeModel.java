package gui;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class SortTreeModel extends DefaultTreeModel {
    private static final long serialVersionUID = 4158144157825991735L;
    private Comparator<DefaultMutableTreeNode> comparator;

    public SortTreeModel( DefaultMutableTreeNode node,
            Comparator<DefaultMutableTreeNode> c ) {
        super( node );
        comparator = c;
    }

    public SortTreeModel( DefaultMutableTreeNode node,
            boolean asksAllowsChildren, Comparator<DefaultMutableTreeNode> c ) {
        super( node, asksAllowsChildren );
        comparator = c;
    }

    public void insertNodeInto( DefaultMutableTreeNode child,
            DefaultMutableTreeNode parent ) {
        int index = findIndexFor( child, parent );
        super.insertNodeInto( child, parent, index );
    }

    private int findIndexFor( DefaultMutableTreeNode child,
            DefaultMutableTreeNode parent ) {
        int cc = parent.getChildCount();
        if ( cc == 0 ) {
            return 0;
        }
        if ( cc == 1 ) {
            return comparator.compare( child,
                    (DefaultMutableTreeNode) parent.getChildAt( 0 ) ) <= 0 ? 0
                    : 1;
        }
        return findIndexFor( child, parent, 0, cc - 1 );
    }

    private int findIndexFor( DefaultMutableTreeNode child,
            DefaultMutableTreeNode parent, int i1, int i2 ) {
        if ( i1 == i2 ) {
            return comparator.compare( child,
                    (DefaultMutableTreeNode) parent.getChildAt( i1 ) ) <= 0 ? i1
                    : i1 + 1;
        }
        int half = ( i1 + i2 ) / 2;
        if ( comparator.compare( child,
                (DefaultMutableTreeNode) parent.getChildAt( half ) ) <= 0 ) {
            return findIndexFor( child, parent, i1, half );
        }
        return findIndexFor( child, parent, half + 1, i2 );
    }
}
