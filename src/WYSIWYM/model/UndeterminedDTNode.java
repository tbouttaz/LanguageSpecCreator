package WYSIWYM.model;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.transformer.DependencyTreeTransformer;
import WYSIWYM.ontology.OntologyReader;
import javax.naming.NameAlreadyBoundException;

/***
 *	UndeterminedDTNode is a node in the Dependency Tree whose referring expression
 *	(i.e. its final surface form) has not been assigned yet. It holds all information
 *	needed at a later stage to determine that referring expression:
 *	proper name and pronoun of the object (if known), the type and the SG id.
 *
 * @author Feikje Hielkema
 * @version 1.4 2008/07/25
 */
public class UndeterminedDTNode extends DTNode
{
	private String properName, type;
	private boolean topic = false;
	private int conjunctNr = 0;
	private OntologyReader reader;
	
	/**	Constructs a node with the given linguistic information
	 *	@param syncat Syntactic category
	 *	@param dep Dependency Label
	 *	@param sg SGNode that this DTNode corresponds to
	 *	@param reader Ontology
	 */
	public UndeterminedDTNode(String syncat, String dep, SGNode sg, OntologyReader reader)
	{
		super(syncat, dep, null, null);
		this.reader = reader;
		storeSGInfo(sg);
		setLeaf(true);
		if (sg.isQuote())
			setMorph(new Morph(null, null, null, null, null, LinguisticTerms.QUOTE, null));
	}
	
	/**	Constructs a node with the given linguistic information
	 *	@param syncat Syntactic category
	 *	@param dep Dependency Label
	 *	@param m Morphology information
	 *	@param sg SGNode that this DTNode corresponds to
	 */
	public UndeterminedDTNode(String syncat, String dep, Morph m, SGNode sg)
	{
		super(syncat, dep, null, m);
		if (sg.isQuote() && (m != null))
			m.setQuote(true);
		storeSGInfo(sg);
		setLeaf(true);
	}
	
	/**	Returns an nl-expression consisting of the proper name (if any)
	 *	and the class name.
	 *	@return String
	 */
	public String getExpr()
	{
		StringBuffer sb = new StringBuffer();
		if (properName != null)
			sb.append(properName + " ");
		sb.append(type);
		return sb.toString();
	}
	
	/**	Returns the class name
	 *	@return String
	 */
	public String getType()
	{
		return type;
	}
	
	/**	Returns an appropriate referring expression.
	 *	This method makes some heavy assumptions. 
	 *	The topic of a paragraph is always presented by a pronoun, and
	 *	no other node is. Proper checking of salience was too complex
	 * 	to implement as this project focuses on other, aggregation issues.
	 *
	 *	@param previousTree HashMap with the UndeterminedDTNodes of the previous Dependency Tree
	 *	@param dt This DependencyTreeTransformer
	 */	
	public void setExpression(Map<String, List<UndeterminedDTNode>> previousTree, DependencyTreeTransformer dt)
	{
		if (topic)
		{
			setUseAsPronoun(true);	//assume salience for topic of paragraph 
			setAnchor(null);		//after all it will be mentioned in the header and every sentence
			return;
		}
		
		String noun = null;
		String det = null;
		String mod = null;
		
		if (!previousTree.containsKey(type))	//no node of this type was in previous tree; must be new
		{
			if (properName != null)	//use the proper name
				noun = properName;
			else
			{	//if there is none, use 'a' or 'another'
				noun = type;		//depending on the conjunct nr.
				det = "a";
				if (conjunctNr > 0)
					det = "another";
			}
		}
		else
		{
			List<UndeterminedDTNode> old = previousTree.get(type);
			for (int i = 0; i < old.size(); i++)
			{
				if (old.get(i).getSGID().equals(getSGID()))	//if we wanted to use pronouns we should check here.
				{	//if this node was mentioned in the previous tree
					if (properName != null)	//use the proper name
						noun = properName;
					else
					{	//if there is none, use 'the [conjunctNr]th type'
						noun = type;		//depending on the conjunct nr. of its previous usage!
						det = "the";
						conjunctNr = old.get(i).getConjunctNr();	//replace conjunctNr
						if (conjunctNr > 0)
							mod = getNLRank(conjunctNr);
					}
				}
				else	//no mention in previous tree of this node but others of this type have been
				{	//so use proper name or 'yet another'
					if (properName != null)	//use the proper name
						noun = properName;
					else
					{	//if there is none, use 'a' or 'another'
						noun = type;		//depending on the conjunct nr.
						det = "yet another";
					}
				}
			}
		}		
		
		makeNP(noun, det, mod, dt);	//make a noun phrase and add it to the tree
	}
	
	private String getNLRank(int i)
	{
		switch (i)
		{
			case 1:	return "first";
			case 2: return "second";
			case 3: return "third";
			case 4:	return "fourth";
			case 5: return "fifth";
			case 6: return "sixth";
			case 7: return "seventh";
			case 8: return "eight";
			case 9: return "ninth";
			case 10: return "tenth";
			default: return "umpteenth";
		}
	}
	
	/**	Returns a noun phrase containing a proper referring expression for this node
	 *	@param noun The noun
	 *	@param determiner The determiner
	 *	@param mod A modifier, or null if there is none
	 *	@param dt DependencyTreeTransformer to which the node is added.
	 */
	public void makeNP(String noun, String determiner, String mod, DependencyTreeTransformer dt)
	{
		try
		{
			setLeaf(false);
			if (determiner != null)
			{
				List<DTNode> determiners = getDepChildren(LinguisticTerms.DET);
				if (determiners.size() > 0)	//must have been determined in advance, but this is probably more important???
					determiners.get(0).setRoot(determiner);
				else
				{
					DTNode det = new DTNode(LinguisticTerms.DET, LinguisticTerms.DET, determiner, null);			
					det.setID(dt.getGraph().getFreeID());
					DTEdge detEdge = new DTEdge(LinguisticTerms.DET, dt.getGraph().getFreeID(), this, det);
					dt.addEdge(detEdge);
				}
			}
			if (mod != null)
			{	//must have been determined in specification, so keep to that!
				List<DTNode> modifiers = getDepChildren(LinguisticTerms.MODIFIER);
				if (modifiers.size() > 0)	//must have been determined in specification, so insert new one before the old, keeping both!
					modifiers.get(0).setRoot(modifiers.get(0).getRoot() + ", " + mod);	//e.g. 'the third, interesting project'
				else
				{
					DTNode modifier = new DTNode(LinguisticTerms.ADJECTIVE, LinguisticTerms.MODIFIER, mod, null);
					modifier.setID(dt.getGraph().getFreeID());
					DTEdge modEdge = new DTEdge(LinguisticTerms.DET, dt.getGraph().getFreeID(), this, modifier);
					dt.addEdge(modEdge);
				}
			}
				
			DTNode nounNode = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, noun, getMorph());
			nounNode.setID(dt.getGraph().getFreeID());
			nounNode.setAnchor(getAnchor());
			setMorph(null);
			setAnchor(null);		//give the anchor to noun, rather than the whole phrase!
			DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD, dt.getGraph().getFreeID(), this, nounNode);
			dt.addEdge(nounEdge);
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("UndeterminedDTNode 68: Didn't think namealreadyboundexception was possible here!!!");
		}
	}
	
	/**	Sets whether this node is the topic of the paragraph
	 *	@param t boolean
	 */
	public void setTopic(boolean t)
	{
		topic = t;
	}
	
	/**	Checks whether this node is the topic of the paragraph
	 *	@return true if this is the topic
	 */
	public boolean isTopic()
	{
		return topic;
	}
	
	private void storeSGInfo(SGNode sg)
	{
		setSGID(sg.getSGID());
		if (getSGID() == null)
			System.out.println("UNDETERMINED NODE " + sg.getLabel() + " DOES NOT HAVE SG ID!");
		if (sg instanceof DatatypeNode)
		{
			setRoot(sg.getNLLabel(reader));
			return;
		}
	
		type = SGNode.normalise(sg.getLabel());

		for (Iterator it = sg.getOutgoingEdges(); it.hasNext(); )
		{
			SGEdge edge = (SGEdge) it.next();
			String label = edge.getLabel();
			if (label.equalsIgnoreCase("Name") || label.equalsIgnoreCase("Title"))
				properName = edge.getTarget().getLabel();
			if (label.equalsIgnoreCase("Gender"))
			{
				String value = edge.getTarget().getLabel();
				if (value.indexOf(LinguisticTerms.FEMALE) > -1)
					setPronoun("she");
				else if (value.indexOf(LinguisticTerms.MALE) > -1)
					setPronoun("he");
				else
					setPronoun("it");
			}
		}
		if (pronoun == null)
			setPronoun("it");		//default is neutral
		setAnchor(sg.getAnchor());
	}
	
	/**	Checks whether the given character is a vowel
	 *	@param c character
	 *	@return true if c is a vowel
	 */
	public static boolean isVowel(char c)
	{
		if ((c == 'a') || (c == 'i') || (c == 'o') || (c == 'u') || (c == 'e'))
			return true;
		return false; 
	}
	
	/**	Checks whether this node represents a Person
	 *	@return true if the class name (or type) equals 'Person'
	 */
	public boolean isPerson()
	{
		if (type.equalsIgnoreCase("Person"))
			return true;
		return false;
	}
	
	/**	Computes the conjunct nr, which is relevant when choosing a referring expression.
	 *	-1 for the paragraph topic, else the order of the
	 *	conjunct it is part of, or if that is not the case 0 by default.
	 */
	public void setConjunctNr()
	{
		if (topic)
			conjunctNr = -1;
		else
		{
			DTNode node = this;
		 	while (true)
    		{	//find a parent that is a conjunct
    			Iterator it = node.getIncomingEdges();
    			if (!it.hasNext())	//if there is none, stop
    				break;
    		
    			DTEdge edge = (DTEdge) it.next();	
    			if (edge.getLabel().equals(LinguisticTerms.CONJUNCT))
    			{
    				conjunctNr = edge.getOrder();	//if there is, return its order n
    				break;
    			}
    			node = edge.getSource();	//recurse
    		}
    	}
	}
	
	/**	Sets the conjunct nr, which is relevant when choosing a referring expression.
	 *	@param i conjunct number
	 */
	public void setConjunctNr(int i)
	{
		conjunctNr = i;
	}
	
	/**	Retrieves the conjunct nr, which is relevant when choosing a referring expression.
	 *	@return conjunct number
	 */
	public int getConjunctNr()
	{
		return conjunctNr;
	}
}