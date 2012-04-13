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

import simplenlg.exception.SimplenlgException;
import simplenlg.lexicon.Lexicon;



/**
 * Realiser converts a text spec into text or HTML.
 * <P>
 * It has several parameters:
 * <UL>
 * <LI> simplenlg.lexicon (should not normally need to be specified)
 * <LI> lineLength - output poured into lines of this length (default 70)
 * <LI> HTML - if true, include HTML markups (default false)
 * </UL>
  *
 * @author ereiter
 * 
 */
public class Realiser {

	// realiser parameters ************************************************
	private boolean HTML = false; // output HTML instead of text
	
	private int lineLength = 70; 	// max line length

	private Lexicon lexicon;	// lexicon
		
	// constants for knowledge about characters

	private final static String NEWLINE = "\n";	// add this to start a new line in output text
	
	private final static String FOLLOW_NOSPACE = " (>"; 	// don't need a space after this
	private final static String START_NOSPACE = " :;,.!?)<"; // don't need a space before this
	
	// punctuation symbols, strongest first (in Nirenburg's sense)
	public final static String PUNCTUATION = "!?.:;-,";
	public final static String SENTENCE_TERMINATORS = "!?.";  // sentence-end symbols


	// constructors ********************************************************
	public Realiser() { // default constructor - use default lexicon
		//this(Lexicon.createDefaultLexicon());
		this(new Lexicon());
	}

	public Realiser(Lexicon lexicon) {	// main constructor, including lexicon
		this.lexicon = lexicon;
	}

	// getters and setters for paramters ************************************
	public boolean isHTML() {
		return HTML;
	}

	public void setHTML(boolean html) {
		HTML = html;
	}

	public void setLexicon(Lexicon lexicon) {
		this.lexicon = lexicon;
	}

	public Lexicon getLexicon() {
		return lexicon;
	}

	public void setLineLength(int lineLength) {
		this.lineLength = lineLength;
	}
	
	/** gets output line length */
	public int getLineLength() {
		return lineLength;
	}
	
	// legacy/convenuence methods **********************************************
	
	// realise method as part of Realiser class
	public List<AnchorString> realise(Object spec) {
		List<AnchorString> l = new ArrayList<AnchorString>();
		if (spec == null)
			l.add(new AnchorString("", null));
		else if (spec instanceof String)
			l.add(new AnchorString((String) spec, null));
		else if (spec instanceof Spec)
		{	//if the Spec is elided, return an empty list!
			if (! ((Spec)spec).isElided())
				return ((Spec)spec).realise(this);
		}
		else if (spec instanceof List)	
		{
			List list = (List) spec;
			if (list.size() == 0)
				return l;		//already realised!
			if (list.get(0) instanceof AnchorString)
				return (List<AnchorString>) list;
			else
				throw new SimplenlgException("Can only realise Strings or Specs");
		}				
		else
			throw new SimplenlgException("Can only realise Strings or Specs");
		return l;
	}
	
	// realise something, forcing to paragraph level or higher
	public List<AnchorString> realiseDocument(Spec spec) {
		return realise(spec.promote(DocStructure.DOCUMENT));
	}

	// orthography, layout, spacing code ********************************************
	
	// sentence orthography *********************************

	// applySentenceOrthography - corrects orthography for a sentence
	// Make sure that
	// * its first word is capitalised
	// * the sentence ends with a full stop or other sentence terminator
	// * leading/trailink blanks are eliminated
	public List<AnchorString> applySentenceOrthography(List<AnchorString> body) 
	{	// stop if list is empty
		if ((body.size() == 0) || (body.get(0).toString().length() == 0))	
			return body;

		// eliminate leading/trailing blanks
		AnchorString as = null;
		String s = null;
		for (int i = 0; i < body.size(); i++)
		{
			as = body.get(i);
			s = as.toString();
			if (s.charAt(0) != '<')	//if the string is an html-tag, skip it
				break;		
	
			if ((i + 1) == body.size())
				return body;		//very unlikely; if the entire sentence is an html tag
		}
		s = s.trim();

		int cntr = 0;
		char firstChar = '-';
		for (cntr = 0; cntr < s.length(); cntr++)
			if (Character.isLetter(s.charAt(cntr)))
			{
				firstChar = s.charAt(cntr);
				break;
			}
				
		if ((firstChar != '-') && Character.isLowerCase(firstChar))
			if (cntr < s.length())
				s = s.substring(0, cntr) + Character.toUpperCase(firstChar) + s.substring(cntr + 1);
			else
				s = s.substring(0, cntr) + Character.toUpperCase(firstChar);
		as.setString(" " + s);			//we do need one space at the beginning!
		
		AnchorString as2 = null;
		String s2 = null;
		for (int i = body.size(); i > 0; i--)
		{
			as2 = body.get(i - 1);
			s2 = as2.toString();
			if (s2.charAt(s2.length() - 1) != '>')	//if the string is an html-tag, skip it
				break;
			else
				as2.setString(s2.trim());	//remove white spaces
		}
		s2 = s2.trim();
		
		if (s2.charAt(s2.length() - 1) != ':')
			as2 = addPunctuation(as2, '.');
		return body;
	}
	
	/*	Applies subsentence orthography; ie. komma's before and after. First though,
	 *	it eliminates leading and trailing blanks.
	 */
	public List<AnchorString> applySubsentenceOrthography(List<AnchorString> body, boolean parenthesis)
	{	// stop if list is empty
		if ((body.size() == 0) || (body.get(0).toString().length() == 0))	
			return body;

		AnchorString as = null;			// eliminate leading blanks
		String s = null;
		for (int i = 0; i < body.size(); i++)
		{
			as = body.get(i);
			s = as.toString();
			if (s.charAt(0) != '<')	//if the string is an html-tag, skip it
				break;		
			if ((i + 1) == body.size())
				return body;		//very unlikely; if the entire sentence is an html tag
		}
		s = s.trim();
		if (parenthesis)
			body.add(0, new AnchorString("(", null));
		else
			body.add(0, new AnchorString(", ", null));	//insert komma with space
		
		AnchorString as2 = null;	//eliminate trailing blanks
		String s2 = null;
		for (int i = body.size(); i > 0; i--)
		{
			as2 = body.get(i - 1);
			s2 = as2.toString();
			if (s2.charAt(s2.length() - 1) != '>')	//if the string is an html-tag, skip it
				break;
			else
				as2.setString(s2.trim());	//remove white spaces
		}
		s2 = s2.trim();
		if (parenthesis)
			addPunctuation(body, ')');
		else
			addPunctuation(body, ',');
		return body;
	}
	
	// sentence set orthography (i.e. linebreak) ****************************************
	
	public List<AnchorString> applySentenceSetOrthography(List<AnchorString> body)
	{
		List<AnchorString> result = body;	//new ArrayList<AnchorString>();
//		result = pour(body, lineLength);	
		// add para formatting
		if (HTML)
			result.add(new AnchorString("<br>", null));
		else
		{
			result.add(0, new AnchorString("\n", null));
			result.add(new AnchorString("\n", null));
		}
		return result;
	}

	// paragraph orthography ****************************************


	public List<AnchorString> applyParagraphOrthography(List<AnchorString> body) {

		List<AnchorString> result = body;		//new ArrayList<AnchorString>();
//		result = pour(body, lineLength);	

		// add para formatting
		if (HTML)
			result.add(0, new AnchorString("<P>", null));
		else
		{
			result.add(0, new AnchorString("\r\n", null));
			result.add(new AnchorString("\r\n", null));
		}
		return result;
	}
	
	public List<AnchorString> applySetHeaderOrthography(List<AnchorString> body)
	{
		List<AnchorString> result = new ArrayList<AnchorString>();
		for (int i = 0; i < body.size(); i++)
		{
			AnchorString as = body.get(i);
			StringBuffer sb = new StringBuffer(as.toString());
			if (sb.length() == 0)
				continue;
			if (as.toString().equals("xxx"))
			{	//don't include this header
				for (int j = (i + 1); j < body.size(); j++)
					result.add(body.get(j));
				result.add(0, new AnchorString("<br>", null));	//do add a linebreak
				return result;
			}
			
			char c = sb.charAt(0);
			if (Character.isLetter(c))
			{
				sb.setCharAt(0, Character.toUpperCase(c));	//set the first letter to a capital
				as.setString(sb.toString());
				result.add(as);
				break;
			}
		}
		// add para formatting
		if (HTML)
		{
			result.add(0, new AnchorString("<br><u>", null));
			result.add(new AnchorString(":</u>", null));
		}
		return result;
	}
	
	public List<AnchorString> applyParHeaderOrthography(List<AnchorString> body) {
		List<AnchorString> result = new ArrayList<AnchorString>();
		for (int i = 0; i < body.size(); i++)
		{
			if (body.get(i).toString().length() == 0)
				continue;
			StringBuffer sb = new StringBuffer(body.get(i).toString());
			char c = sb.charAt(0);
			if (Character.isLetter(c))
			{
				sb.setCharAt(0, Character.toUpperCase(c));	//set the first letter to a capital
				body.get(i).setString(sb.toString());
				break;
			}
		}
		
		result = addPunctuation(body, ':');	//pour(body, lineLength);	

		// add para formatting
		if (HTML)
		{
			result.add(0, new AnchorString("<font size=\"+1\"><i>", null));
			result.add(new AnchorString("</i></font>", null));
		}
		return result;
	}
	
	public List<AnchorString> applyListOrthography(List<AnchorString> body)
	{	//add 'begin list' and 'end list' tags
		if (HTML && (body.size() > 0))
		{
			body.add(0, new AnchorString("<UL>", null));
			body.add(new AnchorString("</UL>", null));
		}
		return body;
	}
	
	public List<AnchorString> applyListHeaderOrthography(List<AnchorString> body)
	{	//same orthography as a sentence, but with colon at the end ('I'm looking for transcripts:')
		List<AnchorString> result = applySentenceOrthography(body);
		AnchorString as = result.get(result.size() - 1);
		StringBuffer s = new StringBuffer(as.toString());
		if (s.charAt(s.length() - 1) == '.')
		{
			s.setCharAt(s.length() - 1, ':');
			as.setString(s.toString());
		}
		return result;
	}
	
	public List<AnchorString> applyListItemOrthography(List<AnchorString> body)
	{	//add a 'list item' tag
		if (HTML && (body.size() > 0))
			body.add(0, new AnchorString("<LI>", null));
		return body;
	}

	// spacing  and punctuation code ***************************************************


	// append N strings, adding a space if needed
	public String appendSpace(String ... strings) {
		String result = "";
		for (String s : strings) {
			if (result.length() == 0)
				result = s;
			else if (s == null)
				result = result;
			else if (spaceNeeded(result, s)) // append, with spaces if needed
				result = result + " " + s;
			else
				result = result + s;
		}
		return result;
	}
	
	public List<AnchorString> listWords(List<AnchorString> ... lists)
	{
		List<AnchorString> result = new ArrayList<AnchorString>();
		for (List<AnchorString> l : lists)
			for (int i = 0; i < l.size(); i++)
			{
				AnchorString input = l.get(i);
				if ((input == null) || (input.toString().length() == 0))
					continue;
				if (spaceNeeded(result, input.toString()))
					input.setString(new String(" " + input.toString()));

				result.add(input);
			}
		return result;
	}
	
	// convenience method to append a string and a char
	public String appendSpace(String body, char c) {
		return appendSpace(body, Character.toString(c));
	}
	
	// spaceNeeded - return TRUE if need to add a space between segments
	private boolean spaceNeeded(String firstString, String secondString) {
		// Don't need a space if either string is empty
		if (firstString == null || secondString == null || firstString.length() == 0 | secondString.length() == 0)
			return false;

		// Don't need a space if firstString ends with char in FOLLOW_NOSPACE
		// or if it is whitesoace itself
		char lastChar = firstString.charAt(firstString.length() - 1);
		if (Character.isWhitespace(lastChar)
				|| (FOLLOW_NOSPACE.indexOf(lastChar) >= 0))
			return false;

		// Don't need a space if secondString starts with a char in
		// START_NOSPACE
		// or if it is whitesoace itself
		char firstChar = secondString.charAt(0);
		if (Character.isWhitespace(firstChar)
				|| (START_NOSPACE.indexOf(firstChar) >= 0))
			return false;

		// All tests passed, space needed so return true
		return true;
	}
	
	private boolean spaceNeeded(List<AnchorString> list, String string)
	{
		if ((string == null) || (string.length() == 0) || (list == null) || (list.size() == 0))
			return false;
		
		for (int i = list.size(); i > 0; i--)
		{
			AnchorString as = list.get(i-1);
			if (as.toString().charAt(0) != '<')
				return spaceNeeded(as.toString(), string);
		}
		return false;
	}

	
	// realise lists (with or without conjuncts)
	public List<AnchorString> realiseList(List elements) {
		// realise list; concatenate together with spaces as needed
		// List elements are specs or strings
		if (elements == null || elements.isEmpty())
		{
			List<AnchorString> l = new ArrayList<AnchorString>();
			l.add(new AnchorString("", null));
			return l;
		}
		else if (elements.size() == 1)
			return realise(elements.get(0));
		else
		{
			List<AnchorString> result = realise(elements.get(0));
			for (int i = 1; i < elements.size(); i++)
			{
				List<AnchorString> in = realise(elements.get(i));
				if (in.size() == 0)
					continue;
				AnchorString as = result.get(result.size() - 1);
				if (spaceNeeded(as.toString(), in.get(0).toString()))
				{
					String s = as.toString();
					s = s + " ";
					as.setString(s);
				}
				result.addAll(in);
			}
			return result;
		}
	}
	
	// realise a list, including a conjunct before last element
	// separator is "," unless an element includes "," or ";", in which
	// case it is ";"
	// separator is absorbed if necessary, so works when conjoining sentences
	public List<AnchorString> realiseConjunctList(List elements, AnchorString conjunct) {
		if (elements == null)
		{
			List<AnchorString> l = new ArrayList<AnchorString>();
			l.add(new AnchorString("", null));
			return l;
		}

		switch (elements.size()) {
		case 0:
			List<AnchorString> l = new ArrayList<AnchorString>();
			l.add(new AnchorString("", null));
			return l;
		case 1:
			return realise(elements.get(0));
		case 2:
			List<AnchorString> l2 = new ArrayList<AnchorString>();
			l2.add(conjunct);
			return listWords(realise(elements.get(0)), l2,
					realise(elements.get(1))); // eg, apples and oranges
		default:
			// large list
			// eg,    pears, apples, and oranges
			// get all elements
			ArrayList<AnchorString> l3 = new ArrayList<AnchorString>();
			char separator = ',';
			int elNr = elements.size();
			List<AnchorString>[] lists = new List[elNr];
			for (int i = 0; i < elNr; i++)
			{
				List<AnchorString> l4 = realise(elements.get(i));
				for (AnchorString as : l4)
				{
					String s = as.toString();
					if (s.contains(",") || s.contains(";"))
						separator = ';';
				}
				lists[i] = l4;
			}
			// now add separators
			ArrayList<AnchorString> result = new ArrayList<AnchorString>();
			for (int i = 0; i < (elNr - 1); i++)
			{
				for (int j = 0; j < (lists[i].size() - 1); j++)
					result.add(lists[i].get(j));
				AnchorString as = addPunctuation(lists[i].get(lists[i].size() - 1), separator);
				StringBuffer temp = new StringBuffer(as.toString());
				AnchorString as2 = lists[i+1].get(0);
				if (spaceNeeded(temp.toString(), as2.toString()))
					temp.append(" ");
				as.setString(temp.toString());
				result.add(as);
			}
			
			List<AnchorString> conjunctList = new ArrayList<AnchorString>();
			conjunctList.add(conjunct);
			return listWords(result, conjunctList, lists[elNr - 1]);
		}
	}
	
	// convenience method to realise a conjunct list with "and" as conjunct
	public List<AnchorString> realiseAndList(List elements) {	
		return realiseConjunctList(elements,new AnchorString("and", null));
	}

	// punctuation merging code ****************************
	// this adds a punc symbol unless it is absorbed (as in Nirenburg)
	
	
	public AnchorString addPunctuation(AnchorString as, char punctuation) {
		String body = as.toString();
		String result;
		char lastChar = body.charAt(body.length() - 1);
		
		// if body does not end in a punct symbol, just append punc
		if (PUNCTUATION.indexOf(lastChar) < 0)
			result = appendSpace(body, punctuation);
		// else drop the weaker punct symbol
		else if (PUNCTUATION.indexOf(lastChar) <= PUNCTUATION.indexOf(punctuation))	//I DON'T WANT COLONS ALWAYS TO BE REPLACED BY DOTS ETC.
			result = body;
		else
			result = appendSpace(body.substring(0,body.length()-1), punctuation);
		as.setString(result);
		return as;
	}
	
	public List<AnchorString> addPunctuation(List<AnchorString> list, char punctuation)
	{
		int cntr = list.size() - 1;
		while (cntr >= 0)
		{
			AnchorString as = list.get(cntr);
			String body = as.toString();
			if (body.indexOf("</font>") >= 0)
			{
				cntr--;
				continue;	//not part of NL; go to previous tag
			}
			
			String result;
			char lastChar = body.charAt(body.length() - 1);
			if (PUNCTUATION.indexOf(lastChar) < 0)		// if body does not end in a punct symbol, just append punc
				result = appendSpace(body, punctuation);			// else drop the weaker punct symbol
			else if (PUNCTUATION.indexOf(lastChar) <= PUNCTUATION.indexOf(punctuation))	//I DON'T WANT COLONS ALWAYS TO BE REPLACED BY DOTS ETC.
				result = body;
			else
				result = appendSpace(body.substring(0,body.length()-1), punctuation);
			as.setString(result);
			break;
		}
		return list;
	}

	// pouring code *****************************************************
	private String pour(String body, int length) {
		// pour string into lines of specified length

		if (body.length() <= length)
			return body;

		for (int i = length; i > 0; i--) {
			if (Character.isWhitespace(body.charAt(i)))
				return body.substring(0, i) + NEWLINE
						+ pour(body.substring(i + 1), length);
		}

		// cannot pour, so just return body and hope for the best...
		return body;
	}

	private List<AnchorString> pour(List<AnchorString> body, int length)
	{
		int line = 0;
		for (int i = 0; i < body.size(); i++)
		{
			String s = body.get(i).toString();
			line += s.length();
			if (line >= length)
			{
				s = new String(NEWLINE + s);
				body.get(i).setString(s);
				line = 0;
			}
		}
		return body;
	}

}
