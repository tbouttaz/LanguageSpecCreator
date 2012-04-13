package WYSIWYM.model;

import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.transformer.DependencyTreeTransformer;
/***
 *	DTEdge is an edge in a Dependency Tree.
 *	
 * @author Feikje Hielkema
 * @version 1.00 2006/11/13
 *
 *	@version 1.1 20-09-2007
 */
public class DTEdge extends Edge
{
	private int order = 0;	//order in which this edge was added (compared to others with same label)

	/**	Constructs a DTEdge with the given dependency label
	 *	@param deplbl	dependency label
	 */
    public DTEdge(String deplbl) 
    {
    	super(deplbl);
    }
    
    /**	Constructs a DTEdge with the given dependency label, source and target
	 *	@param deplbl	dependency label
	 *	@param source	source node
	 *	@param target	target node
	 *	@throws NameAlreadyBoundException if edge is already in source or target
	 */
    public DTEdge(String deplbl, DTNode source, DTNode target) throws NameAlreadyBoundException
    {
    	super(deplbl, source, target);
    }
    
    /**	Constructs a DTEdge with the given dependency label,id, source and target
	 *	@param deplbl	dependency label
	 *	@param id ID
	 *	@param source source node
	 *	@param target	target node
	 *	@throws NameAlreadyBoundException if edge is already in source or target
	 */
    public DTEdge(String deplbl, String id, DTNode source, DTNode target) throws NameAlreadyBoundException
    {
    	super(deplbl, id, source, target);
    }
    
    /**	Copies the given edge and creates a new one. Also copies the target node,
     *	checking if it already has been copied in the the HashMap. Adds the new edge
     *	to the given DependencyTree.
     *
     *	@param	oldEdge The edge to be copied
     *	@param	existing HashMap with copied nodes
     *	@param	dt DependencyTreeTransformer containing all copies
     *	@param	source Source node of the new edge
     *	@return	The new DTEdge
     *	@throws	NameAlreadyBoundException very unlikely
     */
    public static DTEdge copyDTEdge(DTEdge oldEdge, Map<String,DTNode> existing, DependencyTreeTransformer dt, DTNode source) throws NameAlreadyBoundException
	{
		DTEdge newEdge = new DTEdge(oldEdge.getLabel());
		newEdge.setOrder(oldEdge.getOrder());
		newEdge.setID(dt.getGraph().getFreeID());
		DTNode target = oldEdge.getTarget();
		newEdge.setSource(source);
		
		if (existing.containsKey(target.getID()))
			newEdge.setTarget(existing.get(target.getID()));
		else
			newEdge.setTarget(DTNode.copyNode(target, existing, dt));
		dt.addEdge(newEdge, true);
		return newEdge;
	}
    
    /**	Sets the rank order, the order in which this edge was added (compared to others with same label)
     *	@param nr rank order
     */
    public void setOrder(int nr)
    {
    	order = nr;
    }
    
    /**	Gets the rank order, the order in which this edge was added (compared to others with same label)
     *	@return int
     */
    public int getOrder()
	{
		return order;
	}
	
	/**	Returns the source of this edge
	 *	@return DTnode
	 */
	public DTNode getSource()
	{
		return (DTNode) super.getSource();
	}
	
	/**	Returns the target node
	 *	@return DTNode
	 */
	public DTNode getTarget()
	{
		return (DTNode) super.getTarget();
	}
}