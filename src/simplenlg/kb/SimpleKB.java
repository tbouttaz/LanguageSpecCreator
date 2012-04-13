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


/** SimpleKB is a simple implementation of an NLGKB
 * <P>
 * This provides a very simple implementation of an NLGKB.
 * The KB is explicitly constructed via calls to addClass()
 * and setFeatureValue().  Classes can have ancestors, and
 * inherit default feature values from them
 * <P>
 * Classes are represented as SimpleKBClass objects
 * a hash table maps names to classes
 * <P>
 * KBEntity's are represented as SimpleKBClass  (ie, there is
 * no distinction between instances and classes)
 * 
 * @author ereiter
 * 
 */
public class SimpleKB implements NLGKB {

	// maps that hold information
	Map<String, SimpleKBClass> classes; // map a name to the class with this

	// name

	/** create an empty simpleKB */
	public SimpleKB() {
		classes = new HashMap<String, SimpleKBClass>();
	}

	/** add a class to the KB.
	 */ 
	public void addClass(SimpleKBClass newClass) {
		String className = newClass.getName();

		// if class has a name, add it to the lookup table
		if (className != null) {

			// check if class already exists
			if (classes.containsKey(className)) {
				throw new SimplenlgException(
						"simpleNLGKB - class is already defined: " + className);
			}

			// if not, then add it to the maps
			classes.put(className, newClass);
		}
	}

	public KBEntity getClass(String className) {
		// get class with this name
		// return null if no such class exists
		return classes.get(className);
	}

	public boolean isAncestor(String className, String ancestorName) {
		// return true if ancestorName is the name of an ancestor of className
		SimpleKBClass c = classes.get(className);
		SimpleKBClass ancestor = classes.get(ancestorName);

		// case 1: class or ancestor don't exist, return false
		if (c == null || ancestor == null)
			return false;

		// case 2: className is same as ancestorName
		if (c == ancestor)
			return true;

		// case 3: try parents
		while (c != null) {
			if (c == ancestor)
				return true;
			c = c.getParent();
		}

		// case 4: not found, return false
		return false;

	}


	/** add a class with specified name to the KB */
	public SimpleKBClass newClass(String className) {
		SimpleKBClass c = new SimpleKBClass(className);
		this.addClass(c);
		return c;
	}

	/** add a class with specified name and specified parent to the KB */
	public SimpleKBClass newClass(String className, SimpleKBClass parent) {
		SimpleKBClass c = new SimpleKBClass(className, parent);
		this.addClass(c);
		return c;
	}

	/** add a class with specified parent to the KB */
	public SimpleKBClass newClass(SimpleKBClass parent) {
		SimpleKBClass c = new SimpleKBClass(parent);
		this.addClass(c);
		return c;
	}
}
