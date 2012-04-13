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

import simplenlg.exception.SimplenlgException;

/**
 * DocStructure is the document structure level.
 * <P>
 * This follows Powers et al (2002)
 * <P>
 * Structures are:
 * <UL>
 * <LI>PHRASE - phrase withing a sentence
 * <LI>PHRASESET - set of phrases
 * <LI>LISTITEM	- an item in a list
 * <LI>SENTENCE - sentence
 * <LI>SETHEADER - set header
 * <LI>PARHEADER - par header
 * <LI>LISTHEADER - list header
 * <LI>SENTENCESET - set of sentences
 * <LI>LIST - a list
 * <LI>PARAGRAPH - paragraph
 * <LI>PARAGRAPHSET - set of paragraphs
 * <LI>DOCUMENT - complete document
 * </UL>
 * 
 * Ehud Reiter 29-Jun-06
 */
public enum DocStructure {PHRASE, PHRASESET, SENTENCE, SETHEADER, PARHEADER, LISTITEM, LISTHEADER, SENTENCESET, LIST, PARAGRAPH, PARAGRAPHSET, DOCUMENT;

	public boolean isSentence() {
		return (this == SENTENCE);
		}

	public boolean isSentenceSet() {
		return (this == SENTENCESET);
		}

	public boolean isParagraph() {
		return (this == PARAGRAPH);
		}
	
	public boolean isSetHeader() {
		return (this == SETHEADER);
	}
	
	public boolean isParHeader() {
		return (this == PARHEADER);
	}
	
	public boolean isListHeader()
	{
		return (this == LISTHEADER);
	}
	
	public boolean isListItem()
	{
		return (this == LISTITEM);
	}
	
	public boolean isList()
	{
		return (this == LIST);
	}
	
	public boolean isSentenceOrLower() {
		// return T if this structure is a sentence or part of a sentence
		return (compareTo(SENTENCE) <= 0);
	}
	
	public boolean isHeaderOrLower() {
		// return T if this structure is a sentence or part of a sentence
		return (compareTo(LISTHEADER) <= 0);
	}

	public boolean isListOrHeader()
	{
		if (this == LISTITEM)
			return true;
		if (this == LIST)
			return true;
		if (this == LISTHEADER)
			return true;
		if (this == SETHEADER)
			return true;
		if (this == PARHEADER)
			return true;
		return false;
	}

	public boolean isSentenceComponent() {
		// return T if this structure is  part of a sentence
		return (compareTo(SENTENCE) < 0);
	}

	public DocStructure max(DocStructure d2) {
		// return maximum of self and d2
		if (compareTo(d2) > 0)
			return this;
		else
			return d2;
	}
	
	public boolean greaterThan(DocStructure d2) {
		// return T if I am greater than d2
		return (compareTo(d2) > 0);
	}
	
	public DocStructure next() {
		// return next highest level
		switch (this) {
		case PHRASE: return PHRASESET;
		case PHRASESET: return SENTENCE;
		case LISTITEM: return LIST;
		case LISTHEADER: return LIST;
		case SENTENCE: return SENTENCESET;
		case SENTENCESET: return PARAGRAPH;
		case PARAGRAPH: return PARAGRAPHSET;
		case PARAGRAPHSET: return DOCUMENT;
		case DOCUMENT: return DOCUMENT;   // no higher level!
		default: return DOCUMENT;         
		}
	}

	public DocStructure previous() {
		// return next lowest level
		switch (this) {
		case PHRASE: return PHRASE;  // no lower level!
		case PHRASESET: return PHRASE;
		case SENTENCE: return PHRASESET;
		case SENTENCESET: return SENTENCE;
		case PARAGRAPH: return SENTENCESET;
		case PARAGRAPHSET: return PARAGRAPH;
		case DOCUMENT: return PARAGRAPHSET;   
		default: return PHRASE;         // don't really need this, but makes Java happy
		}
	}

	public DocStructure toSet() {
		// return self if a set, otherwise next highest level which is a set
		switch (this) {
		case PHRASE: return PHRASESET;
		case PHRASESET: return PHRASESET;
		case SENTENCE: return SENTENCESET;
		case LISTITEM: return LIST;
		case LISTHEADER: return LIST;
		case SENTENCESET: return SENTENCESET;
		case PARAGRAPH: return PARAGRAPHSET;
		case PARAGRAPHSET: return PARAGRAPHSET;
		case DOCUMENT: return DOCUMENT;   // no higher level!
		default: return DOCUMENT;         
		}
	}

	public String getName() {
		// return name of level
		switch (this) {
		case PHRASE: return "PHRASE";
		case PHRASESET: return "PHRASESET";
		case LISTITEM: return "LISTITEM";
		case SENTENCE: return "SENTENCE";
		case SETHEADER: return "SETHEADER";
		case PARHEADER: return "PARHEADER";
		case LISTHEADER: return "LISTHEADER";
		case SENTENCESET: return "SENTENCESET";
		case LIST: return "LIST";
		case PARAGRAPH: return "PARAGRAPH";
		case PARAGRAPHSET: return "PARAGRAPH";
		case DOCUMENT: return "DOCUMENT";  
		default: throw new RuntimeException("Unknown document structure: " + this);
		}
	}
	
	public static DocStructure getStructure (String name) { // construct from a name
		if (name.equalsIgnoreCase("PHRASE"))
			return PHRASE;
		if (name.equalsIgnoreCase("PHRASESET"))
			return PHRASESET;
		if (name.equalsIgnoreCase("LISTITEM"))
			return LISTITEM;
		if (name.equalsIgnoreCase("SENTENCE"))
			return SENTENCE;
		if (name.equalsIgnoreCase("SETHEADER"))
			return SETHEADER;
		if (name.equalsIgnoreCase("PARHEADER"))
			return PARHEADER;
		if (name.equalsIgnoreCase("LISTHEADER"))
			return LISTHEADER;
		if (name.equalsIgnoreCase("SENTENCESET"))
			return SENTENCESET;
		if (name.equalsIgnoreCase("LIST"))
			return LIST;
		if (name.equalsIgnoreCase("PARAGRAPH"))
			return PARAGRAPH;
		if (name.equalsIgnoreCase("PARAGRAPHSET"))
			return PARAGRAPHSET;
		if (name.equalsIgnoreCase("DOCUMENT"))
			return DOCUMENT;
		else
			throw new SimplenlgException("Unknown document structure: " + name);
	}


	}