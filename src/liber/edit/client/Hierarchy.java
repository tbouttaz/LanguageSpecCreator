package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	Contains a class hierarchy, with class names and their 
 *	natural language expressions.
 *	@author Feikje Hielkema
 *	@version 1.0 May 2007
 */
public class Hierarchy implements IsSerializable
{
	private String value;
	private String nlexpr;
	private Hierarchy[] sub;
	
	public Hierarchy()
	{}
	
	/** Constructor
	 *	@param v Class name
	 *	@param nl NL-expression
	 *	@param size Number of children
	 */
	public Hierarchy(String v, String nl, int size)
	{
		value = v;
		nlexpr = nl;
		sub = new Hierarchy[size];
	}
	
	/**	Add a child
	 *	@param i Index
	 *	@param s Child Hierarchy
	 */
	public void addSub(int i, Hierarchy s)
	{
		sub[i] = s;
	}
	
	/**	Return class name
	 *	@return String
	 */
	public String getValue()
	{
		return value;
	}
	
	/**	Return nl-expression
	 *	@return String
	 */
	public String getNLExpr()
	{
		return nlexpr;
	}
	
	/**	Add a phrase stating the number of instances of this class
	 *	@param nr String, e.g. "(23)"
	 */
	public void addInstanceNr(String nr)
	{
		nlexpr = new String(nlexpr + " (" + nr + ")");
	}
	
	/**	Returns the children
	 *	@return Hierarchy[]
	 */
	public Hierarchy[] getSub()
	{
		return sub;
	}
}