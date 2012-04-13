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

import simplenlg.lexicon.lexicalitems.Conjunction;
import simplenlg.lexicon.lexicalitems.Verb;

/**
 * This is the class for Coordinated verb phrases. A coordinate VP is
 * represented as:
 * <ul>
 * <li>A set of features (which determine the nature of the auxiliary group)</li>
 * <li>A conjunction ("and") by default</li>
 * <li>A list of main verbs</li>
 * </ul>
 * 
 * <p>
 * All the children of a <code>CoordinateVerbGroupSpec</code> inherit its
 * simplenlg.features, that is:
 * 
 * <ul>
 * <li>tense</li>
 * <li>perfect</li>
 * <li>progressive</li>
 * <li>passive</li>
 * <li>negated</li>
 * <li>form (a value of {@link simplenlg.features.Form})</li>
 * <li>mood (a value of {@link simplenlg.features.Mood})</li>
 * </ul>
 * 
 * <p>
 * The default realisation of a <code>CoordinateVerbGroupSpec</code> is
 * as follows: AUX + main verb conjunction. Thus, there is one auxiliary.
 * <p>
 * The default aggregation behaviour can be overridden using
 * {@link CoordinateVerbGroupSpec#setAggregate(boolean)}.
 * 
 * <p>
 * Examples involving aggregation:
 * 
 * <ul>
 * <li>The instrument <strong>was being tried and tested</strong>
 * (progressive, past, passive)</li>
 * <li>It <strong>rained, snowed and hailed</strong> (past)</li>
 * <li>John <strong>has been arrested and detained</strong> (present, perfect,
 * passive)</li>
 * <li>The dog <strong>has been being fed and watered</strong> (perfect,
 * present, passive, progressive)</li>
 * </ul>
 * 
 * <p>
 * 
 * @see VerbGroupSpec#coordinate(VerbGroupSpec)
 * @see SPhraseSpec#addVerb(String)
 * 
 * @author agatt
 * @adapted by fhielkema
 * 
 */

public class CoordinateVerbGroupSpec extends VerbGroupSpec {

	private boolean aggregate = true;

	private Conjunction conjunction = Conjunction.AND;

	List<Verb> coordinates;

	/**
	 * Constructs a new instance of <code>CoordinateVerbGroupSpec</code>
	 * 
	 */
	public CoordinateVerbGroupSpec() {
		super();
		coordinates = new ArrayList<Verb>();
	}

	/**
	 * Constructs a new instance of <code>CoordinateVerbGroupSpec</code> with
	 * a list of {@link simplenlg.lexicon.lexicalitems.Verb}s as children.
	 * 
	 * @param coords
	 *            One or more verbs
	 */
	public CoordinateVerbGroupSpec(Verb... coords) {
		this();

		for (Verb v : coords) {
			addCoordinate(v);
		}
	}

	/**
	 * Constructs a new instance of <code>CoordinateVerbGroupSpec</code> given
	 * a string of verbs. The constructor automatically renders each verb as the
	 * head of a new <code>VerbGroupSpec</code>
	 * 
	 * @param coords
	 *            One or more <code>String</code>s (presumably verbs)
	 * 
	 */
	public CoordinateVerbGroupSpec(StringPhraseSpec... coords) {
		this();

		for (StringPhraseSpec s : coords) {
			Verb v = new Verb(s.getHead());
			v.setAnchor(s.getAnchor());
			addCoordinate(v);
		}
	}

	/**
	 * @param coord
	 *            A conjunction defined in
	 *            {@link simplenlg.lexicon.lexicalitems.Conjunction}
	 * @see simplenlg.lexicon.lexicalitems.Conjunction
	 */
	public void setConjunction(Conjunction coord) {
		conjunction = coord;
	}

	/**
	 * @param coord
	 *            The <code>String</code> of the conjunction.
	 */
	public void setConjunction(String coord) {
		conjunction = Conjunction.getConjunction(coord);
	}

	/**
	 * 
	 * @return the {@link simplenlg.lexicon.lexicalitems.Conjunction} in this
	 *         coordinate verb phrase.
	 */
	public Conjunction getConjunction() {
		return conjunction;
	}

	/**
	 * Adds the head verb of a {@link VerbGroupSpec} as a child of this coordinate verb group. 
	 * Note that the argument can itself 
	 * be a <code>CoordinateVerbGroupSpec</code>, in which case,
	 * all its children become children of this one.
	 *  All children of a 
	 * <code>CoordinateVerbGroupSpec</code> inherit its features.
	 * 
	 * @param vg
	 *            The verb group to be added.
	 * @see VerbGroupSpec#coordinate(VerbGroupSpec)
	 */
	public void addCoordinate(VerbGroupSpec vg) {	
		
		if( vg instanceof CoordinateVerbGroupSpec ) 
			coordinates.addAll( ((CoordinateVerbGroupSpec)vg).getCoordinates());
		else if (coordinates.size() == 0)
		{
			setPassive(vg.isPassive());
			setProgressive(vg.isProgressive());
			setPerfect(vg.isPerfect());
			setNegated(vg.isNegated());
			setForm(vg.getForm());
			setMood(vg.getMood());
			coordinates.add(vg.getVerbalHead());
		}
		else 
			coordinates.add(vg.getVerbalHead());
	}
	
	/**
	 * Add a Verb. This method is exactly like {@link CoordinateVerbGroupSpec#addCoordinate(VerbGroupSpec)}
	 * except that it takes a verb as argument.
	 * 
	 * @param v
	 *            The <code>Verb</code>
	 * @see CoordinateVerbGroupSpec#addCoordinate(VerbGroupSpec)
	 * @see VerbGroupSpec#coordinate(VerbGroupSpec)
	 */
	public void addCoordinate( Verb v ) {
		coordinates.add(v);
	}

	/**
	 * 
	 * @return The list of coordinates, children of this phrase.
	 */
	public List<Verb> getCoordinates() {
		return coordinates;
	}

	/**
	 * The aggregation parameter determines whether the
	 * <code>CoordinateVerbGroupSpec</code> is realised with a single
	 * auxiliary phrase with wide scope over all the coordinates or not. By
	 * default, this is set to <code>true</code>. This accounts for
	 * "aggregated" coordinate VPs such as <i>john was eating and drinking</i>.
	 * If set to false using this method, the example becomes <i>john was eating
	 * and was drinking</i>.
	 * 
	 * @param aggr
	 */
	public void setAggregate(boolean aggr) {
		this.aggregate = aggr;
	}

	public boolean isAggregated() {
		return aggregate;
	}


	@Override
	public List<AnchorString> realise(Realiser r) 
	{		
		List<AnchorString> auxRealisation = realiseAuxiliary(r);
		String realisation;
		List conjuncts = new ArrayList();
		
		for (Verb v : coordinates) {
			List<AnchorString> mainVerb = new ArrayList<AnchorString>();
			if (aggregate)
			{
				mainVerb.addAll(auxRealisation);
				mainVerb.add(new AnchorString(" ", null));
			}
			mainVerb.addAll(realiseMainVerb(r, v));
			conjuncts.add(mainVerb);																	
		}
		
		List<AnchorString> result = new ArrayList<AnchorString>();
		if( !aggregate )
			result = r.realiseConjunctList(conjuncts, conjunction.getBaseForm() );
		else 
		{
			result.addAll(auxRealisation);
			result.add(new AnchorString(" ", null));
			result.addAll(r.realiseConjunctList(conjuncts, conjunction.getBaseForm()));
		}
		return flash(result);
	}
	
	private List<AnchorString> realiseMainVerb (Realiser r, Verb verb) {
		// realise verb as main verb in this VG
		// do this by temporarily setting head to verb
		Verb oldHead = getVerbalHead();
		setHead(verb);
		List<AnchorString> realisation = realiseMainVerb(r);
		setHead(oldHead);
		return realisation;
	}

}
