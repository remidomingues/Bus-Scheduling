package antColonyOptimization;

import java.util.ArrayList;
import java.util.List;

import locationsData.Bus;
import locationsData.DataMatrix;
import locationsData.Location;
import locationsData.Path;
import locationsData.TrafficInformation;
import application.BusSchedulingException;

/**
 * Contains the data used by the ant system and the ants in order to find a path
 * as short as possible.
 */
public class SolverData
{
	/** Time necessary to change the bus driver */
	protected static int DRIVER_SWAP_TIME = 5 * 60;

	/** Time requested for a driver to eat */
	protected static int DINNER_TIME = 45 * 60;

	/** Time requested for a driver to take its coffee pause */
	protected static int COFFEE_TIME = 30 * 60;

	/** Number of hour a driver work per day. Necessary in order to calculate the driver swap hour */
	protected static int DRIVER_WORK_TIME = 8 * 3600 + 20 * 60;

	/** Time, in seconds, needed to take or deposit somebody in wheel chair */
	protected static int WHEELCHAIR_ADDITIONAL_TIME = 60 * 5;

	/** Time, in seconds, needed to take or deposit somebody without wheel chair */
	protected static int BASIC_ADDITIONAL_TIME = 60 * 4;

	/** The coefficient used to set automatically the time frame. 
	 * In fact the minimal time is set to the duration between the origin 
	 * and the destination multiplied by this coefficient. */
	protected static int TIME_FRAME_COEFFICIENT = 5;
	
	/** Matrix containing paths weight, used in order to choose paths */
	protected DataMatrix distancesMatrix;

	/** List of the paths that must be visited */
	protected List<Path> paths;

	/** */
	protected TrafficInformation traffic;

	/** The bus used for the route */
	protected List<Bus> bus;

	/**
	 * Full Constructor
	 * @param matrix The distance matrix containing paths weight
	 * @param paths the paths 
	 * @param bus the bus used for the route
	 * @param traffic the class containing information about traffic
	 * @throws BusSchedulingException 
	 */
	public SolverData(DataMatrix matrix, List<Path> paths, List<Bus> bus, TrafficInformation traffic)
			throws BusSchedulingException
	{
		this.distancesMatrix = matrix;
		this.setBus(bus);
		this.traffic = traffic;
		setPaths(paths);
	}

	/**
	 * Minimal constructor
	 * @param matrix The distance matrix containing paths weight
	 * @param bus the bus used for the route
	 * @throws BusSchedulingException 
	 */
	public SolverData(DataMatrix matrix, List<Bus> bus) throws BusSchedulingException
	{
		this(matrix, new ArrayList<Path>(), bus, new TrafficInformation());
	}

	/**
	 * Return the data matrix
	 * @return the data matrix used by this solver
	 */
	public DataMatrix getDataMatrix()
	{
		return distancesMatrix;
	}

	/**
	 * Return the value of the pheromones at the specified indexes
	 * @param origin Pheromones matrix lines
	 * @param destination Pheromones matrix columns
	 * @return the pheromones value
	 */
	public double getPheromones(Location origin, Location destination)
	{
		return distancesMatrix.getPheromones(origin, destination);
	}

	/**
	 * Return the weight of the path at the specified indexes
	 * @param origin  Distances matrix lines
	 * @param destination Distances matrix columns
	 * @return the weight value
	 */
	public int getDuration(Location origin, Location destination)
	{
		return distancesMatrix.getDuration(origin, destination);
	}

	/**
	 * Return the list of locations used by the matrix
	 * @return the list of locations 
	 */
	public List<Location> getLocations()
	{
		return distancesMatrix.getLocations();
	}

	/**
	 * Add pheromones to the specified pheromones indexes
	 * @param origin Pheromones matrix lines
	 * @param destination Pheromones matrix columns
	 * @param pheromone The new pheromones value
	 */
	public void addPheromones(Location origin, Location destination, double pheromone)
	{
		distancesMatrix.addPheromones(origin, destination, pheromone);
	}

	/**
	 * Set a new pheromones value to the specified pheromones indexes
	 * @param origin Pheromones matrix lines
	 * @param destination Pheromones matrix columns
	 * @param pheromones The new pheromones value
	 */
	public void setPheromones(Location origin, Location destination, double pheromones)
	{
		distancesMatrix.setPheromones(origin, destination, pheromones);
	}

	/**
	 * Return the number of locations to visit
	 * @return the number of locations to visit
	 */
	public int getCountLocations()
	{
		return distancesMatrix.getMatrixSize();
	}

	/**
	 * Return the additionalTimePerStop value
	 * @return The additionalTimePerStop value
	 */
	public static int getWheelchairTime()
	{
		return WHEELCHAIR_ADDITIONAL_TIME;
	}

	/**
	 * Update the additionalTimePerStop value
	 * @param additionalTimePerStop The additionalTimePerStop value
	 * @throws BusSchedulingException 
	 */
	public static void setWheelchairAdditionalTime(int additionalTimePerStop) throws BusSchedulingException
	{
		if (additionalTimePerStop >= 0)
		{
			WHEELCHAIR_ADDITIONAL_TIME = additionalTimePerStop;
		}
		else
			throw new BusSchedulingException(
					"O tempo necessário para tomar uma pessoa com um cadeirante deve ser mais alto ou igual a 0.");
	}

	/**
	 *  Return the paths list
	 * @return The paths list
	 */
	public List<Path> getPaths()
	{
		return paths;
	}

	/**
	 * Set the paths list
	 * @param paths the path list to set
	 */
	public void setPaths(List<Path> paths)
	{
		this.paths = paths;
	}

	/**
	 * Return the time, in seconds, need to take or deposit a person that is not in wheelchair
	 * @return  the time, in seconds, need to take or deposit a person that is not in wheelchair
	 */
	public static int getBasicAdditionalTime()
	{
		return BASIC_ADDITIONAL_TIME;
	}

	/**
	 * Set the time, in seconds, need to take or deposit a person that is not in wheelchair
	 * @param basicAdditionalTime the time, in seconds, need to take or deposit a person that is not in wheelchair
	 * @throws IllegalArgumentException if the additional time is less than 0
	 */
	public static void setBasicAdditionalTime(int basicAdditionalTime) throws IllegalArgumentException
	{
		if (basicAdditionalTime >= 0)
		{
			BASIC_ADDITIONAL_TIME = basicAdditionalTime;
		}
		else
			throw new IllegalArgumentException(
					"O tempo necessãrio para tomar uma pessoa sem cadeirante deve ser mais alto ou igual a 0.");
	}

	/**
	 * Return the DRIVER_SWAP_TIME value
	 * @return The DRIVER_SWAP_TIME value
	 */
	public static int getDriverSwapTime()
	{
		return DRIVER_SWAP_TIME;
	}

	/**
	 * Update the DRIVER_SWAP_TIME value
	 * @param time The DRIVER_SWAP_TIME value
	 */
	public static void setDriverSwapTime(int time)
	{
		if (time >= 0)
			DRIVER_SWAP_TIME = time;
		else
			throw new IllegalArgumentException(
					"O tempo necessário para fazer o troca de motorista deve ser mais alto ou igual a 0.");
	}

	/**
	 * Return the dINNER_TIME value
	 * @return The dINNER_TIME value
	 */
	public static int getDinnerTime()
	{
		return DINNER_TIME;
	}

	/**
	 * Update the dINNER_TIME value
	 * @param time The dINNER_TIME value
	 */
	public static void setDinnerTime(int time)
	{
		if (time >= 0)
			DINNER_TIME = time;
		else
			throw new IllegalArgumentException("O tempo de janta deve ser mais alto ou igual a 0.");
	}

	/**
	 * Return the COFFEE_TIME value
	 * @return The COFFEE_TIME value
	 */
	public static int getCoffeeTime()
	{
		return COFFEE_TIME;
	}

	/**
	 * Update the COFFEE_TIME value
	 * @param time The COFFEE_TIME value
	 */
	public static void setCoffeeTime(int time)
	{
		if (time >= 0)
			COFFEE_TIME = time;
		else
			throw new IllegalArgumentException("O tempo de cafe deve ser mais alto ou igual a 0.");
	}

	/**
	 * Return the DRIVER_WORK_TIME value
	 * @return The DRIVER_WORK_TIME value
	 */
	public static int getDriverWorkTime()
	{
		return DRIVER_WORK_TIME;
	}

	/**
	 * Update the DRIVER_WORK_TIME value
	 * @param time The DRIVER_WORK_TIME value
	 */
	public static void setDriverWorkTime(int time)
	{
		if (time >= 0)
			DRIVER_WORK_TIME = time;
		else
			throw new IllegalArgumentException(
					"O número de horas de trabalho por dia por motorista deve ser mais alto ou igual a 0.");
	}

	/** 
	 * Set the traffic information attribute 
	 * @param traffic the traffic information attribute
	 */
	public void setTraffic(TrafficInformation traffic)
	{
		this.traffic = traffic;
	}

	/**
	 * Return the traffic coefficient incidence for a given hour
	 * @param hour the given hour
	 * @return the traffic coefficient for the given hour
	 */
	public float getTrafficCoefficient(int hour)
	{
		return traffic.getCoefficient(hour);
	}

	/** 
	 * Return the list of bus used by the solver
	 * @return  the list of bus used by the solver
	 */
	public List<Bus> getAllBus()
	{
		return bus;
	}

	/**
	 * Return the bus at the given index
	 * @param index the index of the bus
	 * @return the bus 
	 */
	public Bus getBus(int index){
		return bus.get(index);
	}
	
	/**
	 *  Set the bus list
	 * @param bus the bus list to set
	 */
	protected void setBus(List<Bus> bus)
	{
		this.bus = bus;
	}

}
