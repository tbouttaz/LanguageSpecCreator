package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	Contains the requirements on a date that a user has specified.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 January 2008
 */
public class QueryDateValue implements IsSerializable
{
	private int comparator = -1;
	private String[] dates;
	
	public QueryDateValue()
	{}
	
	/**	Constructor, sets the numeric comparator
	 *	(before, after, during) and the date itself
	 *	@param comp Numeric comparator. 1=before, 2=after, 3=during.
	 *	@param d String[] with start and end day, month and year
	 */
	public QueryDateValue(int comp, String[] d)
	{
		dates = d;
		comparator = comp;
	}
	
	/**	Constructor, sets the date
	 *	@param d String[] with start and end day, month and year
	 */
	public QueryDateValue(String[] d)
	{
		dates = d;
	}
	
	/**	Returns the numeric comparator.
	 *	1=before, 2=after, 3=during.
	 *	@return int
	 */
	public int getComparator()
	{
		return comparator;
	}
	
	/**	Sets the numeric comparator.
	 *	1=before, 2=after, 3=during.
	 *	@param c int
	 */
	public void setComparator(int c)
	{
		comparator = c;
	}
	
	/**	Returns the date
	 *	@return String[] with start and end day, month and year
	 */
	public String[] getDates()
	{
		return dates;
	}
}