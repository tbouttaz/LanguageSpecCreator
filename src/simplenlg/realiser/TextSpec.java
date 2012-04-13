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
import WYSIWYM.model.Anchor;

/**
 * simplenlg.TextSpec represents a text specification.
 * <P>
 * This follows the terminology of Reiter and Dale (2000).
 * <P>
 * A TextSpec is a tree.  It consists of
 * <UL>
 * <LI>A list of children (which can be TextSpec or PhraseSpec)
 * <LI>A document structure level (eg, sentence)
 * <LI>A list conjunct
 * </UL>
 * <P>
 * If listConjunct is specified, it is inserted before the last child,
 * and commas are added before other children if this is below sentence level.
 * If listConjunct is not specified, it will be forced to "and" if this the
 * TextSpec is sentence level or below
 * 
 * @author ereiter
 * adapted by fhielkema December 2006
 *
 *
 */
public class TextSpec extends Spec {
	
	// parameters - children and document structure
	
	private List<Spec> children;  // constituent phrase/text specs
	
	private DocStructure docStructure; // doc structure (eg, paragraph)
	
	private String listConjunct;	// conjunct for list (null if no list)
	

	// constructors *************************************
	public TextSpec() {
		super();
		listConjunct = null;
		children = new ArrayList<Spec>();
		docStructure = DocStructure.SENTENCE;
	}
	
	public TextSpec(Object... specs ) {  // construct a TextSpec from a list of specs
		// specs are Object because we also allow Strings
		// also, if a spec is a DocStructure, it sets the doc structure of the TS
		this();   //
		for (Object spec: specs) {
			if (spec instanceof DocStructure)
				setDocStructure((DocStructure)spec);
			else
				addSpec(spec);
		}
	}
	
	
	// getters, setters, and also addSpec (which is a sort of setter)
	
	public void setDocStructure(DocStructure docStructure) {
		this.docStructure = docStructure;
	}
	
	public DocStructure getDocStructure() {
		return docStructure;
	}

	public void setSentence() { // convenience method for sentence DS
		setDocStructure(DocStructure.SENTENCE);
	}

	public void setParagraph() { // convenience method for paragraph DS
		setDocStructure(DocStructure.PARAGRAPH);
	}

	public void setDocument() { // convenience method for document DS
		setDocStructure(DocStructure.DOCUMENT);
	}

	public void setListConjunct(String listConjunct) {
		this.listConjunct = listConjunct;
	}

	public List<Spec> getChildren() {
		return children;
	}

	public void setChildren(List<Spec> children) {
		this.children = children;
	}

	/** adds a child to this text spec (in addition to existing children) */
	public void addChild (Object spec) {
		addSpec(spec);
	}

	public void addSpec(Object spec) { // add a spec
		// convert a String into a StringPhraseSpec
		if (spec instanceof Spec)
			children.add((Spec)spec);
		else if (spec instanceof String)
			children.add(new StringPhraseSpec((String)spec));
		else if (spec instanceof AnchorString)
		{
			AnchorString as = (AnchorString) spec;
			StringPhraseSpec sps = new StringPhraseSpec(as.toString());
			sps.setAnchor(as.getAnchor());
			children.add(sps);
		}
		else if (spec instanceof List)
		{
			List list = (List) spec;
			for (int i = 0; i < list.size(); i++)
			{
				AnchorString as = (AnchorString) list.get(i);
				StringPhraseSpec sps = new StringPhraseSpec(as.toString());
				sps.setAnchor(as.getAnchor());
				children.add(sps);
			}
		}
		else
			throw new SimplenlgException("addSpec: spec must be a PhraseSpec, TextSpec, String, or (List of) AnchorString");	
	}
	
	public void setAnchor(Anchor a)
	{
		if (docStructure.isSetHeader() || docStructure.isParHeader())
			if (children.size() > 0)
				children.get(0).setAnchor(a);
	}
	
	// realise method *************************************************
	public List<AnchorString> realise(Realiser r) { // realise a TextSpec
		// force sentence and below to be "and" conjuncts,
		// unless another conject is specified or cue phrases are included
		// increase my doc structure so its higher than my children
		if (!docStructure.isListOrHeader())	//unless the doc structure is a list (component) or header
			docStructure = docStructure.max(maxDSChildren().toSet());		
		// if I am higher-than sentence, promote children to sentence-level
		List<Spec> promotedChildren = children;
			
		if(!docStructure.isHeaderOrLower()) {
			promotedChildren = new ArrayList<Spec>();
			for (Spec child: children)
				promotedChildren.add(child.promote(DocStructure.SENTENCE));
		}
		
		// now merge together, with appropriate conjunct
		List<AnchorString> result;
		if (listConjunct != null)
			result = r.realiseConjunctList(promotedChildren, new AnchorString(listConjunct, null));
		else if (docStructure.isSentenceOrLower()) {
			if (!hasCuePhrase())
				result = r.realiseAndList(promotedChildren);
			else
				result = r.realiseConjunctList(promotedChildren, new AnchorString(",", null));
		}
		else
			result = r.realiseList(promotedChildren);

		if (docStructure.isSetHeader())
			result = r.applySetHeaderOrthography(result);
		else if (docStructure.isList())
			result = r.applyListOrthography(result);
		else if (docStructure.isListItem())
		{
			result = r.applySentenceOrthography(result);
			result = r.applyListItemOrthography(result);
		}
		else if (docStructure.isListHeader())
			result = r.applyListHeaderOrthography(result);
		else if (docStructure.isParHeader())
			result = r.applyParHeaderOrthography(result);
		else if (docStructure.isSentence())
			result = r.applySentenceOrthography(result);
		else if (docStructure.isSentenceSet())
			result = r.applySentenceSetOrthography(result);
		else if (docStructure.isParagraph())
			result = r.applyParagraphOrthography(result);
		
		return flash(result);
	}

		
	// utility methods ******************************
	
	public int size() { // number of component specs
		return children.size();
	}
	
	private boolean hasCuePhrase() {
		// return T if I or any of my grandchildren is an SPhraseSpec
		// with a cue phrase
		// just sentence-level SPhraseSpec, ignores embedded
		for (Spec spec: children) {
			if (spec instanceof SPhraseSpec && ((SPhraseSpec)spec).hasCuePhrase())
				return true;
			else if (spec instanceof TextSpec && ((TextSpec)spec).hasCuePhrase())
				return true;
		}
		return false;
	}
	
	private DocStructure maxDSChildren() {
		// return the highest DS of my children
		DocStructure maxDSvalue = DocStructure.PHRASE;
		for (Spec child: children)
			if (child instanceof TextSpec)
				maxDSvalue = maxDSvalue.max(((TextSpec)child).maxDS());
		return maxDSvalue;
	}
	
	private DocStructure maxDS() {
		// return highest DS of node and its children
		return this.getDocStructure().max(maxDSChildren());
	}

	@Override
	public TextSpec promote(DocStructure level) {
		// return a TextSpec with the same content, but at the
		// target level (or higher)
		if ((level == DocStructure.SETHEADER) || (docStructure == DocStructure.SETHEADER))
		{
			docStructure = DocStructure.SETHEADER;
			return this;
		}
		if ((level == DocStructure.PARHEADER) || (docStructure == DocStructure.PARHEADER))
		{
			docStructure = DocStructure.PARHEADER;
			return this;
		}
		if ((level == DocStructure.LISTHEADER) || (docStructure == DocStructure.LISTHEADER))
		{
			docStructure = DocStructure.LISTHEADER;
			return this;
		}
		if ((level == DocStructure.LISTITEM) || (docStructure == DocStructure.LISTITEM))
		{
			docStructure = DocStructure.LISTITEM;
			return this;
		}
		if ((level == DocStructure.LIST) || (docStructure == DocStructure.LIST))
		{
			docStructure = DocStructure.LIST;
			return this;
		}
		if (docStructure.compareTo(level) >= 0)
			return this;
		TextSpec newts = new TextSpec(docStructure.next(), this);
		return newts.promote(level);
	}
	

	public String toString() {
		// return String for a text spec
		return toString("", new Realiser());  // call main method with null indent
	}
	
	private String toString(String indent, Realiser r) {
		String result = indent +docStructure + ": ";
		if (listConjunct != null)
			result = result + "TextSpec-List " + listConjunct;
		else
			result = result + "TextSpec";
		
		if (children.size() == 1 && !(children.get(0) instanceof TextSpec))
			result = result + " -- " + r.realise(children.get(0)) + "\n";
		else {
			result = result + "\n";
			for (Spec child: children) {
				if (child instanceof TextSpec)
					result = result + ((TextSpec)child).toString(indent + "+", r);
				else
					result = result + indent + "+" + r.realise(child) + "\n";
			}
		}
		
		return result;
	}

}
