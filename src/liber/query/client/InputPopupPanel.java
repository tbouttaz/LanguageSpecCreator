package liber.query.client;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;

/**	A popup panel that does not 'eat' events happening outside it
 *
 *	@author	Feikje Hielkema
 *	@version 1.0 April 6, 2007
 */
public class InputPopupPanel extends PopupPanel
{
	private boolean showing = false;
	
	/**	@return true
	 *	@link PopupPanel#onEventPreview(Event)
	 */
	public boolean onEventPreview(Event event) 
	{
   		return true;
   	}
   	
   	/**	Show the panel
   	 */
   	public void show()
   	{
   		super.show();
   		showing = true;
   	}
   	
   	/** Hide the panel
   	 */
   	public void hide()
   	{
   		super.hide();
   		showing = false;
   	}
   	
   	/**	Check whether the panel is showing
   	 *	@return true if panel is visible
   	 */   	
   	public boolean isShowing()
   	{
   		return showing;
   	}
}