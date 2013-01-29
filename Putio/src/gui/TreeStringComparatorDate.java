package gui;

import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;

import api.Item;

public class TreeStringComparatorDate implements
        Comparator<DefaultMutableTreeNode> {
    public int compare( DefaultMutableTreeNode o1, DefaultMutableTreeNode o2 ) {
        String s1 = ( (Item) o1.getUserObject() ).getCreatedAt();
        String s2 = ( (Item) o2.getUserObject() ).getCreatedAt();
        return s2.compareToIgnoreCase( s1 );
    }
}