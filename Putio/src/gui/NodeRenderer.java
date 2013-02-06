package gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import util.StaticIcon;

public class NodeRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 3297546151711973066L;
    
    private StaticIcon openFolderIcon = new StaticIcon( StaticIcon.openFolderIcon );
    private StaticIcon openFolderSelIcon = new StaticIcon( StaticIcon.openFolderSelIcon );
    private StaticIcon closedFolderIcon = new StaticIcon( StaticIcon.closedFolderIcon );
    private StaticIcon closedFolderSelIcon = new StaticIcon( StaticIcon.closedFolderSelIcon );
    private StaticIcon putioIcon = new StaticIcon( StaticIcon.putioTreeIcon );
    private Color colorSel = new Color( 180, 180, 180 );

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