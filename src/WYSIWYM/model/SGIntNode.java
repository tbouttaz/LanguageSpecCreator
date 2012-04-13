package WYSIWYM.model;

import java.io.IOException;

import WYSIWYM.ontology.OntologyReader;

/**	SGNumberNode is a SGNode that contains a float value.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 02-04-2007
 */
public class SGIntNode extends SGNode implements DatatypeNode
{
	private Integer value, old;
	
	/**	Default constructor
	 */
	public SGIntNode()
	{
		super("number");
	}
	
	/**	Constructs a node with the given Integer value
	 *	@param i Integer
	 */
	public SGIntNode(Integer i)
	{
		super("number");
		value = i;
	}
	
	/**	Constructs a node with the given Integer value
	 *	@param o Object
	 *	@throws IOException if o cannot be cast to an Integer
	 */
	public SGIntNode(Object o) throws IOException
	{
		super("number");
		setValue(o);
	}
	
	/**	Constructs a node with the given int value
	 *	@param f int value
	 */
	public SGIntNode(int f)
	{
		super("number");
		value = new Integer(f);
	}
	
	/**	@see DatatypeNode#getDatatype()
	 */
	public int getDatatype()
	{
		return INT;
	}
	
	/**	Returns the int value as a string, or "number" if the value is null
	 *	@return String
	 */
	public String getLabel()
	{
		if (value == null)
			return "number";
		return value.toString();
	}
	
	/**	Sets the int value
	 *	@param f int value
	 */
	public void setLabel(int f)
	{
		value = new Integer(f);
	}
	
	/**	Returns the nl-representation of the value
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getNLLabel(OntologyReader reader)
	{
		if (value == null)
			return "this many";
		return SGNode.getNL(value);
	}
	
	/**	Returns the Integer value
	 *	@return Object
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**	Sets the Integer value
	 *	@param val Object
	 *	@throws IOException if val cannot be cast to an Integer
	 */
	public void setValue(Object val) throws IOException
	{
		old = value;
		if (val instanceof Integer)
			value = (Integer) val;
		else
			throw new IOException("Expected Integer in SGIntNode!");
	}
	
	/**	Restores the previous label of this node (if any), deleting the final
	 *	NL label if that exists
	 *
	 *	@return true if the previous label could be restored
	 */
	public boolean restoreLabel()
	{
		if (old == null)
			return false;
		value = old;
		old = null;
		return true;
	}
	
	/**	Returns the original Integer value
	 *	@return Object 
	 */
	public Object getOldValue()
	{
		return old;
	}
	
	/**	Sets the original value
	 *	@param b Object
	 *	@throws IOException if b could not be cast to an Integer
	 */
	public void setOldValue(Object b) throws IOException
	{
		if (b == null)
			return;
		if (b instanceof Integer)
			old = (Integer) b;
		else
			throw new IOException("Expected Integer in SGIntNode!");
	}
	
	/**	Returns null, because a number does not need a determiner 
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