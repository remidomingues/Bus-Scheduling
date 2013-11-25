/**
 * File : GoogleService.java
 *
 * Created on May 17, 2012, 11:13:22 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package dataLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import locationsData.Location;

//TODO YOANN : Amelioration possibles : ajout d une interface que les classes a geocoder ou pour distance matrix devront implementer (ameliore la reprise du code)
// Ameliorer la gestion des erreur des status en redefinissant la methode dans les clases pour les erreur specifiques et en appelant la classe mere pour les erreurs communes
// Ameliorer la gestion des IO exception avec Google Distances

/** 
 *  Class that contains exceptions, methods and attributes that are the same in the different google web services.
 */
public abstract class GoogleService
{
	/** Exception that indicates the google quotas limit is reached. */
	public static class OverQueryLimitException extends Exception
	{
		/** Serial version ID */
		private static final long serialVersionUID = 1L;
	}

	/** Exception throw when a location wasn't found by google. */
	public static class LocationNotFoundException extends Exception
	{
		/** Serial version ID  */
		private static final long serialVersionUID = 1L;

		/** The origin location which wasn't found. */
		private Location location;

		/** 
		 * Full constructor
		 * @param message message of the exception
		 * @param l the location that wasn't found
		 */
		public LocationNotFoundException(String message, Location l)
		{
			super(message);
			this.location = l;
		}

		/**
		 * Minimal constructor
		 * @param message message of the exception
		 */
		public LocationNotFoundException(String message)
		{
			this(message, null);
		}

		/**
		 * Return the location that was not found
		 * @return The location or null if non set
		 */
		public Location getLocation()
		{
			return location;
		}
	}

	/** Exception throw when google denied the software to send requests */
	public static class RequestDeniedException extends Exception
	{
		/** Default serial version ID */
		private static final long serialVersionUID = 1L;
	}

	/** 
	 * Abstract class containing attributes and methods that are commons for every request sent on google web services API
	 */
	public abstract class GoogleRequest
	{
		/**
		 *  Builds the URL using the parameters given to the class 
		 * @return a String representing the URL to send to the google web service
		 */
		protected abstract String getRequestURL();
	}

	/** Says if we use a premium account or not */
	protected boolean subscription = false;

	/**
	 * Send the request too google and return the result as an URLConnection
	 * @param request the request to send to google
	 * @return the {@link URLConnection} of the connection
	 * @throws IllegalArgumentException 
	 * @throws MalformedURLException if the request is incorrect
	 * @throws IOException If an input / output exception occurs when saving response or opening url
	 */
	protected static URLConnection connect(GoogleRequest request) throws IllegalArgumentException,
			IOException
	{
		try
		{
			// Try to create the URL and open the connection
			return new URL(request.getRequestURL()).openConnection();
		}
		catch (NullPointerException e)
		{
			throw new IllegalArgumentException("The given URL can't be null.", e);
		}
		catch (MalformedURLException e)
		{
			throw new IllegalArgumentException("The given URL is not a valid one.", e);
		}
		catch (IOException e)
		{
			throw new IOException("An error occured while trying to connect to Google.", e);
		}
	}

	/** 
	 * Check the given status code and throw the correct exception is needed
	 * @param status the status code to check
	 * @return true if status == OK, false is a unknown status (no exception thrown)
	 * @throws OverQueryLimitException if status indicates that the query limit is over
	 * @throws RequestDeniedException if status indicates that the query was denied
	 */
	protected static boolean checkStatus(String status) throws OverQueryLimitException,
			RequestDeniedException
	{
		if (status == null)
			throw new NullPointerException("Given status can't be null");
		if (status.equals("OK"))
			return true;
		if (status.equals("OVER_QUERY_LIMIT"))
			throw new OverQueryLimitException();
		if (status.equals("REQUEST_DENIED"))
			throw new RequestDeniedException();
		if (status.equals("UNKNOWN_ERROR"))
			throw new UnknownError("A unknown error occured with Google.");
		return false;

	}

}
