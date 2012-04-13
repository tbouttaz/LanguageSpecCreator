package simplenlg.exception;

/**
 * A <code>RuntimeException</code> thrown by any module within the
 * Microplanner. Represented in a separate class to permit applications to
 * specifically handle Microplanning exceptions.
 * 
 * @author agatt
 * 
 */

public class MicroplannerException extends SimplenlgException {

	static final long serialVersionUID = 1; // needed because this is
											// serialisable


	/**
	 * The constructor simply calls the superclass
	 * and prints an error message to the default 
	 * <code>System.err</code>.
	 */
	public MicroplannerException(String arg0) {
		super(arg0);		
		System.err.println("Microplanner simplenlg.exception: " + arg0);		
	}
	
}
