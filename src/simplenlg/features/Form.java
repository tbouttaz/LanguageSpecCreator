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

package simplenlg.features;

/**
 * The form of the verb (and its associated sentence). Cases distinguished:
 * <ul>
 * <li>NORMAL: any form except the following:</li>
 * <li>INFINITIVE: To eat an apple.</li>
 * <li>GERUND: Eating an apple.</li>
 * <li>IMPERATIVE: Eat an apple.</li>
 * </ul>
 * 
 * @author agatt
 * 
 */

public enum Form {
	NORMAL, INFINITIVE, GERUND, IMPERATIVE;

	
	/**
	 * 
	 * @return <code>true</code> if a sentence in this form can have a
	 *         subject. This is in fact only true for the <code>NORMAL</code>
	 *         case.
	 */
	public boolean hasSubject() {
		return (this == NORMAL);
	}

	/**
	 * Check if a sentence in this form can be embedded within another sentence.
	 * This is in fact only allowed for the values <code>INFINITIVE</code> and
	 * <code>GERUND</code>. Examples:
	 * 
	 * <ul>
	 * <li>(INFINITIVE) <i>To eat an apple</i> is dangerous.</li>
	 * <li>(GERUND) <i>Eating an apple</i> is dangerous.</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if a sentence in this form can be embedded.
	 */
	public boolean isEmbedded() {
		return (this == INFINITIVE || this == GERUND);
	}
	
	/***
	 * * check if this form allows tenses (past, future)
	 */
	public boolean allowsTense() {
		return (this == NORMAL);
	}

	/**
	 * Tests the compatibility of a given <code>Form</code> with a 
	 * given {@link simplenlg.features.Mood}. In particular, you cannot
	 * have a <code>GERUND</code> or <code>INFINITIVE</code> form for 
	 * a clause if the clause is a <code>SUBJUNCTIVE</code>, <code>COMMAND</code> 
	 * (imperative) or a <code>QUESTION</code>.
	 * @param m A value of {@link simplenlg.features.Mood}
	 * @return <code>true</code> if this mood and this form are compatible.
	 */
	public boolean isCompatible(Mood m) {
		switch (m) {
		case NORMAL:
			return true;

		case SUBJUNCTIVE:
			return (this == NORMAL);

		default:
			return false; //unnecessary, but makes java happy
		}
	}

}
