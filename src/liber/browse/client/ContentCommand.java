package liber.browse.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Command;

import java.util.List;
import java.util.ArrayList;
import java.lang.StringBuffer;

import liber.edit.client.AnchorInfo;
 
/**
 * 	Executed when an  browsing option ('show/hide information') from a PopupMenu is selected. 
 *
 *	@author	Feikje Hielkema
 *	@version 1.2 March 17, 2008
 */
public class ContentCommand implements Command
{
	protected int type = 0; //session type
	protected String key;	//unique identifier
	private String anchor;
	private WysiwymTab tab;
	private WYSIWYMinterface parent;
	private PopupMenu menu;
	private boolean showOption;
	
	/**	Constructor
	 *	@param a Anchor ID
	 *	@param wt Parent window
	 *	@param wysInt EntryPoint
	 *	@param m PopupMenu
	 *	@param s Session type (edit, query, browse)
	 */
	public ContentCommand(String a, WysiwymTab wt, WYSIWYMinterface wysInt, PopupMenu m, boolean s)
	{
		anchor = a;
		showOption = s;
		tab = wt;
		parent = wysInt;
		menu = m;
		key = tab.getKey();
		if (tab instanceof BrowsingTab)
			type = wysInt.BROWSE_TAB;
	}

	/**	Executes the command, when the user selects the menu item
	 */
	public void execute()
	{	//if it's a datatype, let the user specify a value, then update the feedback text
		menu.hide();
		parent.showHourglass();
		
		String user = parent.getUser();
		if (user == null)
			return;
		parent.getHandler().changeTextContent(user, anchor, showOption, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			if ((o != null) && (o instanceof AnchorInfo[]))
    			{
    				AnchorInfo[] info = (AnchorInfo[]) o;
    				if (info[info.length - 1] == null)
    				{
    					AnchorInfo[] copy = new AnchorInfo[info.length - 1];
    					for (int i = 0; i < (info.length - 1); i++)
    						copy[i] = info[i];
    					o = copy;
    					Window.alert("There is no more information in the Archive about this object. If you want, you can add some information yourself.");
    				}
    			}
    			tab.regenerateFeedbackText(o, false, false);
    		}
	   		
	   		public void onFailure(Throwable caught) 
	   		{
	   			parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
	}
}