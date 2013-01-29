package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class AboutScreen extends JFrame {
    private static final long serialVersionUID = 3271333970390339799L;

    // Content area
    protected JPanel contentPanel;

    // About text
    protected JPanel aboutTextPanel;
    protected JLabel aboutTextLabel;

    public AboutScreen() {
	// Content
	contentPanel = new JPanel();
	contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));
	contentPanel.setBorder(new LineBorder(Color.black, 1));

	// About text
	aboutTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	aboutTextLabel = new JLabel("<HTML><CENTER><STRONG>put.io Download Manager 0.1.3</STRONG><BR>" +
			"Build date: 08.11.2012<BR>" +
			"<BR>" +
			"Author: Günhan Özden<BR>" +
			"Wrote this small program with love and hopes,<BR>" +
			"it makes your life a little bit easier. Enjoy it!<BR>" +
			"This program is completely free<BR>" +
			"<BR>" +
			"<STRONG>Libraries</STRONG><BR>" +
			"<STRONG>libputio 0.1</STRONG> - http://sourceforge.net/projects/libputio<BR>" +
			"<STRONG>Flexjson 2.0</STRONG> - http://flexjson.sourceforge.net</CENTER></HTML>", JLabel.CENTER);
	aboutTextLabel.setHorizontalAlignment(JLabel.CENTER);
	aboutTextPanel.add(aboutTextLabel);

	// Add to content pane
	contentPanel.add(aboutTextPanel);

	this.getContentPane().add(contentPanel, BorderLayout.CENTER);

	this.setTitle("About Program");
	this.setSize(new Dimension(300, 200));
	this.setResizable(false);
    }

}