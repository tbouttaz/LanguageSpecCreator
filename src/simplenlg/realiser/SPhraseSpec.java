/* Copyright (c) 2007, the University of Aberdeen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted FOR RESEARCH PURPOSES ONLY, provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 * 		this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.
 * 3. Neither the name of the University of Aberdeen nor the names of its contributors 
 * 	  may be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 *    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 *    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 *    FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 *     ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 *     EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *     
 *  Redistribution and use for purposes other than research requires special permission by the
 *  copyright holders and contributors. Please contact Ehud Reiter (ereiter@csd.abdn.ac.uk) for
 *  more information.   
 */

/* change log 
 * 19-Dec-06:  do nothing if add null subject/complement/etc
 * */
package simplenlg.realiser;

import java.util.ArrayList;
import java.util.List;

import simplenlg.exception.SimplenlgException;
import simplenlg.features.Form;
import simplenlg.features.Mood;
import simplenlg.features.Number;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.lexicon.lexicalitems.Verb;
import WYSIWYM.model.Anchor;

/**
 * simplenlg.SPhraseSpec is a simple syntactic representation of a sentential
 * phrase.
 * <P>
 * It consists of
 * <UL>
 * <LI> cue phrase
 * <LI>frontModifier(s)
 * <LI> subject(s)
 * <LI> verb (including particle)
 * <LI> complement(s)
 * <LI> modifier(s)
 * </UL>
 * These are linearised in the above order
 * <P>
 * Also various simplenlg.features: tense, form, passive, negated, progressive
 * 
 * <p>
 * The simplenlg.features of a sentence are inherited by its verb phrase; this
 * is updated every time a change is made to the sentence properties, because
 * <code>SPhraseSpec</code> has a
 * <code>java.beans.PropertyChangeSupport</code>.
 * 
 * @author ereiter -- Ehud Reiter 15-Feb-06
 *	adapted by F. Hielkema, May 2007
 * 
 */
public class SPhraseSpec extends SyntaxPhraseSpec {
	
	// components
	List<PhraseSpec> subjects;  // subjects (at least one, can be more than one)
	VerbGroupSpec verbGroup;
	List<PhraseSpec> complements; // complements (can be empty)
	List<PhraseSpec> endModifiers;  // modifiers (can be empty)
	List<PhraseSpec> frontModifiers;  // modifiers which occur at beginning of sentence
	PhraseSpec cuePhrase;	// cue phrase at beginning of sentence
	
	boolean subsentence = false, parenthesis = false;		//true if this is a subsentence (e.g. relative clause)
	// verb number depends on size of subjects list
	// constructors **************************************

	public SPhraseSpec() {
		super();
		subjects = new ArrayList<PhraseSpec> ();   // no subjects
		verbGroup = new VerbGroupSpec(); // empty constructor initialised
											// with null verb
		complements = new ArrayList<PhraseSpec> ();  //no complements
		endModifiers = new ArrayList<PhraseSpec> ();  //no modifiers
		frontModifiers = new ArrayList<PhraseSpec> ();  //no front modifiers
		cuePhrase = null;					// no cue phrase
	}
	
	public SPhraseSpec(Object subject, Object verb, Object complement) {
		this();
		addSubject(subject);
		setVerb(verb);
		addComplement(complement);
	}
	
	public SPhraseSpec(Object subject, Object verb) {
		this();
		addSubject(subject);
		setVerb(verb);
	}

	public void setSubsentence(boolean s)
	{
		subsentence = s;
	}

	public boolean isSubsentence()
	{
		return subsentence;
	}

	public void setParenthesis(boolean s)
	{
		parenthesis = s;
	}

	public boolean hasParenthesis()
	{
		return parenthesis;
	}

	// Getters, setters *************************************
	
	public List<PhraseSpec> getSubjects() {
		return subjects;
	}

	public void addSubject(Object subject) {  // add a subject
		subjects.add(makePhraseSpec(subject));	
	}
	
	public void setSubject(Object subject) {  // set a subject (gets rid of previous subject)
		subjects.clear();
		subjects.add(makePhraseSpec(subject));		
	}

	public List<PhraseSpec> getComplements() {
		return complements;
	}
	
	/**
	 * returns verb
	 * <P>
	 * Does not include particle (if specified).
	 * 
	 * @see SPhraseSpec#getParticle()
	 */
	public String getVerb() {
		return verbGroup.getHead();
	}

	/**
	 * 
	 * @return The {@link VerbGroupSpec} of this sentence. Unlike
	 *         {@link SPhraseSpec#getVerb()}, this methof will return the verb
	 *         group object.
	 * 
	 */
	public VerbGroupSpec getVerbGroup() {
		return verbGroup;
	}
	
	/**
	 * sets verb (replaces existing verb). A particle can be specified, for
	 * example "split up". Using this method, the verb group of the sentence can
	 * be specified using simply a <code>String</code> (this is then
	 * internally rendered as head of the
	 * {@link simplenlg.realiser.VerbGroupSpec} of the sentence.
	 */
	public void setVerb(Object verb) {
		String v = null;
		Anchor a = null;
		if (verb instanceof String)
			v = (String) verb;
		else if (verb instanceof StringPhraseSpec)
		{
			a = ((StringPhraseSpec)verb).getAnchor();
			v = ((StringPhraseSpec)verb).getHead();
		}
		if (isBeVerb(v)) {
			verbGroup.setHead("be");

		} else {
			verbGroup.setHead(v);
		}
		verbGroup.setAnchor(a);
	}

	/**
	 * sets verb (replaces existing verb)
	 * 
	 * @param verb
	 *            An instance of {@link simplenlg.lexicon.lexicalitems.Verb}
	 */
	public void setVerb(simplenlg.lexicon.lexicalitems.Verb verb) {
		verbGroup.setHead(verb);
	}

	/**
	 * Add a verb (in addition to the existing verb). This means that the verb group
	 * in this sentence is now a coordinate, 
	 * i.e. an instance of {@link simplenlg.realiser.CoordinateVerbGroupSpec}.
	 * All verb phrases will inherit the tense, passive,
	 * negated, progressive and perfect simplenlg.features of the sentence.
	 * E.g.:
	 * <ul>
	 * <li>the man walks and talks (present tense)</li>
	 * <li>the man is walking and talking (progressive)</li>
	 * <li>the man had been walking, talking and singing (past, perfect,
	 * progressive)</li>
	 * </ul>
	 */
	public void addVerb(String verb, Anchor a) {

		if (isBeVerb(verb)) {
			verbGroup = verbGroup.coordinate(new VerbGroupSpec("be"));
		} else {
			verbGroup = verbGroup.coordinate(new VerbGroupSpec(verb));
		}
		if ((a != null) && (verbGroup.getAnchor() == null))
			verbGroup.setAnchor(a);
	}

	/**
	 * Sets the verb particle.
	 * 
	 * @see SPhraseSpec#setVerb
	 */
	public void setParticle(Object particle) {
		if (particle instanceof String)
			verbGroup.setParticle(new AnchorString((String) particle, null));
		else
			verbGroup.setParticle((AnchorString) particle);
	}

	/**
	 * Like {@link SPhraseSpec#setVerb(Verb)}, but using a full
	 * {@link VerbGroupSpec}. This results in the sentence having the argument
	 * vp as its verb phrase, that is, this method will replace whatever verb
	 * group the <code>SPhraseSpec</code> had. Note that the sentence will
	 * inherit all the tense/aspect {@link simplenlg.features} of the verb
	 * group.
	 * 
	 * @param vp
	 *            The {@link VerbGroupSpec}.
	 */
	public void setVerbGroup(VerbGroupSpec vp) {
		if (verbGroup.getModal() != null)
			vp.setModal(verbGroup.getModal());
		this.verbGroup = vp;

	}
	
	public List<PhraseSpec> getEndModifiers() {
		return endModifiers;
	}
	
	public List<PhraseSpec> getFrontModifiers() {
		return frontModifiers;
	}	
	
	public void addComplement(Object complement) {  // add a complement
		if (complement != null)
			complements.add(makePhraseSpec(complement));
	}
	
	/**
	 * adds a modifier, tries to guess location
	 * currently single word is headModifier, otherwise endModifier
	 */
	public void addModifier(Object modifier) { // add a modifier
		// put new front endModifiers at beginning of list
		if (modifier != null && modifier instanceof String &&
				!((String)modifier).contains(" "))
			addHeadModifier(modifier);
		else
			addEndModifier(modifier);
	}
	/*
	public void addModifier(Object modifier) {  // add a modifier
		if (modifier != null)
			endModifiers.add(makePhraseSpec(modifier));	
	}*/	
	
		/** adds an (end of phrase) modifier (in addition to existing endModifiers) */
	public void addEndModifier(Object modifier) { // add a modifier
		if (modifier != null)
			endModifiers.add(makePhraseSpec(modifier));
	}

	public void addFrontModifier(Object modifier) {  // add a front modifier
		// put new front modifiers at beginning of list
		if (modifier != null)
			frontModifiers.add(0, makePhraseSpec(modifier));
	}	

	/** returns (before head) endModifiers, such as adverbs */
	public List<PhraseSpec> getHeadModifiers() {
		return verbGroup.getHeadModifiers();
	}

	/**
	 * adds a (before head, eg adverb) modifier (in addition to existing head
	 * endModifiers)
	 */
	public void addHeadModifier(Object modifier) { // add a front modifier
		verbGroup.addHeadModifier(modifier);
	}

	public void setNumber(Number n)
	{
		verbGroup.setNumber(n);
	}

	/** sets the tense of a phrase */
	public void setTense(Tense t) {
		verbGroup.setTense(t);
	}

	/** gets the tense of a phrase */
	public Tense getTense() {
		return verbGroup.getTense();
	}

	/**
	 * This method sets the "form" (ininitive, gerund or default) of the sentence.
	 * By convention, mood has precedence over form, so that if the sentence mood
	 * has been set and it is incompatible with the new form, an exception is thrown.
	 * For example, the imperative mood is incompatible with the gerund form.
	 * <p>
	 * This method calls the {@link VerbGroupSpec#setForm(Form)} method
	 * of the child {@link VerbGroupSpec}.
	 * </p>
	 * 
	 * @param f
	 *            The {@link simplenlg.features.Form} of this sentence.
	 * @throws {@link simplenlg.exception.SimplenlgException}
	 *             if the selected form is incompatible with the
	 *             {@link simplenlg.features.Mood} of the sentence.
	 * @see {@link simplenlg.features.Form#isCompatible(Mood)}
	 * @see {@link VerbGroupSpec#setForm(Form)} 
	 */
	public void setForm(Form f) throws SimplenlgException {

		try {
			verbGroup.setForm(f);
			
		} catch( SimplenlgException se ) {
			throw se;
		}
		
	}

	/**
	 * 
	 * @return The {@link simplenlg.features.Form} of this sentence.
	 */
	public Form getForm() {
		return verbGroup.getForm();
	}

	/**
	 * @param m
	 *            The {@link simplenlg.features.Mood} of the sentence.
	 */
	public void setMood(Mood m) {
		verbGroup.setMood(m);
	}

	/**
	 * 
	 * @return the {@link simplenlg.features.Mood} of this sentence.
	 */
	public Mood getMood() {
		return verbGroup.getMood();
	}
	
	/**
	 * 
	 * @return the modal auxiliary of this phrase
	 */
	public String getModal() {
		return verbGroup.getModal().getHead();
	}

	/**
	 * Sets the modal auxiliary of the verb (eg, "can", "must")
	 * 
	 * @param modal
	 */
	public void setModal(PhraseSpec modal) {
		verbGroup.setModal(modal);
	}
	
	/**
	 * sets whether the phrase is perfect or not. -- AG
	 */
	public void setPerfect(boolean perf) {
		verbGroup.setPerfect(perf);
	}

	/**
	 * @return <code>true</code> if this sentence is perfect
	 */
	public boolean isPerfect() {
		return verbGroup.isPerfect();
	}

	/**
	 * sets whether the sentence is progressive aspect.
	 * <P>
	 * For example,
	 * <UL>
	 * <li> "John eats" (not progressive)
	 * <li> "John is eating" (progressive)
	 * </UL>
	 * 
	 * @param prog
	 *            progressive aspect if true
	 */
	public void setProgressive(boolean prog) {
		verbGroup.setProgressive(prog);
	}

	/**
	 * returns True if sentence is in Progressive aspect.
	 * <P>
	 * 
	 * @see SPhraseSpec#setProgressive(boolean)
	 */
	public boolean isProgressive() {
		return verbGroup.isProgressive();
	}

	/**
	 * sets whether the sentence is negated.
	 * <P>
	 * For example,
	 * <UL>
	 * <li> "John eats" (not negated)
	 * <li> "John does not eat" (negated)
	 * </UL>
	 * 
	 * @param neg
	 *            sentence is negated if true
	 */
	public void setNegated(boolean neg) {
		verbGroup.setNegated(neg);
	}

	/**
	 * returns True if sentence is negated.
	 * <P>
	 * 
	 * @see SPhraseSpec#setNegated(boolean)
	 */
	public boolean isNegated() {
		return verbGroup.isNegated();
	}

	/**
	 * returns True if sentence is in passive voice.
	 * <P>
	 * 
	 * @see SPhraseSpec#setPassive(boolean)
	 */
	public boolean isPassive() {
		return verbGroup.isPassive();
	}

	/**
	 * sets whether the sentence is passive voice.
	 * <P>
	 * For example,
	 * <UL>
	 * <li> "John eats an apple" (not passive)
	 * <li> "An apple is eaten by John" (passive)
	 * </UL>
	 * 
	 * @param pass
	 *            passive voice if true
	 */
	public void setPassive(boolean pass) {
		verbGroup.setPassive(pass);
	}

	public void setCuePhrase(Object cuePhrase) {
		this.cuePhrase = makePhraseSpec(cuePhrase);
	}
	
	public boolean hasCuePhrase() {
		// return T if a cue phrase is present
		return (cuePhrase != null);
	}

	/** returns the cue phrase (null if no cue phrase specified) */
	public PhraseSpec getCuePhrase() {
		return cuePhrase;
	}

	/**
	 * returns the verb particle (null if no particle specified)
	 * <P>
	 * 
	 * @see SPhraseSpec#setVerb(String)
	 */
	public AnchorString getParticle() {
		return verbGroup.getVerbalHead().getParticle();
	}

	// Realisation methods

	private boolean isBeVerb(String verb) {
		// returns T if this verb is a form of "be"
		return verb.equalsIgnoreCase("be") || verb.equalsIgnoreCase("am")
				|| verb.equalsIgnoreCase("are") || verb.equalsIgnoreCase("is")
				|| verb.equalsIgnoreCase("was")
				|| verb.equalsIgnoreCase("were");
	}
	
	//	 realise an Sphrase spec
	// calculate surface subjects, etc
	// then realise and linearise
	@Override
	public List<AnchorString> realise(Realiser r) {
		// get the Form and Mood
		Form form = verbGroup.getForm();

		// calculate surface subject
		List<PhraseSpec> surfaceSubjects = subjects; // normally the deep subject
		if (!form.hasSubject()) // no surface subject in these forms
			surfaceSubjects = new ArrayList<PhraseSpec>();
		else if (verbGroup.isPassive()) //else if (passive) // complements are surface subject in
										// passive form
			surfaceSubjects = complements;				

		// if they are S's, force Gerund form if necessary
		// also mark as part of the subject for NPPhraseSpec
		for (PhraseSpec spec : surfaceSubjects)
			if (spec instanceof SPhraseSpec) 
			{
				SPhraseSpec s = (SPhraseSpec) spec;
				if (!form.isEmbedded())
					s.setForm(Form.GERUND);

			}
			else if (spec instanceof NPPhraseSpec)
				((NPPhraseSpec) spec).setSubjectNP(true);
			else if (spec instanceof StringPhraseSpec)
				((StringPhraseSpec) spec).setSubject(true);
			else if (spec instanceof AggregatePhraseSpec)
				((AggregatePhraseSpec) spec).setSubject(true);

		// calculate surface complements
		List<PhraseSpec> surfaceComplements = complements;	// normally the deep complement
		if (verbGroup.isPassive()) // but no complements in passive
			surfaceComplements = new ArrayList<PhraseSpec>();
		
		// if they are S's, force Infinitive form if necessary
		for (PhraseSpec spec : surfaceComplements)
			if (spec instanceof SPhraseSpec) {
				SPhraseSpec s = (SPhraseSpec) spec;
				if (!form.isEmbedded())
					s.setForm(Form.INFINITIVE);
			} else if (spec instanceof NPPhraseSpec)
				((NPPhraseSpec) spec).setSubjectNP(false);
			else if (spec instanceof AggregatePhraseSpec)
				((AggregatePhraseSpec) spec).setSubject(false);

		// calculate surface endModifiers
		List<PhraseSpec> surfaceModifiers = new ArrayList<PhraseSpec>(endModifiers); 
		if (verbGroup.isPassive() && !subjects.isEmpty()) { // but add BY
															// subjects PP if
			// passive and at least one
			// subject
			PPPhraseSpec ByPP = new PPPhraseSpec("by");
			ByPP.setObjects(subjects);
			surfaceModifiers.add(ByPP);
		}

		// calculate NP that verb should agree with
		// usually subjects
		List<PhraseSpec> agreeNP = surfaceSubjects;
		// but agree with complements for "there be"
		if ((surfaceSubjects.size() == 1) && !surfaceComplements.isEmpty() &&
				surfaceSubjects.get(0).getHead().equalsIgnoreCase("there") &&
				verbGroup.getHead().equalsIgnoreCase("be"))
			agreeNP = surfaceComplements;
		
		// now realise components
		List<AnchorString> cuePhraseText = r.realise(cuePhrase);
		List<AnchorString> frontModifierText = r.realiseList(frontModifiers);
		List<AnchorString> subjectText = r.realiseAndList(surfaceSubjects);
		
		// get the agreement features (person and number)
		verbGroup.setPerson( getPersonFeature( agreeNP ) );
		verbGroup.setNumber( getNumberFeature( agreeNP ) );
		
		//realise the verb group
		List<AnchorString> verbText = r.realise(verbGroup);
		
		List<AnchorString> complementText = r.realiseAndList(surfaceComplements);
		List<AnchorString> modifierText = r.realiseList(surfaceModifiers);

		// now modify depending on form
		// no cue phrases in embedded sentences
		if (form.isEmbedded())
		{
			cuePhraseText = new ArrayList<AnchorString>();
			cuePhraseText.add(new AnchorString("", null));
		}
		
		// front modifiers at end if no subject (in order to emphasise verb)
		if (!form.hasSubject()) {
			modifierText = r.listWords(modifierText,frontModifierText);
			frontModifierText = new ArrayList<AnchorString>();
			frontModifierText.add(new AnchorString("", null));
		}
		
		List<AnchorString> mainSent = r.listWords(cuePhraseText, frontModifierText, subjectText, verbText/*, particleText*/, complementText, modifierText);
		if (subsentence)
	//	{	//in a subsentence/relative clause, the subject comese first and the front modifiers last
		//	mainSent = r.listWords(cuePhraseText, subjectText, verbText/*, particleText*/, complementText, modifierText, frontModifierText);
			mainSent = r.applySubsentenceOrthography(mainSent, parenthesis);
	//	}
	//	else
	//		mainSent = r.listWords(cuePhraseText, frontModifierText, subjectText, verbText/*, particleText*/, complementText, modifierText);
		return flash(mainSent);
	}

	// get the Person feature of this sentence (depending on the subject)
	public Person getPersonFeature(List<PhraseSpec> agreeNP) {
				
	
		// more than 1 np => always 3rd person
		if (agreeNP.size() > 1) {
			return Person.THIRD;

		} else if( agreeNP.size() > 0 ){						
			return agreeNP.get(0).getPerson();
		
		} else {
			return Person.THIRD;
		}
	}

	// get the Number feature of this sentence
	private Number getNumberFeature(List<PhraseSpec> agreeNP) {
		if (verbGroup.getNumber() == Number.PLURAL)
			return Number.PLURAL;

		if (agreeNP.size() > 1) {
			return Number.PLURAL;
		} else if( agreeNP.size() > 0 ){
			return (agreeNP.get(0).isPlural() ? Number.PLURAL : Number.SINGULAR);
		
		} else {
			return Number.SINGULAR;
		}
	}
	
	// other methods *************************************
	@Override
	public String getHead() {
		return verbGroup.getHead();
	//	return verb.getHead();
	}

}
