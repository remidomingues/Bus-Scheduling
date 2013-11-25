/**
 * File : BusSchedulingException.java
 *
 * Created on 10 mai 2012, 19:45:22
 *
 * Authors : Remi DOMINGUES & Yoann ALVAREZ
 */

package application;

/**
 * Internal software error exception. This one is threw when an error must be up to the user. 
 */
public class BusSchedulingException extends Exception
{

	/** Default serial ID */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * @param message Error message 
	 */
	public BusSchedulingException(String message)
	{
		super(message);
	}

	/**
	 * Default constructor
	 */
	public BusSchedulingException()
	{
		super();
	}
}
