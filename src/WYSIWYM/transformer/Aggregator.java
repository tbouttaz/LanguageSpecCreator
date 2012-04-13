package WYSIWYM.transformer;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Edge;
import WYSIWYM.model.Morph;
import WYSIWYM.model.SummationAnchor;
import WYSIWYM.model.UndeterminedDTNode;
import WYSIWYM.util.Dotter;

/**
 *	Aggregator performs syntactic aggregation within a paragraph.
 *	It orders a list of dependency trees to enable the maximum amount of
 *	ellipsis, then performs the planned aggregations.
 *	See thesis for theory and explanation 
 *	(F. Hielkema (2009): Using Natural Language Generation to Provide Access to Semantic Metadata, University of Aberdeen)
 *
 * @author Feikje Hielkema
 * @version 1.4 2008/07/29
 */
public class Aggregator 
{
	private List<DependencyTreeTransformer> input = new ArrayList<DependencyTreeTransformer>();
	private List<DependencyTreeTransformer> aggregatedTrees = new ArrayList<DependencyTreeTransformer>();

	/**	Aggregator orders a list of dependency trees to enable the maximum amount of
	 *	ellipsis, then performs the planned aggregations.
	 *	
	 *	@param trees List containing DependencyTreeTransformers to be aggregated
	 *	@return List<DependencyTreeTransformer> with all trees (aggregated and untouched) ordered and ready for REG.
	 */
	public List<DependencyTreeTransformer> aggregate(List<DependencyTreeTransformer> trees)
	{
		if (trees.size() == 1)	//if there is only one tree there is nothing to aggregate
			return trees;
		
		input = trees;
		elide();			//find the best grouping for aggregation and elide where possible
		List<DependencyTreeTransformer> result = orderTrees();	//order the remaining trees for maximum coherence
		for (DependencyTreeTransformer tree : result)
			tweakAggregate((DTNode) tree.getGraph().getRoot(), tree);
		return result;
	}
	
	/**	Orders the aggregated and non-aggregated trees. Current method only sorts
	 *	into 'long - short sentence' patterns; but really we should look at 
	 *	coherence too ?!
	 */
	private List<DependencyTreeTransformer> orderTrees()
	{
		List<DependencyTreeTransformer> result = new ArrayList<DependencyTreeTransformer>();
		while (!(aggregatedTrees.isEmpty() || input.isEmpty()))
		{	//alternate long and short sentences until one of the lists is empty
			result.add(aggregatedTrees.get(0));
			result.add(input.get(0));
			aggregatedTrees.remove(0);	//remove them from their original lists
			input.remove(0);
		}
		
		result.addAll(input);	//then add the remaining trees and return
		result.addAll(aggregatedTrees);	
		return result;
	}
		
	/**	If the two nodes and their branches are equal, it returns the nodes;
	 *	if one of their subnodes are equal, it returns those; if the two branches
	 *	are completely different, it returns an empty list.
	 */ 
	private List<DTNode> findEqualNodes(DTNode node1, DTNode node2)
	{
		boolean identical = false;
		List<DTNode> result = new ArrayList<DTNode>();
		if ((node1 == null) || (node2 == null))	//definitely not identical...
			return result;
		
		if (!node1.getDeplbl().equals(node2.getDeplbl()))
		{	//the object of a passive tree is identical or equivalent to a ppmod
			if (node1.getDeplbl().equals(LinguisticTerms.SUBJECT) && node2.getDeplbl().equals(LinguisticTerms.PPMODIFIER) && compareObjectAndPP(node1, node2))
				identical = true;	//they are identical
			else if (node2.getDeplbl().equals(LinguisticTerms.SUBJECT) && node1.getDeplbl().equals(LinguisticTerms.PPMODIFIER) && compareObjectAndPP(node2, node1))
				identical = true;		//they are identical
			else
				return result;	//not identical, so return empty list
		}
		else if (!compareMorphs(node1.getMorph(), node2.getMorph()))
			return result;
		else if ((node1 instanceof UndeterminedDTNode) || (node2 instanceof UndeterminedDTNode))
		{
			if ((node1 instanceof UndeterminedDTNode) && (node2 instanceof UndeterminedDTNode))
			{
				if ((node1.getSGID() == null) || (node2.getSGID() == null))
					System.out.println("SG ID IS NULL FOR UNDERTERMINEDDTNODE - WHY?");
				else if (!node1.getSGID().equals(node2.getSGID()))
					return result;	//not identical
				else
					identical = true;
			}
			else
				return result;		//not identical
		}
		else if (node1.isLeaf() || node2.isLeaf())
		{
			if (node1.isLeaf() && node2.isLeaf())
			{	//if both nodes are leaves, compare the root
				if (!node1.getRoot().equals(node2.getRoot()))
					return result;
				else
					identical = true;	//if they are leaves and everything else if identical, they must be identical
			}
			else 
				return result;
		}
	
		if (!identical)		//if the nodes are different so far, check their children
		{
			List<DTNode> equal = new ArrayList<DTNode>();
			for (Iterator it = node1.getOutgoingEdges(); it.hasNext(); )
			{
				DTEdge edge = (DTEdge) it.next();
				DTNode target = edge.getTarget();
				List<DTNode> list = node2.getDepChildren(edge.getLabel());
				for (int i = 0; i < list.size(); i++)
					equal.addAll(findEqualNodes(target, list.get(i)));	//add all subnodes that are equal
			}
			if (equal.size() == 0)	//no equal subnodes
				return result;
			
			int size = (equal.size() / 2);
			if (equal.get(0).getParent().getID().equals(node1.getID()) && (size == node1.getOutgoingEdgeNr()) && (size == node2.getOutgoingEdgeNr()))
				identical = true;	//if all the child nodes are identical, and direct children of this node, then the whole node is identical
			else if (!node1.getLabel().equals(LinguisticTerms.CONJUNCTION))	//for a conjunction, all terms should be equal; don't elide half of them!!
				result.addAll(equal);	//else just add those child nodes that are identical to result
		}
		
		if (identical)
		{
			result.add(node1);
			result.add(node2);
		}
		return result;
	}
	
	/**	Compares the two nodes and their branches to see if they're identical.
	 *	If not, it identifies which nodes are different (the branches might only
	 *	differ at a lower level, in which case the conjunction should happen there)
	 * 	and returns a list with those nodes.
	 *
	 *	We can use this method again for comparing other elision opportunities??
	 */ 
	private List<DTNode> compareBranches(DTNode node1, DTNode node2)
	{
		boolean different = false;
		List<DTNode> result = new ArrayList<DTNode>();
		
		if (!node1.getDeplbl().equals(node2.getDeplbl()))
		{	//the object of a passive tree is identical or equivalent to a ppmod
			if (node1.getDeplbl().equals(LinguisticTerms.SUBJECT) && node2.getDeplbl().equals(LinguisticTerms.PPMODIFIER) && compareObjectAndPP(node1, node2))
				return result;		//they are identical, so return empty list
			if (node2.getDeplbl().equals(LinguisticTerms.SUBJECT) && node1.getDeplbl().equals(LinguisticTerms.PPMODIFIER) && compareObjectAndPP(node2, node1))
				return result;		//they are identical, so return empty list
			different = true;		//not identical!
		}
		else if (!compareMorphs(node1.getMorph(), node2.getMorph()))
			different = true;
		else if ((node1 instanceof UndeterminedDTNode) || (node2 instanceof UndeterminedDTNode))
		{
			if ((node1 instanceof UndeterminedDTNode) && (node2 instanceof UndeterminedDTNode))
			{
				if (!node1.getSGID().equals(node2.getSGID()))
					different = true;
				else
					return result;
			}
			else
				different = true;
		}
		else if (node1.isLeaf() || node2.isLeaf())
		{
			if (node1.isLeaf() && node2.isLeaf())
			{	//if both nodes are leaves, compare the root
				if (!node1.getRoot().equals(node2.getRoot()))
					different = true;
			}
			else 
				different = true;
		}
		else if (node1.getOutgoingEdgeNr() != node2.getOutgoingEdgeNr())
			different = true;

		if (!different)		//if the nodes are equal so far, check their children
		{
			List<DTNode> checked = new ArrayList<DTNode>();
			List<DTNode> differences = new ArrayList<DTNode>();
			for (Iterator it = node1.getOutgoingEdges(); it.hasNext(); )
			{
				DTEdge edge = (DTEdge) it.next();
				DTNode target = edge.getTarget();
				List<DTNode> list = getEquivalentNodes(node2, edge.getLabel(), checked);
				if (list.size() == 0)	//no equivalent constituent; nodes not identical
				{
					different = true;
					break;
				}
			
				for (int i = 0; i < list.size(); i++)
				{
					List<DTNode> diff = compareBranches(target, list.get(i));
					if (diff.size() == 0)		//the branches are identical
						checked.add(list.get(i));	//so save this node to checkednodes, that it is not claimed identical to any others
					else		//different nodes found!
						differences.addAll(diff);	//store them
				}
			}
			
			if (differences.size() == 0);
			else if (differences.size() > 2)
				different = true;
			else if (node1.getLabel().equals(LinguisticTerms.PP))
				different = true;
			else	//exactly one constituent is different
				result.addAll(differences);
		}
		
		if (different)
		{
			result.add(node1);
			result.add(node2);
		}
		return result;
	}
	
	/**	Compared two morphology objects
	 */
	private boolean compareMorphs(Morph m1, Morph m2)
	{
		if ((m1 == null) && (m2 == null))
			return true;
		if ((m1 == null) || (m2 == null))
		{
			if ((m1 != null) && m1.isStandard())
				return true;
			if ((m2 != null) && m2.isStandard())
				return true;
			return false;
		}
		return m1.equals(m2);
	}
	
	/**	Returns all children of the source node with the given deplabel, that
	 *	are not in 'checked'.
	 */
	private List<DTNode> getEquivalentNodes(DTNode source, String dep, List<DTNode> checked)
	{
		List<Edge> list = source.getOutgoingEdges(dep);
		List<DTNode> result = new ArrayList<DTNode>();
		
		for (int i = 0; i < list.size(); i++)
		{
			DTNode node = (DTNode) list.get(i).getTarget();
			if (checked.contains(node))
				continue;	//this node is already equivalent to another
			else
				result.add(node);
		}
		return result;
	}
	
	/**	Compares the object of a passive sentence and a ppmodifier, to see if 
	 *	they are identical.
	 */
	private boolean compareObjectAndPP(DTNode object, DTNode pp)
	{	//check if the preposition is 'by'
		DTNode prep = pp.getDepChild(LinguisticTerms.HEAD);
		if (!prep.getRoot().equals("by"))
			return false;	//if the preposition isn't 'by', the nodes are different
			
		DTNode child = pp.getDepChild(LinguisticTerms.OBJECT);		//check if object is equal to the object of pp
		if (compareBranches(object, child).size() > 0)			//different nodes
			return false;
		return true;			//nodes represent identical phrases!
	}
	
	/**	Computes the elision score for each possible combination, then combines trees
	 *	to maximise the total elision score.
	 */
	private void elide()
	{
		List<Combination> combinations = findElisionOpportunities();
		combinations = maximiseElisionScore(combinations);
		for (int i = 0; i < combinations.size(); i++)
			aggregatedTrees.add(performElision(combinations.get(i)));
	}
	
	/**	Performs the elisions planned for this tree combination.
	 */
	private DependencyTreeTransformer performElision(Combination combi)
	{	//make a conjunction of the trees
		DTNode conjunction = new DTNode(LinguisticTerms.CONJUNCTION, LinguisticTerms.ROOT, null, null);
		DTNode conjunctor = new DTNode(LinguisticTerms.CONJUNCTOR, LinguisticTerms.CONJUNCTOR, "and", null);
		DependencyTreeTransformer result = new DependencyTreeTransformer(conjunction);
		
		try
		{
			boolean allFlash = true;
			for (int i = 0; i < combi.trees.size(); i++)
			{	//add all trees in the combination as conjuncts
				DTNode root = (DTNode) combi.trees.get(i).getGraph().getRoot();
				root.setDeplbl(LinguisticTerms.CONJUNCT);
				DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCT, result.getGraph().getFreeID(), conjunction, root);
				edge.setOrder(i);	//mark the order of the constituents - v. important for elision!
				result.addBranchWithNewIDs(root);
				result.addEdge(edge);
				if (combi.trees.get(i).getGraph().flash())
					root.setFlash(true);
				else
					allFlash = false;
			}
			
			result.getGraph().setFlash(allFlash);
			DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCTOR, result.getGraph().getFreeID(), conjunction, conjunctor);
			edge.setOrder(combi.trees.size());
			result.addEdge(edge);
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("NameAlreadyBoundException shouldn't be possible here!");
			e.printStackTrace();
		}
			
		for (int i = 0; i < combi.elided.size(); i++)	//then elide all marked constituents
		{	//for each elision type
			Elision elision = combi.elided.get(i);
			int begin = 0;			//elide constituent from first claus(es)
			int end = elision.elidedNodes.size() - 1;
			boolean passive = result.isPassive();
			
			if (elision.type.equals(LinguisticTerms.HEAD) || 	//if we are eliding the verb (gapping)
				(passive && elision.type.equals(LinguisticTerms.OBJECT)) || //or the object of a passive sentence
				((!passive) && elision.type.equals(LinguisticTerms.SUBJECT)))	//or the subject of an active one
			{	//unless this is gapping or conjunction-reduction, then remove from latter clauses
				begin = 1;
				end = elision.elidedNodes.size();
			}
			
			for (int j = begin; j < end; j++)
			{	//specify for each constituent that it ought to be elided
				List<DTNode> toElide = elision.elidedNodes.get(j);
				for (int k = 0; k < toElide.size(); k++)
					toElide.get(k).setElided(true);
			}			
		}
		
		DTNode secondClause = conjunction.getOrderedDepChild(LinguisticTerms.CONJUNCT, 1);
		boolean onlyPP = true;
		for (Iterator it = secondClause.getOutgoingEdges(); it.hasNext(); )
		{
			DTEdge edge = (DTEdge) it.next();
			if (edge.getTarget().isElided())
				continue;
			if (edge.getLabel().equals(LinguisticTerms.PPMODIFIER))
				continue;
			if (edge.getLabel().equals(LinguisticTerms.SUBJECT) && result.isPassive())	//by ..
				continue;
				
			onlyPP = false;
			break;
		}
		if (onlyPP)
			conjunctor.setRoot(",");	//if the second (and therefore third?) clause only have pp-modifiers, set the conjunct to ','
				
		return result;
	}
	
	/**	Helper class; stores a potential elision combination.
	 */
	private class Combination
	{
		String name;
		double elisionScore = 0;	
		List<Elision> elided = new ArrayList<Elision>();
		List<DependencyTreeTransformer> trees = new ArrayList<DependencyTreeTransformer>();
		
		public Combination(DependencyTreeTransformer tree1, DependencyTreeTransformer tree2)
		{
			trees.add(tree1);
			trees.add(tree2);
		}
		
		public Combination(List<DependencyTreeTransformer> t)
		{
			trees.addAll(t);
		}
		
		public Combination copy()
		{
			Combination c = new Combination(trees);
			c.elisionScore = elisionScore;
			for (Elision e : elided)
				c.elided.add(e.copy());
			return c;
		}
		
		/**	Adds an elision type to the combination. Arguments are the dependency label
		 *	and the nodes that could be elided.
		 */
		public void addElision(String type, DTNode node1, DTNode node2)
		{
			addElision(type, node1, node2, 1);
		}
		
		/**	Adds an elision type to the combination. Arguments are the dependency label,
		 *	the nodes that could be elided and the score for this elision type.
		 */
		public void addElision(String type, DTNode node1, DTNode node2, double e)
		{
			Elision elision = new Elision(type, node1, node2);
			elided.add(elision);
			elisionScore += (elided.size() * e);	//each extra elided constituent has a larger contribution
		}
		
		/**	Adds an elision type to the combination. Arguments are the dependency label
		 *	and the nodes that could be elided.
		 */
		public void addElision(String type, List<DTNode> list)
		{
			addElision(type, list, 1);
		}
		
		/**	Adds an elision type to the combination. Arguments are the dependency label,
		 *	the nodes that could be elided and the score for this elision type.
		 */
		public void addElision(String type, List<DTNode> list, double e)
		{
			Elision elision = new Elision(type, list);
			elided.add(elision);
			elisionScore += (elided.size() * e);	//each extra elided constituent has a larger contribution
		}
		
		/**	Checks if the elision score is high enough. If it's lower than 1
		 *	(meaning the only elision was gapping 'to be', don't aggregate this combination.
		 */		
		public boolean skip()
		{
			if (elisionScore < 1)
				return true;
			return false;
		}
		
		/**	Adds another tree to this combination, and doubles the elision score.
		 */
		public void addTree(DependencyTreeTransformer dt)
		{
			trees.add(dt);						//add tree and
			elisionScore = elisionScore * 2;	//double score
		}
	}
	
	/**	Helper class, models a type of elision. The type is equal to the
	 *	Dependency label (e.g. 'head' for gapping, 'subject' for Conjunction
	 *	Reduction), the nodes are the ones that are identical and may be elided.
	 */
	private class Elision
	{
		String type;
		List<List<DTNode>> elidedNodes = new ArrayList<List<DTNode>>();
		
		public Elision(String t, List<DTNode> input)
		{
			type = t;
			List<DTNode> list1 = new ArrayList<DTNode>();			
			List<DTNode> list2 = new ArrayList<DTNode>();
			
			for (int i = 0; i < input.size(); i += 2)
			{
				list1.add(input.get(i));
				list2.add(input.get(i + 1));
			}
			elidedNodes.add(list1);
			elidedNodes.add(list2);
		}
		
		public Elision(String t, DTNode node1, DTNode node2)
		{
			type = t;
			List<DTNode> list1 = new ArrayList<DTNode>();			
			List<DTNode> list2 = new ArrayList<DTNode>();
			list1.add(node1);
			list2.add(node2);
			elidedNodes.add(list1);
			elidedNodes.add(list2);
		}
		
		public Elision()
		{}
		
		public Elision copy()
		{
			Elision e = new Elision();
			e.type = type;
			e.elidedNodes.addAll(elidedNodes);
			return e;
		}
		
		public void addTree(List<DTNode> list)
		{
			elidedNodes.add(list);
		}
	}
	
	/**	Finds all possible tree combinations that have an elision score > 0 by
	 *	searching identical nodes, and stores the trees in Combinations.
	 *
	 *	Don't make combinations involving more than 3 trees!
	 */
	private List<Combination> findElisionOpportunities()
	{
		List<Combination> result = new ArrayList<Combination>();
		for (int i = 0; i < input.size(); i++)
		{
			List<Combination> combis = new ArrayList<Combination>();
			DependencyTreeTransformer dt = input.get(i);
			for (int j = (i + 1); j < input.size(); j++)
			{
				DependencyTreeTransformer match = input.get(j);
				List<Combination> copies = new ArrayList<Combination>();
				for(int k = 0; k < combis.size(); k++)		//if this tree is compatible with a previous combination
				{
					Combination copy = addIfCompatible(combis.get(k), match);
					if (copy != null)
						copies.add(copy);	
				}
				combis.addAll(copies);
				Combination c = combineTrees(dt, match);
				if (c != null)		//try to combine these two
					combis.add(c);
			}
			result.addAll(combis);	//add all combinations with this tree to result
		}
		return result;
	}
	
	/**	Combines two trees if one or more forms of elision are possible
	 */
	private Combination combineTrees(DependencyTreeTransformer tree1, DependencyTreeTransformer tree2)
	{
		Combination combi = new Combination(tree1, tree2);
		if (!(tree1.isPassive() ^ tree2.isPassive()))
		{	//only try eliding verb, subject and object if the trees have the same actor!
			tryCombination(LinguisticTerms.HEAD, combi);		//try Gapping
			tryCombination(LinguisticTerms.SUBJECT, combi);		//try CR
			tryCombination(LinguisticTerms.OBJECT, combi);
		}
		tryCombination(LinguisticTerms.COMPLEMENT, combi);	//try RNR		
		tryCombination(LinguisticTerms.MODIFIER, combi);
		tryCombination(LinguisticTerms.PPMODIFIER, combi);
		
		if (combi.elisionScore > 0)
			return combi;
		return null;
	}
	
	/**	Checks whether the given dependency tree is compatible with the combination;
	 *	if so, it adds it and increases the elision score.
	 */
	private Combination addIfCompatible(Combination combi, DependencyTreeTransformer dt)
	{
		if (combi.trees.size() == 3)	//don't aggregate more than 3 trees, no matter how compatible they are!
			return null;
			
		DependencyTreeTransformer tree1 = combi.trees.get(0);
		boolean compatible = true;
		Map<Elision, List<DTNode>> map = new HashMap<Elision, List<DTNode>>();
		
		for (int i = 0; i < combi.elided.size(); i++)
		{
			Elision elision = combi.elided.get(i);
			DTNode child = ((DTNode) dt.getGraph().getRoot()).getDepChild(elision.type);
			List<DTNode> equalNodes = findEqualNodes(((DTNode) tree1.getGraph().getRoot()).getDepChild(elision.type), child);
			List<DTNode> list = new ArrayList<DTNode>();
			
			if (equalNodes.size() > 0)	
			{		//found some equal nodes; check if they are the same as features in the original combination!
				for (int j = 0; j < equalNodes.size(); j += 2)
				{	//collect the nodes from the second tree in a list
					if (elision.elidedNodes.get(0).contains(equalNodes.get(j)))
						list.add(equalNodes.get(j + 1));
					else
					{	//if the node from the first tree is not in the original combination
						compatible = false;
						break;	//then this tree is not compatible so stop comparing
					}
				}
				map.put(elision, list);
			}
			else
			{	//if the nodes are completely different
				if (elision.type.equals(LinguisticTerms.OBJECT))	//try comparing to PPMOD
				{
					DTNode node = ((DTNode) dt.getGraph().getRoot()).getDepChild(LinguisticTerms.PPMODIFIER);
					if ((node != null) && compareObjectAndPP(((DTNode) tree1.getGraph().getRoot()).getDepChild(elision.type), node))
					{	//if they are identical, remember the node
						list.add(node);
						map.put(elision, list);
						continue;	//and continue to check the other elisions
					}
				}
				else if (elision.type.equals(LinguisticTerms.PPMODIFIER))	//try comparing to OBJECT
				{
					DTNode node = ((DTNode) dt.getGraph().getRoot()).getDepChild(LinguisticTerms.OBJECT);
					if ((node != null) && compareObjectAndPP(node, ((DTNode) tree1.getGraph().getRoot()).getDepChild(elision.type)))
					{	//if they are identical, remember the node
						list.add(node);
						map.put(elision, list);
						continue; //and continue to check the other elisions
					}
				}
				//no identical nodes found, so trees are not compatible
				compatible = false;
				break;	//so stop comparing
			}	
		}	//end for loop
		
		if (compatible)	//if the trees are compatible
		{	//add the tree to the combination, and increment the elision score
			Combination copy = combi.copy();
			combi.addTree(dt);		
			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
			{	//now add the new nodes to the elision so they can be elided later
				Elision e = (Elision) it.next();
				e.addTree(map.get(e));
			}
			return copy;
		}
		return null;
	}
	
	/**	Checks whether a constituent with the provided deplbl is identical in
	 *	both trees, so that it can be elided.
	 */
	private void tryCombination(String deplbl, Combination combi)
	{
		DTNode node1 = ((DTNode) combi.trees.get(0).getGraph().getRoot()).getDepChild(deplbl);
		DTNode node2 = ((DTNode) combi.trees.get(1).getGraph().getRoot()).getDepChild(deplbl);
		if ((node1 != null) && (node2 != null)) 
		{
			List<DTNode> nodes = findEqualNodes(node1, node2);
			List<DTNode> elisionNodes = new ArrayList<DTNode>();
			for (int i = 0; i < nodes.size(); i += 2)
			{
				DTNode node = nodes.get(i);
				if (!LinguisticTerms.isConstituent(node.getLabel(), node.getDeplbl()))
					continue;	//nodes that are determiners, prepositions etc. should not be elided
				if (node.getParent().getDeplbl().equals(LinguisticTerms.PPMODIFIER))
					continue;	//should not elide part of a PP
				elisionNodes.add(node);
				elisionNodes.add(nodes.get(i+1));
			}

			if (elisionNodes.size() > 0)
			{
				if (node1.getLabel().equals(LinguisticTerms.VERB) && (node1.getRoot() != null) && node1.getRoot().equals("be"))
					combi.addElision(deplbl, elisionNodes, 0.4);
				else
					combi.addElision(deplbl, elisionNodes);
				return;
			}
		}
		
		if (deplbl.equals(LinguisticTerms.OBJECT))	//|| deplbl.equals(LinguisticTerms.PPMODIFIER))
		{
			DTNode n1 = ((DTNode) combi.trees.get(0).getGraph().getRoot()).getDepChild(LinguisticTerms.OBJECT);
			DTNode n2 = ((DTNode) combi.trees.get(1).getGraph().getRoot()).getDepChild(LinguisticTerms.PPMODIFIER);
			if ((n1 != null) && (n2 != null) && compareObjectAndPP(n1, n2))
				combi.addElision(LinguisticTerms.OBJECT, n1, n2);
		
			n1 = ((DTNode) combi.trees.get(0).getGraph().getRoot()).getDepChild(LinguisticTerms.PPMODIFIER);
			n2 = ((DTNode) combi.trees.get(1).getGraph().getRoot()).getDepChild(LinguisticTerms.OBJECT);
			if ((n1 != null) && (n2 != null) && compareObjectAndPP(n2, n1))
				combi.addElision(LinguisticTerms.PPMODIFIER, n1, n2);
		}
	}
	
	/**	Finds the maximum elision score that the given combinations can amount to
	 *	(provided each tree is only used once :-), using a greedy algorithm.
	 */
	private List<Combination> maximiseElisionScore(List<Combination> combinations)
	{	//sort the combinations according to elision score (from high to low)
		Collections.sort(combinations, new CombiComparator());		
		List<Combination> result = new ArrayList<Combination>();
		for (int i = 0; i < combinations.size(); i++)	//iterate through the sorted list of combinations
		{	//check if the constituents of the next combination are left in input
			Combination combi = combinations.get(i);
			if (combi.skip())	//Skip combinations where the only elision is gapping 'to be'
				continue;
	
			boolean unused = true;
			for (int j = 0; j < combi.trees.size(); j++)
			{
				if (!input.contains(combi.trees.get(j)))
					unused = false;		
			}
			
			if (unused)	//if they are, add this combination to result, and remove its trees from input
			{
				result.add(combi);
				for (int j = 0; j < combi.trees.size(); j++)
					input.remove(combi.trees.get(j));
			}
			
			if (input.size() == 0)	//if there are no more trees left in input, stop searching
				break;
		}
		return result;	//return the list of best combinations
	}
	
	/**	Helper class, that compares two Combinations by their elision scores
	 */
	private class CombiComparator implements Comparator
	{
		public int compare(Object o1, Object o2) throws ClassCastException
		{
			Combination c1 = (Combination) o1;
			Combination c2 = (Combination) o2;
			if (c1.elisionScore < c2.elisionScore)	//we want to sort from high to low!
				return 1;
			if (c2.elisionScore == c2.elisionScore)
				return 0;
			return -1;
		}
	}
	
	/**	Returns all nodes with the syntactic category CONJUNCTION, and all summation nodes
	 */
	private List<DTNode> getAggregatedNodes(DTNode root, DependencyTreeTransformer dt)
	{	//depth-first search for conjunction node
		List<DTNode> result = new ArrayList<DTNode>();
		for (Edge edge : root.getOutgoingEdges(null))
		{
			DTNode target = (DTNode) edge.getTarget();
			if (target.getLabel().equals(LinguisticTerms.CONJUNCTION) || 
				((target.getAnchor() != null) && (target.getAnchor() instanceof SummationAnchor)))
				result.add(target);
			else
				result.addAll(getAggregatedNodes(target, dt));
		}
		return result;
	}
	
	/**	When two or more trees are aggregated in either of the aggregation phases, the morphology
	 *	information of some nodes can become out-of-date. For instance, if the subject becomes a 
	 *	conjunction, the verb should be set to plural. This method checks all aggregated trees to
	 *	see if any morphology information needs updating. 
	 *	This method replaces a similar method in the SurfaceRealiser. It was implemented recently
	 *	and may contain some bugs.
	 */
	private void tweakAggregate(DTNode root, DependencyTreeTransformer dt)
	{
		if (root.getLabel().equals(LinguisticTerms.CONJUNCTION))
		{
			for (DTNode conjunct : root.getDepChildren(LinguisticTerms.CONJUNCT))
				tweakAggregate(conjunct, dt);	//do tweak aggregate for all sentences in conjunction
			return;
		}
		if (!(root.getLabel().equals(LinguisticTerms.SMAIN) || root.getLabel().equals(LinguisticTerms.SSUB)))
			return;	//only try adapting (sub)sentences
			
		List<DTNode> conjunctions = getAggregatedNodes(root, dt);	//once we've split up conjuncted sentence, each branch can only have one aggregated node
		if (conjunctions.size() == 0)
			return;		//no aggregated node in the tree
		
		for (DTNode conjunction : conjunctions)
		{
			DTNode parent = conjunction.getParent();
			if (parent.getLabel().equals(LinguisticTerms.NP) && (!conjunction.getDeplbl().equals(LinguisticTerms.DET)))
				pluraliseNP(parent);	//if the node is the main or modifier part of an np, set the np to plural ('the key words y & z'; 'the x & y methods')
		}
		
		DTNode verb = root.getDepChild(LinguisticTerms.HEAD);
		if (verb.getMorph() == null)
			verb.setMorph(new Morph());
		boolean passive = verb.getMorph().isPassive();
		boolean tobe = verb.getRoot().equalsIgnoreCase("be");
		
		DTNode subject = root.getDepChild(LinguisticTerms.SUBJECT);	//get the subject
		DTNode object = root.getDepChild(LinguisticTerms.OBJECT);		
		DTNode complement = root.getDepChild(LinguisticTerms.COMPLEMENT);

		if ((!passive) && isPlural(subject))
	    	verb.getMorph().setSingular(false);	//if the verb is active and the subject plural, set the verb to plural
		else if (passive && isPlural(object))
	    	verb.getMorph().setSingular(false);	//if the verb is passive and the object plural,  set the verb to plural
		else if (isPlural(complement) && tobe)		//if the complement is plural, do nothing if the subject is inserted (x contest y & z, x is large and stupid)
		{	//but if it wasn't and verb is tobe('source's observation unit is x & y' but not 'source's finding contests y & z'), subject and verb must become plural
			pluraliseNP(subject);
			verb.getMorph().setSingular(false);
		}
		
		if (tobe && (!verb.getMorph().isSingular()))	//if the verb is plural and 'to be', the complement must be pluralised
			pluraliseNP(complement);			//e.g. 'x and y are the authors of z'
	}
	
	private boolean isPlural(DTNode node)
	{	//checks if this node is a conjunction or set to plural
		if (node == null) {
			return false;
		}
		if (node.getLabel().equals(LinguisticTerms.CONJUNCTION))
			return true;
		if ((node.getAnchor() != null) && (node.getAnchor() instanceof SummationAnchor))
			return true;	//e.g. 7 people
		if (node.getMorph() == null)
			return false;
		return (!node.getMorph().isSingular());
	}
	
	private void pluraliseNP(DTNode np)
	{	//sets the given node to plural
		if ((np == null) || (np.getInserted() > 0) || (np.getLabel().equals(LinguisticTerms.CONJUNCTION)))
			return;
		if (np.getMorph() == null)
			np.setMorph(new Morph());
		np.getMorph().setSingular(false);	//set the np to plural
		
		for (Edge edge : np.getOutgoingEdges(null))
		{	//set head, determiner and modifier to plural
			DTNode target = (DTNode) edge.getTarget();
			if (target.getInserted() > 0)
				continue;	//if the node has been inserted as source or target, skip it because it ought to be plural already or not to change??
			if (target.getMorph() == null)
				target.setMorph(new Morph());
			target.getMorph().setSingular(false);
		}
	}
	
	/**	Prints the given tree to a text file with the given name.
	 *
	 *	@param name Filename
	 *	@param dt DependencyTree
	 */
	public static void printTree(String name, DependencyTree dt)
	{
		try
		{
			FileWriter fw = new FileWriter(name + ".txt");
			PrintWriter w = new PrintWriter(fw);
			Dotter d = new Dotter();
			w.print(d.dotDependencyTree(dt));		
			w.close();
			fw.close();
		}
		catch(Exception e)
		{}
	}
}