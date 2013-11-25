package locationsData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stock the data about the points and the distances between them
 */
public class DataMatrix
{
	/** Value use when the distance between two points is unknown */
	public static final int UNKNOW_VALUE = -1;

	/** Value use when there is no way between two points of the matrix. */
	public static final int NO_WAY = Integer.MAX_VALUE;
	// in fact we use the maximum value instead of a special constant in order
	// to simplify the ant work

	/** The list of the different locations */
	protected List<Location> locations;

	/** Map of data containing the duration and the distance */
	protected Map<Location, Map<Location, Data>> data = new HashMap<Location, Map<Location, Data>>();

	/** Internal class that store all information about the path between 2 locations */
	protected class Data
	{
		/** Distance between the 2 locations */
		public int distance = UNKNOW_VALUE;
		/** Duration between 2 locations */
		public int duration = UNKNOW_VALUE;
		/** Pheromones between two locations */
		public double pheromones = UNKNOW_VALUE;
	}

	/**
	 * Constructor by array
	 * @param locations the array containing the locations
	 * @throws NullPointerException if the location array is null or contains null locations
	 */
	public DataMatrix(Location locations[]) throws NullPointerException
	{
		this(Arrays.asList(locations));
	}

	/**
	 * Constructor by list
	 * @param locations the list containing the locations
	 * @throws NullPointerException if the location list is null or contains null locations
	 */
	public DataMatrix(List<Location> locations) throws NullPointerException
	{
		if (locations == null)
			throw new NullPointerException("You can't give a null location list to the matrix.");
		this.locations = new ArrayList<Location>(locations);
		// for each origin locations
		for (Location origin : locations)
		{
			// we test if the location is not null
			if (origin == null)
				throw new NullPointerException("You can't give a null location to the matrix.");
			// for each destination we create a map with a new Data
			Map<Location, Data> tmp = new HashMap<Location, Data>();
			for (Location destination : locations)
			{
				tmp.put(destination, new Data());
			}
			// then we add it to the global map
			data.put(origin, tmp);
		}
		// consequence => we build a double map where the first access is the
		// origin, the second is the destination and finally the result is the
		// data
	}

	/**
	 * Getter for the locations
	 * @return a list containing all the locations
	 */
	public List<Location> getLocations()
	{
		return locations;
	}

	/**
	 * Return the distance between 2 points of the matrix
	 * @param origin Origin point
	 * @param destination Destination point
	 * @return the distance between the two points
	 */
	public int getDistance(Location origin, Location destination)
	{
		return data.get(origin).get(destination).distance;
	}

	/**
	 * Return the duration between 2 points of the matrix
	 * @param origin index of the origin point
	 * @param destination index of the destination point
	 * @return the duration between the two points
	 */
	public int getDuration(Location origin, Location destination)
	{
		return data.get(origin).get(destination).duration;
	}

	/**
	 * Return the pheromone between 2 points of the matrix
	 * @param origin index of the origin point
	 * @param destination index of the destination point
	 * @return the pheromone between the two points
	 */
	public double getPheromones(Location origin, Location destination)
	{
		return data.get(origin).get(destination).pheromones;
	}

	/**
	 * Setter for a distance between two locations.
	 * @param origin  the origin point
	 * @param destination the destination point
	 * @param distance distance between the 2 points
	 * @throws IllegalArgumentException if one or both locations are unknown 
	 */
	public void setDistance(Location origin, Location destination, int distance)
			throws IllegalArgumentException
	{
		data.get(origin).get(destination).distance = distance;
	}

	/**
	 * Setter for a duration between two locations.
	 * @param origin the origin point
	 * @param destination the destination point
	 * @param duration duration between the 2 points
	 * @throws IllegalArgumentException if one or both locations are unknown s=
	 */
	public void setDuration(Location origin, Location destination, int duration)
			throws IllegalArgumentException
	{
		data.get(origin).get(destination).duration = duration;
	}

	/**
	 * Setter for a pheromone between two locations.
	 * NB: arguments values are not controlled (in order to save time). So be sure to give good values.
	 * @param origin  the origin point
	 * @param destination the destination point
	 * @param pheromone pheromone between the 2 points
	 */
	public void setPheromones(Location origin, Location destination, double pheromone)
	{
		data.get(origin).get(destination).pheromones = pheromone;
	}

	/**
	 * Add pheromones between two locations.
	 * NB: arguments values are not controlled (in order to save time). So be sure to give good values.
	 * @param origin  the origin point
	 * @param destination the destination point
	 * @param pheromone pheromone between the 2 points
	 */
	public void addPheromones(Location origin, Location destination, double pheromone)
	{
		data.get(origin).get(destination).pheromones += pheromone;
	}

	/**
	 * Control if given locations are in the map, and if they don't it sends an {@link IllegalArgumentException}.
	 * @param origin the origin location
	 * @param destination the destination location
	 * @throws IllegalArgumentException if one or both locations are unknown 
	 */
	protected void controlLocations(Location origin, Location destination) throws IllegalArgumentException
	{
		if (data.get(origin) == null || data.get(origin).get(destination) == null)
			throw new IllegalArgumentException("Locations given in parameter are unknown.");
	}

	/**
	 * Set all the pheromones between two point to the given value
	 * @param value pheromone value
	 */
	public void fillPheromones(double value)
	{
		for (Location origin : locations)
		{
			for (Location destination : locations)
			{
				setPheromones(origin, destination, value);
			}
		}
	}

	/**
	 * Return the matrix size
	 * @return the matrix size
	 */
	public int getMatrixSize()
	{
		return locations.size();
	}

	/** 
	 * Return a human readable string describing the data matrix
	 * @return  a human readable string describing the data matrix
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("---------------Distances-----------------\n");
		for (Location origin : locations)
		{
			for (Location destination : locations)
			{
				sb.append(getDuration(origin, destination) + "\t");
			}
			sb.append("\n");
		}
		sb.append("---------------Pheromones-----------------\n");
		for (Location origin : locations)
		{
			for (Location destination : locations)
			{
				sb.append(getPheromones(origin, destination) + "\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
