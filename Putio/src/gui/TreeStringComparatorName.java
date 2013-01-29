package gui;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeStringComparatorName implements
        Comparator<DefaultMutableTreeNode> {
    public int compare( DefaultMutableTreeNode o1, DefaultMutableTreeNode o2 ) {
        String s1 = o1.getUserObject().toString();
        String s2 = o2.getUserObject().toString();
        return s1.compareToIgnoreCase( s2 );
    }
}