package WYSIWYM.model;

import java.io.IOException;

import WYSIWYM.ontology.OntologyReader;

/**	SGBooleanNode is a SGNode and DatatypeNode that contains
 *	a boolean value.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 02-04-2007
 */
public class SGBooleanNode extends SGNode implements DatatypeNode
{
	private Boolean value, old;
	
	/**	Default constructor
	 */
	public SGBooleanNode()
	{
		super("boolean");
	}
	
	/**	Constructs a node with the given boolean value
	 *	@param f	boolean value
	 */
	public SGBooleanNode(boolean f)
	{
		super("boolean");
		value = new Boolean(f);
	}
	
	/**	Constructs a node with the given Boolean value
	 *	@param o Object
	 *	@throws IOException if the parameter cannot be cast to a Boolean.
	 */
	public SGBooleanNode(Object o) throws IOException
	{
		super("boolean");
		setValue(o);
	}
	
	/**	@see DatatypeNode#getDatatype()
	 */
	public int getDatatype()
	{
		return BOOLEAN;
	}
	
	/**	Returns the natural language representation of the boolean value
	 *	@return String value
	 */
	public String getLabel()
	{
		String result = getNLLabel(null);
		if (result.equals("neutral"))
			return "boolean";
		return result;
	}
	
	/**	Sets the boolean value
	 *	@param b boolean value
	 */
	public void setLabel(boolean b)
	{
		old = value;
		value = new Boolean(b);
	}
	
	/**	Returns the natural language representation of the boolean value
	 *	@param reader Ontology
	 *	@return String value
	 */
	public String getNLLabel(OntologyReader reader)
	{
		if (value == null)
			return "neutral";	
		return value.toString();
	}
	
	/**	Returns the Boolean value
	 *	@return Object
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**	Sets the boolean value
	 *	@param o Object
	 *	@throws IOException if o cannot be cast to a Boolean
	 */
	public void setValue(Object o) throws IOException
	{
		old = value;
		if (o instanceof Boolean)
			value = (Boolean) o;
		else
			throw new IOException("Expected Boolean in SGBooleanNode!");
	}
	
	/**	Restores the previous label of this node (if any), deleting the final
	 *	NL label if that exists
	 *	@return true if the label could be restored
	 */
	public boolean restoreLabel()
	{
		if (old == null)
			return false;
		value = old;
		old = null;
		return true;
	}
	
	/**	Returns the original Boolean value
	 *	@return Object
	 */
	public Object getOldValue()
	{
		return old;
	}
	
	/**	Sets an old value
	 *	@param b Object
	 *	@throws IOException if b could not be cast to a Boolean
	 */
	public void setOldValue(Object b) throws IOException
	{
		if (b == null)
			return;
		if (b instanceof Boolean)
			old = (Boolean) b;
		else
			throw new IOException("Expected Boolean in SGBooleanNode!");
	}
	
	/**	Returns null, because 'true' does not need a determiner 
	 *	@see SGNode#getDeterminer(int, OntologyReader)
	 *	@param type Type of determiner
	 *	@param r Ontology
	 *	@return null
	 */
	public String getDeterminer(int type, OntologyReader r)
	{
		return null;
	}
}