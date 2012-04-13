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

import simplenlg.features.Number;
import simplenlg.features.Person;
import simplenlg.realiser.AnchorString;
import WYSIWYM.model.Anchor;

/**
 * This <code>enum</code> extends the {@link simplenlg.lexicon.lexicalitems.ContentWord} abstract class 
 * for the personal pronouns. 
 * 
 * @author agatt
 */

public enum Pronoun implements LexicalItem {

	PRO_1SG, PRO_2SG, PRO_3SGM, PRO_3SGF, PRO_1PL, PRO_2PL, PRO_3PL, PRO_3SG;

	private Anchor anchor;

	/**
	 * Get the {@link simplenlg.features.Person} feature of this <code>Pronoun</code>.
	 * E.g. If the <code>Pronoun</code> is Pronoun.PRO_1SG, this returns 
	 * <code>Person.FIRST</code>.  
	 * @return The Person constant appropriate for this pronoun.
	 */
	public Person getPerson() {

		switch (this) {
		
		case PRO_1SG:
		case PRO_1PL:
			return Person.FIRST;
			
		case PRO_2SG:
		case PRO_2PL:
			return Person.SECOND;
							
		default:
			return Person.THIRD;		
		}
	}
	
	/**
	 * Get the {@link simplenlg.features.Number} feature of this <code>Pronoun</code>.
	 * E.g. If the <code>Pronoun</code> is Pronoun.PRO_1SG, this returns 
	 * <code>Number.SINGULAR</code>.  
	 * @return The Number constant appropriate for this pronoun.
	 */
	public Number getNumber() {

		switch (this) {
		
		case PRO_1SG:
		case PRO_2SG:			
		case PRO_3SGM:
		case PRO_3SGF:
		case PRO_3SG:
			return Number.SINGULAR;
						
		default:
			return Number.PLURAL;
		}

	}

	public void setAnchor(Anchor a)
	{
		anchor = a;
	}

	/**
	 * @see simplenlg.lexicon.lexicalitems.LexicalItem#getBaseForm()
	 */
	public AnchorString getBaseForm() {
		String result = null;
		switch (this) {

		case PRO_1SG:
			result = "I";

		case PRO_2SG:
			result = "you";

		case PRO_3SGM:
			result = "he";

		case PRO_3SGF:
			result = "she";
			
		case PRO_3SG:
			result = "it";

		case PRO_1PL:
			result = "we";

		case PRO_2PL:
			result = "you";

		case PRO_3PL:
			result = "they";

	//	default:
	//		return null;
		}
		return new AnchorString(result, anchor);
	}

	/**
	 * @see simplenlg.lexicon.lexicalitems.LexicalItem#getCategory()
	 */
	public Category getCategory() {
		return Category.PRONOUN;
	}

	/**
	 * Utility method, which returns the constant defined in this <code>enum</code> corresponding
	 * to a particular word. For example, this returns <code>PRO_2SG</code> if passed the argument "you".
	 * @param word The string corresponding to the pronoun. Essentially, any of "I", "you", "they", "he", "she", "we". 
	 * Note that the method is NOT case-sensitive.
	 * @return The corresponding <code>Pronoun</code> in this <code>enum</code>
	 */
	public static Pronoun getPronoun( String word ) {
		
		if( word.equalsIgnoreCase( "i" ) ) {
			return PRO_1SG;
			
		} else if( word.equalsIgnoreCase( "you" ) ) {
			return PRO_2SG;
						
		} else if( word.equals( "we" ) ) {
			return PRO_2PL;
			
		} else if( word.equalsIgnoreCase( "they" ) ) {
			return PRO_3PL;
			
		} else if( word.equalsIgnoreCase( "he" ) ) {
			return PRO_3SGM;
			
		} else if( word.equalsIgnoreCase( "she" ) ) {
			return PRO_3SGF;
		
		} else if( word.equalsIgnoreCase( "it" ) ) {
			return PRO_3SG;
			
		} else {
			return null;
		}
	}	
	
}
