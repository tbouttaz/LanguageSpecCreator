package WYSIWYM.model;

import java.io.IOException;
import WYSIWYM.ontology.OntologyReader;

/**	SGNumberNode is a SGNode that instead of a String has a float.
 *
 *	@author Feikje Hielkema
 *	@version 1.1
 */
public class SGDoubleNode extends SGNode implements DatatypeNode
{
	private Double value, old;
	
	/**	Default constructor
	 */
	public SGDoubleNode()
	{
		super("number");
	}
	
	/**	Constructs the node with the given value.
	 *
	 *	@param d Double value
	 */
	public SGDoubleNode(Double d)
	{
		super("number");
		value = d;
	}
	
	/**	Constructs the node with the given value.
	 *
	 *	@param d double value
	 */
	public SGDoubleNode(double d)
	{
		super("number");
		value = new Double(d);
	}
	
	/**	Constructs the node with the given (Double) value.
	 *
	 *	@param o Object
	 *	@throws IOException If d cannot be cast to a Double
	 */
	public SGDoubleNode(Object o) throws IOException
	{
		super("number");
		setValue(o);
	}
	
	/** @see WYSIWYM.model.DatatypeNode#getDatatype()
	 */
	public int getDatatype()
	{
		return DOUBLE;
	}
	
	/**	Returns the node's value as a String
	 *
	 *	@return String value
	 */
	public String getLabel()
	{
		if (value == null)
			return "number";
		return value.toString();
	}
	
	/**	Sets the nodes value
	 *
	 *	@param f double value
	 */
	public void setLabel(double f)
	{
		value = f;
	}
	
	/** Sets the natural language version of the label
	 *
	 *	@param reader Ontology
	 *	@return String nl-label
	 */
	public String getNLLabel(OntologyReader reader)
	{
		if (value == null)
			return "this many";
		return SGNode.getNL(value);
	}
	
	/** @see WYSIWYM.model.DatatypeNode#getValue()
	 */
	public Object getValue()
	{
		return value;	//.doubleValue();
	}

	/** @see WYSIWYM.model#DatatypeNode.setValue(Object)
	 */
	public void setValue(Object o) throws IOException
	{
		old = value;
		if (o instanceof Double)
			value = (Double) o;
		else
			throw new IOException("Expected Double in SGDoubleNode!");		
	}
	
	/**	Restores the previous label of this node (if any), deleting the final
	 *	NL label if that exists
	 *
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
	
	/** @see WYSIWYM.model.DatatypeNode#getOldValue()
	 */
	public Object getOldValue()
	{
		return old;
	}
	
	/** @see DatatypeNode#setOldValue(Object)
	 */
	public void setOldValue(Object b) throws IOException
	{
		if (b == null)
			return;
		if (b instanceof Double)
			old = (Double) b;
		else
			throw new IOException("Expected Double in SGDoubleNode!");
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