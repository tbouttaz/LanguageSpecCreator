package WYSIWYM.util;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import liber.edit.client.AnchorInfo;
import liber.edit.client.ExistingInstances;
import liber.edit.client.FormInfo;
import liber.edit.client.Hierarchy;
import liber.edit.client.InstanceData;
import liber.edit.client.QueryDateValue;
import liber.edit.client.TagCloud;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.Edge;
import WYSIWYM.model.FeedbackText;
import WYSIWYM.model.Node;
import WYSIWYM.model.QueryEdge;
import WYSIWYM.model.QueryGraph;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.model.SequenceEdgeComparator;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.OntologyWriter;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.ontology.Tag;
import WYSIWYM.transformer.AutomaticGenerator;
import WYSIWYM.transformer.BrowsingGenerator;
import WYSIWYM.transformer.FeedbackTextGenerator;
import WYSIWYM.transformer.QueryBuilder;
import WYSIWYM.transformer.QueryResultGenerator;
import WYSIWYM.transformer.SemanticGraphTransformer;

import com.hp.hpl.jena.ontology.OntProperty;

/**	This class contains the instances of one user session
 *
 *	@author Feikje Hielkema
 *	@version 1.4 October 2008
 */
public class LiberSession
{
	private OntologyReader reader;
	private SesameReader sesame;
	private String userID, uri, projectID;
	private String username = "test";
	private SemanticGraphTransformer userGraph;
	
	private FeedbackTextGenerator ft;
	private Map<String,BrowsingGenerator> browseMap = new HashMap<String,BrowsingGenerator>();
	private QueryBuilder qb;
	private HelpSession help;
	
	private long lastUpdated = new GregorianCalendar().getTimeInMillis();
	private List<SGNode> newNodes = new ArrayList<SGNode>();
	private List<SGNode> rangeNodes = new ArrayList<SGNode>();
	private List<String> queryResults;
	
	private UserInfo info = new UserInfo();
	private int type = 0;
	private List<Long> queryTimes = new ArrayList<Long>();
	
	/**	Editing session */
	public static final int EDIT = 0;	
	/**	Querying session */
	public static final int QUERY = 2;
	/**	Browsing session */
	public static final int BROWSE = 4;

	/**	Creats a new LiberSession for a user. It it's an editing session, the 
	 *	resource uri is stored.
	 *
	 *	@param	ontology OntModel containing ontology
	 *	@param	userID user id
	 *	@param	projectID	project id
	 *	@param	resource resource id (null if not an editing session)
	 *	@param	type type of session (edit, query or browse)
	 */
	public LiberSession(OntologyReader ontology, String userID, String projectID, String resource, int type) throws OntologyInputException, SesameException
	{
		reader = new OntologyReader(ontology);
		sesame = new SesameReader(false);
	//	sesame = new SesameReader(true);
	
	//	reader = new GeographyOntologyReader(ontology);
	//	sesame = new SesameReader("wysiwym", false);	//global
	//	sesame = new SesameReader("wysiwym", true);	//local
		this.userID = userID;
		this.projectID = projectID;
		this.type = type;
		
		if (type == EDIT)
		{	//resource is the URI where it can be found
			int start = resource.indexOf(">") + 1;
			int end = resource.indexOf("</");
			if ((start > 0) && (end > 0))	//don't set the feedbacktextgen yet
				uri = resource.substring(start, end);

			updateUserGraph();
			username = userGraph.getGraph().getRoot().getNLLabel(reader);
			userGraph.getGraph().setUser(username);
		}
	}
	
	/**	Debugging method, prints a timestamp
	 */
	public static void printTime()
	{
		System.out.println(new Date().toString());
	}
	
	/**	Initialises the session. If it's a browsing session, resource is the id
	 *	of the object the user wants to explore; otherwise it's its type.
	 *	@param	resource	resource id or type
	 *	@return	Array of AnchorString, containing feedback text
	 */
	public AnchorInfo[] init(String resource)
	{	//find the user's name
		try
		{
			if (type == EDIT)
			{	//resource is the resource type, init a description
				ft = new FeedbackTextGenerator(resource, username, userID, projectID, uri, userGraph, reader, sesame);
				return ft.getSurfaceText();
			}
			else if (type == QUERY)
			{	//resource is the resource type, initialise a query
				qb = new QueryBuilder(resource, "query", userID, reader, sesame); 
				return qb.getSurfaceText();
			}
			else if (type == BROWSE)
			{	//resource is the url where it can be found, and we want its id to view its information
				AutomaticGenerator ag = new AutomaticGenerator(reader, sesame);
				SemanticGraphTransformer sgt = ag.getObjectInformation(userID, resource);	//(String) ids.next());
			//	BrowsingGenerator bg = new BrowsingGenerator(sgt, reader, sesame, userID, false);
			//	addBrowsingGenerator(bg);
				ft = new BrowsingGenerator(sgt, reader, sesame, userID, true);
				return /**bg*/ft.getSurfaceText();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new AnchorInfo[0];	
	}
	
	/**	Initialises an editing session from an initial form.
	 *	@param	data 	The data contained in the form
	 *	@return Array of AnchorString, containing feedback text
	 */
	public AnchorInfo[] init(InstanceData data)
	{
		try
		{
			ft = new FeedbackTextGenerator(data, username, userID, projectID, uri, userGraph, reader, sesame);
			return ft.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new AnchorInfo[0];	
	}
	
	/**	Returns an RDF representation of the semantic graph
	 *	@param	type  Session type
	 *	@param	key  Browsing session key
	 *	@return String with RDF
	 */
	public String getRDF(int type, String key)
	{
		try
		{
			OntologyWriter w = new OntologyWriter(reader);
			if (browseMap.containsKey(key))
				return w.getRDF(browseMap.get(key).getGraph(), sesame);
			if ((type == EDIT) || (type == BROWSE))
			{
				addActivity();
				return w.getRDF(ft.getGraph(), sesame);
			}	
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;	
	}
	
	private void addActivity() throws Exception
	{
		String resourceID = ft.getGraph().getRoot().getUniqueID();
		String sparql = OntologyWriter.getIDNumberQuery(userID);
		WYSIWYM.ontology.QueryResult result = sesame.queryBinding(sparql, userID);
		List<String> str = result.getBindingValues(userID, "nr");
		if ((str != null) && (str.size() > 0))
		{
			int user = new Integer(str.get(0)).intValue();
			new Tag().addActivity(user, projectID, resourceID);
		}
	}
	
	/**	Prints the number of queries the user has produced, and how long each 
	 *	took
	 */
	public void printQueryTimes()
	{
		try
		{
			System.out.println("Printing query times");
			
			GregorianCalendar calendar = new GregorianCalendar();
			StringBuffer filename = new StringBuffer("QUERIES-");
			filename.append(calendar.get(calendar.DATE) + "_");
			filename.append(calendar.get(calendar.HOUR_OF_DAY) + "_");
			filename.append(calendar.get(calendar.MINUTE) + "_");
			filename.append(calendar.get(calendar.SECOND) + ".txt");
			
			FileWriter writer = new FileWriter(filename.toString());
			writer.write(Integer.toString(queryTimes.size()));
			writer.write(" queries were constructed. The times were:\n");
			double total = 0;
			for (int i = 0; i < queryTimes.size(); i++)
			{
				total += queryTimes.get(i).doubleValue();
				writer.write(Long.toString(queryTimes.get(i) / 1000) + " sec.\n");
			}
		
			writer.write("The average time per constructed query was ");
			writer.write(new Double((total / queryTimes.size()) / 1000).toString());
			writer.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**	Creates a SPARQL representation of the query, sends it to Sesame, and
	 *	returns descriptions of any matching resources.
	 *
	 *	@param	time Time it took the user to construct the query
	 *	@return AnchorInfo[][], feedback texts describing the search results
	 */
	public AnchorInfo[][] getSPARQL(long time)
	{
		try
		{
			queryTimes.add(new Long(time));
			
			Map<String, FeedbackText> map = qb.getSparql();
			if (map.size() == 0)
			{
				AnchorInfo[][] result = new AnchorInfo[1][1];
				result[0][0] = new AnchorInfo();
				result[0][0].setWords("No matches found");
				return result;
			}
			
			List<String> ids = new ArrayList<String>();
			AnchorInfo[][] result = new AnchorInfo[map.size()][0];
			int cntr = 0;
			
			for (Iterator it = map.keySet().iterator(); it.hasNext(); )
			{
				String id = (String) it.next();
				ids.add(id);
				result[cntr] = FeedbackTextGenerator.getSurfaceText(map.get(id));
				cntr++;
			}
			setQueryResults(ids);
			return result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new AnchorInfo[0][0];
		}
	}
	
	/**	Creates a SPARQL representation of the query, sends it to Sesame, and
	 *	returns a description of all matching resources.
	 *
	 *	@param	time Time it took to construct the query
	 *	@return	AnchorInfo[] feedback text with description of matches
	 */
	public AnchorInfo[] getQueryResult(long time)
	{
		try
		{
			queryTimes.add(new Long(time));
			QueryResultGenerator qrg = qb.getResult();
			if (qrg == null)
			{
				AnchorInfo[] result = new AnchorInfo[1];
				result[0] = new AnchorInfo();
				result[0].setWords("No matches found");
				return result;
			}
			qrg.init();
			addBrowsingGenerator(qrg);
			return qrg.getSurfaceText();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new AnchorInfo[0];
		}
	}
	
	///////////////////////// undo, redo	///////////////////////////////////
	/**	Undoes the last information-addition
	 *	@param type	session type
	 *	@param key	browsing session key
	 *	@return	AnchorInfo[] feedback text
	 */
	public AnchorInfo[] undo(int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			boolean moreUndo = generator.undo();
			return generator.getSurfaceText(moreUndo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Removes edges from the Semantic Graph
	 *	@param	anchor The selected anchor
	 *	@param 	property The selected property
	 *	@param	values values to be removed
	 *	@param	type type of session
	 *	@param	key browsing session key
	 *	@return	AnchorInfo[] with feedback text
	 */
	public AnchorInfo[] removeProperty(String anchor, String property, String[] values, int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			generator.removePropertyValues(anchor, property, values);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Removes a node from the Semantic Graph
	 *	@param	anchor Selected anchor
	 *	@param	type type of session
	 *	@param	key browsing session key
	 *	@return	AnchorInfo[] with feedback text
	 */
	public AnchorInfo[] removeAnchor(String anchor, int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			generator.removeAnchor(anchor);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Redoes the last undo
	 *	@param type	session type
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with feedback text
	 */
	public AnchorInfo[] redo(int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			if (generator.redo())
			{
				AnchorInfo[] info = generator.getSurfaceText();
				return info;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	//////////////////	get information	//////////////////////////////////////////
	
	/**	Retrieves existing instances that qualify to be a target of this property, and
	 *	instances that already are a target of the property and anchor! Returns an array
	 *	with first the range class name, then those objects already in the range, 
	 *	then a 'null', and then all eligible objects.
	 *
	 *	@param name property name
	 *	@param anchor anchor selected by user
	 *	@param type	type of session
	 *	@param key	Browsing session key
	 *	@return	ExistingInstances, Serialisable object containing existing and potential instances
	 *	for the range of this property
	 */
	public ExistingInstances getRangeInstances(String name, String anchor, int type, String key)
	{	
		FeedbackTextGenerator generator = getGenerator(type, key);
		newNodes = new ArrayList<SGNode>();
		rangeNodes = new ArrayList<SGNode>();
		List<String> newObjects = new ArrayList<String>();
		List<String> newID = new ArrayList<String>();
		List<String> rangeObjects = new ArrayList<String>();
		List<String> rangeID = new ArrayList<String>();
		
		OntProperty p = reader.getProperty(name);
		String[] classes = reader.getRangeClasses(name);	//get an NL-description of the range		
		Anchor a = generator.getText().getAnchor(anchor);
		Node source = generator.getGraph().getNode(a.getNode().getID());
	 	List<Edge> edgeList = source.getOutgoingEdges(name);
	 	String inverse = reader.getInverse(name);
	 	if (inverse != null)
	 		edgeList.addAll(source.getIncomingEdges(inverse));

	 	ExistingInstances result = new ExistingInstances();
	 	if (name.equals("ANYTHING"))
		{	//property is 'any property'; means any nodes in the graph are eligible!
			for (Edge e: edgeList)
				if (e.getLabel().equals(name))
				{
					SGNode target = (SGNode) e.getTarget();
					rangeObjects.add(target.getChoiceLabel(type == QUERY, reader));
					rangeNodes.add(target);
					rangeID.add(target.getUniqueID());
				}
		
			for (Iterator it = generator.getGraph().getNodes(); it.hasNext(); )
			{
				SGNode node = (SGNode) it.next();
				if (rangeNodes.contains(node) || (node instanceof DatatypeNode) || (node instanceof SGDateNode))
					continue;	//if the node is not yet in the range, and is not a datatype or date node, add it to result
				newObjects.add(node.getChoiceLabel(type == QUERY, reader));
				newID.add(node.getUniqueID());
				newNodes.add(node);
			}
		}
		else
		{
			SGEdge[] edges = (SGEdge[]) edgeList.toArray(new SGEdge[0]);
			Arrays.sort(edges, new SequenceEdgeComparator());
			for (int i = 0; i < edges.length; i++)
			{	//get the objects that are already in the range
				SGNode target = edges[i].getTarget();
				if (!edges[i].getLabel().equals(name))
					target = edges[i].getSource();
				rangeNodes.add(target);
				rangeObjects.add(target.getChoiceLabel(type == QUERY, reader));
				rangeID.add(target.getUniqueID());
			}

			for (int j = 0; j < classes.length; j++)
			{	//get any other nodes that are eligible
				List l = generator.getGraph().getNodesWithLabel(classes[j]); 
				for (int i = 0; i < l.size(); i++)
				{
					SGNode n = (SGNode) l.get(i);
					Anchor an = n.getAnchor();
					if (!rangeNodes.contains(n))	//this property with this
					{	//check that this node is not already in the range
						newNodes.add(n);
						newObjects.add(n.getChoiceLabel(type == QUERY, reader));
						newID.add(n.getUniqueID());
					}
				}
			}
		}

		String[] tmp = new String[0];
		result.setRange(rangeObjects.toArray(tmp), rangeID.toArray(tmp));
		result.setOther(newObjects.toArray(tmp), newID.toArray(tmp));	
		if (type == EDIT)
			result.setTagCloud(getObjectTagCloud(classes));	
		return result;
	}
	
	/**	Returns a tag cloud of all instances of the classes in range which are 
	 *	connected with the user or his project.
	 */
	private TagCloud getObjectTagCloud(String[] range)
	{	//Create a sparql query that returns the id's and names/titles of all objects connected to project or user
		try
		{
			List<String> ids = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			for (SGNode node : newNodes)
			{
				ids.add(node.getUniqueID());
				values.add(node.getNLLabel(reader));
			}
			for (SGNode node : rangeNodes)
			{
				ids.add(node.getUniqueID());
				values.add(node.getNLLabel(reader));
			}
			
			String sparql = new OntologyWriter(reader).getConnectedObjectsOfTypeQuery(range, userID, projectID);
			WYSIWYM.ontology.QueryResult result = sesame.queryBinding(sparql);
			List<String> props = reader.getProperNameProperties();
			if (props.contains("Gender"))
				props.remove("Gender");
				
			for (Iterator it = result.getIDs(); it.hasNext(); )
			{
				String id = (String) it.next();
				if (!ids.contains(id))		//don't add nodes twice
				{
					ids.add(id);
					values.add(result.getNLValue(id, props));
				}
			}
			
			int[] freq = new int[ids.size()];
			for (int i = 0; i < ids.size(); i++)
				freq[i] = 3;
			TagCloud tc = new TagCloud();
			tc.create(values.toArray(new String[0]), ids.toArray(new String[0]), freq);
			return tc;			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new TagCloud();
		}
	}
	
	/**	Returns the values of resourceID for the properties specified in 
	 *	formInformation.
	 *
	 *	@param resourceID	object id
	 *	@param	formInformation Specifies the properties in the form
	 *	@return	String[] with values
	 */
	public String[] getDescriptionValues(String resourceID, FormInfo[] formInformation)
	{
		try
		{
			List<String> properties = new ArrayList<String>();
			for (int i = 1; i < formInformation.length; i++)	//skip the first item, it's already filled in
				properties.add(formInformation[i].getProperty());
			
			String sparql = new OntologyWriter(reader).getPropertyValuesQuery(resourceID, properties);
			WYSIWYM.ontology.QueryResult result = sesame.queryBinding(sparql, resourceID);
			String[] values = result.getValuesArray(resourceID, properties);
			return values;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Returns the maximum of times this property can still be instantiated 
	 *	without violating a maximum cardinality constraint.
	 *	(0 if there is no restriction)
	 *
	 *	@param anchor Selected anchor
	 *	@param property Selected property
	 *	@param type Session type
	 *	@param key Browsing session key
	 *	@return Integer, maximum
	 */
	public Integer getMax(String anchor, String property, int type, String key)
	{
		return getGenerator(type, key).getMax(anchor, property);
	}
	
	/**	Retrieves the values already added for this property and anchor
	 *	@param	property Property name
	 *	@param	anchor Selected anchor
	 *	@param type	Session type
	 *	@param key	key of browsing session
	 *	@return	String[] with values already added for this property and anchor
	 */
	public String[] getAddedValues(String property, String anchor, int type, String key)
	{
		return getGenerator(type, key).getAddedValues(property, anchor);
	}
	
	/**	Returns the feedback text
	 *	@param type	type of session
	 *	@param key	key of browsing session
	 *	@return	AnchorInfo[], serialisable version of feedback text
	 */
	public AnchorInfo[] getFeedbackText(int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		try
		{
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Adds a new browsing session for the given resource.
	 *	@param id	resource id
	 *	@return AnchorInfo[], serialisable version of feedback text
	 */
	public AnchorInfo[] getBrowsingDescription(String id)
	{
		try
		{
			AutomaticGenerator ag = new AutomaticGenerator(reader, sesame);
			SemanticGraphTransformer sgt = ag.getObjectInformation(username, id);
			BrowsingGenerator browse = new BrowsingGenerator(sgt, reader, sesame, userID, false);
			addBrowsingGenerator(browse);
			return browse.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Adds a new browsing session for the resource whose index was selected
	 *	in query result.
	 *	@param idx	index
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] getBrowsingDescription(int idx)
	{
		try
		{	
			String id = queryResults.get(idx);
			SemanticGraphTransformer sgt = qb.getQueryResult(id);
			BrowsingGenerator browse = new BrowsingGenerator(sgt, reader, sesame, userID, false);
			addBrowsingGenerator(browse);
			return browse.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Tries to export an object in a browsing pane to the editing pane.
	 *	Returns null if the user's session is expired, false if the operation fails
	 *	and true if it succeeds.
	 *
	 *	@param anchor Selected anchor
	 *	@param key	browsing session key
	 *	@return	true if it succeeded
	 */
	public Boolean exportObject(String anchor, String key)
	{
		if (ft == null)
			return new Boolean(false);
		
		FeedbackTextGenerator generator = getGenerator(BROWSE, key);
		try
		{	//add the selected object to the Semantic Graph in edit
			ft.add(generator, anchor);
			return new Boolean(true);	
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new Boolean(false);
		}
	}	
	
	/**	Finds all objects in the archive of one of the given types and with	
	 *	the given name/title.
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param title	Name/title of object
	 *	@param types	Array with Possible types of object
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return String[] with serialisable version of feedback text
	 */
	public String[] checkDatabase(String anchor, String property, String title, String[] types, int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			String sparql = new OntologyWriter(reader).getObjectQuery(title, types);
			WYSIWYM.ontology.QueryResult result = sesame.queryBinding(sparql);
			result.clean(reader, generator, anchor, property);	
			
			String[] headers = new String[2];
			headers[0] = "Name";
			headers[1] = "Title";	
			return result.getHTML(headers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Finds all objects in the archive of one of the given types and with	
	 *	the given name/title.
	 *	@param	anchor int identifying anchor
	 *	@param property	property name
	 *	@param data	InstanceData provided about object
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return String[] with serialisable version of feedback text
	 */
	public String[] checkDatabase(String anchor, String property, InstanceData data, int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			String sparql = new OntologyWriter(reader).getObjectQuery(data);
			System.out.println("CHECKING DATABASE");
			WYSIWYM.ontology.QueryResult result = sesame.queryBinding(sparql);
			System.out.println(sparql + " ------ " + result.size());
			result.clean(reader, generator, anchor, property);	
			System.out.println(result.size());
			
			String[] headers = new String[2];
			headers[0] = "Name";
			headers[1] = "Title";	
			return result.getHTML(headers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Returns the number of resources that match the current query
	 *	@return	Integer, number of matching objects
	 */
	public Integer getMatchNr()
	{
		try
		{
			return new Integer(qb.getMatchNr());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Stores in the graph which properties in the query the user considers
	 *	optional.
	 *	@param	checks optional checkboxes ticked
	 *	@return	Integer with nr. of object in the archive that match the query
	 */
	public Integer sendOptionalInfo(Boolean[] checks)
	{
		List<QueryEdge> edges = qb.getPlannedEdgeList();
		if (edges.size() != checks.length)
			return null;
		
		for (int i = 0; i < checks.length; i++)
			edges.get(i).setOptional(checks[i].booleanValue());	
		return getMatchNr();
	}
	
	/**	Returns a class hierarchy with the number of instances of each class
	 *	(e.g. 'Person (34)').
	 *	@param	roots Root classes
	 *	@return Hierarchy[] containing class hierarchy
	 */
	public Hierarchy[] getCountedClassHierarchy(String[] roots)
	{
		Hierarchy[] result = reader.getClassHierarchy(roots);	//first get the basic hierarchy
		for (int i = 0; i < result.length; i++)
			findInstanceNr(result[i]);
		return result;
	}	
	
	private void findInstanceNr(Hierarchy h)
	{	//find the number of instances of this type
		try
		{
			String sparql = new OntologyWriter(reader).getInstanceNrQuery(h.getValue(), reader);
			int instances = sesame.queryBinding(sparql).size();
			h.addInstanceNr(Integer.toString(instances));
		}
		catch (SesameException e)
		{
			System.out.println(e.getMessage());
		}
		
		Hierarchy[] children = h.getSub();
		for (int i = 0; i < children.length; i++)	//then recurse
			findInstanceNr(children[i]);
	}
	
	//////////////////////	update methods	///////////////////////////////////////
	
	/**	Called when the users asks to show or hide all known information about a certain
	 *	object from the database.
	 *	@param	anchor int identifying anchor
	 *	@param	show boolean specifying whether the information should be shown or hidden
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] changeTextContent(String anchor, boolean show, int type, String key)
	{
		try
		{	
			FeedbackTextGenerator generator = getGenerator(type, key);
			boolean addedInfo = generator.changeTextContent(anchor, show);	
			return generator.getSurfaceText(addedInfo);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Called when the users asks to show all elements in a summation.
	 *	@param anchor	int identifying anchor
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] showSummation(String anchor, int type, String key)
	{
		try
		{
			FeedbackTextGenerator generator = getGenerator(type, key);
			generator.showSummation(anchor);	
			AnchorInfo[] text = generator.getSurfaceText();
			AnchorInfo[] temp = new AnchorInfo[text.length - 1];
			if (temp.length > 0)		//WHY IS THIS NECESSARY?? WHERE DOES THE JUNK ON THE END COME FROM????
				for (int i = 0; i < temp.length; i++)
					temp[i] = text[i];
			return temp;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Passes the information of what property, in what anchor, the user has selected,
	 *	and optionally what value he/she has added.
	 *
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param value	Value(s) added by user
	 *	@param type type of session
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] update(String anchor, String property, String value, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		generator.incrementLastOp();
		try
		{	
			generator.update(anchor, property, value);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Updates the SG with the newly added date. If it's the first date, it increments
	 *	the 'last operation' cntr; if it's the last, it updates and returns the feedback text.
	 *
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param date	String array with date value added by user
	 *	@param dateCntr Number of dates added so far
	 *	@param updateText True if this is the last value, and the feedback text should be updated
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text, or null if updateText was false or an error occurred
	 */
	public AnchorInfo[] updateDate(String anchor, String property, String[] date, int dateCntr, boolean updateText, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		if (dateCntr == 1)		//only increment lastOp the first time, as for the user this is one operation
			generator.incrementLastOp();
		
		try
		{	
			generator.updateDate(anchor, property, date);
			if (updateText)
				return generator.getSurfaceText();
			return null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Updates the SG with the newly added boolean property.
	 *
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param b	Boolean value specified by user
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] updateBoolean(String anchor, String property, boolean b, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		generator.incrementLastOp();
		try
		{	
			generator.booleanUpdate(anchor, property, b);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Updates the SG with the newly added number property.
	 *
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param nr Number value specified by user
	 *	@param type	type of session
	 *	@param key	browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] updateNumber(String anchor, String property, Number nr, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		generator.incrementLastOp();	
		try
		{	
			generator.numberUpdate(anchor, property, nr);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Updates multiple additions from a restricted range property
	 *
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param idx	String array with values selected by user
	 *	@param type	type of session
	 *	@param key	String with browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] multipleUpdate(String anchor, String property, String[] idx, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		generator.incrementLastOp();	
		try
		{
			List l = new ArrayList();
			for (int i = 0; i < idx.length; i++)
				if ((idx[i] != null) && (!idx[i].equals("")))
					l.add(idx[i]);
	
			generator.multipleUpdate(anchor, property, l);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Adds multiple values for one property in one go. Only for datatype properties in the Query tab.
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param values	Array of QueryDateValues selected by user
	 *	@param operator	boolean operator
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] updateDate(String anchor, String property, QueryDateValue[] values, String operator)
	{
		try
		{	
			int op = QueryGraph.getOperator(operator);
			qb.incrementLastOp();
			qb.updateDate(anchor, property, values, op);
			return qb.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				qb.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Adds multiple values for one property in one go. Only for datatype properties in the Query tab.
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param values	Array of String values selected by user
	 *	@param datatype	Type of data of the property
	 *	@param operator	boolean operator
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] multipleValuesUpdate(String anchor, String property, String[] values,  int datatype, String operator)
	{
		try
		{	
			qb.incrementLastOp();
			int op = QueryGraph.getOperator(operator);
			qb.multipleValuesUpdate(anchor, property, values, datatype, op);	
			return qb.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				qb.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Adds multiple values for one property in one go. Only for datatype properties in the editing and browsing
	 *	modules.
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param values	Array of String values selected by user
	 *	@param datatype	Type of data of the property
	 *	@param type	type of session
	 *	@param key	String with browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] multipleValuesUpdate(String anchor, String property, String[] values, int datatype, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		generator.incrementLastOp();	
		try
		{	
			generator.multipleValuesUpdate(anchor, property, values, datatype);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
		
	/**	Adds multiple values for one property in one go. Only for datatype properties in the editing and browsing
	 *	modules.
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param value	Value selected by user
	 *	@param type	type of session
	 *	@param key	String with browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] updateAbstract(String anchor, String property, String value, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		generator.incrementLastOp();	
		try
		{	
			generator.updateAbstract(anchor, property, value);
			return generator.getSurfaceText();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**	Adds an abstract property.
	 *	@param anchor	int identifying anchor
	 *	@param property	property name
	 *	@param range	Array of Instances created by user
	 *	@param type	type of session
	 *	@param key	String with browsing session key
	 *	@return AnchorInfo[] with serialisable version of feedback text
	 */
	public AnchorInfo[] updateObjectProperty(String anchor, String property, InstanceData[] range, int type, String key)
	{
		FeedbackTextGenerator generator = getGenerator(type, key);
		try
		{
			generator.incrementLastOp();		
			List<String> updated = new ArrayList<String>();	
			
			for (int i = 0; i < range.length; i++)
			{
				if (range[i].getID() != null)	//this object already exists
				{
					String id = range[i].getID();
					boolean check = false;
					for (SGNode node : rangeNodes)
					{	//try to find this node in range nodes
						if (id.equals(node.getUniqueID()))
						{
							check = true;
							updated.add(id);
							generator.updateObjectPropWithRangeObject(property, anchor, node, i);
							break;
						}
					}
					if (!check)
					{	//if not, try to find it in existing nodes
						for (SGNode node : newNodes)
						{
							if (id.equals(node.getUniqueID()))
							{
								check = true;
								generator.updateObjectPropWithExistingInstance(property, anchor, node, i);
								break;
							}
						}
					}
					if (!check)	//must have come from the archive then!
						generator.updateObjectPropWithArchiveInstance(property, anchor, id, i);
				}
				else	//must be new instance of a class, with form information
					generator.updateObjectPropWithNewInstance(property, anchor, range[i], i);
			}
			
			for (SGNode node : rangeNodes)
				if (!updated.contains(node.getUniqueID()))
					generator.removeObjectFromPropRange(property, anchor, node);
					
			getRangeInstances(property, anchor, type, key);		//update the range instances, in case someone presses 'back'
			return generator.getSurfaceText();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				generator.undo(true);		//undo the last operations (if any), as they gave an error
			}
			catch (UndoException ex)	//impossible here
			{
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/////////////////////	Getters and setters	///////////////////////////////
	
	/**	Returns the correct generator.
	 *	@param type	type of session
	 *	@param key	String with browsing session key
	 *	@return	FeedbackTextGenerator
	 */
	public FeedbackTextGenerator getGenerator(int type, String key)
	{
		if (type == EDIT)
			return ft;
		if (type == QUERY)
			return qb;
		if (browseMap.containsKey(key))
			return browseMap.get(key);
		if (type == BROWSE)	//if the user is editing in the browsing tab, he's using ft
			return ft;
		return null;
	}
	
	/**	Returns resource's URI
	 *	@return String with URI
	 *	@deprecated URI is not passed to this servlet anymore so this method will return null
	 */
	public String getURI()
	{
		return uri;
	}
	
	/**	Returns user ID
	 *	@return String with userID
	 */
	public String getUserID()
	{
		return userID;
	}
	
	/**	Updates the Semantic graph with information about the user.
	 *	@throws SesameException If an exception occurred while retrieving the data
	 */
	public void updateUserGraph() throws SesameException
	{
		AutomaticGenerator ag = new AutomaticGenerator(reader, sesame);
		userGraph = ag.getUserInfo(username, userID);
	}
	
	/**	Saves query results to a list
	 *	@param list List<String> with query results
	 */
	public void setQueryResults(List<String> list)
	{
		queryResults = list;
	}
	
	/**	Returns a query result from the QueryResult list
	 *	@param idx index of result in the list
	 *	@return String with query result
	 */
	public String getQueryResult(int idx)
	{
		if (idx >= queryResults.size())
			return null;
		return queryResults.get(idx);
	}
	
	/**	Returns all query results
	 *	@return List<String> with all query results
	 */
	public List<String> getQueryResults()
	{
		return queryResults;
	}
	
	/**	Returns the graph with user information
	 *	@return SemanticGraphTransformer
	 */
	public SemanticGraphTransformer getUserGraph()
	{
		return userGraph;
	}
	
	/**	Returns list of nodes used by method updateObjectProperty
	 *	@return List<SGNode>
	 */
	public List<SGNode> getNewNodes()
	{
		return newNodes;
	}
	
	/**	Sets list of nodes used by method updateObjectProperty
	 *	@param list List<SGNode>
	 */
	public void setNewNodes(List<SGNode> list)
	{
		newNodes = list;
	}
	
	/**	Returns list of nodes used by method updateObjectProperty
	 *	@return List<SGNode>
	 */
	public List<SGNode> getRangeNodes()
	{
		return rangeNodes;
	}
	
	/**	Sets list of nodes used by method updateObjectProperty
	 *	@param list List<SGNode>
	 */
	public void setRangeNodes(List<SGNode> list)
	{
		rangeNodes = list;
	}
	
	/**	Returns helpsession
	 *	@return HelpSession
	 */
	public HelpSession getHelpSession()
	{
		return help;
	}
	
	/**	Sets help session
	 *	@param h Help session
	 */
	public void setHelpSession(HelpSession h)
	{
		help = h;
	}
	
	/**	Returns information about user  (time, operations, etc)
	 *	@return UserInfo
	 * 	@deprecated Only used for usability evaluation
	 */
	public UserInfo getUserInfo()
	{
		return info;
	}
	
	/** Sets information about user's behaviour (time, operations, etc)
	 *	@param i UserInfo
	 * 	@deprecated Only used for usability evaluation
	 */
	public void setUserInfo(UserInfo i)
	{
		info = i;
	}
	
	/**	Returns userID and name
	 *	@return String[] with userID and name
	 */
	public String[] getUserDetails()
	{
		String[] result = new String[2];
		result[0] = userID;
		result[1] = username;
		return result;
	}
	
	/**	Returns username
	 *	@return String username
	 */
	public String getUser()
	{
		return username;
	}
	
	/**	Sets the feedback text generator
	 *	@param ft FeedbackTextGenerator
	 */
	public void setFeedbackTextGenerator(FeedbackTextGenerator ft)
	{
		setLastUpdated();
		this.ft = ft;
	}
	
	/**	Returns the FeedbackTextGenerator
	 *	@return FeedbackTextGenerator
	 */
	public FeedbackTextGenerator getFeedbackTextGenerator()
	{
		setLastUpdated();
		return ft;
	}
	
	/**	Returns the QueryBuilder
	 *	@return QueryBuilder
	 */
	public QueryBuilder getQueryBuilder()
	{
		setLastUpdated();
		return qb;
	}
	
	/**	Sets the QueryBuilder
	 *	@param qb QueryBuilder
	 */
	public void setQueryBuilder(QueryBuilder qb)
	{
		setLastUpdated();
		this.qb = qb;
	}
	
	/**	Adds a BrowsingGenerator
	 *	@param bg BrowsingGenerator
	 *	@return String with unique ID
	 */
	public String addBrowsingGenerator(BrowsingGenerator bg)
	{
		setLastUpdated();
		String key = UUID.randomUUID().toString();
		browseMap.put(key, bg);
		bg.setKey(key);
		return key;
	}
	
	/**	Returns a BrowsingGenerator
	 *	@param key Unique ID
	 *	@return BrowsingGenerator
	 */
	public BrowsingGenerator getBrowsingGenerator(String key)
	{
		return browseMap.get(key);
	}
	
	/**	Removes a BrowsingGenerator
	 *	@param key Unique ID
	 *	@return BrowsingGenerator
	 */
	public BrowsingGenerator removeBrowsingGenerator(String key)
	{
		if (browseMap.containsKey(key))
			return browseMap.remove(key);
		return null;
	}
	
	/**	Returns the time since the session was last updated (i.e. since the user
	 *	last did something)
	 *	@return long with time
	 */
	public long getLastUpdated()
	{
		return lastUpdated;
	}
	
	/**	Updates the session because user just did something
	 */
	public void setLastUpdated()
	{
		lastUpdated = new GregorianCalendar().getTimeInMillis();
	}
	
	/**	Returns the OntologyReader
	 *	@return OntologyReader
	 */
	public OntologyReader getReader()
	{
		return reader;
	}
}