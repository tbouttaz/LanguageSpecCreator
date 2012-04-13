package WYSIWYM.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Edge;
import WYSIWYM.model.Morph;
import WYSIWYM.model.Node;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.QueryGraph;
import WYSIWYM.model.QueryPlan;
import WYSIWYM.model.QueryValue;
import WYSIWYM.model.QueryValueNode;
import WYSIWYM.model.RealiseEdgeComparator;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.model.SequenceEdgeComparator;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.TextPlanningException;

/**
 *	QueryPlanner plans a query; it creates a query plan from a
 *	semantic graph.
 *
 * @author Feikje Hielkema
 *	@version 1.2 15-01-2008
 */
public class QueryPlanner extends TextPlanner
{
	private QueryPlan queryPlan;
	private List<SGNode> mapped = new ArrayList<SGNode>();
	private List<QueryEdge> plannedEdgeOrder;
	
	/**	Constructs an empty query planner, taking only an ontology reader
	 *
	 *	@param r Ontology
	 */
    public QueryPlanner(OntologyReader r) 
    {
    	super(r);
    }
    
    /**	Constructs a query planner with a semantic graph
	 *
	 *	@param r Ontology
	 *	@param g SemanticGraph	
	 */
    public QueryPlanner(OntologyReader r, SemanticGraph g) 
    {
		super(r, g);
    }
    
    /**	Returns the plan.
     *	@return QueryPlan
     */
    public QueryPlan getPlan()
    {
    	return queryPlan;
    }
    
    /**	Initiates the text planning, sorting the edges into paragraphs and mapping
     *	each to a dependency tree
     *
     *	@param sg SemanticGraph
     *	@throws IOException if a linguistic specification could not be found
     *	@throws TextPlanningException
     */
    public void plan(SemanticGraph sg) throws IOException, TextPlanningException
    {
    	plannedEdgeOrder = new ArrayList<QueryEdge>();
    	setGraph(sg);
    	mapped = new ArrayList<SGNode>();
    	SGNode root = (SGNode) sg.getRoot();	
    	queryPlan = new QueryPlan(root);
		plan(root, null, true);
    	sg.stopFlashing();		//all edges now will have been realised once, so stop them flashing
    }
    
    /**	Sorts the edges of this node by edge label and sequence number
     *
     *	@param	Iterator
     *	@param	SGNode, the source of the edges to be sorted
     *	@return	ArrayList
     */
    private List sortEdges(SGNode source)
    {
    	SGEdge[] tempArray = new SGEdge[source.getOutgoingEdgesWithoutIDNumber()];
    	int cntr = 0;
    	for (Iterator it = source.getOutgoingEdges(); it.hasNext(); )
    	{
    		SGEdge edge = (SGEdge) it.next();
   			if (edge.getLabel().equals("ID"))
   				continue;
    		tempArray[cntr] = edge;
    		cntr++;
    	}	//sort the edges according to age
     	Arrays.sort(tempArray, new SequenceEdgeComparator());	//sort these according to which sequence they were added
    
    	List result = new ArrayList();
    	List<SGEdge> sorted = new ArrayList<SGEdge>();
    	for (int i = 0; i < tempArray.length; i++)
    	{
    		if (sorted.contains(tempArray[i]))
    			continue;
    		List<Edge> edges = source.getOutgoingEdges(tempArray[i].getLabel());
    		if (edges.size() == 1)
    			result.add(edges.get(0));
    		else if (edges.get(0).getTarget() instanceof QueryValueNode)
    			result.addAll(edges);
    		else
    		{
    			SGEdge[] aggregate = new SGEdge[0];
    			aggregate = (SGEdge[]) edges.toArray(aggregate);
    			Arrays.sort(aggregate, new RealiseEdgeComparator());	//sort these according to which sequence they were added
    			result.add(aggregate);	//add the list with double nodes; they will be aggregated later on
    		}
    		
    		for (int j = 0; j < edges.size(); j++)
    			sorted.add((SGEdge) edges.get(j));
    	}
    	return result;
    }
    
    private void plan(SGNode source, QueryPlan.QueryItem parent, boolean root) throws IOException, TextPlanningException
    {
    	if (mapped.contains(source))
    		return;
    	
    	mapped.add(source);
    	List edgeList = sortEdges(source);
    	if (source instanceof SGAddressNode)
    		edgeList = ((SGAddressNode)source).getOrderedAddressEdges();
    	for (int i = 0; i < edgeList.size(); i++)
    	{
    		Object o = edgeList.get(i);
    		if (o instanceof SGEdge)
    			mapEdge((SGEdge) o, parent, root);
    		else //if it's a list
    			mapAggregate((SGEdge[])o, parent, root);
    	}
    }
    
    private void mapEdge(SGEdge edge, QueryPlan.QueryItem parent, boolean root) throws IOException, TextPlanningException
    {
    	DependencyTreeTransformer dt = getTree(edge);
    	dt.getGraph().setFlash(edge.flash());
    	boolean inverse = dt.getGraph().getInverseInserted();
		if (inverse)
			dt.setPassive();
		
		SGNode sgSource = edge.getSource();
		DTNode dtSource = mapCurrentTopic(sgSource);
		if (inverse)
			dt.insert(dtSource, Lexicon.TARGET);
		else
			dt.insert(dtSource, Lexicon.SOURCE);
		
		SGNode sgTarget = edge.getTarget();
		if (!(sgTarget instanceof SGBooleanNode))
		{	//insert target
    		DTNode dtTarget = map(sgTarget, dt.getGraph(), edge.getLabel());
    		if (inverse)
    			dt.insert(dtTarget, Lexicon.SOURCE);
    		else
    			dt.insert(dtTarget, Lexicon.TARGET);
    	}
    	else
    	{
    		Boolean b = (Boolean) ((SGBooleanNode)sgTarget).getValue();
			if (!b.booleanValue())
 		   		dt.setNegated(true);
 		}
    		
    	//transform it to a query tree
		SpecificationTransformer transform = new SpecificationTransformer(dt.getGraph());
		if (!transform.toQuery(root))
			throw new TextPlanningException("Could not transform the specification to a query!");
    	
    	QueryPlan.QueryItem item = queryPlan.addItem(transform.getGraph(), parent);
    	plannedEdgeOrder.add((QueryEdge) edge);
    	plan(edge.getTarget(), item, false);
    }
    
    /**	In the Query Planner, with the property 'any relation', the target nodes 
     *	are not necessarily of the same class or tree. In that case we make a conjunction,
     *	e.g. '3 rivers and 2 lakes'.
     */
    private DTNode makeSummation(SGEdge[] edges, DependencyTreeTransformer dt)
    {
    	Map<String, Integer> types = new HashMap<String, Integer>();
   		for (int i = 0; i < edges.length; i++)
   		{
   			Node target = edges[i].getTarget();
    		String lb = target.getLabel();
    		if (types.containsKey(lb))
    			types.put(lb, new Integer(types.get(lb).intValue() + 1));
    		else
    			types.put(lb, new Integer(1));
   		}
   		
    	if (types.size() == 1)
    		return ContentPlanner.makeSummation(edges.length, types.keySet().iterator().next(), dt);

    	DTNode result = new DTNode(LinguisticTerms.CONJUNCTION, null, null, null);
    	for (Iterator it = types.keySet().iterator(); it.hasNext(); )
    	{	//make a conjunction (e.g. '2 rivers and 3 mountains'
    		String type = (String) it.next();
    		int nr = types.get(type).intValue();
    		DTNode target = ContentPlanner.makeSummation(nr, type, dt);
    		try
    		{
    			DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCT, dt.getGraph().getFreeID(), result, target);
    		}
    		catch(NameAlreadyBoundException e)
 		   	{	//impossible
    			e.printStackTrace();
    		}
    	}
    	return result;
    }
    
    private void mapAggregate(SGEdge[] edges, QueryPlan.QueryItem parent, boolean root) throws IOException, TextPlanningException
    {	//get the dependency tree
    	DependencyTreeTransformer dt = getTree(edges[0]);
 	   	boolean inverse = dt.getGraph().getInverseInserted();
        
        SGNode sgSource = edges[0].getSource();
		DTNode dtSource = mapCurrentTopic(sgSource);
		DTNode target = makeSummation(edges, dt);	
		if (inverse)
		{
			dt.insert(target, Lexicon.SOURCE);
			dt.insert(dtSource, Lexicon.TARGET);
		}
		else
		{
			dt.insert(target, Lexicon.TARGET);
			dt.insert(dtSource, Lexicon.SOURCE);
        }
		dt.getGraph().setFlash(edges[0].flash());
		
		//transform it to a query tree
		SpecificationTransformer transform = new SpecificationTransformer(dt.getGraph());
		if (!transform.toQuery(root))
			throw new TextPlanningException("Could not transform the specification to a query!");

    	QueryPlan.QueryItem item = queryPlan.addItem(transform.getGraph(), parent);
    	QueryEdge qEdge = (QueryEdge) edges[0];
    	plannedEdgeOrder.add(qEdge);	//add the first edge to the ordered list
    	qEdge.setSameOptionalValue(edges);	//and tell it that the others should have the same optional value
    	
    	for (int i = 0; i < edges.length; i++)
    	{	//recurse, giving each target that has outgoing edges its own sublist (e.g. 'John: who has ....')
    		SGNode sgTarget = edges[i].getTarget();
			DTNode dtTarget = map(sgTarget, i + 1);	//map(sgTarget, dt.getGraph(), edges[i].getLabel());
			plannedEdgeOrder.add((QueryEdge) edges[i]);	//add this edge to the list
    		QueryPlan.QueryItem child = queryPlan.addItem(new DependencyTree(dtTarget), item);
    		if (sgTarget.getOutgoingEdgesWithoutIDNumber() > 0)	
	    		plan(edges[i].getTarget(), child, false);
    	}		   	
    }
    /**	Maps the given QueryValueNode to a DTNode. The nl-representation will include proper
     *	realisations of boolean and numeric operators, e.g. 'before 2000 and after 1990'
     *	@param node QueryValueNode
     *	@param dt DependencyTree to add DTNode to
     *	@param property Property name, to check if it's the 'any property', which affects the nl-representation
     *	@return DTNode
     *	@throws TextPlanningException
     */
    public DTNode map(QueryValueNode node, DependencyTree dt, String property) throws TextPlanningException
    {
    	int op = node.getBooleanOperator();
    	List<QueryValue> children = node.getValues();	//qValue.getChildren();
		String root = children.get(0).realise(property.equals(QueryEdge.ANYTHING), reader);
    	SGNode sg = children.get(0).getValue();
    	int comp = children.get(0).getComparator();

    	if (children.size() == 1)		//this queryvalue contains only one sgnode or child value; so realise that
    	{			
    		DTNode np = new DTNode(LinguisticTerms.NP, null, root, null);
	   		if (node.isQuote())
   				np.setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
   			if (sg instanceof SGDateNode)
	  			return mapDate((SGDateNode) sg, np, comp, property.equals(QueryEdge.ANYTHING));
	  			
	  		np.setFlash(node.flash());		
	   		return np;   	
    	}
    	
    	if (children.size() == 1)
    	{	//make an NP with as head a Noun
    		DTNode np = new DTNode(LinguisticTerms.NP, null, null, null);
    		DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, children.get(0).realise(reader), null);
    		DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD);
    	
    		try
    		{
	    		nounEdge.setSource(np);
    			nounEdge.setTarget(noun);
   		   		if (op == QueryGraph.NOT)
   		   		{	//if the phrase contains a determiner, make a separate determiner node
    				DTNode modNode = new DTNode(LinguisticTerms.ADJECTIVE, LinguisticTerms.MODIFIER, node.getOperatorNL(property.equals(QueryEdge.ANYTHING)), null);  		
    				DTEdge modEdge = new DTEdge(LinguisticTerms.MODIFIER);
    				modEdge.setSource(np);
    				modEdge.setTarget(modNode);
    			}
    			else
    				noun.setRoot(children.get(0).realise(property.equals(QueryEdge.ANYTHING), reader));
	    	}
    		catch (NameAlreadyBoundException e)
    		{
    			System.out.println("An exception here should not be possible!");
    			e.printStackTrace();
    		}
   			if (node.isQuote())
   				noun.setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
	  		np.setFlash(node.flash());
	  		if (children.get(0).getValue() instanceof SGDateNode)
	  			return mapDate((SGDateNode) children.get(0).getValue(), np, children.get(0).getComparator(), property.equals(QueryEdge.ANYTHING));
	  		return np;
    	}
    	
    	DTNode conjunction = new DTNode(LinguisticTerms.CONJUNCTION, null, null, null);
    	try
    	{  	//Create a conjunction
	    	for (int i = 0; i < children.size(); i++)
	    	{	//add the conjuncts
	    		DTNode conjunct = new DTNode(LinguisticTerms.NP, LinguisticTerms.CONJUNCT, children.get(i).realise(reader), null);
	   		 	DTEdge conjunctEdge = new DTEdge(LinguisticTerms.CONJUNCT);
	   		 	conjunctEdge.setID(dt.getFreeID());
	   		 	conjunctEdge.setOrder(i);
		   		if (node.isQuote())			//'x' or 'y', instead of 'x or y'
   					conjunct.setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
   				
   				if (children.get(i).getValue() instanceof SGDateNode)
	  				conjunct = mapDate((SGDateNode) children.get(i).getValue(), conjunct, children.get(i).getComparator(), false);	//don't add np
	  			
	  			conjunctEdge.setSource(conjunction);
		   		conjunctEdge.setTarget(conjunct);
	    	}
			//create a conjunctor: either 'and', 'or' or 'nor'
		   	DTNode conjunctor = new DTNode(LinguisticTerms.CONJUNCTOR, LinguisticTerms.CONJUNCTOR, /**qValue*/node.getOperatorNL(), null);	//op);
		   	DTEdge conjunctorEdge = new DTEdge(LinguisticTerms.CONJUNCTOR);
		   	conjunctorEdge.setOrder(children.size());
		   	conjunctorEdge.setSource(conjunction);
		   	conjunctorEdge.setTarget(conjunctor);
				
			if (op == QueryGraph.NOT)
			{	//create a modifier 'not'
				DTNode modifier = new DTNode(LinguisticTerms.ADJECTIVE, LinguisticTerms.MODIFIER, "neither", null);
				DTEdge modEdge = new DTEdge(LinguisticTerms.MODIFIER);
				if (children.size() > 1)
					modEdge.setOrder(children.size() + 1);
				else
					modEdge.setOrder(children.size());
			   	modEdge.setSource(conjunction);
			   	modEdge.setTarget(modifier);
			}
			
			DTNode np = children.get(0).getAnyPropertyNP();	//try to get an np representing 'a date' or 'a number'
			if (property.equals(QueryEdge.ANYTHING) && (np != null))
			{
				DTEdge npEdge = new DTEdge(LinguisticTerms.PPMODIFIER, np, conjunction);
				conjunction.setDeplbl(LinguisticTerms.PPMODIFIER);
				np.setFlash(node.flash());
				return np;
			}
	    }
	    catch(NameAlreadyBoundException e)
	    {
	    	System.out.println("Exception here should be impossible!!");
	    	e.printStackTrace();
	    }	
	    
	    conjunction.setFlash(node.flash());	
	    return conjunction;
    }
    
    /**	Maps the given SGNode to a DTNode with a Noun Phrase
     *	@param node SGNode
     *	@param nr Order number
     *	@return DTNode
     */
    public DTNode map(SGNode node, int nr)
    {
    	//make an NP with as head a Noun
    	DTNode np = new DTNode(LinguisticTerms.NP, null, null, null);
    	DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, node.getNLLabel(true, reader), null);
    	DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD);
    	DTNode detNode = new DTNode(LinguisticTerms.DET, LinguisticTerms.DET, Lexicon.getRankNL(nr), null);  		
    	DTEdge detEdge = new DTEdge(LinguisticTerms.DET);
    	
    	try
    	{
	    	nounEdge.setSource(np);
    		nounEdge.setTarget(noun);	
    		detEdge.setSource(np);
    		detEdge.setTarget(detNode);
    	}
    	catch (NameAlreadyBoundException e)
    	{
    		e.printStackTrace();
    	}
    	
    	np.setFlash(node.flash());		
   		np.setAnchor(node.getAnchor());
   		return np;
    }
    
    /**	Overload: Maps the given node to a DTNode representing a Noun Phrase
     *
     *	@param	node SGNode to be mapped
     *	@param 	dt DependencyTree to add DTNode to
     *	@param	property Property name
     *	@return	DTNode
     *	@throws TextPlanningException
     */
    public DTNode map(SGNode node, DependencyTree dt, String property)  throws TextPlanningException
    {  	
    	if (node instanceof QueryValueNode)
    		return map((QueryValueNode) node, dt, property);
 	
    	//make an NP with as head a Noun
    	DTNode np = new DTNode(LinguisticTerms.NP, null, null, null);
    	DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, node.getNLLabel(true, reader), null);
    	DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD);
    	
    	try
    	{
	    	nounEdge.setSource(np);
    		nounEdge.setTarget(noun);
   		   	
   		   	String det = node.getDeterminer(SGNode.A, true, reader);
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
	    	np.setSGID(node.getSGID());	//unique identifier should show up in surface form
   		if (node.isQuote())
   			np.setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
   		
		np.setPronoun(node.getPronoun());
  		np.setFlash(node.flash());		
   		if (!mapped.contains(node))
   			np.setAnchor(node.getAnchor());
		np.setPerson(node.getLabel().equalsIgnoreCase("person"));
   		return np;   	
    }
    
    /**	Overload. Maps the topic of the paragraph to a DTNode.
     *	@param node SGNode
     *	@return DTNode
     */
    public DTNode mapCurrentTopic(SGNode node)
    {
    	DTNode result = super.mapCurrentTopic(node);
    	result.setPerson(node.getLabel().equalsIgnoreCase("person"));
    	return result;
    }
    
    /**	Maps a SGDateNode to a prepositional phrase, with the correct preposition
     *	if 'any' is true, the realisation should be an NP: 'x's description mentions
     *	a date before 2008' or similar
     *
     *	@param node SGDateNode
     *	@param np DTNode with Noun Phrase, which will be part of PP
     *	@param comparator Numeric operator (in this context before, after or during)
     *	@param any True if user selected 'any property'
     *	@return DTNode
     */
    public DTNode mapDate(SGDateNode node, DTNode np, int comparator, boolean any)
    {
    	String preposition = node.getPreposition(true);
    	if (comparator == 1)
    		preposition = "before";
    	else if (comparator == 2)
    		preposition = "after";
    	
    	DTNode pp = new DTNode(LinguisticTerms.PP, null, null, null);
    	DTNode prep = new DTNode(LinguisticTerms.PREP, LinguisticTerms.HEAD, preposition, null);
    	DTEdge prepEdge = new DTEdge(LinguisticTerms.HEAD);
    	DTEdge npEdge = new DTEdge(LinguisticTerms.OBJECT);
    	pp.setDeplbl(np.getDeplbl());		//if np was a conjunct, pp should now get that deplbl
    	np.setDeplbl(LinguisticTerms.OBJECT);
    	
    	try
    	{
    		prepEdge.setSource(pp);
    		prepEdge.setTarget(prep);
    		npEdge.setSource(pp);
    		npEdge.setTarget(np);
    		
    		if (any)
    		{
    			DTNode result = QueryValue.getAnyPropertyNP(0);
    			DTEdge resultEdge = new DTEdge(LinguisticTerms.PPMODIFIER, result, pp);
    			return result;
    		}
    		else
    			return pp;
    	}
    	catch (NameAlreadyBoundException e)
    	{
    		System.out.println("An exception here should not be possible!");
    		e.printStackTrace();
    		return null;
    	}
    }
    
    /*	Returns list of planned edges, in the order they will be realised
     *	@return List<QueryEdge>
     */
   	public List<QueryEdge> getPlannedEdgeList()
    {
    	return plannedEdgeOrder;
    }
}