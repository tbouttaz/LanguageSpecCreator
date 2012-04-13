package SpecificationCreation.look;

import javax.swing.tree.DefaultMutableTreeNode;

/**	A node in the class or property tree. If the minimum required linguistic information has
 *	been added it receives an ok-sign; if it was imported from another ontology, it's grey;
 *	if it signals a submenu, it's blue; if it is a property	with a minimum cardinal restriction, 
 *	it's red.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 October 20 2008
 */
public class ClassTreeNode extends DefaultMutableTreeNode implements Comparable
{
	private boolean shaded = false, completed = false, cardinal = false, root = false, menu = false, nlProp = false;
	
	/**	Constructor. If shaded is true, the node-text is grey; if completed is true, the node
	 *	receives an ok-sign.
	 *
	 *	@param value Label
	 *	@param shaded If true, text should be grey
	 *	@param completed If true, add ok-sign
	 */
	public ClassTreeNode(String value, boolean shaded, boolean completed)
	{
		super(value);
		this.shaded = shaded;	
		this.completed = completed;
	}
	
	/**	Constructor. If shaded is true, the node-text is grey; if completed is true, the node
	 *	receives an ok-sign; if menu is true, the node is blue.
	 *
	 *	@param value Label
	 *	@param shaded If true, text should be grey
	 *	@param completed If true, add ok-sign
	 *	@param menu If true, text should be blue
	 */
	public ClassTreeNode(String value, boolean shaded, boolean completed, boolean menu, boolean nlProp)
	{
		super(value);
		this.shaded = shaded;	
		this.completed = completed;
		this.menu = menu;
		this.nlProp = nlProp;
	}
	
	/**	Checks whether node is grey (imported)
	 *	@return true if node is grey
	 */
	public boolean isShaded()
	{
		return shaded;
	}
	
	/**	Checks whether node is complete
	 *	@return true if node has ok-sign
	 */
	public boolean isCompleted()
	{
		return completed;
	}
	
	/**	Sets completion status of node
	 *	@param c True to give node ok-sign, false to remove it.
	 */
	public void setCompleted(boolean c)
	{
		completed = c;
	}
	
	/**	Checks whether node is complete
	 *	@return true if node has ok-sign
	 */
	public boolean isNLProp()
	{
		return nlProp;
	}
	
	/**	Sets completion status of node
	 *	@param c True to give node ok-sign, false to remove it.
	 */
	public void setNLProp(boolean c)
	{
		nlProp = c;
	}
	
	/**	Checks whether node has a minimum cardinal constraint
	 *	@return true if there is a minimum cardinal constraint 
	 */
	public boolean isCardinal()
	{
		return cardinal;
	}
	
	/**	Sets whether node has a minimum cardinal constraint
	 *	@param c if there is a minimum cardinal constraint 
	 */
	public void setCardinal(boolean c)
	{
		cardinal = c;
	}
	
	/**	Checks whether node is a sub-menu
	 *	@return true if the node is a sub-menu
	 */
	public boolean isMenu()
	{
		return menu;
	}
	
	/**	Sets whether node is a sub-menu
	 *	@param c true if the node is a sub-menu
	 */
	public void setMenu(boolean c)
	{
		menu = c;
	}
	
	/**	Checks whether node is the root of the tree
	 *	@return true if the node is the root
	 */
	public boolean isRoot()
	{
		return root;
	}
	
	/**	Sets whether node is the root of the tree
	 *	@param c true if the node is the root
	 */
	public void setRoot(boolean c)
	{
		root = c;
	}
	
	/**	True for nodes that are not completed, not menus, and have no cardinal
	 *	constraints.
	 *	@return True if there is no special information about this node.
	 */
	public boolean noInfo()
	{
		if (menu || completed || cardinal)
			return false;
		return true;
	}
	
	/**	Compares two nodes to order them in the tree; first 
	 *	nodes with cardinal restrictions, then order alphabetically.
	 *
	 *	@param o Object
	 *	@return positive int if o comes first, negative if this node comes first
	 *	@throws ClassCastException if o cannot be cast to a ClassTreeNode
	 *	@see Comparable#compareTo(Object)
	 */
	public int compareTo(Object o) throws ClassCastException
	{
		ClassTreeNode other = (ClassTreeNode) o;
		if (cardinal ^ other.isCardinal())
		{	//if one is cardinal but not the other
			if (cardinal)
				return -1;	//if this is cardinal, it should go before the other
			else
				return 1;
		}
		//both same situation, so order alphabetically
		return toString().compareToIgnoreCase(other.toString());
	}
}