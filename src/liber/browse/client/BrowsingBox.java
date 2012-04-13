package liber.browse.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.WindowCloseListener;
 import java.util.ArrayList;
 import java.lang.StringBuffer;
 import liber.edit.client.AnchorInfo;

/**
 * 	Displays the full description of a resource, and enables the user to browse.
 *
 *	@author	Feikje Hielkema
 *	@version	1.3 May 16, 2008
 */
public class BrowsingBox extends MyDialogBox
{
	private BrowsingTab bt;
	private ScrollPanel sp;
	private WYSIWYMinterface parent;
	private String id;
	
	/**	Constructor
	 *	@param p Entrypoint
	 *	@param text AnchorInfo[] with feedbacktext
	 *	@param title Window title
	 */
	public BrowsingBox(WYSIWYMinterface p, AnchorInfo[] text, String title)
  	{
  		super(false, false);
  		parent = p;
  		setHeight(Window.getClientHeight() - 80);
  		setWidth(Window.getClientWidth() - 80);
  		setText(title);
  		
  		id = text[text.length - 1].getWords();
  		bt = new BrowsingTab(p, this, id);
  		
  		AnchorInfo[] result = new AnchorInfo[text.length - 1];
  		for (int i = 0; i < result.length; i++)
  			result[i] = text[i];
  		bt.initPanel(result, false);
  		  		
		setCloseListener(new BrowseCloseListener(this, bt));
		p.addBrowsingTab(bt);
		setWidget(bt);
		setPopupPosition(50, 50);
		show();
   	}
 	
 	/**	Closes this browsing window
 	 */			
 	public void close()
 	{
 		hide();
 		String userID = parent.getUser();
		if (userID == null)
			return;
		parent.getHandler().endSession(userID, WYSIWYMinterface.BROWSE_TAB, id, new AsyncCallback()	
		{
			public void onSuccess(Object o)
			{}

			public void onFailure(Throwable caught)
			{}
		});
 	}		
 	
	private class BrowseCloseListener implements ClickListener
  	{
  		BrowsingBox bb;
  		BrowsingTab tab;
  		
  		public BrowseCloseListener(BrowsingBox b, BrowsingTab t)
  		{
  			bb = b;
  			tab = t;
  		}
  		
  		public void onClick(Widget sender)
  		{	//if the user has added some comments, ask him first whether he wants to abandon them
  			if (tab.unsubmittedInfo())
  			{
  				if (Window.confirm("You have not yet submitted the description you made in this browsing window. " +
	  			"If you close it, that description will be lost.\nAre you sure you want to close this window?"))
	  				bb.close();
	  		}
	  		else
	  			bb.close();	
  		}
  	}
}