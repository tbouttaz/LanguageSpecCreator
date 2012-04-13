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
import simplenlg.features.Person;

/**
 * PhraseSpec represents a phrase specification
 * <P>
 * This follows the terminology of Reiter and Dale 2000). 
 * <P>
 * Various representations are possible.
 * 
 */
public abstract class PhraseSpec extends Spec {
	
	public boolean valid() { // check if PhraseSpec is valid
		// return true if valud
		// throw IllegalArgumentException if not valud
		return true;
	}
	
	/** returns the head of a phrase.
	 * <P>
	 * Head depends on type; for example, the head of an
	 * SPhraseSpec is the verb.
	 */
	abstract public String getHead();
		// a phrase must have a head
	
	/** returns person (1, 2, 3).
	 * <P>
	 * Always 3 unless NPPhraseSpec
	 */
	public Person getPerson() {
		// return person.  Default is 3, overridden by NPPhraseSpec
		return Person.THIRD;
	}	
	
	public boolean isPlural()
	{	//Lexicon lex) {
		// return T if plural (for NPs)
		// check if head is plural according to lexicon
		return false;
	//	return lex.isPlural(getHead());
	}

	// utility routine to force a parameter to be a PhraseSpec
	protected PhraseSpec makePhraseSpec(Object spec) {
		if (spec instanceof String)
			return new StringPhraseSpec((String)spec);
		else
			return (PhraseSpec)spec;
	}
	
	/*	should be overriden by NPPhraseSpec, StringPhraseSpec
	 */
	public void setSingular(boolean s)
	{}

	/*	should be overriden by NPPhraseSpec, StringPhraseSpec
	 */
	public void setQuote(boolean s)
	{}

	/*	should be overriden by NPPhraseSpec, StringPhraseSpec
	 */	
	public void setGenitive(boolean s)
	{}
	
	/*	should be overriden by NPPhraseSpec, StringPhraseSpec
	 */	
	public void setAccusative(boolean a)
	{}
	
	public void setRankOrdered(boolean r)
	{}
	
	/*	should be overriden by NPPhraseSpec, StringPhraseSpec
	 */	
	public boolean isGenitive()
	{
		return false;
	}

}
