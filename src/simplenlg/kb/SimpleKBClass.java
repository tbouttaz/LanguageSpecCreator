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



import java.util.HashMap;
import java.util.Map;

import simplenlg.exception.SimplenlgException;


/** SimpleKBClass represents a class in SimpleKB
 * <P>
 * Also can be used to represent a KBEntity
 * <P>
 * setFeatureValue is used to set the value of a feature
 *  
 * @author ereiter
 */

public class SimpleKBClass implements KBEntity {
	
	String name;			// class name (can be null)
	SimpleKBClass parent;   // parent class(null if none)
	Map<String,Object> featureValues;
	
	/** constructs a class with specified name */
	public SimpleKBClass(String name) {
		// construct given name
		// use this when there is no parent
		this.name = name;
		featureValues = new HashMap<String, Object>();
	}

	/** constructs a class with specified name and parent */
	public SimpleKBClass(String name, SimpleKBClass parent) {
		// construct given name and parent
		this(name);
		this.parent = parent;
	}

	/** constructs a class with specified parent */
	public SimpleKBClass(SimpleKBClass parent) {
		// create an unnamed class with a parent
		this(null, parent);
	}
	
	/** sets the value of a feature */
	public void setFeatureValue(String featureName, Object value) {
		// set the value of a feature
		featureValues.put(featureName, value);
	}
	
	public String getType() {
		// return name of self, or first ancestor with a name
		SimpleKBClass c = this;
		while (c != null) {
			if (c.getName() != null)
				return c.getName();
			c = c.getParent();
		}

		throw new SimplenlgException("SimpleKBClass - class does not have a named parent");
	}

	public Object getValue(String featureName) {
		// get feature value
		// return null if feature doesn't exist
		// return parent's value for this feature if class exists but doesn't
		// specify this feature

		SimpleKBClass c = this;

		// look for value, go up hierarchy
		while (c != null) {
			Object value = c.getFeatureValue(featureName);
			if (value != null)
				return value;
			c = c.getParent();
		}

		// no value here or in parents, return null
		return null;
	}
	
	public Object getFeatureValue(String featureName) {
		// get value if a feature from map
		return featureValues.get(featureName);
	}

	public String getName() {
		// return value of name
		return name;
	}
	
	public String toString() {
		// return name of class
		return "SimpleKBClass " + name;
	}

	SimpleKBClass getParent() {
		// return parent of a class
		return parent;
	}



}
