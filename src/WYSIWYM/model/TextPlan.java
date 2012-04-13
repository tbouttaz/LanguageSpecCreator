package WYSIWYM.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import simplenlg.realiser.AnchorString;
import WYSIWYM.util.TextPlanningException;

/**
 *	TextPlan represents a conceptual plan of the feedback text and 
 *	holds DependencyTrees.
 *
 *	TextPlan and TextPlanner are older versions of the planning algorithm;
 *	for aggregation, use ContentPlan and ContentPlanner.
 *
 * @author Feikje Hielkema
 * @version 1.2 2007/12/6
 */
public class TextPlan 
{
	/**	All Dependency Trees in the text plan */
	protected List <DependencyTree> treeList = new ArrayList<DependencyTree>();
	/**	Paragraphs, with their headers as key */
	protected Map<String, Paragraph> plan = new HashMap<String, Paragraph>();	//key is header, value is list of sentence sets 
											//(which are lists of dependency trees or lists of trees to be aggregated)
	/**	Paragraph headers */
	protected List<String> keys = new ArrayList<String> ();	//list of keys (in correct order)
	
	/**	Helper class, contains all dependency trees in a paragraph, divided
	 *	into sentence sets
	 */
	protected class Paragraph
	{
		/**	Paragraph header */
		public List<AnchorString> header;
		/**	Sentence set headers */
		public List<String> keys = new ArrayList<String>();
		/**	Sentence sets, with their headers as key */
		public Map<String, SentenceSet> sets = new HashMap<String, SentenceSet>();		
	}
	
	/**	Helper class, contains dependency trees in a sentence set; meaning, a group	
	 *	of dependency trees without line breaks between them
	 */
	protected class SentenceSet
	{
		/**	Dependency Trees in the sentence set */
		public List<DependencyTree> trees = new ArrayList<DependencyTree>();
	}
    
    /**	Returns the number of sentence sets in the paragraph with the given header
     *	@param paragraph Paragraph header
     *	@return int
     */
    public int getSetNr(String paragraph)
    {
    	if (!plan.containsKey(paragraph))
    		return -1;	//no such paragraph in text plan
    	
    	Paragraph p = plan.get(paragraph);
    	int cntr = 0;
    	for (int i = 0; i < p.keys.size(); i++)
    		if (p.sets.get(p.keys.get(i)).trees.size() > 0)
    			cntr++;
    	return cntr;
    }
    
    /**	Adds a dependency tree to the given paragraph/sentence set
     *
     *	@param tree	DependencyTree to be added
     *	@param p Paragraph header
     *	@param s Sentence set header
     *	@throws TextPlanningException if the paragraph or sentence set does not exist
     */ 
    public void add(DependencyTree tree, String p, String s) throws TextPlanningException
    {
    	if (tree == null)
    		return;
    		
    	SentenceSet set = getSentenceSet(p, s);
    	if (set == null) 
    		throw new TextPlanningException("Trying to add tree to unspecified paragraph or sentence set");
    
    	treeList.add(tree);
		set.trees.add(tree);
    }  
  	   
    /**	Creates a new paragraph with the given header
     *
     *	@param head	String with the header of the new paragraph
     *	@param anchor The Anchor attached to the header (may be null)
     *	@return false if the text already has a paragraph with this header
     */
    public boolean newParagraph(String head, Anchor anchor)
    {
    	if (plan.containsKey(head))
    		return false;
  
  	 	Paragraph p = new Paragraph();    	
  	 	p.header = new ArrayList<AnchorString>();
  	 	p.header.add(new AnchorString(head, anchor));
    	plan.put(head, p); 
    	keys.add(head);
    	return true;
    }
    
    /**	Creates a new paragraph with the given header. If there already is a
     *	paragraph with this header, the new header is changed.
     *
     *	@param head	List<AnchorString> with the header of the new paragraph
     *	@return String with the header of the paragraph 
     */
    public String newParagraph(List<AnchorString> head)
    {
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < head.size(); i++)
    		sb.append(head.get(i).toString());
    	String key = sb.toString();
    	
    	if (!plan.containsKey(key))
    	{
    		Paragraph p = new Paragraph();
    		p.header = head;
    		plan.put(key, p);
    		keys.add(key);
    	}
    	return key;
    }
    
    /**	Creates a new (paragraph and) sentence set with the given headers.
     *	If there already is a sentence set in that paragraph with that header,	
     *	return false.
     *
     *	@param paragraph	String with the header of the paragraph
     *	@param header String with the header of the sentence set
     *	@return false if the paragraph already has a sentence set with this header
     */
    public boolean newSet(String paragraph, String header)
    {
    	newParagraph(paragraph, null);
    	Paragraph p = plan.get(paragraph);
    	if (p.sets.containsKey(header))
    		return false;
    	
    	SentenceSet s2 = new SentenceSet();
     	p.sets.put(header, s2);
     	p.keys.add(header);
     	return true;
    }
    
    /**	Returns the headers of the paragraphs
     *
     *	@return Iterator
     */
    public Iterator getParagraphHeaders()
    {
    	return keys.iterator();
    }
    
    /**	Returns the headers of the sentence sets
     *
     *	@return Iterator
     */
    public Iterator getSetHeaders(String paragraph)
    {
    	if (!plan.containsKey(paragraph))
    		return null;
    	
    	return plan.get(paragraph).keys.iterator();
    }
    
    /**	Returns the sentence set in the given paragraph with given header
     *	@param paragraph Paragraph header
     *	@param set Set header
     *	@return SentenceSet
     */
    protected SentenceSet getSentenceSet(String paragraph, String set)
    {
    	if (!plan.containsKey(paragraph))
    		return null;
    	if (!plan.get(paragraph).sets.containsKey(set))
    		return null;

    	return plan.get(paragraph).sets.get(set);
    }
    
    /**	Returns the paragraph header as an AnchorString
     *	@param key String with paragraph key
     *	@return List<AnchorString>
     */
    public List<AnchorString> getParagraphHeader(String key)
    {
    	if (!plan.containsKey(key))
    		return null;
    	return plan.get(key).header;
    }
    
    /** Returns all DependencyTrees in the denoted sentence set
     *	@param paragraph Paragraph header
     *	@param set Sentence set header
     *	@return List<DependencyTree>
     */
    public List<DependencyTree> getSet(String paragraph, String set)
    {
    	SentenceSet s = getSentenceSet(paragraph, set);
    	if (s != null)
    		return s.trees;
    	return null;
    }
    
    /** Removes the denoted sentence set
     *	@param paragraph Paragraph header
     *	@param set Sentence set header
     *	@return true if textplan contained paragraph and set, and set was removed
     */
    public boolean removeSet(String paragraph, String set)
    {
    	if (!plan.containsKey(paragraph))
    		return false;
    	Paragraph p = plan.get(paragraph);
    	if (!p.sets.containsKey(set))
    		return false;
    	p.sets.remove(set);
    	p.keys.remove(set);
    	return true;
    }
    
    /**	Returns all trees in the text plan, unordered
     *
     *	@return Iterator over DependencyTrees
     */
    public Iterator getTrees()
    {
    	return treeList.iterator();
    }
    
    /**	Returns a map with all the paragraphs and their headers
     *
     *	@return Map<String, Paragraph>
     */
    public Map<String, Paragraph> getParagraphs()
    {
    	return plan;
    }
}