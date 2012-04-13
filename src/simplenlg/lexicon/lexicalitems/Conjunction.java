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

import simplenlg.realiser.AnchorString;
import WYSIWYM.model.Anchor;

public enum Conjunction implements LexicalItem {

	AND("and", CoordType.COORDINATING), 
	OR("or", CoordType.COORDINATING), 
	BUT("but", CoordType.SUBORDINATING), 
	BECAUSE("because", CoordType.SUBORDINATING);

	//the "base form"
	private String baseForm;

	//the category
	private Category category;
	
	//the type of a particular conjunction
	private CoordType conjType;
	
	//the two kinds of conjunction: coordinating or subordinating
	private enum CoordType { SUBORDINATING, COORDINATING };
	
	private Anchor anchor;
	
	Conjunction(String form, CoordType t) {
		baseForm = form;
		category = Category.CONJUNCTION;
		conjType = t;
	}

	public void setAnchor(Anchor a)
	{
		anchor = a;
	}

	/**
	 * @see simplenlg.lexicon.lexicalitems.LexicalItem#getBaseForm()
	 */
	public AnchorString getBaseForm() {
		return new AnchorString(baseForm, anchor);
	}

	/**
	 * Get the <code>Conjunction</code> matching a <code>String</code>.
	 * @param s The <code>String</code>
	 * @return The conjunction
	 */	
	public static Conjunction getConjunction( String s ) throws simplenlg.exception.LexiconException {
		
		if( s.equalsIgnoreCase( "and" ) ) {
			return AND;
		
		} else if( s.equalsIgnoreCase("or" ) ) {
			return OR;
		
		} else if( s.equalsIgnoreCase("but" ) ) {
			return BUT;
		
		} else if( s.equalsIgnoreCase( "because" ) ) {
			return BECAUSE;
			
		} else {
			throw new simplenlg.exception.LexiconException( "No such conjunction: " + s);
		}
	}

	/**
	 * @see simplenlg.lexicon.lexicalitems.LexicalItem#getCategory()
	 */
	public Category getCategory() {
		return category;
	}
	
	/**
	 * 
	 * @return <code>true</code> if this is a Coordinating conjunction (such as "and")
	 */
	public boolean isCoordinating() {		
		return this.conjType==CoordType.COORDINATING;
	}
		
	/**
	 * @return <code>true</code> if this is a Subordinating conjunction (such as "but" and "because")
	 */
	public boolean isSubordinating() {		
		return this.conjType==CoordType.SUBORDINATING;
	}
	
}
