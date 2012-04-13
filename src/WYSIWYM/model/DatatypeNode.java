package WYSIWYM.model;

import java.io.IOException;

/**	DatatypeNode is an interface for nodes that contain some datatype (instead
 *	of corresponding to an individual)
 *
 *	@author Feikje Hielkema
 */
 public interface DatatypeNode
 {	//datatypes
 	public static int STRING = 0;
 	public static int INT = 1;
 	public static int DOUBLE = 2;
 	public static int BOOLEAN = 3;
 	
 	/**	Returns the datatype value of the node
 	 *	@return Object with datatype value
 	 */
 	public Object getValue();
 	
 	/**	Sets the datatype value
 	 *	@param value Datatype value
 	 *	@throws IOException if the parameter is the wrong datatype
 	 */
 	public void setValue(Object value) throws IOException;
 	
 	/**	Returns the datatype
 	 *	@return int with datatype
 	 */
 	public int getDatatype();
 	
 	/**	Sets the old value
 	 *	@param value Value Object
 	 *	@throws IOException if the parameter is the wrong datatype
 	 */
 	public void setOldValue(Object value) throws IOException;
 	
 	/**	Returns the old value
 	 *	@return old value
 	 */
 	public Object getOldValue();
 }