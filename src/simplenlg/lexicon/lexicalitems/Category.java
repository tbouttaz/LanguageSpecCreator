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
import java.util.regex.Pattern;

/**
 * This is an enumeration of the grammatical categories which are relevant to
 * the Lexicon package.
 * <p>
 * Any item that instantiates the {@link simplenlg.lexicon.lexicalitems.LexicalItem}
 * interface must implement the <code>getCategory()</code> method, which
 * returns a constant defined in this enum. The Category of a
 * <code>LexicalItem</code> also determines which (if any) morphological rules
 * can apply to it.
 * <p>
 * Recognised grammatical categories are:
 * <code>NOUN, ADJECTIVE, VERB, DETERMINER, PRONOUN, SYMBOL, ALL_CATEGORIES</code>
 * 
 * @author agatt
 */
public enum Category {
	NOUN, ADJECTIVE, VERB, DETERMINER, PRONOUN, CONJUNCTION, SYMBOL, ALL_CATEGORIES;

	private static final Pattern adjective = Pattern.compile( "^a(.*)", Pattern.CASE_INSENSITIVE );
	private static final Pattern noun = Pattern.compile( "^n(.*)", Pattern.CASE_INSENSITIVE );
	private static final Pattern verb = Pattern.compile( "^v(.*)", Pattern.CASE_INSENSITIVE );
	private static final Pattern pronoun = Pattern.compile( "^p(.*)", Pattern.CASE_INSENSITIVE );
	private static final Pattern determiner = Pattern.compile( "^d(.*)", Pattern.CASE_INSENSITIVE );
	private static final Pattern all = Pattern.compile( "^all", Pattern.CASE_INSENSITIVE );
	private static final Pattern conjunction = Pattern.compile( "^c(.*)", Pattern.CASE_INSENSITIVE );
	private static final Pattern symbol = Pattern.compile( "^sym(.*)", Pattern.CASE_INSENSITIVE );

	
	/**
	 * @return A string representation of the category. For example, if the
	 *         category is <code>NOUN</code>, this returns "N"
	 */
	public String getPartOfSpeech() {

		switch (this) {

		case NOUN:
			return "N";

		case VERB:
			return "V";

		case ADJECTIVE:
			return "ADJ";

		case DETERMINER:
			return "DET";

		case PRONOUN:
			return "PRO";

		case SYMBOL:
			return "SYM";
			
		case CONJUNCTION:
			return "CONJ";

		case ALL_CATEGORIES:
			return "N|V|ADJ|DET|PRO|CONJ|SYM";

		default:
			return null;
		}
	}

	
	/**
	 * Utility method: Returns the actual Category constant which matches the
	 * name of the category.
	 * 
	 * @param c The name of the category. Recognised names are "n(oun)", "adj(ective)",
	 *            "v(erb)", "d(et)", "p(ro)", "s(ymbol)" and "all".
	 * @throws <code>IllegalArgumentException</code> if the name passed as
	 *             argument is unrecognised.
	 * @return The Category whose name is <code>c</code>
	 */
	public static Category getCategory(String c)
			throws IllegalArgumentException {		
		
		if ( noun.matcher(c).matches()) {
			return NOUN;

		} else if (adjective.matcher(c).matches()) {
			return ADJECTIVE;

		} else if (verb.matcher(c).matches()) {
			return VERB;

		} else if (determiner.matcher(c).matches() ) {
			return DETERMINER;

		} else if (pronoun.matcher(c).matches() ) {
			return PRONOUN;

		} else if (symbol.matcher(c).matches() ) {
			return SYMBOL;
			
		} else if( conjunction.matcher(c).matches() ) {
			return CONJUNCTION;

		} else if( all.matcher(c).matches() ) {
			return ALL_CATEGORIES;
			
		}else {
			throw new IllegalArgumentException("Unknown category '" + c
					+ "' passed to Category.getCategory");
		}
	}	

}
