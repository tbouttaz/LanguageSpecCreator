package liber.query.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

/**	A tree item with a mouse listener, which notices double click events and
 *	notifies the given widget about it.
 *	@author Feikje Hielkema
 *	@version 1.3 June 2008
 */
public class ListeningTree extends Tree
{
	private ResourceTypeElicitor rt;
	private ObjectPropertyCommand opc;
	
	/**	Constructor.
	 *	@param rt ResourceTypeElicitor, widget to be notified about double-click events
	 */
	public ListeningTree(ResourceTypeElicitor rt)
	{
		super();
		this.sinkEvents(Event.ONDBLCLICK); 
		this.rt = rt;
	}
	
	/**	Constructor.
	 *	@param opc ObjectPropertyCommand, widget to be notified about double-click events
	 */
	public ListeningTree(ObjectPropertyCommand opc)
	{
		super();
		this.sinkEvents(Event.ONDBLCLICK); 
		this.opc = opc;
	}
	
	/**	Check if event was a double click, then get the item that was
	 *	clicked on and notify the widget
	 *	@param event Event
	 */
	public void onBrowserEvent(Event event) 
	{
		super.onBrowserEvent(event);	//Handle events as a normal TreeItem would
		TreeItem item = getSelectedItem();
		int type = DOM.eventGetType(event);  // Look at the type of event again
      	switch (type) 
   	  	{
       		case Event.ONDBLCLICK: 
       		{
       			if (item == null)
					Window.alert("Please select an item or press 'cancel'.");
       			else if (rt != null)
	       			rt.initSession(item.getText());
	       		else
	       			opc.createItem(item.getText());
       		}
   		}
   	} 
}