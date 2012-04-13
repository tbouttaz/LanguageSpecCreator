package WYSIWYM.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.transformer.SemanticGraphTransformer;

/**	QueryValueNode is a node in the QueryGraph that contains a datatype value. 
 *	This value is presented in a QueryValue, which either contains the datatype and a
 *	numeric comparator (>, <, =, 'during'), or a list of query-values with a boolean operator.
 *
 *	CURRENTLY QUERYVALUENODE ASSUMES THAT A QUERYVALUE HAS ONLY ONE LAYER OF CHILDREN;
 *	MEANING, THAT ALL ITS CHILDREN CONTAIN A DATATYPE!
 *
 *	@author Feikje Hielkema
 *	@version 1.2 08-02-2008
 */
public class QueryValueNode extends QueryNode implements DatatypeNode
{
	private List<QueryValue> values = new ArrayList<QueryValue>();
	private int booleanOperator = -1;
	
	/** Value must match exactly */	
	public static final int EQUAL = 0;
	/** Value must be less */	
	public static final int LESS = 1;
	/** Value must be more */	
	public static final int MORE = 2;
	/** Value must fall in range */	
	public static final int DURING = 3;
	/** Each QueryValueNode has this label */	
	public static final String QUERYVALUE = "-QueryValue-";	//make sure it cannot be confused with ontology property
	
	/**	Default constructor, constructs a node with the default label
	 */
	public QueryValueNode()
	{
		super(QUERYVALUE);
	}
	
	/**	Constructs a node with the default label and the given operator. 
	 *	The default value of the operator is -1; value can be set to
	 *	0 = conjunction (and), 1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *	@param op int between 0 and 2
	 */
	public QueryValueNode(int op)
	{
		super(QUERYVALUE);
		booleanOperator = op;
		//value = new QueryValue(op);
	}

	/**	Returns the datatype of this node's values.
	 *	@see DatatypeNode#getDatatype()
	 *	@return int with datatype
	 */
	public int getDatatype()
	{
		if (values.size() > 0)
		{
			SGNode node = values.get(0).getValue();
			if (node instanceof DatatypeNode)
				return ((DatatypeNode) node).getDatatype();
		}
		return -1;
	}
	
	/**	Returns an array with the nl-representations of each value
	 *
	 *	@param reader Ontology
	 *	@return String[]
	 */
	public String[] getValueStringArray(OntologyReader reader)
	{
		String[] result = new String[values.size()];
		for (int i = 0; i < values.size(); i++)
			result[i] = values.get(i).getPresentationRealisation(reader);	
		return result;
	}
	
	/**	Returns null, as the DatatypeNode value is unused.
	 *	@see DatatypeNode#getValue()
	 *	@return null
	 */
	public Object getValue()
	{
		return null;
	}
	
	/**	Overload.
	 *	Does nothing, as the DatatypeNode value is unused.
	 *	@see DatatypeNode#getValue()
	 *	@param value Object containing old value
	 *	@throws IOException never happens
	 */
	public void setOldValue(Object value) throws IOException
	{}
 	
 	/**	Returns null, as the DatatypeNode value is unused.
	 *	@see DatatypeNode#getOldValue()
	 *	@return null
	 */
 	public Object getOldValue()
 	{
 		return null;
 	}
	
	/**	Sets the QueryValue.
	 *	@param v Object
	 *	@throws IOException if v cannot be cast to a QueryValue
	 */
	public void setValue(Object v) throws IOException
	{
		if (v instanceof QueryValue)
			values.add((QueryValue) v);
		else
			throw new IOException("Expected QueryValue in QueryValueNode!");
	}
	
	/**	Constructs a copy of the given node, and add it with all its edges to the 
	 *	QueryGraph
	 *	@param original QueryValueNode to copy
	 *	@param existing HashMap with copied nodes
	 *	@param sg SemanticGraphTransformer with the copy
	 *	@return QueryValueNode, copy of node
	 *	@throws NameAlreadyBoundException, very unlikely
	 */
	public static QueryValueNode copyQueryValueNode(QueryValueNode original, Map<String,SGNode> existing, SemanticGraphTransformer sg) throws NameAlreadyBoundException
	{
		QueryValueNode result = new QueryValueNode(original.getBooleanOperator());	//getLabel());
		result.setID(original.getID());
		if (original.isFinalLabel())
			result.setFinalNLLabel(new String(original.getFinalLabel()));		
		result.setRemovable(original.isRemovable());
		result.setQuote(original.isQuote());
		result.setOldLabel(original.getOldLabel());
		result.setSequenceNr(original.getSequenceNr());
		result.setRealise(original.mustRealise());
		result.setSGID(original.getSGID());
		existing.put(result.getID(), result);
		try
		{
			for (QueryValue value : original.getValues())
				result.setValue(QueryValue.copyValue(value));
		}
		catch (IOException e)
		{}
		return result;
	}
	
	/**	Returns the labels of this node's values, to present them as
	 * 	(stand-alone) options (instead of in the feedback text).
	 *	For instance "The person John Smith"
	 *
	 *	@param reader Ontology
	 *	@return List<String> with labels
	 */
	public List<String> getChoiceLabels(OntologyReader reader)
	{
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < values.size(); i++)
			result.add(values.get(i).getValue().getChoiceLabel(reader));
		return result;
	}
	
	/**	Returns the empty string
	 *	@see SGNode#getNLLabel(OntologyReader)
	 *
	 *	@param reader Ontology
	 *	@return empty String
	 */
	public String getNLLabel(OntologyReader reader)
	{
		return "";
	}
	
	/**	Sets the nl-label with this node's nl-representation
	 *
	 *	@param reader Ontology
	 */
	public void setNLLabel(OntologyReader reader)
	{
		super.setNLLabel(getRealisation(reader));	
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
	
	/**	If this node contains the given value, that value is removed and the 
	 *	method returns true; otherwise it returns false
	 *
	 *	@param str String value to remove
	 *	@param reader Ontology
	 *	@return true if the value was present and removed
	 */
	public boolean removeValue(String str, OntologyReader reader)
	{
		List<QueryValue> delete = new ArrayList<QueryValue>();
		for (int i = 0; i < values.size(); i++)
		{
			QueryValue v = values.get(i);
			if (v.getValue().getChoiceLabel(reader).equals(str))
				delete.add(v);
		}
		for (int i = 0; i < delete.size(); i++)
			values.remove(delete.get(i));
		
		if ((values.size() == 1) && (booleanOperator < 2))	//if there is only one value left, 
			booleanOperator = -1;		//the booleanoperator can't be 'and' or 'or'
		
		if (delete.size() > 0)
			return true;
		return false;
	}
	
	/**	Returns true if this node contains no more values
	 *	@return true if the node is empty
	 */
	public boolean isEmpty()
	{
		if (values.size() == 0)
			return true;
		return false;
	}
	
	/**	Returns the label of the SGNode, if it corresponds to a class
	 *	@return String
	 */
	public String getClassType()
	{
		if (values.size() == 0)
			return null;
	
		SGNode node = values.get(0).getValue();
		if (node instanceof DatatypeNode)
			return null;
		if (node instanceof SGDateNode)
			return OntologyReader.DATE;
		return node.getLabel();
	}
	
	/**	Gets the surface realisation of this QueryValue
	 *	UNUSED; REALISATION SHOULD TAKE PLACE IN SURFACEREALISER NOT TEXT PLANNING
	 *	@deprecated
	 */
	public String getRealisation(OntologyReader reader)
	{
		StringBuffer sb = new StringBuffer();
		if (booleanOperator == 2)
			sb.append("not ");
		for (int i = 0; i < (values.size() - 2); i++)
		{
			sb.append(values.get(i).realise(reader));
			sb.append(", ");
		}
		if (values.size() > 1)
		{
			sb.append(values.get(values.size() - 2).realise(reader));
			switch (booleanOperator)
			{
				case 0: sb.append(" and "); break;		//x, y and z
				case 1: sb.append(" or "); break;		//x, y or z
				case 2: sb.append(" or "); break;		//not x, y or z
			}
		}
		sb.append(values.get(values.size() - 1).realise(reader));
		return sb.toString();
	}
	
	/**	Adds a QueryValue
	 *	@param child value
	 */
	public void add(QueryValue child)
	{
		values.add(child);
	}
	
	/**	Adds a QueryValue with the given node
	 *	@param n SGNode
	 */
	public void add(SGNode n)
	{
		values.add(new QueryValue(n));
	}
	
	/**	Adds a QueryValue with the given node and numeric comparator. 
	 *	The comparator states how a resource description's value 
	 *	should match the requirement; @see #EQUAL, @see #LESS, @see #MORE, @see #DURING.
	 *
	 *	@param n SGNode
	 *	@param comp Numeric comparator, int between 0 and 3.
	 */
	public void add(SGNode n, int comp)
	{
		values.add(new QueryValue(n, comp));
	}
	
	/**	Removes the QueryValue at the given index
	 *	@param idx Index
	 */
	public void remove(int idx)
	{
		values.remove(idx);
	}
	
	/** Returns the boolean operator. Default value is -1; value can be set to
	 *	0 = conjunction (and), 1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *	@return integer between 0 and 2
	 */
	public int getBooleanOperator()
	{
		return booleanOperator;
	}

	/** Sets the boolean operator. Default value is -1; value can be set to
	 *	0 = conjunction (and), 1 = disjunction (or), 2 = negation (not).
	 *	@see QueryGraph
	 *	@param op integer between 0 and 2
	 */	
	public void setBooleanOperator(int op)
	{
		booleanOperator = op;
	}
	
	/** Returns a list with all QueryValues.
	 *	@return List<QueryValue>
	 */
	public List<QueryValue> getValues()
	{
		return values;
	}
	
	/** Returns the nl-representation of the numeric comparator.
	 *	@param any If true, returns for number values with the boolean operator 'not'
	 *	'a number that is not exactly ..' rather than 'not ...'. Implemented to improve
	 *	the realisation of 'any property' ('is related to not 20' sounds dreadful)
	 *	@return String
	 */
	public String getOperatorNL(boolean any)
	{
		if (values.size() > 1)
			return getOperatorNL();
		
		if (values.size() == 0)
			return getOperatorNL();
				
		SGNode n = values.get(0).getValue();		
		if (any && ((n instanceof SGIntNode) || (n instanceof SGDoubleNode)) && (booleanOperator == 2))
			return "a number that is not";	//e.g. 'a number that is not exactly 20'
		
		return getOperatorNL();		 
	}
	
	/** Returns the nl-representation of the numeric comparator.
	 *	@return String
	 */
	public String getOperatorNL()
	{
		if (booleanOperator == 0)
			return "and";
		else if (booleanOperator == 1)
			return "or";
		else if (booleanOperator == 3)
			return "maybe";
		
		//booleanoperator must be 'not'
		if (values.size() > 1)
			return "nor";		//e.g. neither x nor y		
				 
		if (values.size() > 0)
		{
			if ((values.get(0).getComparator() == 0) || (values.get(0).getComparator() == 3))
				return "not";
			SGNode val = values.get(0).getValue();
			if ((val instanceof SGIntNode) || (val instanceof SGDoubleNode))
				return "no";
		}	
		return "not";		
	}
}