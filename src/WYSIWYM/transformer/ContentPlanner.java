package WYSIWYM.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import simplenlg.realiser.AnchorString;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.Morph;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.SGAbstractNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.model.SummationAnchor;
import WYSIWYM.model.UndeterminedDTNode;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.TextPlanningException;

import com.hp.hpl.jena.ontology.OntProperty;

/**
 *	ContentPlanner plans the text; it creates a text plan from a
 *	semantic graph.
 *
 * @author Feikje Hielkema
 * @version 1.4 2008/07/25
 */
public class ContentPlanner 
{
	protected ContentPlan plan;
	protected SemanticGraph graph;
	protected Lexicon lexicon = new Lexicon();
	protected OntologyReader reader;
	
	protected List<String> mappedEdges;		//list that stores which edges have been mapped
	protected List<SGNode> relativeClauses;
	protected List<SGNode> paragraphs;

    /**	Constructs a text planner with a semantic graph
	 *
	 *	@param r Ontology	
	 *	@param g SemanticGraph	
	 */
    public ContentPlanner(OntologyReader r, SemanticGraph g) 
    {
    	graph = g;
    	reader = r;
    }
    
    /**	Plans the graph to a content plan.
     *
     *	@return ContentPlan
     *	@throws TextPlanningException
     */
    public ContentPlan plan() throws TextPlanningException
    {
    	plan = new ContentPlan();
    	makeParagraphs();
    	mappedEdges = new ArrayList<String>();
    	    	
    	for (int i = 0; i < paragraphs.size(); i++)
    	{
    		SGNode node = paragraphs.get(i);
    		if (node instanceof SGAddressNode)
    			mapAddress((SGAddressNode) node);
    		else if (node instanceof SGAbstractNode)
    			mapAbstract((SGAbstractNode) node);
    		else
    		{	
	    		String header = node.getHeaderLabel(reader);
    			header = plan.newParagraph(header, node.getAnchor());	
    			List<DependencyTreeTransformer> trees = lexicalise(node);
    			trees = new Aggregator().aggregate(trees);
    			plan.add(trees, header);
    		}
    	}

    	addRelativeClauses();    	
    	generateReferringExpressions();
    	graph.stopFlashing();		//all edges now will have been realised once, so stop them flashing
    	return plan;
    }
    
    /**	Determines the minimum number of paragraphs needed in the text. Each 
     *	paragraph describes a node in the graph.
     *
     *	@return List<SGNode> with individuals that should be paragraphs
     */
    protected List<SGNode> makeParagraphs()
    {
    	paragraphs = new ArrayList<SGNode>();
    	Map<SGNode,List<SGEdge>> map = new HashMap<SGNode,List<SGEdge>>();
    	paragraphs.add(graph.getRoot());		//the uploaded resource must be the first paragraph
    	
    	for (Iterator it = graph.getNodes(); it.hasNext(); )
    	{
    		SGNode node = (SGNode) it.next();
    		if (node == graph.getRoot())
    			continue;
    		if ((node instanceof SGAddressNode) || (node instanceof SGAbstractNode))	
    		{	//addresses and abstracts must receive separate paragraphs!
    			paragraphs.add(node);
    			continue;
    		} 			
    		if ((node instanceof DatatypeNode) || (node instanceof SGDateNode))
    			continue;	//don't add leaves (i.e. datatypes), or the root (already in there)
    		
    		int datatype = 0;
    		boolean paragraph = false;
    		List<SGEdge> props = new ArrayList<SGEdge>();
    		
    		for (Iterator it2 = node.getEdges(); it2.hasNext(); )
    		{	
    			SGEdge edge = (SGEdge) it2.next();	
    			if ((edge.mustRealise() >= SGNode.HIDE)	&& (node.mustRealise() >= SGNode.HIDE))
    				continue;	//if the edge doesn't have to be realised (because it was extracted from the database), do nothing
    			if (!Lexicon.specExists(edge.getLabel(), reader))
    				continue;	//if there is no spec for this property, continue
    			if (reader.useAsProperName(edge.getLabel()))	//these edges are not realised as sentences so they don't count
    				continue;
    			if ((edge.getTarget() instanceof DatatypeNode) || (edge.getTarget() instanceof SGDateNode))
    			{	//nodes with OUTGOING datatype properties need their own paragraph
    				if (datatype > 0)
    				{
	    				paragraphs.add(node);
    					paragraph = true;
    					break;
    				}
    				else //if a node has just one datatype property, it doesn't necessarily require a separate paragraph
    				{
    					props.add(edge);
    					datatype++;
    				}	
    			}
    			else
    				props.add(edge);
    		}

			if ((!paragraph) && props.size() > 0)	//hold off judgement on this node
    			map.put(node, props);
    	}

    	//now we have a list of paragraphs and a list of nodes with object properties that we are not sure about.
    	List<SGNode> noParagraph = new ArrayList<SGNode>();
    	relativeClauses = new ArrayList<SGNode>();
    	int tempSize = 0;
    	while (map.size() > 0)			//all nodes have been sorted, so stop
    	{
    		if (tempSize == map.size())	//no changes in previous loop!
    		{	//Add the node with the most properties
    			int max = 0;
    			SGNode choice = null;
    			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
    			{
    				SGNode n = (SGNode) it.next();
    				int nr = map.get(n).size();
    				if (nr > max)
    				{
    					max = nr;
    					choice = n;
    				}
    			}
    			paragraphs.add(choice);
    			map.remove(choice);	
    		}
   			tempSize = map.size();
    		
    		for (Iterator nodeIt = map.keySet().iterator(); nodeIt.hasNext(); )
    		{	//for every node, check if we know whether it should be a paragraph or not yet
    			SGNode node = (SGNode) nodeIt.next();
    			int properties = 0;		//the number of edges that are not yet realised in a paragraph
    			boolean paragraph = false;
    			
    			for (SGEdge edge : map.get(node))
    			{
    				if ((edge.getTarget() instanceof DatatypeNode) || (edge.getTarget() instanceof SGDateNode))
    				{
    					properties++;				//skip the datatype properties, but remember there was one!
    					continue;
    				}
    				
    				SGNode target = edge.getTarget();		//get the other node of this edge
    				if (target.equals(node))
    					target = edge.getSource();
    				
    				if (noParagraph.contains(target))		//if the other node is no paragraph, 
    				{
    					paragraphs.add(node);					//this edge must be realised through this node!
    					paragraph = true;
    					break;
    				}
    				else if (!(relativeClauses.contains(target) || paragraphs.contains(target)))		//if there is an edge with a target that we don't know about yet,
    					properties++;				//then don't remove this node from temp
    			}

				if (!paragraph)
				{
	    			if (properties == 0)		//if the node has no properties to nodes that are not paragraphs or clauses,
    					noParagraph.add(node);		// it doesn't need a paragraph
    				else if (properties == 1)		//if it has exactly one property, it can be a relative clause
    					relativeClauses.add(node);	//else we iterate again!
    			}
    		}
    		
    		for (int i = 0; i < noParagraph.size(); i++)	//remove all nodes that won't be paragraphs from temp
    			map.remove(noParagraph.get(i));
    		for (int i = 0; i < paragraphs.size(); i++)			//and all nodes that will be, and all relative clauses
    			map.remove(paragraphs.get(i));					//leaving only those we're not yet sure about
    		for (int i = 0; i < relativeClauses.size(); i++)		
    			map.remove(relativeClauses.get(i));		
    	}
    	
    	//now we have a list with paragraphs, but should it be ordered too?
    	List<SGNode> result = new ArrayList<SGNode>();
    	List<SGNode> temp = new ArrayList<SGNode>();
    	result.add(paragraphs.get(0));	//root first
    	for (int i = 1; i < paragraphs.size(); i++)
    	{	//check if there is a link between this node and the previous
    		SGNode previous = paragraphs.get(i - 1);
    		SGNode next = paragraphs.get(i);
    		if (previous.isConnected(next))	//if it is, add the next node behind the previous one
    			result.add(next);
    		else							//else add it to the temp list,
    			temp.add(next);				//so the paragraph will be added at the end of the text
    	}
    	
    	for (int i = 0; i < temp.size(); i++)
    	{
    		SGNode node = temp.get(i);
    		boolean added = false;
    		for (int j = 0; j < result.size(); j++)
    		{
    			if (result.get(j).isConnected(node))
    			{	//insert the node in a place where it connects to the previous paragraph
    				result.add(j + 1, node);
    				added = true;
    				break;
    			}
    		}
    		if (!added)	//if there is no such place, add it to the end
    			result.add(node);
    	}
	
    	return result;		//and now, finally, we are done
    }
    
    protected List<DependencyTreeTransformer> lexicalise(SGNode topic)
    {
    	return lexicalise(topic, false);
    }
    
    /**	Lexicalises all edges of this node that haven't been added to a previous
     *	paragraph, and returns a list with the resulting dependency trees.
     *
     *	If a node has >1 instance of the same property, these edges are already
     *	coordinated. Although it would be neater to do it in aggregate(), this 
     *	would mean wasting time on generating dependency trees that are then searched,
     *	combined and thrown away.
     *
     *	@param	topic The SGNode that is the topic of the paragraph
     *	@return List<DependencyTreeTransformer>
     */
    protected List<DependencyTreeTransformer> lexicalise(SGNode topic, boolean relativeClause)
    {
    	List<DependencyTreeTransformer> result = new ArrayList<DependencyTreeTransformer>();
    	for (Iterator it = topic.getEdges(); it.hasNext(); )
    	{
    		SGEdge edge = (SGEdge) it.next();
    		DependencyTreeTransformer dt = lexicaliseEdge(topic, edge, relativeClause);
    		if (dt != null)
    			result.add(dt);
    	}
    	return result;
    }
    
    protected DependencyTreeTransformer lexicaliseEdge(SGNode topic, SGEdge edge, boolean relativeClause)
    {
    	if ((edge.mustRealise() >= SGNode.HIDE)	&& (topic.mustRealise() >= SGNode.HIDE))
    		return null;	//if the edge doesn't have to be realised (because it was extracted from the database), do nothing
    	if (reader.useAsProperName(edge.getLabel()))	
    		return null;		//don't map the name edges		
    	if (mappedEdges.contains(edge.getID()))
    		return null;		//don't map edges that have already been mapped
    		
    	List<String> specProperties = new ArrayList<String>();
    	DependencyTreeTransformer dt = getTree(edge, specProperties, topic, relativeClause);
    	if (dt == null)	//if it couldn't find the linguistic spec, skip this edge
    		return null;

   		dt.getGraph().setFlash(edge.flash());		
   		boolean inverse = dt.isInverseInserted();
   	   	if (inverse)
   			dt.setPassive();

   		boolean incoming = topic.hasIncomingEdge(edge);
   		SGNode object = edge.getTarget();
   		if (incoming)
   			object = edge.getSource();
   		DTNode topicDT = mapNode(topic, !relativeClause);
   	   
   	   	if (object instanceof SGBooleanNode)
		{
			Boolean b = (Boolean) ((SGBooleanNode)object).getValue();
			if (!b.booleanValue())		//if the value is false
				dt.setNegated(true);							//set the sentence to 'negated'
			dt.insert(topicDT, lexicon.SOURCE);			//insert only the source node
			mappedEdges.add(edge.getID());
			return dt;
		}

   		List<SGEdge> edgeList = new ArrayList<SGEdge>();
   		if (!relativeClause)
   		{
   			for (Edge e : topic.getSimilarEdges(edge, specProperties, reader))
    			edgeList.add((SGEdge) e);
    	}
    	else
    		edgeList.add(edge);
   
   		for (int i = 0; i < edgeList.size(); i++)
   			mappedEdges.add(edgeList.get(i).getID());
   			
   		DTNode objectDT = null;
   		boolean summation = false;
   		if ((edgeList.size() > 4) && (edge.showInList() <= SGEdge.HIDDEN_LIST) && (!reader.isDatatypeProperty(edge.getLabel())))
  		{	//if there are more than 4 edges that have the same specification (and hence will be aggregated), and the user has not requested to see them all,
   			objectDT = makeSummation(edgeList, topic, dt);		//then hide them, realising '5 cities' or some such instead.
   			summation = true;
   			edgeList.clear();
   		}
   		else	//else we want to order the list, so that the correct sequence of e.g. authors is preserved
   		{
   			Collections.sort(edgeList);
   			edge = edgeList.get(0);
   			incoming = topic.hasIncomingEdge(edge);
   			object = (SGNode) edge.getTarget();
   	   		if (incoming)
   	   			object = (SGNode) edge.getSource();
   			objectDT = mapNode(object, false);
   		}
   		
   		List<String> targets = new ArrayList<String>();
   		targets.add(object.getID());
   		
   		if (inverse)
   	   	{	//insert the source and target node
   	   		dt.insert(topicDT, lexicon.TARGET);
   	   		if (edgeList.size() > 1)		//if there is more than one edge, make it a conjunction
   	   			dt.append(objectDT, lexicon.SOURCE, 0);
   	   		else
    	   		dt.insert(objectDT, lexicon.SOURCE);
   	   	}
   	   	else
   	   	{
   	   		dt.insert(topicDT, lexicon.SOURCE);
   	   		if (edgeList.size() > 1)
   	   			dt.append(objectDT, lexicon.TARGET, 0);
   	   		else
	   	   		dt.insert(objectDT, lexicon.TARGET);
   	   	} 
    	
    	for (int i = 1; i < edgeList.size(); i++)	//if there is more than one instance of this property (or its inverse)
    	{							//insert the targets of those other edges, to achieve a conjunct coordination
    		SGEdge edge2 = (SGEdge) edgeList.get(i);			//it would be neater to do this in aggregate()
    		SGNode target = edge2.getTarget();					//but as that would also simply waste time and memory, tough.
       		if (topic.hasIncomingEdge(edge2))
       			target = edge2.getSource();		//map to DTNode	
       		if (targets.contains(target.getID()))	//if an edge with the same specification, between the same nodes,
       			continue;		//has already been mapped, skip this one!
       	
       		DTNode dt2 = mapNode(target, false);	
       		if (objectDT.getInserted() == DTNode.SOURCE_INSERTED)	//insert DTNode in the same place as previous node
       			dt.append(dt2, lexicon.SOURCE, i);
       		else if (objectDT.getInserted() == DTNode.TARGET_INSERTED)
       			dt.append(dt2, lexicon.TARGET, i);
    	}
    	
    	return dt;
    }
    
    /**	Maps a SGNode to an undetermined DTNode (if it's an object) or a normal
     *	DTNode (if it's a datatype value).
     */
    private DTNode mapNode(SGNode sg, boolean topic)
    {
    	if (sg instanceof SGDateNode)
    		return mapDate((SGDateNode) sg);
    	else if (sg instanceof DatatypeNode)
    		return new DTNode(LinguisticTerms.NP, null, sg.getNLLabel(reader), null);
		
    	UndeterminedDTNode node = new UndeterminedDTNode(LinguisticTerms.NP, null, sg, reader);
    	node.setTopic(topic);
    	return node;
    }
    
    /**	Finds the linguistic entry for this property or its inverse property
     */
    private DependencyTreeTransformer getTree(OntProperty p) throws IOException
    {	//find the lexicon entry for either this property or its inverse property
		try
		{	//try finding this property's entry
			return lexicon.map(p.getLocalName());
		}
		catch (IOException e)
		{	//if that fails, try finding the inverse
			OntProperty i = reader.getInverse(p);
			if (i == null) //if there is no inverse, there was no entry in the lexicon 
				throw e;		//for the property so we pass on the exception
			DependencyTreeTransformer dt = lexicon.map(i.getLocalName());
			dt.getGraph().setInverseInserted(true);
			return dt;
		}
    }
    
    /**	Finds the linguistic entry for this edge's property, inverse property or 
     *	superproperty
     */
    private DependencyTreeTransformer getTree(SGEdge edge, List<String> propsCovered, SGNode topic, boolean relativeClause)
    {
    	OntProperty p = reader.getProperty(edge.getLabel());
    	boolean inverse = false;
    	if (/**(!relativeClause) &&*/(p != null) && topic.equals(edge.getTarget()))
    	{	//if both normal and inverse properties have a linguistic spec, pick the one that focuses on the topic of the paragraph!
    		OntProperty i = reader.getInverse(p);
    		if (i != null)
    		{
    			inverse = true;
    			p = i;
    		}
    	}
    	propsCovered.add(edge.getLabel());   
	    DependencyTreeTransformer result = null;
	    OntProperty specProp = p;	//Property that has the specification attached
		
		try
		{
			if (edge.getLabel().equals(QueryEdge.ANYTHING))	//'anything' is obviously not a property in the ontology!
				return lexicon.map(edge.getLabel());
			else if (p == null)	//if this is not an ontology property, return null
	    		return null;
			else
				result = getTree(p);		//try finding the entry for this property
		}		
		catch (IOException e)
		{	//if it fails, try the superproperties
			for (Iterator it = p.listSuperProperties(); it.hasNext(); )
			{
				OntProperty parent = (OntProperty) it.next();
				if (p.equals(parent))
					continue;
				try
				{
					result = getTree(parent);	//if we have found a linguistic specification
					specProp = parent;
					break;
				}
				catch(IOException ex)
				{}
			}
		}
		
		if (result == null)
		{
			System.out.println("Property " + edge.getLabel() +	" is not in the lexicon");
			return null;
		}
		
		if (!edge.getLabel().equals(specProp.getLocalName()))
			propsCovered.add(specProp.getLocalName());
		for (OntProperty subProp : reader.getSubProperties(specProp))
		{
			if (subProp.getLocalName().equals(edge.getLabel()))
				continue;

			try
			{	//check if the subproperties have their own specification
				getTree(subProp);
			}
			catch (IOException ex)
			{	//if not, add them to the list of properties covered by this specification
				propsCovered.add(subProp.getLocalName());
			}
		}
		return result;
    }
    
    /**	Creates a summation node, a DTNode that summarises >4 edges of the same property
     *	(e.g. 'this state contains 23 cities).
     *	@param size Number of summarised edges
     *	@param label Property name
     *	@param dt DependencyTreeTransformer to add summation node to
     *	@return DTNode summation node
     */
    public static DTNode makeSummation(int size, String label, DependencyTreeTransformer dt)
    {
    	DTNode result = new DTNode(LinguisticTerms.NP, null, null, null);
    	DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, label.toLowerCase(), new Morph(LinguisticTerms.PLURAL));
    	if (size == 1)
    		noun.getMorph().setSingular(true);
    	DTNode mod = new DTNode(LinguisticTerms.ADJECTIVE, LinguisticTerms.MODIFIER, Lexicon.getNL(size), null);
    	
    	try
    	{
    		DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD, dt.getGraph().getFreeID(), result, noun);
    		DTEdge modEdge = new DTEdge(LinguisticTerms.MODIFIER, dt.getGraph().getFreeID(), result, mod);
    	}
    	catch(NameAlreadyBoundException e)
    	{	//impossible
    		e.printStackTrace();
    	}
    	return result;
    }
    
    /**	Creates a node that states '5 cities' rather than 'New York, Albany, ...'.
     *	Also adds an anchor that enables the user to request the full list.
     */
	private DTNode makeSummation(List<SGEdge> edges, SGNode topic, DependencyTreeTransformer dt)
    {
  	 	List<String> types = new ArrayList<String>();
    	for (SGEdge e : edges)
    	{
    		SGNode target = e.getTarget();
    		if (target.getID().equals(topic.getID()))
    			target = e.getSource();
    		types.add(target.getLabel());
    	}
    	String type = SGNode.getGenericNL(reader, reader.getMostGeneralClass(types).getLocalName());	//get the most generic class type of the collected nodes
   	
    	DTNode result = makeSummation(edges.size(), type, dt);
    	result.setAnchor(new SummationAnchor(edges));
    	return result;
    }
      
    /**	Adds relative clauses for any edges that have not been realised yet.
     */
    protected void addRelativeClauses() throws TextPlanningException
    {
    	for (int i = 0; i < relativeClauses.size(); i++)
    	{	//for each node in the graph
    		SGNode node = relativeClauses.get(i); 
    		List<DependencyTreeTransformer> list = lexicalise(node, true);	//lexicalise the edges that have not yet been mapped
    		if (list.size() > 1)	//this can actually happen, thanks to hidden nodes/edges
    		{	//in that case make a new paragraph
    			plan.addParagraph(node.getHeaderLabel(reader), node.getAnchor(), list);
    			continue;
    		}
    		else if (list.size() == 0)
    			continue;		//no relative clause needed for this property

    		int added = 0;
    		for (Iterator keys = plan.getParagraphHeaders(); keys.hasNext(); )	
    		{	//add the relative clause to a paragraph
    			String key = (String) keys.next();
    			List<DependencyTreeTransformer> trees = plan.getParagraphTrees(key);
    			for (int j = 0; j < trees.size(); j++)
    			{
    				int temp = trees.get(j).addRelativeClause(list.get(0));
    				if (temp == 1)
    				{
    					added = 1;
    					break;
    				}
    				else if (temp == 2)
    					added = 2;
    			}
    		}
    		if (added == 0)		//if impossible, add a new paragraph about this node
    			plan.addParagraph(node.getHeaderLabel(reader), node.getAnchor(), list);	
    	}
    }
    
    /**	Generates referring expressions for all UndeterminedDTNodes in the trees.
     */
    protected void generateReferringExpressions()
    {
    	for (Iterator it = plan.getParagraphHeaders(); it.hasNext(); )
    	{
    		String key = (String) it.next();
    		List<DependencyTreeTransformer> trees = plan.getParagraphTrees(key);
    		Map<String,List<UndeterminedDTNode>> prevTreeNodes = new HashMap<String, List<UndeterminedDTNode>>();
    		
	    	for (int i = 0; i < trees.size(); i++)
    		{	//for every tree
    			DependencyTreeTransformer dt = trees.get(i);
    			List<UndeterminedDTNode> nodes = new ArrayList<UndeterminedDTNode>();
    			
    			for (Iterator it2 = dt.getGraph().getNodes(); it2.hasNext(); )
    			{	//set the expressions of all undetermined dt nodes
    				DTNode node = (DTNode) it2.next();
    				if (node.isElided())	//don't bother generating an expression for a node that elided anyway
    					continue;		//Can we be entirely sure elision happened?
    				if (node instanceof UndeterminedDTNode)	//collect all nodes, and remember the order in which they appear if there is a conjunction
    				{
    					UndeterminedDTNode n = (UndeterminedDTNode) node;
    					n.setConjunctNr();
    					nodes.add(n);
    				}
    			}
    			//clean the list, setting the order nrs depending on the number of nodes of the same type
    			Map<String, List<UndeterminedDTNode>> sortedMap = new HashMap<String,List<UndeterminedDTNode>>();
    			for (int j = 0; j < nodes.size(); j++)		//sort into map of node lists, by type
    			{
    				String type = nodes.get(j).getType();
    				if (!sortedMap.containsKey(type))
    					sortedMap.put(type, new ArrayList<UndeterminedDTNode>());
    				sortedMap.get(type).add(nodes.get(j));
	   			}
    			
    			for (Iterator it2 = sortedMap.keySet().iterator(); it2.hasNext(); )
    			{	//clean the order nrs per type, so that there are no gaps
    				String k = (String) it2.next();
    				List<UndeterminedDTNode> list = sortedMap.get(k);
    				Collections.sort(list, new ConjunctNrComparator());	//sort the list in ascending order
    				Map<String, UndeterminedDTNode> processed = new HashMap<String, UndeterminedDTNode>();	//nodes already seen in this tree
    				int subtract = 0;
    				
    				for (int j = 0; j < list.size(); j++)
    				{
    					UndeterminedDTNode node = list.get(j);
    					if (node.isTopic());	//the topic must remain -1
    					else if (processed.containsKey(node.getSGID()))	//if we have already seen this node in this tree
    					{
    						subtract++;		//one less instance of this type than we thought!
    						node.setConjunctNr(processed.get(node.getSGID()).getConjunctNr());		//give this second occurrence the same conjunct number
    					}
    					else
    					{	//the rest must ascend without gaps (1,2,3)	
    						processed.put(node.getSGID(), node);
    						node.setConjunctNr(j - subtract);	
    					}
    					
	    				node.setExpression(prevTreeNodes, dt);	//now make a referring expression
					}
    			}
    			prevTreeNodes = sortedMap;	//remember that these nodes were used in previous tree
    		}	
    	}
    }
    
    /**	Helper class, compares and orders conjunct numbers of UndeterminedDTNodes
     */
    public class ConjunctNrComparator implements Comparator
	{
		public int compare(Object o1, Object o2) throws ClassCastException
		{
			int value1 = ((UndeterminedDTNode) o1).getConjunctNr();
			int value2 = ((UndeterminedDTNode) o2).getConjunctNr();
			
			if (value1 < value2)
				return -1;
			if (value1 == value2)
				return 0;
			return 1;
		}
	}
      
    /**	Maps a SGDateNode to a prepositional phrase, with the correct preposition.
     */
    private DTNode mapDate(SGDateNode node)
    {
    	DTNode np = new DTNode(LinguisticTerms.NP, LinguisticTerms.OBJECT, node.getNLLabel(reader), null);
    	DTNode pp = new DTNode(LinguisticTerms.PP, null, null, null);
    	DTNode prep = new DTNode(LinguisticTerms.PREP, LinguisticTerms.HEAD, node.getPreposition(false), null);
    	DTEdge prepEdge = new DTEdge(LinguisticTerms.HEAD);
    	DTEdge npEdge = new DTEdge(LinguisticTerms.OBJECT);
    	
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
    
    /**	Makes a paragraph representing the address, with the header specifying 
     *	whose address it is.
     */
    protected void mapAddress(SGAddressNode address) throws TextPlanningException
    {
    	if (address.mustRealise() >= SGNode.HIDE)
    		return;	
     	String nl = address.getRealisation();
     
     	if (nl.length() > 0)
     	{
     		List<AnchorString> header = address.getAddressHeader(reader);
     		String key = plan.newParagraph(header);
	 	   	DTNode mapping = new DTNode(LinguisticTerms.NP, LinguisticTerms.ROOT, nl, null);
     		plan.add(new DependencyTreeTransformer(mapping), key);
     		for (Iterator it = address.getEdges(); it.hasNext(); )
     			mappedEdges.add(((SGEdge) it.next()).getID());	//all edges should have been mapped now
     	}  	
    }
    
    /**	Makes a paragraph representing an abstract, with the header specifying 
     *	whose abstract it is.
     */    
    protected void mapAbstract(SGAbstractNode abstractNode) throws TextPlanningException
    {
	 	List<AnchorString> header = abstractNode.getHeader(reader);
     	DTNode mapping = new DTNode(LinguisticTerms.NP, LinguisticTerms.ROOT, abstractNode.getLabel(), null);
	 	String paragraph = plan.newParagraph(header);
	 	plan.add(new DependencyTreeTransformer(mapping), paragraph);
	 	for (Iterator it = abstractNode.getEdges(); it.hasNext(); )
	 		mappedEdges.add(((SGEdge) it.next()).getID());	//all edges should have been mapped now
    }
}