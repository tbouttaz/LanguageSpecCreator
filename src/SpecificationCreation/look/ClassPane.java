package SpecificationCreation.look;

import java.awt.Button;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import SpecificationCreation.SpecificationCreator;
import SpecificationCreation.ontology.SpecificationOntologyReader;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.SGNode;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;

/**	Displays the LIBER pop-up menu of the selected class, and enables the user
 *	to change this look and add specifications. Invisible properties are shown
 *	in a list on the right, and can be made visible by dragging them to the tree 
 *	on the left. Sub-menus can be added and populated, cardinality constraints
 *	and linguistic specifications added.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 October 21 2008
 */
public class ClassPane implements ActionListener
{
	private Container content;
	private Button backBtn = new Button("<< Back");
	private Button submenuBtn = new Button("Add a submenu");
	private Button exampleBtn = new Button("Show example text");
	
	private SpecificationCreator parent;
	private SpecificationPane specificationPane;
	private SpecificationOntologyReader reader;
	private OntClass c;
	private String className;
	private Map<String,PropertyInfo> map = new HashMap<String,PropertyInfo>();
	private Map<String,SubmenuInfo> submenus = new HashMap<String,SubmenuInfo>();
	
	private JTree propertyTree;
	private JList propertyList;
	private JPopupMenu classMenu;
	private DefaultListModel listModel;
	private DefaultTreeModel treeModel;
	private ClassTreeNode root;
	
	private boolean shaded = false;
	
	/**	Constructor
	 *	@param name Class name
	 *	@param content Root container
	 *	@param reader Ontology
	 *	@param parent Parent frame
	 *	@param shaded True if the class was imported from another ontology
	 */
	public ClassPane(String name, Container content, SpecificationOntologyReader reader, SpecificationCreator parent, boolean shaded)
	{
		this.reader = reader;
		className = name;
		c = reader.getClass(name);
		//TODO: change syso to a popup message
		if (c == null) {
			System.out.println("the class " + name + " wasn't find in the OntologyReader");
		}
		this.content = content;
		this.parent = parent;
		this.shaded = shaded;
		
		init();
	}
	
	/**	Initialise the page with proper layout and all.
	 */
	public void init()
	{
		content.removeAll();
		
		GridBagConstraints constraint = new GridBagConstraints();
 		JTextArea area = new JTextArea("You have selected the class '" + c.getLocalName() +
 			"'. The pane below indicates the way this class will be presented in LIBER's language descriptions, " +
 			"and the pop-up menu that will appear if the user clicks on it. You can add and remove properties to the menu from the list, " +
 			"order them in submenus, and add linguistic and cardinality information by right-clicking on the properties.");
 		area.setEditable(false);
 		area.setLineWrap(true);
 		area.setWrapStyleWord(true);
 		constraint.insets = new Insets(10, 10, 10, 10);
 		constraint.fill = GridBagConstraints.HORIZONTAL;
		constraint.anchor = GridBagConstraints.CENTER;
		constraint.weightx = 1.0;
		constraint.gridx = 0;
		constraint.gridy = 0;
		constraint.gridwidth = 2;
		content.add(area, constraint);
		
		constraint.weightx = 0.7;
		constraint.weighty = 1.0;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 0;
		constraint.gridy = 1;
		constraint.fill = GridBagConstraints.BOTH;
		constraint.gridwidth = 1;	
		JScrollPane scroll = new JScrollPane(getPropertyTree());
 		content.add(scroll, constraint);
 		
 		constraint.weightx = 0.3;
		constraint.weighty = 1.0;	//add extra space to the tree widget, so it can expand
		constraint.anchor = GridBagConstraints.FIRST_LINE_START;
		constraint.gridx = 1;
		constraint.gridy = 1;
		constraint.fill = GridBagConstraints.BOTH;	
		constraint.gridwidth = 1;
		JScrollPane scroll2 = new JScrollPane(propertyList);
 		content.add(scroll2, constraint);
 		
 		constraint.fill = GridBagConstraints.NONE;
 		constraint.anchor = GridBagConstraints.PAGE_START;
 		constraint.weighty = 0;	
 		constraint.weightx = 0;
 		constraint.gridx = 0;
 		constraint.gridy = 2;
 		constraint.gridwidth = 2;
 		backBtn.addActionListener(this);
 		submenuBtn.addActionListener(this);
 		exampleBtn.addActionListener(this);
 		
 		Container cont = new Container();
 		cont.add(backBtn);
 		cont.add(submenuBtn);
 		cont.add(exampleBtn);
 		cont.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
 		content.add(cont, constraint);
 		
 		content.validate();
	}
	
	/**	Creates a tree of properties that resembles a LIBER pop-up menu. Minimum
	 *	Cardinal props are red, submenus blue (and indiv. nodes can be dragged to 
	 *	those submenus), 'finished' props have an ok icon.
	 */
	private JTree getPropertyTree()
	{	//create example 'anchor' with class name
		listModel = new DefaultListModel();
		map = new HashMap<String,PropertyInfo>();
		submenus = new HashMap<String,SubmenuInfo>();
		root = new ClassTreeNode(getNL(c, className, true), shaded, false, false, false);
		root.setRoot(true);
		createMenu(c, root);
		
		Map<String,Integer[]> cardinalityMap = reader.getCardinalities(c.getLocalName());
		treeModel = new DefaultTreeModel(root);
		propertyTree = new JTree(treeModel);
		propertyTree.setCellRenderer(new TreeRenderer("images/ok.png"));

		java.util.List<OntProperty> properties = reader.getDomainProperties(className);
		Map<ClassTreeNode,java.util.List<ClassTreeNode>> tempMap = new HashMap<ClassTreeNode,java.util.List<ClassTreeNode>>();
		tempMap.put(root, new ArrayList<ClassTreeNode>());
		java.util.List<String> tempList = new ArrayList<String>();
		
		for (OntProperty prop : properties)
		{
			ClassTreeNode child = makeNode(prop, cardinalityMap);
			String submenu = reader.getSubmenu(prop, className);
			if (submenu == null)
			{
				if (reader.isImported(prop))
					continue;	//ignore properties that have been imported and are not supplied with menu comments
				if (child.isCardinal())	//is no info has been added yet, put it in the list rather than the tree
				{	//store submenu comment, and add to root
					reader.changeMenu(prop, className, SpecificationOntologyReader.NONE);
					tempMap.get(root).add(child);
				}
				else
					tempList.add(prop.getLocalName());
			}
			else if (submenu.equals(SpecificationOntologyReader.NONE))
				tempMap.get(root).add(child);
			else if (submenus.containsKey(submenu))
			{
				SubmenuInfo info = submenus.get(submenu);
				info.add(prop);	//add this property to that menu
				tempMap.get(info.node).add(child);
			}
			else
			{
				ClassTreeNode sub = new ClassTreeNode(submenu, false, false, true, false);
				SubmenuInfo info = new SubmenuInfo(submenu, sub, makeRemoveMenu(sub));
				info.add(prop);
				java.util.List<ClassTreeNode> list = new ArrayList<ClassTreeNode>();
				list.add(child);
				tempMap.put(info.node, list);
				submenus.put(submenu, info);
			}
		}
		//make sure cardinal nodes are first; then other properties, then submenus.
		java.util.List<ClassTreeNode> nodeList = tempMap.get(root);	//within these categories all should be ordered alphabetically
		Collections.sort(nodeList);	//add all children to root
		for (ClassTreeNode node: nodeList)
			root.add(node);
		ClassTreeNode[] menuArray = (ClassTreeNode[]) tempMap.keySet().toArray(new ClassTreeNode[0]);
		Arrays.sort(menuArray);	
		for (int i = 0; i < menuArray.length; i++)
		{
			if (menuArray[i] == root)
				continue;
			root.add(menuArray[i]);
			nodeList = tempMap.get(menuArray[i]);
			Collections.sort(nodeList);	//add all children to root
			for (ClassTreeNode node: nodeList)
				menuArray[i].add(node);
		}	
		
		Collections.sort(tempList);
		for (String temp : tempList)	//fill the list
			listModel.addElement(temp);
		
		TreeTransferHandler transfer = new TreeTransferHandler(this);
		propertyTree.setDragEnabled(true);
		propertyTree.setDropMode(DropMode.ON_OR_INSERT);	
		propertyTree.setTransferHandler(transfer);
		
		//Items should have popupmenus, to change name, add spec, or add constraint
		propertyTree.addMouseListener(new MouseAdapter()
 		{
 			public void mousePressed(MouseEvent e)
 			{
 				int selRow = propertyTree.getRowForLocation(e.getX(), e.getY());
        		TreePath path = propertyTree.getPathForLocation(e.getX(), e.getY());
         		if ((selRow != -1) && (e.getButton() == MouseEvent.BUTTON3))	//if the user right clicks this node
         		{
         			propertyTree.setSelectionPath(path);	//select the node
         			ClassTreeNode node = (ClassTreeNode) path.getLastPathComponent();
         			if (node != null)
         			{
         				if (node.isRoot())
         					classMenu.show(e.getComponent(), e.getX(), e.getY());
         				else if (map.containsKey(node.toString()))
         				{
							JPopupMenu menu = map.get(node.toString()).menu;		//and show its menu
    		     			menu.show(e.getComponent(), e.getX(), e.getY());
         				}
         				else if (submenus.containsKey(node.toString()))
         				{
         					JPopupMenu menu = submenus.get(node.toString()).removeMenu;		//and show its menu
    		     			menu.show(e.getComponent(), e.getX(), e.getY());
         				}
    	     		}
         		}
 			}
 		});
		
		propertyList = new JList(listModel);
		propertyList.setDragEnabled(true);
		propertyList.setDropMode(DropMode.INSERT);
		propertyList.setTransferHandler(transfer);
		
		return propertyTree;
	}
	
	/**	Checks whether the given property has a specification associated
	 *	with it. If an inverse or super-property has a specification that
	 *	counts as well. If the property is a proper-name property, it does
	 *	not need a specification so the method returns true as well.
	 *	@param p Property
	 *	@return True if no other specification is needed
	 */
	public boolean specExists(OntProperty p)
	{
		if (new File(parent.getDirectory() + p.getLocalName() + ".xml").exists())
			return true;	//if there is a specification for this property, return true
		OntProperty inverse = reader.getInverse(p);
		if ((inverse != null) && new File(parent.getDirectory() + inverse.getLocalName() + ".xml").exists())
			return true;	//if there's a specification for the inverse, return true
	//	if (reader.hasComment(p, SpecificationOntologyReader.PROPERNAME))
	//		return true;	//if this is a proper name property, return true
		
		for (Iterator it = p.listSuperProperties(); it.hasNext(); )
		{	//check the superproperties for linguistic specifications
			OntProperty superP = (OntProperty) it.next();
			if (new File(parent.getDirectory() + superP.getLocalName() + ".xml").exists())
				return true;
			inverse = reader.getInverse(superP);	//and their inverses
			if ((inverse != null) && new File(parent.getDirectory() + inverse.getLocalName() + ".xml").exists())
				return true;
		}
		return false;	//and if all that fails, return false!
	}
	
	/**	Creates a node for a property, and adds a menu with the options:
	 *	change name, make a spec, add a restriction.
	 */
	private ClassTreeNode makeNode(OntProperty prop, Map<String,Integer[]> cardinalityMap)
	{	//find if there is a specification for this property
		String name = prop.getLocalName();
		boolean specExists = specExists(prop);	//check if there already is a specification
		boolean shaded = reader.isImported(prop);
		String nl = getNL(prop, name);
		
		ClassTreeNode result = new ClassTreeNode(nl, shaded, specExists, false, reader.hasComment(prop, SpecificationOntologyReader.PROPERNAME));
		if (cardinalityMap.containsKey(name))
			result.setCardinal(cardinalityMap.get(name)[0] > 0);
		map.put(nl, new PropertyInfo(nl, prop, createMenu(prop, result, specExists)));
		return result;
	}
	
	/**	Removes a node from the tree.
	 */
	public void removeNode(TreePath path)
	{
		ClassTreeNode node = (ClassTreeNode) path.getLastPathComponent();
		String menu = path.getParentPath().getLastPathComponent().toString();
		OntProperty prop = null;
		if (map.containsKey(node.toString()))
			prop = map.get(node.toString()).property;
		else
			prop = reader.getProperty(node.toString());

		if (submenus.containsKey(menu))
			submenus.get(menu).remove(prop);
		treeModel.removeNodeFromParent(node);
	}
	
	/**	Called when the user drops a property in a submenu.
	 *
	 *	@param value Label
	 *	@param path TreePath
	 */
	public void makeNode(String value, TreePath path)
	{
		OntProperty property = null;
		if (map.containsKey(value))
			property = map.remove(value).property;
		else
			property = reader.getProperty(value);
			
		ClassTreeNode node = makeNode(property, reader.getCardinalities(c.getLocalName()));
		ClassTreeNode parent = (ClassTreeNode) path.getLastPathComponent();
		treeModel.insertNodeInto(node, parent, parent.getChildCount());
		if (parent.isRoot())
			reader.changeMenu(property, className, SpecificationOntologyReader.NONE);
		else
		{
			submenus.get(parent.toString()).add(property);	//add this property to the submenu
			reader.changeMenu(property, className, parent.toString());	//and add this info to the ontology
		}
	}
	
	/**	Adds an item to the List
	 *	@param value Label
	 */
	public void addToList(String value)
	{
		listModel.addElement(value);	//remove any submenu comments
		if (map.containsKey(value))
			reader.removeMenu(map.get(value).property, className);
	}
	
	/**	Removes an item from the List
	 *	@param idx Index
	 */
	public void removeListItem(int idx)
	{
		if (idx >= 0)
			listModel.remove(idx);
	}
	
	/**	Creates a Menu for the class, the root of the tree
	 */
	private void createMenu(OntClass c, ClassTreeNode node)
	{
		if (node.isShaded())	//don't make menus for shaded nodes, as nothing can be changed for them
			return;
		classMenu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem("Change the class name");
		item1.addActionListener(new MenuActionListener(c, node));
		classMenu.add(item1);
	}
	
	/**	Creates a menu for a property node.
	 */
	private JPopupMenu createMenu(OntProperty prop, ClassTreeNode node, boolean specExists)
	{
		if (node.isShaded())	//don't make menus for shaded nodes, as nothing can be changed for them
			return null;
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item1 = new JMenuItem("Change the property name");
		item1.addActionListener(new MenuActionListener(prop, node, 1));
		menu.add(item1);
		
		JMenuItem item2 = null;
		if (specExists)
			item2 = new JMenuItem("Edit the linguistic specification");
		else
			item2 = new JMenuItem("Add a linguistic specification");
		item2.addActionListener(new MenuActionListener(prop, node, 2));
		menu.add(item2);
		
		JMenuItem item3 = new JMenuItem("Add a minimum cardinality constraint");
		item3.addActionListener(new MenuActionListener(prop, node, 3));
		menu.add(item3);
		
		JMenuItem item4 = new JMenuItem("Use this property to represent the class (like a name)");
		if (reader.hasComment(prop, SpecificationOntologyReader.PROPERNAME))	//if the class already is such a property
			item4 = new JMenuItem("Don't use this property to represent the class");
		item4.addActionListener(new MenuActionListener(prop, node, 4));
		menu.add(item4);
		
		return menu;
	}
	
	/**	Returns the nl-expression of a class
	 *
	 *	@param r OntClass
	 *	@param name NL-expression defined by ontology; usually not present, but if it is, no need to compute nl-expression
	 *	@param addDeterminer True to include a determiner in the NP
	 *	@return String
	 */
	private String getNL(OntClass r, String name, boolean addDeterminer)	
	{
		String phrase = reader.getNLExpression(r);
		if (!phrase.equals(name))	//if the nl-expression has already been defined, use it
			return phrase;
			
		StringBuffer nl = new StringBuffer(SGNode.normalise(name));
		System.out.println(nl);
		if (addDeterminer)
		{
			char c = nl.charAt(0);
			if (c == 'a' || c == 'e' || c == 'i'|| c == 'o')
				nl.insert(0, "an ");
			else
				nl.insert(0, "a ");
		}
		return nl.toString();
	}
	
	/**	Returns the nl-expression of a property
	 *	@param r OntProperty
	 *	@param name NL-expression defined by ontology; usually not present, but if it is, no need to compute nl-expression
	 *	@return String
	 */
	private String getNL(OntProperty p, String name)
	{
		String phrase = reader.getNLExpression(p);
		if (!phrase.equals(name))	//if the nl-expression has already been defined, use it
			return phrase;		
		return Anchor.getNLExpression(name);
	}
	
	/**	Contains information about a property node (i.e. its menu and OntProperty)
	 */
	private class PropertyInfo
	{
		public String name;
		public OntProperty property;
		public JPopupMenu menu;
		
		public PropertyInfo(String n, OntProperty prop, JPopupMenu m)
		{
			name = n;
			property = prop;
			menu = m;
		}
	}
	
	/**	Contains information about a submenu: its parent, its children
	 *	its label, and its menu (with which it can be removed).
	 */
	private class SubmenuInfo
	{
		public String name;
		public ClassTreeNode node;
		public java.util.List<OntProperty> items = new ArrayList<OntProperty>();
		public JPopupMenu removeMenu;
		
		public SubmenuInfo(String menu, ClassTreeNode n, JPopupMenu m)
		{
			name = menu;
			node = n;
			removeMenu = m;
		}
		
		public void add(OntProperty p)
		{
			items.add(p);
		}
		
		public void remove(OntProperty p)
		{
			items.remove(p);
		}
	}
	
	/**	Creates a 'remove this submenu' menu
	 */
	private JPopupMenu makeRemoveMenu(ClassTreeNode node)
	{
		JPopupMenu menu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Remove this submenu");
		item.addActionListener(new RemoveMenuActionListener(node));
		menu.add(item);
		return menu;
	}
	
	private class RemoveMenuActionListener implements ActionListener
	{
		private ClassTreeNode node;
		
		public RemoveMenuActionListener(ClassTreeNode node)
		{
			this.node = node;
		}
			
		public void actionPerformed(ActionEvent e)
		{
			ArrayList<ClassTreeNode> children = new ArrayList<ClassTreeNode>();
			for (int i = 0; i < node.getChildCount(); i++)
				children.add((ClassTreeNode) node.getChildAt(i));
			for (ClassTreeNode child : children)
			{
				treeModel.removeNodeFromParent(child);	//remove children from this menu
				treeModel.insertNodeInto(child, root, root.getChildCount());	//and add them back into root
			}
			submenus.remove(node.toString());	//remove this menu
			treeModel.removeNodeFromParent(node);	//now remove this node entirely
		}
	}
	
	/**	Action listener for property menu items
	 */
	private class MenuActionListener implements ActionListener
	{
		private OntProperty property;
		private OntClass c;
		private ClassTreeNode node;
		private int type = 0;
		
		public MenuActionListener(OntClass c, ClassTreeNode node)
		{
			this.c = c;
			this.node = node;
		}
		
		public MenuActionListener(OntProperty prop, ClassTreeNode node, int type)
		{
			this.type = type;
			this.node = node;
			property = prop;
		}
		
		public void actionPerformed(ActionEvent e)
		{
			System.out.println("clicked menu");
			switch (type)
			{
				case 0: changeName(c, node); break;
				case 1: changeName(property, node); break;
				case 2: editSpec(property, node); break;
				case 3: addConstraint(property, node); break;
				case 4: setNLProperty(property, node, (JMenuItem) e.getSource()); break;
			}
		}
	}

	/**	Stores the information that a certain property stores proper names in the
	 *	ontology. If that information was already there, remove it instead.
	 *	@param	property OntProperty
	 *	@param treeNode Node
	 *	@param item Menu item
	 */
	public void setNLProperty(OntProperty property, ClassTreeNode treeNode, JMenuItem item)
	{
		if (reader.hasComment(property, SpecificationOntologyReader.PROPERNAME))
		{	//if this property already was an nl-property, remove that attribute
			reader.removeComment(property, SpecificationOntologyReader.PROPERNAME);
			treeNode.setNLProp(false);
		//	if (!specExists(property))	//if there is no specification, remove the green tick
		//		treeNode.setCompleted(false);
			item.setText("Don't use this property to represent the class");
		}
		else
		{	//Set this attribute
			reader.editComment(property, SpecificationOntologyReader.PROPERNAME, "");
			treeNode.setNLProp(true);
		//	treeNode.setCompleted(true);	//property does not need a linguistic specification anymore
			item.setText("Use this property to represent the class (like a name)");
		}
	}

	/**	Changes the nl-representation of a class or property
	 *	@param resource OntResource
	 *	@param treeNode Node
	 */
	public void changeName(final OntResource resource, ClassTreeNode treeNode)
	{
		final String name = resource.getLocalName();
		String nl = null;
		if (resource instanceof OntProperty)
			nl = getNL((OntProperty)resource, name);
		else
			nl = getNL((OntClass) resource, name, false);
		
		StringBuffer sb = new StringBuffer("The phrase below is the natural language representation of the ");
		if (resource instanceof OntProperty)
			sb.append("property '");
		else
			sb.append("class '");
		sb.append(name);
		sb.append("', which is how it will appear in LIBER's pop-up menus.\nIf you wish, you can change this ");
		sb.append("representation here. Please enter the phrase you prefer.");
		
		String newName = JOptionPane.showInputDialog(parent, sb.toString(), nl);
		if ((newName != null) && (!newName.equals(nl)))
		{	
			if (map.containsKey(newName))
				JOptionPane.showMessageDialog(parent, "Duplicate name.",
   	   	    	"That phrase is already used to describe a different class or property.\n" +
   	   	    	"You cannot have two identical phrases, as the user would not be able to tell the difference.\n" +
   	   	    	"Please choose another phrase.", JOptionPane.WARNING_MESSAGE);
   	   	    else
			{	//store the new name as rdfs tag
				if(resource instanceof OntProperty)
				{
					PropertyInfo info = map.remove(treeNode.toString());	//change the name and update the map
					info.name = newName;
					map.put(newName, info);
				}
				reader.editComment(resource, SpecificationOntologyReader.PHRASE, newName);	//store the new name in the ontology
				treeNode.setUserObject(newName);		//change the name of the tree node!
			}
		}
	}
	
	/**	Opens the specification editing pane, where the user can create
	 *	a linguistic spec for the given property
	 *	@param prop Property
	 *	@param treeNode Node
	 */
	public void editSpec(OntProperty prop, ClassTreeNode treeNode)
	{
		try
		{
			specificationPane = new SpecificationPane(prop, c, reader, this, parent);
		}
		catch(CancelledException e)
		{}			
	}
	
	/**	Enables the user to add a minimum cardinality constraint.
	 *	@param prop Property
	 *	@param treeNode Node
	 */
	public void addConstraint(OntProperty prop, ClassTreeNode treeNode)
	{
		String minimum = JOptionPane.showInputDialog(parent, "Please enter the minimum cardinality constraint" +
			"\n(i.e. the minimum number of instantiations of property '" + prop.getLocalName() + "' that the class '" + className + "'must have).",
   	   	    	"Adding a minimum cardinality constraint", JOptionPane.INFORMATION_MESSAGE);
   	   	if ((minimum != null) && (minimum.length() > 0))
   	   	{
   	   		try
   	   		{
	   	   		int m = Integer.parseInt(minimum);
	   	   		reader.addCardinalityConstraint(c, m, prop);
	   	   		treeNode.setCardinal(m > 0);
	   	   		ClassTreeNode parent = (ClassTreeNode) treeNode.getParent();
	   	   		if (!parent.isRoot())	//cardinality nodes are not part of a submenu!
	   	   		{
	   	   			submenus.get(parent.toString()).remove(prop);	//remove from submenu
	   	   			treeModel.removeNodeFromParent(treeNode);			//remove from parent node
	   	   			reader.changeMenu(prop, className, SpecificationOntologyReader.NONE);		//change liber comment
	   	   			treeModel.insertNodeInto(treeNode, root, root.getChildCount());	//add to root
	   	   		}
	   	   	}
	   	   	catch (NumberFormatException e)
	   	   	{
	   	   		JOptionPane.showMessageDialog(parent, "You have to enter a number.", "Error processing " + minimum, JOptionPane.ERROR_MESSAGE);
	   	   	}
   	   	}
	}
	
	/**	Action listener
	 *	@param e ActionEvent
	 */
	public void actionPerformed(ActionEvent e)
 	{
 		if (e.getSource() == backBtn)
   	   		parent.getClassHierarchyPane();
   	   	else if (e.getSource() == submenuBtn)
   	   	{
   	   		String submenu = JOptionPane.showInputDialog(parent, "Please enter the name of your submenu.",
   	   	    	"Adding a submenu", JOptionPane.INFORMATION_MESSAGE);
   	   	    if ((submenu != null) && (submenu.length() > 0))
   	   	    {
   	   	    	if (submenus.containsKey(submenu))
   	   	    		JOptionPane.showMessageDialog(parent, "This submenu already exists. Please enter a different name.", 
   	   	    			"Duplicate submenu", JOptionPane.WARNING_MESSAGE);
   	   	    	else
   	   	    	{
   	   	    		ClassTreeNode node = new ClassTreeNode(submenu, false, false, true, false);
   	   	    		submenus.put(submenu, new SubmenuInfo(submenu, node, makeRemoveMenu(node)));
   	   	    		treeModel.insertNodeInto(node, root, root.getChildCount());
   	   	    	}
   	   	    }
   	   	}
   	   	else if (e.getSource() == exampleBtn)
	   	   new LiberExample(reader, className, parent.getDirectory()); 	
 	}
}