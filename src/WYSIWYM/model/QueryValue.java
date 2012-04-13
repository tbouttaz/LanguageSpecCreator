package WYSIWYM.model;

import java.io.IOException;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.ontology.OntologyReader;

/**	Contains the value(s) of a datatype query node, and their comparators and
 *	boolean operators
 *
 *	@author Feikje Hielkema
 *	@version 1.2 08-02-2008
 */
public class QueryValue
{
	private SGNode node;	//the value is stored in a SGNode in the normal way
	private int comparator = -1;
	
	public QueryValue()
	{}
	
	/**	Constructs a QueryValue with the given node
	 *	@param n SGNode
	 */		
	public QueryValue(SGNode n)
	{
		node = n;
	}
	
	/**	Constructs a QueryValue with the given node and numeric comparator
	 *	@param n SGNode
	 *	@param comp Numeric comparator, int between 0 and 3.
	 *	@see QueryValueNode#EQUAL
	 *	@see QueryValueNode#LESS
	 *	@see QueryValueNode#MORE
	 *	@see QueryValueNode#DURING
	 */	
	public QueryValue(SGNode n, int comp)
	{
		node = n;
		comparator = comp;
	}
	
	/**	Copies the given value
	 *	@param value QueryValue to copy
	 *	@return QueryValue copy
	 */	
	public static QueryValue copyValue(QueryValue value)
	{
		QueryValue result;
		SGNode original = value.getValue();
		SGNode node = null;
		try
		{
			if (original instanceof SGIntNode)
				node = new SGIntNode(((SGIntNode)original).getValue());
			else if (original instanceof SGBooleanNode)
				node = new SGBooleanNode(((SGBooleanNode)original).getValue());
			else if (original instanceof SGDoubleNode)
				node = new SGDoubleNode(((SGDoubleNode)original).getValue());
			else if (original instanceof SGDateNode)
				node = new SGDateNode((SGDateNode) original);
			else if (original instanceof SGAddressNode)
				node = new SGAddressNode(original.getLabel());
			else
				node = new SGNode(original.getLabel());
		}
		catch(IOException e)
		{}
	
		node.setID(original.getID());
		if (original.isFinalLabel())
			node.setFinalNLLabel(new String(original.getFinalLabel()));
		result = new QueryValue(node, value.getComparator());
		return result;
	}
	
	/**	Returns the nl-representation of this value for presentation in a list,
	 *	rather than the feedback text.
	 *	@param reader Ontology
	 *	@return String
	 */	
	public String getPresentationRealisation(OntologyReader reader)
	{
		if (node == null)
			return null;
		
		StringBuffer sb = new StringBuffer();
		if (!(node instanceof SGDateNode))
		{
			switch (comparator)
			{
				case 0: sb.append ("= "); break;
				case 1: sb.append("< "); break;
				case 2: sb.append("> "); break;
			}
		}
		sb.append(node.getNLLabel(reader));
		return sb.toString();
	}
	
	/**	Returns the nl-representation of this value
	 *	@param reader Ontology
	 *	@return String
	 */
	public String realise(OntologyReader reader)
	{
		if (node == null)
			return null;
			
		StringBuffer sb = new StringBuffer();
		if (!(node instanceof SGDateNode))
		{
			switch (comparator)
			{
				case 0: sb.append ("exactly "); break;
				case 1: sb.append("less than "); break;
				case 2: sb.append("more than "); break;
			}
		}
		sb.append(node.getNLLabel(reader));
		return sb.toString();
	}
	
	/**	Returns the nl-representation of this value
	 *	For number nodes for 'any property', realise 'a number less than 2' or 
	 *	'the number 3'
	 *	@param any If true, return realisation for 'any property'
	 *	@param reader Ontology
	 *	@return String	
	 */
	public String realise(boolean any, OntologyReader reader)
	{
		if (!any)
			return realise(reader);
		if (!((node instanceof SGIntNode) || (node instanceof SGDoubleNode)))
			return realise(reader);
		
		StringBuffer sb = new StringBuffer();
		switch (comparator)
		{
			case 0: sb.append ("the number "); break;
			case 1: sb.append("a number less than "); break;
			case 2: sb.append("a number more than "); break;
		}
		sb.append(node.getNLLabel(reader));
		return sb.toString();
	}
	
	/**	Returns an NP for the phrase 'a date' or 'a number', null for all other
	 *	datatypes.
	 *
	 *	@param i integer with datatype. If 0, treat as a date; if 1, treat as a number.
	 *	@return DTNode containing Noun Phrase
	 */
	public static DTNode getAnyPropertyNP(int i)
	{
		try
		{
			DTNode np = new DTNode(LinguisticTerms.NP, null, null, null);
			DTNode det = new DTNode(LinguisticTerms.DET, LinguisticTerms.DET, "a", null);
			DTEdge detEdge = new DTEdge(LinguisticTerms.DET, np, det);
			DTNode noun = new DTNode(LinguisticTerms.NOUN, null, null, null);
			DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD, np, noun);
			
			if (i == 0)
				noun.setRoot("date");
			else if (i == 1)
				noun.setRoot("number");
			else
				return null;
				
			return np;
		}
		catch (NameAlreadyBoundException e)
		{
			return null;
		}	//impossible
	}
	
	/**	Returns an NP for the phrase 'a date' or 'a number',
	 *	return null for all other datatypes
	 *
	 *	@return DTNode containing Noun Phrase
	 */
	public DTNode getAnyPropertyNP()
	{
		if (node instanceof SGDateNode)
			return getAnyPropertyNP(0);
		else if ((node instanceof SGIntNode) || (node instanceof SGDoubleNode))
			return getAnyPropertyNP(1);
		else
			return null;
	}
	
	/** Returns the value Node
	 *	@return SGNode
	 */
	public SGNode getValue()
	{
		return node;
	}
	
	/** Sets the value Node
	 *	@param n SGNode
	 */
	public void setValue(SGNode n)
	{
		node = n;
	}

	/** Returns the numeric comparator
	 *	@return int between 0 and 3
	 */
	public int getComparator()
	{
		return comparator;
	}
	
	/** Sets the numeric comparator
	 *	@param comp int between 0 and 3
	 */
	public void setComparator(int comp)
	{
		comparator = comp;
	}
}