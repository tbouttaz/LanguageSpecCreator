package WYSIWYM.transformer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.naming.NameAlreadyBoundException;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQueryResult;

import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.QueryResultGraph;
import WYSIWYM.model.QueryValueNode;
import WYSIWYM.model.SGAbstractNode;
import WYSIWYM.model.SGAddressNode;
import WYSIWYM.model.SGBooleanNode;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGDoubleNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGIntNode;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SGStringNode;
import WYSIWYM.model.SemanticGraph;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.OntologyWriter;
import WYSIWYM.ontology.QueryResult;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.util.SesameException;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.impl.Util;

/**	AutomaticGenerator adds automatically generated information to the semantic graph,
 *	such as the date of deposit and the depositor details. It also reads information from
 *	the Sesame archive about the user or objects the user has requested information about
 *	through browsing options, and adds it to the Graph. Finally it also retrieves information
 *	about search results and put it in QueryResultGraphs.
 *
 *	@author Feikje Hielkema
 *	@version 1.1 27-02-2007
 *
 *	@version 1.2 27-02-2008
 */
public class AutomaticGenerator
{
	private OntologyReader reader;
	private SesameReader sesame;
	private SemanticGraphTransformer sgt;
	private Map<String, SGNode> idMap = new HashMap<String, SGNode>();
	
	/**	Constructor, takes an ontology model and Sesame archive
	 *	@param r Ontology
	 *	@param s Sesame
	 */
	public AutomaticGenerator(OntologyReader r, SesameReader s)
	{
		reader = r;
		sesame = s;
	}
	
	/**	Retrieves the current date and stores it in an SGDateNode
	 *
	 *	@return	SGDateNode containing the current date
	 */
	public static SGDateNode getCurrentDate()
	{
		Calendar cal = new GregorianCalendar();
    	int year = cal.get(Calendar.YEAR);             // 2002
    	int month = cal.get(Calendar.MONTH);           // 0=Jan, 1=Feb, ...
    	month++;	//months in calendar run from 0 to 11, while we have 1 to 12
    	int day = cal.get(Calendar.DAY_OF_MONTH);      // 1...
    	
    	return new SGDateNode(day, month, year);
	}
	
	/**	Returns a unique identifier (concatenation of namespace and unique ID)
	 *	for a new resource to be added to the database
	 *	@param c OntResource
	 *	@return String with a unique id
	 */
	public static String getUniqueID(OntResource c)
	{
		StringBuffer sb = new StringBuffer(c.getNameSpace());
		sb.append(UUID.randomUUID().toString());
		return sb.toString();
	}
	
	/**	Expands the SemanticGraph with date of deposit and depositor.
	 *	@param sgt SemanticGraphTransformer
	 *	@param user SGNode representing user
	 */
	public static void expandGraph(SemanticGraphTransformer sgt, SGNode user)
	{
		expandGraph(sgt, user, (SGNode) sgt.getGraph().getRoot());
	}
	
	/**	Expands the SemanticGraph with date of deposit and depositor.
	 *	@param sgt SemanticGraphTransformer
	 *	@param user SGNode representing user
	 *	@param source Root SGNode of Graph
	 */
	public static void expandGraph(SemanticGraphTransformer sgt, SGNode user, SGNode source)
	{
		try
		{
			SGNode date = getCurrentDate();
			SGEdge dateEdge = sgt.makeEdge("DateOfDeposit", source, date);
			dateEdge.setRemovable(false);
			sgt.addEdge(dateEdge);
		
			SGEdge nameEdge = sgt.makeEdge("DepositedBy", source, user);
			nameEdge.setRemovable(false);
			sgt.addEdge(nameEdge);
		}
		catch (NameAlreadyBoundException e)
		{
			System.out.println("TextFrame 85: NameAlreadyBoundException should not be possible here!");
			e.printStackTrace();
		}
	}
	
	/**	Expands the SemanticGraph with date of deposit, depositor and project.
	 *	@param sgt SemanticGraphTransformer
	 *	@param user SGNode representing user
	 *	@param projectID Project Sesame ID
	 */
	public static void expandGraph(SemanticGraphTransformer sgt, SGNode user, String projectID)
	{
		expandGraph(sgt, user, sgt.getGraph().getRoot(), projectID);
	}
	
	/**	Expands the SemanticGraph with date of deposit, depositor and project.
	 *	@param sgt SemanticGraphTransformer
	 *	@param user SGNode representing user
	 *	@param source Root SGNode of Graph
	 *	@param projectID Project Sesame ID
	 */
	public static void expandGraph(SemanticGraphTransformer sgt, SGNode user, SGNode source, String projectID)
	{
		expandGraph(sgt, user, source);
		if (projectID != null)
		{
			try
			{
				SGNode project = sgt.getGraph().getUniqueIDNode(projectID);
				if (project == null)
					return;
				//LATER WE WANT TO DISTINGUISH BETWEEN DIFFERENT SUBPROPERTIES OF PRODUCEDINPROJECT
				SGEdge projectEdge = sgt.makeEdge("ProducedInProject", source, project);
				sgt.addEdge(projectEdge);
			}
			catch (NameAlreadyBoundException e)
			{
				System.out.println("TextFrame 104: NameAlreadyBoundException should not be possible here!");
				e.printStackTrace();
			}
		}
	}
	
	/**	Retrieves all information about this object from the archive and puts it in the SemanticGraph.
	 *	@param user Username
	 *	@param id Sesame ID of object
	 *	@return SemanticGraphTransformer
	 *	@throws SesameException
	 */
	public SemanticGraphTransformer getObjectInformation(String user, String id) throws SesameException
	{
		idMap = new HashMap<String, SGNode>();
		try
		{	//create a new node for this object
			SGNode result = new SGNode("x");	
			sgt = new SemanticGraphTransformer(new SemanticGraph(user), reader);		
			SGNode idTarget = new SGStringNode(id);	//add the ID
			SGEdge idEdge = sgt.makeEdge("ID", result, idTarget);
			sgt.addEdge(idEdge);
			//TODO URI: retrieve the nameSpace from the ID of the resource with a sparql query:
			String getTypeSparqlQuery = OntologyWriter.getTypeQuery(id);
			QueryResult qr = sesame.queryBinding(getTypeSparqlQuery, id);
			List<String> types = qr.getBindingValues(id, "type");
			if (!types.isEmpty()) {
				String uri = types.get(0);
				result.setNameSpace(uri.substring( 0, Util.splitNamespace(uri)) );
			}
			
			sgt.getGraph().setRoot(result);	//and add it to the graph
			idMap.put(id, result);
			expandNode(result, id, false, false);	//add all information about the user from the database
			return sgt;
		}
		catch (NameAlreadyBoundException e)
		{
			return null;
		}	//impossible
	}
	
	/**	Return semantic graph with all information about a user; but without
	 *	anchors!
	 *	@param user Username
	 *	@param  id Sesame ID of object
	 *	@return SemanticGraphTransformer
	 *	@throws SesameException
	 */
	public SemanticGraphTransformer getUserInfoNoAnchors(String user, String id) throws SesameException
	{
		idMap = new HashMap<String, SGNode>();
		
		try
		{	//create a new node for this object
			SGNode result = new SGNode("Person");	
			sgt = new SemanticGraphTransformer(new SemanticGraph(user), reader);		
			SGNode idTarget = new SGStringNode(id);	//add the ID
			SGEdge idEdge = sgt.makeEdge("ID", result, idTarget);
			sgt.addEdge(idEdge);
			sgt.getGraph().setRoot(result);	//and add it to the graph
			idMap.put(id, result);
			expandNode(result, id, false);	//add all information about the user from the database

			for (Iterator it = sgt.getGraph().getNodes(); it.hasNext(); )
			{
				SGNode node = (SGNode) it.next();
				node.setRealise(SGNode.SHOW);		//but ensure all nodes are shown
				node.setAnchor(null);				//and there are no anchors
			}
			return sgt;
		}
		catch (NameAlreadyBoundException e)
		{
			return null;
		}	//impossible
	}
	
	/**	Should retrieve all information about the currently logged in user from
	 *	the database. Not all this information needs to be presented in the text,
	 *	but it should be in the SemanticGraph to prevent the violation of cardinality
	 *	constraints etc.
	 *	@param username Username
	 *	@param userID User ID
	 *	@return SemanticGraphTransformer with user information
	 *	@throws SesameException
	 */
	public SemanticGraphTransformer getUserInfo(String username, String userID) throws SesameException
	{	
		idMap = new HashMap<String, SGNode>();
		
		try
		{	//create a new node for this user
			SGNode result = new SGNode("Person");	
			sgt = new SemanticGraphTransformer(new SemanticGraph(username), reader);	//result, username, reader);		
			SGNode idTarget = new SGStringNode(userID);	//add the ID
			SGEdge idEdge = sgt.makeEdge("ID", result, idTarget);
			sgt.addEdge(idEdge);
			sgt.getGraph().setRoot(result);	//and add it to the graph
			idMap.put(userID, result);

			if (expandNode(result, userID, false))		//add all information about the user from the database
				result.setRealise(SGNode.HIDE);		//but don't put it in the text
	
			if (result.getOutgoingEdges("Name").size() == 0)
			{	//if the name isn't added yet (e.g. because this user is not yet in the database), do that now.
				SGNode nameNode = new SGStringNode(username);
				SGEdge edge = sgt.makeEdge("Name", result, nameNode);
				edge.setRemovable(false);
				sgt.addEdge(edge);
			}
		}
		catch (NameAlreadyBoundException e)
		{}	//impossible
		
		return sgt;
	}
	
	private boolean expandNode(SGNode result, String id, boolean hasName)  throws SesameException
	{
		return expandNode(result, id, hasName, true);
	}
	
	/**	Gets all statements about the given id and adds them to the given node
	 */
	private boolean expandNode(SGNode result, String id, boolean hasName, boolean hasType) throws SesameException
	{
		try
		{
			String query = OntologyWriter.getDescriptionQuery(id);
			GraphQueryResult graph = sesame.queryGraph(query);
			boolean addedInfo = false;
			List<String> types = new ArrayList<String>();
	
			while (graph.hasNext())
			{
				Statement s = graph.next();
				if (s.getObject() instanceof URI)
				{
					//TODO: URI: use URI instead of class name
//					String type = ((URI) s.getObject()).getLocalName();
					String type = ((URI) s.getObject()).stringValue();
					String namespace = s.getPredicate().getNamespace();
					if ((namespace.indexOf("www.policygrid.org") < 0) && (namespace.indexOf("www.mooney.net/geo") < 0))
					{
						if ((!hasType) && (s.getPredicate().toString().indexOf("www.w3.org/1999/02/22-rdf-syntax-ns#type") > 0))	//the type
							types.add(type);
						continue;	//don't include properties from other namespaces, 
					}			//as they won't be recognised by OntologyReader, and we already know the type			
				}
				String property = s.getPredicate().getLocalName();
				if (hasName && reader.useAsProperName(property))	//SGEdge.isNLName(property))
					continue;	//if the node already has all NL information, don't add it again
			
				SGNode target = null;
				
				Value object = s.getObject();
				if (object instanceof URI)
					target = getObjectInfo((URI) object, result, property);
				else if (object instanceof Literal)
					target = getDatatypeNode((Literal) object, property, result);
				else if (object instanceof BNode)
					getSequence((BNode) object, s.getPredicate().getLocalName(), result);
			
				if (target != null)
				{
					SGEdge edge = sgt.makeEdge(property, result, target);
					edge.setRealise(SGNode.HIDE);	//does not have to be realised in the text
					sgt.addEdge(edge);
					addedInfo = true;
				}
			}
			
			if (!hasType)
			{
				OntClass c = reader.getMostSpecificClass(types);	//get the most specific class type, and create a new SGNode
				if (c != null)
					result.setLabel(c.getLocalName());
			}
			return addedInfo;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SesameException("Exception when retrieving data from the database about " + id);
		}	
	}

	/**	Retrieves the components of a sequence and adds them to source
	 */
	private void getSequence(Value sequence, String property, SGNode source) throws SesameException
	{
		String id = sequence.toString();
		getSequence(id, property, source);
	}
	
	/**	Retrieves the components of a sequence and adds them to source
	 */
	private void getSequence(String id, String property, SGNode source) throws SesameException
	{
		try
		{
			String query = OntologyWriter.getDescriptionQuery(id);
		//	SesameReader sesame = new SesameReader();
			GraphQueryResult graph = sesame.queryGraph(query);

			while (graph.hasNext())
			{
				Statement s = graph.next();
				String ordering = s.getPredicate().getLocalName();
				int idx = -1;
				try	//try to get the rank order of the sequence
				{
					idx = Integer.parseInt(ordering.substring(1));	//e.g. property is '_1'
				}
				catch (NumberFormatException e)
				{	//must be some other kind of edge (e.g. type), which can be skipped
					continue;
				}
				
				try
				{
					Value object = s.getObject();	
					SGNode target = null;
					if (object instanceof URI)
						target = getObjectInfo((URI) object, source, property);
					else if (object instanceof Literal)
						target = getDatatypeNode((Literal) object, property, source);
						
					if (target != null)
					{
						SGEdge edge = sgt.makeEdge(property, source, target);
						sgt.addEdge(edge);				
						edge.setRealise(SGNode.HIDE);	//does not have to be realised in the text
						edge.setRealiseNr(idx);
					}
				}
				catch (NameAlreadyBoundException e)
				{	//very unlikely
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
	
	/**	Creates a DatatypeNode with the literal's value
	 */
	private SGNode getDatatypeNode(Literal lit, String property, SGNode source) throws SesameException
	{
		try
		{
			SGNode result;
			String value = lit.getLabel();

			if (lit.getDatatype() == null)
				result = new SGStringNode(value);
			else
			{
				String datatype = lit.getDatatype().getLocalName();	
				if (datatype == null)
					result = new SGStringNode(value);
				else if (datatype.equals("int"))
					result = new SGIntNode(new Integer(value));
				else if (datatype.equals("double") || datatype.equals("float"))
					result = new SGDoubleNode(new Double(value));
				else if (datatype.equals("boolean"))
					result = new SGBooleanNode(new Boolean(value));
				else if (property.equals("HasAbstract"))
					result = new SGAbstractNode(value);
				else
					result = new SGStringNode(value);
			}
			
			List<Edge> outgoing = source.getOutgoingEdges(property);
			for (int i = 0; i < outgoing.size(); i++)
			{	//if the graph already has a node with this property, this source and this value, don't add another edge!	
				SGNode target = (SGNode) outgoing.get(i).getTarget();
				if (target.getNLLabel(reader).equals(result.getNLLabel(reader)))
					return null;
//					return result;
			}
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SesameException("Error when creating semantic graph from data from the database.");
		}
	}
	
	/**	Retrieves the information about the object with the given id needed to present it in
	 *	the text - so not all information! Merely its type, NLName property, its URI and access conditions
	 *	(if it has them).
	 */
	private SGNode getObjectInfo(URI object, SGNode source, String property) throws SesameException
	{
		String id = object.toString();
		return getObjectInfo(id, source, property);
	}
	
	/**	Retrieves nl-information about this object from the archive (e.g. type, name, title, gender...) 
	 *	and adds it to the Graph. First though it checks whether the object is already in the Graph;
	 *	if so, return null, and if there is not yet an edge of this property, its inverse, or a super- 
	 *	or sub-property that connects them, an edge is added.
	 *
	 *	@param id Sesame ID
	 *	@param source Parent SGNode, source of edge
	 *	@param property Property name
	 *	@return new SGNode, or null if no new node was created
	 *	@throws SesameException
	 */
	public SGNode getObjectInfo(String id, SGNode source, String property) throws SesameException
	{
		OntProperty prop = reader.getProperty(property);
		try
		{	//Get all information needed to present this object in NL from the database
			if (idMap.containsKey(id))		//if there is already a node with this id in the graph
			{
				List<String> subProps = reader.getSubProperties(property);	
				List<Edge> outgoing = source.getOutgoingEdges(property);
				for (int i = 0; i < subProps.size(); i++)
					outgoing.addAll(source.getOutgoingEdges(subProps.get(i)));
			
				for (int i = 0; i < outgoing.size(); i++)	//if this node is already in the graph with this property or a sub-property, 	
					if (id.equals(((SGNode)outgoing.get(i).getTarget()).getUniqueID()))	//don't add another edge!
						return null;				//check if there has been an edge added between this source and target with a super property;
				
				List<String> superProps = reader.getSuperProperties(property);
				outgoing = new ArrayList<Edge>();
				for (int i = 0; i < superProps.size(); i++)
					outgoing.addAll(source.getOutgoingEdges(superProps.get(i)));

				for (int i = 0; i < outgoing.size(); i++)	//if this node is already in the graph with this property or a sub-property, 	
				{
					SGEdge edge = (SGEdge) outgoing.get(i);
					if (id.equals(edge.getTarget().getUniqueID()))	//don't add another edge!	
					{	//set the label to this more specific property, then return
						edge.setLabel(property);
						return null;
					}
				}

				String inverse = reader.getInverse(property);
				if (inverse != null)
				{	 //the same goes for the inverse property
					List<Edge> incoming = source.getIncomingEdges(inverse);
					subProps = reader.getSubProperties(inverse);	
					
					for (int i = 0; i < subProps.size(); i++)
						incoming.addAll(source.getIncomingEdges(subProps.get(i)));

					for (int i = 0; i < incoming.size(); i++)
						if (id.equals(((SGNode) incoming.get(i).getSource()).getUniqueID()))
							return null;		//check if the super property has already been created
					
					List<String> superInverse = reader.getSuperProperties(inverse);
					incoming = new ArrayList<Edge>();
					for (int i = 0; i < superInverse.size(); i++)
						incoming.addAll(source.getIncomingEdges(superInverse.get(i)));
					
					for (int i = 0; i < incoming.size(); i++)	//if this node is already in the graph with this property or a sub-property, 	
					{
						SGEdge edge = (SGEdge) incoming.get(i);
						if (id.equals(edge.getTarget().getUniqueID()))	//don't add another edge!	
						{	//set the label to this more specific property, then return
							edge.setLabel(inverse);
							return null ;
						}
					}
				}
				else if ((prop != null) && prop.isSymmetricProperty())
				{
					List<Edge> incoming = source.getIncomingEdges(property);
					for (int i = 0; i < incoming.size(); i++)
						if (id.equals(((SGNode) incoming.get(i).getSource()).getUniqueID()))
							return null;	//if property is symmetric, and a link already exists the other way, don't bother adding this one.
				}
				else
				{
					System.out.print(property);
					if (prop == null)
						System.out.println(" DOES NOT EXIST !!!!!!!!!!!!");
					else if (inverse == null)
						System.out.println("Does not have an inverse!!");
				}
							
				return idMap.get(id);		//if none of these edges exist, return the node so a new edge can be created
			}
			
			//TODO: URI classMap: might need to change the sparql query (to retrive other properties as well)
			String sparql = OntologyWriter.getNLQuery(id);	//i.e. name, title, gender and type
			QueryResult result = sesame.queryBinding(sparql, id);
			Map<String, List<String>> map = result.getBindings(id);

			if ((map == null) || (map.size() == 0))
			{
				try
				{
					getSequence(id, property, source);	//it may be because this is a sequence, so try that
				}
				catch (SesameException e)
				{}
				return null;
			}
			
			List<String> types = new ArrayList<String>();
			List<SGEdge> edges = new ArrayList<SGEdge>();
			
			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
			{	//create an edge for each nl property, and store the types in a list
				String key = (String) it.next();
				if (reader.useAsProperName(key) || key.equalsIgnoreCase("HasURI") || key.equalsIgnoreCase("AccessConditions"))
				{
					SGEdge e = sgt.makeEdge(key);
					SGNode target = new SGStringNode(map.get(key).get(0));	//NL properties can only have 1 value
					e.setTarget(target);
					e.setRealise(SGNode.HIDE);		//does not have to be realised in the text
					edges.add(e);
				}
				else
				{
					for (int i = 0; i < map.get(key).size(); i++)
					{	//must be rdf:type property; add all values to the types list
//						String value = map.get(key).get(i);
//						//adapted to foaf ontology (URIs use / instead of #)
//						int indexOfType = value.indexOf("#");
//						if (indexOfType < 0) {
//							indexOfType = value.lastIndexOf("/");
//						}
//						types.add(value.substring(indexOfType + 1));
//						if (map.get(key).get(i).equals("OurSpacesAccount")) {
//							types.add("http://www.policygrid.org/ourspacesVRE.owl#OurSpacesAccount");
//						} else if (map.get(key).get(i).equals("Project")) {
//							types.add("http://www.policygrid.org/project.owl#Project");
//						} else {
							types.add(map.get(key).get(i));
//						}
						
					}
				}
			}
			
			OntClass c = reader.getMostSpecificClass(types);	//get the most specific class type, and create a new SGNode
			if (c == null)
			{
				if ((types.size() > 0) && types.get(0).equals("Seq"))
					getSequence(id, property, source);
				return null;
			}
			
			String type = c.getLocalName();
			
			/*
			 * TODO: URI: change the name of the SGNode to full URI (might break everything else...)
			 * If change the name of the node to the URI, the URI will be used to identify the resource when NLG 
			 */
//			SGNode node = new SGNode(c.getNameSpace() + c.getLocalName());
			SGNode node = new SGNode(type);
			node.setNameSpace(c.getNameSpace());
			
			if (type.equals(reader.DATEPOINT) || type.equals(reader.DATEPERIOD))
				node = getDate(id);
			else if (type.equals("Address"))
				node = new SGAddressNode(type);
			
			SGNode idTarget = new SGStringNode(id);	//add the ID
			SGEdge idEdge = sgt.makeEdge("ID", node, idTarget);
			for (int i = 0; i < edges.size(); i++)	//add all the NL edges
			{
				sgt.addEdge(edges.get(i));
				edges.get(i).setSource(node);
			}
			sgt.addEdge(idEdge);
			
			idMap.put(id, node);	//add node to the 'existing id's' map
			node.setRealise(SGNode.INCOMPLETE);	//not all information about this object has been retrieved yet!
			return node;		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SesameException("Error when creating semantic graph from data from the database.");
		}
	}
	
	/**	Reads and builds an SGDateNode from the database.
	 */
	private SGDateNode getDate(String id) throws SesameException
	{
		SGDateNode result = new SGDateNode();
		try
		{
			String query = OntologyWriter.getDescriptionQuery(id);
			GraphQueryResult graph = sesame.queryGraph(query);
	
			while (graph.hasNext())
			{
				Statement s = graph.next();
				String namespace = s.getPredicate().getNamespace();
				if ((namespace.indexOf("www.policygrid.org") < 0) && (namespace.indexOf("www.mooney.net/geo") < 0))
					continue;
							
				String property = s.getPredicate().getLocalName();	
				Value object = s.getObject();
				if (!(object instanceof Literal))	//ignore any other statements; we're only after the literals
					continue;
				
				Literal lit = (Literal) object;
				String value = lit.getLabel();
				try
				{
					result.setValue(property, Integer.parseInt(value));
				}
				catch(NumberFormatException e)
				{
					System.out.println("Exception when trying to create an Integer from " + value);
				}
			}
			return result;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SesameException("Exception when retrieving data from the database about " + id);
		}	
	}
	
	/**	Retrieves nl-information about the given resource ID and adds it to the graph
	 *	@param sgt SemanticGraphTransformer
	 *	@param id Sesame ID
	 *	@return SGNode
	 *	@throws SesameException
	 */
	public SGNode getNLInformation(SemanticGraphTransformer sgt, String id) throws SesameException
	{
		try
		{
			String sparql = OntologyWriter.getNLQuery(id);	//i.e. name, title, gender and type
			QueryResult result = sesame.queryBinding(sparql, id);
			Map<String, List<String>> map = result.getBindings(id);
			if ((map == null) || (map.size() == 0))
				return null;
			
			List<String> types = new ArrayList<String>();
			List<SGEdge> edges = new ArrayList<SGEdge>();	
			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
			{	//create an edge for each nl property, and store the types in a list
				String key = (String) it.next();
				if (reader.useAsProperName(key) || key.equalsIgnoreCase("HasURI") || key.equalsIgnoreCase("AccessConditions"))
				{
					SGEdge e = sgt.makeEdge(key);
					SGNode target = new SGStringNode(map.get(key).get(0));	//NL properties can only have 1 value
					e.setTarget(target);
					e.setRealise(SGNode.HIDE);		//does not have to be realised in the text
					edges.add(e);
				}
				else
				{
					for (int i = 0; i < map.get(key).size(); i++)
					{	//must be rdf:type property; add all values to the types list
						String value = map.get(key).get(i);
						types.add(value.substring(value.indexOf("#") + 1));
					}
				}
			}
			
			OntClass c = reader.getMostSpecificClass(types);	//get the most specific class type, and create a new SGNode
			if (c == null)
				return null;
			
			String type = c.getLocalName();
			SGNode node = new SGNode(type);
			if (type.equals(reader.DATEPOINT) || type.equals(reader.DATEPERIOD))
				node = getDate(id);
			else if (type.equals("Address"))
				node = new SGAddressNode(type);
			
			SGNode idTarget = new SGStringNode(id);	//add the ID
			SGEdge idEdge = sgt.makeEdge("ID", node, idTarget);
			for (int i = 0; i < edges.size(); i++)	//add all the NL edges
			{
				sgt.addEdge(edges.get(i));
				edges.get(i).setSource(node);
			}
			sgt.addEdge(idEdge);
			
			idMap.put(id, node);	//add node to the 'existing id's' map
			node.setRealise(SGNode.INCOMPLETE);	//not all information about this object has been retrieved yet!
			return node;		
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SesameException("Error when creating semantic graph from data from the database.");
		}
	}
	
	/**	Retrieves all information about the resource corresponding to the given node,
	 *	that has been added to the database at some earlier point, and adds it to
	 *	the semantic graph
	 *
	 *	@param	node The placeholder SGNode about which all information should now be retrieved
	 *	@param	sgt SemanticGraphTransformer holding the semantic graph to which the retrieved information will be added
	 *	@throws SesameException
	 */
	public void getInformation(SGNode node, SemanticGraphTransformer sgt) throws SesameException
	{
		getInformation(node, sgt, true, true);
	}
	
	/**	Retrieves all information about the resource corresponding to the given node,
	 *	that has been added to the database at some earlier point, and adds it to
	 *	the semantic graph
	 *
	 *	@param	node The placeholder SGNode about which all information should now be retrieved
	 *	@param	sgt SemanticGraphTransformer holding the semantic graph to which the retrieved information will be added
	 *	@param	hasNL True if the Graph already contains nl-information about this node
	 *	@param 	hasType True if the Graph already contains this node's class type.
	 *	@throws SesameException
	 */
	public void getInformation(SGNode node, SemanticGraphTransformer sgt, boolean hasNL, boolean hasType) throws SesameException
	{
		this.sgt = sgt;
		idMap = new HashMap<String, SGNode>();
		for (Iterator it = sgt.getGraph().getNodes(); it.hasNext(); )
		{	//add all nodes with a unique id in the semantic graph to the id map
			SGNode n = (SGNode) it.next();
			String id = n.getUniqueID();
			if (id != null)
				idMap.put(id, n);
		}
		
		try
		{
			if (expandNode(node, node.getUniqueID(), hasNL, hasType))	//if any information was added
				node.setRealise(SGNode.SHOW);	//show it in the text
			else
				node.setRealise(SGNode.NOINFO);
			System.out.println("Node must be realised: " + node.mustRealise());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**	Retrievs the information about the object with the given ID that is asked for in the given QueryGraph,
	 *	and adds it to it.
	 *	Preferred methods are getQueryResultDescription(QueryResult, SemanticGraph) or getQueryGraph(String, SemanticGraph)!
	 *	@param 	id Sesame ID
	 *	@param	parent Parent SGNode, source of property
	 *	@param  property Property name
	 *	@param	sgt QueryBuilder holding the QueryGraph
	 *	@throws SesameException
	 */
	public SGNode getQueryInformation(String id, SGNode parent, String property, QueryBuilder sgt) throws SesameException
	{
		this.sgt = sgt;
		idMap = new HashMap<String, SGNode>();
		for (Iterator it = sgt.getGraph().getNodes(); it.hasNext(); )
		{	//add all nodes with a unique id in the semantic graph to the id map
			SGNode n = (SGNode) it.next();
			String i = n.getUniqueID();
			if (i != null)
				idMap.put(i, n);
		}
		
		try
		{
			return getObjectInfo(id, parent, property);
		}
		catch (Exception e)
		{
			throw new SesameException(e.getMessage());
		}
	}
	
	/**	Returns a graph containing the requested information about all matches of
	 *	the query.
	 *	@param matches Search result
	 *	@param queryGraph QueryGraph
	 *	@throws SesameException
	 */
	public QueryResultGenerator getQueryResultDescription(QueryResult matches, SemanticGraph queryGraph) throws SesameException
	{
		QueryResultGraph graph = new QueryResultGraph(queryGraph.getUser());
		sgt = new QueryResultGenerator(graph, reader, sesame);
		idMap = new HashMap<String, SGNode>();
		List<SGNode> list = new ArrayList<SGNode>();
		list.add(queryGraph.getRoot());
		
		for (Iterator it = matches.getIDs(); it.hasNext(); )
			graph.addRoot(expandQueryResult((String) it.next(), null, list, null));
		FeedbackTextGenerator.printGraph("QueryResult", graph);
		return (QueryResultGenerator) sgt;
	}
	
	/**	Retrieves the information about this id that is relevant to the user's query,
	 *	and puts it in a semantic graph.
	 *
	 *	@param id Sesame ID
	 *	@param queryGraph QueryGraph
	 *	@throws SesameException
	 */
	public SemanticGraphTransformer getQueryInformation(String id, SemanticGraph queryGraph) throws SesameException
	{
		sgt = new SemanticGraphTransformer(new SemanticGraph(queryGraph.getUser()), reader);
		idMap = new HashMap<String, SGNode>();
		List<SGNode> list = new ArrayList<SGNode>();
		list.add(queryGraph.getRoot());
		SGNode root = expandQueryResult(id, null, list, null);
		sgt.getGraph().setRoot(root);
		FeedbackTextGenerator.printGraph("QueryResult", sgt.getGraph());
		return sgt;
	}	
		
	/**	Retrieves the information about this node that is relevant to the user's query.
	 */
	private SGNode expandQueryResult(String id, SGNode source, List<SGNode> graphSource, String superProp) throws SesameException
	{
		if (idMap.containsKey(id))
			return idMap.get(id);
			
		try
		{
			SGNode node = new SGNode("x");	//don't know the type yet, x is a placeholder
			node.setUniqueID(id);
			idMap.put(id, node);
			
			String query = OntologyWriter.getDescriptionQuery(id);
			GraphQueryResult graph = sesame.queryGraph(query);
			List<String> types = new ArrayList<String>();
	
			while (graph.hasNext())
			{
				Statement s = graph.next();
				String property = s.getPredicate().getLocalName();
				
				if (s.getObject() instanceof URI)
				{
					String type = ((URI) s.getObject()).getLocalName();
					if ((source != null) && type.equals(source.getUniqueID()) && reader.getInverse(superProp).equals(property))
						continue;	//skip this property, as its already in the graph		
					if (s.getPredicate().toString().indexOf("www.w3.org/1999/02/22-rdf-syntax-ns#type") > 0)	//the type
					{	//if this node is a sequence, map the nodes in the sequence with the given property
						if (type.equals("Seq") && (superProp != null))
						{
							getSequence(id, superProp, source, graphSource);
							return null;
						}
						else
							types.add(type);
						continue;
					}
				}
				String namespace = s.getPredicate().getNamespace();
				if ((namespace.indexOf("www.policygrid.org") < 0) && (namespace.indexOf("www.mooney.net/geo") < 0))
					continue;	//skip all other properties outside the policygrid namespace
				
				Value object = s.getObject();
				List<Edge> outgoing = new ArrayList<Edge>();
				for (int i = 0; i < graphSource.size(); i++)
					outgoing.addAll(getMatchingEdges(graphSource.get(i), property, object));
				if ((!reader.useAsProperName(property)) && outgoing.size() == 0) 
					continue;		//only add information that was asked for, about the depositor, or is the name
					
				SGNode target = null;
				if (object instanceof Literal)
					target = getDatatypeNode((Literal) object, property, node);
				else if (object instanceof URI)
				{
					List<SGNode> recurse = new ArrayList<SGNode>();
					for (int i = 0; i < outgoing.size(); i++)
					{
						SGNode n = (SGNode) outgoing.get(i).getTarget();
						if ((n.getOutgoingEdgesWithoutNLNumber(reader) > 0) && (!recurse.contains(n)))
							recurse.add(n);
					}
					
					if (recurse.size() > 0)
						expandQueryResult(object.toString(), node, recurse, property);
					else	//else just get enough information to present the node in NL and its uri
						target = getObjectInfo((URI) object, node, property);
				}
				
				if ((target != null) && (!hasEdge(node, target, property)))
				{	//check that the edge or its inverse is not already there
					SGEdge edge = sgt.makeEdge(property, node, target);	//if not, add the edge
					edge.setRealise(SGNode.SHOW);
					sgt.addEdge(edge);
				}
			}
			
			OntClass c = reader.getMostSpecificClass(types);	//get the most specific class type, and create a new SGNode
			if (c != null)
			{
				node.setLabel(c.getLocalName());
				if (c.getLocalName().equals("Address"))
					node = new SGAddressNode(node, sgt);
			}
			
			if ((superProp != null) && (!hasEdge(source, node, superProp)))
			{
				SGEdge edge = sgt.makeEdge(superProp, source, node);
				edge.setRealise(SGNode.SHOW);
				sgt.addEdge(edge);
			}
			else
				sgt.addNode(node);		//node may not have been added to the graph yet!
			node.setRealise(SGNode.INCOMPLETE);
			return node;
		}
		catch (Exception e)
		{
			e.printStackTrace(); 	
			throw new SesameException("Exception when retrieving data from the database about " + id);
		}	
	}
	
	private boolean hasEdge(SGNode source, SGNode target, String property)
	{
		List<Edge> edges = source.getOutgoingEdges(property);
		for (int i = 0; i < edges.size(); i++)
		{
			SGNode node = (SGNode) edges.get(i).getTarget();
			String id = node.getUniqueID();
			if ((id != null) && id.equals(target.getUniqueID()))
				return true;
		}		
		
		String inverse = reader.getInverse(property);
		if (inverse == null)
			return false;
	
		edges = target.getOutgoingEdges(inverse);
		for (int i = 0; i < edges.size(); i++)
		{
			SGNode node = (SGNode) edges.get(i).getTarget();
			if (node.getUniqueID().equals(source.getUniqueID()))
				return true;
		}
		
		return false;			
	}
	
	/**	Adds all edges outgoing from the given node in the query that have the label
	 *	'property'. It also adds edges that have the 'anything' label and whose
	 *	target node is of the same or super type as object
	 */
	private List<Edge> getMatchingEdges(SGNode source, String property, Value object) throws SesameException
	{
		List<Edge> result = source.getOutgoingEdges(property);
		List<Edge> any = source.getOutgoingEdges(QueryEdge.ANYTHING);
		for (int i = 0; i < any.size(); i++)
		{
			SGNode target = (SGNode) any.get(i).getTarget();
			if (object instanceof Literal)
			{
				if (!(target instanceof DatatypeNode))
					continue;
				//both are datatypes
				DatatypeNode node = (DatatypeNode) target;
				URI type = ((Literal)object).getDatatype();
				if (type == null) 
				{	//if they are both strings, add the edge to result
					if (node.getDatatype() == DatatypeNode.STRING)
						result.add(any.get(i));
				}
				else
				{	//if they are of the same datatype, add the edge to result
					String datatype = type.getLocalName();
					switch(node.getDatatype())
					{
						case 0: if (datatype.equalsIgnoreCase("string")) result.add(any.get(i)); break;
						case 1: if (datatype.equalsIgnoreCase("int")) result.add(any.get(i)); break;
						case 2: if (datatype.equalsIgnoreCase("double")) result.add(any.get(i)); break;
						case 3: if (datatype.equalsIgnoreCase("boolean")) result.add(any.get(i)); break;
					}
				}
			}
			else
			{	
				String typeName = target.getLabel();
				if (target instanceof DatatypeNode)
				{	//if target is a datatype, they don't match
					int type = ((DatatypeNode)target).getDatatype();
					if (type < 0)
						typeName = ((QueryValueNode)target).getClassType();
					else
						continue;
				}
				
				//both are individuals
				String id = object.toString();
				String sparql = OntologyWriter.getTypeQuery(id);
				QueryResult qr = sesame.queryBinding(sparql, id);
				List<String> types = qr.getBindingValues(id, "type");
				if (types == null)
					continue;
			
				for (int j = 0; j < types.size(); j++)
				{	//if object is the same or a superclass of target, add the edge to result
					if (types.get(j).indexOf(typeName) >= 0)
					{
						result.add(any.get(i));
						continue;
					}
				}
			}
		}
		
		return result;
	}
			
	/**	First parameter could be a BNode (a blank node) or a URI that is an RDF sequence.
	 *	This method extracts the components of the sequence.
	 *
	 */
	private void getSequence(String sequence, String property, SGNode source, List<SGNode> queryNodes) throws SesameException
	{
		try
		{
			String query = OntologyWriter.getDescriptionQuery(sequence);
			GraphQueryResult graph = sesame.queryGraph(query);

			while (graph.hasNext())
			{
				Statement s = graph.next();
				String ordering = s.getPredicate().getLocalName();
				int idx = -1;
				try	//try to get the rank order of the sequence
				{
					idx = Integer.parseInt(ordering.substring(1));	//e.g. property is '_1'
				}
				catch (NumberFormatException e)	//must be some other kind of edge (e.g. type), which can be skipped
				{
					continue;
				}
				
				try
				{
					Value object = s.getObject();	
					SGEdge edge = null;
					if (object instanceof URI)
					{
						SGNode target = expandQueryResult(object.toString(), source, queryNodes, property);
						List<Edge> list = target.getIncomingEdges(property);
						for (int i = 0; i < list.size(); i++)
						{
							edge = (SGEdge) list.get(i);
							edge.setRealiseNr(idx);
							String parentID = edge.getSource().getUniqueID();
							if ((parentID != null) && parentID.equals(source.getUniqueID()))
								break;
						}
					}
					else if (object instanceof Literal)
					{
						SGNode target = getDatatypeNode((Literal) object, property, source);
						if (target != null)
						{
							edge = sgt.makeEdge(property, source, target);
							edge.setRealise(SGNode.SHOW);
							edge.setRealiseNr(idx);
							sgt.addEdge(edge);	
						}
					}
				}
				catch (NameAlreadyBoundException e)
				{	//very unlikely
					e.printStackTrace();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw(new SesameException(e.getMessage()));
		}
	}
}