package SpecificationCreation.nlg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.naming.NameAlreadyBoundException;

import simplenlg.realiser.AnchorString;
import WYSIWYM.libraries.Lexicon;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Edge;
import WYSIWYM.model.FeedbackText;
import WYSIWYM.model.Morph;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.TemplateAnchor;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.transformer.DependencyTreeTransformer;
import WYSIWYM.transformer.SpecificationTransformer;
import WYSIWYM.transformer.SurfaceRealiser;
import WYSIWYM.util.SurfaceRealisationException;

/**	SpecModifier handles the process of fine-tuning a new lexicon entry.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 04-04-2007
 */
public class SpecModifier
{
	private DependencyTreeTransformer dt;
	private String sourceName, targetName;
	private boolean objectProperty = false, inverse = false;
	private FeedbackText text;
	private Stack<OldOperation> operations = new Stack<OldOperation>();
	
	//Items in the pop-up menus of the linguistic specification refinement
	/** Add a determiner to the noun phrase (adjective) */
	public static final String ADD_DET = "Add a determiner ('a', 'the', 'his')";
	/** Add a front modifier to the noun phrase */
	public static final String ADD_MOD = "Add a modifier ('red', 'large')";
	/** Add a front modifier on the sentence */
	public static final String ADD_SMAIN_MOD = "Add a modifier at the beginning of the sentence ('yesterday')";
	/** Add a modifier on the main verb (adverb) */
	public static final String ADD_VERB_MOD = "Add an adverb ('always', 'continuously')";
	/** Add a prepositional modifier on the noun phrase */
	public static final String ADD_PPMOD = "Add a prepositional modifier ('for you', 'by a cat')";
	/** Remove this word */
	public static final String REMOVE = "Remove this word";
	/** Switch source and target */
	public static final String SWITCH = "Switch source and target";
	/** Add quotes */
	public static final String ADD_QUOTES = "Add quotes";
	/** Remove quotes */
	public static final String REMOVE_QUOTES = "Remove quotes";
	/** Set this word to plural  */
	public static final String PLURAL = "Set this word to plural ('car'->'cars')";
	/** Set this word to singular*/
	public static final String SINGULAR = "Set this word to singular ('cars->'car')";
	/** Set the verb to active  */
	public static final String ACTIVE = "Set this verb to active ('is seen'->'sees')";
	/** Set the verb to passive */
	public static final String PASSIVE = "Set this verb to passive ('sees'->'is seen')";
	/** Change the verb tense */
	public static final String SET_TENSE = "Change the verb tense ('see', 'saw', 'seen')";
	/** Change the root of this word */
	public static final String CHANGE_ROOT = "Change this word";
	/** Negate this sentence */
	public static final String NEGATE = "Negate this sentence (John does not like fish)";
	/** Stop negating this sentence */
	public static final String UNNEGATE = "Do not negate this sentence";
	
	/**	Untransformed tree */
	public static final int NORMAL = 0;
	/**	Aggregated tree */
	public static final int AGGREGATE = 1;
	/**	Tree transformed to relative clause */
	public static final int RELATIVE_CLAUSE = 2;
	/**	Tree transformed to query */
	public static final int QUERY = 3;

	/**	Constructor, takes a template DependencyTree and enables user to fine-tune it
	 *	@param dt DependencyTreeTransformer
	 *	@param domain class-name of domain
	 *	@param range class-name of range
	 *	@param objectProp True if this is a specification for an object property
	 */
	public SpecModifier(DependencyTreeTransformer dt, String domain, String range, boolean objectProp)
	{
		this.dt = dt;
		sourceName = domain;
		targetName = range;
		objectProperty = objectProp;
	}
	
	/**	Set whether this is a specification for an inverse property
	 *	@param in True if this is spec for inverse property
	 */
	public void setInverse(boolean in)
	{
		inverse = in;
	}
	
	/**	Helper class, stores a performed operation, including the replaced value and the node it happened to
	 */
	private class OldOperation
	{
		public String type, value;
		public DTNode node, parent;
		
		public OldOperation(String t, String v, DTNode n, DTNode p)
		{
			type = t;
			node = n;
			parent = p;
			value = v;
		}
	}
	
	/**	Undoes the last operation performed.
	 *	@return true if more operations can be undone, false if this was the last and undo button should be disabled
	 */
	public boolean undo()
	{
		if (operations.empty())		//no old operations in stack to undo
			return false;
		undo((OldOperation) operations.pop());
		if (operations.empty())		//undo button should now be disabled
			return false;
		return true;
	}
	
	/**	Undoes last performed operation
	 */
	private void undo(OldOperation op)
	{
		if (op.type.equals(SWITCH))
			switchSourceAndTarget();
		else if (op.type.equals(CHANGE_ROOT))
			op.node.setRoot(op.value);
		else if (op.type.equals(REMOVE))
		{
			try
			{
				dt.addEdge(new DTEdge(op.node.getDeplbl(), dt.getGraph().getFreeID(), op.parent, op.node));
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("SpecModifier 139: NAMEALREADYBOUNDEXCEPTION SHOULD NOT BE POSSIBLE HERE.");
				e.printStackTrace();
			}
		}
		else if (op.type.equals(ADD_MOD) || op.type.equals(ADD_VERB_MOD) || op.type.equals(ADD_PPMOD) || op.type.equals(ADD_DET))
			dt.removeBranch(op.node);
		else
		{
			Morph m = op.node.getMorph();
			if (op.type.equals(SET_TENSE))
				m.setTense(op.value);
			else if (op.type.equals(SINGULAR))
				m.setSingular(false);
			else if (op.type.equals(PLURAL))
				m.setSingular(true);
			else if (op.type.equals(PASSIVE))
				m.setPassive(false);
			else if (op.type.equals(ACTIVE))
				m.setPassive(true);
			else if (op.type.equals(REMOVE_QUOTES))
				m.setQuote(true);
			else if (op.type.equals(ADD_QUOTES))
				m.setQuote(false);
			else if (op.type.equals(NEGATE))
				m.setNegated(false);
			else if (op.type.equals(UNNEGATE))
				m.setNegated(true);
		}	
	}

	/**	Makes a noun phrase representing 'a [label]' (e.g. a person), of if this
	 *	is a date property, a prepositional phrase representing 'on a date'.
	 *
	 *	@param label Noun (class name)
	 *	@param detType Type of determiner, see SGNode for details
	 *	@param objectProp True if this is a specification for an object property
	 *	@return DTNode with noun phrase
	 *	@see SGNode#getDeterminer(int,OntologyReader)
	 */
	public static DTNode makeNP(String label, int detType, boolean objectProp)
	{
	
		DTNode np = new DTNode(LinguisticTerms.NP, null, null, null);
		np.setPerson(label.equalsIgnoreCase("person"));
		try
		{
			DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, SGNode.normalise(label), null);
			DTEdge nounEdge = new DTEdge(LinguisticTerms.HEAD, np, noun);
			if (objectProp)
			{
				DTNode det = new DTNode(LinguisticTerms.DET, LinguisticTerms.DET, SGNode.getDet(detType), null);
				DTEdge detEdge = new DTEdge(LinguisticTerms.DET, np, det);
				if (label.equals("Date"))	//if this is a date property, return a pp instead of an np
				{
					np.setDeplbl(LinguisticTerms.OBJECT);
					DTNode pp = new DTNode(LinguisticTerms.PP, null, null, null);
    				DTNode prep = new DTNode(LinguisticTerms.PREP, LinguisticTerms.HEAD, "on", null);
    				DTEdge prepEdge = new DTEdge(LinguisticTerms.HEAD, pp, prep);
    				DTEdge npEdge = new DTEdge(LinguisticTerms.OBJECT, pp, np);
    				return pp;
				}
			}
		}
		catch(NameAlreadyBoundException e)
		{}
		return np;
	}

	/**	Returns a surface form of the given deptree of the given type.
	 *	@param type Type of surface form (normal, aggregated, relative clause, query)
	 *	@param anchors True to include anchors, false to leave them out
	 *	@return List<AnchorString> with surface form
	 *	@throws SurfaceRealisationException
	 *	@see #NORMAL
	 *	@see #AGGREGATE
	 *	@see #RELATIVE_CLAUSE
	 *	@see #QUERY
	 */
	public List<AnchorString> getSurfaceForm(int type, boolean anchors) throws SurfaceRealisationException
	{
		String domain = Lexicon.SOURCE;
		String range = Lexicon.TARGET;
		if (inverse)
		{
			domain = Lexicon.TARGET;
			range = Lexicon.SOURCE;
		}
		
		SurfaceRealiser sr = new SurfaceRealiser();
		if (type <= AGGREGATE)
		{
			DependencyTreeTransformer copy = new DependencyTreeTransformer(dt);
			DTNode source = makeNP(sourceName, SGNode.SOME, true);
			copy.insert(source, domain);
			if (type == NORMAL)
				copy.insert(makeNP(targetName, SGNode.SOME, objectProperty), range);
			else
			{
				copy.append(makeNP(targetName, SGNode.SOME, objectProperty), range, 0);
				copy.append(makeNP(targetName, SGNode.ANOTHER, objectProperty), range, 1);
			}
			if (anchors)
			{
				copy.getGraph().toFile("specification");
				addAnchors(copy);
			}
			else
				copy.getGraph().toFile("aggregated");
			return sr.realise((DependencyTree) copy.getGraph());
		}
			//else if it should be a relative clause or query
		SpecificationTransformer trans = new SpecificationTransformer(dt);
		trans.insert(makeNP(sourceName, SGNode.SOME, true), domain);
		trans.insert(makeNP(targetName, SGNode.SOME, objectProperty), range);
		if (type == QUERY)
		{
			if (inverse)
				trans.toQuery(trans.getGraph().getInsertedTarget(), true, true);
			else
				trans.toQuery(true, true);
		}
		else if (inverse)
			trans.toRelativeClause(trans.getGraph().getInsertedTarget());
		else			
			trans.toRelativeClause(trans.getGraph().getInsertedSource());
		
		DTNode np = makeNP(sourceName, SGNode.SOME, true);
		np.setDeplbl(LinguisticTerms.SMAIN);
		if (type == QUERY)
			np.setMorph(new Morph(LinguisticTerms.PLURAL));
		DependencyTreeTransformer tmp = new DependencyTreeTransformer(np);
		DTNode root = (DTNode) trans.getGraph().getRoot();
    	root.setDeplbl(LinguisticTerms.PPMODIFIER);	//change the dependency label
    	tmp.addBranchWithNewIDs(root);		//add the branch to this tree
    	try
    	{
	    	tmp.addEdge(new DTEdge(LinguisticTerms.PPMODIFIER, tmp.getGraph().getFreeID(), np, root));
     	}
    	catch (NameAlreadyBoundException e)
    	{}
    	tmp.getGraph().toFile("transformed" + type);
		return sr.realise((DependencyTree) tmp.getGraph());
	}
		
	/**	Returns the specification surface form to be manipulated, plus the other
	 *	surface forms that may be generated from it (e.g. aggregated, relative clause).
	 *	@return List<FeedbackText
	 *	@throws SurfaceRealisationException
	 */
	public List<FeedbackText> getSurfaceForms() throws SurfaceRealisationException
	{	//the default surface form
		List<FeedbackText> result = new ArrayList<FeedbackText>();
		text = new FeedbackText(getSurfaceForm(0, true));
		result.add(text);
		for (int i = 1; i < 4; i++)
			result.add(new FeedbackText(getSurfaceForm(i, false)));
		return result;
	}

	/**	Adds anchors to the given tree, which provide options to refine the specification
	 *	@param copy DependencyTreeTransformer
	 */
	public void addAnchors(DependencyTreeTransformer copy)
	{	//add anchors to the tree 
		for (Iterator it = copy.getGraph().getNodes(); it.hasNext(); )
		{
			DTNode node = (DTNode) it.next();
			if (!node.isLeaf())			//only put anchors in leaves
				continue;

			String dep = node.getDeplbl();
			String cat = node.getLabel();
			DTNode parent = (DTNode) node.getParents().next();
			TemplateAnchor a = new TemplateAnchor(node);	
			Morph m = node.getMorph();
			
			if (node.getID().equals(Lexicon.SOURCE) || node.getID().equals(Lexicon.TARGET))
				a.addEntry(SWITCH);
			else if (cat.equals(LinguisticTerms.NOUN) && (parent.getID().equals(Lexicon.SOURCE) || parent.getID().equals(Lexicon.TARGET)))
			{
				a.setTemplateID(parent.getID());
				a.addEntry(SWITCH);
				m = parent.getMorph();
			}
			else if (dep.equals(LinguisticTerms.DET) || dep.equals(LinguisticTerms.MODIFIER))
			{	//only change and remove words that are not source and target
				if ((parent.getID().equals(Lexicon.SOURCE) || parent.getID().equals(Lexicon.TARGET)) && node.getRoot().equalsIgnoreCase("some"))
					a.setTemplateID(parent.getID());	//can't change or remove determiner and head of NP, as they are computed on the fly?
				else
				{
					a.addEntry(CHANGE_ROOT);
					a.addEntry(REMOVE);
				}
			}
			
			if (m == null)
				m = new Morph();
			
			if (cat.equals(LinguisticTerms.PREP))
				a.addEntry(CHANGE_ROOT);	
			else if ((cat.equals(LinguisticTerms.NOUN) || cat.equals(LinguisticTerms.NP)) &&
					 !(dep.equals(LinguisticTerms.MODIFIER) || dep.equals(LinguisticTerms.DET)))
			{
				if (!a.hasEntry(SWITCH))	//only change the root if the node does not represent source or target
					a.addEntry(CHANGE_ROOT);
				
				if (parent.getLabel().equals(LinguisticTerms.NP))
				{	//if the node is part of an NP, check if that NP has determiner and modifiers
					if (!parent.hasEdge(LinguisticTerms.DET))
						a.addEntry(ADD_DET);
					if (!parent.hasEdge(LinguisticTerms.MODIFIER))
						a.addEntry(ADD_MOD);
					if (!parent.hasEdge(LinguisticTerms.PPMODIFIER))
						a.addEntry(ADD_PPMOD);
				}
				else 
				{	//else check if the node has them
					if (!node.hasEdge(LinguisticTerms.DET))
						a.addEntry(ADD_DET);
					if (!node.hasEdge(LinguisticTerms.MODIFIER))
						a.addEntry(ADD_MOD);
					if (!node.hasEdge(LinguisticTerms.PPMODIFIER))
						a.addEntry(ADD_PPMOD);
				}
				
				if (!a.hasEntry(SWITCH))
				{	
					if (m.isSingular())
						a.addEntry(PLURAL);
					else
						a.addEntry(SINGULAR);
				}
				if (m.isQuote())
					a.addEntry(REMOVE_QUOTES);
				else
					a.addEntry(ADD_QUOTES);
			}
			else if (cat.equals(LinguisticTerms.VERB))
			{
				a.addEntry(CHANGE_ROOT);
				a.addEntry(ADD_VERB_MOD);
				a.addEntry(ADD_SMAIN_MOD);
				a.addEntry(SET_TENSE);
					
				if (!a.hasEntry(SWITCH))
				{	
					if (m.isSingular())
						a.addEntry(PLURAL);
					else
						a.addEntry(SINGULAR);
				}
				if (!m.isPassive() && copy.potentialPassive())	//not every tree can be set to passive and still make sense...
					a.addEntry(PASSIVE);
			
				if (m.isNegated())
					a.addEntry(UNNEGATE);
				else
					a.addEntry(NEGATE);
			}
			if (a.hasEntries())
				node.setAnchor(a);	
		}				
	} 
	
	/**	Adds a prepositional modifier to the given noun phrase
	 */
	private DTNode addPPMod(DTNode source)
	{
		try
		{
			DTNode pp = new DTNode(LinguisticTerms.PP, LinguisticTerms.PPMODIFIER, null, null);
			dt.addEdge(new DTEdge(LinguisticTerms.PPMODIFIER, dt.getGraph().getFreeID(), source, pp));
		
			DTNode prep = new DTNode(LinguisticTerms.PREP, LinguisticTerms.HEAD, "of", null);
			dt.addEdge(new DTEdge(LinguisticTerms.HEAD, dt.getGraph().getFreeID(), pp, prep));
				
			DTNode np = new DTNode(LinguisticTerms.NP, LinguisticTerms.OBJECT, null, null);
			dt.addEdge(new DTEdge(LinguisticTerms.OBJECT, dt.getGraph().getFreeID(), pp, np));
			
			DTNode noun = new DTNode(LinguisticTerms.NOUN, LinguisticTerms.HEAD, "something", null);
			dt.addEdge(new DTEdge(LinguisticTerms.HEAD, dt.getGraph().getFreeID(), np, noun));
			return pp;
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("SpecModifier 425: NAMEALREADYBOUNDEXCEPTION SHOULD NOT BE POSSIBLE HERE.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Switches the source and target of a property
	 */
	private void switchSourceAndTarget()
	{
		DTNode source = (DTNode) dt.getGraph().getNode(Lexicon.SOURCE);
		DTNode target = (DTNode) dt.getGraph().getNode(Lexicon.TARGET);
		DTEdge sourceEdge = (DTEdge) source.getIncomingEdges().next();
		DTEdge targetEdge = (DTEdge) target.getIncomingEdges().next();
		try
		{
			sourceEdge.setTarget(target);
			source.removeIncomingEdge(sourceEdge);
			source.setDeplbl(targetEdge.getLabel());
			targetEdge.setTarget(source);
			target.removeIncomingEdge(targetEdge);
			target.setDeplbl(sourceEdge.getLabel());
			
			if (source.getMorph() == null)
				source.setMorph(new Morph());
			if (target.getMorph() == null)
				target.setMorph(new Morph());
				
			if (source.getMorph().isGenitive())
			{
				source.getMorph().setGenitive(false);
				target.getMorph().setGenitive(true);
			}
			else if (target.getMorph().isGenitive())
			{
				target.getMorph().setGenitive(false);
				source.getMorph().setGenitive(true);
			}
		}
		catch(NameAlreadyBoundException e)
		{
			System.out.println("SpecModifier 425: error in switching source and target");
			e.printStackTrace();
		}
	}
	
	/**	Updates the tree with the option the user has selected
	 *	@param action Option selected by user
	 *	@param id Unique ID of anchor
	 *	@param value Value supplied by user, e.g. root of newly added word. Null for actions that do not require a value
	 */
	public void update(String action, String id, String value)
	{	//update the tree
		try
		{
			TemplateAnchor a = (TemplateAnchor) text.getAnchor(id);
			DTNode node = (DTNode) dt.getGraph().getNode(a.getTemplateID());
			DTNode parent = (DTNode) node.getParents().next();

			if (action.equals(SWITCH))
			{	//switch the source and target node
				switchSourceAndTarget();
				operations.push(new OldOperation(action, null, null, null));
			}
			else if (action.equals(CHANGE_ROOT))
			{
				operations.push(new OldOperation(action, node.getRoot(), node, null));
				if (node.getID().equals(Lexicon.SOURCE) || node.getID().equals(Lexicon.TARGET))
				{	//check if there's a determiner already 
					List<Edge> list = node.getOutgoingEdges(LinguisticTerms.DET);
					if (list.size() > 0)	//if so, change the root
						((DTNode) list.get(0).getTarget()).setRoot(value);
					else	//if not, add one
					{
						try
						{
							DTNode det = new DTNode(LinguisticTerms.DET, LinguisticTerms.DET, value, null);
							dt.addEdge(new DTEdge(LinguisticTerms.DET, node, det));
						}
						catch(NameAlreadyBoundException e)
						{
							e.printStackTrace();
						}
					}
				}
				else
					node.setRoot(value);
			}
			else if (action.equals(REMOVE))
			{
				dt.removeNode(node);
				operations.push(new OldOperation(action, null, node, parent));
			}
			else if (action.equals(ADD_VERB_MOD))
			{
				DTNode modifier = new DTNode(LinguisticTerms.ADVERB, LinguisticTerms.MODIFIER, value, null);
				dt.addEdge(new DTEdge(LinguisticTerms.MODIFIER, dt.getGraph().getFreeID(), node, modifier));
				operations.push(new OldOperation(action, null, modifier, node));
			}
			else if (action.equals(ADD_PPMOD))
			{	//adding a pp to an NP
				if (parent.getLabel().equals(LinguisticTerms.NP))
					operations.push(new OldOperation(action, null, addPPMod(parent), parent));
				else
					operations.push(new OldOperation(action, null, addPPMod(node), node));
			}
			else if (action.equals(ADD_MOD) || action.equals(ADD_DET) || action.equals(ADD_SMAIN_MOD))
			{	//adding modifier to parent of this node, not node itself
				if (parent.getLabel().equals(LinguisticTerms.NP) || action.equals(ADD_SMAIN_MOD))
					node = parent;

				String dep = LinguisticTerms.MODIFIER;
				String cat = LinguisticTerms.ADJECTIVE;
				if (action.equals(ADD_DET))
				{	
					dep = LinguisticTerms.DET;
					cat = LinguisticTerms.DET;
				}
				else if (action.equals(ADD_SMAIN_MOD))
					cat = LinguisticTerms.ADVERB;
								
				DTNode mod = new DTNode(cat, dep, value, null);
				dt.addEdge(new DTEdge(dep, dt.getGraph().getFreeID(), node, mod));
				node.setLeaf(false);
				operations.push(new OldOperation(action, null, mod, node));				
			}
			else	//some morphology operation
			{
				Morph m = node.getMorph();
				if (m == null)
					m = new Morph();
				
				if (action.equals(PLURAL))
				{
					m.setSingular(false);
					if (node.getDeplbl().equals(LinguisticTerms.HEAD) && parent.getLabel().equals(LinguisticTerms.NP))
					{	//if this node is the noun of an NP, set the entire NP to plural
						Morph parentMorph = parent.getMorph();
						if (parentMorph == null)
							parentMorph = new Morph();
						parentMorph.setSingular(false);
						parent.setMorph(parentMorph);
					}
				}
				else if (action.equals(SINGULAR))
				{
					m.setSingular(true);
					if (node.getDeplbl().equals(LinguisticTerms.HEAD) && parent.getLabel().equals(LinguisticTerms.NP))
					{	//if this node is the noun of an NP, set the entire NP to plural
						Morph parentMorph = parent.getMorph();
						if (parentMorph == null)
							parentMorph = new Morph();
						parentMorph.setSingular(true);
						parent.setMorph(parentMorph);
					}
				}
				else if (action.equals(ACTIVE))
					m.setPassive(false);
				else if (action.equals(PASSIVE))
					m.setPassive(true);
				else if (action.equals(ADD_QUOTES))
					m.setQuote(true);
				else if (action.equals(REMOVE_QUOTES))
					m.setQuote(false);
				else if (action.equals(NEGATE))
				{
					System.out.println("negating");
					m.setNegated(true);
					System.out.println(m.isNegated());
				}
				else if (action.equals(UNNEGATE))
					m.setNegated(false);
				else if (action.equals(SET_TENSE))
				{
					operations.push(new OldOperation(action, m.getTense(), node, null));
					m.setTense(value);
				}
				node.setMorph(m);
				System.out.println(node.getMorph().isNegated());
				dt.getGraph().toFile("TEMP");
				if (!action.equals(SET_TENSE))
					operations.push(new OldOperation(action, null, node, null));
			}	
		}
		catch (NameAlreadyBoundException ex)
		{
			System.out.println("SpecModifier 681: NAMEALREADYBOUNDEXCEPTION HERE SHOULDN'T BE POSSIBLE.");
			ex.printStackTrace();
		}
	}
	
	/**	Returns the specification
	 *	@return DependencyTreeTransformer
	 */
	public DependencyTreeTransformer getDT()
	{
		return dt;
	}
}