package WYSIWYM.util;

/**	This exception is thrown when an anchor without relations is created
 *
 *	@author Feikje Hielkema
 *	@version 1.0
 */
public class BadAnchorException extends Exception
{
	/**	@param message  The exception's message
	 */
	public BadAnchorException(String message)
	{
		super(message);
	}
}