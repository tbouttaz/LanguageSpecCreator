package WYSIWYM.transformer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import simplenlg.features.Tense;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.lexicalitems.Verb;
import simplenlg.realiser.AggregatePhraseSpec;
import simplenlg.realiser.AnchorString;
import simplenlg.realiser.CoordinateVerbGroupSpec;
import simplenlg.realiser.DocStructure;
import simplenlg.realiser.NPPhraseSpec;
import simplenlg.realiser.PPPhraseSpec;
import simplenlg.realiser.PhraseSpec;
import simplenlg.realiser.Realiser;
import simplenlg.realiser.SPhraseSpec;
import simplenlg.realiser.StringPhraseSpec;
import simplenlg.realiser.SyntaxPhraseSpec;
import simplenlg.realiser.TextSpec;
import simplenlg.realiser.VerbGroupSpec;
import WYSIWYM.libraries.LinguisticTerms;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DTEdge;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.Edge;
import WYSIWYM.model.QueryResultPlan;
import WYSIWYM.model.TextPlan;
import WYSIWYM.util.SurfaceRealisationException;

/***
 *	SurfaceRealiser handles linearisation and morphology tasks
 *	It uses the SimpleNLG package (Reiter, 2006) for surface realisation
 *
 * @author Feikje Hielkema
 * @version 1.00 2006/11/15
 *
 *	@version 1.4 2008/08/06
 */

public class SurfaceRealiser 
{
	protected Lexicon lexicon;
	protected Realiser realiser;
	private String tense = LinguisticTerms.PRESENT;		//if a verb should be past tense, ditto
	private boolean genitive = false, quote = false;
	private boolean passive = false;	//if a verb is passive, this boolean will store that information until it can be used
	private boolean singularVerb = true, compPlural = false, subjectPlural = false, modPlural = false;

	protected DTNode aggregateNode;
	protected String conjunctor = "";

	/**	HTML output */
	public static final int HTML = 0;
	/**	'Normal' (e.g. \n for linebreaks) output */
	public static final int NORMAL = 1;
	/**	Output without lay-out */
	public static final int NOLAYOUT = 2;

	/**	Default constructor.
	 */
    public SurfaceRealiser() 
    {
    	lexicon = new Lexicon();
    	realiser = new Realiser(lexicon);
    }
    
    /**	Realises the text plan to a feedback text.
     *
     *	@param plan	TextPlan
     *	@return	List<AnchorString> with feedback text
     *	@throws SurfaceRealisationException
     */
    public List<AnchorString> realise(TextPlan plan) throws SurfaceRealisationException
    {
    	TextSpec result = new TextSpec();   
    	for (Iterator it = plan.getParagraphHeaders(); it.hasNext(); )
    	{
    		String parHeader = (String) it.next();
    		if ((!(plan instanceof QueryResultPlan)) && (parHeader.indexOf("address") < 0) && (plan.getSetNr(parHeader) == 0))
    			continue;	//empty paragraph; don't realise
    			
    		TextSpec paragraph = new TextSpec().promote(DocStructure.PARAGRAPH);
    		TextSpec header = new TextSpec(plan.getParagraphHeader(parHeader)).promote(DocStructure.PARHEADER);
     		paragraph.addSpec(header);  
     		
     		List<String> setOrder = TextPlanner.getSetOrder();
     		for (int i = 0; i < setOrder.size(); i++)
     		{
     			String setHeader = setOrder.get(i);
     			TextSpec set = realiseSentenceSet(parHeader, setHeader, plan);
     			if (set != null)
	     			paragraph.addSpec(set);
	     		plan.removeSet(parHeader, setHeader);
     		}
     		
     		if (plan instanceof QueryResultPlan)
     		{
	     		QueryResultPlan qPlan = (QueryResultPlan) plan;
	     		for (Iterator it2 = qPlan.getSetHeaders(); it2.hasNext(); )
	     		{
	     			String setHeader = (String) it2.next();
	     			TextSpec set = realiseSentenceSet(parHeader, setHeader, plan);
     				if (set != null)
	     				paragraph.addSpec(set);
	     		}
	     		
	     	}
     		result.addSpec(paragraph);
    	}
    	result.setDocument();
    	realiser.setHTML(true);	//output html
    	return realiser.realise(result);
    }

	/**	Realises the ContentPlan to a feedback text.
     *
     *	@param plan	ContentPlan
     *	@return	List<AnchorString> with feedback text
     *	@throws SurfaceRealisationException
     */
	public List<AnchorString> realise(ContentPlan plan) throws SurfaceRealisationException
	{
		return (realise(plan, true));
	}
	
	/**	Realises the ContentPlan to a feedback text.
     *
     *	@param plan	ContentPlan
     *	@param html True for output with HTML tags, false for normal output
     *	@return	List<AnchorString> with feedback text
     *	@throws SurfaceRealisationException
     */
	public List<AnchorString> realise(ContentPlan plan, boolean html) throws SurfaceRealisationException
	{	
		TextSpec result = new TextSpec();
		for (Iterator it = plan.getParagraphHeaders(); it.hasNext(); )
    	{
    		String parHeader = (String) it.next();
    		TextSpec paragraph = new TextSpec().promote(DocStructure.PARAGRAPH);
    		TextSpec header = new TextSpec(plan.getParagraphHeader(parHeader)).promote(DocStructure.PARHEADER);
     		paragraph.addSpec(header); 		
     		aggregateNode = null;
     	
     		List<DependencyTreeTransformer> trees = plan.getParagraphTrees(parHeader);
    		for (int i = 0; i < trees.size(); i++)
     		{
				DependencyTree dt = trees.get(i).getGraph();
     			PhraseSpec spec = realiseNode((DTNode) dt.getRoot());
		//		tweakAggregate(spec, dt);
				aggregateNode = null;	//reset these, otherwise the next sentence might be set to plural!
				conjunctor = "";		
     			spec.setFlash(dt.flash());
	 		   	paragraph.addSpec(spec);
	    	}
   		
    		result.addSpec(paragraph);
    	}
    	
    	result.setDocument();
    	realiser.setHTML(html);	//output html
    	return realiser.realise(result);
	}

	private TextSpec realiseSentenceSet(String parHeader, String setHeader, TextPlan plan) throws SurfaceRealisationException
	{
		List<DependencyTree> set = plan.getSet(parHeader, setHeader);
     	if ((set == null) || (set.size() == 0))
     		return null;	//don't show empty sets!
     		
     	TextSpec sentenceset = new TextSpec().promote(DocStructure.SENTENCESET);
     	TextSpec setHeaderSpec = new TextSpec(setHeader).promote(DocStructure.SETHEADER);
     	sentenceset.addSpec(setHeaderSpec);
   		aggregateNode = null;
     	
     	for (int j = 0; j < set.size(); j++)
     	{
			DependencyTree dt = set.get(j);
     		PhraseSpec s = realiseNode((DTNode) dt.getRoot());
			//tweakAggregate(s, dt);	
			aggregateNode = null;	//reset these, otherwise the next sentence might be set to plural!
			conjunctor = "";		
     		s.setFlash(dt.flash());
	    	sentenceset.addSpec(s);
	    }
     	return sentenceset;
	}

/**	private void setComplementPlural(SPhraseSpec spec)
	{	//SHOULD ALL COMPLEMENTS BECOME PLURAL??
		List<PhraseSpec> compList = spec.getComplements();
		for (int i = 0; i < compList.size(); i++)
		{
			compList.get(i).setSingular(false);
			if (compList.get(i) instanceof NPPhraseSpec)
			{
				NPPhraseSpec np = (NPPhraseSpec) compList.get(i);
				List<PhraseSpec> modList = np.getHeadModifiers();
				for (int j = 0; j < modList.size(); j++)
					modList.get(j).setSingular(false);
			}
		}
	}

	public void tweakAggregate(PhraseSpec phrase, DependencyTree dt)
	{
		if (aggregateNode == null)
			return;
		if (phrase instanceof AggregatePhraseSpec)
		{
			List<PhraseSpec> constituents = ((AggregatePhraseSpec)phrase).getConstituents();
			for (int i = 0; i < constituents.size(); i++)
			{
				DependencyTreeTransformer conjunct = new DependencyTreeTransformer();
				DTNode root = ((DTNode) dt.getRoot()).getOrderedDepChild(LinguisticTerms.CONJUNCT, i);
				conjunct.addBranch(root, true);
				conjunct.getGraph().setRoot(root);
				aggregateNode = conjunct.getGraph().getConjunction();	//reset the aggregate node
				tweakAggregate(constituents.get(i), conjunct.getGraph());	//do tweak aggregate for all sentences in conjunction
			}
			return;
		}
		else if (!(phrase instanceof SPhraseSpec))
			return;
				
		if (conjunctor.equals("or") || conjunctor.equals("nor"))
		{
			conjunctor = "";
			return;		//if the conjunctor is 'or' or 'nor', the sentence does not need to be plural!
		}
		
		SPhraseSpec spec = (SPhraseSpec) phrase;
		DTNode source = dt.getInsertedSource();
		if (source.getLabel().equals(LinguisticTerms.CONJUNCTION))
			source = dt.getInsertedTarget();	//we want source to be the non-aggregated node
		DTNode verb = dt.getVerb();
		Morph verbMorph = verb.getMorph();
		boolean passive = false;
		if ((verbMorph != null) && verbMorph.isPassive())
			passive = true;
		boolean tobe = verb.getRoot().equalsIgnoreCase("be");

		DTNode parent = (DTNode) aggregateNode.getParents().next();
		String parentCat = parent.getLabel();
		
		if (aggregateNode.getDeplbl().equals(LinguisticTerms.SUBJECT) || //if target is the subject, or the head of the subject
				(parent.getDeplbl().equals(LinguisticTerms.SUBJECT) && aggregateNode.getDeplbl().equals(LinguisticTerms.HEAD)))	
    	{	//and the verb is not passive,
    		if (!passive)
	    		spec.getVerbGroup().setNumber(simplenlg.features.Number.PLURAL);	//set the verb to plural
	    	if (tobe && source.childOf(LinguisticTerms.COMPLEMENT))	//if the verb is 'to be' and source is (part of) the complement,
				setComplementPlural(spec);	//set the complement to plural ('this person and this person are the authors of this document')
		}
		else if (source.getDeplbl().equals(LinguisticTerms.SUBJECT))
		{	//if source is the subject, and the verb is NOT to be, set the complement to plural
			if (!tobe)		//('x used the y and z methods' but not 'x is the authors of y and z')
				setComplementPlural(spec);
		}
		else if (source.childOf(LinguisticTerms.SUBJECT))	//if source is part of the subject, and the verb is to be
		{	//e.g. 'source's observation units are x and y', 'the time dimensions for source are x and y'
			if (tobe)	//set the verb and the entire subject to plural
			{
				spec.getVerbGroup().setNumber(simplenlg.features.Number.PLURAL);
				List<PhraseSpec> subjects = spec.getSubjects();
				for (int i = 0; i < subjects.size(); i++)
					subjects.get(i).setSingular(false);
			}
		}
		else if (aggregateNode.childOf(LinguisticTerms.COMPLEMENT))	//else, if the target is part of the complement
			setComplementPlural(spec);			//set the rest of the complement to plural

		String greatParentCat = "";
		Iterator it = parent.getParents();
		if (it.hasNext())
			greatParentCat = ((DTNode)it.next()).getLabel();
		
		if ((aggregateNode.getDeplbl().equals(LinguisticTerms.OBJECT) && parentCat.equals(LinguisticTerms.SMAIN)) ||
			(parent.getDeplbl().equals(LinguisticTerms.OBJECT) && aggregateNode.getDeplbl().equals(LinguisticTerms.HEAD) && greatParentCat.equals(LinguisticTerms.SMAIN)))
		{
    			if (passive)
	    			spec.getVerbGroup().setNumber(simplenlg.features.Number.PLURAL);	//set the verb to plural
	    		if (((DTNode)source.getParents().next()).getLabel().equals(LinguisticTerms.PP))
		  	  		setComplementPlural(spec);	
		}
	}

    /**	Realises a single dependency tree, for the property creation package.
     *	@param dt DependencyTree
     *	@return List<AnchorString> surface form
     *	@throws SurfaceRealisationException
     */
    public List<AnchorString> realise(DependencyTree dt) throws SurfaceRealisationException
    {
    	PhraseSpec s = realiseNode((DTNode) dt.getRoot());
    //	tweakAggregate(s, dt);	
    	realiser.setHTML(true);
    	List<AnchorString> result = realiser.realise(s);
    	return realiser.applySentenceOrthography(result);
    }
    
    /**	Realises a single dependency tree to a String, for the property creation package.
     *	@param dt DependencyTree
     *	@return String surface form
     *	@throws SurfaceRealisationException
     */
    public String realiseToString(DependencyTree dt) throws SurfaceRealisationException
    {
    	List<AnchorString> list = realise(dt);
    	StringBuffer sb = new StringBuffer();
    	for (AnchorString as : list)
    		sb.append(as.toString());
    	return sb.toString();
    }
    
    /**	Creates a Spec (see simpleNLG) for a dependency tree
     *
     *	@param	node DTNode to be realised
     *	@return	PhraseSpec, representing node
     *	@throws SurfaceRealisationException
     */
    public PhraseSpec realiseNode(DTNode node) throws SurfaceRealisationException
    {
	    PhraseSpec parent = makeSpec(node);	//check the syncat/postag and create the correct type of phrasespec
	    if (parent instanceof SyntaxPhraseSpec)
	    {
			Iterator it = node.getOutgoingEdges();
    		while (it.hasNext())
    		{
    			DTEdge edge = (DTEdge) it.next();
    			PhraseSpec child = realiseNode((DTNode) edge.getTarget());
    			parent = addPhrase((SyntaxPhraseSpec) parent, child, edge.getLabel());	//add phrase to sentence with right function
    		}
    	}  
   		return parent;
    }
    
    private VerbGroupSpec makeAggregateVerbSpec(DTNode node) throws SurfaceRealisationException
    {
    	List<DTNode> conjuncts = node.getDepChildren(LinguisticTerms.CONJUNCT);
    	if (!conjuncts.get(0).getLabel().equals(LinguisticTerms.VERB))	//if the conjuncts are no verbs, do nothing
    		return null;
    	
    	PhraseSpec spec = realiseNode(conjuncts.get(0));	
    	if (spec instanceof StringPhraseSpec)
    		return null;	//not a verb; do  nothing
    	
    	CoordinateVerbGroupSpec vg = new CoordinateVerbGroupSpec();
    	return vg;
    }
    
    /**	Creates an aggregate phrase spec for a conjunction
     */
    private PhraseSpec makeAggregateSpec(DTNode node) throws SurfaceRealisationException
    {
    	String cat = node.getLabel();
    	if (!cat.equals(LinguisticTerms.CONJUNCTION))
    		throw new SurfaceRealisationException("I can't create an aggregate phrase spec from a node that's not a conjunction!");
    		
    	String dep = node.getDeplbl();
    	PhraseSpec spec = makeAggregateVerbSpec(node);
    	if (spec != null)
    		return spec;
    	
    	AggregatePhraseSpec as = new AggregatePhraseSpec();	//else make an aggregate spec
    	as.setFlash(node.flash());
    	List<Edge> edges = node.getOutgoingEdges(null);
    	Collections.sort(edges, new EdgeOrderComparator());
 
    	for (int i = 0; i < edges.size(); i++)
    	{
    		DTEdge edge = (DTEdge) edges.get(i);
    		if (edge.getLabel().equals(LinguisticTerms.CONJUNCTOR))
    		{
    			conjunctor = edge.getTarget().getRoot();
    			as.setConjunct(conjunctor);
    		}
    		else if (edge.getLabel().equals(LinguisticTerms.CONJUNCT))
    			as.addSpec(realiseNode(edge.getTarget()));
    		else if (edge.getLabel().equals(LinguisticTerms.MODIFIER))
    			as.addFrontModifier(realiseNode(edge.getTarget()));
    		else if (edge.getLabel().equals(LinguisticTerms.PPMODIFIER))
    			as.addEndModifier(realiseNode(edge.getTarget()));
    		else
    			throw new SurfaceRealisationException("A conjunction should only have conjuncts, not " + edge.getLabel() + "s!");	
    	}
    	
		aggregateNode = node;
    	return as;
    }
    
    /**	Creates a phrase for a verb, setting all morphology
     */
    private PhraseSpec makeVerb(DTNode node)
    {
    	String cat = node.getLabel();
    	String dep = node.getDeplbl();
    	
    	if (dep.equals(LinguisticTerms.MODAL) || dep.equals(LinguisticTerms.MODIFIER))
		{
			StringPhraseSpec sps = new StringPhraseSpec(node.getRoot());
			sps.setAnchor(node.getAnchor());
			sps.setElided(node.isElided());
			sps.setFlash(node.flash());
			return sps;
		}
			
		Verb v = new Verb(node.getRoot());
		v.setAnchor(node.getAnchor());
		VerbGroupSpec vg = new VerbGroupSpec(v);
		if (node.getMorph() != null)
		{
			String tense = node.getMorph().getTense();
			if (tense.equals(LinguisticTerms.PRESENT))
				vg.setTense(Tense.PRESENT);
			else if (tense.equals(LinguisticTerms.PAST))
				vg.setTense(Tense.PAST);
			else if (tense.equals(LinguisticTerms.FUTURE))
				vg.setTense(Tense.FUTURE);
    		vg.setPassive(node.getMorph().isPassive());			//phrase is passive
    		vg.setSingular(node.getMorph().isSingular());
    		String particle = node.getMorph().getParticle();
    		if (particle != null)
    			vg.setParticle(new AnchorString(particle, null));
    		vg.setNegated(node.getMorph().isNegated());
		}
		
		vg.setFlash(node.flash());
		vg.setElided(node.isElided());
		return vg;
    }
    
    /**	Creates a spec for the given node-type; if it's a leaf, this will be a stringspec,
     *	otherwise the correct kind of syntaxspec, depending on the syntactic category
     *
     *	@param	node to be realised as a spec
     *	@return	PhraseSpec
     *	@throws SurfaceRealisationException
     */
    private PhraseSpec makeSpec(DTNode node) throws SurfaceRealisationException
    {
    	String cat = node.getLabel();
    	String dep = node.getDeplbl();
		
		if (cat.equals(LinguisticTerms.CONJUNCTION))
			return makeAggregateSpec(node);
		else if (cat.equals(LinguisticTerms.VERB))
			return makeVerb(node);
					
    	if (!node.hasOutgoingEdge())	//(node.isLeaf())
    	{
    		String root;
    		if (node.useAsPronoun())		//textplanner has specified the use of a pronoun here
    			root = node.getPronoun();
    		else if (node.getRoot() != null)
    			root = node.getRoot();
    		else
    			root = node.getID();
    		StringPhraseSpec sps = new StringPhraseSpec(root);	
			if (!(cat.equals(LinguisticTerms.NP) || cat.equals(LinguisticTerms.PRONOUN) || cat.equals(LinguisticTerms.NOUN)))
				sps.setAdjective(true);

    		if (node.getMorph() != null)
    		{
				sps.setSingular(node.getMorph().isSingular());
    			genitive = node.getMorph().isGenitive();
	   			sps.setGenitive(genitive);
	   			if (!node.useAsPronoun())
	   				sps.setQuote(node.getMorph().isQuote());
	   			sps.setRankOrdered(node.getMorph().isRankOrdered());
	    	}

    		sps.setAnchor(node.getAnchor());
    		sps.setFlash(node.flash());
    		sps.setElided(node.isElided());
    		return sps;
    	}
   		//not a leaf, so determine the syntactic category
    	SyntaxPhraseSpec phrase = null;
    	if (cat.equals(LinguisticTerms.NP))
    	{	//NP
    		NPPhraseSpec nps = new NPPhraseSpec();	
    		if (node.getRoot() != null)
    			nps.setNoun(new StringPhraseSpec(node.getRoot()));
    		if (node.getMorph() != null)
    		{
    			nps.setSingular(node.getMorph().isSingular());
    			nps.setQuote(node.getMorph().isQuote());
    			nps.setGenitive(node.getMorph().isGenitive());
    			nps.setRankOrdered(node.getMorph().isRankOrdered());
    		}
    		else
    			nps.setSingular(true);
    		   			
			if (node.useAsPronoun())
				nps.setPronoun(node.getPronoun());
			
    		nps.setAnchor(node.getAnchor()); 
    		nps.setFlash(node.flash());
    		nps.setElided(node.isElided());
    		return nps;
    	}
    	if (cat.equals(LinguisticTerms.PP))
    	{	//PP
    		PhraseSpec p = new PPPhraseSpec();
    		if (node.getAnchor() != null)
	    		p.setAnchor(node.getAnchor()); 
	    	else
	    	{
	    		Iterator it = node.getOutgoingEdges();
	    		boolean of = false, pronoun = false;
	    		DTNode obj = null;
	    		while (it.hasNext())
	    		{
	    			Edge e = (Edge) it.next();
	    			DTNode child = (DTNode) e.getTarget();
	    			String childDep = child.getDeplbl();
	    			if (childDep.equals(LinguisticTerms.HEAD) && child.getRoot().equals("of")) //|| child.getRoot().equals("for")))
	    				of = true;
	    			if (childDep.equals(LinguisticTerms.OBJECT) && child.useAsPronoun())
	    			{
	    				obj = child;
	    				pronoun = true;
	    			}
	    		}
	    	
	    		boolean part = false;
	    		DTNode np = (DTNode) node.getParents().next();
	    		if (np.getLabel().equals(LinguisticTerms.NP))
	    		{	//we don't want to change 'part of it' to 'its part'
		    		String noun = ((DTNode) np.getChildren(LinguisticTerms.HEAD).next()).getRoot();
		    		if ((noun != null) && noun.equals("part"))
		    			part = true;
	    		}
	    		if (pronoun && of && !part)	//change 'of it' to 'its')
	    		{
	    			p = new StringPhraseSpec(obj.getPronoun());	//, id);
				   	p.setGenitive(true);
				   	p.setAnchor(obj.getAnchor());
				}	
	    	}
	    	p.setFlash(node.flash());
	    	p.setElided(node.isElided());
    		return p;
    	}
    	if (cat.equals(LinguisticTerms.SMAIN) || cat.equals(LinguisticTerms.SSUB))
    	{	//SMAIN
    		SPhraseSpec s = new SPhraseSpec();
    		s.setAnchor(node.getAnchor());
    		s.setFlash(node.flash());
    		s.setElided(node.isElided());
    		if (cat.equals(LinguisticTerms.SSUB))
    		{
    			s.setSubsentence(true);
    			if ((node.getParent() != null) && node.getParent().getDeplbl().equals(LinguisticTerms.CONJUNCT))
    				s.setParenthesis(true);	//if the parent is a conjunct, and therefore prob. surrounded by komma's, use brackets for orthography instead
    		}
    		return s;
    	}
    	throw new SurfaceRealisationException ("Don't know what to do about category " + cat + "; not represented in SimpleNLG");
    }
    
    /**	Adds the given child phrase to the parent. Type determines the role child 
     *	plays in parent. 
     *
     *	@param	parent: higher level spec
     *	@param 	child:	spec to be added to parent
     *	@param	type: the dependencylabel of child, giving the role of the child in parent
     *
     *	@return	SyntaxPhraseSpec
     *	@throws SurfaceRealisationException
     */
    private SyntaxPhraseSpec addPhrase(SyntaxPhraseSpec parent, PhraseSpec child, String type) throws SurfaceRealisationException
    {
      	if (parent instanceof NPPhraseSpec)
      	{	//NP can have a determiner and a head, and also (PP)modifiers
    		NPPhraseSpec phrase = (NPPhraseSpec) parent;
    		if (type.equals(LinguisticTerms.HEAD))
    		{
    			if (phrase.isPronoun())	//if the phrase is realised as a pronoun, do nothing 
    				return phrase;
    			phrase.setNoun(child);	//change the head
    		}
    		else if (type.equals(LinguisticTerms.DET))
    		{
    			if (phrase.isGenitiveDeterminer())		//determiner 'its' already added to replace original, so do nothing
    				return phrase;

    			phrase.setDeterminer(child);
    			if (genitive)				//set the determiner to genitive (this person's [noun])
    			{
    				child.setGenitive(true);
    				phrase.setGenitiveDeterminer(true);
    				genitive = false;		//reset
    			}
    		}
    		else if (type.equals(LinguisticTerms.MODIFIER))
    			phrase.addHeadModifier(child);			
    		else if (type.equals(LinguisticTerms.PPMODIFIER))
    		{
    			if (child.isGenitive())	//replace 'the author of it' with 'its author'
    			{
    				phrase.setDeterminer(child);
       				phrase.setGenitiveDeterminer(true);
    			}
    			else
	    			phrase.addEndModifier(child);
	    	}
    		else
    			throw new SurfaceRealisationException("SR 574: Don't know dependency label " + type + "; not represented in simpleNLG");
    		return phrase;
    	}
    	else if (parent instanceof PPPhraseSpec)
    	{	//PP can have a head and an object
    		PPPhraseSpec phrase = (PPPhraseSpec) parent;
    		if (type.equals(LinguisticTerms.HEAD))
    			phrase.setPreposition(child);
    		else if (type.equals(LinguisticTerms.OBJECT))
    		{
    			child.setAccusative(true);		//change 'by who' to 'by whom'
    			phrase.addObject(child);
    		}
    		else
    			throw new SurfaceRealisationException("SR 588: Don't know dependency label " + type + "; not represented in simpleNLG");
    		return phrase;
    	}
    	else if (parent instanceof SPhraseSpec)
    	{	//sentence can have a head, subject, object/complement, (pp)modifier
    		SPhraseSpec phrase = (SPhraseSpec) parent;
    		if (type.equals(LinguisticTerms.HEAD))
    			phrase.setVerbGroup((VerbGroupSpec) child);
    		else if (type.equals(LinguisticTerms.SUBJECT))  			
    			phrase.addSubject(child);
    		else if (type.equals(LinguisticTerms.OBJECT) || type.equals(LinguisticTerms.COMPLEMENT))
    			phrase.addComplement(child);  
    		else if (type.equals(LinguisticTerms.MODIFIER)) 
    			phrase.addFrontModifier(child);
    		else if (type.equals(LinguisticTerms.PPMODIFIER))
    			phrase.addEndModifier(child);
    		else if (type.equals(LinguisticTerms.MODAL))
    			phrase.setModal(child);
    		else
    			throw new SurfaceRealisationException("SR 556: Don't know dependency label " + type + "; not represented in simpleNLG");
    		return phrase;
    	}
    	else if (parent instanceof VerbGroupSpec)
    	{
    		VerbGroupSpec vg = (VerbGroupSpec) parent;
    		if (type.equals(LinguisticTerms.MODIFIER))
    			vg.addHeadModifier(child);
    		else if (type.equals(LinguisticTerms.MODAL))
    			vg.setModal(child);
    		else if (type.equals(LinguisticTerms.CONJUNCT))
    			((CoordinateVerbGroupSpec) vg).addCoordinate((VerbGroupSpec) child);
    		else if (type.equals(LinguisticTerms.CONJUNCTOR))	//already added; do nothing
    			((CoordinateVerbGroupSpec) vg).setConjunction(child.getHead());
 			else
    			throw new SurfaceRealisationException("SR 564: VerbGroupSpec cannot have a child with dependency label " + type);
    		return vg;
    	}
    	else
	    	throw new SurfaceRealisationException("SR 568: Unfamiliar kind of SyntaxPhraseSpec");
	 }
	
	/**	Orders edges (e.g. to get authors in correct order)
	 */ 
	public class EdgeOrderComparator implements Comparator
	{
		/**	Compares two DTEdges, to see which comes first.
		 *	@param o1 First edge
		 *	@param o2 Second edge
		 *	@return negative if o1 comes first, negative if o2 comes first.
		 *	@throws ClassCastException if one of the arguments is not a DTEdge
		*/ 
		public int compare(Object o1, Object o2) throws ClassCastException
		{
			int value1 = ((DTEdge) o1).getOrder();
			int value2 = ((DTEdge) o2).getOrder();
			
			if (value1 < value2)
				return -1;
			if (value1 == value2)
				return 0;
			return 1;
		}
	}
}