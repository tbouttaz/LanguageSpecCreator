package WYSIWYM.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**	SummationAnchor signals that a sequence of >3 edges have been
 *	summarised in a list. If the user clicks this 
 *	anchor, all edges are displayed separately.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 01-10-2008
 */
public class SummationAnchor extends Anchor
{
	private List<SGEdge> edges = new ArrayList<SGEdge>();
	
	/**	Constructs the anchor with the list of edges
	 *	@param list List<SGEdge> edges being summarised
	 */
	public SummationAnchor(List<SGEdge> list)
	{
		edges.addAll(list);
		id = UUID.randomUUID().toString();	
	}
	
	/**	Sets all edges to visible (removing summarisation)
	 */
	public void setVisible()
	{
		for (SGEdge edge : edges)
			edge.setListShown(SGEdge.SHOWN_LIST);
	}
	
	/**	Checks whether one of the edges has a target node
	 *	with the given ID
	 *	@param id Node ID
	 *	@return true if one of the edges has that node as target
	 */
	public boolean containsNode(String id)
	{
		for (SGEdge e : edges)
			if (id.equals(e.getTarget().getSGID()))
				return true;
		return false;
	}
}