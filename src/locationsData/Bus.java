/**
 * File : Bus.java
 *
 * Created on May 7, 2012, 9:23:22 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package locationsData;


/**
 * Stock class dedicated to bus places numbers
 */
public class Bus
{
	/** Bus identifiant */
	private String id;

	/** Bus drivers */
	private String drivers;

	/** Telephone */
	private String phone;

	/** Number of wheel chairs seats */
	private int wheelChairsSeats;

	/** Number of non wheel chairs seats */
	private int nonWheelChairsSeats;

	/** The location from where the bus start and where he goes back */
	private Location origin;

	/** Location where the bus goes in order to change its driver */
	private Location driverSwap;

	/** The earlier hour the bus can start his travel */
	private int min_bus_start_time = 5 * 3600;

	/**
	 * Constructor
	 * @param id Identifiant
	 * @param drivers Drivers
	 * @param phone Telephone
	 * @param wheelChairsSeats Number of wheel chairs seats
	 * @param nonWheelChairsSeats Number of non wheel chairs seats
	 * @param origin Bus origin location
	 * @param driverSwap Bus driver swap location
	 * @param minBusStartHour Bus minimum start hour
	 */
	public Bus(String id, String drivers, String phone, int wheelChairsSeats, int nonWheelChairsSeats,
			Location origin, Location driverSwap, int minBusStartHour)
	{
		this.id = id;
		this.drivers = drivers;
		this.phone = phone;
		this.wheelChairsSeats = wheelChairsSeats;
		this.nonWheelChairsSeats = nonWheelChairsSeats;
		if (minBusStartHour != -1)
		{
			this.min_bus_start_time = minBusStartHour;
		}
		this.driverSwap = driverSwap;
		this.origin = origin;
	}

	/**
	 * Return the wheelChairsSeats value
	 * @return The wheelChairsSeats value
	 */
	public int getWheelChairsSeats()
	{
		return wheelChairsSeats;
	}

	/**
	 * Return the nonWheelChairsSeats value
	 * @return The nonWheelChairsSeats value
	 */
	public int getNonWheelChairsSeats()
	{
		return nonWheelChairsSeats;
	}

	/**
	 * Return the id value
	 * @return The id value
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Return the drivers value
	 * @return The drivers value
	 */
	public String getDrivers()
	{
		return drivers;
	}

	/**
	 * Return the phone value
	 * @return The phone value
	 */
	public String getPhone()
	{
		return phone;
	}

	/** Return the origin value
	 * @return The origin value
	 */
	public Location getOrigin()
	{
		return origin;
	}

	/**
	 * Return the driverSwap value
	 * @return The driverSwap value
	 */
	public Location getDriverSwap()
	{
		return driverSwap;
	}

	/** Update the origin value
	 * @param origin The origin value
	 */
	public void setOrigin(Location origin)
	{
		this.origin = origin;
	}

	/** Return the min_bus_start_time value
	 * @return The min_bus_start_time value
	 */
	public int getMinBusStartTime()
	{
		return min_bus_start_time;
	}

	/** Update the min_bus_start_time value
	 * @param min_bus_start_time The min_bus_start_time value
	 */
	public void setMinBusStartTime(int min_bus_start_time)
	{
		this.min_bus_start_time = min_bus_start_time;
	}
}