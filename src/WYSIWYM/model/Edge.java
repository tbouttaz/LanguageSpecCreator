package WYSIWYM.model;

import javax.naming.NameAlreadyBoundException;

/**	Edge is an interface for tree edges. 
 *	@author Feikje Hielkema
 *	@version 1.1 20-10-2006
 *	@version 1.1 20-09-2007
 */
public abstract class Edge implements TreeComponent
{
	private String label, id;
	private Node source, target;

	
	/**	Constructs Edge with given label
	 *	@param lbl Label
	 */
	public Edge(String lbl)
	{
		label = lbl;
	}
	
	/**	Constructs Edge with given label, source and target
	 *	@param lbl	label
	 *	@param s 	source node
	 *	@param t	target node
	 *	@throws NameAlreadyBoundException if source or target already has this edge
	 */
	public Edge(String lbl, Node s, Node t) throws NameAlreadyBoundException
	{
		label = lbl;
		id = null;
		setSource(s);
		setTarget(t);
	}
	
	/**	Constructs Edge with given label, ID, source and target
	 *	@param lbl	label
	 *	@param id	ID
	 *	@param s	source node
	 *	@param t	target node
	 *	@throws NameAlreadyBoundException if source or target already has this edge
	 */
	public Edge(String lbl, String id, Node s, Node t)throws NameAlreadyBoundException
	{
		label = lbl;
		this.id = id;
		setSource(s);
		setTarget(t);
	}
	
	/** Sets the id of this edge. Also makes sure that the edge is stored in the
	 *	node's hashmap with it's id instead of it's label (which may cause problems
	 *	when a node has 2 edges with the same name
	 *	
	 *	@param i ID
	 */
	public void setID(String i)
	{	
		try
		{
			if (source != null)
				source.removeOutgoingEdge(this);
			if (target != null)
				target.removeIncomingEdge(this);
			
			id = i;
			
			if (source != null)
				source.addOutgoingEdge(this);
			if (target != null)
				target.addIncomingEdge(this);	
		}
		catch (NameAlreadyBoundException e)	
		{
			e.printStackTrace();
		}
	}
	/**	Sets the label
	 *	@param str label
	 */
	public void setLabel(String str)
	{
		label = str;
	}
	
	/**	Sets the source to s. To remove the source node, call this with argument null.
	 *	@param s source node
	 *	@throws NameAlreadyBoundException if source already has this edge
	 */
	public void setSource(Node s) throws NameAlreadyBoundException
	{	
		s.addOutgoingEdge(this);
		source = s;
	}
	
	/**	Sets the target to t. To remove the source node, call this with argument null.
	 *	@param t	target node
	 *	@throws NameAlreadyBoundException if target already has this edge
	 */
	public void setTarget(Node t) throws NameAlreadyBoundException
	{
		t.addIncomingEdge(this);
		target = t;
	}
	
	/**	Returns the label
	 *	@return String
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**	Returns the source
	 *	@return Node
	 */
	public Node getSource()
	{
		return source;
	}

	/**	Returns the target
	 *	@return Node
	 */	
	public Node getTarget()
	{
		return target;
	}
	
	/**	Returns the ID
	 *	@return String
	 */
	public String getID()
	{
		return id;
	}
	
	/**	Overload; shows that this is an edge, not a node
	 *	@see TreeComponent#isEdge()
	 *	@return true
	 */
	public boolean isEdge()
	{
		return true;
	}
	
	/**	Overload; shows that this is an edge, not a node
	 *	@see TreeComponent#isNode()
	 *	@return false
	 */
	public boolean isNode()
	{
		return false;
	}
}