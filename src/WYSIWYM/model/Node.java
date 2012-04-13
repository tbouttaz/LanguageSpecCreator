package WYSIWYM.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.ontology.OntologyReader;

/**	Node is an interface for tree nodes
 *	@author Feikje Hielkema
 *	@version 1.1 20-10-2006
 */
public abstract class Node implements TreeComponent
{	
	private String nameSpace, label, id;
	private HashMap<String, Edge> incoming, outgoing;
	private Anchor anchor;
	private boolean flash = false;
	
	/**	Constructs a Node with the given label
	 *	@param lbl Label
	 */
	public Node(String lbl)
	{
		label = lbl;
		id = null;
		incoming = new HashMap<String, Edge>();
		outgoing = new HashMap<String, Edge>();
	}

	/**	Marks the node in the feedback text, to show it's new
	 *	@param f true if the node should be marked in the text
	 */
	public void setFlash(boolean f)
	{
		flash = f;
	} 
	
	/**	Returns true if the node should be marked in the feedback text
	 *	@return boolean
	 */
	public boolean flash()
	{
		return flash;
	}

	/** @see Object#equals(Object)
	 */
	public boolean equals(Node n)
	{
		if ((id != null) && id.equals(n.getID()))
			return true;
		return false;
	}
	
	/** Add an incoming edge to the node. If the node is already present, throw an exception
	 *	If possible, use the id as key, otherwise the label
	 *
	 *	@param edge	Incoming edge
	 *	@throws NameAlreadyBoundException if the edge already is an incoming edge
	 */
	public void addIncomingEdge(Edge edge) throws NameAlreadyBoundException
	{
		if (edge.getID() == null)
			if (incoming.containsKey(edge.getLabel()))
				throw new NameAlreadyBoundException("Edge " + edge.getLabel() + " already is an incoming edge in " + label);
			else
				incoming.put(edge.getLabel(), edge);
		else
			incoming.put(edge.getID(), edge);
	}
	
	/**	Removes (and returns) edge from incoming edges
	 *
	 *	@param edge Edge to be removed
	 *	@return Removed edge
	 */
	public Edge removeIncomingEdge(Edge edge)
	{
		if (!hasIncomingEdge(edge))
			return null;
		
		if (edge.getID() == null)
			return (Edge) incoming.remove(edge.getLabel());
		return (Edge) incoming.remove(edge.getID());
	}
	
	/**	Removes all incoming edges.
	 */	
	public void removeIncomingEdges()
	{
		incoming.clear();
	}
	
	/** Add an outgoing edge to the node. If the edge is already present, throw an exception
	 *	If possible, use the id as key, otherwise the label
	 *	@param edge	Edge to be added
	 *	@throws NameAlreadyBoundException if the edge already is an outgoing edge
	 */
	public void addOutgoingEdge(Edge edge) throws NameAlreadyBoundException
	{
		if (edge.getID() == null)
			if (outgoing.containsKey(edge.getLabel()))
				throw new NameAlreadyBoundException("Edge " + edge.getLabel() + " already is an outgoing edge in " + label);
			else
				outgoing.put(edge.getLabel(), edge);
		else
			outgoing.put(edge.getID(), edge);
	}
	
	/**	Removes (and returns) edge from outgoing edges
	 *	@param edge Edge to be removed
	 *	@return Removed edge
	 */
	public Edge removeOutgoingEdge(Edge edge)
	{
		if (!hasOutgoingEdge(edge))
			return null;
		
		if (edge.getID() == null)
			return (Edge) outgoing.remove(edge.getLabel());
		return (Edge) outgoing.remove(edge.getID());
	}
	
	/**	Removes (and returns) edge with the given id from outgoing edges
	 *	@param id ID of edge to be removed
	 *	@return Removed edge
	 */
	public Edge removeOutgoingEdge(String id)
	{
		if (!outgoing.containsKey(id))
			return null;
		return (Edge) outgoing.remove(id);
	}
	
	/**	Removes (and returns) edge
	 *	@param edge Edge to be removed
	 *	@return boolean showing whether operation was succesful
	 */
	public boolean removeEdge(Edge edge)
	{
		if (!hasEdge(edge))
			return false;
			
		if (removeIncomingEdge(edge) == null)
			removeOutgoingEdge(edge);	
		return true;
	}
	
	/**	Returns whether this node has at least one incoming edge
	 *	@return true if the node has >=1 incoming edge
	 */
	public boolean hasIncomingEdge()
	{
		return (incoming.size() > 0);
	}
	
	/**	Returns whether this node has at least one outgoing edge
	 *	@return true if node has >=1 outgoing edge
	 */
	public boolean hasOutgoingEdge()
	{
		return (outgoing.size() > 0);
	}
	
	/**	Returns whether this node has the given edge as incoming edge
	 *	@param edge Edge
	 *	@return true if edge is an incoming edge of node
	 */
	public boolean hasIncomingEdge(Edge edge)
	{
		if (edge.getID() == null)
			return incoming.containsKey(edge.getLabel());
		return incoming.containsKey(edge.getID());
	}
	
	/**	Returns whether this node has the given edge as outgoing edge
	 *	@param edge Edge
	 *	@return true if edge is an outgoing edge of node
	 */
	public boolean hasOutgoingEdge(Edge edge)
	{
		if (edge.getID() == null)
			return outgoing.containsKey(edge.getLabel());
		return outgoing.containsKey(edge.getID());
	}
	
	/**	Returns whether this node has the given edge
	 *	@param edge Edge
	 *	@return true if node has this edge
	 */
	public boolean hasEdge(Edge edge)
	{
		if (hasIncomingEdge(edge) || hasOutgoingEdge(edge))
			return true;
		return false;
	}
	
	/**	Returns whether this node has an edge with the given label
	 *	@param	label
	 *	@return true if node has an edge with this label
	 */
	public boolean hasEdge(String label)
	{
		for(Iterator it = getEdges(); it.hasNext(); )
		{
			Edge e = (Edge) it.next();
			if (label.equals(e.getLabel()))
				return true;
		}
		return false;
	}
	
	/**	Sets the ID 
	 *	@param str ID
	 */
	public void setID(String str)
	{
		id = str;
	}
	
	/**	Sets the label
	 *	@param str label
	 */
	public void setLabel(String str)
	{
		label = str;
	}
	
	/**	Returns an iterator with all parent nodes
	 *	@return Iterator over SGNodes
	 */
	public Iterator getParents()
	{	
		ArrayList result = new ArrayList();
		Iterator it = incoming.values().iterator();
		while (it.hasNext())
		{
			Edge edge = (Edge) it.next();
			result.add(edge.getSource());
		}
		return result.iterator();
	}
	
	/*	Returns a (random) parent node
	 * 	@return Parent Node
	 */
	public Node getParent()
	{
		if (incoming.size() > 0)
			return (Node) getParents().next();
		return null;
	}
	
	/**	Returns an iterator with all child nodes
	 *	@return Iterator over SGNodes
	 */
	public Iterator getChildren()
	{
		ArrayList result = new ArrayList();
		Iterator it = outgoing.values().iterator();
		while (it.hasNext())
		{
			Edge edge = (Edge) it.next();
			result.add(edge.getTarget());
		}
		return result.iterator();
	}
	
	/**	Returns an iterator over all target nodes of edges with the given label.
	 *	@param label Dependency label
	 *	@return Iterator over SGNodes
	 */
	public Iterator getChildren(String label)
	{
		List<Edge> edges = getOutgoingEdges(label);
		List<Node> result = new ArrayList<Node>();
		for (int i = 0; i < edges.size(); i++)
			result.add(edges.get(i).getTarget());
		return result.iterator();
	}
	
	/**	Returns an iterator with all incoming edges
	 *	@return Iterator over SGEdges
	 */
	public Iterator getIncomingEdges()
	{
		return incoming.values().iterator();
	}
	
	/**	Returns an iterator with all outgoing edges
	 *	@return Iterator over SGEdges
	 */
	public Iterator getOutgoingEdges()
	{
		return outgoing.values().iterator();
	}
	
	/**	Returns an iterator with all edges
	 *	@return Iterator over SGEdges
	 */
	public Iterator getEdges()
	{
		ArrayList l = new ArrayList();
		Iterator it = outgoing.values().iterator();
		while (it.hasNext())
			l.add(it.next());
		
		it = incoming.values().iterator();
		while (it.hasNext())
			l.add(it.next());
		
		return l.iterator();
	}
	
	/**	Returns the number of edges
	 *	@return edge number
	 */
	public int getNumberOfEdges()
	{
		return incoming.size() + outgoing.size();
	}
	
	/**	Returns the number of outgoing edges
	 *	@return outgoing edge number
	 */
	public int getOutgoingEdgesNr()
	{
		return outgoing.size();
	}
	
	/**	Returns all the outgoing edges with the given label
	 *	@param	label String label
	 *	@return ArrayList<Edge>
	 */
	public ArrayList<Edge> getOutgoingEdges(String label)
	{
		ArrayList<Edge> result = new ArrayList<Edge>();
		Iterator it = outgoing.values().iterator();
		while (it.hasNext())
		{
			Edge edge = (Edge) it.next();
			if ((label == null) || edge.getLabel().equals(label))
				result.add(edge);
		}
		return result;
	}
	
	/**	Returns all the incoming edges with the given label
	 *	@param	label String label
	 *	@return ArrayList<Edge>
	 */
	public ArrayList<Edge> getIncomingEdges(String label)
	{
		ArrayList<Edge> result = new ArrayList<Edge>();
		Iterator it = incoming.values().iterator();
		while (it.hasNext())
		{
			Edge edge = (Edge) it.next();
			if ((label == null) || edge.getLabel().equals(label))
				result.add(edge);
		}
		return result;
	}
	
	/**	Returns all edges with the given label
	 *	@param	label String label
	 *	@return ArrayList<Edge>
	 */
	public ArrayList<Edge> getEdges(String label)
	{
		ArrayList<Edge> result = getOutgoingEdges(label);
		result.addAll(getIncomingEdges(label));
		return result;
	}
	
	/**	Returns all edges of the same property in the same direction,
	 *	and the inverse property in the other direction. For instance,
	 *	if e is an outgoing edge labeled 'hasAuthor', this will return
	 *	all outgoing edges labeled 'hasAuthor' and all incoming edges
	 *	labeled 'authorOf'.
	 *
	 *	@param e Original Edge
	 *	@param inverse	Name of inverse property, null if there is no inverse.
	 *	@return List<Edge> Similar edges
	 */
	public List<Edge> getSimilarEdges(Edge e, String inverse)
	{
		List<Edge> result = new ArrayList<Edge>();
		boolean outgoing = hasOutgoingEdge(e);
		if (outgoing)
		{
			result.addAll(getOutgoingEdges(e.getLabel()));
			if (inverse != null)
				result.addAll(getIncomingEdges(inverse));
		}
		else
		{
			result.addAll(getIncomingEdges(e.getLabel()));
			if (inverse != null)
				result.addAll(getOutgoingEdges(inverse));
		}
		return result;	
	}
	
	/**	Returns all edges of the same properties in the same direction,
	 *	and the inverse property in the other direction.
	 *	If two edges have the same source and target, only one is returned,
	 *	as to do other would result in multiple lexicalisations of the same statement!
	 *
	 *	@param e Original edge
	 *	@param properties List<String> with property names
	 *	@param reader Ontology
	 *	@return List<Edge> with similar edges
	 */
	public List<Edge> getSimilarEdges(Edge e, List<String> properties, OntologyReader reader)
	{
		List<Edge> result = new ArrayList<Edge>();	
		if (hasOutgoingEdge(e))
		{			
			for (int i = 0; i < properties.size(); i++)
			{
				result.addAll(getOutgoingEdges(properties.get(i)));
				String inverse = reader.getInverse(properties.get(i));
				if (inverse != null)
					result.addAll(getIncomingEdges(inverse));
				else if (reader.getProperty(properties.get(i)).isSymmetricProperty())
					result.addAll(getIncomingEdges(properties.get(i)));
			}
		}
		else
		{
			for (int i = 0; i < properties.size(); i++)
			{
				result.addAll(getIncomingEdges(properties.get(i)));
				String inverse = reader.getInverse(properties.get(i));
				if (inverse != null)
					result.addAll(getOutgoingEdges(inverse));
				else if (reader.getProperty(properties.get(i)).isSymmetricProperty())
					result.addAll(getOutgoingEdges(properties.get(i)));
			}
		}
		if (!result.contains(e))
			result.add(e);
		return result;	
	}
	
	/**	Returns number of incoming edges
	 *	@return incoming edge nr
	 */
	public int getIncomingEdgeNr()
	{
		return incoming.size();
	}
	
	/**	Returns number of outgoing edges
	 *	@return outgoing edge nr
	 */
	public int getOutgoingEdgeNr()
	{
		return outgoing.size();
	}
	
	/**	Returns the label
	 *	@return String
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**	Returns the ID
	 *	@return String
	 */
	public String getID()
	{
		return id;
	}
	
	/**	Overload (@see Node#isEdge()); returns that this is a node, not an edge
	 *	@see TreeComponent#isEdge()
	 *	@return false
	 */
	public boolean isEdge()
	{
		return false;
	}
	
	/**	Returns whether this node has any incoming or outgoing edges
	 *	@return boolean
	 */
	public boolean hasEdges()
	{
		return (hasIncomingEdge() || hasOutgoingEdge());
	}
	
	/**	Overload; returns that this is a node, not an edge
	 *	@see TreeComponent#isNode()
	 *	@return true
	 */
	public boolean isNode()
	{
		return true;
	}
	
	/**	Sets the anchor
	 *	@param a Anchor
	 */
	public void setAnchor(Anchor a)
	{
		anchor = a;
	}
	
	/**	Removes the anchor
	 */
	public void removeAnchor()
	{
		anchor = null;
	}
	
	/**	Returns the anchor
	 *	@return Anchor
	 */
	public Anchor getAnchor()
	{
		return anchor;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public String getNameSpace() {
		return nameSpace;
	}
}