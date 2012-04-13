package WYSIWYM.transformer;

import java.util.List;

import simplenlg.realiser.AnchorString;
import simplenlg.realiser.DocStructure;
import simplenlg.realiser.PhraseSpec;
import simplenlg.realiser.TextSpec;
import WYSIWYM.model.DTNode;
import WYSIWYM.model.DependencyTree;
import WYSIWYM.model.QueryPlan;
import WYSIWYM.util.SurfaceRealisationException;

/**
 *	QueryRealiser realises queries; the main difference with the standard SurfaceRealiser
 *	is that the surface form is an html-list.
 *
 * @author Feikje Hielkema
 * @version 1.2 31-01-2008
 */

public class QueryRealiser extends SurfaceRealiser
{
	/**	Default constructor
	 */
	public QueryRealiser()
	{
		super();
	}
	
    /**	Overload. Produces the surface form of the QueryPlan and returns it
     *	as a List of AnchorStrings. The only difference in realisation with the
     *	other modules is that the QueryPlan is realised as a nested list.
     *
     *	@param plan	QueryPlan
     *	@return	List<AnchorString> with the feedback text
     *	@throws SurfaceRealisationException
     */
    public List<AnchorString> realise(QueryPlan plan) throws SurfaceRealisationException
    {	//create a spec to contain the list (full surface form), and add a spec containing the list intro
    	TextSpec result = new TextSpec();
    	TextSpec intro = new TextSpec().promote(DocStructure.LISTHEADER);
    	intro.addSpec(plan.getIntro());
    	result.addSpec(intro);
    	
    	TextSpec list = new TextSpec().promote(DocStructure.LIST);
    	List<QueryPlan.QueryItem> items = plan.getQuery();
    	for (int i = 0; i < items.size(); i++)
    		list.addSpec(realiseQueryItem(items.get(i)));

    	result.addSpec(list);
    	result.setDocument();
    	realiser.setHTML(true);	//output html
    
    	return realiser.realise(result);
    }
    
    /**	Realises a QueryItem, recursing if necessary
     */
    private TextSpec realiseQueryItem(QueryPlan.QueryItem item) throws SurfaceRealisationException
    {
    	DependencyTree dt = item.content;
    	PhraseSpec s = realiseNode((DTNode) dt.getRoot());
	//	tweakAggregate(s, dt);
		aggregateNode = null;
		conjunctor = "";
		s.setFlash(dt.flash());
		
		TextSpec spec = new TextSpec().promote(DocStructure.LISTITEM);
		if (item.children.size() == 0)
			spec.addSpec(s);
		else
		{
			TextSpec header = new TextSpec().promote(DocStructure.LISTHEADER);
			header.addSpec(s);
			spec.addSpec(header);
			
			TextSpec list = new TextSpec().promote(DocStructure.LIST);
			for (int i = 0; i < item.children.size(); i++)
				list.addSpec(realiseQueryItem(item.children.get(i)));
			spec.addSpec(list);
		}
		return spec;
    }
}