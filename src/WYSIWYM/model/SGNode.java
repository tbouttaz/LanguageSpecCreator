package WYSIWYM.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NameAlreadyBoundException;

import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.transformer.SemanticGraphTransformer;
import WYSIWYM.util.BadAnchorException;

/**	SGNode is a node in the Semantic Graph. It has alphabetised lists of the 
 *	incoming and outgoing nodes.
 *	ID is an unique identifier; default setting is identical to label (id exists
 *	in case there are two objects (e.g. persons) with the same name).
 *
 *	@author Feikje Hielkema
 *	@version 1.1, 24-10-2006
 *
 *	@version 1.2, 08-02-08
 */
public class SGNode extends Node implements Comparable
{
	/** Use determiner 'some'*/
	public final static int SOME = 0;
	/** Use determiner 'this'*/
	public final static int THIS = 1;
	/** Use determiner 'the'*/
	public final static int THE = 2;
	/** Use determiner 'a'*/
	public final static int A = 3;
	/** Use determiner 'another'*/
	public final static int ANOTHER = 4;
	
	/** node created in this session */
	public final static int NEW = 0;	
	/** node from database, show its information */	
	public final static int SHOW = 1;
	/** node from database, but has no information beside NL things */
	public final static int NOINFO = 2;	
	/** node from database, hide its information */
	public final static int HIDE = 3;
	/** node from database, but not all its information has been retrieved yet */
	public final static int INCOMPLETE = 4;	
		
	protected int mustRealise = 0;
	protected String nlLabel;
	protected boolean finalLabel = false, removable = true;
	private boolean quote = false;
	private String oldLabel;
	private int sequenceNr = 0;	//rank nr. in order of creation
	private String sgID;
	
	/**	Constructs SGNode with given label
	 *	@see Node#Node(String)
	 *
	 *	@param lbl label
	 */
	public SGNode(String lbl)
	{
		super(lbl);
	}
	
	/**	Constructs a copy of the given node, and adds it with all its edges to the 
	 *	Semantic Graph
	 *
	 *	@param node SGnode to copy
	 *	@param existing HashMap with nodes already copied
	 *	@param sg Semantic Graph Transformer
	 *	@throws NameAlreadyBoundException very unlikely
	 *	@return copy of SGNode
	 */
	public static SGNode copyNode(SGNode node, Map<String,SGNode> existing, SemanticGraphTransformer sg) throws NameAlreadyBoundException
	{
		SGNode result = null;
		try
		{
			if (node instanceof SGIntNode)
				result = new SGIntNode(((SGIntNode)node).getValue());
			else if (node instanceof SGBooleanNode)
				result = new SGBooleanNode(((SGBooleanNode)node).getValue());
			else if (node instanceof SGDoubleNode)
				result = new SGDoubleNode(((SGDoubleNode)node).getValue());
			else if (node instanceof SGDateNode)
				result = new SGDateNode((SGDateNode) node);
			else if (node instanceof SGAddressNode)
				result = new SGAddressNode(node.getLabel());
			else if (node instanceof SGStringNode)
				result = new SGStringNode(((SGStringNode)node).getValue());
			else
				//TODO: URI: might need to change to URI --> add the namespace to result
				result = new SGNode(node.getLabel());
			
			result.setNameSpace(node.getNameSpace());
			
			if (result instanceof DatatypeNode)
				((DatatypeNode)result).setOldValue(((DatatypeNode) node).getOldValue());
			else
				result.setOldLabel(node.getOldLabel());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		result.setID(sg.getGraph().getFreeID());
		if (node.isFinalLabel())
			result.setFinalNLLabel(new String(node.getFinalLabel()));		
		result.setRemovable(node.isRemovable());
		result.setQuote(node.isQuote());
		result.setSequenceNr(node.getSequenceNr());
		result.setRealise(node.mustRealise());
		result.setSGID(node.getSGID());
		
		try
		{
			new Anchor(node.getAnchor(), result);
		}
		catch (BadAnchorException e)
		{}
		existing.put(node.getID(), result);
		
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			if (edge instanceof QueryEdge)
				QueryEdge.copyQueryEdge((QueryEdge) edge, existing, sg, result);
			else
				SGEdge.copySGEdge(edge, existing, sg, result);
		}
		return result;
	}
	
	/**	Sets the type of realisation of this node, i.e. where it came from: just created,
	 *	from the archive, not fully retrieved yet?
	 *	@param r type
	 */
	public void setRealise(int r)
	{
		mustRealise = r;
		if (r > NEW)	//if the node is from the database,
			setRemovable(false);	//it should not be possible to remove it
	}
	
	/**	Returns the type of realisation of this node, i.e. where it came from: just created,
	 *	from the archive, not fully retrieved yet?
	 *	@return type
	 */	
	public int mustRealise()
	{
		return mustRealise;
	}
	
	/**	Returns the ID the Semantic Graph has assigned to this node
	 *	@return SG ID
	 */	
	public String getSGID()
	{
		return sgID;
	}
	
	/**	Sets the ID the Semantic Graph has assigned to this node
	 *	@param id SG ID
	 */	
	public void setSGID(String id)
	{
		sgID = id;
	}
	
	/**	Overload. This stores the previous label before changing it (to enable undo)
	 *	@see Node#setLabel(String)
	 *	@param lbl label
	 */
	public void setLabel(String lbl)
	{
		oldLabel = getLabel();
		if (oldLabel == null)
			oldLabel = "";
		super.setLabel(lbl);
	}
	
	/**	Sets the sequence nr. of this node
	 *
	 *	@param seq sequence number
	 */
	public void setSequenceNr(int seq)
	{
		sequenceNr = seq;
	}
	
	/**	Returns the sequence number
	 *	@return int
	 */
	public int getSequenceNr()
	{
		return sequenceNr;
	}
	
	/**	Restores the previous label of this node (if any), deleting the final
	 *	NL label if that exists
	 *	@return true if the label could be restored
	 */
	public boolean restoreLabel()
	{
		if (oldLabel == null)
			return false;

		super.setLabel(oldLabel);
		if (finalLabel)		//final label should be deleted, as it's the NL expression
		{					//of the PREVIOUS label
			finalLabel = false;
			nlLabel = null;
		}
		return true;
	}
	
	/**	Returns the number of outgoing edges that are not NLEdges (such as 'name'
	 *	or 'title')
	 *	@param reader Ontology
	 *	@return int
	 */
	public int getOutgoingEdgesWithoutNLNumber(OntologyReader reader)
	{
		Iterator it = getOutgoingEdges();
		int result = 0;
		while (it.hasNext())
		{
			SGEdge e = (SGEdge) it.next();
			if ((!reader.useAsProperName(e.getLabel())) && Lexicon.specExists(e.getLabel(), reader))
				result++;
		}
		return result;
	}
	
	/**	Returns the number of outgoing edges that are not NLEdges (such as 'name'
	 *	or 'title')
	 *	@return int
	 */
	public int getOutgoingEdgesWithoutIDNumber()
	{
		Iterator it = getOutgoingEdges();
		int result = 0;
		while (it.hasNext())
		{
			SGEdge e = (SGEdge) it.next();
			if (!e.getLabel().equals("ID")) 
				result++;
		}
		return result;
	}	
	
	/**	Returns all the outgoing edges with the given label
	 *	@param	label Edge label
	 *	@return SGEdge[]
	 */
	public SGEdge[] getOutgoingEdgesArray(String label)
	{
		SGEdge[] result = new SGEdge[1];
		return getOutgoingEdges(label).toArray(result);
	}
	
	/**	Returns all edges with the given label that did not come from the database
	 *	(i.e. that were made this session!)
	 *	@param	label Edge label
	 *	@return SGEdge[]
	 */
	public SGEdge[] getNewOutgoingEdgesArray(String label)
	{
		List<Edge> list = getOutgoingEdges(label);
		List<SGEdge> result = new ArrayList<SGEdge>();
		for (int i = 0; i < list.size(); i++)
		{
			SGEdge edge = (SGEdge) list.get(i);
			if (edge.mustRealise() == NEW)		//only add the edges that were created in this session
				result.add(edge);
		}
		return result.toArray(new SGEdge[0]);
	}
	
	/*	Returns the natural language representation of the given number
	 *	@param n Number
	 *	@return String representation
	 */
	public static String getNL(Number n)
	{
		StringBuffer sb = new StringBuffer();
		if (n instanceof Integer)
			sb.append(((Integer)n).toString());
		else if (n instanceof Double)
			sb.append(((Double)n).toString());
	
		int e = sb.indexOf("E");
		if (e > 0)	//we don't want the number presented as e value!
		{
			try
			{	//e.g. 1.3E5 becomes 130,000
				int power = new Integer(sb.substring(e + 1));
				sb = new StringBuffer(sb.substring(0, e));	//get the number before e
				int dot = sb.indexOf(".");
				if (dot < 0)	//e.g. 3E2 becomes 300
					for (int i = 0; i < power; i++)	
						sb.append("0");
				else
				{
					sb.deleteCharAt(dot);	//remove the dot
					int decimals = sb.length() - dot;
					if (decimals > power)	//if there are so many decimals that we don't have to add zeroes, just move the dot
						sb.insert(dot + power, '.');	//e.g. 1.235E2 becomes first 123.5
					else	//else add some zeroes, the difference between decimals and power
						for (int i = decimals; i < power; i++)	//e.g. 1.1E2 becomes 110
							sb.append("0");	
				}
			}
			catch (NumberFormatException ex)
			{
				ex.printStackTrace();	//that would be weird...
			}
		}
	
		int idx = sb.indexOf(".");
		if (idx < 0)	//if there is no dot, just get the end of the string
			idx = sb.length();
		while (idx > 3)
		{	//add komma's every 3 nrs (e.g. 1,000,000)
			idx = idx - 3;
			sb.insert(idx, ',');
		}
		return sb.toString();
	}	
	
	private String getNL(OntologyReader reader)
	{
		return getNL(false, reader);
	}
	
	private String getNL(boolean query, OntologyReader reader)
	{
		StringBuffer result = new StringBuffer();
		List<Edge> l = getNLEdges(reader);
		String label = reader.getNLExpression(getLabel());	//check if the nl-expr. has been defined in the ontology
		if (label == null)		//if not, derive it from the class name
			label = normalise(getLabel());
			
		if (l.size() > 0)
		{
			SGEdge edge = (SGEdge) l.get(0);
			Node target = edge.getTarget();
			
			StringBuffer sb = new StringBuffer();
			if (query)
				sb.append(label + " ");
			for (int i = 0; i < l.size(); i++)
			{
				if (target instanceof QueryValueNode)
				{
					List<String> list = ((QueryValueNode) l.get(i).getTarget()).getChoiceLabels(reader);
					for (int j = 0; j < list.size(); j++)
					{
						sb.append(list.get(j));
						if ((j+1) < list.size())
							sb.append(" or ");
					}
				}				
				else
					sb.append(((SGEdge)l.get(i)).getTarget().getLabel());
			}
			return sb.toString();
		}												
		else
			return label;	//normalise(getLabel());
	}
	
	/**	If this node has a name or title property, that value is used for its label;
	 *	otherwise its normal label (i.e. the ontology classname), normalised
	 *
	 *	@param reader Ontology
	 */
	public void setNLLabel(OntologyReader reader)
	{
		nlLabel = getNL(reader);
	}
	
	/**	Sets the natural language representation of this node to the given String
	 *	@param label NL-representation
	 */
	public void setNLLabel(String label)
	{
		if (!finalLabel)
			nlLabel = normalise(label);
	}
	
	/**	Sets the Natural language representation of this node to the given label
	 *	@param query True if this is a QueryGraph
	 *	@param	reader Ontology
	 */
	public void setNLLabel(boolean query, OntologyReader reader)
	{
		if (query)
		{
			nlLabel = reader.getNLExpression(getLabel());	//check if the nl-expr. has been defined in the ontology
			if (nlLabel == null)		//if not, derive it from the class name
				nlLabel = normalise(getLabel());
		}
		else
			nlLabel = getNL(reader);
	}
	
	/**	Sets the final natural language representation (cannot be changed except
	 *	by undo) of this node to the given label
	 *	@param label NL-label
	 */
	public void setFinalNLLabel(String label)
	{
		finalLabel = true;
		nlLabel = label;
	}
	
	/**	Checks whether the natural language label has been fixed
	 *	@return true if the NL-label should not be changed
	 */
	public boolean isFinalLabel()
	{
		return finalLabel;
	}
	
	/**	Returns the nl-label, if it has been fixed. Else returns null
	 *	@return String
	 */
	public String getFinalLabel()
	{
		if (finalLabel)
			return nlLabel;
		return null;
	}
	
	/**	Retrieves the natural language representation of this node
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getNLLabel(OntologyReader reader)
	{
		if (!finalLabel)
			setNLLabel(reader);
		return nlLabel;
	}
	
	/**	Retrieves the natural language representation of this node
	 * @param query boolean. If true, return a slightly different representation
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getNLLabel(boolean query, OntologyReader reader)
	{
		if (!finalLabel)
			setNLLabel(query, reader);
		return nlLabel;
	}
	
	/**	Returns the proper pronoun for this node (depends on the gender)
	 *	@return String
	 */
	public String getPronoun()
	{
		ArrayList l = getOutgoingEdges("Gender");
		if (l.size() == 0)
			return "it";	//gender unknown, so assume neutral
		
		SGEdge e = (SGEdge) l.get(0);
		String g = e.getTarget().getLabel();
		if (g.indexOf(LinguisticTerms.FEMALE) > -1)
			return "she";
		else if (g.indexOf(LinguisticTerms.MALE) > -1)
			return "he";
		else
			return "it";
	}
	
	/*	Returns the old label
	 *	@return String
	 */
	public String getOldLabel()
	{
		return oldLabel;
	}
	
	/*	Sets the old label
	 *	@param l String
	 */
	public void setOldLabel(String l)
	{
		oldLabel = l;
	}
	
	/** Returns a phrase suitable for presenting this node in a list of options
	 * ('some person' wouldn't cut it in that context), e.g. 'The person f1'.
	 *
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getChoiceLabel(OntologyReader reader)
	{
		return getChoiceLabel(false, reader);
	}
	
	/** Returns a phrase suitable for presenting this node in a list of options
	 * ('some person' wouldn't cut it in that context), e.g. 'The person f1'.
	 *
	 *	@param query If true, return different representation
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getChoiceLabel(boolean query, OntologyReader reader)
	{
		if (!finalLabel)
			nlLabel = getNL(query, reader);
		
		StringBuffer sb = new StringBuffer(nlLabel);
		String determiner = getDeterminer(THE, query, reader);
		if (determiner != null)
			sb.insert(0, determiner + " ");	
		return sb.toString();
	}
	
	/*	Returns all edges that have been marked as 'proper name' edges in the ontology.
	 *	@param reader Ontology
	 *	@return List<Edge>
	 */
	public List<Edge> getNLEdges(OntologyReader reader)
	{
		List<Edge> result = new ArrayList<Edge>();
		for (String p : reader.getProperNameProperties())
		{
			if (!p.equalsIgnoreCase("gender"))	//gender may provide a pronoun, not good!
				result.addAll(getOutgoingEdges(p));
		}
		return result;
	}
	
	/**	Returns a phrase for presenting this node as the header of a paragraph,
	 * 	e.g. 'The publication "The Toda's"' or 'The publication F0)
	 *
	 *	@param reader Ontology
	 *	@return String
	 */
	public String getHeaderLabel(OntologyReader reader)
	{
		String label = reader.getNLExpression(getLabel());
		if (label == null)
			label = normalise(getLabel());
		
		StringBuffer result = new StringBuffer(label);
		result.insert(0, "The ");
		
		List<Edge> l = getNLEdges(reader);
		if (l.size() > 0)	//append name/title
		{
			if (isQuote())
				result.append(" \"");
			else
				result.append(" ");
			result.append(l.get(0).getTarget().getLabel());
			if (isQuote())
				result.append("\"");
		}	
		return result.toString();
	}
	
	/** Returns a generic natural language expression based on the given label.
	 *	@param label String label
	 *	@param reader Ontology
	 *	@return String
	 */
	public static String getGenericNL(OntologyReader reader, String label)
	{
		String result = reader.getNLExpression(label);
		if (result == null)
			result = normalise(label);
		return result;
	}
	
	/**	Normalises ontology labels by replacing underscores with spaces
	 *	and removing capitals
	 *
	 *	@param	label to be normalised
	 *	@return String
	 */
	 public static String normalise(String label)
	 {
	 	return normalise(label, false);
	 }
	 
	 /**	Normalises ontology labels by replacing underscores with spaces.
	 *	and removing capitals.
	 *
	 *	@param	label to be normalised
	 *	@param capital If true, set the first character to upper case.
	 *	@return String
	 */
	public static String normalise(String label, boolean capital)
	{
		if ((label == null)	|| label.equals("string") || (label.length() == 0))	//if label is null (because class is union class)
		{
			if (capital)
				return "Something";
			else
				return "something";
		}
		
		StringBuffer result = new StringBuffer(label);

		for (int i = 0; i < result.length(); i++)
		{
			char c = result.charAt(i);
			if (c == '_')
				result.setCharAt(i, ' ');
			else if (Character.isUpperCase(c))
			{
				if (capital && (i == 0))
					continue;
				result.setCharAt(i, Character.toLowerCase(c));
				result.insert(i, ' ');
			}
			else if (capital && (i == 0) && Character.isLowerCase(c))
				result.setCharAt(0, Character.toUpperCase(c));
		}	

		String r = result.toString().trim();
		return r.replaceAll("  ", " ");
	}
	
	/** Returns a determiner for this node. If the node is represented by a proper name,
	 *	return null.
	 *	@param type Type of determiner (see global variables)
	 *	@param reader Ontology
	 *	@return String determiner
	 */
	public String getDeterminer(int type, OntologyReader reader)
	{
		if (finalLabel)
			return null;	//datatype nodes don't need determiners
			
		List<Edge> l = getNLEdges(reader);
		if (l.size() > 0)
			return null;	//no determiner needed
		
		return getDet(type);
	}
	
	/** Returns a determiner for this node
	 *	@param type Type of determiner (see global variables)
	 *	@param query If true, do not check for proper names
	 *	@param reader Ontology
	 *	@return String determiner
	 */
	public String getDeterminer(int type, boolean query, OntologyReader reader)
	{
		if (!query)
			return getDeterminer(type, reader);
		if (finalLabel)
			return null;
			
		return getDet(type);
	}
	
	/** Returns a determiner of the given type
	 *	@param type Type of determiner (see global variables)
	 *	@return String determiner
	 */
	public static String getDet(int type)
	{
		switch(type)
		{
			case 0: return "some";
			case 1: return "this";
			case 2: return "the";
			case 3: return "a";
			case 4: return "another";
		}
		return null;
	}
	
	/**	Returns the unique (sesame) ID
	 *	@return String id
	 */
	public String getUniqueID()
	{
		List<Edge> l = getOutgoingEdges("ID");
		if (l.size() > 0)
			return l.get(0).getTarget().getLabel();
		return null;
	}
	
	/**	Sets the unique (sesame) ID
	 *	@param id String ID
	 */
	public void setUniqueID(String id)
	{
		setUniqueID(id, null);
	}
	
	/**	Stores the unique (sesame) ID in an outgoing node connected to this with an ID edge.
	 *	@param id String ID
	 *	@param sgt Semantic Graph Transformer
	 */
	public void setUniqueID(String id, SemanticGraphTransformer sgt)
	{
		try
		{
			List<Edge> l = getOutgoingEdges("ID");
			if (l.size() > 0)
				l.get(0).getTarget().setLabel(id);
			else
			{
				SGNode target = new SGStringNode(id);
				SGEdge edge = new SGEdge("ID", this, target);
				if (sgt != null)
				{
					edge.setID(sgt.getGraph().getFreeID());
					sgt.addEdge(edge);
				}
			}
		}
		catch (NameAlreadyBoundException e)
		{}	
	}
	
	/** Checks whether this is a terminal node
	 *	@return True is this is a terminal node
	 */
	public boolean isLeaf()
	{
		return hasOutgoingEdge();
	}
	
	/**	Checks if there is an edge between the two nodes
	 *	@param node The other node
	 *	@return True if they are connected
	 */
	public boolean isConnected(SGNode node)
	{
		for (Iterator it = getOutgoingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			if (edge.getTarget().equals(node))
				return true;
		}
		for (Iterator it = getIncomingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			if (edge.getSource().equals(node))
				return true;
		}
		return false;
	}
	
	/**	Sets whether this node is a quotation
	 *
	 *	@param q True to add quotes, false to remove them.
	 */
	public void setQuote(boolean q)
	{
		quote = q;
	}
	
	/**	Checks if this node should be presented between quotes; if one of the NL
	 *	edges is quoted then this node is  too
	 *
	 *	@return boolean
	 */
	public boolean isQuote()
	{
		if (quote)
			return true;

		if (getOutgoingEdges("Title").size() > 0)
			return true;
		return false;
	}
	
	/** Compares this node to another, so they can be ordered by sequence number
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) throws ClassCastException
	{
		SGNode n = (SGNode) o;
		int seq = n.getSequenceNr();
		if (sequenceNr > seq)
			return 1;
		if (sequenceNr == seq)
			return 0;
		return -1;
	}
	
	/** Sets whether the user may remove this node from the graph
	 *	@param r true if the node may be removed
	 */
	public void setRemovable(boolean r)
	{
		removable = r;
	}
	
	/** Returns whether the user may remove this node from the graph
	 *	@return true if the node may be removed
	 */
	public boolean isRemovable()
	{	//nodes that are extracted from the database can obviously not be removed...
		if (mustRealise > 0)
			return false;
		return removable;
	}
	
	/**	Returns the other node of an edge of the given property, its inverse property,
	 *	or one of its (inverse) sub-properties.
	 *
	 *	@param property String property name
	 *	@param reader Ontology
	 *	@return SGNode with the other (target) node of an (equivalent) edge of this property.
	 */
	public SGNode getPropertyTarget(String property, OntologyReader reader)
	{
		List<Edge> list = getOutgoingEdges(property);
		if (list.size() > 0)	//try finding edges of this property
			return (SGNode) list.get(0).getTarget();
		String inverse = reader.getInverse(property);
		if (inverse != null)
		{	//try finding edges of the inverse, if there is one
			list = getIncomingEdges(inverse);
			if (list.size() > 0)
				return (SGNode) list.get(0).getSource();
		}
		
		for (String sub : reader.getSubProperties(property))
		{	//try the sub-properties
			list = getOutgoingEdges(sub);
			if (list.size() > 0)
				return (SGNode) list.get(0).getTarget();
			inverse = reader.getInverse(sub);
			if (inverse != null)
			{	//and try their inverse properties
				list = getIncomingEdges(inverse);
				if (list.size() > 0)
					return (SGNode) list.get(0).getSource();
			}
		}
		//if none of these have edges, return null
		return null;
	}
}