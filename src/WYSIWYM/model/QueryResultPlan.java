package WYSIWYM.model;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import WYSIWYM.util.TextPlanningException;
import simplenlg.realiser.AnchorString;

/**
 *	TextPlan represents a conceptual plan of the feedback text and 
 *	holds DependencyTrees.
 *
 * @author Feikje Hielkema
 * @version 1.2 25-03-2008
 */

public class QueryResultPlan extends TextPlan 
{
	private String currentPar, currentSet;
	
	/**	Adds a new paragraph with the given header and sets it to 'current', 
	 *	so that subsequent tree-adding operations use this paragraph
	 *	@param par Paragraph header
	 *	@param anchor Anchor associated with the header
	 *	@return true 
	 *	@throws TextPlaningException if there was already a paragraph with this header
	 */
	public boolean setParagraph(String par, Anchor anchor) throws TextPlanningException
	{
		if (currentPar != null)
			throw new TextPlanningException("Cannot add new paragraph, one already exists");
		
		currentPar = par;
		Paragraph p = new Paragraph();    	
  	 	p.header = new ArrayList<AnchorString>();
  	 	p.header.add(new AnchorString(par, anchor));
    	plan.put(par, p); 
    	keys.add(par);
    	return true;
	}
	
	/**	Adds the given tree to the current sentence set (used by BrowsingPlanner)
	 *
	 *	@param tree Dependency Tree
	 *	@throws TextPlanningException if there is no current paragraph or sentence set
  	 */
  	public void add(DependencyTree tree) throws TextPlanningException
  	{
  		if (currentPar == null)
  			throw new TextPlanningException("The paragraph is not set!");
  		if (currentSet == null)
  			throw new TextPlanningException("Trying to add tree to unspecified paragraph or sentence set");
  		
  		SentenceSet set = getSentenceSet(currentPar, currentSet);
  		treeList.add(tree);
  		set.trees.add(tree);
  	} 
  	
  	/**	Adds the given tree to the denoted sentence set
	 *
	 *	@param tree Dependency Tree
	 *	@param set Sentence set header
	 *	@throws TextPlanningException if there is no sentence set with this header
  	 */
  	public void add(DependencyTree tree, String set) throws TextPlanningException
  	{
  		if (setCurrent(set))
  			add(tree);
  	}
  	
  	/**	Overload; ensures that no class uses this inherited but inapplicable method.
     *
     *	@param tree	DependencyTree to be added
     *	@param p Paragraph header
     *	@param s Sentence set header
     *	@throws TextPlanningException always
     */ 
    public void add(DependencyTree tree, String p, String s) throws TextPlanningException
    {
    	throw new TextPlanningException("This method should not be used!");
    }
      
    /**	Creates a new SentenceSet with the given header
     *	@param header Sentence set header
     *	@return false if there already is such a set in the current paragraph
     *	@throws TextPlanningException if there is no current paragraph
     */
    public boolean newSet(String header) throws TextPlanningException
    {
    	if (currentPar == null)
    		throw new TextPlanningException("The paragraph is not set!");
    		
		Paragraph p = plan.get(currentPar);
		currentSet = header;
    	if (p.sets.containsKey(header))
    		return false;
    	
    	SentenceSet s2 = new SentenceSet();
     	p.sets.put(header, s2);
     	p.keys.add(header);
     	return true;
    }
     
    /**	Sets the paragraph and sentence set that trees are currently added to to
     *	those with the given headers. If the paragraph header is null, it's added
     *	to a random paragraph (as QueryResultPlan ought not to have more than one	
     *	paragraph anyway
     *
     *	@param set Sentence set header
     *	@return true
     *	@throws TextPlanningException if there is no current paragraph
     */
    public boolean setCurrent(String set) throws TextPlanningException
    {
    	if (currentPar == null)
    		throw new TextPlanningException("The paragraph is not set!");
    	
    	Paragraph p = plan.get(currentPar);
    	if (!p.sets.containsKey(set))
    		if (!newSet(currentPar, set))
    			System.out.println("Failed to add " + set + " to plan");
    	currentSet = set;
    	return true;
    }
      
    /**	Returns all set headers in the current paragraph
     *	@return Iterator over Strings, or null if there is no current paragraph
     */
    public Iterator getSetHeaders()
    {
    	if (currentPar == null)
    		return null;
    	Paragraph p = plan.get(currentPar);
    	return p.keys.iterator();
    }
}