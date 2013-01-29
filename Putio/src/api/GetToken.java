package api;

import javax.swing.JOptionPane;

public class GetToken
{
	private static String url = Requestor.getBaseAPI() + "oauth2/authenticate?client_id=289&response_type=token&redirect_uri=http://waynersh.free.fr/put-io-api/callback.php";
	
	public static void browse() {
		if( !java.awt.Desktop.isDesktopSupported() )
        	JOptionPane.showMessageDialog(null, "Desktop is not supported", "Error", JOptionPane.ERROR_MESSAGE);
		
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if( !desktop.isSupported( java.awt.Desktop.Action.BROWSE ) )
        	JOptionPane.showMessageDialog(null, "Desktop doesn't support the browse action", "Error", JOptionPane.ERROR_MESSAGE);

        try {
            java.net.URI uri = new java.net.URI( url );
            desktop.browse( uri );
        }
        catch ( Exception e ) {
        	JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
	}
}