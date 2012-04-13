package SpecificationCreation.look;

/**	This exception is thrown when an anchor without relations is created
 *
 *	@author Feikje Hielkema
 *	@version 1.0 08-12-2008
 */
public class CancelledException extends Exception
{
	/**	@param message	The message
	 */
	public CancelledException(String message)
	{
		super(message);
	}
}