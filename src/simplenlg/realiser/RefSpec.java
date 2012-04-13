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

import simplenlg.kb.KBEntity;

/** RefSpec is a Proto-phrase-spec for a reference to a KB entity.
 * <P>
 * This follows the terminology of Reiter and Dale (2000).
 * <P>
 * A referring expression module should replace this with an
 * NPPhraseSpec, before it is realised.
 * <P>
 * There is a default realise routine, simpleReferringExpression, which is called if there is no
 * proper ref-exp routine.  This just returns value of reference attribute of
 * entity if it exists, otherwise the classname in lower-case
 * 
 *  @author ereiter
 */

public class RefSpec extends PhraseSpec {

	// KB feature which holds ref-exp (if present)
	// used by simpleReferringExpression below
	public static final String REFERENCE_FEATURE = "reference";
	
	// Entity I am referring to
	KBEntity entity;
	
	/** construct a RefSpec for a KBEntity */
	public RefSpec(KBEntity entity) {
		this.entity = entity;
	}

	// head is a bit odd, we'll return the type
	@Override
	public String getHead() {
		return entity.getType();
	}

	// realiser - call default (ultra-simple) ref-exp generator
	@Override
	public List<AnchorString> realise(Realiser r) {
		// TODO Auto-generated method stub
		return simpleReferringExpression();
	}
	
	/** return a referring expression to a KBEntity
	 * 
	 * This is called when a real ref-exp generation is not possible.
	 * It is ultra-simple, just returns value of reference attribute of
	*  entity if it exists, otherwise the classname in lower-case.
	*/
	public List<AnchorString> simpleReferringExpression() {
		List<AnchorString> result = new ArrayList<AnchorString>();
		String ref = (String) entity.getValue(REFERENCE_FEATURE);
		if (ref != null)
			result.add(new AnchorString(ref, getAnchor()));
		//	return ref;
		else
			result.add(new AnchorString(entity.getType().toLowerCase(), getAnchor()));
		//	return entity.getType().toLowerCase();
		return result;
	}

	/** returns entity (reference target) */
	public KBEntity getEntity() {
		return entity;
	}

}
