package WYSIWYM.transformer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import liber.edit.client.InstanceData;
import liber.edit.client.QueryDateValue;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.FeedbackText;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.QueryValue;
import WYSIWYM.model.QueryValueNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGDoubleNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGIntNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SGStringNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.OntologyWriter;
import WYSIWYM.ontology.QueryResult;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.util.BadAnchorException;
import WYSIWYM.util.BadDateException;
import WYSIWYM.util.Dotter;
import WYSIWYM.util.OntologyInputException;
import WYSIWYM.util.SPARQLException;
import WYSIWYM.util.SesameException;
import WYSIWYM.util.SurfaceRealisationException;
import WYSIWYM.util.TextPlanningException;
import WYSIWYM.util.UserInfo;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**	QueryBuilder steers the process of building a query; a feedbacktextgenerator
 *	for the querying module.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 17-07-2007
 *
 *	@version 1.2 31-01-2008
 *
 *	@version 1.5 21-08-2008
 */
public class QueryBuilder extends FeedbackTextGenerator
{
	private Map<String, SemanticGraphTransformer> queryResultMap = new HashMap<String, SemanticGraphTransformer>();
	private List<QueryEdge> plannedEdgeOrder;
	private String sparql;
	
	/*	Constructs the initial QueryGraph with the resource type, and populates it with anchors.
	 *
	 *	@param resource class type
	 *	@param user User name
	 *	@param userID User ID
	 *	@param userInfo SemanticGraphTransformer containing all known information about the user
	 *	@param reader Ontology
	 *	@param s SesameReader
	 *	@throws OntologyInputException
	 *
	public QueryBuilder(String resource, String user, String userID, SemanticGraphTransformer userInfo, OntologyReader reader, 
		SesameReader s) throws OntologyInputException//, IOException, SurfaceRealisationException
	{
		super(resource, user, userID, null, userInfo, reader, s);
	}*/
	
	/**	Constructs the initial QueryGraph with the resource type, and populates it with anchors.
	 *
	 *	@param resource class type
	 *	@param user User name
	 *	@param userID User ID
	 *	@param reader Ontology
	 *	@param s SesameReader
	 *	@throws OntologyInputException
	 *	@throws IOException
	 */
	public QueryBuilder(String resource, String user, String userID, OntologyReader reader, SesameReader s) throws OntologyInputException, IOException, SurfaceRealisationException
	{
		super(userID, reader, s);
		init(resource, user);
	}
	
	private void init(String doctype, String user) throws OntologyInputException, IOException, SurfaceRealisationException
	{
		TextFrame t = new TextFrame(doctype, user, reader);
		setGraph(t.getGraph());
		addAnchors();
		printGraph();
	}	
	
	/**	Generates the SPARQL realisation of the graph, queries Sesame and 
	 *	returns a SemanticGraph transformer containing all matches of the query, 
	 *	plus the information about them that was asked for.
	 *
	 *	@return QueryResultGenerator, which generates descriptions of the search results
	 *	@throws SPARQLException
	 *	@throws SesameException
	 *	@throws IOException
	 */
	public QueryResultGenerator getResult() throws SPARQLException, SesameException, IOException
	{
		OntologyWriter writer = new OntologyWriter(reader);
		sparql = writer.getSPARQL(getGraph());
		QueryResult hits = sesame.queryBinding(sparql);
		if (hits.size() == 0)
			return null;
		AutomaticGenerator gen = new AutomaticGenerator(reader, sesame);
		QueryResultGenerator result = gen.getQueryResultDescription(hits, getGraph());
		result.setUserID(userID);	//remember who the user is, to check access rights
		return result;
	}

	/**	Generates the SPARQL realisation of the graph, queries Sesame and returns a
	 *	HashMap with the result's Sesame IDs and FeedbackText descriptions.
	 *	@return Map<String,FeedbackText>
	 *	@throws SPARQLException
	 *	@throws SesameException
	 *	@throws IOException
	 *	@throws SurfacdeRealisationException
	 *	@throws TextPlanningException
	 */	
	public Map<String,FeedbackText> getSparql() throws SPARQLException, SesameException, IOException, SurfaceRealisationException, TextPlanningException
	{
		OntologyWriter writer = new OntologyWriter(reader);
		sparql = writer.getSPARQL(getGraph());
		QueryResult hits = sesame.queryBinding(sparql);
		Map<String, FeedbackText> result = new HashMap<String, FeedbackText>();
		
		for (Iterator it = hits.getIDs(); it.hasNext(); )
		{	//get description
			String id = (String) it.next();
			AutomaticGenerator gen = new AutomaticGenerator(reader, sesame);
			SemanticGraphTransformer sgt = gen.getQueryInformation(id, getGraph());
			sgt.getGraph().stopFlashing();
			queryResultMap.put(id, sgt);
			
			ContentPlan plan = new ContentPlanner(reader, sgt.getGraph()).plan();
			result.put(id, new FeedbackText(new SurfaceRealiser().realise(plan/**ner.getPlan()*/)));
		}	
		return result;
	}
	
	/**	Sends the current query to sesame and returns the number of resources
	 *	that match it.
	 *	@return number of matches
	 *	@throws SPARQLException
	 *	@throws SesameException
	 */
	public int getMatchNr() throws SPARQLException, SesameException
	{
		OntologyWriter writer = new OntologyWriter(reader);
		String sparql = writer.getSPARQL(getGraph());
		QueryResult hits = sesame.queryBinding(sparql);
		return hits.size();
	}
	/**	Returns the SemanticGraph of the search result with that id.
	 *	@param id Sesame ID
	 *	@return SemanticGraphTransformer
	 */
	public SemanticGraphTransformer getQueryResult(String id)
	{
		if (!queryResultMap.containsKey(id))
			return null;
		return queryResultMap.get(id);
	}
	
	/**	Updates the feedbacktext when the semantic graph has been changed
	 *	@throws SurfaceRealisationException
	 *	@throws TextPlanningException
	 */
	public void updateText() throws SurfaceRealisationException, TextPlanningException
	{	//plan text
		try
		{
			QueryPlanner planner = new QueryPlanner(reader);
			planner.plan((SemanticGraph) getGraph());
			plannedEdgeOrder = planner.getPlannedEdgeList();	//remember the order in which the edges were planned

	/**	Iterator it = planner.getPlan().getTrees();						
		int i = 0;			//puts dependency trees in files, to view with GraphViz
		while (it.hasNext())
		{
			String str = Integer.toString(i);
			i++;
			FileWriter fw = new FileWriter(str + ".txt");
			PrintWriter w = new PrintWriter(fw);
			Dotter d = new Dotter();
			w.print(d.dotDependencyTree((DependencyTree) it.next()));		
			w.close();
			fw.close();
		}*/
	
			QueryRealiser qr = new QueryRealiser();
			setText(new FeedbackText(qr.realise(planner.getPlan())));
		}
		catch(IOException e)
		{
			throw new TextPlanningException(e.getMessage());
		}
	}
	
	/**	Overload; makes a query anchor (i.e. no compulsory properties) instead
	 *	of a standard one.
	 *	@param node SGNode	
	 */
	public void updateAnchor(SGNode node)
	{
		try	//check if the target is an anchor
		{
			OntClass c = reader.getClass(node);
			if (c != null)
				new Anchor(reader, c, node, true, userID);
		}
		catch (BadAnchorException e)
		{
			System.out.println(e.getMessage());
			node.removeAnchor();
		}	
	}
	
	/**	Overload. 
	 *	Removes the specified values that the node with this anchor has for this property.
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values String[] array with values to be removed
	 */
	public void removePropertyValues(String anchor, String property, String[] values)
	{
		Anchor a = getText().getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode node = (SGNode) getGraph().getNode(id);
		
		List<Edge> edgeList = node.getOutgoingEdges(property);
		if (!(edgeList.get(0).getTarget() instanceof QueryValueNode))
		{	//this method only acts differently for QueryValueNodes; all others are referred to the supermethod
			super.removePropertyValues(anchor, property, values);
			return;
		}
		
		undoneGraph = new SemanticGraphTransformer(this, reader);
		for (int i = 0; i < edgeList.size(); i++)
		{
			QueryEdge edge = (QueryEdge) edgeList.get(i);
			QueryValueNode target = (QueryValueNode) edge.getTarget();
			
			for (int j = 0; j < values.length; j++)
				target.removeValue(values[j], reader);
			if (target.isEmpty())
				removePropertyValue(edge);
		}	
		updateAnchor(node);
	}
		
	/**	Adds multiple values for one property in one go. Only for datatype properties.
	 *
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param	values a String containing the values that the user has specified for
	 *	the property
	 *	@param datatype Property's datatype (0 = string, 1 is Integer, 2 is Double)
	 *	@param	operator boolean operator (and, or, not)
	 *	@throws IOException
	 */
	public int multipleValuesUpdate(String anchor, String property, String[] values, int datatype, int operator) throws IOException
	{	
		Anchor a = getText().getAnchor(anchor);
		SGNode oldNode = (SGNode) a.getNode();	//(SGNode) getGraph().getNode(a.getNode().getID());
		
		QueryValueNode valueNode = new QueryValueNode(operator);	
		List<Edge> oldEdges = oldNode.getOutgoingEdges(property);	//get existing edge
		if (oldEdges.size() > 0)		//and target node, add the values to that
		{
			valueNode = (QueryValueNode) oldEdges.get(0).getTarget();
			valueNode.setBooleanOperator(operator);
		}
		else	//no node exists, so create and add an edge
		{
			try
			{
				QueryEdge edge = makeEdge(property);
				edge.setID(getGraph().getFreeID());
				edge.setRealiseNr(oldNode.getOutgoingEdges(property).size());
				edge.setSource(oldNode);
				edge.setTarget(valueNode);
				edge.setSequenceNr(lastOp);
				addEdge(edge);	//add edge and nodes to graph
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("FTGENERATOR 523: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
				e.printStackTrace();
			}
		}
		List<QueryValue> oldValues = valueNode.getValues();	//((QueryValue) valueNode.getValue()).getChildren();
		
		for (int i = 0; i < values.length; i++)
		{
			if (datatype == 0)
			{	//if the datatype is a string
				if (oldValues.size() > i)	//if there is an old node, change the value
					((DatatypeNode) oldValues.get(i).getValue()).setValue(values[i]);
				else	//else create a new node
					valueNode.add(new SGStringNode(values[i]));
			}
			else
			{	//first character is the operator, second a space, and after that comes the number
				try
				{
					int comp = 0;
					if (values[i].indexOf("<") == 0)
						comp = 1;
					else if (values[i].indexOf(">") == 0)
						comp = 2;
					values[i] = values[i].replaceAll(",", "");	//remove all komma's
					
					if (datatype == 1)
					{	//parse the integer and update old node or create new node
						Integer nr = Integer.parseInt(values[i].substring(2));
						if (oldValues.size() > i)
						{
							oldValues.get(i).setComparator(comp);
							((DatatypeNode) oldValues.get(i).getValue()).setValue(nr);
						}
						else
							valueNode.add(new SGIntNode(nr), comp);
					}
					else
					{	//parse the double and update old node or create new node
						Double d = Double.parseDouble(values[i].substring(2));
						if (oldValues.size() > i)
						{
							oldValues.get(i).setComparator(comp);
							((DatatypeNode) oldValues.get(i).getValue()).setValue(d);
						}
						else
							valueNode.add(new SGDoubleNode(d), comp);
					}
				}
				catch(NumberFormatException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		while (oldValues.size() > values.length)	//if there were more values before than now
			oldValues.remove(oldValues.size() - 1);		//remove the extra obsolete values	
		
		valueNode.setSequenceNr(lastOp);		
		updateAnchor(oldNode);
		printGraph();
		return values.length;
	}
	
	/**	Updates the Semantic Graph with the provided dates, by creating a QueryValueNode
	 *	that contains a number of SGDateNodes.
	 *
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values Added QueryDateValues
	 *	@param operator boolean operator (and, or, not)
	 */
	public void updateDate(String anchor, String property, QueryDateValue[] values, int operator) throws BadDateException
	{
		Anchor a = getText().getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
	
		QueryValueNode valueNode = new QueryValueNode(operator);	
		for (int i = 0; i < values.length; i++)
		{	//create new nodes and edges
			SGDateNode newNode = new SGDateNode(values[i].getDates());
			valueNode.add(newNode, values[i].getComparator());
		}
		
		try
		{
			QueryEdge edge = makeEdge(property);
			edge.setID(getGraph().getFreeID());
			edge.setRealiseNr(oldNode.getOutgoingEdges(property).size());
			edge.setSource(oldNode);
			edge.setTarget(valueNode);
			edge.setSequenceNr(lastOp);
			addEdge(edge);	//add edge and nodes to graph
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("FTGENERATOR 523: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
			e.printStackTrace();
		}
				
		updateAnchor(oldNode);
		printGraph();
	}
	
	/**	Overload
	 *	Updates the QueryGraph with an object property to an object from the archive, but
	 *	not all information about that object! Not used anymore by LIBER.
	 *
	 *	@param property Property name
	 *	@param anchor Unique ID of Anchor
	 *	@param id Sesame ID of object
	 *	@param seqNr Sequence number	
	 *	@throws SesameException
	 *	@deprecated
	 */
	public void updateObjectPropWithArchiveInstance(String property, String anchor, String id, int seqNr) throws SesameException
	{	//get all information about this node and add it to the graph
		Anchor a = getText().getAnchor(anchor);
		SGNode oldNode = (SGNode) getGraph().getNode(a.getNode().getID());
		AutomaticGenerator aut = new AutomaticGenerator(reader, sesame);
		SGNode node = aut.getQueryInformation(id, oldNode, property, this);
		//aut.getInformation(node, this, false, false);		
		//then add the object property
		updateObjectPropWithExistingInstance(property, anchor, node, seqNr);
		addAnchors();
	}
	
	/**	Overload.
	 *	Updates the SG with an object property to a new instance.
	 *
	 *	@param property Property name
	 *	@param anchor Unique ID of Anchor
	 *	@param instance Data about instance, specified by user in form
	 *	@param seqNr Sequence number
	 */
	public void updateObjectPropWithNewInstance(String property, String anchor, InstanceData instance, int seqNr)
	{
		String type = instance.getType();
		Anchor a = getText().getAnchor(anchor);
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
				
				QueryValueNode valueNode = new QueryValueNode();
				valueNode.add(new QueryValue(new SGStringNode(instance.getValue(i))));
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
	
	/**	Overload.
	 *
	 *	Retrieves the values already added for this property and anchor
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
		if (list.size() > 0)
			return ((QueryValueNode) list.get(0).getTarget()).getValueStringArray(reader);
		
		String[] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++)		//add the values of their target nodes to result
			result[i] = (((SGNode) list.get(i).getTarget()).getNLLabel(reader));
		return result;
	}

	/**	Ensures that each edge in the QueryGraph is a QueryEdge.
	 *
	 *	@param label Property label
	 *	@return QueryEdge
	 */
	public QueryEdge makeEdge(String label)
	{
		return new QueryEdge(label);
	}
	
	/**	Ensures that each edge in the QueryGraph is a QueryEdge.
	 *
	 *	@param label Property label
	 *	@param source Source node
	 *	@param target Target node
	 *	@return QueryEdge
	 *	@throws NameAlreadyBoundException
	 */
	public QueryEdge makeEdge(String label, SGNode source, SGNode target) throws NameAlreadyBoundException
	{
		QueryEdge result = new QueryEdge(label);
		result.setSource(source);
		result.setTarget(target);
		return result;
	}
	
	/**	Returns the planned QueryEdges in the order in which they'll be realised
	 *	@return List<QueryEdge>
	 */
    public List<QueryEdge> getPlannedEdgeList()
    {
    	return plannedEdgeOrder;
    }
    
    /**	The graph stores at all times which statements the user has currently checked as 
     *	'optional'; when regenerating the feedback text this information is needed to
     *	update the interface and prevent the user from having to check all boxes again!
	 *	@return	Boolean[] which states which requirements are currently optional
	 */
    public Boolean[] getCheckedOptionals()
    {
        List<QueryEdge> edges = getPlannedEdgeList();
		Boolean[] result = new Boolean[edges.size()];		
		for (int i = 0; i < edges.size(); i++)
			result[i] = new Boolean(edges.get(i).isOptional());
		return result;
	}
    
    /** Prints the graph and user information to file. Only used for evaluation.
     *	@param user Username
     *	@param ui Information about user behaviour.
     *	@throws IOException
     *	@deprecated
     */
    public void printGraph(String user, UserInfo ui) throws IOException
	{
		if (sparql != null)
		{	//print the sparql query to a file
			FileWriter fw = null;
			try
			{
				fw = new FileWriter(user + Integer.toString(ui.sessionNr) + "SPARQL.txt");
			}
			catch (IOException e)
			{	//Just in case the filename is impossible, this guarantees the user data get stored somewhere
				fw = new FileWriter("xxx" + Integer.toString(ui.sessionNr) + ".txt");
			}
			PrintWriter w = new PrintWriter(fw);
			w.print(sparql);
			w.close();
			fw.close();
		}
		
		FileWriter fw2 = null;
		try
		{	//print the SG of the query to a file
			fw2 = new FileWriter(user + Integer.toString(ui.sessionNr) + "QuerySG.txt");
		}
		catch (IOException e)
		{	//Just in case the filename is impossible, this guarantees the user data get stored somewhere
			fw2 = new FileWriter("USER" + Integer.toString(ui.sessionNr) + "QuerySG.txt");
		}
		ui.sessionNr++;
		PrintWriter w2 = new PrintWriter(fw2);
		Dotter d = new Dotter();
		w2.print(d.dotSGGraph((SemanticGraph) getGraph()));
		w2.close();
		fw2.close();
	}
}