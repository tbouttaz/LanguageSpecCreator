package liber.edit.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;

/**	Represents a label in a tag cloud. When clicked, the label appears in 
 *	the suggestbox.
 *	@author Feikje Hielkema
 *	@version 1.0 May 2007
 *	@version 1.3 May 2008
 */
public class FolksonomyLabel extends Label
{
	private SuggestBox sb;
	private int idx;
	private ObjectPropertyCommand opc;
	
	/**	Constructor
	 *	@param text Label
	 *	@param s SuggestBox
	 */
	public FolksonomyLabel(String text, SuggestBox s)
	{
		super(text);
		sb = s;
		addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (sb != null)
					sb.setText(((Label)sender).getText());
			}
		});
	}
	
	/**	Constructor
	 *	@param text Label
	 *	@param s SuggestBox
	 *	@param o ObjectPropertyCommand, which contains the SuggestBox
	 *	@param i Index in ObjectPropertyCommand
	 */
	public FolksonomyLabel(String text, SuggestBox s,  ObjectPropertyCommand o, int i)
	{
		super(text);
		sb = s;
		idx = i; 
		opc = o;
		
		addClickListener(new ClickListener()
		{
			public void onClick(Widget sender)
			{
				if (sb != null)
				{
					sb.setText(((Label)sender).getText());
					opc.getInformation(idx);						
				}
			}
		});  					
	}
}