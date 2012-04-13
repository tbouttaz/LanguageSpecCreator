package WYSIWYM.transformer;

import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Morph;

/***
 *	SpecificationTransformer takes a Dependency Tree and performs some transformation:,
 *	it can transform a tree to a relative clause, a query statement, or, knowing that a
 *	particular node has been pluralised, can pluralise the rest of the tree.
 *
 *  @author Feikje Hielkema
 *	@version 1.2 30-01-2008
 */
public class SpecificationTransformer extends DependencyTreeTransformer
{
	/**	Constructs the transformer with a DependencyTree
	 *	@param tree DependencyTree
	 */
	public SpecificationTransformer(DependencyTree tree)
	{
		super(tree);
	}
	
	/**	Constructs the transformer with a DependencyTree
	 *	@param dt DependencyTreeTransoformer
	 */
	public SpecificationTransformer(DependencyTreeTransformer dt)
	{
		super(dt);
	}
	
	/**	Transforms the dependency tree to a query
	 *	@param maintopic If this is a statement about the root of the QueryGraph (which is plural), maintopic should be true
	 *	@return True
	 */
	public boolean toQuery(boolean maintopic)
	{	
		return toQuery(maintopic, false);
	}
	
	/**	Transforms the dependency tree to a query
	 *	@param maintopic If this is a statement about the root of the QueryGraph (which is plural), maintopic should be true
	 *	@param subsentence True for a root node with syncat SSUB, false for syncat SMAIN. This affects orthography.
	 *	@return True
	 */
	public boolean toQuery(boolean maintopic, boolean subsentence)
	{
		DTNode topic = getGraph().getInsertedSource();
		return toQuery(topic, maintopic, subsentence);
	}
	
	/**	Transforms the dependency tree to a query. 
	 *	@param topic Topic is the DTNode in the tree that represents the source node.
	 *	@param maintopic If this is a statement about the root of the QueryGraph (which is plural), maintopic should be true
	 *	@param subsentence True for a root node with syncat SSUB, false for syncat SMAIN. This affects orthography.
	 *	@return True
	 */	
	public boolean toQuery(DTNode topic, boolean maintopic, boolean subsentence)
	{
		if (topic == null)	//must be a specification without the source node, e.g. 'place' in address; so don't bother about changing it to a relative clause.
		{
			System.out.println("SPECTRANS 50: NOT TRANSFORMING TREE TO QUERY AS THERE IS NO TOPIC NODE!");
			return true;
		}
		
		if (!toRelativeClause(topic))	//in a query, all statements are relative clauses
			return false;
		if (!subsentence) //the query statements may be clauses grammatically, but orthographically they're sentences.
			getGraph().getRoot().setLabel(LinguisticTerms.SMAIN);
		if (maintopic)	//if they are properties of the root, they should be plural
			return toPlural(topic);
		return true;
	}
	
	/**	Setting one node to plural affects the rest of the sentence; for instance, if the subject is pluralised, the
	 *	verb should be plural too. This method takes a pluralised DTNode and pluralises the tree as needed. 
	 *	@param topic The pluralised DTNode
	 *	@return True
	 */
	public boolean toPlural(DTNode topic)
	{	
		String dep = topic.getDeplbl();
		DTNode root = (DTNode) getGraph().getRoot();
		DTNode pp = topic.getDepParent(LinguisticTerms.PPMODIFIER);	//check if topic is part of a PP
		DTNode verb = root.getDepChild(LinguisticTerms.HEAD);		//get the main verb
		if (verb.getMorph() == null)								//and its morphology
			verb.setMorph(new Morph());
		DTNode subject = root.getDepChild(LinguisticTerms.SUBJECT);	//get the subject
		DTNode object = root.getDepChild(LinguisticTerms.OBJECT);	//and the object		
		
		//if topic is (part of) the subject, set the verb to plural (as the topic is, e.g., 'all questionnaires')
		if ((!verb.getMorph().isPassive()) && dep.equals(LinguisticTerms.SUBJECT) || 
					(topic.childOf(LinguisticTerms.SUBJECT) && dep.equals(LinguisticTerms.HEAD))) //(!dep.equals(LinguisticTerms.DET))))	
		{	//but only if it's a property of the main topic (the only one actually set to plural)
			if (pp == null)
				verb.getMorph().setSingular(false);
			else if (!verb.getRoot().equals("be"))	
			{	//but if the topic is also part of a pp (which is part of the subject), and the verb is not 'to be',
				verb.getMorph().setSingular(false);		//then if the verb is plural, the rest of the subject must be too
				DTNode noun = subject.getDepChild(LinguisticTerms.HEAD);
				if (noun != null) 
				{
					if (noun.getMorph() == null)
						noun.setMorph(new Morph());
					noun.getMorph().setSingular(false);
				}
			}						
		}
				
		//if the topic is (part of) the direct object of a passive verb, set the verb to plural
		if ((object != null) && (topic.equals(object) || topic.childOf(object)) && verb.getMorph().isPassive() && (pp == null))
			verb.getMorph().setSingular(false);
		return true;
	}

	/**	Transforms the tree to a relative clause whose topic is the given node.
	 *
	 *	@param topic Topic DTNode
	 *	@return True
	 */
	public boolean toRelativeClause(DTNode topic)
	{
		if (topic.isPerson())
			topic.setPronoun("who");
		else
			topic.setPronoun("which");
		topic.setUseAsPronoun(true);
		
		DTNode root = (DTNode) getGraph().getRoot();
		root.setLabel(LinguisticTerms.SSUB);	//tell the sentence it is now a sub-sentence, so the orthography differs
		String dep = topic.getDeplbl();
		DTNode pp = topic.getDepParent(LinguisticTerms.PPMODIFIER);	//check if topic is part of a PP
		DTNode comp = topic.getDepParent(LinguisticTerms.COMPLEMENT);	//check if it's part of a complement
		DTNode verb = root.getDepChild(LinguisticTerms.HEAD);		//get the main verb
		if (verb.getMorph() == null)								//and its morphology
			verb.setMorph(new Morph());
		DTNode subject = root.getDepChild(LinguisticTerms.SUBJECT);	//get the subject
		DTNode object = root.getDepChild(LinguisticTerms.OBJECT);	//and the object
		
		if (dep.equals(LinguisticTerms.COMPLEMENT) || (comp != null))
		{
			if (comp == null)
				comp = topic;
			if (verb.getRoot().equals("be"))
			{	//switch subject and complement, e.g. 'some project's PI is [topic]' -> 'who is some project's PI'
				((DTEdge)comp.getIncomingEdges().next()).setLabel(LinguisticTerms.SUBJECT);
				comp.setDeplbl(LinguisticTerms.SUBJECT);
				subject.setDeplbl(LinguisticTerms.COMPLEMENT);
				((DTEdge)subject.getIncomingEdges().next()).setLabel(LinguisticTerms.COMPLEMENT);
			}
			else	//else make the entire branch that topic belongs to a modifier, e.g. 'some person knows [topic]' -> 'who some person knows' 
				((DTEdge)comp.getIncomingEdges().next()).setLabel(LinguisticTerms.MODIFIER);
		}
		else if (pp != null)
		{	//if topic is part of a PPModifier (i.e. endmodifier), and not part of the subject (e.g. do not 'to which access is ...')
			DTEdge edge = (DTEdge) pp.getIncomingEdges().next();
			DTNode source = edge.getSource();
		
			if (source.equals(root))
			{	//if the ppmod is a direct child of the sentence, 
				pp.setDeplbl(LinguisticTerms.MODIFIER);	//make it a (front) modifier
				edge.setLabel(LinguisticTerms.MODIFIER);
			}
		}
	
		if ((subject != null) && (object != null))
		{	//if the sentence has a subject and object, and the topic is part of the object
			if (object.equals(topic) || topic.childOf(object))	//the verb should be passive
				verb.getMorph().setPassive(true);
		}
			
		if (dep.equals(LinguisticTerms.MODIFIER) || dep.equals(LinguisticTerms.HEAD))
		{	//if topic is the head or modifier of an NP
			DTNode parent = (DTNode) topic.getParents().next();
			if (parent.getLabel().equals(LinguisticTerms.NP))
			{	//then replace that NP by topic
				parent.setAnchor(topic.getAnchor());
				parent.setPronoun(topic.getPronoun());
				parent.setUseAsPronoun(true);
			}
		}
		
		if ((!verb.getRoot().equals("be")) && (dep.equals(LinguisticTerms.COMPLEMENT) || topic.childOf(LinguisticTerms.COMPLEMENT)))
		{	//if the topic is (part of) the complement and the verb is NOT 'to be', set the verb to passive
			verb.getMorph().setPassive(true);
			if (verb.getRoot().equals("have"))	//if the verb is 'to have', replace it with 'to include (otherwise you get 'is had'!)
				verb.setRoot("include");
		}
		return true;	
	}
}