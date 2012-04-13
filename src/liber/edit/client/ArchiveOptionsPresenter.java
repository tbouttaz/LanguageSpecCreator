package liber.edit.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**	Presents existing options in the database; for each option the user can click on 'more' to
 *	see an object's complete descriptions in a browsing pane.
 *
 *	@author Feikje Hielkema
 *	@version 1.3 21 May 2008
 */
 public class ArchiveOptionsPresenter extends InputPopupPanel implements ClickListener
 {
 	private String[] options;	//option descriptions
 	private WYSIWYMinterface parent;
 	private WysiwymTab tab;
 	private ObjectPropertyCommand command;
  	private RadioButton[] radio;	//radio buttons
  	private Label[] more;			//'more' labels, when clicked show full description in browsing pane
  	private String user;	
 	
 	private Button yesBtn = new Button("Yes", this);
 	private Button noBtn = new Button("No", this);
 	private Button helpBtn = new Button("Help!", this);
 	
 	private boolean automatic = false;
 	private Help help;
 	private int browsingSessions = 0, helpSessions = 0;
	
	/**	Constructor, shows the list of options in a pop-up.
	 *	@param options String[] options
	 *	@param parent Entrypoint
	 *	@param command ObjectPropertyCommand, where the selected option will be used
	 *	@param tab Parent pane
	 *	@param aut If false, nothing happens if the user clicks 'no'. If true, a new object will be created if the user clicks 'no'.
	 */
	public ArchiveOptionsPresenter(String[] options, WYSIWYMinterface parent, ObjectPropertyCommand command, WysiwymTab tab, boolean aut)
	{
		super();
		this.parent = parent;
		this.command = command;
		this.options = options;	
		this.tab = tab;	
		this.user = parent.getUser();
		automatic = aut;
		init("The following objects in the archive match the information you provided. Are any of them the object you were describing?");      	
	}
	
	/**	Initialises
	 */
	private void init(String message)
	{
		help = new Help(parent, WYSIWYMinterface.EDIT_TAB);
		setStyleName("wysiwym-popup-textbox");
    	VerticalPanel vp = new VerticalPanel();
    	vp.setSpacing(10);
    	Label lb = new Label(message);
    	lb.setStyleName("wysiwym-label-small");
    	vp.add(lb);
    	
    	radio = new RadioButton[options.length / 2];
    	more = new Label[radio.length];
    	for (int i = 0; i < radio.length; i++)
    	{
    		HorizontalPanel hp = new HorizontalPanel();
    		radio[i] = new RadioButton("People", options[(i*2) + 1], true);
    		hp.add(radio[i]);
    		more[i] = new Label("More...");
			more[i].addClickListener(new optionClickListener(options[(i*2)], user, parent));
			hp.add(more[i]);
			vp.add(hp);
    	}

    	HorizontalPanel btnPanel = new HorizontalPanel();
    	btnPanel.setSpacing(20);
    	btnPanel.add(yesBtn);  	
    	btnPanel.add(noBtn);
    	btnPanel.add(helpBtn);
  
    	vp.add(btnPanel);
    	setWidget(vp);	
    	setPopupPosition(50, 50);
    	parent.hideHourglass();
    	show();
	}
 	
 	/**	Click listener. If the user clicks 'help', show the help dialog box;
 	 *	if 'yes', add the selected value to the command; if 'no', tell command
 	 * 	to create a new value if automatic is true, otherwise just hide the pop-up.
 	 *	@param sender Widget that was clicked on
 	 */
 	public void onClick(Widget sender)
 	{
 		if (sender == yesBtn)
 		{
 			for (int i = 0; i < radio.length; i++)
    		{
    			if (radio[i].isChecked())
    			{
    				hide();
    				parent.showHourglass();
    				String text = options[(i*2)+1];
    				command.addValue(options[(i*2)], text.substring(0, text.indexOf("<ul>")));
    				return;
    			}
    		}
    		Window.alert("Please select an option, or press 'no'.");
 		}
 		else if (sender == noBtn)
 		{
 			hide();
			if (automatic)
	    		command.addValue();
 		}
 		else if (sender == helpBtn)
 		{
 			helpSessions++;
			help.showArchiveOptionsHelp();
 		}
 	}
 	
 	/**	Click listener for the 'more' links. Opens browsing pane with 
 	 *	option's full description.
 	 */
 	private class optionClickListener implements ClickListener
	{
		private String id, user;
		private WYSIWYMinterface parent;
    			
    	public optionClickListener(String str, String u, WYSIWYMinterface p)
    	{
    		id = str;
    		user = u;
    		parent = p;
    	}
    		
    	public void onClick(Widget sender)
    	{
			browsingSessions++;
    		parent.showHourglass();		
    		parent.getHandler().getBrowsingDescription(user, id, new AsyncCallback()
			{
   				public void onSuccess(Object o) 
   				{   	
   					if (o == null)	
   	 				{
   	 					parent.hideHourglass();
   	 					Window.alert("Error occurred when trying to extract data from the database; the resource's description must be malformed.");
   	 					return;
   	 				}	
   	 				AnchorInfo[] result = (AnchorInfo[]) o;	
   	 				if (result.length == 0)
					{
						Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
						parent.close();
					}
					else
   						new BrowsingBox(parent, result, "Full description:");
    			}
    			
	   			public void onFailure(Throwable caught) 
	   			{
	   				parent.hideHourglass();
	   				Window.alert(caught.getMessage());
	   			}
  			});	
    	}
	}
 }
