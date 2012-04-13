package liber.edit.client;

import com.google.gwt.user.client.ui.PopupPanel;
 
/**
 * 	Defines the popup menu widget for the wysiwym feedback text
 *
 *	@author	Feikje Hielkema
 *	@version	1.0 December 6, 2006
 *
 *	@version	1.2	March 26, 2008
 */
public class PopupMenu extends PopupPanel
{
  	WysiwymMenuBar menu, parentBar;
   	
   	/**	Constructor.
   	 *	@param ai AnchorInfo, contains label and Anchor
   	 *	@param w Parent module
   	 *	@param parent Entrypoint
   	 */
  	public PopupMenu(AnchorInfo ai, WysiwymTab w, WYSIWYMinterface parent) 
   	{
   		super(true);
   		setStyleName("ks-popups-Popup");
   		WysiwymMenuBar menu = new WysiwymMenuBar(true);  		
   		menu.setWidth("100%");
		menu.setAutoOpen(true);
		setWidget(menu);
   		
   		if (ai.isSummation())	//if this is a summation anchor, only add one option
			menu.addItem("Show all", true, new SummationCommand(ai.getID(), w, parent, this));
   		else
   		{	//add browsing options
   			if (ai.getShowHide() > 2)		//information about this node is hidden or unretrieved, so give user the option to view it
 				menu.addItem("Show more information", true, new ContentCommand(ai.getID(), w, parent, this, true));
 			else if (ai.getShowHide() == 1)	//old information about this node is visible, so give user the option to hide it
 				menu.addItem("Hide this information", true, new ContentCommand(ai.getID(), w, parent, this, false));
				
			if (ai.getURI() != null)	//if this anchor is a downloadable resource, add an option
	   			menu.addItem("Download", true, new DownloadCommand(ai.getURI(), ai.getWords(), this)); 

	 	    String[] compulsory = ai.getCompulsory();
 		    for (int i = 0; i < compulsory.length; i++)
 	    	{	//add compulsory options, in red boldface
 	    		WysiwymCommand cmd = createCommand(compulsory[i], ai.getCompulsoryNL()[i], ai.getID(), w, parent);
			    menu.addItem("<font color='red'><b>" + ai.getCompulsoryNL()[i] + "</b></font>", true, cmd);    
			}
				
	 		String[] optional = ai.getOptional();
	 		String[] nlexpr = ai.getOptionalNL();
	 		String[] propType = ai.getOptionalPropType(); 	
		
			for (int i = 0; i < propType.length; i++)
			{	//add the optional properties without a sub-menu
				if (propType[i].equals("-NONE-"))
				{
					WysiwymCommand cmd = createCommand(optional[i], nlexpr[i], ai.getID(), w, parent);
					menu.addItem(nlexpr[i], true, cmd);
				}
			}
			WysiwymMenuBar subMenu = new WysiwymMenuBar(true);
	 		for (int i = 0; i < optional.length; i++)
	 		{	//add the sub-menus with their properties
 			   	if ((i > 0) && (!propType[i].equals(propType[i - 1])) && (!propType[i - 1].equals("-NONE-")))
 			    {	//starting on new submenu, so add the previous submenu, then create a new one
 		   			StringBuffer sb = new StringBuffer(propType[i - 1]);
    				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    				sb.append(":</b>");
    	   			menu.addItem("<b>" + sb.toString(), true, subMenu);
 		   			subMenu = new WysiwymMenuBar(true);
 		   		}
 		   		if (propType[i].equals("-NONE-"))
	 				continue;
 		   	
 			   	WysiwymCommand cmd = createCommand(optional[i], nlexpr[i], ai.getID(), w, parent);
 			   	subMenu.addItem(nlexpr[i], true, cmd);
 			}
 			int propNr = propType.length;
 			if ((propNr > 0) && (!propType[propNr - 1].equals("-NONE-")))
 			{
	 			StringBuffer sb = new StringBuffer(propType[propType.length - 1]);
    			sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    			sb.append(":</b>");
 				menu.addItem("<b>" + sb.toString(), true, subMenu);	//add last submenu
			}
			
			if (ai.isRemovableNode())	//add 'remove' options
				menu.addItem("Remove this anchor", true, new UndoCommand(ai.getID(), w, parent, this));
			WysiwymMenuBar undo = new WysiwymMenuBar(true);
			String[] removable = ai.getRemovable();
			String[] removableNL = ai.getRemovableNL();
			for (int i = 0; i < removable.length; i++)
			{
				UndoCommand cmd = new UndoCommand(ai, ai.getID(), removable[i], removableNL[i], i, w, parent, this);
				undo.addItem(removableNL[i], true, cmd);
			}
			if (removable.length > 0)
				menu.addItem("Remove the information:", true, undo);
		}
  	}
  	
  	/**	Create a editing, browsing or querying command. This method ensures all commands for the modules
  	 *	are of the correct type.
  	 *	@param prop Property name
  	 *	@param nl NL-expression of property
  	 *	@param anchor Anchor ID
  	 *	@param w Parent module
  	 *	@param parent Entrypoint
  	 */
  	private WysiwymCommand createCommand(String prop, String nl, String anchor, WysiwymTab w, WYSIWYMinterface parent)
  	{
  		if (w instanceof QueryTab)
  			return new QueryCommand(prop, nl, anchor, w, parent, this);
  		else if (w instanceof BrowsingTab)
  			return new BrowseCommand(prop, nl, anchor, w, parent, this);
   		else
  			return new WysiwymCommand(prop, nl, anchor, w, parent, this);
  	}
}