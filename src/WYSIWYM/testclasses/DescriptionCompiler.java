package WYSIWYM.testclasses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import simplenlg.realiser.AnchorString;
import WYSIWYM.model.ContentPlan;
import WYSIWYM.model.DatatypeNode;
import WYSIWYM.model.SGEdge;
import WYSIWYM.model.SGNode;
import WYSIWYM.ontology.OntologyReader;
import WYSIWYM.ontology.OntologyWriter;
import WYSIWYM.ontology.QueryResult;
import WYSIWYM.ontology.SesameReader;
import WYSIWYM.transformer.AutomaticGenerator;
import WYSIWYM.transformer.ContentPlanner;
import WYSIWYM.transformer.SemanticGraphTransformer;
import WYSIWYM.transformer.SurfaceRealiser;

/**	Takes a user name, finds all unregistered users by that name, and returns
 *	brief descriptions of them all. This class is used by ourSpaces to generate
 *	descriptions of (unregistered) users.
 *
 *	@author Feikje Hielkema
 *	@version 1.4 October 2008
 */
public class DescriptionCompiler
{
	/**	Takes a person's first and last name, finds any users with (partially) those same names, and generates 
	 *	small descriptions of them that include name, email, organisation, resources written, etc.
	 *	If registered is false, only return unregistered users.
	 *	
	 *	@param first First name
	 *	@param last	Last, family name
	 *	@param registered If false, only return unregistered users.
	 *	@return Map<String,String> with user Sesame ID's and descriptions.
	 */
	public Map<String,String> getDescriptions(String first, String last, boolean registered)
	{	
		try
		{	
			SesameReader sesame = new SesameReader(false);
			String sparql = OntologyWriter.getUserDescriptionQuery(first, last);
			WYSIWYM.ontology.QueryResult qResult = sesame.queryBinding(sparql);
			List<String> remove = new ArrayList<String>();
			
			for (Iterator it = qResult.getIDs(); it.hasNext(); )
			{	//remove all users who are already registered
				String id = (String) it.next();
				List<String> registeredList = qResult.getBindingValues(id, "registered");
				if ((!registered) && (registeredList != null) && (registeredList.size() > 0))
					remove.add(id);	//if we want to exclude registered users, remove those in this list
			}
			for (int i = 0; i < remove.size(); i++)
				qResult.remove(remove.get(i));
			
			for (Iterator it = qResult.getIDs(); it.hasNext(); )
			{	//remove all resources but the latest
				String id = (String) it.next();
				List<String> times = qResult.getBindingValues(id, "timestamp");
				if ((times == null) || (times.size() == 0))
					break;
				
				String str = QueryResult.getLiteralValue(times.get(0));	
				double recent = new Double(str).doubleValue();
				int idx = 0;
				for (int i = 1; i < times.size(); i++)
				{
					String s = QueryResult.getLiteralValue(times.get(i));	
					double d = new Double(s).doubleValue();
					if (d > recent)
					{
						recent = d;
						idx = i;
					}
				}
				qResult.removeAllButOne(id, idx, "Authored");
				qResult.removeVariable(id, "timestamp");
			}
			return qResult.getNoLayout("Name");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**	Takes a person's name, finds any users with (partially) the same name, and
	 *	extracts all information about them from the archive into SemanticGraphs.
	 *	Then generates feedback texts that only show certain information types 
	 *	(e.g. email, organisation).
	 *	
	 *	@param name Username
	 *	@return Map<String,String> with user Sesame ID's and descriptions.
	 */
	public Map<String,String> getNLDescriptions(String name)
	{
		Map<String,String> result = new HashMap<String,String>();
		try
		{
			OntologyReader reader = new OntologyReader();
			SesameReader sesame = new SesameReader(false);
			List<String> resourceTypes = reader.getSubClasses("Resource");
		
			//get all users with this name
			String sparql = new OntologyWriter(reader).getUserIDQuery(name);
			WYSIWYM.ontology.QueryResult qResult = sesame.queryBinding(sparql);
			String registerVar = qResult.getVariables().get(1);
			
			for (Iterator it = qResult.getIDs(); it.hasNext(); )
			{	//for each user, build a graph containing the description
				String id = (String) it.next();
				if (qResult.getBindingValues(id, registerVar) != null)	//if the user is already registered, continue
					continue;
					
				AutomaticGenerator generator = new AutomaticGenerator(reader, sesame);
				SemanticGraphTransformer sgt = generator.getUserInfoNoAnchors(name, id);
				
				//only show certain information
				SGNode root = sgt.getGraph().getRoot();
				root.setRealise(SGNode.HIDE);	//looks strange, but it means only SHOW edges will be shown
				boolean personal = false;
				
				for (Iterator nodeIt = root.getOutgoingEdges(); nodeIt.hasNext(); )
				{
					SGEdge edge = (SGEdge) nodeIt.next();
					if (reader.useAsProperName(edge.getLabel()))	//edge.isNLNameEdge())
						continue;
					String label = edge.getLabel();
					if (label.equals("Email") || label.equals("EmployeeOf") || label.equals("MemberOf"))
					{
						edge.setRealise(SGNode.SHOW);
						personal = true;
					}
					else
						edge.setRealise(SGNode.HIDE);
				}
				
				if (!personal)
				{	//if there was no personal information, show the resources the person is connected to instead
					for (Iterator nodeIt = root.getOutgoingEdges(); nodeIt.hasNext(); )
					{
						SGEdge edge = (SGEdge) nodeIt.next();
						SGNode target = (SGNode) edge.getTarget();
						if (target instanceof DatatypeNode)
							continue;
						if (resourceTypes.contains(target.getLabel()))	//if the target is a type of resource
							edge.setRealise(SGNode.SHOW);		//then show this information
					}
				}
				
				//then realise a textual representation
				ContentPlan plan = new ContentPlanner(reader, sgt.getGraph()).plan();
				List<AnchorString> list = new SurfaceRealiser().realise(plan);
				StringBuffer description = new StringBuffer();
				for (int i = 0; i < list.size(); i++)
					description.append(list.get(i).toString());
				result.put(id, description.toString());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}