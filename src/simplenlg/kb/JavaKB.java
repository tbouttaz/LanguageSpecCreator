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


/** JavaKB implements a simpleNLG KB by using Java classes.
 * <P>
 * That is, a KB class is just a Java class, and a KBEntity
 * is just a Java instance of a class.  Features are just
 * fields, and the value of a feature named X is the value
 * of the field named X.
 * <P>
 * All classes referenced in a Java KB must be in a package
 * (which does not have subpackages).  The name of the package
 * is an argument to the constructor.
 * <P>
 * All classes references in a Java KB should also extend
 * (be subclasses of) JavaKBClass.  This implements the
 * KBEntity interface for instances of a class
 * 
 * @author ereiter
 *
 */
public class JavaKB implements NLGKB {

	String packageName;		// name of package that holds the classes
	
	// we keep our own className->Class map, for efficiency
	// if a className maps to null, this means the class does not exist
	private Map<String, Class> lookupClass;
		
	/** create a JavaKB.
	 * 
	 * @param packageName Java package which this KB is based on.
	 */
	public JavaKB(String packageName) {

		// copy packageName, add "." at end if doesn't exist already
		if (packageName.length() > 0 && !packageName.endsWith("."))
			this.packageName = packageName + ".";
		else
			this.packageName = packageName;
		
		lookupClass = new HashMap<String,Class>();
	}

	// get the Java Class object for the class of a given name
	// return null if no such class exists
	private Class getJavaClass(String className) {
		
		// first check if name is already in lookup table
		if (lookupClass.containsKey(className))
			return lookupClass.get(className);
		
		// else see if Java knows about this class
		try {
			Class c = Class.forName(packageName+className);  // get Class object
			lookupClass.put(className, c);					// put in table
			return c;
		} catch (Exception ex) {
			// assume that any simplenlg.exception means the class does not exist
			lookupClass.put(className, null);				// mark class as null in table
			return null;
		}
	}
	
	// see if class is ancestor of another class
	public boolean isAncestor(String className, String ancestorName) {
		Class c = getJavaClass(className);
		Class ancestor = getJavaClass(ancestorName);
		if (c == null || ancestor == null)
			return false;
		else
			return c.isAssignableFrom(ancestor);
	}
	
	public KBEntity getClass(String className) {
		// return a KBEntity object representing this class
		// in this case, return an instance of the class
		Class c = getJavaClass(className);
		if (c== null)
			return null;
		
		try {
			return (KBEntity)(c.newInstance());
		} catch (Exception ex) {
			// throw the simplenlg.exception, this probably means an internal error
			throw new SimplenlgException("JavaKB error: class " + className + " : " + ex.toString());
		}
	}




}
