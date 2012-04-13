package WYSIWYM.util;

/**	This exception is thrown when there is an error in the creation or execution of
 *	a SPARQL query
 *
 *	@author Feikje Hielkema
 *	@version 1.0
 */
public class SPARQLException extends Exception
{
	/**	@param message	The message
	 */
	public SPARQLException(String message)
	{
		super(message);
	}
}