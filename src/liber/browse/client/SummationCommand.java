package liber.browse.client;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 	Executed when a 'show all' option is selected (to show all items in a summation).
 *
 *	@author Feikje Hielkema
 *	@version 1.4 October 1st, 2008
 */
public class SummationCommand implements Command
{
	protected int type = 0;
	protected String key;
	private String anchor;
	private WysiwymTab tab;
	private WYSIWYMinterface parent;
	private PopupMenu menu;
	
	/**	Constructor.
	 *	@param a Anchor ID
	 *	@param wt Parent module
	 *	@param wysInt Entrypoint
	 *	@param m PopupMenu
	 */
	public SummationCommand(String a, WysiwymTab wt, WYSIWYMinterface wysInt, PopupMenu m)
	{
		anchor = a;
		tab = wt;
		parent = wysInt;
		menu = m;
		key = tab.getKey();
		if (tab instanceof BrowsingTab)
			type = wysInt.BROWSE_TAB;
	}
	
	/** Executes the command; tells the server to remove the summation and show all summarised edges.
	 */
	public void execute()
	{	//if it's a datatype, let the user specify a value, then update the feedback text
		menu.hide();
		parent.showHourglass();
		
		String user = parent.getUser();
		if (user == null)
			return;
		parent.getHandler().showSummation(user, anchor, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
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