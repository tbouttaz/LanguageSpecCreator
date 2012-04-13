package liber.edit.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
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

/**	Shows the user the types of resource that he can deposit, and asks him to
 *	specify what he is depositing. 
 *
 *	@author Feikje Hielkema
 *	@version 1.2 October 2007
 */
public class ResourceTypeElicitor extends PopupPanel implements ClickListener
{
	private String user, userID;
	private WysiwymTab tab;
	private WYSIWYMinterface parent;
	private String[] classes;
	
	private Button okBtn = new Button("OK", this);
	private Button helpBtn = new Button("Help!", this);
	private Tree tree, confirmTree;
	private boolean query = false, showing = false, confirm = false;
	private int buttonsPressed = 0;
	private PopupPanel confirmationPP;
	private FocusWidget[] inputWidget;
	
	private Help help;
	private HashMap classMap;
	private PopupPanel formPP;
	
	/**	Constructor.
	 *	@param u Username
	 *	@param id User ID
	 *	@param t Parent module
	 *	@param p Entrypoint
	 */
	public ResourceTypeElicitor(String u, String id, WysiwymTab t, WYSIWYMinterface p)
	{
		super();
		user = u;
		userID = id;
		tab = t;
		parent = p;
		setStyleName("wysiwym-popup-tree");
		help = new Help(parent, tab.getType());
		if (t instanceof QueryTab)
			query = true;
	}
	
	/**	Sets the classes which should be shown as options (with their subclasses)
	 *	@param candidates Names of classes that (with their subclasses) should be displayed
	 */
	public void setCandidates(String[] candidates)
	{
		classes = candidates;
	}
	
	private TreeItem addHierarchy(Hierarchy h)
 	{
 		classMap.put(h.getNLExpr(), h.getValue());
 		TreeItem item = new TreeItem(h.getNLExpr());
 		Hierarchy[] sub = h.getSub();
 		for (int i = 0; i < sub.length; i++)
 			item.addItem(addHierarchy(sub[i]));
 		return item;
 	}
	
	private Label getGreeting()
	{
		StringBuffer sb = new StringBuffer();
		Date c = new Date();
		int hour = c.getHours();
		if (hour < 5)
			sb.append("Good evening");
		else if (hour < 12)
			sb.append("Good morning");
		else if (hour < 18)
			sb.append("Good afternoon");
		else 
			sb.append("Good evening");
	
		if (user != null)
			sb.append(", " + user);	
		sb.append(".\nWhat resource ");
		if (query)
			sb.append("are you searching for");
		else
			sb.append("would you like to deposit");
		sb.append(" today?\nChoose a type from the tree below (please be as specific as possible).");
		Label lb = new Label(sb.toString());
		lb.setStyleName("linebreak-label");
		return lb;
	}
	
	/**	Retrieves the class hierarchy from the server and displays it
	 */
	public void showPanel()
	{
		tree = new ListeningTree(this);
		parent.getHandler().getClassHierarchy(userID, classes, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
    				parent.close();
    				return;
    			}
				show((Hierarchy[]) o);
			}
			
			public void onFailure(Throwable caught) 
	   		{
	   			Window.alert(caught.getMessage());
			}
		});
	}
 	
 	/**	Shows the class hierarchy
 	 *	@param hierarchy Class hierarchy
 	 */
 	public void show(Hierarchy[] hierarchy)
 	{
 		classMap = new HashMap();
 		for (int i = 0; i < hierarchy.length; i++)
 		{
 			TreeItem ti = addHierarchy(hierarchy[i]);
	 		tree.addItem(ti);
	 	}
		
   		VerticalPanel vp = new VerticalPanel();
   		vp.add(getGreeting());
		vp.add(tree);
		openTree(tree, 0);
   		vp.setSpacing(10);
   		
   		HorizontalPanel hp = new HorizontalPanel();
   		hp.setSpacing(20);
   		hp.add(okBtn);
   		hp.add(helpBtn);
   		vp.add(hp);
   		
   		setWidget(vp);
   		setPopupPosition(50, 50);
   		showing = true;
   		parent.hideHourglass();   		
   		show();
	}
	
	/** Opens the first (two) levels in the tree (so the class hierarchy doesn't become
	 * too huge to fit in the screen)
	 */
	private void openTree(Tree tree, int level)
	{
		for (int i = 0; i < tree.getItemCount(); i++)
		{
			TreeItem item = tree.getItem(i);
			if (item.getText().equals("Resource") || item.getText().equals("Task"))
				item.setState(true);
		}
	}
	
	private TreeItem copyItem(TreeItem item)
 	{
 		TreeItem copy = new TreeItem(item.getText());
 		for (int i = 0; i < item.getChildCount(); i++)
 			copy.addItem(copyItem(item.getChild(i)));
 		return copy;
 	}
	
	/**	Displays a pop-up that checks whether the user is sure he does
	 *	not want to specify a subclass (i.e. more specific class)
	 */
	private void confirmChoice(TreeItem item)
	{
		confirmationPP = new PopupPanel();
		confirmationPP.setStyleName("wysiwym-popup-textbox");
		VerticalPanel vp = new VerticalPanel();
		Label lb = new Label("Are you sure you do not want to choose a more specific resource type? " +
		"The more specific the type, the more information can be specified about it.");
		vp.add(lb);

		confirmTree = new ListeningTree(this);
		TreeItem copy = copyItem(item);
		confirmTree.addItem(copy);
		vp.add(confirmTree);
		openTree(confirmTree, 1);	//open extra level in the tree
		for (Iterator it = confirmTree.treeItemIterator(); it.hasNext(); )
			((TreeItem)it.next()).setState(true);
		//confirmTree.setSelectedItem(item);	//this line crashes the interface in Internet Explorer, no idea why.		
		HorizontalPanel btnPanel = new HorizontalPanel();
		Button ok = new Button("OK", new ClickListener()
		{
			public void onClick(Widget sender)
			{
				buttonsPressed++;
				TreeItem item = confirmTree.getSelectedItem();
				if (item == null)
   					Window.alert("Please select an item or press 'cancel'.");
   				else
   				{
   					confirmationPP.hide();
   					confirm = false;
   					initSession(item.getText());
   				}
			}
		});
		btnPanel.setSpacing(20);
		btnPanel.add(ok);
		Button cancel = new Button("Cancel", new ClickListener()
		{
			public void onClick(Widget sender)
			{
				buttonsPressed++;
				confirm = false;
				confirmationPP.hide();
			}
		});
		btnPanel.add(cancel);
	
		Button helpBtn2 = new Button("Help!", new ClickListener()
		{
			public void onClick(Widget sender)
			{
				buttonsPressed++;
				help.showSpecificTypeHelp();
			}
		});
		btnPanel.add(helpBtn2);
		vp.add(btnPanel);
		vp.setSpacing(10);	
		confirmationPP.setWidget(vp);
		confirmationPP.setPopupPosition(100, 100);
		confirmationPP.setWidth("300px");
		confirm = true;
		confirmationPP.show();
		ok.setFocus(true);
	}
	
	/**	If the given item is double-clicked, act as if user pressed 'ok'.
	 *	If it has subclasses, display 'confirm choice', else init session.
	 *	@param item Class in tree
	 */
	public void onDoubleClick(TreeItem item)
	{
		if (item == null)
			Window.alert("Please select an item or press 'cancel'.");
		else if (confirm)
		{
			confirmationPP.hide();
   			confirm = false;
   			initSession(item.getText());
		}
		else if ((!query) && (item.getChildCount() > 0))
   			confirmChoice(item);
		else
			initSession(item.getText());
	}
	
	/**	Displays this pop-up again, if an error happened during initialisation
	 *	of the session
	 */
	public void reshow()
	{
		parent.hideHourglass();
		showing = true;
		show();
	}
	
	/**	Initialises the editing session with the selected item.
	 *	Generates and displays a form that elicits compulsory information 
	 *	about the new object.
	 *	@param item Class in tree
	 */
	public void initSession(final String item)
	{
		parent.showHourglass();
		tab.setMessage(user, item, null);
   		showing = false;
		hide();
		
		final String type = (String) classMap.get(item);
		parent.getHandler().getCardinalStringProperties(userID, type, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					Window.alert("I'm afraid an error happened during the regeneration of your feedback text. "+
					"Please try choosing a different resource type");
					reshow();
   					return;
   				}
   				
   				FormInfo[] result = (FormInfo[]) o;
   				if (result.length == 0)	//no properties have to be specified, so no form can be created
		   			getDescription(type, result);   					
   				else
					showForm(result, item, type);		//create a form
			}
		
			public void onFailure(Throwable caught) 
   			{
   				Window.alert(caught.getMessage());
			}
		});
	}
	
	/**	Extracts the data from the form and initialises a new session.
	 *	@param type Class name
	 *	@param info Details of form, what it should contain
	 */
	public void getDescription(String type, FormInfo[] info)
	{
		InstanceData data = new InstanceData(type, 0);
		if (inputWidget != null)
		{
			data = new InstanceData(type, inputWidget.length);
   			for (int i = 0; i < inputWidget.length; i++)
   			{
   				if (inputWidget[i] instanceof TextBox)
   				{
   					String text = ((TextBox) inputWidget[i]).getText();
   					if (text.length() == 0)
   					{
   						Window.alert("Please provide or select a value for all properties.");
   						parent.hideHourglass();
   						return;	
   					}
   					data.add(info[i].getProperty(), text, i);
   				}
   				else
   				{
   					ListBox lb = (ListBox) inputWidget[i];
   					if (lb.getSelectedIndex() < 0)
   					{
   						Window.alert("Please provide or select a value for all properties.");
   						parent.hideHourglass();
   						return;	
   					}
   					data.add(info[i].getProperty(), lb.getItemText(lb.getSelectedIndex()), i);
   				}
   			}
   			formPP.hide();
   			tab.hideFolksonomy();
		}
		
		parent.getHandler().initSession(userID, data, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   					parent.close();
   					return;
				}
				AnchorInfo[] result = (AnchorInfo[]) o;
   				if (result.length == 0)
				{
					Window.alert("I'm afraid an error happened during the regeneration of your feedback text. "+
					"Please try choosing a different resource type");
					reshow();
   				}
   				else
   					tab.initPanel(result, false);
   			}
		
			public void onFailure(Throwable caught) 
   			{
   				Window.alert(caught.getMessage());
			}
		});		
	}
	
	/**	Shows a tag cloud with the most frequent tags used for the various properties
	 *	in a form. This tag cloud is non-clickable, as we don't know what tags 
	 *	are meant for which boxes! Users will just have to copy paste.
	 *
	 *	@param cloud TagCloud
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
	
	/**	Shows a form with which the cardinal properties can be added quickly.
	 *
	 *	@param info Information about form
	 *	@param nl NL-expression of class
	 *	@param type Class name
	 */
	public void showForm(final FormInfo[] info, String nl, final String type)
	{
		String[] properties = new String[info.length];
		for (int i = 0; i < info.length; i++)
			properties[i] = info[i].getProperty();
		parent.getHandler().getTagCloud(userID, properties, new AsyncCallback()
		{
			public void onSuccess(Object o)
			{
				if (o != null)
					showTagCloud((TagCloud)o);
			}
			public void onFailure(Throwable caught)
			{
				Window.alert(caught.getMessage());
			}
		});
		
		formPP = new InputPopupPanel();
   		formPP.setStyleName("wysiwym-popup-tree");
   		VerticalPanel panel = new VerticalPanel();
   		panel.setSpacing(20);
   		Label l = new Label("Please specify the following information:");
   		l.setStyleName("wysiwym-label-large");
   		panel.add(l);
   		
   		HorizontalPanel hp = new HorizontalPanel();
   		inputWidget = new FocusWidget[info.length];
   		VerticalPanel left = new VerticalPanel();
   		VerticalPanel right = new VerticalPanel();
   		left.setSpacing(20);
   		right.setSpacing(20);
   		
   		for (int i = 0; i < info.length; i++)
   		{			
   			left.add(new Label(info[i].getNL()));
   			String[] values = info[i].getValues();
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
   		panel.add(hp);
   		   		
   		HorizontalPanel btnPanel = new HorizontalPanel();
   		btnPanel.setSpacing(20);
   		Button ok = new Button("OK", new ClickListener()
   		{
   			public void onClick(Widget sender)
   			{	
   				parent.showHourglass();
   				getDescription(type, info);
   			}
   		});
   		Button cancel = new Button("Cancel", new ClickListener()
   		{
   			public void onClick(Widget sender)
   			{
   				formPP.hide();
   				tab.reset();	//reset
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
		formPP.setPopupPosition(25, 50);
		formPP.show();

		parent.hideHourglass();
		inputWidget[0].setFocus(true);	
		if (inputWidget[0] instanceof TextBox)
   			((TextBox) inputWidget[0]).setCursorPos(0);
	}
	
	/**	Click listener on buttons. If help, display help diagram;
	 *	if ok, display 'confirm choice' pop-up or generate form.
	 *	@param sender Button Widget
	 */
	public void onClick(Widget sender)
	{
		buttonsPressed++;
		if (sender == okBtn)
		{
			TreeItem item = tree.getSelectedItem();
   			if (item == null)
   				Window.alert("Please select an item or press 'cancel'.");
   			else if ((!query) && (item.getChildCount() > 0))
   				confirmChoice(item);
   			else
	  			initSession(item.getText());
   		}
   		else if (sender == helpBtn)
   			help.showResourceTypeHelp();
	}
}