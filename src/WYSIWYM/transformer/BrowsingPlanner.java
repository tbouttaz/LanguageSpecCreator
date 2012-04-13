package WYSIWYM.transformer;

import WYSIWYM.model.SemanticGraph;
import WYSIWYM.ontology.OntologyReader;

/**
 *	BrowsingPlanner plans the text produced for a browsing user.
 *
 * @author Feikje Hielkema
 *
 *	@version 1.2 25-03-2007
 *
 *	@deprecated	
 */

public class BrowsingPlanner extends TextPlanner
{
	private String paragraph;
	
	/**	Constructs an empty text planner, taking only an ontology reader
	 *
	 *	@param r Ontology
	 */
    public BrowsingPlanner(OntologyReader r) 
    {
    	super(r);
    }
    
    /**	Constructs a text planner with a SemanticGraph
	 *
	 *	@param r Ontology
	 *	@param g SemanticGraph	
	 */
    public BrowsingPlanner(OntologyReader r, SemanticGraph g) 
    {
    	super(r, g);
    }
}	