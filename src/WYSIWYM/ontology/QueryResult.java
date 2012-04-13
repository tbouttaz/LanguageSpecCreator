package WYSIWYM.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.query.BindingSet;

import WYSIWYM.model.Anchor;
import WYSIWYM.model.Edge;
import WYSIWYM.model.SGNode;
import WYSIWYM.transformer.FeedbackTextGenerator;

import com.hp.hpl.jena.ontology.OntClass;

/**	QueryResult stores the bindings of a SPARQL query in a better sorted way
 *	than the BindingSets that Sesame returns. This is simply a helper method
 *	to make sense of Sesame's RDF retrieval. A QueryResult can be created from
 *	a TupleQueryResult by passing the query's variables to the constructor, and adding
 *	the binding sets one-by-one using add(BindingSet).
 *	Results are stored by their Sesame ID. Either your SPARQL query should request 
 *	the Sesame ID as the first variable, or you should specify each BindingSet's 
 *	Sesame ID yourself using add(BindingSet, String id).
 *
 *	@author Feikje Hielkema
 *	@version 1.2 12-03-2008
 *
 *	@version 1.3 22-03-2008
 */
 public class QueryResult
 {
 	private List<String> variables;
 	private Map<String, Binding> resultMap = new HashMap<String, Binding>();
 	
 	/**	Creates a new QueryResult from the given set of variables
 	 *	(you can get these from TupleQueryResult.getBindingNames())
 	 *	@param vars Variables
 	 */
 	public QueryResult(List<String> vars)
 	{
 		variables = vars;
 	}

	/**	Checks whether the given Sesame ID is present in this QueryResult
	 *	@param id Sesame ID
	 *	@return true if this result contains id
	 */
	public boolean containsKey(String id)
	{
		return resultMap.containsKey(id);
	}
	
	/**	Returns a HashMap with properties as keys,
	 *	and a list of their bindings for the given Sesame ID as values.
	 *	@param id Sesame ID
	 *	@return Map<String, List<String>>
	 */
	public Map<String, List<String>> getBindings(String id)
	{
		if (resultMap.containsKey(id))
			return resultMap.get(id).getBindings();
		return null;
	}
 	
 	/**	Returns a list of the given variable's bindings for the 
 	 *	given Sesame ID.
	 *	@param id Sesame ID
	 *	@param prop Variable
	 *	@return List<String>
	 */
 	public List<String> getBindingValues(String id, String prop)
 	{
 		Map<String, List<String>> map = getBindings(id);
 		if ((map != null) && (map.containsKey(prop)))
 			return map.get(prop);
 		return null;
 	}
 	
 	/**	Returns the values for the given variables as an array; 
 	 *	if a property has >1 value, they are concatenated with komma's.
 	 *
 	 *	@param id Sesame ID
 	 *	@param properties List<String> with variable names
 	 *	@return String[] with (concatenated) values.
 	 */
 	public String[] getValuesArray(String id, List<String> properties)
 	{
 		String[] values = new String[properties.size()];
 		Map<String, List<String>> map = getBindings(id);
 		int cntr = 0;
 		for (String prop : properties)
 		{
 			StringBuffer sb = new StringBuffer();
 			if (map.containsKey(prop))
 			{
 				List<String> list = map.get(prop);
 				for (int i = 0; i < list.size(); i++)
 				{
 					sb.append(list.get(i));
 					if ((i + 2) < list.size())
 						sb.append(", ");
 					else if ((i + 1) < list.size())
 						sb.append(" and ");
 				}
 			}
 			values[cntr++] = sb.toString();	//this way no value is ever null (though it might be an empty string)
 		}
 		return values;
 	}
 	
 	/**	Returns the number of bindings that have been found
 	 *	@return size
 	 */
 	public int size()
 	{
 		return resultMap.size();
 	}
 	
 	/**	Returns an Iterator over the Sesame ID's of all search results.
 	 *	@return Iterator
 	 */
 	public Iterator getIDs()
 	{
 		return resultMap.keySet().iterator();
 	}
 	
 	/**	Returns the list with variables
 	 *	@return List<String> with variable names
 	 */
 	public List<String> getVariables()
 	{
 		return variables;
 	}
 	
 	/**	Adds a new BindingSet; if the Sesame ID (which must be the first binding!) 
 	 *	matches that of one	of the Bindings in resultMap, the (new) values are added to it.
 	 *
 	 *	@param set BindingSet
 	 */
 	public void add(BindingSet set)
 	{
 		if (variables.size() == 0)
 			return;
 		
 		String var = variables.get(0);
 		String id = getValue(set.getBinding(var).toString(), var);
 		if (resultMap.containsKey(id))	//this instance is already in the result
 			resultMap.get(id).add(set, variables);
 		else
 			resultMap.put(id, new Binding(set, variables));
 	}
 	
 	/**	Adds a new BindingSet with the given id. If this id is already in resultMap,
 	 *	the values of the set are added to that Binding.
 	 *
 	 *	@param set BindingSet
 	 *	@param id Sesame ID
 	 */
 	public void add(BindingSet set, String id)
 	{
		if (id == null)
		{
			add(set);
			return;
		}
 		if (variables.size() == 0)
 			return;

 		if (resultMap.containsKey(id))	//this instance is already in the result
 			resultMap.get(id).add(set, variables, id);
 		else
 			resultMap.put(id, new Binding(set, variables, id));
 	}
 	
 	/**	Removes the binding with the given Sesame ID.
 	 *	@param id Sesame ID	
 	 *	@return false if there was no result with this ID, otherwise true
 	 */
 	public boolean remove(String id)
 	{
 		if (!resultMap.containsKey(id))
 			return false;
 		resultMap.remove(id);
 		return true;
 	}
 	
 	/**	Removes all values but the one at the given index from all the given
 	 *	variable lists.
 	 *	@param id Sesame ID
 	 *	@param idx Value index
 	 *	@param vars One or more variables
 	 *	@return false if there was no binding for the given Sesame ID, otherwise true.
 	 */
 	public boolean removeAllButOne(String id, int idx, String... vars)
 	{
 		if (!resultMap.containsKey(id))
 			return false;
 		
 		Map<String, List<String>> bindings = resultMap.get(id).getBindings();	
 		for (String var: vars)
 		{
 			if (!bindings.containsKey(var))
 				continue;
 			
 			List<String> values = bindings.get(var);
 			List<String> replace = new ArrayList<String>();
 			replace.add(values.get(idx));
 			bindings.put(var, replace);
 		}
 		return true;
 	}
 	
 	/**	Removes the bindings of the given variable from the BindingSet with
 	 *	the given ID.
 	 *	@param id Sesame ID
 	 *	@param var Variable
 	 *	@return false if there was no binding for the given Sesame ID, otherwise true.
 	 */
 	public boolean removeVariable(String id, String var)
 	{
 		if (!resultMap.containsKey(id))
 			return false;
 		
 		Map<String, List<String>> bindings = resultMap.get(id).getBindings();	
 		if (bindings.remove(var) == null)
 			return false;
 		return true;
 	}
 	
 	/**	Separates the actual value from the variable name
 	 *
 	 *	@param value String value in BindingSet
 	 *	@param var Variable
 	 *	@return String value without variable
 	 */
 	public static String getValue(String value, String var)
 	{
 		if (value == null)
 			return null;
 		
 		int idx = value.indexOf("\"");
 		if (idx < 0)
	 		return value.substring(var.length() + 1);
	 	return value.substring(idx + 1, value.length() - 1);
 	}
 	
	/**	Returns a literal value, without the RDF type 
	 *	(i.e. remove the "^^rdf:integer..."
 	 *
 	 *	@param value String value in BindingSet
 	 *	@return String value without RDF type
 	 */ 	
 	 public static String getLiteralValue(String value)
 	{
 		int idx = value.indexOf("^^");
 		if (idx < 0)
 			return value;
 		return value.substring(1, idx - 1);
 	}
 	
 	/**	Prints the results to System.out; useful for debugging.
 	 */
 	public void print()
 	{
 		for (Iterator it = resultMap.values().iterator(); it.hasNext(); )
 		{
 			Binding b = (Binding) it.next();
 			b.print();
 		}
 	}
 	
 	/**	Returns a HashMap with descriptions of each search result, without html-tags. 
 	 *	The description does include occurences of 'LINEBREAK' which signal where
 	 *	a linebreak should be inserted. This method is used by ourSpaces.
 	 *	'head' determines which variable should be presented first (e.g. 'name').
 	 *	@param head First variable
 	 *	@return Map<String,String> with a description for each search result.
 	 */
 	public Map<String,String> getNoLayout(String head)
 	{
 		Map<String,String> result = new HashMap<String,String>();
 		for (Iterator it = resultMap.values().iterator(); it.hasNext(); )
 		{
 			Binding b = (Binding) it.next();
 			String id = b.getID();
 			String description = b.getNoLayout(variables, head);
 			result.put(id, description);
 		}
 		return result;
 	}
 	
 	/**	Creates HTML descriptions of the search results. This method is used by LIBER.
 	 * 	'head' determines which variable should be presented first (e.g. 'name').
 	 *	@param head First variable
 	 *	@return String[] with resource descriptions.
 	 */
 	public String[] getHTML(String head)
 	{
 		String[] array = new String[1];
 		array[0] = head;
 		return getHTML(array);
 	}
 	
 	/**	Creates HTML descriptions of the search results. This method is used by LIBER.
 	 * 	'head' determines which variables should be presented first (e.g. 'name', 'email').
 	 *	@param head First variables
 	 *	@return String[] with resource descriptions.
 	 */	
 	public String[] getHTML(String[] head)
 	{
 		String[] result = new String[resultMap.size() * 2];
 		int idx = 0;
 		for (Iterator it = resultMap.values().iterator(); it.hasNext(); )
 		{
 			Binding b = (Binding) it.next();
 			result[idx] = b.getID();
 			idx++;
 			result[idx] = b.getHTML(variables, head);
 			idx++;
 		}
 		return result;
 	}
 	
 	/**	Returns the NL-information about the given Sesame ID. The second parameter
 	 *	is a list of properties that hold NL-information. This method is used by LIBER
 	 *	to quickly extract relevant information about objects.
 	 *	It could be used with any list of properties, not just for NL-information!
 	 *	@param id Sesame ID
 	 *	@param nlproperties List of property names that will be included in the description if they are in the BindingSet.
 	 *	@return String[] with resource descriptions.
 	 */
 	public String getNLValue(String id, List<String> nlproperties)
 	{
 		if (!resultMap.containsKey(id))
 			return null;
 		Binding b = resultMap.get(id);
 		return b.getNLValue(nlproperties);
 	}
 	
 	/**	Removes from the query result all bindings that are already in the semantic graph.
 	 *	Also ensures that every binding only specifies its most specific class type.
 	 *	@param reader Ontology
 	 *	@param ft FeedbackTextGenerator with SemanticGraph	
 	 *	@param anchor Anchor's unique ID
 	 *	@param property Property name
 	 */
 	public void clean(OntologyReader reader, FeedbackTextGenerator ft, String anchor, String property)
 	{
 		Anchor a = ft.getText().getAnchor(anchor);
		String id = a.getNode().getID();
		SGNode node = (SGNode) ft.getGraph().getNode(id);
 		
 		List<Edge> list = node.getOutgoingEdges(property);	//get all existing edges			
 		for (int i = 0; i < list.size(); i++)			//of this property,
 		{									//and remove these values from the query result
 			String key = ((SGNode) list.get(i).getTarget()).getUniqueID();
 			if (resultMap.containsKey(key))
	 			resultMap.remove(key);
	 	}
	 	String inverse = reader.getInverse(property);	
 		if (inverse != null)					//do the same for the incoming edges
 		{
 			list.addAll(node.getIncomingEdges(inverse));	//of the inverse property
 			for (int i = 0; i < list.size(); i++)			//of this property,
 			{									//and remove these values from the query result
 				String key = ((SGNode) list.get(i).getSource()).getUniqueID();
 				if (resultMap.containsKey(key))
	 				resultMap.remove(key);
	 		}
	 	}

 		setMostSpecificType(reader);	
 	}
 	
 	/**	Ensures every binding only specifies its most specific class type
 	 *	@param reader Ontology
 	 */
 	public void setMostSpecificType(OntologyReader reader)
 	{
 		if (!variables.contains("Type"))
 			return;
 		for (Iterator<Binding> it = resultMap.values().iterator(); it.hasNext(); )
 			it.next().setMostSpecificType(reader);
 	}
 	
 	/**	Helper class, stores the values found for a particular instance (defined by 'id')
 	 */
 	private class Binding
 	{
 		private Map<String, List<String>> bindings = new HashMap<String, List<String>>();
 		private String id;	//the unique id of the instance
 		
 		/**	Creates a new Binding from the given set of values
 		 *	The first variable is assumed to be the ID
 		 */
 		public Binding(BindingSet set, List<String> vars)
 		{
 			id = QueryResult.getValue(set.getBinding(vars.get(0)).toString(), vars.get(0));		//the first variable is the ID
 			initValues(vars, set, 1);
 		}
 		
 		/** Creates a new Binding from the given set of values and the given ID
 		 */
 		public Binding(BindingSet set, List<String> vars, String id)
 		{
 			this.id = id;
 			initValues(vars, set, 0);
 		}
 		
 		private void initValues(List<String> vars, BindingSet set, int start)
 		{
 			for (int i = start; i < vars.size(); i++)
 			{
 				String var = vars.get(i);
	 			if (set.getBinding(var) != null)
 				{
	 				String value = QueryResult.getValue(set.getBinding(var).toString(), var);
	 				if ((value.indexOf("#Seq") > 0) || (value.indexOf("_:node") > -1))
 						continue;	//if the value is a sequence, the type of a sequence, or the id (should be of a sequence), skip it!
 					if (var.equalsIgnoreCase("type") || var.equals(OntologyWriter.PROPERTY))
 						//TODO: URI: keep the whole URI of the type to identify bindings
 						value = value;
// 						value = value.substring(value.indexOf("#") + 1);
 					else if (value.indexOf(".owl#") > 0)
 						continue;
 										
 					List<String> list = new ArrayList<String>();
 					if (var.equals(OntologyWriter.PROPERTY))		//value matches a property
 					{	//so make this property the key, and the next binding its value
 						var = value;
 						i++;	//get next variable and value
 						value = QueryResult.getValue(set.getBinding(vars.get(i)).toString(), vars.get(i));
 					}
					list.add(value);
 					bindings.put(var, list);
 				}
 			}
 		}
 		
 		public Map<String, List<String>> getBindings()
 		{
 			return bindings;
 		}
 		 		
 		/**	If the id of the given set matches the id of this Binding, the set of
 		 *	values is added to this binding
 		 */
 		public boolean add(BindingSet set, List<String> vars)
 		{
 			if (!id.equals(QueryResult.getValue(set.getBinding(vars.get(0)).toString(), vars.get(0))))
 				return false;
 			addValues(set, vars, 1);
 			return true;
 		}
 			
 		private void addValues(BindingSet set, List<String> vars, int start)
 		{
 			for (int i = start; i < vars.size(); i++)
 			{
 				String var = vars.get(i);
 				if (set.getBinding(var) != null)
 				{
 					String value = QueryResult.getValue(set.getBinding(var).toString(), var);
 					if ((value.indexOf("#Seq") > 0) || (value.indexOf("_:node") > -1))
 						continue;	//if the value is a sequence or the type of a sequence, skip it!
 					if (var.equalsIgnoreCase("type") || var.equals(OntologyWriter.PROPERTY))
 						value = value.substring(value.indexOf("#") + 1);
 					else if (value.indexOf(".owl#") > 0)
 						continue;
 						
 					if (var.equals(OntologyWriter.PROPERTY))
					{
 						var = value;	//use the value as key, which is the name of the property
 						i++;	//get value of next variable
 						value = QueryResult.getValue(set.getBinding(vars.get(i)).toString(), vars.get(i));
					}
					
 					if (bindings.containsKey(var))
 					{
 						List<String> list = bindings.get(var);
 						if (!list.contains(value))
	 						list.add(value);
	 				}
 					else
 					{
 						List<String> list = new ArrayList<String>();
 						list.add(value);
	 					bindings.put(var, list);
 					}
 				}
 			}
 		}
 		
 		/**	If the given id matches the id of this Binding, the set of
 		 *	values is added to this binding
 		 */
 		public boolean add(BindingSet set, List<String> vars, String id)
 		{
 			if (!this.id.equals(id))
 				return false;
 			addValues(set, vars, 0);
 			return true;
 		}
 		
 		public void print()
 		{
 			System.out.println(id);
 			for (Iterator it = bindings.keySet().iterator(); it.hasNext(); )
 			{
 				String key = (String) it.next();
 				System.out.print(key + ": ");
 				List<String> values = bindings.get(key);
 				for (int i = 0; i < values.size(); i++)
 					if ((i + 1) == values.size())
 						System.out.println(values.get(i));
 					else
	 					System.out.print(values.get(i) + ", ");
 			}
 			System.out.println();
 		}
 		
 		public String getID()
 		{
 			return id;
 		}
 		
 		public String getNoLayout(List<String> vars, String head)
 		{
 			StringBuffer sb = new StringBuffer();
 			sb.append(bindings.get(head).get(0));
 			sb.append("LINEBREAK");
 			
 			for (int i = 1; i < vars.size(); i++)
 			{
 				String key = vars.get(i);
 				if (key.equals(head))
 					continue;
 				if (!bindings.containsKey(key))
 					continue;

 				sb.append(key + ": ");
 				List<String> values = bindings.get(key);
 				for (int j = 0; j < values.size(); j++)
 				{
 					sb.append(values.get(j));
 					if (j + 2 == values.size())	//if it's the second last value, add 'and'
 						sb.append(" and ");
 					else if ((j+1) < values.size())	//else add a komma
	 					sb.append(", ");
	 			}
	 			sb.append("LINEBREAK");
 			}
 			return sb.toString();
 		}
 		
 		public String getNLValue(List<String> nlproperties)
 		{
 			for (String key : nlproperties)
	 			if (bindings.containsKey(key))
 					return bindings.get(key).get(0);
 			return "";	
 		}
 		
 		public String getHTML(List<String> vars, String[] head)		
 		{
 			StringBuffer sb = new StringBuffer();
 			for (int i = 0; i < head.length; i++)
	 			if ((head[i] != null) && bindings.containsKey(head[i]))
 					sb.append(bindings.get(head[i]).get(0));	
 			sb.append("<ul>");
 			
 			boolean property = false;
 			for (int i = 1; i < vars.size(); i++)
 			{	
 				String key = vars.get(i);
 				if (key.equals(OntologyWriter.PROPERTY))
 					property = true;
 				if (!bindings.containsKey(key))
 					continue;
 				if (contains(key, head))
 					continue;
 					
 				sb.append("<li>" + key + ": ");
 				List<String> values = bindings.get(key);
 				for (int j = 0; j < values.size(); j++)
 				{
 					sb.append(values.get(j));
 					if (j + 2 == values.size())	//if it's the second last value, add 'and'
 						sb.append(" and ");
 					else if ((j+1) < values.size())	//else add a komma
	 					sb.append(", ");
	 			}
 			}
 			
 			if (property)	//if the query requested 'any' property, then the keys
 			{			//won't match the vars list.
	 			for (Iterator it = bindings.keySet().iterator(); it.hasNext(); )
 				{
 					String key = (String) it.next();
 					if (vars.contains(key))
 						continue;
 				
 					sb.append("<li>" + SGNode.normalise(key, true) + ": ");
 					List<String> values = bindings.get(key);
 					for (int j = 0; j < values.size(); j++)
 					{
 						sb.append(values.get(j));
 						if (j + 2 == values.size())	//if it's the second last value, add 'and'
 							sb.append(" and ");
 						else if ((j+1) < values.size())	//else add a komma
	 						sb.append(", ");
	 				}
 				}
 			}
 			
 			sb.append("</ul>");
 			return sb.toString();
 		}
 		
 		public void setMostSpecificType(OntologyReader reader)
 		{
 			if (!bindings.containsKey("Type"))
 				return;
 			
 			List<String> types = bindings.get("Type");
 			OntClass c = reader.getMostSpecificClass(types);
 			List<String> result = new ArrayList<String>();
 			result.add(c.getLocalName());
 			bindings.put("Type", result);
 		}
 		
 		private boolean contains(String key, String[] list)
 		{
 			for (int i = 0; i < list.length; i++)
 				if ((list[i] != null) && list[i].equals(key))
 					return true;
 			return false;
 		}
 	}
 }