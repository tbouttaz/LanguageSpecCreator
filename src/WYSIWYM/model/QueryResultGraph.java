package WYSIWYM.model;

import java.util.ArrayList;
import java.util.List;

/**	QueryResultGraph contains information about the matches of a particular query.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 24-09-08
 */
public class QueryResultGraph extends SemanticGraph 
{
	private List<SGNode> roots = new ArrayList<SGNode>();
	
	/**	@see SemanticGraph#SemanticGraph(SemanticGraph)
	 *	@param user username
	 */
	public QueryResultGraph(String user)
	{
		super(user);
	}

	/**	Constructor, sets the given nodes as the roots
	 *	@param r List<SGNode> with the root nodes
	 *	@param user username
	 */
	public QueryResultGraph(List<SGNode> r, String user)
	{
		super(r.get(0), user);
		roots = r;
		for (int i = 1; i < r.size(); i++)
		{
			SGNode node = r.get(i);
			if (node.getID() == null)
				node.setID(getFreeID());
			nodes.put(node.getID(), node);
		}
	}
	
	/**	Copies the user and cntr of given graph
	 *	@param sg QueryResultGraph to copy
	 */
	public QueryResultGraph(QueryResultGraph sg)
	{
		super(sg);
	}
	
	/**	Returns a list with the roots of this graph
	 * 	@return List<SGNode>
	 */
	public List<SGNode> getRoots()
	{
		return roots;
	}
	
	/**	Adds a root node
	 * 	@param node SGNode
	 */
	public void addRoot(SGNode node)
	{
		roots.add(node);
		if (!hasNode(node))
		{
			if (node.getID() == null)
				node.setID(getFreeID());
			nodes.put(node.getID(), node);
		}
	}
}