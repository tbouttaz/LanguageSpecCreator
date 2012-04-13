package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	ExistingInstances contains individuals currently in the range
 *	of a certain property, and other individuals in the graph of the right
 *	class type to be in the range. It also contains a TagCloud of
 *	suitable individuals related to the user.
 *
 *	@author Feikje Hielkema
 *	@version 1.3 May 2008
 */
public class ExistingInstances implements IsSerializable
{
	private String[] range = new String[0];	
	private String[] rangeID = new String[0];
	private String[] other = new String[0];
	private String[] otherID = new String[0];
	private TagCloud tagcloud;
	
	public ExistingInstances()
	{}

	/**	Sets a list of individuals currently in the range of 
	 *	a certain property
	 *	@param r String[] nl-representations
	 *	@param id String[] ID's
	 */	
	public void setRange(String[] r, String[] id)
	{
		range = r;
		rangeID = id;
	}
	
	/**	Sets a list of individuals in the graph suitable for the range of 
	 *	a certain property
	 *	@param o String[] nl-representations
	 *	@param id String[] ID's
	 */
	public void setOther(String[] o, String[] id)
	{
		other = o;
		otherID = id;
	}
	
	/**	Sets the tag cloud
	 *	@param tc TagCloud
	 */
	public void setTagCloud(TagCloud tc)
	{
		tagcloud = tc;
	}
	
	/**	Gets the tag cloud
	 *	@return TagCloud
	 */
	public TagCloud getTagCloud()
	{
		return tagcloud;
	}
	
	/**	Gets nl-representations of individuals currently in the range of 
	 *	a certain property
	 *	@return String[] nl-representations
	 */
	public String[] getRange()
	{
		return range;
	}
	
	/**	Gets ids of individuals currently in the range of 
	 *	a certain property
	 *	@return String[] ids
	 */
	public String[] getRangeID()
	{
		return rangeID;
	}
	
	/**	Gets nl-representations of individuals suitable for the range of 
	 *	a certain property
	 *	@return String[] nl-representations
	 */
	public String[] getOther()
	{
		return other;
	}
	
	/**	Gets ids of individuals suitable for the range of 
	 *	a certain property
	 *	@return String[] ids
	 */
	public String[] getOtherID()
	{
		return otherID;
	}
}
	