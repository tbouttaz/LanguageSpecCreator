package WYSIWYM.ontology;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import liber.edit.client.InstanceData;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.QueryValue;
import WYSIWYM.model.QueryValueNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGDoubleNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGIntNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.transformer.AutomaticGenerator;
import WYSIWYM.util.SPARQLException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.vocabulary.RDF;

/**	OntologyWriter creates RDF and SPARQL. For the editing module, 
 *	it takes a SemanticGraph and creates a RDF-representation. 
 *	For the querying module it generates SPARQL queries that represent
 *	the QueryGraph. It also contains a number of static methods that
 *	return particular queries with information that LIBER frequently
 *	needs. Most of these methods are domain-specific, using the PolicyGrid
 *	ontologies!
 *
 *	@author Feikje Hielkema
 *	@version 1.1 27-02-2007
 *
 *	@version 1.2 March 2008
 *
 *	@version 1.3 22-05-2008
 */
public class OntologyWriter
{
	/**	Geography ontology namespace */
	public static final String GEOGRAPHY = "Geography";
	/**	Geography ontology base URI */
	public static final String GeographyURI = "http://www.mooney.net/geo#";
	
	/**	PolicyGrid ontologies general namespace */	
	public static final String POLICYGRID = "www.policygrid.org";
	/**	RDF base URI */
	public static final String RDFURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	/**	PolicyGrid Utility ontology URI */
	public static final String UtilityURI = "http://www.policygrid.org/utility.owl#";
	/**	PolicyGrid Resource ontology URI */
	public static final String ResourceURI = "http://www.policygrid.org/resource.owl#"; 
	/**	PolicyGrid Utility Task URI */
	public static final String TaskURI = "http://www.policygrid.org/task.owl#"; 
	/**	PolicyGrid Utility ontology namespace */
	public static final String UTILITY = "Utility";
	/**	PolicyGrid Resource ontology namespace */
	public static final String RESOURCE = "Resource";
	/**	PolicyGrid RDF namespace */
	public static final String Rdf = "rdf";
	/**	PolicyGrid Task ontology namespace */
	public static final String TASK = "Task";
	public static final String PROPERTY = "Property";
	
	private OntologyReader reader;
	private OntModel model;

	private Map<String, Individual> map;	
	private List<SGEdge> mappedEdges= new ArrayList<SGEdge>();
	private Map<String, String> mappedNodes = new HashMap<String, String>();
	private Map<String, String> classStatements = new HashMap<String, String>();
	private StringBuffer filters = new StringBuffer();
	private int x = 0, y = 0, z = 0;
	private char[] alphabet = new char[] {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
	
	/**	Constructs an ontology writer that will produce RDF with
	 *	triples described by the given Ontology model.
	 *
	 *	@param	r Ontology
	 */
	public OntologyWriter(OntologyReader r)
	{
		reader = r;
	}

///////////////******************** SPARQL CREATION ****************/////////////
	
	/**	Returns a SPARQL query that takes a user's Sesame ID
	 *	and returns his ourSpaces ID.
	 *	@param	user	Sesame ID
	 *	@return String SPARQL
	 */
	public static String getIDNumberQuery(String user)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(1));	
		sb.append("SELECT ?nr WHERE { <");
		sb.append(user);
		sb.append("> ");
		sb.append(UTILITY);
		sb.append(":IdNumber ?nr.}");
		return sb.toString();
	}

	/**	Creates SPARQL query that returns the name of the user with the given id
	 *	@param	id	Sesame ID
	 *	@return String SPARQL
	 */
	public static String getUserNameQuery(String id)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(1));	
		sb.append("SELECT ?name WHERE { <");
		sb.append(id);
		sb.append("> ");
		sb.append(UTILITY);
		sb.append(":Name ?name.}");
		return sb.toString();
	}
	
	/**	Returns a SPARQL query that finds the id of the resource with the given uri
	 *	@param	url Resource URI
	 *	@return String SPARQL
	 */
	public static String getUrlIDQuery(String url)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(2));	
		sb.append("SELECT ?id WHERE { ?id ");
		sb.append(RESOURCE);
		sb.append(":HasURI \"" + url + "\"}");
		return sb.toString();
	}
	
	/**	Get ID's and registration yes/no of users by this name.
	 *	This information is used by the ourSpaces registration widget
	 *	to find all non-registered users with a certain name.
	 *	@param name Username
	 *	@return String SPARQL
	 */
	public static String getUserIDQuery(String name)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(1));	
		sb.append("SELECT ?id ?registered WHERE { ?id ");
		sb.append(UTILITY);
		sb.append(":Name ?name. FILTER (regex(?name, \"");
		sb.append(name);
		sb.append("\", \"i\")). OPTIONAL {?id ");
		sb.append(UTILITY);
		sb.append(":IsRegistered ?registered. FILTER(regex(?registered, \"true\", \"i\"))}}");
		return sb.toString();
	}

	/**	Creates a query that asks for the full name, email, employer and deposited
	 *	resources of all persons that have the given name, e.g.:
	 *
	 *	Select ?name ?email ?org ?resource ?title
	 *	Where {
	 *	?x a Person.
	 *	?x Name ?name.
	 *	filter (regex(?name, user, "i"))
	 *	OPTIONAL {{?x Email ?email.} UNION {?x Email ?y. ?y ?z ?email}}	//check for sequences
	 *	OPTIONAL {{?x EmployeeOf ?organisation.} UNION {?organisation HasEmployee ?x.}
	 *	?organisation Name ?name.}
	 *	OPTIONAL {{?x DepositorOf ?resource.} UNION {?resource DepositedBy ?x.}
	 *	?resource Title ?title.}}
	 *	
	 *	@param user Username
	 *	@return String SPARQL
	 */
	public static String getUserQuery(String user)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(1));
		sb.append(getPrefix(2));
		
		sb.append("SELECT ?x ?Name ?Email ?Organisation ?Deposited WHERE { ?x a Utility:Person. ");
		sb.append("?x Utility:Name ?Name. FILTER (regex(?Name, \"");
		sb.append(user);
		sb.append("\", \"i\")) OPTIONAL {{?x Utility:Email ?Email.} UNION {?x Utility:Email ?seq. ?seq ?nr ?Email.}} ");
		sb.append("OPTIONAL {{?x Utility:EmployeeOf ?org.} UNION {?org Utility:HasEmployee ?x.} ");
		sb.append("?org Utility:Name ?Organisation.} ");
		sb.append("OPTIONAL {{?x Resource:DepositorOf ?resource.} UNION {?resource Resource:DepositedBy ?x.} ");
		sb.append("?resource Resource:Title ?Deposited.}}");

		return sb.toString();
	}

	/**	Returns a SPARQL query that takes a user's first and last name and
	 *	retrieves his full name, email,	organisation, project, documents he's 
	 *	written, when those were uploaded, and whether he's registered.
	 *	This information is used by the ourSpaces registration widget
	 *	to find and present all non-registered users with a certain name.
	 *	@param first First name
	 *	@param last Last, family name
	 *	@return String SPARQL
	 */
	public static String getUserDescriptionQuery(String first, String last)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(1));
		sb.append(getPrefix(2));
		
		sb.append("SELECT ?x ?Name ?Email ?Organisation ?Project ?Authored ?timestamp ?registered WHERE { ?x a Utility:Person. ");
		sb.append("?x Utility:Name ?Name. FILTER (regex(?Name, \"");
		if ((first != null) && (first.length() > 0))
		{	//if both are there, search for first and last name
			sb.append(first);
			if ((last != null) && (last.length() > 0))
			{
				sb.append("\", \"i\") && regex(?Name, \"");
				sb.append(last);
			}
		}
		else
			sb.append(last);
			
		sb.append("\", \"i\")) OPTIONAL {{?x Utility:Email ?Email.} UNION {?x Utility:Email ?seq. ?seq ?nr ?Email.}} ");
		sb.append("OPTIONAL {{?x Utility:EmployeeOf ?org.} UNION {?org Utility:HasEmployee ?x.} ");
		sb.append("?org Utility:Name ?Organisation.} ");
		sb.append("OPTIONAL {{?x Resource:AuthorOf ?resource.} UNION {?resource Resource:HasAuthor ?x.} ");
		sb.append("?resource Resource:Title ?Authored. ?resource Resource:OurSpacesDate ?timestamp.}");
		sb.append("OPTIONAL {{?x Utility:MemberOf ?proj.} UNION {?proj Utility:HasMember ?x.} ");
		sb.append("?proj Utility:Name ?Project.}");
		sb.append("OPTIONAL {?x Utility:IsRegistered ?registered. FILTER(regex(?registered, \"true\", \"i\"))}}");

		return sb.toString();
	}
	
	/**	Returns a query that finds all instances of a certain class.
	 *	select ?x where {?x a namespace:type.}
	 *	@param type Class type
	 *	@param r Ontology
	 *	@return String SPARQL	
	 */
	public static String getInstanceNrQuery(String type, OntologyReader r)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5; i++)
			sb.append(getPrefix(i));
		
		sb.append("SELECT ?x WHERE {?x a ");
		OntClass c = r.getClass(type);
		sb.append(getNameSpace(c));
		sb.append(":");
		sb.append(type);
		sb.append(" .}");
		return sb.toString();
	}	
	
	/**	Returns a query that finds all instances of the classes in range, which
	 *	are only one step removed from user or project.
	 *
	 *	select ?x ?name ?title ?username where { {?x a ConferencePaper.} UNION {?x a JournalPaper.}
	 *	{?x ?p <userID>.} UNION {?x ?p <projectID>.} 
	 *	{?x utility:name ?name} UNION {?x resource:Title ?title.}}
	 *	@param range Class types
	 *	@param userID Sesame user ID
	 *	@param projectID Sesame project ID
	 *	@return String SPARQL
	 */
	public String getConnectedObjectsOfTypeQuery(String[] range, String userID, String projectID)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5; i++)
			sb.append(getPrefix(i));
		
		sb.append("SELECT ?x WHERE { {");
		for (int i = 0; i < range.length; i++)
		{	//add all possible range types
			OntClass c = reader.getClass(range[i]);
			sb.append("?x a ");
			sb.append(getNameSpace(c));
			sb.append(":");
			sb.append(range[i]);
			if (range.length > (i + 1))
				sb.append(".} UNION {");
		}
		sb.append(".} {?x ?p <");	//x must be connected by a property to the user
		sb.append(userID);
		if ((projectID != null) && (projectID.length() > 0))
		{	//or to the project
			sb.append(">.} UNION {?x ?p <");
			sb.append(projectID);
		}
		sb.append(">.} ");
		List<String> vars = new ArrayList<String>();
		List<OntProperty> props = reader.getProperNameOntProperties();
		for (int i = 0; i < props.size(); i++)
		{
			OntProperty property = props.get(i);
			sb.append("{ ?x " + getNameSpace(property) + ":");
			sb.append(property.getLocalName());
			String var = "?" + property.getLocalName();	//e.g. ?Name
			sb.append(" " + var);
			vars.add(var);
			sb.append(".} ");
			if (props.size() > (i + 1))
				sb.append("UNION ");
		}
		sb.append("}");
		int idx = sb.indexOf("WHERE { {");
		for (String var : vars)
			sb.insert(idx, var + " ");
		return sb.toString();
	}
	
	/**	Returns a query that finds the values that the object with the given id
	 *	has for the given properties.
	 *
	 *	select ?v1 ?v2 where {OPTIONAL{<id> property1 ?v1.} OPTIONAL{<id> property2 ?v2.}}
	 *
	 *	@param id Sesame ID
	 *	@param properties Property names
	 *	@return String SPARQL
	 */
	public String getPropertyValuesQuery(String id, List<String> properties)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5; i++)
			sb.append(getPrefix(i));
		sb.append("SELECT WHERE { ");
		
		for (String prop : properties)
		{
			OntProperty p = reader.getProperty(prop);
			sb.append("OPTIONAL{ <" + id + "> ");	//some objects might not have all information, if there of a higher type
			sb.append(getNameSpace(p) + ":" + prop);
			String var = "?" + prop;	//e.g. ?Name
			sb.append(" " + var + ".} ");
			sb.insert(sb.indexOf("WHERE {"), var + " ");
		}
		sb.append("}");
		return sb.toString();
	}
	
	/**	Returns a query that finds all objects with the given type and title, and
	 *	includes information about people connected to it to help the user identify
	 *	it.
	 *
	 *	@param title Title
	 *	@param types Class types
	 *	@return String SPARQL
	 */
	public String getObjectQuery(String title, String[] types)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++)
			sb.append(getPrefix(i));
		
		sb.append("SELECT ?x ?Type ?Name ?Title ?Property ?PersonName WHERE { ?x a ?Type. ");
		for (int i = 0; i < types.length; i++)
		{
			OntClass c = reader.getClass(types[i]);
			sb.append("{?x a " + getNameSpace(c) + ":" + types[i] + ".} ");
		
			if ((i + 1) < types.length)
				sb.append(" UNION ");
		}
			
		sb.append("{?x Utility:Name ?Name. FILTER (regex(?Name, \"");
		sb.append(title);
		sb.append("\", \"i\"))} UNION {?x Resource:Title ?Title. FILTER (regex(?Title, \"");
		sb.append(title);
		sb.append("\", \"i\"))} ");
		sb.append("OPTIONAL {{?x ?" + PROPERTY + " ?person.} UNION {?x ?");		//?person ?" + PROPERTY + " ?x.} ");
		sb.append(PROPERTY + "?seq. ?seq a " + Rdf + ":Seq. ?seq ?nr ?person.} ");
		sb.append("?person Utility:Name ?PersonName.}}");

		return sb.toString();
	}
	
	/**	Returns all objects that match the given description, and also some extra
	 *	information about how they are connected to people, to help the user identify
	 *	them.
	 *
	 *	@param data Form data
	 *	@return String SPARQL
	 */
	public String getObjectQuery(InstanceData data)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 4; i++)
			sb.append(getPrefix(i));
		
		sb.append("SELECT ?x ?Type");	//select id, type, and all information in instancedata
		for(int i = 0; i < data.size(); i++)
			sb.append(" ?" + data.getProperty(i));		
		sb.append(" ?Property ?PersonName WHERE { ?x a ?Type. {?x a ");	//and any relation to people
	
		OntClass c = reader.getClass(data.getType());
		sb.append(getNameSpace(c));		//specify the type
		sb.append(":" + data.getType() + ".} ");
		
		for (int i = 0; i < data.size(); i++)	//and each of the requirements
		{	//e.g. ?x Utility:Name ?Name. FILTER(regex(?Name, "Feikje", "i")) 
			sb.append("?x ");
			String prop = data.getProperty(i);
			sb.append(getNameSpace(reader.getProperty(prop)));
			sb.append(":");
			sb.append(prop);
			sb.append(" ?");
			sb.append(prop);
			sb.append(". FILTER(regex(?");
			sb.append(prop);
			sb.append(", \"");
			sb.append(data.getValue(i));
			sb.append("\", \"i\")) ");
		}
		
		sb.append("OPTIONAL {{?x ?" + PROPERTY + " ?person.} UNION {?x ?");	
		sb.append(PROPERTY + "?seq. ?seq a " + Rdf + ":Seq. ?seq ?nr ?person.} ");
		sb.append("?person Utility:Name ?PersonName.}}");

		return sb.toString();
	}

	/**	Returns the namespace of the given resource. 
	 *	Used to determine prefixes.
	 *
	 *	@param r OntResource
	 *	@return String namespace
	 */	
	public static String getNameSpace(OntResource r)
	{
		String namespace = r.getNameSpace();
		if (namespace.equals(UtilityURI))
			return UTILITY;
		else if (namespace.equals(ResourceURI))
			return RESOURCE;
		else if (namespace.equals(TaskURI))
			return TASK;
		else if (namespace.equals(GeographyURI))
			return GEOGRAPHY;
		return Rdf;
	}
	
	/**	Returns a query that finds all information important for NL presentation
	 *	of the given object (i.e. its type and, if specified, its name or title.
	 *	E.g.prefix project: <http://www.policygrid.org/project.owl#>
	 *		prefix resource: <http://www.policygrid.org/resource.owl#>
	 *		prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
	 *		select ?name ?title ?type  ?uri ?access where {
	 *		<http://www.policygrid.org/resource.owl#ac438309-fb4d-4d17-b232-9246a5f12471> rdf:type ?type.
	 *		OPTIONAL{<http://www.policygrid.org/resource.owl#ac438309-fb4d-4d17-b232-9246a5f12471> project:Name ?name.}
	 *		OPTIONAL{<http://www.policygrid.org/resource.owl#ac438309-fb4d-4d17-b232-9246a5f12471> resource:Title ?title.}
	 *		OPTIONAL{<http://www.policygrid.org/resource.owl#ac438309-fb4d-4d17-b232-9246a5f12471> resource:HasURI ?uri.}
	 *		OPTIONAL{<http://www.policygrid.org/resource.owl#ac438309-fb4d-4d17-b232-9246a5f12471> resource:AccessConditions ?access.}}
	 *
	 *	@param id Sesame ID
	 *	@return String SPARQL
	 */
	public static String getNLQuery(String id)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5; i++)
			sb.append(getPrefix(i));
		
		sb.append("SELECT ?Name ?Title ?Gender ?HasURI ?AccessConditions ?type WHERE { <");
		sb.append(id + "> ");
		sb.append(Rdf + ":type ?type. ");
		
		sb.append("OPTIONAL {{<");
		sb.append(id);
		sb.append("> ");
		sb.append(UTILITY);
		sb.append(":Name ?Name.} UNION {<");
		sb.append(id);
		sb.append("> ");
		sb.append(GEOGRAPHY);
		sb.append(":Name ?Name.}} OPTIONAL {<");
		sb.append(id);
		sb.append("> ");
		sb.append(UTILITY);
		sb.append(":Gender ?Gender.} OPTIONAL {<");
		sb.append(id);
		sb.append("> ");
		sb.append(RESOURCE);
		sb.append(":Title ?Title.} OPTIONAL {<");
		sb.append(id);
		sb.append("> ");
		sb.append(RESOURCE);
		sb.append(":AccessConditions ?AccessConditions.} OPTIONAL {<");
		sb.append(id);
		sb.append("> ");
		sb.append(RESOURCE);
		sb.append(":HasURI ?HasURI.}}");
		
		return sb.toString();
	}
	
	/**	Returns a query that finds the type of the given id.
	 *	@param id Sesame ID
	 *	@return String SPARQL
	 */
	public static String getTypeQuery(String id)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getPrefix(0));
		sb.append("SELECT ?type WHERE { <");
		sb.append(id + "> ");
		sb.append(Rdf + ":type ?type.}");
		return sb.toString();
	}
	
	/**	Creates a query that finds all statements that have the given id
	 *	as their subject.
	 *
	 *	CONSTRUCT { <http://www.policygrid.org/project.owl#F2> ?y ?z. }
	 *	WHERE { <http://www.policygrid.org/project.owl#F2> ?y ?z. }
	 *
	 *	@param id Sesame ID
	 *	@return String SPARQL
	 */
	public static String getDescriptionQuery(String id)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("CONSTRUCT { <");
		sb.append(id);
		sb.append("> ?y ?z . } WHERE { <");
		sb.append(id);
		sb.append("> ?y ?z . }");
		return sb.toString();
	}

	/**	Returns a SPARQL sentence that defines a prefix. The argument, an integer
	 *	between 0 and 4, determines for which ontology the prefix is defined. 0 is RDF,
	 *	1 is Utility, 2 is Resource, 3 is Task, 4 is Geography.
	 *
	 *	@param ontology Ontology
	 *	@return String SPARQL prefix
	 */
	public static String getPrefix(int ontology)
	{
		StringBuffer sb = new StringBuffer("PREFIX ");
		switch (ontology)
		{
			case 0: sb.append(Rdf); sb.append(": <"); sb.append(RDFURI); break;
			case 1: sb.append(UTILITY); sb.append(": <"); sb.append(UtilityURI); break;
			case 2: sb.append(RESOURCE); sb.append(": <"); sb.append(ResourceURI); break;
			case 3: sb.append(TASK); sb.append(": <"); sb.append(TaskURI); break;
			case 4: sb.append(GEOGRAPHY); sb.append(": <"); sb.append(GeographyURI); break;
		}
		sb.append("> ");
		return sb.toString();
	}

	/**	Transforms the semantic graph into SPARQL. This method is used by LIBER's Querying
	 *	module.
	 *	@param sg	SemanticGraph
	 *	@return String SPARQL
	 */
	public String getSPARQL(SemanticGraph sg) throws SPARQLException
	{
		x = 0;
		y = 0;
		z = 0;	
		mappedEdges = new ArrayList<SGEdge>();
		mapNodes(sg);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 5; i++)
			sb.append(getPrefix(i));
		
		sb.append("SELECT " + mappedNodes.get(sg.getRoot().getID()) + " WHERE { "); 
		filters = new StringBuffer();
		sb.append(getClassStatement(sg.getRoot()));
		sb.append(mapEdges(sg.getRoot()));
		sb.append("}");
		return sb.toString();
	}

	/**	Maps all nodes in the SemanticGraph to a var String, which is stored in a
	 *	HashMap. It returns a String containing a series of 'is a' statements.
	 */
	private void mapNodes(SemanticGraph sg) throws SPARQLException
	{
		mappedNodes = new HashMap<String, String>();
		classStatements = new HashMap<String, String>();

		for (Iterator it = sg.getNodes(); it.hasNext(); )
		{
			StringBuffer sb = new StringBuffer();
			SGNode node = (SGNode) it.next();
			String var = getVarString();
			mappedNodes.put(node.getID(), var);	//and store it in the map
			
			OntClass c = reader.getClass(node);
			if (c != null)	//create a 'is a [class]' statement
			{
				sb.append(var + " a ");
				sb.append(getNameSpace(c));
				sb.append(":" + node.getLabel() + ". ");
				classStatements.put(node.getID(), sb.toString());
			}
		}
	}
	
	private String getClassStatement(SGNode node)
	{
		String id = node.getID();
		if (classStatements.containsKey(id))
		{
			String result = classStatements.get(id);
			classStatements.remove(id);
			return result;
		}
		return "";
	}
	
	/**	Creates a new unique variable for the query
	 */
	private String getVarString() throws SPARQLException
	{
		StringBuffer var = new StringBuffer("?");
		z++;
		if (z > 25)
		{
			z = 0;
			y++;
			if (y > 25)
			{
				y = 0;
				x++;
			}
		}
		if (x > 25)
			throw new SPARQLException("The query is too large; it has more than 26^3 variables");
		
		var.append(alphabet[x]);
		var.append(alphabet[y]);
		var.append(alphabet[z]);	//e.g. ?aai
		return var.toString();
	}

	/**	Maps all outgoing edges of the given node to SPARQL requirements.
	 */
	private String mapEdges(SGNode node) throws SPARQLException
	{
		String nodeVar = mappedNodes.get(node.getID());
		StringBuffer sb = new StringBuffer();
			
		for (Iterator it = node.getOutgoingEdges(); it.hasNext(); )
		{
			String child = "";
			SGEdge edge = (SGEdge) it.next();
			SGNode target = edge.getTarget();
			String lb = edge.getLabel();
			if (mappedEdges.contains(edge) || lb.equals("ID"))
				continue;
			
			if (((QueryEdge) edge).isOptional())	//this requirement is optional
				sb.append("OPTIONAL {");
			
			sb.append("{");	
			sb.append(nodeVar);
					
			OntProperty p = reader.getProperty(lb);
			OntProperty inverse = null;
			if (p != null)
				inverse = reader.getInverse(p);
			
			StringBuffer prop = new StringBuffer(" ");
			if (lb.equals("ANYTHING"))	//if the user is looking for any property with a certain value,
				prop.append(getVarString() + " ");	// add a new variable (e.g. ?property)
			else
				prop.append(getNameSpace(p) + ":" + lb + " ");	
			sb.append(prop.toString());
			mappedEdges.add(edge); //new
			
			String targetVar = null;
			if (target instanceof QueryValueNode)
				targetVar = mapValueNode((QueryValueNode) target);
			else
				targetVar = mappedNodes.get(target.getID());
			
			sb.append(targetVar);			//if there is no inverse,
			sb.append(".} UNION {");		//produce {?x hasKeyword ?y.} UNION {?x hasKeyword ?z. ?z a rdf:Seq. ?z ?nr ?y.}	
			if (inverse == null)
			{
				sb.append(nodeVar);				
				sb.append(prop.toString());
				String newVar = getVarString();
				sb.append(newVar);
				sb.append(". ");
				sb.append(newVar);
				sb.append(" a rdf:Seq. ");
				sb.append(newVar + " ");
				sb.append(getVarString() + " ");
				sb.append(targetVar);
			}
			else
			{	//if there is an inverse produce {?x hasAuthor ?y.} UNION {?y AuthorOf ?x.}
				sb.append(targetVar + " ");
				sb.append(getNameSpace(inverse) + ":" + inverse.getLocalName() + " ");
				sb.append(nodeVar);
			}
					
			if (!(target instanceof QueryValueNode))	//if it's a datatype, append filters etc;
				child = mapEdges(target);
	
			sb.append(".} ");
			sb.append(filters.toString());	//add the filters
			filters = new StringBuffer();	//and reset them
			sb.append(getClassStatement(target));	//add the type of the target node
			sb.append(child);				//add any requirements target might have
			if (((QueryEdge) edge).isOptional())	//if the requirement is optional, add a closing bracket
				sb.append("} ");		
		}
		return sb.toString();
	}
	
	/**	Maps the values contained in a QueryValueNode, including the boolean operator
	 *	and their comparators, to a SPARQL expression
	 */
	private String mapValueNode(QueryValueNode node) throws SPARQLException
	{
		String var = mappedNodes.get(node.getID());
		List<QueryValue> children = node.getValues();	//qValue.getChildren();
		SGNode value = children.get(0).getValue();	//qValue.getValue();
		int booleanOperator = /**qValue*/ node.getBooleanOperator();
		if (value instanceof SGDateNode)
			return mapDateNode(node);
		if ((value instanceof SGIntNode) || (value instanceof SGDoubleNode))
			return mapNumber(node);		
		
		List<String> varList = new ArrayList<String>();
		varList.add(var);	
		StringBuffer result = new StringBuffer(var);
		
		for (int i = 1; i < children.size(); i++)
		{
			if (booleanOperator == 0)	//if the boolean is 'and', each value needs an unique variable
			{
				String str = getVarString();
				varList.add(str);	
				result.append("," + str);	//add it to the list of variables that is returned ('?c, ?d, ?e')
			}
			else	//otherwise they all use the same
				varList.add(var);
		}
		//add the filters
		filters.append("FILTER (");
		if (booleanOperator == 2)
			filters.append("!(");
		
		for (int i = 0; i < children.size(); i++)
		{	//e.g. FILTER (!(regex(?x, "term1", "i") || regex(?x, "term2", "i")))
			filters.append(makeRegex(varList.get(i), children.get(i).getValue().getLabel()));
			if (i < (children.size() - 1))
			{
				if (booleanOperator == 0)
					filters.append(" && ");
				else
					filters.append(" || ");
			}
		}
		
		if (booleanOperator == 2)
			filters.append(")");
		filters.append(") ");
		return result.toString();
	}
	
	/**	Returns a regex filter (e.g. regex(?x, "John Smith", "i") )
	 *	If the value is e.g. 'J. Smith', the regex will be
	 *	(regex(?x, "J", "i") && regex(?x, "Smith", "i"))
	 */
	private String makeRegex(String var, String value)
	{
		String copy = value.trim();
		StringBuffer sb = new StringBuffer("(");
		int prevIdx = 0;
		while (true)
		{
			int idx = getWordBreakIdx(prevIdx, copy);
			sb.append("regex(");
			sb.append(var);
			sb.append(", \"");
			
			if (idx < 0)	//no more full stops or spaces left
			{
				sb.append(copy.substring(prevIdx));
				sb.append("\", \"i\")) ");
				return sb.toString();
			}
			else
			{
				sb.append(copy.substring(prevIdx, idx));
				sb.append("\", \"i\") && ");
				prevIdx = idx + 1;
			}
		}
	}
	
	private int getWordBreakIdx(int start, String str)
	{
		int idx1 = str.indexOf(".", start);
		int idx2 = str.indexOf(" ", start);
		if ((idx1 < idx2) && (idx1 > -1))
			return idx1;
		return idx2;
	}

	/**	Maps a QueryValueNode that contains integers or doubles to a suitable
	 *	SPARQL expression
	 */
	private String mapNumber(QueryValueNode node)
	{
		String var = mappedNodes.get(node.getID());
		int booleanOperator = node.getBooleanOperator();
		filters.append("FILTER (");
		if (booleanOperator == 2)
			filters.append("!(");
		
		List<QueryValue> children = node.getValues();	//qValue.getChildren();
		for (int i = 0; i < children.size(); i++)
		{	//e.g. FILTER ((?var > 20) && (?var < 40))
			filters.append("(");
			filters.append(mapNumberValue(children.get(i), var));
			filters.append(")");
			if (i < (children.size() - 1))
			{
				if (booleanOperator == 0)
					filters.append(" && ");
				else
					filters.append(" || ");
			}
		}
		
		if (booleanOperator == 2)
			filters.append(")");
		filters.append(") ");
		return var;
	}
	
	/**	Creates an expression such as '?var < 20'
	 */
	private String mapNumberValue(QueryValue value, String var)
	{
		StringBuffer result = new StringBuffer(var);
		switch (value.getComparator())
		{
			case 0: result.append(" = "); break;
			case 1: result.append(" < "); break;
			case 2: result.append(" > "); break;
		}
		result.append(value.getValue().getLabel());
		return result.toString();
	}

	/**	Maps the dates contained in the given node to SPARQL
	 */
	private String mapDateNode(QueryValueNode node)	throws SPARQLException
	{
		String var = mappedNodes.get(node.getID());
		int booleanOperator = /**qValue*/node.getBooleanOperator();

		List<QueryValue> children = node.getValues();//qValue.getChildren();
		StringBuffer sb = new StringBuffer();
		if (booleanOperator == 1)
		{	//if the operator is 'or', make a separate expression for each child, and use UNION
			for (int i = 0; i < children.size(); i++)
			{	
				sb.append("{");
				sb.append(mapDate(children.get(0), var, false));
				sb.append("} ");
				if (i < (children.size() - 1))
					sb.append("UNION {");
				else
					for (int j = 1; j < children.size(); j++)
						sb.append("} ");	//add closing brackets after the last item
			}

		}
		else	//if the expression is 'and' or 'not'
		{	//make a separate expression for each child and list them in a row
			for (int i = 0; i < children.size(); i++)
				sb.append(mapDate(children.get(0), var, (booleanOperator == 2)));		
		}
	
		filters.append(sb.toString());
		return var;
	}
	
	/**	Creates a '?x Resource:HasAuthor ?y' statement or similar
	 */
	private String getStatement(String var1, String var2, String prop)
	{	//var1 prop var2 .
		StringBuffer sb = new StringBuffer();
		sb.append(var1);
		sb.append(" ");
		sb.append(prop);
		sb.append(" ");
		sb.append(var2);
		sb.append(" . ");
		return sb.toString();
	}
	
	/**	Creates a (?x < 23) statement or similar, for in a filter
	 */
	private String getFilterStatement(String var, String operator, String value)
	{	//(var operator value)
		StringBuffer sb = new StringBuffer("(");
		sb.append(var);
		sb.append(" ");
		sb.append(operator);
		sb.append(" ");
		sb.append(value);
		sb.append(") ");
		return sb.toString();
	}
	
	/**	Maps one Date value and its comparator to SPARQL
	 */
	private String mapDate(QueryValue value, String var, boolean not) throws SPARQLException
	{
		StringBuffer sb = new StringBuffer();
		SGDateNode node = (SGDateNode) value.getValue();
		Integer[] dates = node.getDayMonthYear();
		String year = getVarString();
		
		if (value.getComparator() == 1)	//check that the end date is before the begin date of the given period
			sb.append(getCompareDate(dates[0], dates[2], dates[4], var, not, true));
		else if (value.getComparator() == 2)
		{	//check that the begin date is before the end date of the given period
			if (dates[5] == null)	//or, if there is no end date specified, its begin date
				sb.append(getCompareDate(dates[0], dates[2], dates[4], var, not, false));
			else
				sb.append(getCompareDate(dates[1], dates[3], dates[5], var, not, false));
		}
		else
		{	//date should match a date or be during a period
			if (node.isPeriod())
			{
				if (not)	//dates should not fall in this period
				{	//get those which started after the end of the period, or ended before its begin
					sb.append(getCompareDate(dates[1], dates[3], dates[5], var, false, false));
					sb.append(getCompareDate(dates[0], dates[2], dates[4], var, false, true));
				}
				else
				{	//check that date falls entirely within this period	IS THAT WHAT WE WANT?
					sb.append(getCompareDate(dates[0], dates[2], dates[4], var, false, false));	//the begin date after the begin of the period
					sb.append(getCompareDate(dates[1], dates[3], dates[5], var, false, true));	//the end date before the end of the period
				}
			}
			else
			{	//match exact date
				String day = getVarString();
				String month = getVarString();
				
				if (dates[0] != null)
					sb.append(getStatement(var, day, UTILITY + ":" + reader.DAY));
				if (dates[2] != null)
					sb.append(getStatement(var, month, UTILITY + ":" + reader.MONTH));
				sb.append(getStatement(var, year, UTILITY + ":" + reader.YEAR));
				
				if (not)
					sb.append("FILTER(!(");
				else
					sb.append("FILTER(");
					
				if (dates[0] != null)
				{
					sb.append(getFilterStatement(day, "=", Integer.toString(dates[0])));
					sb.append(" && ");
				}
				if (dates[2] != null)
				{
					sb.append(getFilterStatement(month, "=", Integer.toString(dates[2])));
					sb.append(" && ");
				}
				sb.append(getFilterStatement(year, "=", Integer.toString(dates[4])));				
				
				if (not)
					sb.append("))");
				else
					sb.append(") ");
			}
		}
		return sb.toString();
	}
	
	/**	e.g. {?date Project:Year ?year} UNION {?date Project:EndYear ?year}
	 *
	 *	@param int type: 0 if a year, 1 if a month, 2 if a year
	 */
	private String getUnionStatement(String var1, String var2, boolean before, int type) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		StringBuffer property = new StringBuffer(UTILITY + ":");
		if (before)
			property.append("End");
		else
			property.append("Begin");
		
		switch (type)
		{
			case 0: sb.append(getStatement(var1, var2, UTILITY + ":" + reader.YEAR)); property.append(reader.YEAR); break;
			case 1: sb.append(getStatement(var1, var2, UTILITY + ":" + reader.MONTH)); property.append(reader.MONTH); break;
			case 2: sb.append(getStatement(var1, var2, UTILITY + ":" + reader.DAY)); property.append(reader.DAY); break;
		}
		sb.append("} UNION {");
		sb.append(getStatement(var1, var2, property.toString())); 
		sb.append("} ");		
		return sb.toString();
	}
	
	/**	Returns a String containing the comparison of a date variable against the given date,
	 *	either 'before' or 'after'
	 */
	private String getCompareDate(Integer day, Integer month, Integer year, String var, boolean not, boolean before) throws SPARQLException
	{
		StringBuffer sb = new StringBuffer();
		String endYear = getVarString();
		String operator = ">";
		if (before)
			operator = "<";
		
		StringBuffer filter = new StringBuffer();
		filter.append("FILTER(");
		if (not)
			filter.append("!(");
			
		sb.append(getUnionStatement(var, endYear, before, 0));		
		filter.append(getFilterStatement(endYear, operator, year.toString()));	//FILTER ((?year < 2002)
		
		if (month != null)
		{
			String endMonth = getVarString();
			sb.append(getUnionStatement(var, endMonth, before, 1));	
			
			filter.append(" || (");	//FILTER ((?year < 2002) || ((?year = 2002) && ((month < 5)
			filter.append(getFilterStatement(endYear, "=", year.toString()));	
			filter.append(" && (");
			filter.append(getFilterStatement(endMonth, operator, month.toString()));		
			
			if (day != null)
			{
				String endDay = getVarString();
				sb.append(getUnionStatement(var, endDay, before, 2));	
			
				filter.append(" || (");	//FILTER ((?year < 2002) || ((?year = 2002) && ((month < 5) || ((month = 5) && (day < 21)))))
				filter.append(getFilterStatement(endMonth, "=", month.toString()));	
				filter.append(" && ");
				filter.append(getFilterStatement(endDay, operator, day.toString()));		
				filter.append(")");
			}
			filter.append("))");
		}
		filter.append(") ");
		
		sb.append(filter.toString());
		return sb.toString();
	}

///////////*********** RDF CREATION ***********////////////////////////////////////

	/**	Transforms the semantic graph into RDF (RDFXML), which is stored
	 *	in the Sesame repository accessed by the given SesameReader. The RDF
	 *	is also returned.
	 *
	 *	@param sg	SemanticGraph
	 *	@param sesame SesameReader
	 *	@return String RDF
	 */
	public String getRDF(SemanticGraph sg, SesameReader sesame)
	{	//create Jena model
		model = ModelFactory.createOntologyModel();
		model.setNsPrefix(UTILITY, UtilityURI);
		model.setNsPrefix(RESOURCE, ResourceURI);
		model.setNsPrefix(TASK, TaskURI);
		map = new HashMap<String, Individual>();		
		addNode(sg.getRoot(), false);	//map the root node and all its decendants to RDF
		finish(sg);	//map any nodes that have been missed

		//convert that model to rdfxml and write it to a String
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		model.write(out, "RDF/XML");
		
		try
		{
			try
			{
				InputStream in = new ByteArrayInputStream(out.toByteArray());
				sesame.write(in);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			FileWriter fw = null;
			StringWriter sw = new StringWriter();
			model.write(sw, "RDF/XML");
			String result = sw.toString();
			sw.close();
			fw = new FileWriter("RDF.txt");
			PrintWriter w = new PrintWriter(fw);
			w.print(result);
			w.close();
			fw.close();
			return result;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	/**	Maps those nodes that were not mapped before, because (due to 'undo' actions)
	 *	they are only connected to the rest of the graph by outgoing edges instead
	 *	incoming, and have therefore been missed by the depth-first traversion of
	 *	the tree.
	 */
	private void finish(SemanticGraph sg)
	{	//ADD THE FEDORA URI WITH PROPERTY 'HASURI'
		Individual object = map.get(sg.getRoot().getID());
		OntProperty hasUri = reader.getProperty("HasURI");
		addProperty(object, hasUri, sg.getURI());
		
		OntProperty ourSpacesDate = reader.getProperty("OurSpacesDate");	//adds timestamp to RDF
		double timestamp = System.currentTimeMillis();
		addProperty(object, ourSpacesDate, model.createTypedLiteral(timestamp, XSDDatatype.XSDdouble), null);
	
		//map any nodes that were overlooked
		for (Iterator it = sg.getNodes(); it.hasNext(); )
		{
			SGNode node = (SGNode) it.next();
			if (map.containsKey(node.getID()))	//if node has been mapped, continue
				continue;
			addNode(node, false);		//map the node and all its outgoing edges
		}	//incoming edges don't need to be mapped, as all edges incoming from one node must be outgoing from another!
	}
	
	/**	Goes depth-first through the semantic graph (recursing), mapping a node and
	 *	all its child properties to RDF
	 *
	 *	@param	Node to be mapped
	 *	@param	Boolean, true when this node is in the range of some property that has to be realised (and so cannot afford to return null)
	 *	@return	Individual corresponding to node
	 */
	private Individual addNode(SGNode node, boolean range)
	{
		OntClass c = reader.getClass(node);
		if (c == null)
			return null;
			
		List l = sortEdges(node);
		if ((!range) && (l.size() == 0) && (node.mustRealise() > SGNode.NEW))	//if neither the node nor any of its edges have to be realised
			return null;		//do nothing
			
		Individual in = model.createIndividual(node.getUniqueID(), c);		
		if (node.mustRealise() == SGNode.NEW)	
		{	//only add the type if the node is new
			List<OntClass> superClasses = reader.getSuperClasses(c);
			for (int i = 0; i < superClasses.size(); i++)
			{	//for each superclass, add that this individual is an instance of them
				OntClass s = superClasses.get(i);	//only add those type statements about classes in the policygrid ontologies
				if ((s.getNameSpace() != null) && (s.getNameSpace().indexOf(POLICYGRID) > 0))		//otherwise you get a lot of nonsense...
					in.addProperty(RDF.type, s);
			}
		}
		map.put(node.getID(), in);
		
		for (int i = 0; i < l.size(); i++)
		{
			Object o = l.get(i);
			if (o instanceof SGEdge[])
			{
				SGEdge[] list = (SGEdge[]) o;
				OntProperty p = reader.getProperty(list[0].getLabel()); 
				Seq seq = model.createSeq(AutomaticGenerator.getUniqueID(p));
				List<Individual> individuals = new ArrayList<Individual>();
				
				for (int j = 0; j < list.length; j++)
				{
					SGNode target = (SGNode) list[j].getTarget();
					if (p.isDatatypeProperty())
					{
						if (target instanceof SGIntNode)
							seq.add(model.createTypedLiteral(((SGIntNode)target).getValue(), XSDDatatype.XSDint));
						else if (target instanceof SGDoubleNode)
							seq.add(model.createTypedLiteral(((SGIntNode)target).getValue(), XSDDatatype.XSDdouble));
						else if (target instanceof SGBooleanNode)
							seq.add(model.createTypedLiteral(((SGNode)target).getNLLabel(reader), XSDDatatype.XSDboolean));
						else
							seq.add(target.getLabel());
					}
					else if (map.containsKey(target.getID()))
					{
						Individual indi = map.get(target.getID());
						seq.add(indi);
						individuals.add(indi);
					}
					else if (target instanceof SGDateNode)
					{
						Individual indi = createDate((SGDateNode) target);
						seq.add(indi);
						individuals.add(indi);
					}
					else		//recurse
					{
						Individual indi = addNode(target, true);
						seq.add(indi);
						individuals.add(indi);
					}
				}
				addProperty(in, p, seq, individuals);				
			}
			else
			{
				SGEdge edge = (SGEdge) o;
				SGNode target = (SGNode) edge.getTarget();
				OntProperty p = reader.getProperty(edge.getLabel()); 
	
				if (p.isDatatypeProperty())
				{
					if (target instanceof SGIntNode)
						addProperty(in, p, model.createTypedLiteral(((SGIntNode)target).getValue(), XSDDatatype.XSDint), null);
					else if (target instanceof SGDoubleNode)
						addProperty(in, p, model.createTypedLiteral(((SGDoubleNode)target).getValue(), XSDDatatype.XSDdouble), null);
					else if (target instanceof SGBooleanNode)
						addProperty(in, p, model.createTypedLiteral(((SGBooleanNode)target).getNLLabel(reader), XSDDatatype.XSDboolean), null);
					else
						addProperty(in, p, ((SGNode)target).getLabel());
				}
				else if (map.containsKey(target.getID()))		//node that has been realised already, so don't recurse
					addProperty(in, p, map.get(target.getID()), null);
				else if (target instanceof SGDateNode)
					addProperty(in, p, createDate((SGDateNode) target), null);
				else		//recurse
					addProperty(in, p, addNode(target, true), null);
			}
		}
		return in;
	}
	
	private void addProperty(Individual source, OntProperty p, String target)
	{
		if (target == null)
			return;

		List<OntProperty> superProps = reader.getSuperProperties(p);
		superProps.add(p);	
		for (int i = 0; i < superProps.size(); i++)
			source.addProperty(superProps.get(i), target);
	}
	
	/**	Adds not just the property to the model, but also all its superproperties
	 *	and their inverses
	 */
	private void addProperty(Individual source, OntProperty p, RDFNode target, List<Individual> individuals)
	{
		List<OntProperty> superProps = reader.getSuperProperties(p);
		superProps.add(p);	
		for (int i = 0; i < superProps.size(); i++)
		{
			OntProperty s = superProps.get(i);	//only add those type statements about classes in the policygrid ontologies
			source.addProperty(s, target);
			OntProperty inverse = reader.getInverse(s);
			if (inverse != null)
			{
				if (target instanceof Individual)
					((Individual)target).addProperty(inverse, source);
				else if (individuals != null)
					for (int j = 0; j < individuals.size(); j++)
						individuals.get(j).addProperty(inverse, source);
			}
		}
	}
	
	/**	Maps a SGDateNode unto a Date individual. This can be a DatePoint or a
	 *	DatePeriod.
	 */
	private Individual createDate(SGDateNode node)
	{
		Integer[] dates = node.getDayMonthYear();
		Individual in = null;
		
		if (node.isPeriod())
		{
			OntClass date = reader.getClass(reader.DATEPERIOD);
			in = model.createIndividual(node.getUniqueID(), date);
			in.addProperty(RDF.type, date);
							
			if (dates[0] != null)
				in.addProperty(reader.getProperty(reader.BEGINDAY), model.createTypedLiteral(dates[0], XSDDatatype.XSDint));
			if (dates[2] != null)
				in.addProperty(reader.getProperty(reader.BEGINMONTH), model.createTypedLiteral(dates[2], XSDDatatype.XSDint));
			in.addProperty(reader.getProperty(reader.BEGINYEAR), model.createTypedLiteral(dates[4], XSDDatatype.XSDint));
				
			if (dates[1] != null)
				in.addProperty(reader.getProperty(reader.ENDDAY), model.createTypedLiteral(dates[1], XSDDatatype.XSDint));
			if (dates[3] != null)
				in.addProperty(reader.getProperty(reader.ENDMONTH), model.createTypedLiteral(dates[3], XSDDatatype.XSDint));
			in.addProperty(reader.getProperty(reader.ENDYEAR), model.createTypedLiteral(dates[5], XSDDatatype.XSDint));
		}
		else
		{	//if it's a point in time, make it a 'DatePoint'
			OntClass date = reader.getClass(reader.DATEPOINT);
			in = model.createIndividual(node.getUniqueID(), date);
			in.addProperty(RDF.type, date);
							
			if (dates[0] != null)
				in.addProperty(reader.getProperty(reader.DAY), model.createTypedLiteral(dates[0], XSDDatatype.XSDint));
			if (dates[2] != null)
				in.addProperty(reader.getProperty(reader.MONTH), model.createTypedLiteral(dates[2], XSDDatatype.XSDint));
			in.addProperty(reader.getProperty(reader.YEAR), model.createTypedLiteral(dates[4], XSDDatatype.XSDint));	
		}
		in.addProperty(RDF.type, reader.getClass(reader.DATE));
		return in;
	}
	
	/**	Sorts the outgoing edges of the node previous to realising the RDF
	 */
	private ArrayList sortEdges(SGNode source)
    {
    	Iterator it = source.getOutgoingEdges();
    	ArrayList result = new ArrayList<SGEdge>();
    	List<String> processedEdges = new ArrayList<String>();
    	while (it.hasNext())
    	{
    		SGEdge e = (SGEdge) it.next();
    		if (e.getLabel().equals("ID"))	//ID is only a helper property that wysiwym uses, and is stored in rdf:about in the metadata
    			continue;
    		if (processedEdges.contains(e.getID()))	//edge already been mapped
    			continue;
    		if (e.mustRealise() > SGNode.NEW)	//if edge came from the database, don't map it again
    			continue;
    			
    		SGEdge[] temp = source.getNewOutgoingEdgesArray(e.getLabel());		//get all edges with this label
    		if (temp.length > 1)		//group identical edges together
    		{
    			Arrays.sort(temp);	//sort the list by the rank order nrs of the edges
    			result.add(temp);	//add the list with double nodes; they will be aggregated later on
    			for (int i = 0; i < temp.length; i++)
    				processedEdges.add(temp[i].getID());
    		}
    		else
    			result.add(temp[0]);		//save single edges for later processing
    	}
    	return result;
    }
}