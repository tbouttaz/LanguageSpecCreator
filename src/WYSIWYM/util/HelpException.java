package WYSIWYM.util;

/**	This exception is thrown when the wrong list is accessed in a help session
 *
 *	@author Feikje Hielkema
 *	@version 1.0
 */
public class HelpException extends Exception
{
	/**	@param message	The message
	 */
	public HelpException(String message)
	{
		super(message);
	}
}