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

package simplenlg.kb;

/** KBEntity is an interface to a knowledge base entity (object, class, instance).
 * <P>
 * KBEntity's are the root nodes in Document Plans; they are the basic
 * events/observations/etc that texts communicate.  In simplenlg,
 * KBEntity's must have a type, and values for attributes.  This is
 * a very simple structure which hopefully can easily be mapped to
 * KBEntity's represented as Java classes, DB columns, Protege instances,
 * etc
 * <P>
 * This interface is to also used to get feature values for NLGKB classes
 * NLGKB.getClass(String className) returns a KBEntity object, so to get
 * the value of feature XX of class CC, we say
 * KB.getClass("CC").getValue("XX");
 * 
 * @author ereiter
 *
 */
public interface KBEntity {

	/** returns the type of this KBEntity
	 * <P>
	 * This is the name of the class if the entity is a class.
	 * If the entity is an instance, this returns the name of
	 * the class the instance instantiates.
	 */
	public String getType();
	
	/** returns the value of the specified feature.
	 * <P>
	 * Value can be Date, Double, Integer, Boolean, String, or KBEntity.
	 * Null is returned if there is no value for this feature
	 * If the value is a class/entity, it is assumed to be a KBEntity.
	 * */
	public Object getValue(String featureName);

}
