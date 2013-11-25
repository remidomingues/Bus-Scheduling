/**
 * File : GoogleGeocodeParser.java
 *
 * Created on May 14, 2012, 9:56:42 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package dataLoader;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.UnexpectedException;
import java.util.Collection;

import locationsData.Location;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** 
 * Service class uses to fill the latitude and a longitude of a location matrix using the Google Geocoding API
 */
public class GoogleGeocode extends GoogleService
{
	/** Contains all information about a google request on the google geocoding API. 
	 * Please read the Google Geocoding API Documentation if you don't understand some of the parameters. 
	 * ( https://developers.google.com/maps/documentation/geocoding )*/
	private class GeocodeRequest extends GoogleRequest
	{
		/** Constant used for building geographic coordinates requests */
		private static final String GEOCODE_REQUEST_TEMPLATE = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&region=%s&language=%s&sensor=%s";
		/** A human readable address that you want to geocode */
		protected String address;
		/** The bounding box of the viewport within which to bias geocode results more prominently. */
//		protected String bounds = "-26.235534,-48.915367|-26.364188,-48.770485";
		/** The region code, specified as a ccTLD ("top-level domain") two-character value. */
		protected String region = "br";
		/** The language in which to return results. */
		protected String language = "pt-BR";
		/** Indicates whether or not the geocoding request comes from a device with a location sensor. */
		protected boolean sensor = false;

		/**
		 * Constructor with address given as a {@link String}
		 * @param address the address
		 * @throws NullPointerException if the given location is null
		 */
		public GeocodeRequest(String address) throws NullPointerException
		{
			if (address == null || address.trim().length() == 0)
				throw new NullPointerException("Given address can't be null.");
			this.address = address.trim().replace(' ', '+');
		}

		/**
		 * Constructor by {@link Location}
		 * @param loc the location 
		 * @throws NullPointerException if the given location is null
		 */
		public GeocodeRequest(Location loc) throws NullPointerException
		{
			this(loc.getStreet() + " - "
					+ loc.getDistrict() + ", Joinville - Santa Catarina, Brasil");
		}

		/**
		* Builds the URL using the parameters given to the class 
		* @return a String representing the URL to send to the google web service
		* @see dataLoader.GoogleService.GoogleRequest#getRequestURL()
		*/
		@Override
		protected String getRequestURL()
		{
			return String.format(GEOCODE_REQUEST_TEMPLATE, address, region, language, sensor);
		}
	}

	/** The location that will be filled by this class */
	private Location loc;

	/**
	 * Constructor
	 * @param loc the {@link Location} to geocode
	 */
	private GoogleGeocode(Location loc)
	{
		this.loc = loc;
	}

	/**
	 * Geocode the given {@link Location} and complete the latitude and longitude field of it
	 * @param loc the {@link Location} to be filled
	 * @throws IOException 
	 * @throws LocationNotFoundException 
	 * @throws RequestDeniedException 
	 * @throws OverQueryLimitException 
	 * @throws NullPointerException 
	 * @throws Exception 
	 */
	public static void geocode(Location loc) throws NullPointerException, OverQueryLimitException, RequestDeniedException, LocationNotFoundException, IOException, Exception
	{
		if (loc == null)
			throw new NullPointerException("You have to give a location to geocode.");
		GoogleGeocode gg = new GoogleGeocode(loc);
		gg.geocode();
	}

	/**
	 * Geocode the given {@link Location} collection and complete the latitude and longitude field of it
	 * @param locs the collection of  {@link Location} to be filled
	 * @throws Exception if a unexpected error occurred, probably due to an error with the file return by google
	 * @throws IOException if error occurred when connecting to Google
	 * @throws LocationNotFoundException if the given location wasn1t found
	 * @throws RequestDeniedException if google refuse to execute the requests
	 * @throws OverQueryLimitException if the google quotas limit is over
	 * @throws NullPointerException if the given location is null
	 */
	public static void geocode(Collection<Location> locs) throws Exception, NullPointerException,
			OverQueryLimitException, RequestDeniedException, LocationNotFoundException, IOException
	{
		for (Location loc : locs)
		{
			geocode(loc);
		}
	}

	/**
	 * Geocode the given {@link Location} array and complete the latitude and longitude field of it
	 * @param locs the array of  {@link Location} to be filled
	 * @throws Exception if a unexpected error occurred, probably due to an error with the file return by google
	 * @throws IOException if error occurred when connecting to Google
	 * @throws LocationNotFoundException if the given location wasn1t found
	 * @throws RequestDeniedException if google refuse to execute the requests
	 * @throws OverQueryLimitException if the google quotas limit is over
	 * @throws NullPointerException if the given location is null
	 */
	public static void geocode(Location[] locs) throws Exception, NullPointerException,
			OverQueryLimitException, RequestDeniedException, LocationNotFoundException, IOException
	{
		for (Location loc : locs)
		{
			geocode(loc);
		}
	}

	/**
	 * Geocode the location attribute and fill the latitude and longitude parameters
	 * @throws IOException 
	 * @throws LocationNotFoundException 
	 * @throws RequestDeniedException 
	 * @throws OverQueryLimitException 
	 * @throws NullPointerException 
	 * @throws Exception 
	 */
	public void geocode() throws NullPointerException, OverQueryLimitException, RequestDeniedException, LocationNotFoundException, IOException, Exception
	{
		try
		{
			parse(connect(new GeocodeRequest(loc)).getInputStream());
		}
		catch (IllegalArgumentException e)
		{
			throw new UnexpectedException("Unexpected exception.", e);
		}

	}

	/**
	 * Fill the latitude and longitude of the location given by parsing a JSON input stream
	 * @param in The JSON input stream
	 * @throws LocationNotFoundException 
	 * @throws RequestDeniedException 
	 * @throws OverQueryLimitException 
	 * @throws Exception 
	 */
	private void parse(InputStream in) throws Exception, OverQueryLimitException, RequestDeniedException,
			LocationNotFoundException
	{
		try
		{
			JsonNode rootNode = new ObjectMapper().readValue(in, JsonNode.class);
			JsonNode status = rootNode.get("status");
			if (status == null)
				throw new Exception("The JSON file is not correctly formatted.");
			if (!checkStatus(status.asText()))
			{
				if (status.asText().equals("ZERO_RESULTS"))
					throw new LocationNotFoundException("Location not found, impossible to geocode it.", loc);
				else
					throw new Exception("Unknow status code.");
			}
			JsonNode geocode = rootNode.path("results").path(0).path("geometry").path("location");
			if (geocode.isMissingNode() || geocode.get("lat") == null || geocode.get("lng") == null)
				throw new Exception("The JSON file is not correctly formatted.");
			loc.setLatitude((float) geocode.get("lat").asDouble());
			loc.setLongitude((float) geocode.get("lng").asDouble());
		}
		catch (JsonParseException e)
		{
			throw new Exception("Error with the JSON file.");
		}
		catch (JsonMappingException e)
		{
			throw new Exception("Error with the JSON file.");
		}
		catch (IOException e)
		{
			throw new Exception("Error with the JSON file.");
		}
	}
}
