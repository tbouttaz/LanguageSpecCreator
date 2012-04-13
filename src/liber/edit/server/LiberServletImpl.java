package liber.edit.server;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import liber.edit.client.AnchorInfo;
import liber.edit.client.ExistingInstances;
import liber.edit.client.FormInfo;
import liber.edit.client.Hierarchy;
import liber.edit.client.InstanceData;
import liber.edit.client.LiberServlet;
import liber.edit.client.QueryDateValue;
import liber.edit.client.TagCloud;
import WYSIWYM.model.SGDateNode;
import WYSIWYM.ontology.Folksonomy;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.util.LiberSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.hp.hpl.jena.ontology.OntProperty;

public class LiberServletImpl extends RemoteServiceServlet implements LiberServlet
{
	private Map<String, LiberSession> userMap = new HashMap<String,LiberSession>();
	private OntologyReader ontology;
	private TimerTask clean;
	
	/**	Constructor. Initialises the servlet, reading the ontology model from static files
	 *	and creating a cleaner thread that removes obsolete sessions.
	 */
	public LiberServletImpl()
	{
		System.out.println("Starting up");
		try
		{
			ontology = new OntologyReader();	//reads PolicyGrid ontologies
		//	ontology = new GeographyOntologyReader(); //reads geography ontology
			
			clean = new TimerTask()
			{
				public void run()
				{	//	Removes obsolete sessions from the map, running every 10 min.
					long time = new GregorianCalendar().getTimeInMillis();
					synchronized(userMap)
					{
						Iterator it = userMap.keySet().iterator();
						List<String> obsolete = new ArrayList<String>();
						while (it.hasNext())
						{
							String key = (String) it.next();
							long timeDif = time - userMap.get(key).getLastUpdated();
							if ((timeDif < 0) || (timeDif > 3600000))		//if an hour has lapsed, or the next day has started
								obsolete.add(key);							//(let's just hope no-one starts using the tool at midnight)
						}
						for (int i = 0; i < obsolete.size(); i++)	//then remove that session
							userMap.remove(obsolete.get(i));
					}
				}
			};
			Timer timer = new Timer();
			timer.schedule(clean, 0, 600000);
			
			Runtime r = Runtime.getRuntime();
			r.gc();			//FORCES GARBAGE COLLECTION
			r.runFinalization();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**	Stops the cleaning thread.
	 */
	public void finalize()
	{
		clean.cancel();
	}
	
	////////////	initialise and end sessions	/////////////////////////////////
	
	/**	Creates a new session for the editing tool.
	 *	@param user	user id
	 *	@param project	Project ID
	 *	@param resource	Resource ID
	 *	@return	String[] with user id and name
	 */
	public String[] newSession(String user, String project, String resource)
	{
		try
		{
			LiberSession session = new LiberSession(ontology, user, project, resource, LiberSession.EDIT);
			userMap.put(user, session);
			
			Runtime r = Runtime.getRuntime();
			r.gc();			//FORCES GARBAGE COLLECTION
			r.runFinalization();
			return session.getUserDetails();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Initialises a new editing session.
	 *
	 *	@param user	user id
	 *	@param data	Data provided about resource
	 *	@return	AnchorInfo[] containing the feedback text.
	 */
	public AnchorInfo[] initSession(String user, InstanceData data)
	{
		try
		{
			LiberSession session = userMap.get(user);
			
			Runtime r = Runtime.getRuntime();
			r.gc();			//FORCES GARBAGE COLLECTION
			r.runFinalization();
			
			synchronized (session)
			{
				session.setLastUpdated();
				return session.init(data);	//initialise the session and return the text
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new AnchorInfo[0];
		}
	}
	
	/**	Initialises a new browsing session, creating a new session for this user.
	 *
	 *	@param user	user id
	 *	@param resource	Resource id
	 *	@param type	Type of session (edit, query, browse)
	 *	@return	AnchorInfo[] containing the feedback text.
	 */
	public AnchorInfo[] initSession(String user, String resource, int type)
	{
		try
		{
			if (type == LiberSession.BROWSE)	//make a new user session if this is the browse tab
				userMap.put(user, new LiberSession(ontology, user, null, resource.replaceAll("\"", ""), type));
			else if (!userMap.containsKey(user))
				return null;
		
			LiberSession session = userMap.get(user);
			Runtime r = Runtime.getRuntime();
			r.gc();			//FORCES GARBAGE COLLECTION
			r.runFinalization();
			
			synchronized (session)
			{
				session.setLastUpdated();
				return session.init(resource.replaceAll("\"", ""));	//initialise the session and return the text
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new AnchorInfo[0];
		}
	}
	
	/**	Initialises a new query session, and returns the class hierarchy. This 
	 *	hierarchy is special because it includes the number of instances of each
	 *	type.
	 *
	 *	@param user	user id
	 *	@param roots root classes of the hierarchy (null to get complete hierarchy)
	 *	@return Hierarchy[] containing class hierarchy
	 */
	public Hierarchy[] initSessionAndGetClassHierarchy(String user, String[] roots)
	{
		try
		{
			if (userMap.containsKey(user))
				userMap.get(user).setLastUpdated();
			else
				userMap.put(user, new LiberSession(ontology, user, null, null, LiberSession.QUERY));
			
			LiberSession session = userMap.get(user);
			
			Runtime r = Runtime.getRuntime();
			r.gc();			//FORCES GARBAGE COLLECTION
			r.runFinalization();
			
			synchronized (session)
			{
				return session.getCountedClassHierarchy(roots);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Prints the number of queries the user has produced, and how long each 
	 *	took.
	 *	@param user User ID
	 */
	public void printQueryTimes(String user)
	{
		if (userMap.containsKey(user))
			userMap.get(user).printQueryTimes();
	}
	
	/**	Ends this particular session. If it's a browsing session, the browsing
	 *	generator is removed; otherwise, the entire user's session can be removed.
	 *	@param user	user id
	 *	@param type type of session
	 *	@param key	browsing session id (null if session type is EDIT)
	 */
	public void endSession(String user, int type, String key)
	{
		if (!userMap.containsKey(user))
			return;
		
		if (type == LiberSession.BROWSE)
		{
			LiberSession session = userMap.get(user);
			synchronized (session)
			{
				session.removeBrowsingGenerator(key);
			}
		}
		else
		{
			if (type == LiberSession.QUERY)
				userMap.get(user).printQueryTimes();
			userMap.remove(user);
		}
		
		Runtime r = Runtime.getRuntime();
		r.gc();			//FORCES GARBAGE COLLECTION
		r.runFinalization();		
	}

	/**	Returns an RDF representation of the feedback text, which is also
	 *	stored in Sesame.
	 *
	 *	@param user	user id
	 *	@param type	session type
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	String RDF, or null if the user's session is expired
	 */
	public String upload(String user, int type, String key)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			return session.getRDF(type, key);
		}
	}
	
	/**	Creates a SPARQL representation of the query, sends it to Sesame, and
	 *	returns descriptions of any matching resources.
	 *	@param user User ID
	 *	@param	time Time it took the user to construct the query
	 *	@return AnchorInfo[][], feedback texts describing the search results,
	 *	or null if the user's session is expired
	 */
	public AnchorInfo[][] getSPARQL(String user, long time)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			return session.getSPARQL(time);
		}
	}	
	
	/**	Creates a SPARQL representation of the query, sends it to Sesame, and
	 *	returns a description of all matching resources.
	 *
	 *	@param user	user id	
	 *	@param	time Time it took the user to construct the query
	 *	@return	description of matches, or null if the user's session is expired
	 */
	public AnchorInfo[] getQueryResult(String user, long time)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			return session.getQueryResult(time);
		}
	}
	
	////////////	get feedback texts	//////////////////////////////////
	
	/**	Returns the feedback text
	 *	@param user	user id
	 *	@param type	type of session
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] getFeedbackText(String user, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized (session)
		{
			session.setLastUpdated();
			return session.getFeedbackText(type, key);
		}
	}
	
	/**	Returns a feedback text with all information about the object with the given id.
	 *	@param user User id
	 *	@param id object id
	 *	@return AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] getBrowsingDescription(String user, String id)
	{	
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized (session)
		{
			session.setLastUpdated();
			return session.getBrowsingDescription(id);
		}
	}	
	
	/**	Retrieves the SemanticGraph of the query result the user is interested in,
	 *	and returns a feedback text with all information about that object
	 *
	 *	@param user user ID
	 *	@param idx Index
	 *	@return AnchorInfo[], serialisable version of feedback text, 
	 *	empty if the user's session is expired, null if an error occurred
	 */
	public AnchorInfo[] getBrowsingDescription(String user, int idx)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized (session)
		{
			session.setLastUpdated();
			return session.getBrowsingDescription(idx);
		}
	}
	

	/////////////////	METHODS FOR GETTING INFORMATION	////////////////////////
	/**	Returns a tree representation of the subclasses of the given nodes, 
	 *	with the nodes in alphabetic order.
	 *
	 *	@param user User id
	 *	@param roots Root nodes
	 *	@return	Hierarchy[] tree representation of class hierarchy, null if session expired
	 */
	public Hierarchy[] getClassHierarchy(String user, String[] roots)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.getReader().getClassHierarchy(roots);
		}
	}	
	
	/**	Returns a tree representation of the classes in the range of the given 
	 *	property, with the nodes in alphabetic order.
	 *
	 *	@param user User id
	 *	@param prop Property name
	 *	@return	Hierarchy[] tree representation of range class hierarchy, empty if session expired
	 */
	public Hierarchy[] getRangeHierarchy(String user, String prop)
	{
		if (!userMap.containsKey(user))
			return new Hierarchy[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			if (prop.equals("ANYTHING"))
				return session.getReader().getClassHierarchy();
			return session.getReader().getRangeHierarchy(prop);
		}
	}
	
	/**	Retrieves existing instances that qualify to be a target of this property, and
	 *	instances that already are a target of the property and anchor! Returns an array
	 *	with first the range class name, then those objects already in the range, 
	 *	then a 'null', and then all eligible objects.
	 *
	 *	@param user user id
	 *	@param name property name
	 *	@param anchor Unique ID of Anchor
	 *	@param type	session type
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	ExistingInstances, holds all necessary information about object
	 *	that already are or could be in the range, empty if session expired, or null if error occurred.
	 */
	public ExistingInstances getInstances(String user, String name, String anchor, int type, String key)
	{
		try
		{
			if (!userMap.containsKey(user))
			{
				ExistingInstances result = new ExistingInstances();
				result.setRange(null, null);
				return result;
			}

			LiberSession session = userMap.get(user);
			synchronized (session)
			{
				session.setLastUpdated();
				ExistingInstances e = session.getRangeInstances(name, anchor, type, key);
				return e;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**	Returns the type of the property: object- or datatype, and whether it has
	 *	restricted values, and any cardinality constraints it might have.
	 *	The first element in the array returns the range type, the second a maximum
	 *	cardinality constraint (0 if there is none)
	 *	
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param type Session type
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	Integer[] with property type. 
	 */
	public Integer[] getType(String user, String anchor, String property, int type, String key)
	{
		if (!userMap.containsKey(user))
			return null;

		LiberSession session = userMap.get(user);
		Integer[] result = new Integer[2];
		synchronized (session)
		{
			session.setLastUpdated();
			result[0] = session.getReader().getRangeType(property);
			result[1] = new Integer(session.getMax(anchor, property, type, key));
		}
		
		return result;
	}
	
	/** Returns the values allowed in the range of the given property.
	 *	@param user	User ID
	 *	@param property	Property name
	 *	@return String[] with possible range values, null if session expired
	 */
	public String[] getRange(String user, String property)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);		
		synchronized (session)
		{
			session.setLastUpdated();
			return session.getReader().getRestrictedRange(property).toArray(new String[0]);
		}
	}
	
	/**	Returns all string datatype properties with cardinal constraints of this
	 *	class.
	 *	@param user	User ID
	 *	@param type 	Class name
	 *	@return	FormInfo[] with information needed to create a small form for new instance creation,
	 *	null if error occurred or session expired
	 */
	public FormInfo[] getCardinalStringProperties(String user, String type)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);		
		synchronized (session)
		{
			session.setLastUpdated();
			try
			{
				return session.getReader().getCardinalStringProperties(type);
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}
	
	/**	Retrieves the values already added for this property and anchor
	 *
	 *	@param user	User ID
	 *	@param property Property name
	 *	@param anchor Unique ID of Anchor
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	String[] with values already added for this property and anchor, 
	 *	first element --EXPIRED-- if session expired, null if error occurred
	 */
	public String[] getAddedValues(String user, String property, String anchor, int type, String key)
	{
		if (!userMap.containsKey(user))
		{
			String[] result = new String[1];
			result[0] = "---EXPIRED---";
			return result;
		}

		try
		{
			LiberSession session = userMap.get(user);		
			synchronized (session)
			{
				session.setLastUpdated();
				return session.getAddedValues(property, anchor, type, key);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Returns an NL-expression of the given date.
	 *	@param	user User ID
	 *	@param input QueryDateValue containing restriction on date
	 *	@return String with nl-expression for date restriction (e.g. 'before 2007'), 
	 *	empty String if session expired or null if error occurred
	 */
	public String getDateExpression(String user, QueryDateValue input)
	{
		if (!userMap.containsKey(user))
			return "";
		try
		{		
			return new SGDateNode(input.getDates()).getQueryNLExpression(input.getComparator());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Returns a tag cloud for the given property.
	 *	@param user User ID
	 *	@param property Property name
	 *	@return TagCloud, null if session expired or error occurred
	 */
	public TagCloud getTagCloud(String user, String property)
	{
		try
		{
			if (!userMap.containsKey(user))
				return null;
		
			LiberSession session = userMap.get(user);
			synchronized (session)
			{
				session.setLastUpdated();
				TagCloud tc = new Folksonomy(user).getTagCloud(session.getReader(), session.getReader().getProperty(property));
				return tc;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Returns the values of resourceID for the properties specified in 
	 *	formInformation.
	 *
	 *	@param	user User ID
	 *	@param	resourceID Resource ID
	 *	@param	formInformation FormInfo[] specifying properties
	 *	@return	String[] with values, null if session expired or error occurred
	 */
	public String[] getDescriptionValues(String user, String resourceID, FormInfo[] formInformation)
	{
		try
		{
			if (!userMap.containsKey(user))
				return null;
		
			LiberSession session = userMap.get(user);
			synchronized (session)
			{
				session.setLastUpdated();
				return session.getDescriptionValues(resourceID, formInformation);				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**	Returns an aggregated tag cloud for a set of properties.
	 *	@param user User ID
	 *	@param properties String[] with property names
	 *	@return TagCloud, null if session expired or error occurred
	 */
	public TagCloud getTagCloud(String user, String[] properties)
	{
		try
		{
			if (!userMap.containsKey(user))
				return null;

			LiberSession session = userMap.get(user);
			synchronized (session)
			{
				session.setLastUpdated();
				List<OntProperty> list = new ArrayList<OntProperty>();
				for (int i = 0; i < properties.length; i++)
					list.add(session.getReader().getProperty(properties[i]));
				TagCloud tc = new Folksonomy(user).getTagCloud(session.getReader(), list);
				return tc;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/////////////////	UNDO/REDO/RESET	//////////////////////////////////////
	/**	Undoes the last information-addition
	 *	@param user User ID
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] undo(String user, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.undo(type, key);
		}
	}
	
	/**	Removes edges from the Semantic Graph
	 *	@param user User ID
	 *	@param	anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values Values added by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] removeProperty(String user, String anchor, String property, String[] values, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.removeProperty(anchor, property, values, type, key);
		}
	}
	
	/**	Removes a node from the Semantic Graph
	 *	@param user User ID
	 *	@param	anchor Unique ID of Anchor
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] removeAnchor(String user, String anchor, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.removeAnchor(anchor, type, key);
		}
	}
	
	/**	Adds the information that was removed last back in the graph
	 *	@param user User ID
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] redo(String user, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.redo(type, key);
		}
	}
	
	/////////////////	UPDATE METHODS	//////////////////////////////////////
	
	/**	Called when the users asks to show or hide all known information about a certain
	 *	object from the database.
	 *	@param user User ID
	 *	@param	anchor Unique ID of Anchor
	 *	@param show If true, show more information, if false, hide information
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] changeTextContent(String user, String anchor, boolean show, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		
		Runtime r = Runtime.getRuntime();
		r.gc();			//FORCES GARBAGE COLLECTION
		r.runFinalization();
		
		synchronized(session)
		{
			session.setLastUpdated();
			return session.changeTextContent(anchor, show, type, key);
		}
	}
	
	/**	Called when the users asks to show all elements in a summation.
	 *	@param user User ID
	 *	@param	anchor Unique ID of Anchor
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] showSummation(String user, String anchor, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);	
		synchronized(session)
		{
			session.setLastUpdated();
			return session.showSummation(anchor, type, key);
		}
	}
	
	/**	Passes the information of what property, in what anchor, the user has selected,
	 *	and optionally what value he/she has added.
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param value Value added by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] update(String user, String anchor, String property, String value, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
			
		LiberSession session = userMap.get(user);
		synchronized (session)
		{
			session.setLastUpdated();
			return session.update(anchor, property, value, type, key);
		}	
	}
	
	/**	Updates the SG with the newly added date. If it's the first date, it increments
	 *	the 'last operation' cntr; if it's the last, it updates and returns the feedback text.
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param date Date value added by user
	 *	@param dateCntr Index number of date
	 *	@param updateText If true, the feedbacktext is regenerated and returned
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if updateText was false or an error occurred
	 */
	public AnchorInfo[] updateDate(String user, String anchor, String property, String[] date, int dateCntr, boolean updateText, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
			
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.updateDate(anchor, property, date, dateCntr, updateText, type, key);
		}
	}
	
	/**	Updates the SG with the newly added boolean property.
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param b Boolean value added by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] updateBoolean(String user, String anchor, String property, boolean b, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.updateBoolean(anchor, property, b, type, key);
		}	
	}
	
	/**	Updates the SG with the newly added number property.
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param nr Number value added by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] updateNumber(String user, String anchor, String property, Number nr, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.updateNumber(anchor, property, nr, type, key);
		}	
	}
	
	/**	Updates multiple additions from a restricted range property
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param idx Values selected by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] multipleUpdate(String user, String anchor, String property, String[] idx, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.multipleUpdate(anchor, property, idx, type, key);
		}
	}
	
	/**	Adds multiple values for one property in one go. Only for datatype properties in the Query tab.
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values Values added by user
	 *	@param operator Boolean operator (and, or, not)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] updateDate(String user, String anchor, String property, QueryDateValue[] values, String operator)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.updateDate(anchor, property, values, operator);
		}
	}	
	
	/**	Adds multiple values for one property in one go. Only for datatype properties in the Query tab.
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values Values added by user
	 *	@param datatype Datatype of property. 1 for a datatype property with restricted values;
	 *	2 for a date; 3 for a double or float; 4 for an integer; 5 for a boolean;
	 *	6 for a string; and 7 for another object.
	 *	@param operator Boolean operator (and, or, not)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] multipleValuesUpdate(String user, String anchor, String property, String[] values,  int datatype, String operator)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.multipleValuesUpdate(anchor, property, values, datatype, operator);
		}
	}
	
	
	/**	Adds multiple values for one property in one go. Only for datatype properties in the editing and browsing
	 *	modules.
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param values Values added by user
	 *	@param datatype Datatype of property. 1 for a datatype property with restricted values;
	 *	2 for a date; 3 for a double or float; 4 for an integer; 5 for a boolean;
	 *	6 for a string; and 7 for another object.
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] multipleValuesUpdate(String user, String anchor, String property, String[] values, int datatype, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.multipleValuesUpdate(anchor, property, values, datatype, type, key);
		}
	}
	
	/**	Adds an abstract property.
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param value Value added by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] updateAbstract(String user, String anchor, String property, String value, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.updateAbstract(anchor, property, value, type, key);
		}
	}
	
	/**	Adds one or more object properties.
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param range InstanceData[] specifying data about objects added by user
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	AnchorInfo[] with serialisable version of feedback text, 
	 *	empty if the user's session is expired or null if an error occurred
	 */
	public AnchorInfo[] updateObjectProperty(String user, String anchor, String property, InstanceData[] range, int type, String key)
	{
		if (!userMap.containsKey(user))
			return new AnchorInfo[0];
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.updateObjectProperty(anchor, property, range, type, key);
		}
	}
	
	//////////////////	other	//////////////////////////////////////////////

	/**	Tries to export an object in a browsing pane to the editing pane.
	 *	Returns null if the user's session is expired, false if the operation fails
	 *	and true if it succeeds. Used by previous version of LIBER.
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@return	true if it succeeded
	 *	@deprecated
	 */
	public Boolean exportObject(String user, String anchor, String key)
	{
		if (!userMap.containsKey(user))	//if this user already has a session, do nothing
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.exportObject(anchor, key);
		}
	}
	
	/**	Returns the number of resources that match the current query
	 *	@param user User ID
	 *	@return Integer
	 */
	public Integer getMatchNr(String user)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.getMatchNr();
		}
	}
		
	/**	Stores in the graph which properties in the query the user considers
	 *	optional.
	 *	@param	user User ID
	 *	@param checks	Boolean[], specifies whether optional checkboxes are checked
	 *	@return	Number of objects in the archive that match the query
	 */
	public Integer sendOptionalInfo(String user, Boolean[] checks)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.sendOptionalInfo(checks);
		}	
	}
	
	/**	Stores in the graph which properties in the query the user considers
	 *	optional.
	 *	@param	user User ID
	 *	@return	Boolean[], specifies whether optional checkboxes are checked
	 */
	public Boolean[] getCheckedOptionals(String user)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.getQueryBuilder().getCheckedOptionals();
		}		
	}
	
	/**	Finds all objects in the archive of one of the given types and with	
	 *	the given name/title.
	 *
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@param title	Name/title of object
	 *	@param types	Possible types of object
	 *	@return String[] with brief descriptions of search results
	 */
	public String[] checkDatabase(String user, String anchor, String property, int type, String key, String title, String[] types)
	{
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.checkDatabase(anchor, property, title, types, type, key);
		}
	}	
	
	/**	Finds all objects in the archive of one of the given types and with	
	 *	the given name/title.
	 *	@param user User ID
	 *	@param anchor Unique ID of Anchor
	 *	@param property Property name
	 *	@param type Session type (edit, query, browse)
	 *	@param key	browsing session id (null if session type is EDIT)
	 *	@param data	Information about object added by user in form
	 *	@return String[] with brief descriptions of search results
	 */
	public String[] checkDatabase(String user, String anchor, String property, int type, String key, InstanceData data)
	{	
		if (!userMap.containsKey(user))
			return null;
		
		LiberSession session = userMap.get(user);
		synchronized(session)
		{
			session.setLastUpdated();
			return session.checkDatabase(anchor, property, data, type, key);
		}
	}
}