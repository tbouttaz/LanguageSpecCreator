package liber.query.client;

import liber.edit.client.AnchorInfo;
import liber.edit.client.TagCloud;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 	Executed when a menu item from PopupMenu is selected. Various widgets can be
 *	shown to elicit values from the user, depending on the type of property selected.
 *	Information (if necessary) is send to the server and the feedback text is regenerated.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 December 6, 2006
 *	@version 1.1 November 5th, 2007
 */
 public class WysiwymCommand implements Command, ClickListener
{
	public static final int EDIT = 0;
	public static final int QUERY = 2;
	public static final int BROWSE = 4;
	protected int type = EDIT;
	protected String key = null;
	
	protected String anchor;
	protected int cardinal;
	protected int dateCntr = 0, selectedInstancesNr = 0;;
	protected Integer[] selectedInstances;
	protected String property, nlExpression;
	protected InputPopupPanel pp, tagCloudPP;
	protected Label l;
	protected int datatype = 0;
	
	protected Button okBtn = new Button("OK", this);
   	protected Button cancelBtn = new Button("Cancel", this);
	protected Button yesBtn = new Button("Yes", this);
	protected Button addBtn = new Button(">>", this);
	protected Button removeBtn = new Button("&lt;&lt;", this);
	protected Button addDateBtn = new Button("Add another date", this);
	protected Button helpBtn = new Button("Help!", this);
	
	protected String[] tags = new String[0];		//Tags in folksonomy currently visible; used for auto-complete
	protected TextBox[] tb;
	protected ListBox[] lists;
	protected TextArea abstractArea = new TextArea();
	protected SuggestBox sb;
	protected MultiWordSuggestOracle oracle = new MultiWordSuggestOracle("");
	protected ListBox lb, multipleValueBox;
	protected Help help;
		
	protected WysiwymTab tab;
	protected WYSIWYMinterface parent;
	protected PopupMenu menu;
	
	protected int panelType = 0;	//the type of popup that is being displayed (determined by type of input that is required)
   	protected int x = 50, y = 50;	//(Window.getClientWidth() / 2) - 200;	//put the pop-up more or less central	
	protected int buttonsPressed = 0, folksonomyValues = 0;		//TEMPORARY, FOR EVALUATION EXPERIMENTS
	
	/**	Constructor.
	 *	@param p Property name
	 *	@param nl NL-expression of property
	 *	@param a Anchor ID
	 *	@param t Parent module
	 *	@param wysInt Entrypoint
	 *	@param m PopupMenu
	 */
	public WysiwymCommand(String p, String nl, String a, WysiwymTab t, WYSIWYMinterface wysInt, PopupMenu m)
	{
		property = p;
		nlExpression = nl;
		anchor = a;
		tab = t;
		parent = wysInt;
		key = tab.getKey();
		menu = m;
		help = new Help(parent, tab.getType());
	}
	
	/**	Executes the menu item. Displays a pop-up whose structure depends on the type of
	 *	property. Object properties are handled by ObjectPropertyCommand; datatype
	 *	properties are divided in boolean, date, number, abstract, restricted value
	 * 	and string properties.
	 */
	public void execute()
	{	//if it's a datatype, let the user specify a value, then update the feedback text
		parent.showHourglass();
		buttonsPressed = 0;
		String userID = parent.getUser();
		if (userID == null)
			return;
		if (property.equals("HasAbstract"))
		{	//an abstract is much larger than an ordinary value, and is therefore displayed with a large textbox.
			showAbstractPane();
			return;
		}

		parent.getHandler().getType(userID, anchor, property, type, key, new AsyncCallback()
		{	//check property type on server
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					parent.hideHourglass();
					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
					parent.close();
					return;
				}
				Integer[] result = (Integer[])o;
				int type = result[0].intValue();
				cardinal = result[1].intValue();
							
				if (type == 0)	//property is a (sub)class. Does not happen in current version of LIBER.
					update(null);
				else if (type == 1)	//datatypeproperty with restricted values
					getDataRange();	//so show list of values
				else if (type == 2)	//range is a date
					showDatePopup();	//so show date pop-up
				else if ((type == 3) || (type == 4))	//range is a number
				{	//ensure that datatype is set so we can check whether values are integers/doubles
					if (type == 3)
						datatype = 2;
					else
						datatype = 1;
					getAddedValues();	//find any previously added values
					showTagCloud();	//and display a tag cloud that shows the frequency of ranges of numbers
				}
				else if (type == 5)	//range is a boolean
					showBooleanPopup();	//so show boolean pop-up
				else if (type == 6)	//range is a string
				{
					showTagCloud();	//display tag cloud
					getAddedValues();	//and retrieve any previously specified values
				}
				else	//property is a object property
					new ObjectPropertyCommand(property, nlExpression, anchor, cardinal, tab, parent).init();
			}

			public void onFailure(Throwable caught)
			{
				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
   	}	
    
    /**	Retrieves a tagcloud from the server and shows it
     */		
   	public void showTagCloud()
   	{
   		String user = parent.getUser();
   		if (user == null)
   			return;
   		parent.getHandler().getTagCloud(user, property, new AsyncCallback()
   		{
   			public void onSuccess(Object o)
   			{
   				TagCloud tc = (TagCloud) o;
   				if (tc.size() == 0)
   					return;

				oracle.clear();
				sb = new SuggestBox(oracle);
   				String[] idArray = new String[tc.size()];
				FolksonomyLabel[] lbArray = new FolksonomyLabel[tc.size()];
				StringBuffer strb = new StringBuffer();
				for (int i = 0; i < tc.size(); i++)
				{
	  				FolksonomyLabel lb = new FolksonomyLabel(tc.getValue(i), sb);
	  				oracle.add(tc.getValue(i));
					lbArray[i] = lb;
   					String id = HTMLPanel.createUniqueId();
   					idArray[i] = id;
   			
   					strb.append("<font size=\"");
   					strb.append(Integer.toString(tc.getFrequency(i) + 1));
   					strb.append("\"><span id='");
   					strb.append(id);
   					strb.append("'></span></font> &nbsp; ");
   					DOM.setStyleAttribute(lb.getElement(), "display", "inline");				
				}
		  		HTMLPanel result = new HTMLPanel(strb.toString());
 		
				for (int i = 0; i < idArray.length; i++)
				{
					if (idArray[i] != null)
						result.add(lbArray[i], idArray[i]);
				}
   				tab.showFolksonomy(result);
   			}
    			
   			public void onFailure(Throwable caught)
   			{
   				parent.hideHourglass();
   				Window.alert("Unable to generate the tag cloud:<\b> " + caught.getMessage());
   			}
   		});
   	}
    
    /**	Helper class, models tag in Folksonomy.	If clicked, it's filled
     * 	in the textbox
     */
   	public class FolksonomyLabel extends Label
	{
		SuggestBox sb;
 		ListBox booleanList, valueList;
 		
 		/**	Constructs a folksonomy label. When clicked, the text
 		 *	is filled in s.
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
					{
						folksonomyValues++;
  						sb.setText(((Label)sender).getText());
					}
				}
			});  					
		}
		
		/**	Constructs a folksonomy label for the number bands in query command.
		 *	When clicked, the range (e.g. '> 100 and < 200') is filled in the listboxes
		 *	@param text Label
		 *	@param b Boolean operator list (set to 'and' when tag is clicked)
		 *	@param v Value list, where restrictions are added.
		 */
		public FolksonomyLabel(String text, ListBox b, ListBox v)
		{
			super(text);
			booleanList = b;
			valueList = v;

			addClickListener(new ClickListener()
			{
				public void onClick(Widget sender)
				{
					String text = ((Label) sender).getText();
					int idx = text.indexOf('-');
					if (idx > 0)
					{
						int itemNr = valueList.getItemCount();
  						valueList.addItem("> " + text.substring(0, idx));
						valueList.addItem("< " + text.substring(idx + 1));
						valueList.setVisibleItemCount(itemNr + 2);
						booleanList.setSelectedIndex(1);
					}
				}
			});
		}
	}
    
    /**	Retrieves restricted values in property range
     */		
   	private void getDataRange()
   	{
   		String userID = parent.getUser();
		if (userID == null)
			return;
   		parent.getHandler().getRange(userID, property, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				String[] result = (String[])o;
   				showInstanceCheck(new String(nlExpression + ":"), result);
			}

			public void onFailure(Throwable caught)
			{
				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
   		});
   	}
    
    /**	Creates and shows a panel in a pop-up - helper method
     *
     *	@param widget Panel to display in pop-up
     */
    protected void show(Panel widget)
    {
    	pp = new InputPopupPanel();
   		pp.setStyleName("wysiwym-popup-textbox");
   		pp.add(widget);
   		pp.setPopupPosition(x, y);
   		parent.hideHourglass();
   		tab.setPreviousPopup(this);
   		pp.show();
    }
    
    /**	Shows a previously shown popup panel
     */
    public void show()
    {
    	if (pp != null)
	    	pp.show();
    }
   
   	/**	Checks whether the command is currently displaying a pop-up
    *	@return true if pop-up is visible
    */
   	public boolean isShowing()
   	{
   		if (pp != null)
	   		return pp.isShowing();
	   	return false;
   	}
    
    /**	Displays pane for adding abstract
     */
    private void showAbstractPane()
    {
    	abstractArea.setSize("500px", "500px");
    	VerticalPanel vp = new VerticalPanel();
    	vp.setSpacing(10);
    	vp.add(new Label("Please type in your abstract."));
    	vp.add(abstractArea);
    	
    	HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(okBtn);
		hp2.add(cancelBtn);
		hp2.setSpacing(30);
    	vp.add(hp2);
    	panelType = 6;
    	
    	show(vp);
    	abstractArea.setFocus(true);		
   		abstractArea.setCursorPos(0);
    }

    protected void initDateBoxes(VerticalPanel vp1)
    {
    	HorizontalPanel hp1 = new HorizontalPanel();
   		HorizontalPanel hp2 = new HorizontalPanel();
   		hp1.setSpacing(5);
   		hp2.setSpacing(5);
    	
   		Label l1 = new Label("From/on:");
   		l1.setStyleName("wysiwym-label-small");
   		l1.setWidth("70px");
   		hp1.add(l1); 		
   		Label l2 = new Label("To:");
   		l2.setStyleName("wysiwym-label-small");
   		l2.setWidth("70px");
   		hp2.add(l2);
    	
    	tb = new TextBox[6];
		for (int i = 0; i < 2; i++)
		{
			tb[i] = new TextBox();
			tb[i].setMaxLength(2);
   			tb[i].setText("dd");
   			tb[i].setWidth("30px");
		}
		for (int i = 2; i < 4; i++)
		{
			tb[i] = new TextBox();
			tb[i].setMaxLength(2);
   			tb[i].setText("mm");
   			tb[i].setWidth("30px");
		}
		for (int i = 4; i < 6; i++)
		{
			tb[i] = new TextBox();
			tb[i].setMaxLength(4);
   			tb[i].setText("yyyy");
   			tb[i].setWidth("60px");
		}
		
		hp1.add(tb[0]);
		hp2.add(tb[1]);
		hp1.add(new Label("/"));
		hp2.add(new Label("/"));
		hp1.add(tb[2]);
		hp2.add(tb[3]);
		hp1.add(new Label("/"));
		hp2.add(new Label("/"));
		hp1.add(tb[4]);
		hp2.add(tb[5]);
		
		vp1.add(hp1);
		vp1.add(hp2);
    }
    
    protected ListBox[] initDateLists()
    {
    	ListBox[] result = new ListBox[2];
   		for (int i = 0; i < 2; i++)
   		{
   			result[i] = new ListBox(false);
   			result[i].addItem("--");		//add an empty item
   		}
   		for (int i = 0; i < 10; i++)
   			result[0].addItem(Integer.toString(i*10));
   		for (int i = 21; i > 0; i--)
   			result[1].addItem(Integer.toString(i));
   		return result;
    }
    
   	/**	Shows the popup that lets the user specify one or more dates
   	 */
   	 protected void showDatePopup()
   	 {
		TabPanel tabs = new TabPanel();
		VerticalPanel vp1 = new VerticalPanel();
		VerticalPanel vp2 = new VerticalPanel(); 				
   		vp1.setSpacing(5);
   		Label lb = new Label("Please specify a date or a period.\nIf unknown, days and months can be left blank.");
   		lb.setStyleName("linebreak-label");
   		vp1.add(lb);
 		initDateBoxes(vp1);		
 		
   		HorizontalPanel btnP = new HorizontalPanel();
   		btnP.setSpacing(10);
   		btnP.add(okBtn);
   		btnP.add(cancelBtn);
   		if (cardinal == 1)
   			addDateBtn.setEnabled(false);
   		btnP.add(addDateBtn);
   		btnP.add(helpBtn);
   		
   		VerticalPanel labelP = new VerticalPanel();
   		labelP.setSpacing(10);
   		labelP.add(new Label("Decade"));
   		labelP.add(new Label("Century"));
    		
   		VerticalPanel boxP = new VerticalPanel();
   		boxP.setSpacing(8);
   		lists = initDateLists();
   		for (int i = 0; i < 2; i++)
   			boxP.add(lists[i]);	
   		HorizontalPanel complex = new HorizontalPanel();
   		complex.add(labelP);
   		complex.add(boxP);
   		vp2.add(complex);
   				
   		tabs.add(vp1, "Date/period");
   		tabs.add(vp2, "Decade/century");
   		tabs.selectTab(0); 		
   		VerticalPanel whole = new VerticalPanel();
   		whole.add(tabs);
   		whole.add(btnP);

   		tabs.setWidth("100%");
   		tabs.getTabBar().setWidth("100%"); 
   		
   		panelType = 4;
   		show(whole);
   		tabs.selectTab(0);
   		tb[0].setFocus(true);		
   		tb[0].setCursorPos(0);
   	 }
   	
   	/**	Retrieves any values the user has already specified for this property,
   	 *	in the correct order, and displays them in the pop-up.
   	 */
   	private void getAddedValues()
   	{
   		if ((cardinal == 1) && (!(this instanceof QueryCommand)))
   		{
   			showPopup();
   			return;
   		}
   		
   		String userID = parent.getUser();
		if (userID == null)
			return;
   		parent.getHandler().getAddedValues(userID, property, anchor, type, key, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					parent.hideHourglass();
					Window.alert("There was an error when searching for added values. Please try again.");
					return;
				}
				String[] result = (String[]) o;
				if ((result.length == 1) && result[0].equals("---EXPIRED---"))
				{
					parent.hideHourglass();
					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
					parent.close();
				}
				else
					showMultipleValuePopup(result);
			}

			public void onFailure(Throwable caught)
			{
				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
   	}
   	
   	/**	Shows the popup that lets the user specify multiple values for the selected property
   	 */	
   	protected void showMultipleValuePopup(String[] values)
   	{
   		HorizontalPanel hp = new HorizontalPanel();  		
   		DockPanel dp = new DockPanel();
   		l = new Label(nlExpression + ":");
   		dp.add(l, DockPanel.NORTH);
   		dp.setSpacing(10);
   		sb = new SuggestBox(oracle);
   		dp.add(sb, DockPanel.CENTER);
   		
		HorizontalPanel hp2 = new HorizontalPanel();
		hp2.add(okBtn);
		hp2.add(cancelBtn);
		hp2.add(helpBtn);
		hp2.setSpacing(20);
		dp.add(hp2, DockPanel.SOUTH);
   		
   		VerticalPanel vp3 = new VerticalPanel();
		vp3.add(addBtn);
		vp3.add(removeBtn);
		vp3.setSpacing(10);
		dp.add(vp3, DockPanel.EAST);
		DockPanel dp2 = new DockPanel();
   		if (cardinal > 0)
   		{
   			Label lb = new Label("Maximum: " + Integer.toString(cardinal));
   			lb.setStyleName("wysiwym-label-small");
    		dp2.add(lb, DockPanel.NORTH);
    	}
   		
   		multipleValueBox = new ListBox();
   		multipleValueBox.setSize("300px", "150px");
   		for (int i = 0; i < values.length; i++)	//add the values that the user
   			multipleValueBox.addItem(values[i]);	//has specified earlier
   		dp2.add(multipleValueBox, DockPanel.CENTER);
   		multipleValueBox.setVisibleItemCount(values.length);
   		
   		dp.setHeight("180px");
   		hp.add(dp);
		hp.add(dp2);
   		
   		panelType = 3;
   		show(hp);
   		sb.setFocus(true);
   	}
   	 	
   	/**	Shows the popup that lets the user specify a value for the selected property
   	 */	
   	protected void showPopup()
   	{
   		VerticalPanel vp = new VerticalPanel();
   		l = new Label(nlExpression + ":");
   		vp.add(l);
   		sb = new SuggestBox(oracle);
   		sb.setWidth("200px");
   		vp.add(sb);
   		vp.setSpacing(10);
   		
   		HorizontalPanel hp = new HorizontalPanel();
   		hp.add(okBtn);
   		hp.add(cancelBtn);
   		hp.add(helpBtn);
   		hp.setSpacing(10);
   		vp.add(hp);
   		
   		panelType = 1;
   		show(vp);
   		sb.setFocus(true);		
   	}
   	
   	/**	Shows the popup that lets the user specify a boolean value
   	 */	
   	private void showBooleanPopup()
   	{
   		VerticalPanel vp = new VerticalPanel();
   		l = new Label(nlExpression + ":");
   		vp.add(l);
		lb = new ListBox();
		lb.addItem("true");
		lb.addItem("false");
		lb.setVisibleItemCount(2);
		vp.add(lb);
		   		
   		HorizontalPanel hp = new HorizontalPanel();
   		hp.add(okBtn);
   		hp.add(cancelBtn);
   		hp.setSpacing(10);
   		vp.add(hp); 	
   		panelType = 5;	
   		show(vp);
   		okBtn.setFocus(true);		
   	}
   	
   	/**	Shows permitted values for a datatype property
   	 */
   	protected void showInstanceCheck(String message, String[] result)
   	{
   		VerticalPanel vp = new VerticalPanel();
   		l = new Label(message);
   		vp.add(l);
   		if (cardinal > 0)
   		{
   			Label l2 = new Label("Maximum: " + Integer.toString(cardinal));
   			l2.setStyleName("wysiwym-label-small");
   			vp.add(l2);
   		}
   		   		
   		lb = new ListBox();
   		for (int i = 0; i < result.length; i++)
   			lb.addItem(result[i]);
   		lb.setVisibleItemCount(lb.getItemCount());	//set all items to be visible
   		lb.setMultipleSelect(true);
   		vp.add(lb);
   		
   		HorizontalPanel hor = new HorizontalPanel();
   		hor.add(yesBtn);	
   		hor.add(cancelBtn);
   		hor.add(helpBtn);
   		hor.setSpacing(10);
   		vp.add(hor); 		
   		
   		panelType = 2;
   		show(vp);
   		yesBtn.setFocus(true);
   	}
   	
   	/**	Regenerates the feedback text without adding more information to the graph
   	 *	(e.g. when user has added two dates, then presses cancel
   	 */
   	private void getText()
   	{
   		parent.showHourglass();
		String userID = parent.getUser();
		if (userID == null)
			return;
		parent.getHandler().getFeedbackText(userID, type, key, new AsyncCallback()
		{
			public void onSuccess(Object o) 
   			{
   				tab.regenerateFeedbackText(o);
   			}
   	
   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
   	}
   	
   	/**	Updates the Graph with a single value
   	 */
   	private void update(String value)
   	{
		parent.showHourglass();
 		String userID = parent.getUser();
		if (userID == null)
			return;			
   		parent.getHandler().update(userID, anchor, property, value, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			tab.regenerateFeedbackText(o);
    		}
	
			public void onFailure(Throwable caught) 
 			{
 				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
	}
  		
	/**	Updates the text with a new date
	 */
	private void updateDate(String[] l, boolean updateText)
	{
		parent.showHourglass();
		String userID = parent.getUser();
		if (userID == null)
			return;			
		parent.getHandler().updateDate(userID, anchor, property, l, dateCntr, updateText, type, key, new AsyncCallback()
		{
   			public void onSuccess(Object o) 
   			{
   				AnchorInfo[] result = null;
   				if (o != null)	//if o is null the user will add more dates first
   				{
    				result = (AnchorInfo[]) o;
    				tab.hideFolksonomy();
   					if (result.length == 0)
   					{
   						parent.hideHourglass();
   						Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   						parent.close();
   					}
   					else if (result[0] == null)
   					{
   						parent.hideHourglass();
   						Window.alert("Error occurred during regeneration of feedback text; your input may have been malformed.");
   					}
   					else
    					tab.initPanel(result);
    				dateCntr = 0;
    			}
    			else
    				parent.hideHourglass();
   			}
   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
	}
	
	/**	Updates the Graph with a boolean value
   	 */
	protected void updateBoolean(boolean b)
	{
		parent.showHourglass();
		String userID = parent.getUser();
		if (userID == null)
			return;			
		parent.getHandler().updateBoolean(userID, anchor, property, b, type, key, new AsyncCallback()
		{
   			public void onSuccess(Object o) 
   			{
   				tab.regenerateFeedbackText(o);
   			}
   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
	}
   	
   	/**	Updates the Graph with a Number value
   	 */
	private void updateNumber(Number nr)
	{
		parent.showHourglass();
		String userID = parent.getUser();
		if (userID == null)
			return;			
		parent.getHandler().updateNumber(userID, anchor, property, nr, type, key, new AsyncCallback()
		{
   			public void onSuccess(Object o) 
   			{
   				tab.regenerateFeedbackText(o);
   			}
   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
	}
	 		  		
	/**	Updates the text with values from a restricted range property
	 */
	private void multipleUpdate(String[] idx)
	{
		parent.showHourglass();
		String userID = parent.getUser();
		if (userID == null)
			return;
		parent.getHandler().multipleUpdate(userID, anchor, property, idx, type, key, new AsyncCallback()
		{
   			public void onSuccess(Object o) 
   			{
  				tab.regenerateFeedbackText(o);
   			}

   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
	}
  	
  	/**	Updates the text with multiple values of the given datatype
  	 */	
   	private void multipleValuesUpdate(String[] values, int datatype)
   	{
   		parent.showHourglass();
   		String userID = parent.getUser();
		if (userID == null)
			return;
   		parent.getHandler().multipleValuesUpdate(userID, anchor, property, values, datatype, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			tab.regenerateFeedbackText(o);
    		}
	
			public void onFailure(Throwable caught) 
			{
				parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
   	}
   	
   	/**	Updates the text with an abstract
  	 */	
   	protected void updateAbstract(String str)
   	{
   		parent.showHourglass();
   		String userID = parent.getUser();
		if (userID == null)
			return;
   		parent.getHandler().updateAbstract(userID, anchor, property, str, type, key, new AsyncCallback()
  		{
    		public void onSuccess(Object o) 
    		{
    			tab.regenerateFeedbackText(o);
    		}
	
			public void onFailure(Throwable caught) 
			{
				parent.hideHourglass();
	   			Window.alert(caught.getMessage());
	   		}
  		});
   	}
   	
   	/** Clicklistener on buttons. Moves values to the list on the right when 
   	 *	clicked add in multiple value pane, or moves from right to left when
   	 *	clicked remove;	updates graph when clicked ok, hides pop-up when click cancel, etc.
   	 *	@param sender Button Widget
   	 */
	public void onClick(Widget sender)
	{	//pass the value to the server
		buttonsPressed++;
		if (sender == okBtn)
		{
			if (panelType == 1)		//single value input
			{
				if (datatype > 0)	//of a number 
				{
					String str = sb.getText();
					Number nr;
					try
					{
						if (datatype == 2)
							nr = new Double(str);
						else
							nr = new Integer(str);			
					}
					catch(NumberFormatException e)
					{
						if (datatype == 1)
							Window.alert("The menu item you have chosen only allows integers (natural numbers) as its values. " +
							"Please enter an integer (e.g. 0, 1, 2...).");
						else
							Window.alert("The menu item you have chosen only allows numbers as its values. Please enter a number.");
						return;
					}
					updateNumber(nr);
				}
				else	//of a string
				{
					String value = sb.getText();	
		  			if (value.length() == 0)
	  				{
						Window.alert("You did not specify a value for this property");
						return;
					}
					update(value);
				}
			}	
			else if (panelType == 3)	//multiple value input
			{
				String leftValue = sb.getText();	//if there is a tag in the text box, and the user does want to add it,
				if ((leftValue.length() > 0) && 	//move it to the list
					Window.confirm("You specified a tag in the textbox that you did not move to the list on the righthand side. Do you want to add this tag?"))
					multipleValueBox.addItem(leftValue);
				
				int items = multipleValueBox.getItemCount();
				if (items == 0)
	  			{
  					Window.alert("You did not specify any values for this property");
  					return;
  				}
  				String[] values = new String[items];	//now add all tags in the list
  				for (int i = 0; i < items; i++)
  					values[i] = multipleValueBox.getItemText(i);
  				multipleValuesUpdate(values, datatype);
			}			
 			else if (panelType == 4)	//date input
			{
				if ((lists[0].getSelectedIndex() == 0) && (lists[1].getSelectedIndex() == 0))
				{
					try
					{
						Integer.parseInt(tb[4].getText());
					}
					catch (NumberFormatException e)
					{
						Window.alert("Please enter a date.");
						return;
					}
				}					
				dateCntr++;
				String[] result = new String[9];
				for (int i = 0; i < 6; i++)
					result[i] = tb[i].getText();
				for (int i = 0; i < 2; i++)
					result[i + 6] = lists[i].getItemText(lists[i].getSelectedIndex());
				updateDate(result, true);
			}
			else if (panelType == 5)	//booleanInput
			{
				boolean b = true;
				if (lb.getSelectedIndex() == 1)
					b = false;
				updateBoolean(b);
			}
			else if (panelType == 6)	//abstract
			{
				if (abstractArea.getText().length() == 0)
				{
					Window.alert("Please type in an abstract, or press 'Cancel'.");
					return;
				}
				updateAbstract(abstractArea.getText());
			}
  			pp.hide();
		}
		else if (sender == cancelBtn)
		{	//cancel
			pp.hide();
			tab.hideFolksonomy();

			if (dateCntr > 0)
				getText();
		}
		else if (sender == yesBtn)	//instance selection
		{	//get the selected instance(s), check how many unspecified instances there are, and update the feedback text
			if (lb.getSelectedIndex() == -1)	//nothing selected
			{
				Window.alert("Please select an item first");
				return;
			}

  			selectedInstances = new Integer[lb.getItemCount()];
  			String[] texts = new String[lb.getItemCount()];
			selectedInstancesNr = 0;
			for (int i = 0; i < lb.getItemCount(); i++)
  			{
  				if (lb.isItemSelected(i))
  				{
  					selectedInstances[selectedInstancesNr] = new Integer(i);
  					texts[selectedInstancesNr] = lb.getItemText(i);
  					selectedInstancesNr++;				
  				}
  			} 
  						
  			if ((cardinal > 0 ) && (selectedInstancesNr > cardinal))
  			{
  				Window.alert("You are exceeding the maximum amount. Please deselect one or more values.");
				return;
  			}
  			pp.hide();
			multipleUpdate(texts);
  		}
  		else if (sender == addBtn)
 		{
			String value = sb.getText(); 
			if (value.length() == 0)
 			{
 				Window.alert("You did not specify a value to add");
 				return;
  			}
  	
  			if (datatype > 0)
  			{
  				try
				{
					if (datatype == 2)
						new Double(value);
					else
						new Integer(value);		
				}
				catch(NumberFormatException e)
				{
					if (datatype == 1)
						Window.alert("Please enter an integer (i.e. a 'natural' number: 0, 1, 2...).");
					else
						Window.alert("Please enter a number.");
					return;
				}
  			}
  			else
  			{
  				if (value.indexOf(",") > -1)		//if the value contains a komma, it may in fact be more than one value
  					if (!Window.confirm("Are you sure this is only one value? Each value should be added separately."))
  						return;
  			}
  		
  			int itemNr = multipleValueBox.getItemCount();
  			if ((cardinal > 0) && (itemNr == cardinal))
  			{
  				Window.alert("If you add this you will exceed the maximum; please remove some other value first");
  				return;
  			}

  			multipleValueBox.addItem(value);
  			multipleValueBox.setVisibleItemCount(itemNr + 1);
  			multipleValueBox.setSelectedIndex(itemNr);
  			sb.setText("");
  			sb.setFocus(true);		
		}
		else if (sender == removeBtn)
		{
			int idx = multipleValueBox.getSelectedIndex();
			int lastItem = multipleValueBox.getItemCount() - 1;
			if (lastItem < 0)
				return;
				
			if (idx > -1)
			{
				sb.setText(multipleValueBox.getItemText(idx));
				for (int i = idx; i < lastItem; i++)
					multipleValueBox.setItemText(i, multipleValueBox.getItemText(i + 1));
			}
			else
				sb.setText(multipleValueBox.getItemText(lastItem));

			multipleValueBox.removeItem(lastItem);
  		}
  		else if (sender == addDateBtn)
  		{
  			if ((lists[0].getSelectedIndex() == 0) && (lists[1].getSelectedIndex() == 0))
			{
				try
				{
					Integer.parseInt(tb[4].getText());
				}
				catch (NumberFormatException e)
				{
					Window.alert("Please enter a date.");
					return;
				}
			}					
			dateCntr++;
			String[] result = new String[9];
			for (int i = 0; i < 6; i++)
			{
				result[i] = tb[i].getText();
				if ((i == 0) || (i == 1))
					tb[i].setText("dd");
				if ((i == 2) || (i == 3))
					tb[i].setText("mm");
				if ((i == 4) || (i == 5))
					tb[i].setText("yyyy");
			}
			for (int i = 0; i < 2; i++)
			{
				result[i + 6] = lists[i].getItemText(lists[i].getSelectedIndex());
				lists[i].setSelectedIndex(0);
			}

 			if ((cardinal > 0) && (dateCntr >= cardinal))
 				addDateBtn.setEnabled(false);	
 			updateDate(result, false);				
  		}
  		else if (sender == helpBtn)
  		{
  			switch(panelType)
  			{
  				case 1: help.showOneValueHelp(datatype); break;
  				case 2: help.showListHelp(); break;
  				case 3: help.showMultipleValueHelp(datatype); break;
  				case 4: help.showDateHelp(); break;
  			}
  		}
  	}
}