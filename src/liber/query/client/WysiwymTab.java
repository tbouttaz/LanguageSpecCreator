package liber.query.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import java.util.HashMap;
import java.util.ArrayList;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.DOM;
import liber.edit.client.AnchorInfo;
 
/**
 * 	Defines the editing module
 *
 *	@author Feikje Hielkema
 *	@version 1.0 December 6, 2006
 *	@version 1.2 January 2008
 *	@version 1.4 August 2008
 */
public class WysiwymTab extends MyTab implements ClickListener
{
  	protected Button submitBtn = new Button("Submit description", this);
  	protected Button undoBtn = new Button("Undo last addition", this);
  	protected Button redoBtn = new Button("Redo last removal", this);
  	public Button resetBtn = new Button("Reset", this);
  	protected Button helpBtn = new Button("Help!", this);
  	protected Button previousBtn = new Button("Edit last addition", this);
  	
   	protected HTMLPanel panel = new HTMLPanel("");
   	public HTMLPanel folksonomyPanel = new HTMLPanel("");
   	protected Label message1, message2;
   	protected VerticalPanel messagePanel;
   	private Label rdfLabel;
   	
  	protected MenuBar menu;
  	protected HorizontalPanel bottom, top2;
  	protected DockPanel top1;
  	protected WYSIWYMinterface parentWidget;
	protected Image image;
	
	private ResourceTypeElicitor rt;	
	private WysiwymLabel activeLabel = null;
	protected boolean redo = false, undo = false;
	WysiwymLabel firstAnchor;
	protected HashMap userMap = new HashMap();
	protected Help help;
	
	protected int type = WYSIWYMinterface.EDIT_TAB;
	protected int htmlIndex = 1;
	protected String key = null;
	
	private Object previousPopupShown;		//previously shown popup, so people can return quickly if they've made a mistake
  	
  	/**	Constructor, initialises editing pane.
  	 *	@param p Entrypoint
  	 */
  	public WysiwymTab(WYSIWYMinterface p)
  	{
  		super();
  		parentWidget = p;
  		help = new Help(p, type);
  		setStyleName("ks-Sink");
  		setSpacing(30);
  		
  		image = new Image();
		image.setUrl("http://www.csd.abdn.ac.uk/~fhielkem/logo.jpg");
  		
  		submitBtn.setStyleName("wysiwym-button-large");
  		HorizontalPanel bottom = new HorizontalPanel();
  		bottom.setSpacing(25);
  		bottom.add(resetBtn);	
  		bottom.add(undoBtn);
  		bottom.add(redoBtn);
  		bottom.add(previousBtn);
  		previousBtn.setEnabled(false);
  		bottom.add(helpBtn);
  		bottom.add(submitBtn);
  						
  		message1 = new Label("Welcome to the PolicyGrid Data Archive.");
  		message1.setStyleName("wysiwym-label-xlarge");
  		messagePanel = new VerticalPanel();
  		messagePanel.add(message1);

		top1 = new DockPanel();
		top1.setWidth("100%");
		top1.add(messagePanel, DockPanel.WEST);
		top1.add(image, DockPanel.EAST);
		
		add(top1); 
		add(panel);
  		add(bottom);
  		setCellWidth(panel, "100%");	
  	}
  	
  	/**	Tell the module that this was the last pop-up displayed.
  	 *	If the user clicks on 'edit last addition', this pop-up 
  	 *	will be displayed again.
  	 *	@param c Object widget that displays pop-up
  	 */
  	public void setPreviousPopup(Object c)
  	{
  		previousPopupShown = c;
  		previousBtn.setEnabled(c != null);
  	}
  	
  	/**	Checks whether any pop-ups are visible. If so, user cannot
  	 *	press any buttons or open menus.
  	 *	@return true if there is a visible pop-up
  	 */
  	public boolean popupShowing()
  	{
  		if (previousPopupShown == null)
  			return false;
  		
  		if (previousPopupShown instanceof InputPopupPanel)
  			return ((InputPopupPanel)previousPopupShown).isShowing();
  		else if (previousPopupShown instanceof WysiwymCommand)
  			return ((WysiwymCommand)previousPopupShown).isShowing();
  		else if (previousPopupShown instanceof UndoCommand)
  			return ((UndoCommand)previousPopupShown).isShowing();
  	
  		return false; 
  	}
  	
  	/**	Returns the session type
  	 *	@return int
  	 */
  	public int getType()
  	{
  		return type;
  	}
  	
  	/**	Checks whether the user has added any information, that should be
  	 *	submitted before closing the window
  	 *	@return true if the session contains information not yet in the archive
  	 */
  	public boolean unsubmittedInfo()
  	{
  		return undo;
  	}
  	
  	/**	Sets undo to false, so that next feedbacktext update the undo button
  	 *	will be disabled
  	 */
  	public void disableUndo()
  	{
  		undo = false;
  	}
  	
  	/**	Retrieves the name of the user with the given ID
  	 *	@param id User ID
  	 */
  	public String getUserName(String id)
  	{
  		if (userMap.containsKey(id))
  			return (String) userMap.get(id);
  		return null;
  	}
  	
  	/**	Changes the welcoming message to a more personalised version
  	 *	@param user User name
  	 *	@param resource Resource type
  	 *	@param title Resource title (may be null)
  	 */
  	public void setMessage(String user, String resource, String title)
  	{
  		if (user != null)
	  		message1.setText("Welcome to the PolicyGrid Data Archive, " + user + ".");
	  	else
	  		message1.setText("Welcome to the PolicyGrid Data Archive.");
  		if (messagePanel.getWidgetCount() == 2)
  			messagePanel.remove(message2);
  		
  		StringBuffer sb = new StringBuffer("You are depositing a");
  		char c = Character.toLowerCase(resource.charAt(0));
  		if ((c == 'a') || (c == 'e') || (c == 'o') || (c == 'u') || (c == 'i'))
  			sb.append("n");
  		sb.append(" " + resource.toLowerCase());
  		sb.append(". Please describe it by editing the text in the pane.");

  		message2 = new Label(sb.toString());
  		message2.setStyleName("wysiwym-label-large");
  		messagePanel.add(message2);
  	}
  	
  	/** Shows a pane that asks the user what resource he/she is depositing.
  	 *	@param user Username
  	 *	@param userID User ID
  	 */
  	public void init(String user, String userID)
  	{
  		userMap.put(userID, user);
  		rt = new ResourceTypeElicitor(user, userID, this, parentWidget);
  		String[] text = {"Resource"};
  		rt.setCandidates(text);
  		rt.showPanel();
  	}
  	
  	/**	Displays the RDF-representation of the Semantic Graph. Used
  	 *	previously for debugging.
  	 *	@param rdf String RDF
  	 *	@deprecated
  	 */
  	public void showRDF(String rdf)
  	{
  		rdfLabel = new Label(rdf);
  		remove(panel);
  		insert(rdfLabel, 1);
  		undoBtn.setText("Back");		
  		remove(top1);		
  		top2 = new HorizontalPanel();
  		top2.setSpacing(10);
  		top2.add(image);
  		insert(top2, 0);
  	}
  	
  	/**	Resets the querying session, displaying the class hierarchy again.
  	 */
  	public void reset()
  	{
  		String user = parentWidget.getUser();
		if (user == null)
			return;
		undo = false;
		redo = false;
		previousBtn.setEnabled(false);
		previousPopupShown = null;
  		ResourceTypeElicitor rt = new ResourceTypeElicitor((String)userMap.get(user), user, this, parentWidget);
		String[] text = {"Resource"};
  		rt.setCandidates(text);
  		rt.showPanel();	
  	}
  	
  	/**	Displays the tagcloud in the given panel in the bottom of this module
  	 *	@param panel HTMLPanel with tagcloud
  	 */
  	public void showFolksonomy(HTMLPanel panel)
  	{
  		folksonomyPanel = panel;
  		folksonomyPanel.setStyleName("wysiwym-popup-textbox");
  		add(folksonomyPanel);
  		setCellWidth(folksonomyPanel, "100%");
  	}
  	
  	/**	Hides the tagcloud, if one is present. It is saved as a variable
  	 *	so it can be displayed if someone presses 'edit last addition'.
  	 */
  	public void hideFolksonomy()
  	{
  		if ((getWidgetCount() > 3) && (folksonomyPanel != null))
	  		remove(folksonomyPanel);
  	}
  	
  	/**	Removes the tagcloud entirely, if one is present.
  	 */
  	public void removeFolksonomy()
  	{
  		hideFolksonomy();
  		folksonomyPanel = null;
  	}
  	
  	/**	Displays the feedback text
  	 *	@param ai Feedback text
  	 */
  	public void initPanel(AnchorInfo[] ai)
  	{
  		initPanel(ai, true);
  	}
  	
  	/**	Get session ID.
  	 *	@return String.
  	 */
  	public String getKey()
  	{
  		return key;
  	}
  	
  	/**	Displays the feedback text
  	 *	@param ai Feedback text
  	 *	@param u If true, enable undo button next time the text is updated
  	 */
  	public void initPanel(AnchorInfo[] ai, boolean u)
  	{
		if (undo)
			undoBtn.setEnabled(undo);
		else
		{
			undoBtn.setEnabled(u);
	 	 	undo = u;
	 	}
	 	
		redoBtn.setEnabled(redo);
  		submitBtn.setEnabled(true);
  		remove(panel);
  	
  		String[] idArray = new String[ai.length];
  		WysiwymLabel[] lbArray = new WysiwymLabel[ai.length];
  		StringBuffer sb = new StringBuffer();

  		for (int i = 0; i < ai.length; i++)
  		{
  			if (ai[i] == null)
  			{
  				undoBtn.setEnabled(false);
  				undo = false;
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
  			else
  				sb.append(words);
  		}
  		
  		if (type == parentWidget.BROWSE_TAB) //in the browsing tab, submit should only be enabled
  		{
  			resetBtn.setEnabled(false);
 			submitBtn.setEnabled(undo);		//if the user has added some information
 		}
  		panel = new HTMLPanel(sb.toString());
  		for (int i = 0; i < idArray.length; i++)
  		{
  			if ((idArray[i] != null) && (lbArray[i] != null))
  			{
  				if (lbArray[i].isRed())
  					submitBtn.setEnabled(false);	//can't submit description with red anchors
  				panel.add(lbArray[i], idArray[i]);
  			}
  		}
  		panel.setStyleName("wysiwym-label-large");
  		insert(panel, htmlIndex);
  		parentWidget.hideHourglass();
  	}
  	
  	/**	If the user clicks on an anchor, that label becomes 'active'
  	 *	and displays its pop-up menu. The previously active anchor 
  	 *	hides its menu.
  	 *	@param lb WysiwymLabel
  	 */
  	public void setActiveLabel(WysiwymLabel lb)
  	{
  		if (activeLabel != null)
	  		activeLabel.hideMenu();
  		activeLabel = lb;
  	}
  	
  	/**	Check if the label at the given index ends in a punctuation
  	 *	character.
  	 *	@param ai AnchorInfo[], contains labels and anchors
  	 *	@param idx Index
  	 *	@return True if the last character of the label is a punctuation symbol.
  	 */
  	protected boolean isPunctuation(AnchorInfo[] ai, int idx)
  	{
  		if ((idx + 1) == ai.length)
  			return false;
  		if ((ai[idx] == null) || (ai[idx+1] == null))
  			return false;
  		
  		String str = ai[idx + 1].getWords();
  		String str2 = ai[idx].getWords();
  		if (str.length() == 0)
  			return isPunctuation(ai, idx + 1);
  		while  (str2.length() == 0)
  		{
  			idx--;
  			if (idx < 0)
  				return false;
  			str2 = ai[idx].getWords();
  		}
  		  		
  		char c = str.charAt(0);
  		if ((c == ',') || (c == '.') || (c == ';') || (c == ':'))
  			return true;
  		c = str2.charAt(0);
  		if ((c == ',') || (c == '.') || (c == ';') || (c == ':'))
  			return true;
  		return false;
  	}
  	
  	/**	Click listener on buttons. If submit, submit description; if undo, undo last
  	 *	information addition; if redo, re-add last information removal; if reset, 
  	 *	reset description; if help, display help diagram; and if previous, display previously
  	 *	show pop-up.
  	 *	@param sender Button Widget
  	 */
  	public void onClick(Widget sender) 
    {	
   		if (sender == submitBtn)	//convert the semantic graph to RDF
     	{	
   			if (Window.confirm("Are you sure you want to submit your description?"))
   			{
   				parentWidget.showHourglass();	
  				parentWidget.submit(type, key);
   			}
   			else
   			{
   				String user = parentWidget.getUser();
				if (user == null)
					return;
   			}
     	}
     	else if (sender == undoBtn)
     	{
   			String userID = parentWidget.getUser();
			if (userID == null)
				return;
			parentWidget.showHourglass();
     		parentWidget.getHandler().undo(userID, type, key, new AsyncCallback()
 			{
   				public void onSuccess(Object o) 
   				{
   					if (o == null)
   					{
   						Window.alert("It was impossible to undo any additions. My apologies for neglecting to disable the 'undo' button.");
   						undoBtn.setEnabled(false);
   						parentWidget.hideHourglass();
   					}
   					else
   					{
   						setPreviousPopup(null);	//no going back to previous edit after undo!
   						regenerateFeedbackText(o, true, true);
   					}
   				}
   				
   				public void onFailure(Throwable caught) 
   				{
   					parentWidget.hideHourglass();
   					Window.alert("It was impossible to undo any additions. My apologies for neglecting to disable the 'undo' button.");
   					undoBtn.setEnabled(false);
   				}
			});
  		}
  		else if (sender == redoBtn)
  		{
  			String userID = parentWidget.getUser();
			if (userID == null)
				return;
			parentWidget.showHourglass();
     		parentWidget.getHandler().redo(userID, type, key, new AsyncCallback()
 			{
   				public void onSuccess(Object o) 
   				{
   					redo = false;
   					undo = true;
   					regenerateFeedbackText(o);
   				}
   				public void onFailure(Throwable caught) 
   				{
   					parentWidget.hideHourglass();
   					Window.alert(caught.getMessage());
   				}
			});
  		}
  		else if (sender == resetBtn)
  		{
  			if (Window.confirm("Are you sure you want to reset? This will delete the description you have created."))
  			{
   				String userID = parentWidget.getUser();
				if (userID == null)
					return;
				reset();
  			}
   			else
   			{
   				String user = parentWidget.getUser();
				if (user == null)
					return;
   			}	
  		}
  		else if (sender == helpBtn)
  			help.showTabHelp();
  		else if (sender == previousBtn)
  		{
  			if (previousPopupShown instanceof WysiwymCommand)
	  			((WysiwymCommand) previousPopupShown).show();
	  		else if (previousPopupShown instanceof PopupPanel)
	  			((PopupPanel) previousPopupShown).show();

	  		if ((getWidgetCount() < 4) && (folksonomyPanel != null))
	  			add(folksonomyPanel);
	  	}
	}
	
	/**	Display the new feedback text
	 *	@param o Object (should be AnchorInfo[]). Null if error occurred during generation.
	 */
	public void regenerateFeedbackText(Object o)
	{
		redo = false;
		regenerateFeedbackText(o, true);
	}
	
	/**	Display the new feedback text
	 *	@param o Object (should be AnchorInfo[]). Null if error occurred during generation.
	 *	@param u True if undo button should be enabled next time the text is regenerated.
	 */
	public void regenerateFeedbackText(Object o, boolean u)
	{
		hideFolksonomy();
		if (o == null)
		{
			Window.alert("Error occurred during regeneration of feedback text; your input may have been malformed.");
  			parentWidget.hideHourglass();
  			return;
		}
		
		AnchorInfo[] result = (AnchorInfo[]) o;
   		if (result.length == 0)
   		{
   			Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   			parentWidget.close();
   			return;
   		}
   	 	if (result[0] == null)	//signal that it is impossible to undo any more operations, because of the error
   	 	{
   	 		parentWidget.hideHourglass();
   	 		Window.alert("Error occurred during regeneration of feedback text; your input may have been malformed.");
   	 		undo = false;
   	 	}
   		initPanel(result, u);
	}
	
	/**	Display the new feedback text
	 *	@param o Object (should be AnchorInfo[]). Null if error occurred during generation.
	 *	@param u True if undo button should be enabled next time the text is regenerated.
	 *	@param r True if redo button should be enabled next time the text is regenerated.
	 */
	public void regenerateFeedbackText(Object o, boolean u, boolean r)
	{
		if (r || redo)
			redo = true;
		else
			redo = false;
		regenerateFeedbackText(o, u);
	}
}
