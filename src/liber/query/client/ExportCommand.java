package liber.query.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
 
/**
 * 	Executed when the 'add to describe your resource' option is selected from
 *	the popup menu. Used by previous version of LIBER
 *
 *	@author Feikje Hielkema
 *	@version 1.3 May 27, 2008
 *
 *	@deprecated
 */
public class ExportCommand implements Command
{
	protected int type = WYSIWYMinterface.BROWSE_TAB;
	protected String key;
	private String anchor;
	private WYSIWYMinterface parent;
	private PopupMenu menu;
	
	/**	Constructor
	 *	@param a Anchor ID
	 *	@param wt Parent window
	 *	@param wysInt EntryPoint
	 *	@param m PopupMenu
	 */
	public ExportCommand(String a, WysiwymTab wt, WYSIWYMinterface wysInt, PopupMenu m)
	{
		anchor = a;
		parent = wysInt;
		menu = m;
		key = wt.getKey();
	}
	
	public void execute()
	{
		menu.hide();
		parent.showHourglass();
		
		String user = parent.getUser();
		if (user == null)
			return;
		parent.getHandler().exportObject(user, anchor, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			if (o == null)
    			{
    				parent.hideHourglass();
    				Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   					parent.close();
   				}
   				else 
   				{
   					parent.hideHourglass();
   					if (((Boolean)o).booleanValue())
   						Window.alert("The object has been added to the information underlying the 'Describe your resource' panel. " +
   						"If you choose an applicable menu item, the object will appear in the list of possible values.\n" +
   						"For instance, if the object you selected was a person, and you select 'has author' in the description panel, " +
   						"the person would appear in the list of options.");
   					else
   						Window.alert("There was an error in exporting this object to the 'Describe your resource' panel. " +
   						"It could be caused by the absence of a description in the 'Describe your resource' panel. " +
   						"If you have not done so yet, please select a resource type in that panel and click 'ok', then try again.");
   				}
    		}
	   		
	   		public void onFailure(Throwable caught) 
	   		{
	   			parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
	}
}