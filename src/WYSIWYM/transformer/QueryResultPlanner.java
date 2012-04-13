package WYSIWYM.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import simplenlg.lexicon.lexicalrules.rulesets.NounInflection;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.QueryResultGraph;
import WYSIWYM.model.SGAbstractNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.TextPlanningException;

/**
 *	Plans the brief description of a resource that has been found through a query.
 *
 * @author Feikje Hielkema
 *	@version 1.2 24-03-2008
 *
 *	@version 1.4 25-09-2008
 *	This version plans a description of all matches that have been found, which
 *	the user can navigate in the same way as the browsing module.
 */
public class QueryResultPlanner extends ContentPlanner
{
	/**	Constructs a ContentPlanner with the QueryResultGraph
	 *	@param r Ontology 
	 *	@param g QueryResultGraph	
	 */
    public QueryResultPlanner(OntologyReader r, QueryResultGraph g) 
    {
    	super(r, g);
    }
    
    /**	Overload
     *	Plans the graph to a content plan.
     *
     *	@return ContentPlan
     *	@throws TextPlanningException
     */
    public ContentPlan plan() throws TextPlanningException
    {
    	plan = new ContentPlan();
    	List<SGNode> list = makeParagraphs();
    	mappedEdges = new ArrayList<String>();   
    	addParagraph(getGraph().getRoots());
	
	   	for (int i = 0; i < list.size(); i++)
    	{
    		SGNode node = list.get(i);
    		if (node instanceof SGAddressNode)
    			mapAddress((SGAddressNode) node);
    		else if (node instanceof SGAbstractNode)
    			mapAbstract((SGAbstractNode) node);
    		else
    		{	
    			String header = node.getHeaderLabel(reader);
    			header = plan.newParagraph(header, node.getAnchor());		
    			List<DependencyTreeTransformer> trees = lexicalise(list.get(i));
    			trees = new Aggregator().aggregate(trees);
    			plan.add(trees, header);
    		}
    	}

    	addRelativeClauses();
    	generateReferringExpressions();
    	graph.stopFlashing();		//all edges now will have been realised once, so stop them flashing
    	return plan;
    }
    
    /**	Overload
     *	Determines the minimum number of paragraphs needed in the text. Each 
     *	paragraph describes a node in the graph.
     *	Each root in the queryresultgraph must have a paragraph of its own.
     *
     *	@return	List of nodes that should be paragraphs
     */
    protected List<SGNode> makeParagraphs()
    {
    	List<SGNode> paragraphs = new ArrayList<SGNode>();
    	Map<SGNode,List<SGEdge>> map = new HashMap<SGNode,List<SGEdge>>();
    	relativeClauses = new ArrayList<SGNode>();
    	
    	for (SGNode node : getGraph().getRoots())
    		if (node.getOutgoingEdgesWithoutNLNumber(reader) > 1)
    			paragraphs.add(node);
    	
    	for (Iterator it = getGraph().getNodes(); it.hasNext(); )
    	{
    		SGNode node = (SGNode) it.next();
    		if (paragraphs.contains(node))
    			continue;
    		if ((node instanceof SGAddressNode) || (node instanceof SGAbstractNode))	
    		{	//addresses and abstracts must receive separate paragraphs!
    			paragraphs.add(node);
    			continue;
    		}
    		if ((node instanceof DatatypeNode) || (node instanceof SGDateNode))
    			continue;	//don't add leaves (i.e. datatypes), or roots (already in there)
	
    		boolean paragraph = false;
    		int datatype = 0;
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
    				else	//if a node has just one datatype property, it doesn't necessarily require a separate paragraph
    				{
    					datatype++;
    					props.add(edge);
    				}
    			}
    			else
    				props.add(edge);	//nodes with only object properties we cannot be sure about yet
    		}
    		
			if ((!paragraph) && props.size() > 0)	//hold off judgement on this node
    			map.put(node, props);
	  	}
    	
    	//now we have a list of paragraphs and a list of nodes with object properties that we are not sure about.
    	List<SGNode> noParagraph = new ArrayList<SGNode>();
    	int tempSize = 0;
    	while (map.size() > 0)			//all nodes have been sorted, so stop
    	{
    		if (tempSize == map.size())	//no changes in previous loop!
    		{	//add the node with the most edges
    			int max = 0;
    			SGNode choice = null;
    			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
    			{
    				SGNode n = (SGNode) it.next();
    				int nr = map.get(n).size();
    				if (nr > max)
    				{
    					nr = max;
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
    				if (node.hasIncomingEdge(edge))
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
       	return paragraphs;		//and now, finally, we are done
    }
        
    /**	Adds an initial paragraph that states how many (and which) matches were found.
     */
    private void addParagraph(List<SGNode> matches) throws TextPlanningException
    {
    	Collections.sort(matches, new AlphabeticOrder());	//sort the results alphabetically
    	String header = getResultHeader(matches);  
    	plan.newParagraph(header, null);	//create the paragraph
    	
    	DependencyTreeTransformer trans = new DependencyTreeTransformer(new DependencyTree());		//now make the trees
    	if (matches.size() == 1)
    	{
    		DTNode node = new DTNode(LinguisticTerms.NP, LinguisticTerms.ROOT, matches.get(0).getNLLabel(reader), null);
    		node.setAnchor(matches.get(0).getAnchor()); 			
    		trans.getGraph().setRoot(node);
    	}
    	else if (matches.size() > 1)
    	{
	    	DTNode root = new DTNode(LinguisticTerms.CONJUNCTION, LinguisticTerms.ROOT, null, null);	
    		for (int i = 0; i < matches.size(); i++)
    		{	//make a conjunction of the matches; that can be the tree
    			try
    			{
	    			DTNode conjunct = new DTNode(LinguisticTerms.NP, LinguisticTerms.CONJUNCT, matches.get(i).getNLLabel(reader), null);
	    			conjunct.setAnchor(matches.get(i).getAnchor());
    				DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCT, trans.getGraph().getFreeID(), root, conjunct);
    				edge.setOrder(i);
    				trans.addEdge(edge);
    			}
    			catch (NameAlreadyBoundException e)
    			{
    				e.printStackTrace();
    			}
    		}
    		trans.getGraph().setRoot(root);
    	}
    	plan.add(trans, header);
    }
    
    /**	Returns the header of the first initial paragraph
     */
    private String getResultHeader(List<SGNode> matches)
    {
    	StringBuffer sb = new StringBuffer("The following ");
    	String type = matches.get(0).getLabel();
    	if (matches.size() == 1)
    	{
    		sb.append(SGNode.normalise(type));
    		sb.append(" matches your search term:");
    	}
    	else
    	{
    		sb.append(NounInflection.PLURAL.apply(SGNode.normalise(type)));
    		sb.append(" match your search term:");
    	}
    	return sb.toString();
    }
  
    private QueryResultGraph getGraph()
    {
    	return (QueryResultGraph) graph;
    }
    
    /**	Orders the results in the first initial graph alphabetically by nl-representation
     */
    private class AlphabeticOrder implements Comparator
    {
    	public int compare(Object o1, Object o2)
    	{
    		String node1 = ((SGNode) o1).getNLLabel(reader);
    		String node2 = ((SGNode) o2).getNLLabel(reader);
    		return node1.compareToIgnoreCase(node2);
    	}
    }  
}