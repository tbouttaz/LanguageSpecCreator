package liber.edit.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
 
/**
 * 	Displays the help search results and helps the user apply them to the
 *	description he already has.
 *	
 *	Left-over from previous version of LIBER that integrated all three
 *	modules. See querying module for documentation.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 26 March 2008
 *	@version 1.3 May 16, 2008
 *	@deprecated
 */
public class QueryResult extends MyDialogBox
{
	private WYSIWYMinterface parentWidget;
	private int y = (Window.getClientHeight() / 2) - 100;
	private QueryTab tab;
	private Help help;
	
	public QueryResult(AnchorInfo[][] results, WYSIWYMinterface p, QueryTab qt)
  	{
  		super(false, false);
  		setHeight(y);
  		setWidth((Window.getClientWidth() / 3) * 2);
  		parentWidget = p;
  		tab = qt;
  		setText("The following resources matched your query:");
  		setPopupPosition(50, y);
  		help = new Help(p, WYSIWYMinterface.BROWSE_TAB);
   		
   		VerticalPanel vp = new VerticalPanel();
   	//	vp.setSpacing(10);
   				
   		HorizontalPanel buttons = new HorizontalPanel();
   		Button reset = new Button("Start new query", new ClickListener()
   		{
   			public void onClick(Widget sender)
   			{
   				parentWidget.showHourglass();
   				hide();
   				tab.reset();
   			}
   		});
   		Button helpBtn = new Button("Help!", new ClickListener()
   		{
   			public void onClick(Widget sender)
   			{
   				help.showTabHelp();
   			}
   		});
   		
   		buttons.add(reset);
   		buttons.add(helpBtn);
   		buttons.setSpacing(20);
   		vp.add(buttons);		
   		
   		for (int i = 0; i < results.length; i++)
   		{	
   			StringBuffer sb = new StringBuffer();
   			String id = null;
   			HTML link = null;
   			
   			for (int j = 0; j < results[i].length; j++)
   			{
   				if (results[i][j].isAnchor())
   				{	//the header should be a hyperlink, that the user can click to get the full description
   					link = new HTML(results[i][j].getWords());
   					link.setStyleName("wysiwym-label-blue");
   					link.addClickListener(new moreClickListener(i, results[i][j].getWords()));
   					id = HTMLPanel.createUniqueId();
   					
   					sb.append("<span id='");
	   				sb.append(id);
	   				sb.append("'></span>");
   				}
   				else	//other things can just be added to the text
	   				sb.append(results[i][j].getWords());
   			}	
   					
 	  		VerticalPanel hp = new VerticalPanel();
 	  		HTMLPanel panel = new HTMLPanel(sb.toString());
 	  		if ((link != null) && (id != null))
	 	  		panel.add(link, id);
 	  		hp.add(panel);
 	  		vp.add(hp);
   		}
   		
   		setWidget(vp);
   		parentWidget.hideHourglass();
   		show();	
  	}
  		
  	private class moreClickListener implements ClickListener
  	{
  		int index;
  		String text;
  		
  		public moreClickListener(int idx, String t)
  		{
  			index = idx;
  			text = t;
  		}
  		
  		public void onClick(Widget sender)
  		{
  			final String userID = parentWidget.getUser();
			if (userID == null)
				return;
			parentWidget.showHourglass();
			parentWidget.getHandler().getBrowsingDescription(userID, index, new AsyncCallback()
     	//	parentWidget.getHandler().getBrowsingText(userID, index, new AsyncCallback()
			{
   				public void onSuccess(Object o) 
   				{   	
   					if (o == null)	//signal that it is impossible to undo any more operations, because of the error
   	 				{
   	 					parentWidget.hideHourglass();
   	 					Window.alert("Error occurred when trying to extract data from the database; the resource's description must be malformed.");
   	 					return;
   	 				}	
   	 				AnchorInfo[] result = (AnchorInfo[]) o;	
	    			if (result.length == 0)
   					{
   						Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   						parentWidget.close();
   					}
    				else
    					new BrowsingBox(parentWidget, result, text);
    			}
    			
	   			public void onFailure(Throwable caught) 
	   			{
	   				Window.alert(caught.getMessage());
	   			}
  			});
  		}
  	}
}