package WYSIWYM.util;

/**	This exception is thrown when something to do with Sesame throws an Exception
 *
 *	@author Feikje Hielkema
 *	@version 1.2
 */
public class SesameException extends Exception
{
	/**	@param message	The message
	 */
	public SesameException(String message)
	{
		super(message);
	}
}