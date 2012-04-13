package WYSIWYM.model;

import java.util.List;

import simplenlg.realiser.AnchorString;

/**	FeedbackText holds the text (consisting of AnchorStrings) that the user 
 *	sees and edits. Other classes can request the AnchorStrings one by one using next().
 *
 *	@author Feikje Hielkema
 *	@version 1.1 30-10-2006
 */
public class FeedbackText
{
	private List<AnchorString> list;
	private int index = 0;	//counter to store 'current' AnchorString
	
	/**	Constructs feedback text from the given list of anchorstrings
	 *	@param l	List<AnchorString>, representing the text
	 */
	public FeedbackText(List<AnchorString> l)
	{
		list = l;
	}
	
	/**	Returns the list of AnchorStrings
	 *	@return List<AnchorString>
	 */
	public List<AnchorString> getText()
	{
		return list;
	}
	
	/**	Returns the next AnchorString
	 *	@return AnchorString
	 */ 
	public AnchorString next()
	{
		if (index < list.size())
		{
			AnchorString result = list.get(index);
			index++;
			return result;
		}
		return null;
	}
	
	/**	Checks whether this is the last AnchorString
	 *	@return true if there are more AnchorStrings left
	 */
	public boolean hasNext()
	{
		if (index < list.size())
			return true;
		return false;
	}
	
	/**	Resets the text, setting the index to 0.
	 */
	public void reset()
	{
		index = 0;
	}
	
	/**	Retrieves the anchor with the given unique ID
	 *	@param anchor ID
	 *	@return Anchor
	 */
	public Anchor getAnchor(String anchor)
	{
		for (AnchorString as : list)
		{
			Anchor a = as.getAnchor();
			if ((a != null) && anchor.equals(a.getID()))
				return a;
		}
		return null;
	}
	
	/**	Returns the number of AnchorStrings in the feedback text
	 *	@return int
	 */
	public int size()
	{
		return list.size();
	}
	
	/**	Inserts an AnchorString at the given index
	 *	@param idx Index
	 *	@param as AnchorString
	 */
	public void insert(int idx, AnchorString as)
	{
		if ((idx >= 0) && (idx < list.size()))
			list.add(0, as);
		else
			list.add(as);
	}
}