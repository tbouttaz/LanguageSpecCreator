package simplenlg.realiser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Feikje Hielkema  13-Feb-07
 *
 */
public class AggregatePhraseSpec extends PhraseSpec 
{
	
	private List<PhraseSpec> constituents = new ArrayList<PhraseSpec>();
	private List<PhraseSpec> frontModifiers = new ArrayList<PhraseSpec>();  // modifiers which occur at beginning of the conjunction
	private List<PhraseSpec> endModifiers = new ArrayList<PhraseSpec>();  // modifiers which occur at end of conjunction
	private String conjunct = "and";
	
	public AggregatePhraseSpec() 
	{ // constructor
		super();
	}
	
	public String getHead()
	{
		return constituents.get(0).getHead();
	}
	
	public List<PhraseSpec> getConstituents()
	{
		return constituents;
	}
	
	public void addSpec(PhraseSpec p)
	{
		constituents.add(p);
	}

	/*	This phrasespec is by definition plural, so setting singular to either true
	 *	or false doesn't make sense
	 */
	public void setSingular(boolean s)
	{}

	public void setQuote(boolean q)
	{
		for (int i = 0; i < constituents.size(); i++)
			constituents.get(i).setQuote(q);
	}
	
	public void setConjunct(String c)
	{
		conjunct = c;
	}
	
	public void setGenitive(boolean g)
	{
		for (int i = 0; i < constituents.size(); i++)
			constituents.get(i).setGenitive(g);
	}
	
	public void setSubject(boolean s)
	{
		if (constituents.size() == 0)
			return;
		if (!(constituents.get(0) instanceof NPPhraseSpec))
			return;
		for (int i = 0; i < constituents.size(); i++)
			((NPPhraseSpec) constituents.get(i)).setSubjectNP(s);
	}

	public void addFrontModifier(Object modifier) {  // add a front modifier
		// put new front modifiers at beginning of list
		if (modifier != null)
			frontModifiers.add(0, makePhraseSpec(modifier));
	}	
	
	public void addEndModifier(Object modifier)
	{
		if (modifier != null)
			endModifiers.add(0, makePhraseSpec(modifier));
	}
	
	@Override
	public List<AnchorString> realise(Realiser r) 
	{	
		List<AnchorString> result = new ArrayList<AnchorString>();
		List<AnchorString> frontModifierText = r.realiseList(frontModifiers);
		List<AnchorString> endModifierText = r.realiseList(endModifiers);
		if (constituents.size() == 0)
			return result;
		if (constituents.size() == 1)
			return constituents.get(0).realise(r);
			
		for (int i = 0; i < constituents.size() - 2; i++)
		{
			result.addAll(constituents.get(i).realise(r));
			r.addPunctuation(result, ',');
		}
		
		result.addAll(constituents.get(constituents.size() - 2).realise(r));
		if (r.PUNCTUATION.indexOf(conjunct) < 0)	//if the conjunctor is not punctuation
			result.add(new AnchorString(conjunct, null));	//add a new anchorstring
		else	//else only add the punctuation if there is no other already there
			r.addPunctuation(result, conjunct.charAt(0));			
		result.addAll(constituents.get(constituents.size() - 1).realise(r));
		result = r.listWords(frontModifierText, result, endModifierText);
		return flash(result);
	}

	@Override
	public boolean isPlural() 
	{
		if (constituents.size() > 1)
			return true;
		return false;
	}

}
