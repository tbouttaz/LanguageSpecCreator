package liber.query.client;

import liber.edit.client.AnchorInfo;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;

/**
 * 	BrowsingTab contains a browsing module. It is contained in a dialog box as part of
 *	the querying module, to present the search results.
 *
 *	@author	Feikje Hielkema
 *	@version 1.2 March 24, 2008
 *	@version 1.3 May 21, 2008
 */
public class BrowsingTab extends WysiwymTab
{
	private BrowsingBox box;
	
	/**	Constructor.
	 *	@param p Entrypoint
	 *	@param b BrowsingBox, its container (usually DialogBox)
	 *	@param k Unique ID of the browsing session
	 */
  	public BrowsingTab(WYSIWYMinterface p, BrowsingBox b, String k)
  	{
  		super(p);
  		box = b;
  		key = k;
  		type = p.BROWSE_TAB;
  		htmlIndex = 0;
  		submitBtn.setEnabled(false);	//can't submit anything unless you have added to description
  		resetBtn.setEnabled(false);		//nothing to reset in an empty pane
	  	setMessage(null);
	  	help = new Help(p, type);
 	}
  	
  	/**	Overload: Makes sure no message is displayed
  	 *	@param user User ID, may be null.
  	 */
  	public void setMessage(String user)
  	{
  		remove(top1);
  	}
  	
  	/**	Resets the tab to an empty text; no reason to use this method.
  	 */	
  	public void reset()
  	{
  		undo = false;
  		resetBtn.setEnabled(false);
  		submitBtn.setEnabled(false);
  		super.initPanel(new AnchorInfo[0], undo);
  	}
  	
  	/**	Closes this module
  	 */
  	public void close()
  	{
  		box.hide();
  	}
  	
  	/**	Click listener. 
  	 */
  	public void onClick(Widget sender)
  	{
  		if (sender == resetBtn)
  		{
  			if (Window.confirm("Are you sure you want to reset? This will remove all descriptions from this tab, and delete any descriptions you may have added."))
  				reset();
  		}
  		else if (sender == submitBtn)	//convert the semantic graph to RDF
     	{	
   			if (Window.confirm("Are you sure you want to submit your description?"))
   			{
   				parentWidget.showHourglass();	
  				parentWidget.submit(type, key, this);
   			}
   			else
   			{
   				String user = parentWidget.getUser();
				if (user == null)
					return;
   			}
     	}
     	else
  			super.onClick(sender);
  	}
}
  	