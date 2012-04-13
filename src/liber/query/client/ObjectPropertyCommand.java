package liber.query.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import liber.edit.client.AnchorInfo;
import liber.edit.client.ExistingInstances;
import liber.edit.client.FormInfo;
import liber.edit.client.Hierarchy;
import liber.edit.client.InstanceData;
import liber.edit.client.TagCloud;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/** ObjectPropertyCommand is initialised when a user selects an object property and elicits which or what
 *	type of objects are in the range of the property.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 December 2007
 *
 *	@version 1.2 March 2008
 *
 *	@version 1.3 May 2008
 */
 public class ObjectPropertyCommand extends InputPopupPanel implements ClickListener
 {
 	private String property, nlname;
 	private String anchor;
 	private int cardinal;
 	private WysiwymTab tab;
 	private WYSIWYMinterface parent;
 	private int type = 0;
 	private String key, valueList;
 	private Help help;
 	
 	private Button okBtn = new Button("OK", this);
 	private Button cancelBtn = new Button("Cancel", this);
 	private Button helpBtn = new Button("Help!", this);
 	
 	private Button addNewBtn = new Button("Add another", this);
 	private Button resetBtn = new Button("Start over", this);

 	private HorizontalPanel newValuePanel = new HorizontalPanel();
 	private Tree tree, confirmTree;
 	private VerticalPanel treePanel, parentPanel;
 	private HorizontalPanel instancePanel;
 	private PopupPanel confirmationPP, treePP, formPP;
 	
 	private TextBox titleBox = new TextBox();
 	private HashMap oldRange = new HashMap(), newInstances = new HashMap(), hierarchyMap = new HashMap();
 	private List currentRange = new ArrayList();
 	private int buttonsPressed = 0;
 	private Hierarchy[] hierarchy;
 	private InstanceData tempData;
 	
 	private FormInfo[] formInformation;
 	private FocusWidget[] inputWidget;
 	private CheckBox[] na;
 		
 	/**	Constructor.
 	 *	@param p Property name
 	 *	@param nl Property's nl-expression
 	 *	@param a Anchor ID
 	 *	@param car Maximum cardinality constraint
 	 *	@param wt Parent module
 	 *	@param wysInt Entrypoint
 	 */		
 	public ObjectPropertyCommand(String p, String nl, String a, int car, WysiwymTab wt, WYSIWYMinterface wysInt)
 	{
 		super();
 		property = p;
 		nlname = nl;
 		anchor = a;
 		cardinal = car;
 		tab = wt;
 		parent = wysInt;
 		key = wt.getKey();
 		if (tab instanceof QueryTab)
			type = wysInt.QUERY_TAB;
		else if (tab instanceof BrowsingTab)
			type = wysInt.BROWSE_TAB;
		help = new Help(parent, type);
 	}
 	
 	/**	Shows this pop-up, and tells the parent module that this is now the
 	 *	last shown pop-up. Also show tagcloud if it's there
 	 */
 	public void show()
 	{
 		super.show();
 		tab.setPreviousPopup(this);
 	}
	
	/**	Initialise. Retrieve all individuals that are currently in the range of the property,
	 *	and all individuals that could be in the range.
	 */
 	public void init()
 	{
   		String userID = parent.getUser();
		if (userID == null)
			return;
		getHierarchy();
   		parent.getHandler().getInstances(userID, property, anchor, type, key, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					parent.hideHourglass();
					Window.alert("There was an error when searching for individuals. Please try again.");
					return;
				}
				ExistingInstances result = (ExistingInstances) o;
				if (result.getRange() == null)
				{
					parent.hideHourglass();
					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
					parent.close();
				}
				else
					showCompletePane(result);
			}

			public void onFailure(Throwable caught)
			{
				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
   	}
   	
   	/**	Display a list of objects currently in range, with 'x' buttons to remove
   	 *	them; also display an 'add another' button and a message
   	 */
   	private void showCompletePane(ExistingInstances result)
   	{
   		setStyleName("wysiwym-popup-textbox");
   		setPopupPosition(50, 50);
   		setWidth("450px");

   		parentPanel = new VerticalPanel();
   		parentPanel.setSpacing(5);
   		
   		for (int i = 0; i < result.getRange().length; i++)	//save the old range
   		{												//which is also the current range!
   			oldRange.put(result.getRange()[i], result.getRangeID()[i]);
   			currentRange.add(result.getRange()[i]);
   		} 		
   		updateHtmlPanel();  		
   		
   		HorizontalPanel hp = new HorizontalPanel();
 		hp.add(okBtn);
 		hp.add(cancelBtn);
 		hp.add(helpBtn);
 		hp.add(resetBtn);
 		hp.setSpacing(20);
 		parentPanel.add(hp);

   		setWidget(parentPanel);
   		parent.hideHourglass();
   		show();
   	}
   	
   	/**	Updates the list of range objects.
   	 */
   	private void updateHtmlPanel()
   	{
   		if (parentPanel.getWidgetCount() > 1)
   		{
	   		parentPanel.remove(0);	//remove the label and horizontal panel
   			parentPanel.remove(0);
   		}
   		
   		HorizontalPanel hp = new HorizontalPanel();
   		hp.setSpacing(10);
   		
   		if (currentRange.size() == 0)
   		{
   			StringBuffer sb = new StringBuffer("You have not added any values ");
   			if (valueList != null)	//add list of possible range objects, if the server has retrieved it yet
   				sb.append(valueList);
   			sb.append(" yet for the property '" + nlname + "'. Please add some values by pressing 'add'.");
   			parentPanel.insert(new Label(sb.toString()), 0);
   			hp.add(new HTMLPanel(""));
   		}
   		else
   		{
   			parentPanel.insert(new Label("The values you have added so far for the property '" + nlname + "' are listed below. " +
   				"You can remove them by clicking 'x', or add new values by pressing 'add'. Press 'start over' to retrieve the original values."), 0);
   			
   			StringBuffer html = new StringBuffer("These are the values you have added so far:<ul>");
   			HashMap tmp = new HashMap();

   			for (int i = 0; i < currentRange.size(); i++)
   			{
   				String key = (String) currentRange.get(i);
   				html.append("<li>" + key + "&nbsp;<span id='");
   				String id = HTMLPanel.createUniqueId();
   				DeleteLabel lb = new DeleteLabel("x", key);
   				lb.setStyleName("wysiwym-label-red");
   				tmp.put(id, lb);
	   			html.append(id + "'></span>");
   				DOM.setStyleAttribute(lb.getElement(), "display", "inline");		
   			}
   		
   			html.append("</ul>");
   			HTMLPanel summaryPanel = new HTMLPanel(html.toString());
   			for (Iterator it = tmp.keySet().iterator(); it.hasNext(); )
   			{
   				String key = (String) it.next();
   				summaryPanel.add((DeleteLabel) tmp.get(key), key);
   			}
   			hp.add(summaryPanel);
   		}
   			
   		hp.add(addNewBtn);
   		parentPanel.insert(hp, 1);
   	}
   	
   	/**	'x' button to remove an object from the range
   	 */
   	private class DeleteLabel extends Label
   	{
   		private String remove;
   		
   		public DeleteLabel(String text, String r)
   		{
   			super(text);
   			remove = r;
   			sinkEvents(Event.ONCLICK | Event.MOUSEEVENTS | Event.ONMOUSEWHEEL);
	   		setStyleName("wysiwym-label-large");
	   		
	   		addClickListener(new ClickListener()
  			{
  				public void onClick(Widget sender)
  				{	//remove the value from the range
  					currentRange.remove(remove);
  					updateHtmlPanel();	//and update the html panel
				} 			
  			});		
   		}
   	}
   	
   	/**	Returns a list of the values in the range, e.g. '(e.g. state, mountain or river)'.
   	 */
   	public void makeValueList()
   	{
   		StringBuffer sb = new StringBuffer();
   		int size = hierarchy.length;
   		
   		if (size > 3)
   			sb.append("(e.g. ");
   		else
   			sb.append("(i.e. ");
   		if (size > 2)
   			sb.append(hierarchy[size - 3].getNLExpr() + ", ");
   		if (size > 1)
	   		sb.append(hierarchy[size - 2].getNLExpr() + " or ");
	   	sb.append(hierarchy[size - 1].getNLExpr() + ")");
	   	valueList = sb.toString();
   	}
   	
   	/**	Retrieves the hierarchy of classes that are in the range of this property
   	 */
   	private void getHierarchy()
   	{
   		String user = parent.getUser();
		if (user == null)
			return;

   		parent.getHandler().getRangeHierarchy(user, property, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				hierarchy = (Hierarchy[]) o;
				if (hierarchy.length == 0)
				{
					parent.hideHourglass();
					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
					parent.close();
				}
				makeValueList();
				makeTree();
			}

			public void onFailure(Throwable caught)
			{
				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
		});
   	}
   	
   	/**	Creates a tree with the class hierarchy of the range
   	 */
   	public void makeTree()
   	{	//create the tree hierarchy
   		treePP = new InputPopupPanel();
   		treePP.setStyleName("wysiwym-popup-textbox");
   		tree = new ListeningTree(this);
   		for (int i = 0; i < hierarchy.length; i++)
   			tree.addItem(addHierarchy(hierarchy[i]));

   		treePanel = new VerticalPanel();
   		treePanel.setSpacing(20);
   		treePanel.add(new Label("Please select the type of object you want to create."));
		treePanel.add(tree);
		openTree(tree, 20);

		HorizontalPanel btnPanel = new HorizontalPanel();
		Button ok = new Button("OK", new ClickListener()
		{
			public void onClick(Widget sender)
			{
				TreeItem item = tree.getSelectedItem();
   				if (item == null)
   					Window.alert("Please select the type of item you want to add from the tree.");
   				else
   					createItem(item.getText());	
			}
		});
		btnPanel.add(ok);
		
		Button cancel = new Button("Cancel", new ClickListener()
		{
			public void onClick(Widget sender)
			{
   				treePP.hide();				
			}
		});
		btnPanel.add(cancel);
		
		Button h = new Button("Help!", new ClickListener()
		{
			public void onClick(Widget sender)
			{
				help.showTreeHelp();
			}
		});
		btnPanel.add(h);
	
		btnPanel.setSpacing(20);
   		treePanel.add(btnPanel);
   		treePP.add(treePanel);
   	}
   	
   	/**	Creates a form for the selected class
   	 *	@param item NL-expression of class
   	 */
   	public void createItem(String item)
   	{
   		treePP.hide();
   		getForm(item, (String) hierarchyMap.get(item));
   	}
   	
   	/**	Shows the class hierarchy to the user
   	 */
   	public void showTree()
   	{
   		if ((hierarchy.length == 1) && (hierarchy[0].getSub().length == 0))//if there is only one type, there is no point asking the user!
   			getForm(hierarchy[0].getNLExpr(), hierarchy[0].getValue());	//so create a form for that object
   		else
   		{
	   		treePP.setPopupPosition(200, 50);
   			treePP.show();
   			parent.hideHourglass();
   		}
   	}
   	
   	/** Opens the first levels in the tree (so the class hierarchy doesn't become
	 * too huge to fit in the screen)
	 */
	private void openTree(Tree tree, int max)
	{
		int visibleItems = tree.getItemCount();
		for (int x = 0; x < tree.getItemCount(); x++)
		{
			TreeItem item = tree.getItem(x);
			visibleItems += item.getChildCount();
			if (visibleItems > max)
				break;
			else
				item.setState(true);
		}
	}
	   	
   	private TreeItem addHierarchy(Hierarchy h)
 	{
 		TreeItem item = new TreeItem(h.getNLExpr());
 		hierarchyMap.put(h.getNLExpr(), h.getValue());
 		Hierarchy[] sub = h.getSub();
 		for (int i = 0; i < sub.length; i++)
 			item.addItem(addHierarchy(sub[i]));
 		return item;
 	}
 	
 	private TreeItem copyItem(TreeItem item)
 	{
 		TreeItem copy = new TreeItem(item.getText());
 		for (int i = 0; i < item.getChildCount(); i++)
 			copy.addItem(copyItem(item.getChild(i)));
 		return copy;
 	}
 	 	
 	/**	Produces a form containing all string datatype properties of the new instance 
 	 *	which have min. cardinality >0.
 	 *	@param nl NL-representation of class
 	 *	@param classType Class name
 	 */
 	public void getForm(final String nl, final String classType)
 	{	//get all cardinal (string datatype?) properties from the server	
 		String user = parent.getUser();
		if (user == null)
			return;
		parent.showHourglass();
		
		parent.getHandler().getCardinalStringProperties(user, classType, new AsyncCallback()
 		{
 			public void onSuccess(Object o) 
   			{
   				if (o == null)
   				{
   					parent.hideHourglass();
   					Window.alert("There was an error while retrieving some information. Please try again.");
   					return;
   				}
   				formInformation = (FormInfo[]) o;
   				makeForm(nl, classType);
   			}
   	
   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
 		}); 		
 	}
 	
 	/**	Shows a tag cloud with the most frequent tags used for the various properties
	 *	in a form. This tag cloud is non-clickable, as we don't know what tags 
	 *	are meant for which boxes! Users will just have to copy paste.
	 */
	public void showTagCloud(TagCloud cloud)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < cloud.size(); i++)
		{
			sb.append("<font size=\"");
   			sb.append(Integer.toString(cloud.getFrequency(i) + 1));
   			sb.append("\">");
   			sb.append(cloud.getValue(i));
   			sb.append("</font> &nbsp; ");
   		}
   		HTMLPanel result = new HTMLPanel(sb.toString());
   		tab.showFolksonomy(result);
	}
 	
 	/**	Creates a form, and presents it to the user.
 	 *	@param nl NL-expression of class
 	 *	@param classType Class name
 	 */
 	public void makeForm(String nl, final String classType)
 	{	
 		if (formInformation.length == 0)
 		{	//no properties have to be specified, so add the new instance to summary
 			tempData = new InstanceData(classType, 0);
 			newInstances.put("A new " + nl, tempData);
 			tempData = null;
 			currentRange.add("A new " + nl);
 			updateHtmlPanel();
 			parent.hideHourglass();
 			return;
 		}
 		
 		String user = parent.getUser();
		if (user == null)
			return;
 		String[] properties = new String[formInformation.length];
		for (int i = 0; i < formInformation.length; i++)
			properties[i] = formInformation[i].getProperty();
		
		parent.getHandler().getTagCloud(user, properties, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o != null)
					showTagCloud((TagCloud)o);
			}
			public void onFailure(Throwable caught)
			{
				parent.hideHourglass();
				Window.alert(caught.getMessage());
			}
		});
 		 		
 		formPP = new InputPopupPanel();
   		formPP.setStyleName("wysiwym-popup-tree");
   		VerticalPanel panel = new VerticalPanel();
   		panel.setSpacing(20);
   		Label l = new Label("You are adding a " + classType + ". Please specify the following information:");
   		l.setStyleName("wysiwym-label-large");
   		panel.add(l);
   		if (type == parent.QUERY_TAB)
   		{
   			Label l2 = new Label("If any of the information is unknown, select N/A.");
   			panel.add(l2);
   		}
   		
   		HorizontalPanel hp = new HorizontalPanel();
   		inputWidget = new FocusWidget[formInformation.length];
   		VerticalPanel left = new VerticalPanel();
   		VerticalPanel right = new VerticalPanel();
   		left.setSpacing(20);
   		right.setSpacing(20);
   		
   		for (int i = 0; i < formInformation.length; i++)
   		{			
   			left.add(new Label(formInformation[i].getNL()));
   			String[] values = formInformation[i].getValues();
   			if (values.length == 0)	//free-text value, so add a textbox
   				inputWidget[i] = new TextBox();
   			else
   			{	//list of restricted values, so create a listbox
   				ListBox lb = new ListBox();
   				for (int j = 0; j < values.length; j++)
   					lb.addItem(values[j]);
   				inputWidget[i] = lb;
   			}
   			right.add(inputWidget[i]);
   		}
   		hp.add(left);
   		hp.add(right);
   		if (type == parent.QUERY_TAB)
   		{
   			na = new CheckBox[formInformation.length];
   			VerticalPanel naPanel = new VerticalPanel();
   			naPanel.setSpacing(20);
   			for (int i = 0; i < formInformation.length; i++)
   			{
   				na[i] = new CheckBox("n/a");
   				naPanel.add(na[i]);
   			}
   			hp.add(naPanel);
   		}
   		panel.add(hp);
   		
   		HorizontalPanel btnPanel = new HorizontalPanel();
   		btnPanel.setSpacing(20);
   		Button ok = new Button("OK", new ClickListener()
   		{
   			public void onClick(Widget sender)
   			{	
   				parent.showHourglass();
   				storeNewInstance(classType);
   			}
   		});
   		Button cancel = new Button("Cancel", new ClickListener()
   		{
   			public void onClick(Widget sender)
   			{
   				formPP.hide();
   				tab.removeFolksonomy();
   			}
   		});
   		
   		Button h = new Button("Help!", new ClickListener()
		{
			public void onClick(Widget sender)
			{
				help.showFormHelp();
			}
		});
   		btnPanel.add(ok);
   		btnPanel.add(cancel);
   		btnPanel.add(h);
   		panel.add(btnPanel);
   		formPP.setWidget(panel);
		formPP.setPopupPosition(150, 100);
		formPP.show();

		parent.hideHourglass();
		inputWidget[0].setFocus(true);	
		if (inputWidget[0] instanceof TextBox)
   			((TextBox) inputWidget[0]).setCursorPos(0);
 	}
 	
 	/**	Stores the data supplied by the user in an InstanceData.
 	 *	@param classType Class name
 	 */
 	public void storeNewInstance(String classType)
 	{	//check if all input has been supplied, then store it and add a reference to summary
   		tempData = new InstanceData(classType, inputWidget.length);
   		for (int i = 0; i < inputWidget.length; i++)
   		{
   			if ((type == parent.QUERY_TAB) && na[i].isChecked())
   				continue;	//ignore those values checked 'not applicable'
   			else if (inputWidget[i] instanceof TextBox)
   			{
   				String text = ((TextBox)inputWidget[i]).getText();
   				if (text.length() == 0)
   				{
   					parent.hideHourglass();
   					Window.alert("Please provide or select a value for all properties.");
   					return;	
   				}
   				else if (!text.equalsIgnoreCase("n/a"))	//in case someone types this rather than ticking the box
   					tempData.add(formInformation[i].getProperty(), text, i);
   			}
   			else
   			{
   				ListBox lb = (ListBox) inputWidget[i];
   				if (lb.getSelectedIndex() < 0)
   				{
   					parent.hideHourglass();
   					Window.alert("Please provide or select a value for all properties.");
   					return;	
   				}
   				tempData.add(formInformation[i].getProperty(), lb.getItemText(lb.getSelectedIndex()), i);
   			}
   		}
   		
   		tab.removeFolksonomy();
   		formPP.hide(); 	
		addValue();
   	}
   	
   	/**	Adds a representation of the newly created object in summaryLB
   	 */
   	public void addValue()
   	{	//create a header and put it in summary
   		String firstProp = tempData.getProperty(0);
   		StringBuffer key = new StringBuffer("The ");
   		key.append(tempData.getType());
   		if (firstProp.equals("Name") || firstProp.equals("Title"))
   			key.append(" \"" + tempData.getValue(0) + "\"");

   		int cntr = 2;
   		while (newInstances.containsKey(key.toString()))
   		{	//make sure there are no two entries with the same title
   			if (cntr == 2)
   				key.append(" (2)");
   			else
   				key.replace(key.length() - 2, key.length() - 1, Integer.toString(cntr));
   			cntr++;
   		}
   		newInstances.put(key.toString(), tempData);	//then store the collected data
   		currentRange.add(key.toString());
   		updateHtmlPanel();
   		tempData = null;
   		parent.hideHourglass();
 	}
 	
 	private void displayOptions(String[] options, boolean automatic)
 	{
 		new ArchiveOptionsPresenter(options, parent, this, tab, automatic);
 	}
 	
 	/**	Sends the elicited information to the server
 	 */
 	private void update()
 	{
 		parent.showHourglass();
 		hide();
 		String user = parent.getUser();
		if (user == null)
			return;
		
		InstanceData[] range = new InstanceData[currentRange.size()]; //[summaryLB.getItemCount()];
		for (int i = 0; i < currentRange.size(); i++)
		{
			String key = (String) currentRange.get(i);
			if (oldRange.containsKey(key))
				range[i] = new InstanceData((String) oldRange.get(key));
			else if (newInstances.containsKey(key))
				range[i] = (InstanceData) newInstances.get(key);
		}
		
 		parent.getHandler().updateObjectProperty(user, anchor, property, range, type, key, new AsyncCallback()
 		{
 			public void onSuccess(Object o) 
   			{
   				if (o == null)
   				{
   					parent.hideHourglass();
   					Window.alert("There was an error while updating the feedback text. Please try again.");
   					return;
   				}
   				AnchorInfo[] result = (AnchorInfo[]) o;
   				if (result.length == 0)
   				{
   					parent.hideHourglass();
   					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   					parent.close();
   				}
   				else
	   				tab.initPanel(result);
   			}
   	
   			public void onFailure(Throwable caught) 
   			{
   				parent.hideHourglass();
   				Window.alert(caught.getMessage());
   			}
 		});
 	}
 
 	/**	Click listener, handles all button events. If cancel, hide the pop-up;
 	 *	if help, show help window; if reset, remove all values; if addNew, show
 	 *	the class hierarchy; and if ok, check that some values were added and 
 	 *	update the feedback text.
 	 *	@param sender Button Widget
 	 */		
   	public void onClick(Widget sender)
   	{
   		buttonsPressed++;
   		if (sender == okBtn)
   		{
   			if (currentRange.size() == 0)	//(summaryLB.getItemCount() == 0)
   				Window.alert("Please specify some item(s) that apply to the property '" + nlname + "'.");
   			else
   				update();
   		}
   		else if (sender == cancelBtn)
			hide();  			
   		else if (sender == helpBtn)
   			help.showObjectPropertyHelp();
   		else if (sender == addNewBtn)
   			showTree();
   		else if (sender == resetBtn)
   		{
   			currentRange = new ArrayList(oldRange.keySet());
   			updateHtmlPanel();
   		}
   	}
 }