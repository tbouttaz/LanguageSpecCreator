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

import simplenlg.lexicon.lexicalitems.Category;
import simplenlg.lexicon.lexicalitems.LexicalItem;

/**
 * This is the basic interface that all rules must implement.
 * 
 * @author agatt
 *
 */
public interface MorphologicalRule {

	/**
	 * Apply this rule to a string.
	 * @param word The <code>String</code> to which the rule will apply.
	 * @return The <code>String</code> result of applying this rule.
	 */
	String apply( String word );
	
	/**
	 * Apply this rule to a <code>LexicalItem</code>.
	 * @param lex The <code>LexicalItem</code> to which the rule will apply.
	 * @return The <code>String</code> result of applying this rule.
	 */
	String apply( LexicalItem lex );
	
	/**
	 * Get the name of this rule. This is usually a <code>String</code> that
	 * is used for fast indexing when <code>MorphologicalRules</code> are added
	 * to a <code>Lexicon</code>, and enables them to be called by name.
	 * @return The name of this rule.
	 * @see simplenlg.lexicon.Lexicon#addRule(MorphologicalRule)
	 */
	String getName();
	
	/**
	 * Check whether this rule applies to items of <code>Category</code> cat
	 * @param cat the <code>Category</code>
	 * @return <code>true</code> if this rule is specified as applying to this <code>Category</code>, or
	 * to <code>Category.ALL_CATEGORIES</code>
	 * @see simplenlg.lexicon.lexicalitems.Category
	 */
	boolean appliesTo( Category cat );
	
	/**
	 * Check whether this rule applies to this <code>LexicalItem</code>. This should only
	 * return true if {@link #appliesTo(Category)} returns true when called with <code>lex.getCategory()</code>. 
	 * @param lex the <code>LexicalItem</code>
	 * @return <code>true</code> if this rule applies to <code>lex</code> 
	 * @see simplenlg.lexicon.lexicalitems.LexicalItem
	 */
	boolean appliesTo( LexicalItem lex );
}
