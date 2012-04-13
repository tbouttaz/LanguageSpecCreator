package WYSIWYM.testclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.transformer.ContentPlanner;
import WYSIWYM.transformer.DependencyTreeTransformer;
import WYSIWYM.util.TextPlanningException;

/**	Acts as a ContentPlanner, but plans two different text types, that
 *	use no syntactic aggregation. This class was used only for the text comparison experiment.
 *
 *	@author	Feikje Hielkema
 *	@version 1.0 22-01-2009
 */
public class TextTypesPlanner extends ContentPlanner
{
	/**	Constructs a text planner with a semantic graph
	 *
	 *	@param r Ontology
	 *	@param g SemanticGraph	
	 */
    public TextTypesPlanner(OntologyReader r, SemanticGraph g) 
    {
    	super(r, g);
    }
	   
	/** Plans the text according to the specified condition
	 *	@param condition 1 for no aggregation, 2 for basic aggregation
	 *	@return ContentPlan
	 *	@throws TextPlanningException
	 */
    public ContentPlan plan(int condition) throws TextPlanningException
    {
    	plan = new ContentPlan();
    	List<SGNode> list = makeParagraphs();
    	mappedEdges = new ArrayList<String>();
    	    	
    	for (int i = 0; i < list.size(); i++)
    	{
    		SGNode node = list.get(i);
    		String header = node.getHeaderLabel(reader);
    		header = plan.newParagraph(header, node.getAnchor());
    		List<SGEdge> edges = new ArrayList<SGEdge>();
    		
    		if (condition == 2)
    			edges = order(node);
    		else
 				for (Iterator it = node.getEdges(); it.hasNext(); )
	    			edges.add((SGEdge) it.next());

    		List<DependencyTreeTransformer> trees = lexicalise(node, edges, condition);
    		plan.add(trees, header);
    	}

    	generateReferringExpressions();
    	return plan;
    }
    
    private List<SGEdge> order(SGNode node)
    {	
    	List<String> labels = new ArrayList<String>();
    	Map<SGEdge,List<SGNode>> edgeMap = new HashMap<SGEdge,List<SGNode>>();
    	Map<SGNode,List<SGEdge>> nodeMap = new HashMap<SGNode,List<SGEdge>>();
    	
    	for (Iterator it = node.getEdges(); it.hasNext(); )
    	{	//group edges with same label
    		SGEdge edge = (SGEdge) it.next();
    		if (labels.contains(edge.getLabel()))
    			continue;	//already sorted this edge
    		if ((edge.mustRealise() >= SGNode.HIDE) && (node.mustRealise() >= SGNode.HIDE))
    			continue;		//if the edge doesn't have to be realised (because it was extracted from the database), do nothing
    		if (!Lexicon.specExists(edge.getLabel(), reader))
    			continue;	//skip those without spec
    		if (reader.useAsProperName(edge.getLabel()))	
    			continue;		//don't map the name edges		

    		labels.add(edge.getLabel());	//remember we've seen this label
    		List<Edge> list = node.getEdges(edge.getLabel());	//get all edges with this label
    		if (list.size() > 1)
    		{	//and add their target nodes to a list
    			List<SGNode> nodes = new ArrayList<SGNode>();
    			for (Edge e : list)
    			{
    				SGNode target = (SGNode) e.getTarget();
    				if (target == node)
    					target = (SGNode) e.getSource();
    				nodes.add(target);
    			}	//put that list in map
    			edgeMap.put(edge, nodes);	
    		}
    		else
    		{	//group the other edges by target node
    			SGNode target = (SGNode) edge.getTarget();
    			if (target == node)
    				target = (SGNode) edge.getSource();
    			
    			if (nodeMap.containsKey(target))
    				nodeMap.get(target).add(edge);
    			else
    			{
    				List<SGEdge> l = new ArrayList<SGEdge>();
    				l.add(edge);
    				nodeMap.put(target, l);
    			}
    		}
    	}
    	   	
    	List<SGEdge> result = new ArrayList<SGEdge>();
    	for (Iterator it = edgeMap.keySet().iterator(); it.hasNext(); )
    	{	//add an aggregated sentence, then see if any of the target nodes are also in nodeMap
    		SGEdge edge = (SGEdge) it.next();
    		result.add(edge);
    		List<SGNode> nodes = edgeMap.get(edge);
    		for (Iterator it2 = nodeMap.keySet().iterator(); it2.hasNext(); )
    		{	//check if one of the nodes in nodeMap is in the list
    			SGNode n = (SGNode) it2.next();
    			if (nodes.contains(n))
    			{	//if so, add all edges with that node to result
    				result.addAll(nodeMap.get(n));	
    				it2.remove();
    				break;
    			}
    		}
    	}
    	for (Iterator it = nodeMap.keySet().iterator(); it.hasNext(); )
    		result.addAll(nodeMap.get((SGNode) it.next()));

    	return result;
    }
    
    /**	Lexicalises all edges of this node that haven't been added to a previous
     *	paragraph, and returns a list with the resulting dependency trees.
     *
     *	Unlike the contentplanner, this class does not use relative clauses, but 
     *	they are planned when the paragraphs are assigned; so when a relative clause
     *	comes along, we need to lexicalise it here and add it to this paragraph.
     *
     *	@param topic	SGNode topic of the paragraph
     *	@return List<DependencyTreeTransformer>
     */
    protected List<DependencyTreeTransformer> lexicalise(SGNode topic, List<SGEdge> edges, int condition)
    {
    	List<DependencyTreeTransformer> result = new ArrayList<DependencyTreeTransformer>();
    	SGNode prevTarget = null;		//remember previous target node
    	for (int i = 0; i < edges.size(); i++)
    	{
    		SGEdge edge = edges.get(i);
    		DependencyTreeTransformer dt = lexicaliseEdge(topic, edge, false);
    		if (dt == null)
    			continue;
    		result.add(dt);
   
    		SGNode target = edge.getTarget();
    		if (target == topic)
    			target = edge.getSource();
    		
    		if (relativeClauses.contains(target))
    		{	//find the edge that has not been assigned to another paragraph, and lexicalise it
    			for (Iterator it2 = target.getEdges(); it2.hasNext(); )
    			{
    				SGEdge e = (SGEdge) it2.next();
    				if ((e.mustRealise() >= SGNode.HIDE) && (topic.mustRealise() >= SGNode.HIDE))
    					continue;	//if the edge doesn't have to be realised (because it was extracted from the database), do nothing
    				if (mappedEdges.contains(e.getID()))
    					continue;		//don't map edges that have already been mapped
    				if (!Lexicon.specExists(e.getLabel(), reader))
    					continue;
    				if (reader.useAsProperName(e.getLabel()))	
    					continue;		//don't map the name edges		
    				
    				SGNode t = edge.getTarget();
    				if (t == target)
    					t = edge.getSource();
    				if (paragraphs.contains(t))
    					continue;	//ignore edges assigned to other paragraphs
    				
    				DependencyTreeTransformer subdt = lexicaliseEdge(target, edge, false);
    				if (subdt != null)
    				{
    					prevTarget = null;
		   				result.add(subdt);
		   			}
    			}
    		}
    		else if ((condition == 2) && (prevTarget != null))
    		{	//check if this edge and the previous one have the same target, and are both unaggregated
    			DependencyTreeTransformer prevDT = result.get(result.size() - 2);
    			if ((!(dt.getGraph().isAggregated() || prevDT.getGraph().isAggregated())) && (target == prevTarget))
				{	//remove the last two trees from result
					result.remove(result.size() - 1);
					result.remove(result.size() - 1);
					result.add(combine(dt, prevDT));
				}
    		}
    		prevTarget = target;
    	}
    	return result;
    }
    
	private DependencyTreeTransformer combine(DependencyTreeTransformer... trees)
	{
		System.out.println("Combining 2 trees!");
		DTNode conjunction = new DTNode(LinguisticTerms.CONJUNCTION, LinguisticTerms.ROOT, null, null);
		DTNode conjunctor = new DTNode(LinguisticTerms.CONJUNCTOR, LinguisticTerms.CONJUNCTOR, "and", null);
		DependencyTreeTransformer result = new DependencyTreeTransformer(conjunction);
		
		try
		{
			int cntr = 0;
			for (DependencyTreeTransformer tree : trees)
			{	//add all trees in the combination as conjuncts
				DTNode root = (DTNode) tree.getGraph().getRoot();
				root.setDeplbl(LinguisticTerms.CONJUNCT);
				DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCT, result.getGraph().getFreeID(), conjunction, root);
				edge.setOrder(cntr++);	//mark the order of the constituents - v. important for elision!
				result.addBranchWithNewIDs(root);
				result.addEdge(edge);
			}

			DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCTOR, result.getGraph().getFreeID(), conjunction, conjunctor);
			edge.setOrder(cntr);
			result.addEdge(edge);
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("NameAlreadyBoundException shouldn't be possible here!");
			e.printStackTrace();
		}
		return result;
	}
}