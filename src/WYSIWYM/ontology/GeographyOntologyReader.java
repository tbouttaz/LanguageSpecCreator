package WYSIWYM.ontology;

import java.io.InputStream;

import WYSIWYM.util.OntologyInputException;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**	This class extends the default ontology reader simply to read a different ontology,
 *	i.e. the geography ontology.
 *	For the rest it does the same things as OntologyReader.
 *
 *	@author Feikje Hielkema
 *	@version 1.5 21-08-2008
 */
public class GeographyOntologyReader extends OntologyReader
{	
	/**	Create a reader using a given Jena model
	 *
	 *	@param ont	Jena OntModel
	 */
	public GeographyOntologyReader(OntologyReader ont)
	{
		super(ont);
		baseURI = "http://www.mooney.net/geo";		
	}
	
	/**	Reads the ontology from a file and constructs a Jena model
	 *
	 *	@throws OntologyInputException
	 */
	public GeographyOntologyReader() throws OntologyInputException
	{
		baseURI = "http://www.mooney.net/geo";
		InputStream in = null;
		try
		{		
			String path = "data/";
			in = getClass().getClassLoader().getResourceAsStream(path + "geography.xml");
			ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
			ontology = (OntModel) ontology.read(in, baseURI);
			in.close();
			System.out.println("Read geography ontology");
			ontology.setNsPrefix(OntologyWriter.GEOGRAPHY, baseURI);
			initMaps();
			
		}
		catch (Exception e)
		{
			System.out.println("Unable to find specified file in OntologyReader");
			try
			{
				in.close();
			}
			catch (Exception ex)
			{}
			throw(new OntologyInputException(e.getMessage()));
		}
	}
}
	