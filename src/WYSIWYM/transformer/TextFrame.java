package WYSIWYM.transformer;

import WYSIWYM.model.QueryGraph;
import WYSIWYM.model.QueryNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.OntologyInputException;

import com.hp.hpl.jena.ontology.OntClass;

/**	TextFrame is a document type-to-semantic graph mapper, to create the initial
 *	semantic graph. It does this by looking up the given type in the ontology, and
 *	creating edges and nodes for all the properties with minimum cardinality 1.
 *	All the leaves of the resulting graph will need to be given labels by either
 *	the AutomaticGenerator or the user!
 *
 *	@author Feikje Hielkema
 *	@version 1.0 26-10-2006
 *
 *	@version 1.1 31-10-2007
 *
 *	@version 1.2 08-02-08
 */
public class TextFrame extends SemanticGraphTransformer
{
	private String doctype;
	private OntologyReader reader;

	/**	Initialises the TextFrame. Finds the equivalent Ontology class, creates
	 *	the graph with it's root, and then expands the Graph with information about the user,
	 *	the date of deposit, and the project.
	 *
	 *	@param	type The type of resource we want to deposit
	 *	@param	user User ID
	 *	@param 	projectID Project ID, may be null
	 *	@param	userInfo SemanticGraphTransformer containing all information about this user in the archive
	 *	@param	r Ontology
	 *	@param	query True if this is the querying module, and we need a QueryGraph
	 *	@throws	OntologyInputException if there was an error extracting information from the archive
	 */
	public TextFrame(String type, String user, String projectID, SemanticGraphTransformer userInfo, OntologyReader r, boolean query) throws OntologyInputException
	{
		super(userInfo, r);
		if (query)	//if it's a query, we don't need to include the user info
			setGraph(new QueryGraph(user));
		reader = r;	
		doctype = type;
		OntClass c = r.getClass(type);
		if (c == null)
			throw new OntologyInputException("Class " + type + " not in ontology");
		
		SGNode userNode = getGraph().getRoot();	//store old root, because we need to pass it to AutomaticGenerator
		SGNode root = new SGNode(type);
		if (query)
			root = new QueryNode(type);
		root.setRemovable(false);
		addNode(root);
		getGraph().setRoot(root);
		
		if (!query)
			AutomaticGenerator.expandGraph(this, userNode, projectID);	
		getGraph().stopFlashing();
	}
	
	/**	Initialises the TextFrame of a query. Finds the equivalent Ontology class, creates
	 *	the graph with it's root, and then expands the Graph with information about the user and
	 *	the date of deposit.
	 *
	 *	@param	type Te type of resource we want to find.
	 *	@param user User ID
	 *	@param r Ontology
	 *	@throws	OntologyInputException if there was an error extracting information from the archive
	 */
	public TextFrame(String type, String user, OntologyReader r) throws OntologyInputException
	{
		super(new QueryGraph(user), r);
		reader = r;	
		doctype = type;
		OntClass c = r.getClass(type);
		if (c == null)
			throw new OntologyInputException("Class " + type + " not in ontology");
		
		SGNode userNode = getGraph().getRoot();	//store old root, because we need to pass it to AutomaticGenerator
		SGNode root = new QueryNode(type);
		root.setRemovable(false);
		addNode(root);
		getGraph().setRoot(root);
		getGraph().stopFlashing();
	}
}