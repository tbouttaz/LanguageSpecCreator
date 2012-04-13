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

import java.util.ArrayList;
import java.util.List;

/** PPPhraseSpec - represent a prepositional phrase.
 * <P>
 * It consists of
 * <UL>
 * <LI> preposition
 * <LI> object(s)
 * </UL>
 * These are linearised in the above order
 * @author ereiter
 *
 */
public class PPPhraseSpec extends SyntaxPhraseSpec {

	// components
	StringPhraseSpec preposition;   // eg, at, in ...
	List<PhraseSpec> objects;    // eg, home, 10:50, etc
	
	// constructors
	
	public PPPhraseSpec(Object preposition) {
		super();
		if (preposition instanceof String)
			this.preposition = new StringPhraseSpec((String) preposition);
		else
			this.preposition = (StringPhraseSpec) preposition;
		objects = new ArrayList<PhraseSpec>();
	}
	
	public PPPhraseSpec() {
		this("error");	// hopefully will be overriden by setPreposition!
	}
	
	public PPPhraseSpec(Object preposition, Object object) {
		// main constructor
		this(preposition);
		addObject(makePhraseSpec(object));
	}
	
	
	public void setPreposition(Object preposition) {
		if (preposition instanceof String)
			this.preposition = new StringPhraseSpec((String) preposition);
		else
			this.preposition = (StringPhraseSpec) preposition;
	}

	// getters, setters
	public void addObject(PhraseSpec object) {
		objects.add(object);
	}

	public void setObjects(List<PhraseSpec> objects) {
		this.objects = objects;
	}

	@Override
	public List<AnchorString> realise(Realiser r) {
		// if objects are S's, force Gerund form
	/*	if (objects != null)
			for (PhraseSpec spec: objects)
				if (spec instanceof SPhraseSpec) {
					SPhraseSpec s = (SPhraseSpec)spec;
					s.setForm(Form.GERUND);
				}*/

		List<AnchorString> l = preposition.realise(r);
		List<AnchorString> result = new ArrayList<AnchorString>();
		if (getAnchor() != null)
			result.add(new AnchorString(r.listWords(l, r.realiseAndList(objects)), getAnchor()));
		else
			result = r.listWords(l, r.realiseAndList(objects));
		return flash(result);
	}
	
	@Override
	public String getHead() {
		return preposition.getHead();
	}

}
