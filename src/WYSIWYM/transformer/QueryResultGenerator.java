package WYSIWYM.transformer;

import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.FeedbackText;
import WYSIWYM.model.QueryResultGraph;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.util.SurfaceRealisationException;
import WYSIWYM.util.TextPlanningException;

/**	QueryResultGenerator generates the feedback text of all objects that matched
 *	a certain query. This text can be expanded by the user in the same way as a 
 *	browsing description.
 *	There is a separate generator because a QueryResultGraph has >1 root.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 24-09-2008
 */
public class QueryResultGenerator extends BrowsingGenerator
{
	/**	Constructs this generator to generate descriptions of the SemanticGraph of a Query result.
	 *	@param graph QueryResultGraph with search results
	 *	@param r Ontology
	 *	@param s SesameReader
	 */
	public QueryResultGenerator(QueryResultGraph graph, OntologyReader r, SesameReader s)
	{
		super(graph, r, s);
	}
	
	/**	Initialises the generator by adding anchors.
	 */
	public void init()
	{
		addAnchors();
		getGraph().stopFlashing();
	}
	
	/**	Overload: Updates the feedbacktext when the semantic graph has been changed.
	 *	@throws SurfaceRealisationException if an error occurs during surface realisation
	 *	@throws TextPlanningException in an error occurs during text planning
	 */
	public void updateText() throws SurfaceRealisationException, TextPlanningException
	{	//plan text
		ContentPlan plan = new QueryResultPlanner(reader, getGraph()).plan();
		SurfaceRealiser sr = new SurfaceRealiser();
		setText(new FeedbackText(sr.realise(plan)));
	}
	
	/**	Returns the graph
	 *	@return QueryResultGraph
	 */
	public QueryResultGraph getGraph()
	{
		return (QueryResultGraph) super.getGraph();
	}
}