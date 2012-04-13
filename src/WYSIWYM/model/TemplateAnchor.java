package WYSIWYM.model;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**	TemplateAnchor is an anchor for the templates used in creating new lexicon
 *	entries
 *
 *	@author Feikje Hielkema
 *	@version 1.1 04-04-2007
 *	@version 1.5 11-2008
 */
public class TemplateAnchor extends Anchor
{
	private List<String> entries = new ArrayList<String>();
	private String templateID;
	
	/**	Constructs a template anchor for the given node
	 *	@param node DTNode
	 */
	public TemplateAnchor(DTNode node)
	{
		setNode(node);
		if (node.getAnchor() != null)
			id = node.getAnchor().getID();
		else
			id = UUID.randomUUID().toString();	
	}
	
	/**	Returns the ID of this anchor, or if there is none,
	 *	the ID of its DTNode.
	 *	@return String
	 */
	public String getTemplateID()
	{
		if (templateID != null)
			return templateID;
		if (getNode() != null)	
			return getNode().getID();
		return null;		
	}
	
	/**	Sets the ID
	 *	@param str ID
	 */
	public void setTemplateID(String str)
	{
		templateID = str;
	}
	
	/**	Adds a menu item to the anchor
	 *	@param text String menu item description
	 */
	public void addEntry(String text)
	{
		if (!entries.contains(text))
			entries.add(text);
	}
	
	/**	Removes the menu item with the given description from the anchor
	 *	@param text String menu item description
	 *	@return true if the item was present and removed
	 */
	public boolean removeEntry(String text)
	{
		if (!entries.contains(text))
			return false;
		entries.remove(text);
		return true;
	}
	
	/**	Checks whether the anchor has any items
	 *	@return true if the anchor contains an item
	 */
	public boolean hasEntries()
	{
		return (entries.size() > 0);
	}
	
	/**	Checks whether the anchor an item with the given description
	 *	@param entry String menu item description
	 *	@return true if the anchor contains this item
	 */
	public boolean hasEntry(String entry)
	{
		if (entries.contains(entry))
			return true;
		return false;
	}
	
	/**	Returns a list of the menu item descriptions
	 *	@return List<String>
	 */
	public List<String> getEntries()
	{
		return entries;
	}
	
	/**	Returns an array with the menu item descriptions
	 *	@return String[]
	 */
	public String[] getEntriesArray()
	{
		String[] result = new String[entries.size()];
		for (int i = 0; i < entries.size(); i++)
			result[i] = entries.get(i);
		return result;
	}
}