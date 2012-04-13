package WYSIWYM.model;

import java.util.Iterator;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.transformer.SemanticGraphTransformer;
import WYSIWYM.util.BadAnchorException;

/**	QueryNode is a node in the QueryGraph. It has a boolean operator, denoting 
 *	whether it is part of a conjunction, disjunction, or is a denial or optional.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 08-02-2008
 */
public class QueryNode extends SGNode
{
	private int booleanOperator = -1;
	
	/**	Constructs QueryNode with given label
	 *
	 *	@param lbl label
	 */
	public QueryNode(String lbl)
	{
		super(lbl);
	}

	/**	Constructs QueryNode with given label and operator
	 *
	 *	@param lbl label
	 *	@param op operator
	 */
	public QueryNode(String lbl, int op)
	{
		super(lbl);
		booleanOperator = op;
	}
	
	/**	Constructs a copy of the given node, and add it with all its edges to the 
	 *	QueryGraph
	 *	@param node Node to copy
	 *	@param existing HashMap with copied nodes
	 *	@param sg SemanticGraphTransformer with the copy
	 *	@return QueryNode, copy of node
	 *	@throws NameAlreadyBoundException, very unlikely
	 */
	public static QueryNode copyQueryNode(QueryNode node, Map<String,SGNode> existing, SemanticGraphTransformer sg) throws NameAlreadyBoundException
	{
		QueryNode result = new QueryNode(node.getLabel(), node.getBooleanOperator());
		result.setID(node.getID());
		if (node.isFinalLabel())
			result.setFinalNLLabel(new String(node.getFinalLabel()));		
		result.setRemovable(node.isRemovable());
		result.setQuote(node.isQuote());
		result.setOldLabel(node.getOldLabel());
		result.setSequenceNr(node.getSequenceNr());
		result.setBooleanOperator(node.getBooleanOperator());
		result.setRealise(node.mustRealise());
		result.setSGID(node.getSGID());
		existing.put(result.getID(), result);
		
		try
		{
			new Anchor(node.getAnchor(), result);
		}
		catch (BadAnchorException e)
		{}
		
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			if (edge instanceof QueryEdge)
				QueryEdge.copyQueryEdge((QueryEdge) edge, existing, sg, result);
			else
				SGEdge.copySGEdge(edge, existing, sg, result);
		}

		return result;
	}
	
	/** Sets the boolean operator. Default value is -1; value can be set to
	 *	0 = conjunction (and), 1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
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
}