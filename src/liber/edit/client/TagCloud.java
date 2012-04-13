package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	Contains the tags and their font sizes for the tag cloud. 
 *
 *	@author Feikje Hielkema
 *	@version 1.0 April 2007
 *
 *	@version 1.4 December 2008
 */
public class TagCloud implements IsSerializable
{
	private String[] values, ids;
	private int[] frequencies;
	private int index;
	private boolean number = false;
	
	/**	Create a tag cloud of values with labels and font sizes
	 *	@param v Labels
	 *	@param f Font sizes
	 */
	public void create(String[] v, int[] f)
	{
		values = v;
		frequencies = f;
	}
	
	/**	Create a tag cloud of objects with labels, font sizes and unique
	 *	ids.
	 *	@param v Labels
	 *	@param i ID's
	 *	@param f Font sizes
	 */
	public void create(String[] v, String[] i, int[] f)
	{
		values = v;
		ids = i;
		frequencies = f;
	}
	
	/**	Tell this tag cloud whether it displays String values
	 *	or number ranges
	 *	@param n True if this tagcloud displays number ranges
	 */
	public void setNumber(boolean n)
	{
		number = n;
	}
	
	/**	Check whether the tagcloud displays String values
	 *	or number ranges
	 *	@return True if this tagcloud displays number ranges
	 */
	public boolean isNumber()
	{
		return number;
	}
	
	/**	Returns the label at the given index
	 *	@param i Index
	 *	@return String
	 */
	public String getValue(int i)
	{
		if (i >= values.length)
			return null;
		return values[i];
	}
	
	/**	Returns the font size at the given index
	 *	@param i Index
	 *	@return int
	 */
	public int getFrequency(int i)
	{
		if (i >= frequencies.length)
			return -1;
		return frequencies[i];
	}
	
	/**	Returns the ID at the given index
	 *	@param i Index
	 *	@return String
	 */
	public String getID(int i)
	{
		if (i >= ids.length)
			return null;
		return ids[i];
	}
	
	/**	Returns number of tags in the cloud
	 *	@return size
	 */
	public int size()
	{
		return values.length;
	}
	
	/**	Returns the index of the given label
	 *	@param value Label
	 *	@return index
	 */
	public int getIndex(String value)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (value.equals(values[i]))
				return i;
		}
		return -1;
	}
}