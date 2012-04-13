package WYSIWYM.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import simplenlg.realiser.AnchorString;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Edge;
import WYSIWYM.model.Morph;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.RealiseEdgeComparator;
import WYSIWYM.model.SGAbstractNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.model.TextPlan;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.TextPlanningException;

import com.hp.hpl.jena.ontology.OntProperty;

/**
 *	TextPlanner plans the text; it creates a text plan from a
 *	semantic graph.
 *	This version does very limited aggregation; it's better to use ContentPlanner.
 *
 * @author Feikje Hielkema
 * @version 1.0 2006/11/13
 *
 *	@version 1.2 20-12-2007
 */

public class TextPlanner 
{
	protected TextPlan textPlan;
	protected SemanticGraph graph;
	private Lexicon lexicon = new Lexicon();
	protected OntologyReader reader;
	protected List<String> familiarNodes, mappedNodes, mappedEdges;

	/**	Constructs an empty text planner, taking only an ontology reader
	 *
	 *	@param r Ontology
	 */
    public TextPlanner(OntologyReader r) 
    {
    	reader = r;
    }
    
    /**	Constructs a text planner with a semantic graph
	 *
	 *	@param r Ontology
	 *	@param g SemanticGraph	
	 */
    public TextPlanner(OntologyReader r, SemanticGraph g) 
    {
    	graph = g;
    	reader = r;
    }
    
    /**	Sets the semantic graph
     *	@param g SemanticGraph
     */
    public void setGraph(SemanticGraph g)
    {
    	graph = g;
    }
    
    /**	Returns the text plan
     *
     *	@return TextPlan
     */
    public TextPlan getPlan()
    {
    	return textPlan;
    }
    
    /**	Initiates the text planning, sorting the edges into paragraphs and mapping
     *	each to a dependency tree
     *
     *	@param g SemanticGraph
     *	@throws IOException
     *	@throws TextPlanningException
     */
    public void plan(SemanticGraph g) throws IOException, TextPlanningException
    {
    	textPlan = new TextPlan();
    	graph = g;
    	if (!graph.getEdges().hasNext())	
    		throw new TextPlanningException("This version of the TextPlanner can't map queries yet!");
    		
    	familiarNodes = new ArrayList<String>();
		mappedNodes = new ArrayList<String>();
		mappedEdges = new ArrayList<String>();
    	
    	SGNode root = (SGNode) graph.getRoot();	
		plan(root);
    	graph.stopFlashing();		//all edges now will have been realised once, so stop them flashing
    }
    
    /**	Sorts the edges of this node by edge label and target node
     *
     *	@param edgeList	List<SGEdge>
     *	@param source	SGNode, the source of the edges to be sorted
     *	@return	List
     */
    protected List sortEdges(List<SGEdge> edgeList, SGNode source)
    {
    	List result = new ArrayList();
    	List<SGEdge> edgeStore = new ArrayList<SGEdge>();
    	List<Edge> sortedEdges = new ArrayList<Edge>();
    	
    	for (int i = 0; i < edgeList.size(); i++)
    	{	//finds and groups edges with the same label, and puts them in separate lists
    		SGEdge edge = edgeList.get(i);
    		if (/**edge.isNLNameEdge()*/ reader.useAsProperName(edge.getLabel()) || sortedEdges.contains(edge) || mappedEdges.contains(edge.getID()))	//don't include the name edge
    			continue;	//or an edge already sorted, or mapped
    		if (edge.getTarget().equals(source) && familiarNodes.contains(edge.getSource().getID()))	//if it's an incoming edge and the source node
    			continue;	//has its own paragraph, do nothing
    		if ((edge.mustRealise() == SGNode.HIDE)	&& (source.mustRealise() == SGNode.HIDE))	//if (!edge.mustRealise())
    			continue;	//if the edge doesn't have to be realised (because it was extracted from the database), do nothing
    		
    		mappedEdges.add(edge.getID());	//state that it is mapped here, not later	
    		List<Edge> temp = source.getEdges(edge.getLabel());		//get all edges of this property
     		String inverse = reader.getInverse(edge.getLabel());
    		if (inverse != null)		//add all incoming edges of the inverse property
    			temp.addAll(source.getEdges(inverse));

    		if (temp.size() > 1)		//group identical edges together
    		{
    			SGEdge[] tempArray = new SGEdge[0];
    			tempArray = (SGEdge[]) temp.toArray(tempArray);
    			Arrays.sort(tempArray, new RealiseEdgeComparator());	//sort these according to which sequence they were added
    			result.add(tempArray);	//add the list with double nodes; they will be aggregated later on
    		}
    		else
    			edgeStore.add((SGEdge) temp.get(0));		//save single edges for later processing
    		sortedEdges.addAll(temp);					//make sure edges don't get sorted twice
    	}
    	
    	Map<SGNode,List<SGEdge>> map = new HashMap<SGNode,List<SGEdge>>();
    	for (int i = 0; i < edgeStore.size(); i++)		//now group all edges with the same target node
    	{	//MAYBE WE COULD AGGREGATE HERE LATER AS WELL?
    		SGNode target = edgeStore.get(i).getTarget();
    		if (target.equals(source))	//incoming instead of outgoing edge
    			target = edgeStore.get(i).getSource();
    			
    		if (map.containsKey(target))
    		{
    			List<SGEdge> temp = map.get(target);
    			temp.add(edgeStore.get(i));
    			map.put(target, temp);
    		}
    		else
    		{
    			List<SGEdge> temp = new ArrayList<SGEdge>();
    			temp.add(edgeStore.get(i));
    			map.put(target, temp);
    		}
    	}

    	Object[] targetNodes = map.keySet().toArray();
    	Arrays.sort(targetNodes);			//sort the nodes into chronologic order
    	for (int i = 0; i < targetNodes.length; i++)
    	{
    		SGNode n = (SGNode) targetNodes[i];
    		List<SGEdge> temp = map.get(n);
    		Object[] edgeArray = temp.toArray();
    		Arrays.sort(edgeArray);				//sort the edges in the list for chronological order
    		int idx = getIndex(result, n);	//trying to put the target nodes with the already sorted equal edges
    		for (int j = 0; j < edgeArray.length; j++)
    			result.add(idx + j, (SGEdge) edgeArray[j]);
    	} 	 	
    	return result;
    }
    
    /**	Takes a list of edges and checks whether any of them has the given node
     *	as a target. Returns a suitable index to add an edge with the same target
     *
     *	@param	ArrayList, containing edges
     *	@param	Node, the target node the list will be searched for
     *	@return	int, the index at which the edge can be added
     */
    private int getIndex(List l, SGNode n)
    {
    	for (int i = 0; i < l.size(); i++)
    	{
    		Object o = l.get(i);
    		if (o instanceof SGEdge[])
    		{
    			SGEdge[] edges = (SGEdge[]) o;
    			if (edges[0].getTarget().equals(n))
    				return i;	//add the edge just before this one
    			if (edges[edges.length - 1].getTarget().equals(n))
    				return (i + 1); //edges.length);	//add edge just after this one
    			if (edges[0].getSequenceNr() > n.getSequenceNr())	//if this edge is older than the aggregated ones,
    				return i;		//add it before
    		}
    	}
    	return l.size();	//if the target node is not in text yet, add at end
    }
    
    /**	Divides the properties into categories, using the OntologyReader
     */
    protected Map<String, List<SGEdge>> breakIntoSets(SGNode node)
    {
    	Map<String, List<SGEdge>> result = new HashMap<String, List<SGEdge>>();    		
    	for (Iterator it = node.getEdges(); it.hasNext(); )
    	{
    		SGEdge edge = (SGEdge) it.next();
    		String type = reader.getSubmenu(reader.getProperty(edge.getLabel()), node.getLabel());	//reader.getPropertyType(edge.getLabel());
    		if (edge.getTarget().equals(node))	//incoming edge
    		{
    			//String inverse = reader.getInversePropertyType(edge.getLabel());
    			OntProperty inverse = reader.getInverse(reader.getProperty(edge.getLabel()));
    			if (inverse != null)
    				type = reader.getSubmenu(inverse, node.getLabel());
    		}
    		if (type == null)
    			continue;
    		List<SGEdge> list = new ArrayList();
    		if (result.containsKey(type))
    			list = result.get(type);
    		list.add(edge);   
    		result.put(type, list); 			
    	}
    	return result;
    }

    /**	Returns the order in which paragraphs should be realised.
     *	THIS IS DOMAIN-SENSITIVE INFORMATION! FUTURE VERSIONS SHOULD GET THIS
     *	SOMEHOW OUT OF AN ONTOLOGY, OR POSSIBLY A SEPARATE TEXT FILE.
     *
     *	Another reason to use ContentPlanner, which does get this information from the ontology model.
     *	@return List<String> of headers
     */
    public static List<String> getSetOrder()
    {
    	List<String> result = new ArrayList<String>();
    	result.add("general information");
    	result.add("access rights");
    	result.add("results/comments");
    	result.add("activities/roles");
    	result.add("when");
    	result.add("where");
    	result.add("who");
    	result.add("what");
    	result.add("how");
    	result.add("why");
       	result.add("tasks and stages");
    	result.add("produced resource");
    	result.add("produced during task");
    	result.add("used in task");
    	result.add("used resources");
    	result.add("relations to projects or resources");
    	result.add("xxx");
    	result.add("abstract");
    	return result;
    }
    
    /**	Maps the given node to a paragraph in the text
     */
    protected boolean plan(SGNode node) throws TextPlanningException, IOException
    {	//if the node has already been mapped, do nothing
    	if (mappedNodes.contains(node.getID()))
    		return false;
     	mappedNodes.add(node.getID());

     	if (!graph.getRoot().equals(node))	//if this node is the root, it has to be mapped in any case
     	{
     	   	boolean map = false;
	     	for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
    	 	{	//if all outgoing edges are name edges, don't have to be realised, or go to a node already mapped,
     			SGEdge edge = (SGEdge) it.next();
     			if (reader.useAsProperName(edge.getLabel()))	//edge.isNLNameEdge())
     				continue;
     			if ((edge.mustRealise() == SGNode.HIDE)	&& (node.mustRealise() == SGNode.HIDE))	//if (!edge.mustRealise())
     				continue;
     			if (!mappedNodes.contains(edge.getTarget().getID()))
     				map = true;
     		}
     		if (!map)
     		{	//and all incoming edges come from a node already mapped, do nothing
     			for (Iterator it = node.getIncomingEdges(); it.hasNext(); )
    	 		{
     				SGEdge edge = (SGEdge) it.next();
     				if ((edge.mustRealise() == SGNode.HIDE)	&& (node.mustRealise() == SGNode.HIDE))	//if (!edge.mustRealise())
     					continue;
     				if (!mappedNodes.contains(edge.getSource().getID()))
     					map = true;
     			}
     		}
     		if (!map)
     			return false;
     	}
     		
     	//map node to a paragraph header and create a new paragraph
     	String paragraph = node.getHeaderLabel(reader);
     	textPlan.newParagraph(paragraph, node.getAnchor());
     	familiarNodes.add(node.getID());
       	//sort the incoming and outgoing edges into sentence sets
       	Map<String, List<SGEdge>> edgeMap = breakIntoSets(node);
       	List<String> setOrder = getSetOrder();
 	
       	for (int i = 0; i < setOrder.size(); i++)
       	{
       		String set = setOrder.get(i);
       		if (!edgeMap.containsKey(set))
       			continue;
       		
       		textPlan.newSet(paragraph, set);
       		List edgeList = sortEdges(edgeMap.get(set), node);
       		
       		for (int j = 0; j < edgeList.size(); j++)
       		{	//map all edges
       			if (edgeList.get(j) instanceof SGEdge)
	       			textPlan.add(map((SGEdge) edgeList.get(j), node), paragraph, set);
	       		else if (edgeList.get(j) instanceof SGEdge[])
	       			textPlan.add(mapAggregate((SGEdge[]) edgeList.get(j), node), paragraph, set);
       		}
       	}
       	return true;
    }
    
    /**	Maps this node to a DTNode using a pronoun.
     *	@param node SGNode
     *	@return DTNode
     */
    public DTNode mapCurrentTopic(SGNode node)
    {
    	DTNode np = new DTNode(LinguisticTerms.NP, null, node.getNLLabel(reader), null);
    	np.setPronoun(node.getPronoun());	
	  	np.setUseAsPronoun(true);
   		if (node.isQuote())
   			np.setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
	  	return np;
    }
    
    /**	Maps this address node to a DTNode.
     *	@param address SGAddressNode
     *	@return false if the argument was already mapped
     *	@throws IOException
     *	@throws TextPlanningException
     */
    public boolean mapAddress(SGAddressNode address) throws TextPlanningException, IOException
    {
    	if (mappedNodes.contains(address.getID()))
    		return false;
     	mappedNodes.add(address.getID());
     	
     	List<AnchorString> paragraph = address.getAddressHeader(reader);
     	String key = textPlan.newParagraph(paragraph);
     	familiarNodes.add(address.getID());
     	String add = address.getNLLabel(reader);
     	if (add.length() > 0)
     	{
	 	   	DTNode mapping = new DTNode(LinguisticTerms.NP, LinguisticTerms.ROOT, address.getNLLabel(reader), null);
	 	   	textPlan.newSet(key, "xxx");
     		textPlan.add(new DependencyTree(mapping), key, "xxx");
     	}
     	
     	List<SGEdge> edges = address.getOtherEdges();
     	for (int i = 0; i < edges.size(); i++)
     	{
     		SGNode target = edges.get(i).getTarget();
     		if (target instanceof SGAddressNode)
     			target = edges.get(i).getSource();
     		plan(target);
     	}
     	return true;
    }
    
    /**	Maps this abstract node to a DTNode.
     *	@param sgTarget SGAbstractNode
     *	@param sgSource Source of property
     *	@return false if the argument was already mapped
     *	@throws TextPlanningException
     */
    public boolean mapAbstract(SGAbstractNode sgTarget, SGNode sgSource) throws TextPlanningException
    {
    	if (mappedNodes.contains(sgTarget.getID()))
    		return false;
     	mappedNodes.add(sgTarget.getID());
     	
     	String paragraph = sgSource.getHeaderLabel(reader);
     	DTNode mapping = new DTNode(LinguisticTerms.NP, LinguisticTerms.ROOT, sgTarget.getLabel(), null);
	 	textPlan.newSet(paragraph, "abstract");
     	textPlan.add(new DependencyTree(mapping), paragraph, "abstract");
     	return true;
    }
    
    /**	Maps a SGDateNode to a prepositional phrase, with the correct preposition.
     *
     *	@param node SGDateNode
     *	@param np DTNode with noun phrase, will be part of PP
     *	@return DTNode with PP
     */
    public DTNode mapDate(SGDateNode node, DTNode np)
    {
    	DTNode pp = new DTNode(LinguisticTerms.PP, null, null, null);
    	DTNode prep = new DTNode(LinguisticTerms.PREP, LinguisticTerms.HEAD, node.getPreposition(false), null);
    	DTEdge prepEdge = new DTEdge(LinguisticTerms.HEAD);
    	DTEdge npEdge = new DTEdge(LinguisticTerms.OBJECT);
    	np.setDeplbl(LinguisticTerms.OBJECT);
    	
    	try
    	{
    		prepEdge.setSource(pp);
    		prepEdge.setTarget(prep);
    		npEdge.setSource(pp);
    		npEdge.setTarget(np);
    	}
    	catch (NameAlreadyBoundException e)
    	{
    		System.out.println("An exception here should not be possible!");
    		e.printStackTrace();
    	}
    	return pp;
    }
    
    /**	Maps the given node to a DTNode representing a Noun Phrase
     *
     *	@param	node SGNode to be mapped
     *	@return	DTNode
     */
    public DTNode map(SGNode node)
    {  	//make an NP with as head a Noun
    	DTNode np = new DTNode(LinguisticTerms.NP, null, null, null);
    	DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, node.getNLLabel(reader), null);
    	DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD);
    	
    	try
    	{
	    	nounEdge.setSource(np);
    		nounEdge.setTarget(noun);
   		   	
   		   	String det = node.getDeterminer(SGNode.A, reader);
   		   	if (det != null)
    		{	//if the phrase contains a determiner, make a separate determiner node
    			DTNode detNode = new DTNode(LinguisticTerms.DET, LinguisticTerms.DET, det, null);  		
    			DTEdge detEdge = new DTEdge(LinguisticTerms.DET);
    			detEdge.setSource(np);
    			detEdge.setTarget(detNode);
    		}
    	}
    	catch (NameAlreadyBoundException e)
    	{
    		System.out.println("An exception here should not be possible!");
    		e.printStackTrace();
    	}
    	if (!(node instanceof SGDateNode))
    		np.setSGID(node.getSGID());	//sg identifier is used in surface form for disambiguation
   		if (node.isQuote())
   			np.setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
   		
		np.setPronoun(node.getPronoun());
  		np.setFlash(node.flash());		
   		if (!familiarNodes.contains(node.getID()))
   		{
   			np.setAnchor(node.getAnchor());
   			familiarNodes.add(node.getID());
   		}
   		
   		if (node instanceof SGDateNode)
   			return mapDate((SGDateNode) node, np);
   		return np;   	
    }
    
    private DependencyTreeTransformer getTree(OntProperty p) throws IOException
    {	//find the lexicon entry for either this property or its inverse property
		try
		{
			return lexicon.map(p.getLocalName());
		}
		catch (IOException e)
		{
			OntProperty i = reader.getInverse(p);
			if (i == null) //if there is no inverse, there was no entry in the lexicon 
				throw e;		//for the property so we pass on the exception
			DependencyTreeTransformer dt = lexicon.map(i.getLocalName());
			dt.getGraph().setInverseInserted(true);
			return dt;
		}
    }
    
    protected DependencyTreeTransformer getTree(SGEdge edge) throws IOException
    {
    	OntProperty p = reader.getProperty(edge.getLabel());
		try
		{
			if (edge.getLabel().equals(QueryEdge.ANYTHING))	//'anything' is obviously not a property in the ontology!
				return lexicon.map(edge.getLabel());
			return getTree(p);
		}		
		catch (IOException e)
		{
			for (Iterator it = p.listSuperProperties(); it.hasNext(); )
			{
				OntProperty parent = (OntProperty) it.next();
				if (p.equals(parent))
					continue;
				try
				{
					return getTree(parent);
				}
				catch(IOException ex)
				{}
			}
		}
		throw new IOException("Property " + edge.getLabel() +	" is not in the lexicon");
    }
    
    /**	If this returns true, 'plan' should return immediately
     */
    private DependencyTreeTransformer getTree(SGEdge edge, SGNode sgSource, SGNode sgTarget, 
    	boolean aggregate, boolean incoming) throws IOException
    {
		DependencyTreeTransformer dt = getTree(edge);
		boolean inverse = dt.getGraph().getInverseInserted();
		dt.getGraph().setFlash(edge.flash());
		DTNode source = mapCurrentTopic(sgSource);
		
		if (sgTarget instanceof SGBooleanNode)
		{
			Boolean b = (Boolean) ((SGBooleanNode)sgTarget).getValue();
			if (!b.booleanValue())		//if the value is false
				dt.setNegated(true);							//set the sentence to 'negated'
			dt.insert(source, lexicon.SOURCE);			//insert only the source node
			return dt;
		}

		
		DTNode dtTarget = map(sgTarget);
		dtTarget.setFlagged(true);		//flag this node so we can find it later

		if (inverse)
			dt.setPassive();

		if ((inverse || incoming) && !(inverse && incoming))	//add the new source and target to the tree;
		{				//if we used the inverse property they need to exchange places
			if (aggregate)
				dt.append(dtTarget, lexicon.SOURCE, 0);
			else
				dt.insert(dtTarget, lexicon.SOURCE);
			dt.insert(source, lexicon.TARGET);
		}	
		else
		{
			dt.insert(source, lexicon.SOURCE);
			if (aggregate)
				dt.append(dtTarget, lexicon.TARGET, 0);
			else
				dt.insert(dtTarget, lexicon.TARGET);
		}
		
		return dt;
    }
    
    /**	Maps the given edge of the semantic graph to a Dependency Tree. The target
     *	node is mapped to a DTNode, and the method recurses the map the target 
     *	node.
     *
     *	@param	edge The edge to be mapped
     *	@param	sgSource The source node of the edge in the semantic graph
     *	node has been realised (and so whether it should be an anchor or not)
     *	
     *	@return	DependencyTree
     *	@throws IOException
     *	@throws TextPlanningException
     */
    protected DependencyTree map(SGEdge edge, SGNode sgSource) throws IOException, TextPlanningException
    {
		SGNode sgTarget = edge.getTarget();
		boolean incoming = false;
		if (sgTarget.getID().equals(sgSource.getID()))
		{
			incoming = true;
			sgTarget = edge.getSource();
		}
			
		if (sgTarget instanceof SGAddressNode)
		{
			mapAddress((SGAddressNode) sgTarget);
			return null;
		}
		if (sgTarget instanceof SGAbstractNode)
		{
			mapAbstract((SGAbstractNode) sgTarget, sgSource);
			return null;
		}
		DependencyTree dt = getTree(edge, sgSource, sgTarget, false, incoming).getGraph();
		plan(sgTarget);
		return dt;
    }
    
    /**	Maps a list of edges with the same label to a series of near-identical dependency
     *	trees, which will be aggregated in the surface realiser
     *
     *	@param	l SGEdge[] with edges to be mapped
     *	@param	sgSource The DTNode corresponding to the source node
     *	@return	DependencyTreeTransformer
     *	@throws IOException
     *	@throws TextPlanningException
     */
    protected DependencyTree mapAggregate(SGEdge[] l, SGNode sgSource) throws IOException, TextPlanningException
    { 
    	SGNode sgTarget = l[0].getTarget();
    	boolean incoming = false;
    	if (sgTarget.getID().equals(sgSource.getID()))
    	{
    		incoming = true;
    		sgTarget = l[0].getSource();
    	}
    	if (sgTarget instanceof SGAddressNode)
    	{
    		mapAddress((SGAddressNode) sgTarget);
    		for (int i = 1; i < l.length; i++)
    		{
    			SGNode target = l[i].getTarget();
    			if (target.getID().equals(sgSource.getID()))
    				target = l[i].getSource();
    			mapAddress((SGAddressNode) target);
    		}
			return null;
    	}
    	
    	DependencyTreeTransformer dt = getTree(l[0], sgSource, sgTarget, true, incoming);
    	plan(sgTarget);		//recurse
    
    	String property = l[0].getLabel(); 	
    	for (int i = 1; i < l.length; i++)
    	{ 
			SGNode mappedNode = l[i].getTarget();
			if (mappedNode.getID().equals(sgSource.getID()))
    			mappedNode = l[i].getSource();			
			DTNode mapping =  map(mappedNode);
		
			DTNode flagged = dt.getGraph().getFlaggedNode();	
			while (flagged.getInserted() == 0)
			{	//find out how the previous conjunct was inserted (as source or target)
				Iterator it = flagged.getParents();
				if (!it.hasNext())
					break;
				flagged = (DTNode) it.next();
			}	//and insert this conjunct in the same place
			if (flagged.getInserted() == 1) 
				dt.append(mapping, lexicon.SOURCE, i);
			else if (flagged.getInserted() == 2)
				dt.append(mapping, lexicon.TARGET, i);
			else
				throw new TextPlanningException("TextPlanner 595: Can't find previously inserted node!");
			plan(mappedNode);		//recurse
    	}
    	
    	return dt.getGraph();
    }
}