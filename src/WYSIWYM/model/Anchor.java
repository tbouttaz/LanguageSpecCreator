package WYSIWYM.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.BadAnchorException;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;

/**	Anchor represents the expansion points in the feedback text. It holds a node
 *	in the semantic graph and a list of compulsory and optional properties. If any
 *	properties are compulsory (i.e. minimum cardinality > 1) the anchor is red.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 26-10-2006
 *
 *	@version 1.2 27-02-2008
 */
public class Anchor
{
	private Node node;	//The node (usually SGNode) associated with this anchor
	private List<OntProperty> optional = new ArrayList<OntProperty>();	//List of properties the node can have
	private List<OntProperty> compulsory = new ArrayList<OntProperty>();		//List of properties the node should have
	private boolean redAnchor = false;				//true if there are properties the node should have but doesn't
	private OntologyReader reader;
	
	private List<String> op, opNL, optionalPropType;	//List of Strings with names, NL-representations and types of optional properties
	private String[] comp, compNL, removable, removableNL;	//String arrays with names and NL-represenations of compulsory and removable properties
	private Map<String, List<String>> removableMap;			//HashMap with values that can be removed, sorted by property
	
	private boolean query = false;		//true if this is an anchor in a query graph
	protected String id, uri;				//unique ID, URI where resource can be found
	
	/**	Default constructor
	 */
	public Anchor()
	{}
	
	/**	Create a new anchor for the given node, specifying which relations it must
	 *	or can have. Relations whose maximum cardinality constraint is already fullfilled
	 *	are not added to the menu.
	 *
	 *	@param r	OntologyReader to extract information from ontology
	 *	@param c	OntClass corresponding to this anchor
	 *	@param n	SGNode for which this anchor is created
	 *	@param userID UserID
	 *	@throws BadAnchorException if the given SGNode does not need an anchor
	 */
	public Anchor(OntologyReader r, OntClass c, SGNode n, String userID) throws BadAnchorException
	{
		this(r, c, n, false, userID);
	}	
	
	/**	Create a new anchor for the given node, specifying which relations it must
	 *	or can have. Relations whose maximum cardinality constraint is already fullfilled
	 *	are not added to the menu.
	 *
	 *	@param r	OntologyReader to extract information from ontology
	 *	@param c	OntClass corresponding to this anchor
	 *	@param n	SGNode for which this anchor is created
	 *	@param query  true if this is an anchor for a node in the QueryGraph
	 *	@param userID UserID
	 *	@throws BadAnchorException if the given SGNode does not need an anchor
	 */
	public Anchor(OntologyReader r, OntClass c, SGNode n, boolean query, String userID) throws BadAnchorException
	{
		node = n;
		this.query = query;
		if (node.getAnchor() != null)
			id = node.getAnchor().getID();
		else
			id = UUID.randomUUID().toString();	
		reader = r;	
		if (n instanceof SGDateNode)
			throw new BadAnchorException("A date does not need an anchor");
		
		node.setAnchor(this);		//tell the node it has an anchor
		setURI(userID);
		if (addShowOrHideOption() == SGNode.NEW)
			init(c, query);	//only make extensive anchors for newly created nodes
	}
	
	/**	Constructs an anchor by copying all information from the given anchor
	 *
	 *	@param	a Another anchor
	 *	@throws BadAnchorException if the given anchor is null
	 */
	public Anchor(Anchor a) throws BadAnchorException
	{
		if (a == null)
			throw new BadAnchorException("Failed attempt to make an anchor out of a null object");
		merge(a);
		query = a.isQuery();
		redAnchor = a.isRed();
		//datatype = a.isDataType();
		reader = a.getReader();
		id = a.getID();
		uri = a.getURI();
		
		if (a.getNode() != null)
		{
			node = a.getNode();
			node.setAnchor(this);
		}
	}
	
	/**	Constructs an anchor for the given node by copying all information from
	 *	the given anchor
	 *
	 *	@param a	Another anchor
	 *	@param n	The node to which the anchor must be attached
	 *	@throws BadAnchorException if the given anchor is null
	 */	
	public Anchor(Anchor a, SGNode n) throws BadAnchorException
	{
		if (a == null)
			throw new BadAnchorException("Failed attempt to make an anchor out of a null object");
		
		reader = a.getReader();
		node = n;
		query = a.isQuery();
		compulsory.addAll(a.getCompulsory());
		optional.addAll(a.getOptional());
		sort(query);
		redAnchor = a.isRed();
		reader = a.getReader();
		id = a.getID();
		uri = a.getURI();
		n.setAnchor(this);
	}

	/**	Checks whether the node is part of a query graph
	 *	@return	true if the node is part of a query graph
	 */
	public boolean isQuery()
	{
		return query;
	}
	
	/**	Returns the unique ID of this anchor
	 *	@return Anchor ID
	 */
	public String getID()
	{
		return id;
	}
	
	/**	Initialises the anchor, finding the node's compulsory and optional 
	 *	properties and their NL representations and ordering it all in lists and
	 *	maps, using the submenus.
	 *
	 *	@param c	OntClass corresponding to this anchor
	 *	@param query  true if this is an anchor for a node in the QueryGraph
	 *	@throws BadAnchorException if the given SGNode does not need an anchor
	 */
	public void init(OntClass c, boolean query) throws BadAnchorException
	{
		Map<String, Integer[]> map = reader.getCardinalities(c.getLocalName());
		List<OntProperty> list = reader.getDomainProperties(c.getLocalName());		
		for (int i = 0; i < list.size(); i++)
		{
			OntProperty p = list.get(i);			
			String name = p.getLocalName();
			int nr = node.getOutgoingEdges(name).size();	//number of times the node already has this property
			String inverse = reader.getInverse(name);//, n.getLabel(), null);
			if (inverse != null)
				nr += node.getIncomingEdges(inverse).size();	//plus the number of times the inverse has this node as range
			
			if (map.containsKey(name))		
			{
				int min = map.get(name)[0];	//if the minimum cardinality is not satisfied, add to 'compulsory'
				int max = map.get(name)[1];	
				if (query)
					optional.add(p);		//for the query, cardinality constraints do not matter
				else if ((min != 0) && (min > nr))	//if the minimum cardinality > 0, add the property to compulsory
					compulsory.add(p);
				else if ((max == 0) || (max > nr))	//if the maximum cardinality will not be violated, add to optional
					optional.add(p);
			}
			else
				optional.add(p);					//no cardinality constraints, so add to optional
		}
		
		if (!compulsory.isEmpty())				//if there are compulsory properties, this is a red anchor
			redAnchor = true;
		else if (optional.isEmpty())	//compulsory and optional both empty - means this is not an anchor
			throw new BadAnchorException("Node " + node.getLabel() + " is not an anchor anymore; all its relations are specified");
		
		sort(query);		
	}
	
	/**	If the node in this anchor has a URI where its resource can be found, 
	 *	this method returns	that URI, otherwise null.
	 *	@return	String URI of resource represented by the node
	 */
	public String getURI()
	{
		return uri;
	}
	
	/**	If this is a resource that is available, and this user has access to it,
	 *	this method sets the URI.
	 */
	private void setURI(String userID)
	{
		if ((userID == null) || (!(node instanceof SGNode)))
			return;	//only do this for SGNodes
		
		SGNode n = (SGNode) node;
		List<Edge> list = n.getOutgoingEdges("HasURI");
		if (list.size() > 0)
		{
			SGStringNode target = (SGStringNode) list.get(0).getTarget();
			uri = (String) target.getValue();
		}
		if (uri == null)
			return;	//if there is no URI, we are done
		
		list = n.getOutgoingEdges("AccessConditions");
		if (list.size() == 0)
		{
			System.out.println("Could not find access rights to node " + n.getNLLabel(reader));
			uri = null;
			return;		//assume that access must be public, if unspecified
		}
		String access = (String) ((SGStringNode) list.get(0).getTarget()).getValue();
		if (access.equals("public"))
			return;		//everyone has access to a public resource
			
		SGNode depositor = n.getPropertyTarget("DepositedBy", reader);
		if ((depositor != null) && userID.equals(depositor.getUniqueID()))
			return;		//depositor has access to all his resources
		
		if (access.equals("restricted to project members")) //access restricted to project members
		{	//find out which project the resource is associated with
			SGNode project = n.getPropertyTarget("ProducedInProject", reader);
			if (project != null)
			{	//find out if this user is a member
				List<String> subprops = reader.getSubProperties("HasMember");
				for (Iterator it = project.getOutgoingEdges(); it.hasNext(); )
				{
					SGEdge edge = (SGEdge) it.next();
					if (subprops.contains(edge.getLabel()) && userID.equals(edge.getTarget().getUniqueID()))
					{
						System.out.println("FOUND MEMBER OF PROJECT " + project.getNLLabel(reader) + ": " + edge.getTarget().getNLLabel(reader));
						return;
					}
				}
				
				subprops = reader.getSubProperties("MemberOf");
				for (Iterator it = project.getIncomingEdges(); it.hasNext(); )
				{
					SGEdge edge = (SGEdge) it.next();
					if (subprops.contains(edge.getLabel()) && userID.equals(edge.getSource().getUniqueID()))
					{
						System.out.println("FOUND MEMBER OF PROJECT " + project.getNLLabel(reader) + ": " + edge.getSource().getNLLabel(reader));
						return;
					}
				}
			}
		}
		else if (access.equals("restricted to authors"))
		{
			for (Edge edge : n.getOutgoingEdges("HasAuthor"))
			{
				if (userID.equals(((SGNode) edge.getTarget()).getUniqueID()))
					return;
			}
			for (Edge edge : n.getIncomingEdges("AuthorOf"))
			{
				if (userID.equals(((SGNode) edge.getSource()).getUniqueID()))
					return;
			}
		}
		
		uri = null;	//in all other cases, the user should not have access to the resource 
	}
	
	/**	Checks if this node's description should be shown in the feedback text.
	 *	@return	int Visibility status
	 *	@see SGNode#SHOW
	 *	@see SGNode#HIDE
	 *	@see SGNode#NEW
	 *	@see SGNode#INCOMPLETE
	 */
	public int addShowOrHideOption()
	{	//if the node is from the database (not created in this session), add an option to show/hide all information about it
		if (node instanceof SGNode)
			return ((SGNode)node).mustRealise();
		return 0;
	}
	
	/**	Sorts the properties in compulsory and optional by submenu and alphabetical order.
	 *	Also specifies which properties can be removed.
	 */
	private void sort(boolean query)
	{
		comp = new String[compulsory.size()];
		compNL = new String[compulsory.size()];
		Map<String, String> map = new HashMap<String,String>();
		for (int i = 0; i < compulsory.size(); i++)
		{
			Object o = compulsory.get(i);
			if (o instanceof String)
			{
				comp[i] = (String) o;
				compNL[i] = getNLExpression(comp[i]);
			}
			else if (o instanceof OntResource)
			{
				OntResource r = (OntResource) o;
				comp[i] = r.getLocalName();
				compNL[i] = getNLExpression(r);
			}
			map.put(compNL[i], comp[i]);
		}
		Arrays.sort(compNL);	//sort the nl-expressions, then make sure the original names are in the same sequence
		for (int i = 0; i < compNL.length; i++)
			comp[i] = map.get(compNL[i]);
		
		Map<String, List<OntProperty>> menuMap = getMenus();
		String[] menuOrder = (String[]) menuMap.keySet().toArray(new String[0]);
		Arrays.sort(menuOrder);	//sort the menu's alphabetically
		op = new ArrayList<String>();
		opNL = new ArrayList<String>();
		optionalPropType = new ArrayList<String>();
		
		for (int i = 0; i < menuOrder.length; i++)
		{
			if (!menuMap.containsKey(menuOrder[i]))
				continue;
		
			List<OntProperty> submenu = menuMap.get(menuOrder[i]);
			map = new HashMap<String,String>();
			String[] nlexpr = new String[submenu.size()];
			for (int j = 0; j < submenu.size(); j++)
			{
				String name = submenu.get(j).getLocalName();
				nlexpr[j] = getNLExpression(submenu.get(j));
				map.put(nlexpr[j], name);
			}
			Arrays.sort(nlexpr);
			for (int j = 0; j < nlexpr.length; j++)
			{
				opNL.add(nlexpr[j]);
				op.add(map.get(nlexpr[j]));
				optionalPropType.add(menuOrder[i]);
			}
		}

		//collect all edges the node has (and their values) to offer them as 'undo' options
		removableMap = new HashMap<String, List<String>>();
		map = new HashMap<String, String>();
		
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			if (!edge.isRemovable())
				continue;
		
			String property = edge.getLabel();
			List<String> values = new ArrayList<String>();
			if (edge.getTarget() instanceof QueryValueNode)
				values = ((QueryValueNode)edge.getTarget()).getChoiceLabels(reader);
			else
				values.add(edge.getTarget().getChoiceLabel(query, reader));
			
			if (removableMap.containsKey(property))			
				removableMap.get(property).addAll(values);
			else
			{
				map.put(getNLExpression(property), property);		
				removableMap.put(property, values);
			}	
		}

		removableNL = new String[map.size()];
		int cntr = 0;
		for (Iterator it = map.keySet().iterator(); it.hasNext(); )
		{
			removableNL[cntr] = (String) it.next();	
			cntr++;
		}	
		Arrays.sort(removableNL);		
		removable = new String[removableNL.length];
		for (int i = 0; i < removableNL.length; i++)
			removable[i] = map.get(removableNL[i]);
	}
	
	/**	Divides the list of optional properties into submenus.
	 */
	private Map<String, List<OntProperty>> getMenus()
	{
		Map<String, List<OntProperty>> result = new HashMap<String, List<OntProperty>>();    		
    	for (int i = 0; i < optional.size(); i++)
    	{
    		String menu = reader.getSubmenu(optional.get(i), node.getLabel());
    		if (menu == null)
    			continue;
    		List<OntProperty> list = new ArrayList();
    		if (result.containsKey(menu))
    			list = result.get(menu);
    		list.add(optional.get(i));   
    		result.put(menu, list); 			
    	}
    	return result;
	}
	
	/**	Checks whether the given character is a vowel
	 *	@param a	char 
	 *	@return boolean True if the given character is a vowel
	 */
	public static boolean isVowel(char a)
	{
		char c = Character.toLowerCase(a);
		if ((c == 'a') || (c == 'u') || (c == 'o') || (c == 'i') || (c == 'e'))
			return true;
		return false;
	}
	
	/**	Returns the natural language representation of the given resource
	 *	@param	r Ontology resource
	 *	@return String NL-representation
	 */
	public String getNLExpression(OntResource r)
	{
		String str = reader.getNLExpression(r);
		if (str != null)	//if the nl-expr. has been defined in the ontology, use that
			return str;
		return getNLExpression(r.getLocalName());
	}
	
	/**	Returns the nl expression of a property name
	 *	@param	s property name
	 *	@return String NL-representation
	 */
	public static String getNLExpression(String s)
	{
		StringBuffer result = new StringBuffer(s);
		if (s.length() < 3)
			return s;		//LET'S ASSUME FOR NOW THAT ONLY HAPPENS WITH ABBREVIATIONS, WHICH SHOULD BE CAPITALISED ANYWAY
		String sub = result.substring(result.length() - 2);		//get the last two characters
		if (sub.equals("Of") || (result.indexOf("For") == (result.length() - 3)))
		{
			if (!result.substring(0, 2).toLowerCase().equals("is"))
				result.insert(0, "Is ");
		}
		else if ((sub.equals("By") || sub.equals("As")) && !result.substring(0,2).toLowerCase().equals("is"))
			result.insert(0, "Is ");
		
		for (int i = 1; i < result.length(); i++)
		{	//replace all capitals with lowercase letters, and add spaces if needed
			char c = result.charAt(i);
			if (Character.isUpperCase(c))
			{
				result.setCharAt(i, Character.toLowerCase(c));
				if (!(result.charAt(i - 1) == ' ') && !(result.charAt(i - 1) == '_'))
					result.insert(i, ' ');		//insert a space				
			}
		}
		result.setCharAt(0, Character.toUpperCase(result.charAt(0)));	//set first character to upper case
		return result.toString().replaceAll("_", " ");				//replace all underscores with spaces
	}
	
	/**	Checks whether adding p to this node would violate cardinality
	 *	constraints
	 *	@param p	OntProperty
	 *	@return true if adding the property would violate a cardinality constraint
	 */
	public boolean violatesCardinality(OntProperty p)
	{
		if (p.isInverseFunctionalProperty())
		{
			if (node.getIncomingEdges(p.getLocalName()).size() > 0)
				return true;
		}
		OntProperty inverse = reader.getInverse(p);
		if (inverse == null)
			return false;		//if the inverse property does not exist there are no cardinal constraints on it...
		if (optional.contains(inverse) || compulsory.contains(inverse))
			return false;
		if (addShowOrHideOption() == SGNode.INCOMPLETE)
			return false;
		return true;
	}
	
	/**	Returns whether this anchor is red (i.e. has compulsory items)
	 *	@return true if this anchor is red
	 */
	public boolean isRed()
	{
		return redAnchor;
	}
	
	/**	Returns the OntologyReaderor
	 *	@return OntologyReader
	 */
	public OntologyReader getReader()
	{
		return reader;
	}
	
	/**	Returns an iterator over the compulsory properties
	 *	@return Iterator over OntProperties
	 */
	public Iterator getCompulsoryProperties()
	{
		return compulsory.iterator();
	}
	
	/**	Returns an iterator over the optional properties
	 *	@return Iterator over OntProperties
	 */
	public Iterator getOptionalProperties()
	{
		return optional.iterator();
	}
	
	/**	Returns a list of compulsory properties
	 *	@return List with OntProperties
	 */
	public List getCompulsory()
	{
		return compulsory;
	}
	
	/**	Returns a list of optional properties
	 *	@return List with OntProperties
	 */
	public List getOptional()
	{
		return optional;
	}
	
	/**	Merges the information (i.e. compulsory and optional properties) in this 
	 *	anchor with the given one
	 *	@param a	Anchor
	 *	@return Anchor
	 */
	public Anchor merge(Anchor a)
	{
		if (a != null)
		{	
			for (Iterator it = a.getCompulsoryProperties(); it.hasNext(); )
			{
				OntProperty p = (OntProperty) it.next();
				if (!compulsory.contains(p))
					compulsory.add(p);
				redAnchor = true;
			}
			for (Iterator it = a.getOptionalProperties(); it.hasNext(); )
			{
				OntProperty o = (OntProperty) it.next();
				if (!optional.contains(o))
					optional.add(o);
			}
		}
		sort(false);		//DOES THIS EVER GET USED BY QUERY???
		return this;
	}
	
	/**	Returns a String array of labels of the compulsory properties
	 *	@return String[]
	 */
	public String[] getCompulsoryArray()
	{
		if (comp == null)
			return new String[0];
		return comp;
	}

	/**	Returns a String array of labels of the optional properties
	 *	@return String[]
	 */	
	public String[] getOptionalArray()
	{
		String[] str = new String[0];
		if (op == null)
			return str;
		return (String[]) op.toArray(str);
	}
	
	/**	Returns a String array of NL-labels of the compulsory properties
	 *	@return String[]
	 */
	public String[] getCompulsoryNL()
	{
		if (compNL == null)
			return new String[0];
		return compNL;
	}

	/**	Returns a String array of NL-labels of the optional properties
	 *	@return String[]
	 */	
	public String[] getOptionalNL()
	{
		String[] str = new String[0];
		if (opNL == null)
			return str;
		return (String[]) opNL.toArray(str);
	}

	/**	Returns an String array of labels of the optional property types
	 *	@return String[]
	 */
	public String[] getOptionalPropType()
	{
		String[] str = new String[0];
		if (optionalPropType == null)
			return str;
		return (String[]) optionalPropType.toArray(str);
	}
	
	/**	Returns the node this anchor is attached to
	 *	@return Node
	 */
	public Node getNode()
	{
		return node;
	}
	
	/**	Sets the node to which this anchor is attached.
	 *	@param	n Node
	 */
	public void setNode(Node n)
	{
		node = n;
	}
	
	/**	Returns a String array of removable property names
	 *	@return String[]
	 */
	public String[] getRemovable()
	{
		if (removable == null)
			return new String[0];
		return removable;
	}
	
	/**	Returns a String array of removable property nl-representations
	 *	@return String[]
	 */
	public String[] getRemovableNL()
	{
		if (removableNL == null)
			return new String[0];
		return removableNL;
	}
	
	/**	Returns the values that have been added for this property, and can be
	 *	removed.
	 *
	 *	@param property	Property name
	 *	@return String[] with values 
	 */
	public String[] getValues(String property)
	{
		if (!removableMap.containsKey(property))
			return new String[0];
			
		List<String> list = removableMap.get(property);
		String[] array = new String[0];
		return (String[]) list.toArray(array);
	}
}