package WYSIWYM.transformer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.model.Anchor;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.Node;
import WYSIWYM.model.QueryGraph;
import WYSIWYM.model.QueryNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.BadAnchorException;
import WYSIWYM.util.Dotter;

import com.hp.hpl.jena.ontology.OntClass;

/**	SemanticGraphTransformer performs operations on the SemanticGraph. It will 
 *	not add components who share their id with other components in the graph!
 *
 *	@author Feikje Hielkema
 *	@version 1.1 20-10-2006
 *
 *	@version 1.2 15-02-2008
 */
public class SemanticGraphTransformer extends GraphTransformer
{
	protected OntologyReader reader;
	protected String userID;
	
	/**	Constructs transformer, setting the semantic graph
	 *	@param g	SemanticGraph
	 *	@param r	Ontology
	 */
	public SemanticGraphTransformer(SemanticGraph g, OntologyReader r)
	{
		super(g);
		reader = r;
	}
	
	/**	Constructs transformer by copying the Graph from the given Transformer
	 *	@param sgt	SemanticGraphTransformer
	 *	@param r	Ontology
	 */
	public SemanticGraphTransformer(SemanticGraphTransformer sgt, OntologyReader r)
	{
		reader = r;
		if (sgt instanceof QueryBuilder)
			setGraph(new QueryGraph((QueryGraph)sgt.getGraph()));
		else
			setGraph(new SemanticGraph(sgt.getGraph()));
	
		try
		{
			HashMap<String,SGNode> nodeMap = new HashMap<String,SGNode>();
			SGNode root = sgt.getGraph().getRoot();
			if (root instanceof QueryNode)
				getGraph().setRoot(QueryNode.copyQueryNode((QueryNode) root, nodeMap, this));
			else
				getGraph().setRoot(SGNode.copyNode(root, nodeMap, this));
		}
		catch(NameAlreadyBoundException e)
		{
			System.out.println("SemanticGraph 52: A NAMEALREADYBOUNDEXCEPTION HERE SHOULD NOT BE POSSIBLE!");
			e.printStackTrace();
		}
	}
	
	/**	Default constructor, does not construct a Graph.
	 *	@param r Ontology
	 */
	public SemanticGraphTransformer(OntologyReader r)
	{
		super();
		reader = r;
	}
	
	/**	Returns the SemanticGraph
	 *	@return SemanticGraph
	 */
	public SemanticGraph getGraph()
	{
		return (SemanticGraph) super.getGraph();
	}
	
	/**	Sets the user ID
	 *	@param u User ID
	 */
	public void setUserID(String u)
	{
		userID = u;
	}
	
	/**	Adds the node and then sets its Sesame ID.
	 *	hasID should be true if the node already has a Sesame id.
	 *
	 *	@param node Node
	 *	@param hasID True if node already has a Sesame ID
	 *	@return false if the node was already in the Graph, otherwise true
	 */
	public boolean addNode(Node node, boolean hasID)
	{
		if (!super.addNode(node, hasID))
			return false;
		
		if (node instanceof DatatypeNode)
			return true;	//don't add id's to datatype nodes

		SemanticGraph sg = (SemanticGraph) getGraph();
		if (sg.getUser().length() == 0)	
			return true;	//no userID available, makes it impossible to create unique ID's at this point. Interface should make this impossible!
		if (hasID)      	//this node already has a database ID
			return true;
		SGNode sgNode = (SGNode) node;
		sgNode.setSGID(sg.getUser() + Integer.toString(sg.getCntr()));	//the sgID will be used for disambiguation in the feedback text
		sg.incrementCntr();
		
		if (!node.hasEdge("ID"))
		{	//if the node has no unique identifier yet, make one
			try
			{	//create a new ID edge for this node, whose target holds the unique identifier (used for the database)
				SGEdge e = new SGEdge("ID");
				e.setID(getGraph().getFreeID());
				e.setSource(node);
				OntClass c = reader.getClass((SGNode) node);
				SGNode target = new SGNode(AutomaticGenerator.getUniqueID(c));	//as the uniqueID is too long (12 digits) 
				e.setTarget(target);
				addEdge(e, true);
			}
			catch(NameAlreadyBoundException e)
			{}	//can't happen as node and edge were just created
		}
		else	//add the id edge to the graph (in case it isn't in there already)
			addEdge(node.getOutgoingEdges("ID").get(0));
			
		return true;
	}
	
	/**	Implements the undo-function, by searching the last added or changed nodes
	 *	and edges in the graph, and either removing them or undoing the changes.
	 *
	 *	@param	seq The number of the last operation
	 *	@param	oa Ontology
	 *	@return	Anchor. In some cases an anchor that was deleted will be regenerated.
	 */
	public Anchor removeSequence(int seq, OntologyReader oa)
	{
		ArrayList<SGNode> nodeList = ((SemanticGraph)getGraph()).getSequenceNodes(seq);
		Anchor result = null;
		for (int i = 0; i < nodeList.size(); i++)	//last change was change node to a more specific class, so restore the node's original label
		{
			SGNode n = nodeList.get(i);
			String label = n.getLabel();
			if (!n.restoreLabel())
				removeNode(n);	//This node was added; now removing
			else
			{
				try		//not removing node; should restore anchor
				{
					OntClass c = reader.getClass(n);//.getLabel());
					if (c == null)		//datatype property target OR a generic object (something)
					{
						c = reader.getClass(label);
						Edge e = (Edge) n.getIncomingEdges().next();
						String p = e.getLabel();
						OntClass r = (OntClass) reader.getRange(reader.getProperty(p)); //, e.getSource().getLabel(), label));
						boolean query = (this instanceof QueryBuilder);
						result = new Anchor(reader, r, n, query, userID);	//This could throw an exception, in a badly shaped ontology??
						n.setAnchor(result);
					}
					else
					{
						result = new Anchor(reader, c, n, userID);
						n.setAnchor(result);
					}
				}
				catch (BadAnchorException be)		//impossible because at the very least the previous operation
				{									//can be repeated - there must be an anchor
					System.out.println("SGT line 124: A bad anchor exception here shouldn't be possible!");
					be.printStackTrace();
				}
			}
		}
		
		//last change was the addition of one or more edges (and possibly target nodes)
		ArrayList<SGEdge> edgeList = ((SemanticGraph)getGraph()).getSequenceEdges(seq);
		for (int i = 0; i < edgeList.size(); i++)
		{	//remove edge from graph
			SGEdge e = edgeList.get(i);
			Node target = e.getTarget();
			SGNode source = (SGNode) e.getSource();
			removeEdge(e);
			//remove edge from target and source node
			target.removeEdge(e);
			source.removeEdge(e);
			//if target node has no other incoming or outgoing edges, remove that too
			if (!target.hasEdges())
				removeNode(target);
			else if ((target.getNumberOfEdges() == 1) && target.hasEdge("ID"))
				removeNode(target);

			try		//restore anchor
			{
				OntClass c = reader.getClass(source);
				result = new Anchor(reader, c, source, userID);
				source.setAnchor(result);
			}
			catch (BadAnchorException be)	//impossible because at the very least the previous operation
			{									//can be repeated - there must be an anchor
				System.out.println("SGT line 106: A bad anchor exception here shouldn't be possible!");
				be.printStackTrace();
			}
		}
		return result;	//an anchor that was lost should be added again
	}
	
	/**	Prints the semantic graph to a text file in dot format, so it can be visualised
	 *	with GraphViz
	 */
	public void printGraph()
	{
		printGraph("SEMANTIC-GRAPH.txt");
	}
	
	/**	Prints the semantic graph to a text file with the given name in .dot format, 
	 *	so it can be visualised	with GraphViz
	 *	@param fileName Filename
	 */
	public void printGraph(String fileName)
	{
		try
		{
			FileWriter fw = new FileWriter(fileName);
			PrintWriter w = new PrintWriter(fw);
			Dotter d = new Dotter();
			w.print(d.dotSGGraph(getGraph(), null/**, getAnchors()*/));
			w.close();
			fw.close();
		}
		catch (IOException e)
		{
			System.out.println("FTGenerator 695: Error when trying to print the SemanticGraph to file.");
			e.printStackTrace();
		}
	}
	
	/**	Function for QueryBuilder to overload. Ensures that edges of the proper kind
	 *	(QueryEdges for a QueryGraph, SGEdges for SemanticGraph) are created.
	 *
	 *	@param label Property label
	 *	@return SGEdge
	 */
	public SGEdge makeEdge(String label)
	{
		if (this instanceof QueryBuilder)
			return ((QueryBuilder)this).makeEdge(label);
		return new SGEdge(label);
	}
	
	/**	Function for QueryBuilder to overload. Ensures that edges of the proper kind
	 *	(QueryEdges for a QueryGraph, SGEdges for SemanticGraph) are created.
	 *	@param label Property label
	 *	@param source Source of edge
	 *	@param target Target of edge
	 *	@return SGEdge
	 *	@throws NameAlreadyBoundException if either node already has an edge with the new edge's ID
	 */
	public SGEdge makeEdge(String label, SGNode source, SGNode target) throws NameAlreadyBoundException
	{
		if (this instanceof QueryBuilder)
			return ((QueryBuilder)this).makeEdge(label);
		return new SGEdge(label, source, target);
	}
}