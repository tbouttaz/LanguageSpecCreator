package liber.query.client;

import java.util.ArrayList;
import java.util.List;

import liber.edit.client.AnchorInfo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 	Entrypoint of the LIBER editing module
 *
 *	@author Feikje Hielkema
 *	@version 1.0 December 6, 2006
 *
 *	@version 1.2 December 2007
 *
 *	@version 1.3 March 2008
 *
 *	@version 1.4 July 2008
 *
 *	@version 1.5 August 2008
 */
public class WYSIWYMinterface implements EntryPoint, WindowCloseListener
{
	//	Remnant of previous LIBER version that integrated all three modules. Still used to identify different session types.
	public static final int EDIT_TAB = 0;	//unexplained mystery: the TabBar seems to have only tabs with even numbers...
	public static final int QUERY_TAB = 2;
	public static final int HELP_TAB = 4;
	public static final int BROWSE_TAB = 4;
	
  	private LiberServletAsync liber;
  	private QueryTab qt;
 
 	private List browseTabs = new ArrayList();
  	public static final String USER = "wysiwym_user";
  	
  	public PopupPanel waitPanel = new PopupPanel(false, true);	//grabs focus and eats events when hourglass is displayed, to enforce waiting
  	private long startTime = 0, totalWaitingTime = 0;
  	public long startQuery = 0;
	
	/**
	 * This is the entry point method. It creates the servlet and initialises the session.
	 */
	public void onModuleLoad() 
	{
		liber = (LiberServletAsync) GWT.create(LiberServlet.class);
  		ServiceDefTarget endpoint = (ServiceDefTarget) liber;
  		String moduleRelativeURL = GWT.getModuleBaseURL() + "/wysiwym2";
  		endpoint.setServiceEntryPoint(moduleRelativeURL);
		init();
		Window.addWindowCloseListener(this);
	}
  	
  	/**	Initialise querying session
  	 */
  	private void init()
  	{
  		waitPanel.setWidget(new VerticalPanel());	//waitpanel ensures that while hourglass is showing, the user cannot do anything
  		waitPanel.setPopupPosition(Window.getClientWidth(), Window.getClientHeight());
  		showHourglass();

		qt = new QueryTab(this);
		RootPanel.get().add(qt);
		qt.init();
  	}
  	
  	/**	Reads the user ID from a Cookie. If there is no Cookie, displays
  	 *	a message that the session has expired.
  	 *	@return String user ID, or null if there is no cookie
  	 */		
  	public String getUser()
  	{
  		String result = Cookies.getCookie(USER);
  		if (result == null)
	  		Window.alert("I'm sorry, but your session has expired. Please close the window to go back to ourSpaces.");
	  	else
	  		result = result.replaceAll("\"", "");
  		return result;
  	}
  	
  	/**	Returns the name of the user.
  	 *	@return String username, or null if the session is expired.
  	 */	
  	public String getUserName()
  	{
  		String id = getUser();
  		if (id != null)
  			return qt.getUserName(id);
  		return null;
  	}
	
	/**	Returns the servlet
	 *	@return LiberServletAsync
	 */
	public LiberServletAsync getHandler()
	{
		return liber;
	}
	
	/**	Submits the query.
	 *	@param type Session type (edit, query, browse)
	 *	@param key Session ID
	 */
	public void submit(final int type, final String key)
	{
		submit(type, key, null);
	}
	
	/**	Closes LIBER
	 */
	native public static void close()/*-{
 		$wnd.close();
	}-*/; 
	
	/**	Submits the query.
	 *	@param type Session type
	 *	@param key Session ID
	 *	@param bt null
	 */
	public void submit(final int type, final String key, final BrowsingTab bt)
	{
		String userID = getUser();
		if (userID == null)
			return;
		
		long time = System.currentTimeMillis() - startQuery;
		getHandler().getQueryResult(userID, time, new AsyncCallback()
		{
 			public void onSuccess(Object o) 
    		{
    			if (o == null)
    			{
    				Window.alert("I'm sorry, but your session has expired. Please return to ourSpaces to start a new session.");
    				hideHourglass();
  					close();
    			}
    			else
	    			qt.showResult((AnchorInfo[])o);
    		}	
		
			public void onFailure(Throwable caught) 
			{
				Window.alert(caught.getMessage());
			}
  		});
	}
	
	/**	Adds a browsing tab to display
	 *	@param bt BrowsingTab
	 */
	public void addBrowsingTab(BrowsingTab bt)
	{
		browseTabs.add(bt);
	}
	
	/**	@deprecated
	 */
	public void setMessage(String user)//, String resource, String title)
	{
	//	help.setMessage(user);
	}

	/**	Shows an hourglass or waiting cursor, and ensures
	 *	the user cannot do anything while it is displayed
	 */
	public void showHourglass()
  	{
  		RootPanel.get().addStyleName("hourglass");
  		waitPanel.show();
  		if (startTime == 0)
	  		startTime = System.currentTimeMillis();
  	}
  	
  	/**	Sets the cursor back to normal
  	 */
  	public void hideHourglass()
  	{
  		waitPanel.hide();
  		RootPanel.get().removeStyleName("hourglass");
  		if (startTime > 0)
  		{
  			long interval = System.currentTimeMillis() - startTime;
	  		totalWaitingTime += interval;
	  	}
	  	startTime = 0;
  	}
  	
  	/**	LIBER counts the time that the hourglass has been showing 
  	 *	(for previous evaluation purposes). This method resets the counter
  	 */
  	public void resetHourglass()
  	{
  		totalWaitingTime = 0;
  	} 	
  	
  	/** Does nothing.
  	 */
  	public void onWindowClosed()
  	{
  	/**	String user = Cookies.getCookie(USER);
  		if (user == null)
  			return;
  		liber.endSession(user, QUERY_TAB, null, new AsyncCallback()	
  		{	//store the user's actions on the server
  			public void onSuccess(Object o) 
    		{}	
			public void onFailure(Throwable caught) 
			{}
  		});
  		Window.alert("Thank you for using LIBER!");*/
  	}
  	
  	/**	Checks if the user has an unsubmitted description. If so, displays warning.
  	 *	@return String warning, or null if the window should be closed.
  	 */
  	public String onWindowClosing()
  	{  	
	  	for (int i = 0; i < browseTabs.size(); i++)
	  	{
		  	if (((BrowsingTab)browseTabs.get(i)).unsubmittedInfo())
		  		return new String("You have not yet submitted the description you made in one of the browsing windows. " +
	  			"If you leave this page, that description will be lost.");
	  	}
	  	
	  	return new String("This means you would leave the LIBER querying tool.");
  	}
}
