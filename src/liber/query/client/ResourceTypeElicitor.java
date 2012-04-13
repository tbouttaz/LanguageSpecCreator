package liber.query.client;

import java.util.HashMap;

import liber.edit.client.AnchorInfo;
import liber.edit.client.Hierarchy;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**	Shows the user the types of resource that he can deposit, and asks him to
 *	specify what he is searching for. 
 *
 *	@author Feikje Hielkema
 *	@version 1.2 October 2007
 *	@version 1.4 August 2008
 */
public class ResourceTypeElicitor extends PopupPanel implements ClickListener
{
	private WysiwymTab tab;
	private WYSIWYMinterface parent;
	private String[] classes;
	
	private Button okBtn = new Button("OK", this);
	private Button helpBtn = new Button("Help!", this);
	private Tree tree, confirmTree;
	private boolean query = false, showing = false, confirm = false;
	private int buttonsPressed = 0;
	private PopupPanel confirmationPP;
	
	private Help help;
	private HashMap classMap;
	
	/**	Constructor.
	 *	@param u Username
	 *	@param id User ID
	 *	@param t Parent module
	 *	@param p Entrypoint
	 */
	public ResourceTypeElicitor(String u, String id, WysiwymTab t, WYSIWYMinterface p)
	{
		super();
		tab = t;
		parent = p;
		setStyleName("wysiwym-popup-tree");
		help = new Help(parent, tab.getType());
		if (t instanceof QueryTab)
			query = true;
	}
	
	/**	Constructor.
	 *	@param t Parent module
	 *	@param p Entrypoint
	 */
	public ResourceTypeElicitor(WysiwymTab t, WYSIWYMinterface p)
	{
		super();
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
		sb.append("What kind of resource are you searching for? Please choose a type from the tree below.");
		return new Label(sb.toString());
	}
	
	/**	Retrieves the class hierarchy from the server and displays it
	 */
	public void showPanel()
	{
		tree = new ListeningTree(this);
		String userID = parent.getUser();
		if (userID == null)
			return;
			
		parent.getHandler().initSessionAndGetClassHierarchy(userID, classes, new AsyncCallback()
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
   		setWidth("400px");
   		setPopupPosition(50, 50);
   		showing = true;
   		parent.hideHourglass();   		
   		show();
   		parent.startQuery = System.currentTimeMillis();
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

	/**	Displays this pop-up again, if an error happened during initialisation
	 *	of the session
	 */		
	public void reshow()
	{
		parent.hideHourglass();
		showing = true;
		show();
	}
	
	/**	Initialises the querying session with the selected item.
	 *	@param item Class in tree
	 */
	public void initSession(String item)
	{
		String userID = parent.getUser();
		if (userID == null)
			return;
			
		parent.showHourglass();
		((QueryTab)tab).setMessage(item);
   		showing = false;
		hide();

		parent.getHandler().initSession(userID, (String) classMap.get(item), tab.getType(), new AsyncCallback()	
		{
			public void onSuccess(Object o)
			{
				if (o == null)
				{
					Window.alert("I'm afraid an error happened during the generation of your query. "+
					"Please try choosing a different resource type");
					reshow();
   					return;
   				}
   				
   				AnchorInfo[] result = (AnchorInfo[]) o;
   				if (result.length == 0)
		   		{
   					Window.alert("I'm afraid your session has expired. Please return to ourSpaces to start a new session.");
   					parent.close();
   					return;
   				}
				tab.initPanel((AnchorInfo[])o, false);
			}
		
			public void onFailure(Throwable caught) 
   			{
   				Window.alert(caught.getMessage());
			}
		});
	}
	
	/**	Click listener on buttons. If help, display help diagram;
	 *	if ok, initialise session.
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
   			else
	  			initSession(item.getText());
   		}
   		else if (sender == helpBtn)
   			help.showResourceTypeHelp();
	}
}