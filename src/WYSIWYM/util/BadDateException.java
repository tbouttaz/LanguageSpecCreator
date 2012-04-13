package WYSIWYM.util;

/**	This exception is thrown when a unwelldefined date is created, or an error is
 *	encountered when trying to generate a NL expression for this date
 *
 *	@author Feikje Hielkema
 *	@version 1.1
 */
public class BadDateException extends Exception
{
	/**	@param message	The message
	 */
	public BadDateException(String message)
	{
		super(message);
	}
}