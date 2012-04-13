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

import java.util.Iterator;

import simplenlg.lexicon.lexicalitems.Category;
import simplenlg.lexicon.lexicalitems.LexicalItem;
import simplenlg.lexicon.lexicalrules.rulesets.VerbLists;

public class VerbInflectionRule extends InflectionRule {

	private boolean doublingApplies = false;	
	
	public VerbInflectionRule() {
		super();
		applicableCategory = Category.VERB;
		doublingApplies = false;
	}

	public VerbInflectionRule(String ruleName) {
		super(ruleName, Category.VERB);
		doublingApplies = false;
	}

	public VerbInflectionRule(String ruleName, boolean applyDoubling) {
		super(ruleName, Category.VERB);
		doublingApplies = applyDoubling;
	}
	
	public VerbInflectionRule( String ruleName, boolean applyDoubling, PatternActionRule defRule ) {
		super( ruleName, Category.VERB, defRule );
		doublingApplies = applyDoubling;
	}

	public VerbInflectionRule(String ruleName, boolean applyDoubling, PatternActionRule[] rules) {
		super(ruleName, Category.VERB, rules);
		doublingApplies = applyDoubling;
	}

	public VerbInflectionRule(String ruleName, boolean applyDoubling, PatternActionRule[] rules,
			PatternActionRule defRule ) {
		super(ruleName, Category.VERB, rules, defRule );
		doublingApplies = applyDoubling;
	}
	
	
	public void setApplyDoubling(boolean doubling) {
		doublingApplies = doubling;
	}

	public boolean appliesDoubling() {
		return doublingApplies;
	}

	private String doubleFinalConsonant(String word) {
		StringBuffer buffer = new StringBuffer(word);
		buffer.append(buffer.charAt(buffer.length() - 1));
		return buffer.toString();
	}

	public String apply(LexicalItem lex) {

		String result = null;

		if (lex.getCategory() == Category.VERB) {
			result = apply(lex.getBaseForm().toString());
		}

		return result; //new AnchorString(result, lex.getBaseForm().getAnchor());
	}

	public String apply(String word) {
		wordMatcher.reset( word );
		
		if( !wordMatcher.matches() ) {
			return word;
		}
		
		String result = null;		
		Iterator<PatternActionRule> iter = patternActionRules.iterator();

		while (iter.hasNext()) {
			PatternActionRule currentRule = iter.next();

			if (currentRule.applies(word)) {
				result = currentRule.fire(word);
				break;
			}
		}

		if (result == null && defaultRule != null ) {
			
			if( defaultRule.applies(word ) ) {
				
				if (doublingApplies && VerbLists.CONS_DOUBLING.contains(word)) {
					word = doubleFinalConsonant(word);	
				}				
			}
			
			result = defaultRule.fire( word );
		}

		return result;
	}

}
