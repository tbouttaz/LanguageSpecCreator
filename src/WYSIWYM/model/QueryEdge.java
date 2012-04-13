package WYSIWYM.model;

import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.transformer.SemanticGraphTransformer;

/**	QueryEdge in an edge in the QueryGraph. It has a boolean operator, denoting 
 *	whether it is part of a conjunction, disjunction, or is a denial or optional.
 *	@author Feikje Hielkema
 *
 *	@version 1.2 08-02-08
 */
public class QueryEdge extends SGEdge
{
	/**	Denotes the 'any property' edges */
	public static final String ANYTHING = "ANYTHING";
	private int booleanOperator = -1;
	private boolean optional = false;
	private QueryEdge[] sameValueEdges = new QueryEdge[0];
	
	/**	Constructs an edge with the given label
	 *	@param label Label
	 */
	public QueryEdge(String label)
	{
		super(label);
	}
	
	/**	Constructs QueryEdge with given label and boolean operator.
	 *	Possible values for operator are 0 = conjunction (and), 
	 *	1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *	@param label Label
	 *	@param op Boolean operator, int between 0 and 2
	 */	
	public QueryEdge(String label, int op)
	{
		super(label);
		booleanOperator = op;
	}
	
	/**	Constructs QueryEdge with given label, operator, source and target node
	 *	ossible values for operator are 0 = conjunction (and), 
	 *	1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *
	 *	@param lbl	label
	 *	@param op	Boolean operator, int between 0 and 2
	 *	@param s	source node
	 *	@param t	target node
	 *	@throws NameAlreadyBoundException when this edge is already in the source
	 *	or target node
	 */
	public QueryEdge(String lbl, int op, QueryNode s, QueryNode t) throws NameAlreadyBoundException
	{
		super(lbl, s, t);
		booleanOperator = op;
	}
	
	/**	Constructs a copy of the given edge
	 *	@param edge Edge to copy
	 */	
	public QueryEdge(QueryEdge edge)
	{
		super(edge);
		booleanOperator = edge.getBooleanOperator();
	}
	
	/**	Constructs a copy of the given SGEdge. If the edge is a 
	 *	QueryEdge, it also copies the boolean operator.
	 *	@param edge SGEdge to copy
	 */	
	public QueryEdge(SGEdge edge)
	{
		super(edge);
		if (edge instanceof QueryEdge)
			booleanOperator = ((QueryEdge)edge).getBooleanOperator();
	}
	
	/**	Constructs a copy of the given edge, and adds it with copies of the source and target
	 *	to the Query Graph
	 *
	 *	@param oldEdge QueryEdge to copy
	 *	@param existing HashMap with nodes already copied (potential source and targets)
	 *	@param sg Semantic Graph Transformer
	 *	@param source Source of the new SGEdge, already copied
	 *	@throws NameAlreadyBoundException very unlikely
	 *	@return copy of QueryEdge
	 */	
	public static QueryEdge copyQueryEdge(QueryEdge oldEdge, Map<String,SGNode> existing, SemanticGraphTransformer sg, SGNode source) throws NameAlreadyBoundException
	{
		QueryEdge newEdge = new QueryEdge(oldEdge);
		newEdge.setOptional(oldEdge.isOptional());
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
	
	/**	Sets whether this edge is an optional requirement. If it's part of an
	 *	aggregation, the other edges should receive the same optional value.
	 *
	 *	@param o true if this requirement is optional
	 */
	public void setOptional(boolean o)
	{
		optional = o;
		for (int i = 0; i < sameValueEdges.length; i++)
			sameValueEdges[i].setOptional(o);
		sameValueEdges = new QueryEdge[0];
	}
	
	/**	Checks whether this edge is an optional requirement. 
	 *
	 *	@return true if this requirement is optional
	 */
	public boolean isOptional()
	{
		return optional;
	}
	
	/**	Tells this edge which other edges are part of its aggregation
	 *
	 *	@param edges SGEdge[] with other edges in its aggregation
	 */
	public void setSameOptionalValue(SGEdge[] edges)
	{
		sameValueEdges = new QueryEdge[edges.length - 1];
		for (int i = 1; i < edges.length; i++)
			sameValueEdges[i-1] = (QueryEdge) edges[i];
	}
	
	/** Sets the boolean operator. Default value is -1; value can be set to
	 *	0 = conjunction (and), 1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *	
	 *	@param op integer between 0 and 2
	 */
	public void setBooleanOperator(int op)
	{
		booleanOperator = op;
	}
	
	/** Returns the boolean operator. Default value is -1; value can be set to
	 *	0 = conjunction (and), 1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *	@return integer between 0 and 2
	 */
	public int getBooleanOperator()
	{
		return booleanOperator;
	}
	
	/**	Returns the other edges that are part of the same aggregation as this one
	 *
	 *	@return SGEdge[] with other edges in its aggregation
	 */
	public QueryEdge[] getSameValueEdges()
	{
		return sameValueEdges;
	}
}