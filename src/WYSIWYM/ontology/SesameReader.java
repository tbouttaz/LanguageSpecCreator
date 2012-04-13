package WYSIWYM.ontology;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.memory.MemoryStore;

import WYSIWYM.util.SesameException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**	SesameReader retrieves an ontology from a Sesame repository (local or remote)
 *	and puts it either in a Sesame graph or in a OntologyModel
 *
 *	THE URLS TO THE POLICYGRID SESAME REPOSITORIES HAVE BEEN REMOVED; TO USE THIS CLASS
 *	YOU MUST SPECIFY YOUR OWN!
 *	
 *	@author Feikje Hielkema
 *	@version 1.2 26-02-2008
 */

public class SesameReader
{
	private RepositoryConnection connection;
	private Repository sesameRepository;
	/**	PolicyGrid ontologies' general base URI */
	public static final String baseURI = "http://www.policygrid.org/";
	/**	Name of Sesame repository */
	public static final String repository = "ourspacesVRE";
//	public static final String repository = "ourSpaces";
	/**	Sesame URL */	
	public String sesame = "http://howling.esc.abdn.ac.uk:8081/openrdf-sesame";	

	
	/**	Constructs a SesameReader to a repository. Use only for a local repository; for a a remote repository, use
	 *	SesameReader(String,String).
	 *	@param local If true, this will create a local repository. If false, it will access a global repository.
	 *	@throws SesameException if there is an error accessing the repository.
	 */
	public SesameReader(boolean local) throws SesameException
	{
		this(repository, local);
	}
	
	/**	Constructs a SesameReader to the named repository. Use only for a local repository; for a a remote repository, use
	 *	SesameReader(String,String).
	 *	@param repName Repository name	
	 *	@param local If true, this will create a local repository. If false, it will access a global repository.
	 *	@throws SesameException if there is an error accessing the repository
	 */
	public SesameReader(String repName, boolean local) throws SesameException
	{
		try
		{	
			if (local)
				getLocalRepository();
			else
				getRemoteRepository(repName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}	
	
	/**	Constructs a SesameReader to the named repository.
	 *	@param url Sesame url
	 *	@param repName Repository name	
	 *	@throws SesameException if there is an error accessing the repository
	 */
	public SesameReader(String url, String repName) throws SesameException
	{
		sesame = url;
		try
		{
			getRemoteRepository(repName);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Set the Sesame url
	 *	@param url Sesame url
	 */
	public void setSesameURL(String url)
	{
		sesame = url;
	}
	
	/**	Constructs a Jena OntModel from the RDF in the repository.
	 *	This method was developed for LIBER, but is not used because it takes about
	 *	45 seconds to read all three ontologies from Sesame! Better to read them
	 *	from local files instead.
	 *	
	 *	@throws SesameException if there is an error reading from the repository
	 */
	public OntModel getOntModel()  throws SesameException
	{
		try
		{
			return readFromRepository();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Writes RDF data from the inputstream into the sesame repository
	 *	@param in InputStream
	 *	@throws SesameException if there is an error writing to the repository
	 */
	public void write(InputStream in) throws SesameException
	{
		try
		{
			connection.add(in, baseURI, RDFFormat.RDFXML);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Sends the SPARQL query to the repository and results the results in a 
	 *	Sesame GraphQueryResult. GraphQueryResult is an Iterator of Sesame statements,
	 *	which correspond to RDF triples.
	 *	@param sparql SPARQL query
	 *	@return GraphQueryResult
	 *	@throws SesameException if there is an error querying the repository
	 */
	public GraphQueryResult queryGraph(String sparql) throws SesameException
	{
		try
		{
			GraphQuery query = connection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
			GraphQueryResult result = query.evaluate();
		
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RDFXMLWriter writer = new RDFXMLWriter(out);
			query.evaluate(writer);
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Takes a SPARQL query and sends it to Sesame; the answer is returned in XML.
	 *	
	 *	@param sparql String containing SPARQL query
	 *	@return XML String
	 *	@throws SesameException if there is an error querying the repository
	 */
	public String queryXML(String sparql) throws SesameException
	{
		try
		{
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			SPARQLResultsXMLWriter writer = new SPARQLResultsXMLWriter(out);
			query.evaluate(writer);
			return out.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Takes a SPARQL query and sends it to Sesame; the answer is returned as a
	 *	LIBER QueryResult. The QueryResult orders all search results by the Sesame ID
	 *	in the first variable of the query.
	 *	
	 *	@param sparql String containing SPARQL query
	 *	@return QueryResult
	 *	@throws SesameException if there is an error querying the repository
	 */
	public QueryResult queryBinding(String sparql) throws SesameException
	{
		return queryBinding(sparql, null);
	}

	/**	Takes a SPARQL query and sends it to Sesame; the answer is returned as a
	 *	LIBER QueryResult. This method assumes that all search results belong to
	 *	the object with the given ID! The results are all assigned to the same BindingSet
	 *	in QueryResult.
	 *	
	 *	@param sparql String containing SPARQL query
	 *	@param id Sesame ID
	 *	@return QueryResult
	 *	@throws SesameException if there is an error querying the repository
	 */	
	public QueryResult queryBinding(String sparql, String id) throws SesameException
	{
		try
		{
			TupleQuery query = connection.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			query.evaluate(builder);
		
			TupleQueryResult tuple = builder.getQueryResult();
			QueryResult result = new QueryResult(tuple.getBindingNames());
			while(tuple.hasNext())
				result.add((BindingSet) tuple.next(), id);
		
			tuple.close();
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Connects to remote repository 
	 *
	 *	@param	repName name of the remote Sesame repository
	 *	@throws SesameException if there is an error querying the repository 	
	 */
	private void getRemoteRepository(String repName) throws SesameException
	{
		try
		{
			sesameRepository = new HTTPRepository(sesame, repName);
			sesameRepository.initialize();
			connection = sesameRepository.getConnection();	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Creates a local repository from the given filename.
	 *	This repository is not persistent, but will disappear with garbage
	 *	collection!
	 */
	private void getLocalRepository() throws SesameException
	{
		try
		{
			sesameRepository = new SailRepository(new MemoryStore());
			sesameRepository.initialize();
			connection = sesameRepository.getConnection();
			
			String path = "data/DatabaseRdf.rdf";
			InputStream in = getClass().getClassLoader().getResourceAsStream(path);
			connection.add(in, baseURI, RDFFormat.RDFXML);	//read data from file
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	This reads the data from a remote repository, and returns a Jena model
	 *	containing that data.
	 *
	 *	@return	OntModel, either a Sesame Graph or a Jena model
	 *	@throws Exception
	 */
	private OntModel readFromRepository() throws SesameException
	{ 
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			RDFXMLWriter writer = new RDFXMLWriter(out);
			connection.export(writer);
	
			InputStream in = new ByteArrayInputStream(out.toByteArray());
			OntModel result = ModelFactory.createOntologyModel();
			result = (OntModel) result.read(in, baseURI);
			in.close();
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Closes the connection to the Sesame repositor
	 *	@throws Throwable if there is any error
	 */
	protected void finalize() throws Throwable
	{
		connection.close();
		super.finalize();
	}
}	