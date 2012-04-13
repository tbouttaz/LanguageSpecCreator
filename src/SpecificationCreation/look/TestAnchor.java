package SpecificationCreation.look;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import SpecificationCreation.ontology.SpecificationOntologyReader;
import WYSIWYM.model.Anchor;
import WYSIWYM.model.DTNode;

import com.hp.hpl.jena.ontology.OntProperty;

/**	Implements an anchor for generating an example feedback text
 *
 *	@author Feikje Hielkema
 * 	@version 1.0 10/11/2008
 */
public class TestAnchor extends Anchor
{
	private List<String> compulsory = new ArrayList<String>();
	private Map<String, java.util.List<String>> optional = new HashMap<String,java.util.List<String>>();
	
	/**	Constructor, adds the menu items
	 *	@param reader Ontology
	 *	@param className Class name
	 *	@param node DTNode with nl-representation
	 */
	public TestAnchor(SpecificationOntologyReader reader, String className, DTNode node)
	{
		List<OntProperty> properties = reader.getDomainProperties(className);
		if (properties.size() == 0)	//no anchor needed
			return;
			
		Map<String,Integer[]> cardinalityMap = reader.getCardinalities(className);
		for (OntProperty p : properties)
		{
			String name = p.getLocalName();
			String submenu = reader.getSubmenu(p, className);
			if (cardinalityMap.containsKey(name) && (cardinalityMap.get(name)[0] > 0))
				compulsory.add(name);	//compulsory property
			else if (submenu == null)
				continue;
			else if (optional.containsKey(submenu))
				optional.get(submenu).add(name);
			else
			{
				java.util.List<String> list = new ArrayList<String>();
				list.add(name);
				optional.put(submenu, list);
			}	
		}
		
		node.setAnchor(this);
	}
	
	/**	Returns compulsory property names
	 *	@return List<String>
	 */
	public List<String> getCompulsory()
	{
		return compulsory;
	}
	
	/**	Returns submenu list
	 *	@return List<String>
	 */
	public List<String> getMenus()
	{	//return a list of the submenus, with 'none' first then ordered alphabetically
		java.util.List<String> result = new ArrayList<String>();
		for (Iterator it = optional.keySet().iterator(); it.hasNext(); )
		{
			String menu = (String) it.next();
			if (!menu.equals(SpecificationOntologyReader.NONE))
				result.add(menu);
		}
		Collections.sort(result);
		return result;
	}
	
	/**	Returns optional property names in the given menu
	 *	@param menu Submenu
	 *	@return List<String>
	 */
	public List<String> getOptional(String menu)
	{
		if (optional.containsKey(menu))
			return optional.get(menu);
		return new ArrayList<String>();
	}
	
	/**	Checks whether the anchor is red (has compulsory items)
	 *	@return true if the anchor is red
	 */
	public boolean isRed()
	{
		if (compulsory.size() > 0)
			return true;
		return false;
	}
}