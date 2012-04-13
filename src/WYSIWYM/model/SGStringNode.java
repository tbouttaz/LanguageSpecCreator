package WYSIWYM.model;

import java.io.IOException;

import WYSIWYM.ontology.OntologyReader;

/**	SGStringNode is a SGNode that contains a String datatype
 *
 *	@author Feikje Hielkema
 *	@version 1.1 02-04-2007
 */
public class SGStringNode extends SGNode implements DatatypeNode
{
	private String value, old;
	
	/**	Default constructor
	 */
	public SGStringNode()
	{
		super("string");
	}
	
	/**	Constructs a node with the given String value
	 *	@param i String value
	 */
	public SGStringNode(String i)
	{
		super("string");
		value = i;
		setFinalNLLabel(i);
	}
	
	/**	Constructs a node with the given String value
	 *	@param o Object
	 *	@throws IOException if o could not be cast to a String
	 */
	public SGStringNode(Object o) throws IOException
	{
		super("string");
		setValue(o);
		setFinalNLLabel(value);
	}
	
	/**	@see DatatypeNode#getDatatype()
	 */
	public int getDatatype()
	{
		return STRING;
	}
	
	/**	Returns the value, or "something" if the value is null
	 *	@return String
	 */
	public String getLabel()
	{
		return getNLLabel(null);
	}
	
	/*	Sets the value
	 *	@param s String value
	 */
	public void setLabel(String s)
	{
		old = value;
		value = s;		
	}
	
	/**	Returns the value, or "something" if the value is null
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getNLLabel(OntologyReader reader)
	{
		if (value == null)
			return "something";
		return value;
	}
	
	/**	Returns the String value
	 *	@return Object
	 */
	public Object getValue()
	{
		return value;
	}
	
	/**	Sets the String value
	 *	@param val Object
	 *	@throws IOException if val cannot be cast to a String
	 */
	public void setValue(Object val) throws IOException
	{
		old = value;		//remember the old value, so we can undo the change
		if (val instanceof String)
		{
			value = (String) val;
			setFinalNLLabel(value);
		}
		else
			throw new IOException("Expected String in SGStringNode!");
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
	
	/**	Returns the original String value
	 *	@return Object 
	 */
	public Object getOldValue()
	{
		return old;
	}
	
	/**	Sets the original value
	 *	@param l Object
	 *	@throws IOException if l could not be cast to an String
	 */
	public void setOldValue(Object l) throws IOException
	{
		if (l == null)
			return;
		if (l instanceof String)
			old = (String) l;
		else
			throw new IOException("Expected String in SGStringNode!");
	}
	
	/**	Returns null
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