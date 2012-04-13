package SpecificationCreation.look;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import simplenlg.realiser.AnchorString;
import WYSIWYM.model.TemplateAnchor;

/**	LiberLabel is a label which is red or blue, and may pop up a menu when clicked.
 *	@author Feikje Hielkema
 * 	@version 1.0 10/11/2008
 */
 public class LiberLabel extends JLabel
 {
 	private TemplateAnchor anchor;
 	private JPopupMenu menu;
 	private SpecificationPane parent;
 	
 	/**	Constructor with value and parent frame
 	 *	@param as AnchorString contains label and anchor
 	 *	@param parent Parent Frame
 	 */
 	public LiberLabel(AnchorString as, SpecificationPane parent)
 	{
 		super(" [" + as.toString() + "] ");
 		this.parent = parent;
 		anchor = (TemplateAnchor) as.getAnchor();
 		if (anchor == null)
 			return;
 		
 		setFont(makeFont());
 		if (anchor.isRed())
	 		setForeground(Color.red);
	 	else
	 		setForeground(Color.blue);
	 	
	 	addMenu();
	 	addMouseListener(new MouseAdapter()
	 		{
	 			public void mousePressed(MouseEvent e)
			 	{
 					if ((menu != null) && (e.getButton() == MouseEvent.BUTTON3))
 						menu.show(e.getComponent(), e.getX(), e.getY());
 				}
	 		});
 	}
 	
 	/**	Sets the proper font (red or black)
 	 */
 	private Font makeFont()
 	{
 		if (anchor.isRed())
 			return new Font("Serif", Font.BOLD, 12);
		return new Font("Serif", Font.ITALIC, 12);
 	}
 	
 	/**	Adds the pop-up menu
 	 */
 	private void addMenu()
 	{	//add the entries in the anchor to the menu
 		menu = new JPopupMenu();
 		for (String entry : anchor.getEntries())
 		{
			JMenuItem item = new JMenuItem(entry);
			item.addActionListener(new LiberActionListener(entry, anchor.getID()));
			menu.add(item);
 		}
 	}
 	
 	/**	Action listener
 	 */
 	private class LiberActionListener implements ActionListener
 	{
 		private String entry, id;
 		
 		public LiberActionListener(String e, String i)
 		{
 			entry = e;
 			id = i;
 		}
 
	 	public void actionPerformed(ActionEvent e)
 		{	
 			parent.elicitValue(entry, id);
 		}
 	}
 }