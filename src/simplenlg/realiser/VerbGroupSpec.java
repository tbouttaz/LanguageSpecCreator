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

package simplenlg.realiser;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import simplenlg.exception.SimplenlgException;
import simplenlg.features.Form;
import simplenlg.features.Mood;
import simplenlg.features.Number;
import simplenlg.features.Person;
import simplenlg.features.Tense;
import simplenlg.lexicon.Lexicon;
import simplenlg.lexicon.lexicalitems.Category;
import simplenlg.lexicon.lexicalitems.Verb;

/**
 * This is the class that encapsulates a Verb Group (the part of the verb phrase 
 * containing a verb, and possibly an auxiliary verb phrase). A <code>VerbGroupSpec</code>
 * consista of the following components:
 * 
 * <ul>
 * <li> A head verb (possibly with a particle). </li>
 * <li> A list of temporal and aspectual simplenlg.features determining whether
 * the VP is:
 * <ul>
 * <li>perfect</li>
 * <li> present, past or future</li>
 * <li> progressive</li>
 * <li> passive</li>
 * <li>negated</li>
 * </ul>
 * <li>A {@link simplenlg.features.Person} feature</li>
 * <li>A {@link simplenlg.features.Number} feature</li>
 * <li> A {@link simplenlg.features.Mood} feature </li>
 * <li> A {@link simplenlg.features.Form} feature</li>
 * </ul>
 * 
 * <p>
 * Although getter and setter methods are provided, none of these needs to be
 * set explicitly if the <code>VerbGroupSpec</code> is embedded within a
 * sentence (an {@link SPhraseSpec}), where setting any of the above
 * simplenlg.features will result in the verb phrase inheriting them.
 * </p>
 *
 * @author agatt
 * 
 */

public class VerbGroupSpec extends SyntaxPhraseSpec {

	private Verb head;

	boolean perfect, progressive, passive, negated, realiseAuxiliary;
	
	PhraseSpec modal;					// modal, eg "must"
	
	List<PhraseSpec> headModifiers;	// pre-head modifier, eg adverb such as "quickly"

	Tense tense;

	Person person;

	Number number;

	Mood mood;

	Form form;
	
	// private vars to hold components of a realisation
	private List<AnchorString> auxiliaryRealisation;	// realisation of auxiliaries
	private List<AnchorString> mainVerbRealisation;	// realisation of main verb (and trailing "not")

	/**
	 * Constructs a new instance of </code>VerbGroupSpec</code>. By default
	 * the phrase has all the simplenlg.features perfect, progressive, passive,
	 * negated set to <code>false</code> and its tense set to present.
	 * 
	 */
	public VerbGroupSpec() {
		perfect = false;
		progressive = false;
		passive = false;
		negated = false;
		modal = null;
		headModifiers = new ArrayList<PhraseSpec>(); // no head endModifiers
		tense = Tense.PRESENT;
		person = Person.THIRD;
		number = Number.SINGULAR;
		mood = Mood.NORMAL;
		form = Form.NORMAL;
		head = simplenlg.lexicon.Lexicon.NULL_VERB;
		realiseAuxiliary = true;
	}

	/**
	 * Constructs a new instance of a <code>VerbGroupSpec</code> with a head
	 * verb. This is an instance of {@link simplenlg.lexicon.lexicalitems.Verb}
	 * 
	 * @param v
	 *            The Verb
	 */
	public VerbGroupSpec(Verb v) {
		this();
		head = v;
	}

	/**
	 * Constructs a new instance of a <code>VerbGroupSpec</code> with a head
	 * verb string. The constructor automatically renders the string as an
	 * instance of a {@link simplenlg.lexicon.lexicalitems.Verb}. The String
	 * may consist of head + one or more particles. Thus "get up" and "get" are
	 * both legal arguments to this constructor.
	 * 
	 * @param verb
	 */
	public VerbGroupSpec(String verb) {
		this();
		head = new Verb(verb);
	}

	/**
	 * Set or replace the head verb.
	 * 
	 * @param v
	 *            A {@link simplenlg.lexicon.lexicalitems.Verb}
	 */
	public void setHead(Verb v) {
		head = v;
	}

	/**
	 * Set or replace the head verb to the new Verb created from the argument
	 * string.
	 * 
	 * @param s
	 */
	public void setHead(String s) {
		head = new Verb(s);
	}

	/**
	 * Sets the form of this <code>verbGroup</code>.
	 * 
	 * @throws {@link simplenlg.exception.SimplenlgException}
	 *             if the selected form is incompatible with the
	 *             {@link simplenlg.features.Mood} of the sentence.
	 * @see {@link simplenlg.features.Form#isCompatible(Mood)}
	 * @param f
	 *            The {@link simplenlg.features.Form}
	 */
	public void setForm(Form f) throws SimplenlgException {

		if (f.isCompatible(mood)) {
			form = f;

			// can't have PAST or FUTURE with gerunds or infinitives
			if (form == Form.GERUND || form == Form.INFINITIVE) {
				tense = Tense.PRESENT;
			}

		} else {
			throw new SimplenlgException("Form " + f
					+ " is incompatible with mood " + mood);
		}

	}

	/**
	 * 
	 * @return The {@link simplenlg.features.Form} of this verb.
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * 
	 * @param m
	 *            The {@link simplenlg.features.Mood}
	 */
	public void setMood(Mood m) {
		mood = m;

	}

	/**
	 * 
	 * @return The {@link simplenlg.features.Mood} of this verb.
	 */
	public Mood getMood() {
		return mood;
	}

	/**
	 * Sets the </code>passive</code> feature of this <code>VerbGroupSpec</code>
	 * 
	 * @param pass
	 */
	public void setPassive(boolean pass) {
		passive = pass;
	}

	/**
	 * 
	 * @return <code>true</code> if this <code>VerbGroupSpec</code> is
	 *         passive.
	 */
	public boolean isPassive() {
		return passive;
	}

	/**
	 * Sets the </code>progressive</code> feature of this <code>VerbGroupSpec</code>
	 * 
	 * @param prog
	 */
	public void setProgressive(boolean prog) {
		progressive = prog;
	}

	/**
	 * 
	 * @return <code>true<code> if this <code>VerbGroupSpec</code> is progressive.
	 */
	public boolean isProgressive() {
		return progressive;
	}

	/**
	 * Sets the </code>perfect</code> feature of this <code>VerbGroupSpec</code>
	 * 
	 * @param perf
	 */
	public void setPerfect(boolean perf) {
		perfect = perf;
	}

	/**
	 * 
	 * @return <code>true<code> if this <code>VerbGroupSpec</code> is passive.
	 */
	public boolean isPerfect() {
		return perfect;
	}

	/**
	 * Sets the </code>negated</code> feature of this <code>VerbGroupSpec</code>.
	 * This will result in automatic do-insertion if required at
	 * simplenlg.realiser stage (e.g. <i>did not do</i>).
	 * 
	 * @param neg
	 */
	public void setNegated(boolean neg) {
		negated = neg;
	}

	/**
	 * 
	 * @return <code>true<code> if this <code>VerbGroupSpec</code> is negated.
	 */
	public boolean isNegated() {
		return negated;
	}

	/**
	 * Sets the tense of the verb.
	 * 
	 * @param t
	 *            An instance defined in {@link simplenlg.features.Tense}
	 */
	public void setTense(Tense t) {
		tense = t;
	}

	/**
	 * 
	 * @return the {@link simplenlg.features.Tense} of this phrase
	 */
	public Tense getTense() {
		return tense;
	}


	/**
	 * 
	 * @return the modal auxiliary of this phrase
	 */
	public PhraseSpec getModal() {
		return modal;
	}

	/**
	 * Sets the modal auxiliary of the verb (eg, "can", "must")
	 * 
	 * @param modal
	 */
	public void setModal(PhraseSpec modal) {
		this.modal = modal;
	}

	/** returns (before head) endModifiers, such as adverbs */
	public List<PhraseSpec> getHeadModifiers() {
		return headModifiers;
	}

	/**
	 * adds a (before head, eg adverb) modifier (in addition to existing head
	 * endModifiers)
	 */
	public void addHeadModifier(Object modifier) { // add a front modifier
		// put new head endModifiers at beginning of list
		if (modifier != null)
			headModifiers.add(0, makePhraseSpec(modifier));
	}

	/**
	 * Set the person feature of this verb.
	 * 
	 * @param p
	 *            The {@link simplenlg.features.Person} value.
	 */
	public void setPerson(Person p) {
		person = p;
	}

	@Override
	public Person getPerson() {
		return person;
	}

	public void setSingular(boolean s)
	{
		if (s)
			setNumber(Number.SINGULAR);
		else
			setNumber(Number.PLURAL);
	}
	
	public boolean isSingular()
	{
		if (number.equals(Number.PLURAL))
			return false;
		return true;
	}

	/**
	 * Set the number feature of this verb group.
	 * 
	 * @param n
	 *            The {@link simplenlg.features.Number} value.
	 */
	public void setNumber(Number n) {
		number = n;
	}

	/**
	 * 
	 * @return The {@link simplenlg.features.Number} feature of this verb group.
	 */
	public Number getNumber() {
		return number;
	}

	/**
	 * Get the base form of the head verb, as a <code>String</code>
	 */
	public String getHead() {
		return head.getBaseForm().toString();
	}

	/**
	 * Unlike {@link VerbGroupSpec#getHead()}, this returns the
	 * {@link simplenlg.lexicon.lexicalitems.Verb} object that heads the verb
	 * group.
	 * 
	 * @return The <code>Verb</code> that heads the group.
	 */
	public Verb getVerbalHead() {
		return head;
	}

	/**
	 * For use in case the head verb is phrasal. (e.g. "get up"). The method
	 * adds the particle to the main verb of this phrase.
	 * 
	 * @param particle
	 *            The particle
	 * @see VerbGroupSpec#setParticle(String)
	 */
	public void setParticle(AnchorString particle) {
		this.head.setParticle(particle);
	}

	/**
	 * Compare two <code>VerbGroupSpec</code>s on the basis of grammatical
	 * simplenlg.features.
	 * 
	 * @param vp
	 *            The <code>VerbGroupSpec</code> that will be compared to this
	 *            one.
	 * @return <code>true</code> if the two <code>VerbGroupSpec</code>s
	 *         have identical tense/aspect simplenlg.features (passive, perfect,
	 *         negated, progressive) and identical grammatical/agreement
	 *         simplenlg.features (person and number).
	 */
	public boolean hasSameFeatures(VerbGroupSpec vp) {
		return (this.passive == vp.passive) && (this.perfect == vp.perfect)
				&& (this.negated == vp.negated)
				&& (this.progressive == vp.progressive)
				&& (this.person == vp.person) && (this.number == vp.number);
	}

	/**
	 * Suppress the auxiliary phrase during realisation.
	 * This means that only
	 * the head verb is realised. This is mostly used in conjunction with
	 * {@link CoordinateVerbGroupSpec}.
	 * 
	 * @param aux If <code>true</code>, the auxiliary is not realised.
	 */
	public void suppressAuxiliary(boolean aux) {
		realiseAuxiliary = !aux;
	}

	/**
	 * VCoordinate this <code>VerbGroupSpec</code> to another one to form a
	 * {@link CoordinateVerbGroupSpec}. The new coordinate verb group will have
	 * the same features (tense, passive, etc.) of this
	 * <code>VerbGroupSpec</code>.
	 * 
	 * @param vp
	 *            The <code>VerbGroupSpec</code>
	 * @return a <code>CoordinateVerbGroupSpec</code>
	 */
	public CoordinateVerbGroupSpec coordinate(VerbGroupSpec vp) {
		CoordinateVerbGroupSpec cvp = new CoordinateVerbGroupSpec(this.head, vp
				.getVerbalHead());
		cvp.setPassive(passive);
		cvp.setProgressive(progressive);
		cvp.setPerfect(perfect);
		cvp.setNegated(negated);
		cvp.setForm(form);
		cvp.setMood(mood);
		if (getAnchor() == null)
			cvp.setAnchor(vp.getAnchor());
		else
			cvp.setAnchor(getAnchor());
		return cvp;
	}

	// ***************
	// Realisation
	// ***************
	@Override
	public List<AnchorString> realise(Realiser r) {
		if (isElided())	//don't realise if it's elided!
			return new ArrayList<AnchorString>();
		computeRealisation(r);
		List<AnchorString> result = r.listWords(auxiliaryRealisation, mainVerbRealisation);
		return flash(result);
	}

	// return just the auxiliary
	public List<AnchorString> realiseAuxiliary(Realiser r) {
		computeRealisation(r);
		return auxiliaryRealisation;
	}

	// return just the main verb realisation
	public List<AnchorString> realiseMainVerb(Realiser r) {
		computeRealisation(r);
		return mainVerbRealisation;
	}

	
	private void computeRealisation(Realiser r) {
		// compute two components of realisation, auxiliary and main verb
		//note that main verb realisation includes trailing "not"
		// get constituents
		List<AnchorString> VGWords = realiseVerbGroup(r);
		
		// now put this together
		mainVerbRealisation = new ArrayList<AnchorString>();		// main verb, plus ending "not" if necessary
		auxiliaryRealisation = new ArrayList<AnchorString>();  // auxiliaries (including endModifiers)
		for (AnchorString word: VGWords) {
			List<AnchorString> temp = new ArrayList<AnchorString>();
			temp.add(word);
			if ((mainVerbRealisation.size() == 0) || mainVerbRealisation.get(mainVerbRealisation.size() - 1).toString().equalsIgnoreCase("not"))
				mainVerbRealisation = r.listWords(temp, mainVerbRealisation);
			else
				auxiliaryRealisation = r.listWords(temp, auxiliaryRealisation);

		}
		// add endModifiers
		List<AnchorString> headModifierText = r.realiseList(headModifiers);
		auxiliaryRealisation = r.listWords(auxiliaryRealisation, headModifierText);
	}

	
	private List<AnchorString> realiseVerbGroup(Realiser r) {
		// build verb group from head verb out
		// return this as a list of constituents, element 0 being last word
		// frontVG is Verb currently at front of VG
		// restVG is rest of VG
		// start off with main verb
		Verb frontVG = head;
		Stack<AnchorString> restVG = new Stack<AnchorString>();
		Lexicon lex = r.getLexicon();
		
		// passive
		if (passive) {
			List<AnchorString> list = frontVG.getPastParticiple();
			for (int i = list.size(); i > 0; i--)
				restVG.push(list.get(i - 1));
			frontVG = (Verb)lex.getItem(Category.VERB, "be");
		}

		// progressive
		if (progressive) {
			List<AnchorString> list = frontVG.getPresentParticiple();
			for (int i = list.size(); i > 0; i--)
				restVG.push(list.get(i - 1));
			frontVG = (Verb)lex.getItem(Category.VERB, "be");
		}

		// perfect
		if (perfect) {
			List<AnchorString> list = frontVG.getPastParticiple();
			for (int i = list.size(); i > 0; i--)
				restVG.push(list.get(i - 1));
			frontVG = (Verb)lex.getItem(Category.VERB, "have");
		}
		
		// modal
		List<AnchorString> actualModal = new ArrayList<AnchorString>();
		if (form == Form.INFINITIVE)
			actualModal.add(new AnchorString("to", null));
		else if (modal != null)
			actualModal.addAll(modal.realise(r));
		else if (form.allowsTense() && tense == Tense.FUTURE)
			actualModal.add(new AnchorString("will", null));
		
		if (actualModal.size() > 0) {
			restVG.push(frontVG.getBaseForm());
			frontVG = null;
		}
		
		// negated
		if (negated) {
			if (!restVG.empty() || (frontVG != null && frontVG.isCopular()))
				restVG.push(new AnchorString("not", null));
			else {
				if (frontVG != null)
					restVG.push(frontVG.getBaseForm());
				restVG.push(new AnchorString("not", null));
				frontVG = (Verb)lex.getItem(Category.VERB, "do");
			}
		}

		// now inflect frontVG (if it exists) and push it on restVG
		if (frontVG != null) {
			List<AnchorString> list = new ArrayList<AnchorString>();
			
			if (form == Form.GERUND)		// gerund - use ING form
				list = frontVG.getPresentParticiple();
			else if (mood == Mood.SUBJUNCTIVE)
				list = frontVG.getSubjunctive();	
			else if (!form.allowsTense())
				list.add(frontVG.getBaseForm());
			else
			{
				list.add(new AnchorString(lex.getVerbForm(frontVG.getBaseForm().toString(), 
					tense, person, number), frontVG.getBaseForm().getAnchor()));
			}
		
			for (int i = list.size(); i > 0; i--)
				restVG.push(list.get(i - 1));
		}
		
		// add modal, and we're done
		for (int i = 0; i < actualModal.size(); i++)
			restVG.push(actualModal.get(i));

		return restVG;
	}
}
