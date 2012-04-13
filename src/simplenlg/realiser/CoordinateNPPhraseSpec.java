package simplenlg.realiser;

import java.util.ArrayList;
import java.util.List;

import simplenlg.features.Person;
import simplenlg.lexicon.lexicalitems.Conjunction;

/**
 * This class represents a coordinate Noun Phrase consisting of:
 * <ul>
 * <li>An arbitrary number of coordinates</li>
 * <li>A {@link simplenlg.lexicon.lexicalitems.Conjunction}. The default is "and"</li>
 * </ul>
 * 
 * <p>
 * Examples:
 * <ul>
 * <li><strong>The man, the boy and the girl</strong> were in the park</li>
 * <li><strong>Every woman and her little sister</strong> went to the meeting</li>
 * </ul>
 * 
 * <p>
 * By default, all the child NPs of a coordinate NP are realised with their own
 * determiner.
 * 
 * @see NPPhraseSpec
 * @author agatt
 * @adapted by fhielkema
 * 
 */

public class CoordinateNPPhraseSpec extends NPPhraseSpec {

	private Conjunction conjunction = Conjunction.AND;

	private List<NPPhraseSpec> coordinates;

	/**
	 * Construstcs an instance of <code>COordinateNPPhraseSpec</code> with no
	 * child noun phrases.
	 * 
	 */
	public CoordinateNPPhraseSpec() {
		super();
		coordinates = new ArrayList<NPPhraseSpec>();
	}

	/**
	 * Constructs a new <code>CoordinateNPPhraseSpec</code> with the given set
	 * of <code>NPPhraseSpec</code> children.
	 * 
	 * @param coords
	 */
	public CoordinateNPPhraseSpec(NPPhraseSpec... coords) {
		this();

		for (NPPhraseSpec np : coords) {
			coordinates.add(np);			
		}
	}

	/**
	 * 
	 * @param coord
	 *            The {@link simplenlg.lexicon.lexicalitems.Conjunction} to use in this
	 *            coordinate NP.
	 */
	public void setConjunction(Conjunction coord) {
		conjunction = coord;
	}

	/**
	 * @param coord
	 *            The string conjunction to use in this coordinate NP. The
	 *            method tries to find the conjunction with this string in the
	 *            {@link simplenlg.lexicon.lexicalitems.Conjunction} <code>enum</code>.
	 */
	public void setConjunction(String coord) {
		conjunction = Conjunction.getConjunction(coord);
	}

	/**
	 * 
	 * @return The {link @simplenlg.lexicon.lexicalitems.Conjunction} in this coordinate NP.
	 */
	public Conjunction getConjunction() {
		return conjunction;
	}

	/**
	 * Add a coordinate to this <code>COordinateNPPhraseSpec</code>.
	 * 
	 * @param np
	 *            The new child.
	 */
	public void addCoordinate(NPPhraseSpec np) {
		coordinates.add(np);		
	}

	/**
	 * 
	 * @return The <code>java.util.List</code> of {@link NPPhraseSpec}
	 *         children of this <code>CoordinateNPPhraseSpec</code>.
	 */
	public List<NPPhraseSpec> getCoordinates() {
		return coordinates;
	}
	
	@Override
	public boolean isPlural() {
		return coordinates.size() > 1 && conjunction != Conjunction.OR;
	}

	/**
	 * This method always returns <code>Person.THIRD</code> since a coordinate
	 * NP always triggers third-person agreement with the verb.
	 */
	public Person getPerson() {
		return Person.THIRD;
	}

	@Override
	public List<AnchorString> realise(Realiser r) {
		List<AnchorString> result = new ArrayList<AnchorString>();

		for (NPPhraseSpec node : coordinates) {
			result.addAll(node.realise(r));
		}
	
		result = r.realiseConjunctList(result, conjunction.getBaseForm());
		return flash(result);
	}
}
