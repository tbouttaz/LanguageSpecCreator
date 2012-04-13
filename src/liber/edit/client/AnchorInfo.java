package liber.edit.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**	Contains all the information need to show the feedback text (incl. popupmenus)
 *	in the interface.
 *
 *	@author Feikje Hielkema
 *	@version 1.0 January 2007
 */
public class AnchorInfo implements IsSerializable
{
	private String words, uri, id;
	private int showhide = 0;
	private boolean isAnchor = false, removableNode = true, summation = false;
	private boolean isRed;
	private String[] compulsory;
	private String[] compulsoryNL;
	
	private String[] optional;
	private String[] optionalNL;
	private String[] optionalPropType;
	
	private String[] removable;
	private String[] removableNL;
	private String[][] removableValues;

	/**	Set the ID
	 *	@param id ID
	 */
	public void setID(String id)
	{
		this.id = id;
	}
	
	/**	Return the ID
	 *	@return String
	 */
	public String getID()
	{
		return id;
	}

	/**	Set whether this contains a Summation Anchor
	 *	@param s True if this contains a summation
	 */
	public void setSummation(boolean s)
	{
		summation = s;
	}
	
	/**	Checks whether this contains a Summation Anchor
	 *	@return True if this contains a summation
	 */
	public boolean isSummation()
	{
		return summation;
	}

	/**	Sets whether this contains an Anchor
	 *	@param a True if this contains an anchor
	 */
	public void setAnchor(boolean a)
	{
		isAnchor = a;
	}
	
	/**	Sets the URL where the resource can be found, if this
	 *	is an anchor for a resource the user has permission to see
	 *	@param u URL
	 */
	public void setURI(String u)
	{
		uri = u;
	}
	
	/**	Set whether the anchor has a browsing option, and whether
	 *	it is for showing or hiding information.
	 *	@param s 1 to hide information, 2 for no  browsing option, 3 to show information
	 */
	public void setShowHide(int s)
	{
		showhide = s;
	}
	
	/**	Checks whether the anchor has a browsing option, and whether
	 *	it is for showing or hiding information.
	 *	@return 1 to hide information, 2 for no  browsing option, 3 to show information
	 */
	public int getShowHide()
	{
		return showhide;
	}
	
	/**	Checks whether this contains an anchor
	 *	@return true if this contains an Anchor, false if it is just a String
	 */
	public boolean isAnchor()
	{
		return isAnchor;
	}
	
	/**	Sets whether this anchor should be red (because it has compulsory items).
	 *	@param r True if red
	 */
	public void setRed(boolean r)
	{
		isRed = r;
		isAnchor = true;	//if it has a colour, it must be an anchor
	}
	
	/**	Checks whether this anchor should be red (because it has compulsory items).
	 *	@return True if red
	 */
	public boolean isRed()
	{
		return isRed;
	}
	
	/**	Sets the label.
	 *	@param w Label
	 */
	public void setWords(String w)
	{
		words = w;
	}
	
	/**	Returns the label.
	 *	@return Label
	 */
	public String getWords()
	{
		return words;
	}
	
	/**	Returns the URL.
	 *	@return String URL
	 */
	public String getURI()
	{
		return uri;
	}
	
	/**	Sets the compulsory properties
	 *	@param s String[]
	 */
	public void setCompulsory(String[] s)
	{
		compulsory = s;
	}
	
	/**	Sets the compulsory menu items (nl-representations of properties)
	 *	@param s String[]
	 */
	public void setCompulsoryNL(String[] s)
	{
		compulsoryNL = s;
	}
	
	/**	Gets the compulsory properties
	 *	@return String[]
	 */
	public String[] getCompulsory()
	{
		return compulsory;
	}
	
	/**	Gets the compulsory menu items (nl-representations of properties)
	 *	@return String[]
	 */
	public String[] getCompulsoryNL()
	{
		return compulsoryNL;
	}
	
	/**	Sets the optional properties
	 *	@param s String[]
	 */
	public void setOptional(String[] s)
	{
		optional = s;
	}
	
	/**	Sets the optional menu items (nl-representations of properties)
	 *	@param s String[]
	 */
	public void setOptionalNL(String[] s)
	{
		optionalNL = s;
	}
	
	/**	Gets the optional properties
	 *	@return String[]
	 */
	public String[] getOptional()
	{
		return optional;
	}
	
	/**	Gets the optional menu items (nl-representations of properties)
	 *	@return String[]
	 */
	public String[] getOptionalNL()
	{
		return optionalNL;
	}
	
	/**	Gets the submenus the optional menu items should be in
	 *	@return String[]
	 */
	public String[] getOptionalPropType()
	{
		return optionalPropType;
	}
	
	/**	Sets the submenus the optional menu items should be in
	 *	@param pt String[]
	 */
	public void setOptionalPropType(String[] pt)
	{
		optionalPropType = pt;
	}	
	
	/**	Sets the removable properties
	 *	@param s String[]
	 */
	public void setRemovable(String[] s)
	{
		removable = s;
	}
	
	/**	Sets the nl-representations of the removable properties
	 *	@param s String[]
	 */
	public void setRemovableNL(String[] s)
	{
		removableNL = s;
	}
	
	/**	Gets the removable properties
	 *	@return String[]
	 */
	public String[] getRemovable()
	{
		return removable;
	}
	
	/**	Gets the nl-representations of the removable properties
	 *	@return String[]
	 */
	public String[] getRemovableNL()
	{
		return removableNL;
	}
	
	/**	Sets the values of the removable properties
	 *	@param v String[][], the properties and their values
	 */
	public void setRemovableValues(String[][] v)
	{
		removableValues = v;
	}
	
	/**	Gets the values of the removable properties
	 *	@return String[][], the properties and their values
	 */
	public String[][] getRemovableValues()
	{
		return removableValues;
	}
	
	/**	Sets whether this anchor's node can be removed.
	 *	@param r True if this node can be removed
	 */
	public void setRemovableNode(boolean r)
	{
		removableNode = r;
	}
	
	/**	Checks whether this anchor's node can be removed.
	 *	@return True if this node can be removed
	 */
	public boolean isRemovableNode()
	{
		return removableNode;
	}
}
