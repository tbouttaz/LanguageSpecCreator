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


import java.lang.reflect.Method;
import java.util.Collection;

import simplenlg.exception.SimplenlgException;
import simplenlg.realiser.PhraseSpec;
import simplenlg.realiser.RefSpec;
import simplenlg.realiser.SPhraseSpec;
import simplenlg.realiser.StringPhraseSpec;


/** Lexicaliser converts a KBEntity into a PhraseSpec
 * <P>
 * The lexicaliser is part of the microplanner.  Its job is to convert
 * KBEntity's (leaf nodes of DocPlan's) into PhraseSpec (leaf nodes of
 * TextSpec).
 * <P>
 * KBEntity's must have a type, and can have values for simplenlg.features; see
 * KBEntity interface.
 * <P>
 * A lexicaliser is based on a NLGKB (NLG Knowledge Base), which
 * records information about classes/entities and their attributes.
 * See NLGKB interface
 * <P>
 * This lexicaliser works by instantiating templates.  Templates are
 * stored in the nlgKB, as feature values of KBEntity's and classes
 * The lexicaliser will look for the following simplenlg.features of a KBEntity
 * (which may be inherited from its class) (all simplenlg.features have String values)
 * <UL>
 * <LI> template - converted into a StringPhraseSpec
 * <LI> verb (required), subject, complement, modifier (optional) - converted into
 *     an SPhraseSpec.  subject, complement, modifier can be be lists of
 *     Strings (or null), as well as single strings
 * </UL>
 * <P>
 * A template string consists of words and [xxxx] constructs. [xxxx]
 * constructs refer to parameters and the ontology, as follows
 * <UL>
 * <LI> [X] - value of feature X of KBEntity
 * <LI> [$X] - ontology entity whose name is X
 * <LI> [#Z] - invokes function Z on the class
 * <LI> [X.Y] - value of feature Y of (class/entity which is value of feature X of KBEntity)
 * <LI> [$X.Y]- value of feature Y of (ontology entity whose name is X)
 * <LI> [X.Y.Z], [$X.Y.Z] - also allowed
 * <LI> [X|fff], [X.Y|fff], etc - fff is a format string.  Either a
 *    String beginning with %; this is passed to String.format.
 *    Or areference to a feature value; this must be a string, it is used as a format string
 * </UL>
 * <P>
 * If a format string is not specified, default Java formatting is used.
 * Exception: If the value is a KBEntity, a "referring expression" to
 * this entity is created. If the value is the entire template,
 * then a RefSpec is returned.  Otherwise (if the reference is embedded in a template which
 * also contains other things), the reference is the value of the
 * entity's reference feature if it exists, otherwise the name of the
 * class (in lower case).
 * This needs to be replaced by a proper ref-exp routine!!!
 * <P>
 * Example - for a PICK-UP-BABY event
 * <UL>
 * <LI> verb = "pick up"   (particles are allowed)
 * <LI> subject = "[agent]"  (assuming agent is a feature of the event)
 * <LI> complement = "[$BABY]" (assuming BABY is a class in the ontology)
 * </UL>
 * <P>
 * This is obviously a very basic approach to lexicalisation, but hopefully
 * applications can easily extend it, by post-processing the generated
 * phraseSpec (eg, adding endModifiers which require complex computation,
 * or deciding whether to passivize based on a discourse/focus model);
 * and/or by defining "psuedo-simplenlg.features" at the KBEntity level (for example,
 * calculating duration from startTime and endTime, if these are simplenlg.features
 * but duration is not)
 * 
 * @author ereiter
 * 
 */
public class Lexicaliser {

	NLGKB kb;				// knowledge base
	
	// special simplenlg.features
	public static final String TEMPLATE_FEATURE = "template";
	public static final String VERB_FEATURE = "verb";
	public static final String SUBJECT_FEATURE = "subject";
	public static final String COMPLEMENT_FEATURE = "complement";
	public static final String MODIFIER_FEATURE = "modifier";
	
	public static final char START_BRACKET = '[';
	public static final char END_BRACKET = ']';
	public static final char START_FORMAT = '|';
	public static final char LITERAL_FORMAT = '%';
	public static final char FEATURE_CHAR = '.';
	public static final char CLASS_CHAR = '$';
	public static final char METHOD_CHAR = '#';
	
	/** constructor; a KB must be specified */
	public Lexicaliser (NLGKB kb) {
		this.kb = kb;
	}

	/** lexicalise a message (KBEntity) */
	public PhraseSpec lexicalise(KBEntity entity) {
		// first see if message has a template feature defined
		String template = (String) entity.getValue(TEMPLATE_FEATURE);
		if (template != null)
			return expandString(template, entity);
		
		// then see if it has a verb feature defined
		String verb = (String) entity.getValue(VERB_FEATURE);
		if (verb != null) {
			SPhraseSpec spec = new SPhraseSpec();
			verb = expandString(verb,entity).getHead();
			spec.setVerb(verb);
			
			// subjects
			Object subjects = entity.getValue(SUBJECT_FEATURE);
			if (subjects != null) {
				if (subjects instanceof Collection)
					for (Object subject: (Collection)subjects)
						spec.addSubject(expandString((String)subject, entity));
				else
					spec.addSubject(expandString((String)subjects, entity));
			}
			
			// objects
			Object complements = entity.getValue(COMPLEMENT_FEATURE);
			if (complements != null) {
				if (complements instanceof Collection)
					for (Object complement: (Collection)complements)
						spec.addComplement(expandString((String)complement, entity));
				else
					spec.addComplement(expandString((String)complements, entity));
			}
			
			// endModifiers
			Object modifiers = entity.getValue(MODIFIER_FEATURE);
			if (modifiers != null) {
				if (modifiers instanceof Collection)
					for (Object modifier: (Collection)modifiers)
						spec.addModifier(expandString((String)modifier, entity));
				else
					spec.addModifier(expandString((String)modifiers, entity));
			}
			
			return spec;

		}

		return null;
	}
	
	// expand a template string
	private PhraseSpec expandString(String template, KBEntity entity) {
				
		// check for empty template
		if (template == null || template.length() == 0)
			return null;
		
		// check if entire string is one reference
		if (template.charAt(0) == START_BRACKET && template.indexOf(END_BRACKET) == template.length()-1) {
			// call realiseParameter, allow RefSpec if generateRefSpec
			Object result = realiseParameter(template.substring(1,template.indexOf(END_BRACKET)), entity);
			if (result instanceof String)		// convert String into StringPhraseSpec
				result = new StringPhraseSpec ((String)result);
			return (PhraseSpec)result;
		}
		
		// else process template piece by piece
		int currentPos = 0;		// current position in String
		StringBuilder result = new StringBuilder();   // result string
		
		// loop. substituting parameters
		while (true) {
			
			// look for start of next parameter
			int startBracketPos = template.indexOf(START_BRACKET, currentPos);
			if (startBracketPos < 0)
				break;
			
			// look for end of next parameter
			int endBracketPos = template.indexOf(END_BRACKET, startBracketPos);
			if (endBracketPos < 0)
				throw new SimplenlgException("Impossible nlgbase template: " + template);
			
			// add to result stuff before parameter
			if (currentPos < startBracketPos)
				result.append(template.substring(currentPos, startBracketPos));
			
			// process parameter
			Object value = realiseParameter(template.substring(startBracketPos+1,endBracketPos), entity);
			
			// if value is a RefSpec, generate a simple ref exp for it
			// this is a hack, until we allow more complex PhraseSpec to be built
			if (value instanceof RefSpec)
				value = ((RefSpec)value).simpleReferringExpression();
			
			// add it to template string
			result.append((String)value);
			
			// update current pos
			currentPos = endBracketPos+1;
		}
		
		result.append(template.substring(currentPos));
		
		return new StringPhraseSpec(result.toString());
		
	}
	
	// get the value of a parameter as a String
	// refer to entity for feature values
	// returns either String or RefSpec
	private Object realiseParameter(String parameterString, KBEntity entity) {
		// split into feature spec and format spec
		String featureSpec = parameterString;
		String formatSpec = null;
		int formatPos = parameterString.indexOf(START_FORMAT);
		if (formatPos >= 0) {
			featureSpec = parameterString.substring(0,formatPos);
			formatSpec = parameterString.substring(formatPos+1);
		}
		
		Object value = getValue(featureSpec, entity);
		if (value == null)
			throw new SimplenlgException("Simplenlg: No value for feature " + featureSpec + " in entity of type " + entity.getType());
		
		if (formatSpec != null) {
			String format = (String)getValue(formatSpec, entity);
			return String.format(format, value);
		}
		else if (value instanceof KBEntity)
			return new RefSpec((KBEntity)value);
		else
			return value.toString();
	}
	
	// get the value of a feature spec string.
	private Object getValue(String featureSpec, KBEntity message) {
		// case 1 - featureSpec is empty, return message
		if (featureSpec == null || featureSpec.length() == 0)
			return message;
		
		// case 2 - featureSpec starts with %; return as literal
		if (featureSpec.charAt(0) == LITERAL_FORMAT)
			return featureSpec;
		
		// separate out bit before "."
		
		int dotPos = featureSpec.indexOf(FEATURE_CHAR);
		String featureName = featureSpec;
		if (dotPos>=0)
			featureName = featureSpec.substring(0,dotPos);
		
		Object value = message;
		if (featureName.charAt(0) == CLASS_CHAR)
			value = kb.getClass(featureName.substring(1));
		else if (featureName.charAt(0) == METHOD_CHAR) {
			try {
			Class c = message.getClass();
			Method m = c.getMethod(featureName.substring(1));
			value = m.invoke(message);
			} catch (Exception ex) {
				throw new SimplenlgException("Simplenlg: Cannot invoke method " + featureName.substring(1) + " in entity of type " + message.getType());
			}
		}
		else
			value = message.getValue(featureName);
		
		// case 3 - "." exists, recurse
		if (dotPos >= 0) {
			// force value to be a class if it is a String
			if (value instanceof String)
				value = kb.getClass((String)value);
			if (value == null || ! (value instanceof KBEntity))
				throw new SimplenlgException("entity of type " + message.getType() +
						" does not have class-valued feature " + featureName +
						" in featurespec " + featureSpec);
			return getValue(featureSpec.substring(dotPos+1), (KBEntity)value);
		}
		// case 4 - no".", just return value
		else
			return value;
	}
	

}
