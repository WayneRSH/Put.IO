package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import util.StaticIcon;

public class AnimatedTreeUI extends BasicTreeUI {

    private static BufferedImage topImage;
    private static BufferedImage bottomImage;
    private static BufferedImage compositeImage;

    // amount to offset bottom image position during animation
    private static int offsetY;
    // height of newly exposed subtree
    private static int subTreeHeight;

    private static Timer timer;
    private static ActionListener timerListener;

    private static enum AnimationState {
        EXPANDING, COLLAPSING, NOT_ANIMATING
    };

    private static AnimationState animationState;

    // animation progresses 1f -> 0f
    private static float animationComplete = 0f;
    // 0f = faster, 1f = slower
    private static float ANIMATION_SPEED = 0.8f;

    private static Color colorLeaf = new Color( 0xB7, 0xF5, 0xC6, 180 );
    private static Color colorLeafBorder = new Color( 0x9E, 0xDA, 0xAD, 180 );
    private static Color colorLeafSel = new Color( 0x29, 0x97, 0xF8, 180 );
    private static Color colorLeafSelBorder = new Color( 0x11, 0x64, 0xAC, 180 );
    private static Color colorLeafPause = new Color( 0xEB, 0xB0, 0x30, 180 );
    private static Color colorLeafPauseBorder = new Color( 0xB4, 0x85, 0x1E, 180 );
    private static Color colorLeafFaulty = new Color( 0xDB, 0x38, 0x38, 180 );
    private static Color colorLeafFaultyBorder = new Color( 0xB0, 0x1F, 0x1F, 180 );
    
    private static StaticIcon play = new StaticIcon( StaticIcon.miniPlayIcon );
    private static StaticIcon playGrey = new StaticIcon( StaticIcon.miniPlayGreyIcon );
    private static StaticIcon pause = new StaticIcon( StaticIcon.miniPauseIcon );
    private static StaticIcon pauseGrey = new StaticIcon( StaticIcon.miniPauseGreyIcon );
    private static StaticIcon stop = new StaticIcon( StaticIcon.miniStopIcon );
    private static StaticIcon stopGrey = new StaticIcon( StaticIcon.miniStopGreyIcon );
    private static StaticIcon del = new StaticIcon( StaticIcon.miniDelIcon );
    private static StaticIcon delGrey = new StaticIcon( StaticIcon.miniDelGreyIcon );
    private static StaticIcon retry = new StaticIcon( StaticIcon.miniRetryIcon );
    private static StaticIcon retryGrey = new StaticIcon( StaticIcon.miniRetryGreyIcon );

    public AnimatedTreeUI() {
        super();
        animationState = AnimatedTreeUI.AnimationState.NOT_ANIMATING;
        timerListener = new TimerListener();
        timer = new Timer( 1000 / 90, timerListener );
    }
    
    public static void drawCell( Graphics g, Rectangle bounds, JTree tree, TreePath path, int row ) {
        boolean isRowSelected = tree.isRowSelected( row );
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if ( node instanceof LeafNode ) {
            Color colorTmp = colorLeaf;
            Color colorBorderTmp = colorLeafBorder;
            if ( isRowSelected ) {
                colorTmp = colorLeafSel;
                colorBorderTmp = colorLeafSelBorder;
            }
            else if ( ( (LeafNode) node ).getDownload() != null && ( (LeafNode) node ).getDownload().isPaused() ) {
                colorTmp = colorLeafPause;
                colorBorderTmp = colorLeafPauseBorder;
            }
            else if ( ( (LeafNode) node ).getDownload() != null && ( (LeafNode) node ).getDownload().isFaulty() ) {
                colorTmp = colorLeafFaulty;
                colorBorderTmp = colorLeafFaultyBorder;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor( colorTmp );
            // Download bar
            g2.fillRect( tree.getVisibleRect().x + 4, bounds.y + 2, Math.round( ( ( tree.getVisibleRect().width - 10 )
                    * ( (LeafNode) node ).getDownPerc() * 100 ) / 100 ), bounds.height - 5 );
            g2.setColor( colorBorderTmp );
            g2.drawRect( tree.getVisibleRect().x + 4, bounds.y + 2, tree.getVisibleRect().width - 10, bounds.height - 5 );
            g2.dispose();
        }
    }
    
    public static void drawControls( Graphics g, Rectangle bounds, JTree tree, TreePath path, int row, int[] mousePos ) {
        boolean isRowSelected = tree.isRowSelected( row );
        if (isRowSelected) {
            Object obj = tree.getLastSelectedPathComponent();
            
            if (!(obj instanceof LeafNode))
                return;
            
            LeafNode node = (LeafNode) obj;
            
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            int xPos = ( tree.getVisibleRect().width + tree.getVisibleRect().x ) - 43;
            
            // Background for controls
            g2.setColor( new Color( 255, 255, 255, 170 ) );
            g2.fillRect( xPos, bounds.y + 3, 37, 18 );
            
            int selection = 0;
            // Mouse over control
            if ( mousePos[0] >= xPos && mousePos[0] < (xPos + 19) )
                selection = 1;
            else if ( mousePos[0] >= ( xPos + 19 ) && mousePos[0] < (xPos + 37 ) )
                selection = 2;
            
            int w;
            int h;
            BufferedImage bi;
            Graphics g3;
            
            if ( node.getDownload().isCompleted() 
                    && !node.getDownload().isFaulty() ) {
                if ( selection == 1 ) {
                    w = play.getIconWidth();
                    h = play.getIconHeight();
                    bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    g3 = bi.getGraphics();
                    play.paintIcon(null, g3, 0, 0);
                    g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                    tree.setToolTipText( "Launch file" );
                }
                else {
                    w = playGrey.getIconWidth();
                    h = playGrey.getIconHeight();
                    bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    g3 = bi.getGraphics();
                    playGrey.paintIcon(null, g3, 0, 0);
                    g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                }
                
                if ( selection == 2 ) {
                    w = del.getIconWidth();
                    h = del.getIconHeight();
                    bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    g3 = bi.getGraphics();
                    del.paintIcon(null, g3, 0, 0);
                    g2.drawImage(bi, null, xPos + 19, bounds.y + 4 );
                    tree.setToolTipText( "Remove from downloads" );
                }
                else {
                    w = delGrey.getIconWidth();
                    h = delGrey.getIconHeight();
                    bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                    g3 = bi.getGraphics();
                    delGrey.paintIcon(null, g3, 0, 0);
                    g2.drawImage(bi, null, xPos + 19, bounds.y + 4 );
                }
            }
            else {
                if ( node.getDownload().isCanceled()
                        || node.getDownload().isFaulty() ) {
                    if ( selection == 1 ) {
                        w = retry.getIconWidth();
                        h = retry.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        retry.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                        tree.setToolTipText( "Retry download" );
                    }
                    else {
                        w = retryGrey.getIconWidth();
                        h = retryGrey.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        retryGrey.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                    }
                }
                else if ( node.getDownload().isPaused() ) {
                    if ( selection == 1 ) {
                        w = play.getIconWidth();
                        h = play.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        play.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                        tree.setToolTipText( "Resume download" );
                    }
                    else {
                        w = playGrey.getIconWidth();
                        h = playGrey.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        playGrey.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                    }
                }
                else {
                    if ( selection == 1 ) {
                        w = pause.getIconWidth();
                        h = pause.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        pause.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                        tree.setToolTipText( "Pause download" );
                    }
                    else {
                        w = pauseGrey.getIconWidth();
                        h = pauseGrey.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        pauseGrey.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 2, bounds.y + 4 );
                    }
                }
                
                if ( node.getDownload().isCanceled() 
                        || node.getDownload().isFaulty() ) {
                    if ( selection == 2 ) {
                        w = del.getIconWidth();
                        h = del.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        del.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 19, bounds.y + 4 );
                        tree.setToolTipText( "Remove from downloads" );
                    }
                    else {
                        w = delGrey.getIconWidth();
                        h = delGrey.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        delGrey.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 19, bounds.y + 4 );
                    }
                }
                else {
                    if ( selection == 2 ) {
                        w = stop.getIconWidth();
                        h = stop.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        stop.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 19, bounds.y + 4 );
                        tree.setToolTipText( "Cancel download" );
                    }
                    else {
                        w = stopGrey.getIconWidth();
                        h = stopGrey.getIconHeight();
                        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                        g3 = bi.getGraphics();
                        stopGrey.paintIcon(null, g3, 0, 0);
                        g2.drawImage(bi, null, xPos + 19, bounds.y + 4 );
                    }
                }
            }
            
            if ( selection == 0 )
                tree.setToolTipText( "Select item" );
            
            g2.dispose();
        }
    }

    @Override
    protected void paintRow( Graphics g, Rectangle clipBounds, Insets insets, Rectangle bounds,
            TreePath path, int row, boolean isExpanded, boolean hasBeenExpanded, boolean isLeaf ) {
        
        drawCell( g, bounds, tree, path, row );

        super.paintRow( g, clipBounds, insets, bounds, path, row, isExpanded, hasBeenExpanded, isLeaf );
    }

    /**
     * All expand and collapsing done by the UI comes through here. Use it to
     * trigger the start of animation
     * 
     * @param path
     */
    protected void toggleExpandState( TreePath path ) {
        if ( animationState != AnimatedTreeUI.AnimationState.NOT_ANIMATING )
            return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if ( node instanceof LeafNode )
            return;
        if ( node == treeModel.getRoot() )
            return;
        if ( node.isLeaf() )
            return;

        animationComplete = 1f;
        boolean state = !tree.isExpanded( path );

        if ( state ) {
            super.toggleExpandState( path );
            subTreeHeight = getSubTreeHeight( path );
            createImages( path );
            animationState = AnimatedTreeUI.AnimationState.EXPANDING;
        }
        else {
            subTreeHeight = getSubTreeHeight( path );
            createImages( path );
            super.toggleExpandState( path );
            animationState = AnimatedTreeUI.AnimationState.COLLAPSING;
        }

        updateCompositeImage();
        timer.restart();
    }

    /**
     * Grab two images from the tree. The bit below the expanded node
     * (bottomImage), and the rest above it (topImage)
     */
    private void createImages( TreePath path ) {
        int h = tree.getHeight();
        int w = tree.getWidth();
        BufferedImage baseImage = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );
        Graphics g = baseImage.getGraphics();
        tree.paint( g );

        // find the next row, this is where we take the overlay image from
        int row = tree.getRowForPath( path ) + 1;
        offsetY = tree.getRowBounds( row ).y;

        topImage = new BufferedImage( tree.getWidth(), offsetY, BufferedImage.TYPE_INT_RGB );
        Graphics topG = topImage.getGraphics();
        topG.drawImage( baseImage, 0, 0, w, offsetY, // destination
                0, 0, w, offsetY, // source
                null );

        bottomImage = new BufferedImage( w, baseImage.getHeight() - offsetY, BufferedImage.TYPE_INT_RGB );
        Graphics bottomG = bottomImage.getGraphics();
        bottomG.drawImage( baseImage, 0, 0, w, baseImage.getHeight() - offsetY, // destination
                0, offsetY, w, baseImage.getHeight(), // source
                null );

        compositeImage = new BufferedImage( w, h, BufferedImage.TYPE_INT_RGB );

        g.dispose();
        topG.dispose();
        bottomG.dispose();
    }

    /**
     * create image to paint when hijacked, by painting the lower half of the
     * image offset by an amount determined by the animation. Then paint the top
     * part of the tree over the top, so some of the bottom peeks out.
     */
    private void updateCompositeImage() {
        Graphics g = compositeImage.getGraphics();

        g.setColor( tree.getBackground() );
        g.fillRect( 0, 0, compositeImage.getWidth(), compositeImage.getHeight() );

        int yOff = (int) ( ( (float) subTreeHeight ) * ( animationComplete ) );
        if ( animationState == AnimatedTreeUI.AnimationState.COLLAPSING )
            yOff = subTreeHeight - yOff;

        int dy1 = offsetY - yOff;
        g.drawImage( bottomImage, 0, dy1, null );
        g.drawImage( topImage, 0, 0, null );

        g.dispose();
    }

    private boolean isAnimationComplete() {
        switch ( animationState ) {
        case COLLAPSING:
        case EXPANDING:
            return animationComplete * offsetY < 1.3f;
        default:
            return true;
        }
    }

    /**
     * get the height of the sub tree by measuring from the location of the
     * first child in the subtree to the bottom of its last sibling The sub tree
     * should be expanded for this to work correctly.
     */
    public int getSubTreeHeight( TreePath path ) {
        if ( path.getParentPath() == null )
            return 0;

        Object origObj = path.getLastPathComponent();
        if ( getModel().getChildCount( origObj ) == 0 )
            return 0;

        Object firstChild = getModel().getChild( origObj, 0 );
        Object lastChild = getModel().getChild( origObj, getModel().getChildCount( origObj ) - 1 );

        TreePath firstPath = path.pathByAddingChild( firstChild );
        TreePath lastPath = path.pathByAddingChild( lastChild );

        int topFirst = getPathBounds( tree, firstPath ).y;
        int bottomLast = getPathBounds( tree, lastPath ).y + getPathBounds( tree, lastPath ).height;

        int height = bottomLast - topFirst;
        return height;
    }

    private class TimerListener implements ActionListener {
        public void actionPerformed( ActionEvent actionEvent ) {
            animationComplete *= ANIMATION_SPEED;

            if ( isAnimationComplete() ) {
                animationState = AnimatedTreeUI.AnimationState.NOT_ANIMATING;
                timer.stop();
            }
            else {
                updateCompositeImage();
            }
            tree.repaint();
        }
    }

    // overridden because the default clipping routine gives a NPE
    // when the painting is hijacked.
    @Override
    public void update( Graphics g, JComponent c ) {
        if ( c.isOpaque() ) {
            g.setColor( c.getBackground() );
            g.fillRect( 0, 0, c.getWidth(), c.getHeight() );
        }

        if ( animationState != AnimatedTreeUI.AnimationState.NOT_ANIMATING ) {
            if ( c.getParent() instanceof JViewport ) {
                JViewport vp = (JViewport) c.getParent();
                Rectangle visibleR = vp.getViewRect();
                g.setClip( visibleR.x, visibleR.y, visibleR.width, visibleR.height );
            }
            else {
                g.setClip( 0, 0, compositeImage.getWidth(), compositeImage.getHeight() );
            }
        }
        paint( g, c );
    }

    // Hijack painting when animating
    @Override
    public void paint( Graphics g, JComponent c ) {
        if ( animationState != AnimatedTreeUI.AnimationState.NOT_ANIMATING ) {
            g.drawImage( compositeImage, 0, 0, null );
            return;
        }
        else {
            try {
                super.paint( g, c );
            }
            catch ( Exception e ) {
                try {
                    Thread.sleep( 500 );
                    super.paint( g, c );
                }
                catch ( Exception e1 ) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
