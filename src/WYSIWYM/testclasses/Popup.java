package WYSIWYM.testclasses;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JFrame;

import liber.edit.client.AnchorInfo;

/**	Shows a pop-up with a LIBER browsing text. Used to generate texts for the
 *	text comparison experiment.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 January 2008
 */
 public class Popup extends JFrame
 {
 	private Test parent;
 	
 	/**	Constructor.
 	 *	@param parent Parent Frame
 	 */
	public Popup(Test parent)
 	{
 		super("Browsing text");
 		this.parent = parent;
 		setBounds(100, 100, 500, 300);
 		setResizable(true);
		setBackground(Color.white);
		getContentPane().setBackground(Color.white);
 	}
	
	/**	Show the frame with the given feedback text
	 *	@param text AnchorInfo[] LIBER feedback text
	 */
	public void show(AnchorInfo[] text)
	{
		Container content = getContentPane();
		content.removeAll();
 		content.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
 		for (int i = 0; i < text.length; i++)
		{
			LiberLabel lb = new LiberLabel(text[i], parent);
			content.add(lb);
		}		
		content.validate();
		setVisible(true);
	}
 }