package liber.query.client;

import liber.edit.client.AnchorInfo;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * 	A label that, if clicked, opens a popup menu
 *
 *	@author	Feikje Hielkema
 *	@version 1.0 December 6, 2006
 */
public class WysiwymLabel extends FocusWidget	//extends Label
{
	private PopupMenu menu;
	private WysiwymTab wt;
	private WYSIWYMinterface parent;
	private AnchorInfo anchorInfo;
  	
  	/**	Constructs a FocusWidget with the given label and anchor, to add to the
  	 *	WYSIWYM feedback text
  	 *	@param words Label
  	 *	@param ai AnchorInfo, contains Anchor
  	 *	@param w parent module
  	 *	@param par Entrypoint
  	 */	
  	public WysiwymLabel(String words, AnchorInfo ai, WysiwymTab w, WYSIWYMinterface par)
  	{
  		super(DOM.createDiv());
	    sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS | Event.ONMOUSEWHEEL);
	    setStyleName("wysiwym-label-large");
		setText(words);
		
  		wt = w;
  		anchorInfo = ai;
  		parent = par;
  		if (ai.isRed())
			setStyleName("wysiwym-label-red");
		else
			setStyleName("wysiwym-label-blue");
			
  		addClickListener(new ClickListener()
  		{
  			public void onClick(Widget sender)
  			{
  				if (wt.popupShowing())
  					return;			//if there is a panel showing of another menu item, do nothing
  				if (menu == null)
  					menu = new PopupMenu(anchorInfo, wt, parent);
  				((WysiwymLabel) sender).showMenu();
  			}
  		});
  		addKeyboardListener(new LabelListener());			
  	}
 
  	/**	Checks whether the label should be red
   	 *	@return True if red
   	 */
  	public boolean isRed()
  	{
  		return anchorInfo.isRed();
  	}
  	
  	/**	Sets the text of the label
  	 *	@param text Text
  	 */
  	public void setText(String text) 
  	{
    	DOM.setInnerText(getElement(), text);
  	}
  	
  	/**	Attempt to implement version of WYSIWYM that does not require the mouse;
  	 *	user should be able to hop from anchor to anchor with the tab key, and open
  	 *	a menu using the space bar. Never could get the menu opening to work, but it is theoretically
  	 *	possible.
  	 */
  	public class LabelListener implements KeyboardListener
  	{
  		public void onKeyDown(Widget sender, char keycode, int modifiers)
  		{}
  		
  		public void onKeyPress(Widget sender, char keycode, int modifiers)
  		{}
  		
  		public void onKeyUp(Widget sender, char keycode, int modifiers)
  		{
  			if ((keycode == (char) KeyboardListener.KEY_ENTER) || (keycode == ' ')) //??
  			{
  				if (menu == null)
  					menu = new PopupMenu(anchorInfo, wt, parent);
  				((WysiwymLabel) sender).showMenu();
  			}
  		}
  	}
	
	/**	Show the pop-up menu, with the top left corner touching this label.
	 */
  	public void showMenu()
  	{
  		int left = getAbsoluteLeft() + 50;
  		int top = getAbsoluteTop() + 10;
  		
  		if ((getAbsoluteLeft() + getOffsetWidth()) > Window.getClientWidth())
  		{	//if the anchor runs onto the next line, open the menu there
  			left = 50;
  			top += 30;
  		}
  		else if ((Window.getClientWidth() - left) < 100)
  			left = Window.getClientWidth() - 100;
    	
    	menu.setPopupPosition(left, top);
  		wt.setActiveLabel(this);
	  	menu.show();
  	}
  	
  	/**	Hide the menu
  	 */
  	public void hideMenu()
  	{
  		menu.hide();
  	}
}