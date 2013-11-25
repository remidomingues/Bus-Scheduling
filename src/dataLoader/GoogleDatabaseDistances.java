/**
 * File : GoogleDatabaseDistances.java
 *
 * Created on May 29, 2012, 12:45:41 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package dataLoader;

import java.io.IOException;
import java.sql.SQLException;
import application.BusScheduling;
import application.BusSchedulingException;

import locationsData.DataMatrix;
import locationsData.Location;
import presentation.ProgressionEvent.ProgressionType;

/**
 * Service class uses to fill a distance matrix using the Google Distances Matrix API
 */
public class GoogleDatabaseDistances extends GoogleDistances
{
	/**
	 * Constructor
	 * @param matrix Matrix to fill
	 * @param busScheduling BusScheduling used to fire progression events
	 * @param subscription true if a Google Business subscription is owned, false else
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 * @throws BusSchedulingException 
	 */
	private GoogleDatabaseDistances(DataMatrix matrix, BusScheduling busScheduling, boolean subscription)
			throws NullPointerException, IllegalArgumentException, BusSchedulingException
	{
		super(matrix, busScheduling, subscription);
	}
	
	/**
	 * Fill a distance matrix given in parameter by asking google distances between each
	 * points it contains
	 * @param matrix The matrix to fill
	 * @param busScheduling BusScheduling used to fire progression events
	 * @param subscription true if a Google Business subscription is owned, false else
	 * @throws SQLException 
	 * @throws OverQueryLimitException
	 * @throws LocationNotFoundException
	 * @throws Exception 
	 */
	public static void fillMatrix(DataMatrix matrix, BusScheduling busScheduling, boolean subscription)
			throws SQLException, OverQueryLimitException, LocationNotFoundException, Exception
	{
		if (matrix == null)
			throw new NullPointerException(
					"You have to give a Data Matrix to the parser in order to fill it while parsing.");
		GoogleDatabaseDistances gdd = new GoogleDatabaseDistances(matrix, busScheduling, subscription);
		gdd.fill();
	}
	
	/**
	 * Fill the matrix in attribute
	 * @throws IllegalArgumentException
	 * @throws SQLException
	 * @throws OverQueryLimitException
	 * @throws LocationNotFoundException
	 * @throws Exception 
	 * @see dataLoader.GoogleDistances#fill()
	 */
	public void fill() throws IllegalArgumentException, SQLException, OverQueryLimitException, LocationNotFoundException, Exception
	{
		int cpt = 1, percent = 0, tmpPercent;
		DatabaseManager databaseManager = busScheduling.getDatabaseManager();
		// First we cut the locations to create the different requests
		DistanceRequest request;
		boolean retry = false;
		int requestCpt = 0;
//		long startRequestsTime = new Date().getTime(), elapsedTime;
		Location[] origins = new Location[1], destinations = new Location[1];
		// Then for each request we ask google and parse the result
		for (Location src : matrix.getLocations())
		{
			origins[0] = src;
			for(Location dest : matrix.getLocations())
			{
				if(!databaseManager.existPathData(src, dest))
				{
					if(src == dest)
					{
						databaseManager.insertPathData(src, dest, 0, 0);
						matrix.setDistance(src, dest, 0);
						matrix.setDuration(src, dest, 0);
					}
					else
					{
						destinations[0] = dest;
						++requestCpt;
						do
						{
							try
							{
								request = new DistanceRequest(origins, destinations);
								parse(connect(request).getInputStream(), request);
								databaseManager.insertPathData(src, dest, matrix.getDistance(src, dest), matrix.getDuration(src, dest));
								retry = false;
							}
							catch (IOException e)
							{
								if(retry)
									throw new IOException("I/O error occured while trying to get data", e);
								retry = true;
								Thread.sleep(TIME_BETWEEN_REQUEST);
							}
							catch (OverQueryLimitException e)
							{
								if(retry)
									throw e;
								retry = true;
								Thread.sleep(TIME_BETWEEN_REQUEST);
							}
						}
						while (retry);
					}
				}
				else
				{
					matrix.setDistance(src, dest, databaseManager.getDistance(src.getId(), dest.getId()));
					matrix.setDuration(src, dest, databaseManager.getDuration(src.getId(), dest.getId()));
				}
			}
			// we wait the amount of time needed before sending a new request
			tmpPercent = 42 * cpt / matrix.getLocations().size();
			if (tmpPercent != percent)
			{
				busScheduling.fireEvent(this, ProgressionType.INCREMENT, tmpPercent - percent);
				percent = tmpPercent;
			}
			++cpt;
		}
		System.out.print(" (" + requestCpt + " pedidos efectuados) ");
	}
}
