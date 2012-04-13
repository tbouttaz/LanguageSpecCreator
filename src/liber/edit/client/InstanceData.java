package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	Contains data about an individual: the id, class type,
 *	and any values for properties that have been specified.
 *	@author Feikje Hielkema
 *	@version 1.0 May 2008
 */
public class InstanceData implements IsSerializable
{
	private String type, id;
	private String[] properties, values;
	
	/**	Default constructor
	 */
	public InstanceData()
	{}
	
	/**	Constructor for existing individuals
	 *	@param i ID
	 */
	public InstanceData(String i)
	{
		id = i;
	}
	
	/**	Constructor for newly created individuals
	 *	@param t class type
	 *	@param size number of property values specified
	 */
	public InstanceData(String t, int size)
	{
		type = t;
		values = new String[size];
		properties = new String[size];
	}
	
	/**	Adds a property and value at the given index
	 *	@param prop Property name
	 *	@param value Value
	 *	@param cntr Index
	 */
	public void add(String prop, String value, int cntr)
	{
		if (cntr >= properties.length)
			return;
		properties[cntr] = prop;
		values[cntr] = value;
	}
	
	/**	Returns the value at index i
	 *	@param i Index
	 *	@return String value
	 */
	public String getValue(int i)
	{
		return values[i];
	}
	
	/**	Returns the property at index i
	 *	@param i Index
	 *	@return String property name
	 */
	public String getProperty(int i)
	{
		return properties[i];
	}
	
	/**	Returns the class name
	 *	@return String class name
	 */
	public String getType()
	{
		return type;
	}
	
	/**	Returns the unique ID
	 *	@return String ID
	 */
	public String getID()
	{
		return id;
	}
	
	/**	Returns the number of values
	 *	@return size
	 */
	public int size()
	{
		return properties.length;
	}
}