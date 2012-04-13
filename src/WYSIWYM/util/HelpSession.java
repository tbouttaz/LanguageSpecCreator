package WYSIWYM.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

/**	This class contains the instances of all help sessions of one user
 *	
 *	@author Feikje Hielkema
 *	@version 1.0
 */
public class HelpSession
{
	private Map<String, Session> map = new HashMap<String, Session>();
	
	private class Session
	{
		private List<List<OntClass>> classHistory = new ArrayList<List<OntClass>>();
		private List<List<OntProperty>> propHistory = new ArrayList<List<OntProperty>>();
		
		public OntClass getClass(int backtrack, int idx) throws HelpException
		{	//check that it really is a class you should be getting, not a property (this can happen sometimes)s
			if (classHistory.get(classHistory.size() - (backtrack + 1)).size() == 0)
				throw new HelpException("Error when trying to get a class; should get property instead.");
		
			for (int i = 0; i < backtrack; i++)
			{
				propHistory.remove(propHistory.size() - 1);
				classHistory.remove(classHistory.size() - 1);
			}
			return classHistory.get(classHistory.size() - 1).get(idx);			
		}
		
		public OntProperty getProperty(int backtrack, int idx)
		{
			for (int i = 0; i < backtrack; i++)
			{
				classHistory.remove(classHistory.size() - 1);
				propHistory.remove(propHistory.size() - 1);
			}
			return propHistory.get(propHistory.size() - 1).get(idx);			
		}
		
		public void addClassResult(List<OntClass> result)
		{
			classHistory.add(result);
		}
		
		public void addPropResult(List<OntProperty> result)
		{
			propHistory.add(result);
		}
	}
	
	/**	Retrieves an OntClass from one of the lists in this helpsession.
	 *
	 *	@param session The help session
	 *	@param backtrack  Number of steps user has gone back (if any)
	 *	@param idx Index of class
	 *	@return OntClass
	 */
	public OntClass getClass(String session, int backtrack, int idx) throws HelpException
	{
		Session s = map.get(session);
		return s.getClass(backtrack, idx);
	}
	
	/** 	Retrieves an OntProperty from one of the lists in this helpsession.
	 *
	 *	@param session The help session
	 *	@param backtrack  Number of steps user has gone back
	 *	@param idx Index of class
	 *	@return OntProperty
	 */
	public OntProperty getProperty(String session, int backtrack, int idx)
	{
		Session s = map.get(session);
		return s.getProperty(backtrack, idx);
	}
	
	/** 	Adds a new search result to the session
	 *
	 *	@param session The help session
	 *	@param classes List of OntClasses
	 *	@param props List of OntProperties
	 */
	public void addResult(String session, List<OntClass> classes, List<OntProperty> props)
	{
		if (!map.containsKey(session))
			return;

		Session s = map.get(session);
		if (classes == null)
			s.addClassResult(new ArrayList<OntClass>());
		else
			s.addClassResult(classes);
			
		if (props == null)
			s.addPropResult(new ArrayList<OntProperty>());
		else
			s.addPropResult(props);
	}
	
	/**	Starts a new session
	 *	@return String with unique id
	 */
	public String newSession()
	{
		int k = map.size();
		while (map.containsKey(new Integer(k).toString()))
			k++;
		String key = new Integer(k).toString();	
		map.put(key, new Session());
		return key;
	}
	
	/**	Ends a session
	 *	@param session Session's unique id
	 */
	public void endSession(String session)
	{
		if (map.containsKey(session))
			map.remove(session);
	}
}