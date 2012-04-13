package WYSIWYM.model;

/**	TreeComponent is an interface for components of trees (nodes and edges).
 *	@author Feikje Hielkema
 *	@version 1.1 20-10-2006
 */
public interface TreeComponent
{	
	/**	Sets the label
	 *	@param label
	 */
	public void setLabel(String label);
	
	/**	Sets the local ID
	 *	@param id Local ID
	 */
	public void setID(String id);
	
	/**	Gets the label
	 *	@return label
	 */
	public String getLabel();
	
	/**	Gets the local ID
	 *	@return Local ID
	 */
	public String getID();

	/**	Returns whether this component is an edge
	 *	@return true if this is an edge
	 */
	public boolean isEdge();
	
	/**	Returns whether this component is a node
	 *	@return true if this is a node
	 */
	public boolean isNode();
}