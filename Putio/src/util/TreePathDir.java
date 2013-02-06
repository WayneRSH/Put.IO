package util;

import java.io.File;

import javax.swing.tree.TreePath;

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
