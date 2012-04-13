package liber.edit.client;

import java.util.ArrayList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
 
/**
 * 	Executed when a 'remove information' menu item from a PopupMenu is selected. 
 *
 *	@author Feikje Hielkema
 *	@version 1.0 November 5th, 2007
 */
public class UndoCommand implements Command, ClickListener
{
	protected int type = 0;
	protected String key;
	private int buttonsPressed = 0;
	private String anchor;
	private String property, nlExpression;

	private WysiwymTab tab;
	private WYSIWYMinterface parent;
	private PopupMenu menu;
	private String[] values;
	private Help help;

	private InputPopupPanel pp;
	private ListBox lb;
	private Button selectAll = new Button("Remove all values", this);
	private Button selectSome = new Button("Remove selected values", this);
	private Button cancel = new Button("Cancel", this);
	private Button helpBtn = new Button("Help!", this);
	
	/**	Constructor.
	 *	@param a Anchor ID
	 *	@param wt Parent module
	 *	@param wysInt Entrypoint
	 *	@param m PopupMenu
	 */
	public UndoCommand(String a, WysiwymTab wt, WYSIWYMinterface wysInt, PopupMenu m)
	{
		anchor = a;
		tab = wt;
		parent = wysInt;
		menu = m;
		key = tab.getKey();
		if (tab instanceof QueryTab)
			type = wysInt.QUERY_TAB;
		else if (tab instanceof BrowsingTab)
			type = wysInt.BROWSE_TAB;
		help = new Help(parent, type);
	}
	
	/**	Constructor.
	 *	@param ai AnchorInfo
	 *	@param a Anchor ID
	 *	@param p Property name
	 *	@param nl NL-expression of property
	 *	@param idx Index of property in the AnchorInfo, so emovable values for this property can be extracted
	 *	@param wt Parent module
	 *	@param wysInt Entrypoint
	 *	@param m PopupMenu
	 */
	public UndoCommand(AnchorInfo ai, String a, String p, String nl, int idx, WysiwymTab wt, WYSIWYMinterface wysInt, PopupMenu m)
	{
		this(a, wt, wysInt, m);
		property = p;
		nlExpression = nl;
		if (ai != null)
			values = ai.getRemovableValues()[idx];
	}
	
	/**	Executes menu item. If the item was 'remove anchor', tell server to remove the node
	 *	with that anchor from the graph; if the item was 'remove property', check (if there is
	 *	more than 1 value) which values the user wishes to remove, and tell the server.
	 */
	public void execute()
	{	//if it's a datatype, let the user specify a value, then update the feedback text
		menu.hide();
		parent.showHourglass();
		buttonsPressed = 0;
		
		if (property == null)
			removeAnchor();
		else if (values.length == 1)
			removeProperty(values);
		else
			showValues();		
	}
	
	/**	Remove given values of the property
	 */
	private void removeProperty(String[] selected)
	{
		String user = parent.getUser();
		if (user == null)
			return;

		parent.getHandler().removeProperty(user, anchor, property, selected, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			tab.regenerateFeedbackText(o, true, true);
    		}
	   		
	   		public void onFailure(Throwable caught) 
	   		{
	   			parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
	}
	
	/**	Tells the server to remove the object corresponding to the anchor, and all
	 *	information associated with it.
	 */
	private void removeAnchor()
	{
		String user = parent.getUser();
		if (user == null)
			return;
			
		parent.getHandler().removeAnchor(user, anchor, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			tab.regenerateFeedbackText(o, true, true);
    		}
	   		
	   		public void onFailure(Throwable caught) 
	   		{
	   			parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
	}
	
	/**	Check whether the pop-up is displayed
	 */
	public boolean isShowing()
   	{
   		if (pp != null)
	   		return pp.isShowing();
	   	return false;
   	}
	
	/**	Shows the values to the user and enquires which should be deleted
	 */
	private void showValues()
	{
		pp = new InputPopupPanel();
   		pp.setStyleName("wysiwym-popup-textbox");
   		
   		VerticalPanel vp = new VerticalPanel();
   		vp.add(new HTML("You have supplied a number of values for the property '" + nlExpression 
   						+ "'.<br>Please select the values you wish to remove.")); 
   		lb = new ListBox();
   		for (int i = 0; i < values.length; i++)
   			lb.addItem(values[i]);
   		lb.setVisibleItemCount(lb.getItemCount());	//set all items to be visible
   		lb.setMultipleSelect(true);
   		vp.add(lb);
   		vp.setSpacing(10);
   		
   		HorizontalPanel hor = new HorizontalPanel();
   		hor.add(selectAll);
   		hor.add(selectSome);	
   		hor.add(cancel);
   		hor.add(helpBtn);
   		hor.setSpacing(10);
   		vp.add(hor); 		
   		
   		selectAll.setFocus(true);
   		pp.add(vp);
   		pp.setPopupPosition(Window.getClientWidth() / 100, 50);
   		parent.hideHourglass();
   		pp.show();
	}
	
	/**	Should be called when user clicks 'remove all values' or 'remove selected
	 *	values' (or cancel, obviously). Tells server what to remove.
	 *	@param sender Button Widget
	 */
	public void onClick(Widget sender)
	{
		buttonsPressed++;
		if (sender == selectAll)
		{
			pp.hide();
			parent.showHourglass();
			removeProperty(values);
		}
		else if (sender == selectSome)
		{
			if (lb.getSelectedIndex() < 0)
			{
				Window.alert("Please select the values you wish to remove, or click 'cancel'.");
				return;
			}
			
			pp.hide();
			parent.showHourglass();
			ArrayList selected = new ArrayList();
			for (int i = 0; i < lb.getItemCount(); i++)
				if (lb.isItemSelected(i))
					selected.add(lb.getItemText(i));
			String[] array = new String[0];
			removeProperty((String[])selected.toArray(array));
		}
		else if (sender == cancel)
			pp.hide();
		else if (sender == helpBtn)
			help.showUndoHelp();
	}
}
	