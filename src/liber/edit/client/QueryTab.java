package liber.edit.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
 
/**
 * 	Defines the query building widget.
 *	Left-over from previous version of LIBER that integrated all three
 *	modules. See querying module for documentation.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 July 17, 2007
 *	@version 1.2 February 1, 2008
 *	
 *	@deprecated
 */
public class QueryTab extends WysiwymTab
{
	private List checkBoxes;	//list of 'optional' checkboxes in the feedback text
	private Label message3;
	private HorizontalPanel message3Panel = new HorizontalPanel();
	
  	public QueryTab(WYSIWYMinterface p)
  	{
  		super(p);
  		type = p.QUERY_TAB;
  		submitBtn.setText("Submit query");
  		help = new Help(p, type);
 	}
  	
  	/* Should ask user for type of resource he's looking for, then create initial 
  	 * query
  	 */
  	public void init(String user, String userID)
  	{
  		ResourceTypeElicitor rt = new ResourceTypeElicitor(user, userID, this, parentWidget);
	  	rt.setCandidates(null);
  		rt.showPanel();
  	}
  	
  	/*	Overload
  	 */
  	public void setMessage(String user, String resource, String title)
  	{
  		message1.setText("Welcome to the PolicyGrid Data Archive, " + user + ".");
  		if (messagePanel.getWidgetCount() > 1)
  		{
			messagePanel.remove(message3Panel);
  			messagePanel.remove(message2);
  		}
  		
  		StringBuffer sb = new StringBuffer("You are searching for a");
  		char c = Character.toLowerCase(resource.charAt(0));
  		if ((c == 'a') || (c == 'e') || (c == 'o') || (c == 'u') || (c == 'i'))
  			sb.append("n");
  		sb.append(" " + resource.toLowerCase());
  		sb.append(". Please describe what you are looking for by editing the query below.");

  		message2 = new Label(sb.toString());
  		message2.setStyleName("wysiwym-label-large");
  		messagePanel.add(message2);
  		messagePanel.add(message3Panel);
  	}
  	
  	/*	Shows the number of resources in the database that match the current query
  	 */
  	public void showMatchNumber(Integer nr)
  	{
  		int i = nr.intValue();
  		if (i == 0)
  			Window.alert("There are no objects in the archive that match your query.\n" +
  				"You may have more luck if you make some of your requirements 'optional' by clicking their checkboxes, " +
  				"or by using 'or' rather than 'and' when specifying values.\n" +
  				"You could also try using the 'any relation' menu item. This checks for any property that relates two objects to each other;" +
  				" for instance, if you specify 'a paper which has any relation with a person called John', the search will return " +
  				"any papers which were deposited, created or written by people called John.");
  				
  		if (message3Panel.getWidgetCount() == 1)
  			message3Panel.remove(message3);
  				
  		StringBuffer sb = new StringBuffer("The archive contains ");
  		sb.append(nr.toString());
  		sb.append(" resource");
  		if (i == 1)
  			sb.append(" that matches");
  		else
  			sb.append("s that match");
  		sb.append(" the query below.");		
  		if (i > 1)
  			sb.append(" Click 'submit' to view their descriptions.");
  		else if (i == 1)
  			sb.append(" Click 'submit' to view its description.");
  			
  		message3 = new Label(sb.toString());
  		message3.setStyleName("wysiwym-label-xlarge");
  		message3Panel.add(message3);
  		message3Panel.setSpacing(20);
  	} 
  	
  	/*	True if the given string denotes some HTML orthography of a list (item)
  	 */
  	private boolean isListOrthography(String str)
  	{
  		if (str.equals("<LI>") || str.equals("</UL>") || str.equals("<UL>"))
  			return true;
  		return false;
  	}
  	
  	public void sendOptionalInformation()
  	{
		Boolean[] result = new Boolean[checkBoxes.size()];
		for (int i = 0; i < checkBoxes.size(); i++)
		{
			if (((CheckBox)checkBoxes.get(i)).isChecked())
				result[i] = new Boolean(true);
			else
				result[i] = new Boolean(false);
		}
		
		String userID = parentWidget.getUser();
		if (userID == null)
			return;
		parentWidget.getHandler().sendOptionalInfo(userID, result, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			if (o == null)
    			{
    				Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   					parentWidget.close();
   				}
   				else
	    			showMatchNumber((Integer) o);   
    		}
	
			public void onFailure(Throwable caught) 
			{
	   			Window.alert(caught.getMessage());
	   		}
  		});
  	}
  	
  	/*	Overload. In QueryTab, after each line an 'optional' checkbox is inserted
  	 */
  	public void initPanel(AnchorInfo[] ai, boolean u)
  	{
		undoBtn.setEnabled(undo);
	  	undo = true;
		redoBtn.setEnabled(redo);
  		redo = false;
  		
  		remove(panel);
  		String[] idArray = new String[ai.length];
  		WysiwymLabel[] lbArray = new WysiwymLabel[ai.length];
  		StringBuffer sb = new StringBuffer();
  		checkBoxes = new ArrayList();
  		List checkBoxIDs = new ArrayList();
  		int cntr = 0;

  		for (int i = 0; i < ai.length; i++)
  		{
  			if (ai[i] == null)
  			{
  				undoBtn.setEnabled(false);
  				continue;
  			}
  			String words = ai[i].getWords();
  			
  			if (ai[i].isAnchor())
  			{
  				WysiwymLabel lb = new WysiwymLabel(words, ai[i], this, parentWidget);
  				
  				lbArray[i] = lb;
	   			String id = HTMLPanel.createUniqueId();
	   			idArray[i] = id;
	   			
	   			if ((i > 0) && (!isPunctuation(ai, i - 1)))
	   				sb.append("&nbsp;");
	   			sb.append("<span id='");
	   			sb.append(id);
	   			sb.append("'></span>");
	   			if (!isPunctuation(ai, i))
	   				sb.append("&nbsp;");
	   			DOM.setStyleAttribute(lb.getElement(), "display", "inline");				
  			}
  			else if (isListOrthography(words))
  			{	//insert an 'optional' checkbox just before the next line
  				int previous = 1;
  				while (ai[i-previous].getWords().length() == 0)
  					previous++;
  				if (!isListOrthography(ai[i-previous].getWords()))	//but not if it has just been done...
  				{
	  				cntr++;	//and not for the very first line ('find all resources')
  					if (cntr > 1)
  					{
  						String id = HTMLPanel.createUniqueId();
  						checkBoxIDs.add(id);
  						sb.append("&nbsp;<span id='");
	   					sb.append(id);
	   					sb.append("'></span>");
	   				}
	   			}
	   			sb.append(words);
  			}
  			else
  				sb.append(words);
  		}
  		panel = new HTMLPanel(sb.toString());
  		panel.setStyleName("wysiwym-label-large");
  		
  		for (int i = 0; i < idArray.length; i++)
  		{
  			if (idArray[i] != null)
  				panel.add(lbArray[i], idArray[i]);
  		}
  		ClickListener listener = new OptionalListener();
  		for (int i = 0; i < checkBoxIDs.size(); i++)
  		{
  			CheckBox cb = new CheckBox("Optional");
  			cb.addClickListener(listener);
  			checkBoxes.add(cb);
  			panel.add(cb, (String) checkBoxIDs.get(i));
  		}
		insert(panel, 1);
		
		String userID = parentWidget.getUser();
		if (userID == null)
			return;
		parentWidget.getHandler().getCheckedOptionals(userID, new AsyncCallback()
  		{	//retrieve the information from the server which optional boxes were checked before
    		public void onSuccess(Object o) 
    		{
    			if (o == null)
    			{
    				Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   					parentWidget.close();
   					return;
    			}
    			//and check them again
    			Boolean[] checked = (Boolean[]) o;
    			for (int i = 0; i < checked.length; i++)
    				((CheckBox) checkBoxes.get(i)).setChecked(checked[i].booleanValue());
    			parentWidget.hideHourglass();
    		}
	
			public void onFailure(Throwable caught) 
			{
				parentWidget.hideHourglass();
			}
  		});
  		
  		parentWidget.getHandler().getMatchNr(userID, new AsyncCallback()
  		{	//send the current sparql query to sesame and return the number of answers
    		public void onSuccess(Object o) 
    		{
    			if (o != null)
	    			showMatchNumber((Integer) o);    			
    		}
	
			public void onFailure(Throwable caught) 
			{}
  		});
  	}
  	
  	private class OptionalListener implements ClickListener
  	{	//Whenever an optional checkbox is checked or unchecked, check the number of matches in the database
  		public void onClick(Widget sender)
  		{
  			sendOptionalInformation();
  		}
  	}
  	
  	/*	Returns the list with 'optional' checkboxes, so the Command can see which
  	 *	are checked
  	 */
  	public List getCheckBoxes()
  	{
  		return checkBoxes;
  	}

  	public void showResults(AnchorInfo[][] results)
  	{
  		if (results.length == 0)
  		{
  			Window.alert("Error while searching for matches");
  			parentWidget.hideHourglass();
  		}
  		else if ((results.length == 1) && results[0][0].equals("No matches found"))
  		{
  			Window.alert("I'm afraid I could find no resources that matched your query. Please try again.");
  			parentWidget.hideHourglass();
  		}
  		else
  			new QueryResult(results, parentWidget, this);
  	}
  		
 	public void onClick(Widget sender)
  	{
  		if (sender == submitBtn)
  		{
			parentWidget.showHourglass();
  			parentWidget.submit(type, key);
   		}
  		else if (sender == resetBtn)
  			reset();
  		else
  			super.onClick(sender);
  	}
  	
  	public void reset()
  	{
  		String user = parentWidget.getUser();
		if (user == null)
			return;
		
		ResourceTypeElicitor rt = new ResourceTypeElicitor(parentWidget.getUserName(), user, this, parentWidget);
	 // 	String[] text = {"Resource"};
  	 //	rt.setCandidates(text);
  	 	rt.setCandidates(null);
  		rt.showPanel();
  	}
}