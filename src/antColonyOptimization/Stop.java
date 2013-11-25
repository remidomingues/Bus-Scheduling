/**
 * File : Stop.java
 *
 * Created on May 22, 2012, 10:37:22 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package antColonyOptimization;

import locationsData.Location;

/** 
 * Contains commons data about a bus stop
 */
public class Stop
{
	/** The minimum hour authorized for the different time constraints */
	protected static final int MAX_HOUR = 24 * 3600 - 1;

	/** The minimum hour authorized for the different time constraints */
	protected static final int MIN_HOUR = 0 * 3600;

	/** The location where the bus stop */
	protected Location location;

	/** Time when the bus arrived at the location */
	protected int arrivalTime = 0;

	/**
	 * Full constructor
	 * @param location the location of the bus stop
	 * @param arrivalTime the time the bus needs to stay at the location
	 */
	public Stop(Location location, int arrivalTime)
	{
		this.location = location;
		setArrivalTime(arrivalTime);
	}

	/**
	 * Copy constructor
	 * @param stop the bus stop to copy
	 */
	public Stop(Stop stop)
	{
		this(stop.getLocation(), stop.getArrivalTime());
	}

	/** 
	 * Return the location 
	 * @return the location 
	 */
	public Location getLocation()
	{
		return location;
	}

	/** 
	 * Set the location
	 * @param location The location to set
	 */
	protected void setLocation(Location location)
	{
		if (location == null)
			throw new NullPointerException("The location can't be null on a stop.");

		this.location = location;
	}

	/** 
	 * Return the bus arrival time
	 * @return The bus arrival time
	 */
	public int getArrivalTime()
	{
		return arrivalTime;
	}

	/** 
	 * Set the bus arrival time
	 * @param arrivalTime the bus arrival time
	 */
	public void setArrivalTime(int arrivalTime)
	{
		controlTime(arrivalTime);
		this.arrivalTime = arrivalTime;
	}

	/**
	 * Control if the given time is a correct value, if it is not it sends an exception
	 * @param time the time to control
	 * @throws IllegalArgumentException if the time is incorrect
	 */
	protected void controlTime(int time) throws IllegalArgumentException
	{
		// if (time < MIN_HOUR || time > MAX_HOUR)
		// throw new IllegalArgumentException("A time must be between " +
		// MIN_HOUR + " and " + MAX_HOUR
		// + " seconds");
	}

	/** 
	 * Clone this object
	 * @return the object clone
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Stop clone()
	{
		return new Stop(getLocation(), getArrivalTime());
	}

	/** 
	 * @return s string describing the object
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder().append(getArrivalTime() / 3600).append(":")
				.append((getArrivalTime() / 60) % 60);
		return sb.toString();
	}

	/**
	 * Represent a driver swap stop
	 */
	static class DriverSwapStop extends Stop
	{
		/** Represent the time difference of the driver swap */
		int timeDifference;

		/** 
		 * Full constructor 
		 * @param location the location where the drivers swap
		 * @param arrivalTime the time where they swap
		 * @param timeDiff the difference time between the real swap time and the schedule time
		 */
		public DriverSwapStop(Location location, int arrivalTime, int timeDiff)
		{
			super(location, arrivalTime);
			this.timeDifference = timeDiff;
		}

		/**
		 *  Return the time between the real swap time and the schedule time
		 * @return The  time between the real swap time and the schedule time
		 */
		public int getTimeDifference()
		{
			return timeDifference;
		}

		/** 
		 * @return a clone of this object
		 * @see antColonyOptimization.Stop#clone()
		 */
		@Override
		public Stop clone()
		{
			return new DriverSwapStop(getLocation(), getArrivalTime(), getTimeDifference());
		}
	}

}
