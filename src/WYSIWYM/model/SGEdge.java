package WYSIWYM.model;

import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.transformer.SemanticGraphTransformer;

/**	SGEdge is an edge in the Semantic Graph. 
 *	@author Feikje Hielkema
 *	@version 1.0 24-10-2006
 *
 *	@version 1.1 20-09-2007
 *
 *	@version 1.2 08-02-08
 */
public class SGEdge extends Edge implements Comparable
{
	private int sequenceNr = 0;		//represents the order in which edges are attached to enable undo
	private int realiseNr = -1;		//represents the order in which identical edges must be added
	private int showInList = -1;	
	private int mustRealise = 0;
	private boolean flash = true, removable = true;	//, mustRealise = true;	//new additions should flash (or be marked) when they appear in the feedback text
	
	/** This edge is part of a summation list, and must be shown */
	public static int SHOWN_LIST = 1;
	/** This edge is part of a summation list, and must be hidden */
	public static int HIDDEN_LIST = 0;
	
	/**	Constructs SGEdge with given label	
	 *	@param lbl Label
	 */							
	public SGEdge(String lbl)		
	{
		super(lbl);
		if (lbl.equals("ID"))
			removable = false;
	}
	
	/**	Constructs SGEdge with given label, source and target node
	 *
	 *	@param lbl	label
	 *	@param s	source node
	 *	@param t	target node
	 *	@throws NameAlreadyBoundException when this edge is already in the source
	 *	or target node
	 */
	public SGEdge(String lbl, SGNode s, SGNode t) throws NameAlreadyBoundException
	{
		super(lbl, s, t);
		if (lbl.equals("ID"))
			removable = false;
	}
	
	/**	Constructs a copy of the given edge
	 *	@param edge SGEdge to copy
	 */	
	public SGEdge(SGEdge edge)
	{
		this(new String(edge.getLabel()));
		setID(edge.getID());
		setSequenceNr(edge.getSequenceNr());
		setRealiseNr(edge.getRealiseNr());
		setRemovable(edge.isRemovable());
		setRealise(edge.mustRealise());
	}
	
	/**	Constructs a copy of the given edge, and adds it with copies of the source and target
	 *	to the Semantic Graph
	 *
	 *	@param oldEdge SGEdge to copy
	 *	@param existing HashMap with nodes already copied (potential source and targets)
	 *	@param sg Semantic Graph Transformer
	 *	@param source Source of the new SGEdge, already copied
	 *	@throws NameAlreadyBoundException very unlikely
	 *	@return copy of SGEdge
	 */
	public static SGEdge copySGEdge(SGEdge oldEdge, Map<String,SGNode> existing, SemanticGraphTransformer sg, SGNode source) throws NameAlreadyBoundException
	{
		SGEdge newEdge = new SGEdge(oldEdge);
		newEdge.setID(sg.getGraph().getFreeID());
		SGNode target = oldEdge.getTarget();
		newEdge.setSource(source);
		
		if (existing.containsKey(target.getID()))
			newEdge.setTarget(existing.get(target.getID()));
		else if (target instanceof QueryValueNode)
			newEdge.setTarget(QueryValueNode.copyQueryValueNode((QueryValueNode) target, existing, sg));
		else if (target instanceof QueryNode)
			newEdge.setTarget(QueryNode.copyQueryNode((QueryNode) target, existing, sg));
		else
			newEdge.setTarget(SGNode.copyNode(target, existing, sg));
		sg.addEdge(newEdge, true);
		return newEdge;
	}
	
	/**	Set whether this edge is shown or hidden in a summation node
	 *	@see #SHOWN_LIST
	 *	@see #HIDDEN_LIST
	 *	@param i Show or hide
	 */	
	public void setListShown(int i)
	{
		showInList = i;
	}
	
	/**	Check whether this edge is shown or hidden in a summation node
	 *	@see #SHOWN_LIST
	 *	@see #HIDDEN_LIST
	 *	@return 1 if it should be shown, 0 if it should be hidden
	 */	
	public int showInList()
	{
		return showInList;
	}
	
	/**	Set whether this edge is new, from the archive, shown, hidden.
	 *	@see SGNode#NEW
	 *	@see SGNode#SHOW
	 *	@see SGNode#HIDE
	 *	@param r new edge, or old edge that is shown or hidden
	 */	
	public void setRealise(int r)
	{
		mustRealise = r;
		if (r > SGNode.NEW)	//if the node is from the database,
			setRemovable(false);	//it should not be possible to remove it
	}
	
	/**	Returns whether this edge is new, from the archive, shown, hidden.
	 *	@see SGNode#NEW
	 *	@see SGNode#SHOW
	 *	@see SGNode#HIDE
	 *	@return int
	 */
	public int mustRealise()
	{
		return mustRealise;
	}
	
	/**	Set whether this edge must be marked in the feedback text
	 *	@param f boolean, true if edge should be marked
	 */
	public void setFlash(boolean f)
	{
		flash = f;
	}
	
	/**	Checks whether this edge must be marked in the feedback text
	 *	@return true if edge should be marked
	 */
	public boolean flash()
	{
		return flash;
	}
	
	/**	Returns the source of this edge
	 *	@return SGNode
	 */
	public SGNode getSource()
	{
		return (SGNode) super.getSource();
	}
	
	/**	Returns the target of this edge
	 *	@return SGNode
	 */
	public SGNode getTarget()
	{
		return (SGNode) super.getTarget();
	}
	
	/**	Sets the sequence nr, the order in which edges are attached to enable undo
	 *	@param i int
	 */
	public void setSequenceNr(int i)
	{
		sequenceNr = i;
	}
	
	/**	Retrieves the sequence nr, the order in which edges are attached to enable undo
	 *	@return int
	 */
	public int getSequenceNr()
	{
		return sequenceNr;
	}
	
	/**	Sets the realisation nr, the order in which identical edges must be added
	 *	@param i int
	 */
	public void setRealiseNr(int i)
	{
		realiseNr = i;
	}
	
	/**	Retrieves the realise nr, the order in which identical edges must be added
	 *	@return int
	 */
	public int getRealiseNr()
	{
		return realiseNr;
	}
	
	/**	Implements the compareTo function, so the edges can be ordered for 
	 *	realisation in the feedback text
	 *	@see Comparable#compareTo(Object)
	 *	@param o Object
	 *	@return int
	 *	@throws ClassCastException if o cannot be cast to an SGEdge
	 */
	public int compareTo(Object o) throws ClassCastException
	{
		SGEdge e = (SGEdge) o;
		if (realiseNr < 0)
		{
			int seq = e.getSequenceNr();
			if (sequenceNr > seq)
				return 1;
			if (sequenceNr == seq)
				return 0;
			return -1;
		}
				
		int r = e.getRealiseNr();
		if (r == realiseNr)
			return 0;
		if (r > realiseNr)
			return -1;
		return 1;
	}	
	
	/**	Sets whether this edge may be removed by the user
	 *	@param r true if edge can be removed
	 */
	public void setRemovable(boolean r)
	{
		removable = r;
	}
	
	/**	Checks whether this edge may be removed by the user
	 *	@return r true if edge may be removed
	 */
	public boolean isRemovable()
	{	//if the edge is extracted from the database, it cannot be removed
		if (mustRealise > 0)
			return false;
		return removable;
	}
}