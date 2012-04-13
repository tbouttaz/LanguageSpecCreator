package WYSIWYM.transformer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameAlreadyBoundException;

import liber.edit.client.AnchorInfo;
import liber.edit.client.InstanceData;
import simplenlg.realiser.AnchorString;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.FeedbackText;
import WYSIWYM.model.Node;
import WYSIWYM.model.SGAbstractNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGDoubleNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGIntNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SGStringNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.model.SummationAnchor;
import WYSIWYM.ontology.Folksonomy;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.util.BadAnchorException;
import WYSIWYM.util.BadDateException;
import WYSIWYM.util.Dotter;
import WYSIWYM.util.FolksonomyException;
import WYSIWYM.util.OntologyInputException;
import WYSIWYM.util.SesameException;
import WYSIWYM.util.SurfaceRealisationException;
import WYSIWYM.util.TextPlanningException;
import WYSIWYM.util.UndoException;
import WYSIWYM.util.UserInfo;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**	FeedbackTextGenerator steers the process of turning the semantic graph into
 *	a feedback text.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 30-10-2006
 *
 *	@version 1.1 20-09-2007
 */
public class FeedbackTextGenerator extends SemanticGraphTransformer
{
	private Folksonomy folk;
	private FeedbackText text;
	protected int lastOp = 0;			//tracks which nodes and edges were added last
	protected SemanticGraphTransformer undoneGraph;
	protected SesameReader sesame;

	/**	Constructs the generator with the given graph, ontology model and Sesame repository.
	 *	@param graph SemanticGraph
	 *	@param r Ontology
	 *	@param s SesameReader
	 */
	public FeedbackTextGenerator(SemanticGraph graph, OntologyReader r, SesameReader s)
	{
		super(graph, r);
		sesame = s;
	}

	/**	Constructs the initial SemanticGraph from a TextFrame, and populates it with anchors.
	 *
	 *	@param	doctype class name
	 *	@param user User name
	 *	@param userID User ID
	 *	@param projectID Project ID, may be null
	 *	@param userInfo SemanticGraphTransformer containing all known information about the user
	 *	@param r	OntologyReader, to read information from the ontology
	 *	@param s SesameReader
	 *	@throws OntologyInputException
	 */
	public FeedbackTextGenerator(String doctype, String user, String userID, String projectID, SemanticGraphTransformer userInfo, 
		OntologyReader r, SesameReader s) throws OntologyInputException//, IOException, SurfaceRealisationException
	{
		super(r);
		setUserID(userID);
		sesame = s;
		folk = new Folksonomy(userID);
		init(doctype, user, projectID, userInfo);
	}
	
	/**	Constructs the initial SemanticGraph from a TextFrame, and populates it with anchors.
	 *
	 *	@param doctype class name
	 *	@param user User name
	 *	@param userID User ID
	 *	@param projectID Project ID, may be null
	 *	@param uri Fedora resource URI
	 *	@param userInfo SemanticGraphTransformer containing all known information about the user
	 *	@param r OntologyReader, to read information from the ontology
	 *	@param s SesameReader
	 *	@throws OntologyInputException
	 */
	public FeedbackTextGenerator(String doctype, String user, String userID, String projectID, String uri, SemanticGraphTransformer userInfo, 
		OntologyReader r, SesameReader s) throws OntologyInputException//, IOException, SurfaceRealisationException
	{
		this(doctype, user, userID, projectID, userInfo, r, s);
		getGraph().setURI(uri);
	}
	
	/**	Constructs the generator and creates its graph with the given user ID.
	 *	@param userID User ID
	 *	@param r Ontology
	 *	@param s SesameReader
	 *	@throws OntologyInputException
	 */
	public FeedbackTextGenerator(String userID, OntologyReader r, SesameReader s) throws OntologyInputException//, IOException, SurfaceRealisationException
	{
		super(r);
		setUserID(userID);
		sesame = s;
		folk = new Folksonomy(userID);
	}
	
	/**	Constructs the generator with the given graphtransformer, ontology model and Sesame repository.
	 *	@param sgt SemanticGraph
	 *	@param r Ontology
	 *	@param s SesameReader
	 *	@param userID User ID
	 */
	public FeedbackTextGenerator(SemanticGraphTransformer sgt, OntologyReader r, SesameReader s, String userID)
	{
		super(sgt, r);
		setUserID(userID);
		sesame = s;
		folk = new Folksonomy(userID);
	}
	
	/**	Constructs the initial SemanticGraph from a TextFrame and the information specified by the user
	 *	in a form, and populates it with anchors.
	 *
	 *	@param data InstanceData with information specified through form
	 *	@param user User name
	 *	@param userID User ID
	 *	@param projectID Project ID, may be null
	 *	@param uri Fedora resource URI
	 *	@param userInfo SemanticGraphTransformer containing all known information about the user
	 *	@param r OntologyReader, to read information from the ontology
	 *	@param s SesameReader
	 *	@throws OntologyInputException
	 */
	public FeedbackTextGenerator(InstanceData data, String user, String userID, String projectID, String uri, SemanticGraphTransformer userInfo, 
		OntologyReader r, SesameReader s) throws OntologyInputException//, IOException, SurfaceRealisationException
	{
		super(r);
		setUserID(userID);
		sesame = s;
		folk = new Folksonomy(userID);
		init(data, user, projectID, userInfo);
		getGraph().setURI(uri);
	}
	
	/**	Initialises the FeedbackTextGenerator; the first time there have 
	 *	to be anchors for as yet unspecified datatype properties. Later 
	 *	the user has to supply a value for every datatype property as soon
	 *	as its generated
	 *
	 *	@param	doctype: the name of the ontological class that the initial object could be
	 *	(currently a 'document'
	 *	@param	OntologyReader, to read information from the ontology
	 *	@throws OntologyInputException, IOException, SurfaceRealisationException
	 */
	private void init(String doctype, String user, String projectID, SemanticGraphTransformer userInfo) throws OntologyInputException//, IOException, SurfaceRealisationException
	{
		boolean query = (this instanceof QueryBuilder);
		TextFrame t = new TextFrame(doctype, user, projectID, userInfo, reader, query);
		setGraph(t.getGraph());
		addAnchors();
		printGraph();
	}
	
	private void init(InstanceData data, String user, String projectID, SemanticGraphTransformer userInfo) throws OntologyInputException
	{
		TextFrame t = new TextFrame(data.getType(), user, projectID, userInfo, reader, false);
		setGraph(t.getGraph());
		
		for (int i = 0; i < data.size(); i++)
		{	//add the information that the user has specified through the form
			if (data.getProperty(i) == null)
				continue;	//not supplied
			
			try
			{
				SGEdge valueEdge = makeEdge(data.getProperty(i));
				valueEdge.setID(getGraph().getFreeID());				//set the ID
				valueEdge.setSequenceNr(lastOp);						//set the sequence nr
				valueEdge.setRealiseNr(0);
			
				SGNode valueNode = new SGStringNode(data.getValue(i));
				valueNode.setFinalNLLabel(data.getValue(i));
				valueEdge.setSource(getGraph().getRoot());
				valueEdge.setTarget(valueNode);
				addEdge(valueEdge);
			}
			catch (NameAlreadyBoundException e)
			{
				e.printStackTrace();
			}
		}
		
		addAnchors();
		printGraph();
	}
	
	/**	Undoes the last operation performed. Throws an UndoException if undo was
	 *	impossible; returns false if doing undo is, after this operation, no
	 *	longer possible.
	 *
	 *	@param error True if an error just occurred, and whatever information was added
	 *	before that should be removed.
	 *	@return True if this was a user-commissioned 'undo' action, and more additions can be undone, 
	 *	so undo should not be disabled. False in all cases.
	 *	@throws UndoException if no information could be removed.
	 */
	public boolean undo(boolean error) throws UndoException
	{
		boolean undid = false;
		while (lastOp > 0)		//cannot undo the generic feedback text
		{
			if (getGraph().hasSequenceComponents(lastOp))
			{
				undoneGraph = new SemanticGraphTransformer(this, reader);	//store a copy of the current graph
				undoneGraph.printGraph("graph-copy.txt");		//and print it
				removeSequence(lastOp, reader);		//then undo the last changes
				lastOp--;							//and adjust the last operation counter
				undid = true;
				addAnchors();	//update the anchors
				break;
			}
			else if (error)
				return false;
			else
				lastOp--;
		}
		if (!undid)
			throw new UndoException("No undo was possible");
			
		while (true)
		{
			if (lastOp == 0)
				return false;
			if (getGraph().hasSequenceComponents(lastOp))
				return true;
			lastOp--;
		}
	}
	
	/**	Undoes the last operation performed. Throws an UndoException if undo was
	 *	impossible; returns false if doing undo is, after this operation, no
	 *	longer possible.
	 *
	 *	@return True if this was a user-commissioned 'undo' action, and more additions can be undone, 
	 *	so undo should not be disabled. False in all cases.
	 *	@throws UndoException if no information could be removed.
	 */
	public boolean undo() throws UndoException
	{
		return undo(false);
	}
	
	/**	Redo; replaces the current Graph with the Graph that was saved before
	 *	the last 'undo' action. There is only a single redo!
	 *	@return false if there was no saved graph, true if the operation succeeded
	 */
	public boolean redo()
	{
		if (undoneGraph == null)
			return false;
		setGraph(undoneGraph.getGraph());	//restore the previous graph
		lastOp = getGraph().getLargestSequenceNr();
		getGraph().stopFlashing();
		undoneGraph = null;
		printGraph("redone-graph.txt");
		
		Runtime r = Runtime.getRuntime();
		r.gc();			//FORCES GARBAGE COLLECTION
		r.runFinalization();
		
		return true;
	}
	
	/**	Removes the node with the given anchor from the Graph.
	 *	All edges of the node are removed as well, and any nodes that
	 *	because of this have no connection to the root node anymore.
	 *	The results of this operation may be further reaching than the
	 *	user intends, which is why we have implemented 'redo'!
	 *	@param anchor Unique ID of Anchor
	 */
	public void removeAnchor(String anchor)
	{
		undoneGraph = new SemanticGraphTransformer(this, reader);
		Anchor a = text.getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode source = (SGNode) getGraph().getNode(id);
		removeAnchor(source, true);
		removeNode(source);	
		addAnchors();		//add all anchors anew, as much information MAY have been removed
	}
	
	/**	Removes all children (and grandchildren etc) of the given node that have
	 *	no other incoming edges, and is not the root of the tree
	 * (this might result in removing entire chains, but
	 *	I can't see any way to prevent that without forcing users to remove each
	 *	property separately). Basically it removes all parts of the tree that, through
	 *	the removal of some node or edge, become disconnected from the root node.
	 *
	 *	if 'remove' is true, this root has to be removed no matter its outgoing edges
	 * (because the user selected this one for removal)
	 *
	 *	Returns true if the given node can be removed itself, false if it shouldn't.
	 */
	private boolean removeAnchor(Node node, boolean remove)
	{
		Node root = getGraph().getRoot();
		List<Node> removable = new ArrayList<Node>();
		int outNr = node.getOutgoingEdgeNr();
		if (outNr == 0)		//if a node has no outgoing edges, it is removable
			return true;	
		
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			Node target = ((Edge) it.next()).getTarget();
			if ((!target.equals(root)) && (target.getIncomingEdgeNr() == 1))
			{
				if (removeAnchor(target, false))		//recurse
					removable.add(target);	
			}
		}
		
		if ((!remove) && (outNr > removable.size()))	//not all children can be removed; that means\
			return false;				//this node should not be removed nor any of its children
						//unless the user expressly stated that this one has to be removed
		for (int i = 0; i < removable.size(); i++)		//else, start removing!
			removeNode(removable.get(i));
		return true;
	}
	
	/**	Removes the specified values that the node with this anchor has for this property.
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values String[] array with values to be removed
	 */
	public void removePropertyValues(String anchor, String property, String[] values)
	{
		undoneGraph = new SemanticGraphTransformer(this, reader);
		Anchor a = text.getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode node = (SGNode) getGraph().getNode(id);
		
		List<Edge> edgeList = node.getOutgoingEdges(property);
		if (edgeList.size() > values.length)	//only remove some values
		{
			for (int i = 0; i < edgeList.size(); i++)
			{
				SGEdge edge = (SGEdge) edgeList.get(i);
				String value = edge.getTarget().getChoiceLabel(this instanceof QueryBuilder, reader);
				int idx = -1;
				for (int j = 0; j < values.length; j++)
				{	//check if this edge should be removed
					if (value.equals(values[j]))
					{
						idx = j;
						break;
					}
				}
				if (idx >= 0)
					removePropertyValue(edge);
			}
		}
		else //user chose to remove all values
			for (int i = 0; i < edgeList.size(); i++)
				removePropertyValue((SGEdge) edgeList.get(i));
		
		addAnchors();	//re-compute all anchors, as many things may have been removed
	}
	
	protected void removePropertyValue(SGEdge edge)
	{
		SGNode target = edge.getTarget();
		if (target.getIncomingEdgeNr() == 1) 
		{//remove all parts of the tree that become disconnected if you remove this edge
			if (removeAnchor(target, false))	
				removeNode(target);
		}
		removeEdge(edge);
	}
	
	/**	Adds anchors to the initial graph
	 */
	protected void addAnchors()
	{
		Iterator nodes = getGraph().getNodes();
		while (nodes.hasNext())
		{
			SGNode node = (SGNode) nodes.next();
			updateAnchor(node);
		}
	}
	
	/**	Updates this node's anchor, as its menu items
	 *	may change when the user adds information.
	 *	@param node SGNode
	 */
	public void updateAnchor(SGNode node)
	{
		try	//check if the target is an anchor
		{
			OntClass c = reader.getClass(node);
			if (c != null)
			{
				Anchor a = new Anchor(reader, c, node, false, userID);
				if ((node.mustRealise() < 1) || (node.mustRealise() > 2))
					return;	//if the node is new, hidden or incomplete, we are done

				if (userID.equals(node.getUniqueID()))	//if this is the user,
					a.init(c, false);	//make sure he/she can describe it!	
				else
				{	//if this node describes a resource deposited by the user, he can add to its description too
					for (Edge edge : node.getOutgoingEdges("DepositedBy"))
					{
						if (userID.equals(((SGNode) edge.getTarget()).getUniqueID()))
						{
							a.init(c, false);
							break;
						}
					}
					for (Edge edge : node.getIncomingEdges("DepositorOf"))
					{
						if (userID.equals(((SGNode) edge.getSource()).getUniqueID()))
						{
							a.init(c, false);
							break;
						}
					}
				}
			}		//other existing nodes get a curtailed anchor
		}
		catch (BadAnchorException e)
		{
			System.out.println(e.getMessage());
			node.removeAnchor();
		}	
	}
	
	/**	Updates the feedbacktext when the semantic graph has been changed
	 *	@throws	SurfaceRealisationException	if an error occurred during surface realisation
	 *	@throws	TextPlanningException if an error occurred during text planning
	 */
	public void updateText() throws SurfaceRealisationException, TextPlanningException
	{	//plan text
		ContentPlanner planner = new ContentPlanner(reader, getGraph());
		ContentPlan cp = planner.plan();
	/*	Iterator it = cp.getTrees();						
		int i = 0;			//puts dependency trees in files, to view with GraphViz
		while (it.hasNext())
		{
			String str = Integer.toString(i);
			i++;
			FileWriter fw = new FileWriter(str + ".txt");
			PrintWriter w = new PrintWriter(fw);
			Dotter d = new Dotter();
			w.print(d.dotDependencyTree(((DependencyTreeTransformer) it.next()).getGraph()));		
			w.close();
			fw.close();
		}*/
		
		SurfaceRealiser sr = new SurfaceRealiser();
		text = new FeedbackText(sr.realise(cp));	//planner.getPlan()));
	}
	
	/**	Updates the feedbacktext when the user has added a text value for a property with
	 *	maximum	cardinality 1.
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param	value The value the user specified for the range (can be null)
	 *	@throws FolksonomyException if the tag could not be stored
	 */
	public void update(String anchor, String property, String value)  throws FolksonomyException
	{
		Anchor a = text.getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode oldNode = (SGNode) getGraph().getNode(id);
	
		OntProperty p = reader.getProperty(property);		//property must be a property
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);
		if (oldEdges.size() > 0)	//if a value was added before, replace it!
		{
			SGStringNode target = (SGStringNode) oldEdges.get(0).getTarget();
			target.setLabel(value);
			target.setFinalNLLabel(value);
			target.setFlash(true);
		}
		else
		{
			SGStringNode newNode = new SGStringNode(value);
			newNode.setFinalNLLabel(value);	//make sure the label can't be changed anymore (because otherwise the textplanner will tack 'this' on),	
		
			try
			{	//create an edge,
				SGEdge edge = makeEdge(property);	
				edge.setID(getGraph().getFreeID());
				edge.setSource(oldNode);
				edge.setTarget(newNode);
				edge.setSequenceNr(lastOp);
				if (reader.useAsProperName(p))	//SGEdge.isNLName(property))	//names are indirectly realised; if they are
					oldNode.setFlash(true);		//flash when new, we need to tell the source node
				addEdge(edge);	//add edge and nodes to graph
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 299: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		
		updateAnchor(oldNode);
		if (reader.useAsProperName(p))
		{	//nl realisation of this node has changed; so the anchors of any nodes that have edges to this one
			for (Iterator it = oldNode.getIncomingEdges(); it.hasNext(); )	//have to be updated concerning the presentation of removable items
			{
				SGEdge e = (SGEdge) it.next();
				updateAnchor(e.getSource());
			}
		}
				
		folk.store(p, value, oldNode.getUniqueID());
		printGraph();
	}
	
	/**	Updates the feedbacktext when the user has added Number values for a property.
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param	nr The value the user specified (an Integer or Double)
	 *	@throws IOException if nr was the wrong type for this property
	 */
	public void numberUpdate(String anchor, String property, Number nr) throws IOException
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);
		if (oldEdges.size() > 0)	//if a value was added before, replace it!
		{
			SGNode target = (SGNode) oldEdges.get(0).getTarget();
			if (nr instanceof Double)
				((SGDoubleNode)target).setValue((Double) nr);
			else
				((SGIntNode)target).setValue((Integer) nr);
			target.setSequenceNr(lastOp);
			target.setFlash(true);				//realisation of node has changed, so mark it
		}
		else
		{	//In all other cases: create a new node,
			SGNode newNode;
			if (nr instanceof Double)
				newNode = new SGDoubleNode((Double) nr);
			else
				newNode = new SGIntNode((Integer) nr);

			try
			{	//create an edge,
				SGEdge edge = makeEdge(property);	
				edge.setID(getGraph().getFreeID());
				edge.setSource(oldNode);
				edge.setTarget(newNode);
				edge.setSequenceNr(lastOp);
				addEdge(edge);	//add edge and nodes to graph
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 346: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		updateAnchor(oldNode);
		printGraph();
	}
	
	/**	Updates the feedbacktext when the user has added a boolean value for a property.
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param	b The value the user specified (a boolean)
	 *	@throws IOException if b was the wrong type for this property
	 */
	public void booleanUpdate(String anchor, String property, boolean b) throws IOException
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		
		OntProperty p = reader.getProperty(property);//, oldNode.getLabel(), null);		//property must be a property
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);
		if (oldEdges.size() > 0)	//if a value was added before, replace it!
		{
			SGBooleanNode target = (SGBooleanNode) oldEdges.get(0).getTarget();
			target.setValue(new Boolean(b));
			target.setSequenceNr(lastOp);
			target.setFlash(true);				//realisation of node has changed, so mark it
		}
		else
		{	//In all other cases: create a new node,	
			SGNode newNode = new SGBooleanNode(b);
			try
			{//create an edge,
				SGEdge edge = makeEdge(property);	
				edge.setID(getGraph().getFreeID());
				edge.setSource(oldNode);
				edge.setTarget(newNode);
				edge.setSequenceNr(lastOp);
				addEdge(edge);	//add edge and nodes to graph
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 391: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		updateAnchor(oldNode);
		printGraph();
	}
	
	/**	Updates a 'date' property, by creating a special 'vague date' node.
	 *	Instead of using any of the RDF-datatypes that model dates or times,
	 *	LIBER uses the Date class defined in the PolicyGrid Utility ontologies
	 *	to store social-science dates. These dates can be specific to the day,
	 *	or as vague as a century.
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param  input Contains the information needed to construct a date
	 *	@throws BadDateException if the input was malformed
	 */
	public void updateDate(String anchor, String property, String[] input) throws BadDateException
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		OntProperty p = reader.getProperty(property);
		
		SGDateNode newNode = new SGDateNode(input);
		try
		{
			SGEdge edge = makeEdge(property);
			edge.setID(getGraph().getFreeID());
			edge.setRealiseNr(oldNode.getOutgoingEdges(property).size());	
			edge.setSource(oldNode);
			edge.setTarget(newNode);
			edge.setSequenceNr(lastOp);			
			addEdge(edge);	//add edge and nodes to graph
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("FTGENERATOR 445: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
			e.printStackTrace();
		}	
		updateAnchor(oldNode);
		printGraph();	
	}
	
	/**	Updates the feedbacktext when the user has added an abstract for a property.
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param	value The String value the user specified (an abstract)
	 */
	public void updateAbstract(String anchor, String property, String value)
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		OntProperty p = reader.getProperty(property);
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);
		if (oldEdges.size() > 0)
		{
			SGAbstractNode target = (SGAbstractNode) oldEdges.get(0).getTarget();
			target.setLabel(value);
			target.setSequenceNr(lastOp);
			target.setFlash(true);
		}
		else
		{
			SGAbstractNode newNode = new SGAbstractNode(value);
			try
			{
				SGEdge edge = makeEdge(property);
				edge.setID(getGraph().getFreeID());
				edge.setRealiseNr(oldNode.getOutgoingEdges(property).size());
				edge.setSource(oldNode);
				edge.setTarget(newNode);
				edge.setSequenceNr(lastOp);
				addEdge(edge);
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 445: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		updateAnchor(oldNode);
		printGraph();
	}

	/**	Adds multiple values for one property in one go. Only for datatype properties.
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param	values The String values the user specified
	 *	@throws IOException if a value was the wrong type for this property
	 *	@throws FolksonomyException if the tag could not be stored
	 */
	public int multipleValuesUpdate(String anchor, String property, String[] values, int datatype) throws IOException, FolksonomyException
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		OntProperty p = reader.getProperty(property);//, oldNode.getLabel(), null);

		if (datatype == 0)
			folk.store(p, values, oldNode.getUniqueID());
		
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);	//get all existing edges; they may need to be changed!
		
		for (int i = 0; i < values.length; i++)	
		{	
			Object value = values[i];	//get the string value
			if (datatype == 1)	//value is an integer
				value = new Integer(values[i]);
			else if (datatype == 2)	//value is a double
				value = new Double(values[i]);
		
			if (oldEdges.size() > i)
			{	//if there is a previously added value, set the new value in that node's target
				SGNode target = (SGNode) oldEdges.get(i).getTarget();
				if (!target.getNLLabel(reader).equals(values[i]))
					target.setSequenceNr(lastOp);	//node changes, record this to enable 'undo'			
				((DatatypeNode) target).setValue(value);
				continue;
			}
			
			SGNode newNode;		//else, create a new node and edge
			if (datatype == 1)	//values are integers
				newNode = new SGIntNode(value);
			else if (datatype == 2) //values are doubles
				newNode = new SGDoubleNode(value);
			else				//values are strings
			 	newNode = new SGStringNode(value);

			try
			{
				SGEdge edge = makeEdge(property);
				edge.setID(getGraph().getFreeID());
				edge.setRealiseNr(oldNode.getOutgoingEdges(property).size());
				edge.setSource(oldNode);
				edge.setTarget(newNode);
				edge.setSequenceNr(lastOp);
				addEdge(edge);	//add edge and nodes to graph
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 523: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		
		if (oldEdges.size() > values.length)	//the user must have deleted some values
		{	//so remove the extra, obsolete values
			for (int i = values.length; i < oldEdges.size(); i++)
			{
				Edge edge = oldEdges.get(i);
				oldNode.removeOutgoingEdge(edge);
				removeNode(edge.getTarget());
			}
		}
		
		updateAnchor(oldNode);
		printGraph();
		return values.length;
	}
	
	/**	Creates one or more new datatype properties with restricted values (e.g.
	 *	access can be private, public or limited)
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@param	values List with String values the user specified
	 *	@throws IOException if b was the wrong type for this property
	 *	@throws FolksonomyException if the tag could not be stored
	 */
	public void multipleUpdate(String anchor, String property, List values) throws FolksonomyException, IOException
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		OntProperty p = reader.getProperty(property);
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);	//get all existing edges; they may need to be changed!
							
		for (int i = 0; i < values.size(); i++)
		{
			if (oldEdges.size() > i)
			{	//if there is a previously added value, set the new value in that node's target
				SGStringNode target = (SGStringNode) oldEdges.get(i).getTarget();
				if (!target.getNLLabel(reader).equals(values.get(i)))
					target.setSequenceNr(lastOp);	//node changes, record this to enable 'undo'			
				target.setValue(values.get(i));
				continue;
			}
			
			try
			{//create new nodes and edges
				SGNode newNode = new SGStringNode((String) values.get(i));
				newNode.setFinalNLLabel((String) values.get(i));	//make sure the label can't be changed anymore (because otherwise the textplanner will tack 'this' on),
				SGEdge edge = makeEdge(property);
				edge.setID(getGraph().getFreeID());
				edge.setRealiseNr(oldNode.getOutgoingEdges(property).size());	
				edge.setSource(oldNode);
				edge.setTarget(newNode);
				edge.setSequenceNr(lastOp);
				addEdge(edge);	//add edge and nodes to graph
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 639: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		
		if (oldEdges.size() > values.size())	//the user must have deleted some values
		{	//so remove the extra, obsolete values
			for (int i = values.size(); i < oldEdges.size(); i++)
			{
				Edge edge = oldEdges.get(i);
				oldNode.removeOutgoingEdge(edge);
				removeNode(edge.getTarget());
			}
		}
		
		folk.store(p, values, oldNode.getUniqueID());
		updateAnchor(oldNode);
		printGraph();
	}

	/**	Updates the SemanticGraph with an object property to a new instance, so that 
	 *	an edge and a new non-terminal node (+ unique id) are created.
	 *
	 *	@param	property The label of the selected property
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	instance InstanceData with data specified about the individual in a form
	 *	@param seqNr Sequence number; denotes the sequence of this edge in a series of identical edges. 
	 *	For instance, it might denote the second author of a paper.
	 */
	public void updateObjectPropWithNewInstance(String property, String anchor, InstanceData instance, int seqNr)
	{
		String type = instance.getType();
		Anchor a = text.getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode source = (SGNode) getGraph().getNode(id);
		SGNode target = new SGNode(type);
		if (type.equals("Address"))
			target = new SGAddressNode(type);
		OntProperty p = reader.getProperty(property);
		
		try
		{
			SGEdge edge = makeEdge(property);				//create an edge
			edge.setID(getGraph().getFreeID());				//set the ID
			edge.setSequenceNr(lastOp);						//set the sequence nr
			edge.setRealiseNr(seqNr);
			edge.setSource(source);
			edge.setTarget(target);
			addEdge(edge);
			
			//add the information that the user has specified through the form
			for (int i = 0; i < instance.size(); i++)
			{
				if (instance.getProperty(i) == null)
					continue;	//not supplied
				SGEdge valueEdge = makeEdge(instance.getProperty(i));
				valueEdge.setID(getGraph().getFreeID());				//set the ID
				valueEdge.setSequenceNr(lastOp);						//set the sequence nr
				valueEdge.setRealiseNr(0);
				
				SGNode valueNode = new SGStringNode(instance.getValue(i));
				valueNode.setFinalNLLabel(instance.getValue(i));
				valueEdge.setSource(target);
				valueEdge.setTarget(valueNode);
				addEdge(valueEdge);
			}
		}					//add edge and nodes to graph
		catch (NameAlreadyBoundException ex)
		{
			System.out.println("FTGENERATOR 168: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
			ex.printStackTrace();
		}
		
		updateAnchor(target);
		updateAnchor(source);
		printGraph();			
	}
	
	/**	Updates the SemanticGraph with an object property to an instance already in it.
	 *
	 *	@param	property The label of the selected property
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	target The target node
	 *	@param seqNr Sequence number; denotes the sequence of this edge in a series of identical edges. 
	 *	For instance, it might denote the second author of a paper.
	 */
	public void updateObjectPropWithExistingInstance(String property, String anchor, SGNode target, int seqNr)
	{
		Anchor a = text.getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode source = (SGNode) getGraph().getNode(id);		
		OntProperty p = reader.getProperty(property);

		try
		{
			SGEdge edge = makeEdge(property);				//create an edge
			edge.setID(getGraph().getFreeID());				//set the ID
			edge.setSequenceNr(lastOp);						//set the sequence nr
			edge.setRealiseNr(seqNr); 
			edge.setSource(source);
			edge.setTarget(target);
			addEdge(edge);		
		}					//add edge and nodes to graph
		catch (NameAlreadyBoundException ex)
		{
			System.out.println("FTGENERATOR 168: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
			ex.printStackTrace();
		}
		
		updateAnchor(target);
		updateAnchor(source);
		printGraph();
	}
	
	/**	Updates the SemanticGraph with an object property to an object from the archive, 
	 *	extracting the NL-information about that object from the archive.
	 *
	 *	@param	property The label of the selected property
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	id Sesame ID of the individual
	 *	@param seqNr Sequence number; denotes the sequence of this edge in a series of identical edges. 
	 *	For instance, it might denote the second author of a paper.
	 *	@throws SesameException if there is an error retrieving information from the archive
	 */
	public void updateObjectPropWithArchiveInstance(String property, String anchor, String id, int seqNr) throws SesameException
	{	//get nl-information about this node and add it to the graph
		AutomaticGenerator aut = new AutomaticGenerator(reader, sesame);
		SGNode node = aut.getNLInformation(this, id);
		//then add the object property
		updateObjectPropWithExistingInstance(property, anchor, node, seqNr);
		addAnchors();
	}
	
	/**	Adjusts the sequence number of the edge attaching the target node
	 *	with the property to the node corresponding to the anchor.
	 *
	 *	@param	property The label of the selected property
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	target Target node
	 *	@param seqNr Sequence number; denotes the sequence of this edge in a series of identical edges. 
	 *	For instance, it might denote the second author of a paper.
	 */
	public void updateObjectPropWithRangeObject(String property, String anchor, SGNode target, int seqNr)
	{
		SGEdge edge = getEdge(property, anchor, target);
		if (edge != null)
			edge.setRealiseNr(seqNr);
	}
	
	/**	Removes the edge with the given property from the node with the anchor to the target node.
	 *
	 *	@param	property The label of the selected property
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	target Target node
	 */
	public void removeObjectFromPropRange(String property, String anchor, SGNode target)
	{
		SGEdge edge = getEdge(property, anchor, target);
		if (edge != null)
			removePropertyValue(edge);
	}
	
	private SGEdge getEdge(String property, String anchor, SGNode target)
	{
		Anchor a = text.getAnchor(anchor);
		String id = a.getNode().getID();
		SGEdge edge = null;
		
		for (Iterator it = target.getIncomingEdges(property).iterator(); it.hasNext(); )
		{
			SGEdge e = (SGEdge) it.next();
			if (e.getSource().getID().equals(id))
			{
				edge = e;
				break;
			}
		}
		if (edge == null)
		{
			String inverse = reader.getInverse(property);//, a.getNode().getLabel(), target.getLabel());
			if (inverse != null)
			{
				for (Iterator it = target.getOutgoingEdges(inverse).iterator(); it.hasNext(); )
				{
					SGEdge e = (SGEdge) it.next();
					if (e.getTarget().getID().equals(id))
					{
						edge = e;
						break;
					}
				}
			}
		}
		return edge;		
	}
	
	/**	Shows or hides all known information about the given anchor's object.
	 *	@param	anchor The unique ID of an Anchor
	 *	@param 	show True if information should be shown, false if it should be hidden
	 *	@return false if the archive contained no more information about this node, otherwise true
	 */
	public boolean changeTextContent(String anchor, boolean show) throws SesameException
	{
		Anchor a = text.getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		if (show)
		{
			if (oldNode.mustRealise() == SGNode.INCOMPLETE)
			{
				new AutomaticGenerator(reader, sesame).getInformation(oldNode, this);
				addAnchors();	//update existing anchors and create them for the new nodes
				if (oldNode.mustRealise() == SGNode.NOINFO)
					return false;	//no info could be found about this node
			}
			else
				oldNode.setRealise(SGNode.SHOW);
		}
		else
			oldNode.setRealise(SGNode.HIDE);
		
		updateAnchor(oldNode);		//update the anchor; now that information is showing or hidden, it may change!
		printGraph();
		return true;
	}
	
	/**	Shows all edges summarised by the anchor's summation node separately.
	 *	@param	anchor The unique ID of an Anchor
	 */
	public void showSummation(String anchor)
	{
		SummationAnchor a = (SummationAnchor) text.getAnchor(anchor);
		a.setVisible();
	}
	
	/**	Returns the node that has the Anchor with this unique ID.
	 *	@param	anchor The unique ID of an Anchor
	 *	@return SGNode with this Anchor
	 */
	public SGNode getNodeWithAnchor(String anchor)
	{
		Anchor a = text.getAnchor(anchor);
		return (SGNode) getGraph().getNode(a.getNode().getID());
	}
	
	/**	Imports the node from the given text generator into this one, together
	 *	with all its information. Used in previous version of LIBER to export
	 *	nodes from the browsing module to the editing module.
	 *
	 *	@param ftg FeedbackTextGenerator containing the node
	 *	@param anchor	The unique ID of an Anchor
	 *	@throws NameAlreadyBoundException
	 *	@throws SesameException
	 *	@deprecated
	 */
	public void add(FeedbackTextGenerator ftg, String anchor) throws NameAlreadyBoundException, SesameException
	{
		SGNode node = ftg.getNodeWithAnchor(anchor);	//get the node
		if (getGraph().hasUniqueIDNode(node.getUniqueID()))
			return;			//if it is already in the graph, return

		SGNode copy = SGNode.copyNode(node, new HashMap<String,SGNode>(), this);	//else add a copy to this graph
		copy.setSGID(getGraph().getFreeID());
		addNode(copy, true);	//add the node to the graph
		addAnchors();	//update existing anchors and create them for the new nodes
		if (copy.mustRealise() != SGNode.INCOMPLETE)
			copy.setRealise(SGNode.HIDE);	//don't show all information associated with this node immediately
	}

	/**	Takes a string and separates the different values the user specified 
	 *	(using the line breaks)	
	 *
	 *	@param	input Values specified by user
	 *	@return	List<String>, containing the separated values
	 */
	private List<String> separate(String input)
	{
		ArrayList<String> result = new ArrayList<String> ();
		StringBuffer sb = new StringBuffer(input);
		int idx;
		while ((idx = sb.indexOf("\n")) > -1)
		{
			String str = sb.substring(0, idx);
			if (str.length() > 0)
				result.add(str);
			sb.delete(0, idx + 1);
		}
		return result;
	}
	
	/**	Returns the maximum number of times this property can still be instantiated 
	 *	in this anchor without violating a cardinality restriction, or 0 if there is no restriction.
	 *
	 *	@param	anchor The unique ID of an Anchor
	 *	@param	property The label of the selected property
	 *	@return Integer
	 */
	public Integer getMax(String anchor, String property)
	{
		Anchor a = getText().getAnchor(anchor);
		SGNode n = (SGNode) a.getNode();
		//if (a.isDataType())
		//	n = (SGNode) n.getParents().next();		//if this is a pre-generated node, we need its parent

		int maxCard = reader.getMaxCardinality(n.getLabel(), property);
		if (maxCard == 0)	//no maximum cardinality restriction
			return 0;
		
		int existing = n.getOutgoingEdges(property).size();
		if ((existing > 0) && (/**a.isDataType() ||*/ reader.useAsProperName(property))) //SGEdge.isNLName(property)))	//property is a name property 												//that has been instantiated automatically
			existing--;		//edge exists but node has no value yet
		return new Integer(maxCard - existing);
	}
	
	/**	Retrieves the values already added for this property and anchor
	 *	@param	property name
	 *	@param	anchor The unique ID of an Anchor
	 *	@return	String array with values already added for this property and anchor
	 */
	public String[] getAddedValues(String property, String anchor)
	{
		OntProperty p = reader.getProperty(property);
		if (p == null)
			return null;
			
		SGNode node = getNodeWithAnchor(anchor);	//get all edges of this property
		List<Edge> list = node.getOutgoingEdges(property);	
		String[] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++)		//add the values of their target nodes to result
			result[i] = (((SGNode) list.get(i).getTarget()).getNLLabel(reader));
		return result;
	}

	/**	Returns the feedbacktext
	 *	@return FeedbackText
	 */
	public FeedbackText getText()
	{
		return text;
	}
	
	/**	Sets the feedbacktext
	 *	@param t FeedbackText
	 */
	public void setText(FeedbackText t)
	{
		text = t;
	}
	
	/**	Returns the serialisable surface form of the FeedbackText as an array of AnchorInfo
	 *	@return AnchorInfo[]
	 *	@throws SurfaceRealisationException if an error occurred during surface realisation
	 *	@throws TextPlanningException if an error occurred during text planning
	 */
	public AnchorInfo[] getSurfaceText() throws SurfaceRealisationException, TextPlanningException
	{
		return getSurfaceText(true);
	}
	
	/**	Returns the serialisable surface form of the FeedbackText as an array of AnchorInfo
	 *	@param undo Set to false if no (more) undo is possible now; the last item in the array will be set to null,
	 *	and this information causes the interface to disable the undo button.
	 *	@return AnchorInfo[]
	 *	@throws SurfaceRealisationException if an error occurred during surface realisation
	 *	@throws TextPlanningException if an error occurred during text planning
	 */
	public AnchorInfo[] getSurfaceText(boolean undo) throws SurfaceRealisationException, TextPlanningException
	{
		updateText();
		int size = text.size();
		if (!undo)
			size++;
			
		AnchorInfo[] result = new AnchorInfo[size];
		for (int i = 0; i < text.size(); i++)
			result[i] = text.next().toAnchorInfo();			
		if (!undo)
			result[size - 1] = null;
		
		Runtime r = Runtime.getRuntime();
		r.gc();			//FORCES GARBAGE COLLECTION
		r.runFinalization();		
		return result;
	}
	
	/**	Returns a serialisable version of the given Feedbacktext
	 *	@param text FeedbackText
	 *	@return AnchorInfo[]
	 */
	public static AnchorInfo[] getSurfaceText(FeedbackText text)
	{
		AnchorInfo[] result = new AnchorInfo[text.size()];
		for (int i = 0; i < text.size(); i++)
			result[i] = text.next().toAnchorInfo();			
		return result;
	}
	
	/**	Increments the last operation counter by one. When edges and nodes are added,
	 *	they receive an operation value of this counter. This information is used by undo
	 *	to find the edges and nodes that were added last. If the counter is set to 0, 
	 *	no more undo operations are possible.
	 */
	public void incrementLastOp()
	{
		lastOp++;
	}
	
	/**	Prints the SemanticGraph to a text file with the given name
	 *	@param name Filename
	 *	@param graph SemanticGraph
	 */
	public static void printGraph(String name, SemanticGraph graph)
	{
		try
		{
			FileWriter fw2 = null;
			fw2 = new FileWriter(name + ".txt");
			PrintWriter w2 = new PrintWriter(fw2);
			Dotter d = new Dotter();
			w2.print(d.dotSGGraph(graph));
			w2.close();
			fw2.close();
		}
		catch(IOException e)
		{
			System.out.println("Failed to print graph to " + name);
		}
	}
	
	/**	Prints information about the user's behaviour. Only used during evaluation.
	 *	@param user User name
	 *	@param ui Information about user behaviour
	 *	@throws IOException if the file could not be created/opened/written to
	 *	@deprecated
	 */
	public static void printUserInfo(String user, UserInfo ui) throws IOException
	{
		FileWriter fw = null;
		try
		{
			fw = new FileWriter(user + Integer.toString(ui.sessionNr) + "FINAL.txt");
		}
		catch (IOException e)
		{	//Just in case the filename is impossible, this guarantees the user data get stored somewhere
			System.out.println("ERROR OPENING FILE TO STORE USER DATA; USING 'XXX' INSTEAD.");
			fw = new FileWriter("xxx" + Integer.toString(ui.sessionNr) + "FINAL.txt");
		}
		PrintWriter w = new PrintWriter(fw);
		w.print(user);
		ui.print(w);
		w.close();
		fw.close();
	}
	
	/**	Prints the SemanticGraph and information about the user's behaviour. 
	 *	Only used during evaluation.
	 *	@param user User name
	 *	@param ui Information about user behaviour
	 *	@throws IOException if the file could not be created/opened/written to
	 *	@deprecated
	 */
	public void printGraph(String user, UserInfo ui) throws IOException
	{
		FileWriter fw = null;
		try
		{
			fw = new FileWriter(user + Integer.toString(ui.sessionNr) + ".txt");
		}
		catch (IOException e)
		{	//Just in case the filename is impossible, this guarantees the user data get stored somewhere
			System.out.println("ERROR OPENING FILE TO STORE USER DATA; USING 'XXX' INSTEAD.");
			fw = new FileWriter("xxx" + Integer.toString(ui.sessionNr) + ".txt");
		}
		PrintWriter w = new PrintWriter(fw);
		w.print(user);
		ui.print(w);
		w.println();
		List<AnchorString> l = text.getText();
		for (int i = 0; i < l.size(); i++)
			w.print(l.get(i).toString());	
		w.close();
		fw.close();
		
		FileWriter fw2 = null;
		try
		{
			fw2 = new FileWriter(user + Integer.toString(ui.sessionNr) + "SG.txt");
		}
		catch (IOException e)
		{	//Just in case the filename is impossible, this guarantees the user data get stored somewhere
			fw2 = new FileWriter("xxx" + Integer.toString(ui.sessionNr) + "SG.txt");
		}
		
		PrintWriter w2 = new PrintWriter(fw2);
		Dotter d = new Dotter();
		w2.print(d.dotSGGraph((SemanticGraph) getGraph()));
		w2.close();
		fw2.close();
	}
}