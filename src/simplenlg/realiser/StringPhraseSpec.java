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

import simplenlg.features.Person;

/**
 * @author ereiter  - Ehud Reiter  14-Feb-06
 *
 *	extended by Feikje Hielkema	December 2006
 *
 */
public class StringPhraseSpec extends PhraseSpec {
	
	String spec, id;
	private boolean singular = false, plural = false, quote = false, subject = false, genitive = false, accusative = false, adjective = false, rank = false;
	
	public StringPhraseSpec(String spec)
	{
		super();
		this.spec = spec;
	}	
	
	public StringPhraseSpec(String spec, String id) { // constructor
		super();
		this.spec = spec;
		this.id = id;
	}

	@Override
	public String getHead() {
		if (quote)
			return ("\"" + spec + "\"");
		return spec;
	}

	public void setSingular(boolean s)
	{
		if (adjective)
		{	//adjective cannot be plural!
			singular = true;
			plural = false;
		}
		else
		{
			singular = s;
			plural = !s;
		}
	}

	public void setAdjective(boolean a)
	{
		adjective = a;
	}

	public void setQuote(boolean q)
	{
		quote = q;
	}
	
	public void setSubject(boolean o)
	{
		subject = o;
	}
	
	public void setAccusative(boolean a)
	{
		accusative = a;
	}
	
	public void setGenitive(boolean g)
	{
		if (adjective)
			genitive = false;
		else
			genitive = g;
	}
	
	public boolean isGenitive()
	{
		return genitive;
	}
	
	public void setRankOrdered(boolean r)
	{
		rank = r;
	}

	@Override
	public List<AnchorString> realise(Realiser r) {
		// TODO Auto-generated method stub
		String str = new String(spec);
		if (plural)
			str = r.getLexicon().getPlural(str);
		
		if (genitive)
		{
			if (str.equalsIgnoreCase("i") || str.equalsIgnoreCase("me"))
				str = "my";
			else if (str.equalsIgnoreCase("we") || str.equalsIgnoreCase("us"))
				str = "our";
			else if (str.equalsIgnoreCase("he") || str.equalsIgnoreCase("him"))
				str = "his";
			else if (str.equalsIgnoreCase("she") || str.equalsIgnoreCase("her"))
				str = "her";
			else if (str.equalsIgnoreCase("they") || str.equalsIgnoreCase("them"))
				str = "their";
			else if (str.equalsIgnoreCase("it"))
				str = "its";
			else if (str.equalsIgnoreCase("which") || str.equalsIgnoreCase("that") || str.equalsIgnoreCase("who"))
				str = "whose";
			else
			{
				StringBuffer sb = new StringBuffer(str);
				if (str.charAt(str.length() - 1) == 's')
					sb.append("'");
				else if (str.equals("it"))
					sb.append("s");
				else
					sb.append("'s");
				str = sb.toString();
			}
		}
		else if (accusative)
		{
			if (str.equalsIgnoreCase("who"))	//'by whom'
				str = "whom";
			else if (str.equalsIgnoreCase("he"))
				str = "him";
			else if (str.equalsIgnoreCase("she"))
				str = "her";
			else if (str.equalsIgnoreCase("they"))
				str = "them";
			else if (str.equalsIgnoreCase("i"))
				str = "me";					
		}
		else if (subject) 
		{
			if (str.equalsIgnoreCase("i") || str.equalsIgnoreCase("me"))
				str = "I";
			else if (str.equalsIgnoreCase("we") || str.equalsIgnoreCase("us"))
				str = "we";
			else if (str.equalsIgnoreCase("he") || str.equalsIgnoreCase("him"))
				str = "he";
			else if (str.equalsIgnoreCase("she") || str.equalsIgnoreCase("her"))
				str = "she";
			else if (str.equalsIgnoreCase("they") || str.equalsIgnoreCase("them"))
				str = "they";
		}
		else if (rank)
		{	//check what the rightmost number is, and append the correct postfix
			StringBuffer sb = new StringBuffer(str);
			try
			{
				int nr = Integer.parseInt(spec) % 10;
				if (nr == 1)
					sb.append("st");
				else if (nr == 2)
					sb.append("nd");
				else if (nr == 3)
					sb.append("rd");
				else
					sb.append("th");
			}
			catch(NumberFormatException e)
			{
				if (str.toLowerCase().lastIndexOf("one") == (str.length() - 3))
					sb.replace(str.length() - 3, str.length(), "first");
				else if (str.toLowerCase().indexOf("two")  == (str.length() - 3))
					sb.replace(str.length() - 3, str.length(), "second");
				else if (str.toLowerCase().indexOf("five")  == (str.length() - 4))
					sb.replace(str.length() - 4, str.length(), "fifth");
				else if (str.toLowerCase().indexOf("nine")  == (str.length() - 4))
					sb.replace(str.length() - 4, str.length(), "ninth");
				else if (str.toLowerCase().indexOf("three")  == (str.length() - 5))
					sb.replace(str.length() - 5, str.length(), "third");
				else 
					sb.append("th");
			}
			str = sb.toString();
		}
		else
		{
			if (str.equalsIgnoreCase("i") || str.equalsIgnoreCase("me"))
				str = "me";
			else if (str.equalsIgnoreCase("we") || str.equalsIgnoreCase("us"))
				str = "us";
			else if (str.equalsIgnoreCase("he") || str.equalsIgnoreCase("him"))
				str = "him";
			else if (str.equalsIgnoreCase("she") || str.equalsIgnoreCase("her"))
				str = "her";
			else if (str.equalsIgnoreCase("they") || str.equalsIgnoreCase("them"))
				str = "them";
		}
		
		if (id != null)
			str = new String(str + " (" + id + ")");
	
		List l = new ArrayList();
		if (quote)
			l.add(new AnchorString("\"" + str + "\"", getAnchor()));
		else
			l.add(new AnchorString(str, getAnchor()));

		return flash(l);
	}

	@Override
	public Person getPerson() {	//Lexicon lex) {
		// TODO Auto-generated method stub
		if (spec.equalsIgnoreCase("i") || spec.equalsIgnoreCase("me")
				|| spec.equalsIgnoreCase("we") || spec.equalsIgnoreCase("us"))
				return Person.FIRST;
		else if (spec.equalsIgnoreCase("you"))
			return Person.SECOND;
		else
			return Person.THIRD;
	}

	@Override
	public boolean isPlural()	{	//Lexicon lex) {
		// return T if plural (for NPs)
		// check if head is plural according to lexicon
		if (spec.equalsIgnoreCase("we") || spec.equalsIgnoreCase("us")
				|| spec.equalsIgnoreCase("you")
				|| spec.equalsIgnoreCase("they") || spec.equalsIgnoreCase("them"))
			return true;
		
		if (singular)
		 	return false;
		if (plural)
		 	return true;
		return false;
	//	return lex.isPlural(getHead());
	}

}
