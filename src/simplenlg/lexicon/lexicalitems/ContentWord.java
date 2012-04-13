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

/**
 * This is an abstract implementation of the {@link simplenlg.lexicon.lexicalitems.LexicalItem} interface,
 * which implements a number of additional simplenlg.features inherited by the classes {@link Noun}, {@link Verb}, 
 * {@link Adjective} and {@link Symbol}. Every <code>ContentWord</code> has the following fields:
 * <p><code>String baseForm</code>: This is a string with which the <code>ContentWord</code> is initialised. 
 * <p> <code>Category category</code>: This is the grammatical Category, an element of the <code>enum</code> 
 * {@link simplenlg.lexicon.lexicalitems.Category}.
 * 
 *   @see simplenlg.lexicon.lexicalitems.LexicalItem
 *   @see simplenlg.lexicon.lexicalitems.Noun 
 *   @see simplenlg.lexicon.lexicalitems.Adjective
 *   @see simplenlg.lexicon.lexicalitems.Verb
 *   @see simplenlg.lexicon.lexicalitems.Symbol
 *   @author agatt
 */
public abstract class ContentWord implements LexicalItem, Cloneable {
	protected Anchor anchor;
	
	protected String baseForm;

	protected Category category;
	
	protected Object semantics;

	/**
	 * Constructs a new (empty) instance of <code>ContentWord</code>	
	 */
	public ContentWord() {
		super();
		baseForm = null;		
	}
	
	/**
	 * Constructs a new instance of ContentWord, whose baseform is <code>baseform</code>.	 
	 * @param baseform The baseform of this lexical item (a word)
	 */
	public ContentWord(String baseform) {
		baseForm = baseform;		
	}
	
	public void setAnchor(Anchor a)
	{
		anchor = a;
	}
	
	public Anchor getAnchor()
	{
		return anchor;
	}
	
	/** 
	 * @return the baseform of this <code>ContentWord</code>
	 */
	public AnchorString getBaseForm() {
		return new AnchorString(baseForm, anchor);
	}

	/**
	 * @return the category of this <code>ContentWord</code>, 
	 * an instance of {@link simplenlg.lexicon.lexicalitems.Category}
	 */
	public Category getCategory() {
		return this.category;
	}
	
	/**
	 * Returns a String representation of the ContentWord, essentially, this is
	 * just its baseform
	 * @return The String representation
	 */
	public String toString() {
		return baseForm;
	}

	/**
	 * Clones this instance of <code>ContentWord</code>. 
	 * @return A field-by-field copy of this ContentWord
	 */
	public ContentWord clone() {
		try {
			return (ContentWord) super.clone();
		} catch (CloneNotSupportedException cnse) {
			throw new InternalError(cnse.getMessage());
		}
	}

}
