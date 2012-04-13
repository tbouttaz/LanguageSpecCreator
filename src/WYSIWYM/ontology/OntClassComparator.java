package WYSIWYM.ontology;

import java.util.Comparator;

import com.hp.hpl.jena.ontology.OntClass;

/**	Compares two OntClasses through their local name, to achieve an alphabetic order.
 *
 *	@author Feikje Hielkema
 *	@version 1.2 28-03-2008
 */
public class OntClassComparator implements Comparator
{
	/**	Compares two OntClasses by their local names. NB: this ignores name-spaces!
	 *	@see Comparator#compare(Object,Object)
	 *	@param o1 Object
	 *	@param o2 Object
	 *	@return int, positive if o1 comes later alphabetically, negative if o2 comes earlier
	 */	
	public int compare(Object o1, Object o2)
	{
		String name1 = ((OntClass) o1).getLocalName();
		String name2 = ((OntClass) o2).getLocalName();
		return name1.compareTo(name2);
	}
}