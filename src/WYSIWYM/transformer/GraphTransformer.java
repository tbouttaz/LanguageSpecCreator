package WYSIWYM.transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.model.Edge;
import WYSIWYM.model.Graph;
import WYSIWYM.model.Node;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.TreeComponent;

/**
 *	Provides some basic graph transforming functionalities, like adding/removing
 *	nodes and edges.
 *
 * @author Feikje Hielkema 
 * @version 1.00 2006/11/13
 *
 *	@version 1.2	March 2008
 *	@version 1.3	28 May 2008
 */
public class GraphTransformer 
{
    private Graph graph;
	
	/**	Constructor, sets the graph
	 *	@param g Graph
	 */
	public GraphTransformer(Graph g)
	{
		graph = g;
	}

	/**	Default constructor, does not create a graph.
	 */
	public GraphTransformer()
	{
		graph = null;
	}
	
	/**	Sets the graph
	 *	@param g	Graph
	 */
	public void setGraph(Graph g)
	{
		graph = g;
	}
	
	/**	Adds the edge to the semantic graph. Also adds the source and target node, if
	 *	there are any.
	 *	If (an equivalent) source and target node is already in the graph, 
	 *	the edge's source/target is set to that node
	 *
	 *	@param edge	Edge to be added
	 *	@param nodesHaveID True if the nodes already have a Sesame ID
	 *	@return false if the Graph already contains an edge with this ID
	 */
	public boolean addEdge(Edge edge, boolean nodesHaveID)
	{
		if (graph.hasEdge(edge))
			return false;
		if (edge.getID() == null)
			edge.setID(graph.getFreeID());
		graph.getEdgeMap().put(edge.getID(), edge);
		try
		{
			Node source = (Node) edge.getSource();				
			if (source != null)
			{
				if (!graph.hasNode(source))
					addNode(source, nodesHaveID);
				else
					edge.setSource(graph.getNode(source.getID()));
			}
			Node target = edge.getTarget();
			if (target != null) 
			{
				if (!graph.hasNode(target))
					addNode(target, nodesHaveID);
				else
					edge.setTarget(graph.getNode(target.getID()));
			}
		}
		catch (NameAlreadyBoundException e)
		{	//This should not be possible!
			e.printStackTrace();
			return false;
		}
			
		return true;
	}
	
	/**	Adds the edge to the semantic graph. Also adds the source and target node, if
	 *	there are any.
	 *	If (an equivalent) source and target node is already in the graph, 
	 *	the edge's source/target is set to that node
	 *
	 *	@param edge	Edge to be added
	 *	@return false if the Graph already contains an edge with this ID
	 */
	public boolean addEdge(Edge edge)
	{
		return addEdge(edge, false);
	}
	
	/**	Adds the node to the semantic graph. Usually not necessary as nodes get
	 *	added automatically with the edges. If the graph already contains a node with 
	 *	this id, this returns false
	 *
	 *	@param node	Node to be added
	 *	@param hasID True if node already has a Sesame ID
	 *	@return false if the Graph already contains a node with this ID
	 */
	public boolean addNode(Node node, boolean hasID)
	{
		if (graph.hasNode(node))
			return false;
		if (node.getID() == null)
			node.setID(graph.getFreeID());
		graph.getNodeMap().put(node.getID(), node);
		return true;
	}
	
	/**	Adds the node to the semantic graph. Usually not necessary as nodes get
	 *	added automatically with the edges. If the graph already contains a node with 
	 *	this id, return false.
	 *
	 *	@param node	Node to be added
	 *	@return false if the Graph already contains a node with this ID
	 */
	public boolean addNode(Node node)
	{
		return addNode(node, false);
	}
	
	/**	Adds the node with all its descendants to the semantic graph. If the graph 
	 *	already contains a node with this id, the descendants are not added!
	 *
	 *	@param node	Root of branch to be added
	 *	@return false if the Graph already contains a node with this ID
	 */
	public boolean addBranch(Node node)
	{
		return addBranch(node, false);
	}
	
	/**	Adds the node with all its descendants to the semantic graph. If the graph 
	 *	already contains a node with this id, the descendants are not added!
	 *
	 *	@param node	Root of branch to be added
	 *	@param ID True if the nodes in the branch already have Sesame ID's.
	 *	@return false if the Graph already contains a node with this ID
	 */
	public boolean addBranch(Node node, boolean ID)
	{
		if (!addNode(node, ID))
			return false;
		
		List<Edge> noID = new ArrayList<Edge>();
		List<Edge> hasID = new ArrayList<Edge>();
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			Edge edge = (Edge) it.next();
			if (edge.getID() == null)//|| graph.hasEdge(edge))
				noID.add(edge);
			else
				hasID.add(edge);
		}
		for (int i =0; i < noID.size(); i++)
		{	//THIS ROUNDABOUT WAY IS NECESSARY TO AVOID CONCURRENT MODIFICATION EXCEPTIONS	
			Edge edge = noID.get(i);
			edge.setID(graph.getFreeID());
			graph.getEdgeMap().put(edge.getID(), edge);
			addBranch(edge.getTarget());
		}
		for (int i = 0; i < hasID.size(); i++)
		{	
			Edge edge = hasID.get(i);	
			graph.getEdgeMap().put(edge.getID(), edge);
			addBranch(edge.getTarget());
		}
		return true;
	}
	
	/**	Removes the node and all its descendants from the graph.
	 *	@param node Root of branch to remove
	 *	@return false if the Graph did not contain this node.
	 */
	public boolean removeBranch(Node node)
	{
		if (!graph.hasNode(node))
			return false;
		
		Iterator it = node.getOutgoingEdges();
		List<Edge> l = new ArrayList<Edge> ();
		while (it.hasNext())
			l.add((Edge) it.next());

		for (int i = 0; i < l.size(); i++)
		{
			Edge e = l.get(i);
			removeBranch(e.getTarget());
			removeEdge(e);
		}
		removeNode(node);
		return true;
	}
	
	/**	Removes the given node from the graph, and also all edges associated with it
	 *
	 *	@param node	Node to be removed
	 *	@return false if the Graph did not contain this node.
	 */
	public boolean removeNode(Node node)
	{
		if (!graph.hasNode(node))
			return false;
		
		Iterator it = node.getEdges();
		while (it.hasNext())
			removeEdge((Edge) it.next());

		graph.getNodeMap().remove(node.getID());
		return true;
	}
	
	/**	Replaces the first node with the second.
	 *
	 *	@param old	Node to replace
	 *	@param newNode Substitute
	 *	@throws NameAlreadyBoundException 
	 */
	public void replaceNode(Node old, Node newNode) throws NameAlreadyBoundException
	{
		newNode.setID(old.getID());
		for (Iterator it = old.getOutgoingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			edge.setSource(newNode);
			it.remove();
		}
		for (Iterator it = old.getIncomingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			edge.setTarget(newNode);
			it.remove();
		}
		graph.getNodeMap().put(newNode.getID(), newNode);
	}
	
	/**	Removes the given node, but leaves its edges!
	 *	@param node Node to remove
	 *	@return false if the Graph did not contain this node
	 */
	public boolean removeNodeLeaveEdges(Node node)
	{
		if (!graph.hasNode(node))
			return false;
		graph.getNodeMap().remove(node.getID());
		return true;
	}
	
	/** Removes the given edge from the graph
	 *
	 *	@param edge	Edge to be removed
	 *	@return false if the Graph did not contain this edge
	 */
	public boolean removeEdge(Edge edge)
	{
		if (!graph.hasEdge(edge))
			return false;
		
		if (edge.getSource() != null)
			edge.getSource().removeEdge(edge);
		if (edge.getTarget() != null)
			edge.getTarget().removeEdge(edge);
		graph.getEdgeMap().remove(edge.getID());
		return true;
	}
	
	/**	Adds a tree component to the graph
	 *
	 *	@param comp	TreeComponent (node or edge) to be added
	 *	@return false if the Graph already contains a component with this ID
	 */
	public boolean add(TreeComponent comp)
	{
		if (comp.isEdge())
			return addEdge((Edge) comp);
		else if (comp.isNode())
			return addNode((Node) comp);
		return false;
	}
	
	/** Removes the given component from the graph
	 *
	 *	@param comp	component to be removed
	 *	@return false if the component was not in the Graph
	 */
	public boolean remove(TreeComponent comp)
	{
		if (comp.isEdge())
			return removeEdge((Edge) comp);
		else if (comp.isNode())
			return removeNode((Node) comp);
		return false;
	}
	
	/**	@return Graph
	 */
	public Graph getGraph()
	{
		return graph;
	}   
}