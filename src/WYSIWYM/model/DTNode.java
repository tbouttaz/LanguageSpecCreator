package WYSIWYM.model;

/***
 *	DTNode is a node in the Dependency Tree. It has alphabetised lists of the 
 *	incoming and outgoing nodes, and Strings holding syntactic, dependency & 
 *	morphological information.
 *	ID is an unique identifier; default setting is identical to label (id exists
 *	in case there are two objects (e.g. persons) with the same name).
 *
 * @author Feikje Hielkema
 * @version 1.0 2006/11/13
 */

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import WYSIWYM.transformer.DependencyTreeTransformer;
import javax.naming.NameAlreadyBoundException;

public class DTNode extends Node
{
	protected String deplbl, root, pronoun, sgID;
	protected Morph morph;
	private boolean leaf = false, person = false, elided = false;
	private int order;
	private int inserted = 0;
	private boolean useAsPronoun = false;
	private boolean flagged = false;
	
	public static final int NOT_INSERTED = 0;
	public static final int SOURCE_INSERTED = 1;
	public static final int TARGET_INSERTED = 2;
	public static final int PREDICATE_INSERTED = 3;
	
	/**	Constructs DTNode with given syntactic category, dependency label, word root
	 *	and morphology
	 *	@param syncat	syntactic category
	 *	@param dep	dependency label
	 *	@param w	word root
	 *	@param m	morphology
	 */
    public DTNode(String syncat, String dep, String w, Morph m) 
    {
    	super(syncat);
    	deplbl = dep;
    	root = w;
    	morph = m;
    	if (root != null)
    		leaf = true;
    }
    
    /**	Constructs DTNode with given syntactic category, dependency label and ID
	 *	@param syncat	syntactic category
	 *	@param dep	dependency label
	 *	@param idx	ID
	 */
    public DTNode(String syncat, String dep, String idx) 
    {
    	super(syncat);
    	deplbl = dep;
    	setID(idx);
	}
	
	/**	Constructs DTNode from given node
	 *	@param n DTNode
	 */
	public DTNode(DTNode n)
	{
		super(n.getLabel());
		deplbl = n.getDeplbl();
		root = n.getRoot();
		morph = n.getMorph();
		leaf = n.isLeaf();
		setInserted(n.getInserted());
	}
	
	/**	Constructs a copy of the given node, and adds it with all its edges to the 
	 *	DependencyTree
	 *
	 *	@param node DTNode to copy
	 *	@param existing HashMap with nodes already copied
	 *	@param dt DependencyTreeTransformer
	 *	@throws NameAlreadyBoundException very unlikely
	 *	@return copy of DTNode
	 */
	public static DTNode copyNode(DTNode node, Map<String,DTNode> existing, DependencyTreeTransformer dt) throws NameAlreadyBoundException
	{
		DTNode result = new DTNode(node.getLabel(), node.getDeplbl(), node.getRoot(), null);
		if (node.getMorph() != null)
			result.setMorph(new Morph(node.getMorph()));
		result.setID(node.getID());
		result.setSGID(node.getSGID());
		result.setOrder(node.getOrder());
		result.setPronoun(node.getPronoun());
		result.setUseAsPronoun(node.useAsPronoun());
		result.setPerson(node.isPerson());
		result.setElided(node.isElided());
		result.setInserted(node.getInserted());
		result.setAnchor(node.getAnchor());
		existing.put(node.getID(), result);
		
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			DTEdge edge = (DTEdge) it.next();
			DTEdge.copyDTEdge(edge, existing, dt, result);
		}
		return result;
	}
		
	/**	Checks whether this node is a terminal node
	 *	@return boolean
	 */
	public boolean isLeaf()
	{
	//	return (!hasOutgoingEdge());
		return leaf;
	}
	
	/**	Sets whether this node is or should be considered a terminal node
	 *	@param l boolean
	 */
	public void setLeaf(boolean l)
	{
		leaf = l;
	}
	
	/**	Checks whether this node has been elided, and should
	 *	not appear in the surface form.
	 *	@return true if this node has been elided.
	 */
	public boolean isElided()
	{
		if (elided)
			return true;
		
		Iterator it = getParents();
		if (!it.hasNext())
			return false;
		if (((DTNode) it.next()).isElided())
			return true;	//if its parent is elided, this must be too!
		return false;
	}
	
	/**	Sets whether this node must not appear in the surface form.
	 *	@param e true if this node must be elided, and not appear in the surface form.
	 */
	public void setElided(boolean e)
	{
		elided = e;
	}
	
	/**	Helper method to flag some node you want to retrieve later
	 *	@param f true if the node should be flagged
	 */
	public void setFlagged(boolean f)
	{
		flagged = true;
	}
	
	/**	Helper method, checks if some node you wanted to retrieve later has been flagged
	 *	@return true if the node has been flagged
	 */
	public boolean isFlagged()
	{
		return flagged;
	}
	
	/**	Returns whether this node is inserted (as source or target) into a DependencyTree.
	 *	0 if not inserted, 1 if inserted as source, 2 if inserted as target
	 *	@return int
	 */
	public int getInserted()
	{
		return inserted;
	}
	
	/**	Sets whether this node is inserted as source or target
	 *	0 if not inserted, 1 if inserted as source, 2 if inserted as target
	 *	@param i int
	 */
	public void setInserted(int i)
	{
		inserted = i;
	}
	
	/**	Sets the pronoun realisation for this node
	 *	@param p pronoun
	 */
	public void setPronoun(String p)
	{
		pronoun = p;
	}
	
	/**	Returns the pronoun realisation for this node
	 *	@return String
	 */
	public String getPronoun()
	{
		return pronoun;
	}
	
	/**	Returns whether this node should be realised as a pronoun
	 *	@return boolean
	 */
	public boolean useAsPronoun()
	{
		return useAsPronoun;
	}
	
	/**	Sets whether this node should be realised as a pronoun
	 *	@param u true if this should be presented by a pronoun
	 */
	public void setUseAsPronoun(boolean u)
	{
		useAsPronoun = u;
	}
	
	/**	Returns the dependency label
	 *	@return String
	 */
	public String getDeplbl()
	{
		return deplbl;
	}
	
	/**	Sets the dependency label
	 *	@param d dependency label
	 */
	public void setDeplbl(String d)
	{
		deplbl = d;
	}
	
	/**	Returns the word root
	 *	@return String
	 */
	public String getRoot()
	{
		return root;
	}
	
	/**	Returns the morphology
	 *	@return Morph
	 */
	public Morph getMorph()
	{
		return morph;
	}
	
	/**	Sets the word root
	 *	@param r String root
	 */
	public void setRoot(String r)
	{
		root = r;
		if (root == null)
			leaf = false;
		else
			leaf = true;
	}
	
	/** Sets the Unique ID assigned by the Semantic Graph
	 *	@param id SG ID
	 */
	public void setSGID(String id)
	{
		sgID = id;
	}
	
	/** Gets the Unique ID assigned by the Semantic Graph
	 *	@return String
	 */
	public String getSGID()
	{
		return sgID;
	}
	
	/**	Sets the Morphology
	 *	@param m Morph
	 */
	public void setMorph(Morph m)
	{
		morph = m;
	}
	
	/**	Sets the rank order in the tree (unused)
	 *	@param nr rank order
	 *	@deprecated
	 */
	public void setOrder(int nr)
	{
		order = nr;
	}
	
	/**	Returns the rank order in the tree (unused)
	 *	@return int
	 *	@deprecated
	 */
	public int getOrder()
	{
		return order;
	}
	
	/**	Sets whether this node represents an object of type Person
	 *	@param p true if this represents a Person object
	 */
	public void setPerson(boolean p)
	{
		person = p;
	}
	
	/**	Checks whether this node represents an object of type Person
	 *	@return true if this represents a Person object
	 */
	public boolean isPerson()
	{
		return person;
	}
	
	/**	Checks whether this node has some ancestor with the given
	 *	Dependency Label
	 *	@param dep Dependency Label
	 *	@return true if some ancestor has this dependency label
	 */
	public boolean childOf(String dep)
	{
		DTNode node = this;
		
	 	while (true)
    	{
    		Iterator it = node.getParents();
    		if (!it.hasNext())
    			return false;
    			
    		node = (DTNode) it.next();
    		if (node.getDeplbl().equals(dep))
    			return true;
    	}
	}
	
	/**	Retrieves the child node with the given
	 *	Dependency Label
	 *	@param dep Dependency Label
	 *	@return DTNode child
	 */
	public DTNode getDepChild(String dep)
	{
		for (Iterator it = getOutgoingEdges(); it.hasNext(); )
		{
			DTEdge edge = (DTEdge) it.next();
			if (edge.getLabel().equals(dep))
				return edge.getTarget();
		}
		return null;
	}
	
	/**	Returns the target of the outgoing edge with the given dependency label and order
	 *	 nr (if it exists).
	 *	@param dep Dependency label
	 *	@param order rank order number
	 *	@return DTNode, or null if no node met the requirements
	 */
	public DTNode getOrderedDepChild(String dep, int order)
	{
		List<Edge> list = getOutgoingEdges(dep);
		for (int i = 0; i < list.size(); i++)
			if (((DTEdge) list.get(i)).getOrder() == order)
				return (DTNode) list.get(i).getTarget();
		return null;
	}
	
	/**	Returns the targets of all outgoing edges with the given dependency label.
	 *	@param dep Dependency label
	 *	@return List<DTNode>
	 */
	public List<DTNode> getDepChildren(String dep)
	{
		List<DTNode> result = new ArrayList<DTNode>();
		for (Iterator it = getOutgoingEdges(); it.hasNext(); )
		{
			DTEdge edge = (DTEdge) it.next();
			if (edge.getLabel().equals(dep))
				result.add(edge.getTarget());
		}
		return result;
	}
	
	/**	Checks whether this node is a descendant of the given node.
	 *	@param parent DTNode
	 *	@return true if parent is an ancestor of this node.
	 */
	public boolean childOf(DTNode parent)
	{
		DTNode node = this;
		
		while (true)
		{
			Iterator it = node.getParents();
			if (!it.hasNext())
				return false;
			
			node = (DTNode) it.next();
			if (node.equals(parent))
				return true;
		}
	}
	
	/**	Returns the ancestor with the given dependency label
	 *	@param dep Dependency label
	 *	@return DTNode, or null if there is no such ancestor
	 */
	public DTNode getDepParent(String dep)
	{
		DTNode node = this;
		
	 	while (true)
    	{
    		Iterator it = node.getParents();
    		if (!it.hasNext())
    			return null;
    			
    		node = (DTNode) it.next();
    		if (node.getDeplbl().equals(dep))
    			return node;
    	}
	}
	
	/**	Returns the parent node. If there is more than one parent node
	 * 	(which in the DependencyTree there shouldn't really be), it returns
	 *	a random choice.
	 *	@return DTNode
	 */
	public DTNode getParent()
	{
		return (DTNode) super.getParent();
	}
}