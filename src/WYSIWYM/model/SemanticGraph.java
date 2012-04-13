package WYSIWYM.model;

import java.util.ArrayList;
import java.util.Iterator;

/**	SemanticGraph holds the metadata structure/query the user is building.
 *	It has a map of nodes and edges in the graph, with id as key.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 24-10-2006
 *
 *	@version 1.1 20-09-2007
 *
 *	@version 1.2 08-02-08
 */
public class SemanticGraph extends Graph
{
	private String user = "";	//username
	private String uri;	//uri where Fedora stored the resource
	private int cntr = 0;	//used to make id's unique within the semantic graph, by combining username and cntr
	
	/**	Constructs a SemanticGraph with the given username
	 *	@param user username
	 */
	public SemanticGraph(String user)
	{
		super();
		if ((user != null) && (user.length() > 0))
			this.user = user.substring(0,1);
	}

	/**	Constructor, sets the given node as the root
	 *	@param r SGNode
	 *	@param user username
	 */
	public SemanticGraph(SGNode r, String user)
	{
		super(r);
		if (user != null)
			this.user = user.substring(0,1);
	}
	
	/**	Copies the basic information of the given graph, but not
	 *	the nodes and edges
	 *	@param sg SemanticGraph to copy
	 */
	public SemanticGraph(SemanticGraph sg)
	{
		this(sg.getUser());
		uri = sg.getURI();
		cntr = sg.getCntr();
	}
	
	/**	Sets the resource URI
	 *	@param uri Resource URI
	 */
	public void setURI(String uri)
	{
		this.uri = uri;
	}
	
	/**	Returns the resource URI
	 *	@return String
	 */
	public String getURI()
	{
		return uri;
	}
	
	/** Returns the root node
	 *	@return SGNode
	 */
	public SGNode getRoot()
	{
		return (SGNode) super.getRoot();
	}
	
	/**	Sets the root node, and specifies its SG id.
	 *	@param r	root node
	 */
	public void setRoot(SGNode r)
	{
		super.setRoot(r);
		if ((r.getSGID() == null) && (user.length() > 0))
			r.setSGID(user + Integer.toString(cntr));	//the sgID will be used for disambiguation in the feedback text
	}
	
	/**	Returns the username
	 *	@return String
	 */
	public String getUser()
	{
		return user;
	}
	
	/**	Sets the username
	 *	@param u username
	 */
	public void setUser(String u)
	{
		user = u;
	}
	
	/**	Returns the current counter value
	 *	@return int
	 */
	public int getCntr()
	{
		return cntr;
	}
	
	/**	Increments the counter
	 */
	public void incrementCntr()
	{
		cntr++;
	}
	
	/**	Checks whether the Semantic Graph contains a node with this
	 *	unique sesame id (the id that is stored in a separate target node)
	 *	@param id Sesame ID
	 *	@return true if the graph contains this node
	 */
	public boolean hasUniqueIDNode(String id)
	{
		for (Iterator it = getNodes(); it.hasNext(); )
		{
			SGNode node = (SGNode) it.next();
			if (id.equals(node.getUniqueID()))
				return true;
		}
		return false;
	}
	
	/**	Returns the node that corresponds to the object with this unique sesame id;
	 *	so it returns the PARENT node of the SGNode containing the id!
	 *	@param id Sesame ID
	 *	@return SGNode
	 */
	public SGNode getUniqueIDNode(String id)
	{
		for (Iterator it = getNodes(); it.hasNext(); )
		{
			SGNode node = (SGNode) it.next();
			if (id.equals(node.getLabel()))
				return (SGNode) node.getParent();
		}
		return null;
	}
	
	/**	Retrieves all nodes with the given label
	 *	@param label label
	 *	@return ArrayList<SGNode>
	 */
	public ArrayList<SGNode> getNodesWithLabel(String label)
	{
		Iterator it = getNodes();
		ArrayList<SGNode> result = new ArrayList<SGNode>();
		while (it.hasNext())
		{
			SGNode n = (SGNode) it.next();
			if ((n.getLabel() != null) && n.getLabel().equals(label))
				result.add(n);
		}
		return result;
	}

	/**	Retrieves all edges with the given sequence number (rank nr. in order of creation)
	 *	@param seq sequence number
	 *	@return ArrayList<SGEdge>
	 */	
	public ArrayList<SGEdge> getSequenceEdges(int seq)
	{
		ArrayList<SGEdge> result = new ArrayList<SGEdge> ();
		Iterator it = getEdges();
		while (it.hasNext())
		{
			SGEdge e = (SGEdge) it.next();
			if (e.getSequenceNr() == seq)
				result.add(e);
		}
		return result;
	}
	
	/**	Retrieves all nodes with the given sequence number (rank nr. in order of creation)
	 *	@param seq sequence number
	 *	@return ArrayList<SGNode>
	 */
	public ArrayList<SGNode> getSequenceNodes(int seq)
	{
		ArrayList<SGNode> result = new ArrayList<SGNode> ();
		Iterator it = getNodes();
		while (it.hasNext())
		{
			SGNode n = (SGNode) it.next();
			if (n.getSequenceNr() == seq)
				result.add(n);
		}
		return result;
	}
	
	
	/**	Checks if there are components with the given sequence number (rank nr. in order of creation)
	 *	@param seq sequence number
	 *	@return true if the graph contains a component with this sequence number
	 */
	public boolean hasSequenceComponents(int seq)
	{
		if (getSequenceNodes(seq).size() > 0)
			return true;
		if (getSequenceEdges(seq).size() > 0)
			return true;
		return false;
	}
	
	/**	Returns the highest sequence number (rank nr. in order of creation) in the graph
	 *	@return int
	 */
	public int getLargestSequenceNr()
	{
		int result = 0;
		for (Iterator it = getEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			if (edge.getSequenceNr() > result)
				result = edge.getSequenceNr();
		}
		for (Iterator it = getNodes(); it.hasNext(); )
		{
			SGNode node = (SGNode) it.next();
			if (node.getSequenceNr() > result)
				result = node.getSequenceNr();
		}
		return result;
	}
	
	/**	Ensures none of the edges are marked in the feedback text 
	 *	when realised next
	 */	
	public void stopFlashing()
	{
		for (Iterator it = getEdges(); it.hasNext(); )
			((SGEdge)it.next()).setFlash(false);
		for (Iterator it = getNodes(); it.hasNext(); )
			((SGNode)it.next()).setFlash(false);
	}
}