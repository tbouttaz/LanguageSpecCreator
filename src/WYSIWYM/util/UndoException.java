package WYSIWYM.util;

/**	This exception is thrown when an anchor without relations is created
 *
 *	@author Feikje Hielkema
 	@version 1.0
 */
public class UndoException extends Exception
{
	/**	@param message	The message
	 */
	public UndoException(String message)
	{
		super(message);
	}
}