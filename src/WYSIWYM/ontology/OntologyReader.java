package WYSIWYM.ontology;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import liber.edit.client.FormInfo;
import liber.edit.client.Hierarchy;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.util.OntologyInputException;

import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.DataRange;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;

/**	OntologyEditor wraps a Jena class OntModel and provides information about this ontology,
 *	e.g. which properties a class can (and must) have, what the domain and range of 
 *	properties are, what the cardinality restrictions on a class are, comments, etc.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 20-10-2006
 *
 *	@version 1.5 21-08-2008
 */
public class OntologyReader
{
	protected OntModel ontology;
	protected String baseURI;
	
	//datatypes
	public static final String INT = "int";
	public static final String FLOAT = "float";
	public static final String STRING = "string";
	public static final String BOOLEAN = "boolean";	
	
	//constants related to the Date classes in the PolicyGrid Utility ontology
	public static final String DATE = "Date";
	public static final String DATEPOINT = "DatePoint";
	public static final String DATEPERIOD = "DatePeriod";
	public static final String DAY = "Day";
	public static final String BEGINDAY = "BeginDay";
	public static final String ENDDAY = "EndDay";
	public static final String MONTH = "Month";
	public static final String BEGINMONTH = "BeginMonth";
	public static final String ENDMONTH = "EndMonth";
	public static final String YEAR = "Year";
	public static final String BEGINYEAR = "BeginYear";
	public static final String ENDYEAR = "EndYear";
	
	//terminology with which nl-info is stored in the ontology
	public final static String MENU = "LIBERmenu=";
	public final static String PHRASE = "LIBERphrase=";
	public final static String PROPERNAME = "LIBERpropername";
	public final static String NONE = "-NONE-";
	
	//these are objects that speed up the retrieval of information about domain and ranges of properties etc., 
	//as LIBER needs to do that a lot and it is rather slow
	private Map<String,OntProperty> propertyMap = new HashMap<String,OntProperty>();
	private Map<String,OntClass> classMap = new HashMap<String,OntClass>();
	private Map<String, List<OntProperty>> domainMap, rangeMap;
	private Map<String, Map<String, Integer[]>> cardinalityMap;
	private Map<OntProperty,Map<String,String>> submenuMap;
	private List<OntProperty> properNameProperties = new ArrayList<OntProperty>();
	private Map<String,String> nlExpressions;
	// Store list of processed classes to check that we don't process the same class several time (to avoid infinite recursion in getClasses(String))
	private List<OntClass> processedClasses = new ArrayList<OntClass>();
	
	/**	Default constructor.
	 * 	Imports all three PolicyGrid ontologies into one Jena model, and sets the
	 *	appropriate prefixes.
	 *
	 *	This is the constructor that LIBER uses.
	 *
	 *	@throws OntologyInputException if the ontologies could not be read
	 */
	public OntologyReader() throws OntologyInputException
	{
		InputStream in = null;
		try
		{	
			// Ontology for ourSpaces repository:
//			String path = "/Users/thomas/Documents/Development/LIBER/liber/data/";
			// Ontology for ourspacesVRE repository:
			String path = "/Users/thomas/Documents/workspace/OurSpaces/trunk/web/ontologies/";
			// Ontology adapted to Liber (with portability tool)
//			String path = "/Users/thomas/Documents/workspace/SpecificationCreation/data/utility/";
//			String path = "data/";
			//in = getClass().getClassLoader().getResourceAsStream(path + "Utility.xml");			
//			in = new FileInputStream(path + "utility.rdf");
			in = new FileInputStream(path + "Utility.owl");
//			in = new FileInputStream(path + "ontology.rdf");
			ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/utility.owl");	
			in.close();
			System.out.println("Read utility ontology");

//			path = "/Users/thomas/Documents/workspace/OurSpaces/trunk/web/ontologies/";
			ontology.getDocumentManager().setProcessImports(false);	//left to itself Jena looks for the imports online, and currently they are not there!
		//	in = getClass().getClassLoader().getResourceAsStream(path + "Resource.xml");
//			in = new FileInputStream(path + "resource.rdf");
//			in = new FileInputStream(path + "Resource.owl");
//			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/resource.owl");
//			in.close();
//			System.out.println("read resource ontology");	
			
		//	in = getClass().getClassLoader().getResourceAsStream(path + "Task.xml");
//			in = new FileInputStream(path + "task.rdf");
//			in = new FileInputStream(path + "Task.owl");
//			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/task.owl");
//			in.close();
//			System.out.println("read task ontology");
			
			// new ontology file: foaf (used to describe a personne)
			in = new FileInputStream(path + "foaf.owl");
			ontology = (OntModel) ontology.read(in, "http://xmlns.com/foaf/0.1/");
			in.close();
			System.out.println("read foaf ontology");

			// new ontology file: opm
			in = new FileInputStream(path + "opm.owl");
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/opm.owl");
			in.close();
			System.out.println("read opm ontology");

			// new ontology file: opm-Resource
//			path = "/Users/thomas/Documents/workspace/SpecificationCreation/data/opm-Resource/";
			in = new FileInputStream(path + "opm-Resource.owl");
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/opm-resource.owl");
			in.close();
			System.out.println("read opm-Resource ontology");

			// new ontology file: OurSpacesVRE
			in = new FileInputStream(path + "OurSpacesVRE.owl");
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/ourspacesVRE.owl");
			in.close();
			System.out.println("read OurSpacesVRE ontology");

			// new ontology file: project
			in = new FileInputStream(path + "project.owl");
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/project.owl");
			in.close();
			System.out.println("read project ontology");

			// new ontology file: sioc
			in = new FileInputStream(path + "sioc.owl");
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/sioc.owl");
			in.close();
			System.out.println("read sioc ontology");

			// new ontology file: geo-properties
			in = new FileInputStream(path + "geo-properties.owl");
			ontology = (OntModel) ontology.read(in, "http://www.policygrid.org/geo-properties.owl");
			in.close();
			System.out.println("read geo-properties ontology");

			ontology.setNsPrefix(OntologyWriter.UTILITY, "http://www.policygrid.org/utility.owl");
			ontology.setNsPrefix(OntologyWriter.RESOURCE, "http://www.policygrid.org/resource.owl");
			ontology.setNsPrefix(OntologyWriter.TASK, "http://www.policygrid.org/task.owl");
			baseURI = "http://www.policygrid.org/";
			
			initMaps(); //seed the HashMaps with information that speeds up further processes
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
	
	/**	Constructs a copy of the given reader
	 *
	 *	@param ont OntologyReader
	 */
	public OntologyReader(OntologyReader ont)
	{
		ontology = ont.getOntology();
		propertyMap = new HashMap(ont.getPropertyMap());
		classMap = new HashMap(ont.getClassMap());
		
		domainMap = new HashMap(ont.getDomainMap().size());
		for (Iterator it = ont.getDomainMap().keySet().iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			List<OntProperty> temp = new ArrayList<OntProperty>(ont.getDomainMap().get(key));
			domainMap.put(key, temp);
		}
		
		rangeMap = new HashMap(ont.getRangeMap().size());
		for (Iterator it = ont.getRangeMap().keySet().iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			List<OntProperty> temp = new ArrayList<OntProperty>(ont.getRangeMap().get(key));
			rangeMap.put(key, temp);
		}
		
		cardinalityMap = new HashMap(ont.getCardinalityMap().size());
		for (Iterator it = ont.getCardinalityMap().keySet().iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			Map<String,Integer[]> temp = new HashMap<String,Integer[]>(ont.getCardinalityMap().get(key));
			cardinalityMap.put(key, temp);
		}
		
		submenuMap = new HashMap(ont.getSubmenuMap().size());
		for (Iterator it = ont.getSubmenuMap().keySet().iterator(); it.hasNext(); )
		{
			OntProperty key = (OntProperty) it.next();
			Map<String,String> temp = new HashMap<String,String>(ont.getSubmenuMap().get(key));
			submenuMap.put(key, temp);
		}
				
		properNameProperties.addAll(ont.getProperNameOntProperties());
		nlExpressions = new HashMap(ont.getNLExpressionMap());
	}
	
	/** Constructs a reader by creating a Jena model from the given file.
	 *	@param file File
	 *	@param baseURI The ontology's BaseURI
	 *	@throws OntologyInputException if the ontology could not be read
	 */
	public OntologyReader(File file, String baseURI) throws OntologyInputException
	{
		this.baseURI = baseURI;
		InputStream in = null;
		try
		{		
			in = new FileInputStream(file);
			ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
			ontology = (OntModel) ontology.read(in, baseURI);
			in.close();
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
	
		
	/**	Reads the ontology from a file with the given name and constructs a Jena model
	 *
	 *	@param fileName	String file name
	 *	@param baseURI The ontology's base URI
	 *	@throws OntologyInputException if the ontology could not be read
	 */
	public OntologyReader(String fileName, String baseURI) throws OntologyInputException
	{
		this.baseURI = baseURI;
		InputStream in = null;
		try
		{		
			String path = "data/";
			in = getClass().getClassLoader().getResourceAsStream(path + fileName);
			ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
			ontology = (OntModel) ontology.read(in, baseURI);	
			in.close();			
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
	
	/**	Adds another ontology from the given file to the current ontModel.
	 *	@param file File
	 *	@param baseURI The ontology's base URI
	 *	@throws OntologyInputException if the ontology could not be read
	 */
	public void addOntology(File file, String baseURI) throws OntologyInputException
	{
		InputStream in = null;
		try
		{		
			in = new FileInputStream(file);
			ontology = (OntModel) ontology.read(in, baseURI);	
			in.close();
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
	
	/**	Returns the base URI
	 *	@return String
	 */
	public String getBaseURI()
	{
		return baseURI;
	}
	
	/**	Returns a HashMap where the keys are local names of classes,
	 *	and the values are lists of the properties that have the class
	 *	in their domain
	 *	@return Map<String,List<OntProperty>>
	 */
	public Map<String, List<OntProperty>> getDomainMap()
	{
		return domainMap;
	}
	
	/**	Returns a HashMap where the keys are local names of classes,
	 *	and the values are lists of the properties that have the class
	 *	in their range
	 *	@return Map<String,List<OntProperty>>
	 */
	public Map<String, List<OntProperty>> getRangeMap()
	{
		return rangeMap;
	}
	
	/**	Returns a HashMap where the keys are local names of classes,
	 *	and the values are HashMaps with local names of properties and 
	 *	minimum & maximum cardinal constraints on them.
	 *	@return Map<String,Map<String,Integer[]>>
	 */
	public Map<String, Map<String,Integer[]>> getCardinalityMap()
	{
		return cardinalityMap;
	}
	
	/**	Returns a HashMap where the keys are local names of properties,
	 *	and the values are the OntProperties with that name
	 *	@return Map<String,OntProperty>
	 */
	public Map<String,OntProperty> getPropertyMap()
	{
		return propertyMap;
	}
	
	/**	Returns a HashMap where the keys are local names of classes,
	 *	and the values are the OntClasses with that name
	 *	@return Map<String,OntClass>
	 */
	public Map<String,OntClass> getClassMap()
	{
		return classMap;
	}
	
	/**	Creates various HashMaps that store information about local names,
	 *	domains and ranges of classes and properties, which is needed by many
	 *	of LIBER's methods. This method takes 10 sec. to run, but saves a lot
	 *	of time later on!
	 */
	protected void initMaps()
	{
		collectProperNameOntProperties();		
		collectNLExpressions();
		
		for (Iterator it = ontology.listOntProperties(); it.hasNext(); )
		{
			OntProperty p = (OntProperty) it.next();
			propertyMap.put(p.getLocalName(), p);
		}
		for (Iterator it = ontology.listNamedClasses(); it.hasNext(); )
		{
			OntClass c = (OntClass) it.next();
			System.out.println("OntClass: " + c.getURI());
//			classMap.put(c.getLocalName(), c);
			classMap.put(c.getURI(), c);
		}
		
		domainMap = new HashMap<String, List<OntProperty>>();
		rangeMap = new HashMap<String, List<OntProperty>>();
		cardinalityMap = new HashMap<String, Map<String, Integer[]>>();
		
		for (Iterator it = getClasses(); it.hasNext(); )
		{
			OntClass c = (OntClass) it.next();
			//ignores specific ontologies:
//			if (!c.toString().endsWith("User") || !c.toString().endsWith("OurSpacesAccount")){
//					|| !c.toString().startsWith("http://xmlns.com/foaf/0.1/OnlineAccount")
//					|| !c.toString().startsWith("http://rdfs.org/sioc/ns#User")) {
//			if ( !( c.toString().equals("http://www.policygrid.org/ourspacesVRE.owl#OurSpacesAccount") || c.toString().equals("http://xmlns.com/foaf/0.1/Person") ) ){
//	 	 	if (c.getEquivalentClass() != null) {
//			if ( !( c.toString().equals("http://www.policygrid.org/ourspacesVRE.owl#OurSpacesAccount") || c.toString().equals("http://rdfs.org/sioc/ns#User") ) ){
				System.out.println("\t Processing " + c.toString());
				domainMap.put(c.getLocalName(), getProperties(c, true));
				rangeMap.put(c.getLocalName(), getRangeProperties(c));
				cardinalityMap.put(c.getLocalName(), initCardinalities(c));
//			}
		}		
		collectSubmenus();	
	}
	
	/**	Returns all properties that have a class with the given local 
	 *	name in their range
	 *	@param name Class name
	 *	@return List<OntProperty> properties with that class in their range
	 */
	public List<OntProperty> getRangeProperties(String name)
	{
		if (rangeMap.containsKey(name))
			return rangeMap.get(name);
		return new ArrayList<OntProperty>();
	}
	
	/**	Returns all properties that have a class with the given local 
	 *	name in their domain
	 *	@param name Class name
	 *	@return List<OntProperty> properties with that class in their domain
	 */
	public List<OntProperty> getDomainProperties(String name)
	{
		if (domainMap.containsKey(name))
			return domainMap.get(name);
		return new ArrayList<OntProperty>();
	}
		
	/**	Returns all properties that have the given class in their range;
	 *	Used by initMaps()
	 */
	private List<OntProperty> getRangeProperties(OntClass c)
	{
		List<OntClass> classes = getSuperClasses(c);
		List<OntProperty> result = new ArrayList<OntProperty>();
		for (Iterator it = ontology.listOntProperties(); it.hasNext(); )
		{	//Get the ranges of all the properties
			OntProperty p = (OntProperty) it.next();
//			System.out.println("property " + p.getURI());
			for (Iterator i = getRangeList(p).iterator(); i.hasNext(); )
			{	//and check whether one of the classes is in it
				OntClass ontClass = (OntClass) i.next();
//				System.out.println("Range of that prop: " + ontClass.getURI());
				if (classes.contains(ontClass))
				{	//if so, add the property to the result
					result.add(p);
					break;
				}
			}
		}
		return excludeSuperProperties(result, c);
	}
	
	/**	This returns an iterator with all the properties that have the given
	 *	class as their domain. If inherited is true, it included the properties 
	 *	that have its superclasses as their domain (with the exception of those
	 *	that are superproperties of one that this class has!).
	 *	Used by initMaps
	 */
	private List<OntProperty> getProperties(OntClass c, boolean inherited)
	{
		List<OntClass> classes = new ArrayList<OntClass>();
		if (inherited)
			classes = getSuperClasses(c);	//get all superclasses, until the top of the hierarchy
		else
			classes.add(c);					//else just use this class

		List<OntProperty> result = new ArrayList<OntProperty>();
		Iterator it = ontology.listOntProperties();
		while (it.hasNext())
		{	//Get the domains of all the properties
			OntProperty p = (OntProperty) it.next();
			Iterator i = getDomain(p);
			while (i.hasNext())
			{	//and check whether the classes are in the domains
				if (classes.contains(i.next()))
				{	//if so, add the property to the result
					result.add(p);
					break;
				}
			}
		}
		return excludeSuperProperties(result, c);
	}
	
	private List<OntProperty> excludeSuperProperties(List<OntProperty> propList, OntClass c)
	{
		List<OntProperty> remove = new ArrayList<OntProperty>();
		for (int i = 0; i < propList.size(); i++)
		{
			OntProperty p = propList.get(i);
			for (Iterator it = p.listSuperProperties(); it.hasNext(); )
			{
				OntProperty superProp = (OntProperty) it.next();
				if (!superProp.getLocalName().equals(p.getLocalName()) && !c.hasDeclaredProperty(superProp, true))	//for some reason Jena 
					remove.add(superProp);			//includes the property itself in its superproperties; check for this
			}
		}
		for (int i = 0; i < remove.size(); i++)
		{	//remove all superproperties from the list
			int idx = propList.indexOf(remove.get(i));
			if (idx > -1)
				propList.remove(idx);
		}
		return propList;
	}
	
	/**	Returns a list with p's super properties
	 *
	 *	@param p OntProperty	
	 *	@return List<OntProperty> with super properties
	 */
	public List<OntProperty> getSuperProperties(OntProperty p)
	{
		List<OntProperty> result = new ArrayList<OntProperty>();
		for (Iterator it = p.listSuperProperties(); it.hasNext(); )
		{	//exclude this property
			OntProperty superProp = (OntProperty) it.next();
			if (!superProp.getLocalName().equals(p.getLocalName()))
				result.add(superProp);
		}
		return result;
	}
	
	/**	Returns a list with local names of the super properties 
	 *	of the property by this name, or an empty list if the 
	 *	property does not exist.
	 *
	 *	@param prop Property name	
	 *	@return List<String> with super properties
	 */
	public List<String> getSuperProperties(String prop)
	{
		OntProperty p = getProperty(prop);
		List<String> result = new ArrayList<String>();
		if (p != null)
		{
			List<OntProperty> superProps = getSuperProperties(p);
			for (int i = 0; i < superProps.size(); i++)
				result.add(superProps.get(i).getLocalName());
		}
		return result;
	}
	
	/**	Returns a list with p's sub-properties
	 *
	 *	@param p OntProperty	
	 *	@return List<OntProperty> with sub-properties
	 */
	public List<OntProperty> getSubProperties(OntProperty p)
	{
		List<OntProperty> result = new ArrayList<OntProperty>();
		for (Iterator it = p.listSubProperties(); it.hasNext(); )
		{	//exclude this property
			OntProperty subProp = (OntProperty) it.next();
			if (!subProp.getLocalName().equals(p.getLocalName()))
				result.add(subProp);
		}
		return result;
	}
	
	/**	Returns a list with local names of the sub-properties 
	 *	of the property by this name, or an empty list if the 
	 *	property does not exist.
	 *
	 *	@param prop Property name	
	 *	@return List<String> with sub-properties
	 */
	public List<String> getSubProperties(String prop)
	{
		OntProperty p = getProperty(prop);
		List<String> result = new ArrayList<String>();
		if (p != null)
		{
			List<OntProperty> subProps = getSubProperties(p);
			for (int i = 0; i < subProps.size(); i++)
				result.add(subProps.get(i).getLocalName());
		}
		return result;
	}
	
	/**	Returns a list with c and its c's superclasses
	 *
	 *	@param c	OntClass
	 *	@return List<OntClass> with superclasses
	 */
	public List<OntClass> getSuperClasses(OntClass c)
	{
		ArrayList<OntClass> result = new ArrayList();
		result.add(c);
//		System.out.println("added " + c.toString() + " to list");
		// add OntClass to list
		processedClasses.add(c);
		
		for (Iterator i = c.listSuperClasses(false); i.hasNext(); ) 
		{
 	 		OntClass sc = (OntClass) i.next();
 	 		if (sc.isIntersectionClass())	//stupid owl/jena construct
 	 		{
 	 			IntersectionClass ic = sc.asIntersectionClass();
 	 			Iterator it = ic.listOperands();
 	 			while (it.hasNext())
 	 			{
 	 				List<OntClass> temp = getSuperClasses((OntClass) it.next());
 	 				for (int j = 0; j < temp.size(); j++)
 	 					if (!result.contains(temp.get(j)))
 	 						result.add(temp.get(j));
 	 			}
 	 		}
 	 		else	//if sc's a normal class
 	 		{
 				List<OntClass> temp = getSuperClasses(sc);
 	 			for (int j = 0; j < temp.size(); j++)
 	 				if (!result.contains(temp.get(j)))
 	 					result.add(temp.get(j));
 			}	
		}
		for (Iterator i = c.listEquivalentClasses(); i.hasNext(); )
		{
 	 		OntClass sc = (OntClass) i.next();
 	 		if (sc.isIntersectionClass())	//stupid owl/jena construct
 	 		{
 	 			IntersectionClass ic = sc.asIntersectionClass();
 	 			Iterator it = ic.listOperands();
 	 			while (it.hasNext())
 	 			{
 	 				List<OntClass> temp = getSuperClasses((OntClass) it.next());
 	 				for (int j = 0; j < temp.size(); j++)
 	 					if (!result.contains(temp.get(j)))
 	 						result.add(temp.get(j));
 	 			}
 	 		}
 	 		else	//if it's a normal class
 	 		{
 	 			System.out.println( "super class: " + sc.toString());
 	 			//TODO: infinite recursion: check that the class has't been processed yet
 	 			if (! processedClasses.contains(sc)) {
 	 				//TODO: if it has equivalent class, only do the loop once
// 	 	 			if (sc.getEquivalentClass() == null) {
// 	 	 			} else {
//	 	 				System.out.println(sc.toString() + "'s equivalent class is :" + sc.getEquivalentClass().toString() + ". this class will be ignored");
//					}
	 				List<OntClass> temp = getSuperClasses(sc);
	 				for (int j = 0; j < temp.size(); j++)
	 	 				if (!result.contains(temp.get(j)))
	 	 					result.add(temp.get(j));
				}
 			}
		}
		return result;		
	}
	
	/**	Returns the class that is lowest in the hierarchy. This method assumes 
	 *	that all the given subclasses are sub-or superclasses of each other!
	 *	If this is not the case, the method will not work reliably.
	 *
	 *	@param classes List<String> with local names of classes
	 *	@return lowest OntClass in hierarchy
	 */
	public OntClass getMostSpecificClass(List<String> classes)
	{
		List<OntClass> list = new ArrayList<OntClass>();
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < classes.size(); i++)
		{	//the same class name may be in the list more than once
			if (names.contains(classes.get(i)))
				continue;
			names.add(classes.get(i));
			OntClass c = getClass(classes.get(i));
			if (c != null)
				list.add(c);
		}
		
		if (list.size() == 0)
			return null;
		
		OntClass result = list.get(0);
		for (int i = 1; i < list.size(); i++)
			if (getSubClasses(result).contains(list.get(i)))
				result = list.get(i);

		return result;
	}
	
	/**	Returns the class that is highest in the hierarchy, or one that is higher
	 *	than all of them (e.g. paper and presentation -> resource).
	 *	The method does not necessarily return the most specific superclass!
	 *
	 *	@param classes List<String> with local names of classes
	 *	@return most general OntClass in hierarchy
	 */
	public OntClass getMostGeneralClass(List<String> classes)
	{
		List<OntClass> list = new ArrayList<OntClass>();
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < classes.size(); i++)
		{	//the same class name may be in the list more than once
			if (names.contains(classes.get(i)))
				continue;
			names.add(classes.get(i));
			OntClass c = getClass(classes.get(i));
			if (c != null)
				list.add(c);
		}
		
		if (list.size() == 0)
			return null;
		if (list.size() == 1)
			return list.get(0);
		
		List<OntClass> superClasses = getSuperClasses(list.get(0));	//get al superclasses of the first item
		for (OntClass s : superClasses)
		{	//for each superclass, check if all classes in list are its subclasses
			boolean suitable = true;
			List<OntClass> subClasses = getSubClasses(s);
			for (int i = 1; i < list.size(); i++)
				if (!subClasses.contains(list.get(i)))
					suitable = false;
			if (suitable)	//if so, use it. This method may not return the most
				return s;	//specific superclass!
		}

		System.out.println("ONTOLOGYREADER#getMostGeneralClass(): COULD NOT FIND SHARED SUPERCLASS");
		return list.get(0);	//the method should not ever get to this point! If it does, it really should return 'Owl:thing', but that's no ontClass in Jena (don't know why)
	}

	/**	Gets the subclasses of the given OntClass
	 *	@param c	OntClass
	 *	@return List<OntClass>: c's subclasses
	 */
	public List<OntClass> getSubClasses(OntClass c)
	{
		List<OntClass> result = new ArrayList();
		for (Iterator it = c.listSubClasses(false); it.hasNext(); )
			result.add((OntClass) it.next());
		return result;
	}
	
	/**	Gets the subclasses of the OntClass with the given local name
	 *	@param c	Class name
	 *	@return List<String> with c's subclasses
	 */
	public List<String> getSubClasses(String c)
	{
		List<String> result = new ArrayList<String>();
		OntClass cl = getClass(c);
		if (cl != null)
		{
			List<OntClass> list = getSubClasses(cl);
			for (int i = 0; i < list.size(); i++)
				result.add(list.get(i).getLocalName());
		}
		return result;
	}
	
	/**	This returns the domain of the given property. If that domain is unspecified,
	 *	it checks the domain of the super properties.
	 *
	 *	@param p	Property
	 *	@return Iterator over classes in the domain of this property or, if necessary,
	 *	its superproperties 
	 */
	public Iterator getDomain(OntProperty p)
	{
		return getDomainList(p, false).iterator();
	}
	
	/*	This returns a list with the classes in the domain of the given
	 *	property. The second parameter specifies whether subclasses are included.
	 *	@param p OntProperty
	 *	@param subclasses If true, add all subclasses of the found classes to the list
	 *	@return List<OntClass>
	 */
	public List<OntClass> getDomainList(OntProperty p, boolean subclasses)
	{
		ArrayList<OntClass> result = new ArrayList<OntClass>();
		Iterator it = p.listDomain();
		
		while (it.hasNext())
		{	//add all the classes in the domain
			OntResource r = (OntResource) it.next();				
			addClass(result, r);
		}
		if (result.isEmpty())
		{	//if the domain was empty, check the domain of the superproperties
			Iterator i = p.listSuperProperties(true);
			while (i.hasNext())
			{
				Iterator it2 = getDomain((OntProperty) i.next());
				while (it2.hasNext())
					result.add((OntClass) it2.next());
			}
		}
			
		if (result.isEmpty() || result.contains(OWL.Thing))
			for (Iterator i = getClasses(); i.hasNext(); )
				result.add((OntClass) i.next());
		
		if (subclasses)
		{
			List<OntClass> add = new ArrayList<OntClass>();
			for (int i = 0; i < result.size(); i++)
			{	//add the subclasses
				List<OntClass> sub = getSubClasses(result.get(i));
				for (int j = 0; j < sub.size(); j++)
					if (!result.contains(sub.get(j)))
						add.add(sub.get(j));
			}
			for (int i = 0; i < add.size(); i++)
				result.add(add.get(i));
		}
		return result;
	}
	
	/**	Returns the range of the given property. If that range is unspecified,
	 *	it checks the range of the super properties. If there is more than one class
	 *	in the range, a UnionClass is returned that unifies them.
	 *	
	 *	@param p	OntProperty
	 *	@return OntClass in range or, if more than one class in the range, a unionclass
	 *	with range classes as operands
	 */
	public OntClass getRange(OntProperty p)
	{
		List<OntClass> result = getRangeList(p);
		if (result.size() == 1)
			return result.get(0);
		else
			return makeRangeClass(result);		
	}
	
	/**	Returns the names of all classes in the range of the property with the given name.
	 *	If there is no such property, it returns an empty array. 
	 *	If that range is unspecified, it checks the range of the super properties. 
	 *
	 *	@param property	Property name
	 *	@return String[] with names of classes in the range
	 */
	public String[] getRangeClasses(String property)
	{
		OntProperty p = getProperty(property);
		if (p == null)
			return new String[0];
		List<OntClass> classes = getRangeList(p);
		
		String[] names = new String[classes.size()];
		for (int i = 0; i < classes.size(); i++)
			names[i] = classes.get(i).getLocalName();
		return names;
	}
	
	/**	Returns all classes in the range of the given property. 
	 *	If that range is unspecified, it checks the range of the super properties. 
	 *
	 *	@param p	Property
	 *	@return List<OntClass> with classes in the range
	 */
	public List<OntClass> getRangeList(OntProperty p)
	{
		List<OntClass> result = new ArrayList<OntClass>();
		Iterator it = p.listRange();
		
		while (it.hasNext())
		{	//add all classes in the range
			OntClass r = (OntClass) it.next();
//			System.out.println("Range URI: " + r.getURI());
			if (r.getURI() != null) {
				addClass(result, r);
			}
		}
		
		if (result.isEmpty())
		{	//if the range was empty, check the domain of the superproperties
			Iterator i = p.listSuperProperties(true);	//first check the direct superproperties
			while (i.hasNext())
			{
				OntProperty p1 = (OntProperty) i.next();
				result.addAll(getRangeList(p1));
			}
		}
		return result;
	}

	/** This takes an OntClass. If it is a unionclass, all classes in
	 *	the union are added to the arraylist, otherwise just the class itself
	 *
	 *	@param	result List with OntClasses
	 *	@param r	OntResource to be added to the list, either as is or all
	 *	operands separately
	 */
	private void addClass(List result, OntResource r)
	{
		if (r.isClass())
		{	//check if it's an OntClass
			OntClass c = r.asClass();					
			if (c.isUnionClass())
			{	//check if it's a Union class; if so, add the separate classes to result
				UnionClass u = c.asUnionClass();
				ExtendedIterator e = u.listOperands();
				while (e.hasNext())
				{
					OntClass cl = (OntClass) e.next();						
					result.add(cl);
				}
			}
			else
				result.add(c);
		}
	}
	
	/**	Returns the comments on this resource, concatenated in one String
	 *	@param r	OntResource
	 *	@return String with concatenated comments
	 */
	public String getComment(OntResource r)
	{
		Iterator it = r.listComments(null);
		StringBuffer sb = new StringBuffer();
		while (it.hasNext())
		{
			try
			{
				Literal l = (Literal) it.next(); 
				sb.append(l.getString());
			}
			catch (Exception e)
			{}
		}
		
		return sb.toString();		
	}
	
	/**	Adds the given comment to this resource
	 *	@param r	OntResource
	 *	@param comment String with comment
	 */
	public void addComment(OntResource r, String comment)
	{
		Literal lit = ontology.createLiteral(comment);
		r.addComment(lit);
	}
	
	/** Returns a HashMap where properties are keys, and minimum and maximum
	 *	cardinality constraints on this class are values.
	 *	@param c	OntClass
	 *	@return	HashMap<String, Integer[]> with all minimum and maximum
	 *	cardinality constraints on the given class
	 */
	public Map<String, Integer[]> getCardinalities(String c)
	{
		if (cardinalityMap.containsKey(c))
			return cardinalityMap.get(c);
		return new HashMap<String, Integer[]>();
	}
	
	private Map<String, Integer[]> initCardinalities(OntClass c)
	{
		HashMap<String, Integer[]> result = new HashMap<String, Integer[]> ();
		for (Iterator i = c.listSuperClasses(false); i.hasNext(); ) 
		{
 	 		OntClass sc = (OntClass) i.next();
			result = getCardinality(sc, result);			
		}
		for (Iterator i = c.listEquivalentClasses(); i.hasNext(); )
		{
 	 		OntClass sc = (OntClass) i.next();
 	 		//TODO: infinite recursion: check that the class has't been processed yet
 	 		if (! processedClasses.contains(sc)) {
 	 			result = getCardinality(sc, result);
			}
		}
		
		List<OntProperty> list = getDomainProperties(c.getLocalName());
		for (int i = 0; i < list.size(); i++)
		{
			OntProperty p = list.get(i);
			if (p.isFunctionalProperty())
			{
				if (result.containsKey(p.getLocalName()))
				{
					Integer[] l = result.get(p.getLocalName());
					l[1] = 1;
				}
				else
				{
					Integer[] l = new Integer[2];
					l[0] = 0;
					l[1] = 1;
					result.put(p.getLocalName(), l);
				}
			}
		}
		return result;
	}
	
	/**	Takes a class and returns a HashMap with as keys all 'string datatype' 
	 *	properties that have a  minimum cardinality restriction on this class. 
	 *	If the property has restricted values, these are returned as a value in 
	 *	an array.
	 *	LIBER uses this method to generate miniature forms, to quickly create an
	 *	object and specify the most important information.
	 *	@param c OntClass
	 *	@return Map<String,String[]> with property names and restricted values.
	 */
	public Map<String,String[]> getCompulsoryStringProperties(OntClass c)
	{
		Map<String,Integer[]> map = getCardinalities(c.getLocalName());
		Map<String,String[]> result = new HashMap<String,String[]>();
		
		for (Iterator<String> it = map.keySet().iterator(); it.hasNext(); )
		{
			String prop = it.next();
			if (map.get(prop)[0] == 0)
				continue;
				
			OntProperty p = getProperty(prop);
			if (!p.isDatatypeProperty())
				continue;		
			//if range is a data range, add all possible values to map
			OntResource r = getRange(p);
			if (r.isDataRange())
			{
				DataRange d = r.asDataRange();
				List<String> list = new ArrayList<String>();
				for (Iterator it2 = d.listOneOf(); it2.hasNext(); )
				{
					Literal lit = (Literal) it2.next();
					try
					{
						list.add(lit.getString());
					}
					catch (Exception e)
					{
						list.add(lit.getLexicalForm());
					}
				}
				result.put(prop, (String[]) list.toArray(new String[0]));
			}
			else
			{//if range is a string, add the property to the map
				String name = r.getLocalName();
				if ((name == null) || (name.indexOf("string") >= 0))
					result.put(prop, new String[0]);
			}
		}
		return result;
	}
	
	/*	Checks whether there is a property with the given name, and 
	 *	whether it is a datatype property
	 *	@param p property name
	 *	@return true if this is a datatype property, false if it is object property or does not exist
	 */
	public boolean isDatatypeProperty(String p)
	{
		OntProperty prop = getProperty(p);
		if (prop == null)
			return false;
		return prop.isDatatypeProperty();
	}
	
	/**	Retrieves the maximum cardinality constraint (if any) on the given class and
	 *	property
	 *	@param c	class name
	 *	@param p	property name
	 *	@return	maximum cardinality
	 */
	public int getMaxCardinality(String c, String p)
	{
		Map<String, Integer[]> map = getCardinalities(c);
		if (map.containsKey(p))
			return map.get(p)[1];
		return 0;
	}
	
	/** Helper method for getCardinalities
	 *	@param	OntClass
	 *	@param	HashMap to which results are added
	 *	@return HashMap with cardinality constraints
	 */
	private HashMap <String, Integer[]> getCardinality(OntClass c, HashMap<String, Integer[]> result)
	{
		if (c.isRestriction()) 
  		{
  			Restriction rs = c.asRestriction();
  			if (rs.isCardinalityRestriction()) 
			{
				CardinalityRestriction av = rs.asCardinalityRestriction();
				if (av.getCardinality() > 0)
				{
					OntProperty p = av.getOnProperty();
					Integer[] l = new Integer[2];
					l[0] = av.getCardinality();
					l[1] = l[0];
					result.put(p.getLocalName(), l);
				}
					
    		}
    		else if (rs.isMinCardinalityRestriction())
    		{
    			MinCardinalityRestriction av = rs.asMinCardinalityRestriction();
    			if (av.getMinCardinality() > 0)
    			{
    				OntProperty p = av.getOnProperty();
    				String name = p.getLocalName();
    				if (result.containsKey(name))
    				{
    					Integer[] l = result.get(name);
    					l[0] = av.getMinCardinality();
    				}
    				else
    				{
    					Integer[] l = new Integer[2];
						l[0] = av.getMinCardinality();
						l[1] = 0;
						result.put(name, l);
    				}
				}
    		}
    		else if (rs.isMaxCardinalityRestriction())
    		{
    			MaxCardinalityRestriction av = rs.asMaxCardinalityRestriction();
    			if (av.getMaxCardinality() > 0)
    			{
    				OntProperty p = av.getOnProperty();
    				String name = p.getLocalName();
    				if (result.containsKey(name))
    				{
    					Integer[] l = result.get(name);
    					l[1] = av.getMaxCardinality();
    				}
    				else
    				{
    					Integer[] l = new Integer[2];
						l[1] = av.getMaxCardinality();
						l[0] = 0;
						result.put(name, l);
    				}
				}
    		}
		}
       	else if (c.isIntersectionClass())
       	{
  			IntersectionClass ic = c.asIntersectionClass();
  			for (Iterator it = ic.listOperands(); it.hasNext();)
  			{
  				OntClass oc = (OntClass) it.next();
  				result = getCardinality(oc, result);
  			}
  		}
  		else	//if it's just a normal class, it's a superclass, which may have super- and equivalent classes of its own
  		{		//only add those constraints that are not already in the map (constraints on current class take precedence over those on superclasses
  			Map<String, Integer[]> temp = initCardinalities(c);
  			for (String key: temp.keySet())
  			{
  				if (!result.containsKey(key))
  					result.put(key, temp.get(key));
  			} 				
  		}
  		return result;
	}
	
	/**	Retrieves all properties in the ontology
	 *	@return Iterator with properties
	 */
	public Iterator getProperties()
	{
		return ontology.listOntProperties();
	}
	
	/**	Retrieves all classes in the ontology
	 *	@return Iterator with classes
	 */
	public Iterator getClasses()
	{
		return ontology.listNamedClasses();
	}
	
	/**	Returns the ontology that is wrapped by the reader
	 *	@return OntModel
	 */
	public OntModel getOntology()
	{
		return ontology;
	}
	
	/**	Returns the class with the given string as its local name. 
	 *	This method does not distinguish between namespaces, 
	 *	so if you have an ontmodel with 2 identically named classes
	 *	there may be problems.
	 *	@param name	class name
	 *	@return OntClass
	 *  
	 *  Changed to deal URI instead of class names
	 */
	public OntClass getClass(String name)
	{
//		if (classMap.containsKey(name))
//			return classMap.get(name);
		if (classMap.containsKey(name)){
			return classMap.get(name);
		} else {
			// if we only have the class name (instead of the full URI), look for the URI that ends with that class name
			for (String key : classMap.keySet()) {
				if (key.endsWith("#" + name) || key.endsWith("/" + name)) {
					return classMap.get(key);
				} 
			}
		}

		return null;		//no such class in ontology
	}
	
	/**	Returns the class of which this node is an individual, unless this node
	 *	is a datatype node. 
	 *	This method is preferred over getClass(String), as that might return a class
	 *	for a datatype that matched a class name.
	 *	@param node SGNode node
	 *	@return OntClass
	 */
	public OntClass getClass(SGNode node)
	{
		if (node instanceof DatatypeNode)
			return null;
		if (node instanceof SGDateNode)
			return getClass(DATE);
		if (node instanceof SGAddressNode)
			//TODO: URI Address: change to full URI? (http://www.policygrid.org/utility.owl#Address)
			return getClass("http://www.policygrid.org/utility.owl#Address");
		//TODO: URI: get the Ontclass from namespace and class name
		String nameSpace = node.getNameSpace();
		if (nameSpace != null && (!nameSpace.isEmpty())) {
			return getClass(nameSpace + node.getLabel());
		}
		return getClass(node.getLabel());
	}
	
	/**	Returns an OntProperty with the given name. This method does not distinguish
	 *	between namespaces, so if you have an ontmodel with 2 identically named properties
	 *	there may be problems.
	 *	@param name Property name
	 *	@return OntProperty
	 */
	public OntProperty getProperty(String name)
	{
		if (propertyMap.containsKey(name))
			return propertyMap.get(name);
		return null;
	}
	
	/** Returns all classes whose names have the given phrase as substring (not case sensitive)
	 *
	 *	@param regex String phrase
	 *	@return List<OntClass> 
	 */
	public List<OntClass> getMatchingClasses(String regex)
	{
		List<OntClass> result = new ArrayList<OntClass>();
		for (Iterator it = ontology.listNamedClasses(); it.hasNext(); )
		{	
			OntClass c = (OntClass) it.next();
			String name = c.getLocalName().replaceAll("_", "");	//remove all underscores
			if (name.toLowerCase().indexOf(regex) > -1)
				result.add(c);
		}
		return result;	
	}
	
	/** Returns all properties whose names have the given phrase as substring (not case sensitive)
	 *
	 *	@param regex String phrase
	 *	@return List<OntProperty>
	 */
	public List<OntProperty> getMatchingProperties(String regex)
	{
		List<OntProperty> result = new ArrayList<OntProperty>();
		for (Iterator it = ontology.listOntProperties(); it.hasNext(); )
		{	
			OntProperty p = (OntProperty) it.next();
			String name = p.getLocalName().replaceAll("_", "");	//remove all underscores
			if (name.toLowerCase().indexOf(regex) > -1)
				result.add(p);
		}
		return result;
	}
	
	/**	If there are no restrictions on the range of a property with respect to a class,
	 *	and there is more than 1 class in the range, the textframe should make one branch
	 *	with a node that can be any of those classes. This method returns a unionclass
	 *	holding all the classes in the range. If there is just 1 class in the range,
	 *	that class is returned
	 *
	 *	@param	ArrayList with the classes to be put in the range
	 *	@return	OntClass (usually a union class) 
	 */
	private OntClass makeRangeClass(List<OntClass> l)
	{
		if (l.size() == 1)
			return l.get(0);
		
		UnionClass u = ontology.createUnionClass(null, null);
		
		//range was 'thing', so all classes (pref. all root classes) should be in this union class
		if (l.size() == 0)
		{
			Iterator it = getClasses();
			while (it.hasNext())
			{
				OntClass c = (OntClass) it.next();
				if (getSuperClasses(c).size() == 1)	//only add c if it is a root class
					l.add(c);
			}
		}
		
		u.addOperands(l.iterator());
		return u;
	}
	
	/**	Returns true iff the given resource has a comment which includes the given string.
	 *	LIBER uses this method to check for relevant comments. For instance, the 'proper name
	 *	properties' are marked with a comment.
	 *
	 *	@param resource OntResource
	 *	@param type String comment type
	 *	@return true if the resource has such a comment.
	 */
	public boolean hasComment(OntResource resource, String type)
	{
		for (Iterator it = resource.listComments(null); it.hasNext(); )
		{
			Literal lit = (Literal) it.next();
			if (lit.getString().indexOf(type) >= 0)
				return true;
		}
		return false;
	}
	
	/**	Checks whether this property is used to store proper names
	 *	@param p OntProperty
	 *	@return true if this stores proper names
	 */
	public boolean useAsProperName(OntProperty p)
	{
		return properNameProperties.contains(p);
	}
	
	/**	Checks whether this name denotes a property that is used to store proper names
	 *	@param p String property name
	 *	@return true if this stores proper names
	 */
	public boolean useAsProperName(String p)
	{
		for (OntProperty prop : properNameProperties)
			if (prop.getLocalName().equals(p))
				return true;
		return false;
	}
	
	/**	Returns all 'proper name properties'.
	 *	Used by LIBER's NL generator
	 *	@return List<OntProperty>
	 */
	public List<OntProperty> getProperNameOntProperties()
	{
		return properNameProperties;
	}
	
	private void collectProperNameOntProperties()
	{	
		properNameProperties = new ArrayList<OntProperty>();
		for (Iterator it = getProperties(); it.hasNext(); )
		{
			OntProperty p = (OntProperty) it.next();
			//TODO: NLG: set surname and title as ProperNames so that those properties are used to identify resources when NLG
			if (hasComment(p, PROPERNAME) /*|| p.toString().equals("http://www.policygrid.org/utility.owl#HasEmployee") */
					|| p.toString().equals("http://xmlns.com/foaf/0.1/surname") || p.toString().equals("http://www.policygrid.org/opm-resource.owl#title") 
					|| p.toString().equals("http://www.policygrid.org/project.owl#projectTitle") 
//					|| p.toString().equals("http://xmlns.com/foaf/0.1/topic_interest") || p.toString().equals("http://xmlns.com/foaf/0.1/member")
//					|| p.toString().equals("http://xmlns.com/foaf/0.1/firstName") || p.toString().equals("http://xmlns.com/foaf/0.1/interest")
//					|| p.toString().equals("http://xmlns.com/foaf/0.1/knows") || p.toString().equals("http://xmlns.com/foaf/0.1/title")
//					|| p.toString().equals("http://www.policygrid.org/task.owl#HasOutput") || p.toString().equals("http://www.policygrid.org/resource.owl#HasResults") || p.toString().equals("http://www.policygrid.org/utility.owl#CollaboratorOf")
//					|| p.toString().equals("http://www.policygrid.org/utility.owl#AdministratorOf") || p.toString().equals("http://www.policygrid.org/resource.owl#HasResults") || p.toString().equals("http://www.policygrid.org/utility.owl#CollaboratorOf")
					/*|| p.toString().equals("http://www.policygrid.org/opm-resource.owl#depositedBy") /*|| p.toString().equals("http://www.policygrid.org/opm-resource.owl#producedInProject") || p.toString().equals("http://www.policygrid.org/opm-resource.owl#hasAuthor")*/)


				properNameProperties.add(p);
		}
	}
	
	/**	Returns the names of all 'proper name properties'.
	 *	Used by LIBER's NL generator
	 *	@return List<String>
	 */
	public List<String> getProperNameProperties()
	{
		List<String> result = new ArrayList<String>();
		for (OntProperty p : properNameProperties)
			result.add(p.getLocalName());
		return result;
	}
	
	/**	Returns a HashMap with properties as keys, and as values other HashMaps
	 *	with class names as keys, and the names of the submenus as values.
	 *	Used by LIBER's NL generator
	 *	@return Map<OntProperty,Map<String,String>>
	 */
	public Map<OntProperty,Map<String,String>> getSubmenuMap()
	{
		return submenuMap;
	}
	
	/** Returns a HashMap with any classes and properties whose natural language 
	 *	expressions are defined in the ontology (others are derived from the local name
	 *	by LIBER).
	 *	@return Map<String,String> with OntResource names and nl-expressions
	 */
	public Map<String,String> getNLExpressionMap()
	{
		return nlExpressions;
	}
	
	private void collectSubmenus()
	{
		submenuMap = new HashMap<OntProperty,Map<String,String>>();
		for (Iterator it = getProperties(); it.hasNext(); )
		{
			OntProperty p = (OntProperty) it.next();
			Map<String,String> map = new HashMap<String,String>();
			for (Iterator it2 = p.listComments(null); it2.hasNext(); )
			{
				String comment = ((Literal) it2.next()).getString();
				int idx = comment.indexOf(MENU);
				if (idx >= 0)
				{
					String rest = comment.substring(idx + MENU.length());
					int end = rest.indexOf("=");
					String className = rest.substring(0, end);
					String submenu = rest.substring(end + 1);
					map.put(className, submenu);
				}
			}
			submenuMap.put(p, map);
		}
	}
	
	/**	Returns the submenu that this property belongs to, or null if there is none.
	 *
	 *	@param prop OntProperty
	 *	@param className Class name
	 *	@return String submenu
	 */
	public String getSubmenu(OntProperty prop, String className)
	{
		if (!submenuMap.containsKey(prop))
			return null;
		Map<String,String> menus = submenuMap.get(prop);
		if (menus.containsKey(className))
			return menus.get(className);
		
		for (OntClass c : getSuperClasses(getClass(className)))
		{
			if (menus.containsKey(c.getLocalName()))
				return menus.get(c.getLocalName());
		}
		return null;
	}
	
	private void collectNLExpressions()
	{
		nlExpressions = new HashMap<String,String>();
		for (Iterator it = getProperties(); it.hasNext(); )
		{
			OntProperty p = (OntProperty) it.next();
			for (Iterator it2 = p.listComments(null); it2.hasNext(); )
			{
				String comment = ((Literal) it2.next()).getString();
				int idx = comment.indexOf(PHRASE);
				if (idx >= 0)
					nlExpressions.put(p.getLocalName(), comment.substring(idx + PHRASE.length()));
			}
		}
		for (Iterator it = getClasses(); it.hasNext(); )
		{
			OntClass c = (OntClass) it.next();
			for (Iterator it2 = c.listComments(null); it2.hasNext(); )
			{
				String comment = ((Literal) it2.next()).getString();
				int idx = comment.indexOf(PHRASE);
				if (idx >= 0)
					nlExpressions.put(c.getLocalName(), comment.substring(idx + PHRASE.length()));
			}
		}
	}
	
	/**	Returns the nl-expresssion of the given resource
	 *	@param r OntResource
	 *	@return String
	 */
	public String getNLExpression(OntResource r)
	{
		return getNLExpression(r.getLocalName());
	}
	
	/**	Returns the nl-expresssion of the resource with the given name
	 *	@param str Resource name
	 *	@return String
	 */
	public String getNLExpression(String str)
	{
		if (nlExpressions.containsKey(str))
			return nlExpressions.get(str);
		return null;
	}
	
	/**	Returns the operands of c (if c is a UnionClass)
	 *	@param c	OntClass
	 *	@return Iterator with operands
	 */
	public Iterator getOperands(OntClass c)
	{
		if (!c.isUnionClass())
			return null;
		UnionClass u = c.asUnionClass();
		return u.listOperands();
	}
	
	/**	Returns the inverse property
	 *	@param p Property
	 *	@return Inverse property
	 */
	public OntProperty getInverse(OntProperty p)
	{
		return p.getInverse();
	}
	
	/**	Returns the local name of the inverse property, or null if this is not
	 *	a property, or has no inverse.
	 *	@param p local name of property
	 *	@returns String with local name of inverse property
	 */
	public String getInverse(String p)
	{
		OntProperty prop = getProperty(p);
		if (prop == null)
			return null;
		OntProperty inv = prop.getInverse();
		if (inv == null)
			return null;
		return inv.getLocalName();
	}	
	
	/**	Returns a tree representation of the classes in the range of the given 
	 *	property, with the nodes in alphabetic order.
	 *
	 *	@param property property name
	 *	@return	Hierarchy[] tree representation of range class hierarchy
	 */
	public Hierarchy[] getRangeHierarchy(String property)
	{
		OntProperty p = getProperty(property);
		if (p == null)
			return new Hierarchy[0];
		
		List<OntClass> classList = getRangeList(p);
		Hierarchy[] result = new Hierarchy[classList.size()];
		for (int i = 0; i < classList.size(); i++)
			result[i] = createHierarchy(classList.get(i));
		return result;
	}
	
	/**	Creates a class hierachy with the given classes as its roots
	 *
	 *	@param roots Root classes
	 *	@return	Hierarchy[] tree representation of class hierarchy
	 */
	public Hierarchy[] getClassHierarchy(String[] roots)
	{
		if ((roots == null) || (roots.length == 0))
			return getClassHierarchy();
		
		Arrays.sort(roots);
		Hierarchy[] result = new Hierarchy[roots.length];
		for (int i = 0; i < roots.length; i++)
			result[i] = createHierarchy(getClass(roots[i]));
		
		return result;
	}
	
	/**	Creates a class hierarchy with every class in the ontology
	 *
	 *	@return	Hierarchy[] tree representation of class hierarchy
	 */
	public Hierarchy[] getClassHierarchy()
	{
		List<Hierarchy> result = new ArrayList<Hierarchy>();
		List<OntClass> namedClasses = new ArrayList<OntClass>();
		for (Iterator it = getClasses(); it.hasNext(); )
			namedClasses.add((OntClass) it.next());
		Collections.sort(namedClasses, new OntClassComparator());
		
		for (int i = 0; i < namedClasses.size(); i++)
		{
			OntClass c = namedClasses.get(i);
			if (c.getLocalName().indexOf("Axiom_") > -1)
				continue;
			if (c.getLocalName().equals("Date"))
				continue;	//skip the dates
				
			boolean rootclass = true;
			for (Iterator it = c.listSuperClasses(); it.hasNext(); )
			{
				OntClass oc = (OntClass) it.next();
				if (namedClasses.contains(oc))
					rootclass = false;
			}
			if (rootclass)
				result.add(createHierarchy(c));
		}
		Hierarchy[] array = new Hierarchy[result.size()];
		for (int j = 0; j < result.size(); j++)
			array[j] = result.get(j);
		return array;
	}
	
	/**	Creates a class hierachy with the given class as its root
	 *	@param c root OntClass
	 *	@return	Hierarchy[] tree representation of class hierarchy
	 */
	public Hierarchy createHierarchy(OntClass c)
	{
		List<OntClass> sub = new ArrayList<OntClass>();
		for (Iterator it = c.listSubClasses(true); it.hasNext(); )
			sub.add((OntClass) it.next());	
		Collections.sort(sub, new OntClassComparator());
		
		String name = c.getLocalName();
		Hierarchy h = new Hierarchy(name, Anchor.getNLExpression(name), sub.size());
		for (int i = 0; i < sub.size(); i++)
			h.addSub(i, createHierarchy(sub.get(i)));
		return h;
	}

	/**	Returns an integer indicating the type of range of this property.
	 *	0 if it is no property; 1 for a datatype property with restricted values;
	 *	2 for a date; 3 for a double or float; 4 for an integer; 5 for a boolean;
	 *	6 for a string; and 7 for another object.
	 *
	 *	@param property Property name
	 *	@return Integer
	 */
	public Integer getRangeType(String property)
	{
		return getRangeType(getProperty(property));
	}
	
	/**	Returns an integer indicating the type of range of this property.
	 *	0 if it is no property; 1 for a datatype property with restricted values;
	 *	2 for a date; 3 for a double or float; 4 for an integer; 5 for a boolean;
	 *	6 for a string; and 7 for another object.
	 *
	 *	@param p Property
	 *	@return Integer
	 */
	public Integer getRangeType(OntProperty p)
	{
		if (p == null)
			return new Integer(0);
		
		if (p.isDatatypeProperty())
		{
			OntResource r = getRange(p);
			if (r.isDataRange())
				return new Integer(1);
			else if (r.getLocalName() != null)
			{
				if ((r.getLocalName().indexOf("float") > -1) || (r.getLocalName().indexOf("double") > -1))
					return new Integer(3);
				else if (r.getLocalName().indexOf("int") > -1)
					return new Integer(4);
				else if (r.getLocalName().indexOf("boolean") > -1)
					return new Integer(5);
				else
					return new Integer(6);
			}
			else
				return new Integer(6);
		}
		else
		{
			String range = getRange(p).getLocalName();
			if ((range != null) && range.equalsIgnoreCase("date"))
				return new Integer(2);		//range is a date, for which we use a separate class instead of a datatype
			else
				return new Integer(7);		//range is an object
		}
	}
	
	/**	Returns the range (restricted data values) of the given property
	 *	@param property Property name
	 *	@return List<String> with possible range values
	 */
	public List<String> getRestrictedRange(String property)
	{
		List<String> result = new ArrayList<String>();
		OntProperty p = getProperty(property);
		if (p != null)
		{
			OntResource r = getRange(p);
			if (r.isDataRange())
			{
				DataRange d = r.asDataRange();
				Iterator it = d.listOneOf();		
				while (it.hasNext())
				{
					Literal value = (Literal) it.next();
					try
					{
						result.add(value.getString());
					}
					catch (Exception e)
					{
						result.add(value.getLexicalForm());
					}
				}
			}
		}
		return result;
	}
	
	/**	Returns all string datatype properties with cardinal constraints of this
	 *	class.
	 *	@param type 	class name
	 *	@return	FormInfo[], contains information needed to create a small form for new instance creation
	 */
	public FormInfo[] getCardinalStringProperties(String type)
	{
		OntClass c = getClass(type);
		Map<String, String[]> map = getCompulsoryStringProperties(c);
		FormInfo[] result = new FormInfo[map.size()];
		int cntr = 0;
		
		for (Iterator it = map.keySet().iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			result[cntr] = new FormInfo(key, SGNode.normalise(key, true), (String[]) map.get(key));
			if (key.equals("Name") || key.equals("Title"))
				swap(result, 0, cntr);	//name/title should be the first property!
			cntr++;
		}
		return result;
	}
	
	private void swap(Object[] array, int i, int j)
	{
		if (i == j)
			return;
		Object o = array[i];
		array[i] = array[j];
		array[j] = o;
	}
}