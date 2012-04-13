package WYSIWYM.model;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.util.TextPlanningException;
import WYSIWYM.transformer.DependencyTreeTransformer;
import simplenlg.realiser.AnchorString;

/**
 *	ContentPlan represents a conceptual plan of the feedback text and 
 *	holds DependencyTrees.
 *
 * @author Feikje Hielkema
 * @version 1.4 2008/07/25
 */
public class ContentPlan 
{
	/** List with all DependencyTreeTransformers in the ContentPlan */
	protected List<DependencyTreeTransformer> treeList = new ArrayList<DependencyTreeTransformer>();	//list of all sentences
	/** Paragraphs, with their headers as key */
	protected Map<String, Paragraph> plan = new HashMap<String, Paragraph>();	//key is header, value is list of sentence sets 
											//(which are lists of dependency trees or lists of trees to be aggregated)
	/** Paragraph headers/keys */
	protected List<String> keys = new ArrayList<String> ();	//list of keys (in correct order)
	
	/**	Helper class, contains all dependency trees in a paragraph, divided
	 *	into sentence sets
	 */
	protected class Paragraph
	{
		/**	Paragraph headers */
		public List<AnchorString> header;	//paragraph header
		/** List with all DependencyTreeTransformers in the Paragraph */
		public List<DependencyTreeTransformer> trees = new ArrayList<DependencyTreeTransformer>();	//sentences in paragraph
		
		/**	Adds a sentence.
		 *	@param tree DependencyTree
		 */
		public void add(DependencyTreeTransformer tree)
		{
			trees.add(tree);
		}
		
		/**	Adds a list of sentences
		 *	@param list List<DependencyTreeTransformer>
		 */
		public void addAll(List<DependencyTreeTransformer> list)
		{
			trees.addAll(list);
		}
	}
    
    /**	Adds a dependency tree to a paragraph
     *
     *	@param	tree DependencyTree to be added
     *	@param	par Paragraph header
     *	@throws TextPlanningException if there is no paragraph with this header
     */ 
    public void add(DependencyTreeTransformer tree, String par) throws TextPlanningException
    {
    	if (tree == null)
    		return;

    	if (!plan.containsKey(par))
    		throw new TextPlanningException("Trying to add tree to unspecified paragraph or sentence set");
    	
    	treeList.add(tree);
		plan.get(par).add(tree);
    }  
  	   
  	/**	Adds a list of dependency trees to a paragraph
     *
     *	@param	trees List with DependencyTrees to be added
     *	@param	par Paragraph header
     *	@throws TextPlanningException if there is no paragraph with this header
     */    
  	public void add(List<DependencyTreeTransformer> trees, String par) throws TextPlanningException
  	{
  		if (!plan.containsKey(par))
    		throw new TextPlanningException("Trying to add tree to unspecified paragraph or sentence set");
    	
    	treeList.addAll(trees);
    	plan.get(par).addAll(trees);
  	}
  	   
    /**	Creates a new paragraph with the given header. If there already is a 
     *	paragraph with that header, it will modify the new header. For instance,
     *	it might become 'The second interview' instead of 'the interview'.
     *
     *	@param header	The header of the new paragraph
     *	@param	anchor Anchor attached to the header (can be null)
     *	@return String The header, which may have changed
     */
    public String newParagraph(String header, Anchor anchor)
    {
    	String head = getUniqueHeader(header);
    	if (header.length() > 0)
    	{
	  	 	Paragraph p = new Paragraph();    	
  		 	p.header = new ArrayList<AnchorString>();
  	 		p.header.add(new AnchorString(head, anchor));
    		plan.put(head, p); 
    		keys.add(head);
    	}
    	return head;	//true;
    }
    
    private String getUniqueHeader(String header)
    {
    	if (!plan.containsKey(header))
    		return header;
    		
   		if (header.substring(0, 4).equalsIgnoreCase("the "))
    	{
    		for (int i = 2; i < 20; i++)
    		{
    			String nr = Lexicon.getRankNL(i);
    			StringBuffer sb = new StringBuffer(header);
    			sb.insert(4, nr + " ");
    			if (!plan.containsKey(sb.toString()))
					return sb.toString();
    		}
    	}
    	System.out.println("COULD NOT CREATE HEADER FOR PARAGRAPH " + header);
    	return "";  	
    }
    
    /**	Creates a new paragraph with the given header. If there already is a 
     *	paragraph with that header, it will modify the new header. For instance,
     *	it might become 'The second interview' instead of 'the interview'.
     *	The list of trees is added to the new paragraph.
     *
     	@param head	String with the header of the new paragraph
     *	@param	anchor Anchor attached to the header (can be null)
     *	@param	trees List of sentences for the paragraph.
     *	@return String The header, which may have changed
     */
    public boolean addParagraph(String head, Anchor anchor, List<DependencyTreeTransformer> trees)
    {
    	String header = newParagraph(head, anchor);
    	if (header.length() == 0)
    		return false;
    	
    	treeList.addAll(trees);
    	plan.get(header).addAll(trees);
    	return true;
    }

	/**	Creates a new paragraph with the given header. If there already is a 
     *	paragraph with that header, it will modify the new header. For instance,
     *	it might become 'The second interview' instead of 'the interview'.
     *
     *	@param head	List<AnchorString> with the header of the new paragraph
     *	@return String The header, which may have changed
     */
    public String newParagraph(List<AnchorString> head)
    {
    	StringBuffer sb = new StringBuffer();
    	for (int i = 0; i < head.size(); i++)
    		sb.append(head.get(i).toString());
    	String key = getUniqueHeader(sb.toString());
    	
    	if (key.length() > 0)
    	{
    		Paragraph p = new Paragraph();
    		p.header = head;
    		plan.put(key, p);
    		keys.add(key);
    	}
    	return key;
    }
    
    /**	Returns the headers of the paragraphs
     *
     *	@return Iterator
     */
    public Iterator getParagraphHeaders()
    {
    	return keys.iterator();
    }
    
    /**	Returns the DependencyTreeTransformers in the paragraph.
     *	@param	key Paragraph header
     *	@return	List<DependencyTreeTransformer> of paragraph sentences
     */
    public List<DependencyTreeTransformer> getParagraphTrees(String key)
    {
    	if (!plan.containsKey(key))
    		return null;
    	return plan.get(key).trees;
    }
    
    /**	Returns the list of anchorstrings that form that paragraph header
     *	@param key	Paragraph header
     *	@return List of AnchorStrings with that header
     */
    public List<AnchorString> getParagraphHeader(String key)
    {
    	if (!plan.containsKey(key))
    		return null;
    	return plan.get(key).header;
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