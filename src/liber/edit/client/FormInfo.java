package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	Describes a form.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 August 2008
 */
public class FormInfo implements IsSerializable
{
	private String property, nl;	//property and its nl representation
	private String[] values;		//restricted values
	
	public FormInfo()
	{}
	
	/**	Constructor
	 *	@param prop Property name
	 *	@param n Nl-representation of property
	 *	@param v String[] with restricted values of property. If values are not restricted, pass an empty array
	 */
	public FormInfo(String prop, String n, String[] v)
	{
		values = v;
		property = prop;
		nl = n;
	}
	
	/**	Returns the restricted values
	 *	@return String[]
	 */
	public String[] getValues()
	{
		return values;
	}
	
	/**	Returns the property name
	 *	@return String
	 */
	public String getProperty()
	{
		return property;
	}
	
	/**	Returns the nl-representation of the property name
	 *	@return String
	 */
	public String getNL()
	{
		return nl;
	}
}