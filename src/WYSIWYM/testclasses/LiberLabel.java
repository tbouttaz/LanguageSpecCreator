package WYSIWYM.testclasses;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import liber.edit.client.AnchorInfo;

/**	A label which is black, red or blue, and may pop up a menu when clicked.
 *	@author Feikje Hielkema
 *	@version 1.4 January 2008
 */
 public class LiberLabel extends JLabel
 {
 	private JPopupMenu menu;
 	private Test parent;
 	private AnchorInfo ai;
 	
 	/*	Constructor
 	 *	@param ai AnchorInfo
 	 *	@param parent Parent Frame
 	 */
 	public LiberLabel(AnchorInfo ai, Test parent)
 	{
 		super(ai.getWords());
 		this.parent = parent;
 		this.ai = ai;
 		if (!ai.isAnchor())
 			return;
 		
 		setFont(new Font("Serif", Font.ITALIC, 12));
 		setForeground(Color.blue);
	 	
	 	addMenu();
	 	addMouseListener(new MouseAdapter()
	 	{
	 		public void mousePressed(MouseEvent e)
		 	{
 				if ((menu != null) && (e.getButton() == MouseEvent.BUTTON1))
 					menu.show(e.getComponent(), e.getX(), e.getY());
 			}
	 	});
 	}
 		
 	private void addMenu()
 	{	//add the entries in the anchor to the menu
 		menu = new JPopupMenu();
 		JMenuItem item = null;
 		if (ai.isSummation())	//if this is a summation anchor, only add one option
 		{
			item = new JMenuItem("Show all");
			item.addActionListener(new LiberActionListener(ai.getID(), 1));
		}
   		else if (ai.getShowHide() > 2)		//information about this node is hidden or unretrieved, so give user the option to view it
 		{
			item = new JMenuItem("Show more information");
			item.addActionListener(new LiberActionListener(ai.getID(), 2));
		}
 		else if (ai.getShowHide() == 1)	//old information about this node is visible, so give user the option to hide it
 		{
			item = new JMenuItem("Hide this information");	
			item.addActionListener(new LiberActionListener(ai.getID(), 3));
		}
 		if (item != null)
			menu.add(item);
 	}
 	
 	private class LiberActionListener implements ActionListener
 	{
 		int type;
 		String anchor;
 		
 		public LiberActionListener(String anchor, int type)
 		{
 			this.anchor = anchor;
 			this.type = type;
 		}
 
	 	public void actionPerformed(ActionEvent e)
 		{	
 			parent.update(anchor, type);
 		}
 	}
 }