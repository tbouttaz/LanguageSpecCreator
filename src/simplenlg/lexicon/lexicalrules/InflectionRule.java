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


package simplenlg.lexicon.lexicalrules;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import simplenlg.lexicon.lexicalitems.Category;
import simplenlg.lexicon.lexicalitems.LexicalItem;
import simplenlg.lexicon.lexicalrules.rulesets.BasicPatterns;

/**
 *
 * Abstract implementation of <code>MorphologicalRule</code>. This is extended by
 * {@link NominalInflectionRule} and {@link VerbInflectionRule}.
 * 
 * <p>
 * Inflectional rules are conceived as containers for {@link PatternActionRule}s.
 * 
 *  @author agatt
 */
public abstract class InflectionRule implements MorphologicalRule {

	protected List<PatternActionRule> patternActionRules;
	
	protected PatternActionRule defaultRule;
	
	protected String name;

	protected Category applicableCategory;
	
	protected Matcher wordMatcher = Pattern.compile( BasicPatterns.ANY_STEM ).matcher( "blablabla");

	public InflectionRule() {
		patternActionRules = new ArrayList<PatternActionRule>();
		applicableCategory = Category.ALL_CATEGORIES;
	}

	public InflectionRule(String ruleName) {
		name = ruleName;
		applicableCategory = Category.ALL_CATEGORIES;
		patternActionRules = new ArrayList<PatternActionRule>();
	}

	public InflectionRule(String ruleName, Category cat) {
		name = ruleName;
		applicableCategory = cat;
		patternActionRules = new ArrayList<PatternActionRule>();
	}

	public InflectionRule( String ruleName, Category cat, PatternActionRule defRule ) {
		name = ruleName;
		applicableCategory = cat;
		patternActionRules = new ArrayList<PatternActionRule>();
		defaultRule = defRule;
	}
	
	public InflectionRule(String ruleName, Category cat, PatternActionRule[] ruleSet) {
		name = ruleName;
		applicableCategory = cat;
		patternActionRules = new ArrayList<PatternActionRule>(Arrays
				.asList(ruleSet));
		Collections.sort(patternActionRules);
	}

	public InflectionRule(String ruleName, PatternActionRule[] ruleSet) {
		name = ruleName;
		applicableCategory = Category.ALL_CATEGORIES;
		patternActionRules = new ArrayList<PatternActionRule>(Arrays
				.asList(ruleSet));
		Collections.sort(patternActionRules);
	}

	public InflectionRule(String ruleName, Category cat, PatternActionRule[] ruleSet, PatternActionRule defRule) {
		name = ruleName;
		defaultRule = defRule;
		applicableCategory = cat;
		patternActionRules = new ArrayList<PatternActionRule>(Arrays
				.asList(ruleSet));
		Collections.sort(patternActionRules);
	}

	public InflectionRule(String ruleName, PatternActionRule[] ruleSet, PatternActionRule defRule) {
		name = ruleName;
		defaultRule = defRule;
		applicableCategory = Category.ALL_CATEGORIES;
		patternActionRules = new ArrayList<PatternActionRule>(Arrays
				.asList(ruleSet));
		Collections.sort(patternActionRules);
	}
	
	
	public String getName() {
		return name;
	}

	public void setRuleName(String newName) {
		name = newName;
	}

	public boolean appliesTo(LexicalItem lex) {

		if (applicableCategory == Category.ALL_CATEGORIES
				|| lex.getCategory() == applicableCategory) {
			return true;
		}

		return false;
	}

	public boolean appliesTo(Category cat) {

		if (applicableCategory == Category.ALL_CATEGORIES
				|| cat == applicableCategory) {
			return true;
		}

		return false;
	}

	public void addRule(PatternActionRule par) {
		patternActionRules.add(par);
		Collections.sort( patternActionRules );
	}

	public int getNumberOfRules() {
		return patternActionRules.size();
	}

	/*
	public boolean analyse(String word) {

		Iterator<PatternActionRule> iter = patternActionRules.iterator();
		
		while (iter.hasNext()) {
			PatternActionRule currentRule = iter.next();
			if (currentRule.analyse(word)) {
				return true;
			}
		}

		return false;
	}*/

}
