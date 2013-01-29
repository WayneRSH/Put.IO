package start;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import gui.MainScreen;

public class Start {
    public static MainScreen MS;

    /**
     * @param args
     */
    public static void main(String[] args) {
	/* Use an appropriate Look and Feel */
	try {
	    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
	    //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
	} catch (UnsupportedLookAndFeelException ex) {
	    ex.printStackTrace();
	} catch (IllegalAccessException ex) {
	    ex.printStackTrace();
	} catch (InstantiationException ex) {
	    ex.printStackTrace();
	} catch (ClassNotFoundException ex) {
	    ex.printStackTrace();
	}
	/* Turn off metal's use of bold fonts */
	UIManager.put("swing.boldMetal", Boolean.FALSE);

	MS = new MainScreen();
    }

}