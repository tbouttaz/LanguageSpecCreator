package WYSIWYM.model;

import java.util.*;

/**	Graph is an abstract superclass for graph structures (such as SemanticGraph
 *	and DependencyTree)
 *
 *	@author Feikje Hielkema
 *	@version 1.1 09-11-2006
 *
 *	@version 1.2 20-09-2007
 */
public abstract class Graph
{
	private Node root;
	protected Map<String, Node> nodes;	//contain all nodes and edges in the graph. Keys are their labels
	protected Map<String, Edge> edges;
	
	private int x = 0, y = 0, z = 0;

	/**	Default constructor
	 */
	public Graph()
	{
		nodes = new HashMap<String, Node>();
		edges = new HashMap<String, Edge>();
	}

	/**	Constructor, sets the given node to the root
	 *	@param r Root node
	 */
	public Graph(Node r)
	{
		nodes = new HashMap<String, Node>();
		edges = new HashMap<String, Edge>();	
		r.setID(getFreeID());
		root = r;
		nodes.put(root.getID(), root);		
	}
	
	/**	Returns whether the given node is part of this graph
	 *	@param node Node
	 *	@return true if node is part of the graph
	 */
	public boolean hasNode(Node node)
	{
		if (node.getID() == null)
			return false;
		 return nodes.containsKey(node.getID());
	}

	/**	Returns whether the given edge is part of this graph
	 *	@param edge Edge
	 *	@return true if edge is part of the graph
	 */	
	public boolean hasEdge(Edge edge)
	{
		if (edge.getID() == null)
			return false;
		return edges.containsKey(edge.getID());	
	}	
	
	/**	Returns whether the component with the given id is part of this graph
	 *	@param id ID
	 *	@return true if the graph contains a component with this id
	 */
	public boolean hasComponent(String id)
	{
		if (nodes.containsKey(id) || edges.containsKey(id))
			return true;
		return false;
	}
	
	/**	Returns the root node
	 *	@return Node
	 */
	public Node getRoot()
	{
		return root;
	}
	
	/**	Sets the root node
	 *	@param r	root node
	 */
	public void setRoot(Node r)
	{
		root = r;
		if (root.getID() == null)
			root.setID(getFreeID());
		
		if (!hasNode(root))
			nodes.put(root.getID(), root);
	}
	
	/**	Returns alle edges in the graph
	 *	@return Iterator over Edges
	 */
	public Iterator getEdges()
	{
		return edges.values().iterator();
	}
	
	/**	Returns a HashMap with all edges, with their ID's as key
	 *	@return HashMap<String,Edge>
	 */
	public Map<String, Edge> getEdgeMap()
	{
		return edges;
	}
	
	/**	Returns the edge with the given ID
	 *	@param id ID
	 *	@return Edge
	 */	
	public Edge getEdge(String id)
	{
		return (Edge) edges.get(id);
	}
	
	/**	Returns all nodes in the graph
	 *	@return Iterator over Nodes
	 */
	public Iterator getNodes()
	{
		return nodes.values().iterator();
	}
	
	/**	Returns a HashMap with all nodes, with their ID's as key
	 *	@return HashMap
	 */
	public Map<String, Node> getNodeMap()
	{
		return nodes;
	}
	
	/**	Returns the node with the given ID
	 *	@param id ID
	 *	@return Node
	 */	
	public Node getNode(String id)
	{
		return (Node) nodes.get(id);
	}
	
	/**	Returns the component with the given ID
	 *	@param id ID
	 *	@return Object (a Node or an Edge)
	 */	
	public Object getComponent(String id)
	{
		if (nodes.containsKey(id))
			return nodes.get(id);
		else
			return edges.get(id);
	}
	
	/**	Returns all components in the graph
	 *	@return Iterator over Nodes and Edges
	 */
	public Iterator getComponents()
	{
		Collection c = nodes.values();
		c.addAll(edges.values());
		return c.iterator();
	}
	
	/*** This will return an unused ID - unless there are already 26^3 elements 
	 *	in the graph (which seems unlikely)
	 *	@return String with unused ID
	 */
	public String getFreeID()
	{
		char[] alphabet = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
		z++;
		if (z > 25)
		{
			z = 0;
			y++;
			if (y > 25)
			{
				y = 0;
				x++;
				if (x > 25)
					return null;
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append(alphabet[x]);
		sb.append(alphabet[y]);
		sb.append(alphabet[z]);
		if (hasComponent(sb.toString()))	//try again
			return getFreeID();
		else
			return sb.toString();
	}
}