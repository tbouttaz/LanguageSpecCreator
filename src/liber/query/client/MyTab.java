package liber.query.client;

import com.google.gwt.user.client.ui.VerticalPanel;

/**	Extends VerticalPanel, parent of editing, querying and browsing tabs.
 *	Used by previous version of LIBER that integrated all three modules
 *	in a tabbed interface; the methods showPopup() and hidePopup() were
 *	meant to be overloaded by each child class to arrange the hiding and
 *	appearance of each tab's pop-ups when the user switched tabs.
 *	Currently still the parent class of the modules, but with no real
 *	purpose.
 */
public class MyTab extends VerticalPanel
{
	public MyTab()
	{
		super();
	}
	
	public void showPopup()
	{}
	
	public void hidePopup()
	{}
}