package WYSIWYM.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.AnchorString;

/**
 *	QueryPlan represents a conceptual plan of the query.
 *
 * @author Feikje Hielkema
 * @version 1.2 15/01/2008
 */
public class QueryPlan extends TextPlan
{
	private List<AnchorString> intro = new ArrayList<AnchorString>();
	private List<QueryItem> list = new ArrayList<QueryItem>();
	
	/**	Represents a bulletpoint in the query list; also contains any nested lists
	 *	it may have
	 */
	public class QueryItem
	{
		/**	List header */
		public DependencyTree content;
		/**	List items */
		public List<QueryItem> children = new ArrayList<QueryItem>();
	}
	
	/**	Constructor, uses the given node to construct the intro (the very first header, e.g. 'find all papers')
	 *	@param root SGNode
	 */
	public QueryPlan(SGNode root)
	{
		Lexicon lex = new Lexicon();
		String plural = lex.getPlural(root.normalise(root.getLabel()));	//lex.getPlural(root.getNLLabel());
		
		intro.add(new AnchorString("Find all ", null));
		intro.add(new AnchorString(plural, root.getAnchor()));
	}
	
	/**	Adds the tree as list item to the given parent
	 *	@param tree DependencyTree
	 *	@param parent QueryItem parent item
	 *	@return newly created QueryItem
	 */
	public QueryItem addItem(DependencyTree tree, QueryItem parent)
	{
		QueryItem item = new QueryItem();
		item.content = tree;
		
		if (parent == null)
			list.add(item);
		else
			parent.children.add(item);
		
		return item;
	}
	
	/**	Returns an Iterator over all Dependency Trees
	 *	@return Iterator over DependencyTrees
	 */
	public Iterator getTrees()
	{
		List<DependencyTree> result = new ArrayList<DependencyTree>();
		for (int i = 0; i < list.size(); i++)
		{
			result.add(list.get(i).content);
			if (list.get(i).children.size() > 0)
				result.addAll(getTrees(list.get(i)));
		}
		return result.iterator();
	}
	
	private List<DependencyTree> getTrees(QueryItem item)
	{
		List<DependencyTree> result = new ArrayList<DependencyTree>();
		for (int i = 0; i < item.children.size(); i++)
		{
			QueryItem child = item.children.get(i);
			result.add(child.content);
			if (child.children.size() > 0)
				result.addAll(getTrees(child));
		}
		return result;
	}
	
	/**	Returns the intro, the very first item (e.g. 'find all papers')
	 *	@return List<AnchorString>
	 */
	public List<AnchorString> getIntro()
	{
		return intro;
	}
	
	/**	Returns the list items
	 *	@return List<QueryItem>
	 */
	public List<QueryItem> getQuery()
	{
		return list;
	}
}