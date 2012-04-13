package WYSIWYM.util;

/**	This exception is thrown when there is an error in getting tag clouds or storing
 *	tags.
 *	@author Feikje Hielkema
 *	@version 1.4
 */
public class FolksonomyException extends Exception
{
	/**	@param message	The message
	 */
	public FolksonomyException(String message)
	{
		super(message);
	}
}