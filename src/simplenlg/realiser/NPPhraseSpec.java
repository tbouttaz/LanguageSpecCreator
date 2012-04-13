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
import java.util.Arrays;
import java.util.List;

import simplenlg.features.Person;

//import lexicon.Lexicon;

/**
 * NPPhraseSpec is a very simple syntactic
 * representation of a noun phrase.
 * <P>
 * It consists of
 * <UL>
 * <LI> determiner
 * <LI> (head) modifier(s) (such as adjectives)
 * <LI> noun
 * <LI> (end) modifier(s) (such as prepositional phrases)
 * </UL>
 * These are linearised in the above order
 * <P>
 * Also various simplenlg.features:
 * <UL>
 * <LI> elided - realise as empty string
 * <LI> plural - NP is plural, inflect head appropriately
 * <LI> treatAsPlural - NP is plural, but do not inflect head
 * </UL>
 * 
 * @author ereiter
 * adapted by fhielkema December 2006
 *
 */
public class NPPhraseSpec extends SyntaxPhraseSpec {

	// pronouns
	public static final String I = "i";
	public static final String ME = "me";	
	public static final String YOU = "you";
	public static final String HE = "he";
	public static final String SHE = "she";	
	public static final String HER = "her";
	public static final String HIM = "him";	
	public static final String IT = "it";
	public static final String WE = "we";	
	public static final String US = "us";
	public static final String THEY = "they";
	public static final String THEM = "them";
	public static final String THERE = "there";
	public static final String THAT = "that";
	public static final String WHICH = "which";
	public static final String WHO = "who";
	
	public static final String[] PRONOUNS = {I, ME, YOU, HE, SHE,
		HER, HIM, IT, WE, US, THEY, THEM, THERE, THAT, WHICH, WHO};
	public final static List<String> PRONOUN_LIST = Arrays.asList(PRONOUNS);

	// components
	private PhraseSpec determiner;   // eg, the, a, your, ...
	private List<PhraseSpec> headModifiers;  // red, first
	private List<PhraseSpec> endModifiers;  // eg, with a red face
	private PhraseSpec noun;
	private String id;
	
	// flags
	private boolean elided;		// realise as empty string
	private boolean genitive;		//realise as genitive case
	private boolean accusative;
	private boolean genitiveDeterminer;
	private boolean pronoun;	// this is a pronoun
	private boolean plural;		// make this plural
	private boolean singular;		//apocryphal; it has to be possible to prevent names ending in s being seen as plural
	private boolean treatAsPlural;	// treat as plural
	private boolean quote;
	private boolean subjectNP;	// is subject (set during realisation)
	private boolean rank;	//a rankordered number (e.g. first, second..)
	private boolean modPlural;
	
	// constructors *****************************************
	public NPPhraseSpec()
	{
		super();
		determiner = new StringPhraseSpec("");
		endModifiers = new ArrayList<PhraseSpec>();
		headModifiers = new ArrayList<PhraseSpec>();
		elided = false;
		pronoun = false;
		plural = false;
		singular = false;
		accusative = false;
		quote = false;
		noun = new StringPhraseSpec("");
		genitive = false;
		treatAsPlural = false;
		subjectNP = false;
		rank = false;
		modPlural = false;
	}
	
	public NPPhraseSpec(String id) 
	{
		this();
		this.id = id;
	}
	
	public NPPhraseSpec(PhraseSpec noun){
		this();
		this.noun = noun;
		if (PRONOUN_LIST.contains(noun.getHead().toLowerCase()))
			pronoun = true;
	}
	
	public NPPhraseSpec(PhraseSpec determiner, PhraseSpec noun){
		this(noun);
		this.determiner = determiner;
	}

	// getters/setters *****************************************
	
	public void addModifier(Object modifier) {
		// put new front endModifiers at beginning of list
		if (modifier != null && modifier instanceof String &&
				!((String)modifier).contains(" "))
			addHeadModifier(modifier);
		else
			addEndModifier(modifier);
	}
	
	/** add a modifier before the head (eg, adj) (in addition to existing modifiers) */
	public void addHeadModifier(Object modifier) {
		headModifiers.add(makePhraseSpec(modifier));
	}

	public List<PhraseSpec> getHeadModifiers()
	{
		return headModifiers;
	}

	public void addEndModifier(Object modifier) {
		endModifiers.add(makePhraseSpec(modifier));
	}

	public void setDeterminer(PhraseSpec spec) {
		this.determiner = spec;
	}
	
	public PhraseSpec getDeterminer()
	{
		return determiner;
	}
	
	public void setNoun(PhraseSpec spec) {
		noun = spec;
		if (PRONOUN_LIST.contains(noun.getHead().toLowerCase()))
			pronoun = true;
		if (noun instanceof AggregatePhraseSpec)
			modPlural = true;
	}
	
	public void setElided(boolean elided) {
		this.elided = elided;
	}

	public void setPlural(boolean plural) {
		this.plural = plural;
		this.singular = !plural;
	}
	
	public void setGenitive(boolean g)
	{
		genitive = g;
		if (noun != null)
			noun.setGenitive(g);
	}
	
	public void setRankOrdered(boolean r)
	{
		rank = r;
		if (noun != null)
			noun.setRankOrdered(r);
	}
	
	public boolean isGenitive()
	{
		return genitive;
	}
	
	public void setGenitiveDeterminer(boolean g)
	{
		genitiveDeterminer = g;
		if (determiner != null)
			determiner.setGenitive(g);
	}
	
	public boolean isGenitiveDeterminer()
	{
		return genitiveDeterminer;
	}
	
	public void setSingular(boolean s)
	{
		singular = s;
		plural = !s;
	}
	
	public void setAccusative(boolean a)
	{
		accusative = a;
	}
	
	public void setQuote(boolean q)
	{
		quote = q;
	}

	public boolean isPronoun()
	{
		return pronoun;
	}

	public void setPronoun(boolean pronoun) {
		// set pronoun flag
		this.pronoun = pronoun;
	}

	public void setPronoun(String pronoun) {
		// specify the actual pronoun
		noun = new StringPhraseSpec(pronoun);
		if (PRONOUN_LIST.contains(pronoun.toLowerCase()))
			this.pronoun = true;
		else
			throw new IllegalArgumentException("Argument not a pronoun: " + pronoun);
	}

	public void setTreatAsPlural(boolean treatAsPlural) {
		this.treatAsPlural = treatAsPlural;
	}

	boolean isSubjectNP() {
		return subjectNP;
	}

	void setSubjectNP(boolean subjectNP) {
		this.subjectNP = subjectNP;
	}

	// ************************* realise method ******************
	@Override
	public List<AnchorString> realise(Realiser r) {
		// do nothing if elided
		if (elided)
		{
			List<AnchorString> l = new ArrayList<AnchorString>();
			l.add(new AnchorString("", null));
			return l;
		}
		
		String result = new String();
		String n = noun.getHead();
		// if pronoun, return just pronoun, corrected for subject/object
		if (pronoun && !(noun instanceof AggregatePhraseSpec)) 
		{	//we'd lose the anchors if we used a pronoun for an aggregate spec
			if (genitive)
			{
				if (n.equalsIgnoreCase("i") || n.equalsIgnoreCase("me"))
					result = "my";
				else if (n.equalsIgnoreCase("we") || n.equalsIgnoreCase("us"))
					result = "our";
				else if (n.equalsIgnoreCase("he") || n.equalsIgnoreCase("him"))
					result = "his";
				else if (n.equalsIgnoreCase("she") || n.equalsIgnoreCase("her"))
					result = "her";
				else if (n.equalsIgnoreCase("they") || n.equalsIgnoreCase("them"))
					result = "their";
				else if (n.equalsIgnoreCase("it"))
					result = "its";
				else if (n.equalsIgnoreCase("which") || n.equalsIgnoreCase("that") || n.equalsIgnoreCase("who"))
					result = "whose";
				else
					result = n;
			}
			else if (accusative)
			{
				if (n.equalsIgnoreCase("who"))	//'by whom'
					result = "whom";
				else if (n.equalsIgnoreCase("he"))
					result = "him";
				else if (n.equalsIgnoreCase("she"))
					result = "her";
				else if (n.equalsIgnoreCase("they"))
					result = "them";
				else if (n.equalsIgnoreCase("i"))
					result = "me";	
				else
					result = n;	
			}
			else if (subjectNP) 
			{
				if (n.equalsIgnoreCase("i") || n.equalsIgnoreCase("me"))
					result = "I";
				else if (n.equalsIgnoreCase("we") || n.equalsIgnoreCase("us"))
					result = "we";
				else if (n.equalsIgnoreCase("he") || n.equalsIgnoreCase("him"))
					result = "he";
				else if (n.equalsIgnoreCase("she") || n.equalsIgnoreCase("her"))
					result = "she";
				else if (n.equalsIgnoreCase("they") || n.equalsIgnoreCase("them"))
					result = "they";
				else
					result = n;
			}
			else
			{
				if (n.equalsIgnoreCase("i") || n.equalsIgnoreCase("me"))
					result = "me";
				else if (n.equalsIgnoreCase("we") || n.equalsIgnoreCase("us"))
					result = "us";
				else if (n.equalsIgnoreCase("he") || n.equalsIgnoreCase("him"))
					result = "him";
				else if (n.equalsIgnoreCase("she") || n.equalsIgnoreCase("her"))
					result = "her";
				else if (n.equalsIgnoreCase("they") || n.equalsIgnoreCase("them"))
					result = "them";
				else
					result = n;
			}
			
			List<AnchorString> l = new ArrayList<AnchorString>();
			if (id != null)
				result = new String(result + " (" + id + ")");
			l.add(new AnchorString(result, noun.getAnchor()));
			return flash(l);
		}
		
		// compute components
		List<AnchorString> nounText = new ArrayList<AnchorString>();
		if (plural)
			noun.setSingular(false);
	
		noun.setRankOrdered(rank);
		noun.setGenitive(genitive);
		nounText.addAll(noun.realise(r));
		for (int i = 0; i < headModifiers.size(); i++)
			headModifiers.get(i).setSingular(!modPlural);
		
		List<AnchorString> modifierText = r.realiseList(headModifiers);
		List<AnchorString> endModifierText = r.realiseList(endModifiers);
		List<AnchorString> resultList = r.listWords(modifierText, nounText, endModifierText);
			
		// process determiners - "a"/"an" is special case
		if (genitiveDeterminer)
			determiner.setGenitive(true);
		
		List<AnchorString> determinerText = determiner.realise(r);
		if (determiner instanceof StringPhraseSpec)
		{
			AnchorString detAnchor = determinerText.get(0);
			String det = detAnchor.toString();	
			if (det.equalsIgnoreCase("a") || det.equalsIgnoreCase("an")) {
				if (plural || treatAsPlural)		// zap if plural
					detAnchor.setString("");
				else {
					char firstChar = resultList.get(0).toString().charAt(0);
					if (!Character.isLowerCase(firstChar));	// don't figure out dets for acronyms
					else if (firstChar == 'a' || firstChar == 'e' || firstChar == 'i'
						|| firstChar == 'o'/* || firstChar == 'u'*/)
						detAnchor.setString("an");
					else
						detAnchor.setString("a");
				}
			}
		}
		
		List<AnchorString> list;
		if (getAnchor() == null)
		{
			list = r.listWords(determinerText, resultList);
			if (quote)
			{
				list.get(0).setString("\"" + list.get(0).toString());
				list.get(list.size() - 1).setString(list.get(list.size() - 1).toString() + "\"");
			}
			if (id != null)
				list.add(new AnchorString(" (" + id + ")", null));
		}
		else
		{
			AnchorString as = new AnchorString(r.listWords(determinerText, resultList), getAnchor());	
			list = new ArrayList<AnchorString>();
			if (quote)
				as.setString("\"" + as.toString() + "\"");
			if (id != null)
				as.setString(as.toString() + " (" + id + ")");			
			list.add(as);
		}	
		return flash(list);
	}

	@Override
	public String getHead() {
		return noun.toString();
	}

	/** return person (1, 2, 3).	 */
	@Override
	public Person getPerson() {	
		String n = noun.getHead();	
		if (!pronoun)
			return Person.THIRD;
		else if (n.equalsIgnoreCase("you"))
			return Person.SECOND;
		else if (n.equalsIgnoreCase("i") || n.equalsIgnoreCase("me") ||
				(n.equalsIgnoreCase("we") || n.equalsIgnoreCase("us")))
			return Person.FIRST;
		else
			return Person.THIRD;
	}

	@Override
	public boolean isPlural() {	//Lexicon lex) {
		if (plural || treatAsPlural)
			return true;
		if (singular)
			return false;
		else
			return false;
			//return lex.isPlural(noun.toString());
	}

}
