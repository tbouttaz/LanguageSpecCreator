package WYSIWYM.util;

/**	This exception is thrown when there is an error reading the ontology.
 *
 *	@author Feikje Hielkema
 *	@version 1.0
 */
public class OntologyInputException extends Exception
{
	/**	@param message	The message
	 */
	public OntologyInputException(String message)
	{
		super(message);
	}
}