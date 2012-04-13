package SpecificationCreation.ontology;

//import uk.abdn.csd.Hierarchy;
import java.util.ArrayList;
import java.util.List;

import liber.edit.client.Hierarchy;

/**	A class hierarchy that, besides parent and child nodes, specifies
 *	whether a class was imported from another ontology.
 *
 *	@author Feikje Hielkema
 * 	@version 1.0 10/11/2008
 */
public class CheckedHierarchy
{
	private String value, nlExpr;
	private boolean shaded = false;	//if this is true this item is 'faded out' in the tree
	private List<CheckedHierarchy> subHierarchy = new ArrayList<CheckedHierarchy>();
	
	/**	Constructor
	 *	@param h Hierarchy
	 */
	public CheckedHierarchy(Hierarchy h)
	{
		value = h.getValue();
		nlExpr = h.getNLExpr();
	}
	
	/**	Check whether class was imported
	 *	@return true if class was imported
	 */
	public boolean isShaded()
	{
		return shaded;
	}
	
	/**	Sets whether class was imported
	 *	@param shaded true if class was imported
	 */
	public void setShaded(boolean shaded)
	{
		this.shaded = shaded;
	}
	
	/**	Get class name
	 *	@return String
	 */
	public String getValue()
	{
		return value;
	}
	
	/**	Get nl-expression
	 *	@return String
	 */
	public String getNLExpression()
	{
		return nlExpr;
	}
	
	/**	Adds a child class
	 *	@param sub CheckedHierarchy
	 */
	public void addSub(CheckedHierarchy sub)
	{
		subHierarchy.add(sub);
	}
	
	/** Returns the child class at given index
	 *	@param i Index
	 *	@return CheckedHierarchy
	 */
	public CheckedHierarchy getSub(int i)
	{
		return subHierarchy.get(i);
	}
	
	/**	Returns number of children
	 *	@return size
	 */
	public int size()
	{
		return subHierarchy.size();
	}
}