package dataLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import locationsData.DataMatrix;
import locationsData.Location;
import presentation.ProgressionEvent.ProgressionType;
import application.BusScheduling;
import application.BusSchedulingException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service class uses to fill a locations matrix using the Google Distances Matrix API
 */
public class GoogleDistances extends GoogleService
{
	/** Contains all information about a google request on the google distances API.*/
	protected class DistanceRequest extends GoogleRequest
	{
		/** Constant used for building distance requests. */
		private static final String DISTANCE_REQUESTS_TEMPLATE = "http://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&language=en_US&sensor=false";

		/** Array of the origins used for the request.*/
		private Location[] origins;
		
		/** Array of the destinations used for the request.*/
		private Location[] destinations;

		/**
		 * Full constructor
		 * @param origins array of the origins used for the request
		 * @param destinations array of the destinations used for the request
		 */
		public DistanceRequest(Location[] origins, Location[] destinations)
		{
			this.origins = origins;
			this.destinations = destinations;
		}

		/**
		 * Builds the URL using the parameters given to the class.
		 * @return a String representing the URL to send to the google web service
		 */
		@Override
		protected String getRequestURL()
		{
			StringBuilder origin = new StringBuilder();
			StringBuilder destination = new StringBuilder();
			for (Location ori : origins)
			{
				if (ori != null)
				{
					origin.append(ori.getLatitude()).append("+").append(ori.getLongitude()).append("|");
				}
			}
			for (Location dest : destinations)
			{
				if (dest != null)
				{
					destination.append(dest.getLatitude()).append("+").append(dest.getLongitude())
							.append("|");
				}
			}
			return String.format(DISTANCE_REQUESTS_TEMPLATE, origin, destination);
		}

	}

	/** Maximum number of locations allowed per distance request on Google Distance Matrix API */
	protected static final int MAX_LOCATIONS_PER_DISTANCE_REQUEST = 10;

	/** Maximum number of locations allowed per distance request on Google Distance Matrix API with a Google Business subscription */
	protected static final int MAX_LOCATIONS_PER_SUBSCRIBED_DISTANCE_REQUEST = 25;

	/** Maximal number of locations which can be requested per 24 hours without a Google Business subscription */
	protected static final int MAX_LOCATIONS_NO_SUBSCRIPTION = 50;

	/** Maximal number of locations which can be requested per 24 hours with a Google Business subscription */
	protected static final int MAX_LOCATIONS_BUSINESS_SUBSCRIPTION = 316;

	/** Minimal time in millisecond between two requests sent to google*/
	protected static final int TIME_BETWEEN_REQUEST = 10000;

	// Unused
	/** The name of the directory use to save the files returns by the distances API */
	protected static final String SAVES_DIRECTORY = "distances";

	/** The formatter to format the date in order to create the directory used to stock distances data. */
	protected static final SimpleDateFormat directory_formatter = new SimpleDateFormat("dd-MM-yyyy");

	/** The formatter to format the date in order to create the file used to stock distances data. */
	protected static final SimpleDateFormat file_formatter = new SimpleDateFormat("HH-mm-ss");

	
	/*---------------------- NON-STATIC ATTRIBUTES ----------------------*/

	/** The matrix that will be filled by the parsed data. */
	protected DataMatrix matrix;
	
	/** Bus Scheduling used to fire progression events */
	protected BusScheduling busScheduling;

	/**
	 * Fill the data matrix given 
	 * @param matrix the {@link DataMatrix} to fill
	 * @param busScheduling BusScheduling used to fire events
	 * @param subscription 
	 * @throws Exception 
	 */
	public static void fillMatrix(DataMatrix matrix, BusScheduling busScheduling, boolean subscription)
			throws Exception
	{
		if (matrix == null)
			throw new NullPointerException(
					"You have to give a Data Matrix to the parser in order to fill it while parsing.");
		GoogleDistances gd = new GoogleDistances(matrix, busScheduling, subscription);
		gd.fill();
	}

	/** 
	 * Full constructor
	 * @param matrix the Data Matrix that will be filled by the parsed data
	 * @param busScheduling BusScheduling used to fire events
	 * @param subscription true if a Google Business subscription is owned, false else
	 * @throws NullPointerException if you don't give a matrix
	 * @throws IllegalArgumentException if the matrix has more locations than authorized by the system
	 * @throws BusSchedulingException If the number of locations is too much high for the subscription
	 */
	protected GoogleDistances(DataMatrix matrix, BusScheduling busScheduling, boolean subscription)
			throws NullPointerException, IllegalArgumentException, BusSchedulingException
	{
		if (matrix == null)
			throw new NullPointerException(
					"You have to give a Data Matrix to the parser in order to fill it while parsing.");
		this.matrix = matrix;
		this.busScheduling = busScheduling;
		this.subscription = subscription;
//		generateFolders();
	}

	/** 
	 * Fill the data matrix attribute.
	 * @throws Exception 
	 * @throws IllegalArgumentException 
	 */
	public void fill() throws IllegalArgumentException, Exception
	{
		int cpt = 1, percent = 0, tmpPercent;
		// First we cut the locations to create the different requests
		DistanceRequest[] requests = createRequests();
		int tries = 0;
		boolean retry = false;
		// Then for each request we ask google and parse the result
		for (DistanceRequest request : requests)
		{
			tries = 0;
			do
			{
				try
				{
					parse(new FileInputStream(new File(saveResponse(connect(request)))), request);
					retry = false;
				}
				catch (IOException e)
				{
					retry = true;
					if (tries < 2)
					{
						tries++;
					}
					else
						throw new IOException("I/O error occured while trying to get data", e);
				}
				catch (OverQueryLimitException e)
				{
					retry = true;
					if (tries < 2)
					{
						tries++;
						Thread.sleep(2000);
					}
					else
						throw e;
				}
			}
			while (retry);
			// we wait the amount of time needed before sending a new
			// request
			Thread.sleep(TIME_BETWEEN_REQUEST);
			tmpPercent = 40 * cpt / requests.length;
			if (tmpPercent != percent)
			{
				busScheduling.fireEvent(this, ProgressionType.INCREMENT, tmpPercent - percent);
				percent = tmpPercent;
			}
			++cpt;
		}
	}

	/**
	 * Create the requests that will need to be sent to Google
	 * @return an array of {@link DistanceRequest} that need to be sent to google.
	 */
	protected DistanceRequest[] createRequests()
	{
		DistanceRequest[] requests;

		List<Location> locs = matrix.getLocations();
		int nbSteps, i, j, pos, start, end, tabSize = locs.size();
		int nbLocPerRequest = subscription ? MAX_LOCATIONS_PER_SUBSCRIBED_DISTANCE_REQUEST
				: MAX_LOCATIONS_PER_DISTANCE_REQUEST;

		nbSteps = (int) Math.ceil(tabSize / (float) nbLocPerRequest);

		Location origins[], destinations[];
		requests = new DistanceRequest[nbSteps * nbSteps];
		// separate the points array in different parts depending the number of
		// max points by request
		for (i = 0 ; i < nbSteps ; i++)
		{
			// we create the origin points array
			start = i * nbLocPerRequest; // the first point to read
			// the locations array
			end = start + nbLocPerRequest; // the last point to read
			// in the location array
			if (end > tabSize)
			{
				end = tabSize; // avoid to exceed the limit size
			}
			origins = new Location[end - start];
			for (pos = start ; pos < end ; pos++)
			{
				// copy the origins points in the array
				origins[pos - start] = locs.get(pos);
			}

			// for each origins point array we need to build the all
			// destinations array
			for (j = 0 ; j < nbSteps ; j++)
			{

				start = j * nbLocPerRequest;
				end = start + nbLocPerRequest;

				if (end > tabSize)
				{
					end = tabSize; // avoid to exceed the limit size
				}
				destinations = new Location[end - start];
				for (pos = start ; pos < end ; pos++)
				{
					destinations[pos - start] = locs.get(pos);
				}
				// then we can build urls
				requests[i * nbSteps + j] = new DistanceRequest(origins, destinations);
			}
		}
		return requests;
	}

	/**
	 * Parse a file and fill the matrix
	 * @param in contains the distances informations
	 * @param request url request
	 * @throws Exception 
	 */
	protected void parse(InputStream in, DistanceRequest request) throws Exception
	{
		ObjectMapper mapper;
		JsonNode rootNode, status, distance, duration;
		String statusValue;
		int distanceValue, durationValue;
		int row = 0, col = 0;

		try
		{
			mapper = new ObjectMapper();
			rootNode = mapper.readValue(in, JsonNode.class);
			status = rootNode.get("status");
			if (status == null)
				throw new IllegalArgumentException("The JSON file is not correctly formatted.");
			if (status.asText().equals("OVER_QUERY_LIMIT"))
				throw new OverQueryLimitException();
			if (status.asText().equals("UNKNOWN_ERROR"))
				throw new Exception("A unknown error occured with Google.");
			if (!status.asText().equals("OK"))
				throw new IllegalArgumentException("An error occured with the google API. Please try again.");
			// We get all the origin locations
			row = col = 0;
			for (JsonNode node : rootNode.path("origin_addresses"))
			{
				if (node.isTextual())
				{
					if (node.asText().isEmpty())
						throw new LocationNotFoundException("Origin location was not found.",
								request.origins[row]);
				}
				row++;
			}
			// We get all the destination locations
			for (JsonNode node : rootNode.path("destination_addresses"))
			{
				if (node.isTextual())
				{
					if (node.asText().isEmpty())
						throw new LocationNotFoundException("Destination location was not found.",
								request.destinations[row]);
				}
			}

			row = col = 0;
			for (JsonNode rows : rootNode.get("rows"))
			{
				for (JsonNode element : rows.get("elements"))
				{
					status = element.get("status");
					if (status == null || !status.isTextual())
						throw new IllegalArgumentException("The JSON file is unvalid. (Error with the status value)");
					statusValue = status.asText();

					if (statusValue.equals("OK"))
					{
						distance = element.get("distance").get("value");
						duration = element.get("duration").get("value");
						if (distance == null || duration == null || !distance.isInt() || !duration.isInt())
							throw new IllegalArgumentException(
									"The JSON file is unvalid. (Error with the distance/duration values.)");
						distanceValue = distance.asInt();
						durationValue = duration.asInt();
						matrix.setDistance(request.origins[row], request.destinations[col], distanceValue);
						matrix.setDuration(request.origins[row], request.destinations[col], durationValue);
					}
					else if (statusValue.equals("ZERO_RESULTS"))
					{
						matrix.setDistance(request.origins[row], request.destinations[col], DataMatrix.NO_WAY);
						matrix.setDuration(request.origins[row], request.destinations[col], DataMatrix.NO_WAY);
					}
					else if (statusValue.equals("NOT_FOUND"))
						throw new LocationNotFoundException("One location was not found by google.");
					else
						throw new IllegalArgumentException("The status code in the JSON file is unknown.");
					col++;
				}
				row++;
				if (col != request.destinations.length)
					throw new IllegalArgumentException(
							"The JSON file is unvalid. (The number of elements in the file are incoherent with the number of locations.)");
				col = 0;
			}
		}
		catch (JsonParseException e)
		{
			throw new IllegalArgumentException("Error with the JSON file.");
		}
		catch (JsonMappingException e)
		{
			throw new IllegalArgumentException("Error with the JSON file.");
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Error with the JSON file.");
		}
	}

	/**
	 * Save the response into a file and return the name of the file
	 * @param uc the {@link URLConnection} to save
	 * @return the name of the file where data are saved
	 * @throws IOException if an error occurred with the file where saved data
	 */
	protected String saveResponse(URLConnection uc) throws IOException
	{
		FileWriter out;
		String line, fileName;
		BufferedReader in;

		// We open the connection
		try
		{
			in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			// We create the file where we'll save the answer
			fileName = getFileName();
			out = new FileWriter(fileName);

			while ((line = in.readLine()) != null)
			{
				out.write(String.format("%s\n", line));
			}
			in.close();
			out.close();
			return fileName;
		}
		catch (IOException e)
		{
			throw new IOException("An error occured while saving the google answer in a file.", e);
		}
	}

	/** 
	 *  Generate the folders where store the results of google distances
	 */
	protected static void generateFolders()
	{
		File directory;
		String directoryName;

		directoryName = directory_formatter.format(new Date());

		directory = new File(SAVES_DIRECTORY + "/" + directoryName);

		if (!(directory.exists() && directory.isDirectory()))
		{
			directory.mkdirs();
		}
	}

	/** 
	 * Return the name of the file where to save data
	 * @return the name of the file where to save data
	 */
	protected String getFileName()
	{
		return SAVES_DIRECTORY + "/" + directory_formatter.format(new Date()) + "/"
				+ file_formatter.format(new Date());
	}

	/**
	 * Fill a data matrix from files
	 * @param matrix The matrix to fill
	 * @param busScheduling Used for fire progression events
	 * @param fileNames File names
	 * @param subscription true if a subscription is available, false else
	 * @throws Exception 
	 */
	public static void fillMatrix(DataMatrix matrix, BusScheduling busScheduling, String fileNames[], boolean subscription)
			throws Exception
	{
		if (matrix == null)
			throw new NullPointerException(
					"You have to give a Data Matrix to the parser in order to fill it while parsing.");
		GoogleDistances gd = new GoogleDistances(matrix, busScheduling, subscription);
		gd.fill(fileNames);
	}

	/**
	 * Fill the matrix from files
	 * @param fileNames Files to read
	 * @throws Exception 
	 */
	public void fill(String[] fileNames) throws Exception
	{
		DistanceRequest[] requests = createRequests();
		int i = 0;
		for (String name : fileNames)
		{
			parse(new FileInputStream(new File(name)), requests[i]);
			i++;
		}
	}

}
