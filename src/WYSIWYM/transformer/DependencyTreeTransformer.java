package WYSIWYM.transformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Edge;
import WYSIWYM.model.Morph;
import WYSIWYM.model.SummationAnchor;
import WYSIWYM.model.UndeterminedDTNode;

/**
 * DependencyTreeTransformer.java performs all transformations on a DependencyTree.
 *
 * @author Feikje Hielkema
 * @version 1.0 2006/11/13
 *
 *	@version 1.4	2008/07/30
 */
public class DependencyTreeTransformer extends GraphTransformer
{
	/**	Constructor, sets the DependencyTree
	 *	@param tree DependencyTree
	 */
    public DependencyTreeTransformer(DependencyTree tree) 
    {
		super(tree);
	}

	/**	Default constructor, creates a new, empty tree
	 */
	public DependencyTreeTransformer()
	{
		super();
		setGraph(new DependencyTree());
	}
	
	/**	Constructs a copy of the given transformer
	 *	@param dt DependencyTreeTranformer
	 */
	public DependencyTreeTransformer(DependencyTreeTransformer dt)
	{
		super();
		setGraph(new DependencyTree());
		try
		{
			HashMap<String,DTNode> nodeMap = new HashMap<String,DTNode>();
			DTNode root = (DTNode) dt.getGraph().getRoot();
			getGraph().setRoot(DTNode.copyNode(root, nodeMap, this));
		}
		catch(NameAlreadyBoundException e)
		{
			e.printStackTrace();
		}
	}
	
    /**	Constructor, creates new DependencyTree with the given node as its root
     *	@param	node Root DTNode of the tree
     */
    public DependencyTreeTransformer(DTNode node)
    {
    	super();
    	setGraph(new DependencyTree(node));
    }
    
    /**	Returns the tree
	 *	@return DependencyTree
	 */
    public DependencyTree getGraph()
    {
    	return (DependencyTree) super.getGraph();
    }
    
   	/**	Inserts a node into the tree in a conjunction, as a child of the specified type; used
   	 *	in the first aggregation stage (in ContentPlanner and TextPlanner)
   	 *
   	 *	@param node Inserted node
   	 *	@param type Lexicon.SOURCE (source of property) or Lexicon.TARGET (target of property)
   	 *	@param nr Sequence number of conjunct (to preserve order of authors etc.)
  	 */
    public void append(DTNode node, String type, int nr)
    {
    	DTNode old = (DTNode) getGraph().getNode(type);
    	if (old == null)
	   		return;    	
    	
    	getGraph().setAggregated(LinguisticTerms.CC);
    	if (type.equals(Lexicon.SOURCE))
	 		node.setInserted(DTNode.SOURCE_INSERTED);
	 	else if (type.equals(Lexicon.TARGET))
	 		node.setInserted(DTNode.TARGET_INSERTED);
	 	
	 	Morph m = node.getMorph(); 		
 		if (m == null)
		 	node.setMorph(old.getMorph());		//copy the morphological information
		else
		{
			Morph oldM = old.getMorph();
			if (oldM != null)
			{
				oldM.copy(m);
				node.setMorph(oldM);
			}
		}
		node.setDeplbl(LinguisticTerms.CONJUNCT);
		old.setLabel(LinguisticTerms.CONJUNCTION);
		old.setLeaf(false);
		
		for (Iterator it = old.getOutgoingEdges(); it.hasNext(); )
		{	//a node can't have two determiners
			DTEdge edge = (DTEdge) it.next();
			if (edge.getLabel().equals(LinguisticTerms.CONJUNCT))	//don't copy or remove the conjunct edges!!
				continue;
			else if (edge.getLabel().equals(LinguisticTerms.PPMODIFIER))
				continue;		//leave the pp-modifiers where they are, so they can be realised at the end of the conjunction
			else if (edge.getLabel().equals(LinguisticTerms.DET) && node.hasEdge(LinguisticTerms.DET))
			{	//replace the determiner of node with that of old
				List<Edge> l = node.getOutgoingEdges(LinguisticTerms.DET);
				for (int j = 0; j < l.size(); j++)
					node.removeOutgoingEdge(l.get(j));
			}
			else	
			{	//add the edge to node, and copy the morphological information
				List<Edge> l = node.getOutgoingEdges(edge.getLabel());
				if (l.size() > 0)
				{
					DTNode child = (DTNode) l.get(0).getTarget();
					if (child.getMorph() != null)
						((DTNode)edge.getTarget()).setMorph(child.getMorph());
				}
			}
			try
			{	//attach these edges to node
				edge.setSource(node);
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("DTTRANSFORMER 125: AN EXCEPTION HERE SHOULD NOT BE POSSIBLE.");
				e.printStackTrace();
			}
			it.remove();	//remove all these edges from old
		}
		
		node.setID(getGraph().getFreeID());
		if (!addBranch(node))
			System.out.println("DTT 122: Problem with adding branch");
		DTEdge edge = new DTEdge(LinguisticTerms.CONJUNCT);
		edge.setID(getGraph().getFreeID());
		edge.setOrder(nr);
		try
		{
			edge.setSource(old);
			edge.setTarget(node);
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("DTTRANSFORMER 125: AN EXCEPTION HERE SHOULD NOT BE POSSIBLE.");
			e.printStackTrace();
		}
		addEdge(edge);
    }
    
    /**	If a number of conditions are fullfilled (most important, that the old node
     *	is only part of an NP, and the other node of that NP is identical to the noun
     * 	of the new node), then the parent NP is replaced rather than its child.
     */
    private boolean replaceParent(DTNode old, DTNode node)
    {
    	if ((node instanceof UndeterminedDTNode) || (!node.hasOutgoingEdge()))
    		return false;
    	if (node.getDepChild(LinguisticTerms.HEAD) == null)
    		return false;
    	DTNode parent = old.getParent();
    	if ((parent == null) || (!parent.getLabel().equals(LinguisticTerms.NP)))
    		return false;
    	
    	int result = 0;
    	String newRoot = node.getDepChild(LinguisticTerms.HEAD).getRoot();
    	DTNode noun = parent.getDepChild(LinguisticTerms.HEAD);
    	if ((noun != null) && (noun.getRoot() != null))
    	{
    		String oldRoot = noun.getRoot();
    		if (oldRoot.equalsIgnoreCase(newRoot))
    			result = 2;
    	}
    	else	//if the old modifier is the same as the new noun, the parent node should
    	{		//be replaced but the modifier should not be copied!
    		DTNode mod = parent.getDepChild(LinguisticTerms.MODIFIER);
    		if ((mod != null) && (mod.getRoot() != null))
    		{
    			String oldRoot = mod.getRoot();
    			if (oldRoot.equalsIgnoreCase(newRoot))
    				result = 1;
    		}
    	}
    	
    	if (result == 0)
    		return false;
    	
    	node.setDeplbl(parent.getDeplbl());
    	for (Iterator it = parent.getIncomingEdges(); it.hasNext(); )
 		{
 			DTEdge edge = (DTEdge) it.next();
    		node.setDeplbl(edge.getLabel());
    		try
			{
				edge.setTarget(node);
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("DTTRANSFORMER 125: AN EXCEPTION HERE SHOULD NOT BE POSSIBLE.");
				e.printStackTrace();
			}
    		it.remove();			
 		}
 			
 		if (result > 1)
 		{	//copy across determiners and modifiers
			replace(parent, node); 	//copy across any edges of the old parent node
			for (Iterator it = parent.getOutgoingEdges(); it.hasNext(); )	
			{
				it.next();
				it.remove();
			}
		}		
					 
 		removeNode(parent);
		if (!addBranch(node))
			System.out.println("DTT 227: Problem with adding branch");
		if (getGraph().getRoot().getID().equals(parent.getID()))
 			getGraph().setRoot(node);
 		return true;
    }
    
    /**	Inserts a new source or target node in the dependency tree
     *	in the place of the old one. This is the default method of insertion;
     *	for aggregation user append(DTNode, String, int)
     *
     *	@param node	DTNode to be inserted
     *	@param type Lexicon.SOURCE (source of property) or Lexicon.TARGET (target of property)
     */
    public void insert(DTNode node, String type)
    {
    	DTNode old = (DTNode) getGraph().getNode(type);
    	if (old == null)
    		return;

 		if (type.equals(Lexicon.SOURCE))
	 		node.setInserted(DTNode.SOURCE_INSERTED);
	 	else if (type.equals(Lexicon.TARGET))
	 		node.setInserted(DTNode.TARGET_INSERTED);
	 	else if (type.equals(LinguisticTerms.PREDICATE))
	 		node.setInserted(DTNode.PREDICATE_INSERTED);
	 	
	 	if (replaceParent(old, node))	//try to see if this is a special case where
	 		return;			//the new node replaces the parent of the designated slot node
 	
	 	node.setDeplbl(old.getDeplbl());
	 	node.setID(type);
	 	if (type.equals(LinguisticTerms.PREDICATE))
			node.setLabel(old.getLabel());
			
 		for (Iterator it = old.getIncomingEdges(); it.hasNext(); )
 		{
 			DTEdge edge = (DTEdge) it.next();
    		node.setDeplbl(edge.getLabel());
    		try
			{
				edge.setTarget(node);
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("DTTRANSFORMER 125: AN EXCEPTION HERE SHOULD NOT BE POSSIBLE.");
				e.printStackTrace();
			}
    		it.remove();			
 		}
 		
 		List<DTNode> nodes = new ArrayList<DTNode>();
	 	if (node.getLabel().equals(LinguisticTerms.CONJUNCTION))
	 	{	//morphological information etc. needs to be attached to conjuncts, not conjunction
	 		for (Iterator it = node.getOutgoingEdges(LinguisticTerms.CONJUNCT).iterator(); it.hasNext(); )
	 			nodes.add(((DTEdge)it.next()).getTarget());
	 	}
	 	else
	 	{
	 		boolean not = false;
	 		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
	 		{
	 			DTNode target = ((DTEdge) it.next()).getTarget();
	 			if (target.isLeaf() && target.getRoot().equals("not"))
	 				not = true;
	 			else
	 				nodes.add(target);
	 		}
	 		if (!not)
			{
				nodes = new ArrayList<DTNode>();
				nodes.add(node);
			}
	 	}
	 	
	 	for (int i = 0; i < nodes.size(); i++)
	 		replace(old, nodes.get(i));
		for (Iterator it = old.getOutgoingEdges(); it.hasNext(); )	
		{
			it.next();
			it.remove();
		}
    	removeNode(old);
		if (!addBranch(node))
			System.out.println("DTT 227: Problem with adding branch");
		if (getGraph().getRoot().getID().equals(old.getID()))
 			getGraph().setRoot(node);
	}
    
    /**	Replaces the first node with the second. Modifiers of the old node
     *	are copied to the new one, as well as morphology information.
     */
    private void replace(DTNode old, DTNode node)
    {
    	Morph m = node.getMorph(); 		
 		if (m == null)
		 	node.setMorph(old.getMorph());		//copy the morphological information
		else
		{
			Morph oldM = old.getMorph();
			if (oldM != null)
			{
				oldM.copy(m);
				node.setMorph(oldM);
			}
		}

		for (Iterator it = old.getOutgoingEdges(); it.hasNext(); )
		{	//a node can't have two determiners
			DTEdge edge = (DTEdge) it.next();
			if (edge.getLabel().equals(LinguisticTerms.HEAD))
				continue;	//don't copy across the head
			if (edge.getLabel().equals(LinguisticTerms.DET) && node.hasEdge(LinguisticTerms.DET))
			{
				List<Edge> l = node.getOutgoingEdges(LinguisticTerms.DET);
				for (int j = 0; j < l.size(); j++)
					node.removeOutgoingEdge(l.get(j));
			}
			else	
			{	
				List<Edge> l = node.getOutgoingEdges(edge.getLabel());
				if (l.size() > 0)
				{	//copy the morphological information
					DTNode child = (DTNode) l.get(0).getTarget();
					if (child.getMorph() != null)
						((DTNode)edge.getTarget()).setMorph(child.getMorph());
				}
			}
			try
			{
				edge.setSource(node);
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("DTTRANSFORMER 125: AN EXCEPTION HERE SHOULD NOT BE POSSIBLE.");
				e.printStackTrace();
			}
		}
    }
    
    /**	Adds a branch from another Dependency tree to this one. For that, all id's
     *	must be changed (otherwise you may get components with identical id's).
     *	So all nodes and edges receive new id's, and the nodes are told about the 
     *	edges' new ids.
     *
     *	@param node Node to add
     */
    public void addBranchWithNewIDs(DTNode node)
    {
    	node.setID(getGraph().getFreeID());
    	List<String> oldIDs = new ArrayList<String>();
    	for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
    	{	//give each edge a new id; remember the old id's
    		DTEdge edge = (DTEdge) it.next();
    		oldIDs.add(edge.getID());
    	}
    	
    	for (int i = 0; i < oldIDs.size(); i++)		//remove the old edges
    	{	//now remove the old edges
    		DTEdge edge = (DTEdge) node.removeOutgoingEdge(oldIDs.get(i));
    		while (true)
    		{
    			try
    			{
    				String id = getGraph().getFreeID();
    				while (oldIDs.contains(id))
    					id = getGraph().getFreeID();
    					
    				edge.setID(id);
    				node.addOutgoingEdge(edge);
    				break;
    			}
    			catch(NameAlreadyBoundException e)
    			{}	//try again with new id
    		}   		
    		addBranchWithNewIDs(edge.getTarget());	//recurse
	    	addEdge(edge);	//and add the edge (with nodes) to the graph
	    }
    }
    
  	/**	Checks whether this tree can be set to passive, by checking whether it
  	 *	has a object (instead of a complement).
  	 *
  	 *	@return true if this tree's verb can be set to passive.
  	 */
  	public boolean potentialPassive()
  	{
  		DTNode root = (DTNode) getGraph().getRoot();
  		if (root.getOutgoingEdges(LinguisticTerms.OBJECT).size() == 0)
  			return false;
  		if (root.getOutgoingEdges(LinguisticTerms.SUBJECT).size() == 0)
	  		return false;
	  	return true;
  	}
    
    /**	Sets the sentence to active or passive, depending on what it is now;
     *	if it is now passive, it will be active, and vice versa.
     */
    public void setPassive()
    {
    	if (!potentialPassive())
    		return;
    	
    	DTNode n = ((DependencyTree)getGraph()).getVerb();
    	Morph m = n.getMorph();
    	if (m == null)
    		m = new Morph(true, true);
    	else if (m.isPassive())
    		m.setPassive(false);
    	else
    		m.setPassive(true);
    	n.setMorph(m);
    }
    
    /**	Checks if the tree's verb is set to passive.
     *	@return true if the tree verb morphology says passive
     */
    public boolean isPassive()
    {
    	DTNode n = getGraph().getVerb();
    	Morph m = n.getMorph();
    	if (m == null)
    		return false;
    	return m.isPassive();
    }
    
    /**	Negates or unnegates the sentence.
     *
     *	@param negated True if the tree should be negated
     */
    public void setNegated(boolean negated)
    {
    	DTNode n = ((DependencyTree)getGraph()).getVerb();
    	Morph m = n.getMorph();
    	if (m == null)
    		m = new Morph();
    		
   		m.setNegated(negated);
    	n.setMorph(m);
    }
    
    /**	Sets the main verb to plural or singular
     *
     *	@param plural True if the verb should be pluralised
     */
    public void setPlural(boolean plural)
    {
    	DTNode n = getGraph().getVerb();
    	Morph m = n.getMorph();
    	if (m == null)
    		m = new Morph();
    		
   		m.setSingular(!plural);
    	n.setMorph(m);
    }
    
    /**	Retrieves a node with the given root word.
     *
     *	@param	root String root word
     *	@return DTNode
     */
    public DTNode getNodeWithRoot(String root)
    {
    	Iterator it = getGraph().getNodes();
    	while (it.hasNext())
    	{
    		DTNode n = (DTNode) it.next();
    		if ((n.getRoot() != null) && (n.getRoot().equals(root)))
    			return n;
    	}
    	return null;
    }
    
    /**	Checks whether source and target of the tree were inserted the
     *	other way around (to accommodate an inverse property)
     *	@return true if source and target were inversely inserted
     */
    public boolean isInverseInserted()
    {
    	return getGraph().getInverseInserted();
    }

	/**	Finds the DTNode with the same SemanticGraph ID as the given node
	 *	@param dt old DTNode
	 *	@return Equivalent DTNode
	 */
    public DTNode getInsertedNode(DTNode dt)
    {
    	if ((dt == null) || (dt.getSGID() == null))
    		return null;
    	
    	String id = dt.getSGID();
    	List<DTNode> inserted = getGraph().getInsertedSources();
    	inserted.addAll(getGraph().getInsertedTargets());
    	for (int i = 0; i < inserted.size(); i++)
    	{
    		DTNode node = inserted.get(i);
    		if ((node instanceof UndeterminedDTNode) && id.equals(node.getSGID()))
    			return node;
    			
    		Anchor a = node.getAnchor();
    		if ((a != null) && (a instanceof SummationAnchor) &&((SummationAnchor)a).containsNode(id))
    			return node;	//if this is a summation node that encompasses the node we are searching, return it
	   	}
		return null;
    }
    
    /**	If the topic of the relative clause is in this tree, and does not have
     *	other endmodifiers or the dep-role of determiner/modifier, the given
     *	relative clause is added.
     *	@param clause DependencyTreeTransformer with clause to add
     *	@return	0 if there were no suitable nodes to add the clause to,
     *	1 if the clause was added, 2 if there might have been a suitable 
     *	node but it was replaced by a summation node.
     */
    public int addRelativeClause(DependencyTreeTransformer clause)
    {
    	DTNode topic = clause.getGraph().getInsertedSource();	//find two corresponding nodes in the tree
		DTNode np = getInsertedNode(topic);
		if (np == null)
		{
			topic = clause.getGraph().getInsertedTarget();
			np = getInsertedNode(topic);
		}
    	
    	if (np == null)		//could not find a match!
    		return 0;
    	if ((np.getAnchor() != null) && (np.getAnchor() instanceof SummationAnchor))
    		return 2;
    	if (np.getOutgoingEdges(LinguisticTerms.PPMODIFIER).size() > 0)
    		return 0;	//if this node has other end modifiers, can't add relative clause
    	String dep = np.getDeplbl();
    	if (dep.equals(LinguisticTerms.DET) || dep.equals(LinguisticTerms.MODIFIER))
    		return 0;	//if it acts as determiner or modifier, can't add relative clause
    	
    	SpecificationTransformer trans = new SpecificationTransformer(clause.getGraph());
    	trans.toRelativeClause(topic);		//makes a relative clause    	
    	
    	clause.getGraph().getInsertedSource().setInserted(0);	//remove the 'inserted' flag otherwise tweakaggragete in SR won't work
    	clause.getGraph().getInsertedTarget().setInserted(0);
    	
    	np.setUseAsPronoun(false);	//don't realise as pronoun if you attach a relative clause!
    	DTNode root = (DTNode) trans.getGraph().getRoot();
    	root.setFlash(clause.getGraph().flash());
    	root.setDeplbl(LinguisticTerms.PPMODIFIER);	//change the dependency label
    	addBranchWithNewIDs(root);		//add the branch to this tree
    	try
    	{
	    	DTEdge edge = new DTEdge(LinguisticTerms.PPMODIFIER, getGraph().getFreeID(), np, root);
    		addEdge(edge);
    	}
    	catch (NameAlreadyBoundException e)
    	{}
    	return 1;
    }
}