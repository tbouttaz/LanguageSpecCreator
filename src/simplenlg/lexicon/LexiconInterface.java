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


package simplenlg.lexicon;

import simplenlg.lexicon.lexicalitems.Category;
import simplenlg.lexicon.lexicalitems.LexicalItem;

/**
 * This is the basic interface implementing a Lexicon. A <code>Lexicon</code> is conceived as a 
 * repository of <code>LexicalItem</code>s, each of which is defined for a finite number of morphological
 * operations, depending on its grammatical category. The interface defines a few minimal requirements, mainly
 * related to the addition and retrieval of items from the <code>Lexicon</code>.
 * 
 * @author agatt
 */
public interface LexiconInterface {

	/**
	 * Add a <code>LexicalItem</code> to this <code>Lexicon</code>
	 * @param lex The <code>LexicalItem<code> to be added.
	 * @see simplenlg.lexicon.lexicalitems.LexicalItem  
	 */
	void addItem( LexicalItem lex );
	
	/**
	 * Add an item to this <code>Lexicon</code> by passing it a word and a <code>Category</code>
	 * The <code>Lexicon</code> will construct a new item, depending on the category passed.
	 * @param cat The <code>Category</code>
	 * @param word The baseform of the lexical item.
	 * @see simplenlg.lexicon.lexicalitems.Category
	 */
	void addItem( Category cat, String word );
	
	/**
	 * Get the <code>LexicalItem</code> with baseform <code>word</code>
	 * @param word The baseform
	 * @return The <code>LexicalItem</code> with the baseform <code>word</code> 
	 * 			if it is contained in this simplenlg.lexicon, <code>null</code> otherwise. 
	 */
	LexicalItem getItem( String word );
	
	/**
	 * Get the <code>LexicalItem</code> with baseform <code>word</code> and <code>Category</code> cat 
	 * @param cat The <code>Category</code>
	 * @param word The baseform
	 * @return The <code>LexicalItem</code> with the baseform <code>word</code> and
	 * 			Category <code>cat</code> if it is contained in this simplenlg.lexicon, <code>null</code> otherwise. 
	 */
	LexicalItem getItem( Category cat, String word );
	
	/**
	 * Check whether this <code>Lexicon</code> contains a <code>LexicalItem</code> of <code>Category</code> cat
	 * with baseform <code>word</code>
	 * @param cat The <code>Category</code>
	 * @param word The baseform
	 * @return <code>true</code> if the <code>Lexicon</code> contains an item with this <code>Category</code> and this
	 * 			baseform.
	 */
	boolean hasItem( Category cat, String word );
	
	/**
	 * Check whether this <code>Lexicon</code> contains a <code>LexicalItem</code> with baseform <code>word</code>.	
	 * @param word The baseform
	 * @return <code>true</code> if the <code>Lexicon</code> contains an item with this this
	 * 			baseform.
	 */
	boolean hasItem( String word );
}
