/* ==========================================
 * Lexicon Generation API
 * ==========================================
 *
 * Copyright (c) 2007, the University of Aberdeen
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
 *     
 *	   =================    
 *     Acknowledgements:
 *     =================
 *     This library contains a re-implementation of some rules derived from the MorphG package
 *     by Guido Minnen, John Carroll and Darren Pearce. You can find more information about MorphG
 *     in the following reference:
 *     	Minnen, G., Carroll, J., and Pearce, D. (2001). Applied Morphological Processing of English.
 *     		Natural Language Engineering 7(3): 207--223.
 *     Thanks to John Carroll for permission to re-use the MorphG rules. 
 */

package simplenlg.lexicon.lexicalitems;

import java.util.ArrayList;
import java.util.List;

import simplenlg.exception.LexiconException;
import simplenlg.features.Number;
import simplenlg.features.Person;
import simplenlg.lexicon.lexicalrules.rulesets.VerbInflection;
import simplenlg.realiser.AnchorString;

/**
 * This class extends the {@link simplenlg.lexicon.lexicalitems.ContentWord}
 * abstract class for verbs. Any instance of <code>Verb</code> is defined for
 * the following inflectional rules:
 * <p>
 * past participle ( {@link Verb#getPastParticiple()} )
 * <p>
 * present tense ( {@link Verb#getPast() } )
 * <p>
 * present participle ("ing" form) ( {@link Verb#getPresentParticiple()} )
 * <p>
 * past tense ( {@link Verb#getPresent3SG() } )
 * 
 * @author agatt
 * @adapted by fhielkema
 */
public class Verb extends ContentWord {

	private boolean consonantDoubling = false;

	private boolean isNullAffixVerb = false;

	private String pastParticipleForm;

	private String pastTenseForm;

	private String presentParticipleForm;

	private String present_3SG;

	// TODO Change this to a Lexical Item
	private AnchorString particle; // for phrasal verbs;

	/**
	 * Constructs a new instance of Verb
	 * 
	 * @param baseform
	 *            The base form of this verb, possibly consisting of verb and
	 *            particle separated by a space. If the baseform does contain a
	 *            space between two substrings (e.g. "get up"), it is assumed to
	 *            be a phrasal verb.
	 * @see Verb#Verb(String, String)
	 */
	public Verb(String baseform) {
		super();		
		
		if (baseform.indexOf(" ") > 0) {
			int spaceIndex = baseform.indexOf(" ");
			String p = baseform.substring(spaceIndex + 1);
			
			if (p.length() == 0 || baseform.length() == 0
					|| p.indexOf(" ") > 0) {
				throw new LexiconException("Impossible verb: " + baseform);
			}
			
			if( !p.equals("") ) {
				this.particle = new AnchorString(p, null);				
			}
			
			this.baseForm = baseform.substring(0, spaceIndex);

		} else {
			this.baseForm = baseform;
		}

		category = Category.VERB;
	}

	/**
	 * Constructs a new instance of <code>Verb</code>
	 * 
	 * @param baseform
	 *            The baseform
	 * @param part
	 *            The particle (assuming that this is a phrasal verb)
	 */
	public Verb(String baseform, AnchorString part) {
		this(baseform);
		this.particle = part;
	}

	/**
	 * Make this a phrasal verb, consisting of the baseform and a
	 * (prepositional) particle
	 * 
	 * @param part
	 *            The <code>java.lang.String</code> particle
	 * @see Verb#setParticle(LexicalItem)
	 */
	public void setParticle(AnchorString part) {
		particle = part;
	}

	/**
	 * Make this a phrasal verb with a prepositional particle.
	 * 
	 * @param part
	 *            The <code>LexicalItem</code> particle
	 */
	public void setParticle(LexicalItem part) {
		particle = part.getBaseForm();
	}

	// TODO alter this to also include a version with LexicalItem
	public AnchorString getParticle() {
		return particle;
	}

	@Override
	public AnchorString getBaseForm() {	//EVENTUEEL ANCHOR VAN PARTICLE KAN VERLOREN GAAN!!
		List<AnchorString> l = render(baseForm);
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < l.size(); i++)
			sb.append(l.get(i).toString());
		return new AnchorString(sb.toString(), getAnchor());
//		return render( baseForm );
	}
	
	/**
	 * 
	 * @return <code>true</code> if this verb has a particle
	 */
	public boolean isPhrasalVerb() {
		return (particle != null);
	}
	
	/**
	 * 
	 * @return <code>true</code> if this is the verb "to be"
	 */
	public boolean isCopular() {
		return baseForm.equalsIgnoreCase("be");
	}

	/**
	 * Set the "consonantDoubling" parameter for this <code>Verb</code>. If
	 * set to "true", this will mean that the final consonant of the verb is
	 * doubled whenever a rule applies where this is required.
	 * 
	 * @param consDoubling
	 *            Whether this <code>Verb</code> takes consonant doubling or
	 *            not.
	 */
	public void setConsonantDoubling(boolean consDoubling) {
		consonantDoubling = consDoubling;
	}

	/**
	 * Set the "isNullAffixVerb" parameter for this verb. If set to "true", this
	 * means that the verb is never inflected in any form. Mostly used for the
	 * modal verbs "would", "should" etc.
	 * 
	 * @param neverInflected
	 *            whether this verb is ever inflected in any form
	 */
	public void setIsNullAffixVerb(boolean neverInflected) {
		if (neverInflected) {
			pastParticipleForm = baseForm;
			pastTenseForm = baseForm;
			present_3SG = baseForm;
			presentParticipleForm = baseForm;
		}

		isNullAffixVerb = neverInflected;
	}

	/**
	 * Check if this verb ever takes inflections.
	 * 
	 * @return <code>true</code> if this is a verb specified as never taking
	 *         inflections in any form.
	 */
	public boolean isNullAffixVerb() {
		return isNullAffixVerb;
	}

	/**
	 * Check if this verb takes consonant doubling.
	 * 
	 * @return <code>true</code> if this verb takes consonant doubling
	 */
	public boolean hasConsonantDoubling() {
		return consonantDoubling;
	}

	/**
	 * Processes the baseform of this <code>Verb</code> to obtain its "en"
	 * form. The form is stored in a field the first time this method is called,
	 * thus reducing processing overhead on subsequent calls.
	 * <p>
	 * This method involves a call to
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbInflection#PAST_PARTICIPLE}
	 * in package {@link simplenlg.lexicon.lexicalrules.rulesets}.
	 * 
	 * @return The past participle form (a <code>String</code>)
	 */
	public List<AnchorString> getPastParticiple() {

		if (pastParticipleForm == null) {
			pastParticipleForm = VerbInflection.PAST_PARTICIPLE.apply(baseForm);
		}

		return render(pastParticipleForm);
	}

	/**
	 * Sets the past participle form of this verb. Useful to override the
	 * default behaviour from the in-built rules.
	 * 
	 * @param ppart -
	 *            The past participle form
	 */
	public void setPastParticiple(String ppart) {
		pastParticipleForm = ppart;
	}

	/**
	 * Processes the baseform of this <code>Verb</code> to obtain its "ing"
	 * form. The form is stored in a field the first time this method is called,
	 * thus reducing processing overhead on subsequent calls.
	 * <p>
	 * This method involves a call to
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbInflection#ING_FORM}
	 * in package {@link simplenlg.lexicon.lexicalrules.rulesets}.
	 * 
	 * @return The "ing" form (a <code>String</code>).
	 */
	public List<AnchorString> getPresentParticiple() {

		if (presentParticipleForm == null) {
			presentParticipleForm = VerbInflection.ING_FORM.apply(baseForm);
		}
		return render(presentParticipleForm);
	}

	/**
	 * Sets the present participle form of this verb. Useful to override the
	 * default behaviour from the in-built rules.
	 * 
	 * @param presPart -
	 *            The past participle form
	 */
	public void setPresentParticiple(String presPart) {
		presentParticipleForm = presPart;
	}

	/**
	 * Processes the baseform of this <code>Verb</code> to obtain its past
	 * tense form. In most cases The form is stored in a field the first time
	 * this method is called, thus reducing processing overhead on subsequent
	 * calls. This method involves a call to
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbInflection#PAST_TENSE}
	 * in package {@link simplenlg.lexicon.lexicalrules.rulesets}.
	 * 
	 * @return The past tense form (a <code>String</code>).
	 */
	public List<AnchorString> getPast() {

		if (pastTenseForm == null) {
			pastTenseForm = VerbInflection.PAST_TENSE.apply(baseForm);
		}
		return render(pastTenseForm);
	}

	/**
	 * Sets the past form of this verb. Useful to override the default behaviour
	 * from the in-built rules.
	 * 
	 * @param pst -
	 *            The past form
	 */
	public void setPast(String pst) {
		pastTenseForm = pst;
	}

	/**
	 * Processes the baseform of this <code>Verb</code> to obtain its Third
	 * Person Singular Present form.
	 * <p>
	 * The form is stored in a field the first time this method is called, thus
	 * reducing processing overhead on subsequent calls.
	 * <p>
	 * This method involves a call to
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbInflection#PRESENT_TENSE}
	 * in package {@link simplenlg.lexicon.lexicalrules.rulesets}.
	 * 
	 * @return The 3rd person present tense form (a <code>String</code>).
	 */
	public List<AnchorString> getPresent3SG() {

		if (present_3SG == null) {
			present_3SG = VerbInflection.PRESENT_TENSE.apply(baseForm);
		}
		return render(present_3SG);
	}

	/**
	 * Sets the 3rd person present form of this verb. Useful to override the
	 * default behaviour from the in-built rules.
	 * 
	 * @param pres -
	 *            The present tense form
	 */
	public void setPresent3SG(String pres) {
		present_3SG = pres;
	}

	/**
	 * Processes the baseform of this <code>Verb</code> to obtain the present
	 * tense form for a specific <code>Person</code> and <code>Number</code>
	 * combination. The default behaviour is to assume that all present tense
	 * forms except for 3rd person singular are identical to the baseform.
	 * <p>
	 * If using {@link simplenlg.lexicon.Lexicon}, or any of its subclasses,
	 * this comes pre-loaded with a list of exceptions which take suppletive
	 * forms in some person/number combinations. E.g. the verb "to be" has
	 * different forms for 1st, 2nd and so on.
	 * <p>
	 * The form is stored in a field the first time this method is called, thus
	 * reducing processing overhead on subsequent calls. This method involves a
	 * call to
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbInflection#PRESENT_TENSE}
	 * in package {@link simplenlg.lexicon.lexicalrules.rulesets}.
	 * 
	 * @return The present tense form of a specific PersonNumber configuration
	 *         (a <code>String</code>).
	 */
	public List<AnchorString> getPresent(Person p, Number n) {
		if (p == Person.THIRD && n == Number.SINGULAR) {
			return getPresent3SG();

		} else if (baseForm.equalsIgnoreCase("be")) {

			if (n == Number.SINGULAR) {

				switch (p) {
				case FIRST:
					return render("am");

				case SECOND:
					return render("are");

				case THIRD:
					return render("is");

				default:
					{
						List<AnchorString> l = new ArrayList<AnchorString>();
						l.add(new AnchorString("", getAnchor())); // makes java happy -- not needed
						return l;
					} 
				}

			} else {
				return render("are");
			}

		} else {
			return render(baseForm);
		}
	}

	/**
	 * Processes the baseform of this <code>Verb</code> to obtain the past
	 * tense form for a specific <code>PersonNumber</code> combination. 
	 * <p>
	 * If using {@link simplenlg.lexicon.Lexicon}, or any of its subclasses,
	 * this comes pre-loaded with a list of exceptions which take suppletive
	 * forms in some person/number combinations. E.g. the verb "to be" has
	 * different forms for 1st, 2nd and so on.
	 * <p>
	 * The form is stored in a field the first time this method is called, thus
	 * reducing processing overhead on subsequent calls. This method involves a
	 * call to
	 * {@link simplenlg.lexicon.lexicalrules.rulesets.VerbInflection#PAST_TENSE}
	 * in package {@link simplenlg.lexicon.lexicalrules.rulesets}.
	 * 
	 * @return The past tense form of a specific PersonNumber configuration (a
	 *         <code>String</code>).
	 */
	public List<AnchorString> getPast(Person p, Number n) {
		if (baseForm.equalsIgnoreCase("be")) {

			if (n == Number.SINGULAR) {

				switch (p) {
				case FIRST:
				case THIRD:
					return render( "was" );

				case SECOND:
					return render( "were" );

				default:
					{
						List<AnchorString> l = new ArrayList<AnchorString>();
						l.add(new AnchorString("", getAnchor())); // makes java happy -- not needed
						return l;
					} 
				}

			} else {
				return render( "were" );
			}

		} else {
			return render(getPast());
		}
	}
	
	/**
	 * 
	 * @return The subjunctive form of the verb (i.e. the one used in conditionals).  
	 */
	public List<AnchorString> getSubjunctive() {
		
		if( baseForm.equalsIgnoreCase("be") ) {
			return render( "were" );
		
		} else {
			return render( getPast() );
		}
	}
	
	/**
	 * 
	 * @return The infinitive form (i.e. "to " followed by the base form and particle if any) 
	 */
	public List<AnchorString> getInfinitive() {
		List<AnchorString> result = new ArrayList<AnchorString>();
		result.add(new AnchorString("to", null));
		result.addAll(render(baseForm));
		return result;
	}

	// Return a form, adding a particle to a morphologically inflected form if
	// we have one
	List<AnchorString> render(String morphForm) {
		List<AnchorString> result = new ArrayList<AnchorString>();
		result.add(new AnchorString(morphForm, getAnchor()));
		if (isPhrasalVerb())
		{
			result.add(new AnchorString(" ", null));
			result.add(particle);
		}
		return result;
//		return (isPhrasalVerb() ? morphForm + " " + particle : morphForm);
	}

	List<AnchorString> render(List<AnchorString> morphForm) {
		if (isPhrasalVerb())
		{
			morphForm.add(new AnchorString(" ", null));
			morphForm.add(particle);
		}
		return morphForm;
//		return (isPhrasalVerb() ? morphForm + " " + particle : morphForm);
	}

}
