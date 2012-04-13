package WYSIWYM.model;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.util.Dotter;

/***
 *	DependencyTree represents one sentence in the text plan. It holds
 *	DTNodes and DTEdges.
 *
 * @author Feikje Hielkema
 * @version 1.00 2006/11/13
 *
 * @version 1.1 20-09-2007
 *
 *	@version 1.4 08-08-2008
 */
public class DependencyTree extends Graph
{
	private boolean flash = false;				//new additions in the text are marked; flash if true if this tree has just been added
	private int order;							//order in text plan
	private boolean inverseInserted = false;	//helper boolean, remembers if source and target of the property were switched 
	private int aggregated = 0;					//aggregation status
	
	/**	Default constructor
	 */
    public DependencyTree() 
   	{
   		super();
    }

	/**	Constructor, sets given node as root.
	 *	@param root root node
	 */
    public DependencyTree(DTNode root)
    {
    	super(root);
    }
      
    /**	Tells tree that it should be marked in the text, because it's new.
     *	@param f true if the tree is new
     */   
    public void setFlash(boolean f)
	{
		flash = f;
	} 
	
	/**	Checks whether this tree should be marked in the feedback text.
	 *	@return	true if this tree should be marked.
	 */
	public boolean flash()
	{
		return flash;
	}
	
	/**	Sets the aggregation status; @see WYSIWYM.libraries.LinguisticTerms for
	 *	the possible values.
	 *	@param	a Aggregation status
	 */
	public void setAggregated(int a)
	{
		aggregated = a;
	}
	
	/**	Returns the aggregation status; @see WYSIWYM.libraries.LinguisticTerms for
	 *	the possible values.
	 *	@return	int Aggregation status
	 */
	public int getAggregated()
	{
		return aggregated;
	}
	
	/**	Checks whether this tree has been aggregated
	 *	@return true if this tree is aggregated in some way.
	 */
	public boolean isAggregated()
	{
		return (aggregated > 0);
	}
	
    /**	Sets the rank order of this tree in the text plan
     *	@param nr	order
     */
    public void setOrder(int nr)
    {
    	order = nr;
    }
    
    /**	Returns the main verb of this tree (assumes there's only one verb!)
     *	@return DTNode, or null if there is no verb in the tree
     */
    public DTNode getVerb()
    {
    	Iterator it = getNodes();
    	while (it.hasNext())
    	{
    		DTNode n = (DTNode) it.next();
    		if (n.getLabel().equals(LinguisticTerms.VERB) && n.getDeplbl().equals(LinguisticTerms.HEAD))
    			return n;
    	}
    	return null;
    }
    
    /**	Returns the first node it finds that's flagged
     *	@return DTNode, or null if there is no flagged node
     */
    public DTNode getFlaggedNode()
    {
    	Iterator it = getNodes();
    	while (it.hasNext())
    	{
    		DTNode n = (DTNode) it.next();
    		if (n.isFlagged())
    			return n;
    	}
    	return null;
    }
    
    /**	Returns DTNode that has the ID 'PREDICATE' (should only be one in the tree!)
     *
     *	@return DTNode
     */
    public DTNode getPredicate()
    {
    	for (Iterator it = getNodes(); it.hasNext(); )
    	{
    		DTNode n = (DTNode) it.next();
    		if (n.getID().equals(LinguisticTerms.PREDICATE))
    			return n;
    	}
    	return null;
    }
    
    /**	Returns the node that was inserted as target, or if it's more than one,
     *	their conjunction.
     *	@return DTNode
     */
    public DTNode getInsertedTarget()
    {
    	List<DTNode> inserted = getInsertedTargets();
    	if (inserted.size() == 0)
	    	return null;  
	    else if (inserted.size() > 1)	//if there is more than one inserted target
	    	return (DTNode) inserted.get(0).getParents().next();	//return their conjunction
	    return inserted.get(0);
    }
    
    /**	Returns the node that was inserted as source, or if it's more than one,
     *	their conjunction.
     *	@return DTNode
     */
    public DTNode getInsertedSource()
    {
    	List<DTNode> inserted = getInsertedSources();
    	if (inserted.size() == 0)
	    	return null;  
	    else if (inserted.size() > 1)	//if there is more than one inserted source
	    	return (DTNode) inserted.get(0).getParents().next();	//return their conjunction
	    return inserted.get(0);
    }
    
    /**	Returns all nodes inserted as source individually
     *	@return List<DTNode>
     */
    public List<DTNode> getInsertedSources()
    {
    	List<DTNode> result = new ArrayList<DTNode>();
    	for (Iterator it = getNodes(); it.hasNext(); )
    	{
    		DTNode n = (DTNode) it.next();
    		if (inverseInserted && (n.getInserted() == DTNode.TARGET_INSERTED))
    			result.add(n);
    		else if (!inverseInserted && (n.getInserted() == DTNode.SOURCE_INSERTED))
    			result.add(n);
    	}
    	return result;
    }
    
    /**	Returns all nodes inserted as target individually
     *	@return List<DTNode>
     */
    public List<DTNode> getInsertedTargets()
    {
    	List<DTNode> result = new ArrayList<DTNode>();
    	for (Iterator it = getNodes(); it.hasNext(); )
    	{
    		DTNode n = (DTNode) it.next();
    		if (inverseInserted && (n.getInserted() == DTNode.SOURCE_INSERTED))
    			result.add(n);
    		else if (!inverseInserted && (n.getInserted() == DTNode.TARGET_INSERTED))
    			result.add(n);
    	}
    	return result;
    } 
    
    /**	Returns the preposition of the PP with the given object
     *	@param date	DTNode containing the PP's object
     *	@return DTNode
     */
    public DTNode getPreposition(DTNode date)
    {
    	DTNode pp = date.getDepParent(LinguisticTerms.PPMODIFIER);
    	if (pp == null)
    		return null;
    	return (DTNode) pp.getOutgoingEdges(LinguisticTerms.HEAD).get(0).getTarget();
    }
    
    /**	Returns a node with a conjunction (if there is more than one, a random choice).
     *	@return DTNode
     */
    public DTNode getConjunction()
    {
    	Iterator it = getNodes();
    	while (it.hasNext())
    	{
    		DTNode n = (DTNode) it.next();
    		if (n.getLabel().equals(LinguisticTerms.CONJUNCTION))
    			return n;
    	}
    	return null;
    }
    
    /**	Tells the tree it's source and target have been inserted the other way around
     *	(for an inverse property).
     *	@param b	true if the nodes have been inserted the other way around
     */
    public void setInverseInserted(boolean b)
    {
    	inverseInserted = b;
    }
    
    /**	Checks whether source and target have been inserted the other way around.
     *
     *	@return true if the source and target have been inversely inserted.
     */
    public boolean getInverseInserted()
    {
    	return inverseInserted;
    }
    
    /**	Saves a .dot representation to a file, so the tree can be visualised 
     *	using GraphViz.
     *	@param name Filename (minus .txt!)
     */
    public void toFile(String name)
    {
    	try
    	{
	    	FileWriter fw = new FileWriter(name + ".txt");
			PrintWriter w = new PrintWriter(fw);
			Dotter d = new Dotter();
			w.print(d.dotDependencyTree(this));		
			w.close();
			fw.close();
		}
		catch(Exception e)
		{}
    }
}