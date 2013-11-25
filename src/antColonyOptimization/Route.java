/**
 * File : NewRoute.java
 *
 * Created on May 24, 2012, 11:18:26 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package antColonyOptimization;

import java.util.ArrayList;
import java.util.List;

import locationsData.Location;
import locationsData.Person;
import antColonyOptimization.MinMaxAntSystem.SwapConductorPath;
import antColonyOptimization.Stop.DriverSwapStop;
import antColonyOptimization.TransportStop.DepositStop;
import antColonyOptimization.TransportStop.PickupStop;

/**
 *  Contains an ordered list of the locations that the bus done,
 *  and all informations link to this bus route.
 */
public class Route
{
	/** Ordered list of the stops */
	protected List<Stop> orderedStops;

	/** Total advance of the route */
	protected int totalAdvance = 0;

	/** Total excessive advance of the route */
	protected int totalExcessiveAdvance = 0;

	/** Total delay of the route */
	protected int totalDelay = 0;

	/** Total distance of the route */
	protected int totalDistance = 0;

	/** Total time spent in the bus by all the users */
	protected int totalUserTime = 0;

	/**
	 * Full constructor 
	 * @param size the number of stops expected.
	 *  This size doesn't limit the capacity of this class, but a good value improves performances.
	 */
	public Route(int size)
	{
		orderedStops = new ArrayList<Stop>(size);
	}

	/**
	 * Minimal constructor. 
	 * Equivalent to call new Route(30).
	 */
	public Route()
	{
		this(30);
	}

	/**
	 * Constructor by copy
	 * @param route the route to copy
	 */
	public Route(Route route)
	{
		this.totalAdvance = route.getTotalAdvance();
		this.totalDelay = route.getTotalDelay();
		this.totalDistance = route.getTotalDistance();
		this.totalUserTime = route.getTotalUserTime();

		this.orderedStops = new ArrayList<Stop>(route.getCountStops());
		for (Stop stop : route.orderedStops)
		{
			this.orderedStops.add(stop.clone());
		}
	}

	/** 
	 * Return the number of person transported in this route
	 * @return the number of person transported in this route
	 */
	public int getCountTransportedPerson()
	{
		int sum = 0;
		for (Stop stop : orderedStops)
		{
			if (stop instanceof DepositStop)
				sum++;
		}
		return sum;
	}

	/** 
	 * Return the location of the given index
	 * @param index the index of the location needed
	 * @return the location of the given index
	 */
	public Location getLocation(int index)
	{
		return orderedStops.get(index).getLocation();
	}

	/** 
	 * Return the number of stops in the route
	 * @return the number of stops in the route
	 */
	public int getCountStops()
	{
		return orderedStops.size();
	}

	/** 
	 * Return the total duration of the route
	 * @return  the total duration of the route
	 */
	public int getTotalDuration()
	{
		return orderedStops.get(orderedStops.size() - 1).getArrivalTime()
				- orderedStops.get(0).getArrivalTime();
	}

	/** 
	 * Return the person linked of the given stop index
	 * @param index the index 
	 * @return the person linked of the given stop index
	 */
	public Person getPerson(int index)
	{
		if (orderedStops.get(index) instanceof TransportStop)
		{
			return ((TransportStop) orderedStops.get(index)).getPerson();
		}
		return null;
	}

	/** 
	 * Return the delay of the given index stop
	 * @param index the stop index
	 * @return  the delay of the given index stop
	 */
	public int getDelay(int index)
	{
		if (isDepositStop(index))
		{
			return ((DepositStop) orderedStops.get(index)).getDelay();
		}
		if (isDriverSwapStop(index))
		{
			return ((DriverSwapStop) orderedStops.get(index)).getTimeDifference();
		}
		return 0;
	}

	/** 
	 * Return the total delay of the route
	 * @return the total delay of the route
	 */

	public int getTotalDelay()
	{
		return totalDelay;
	}

	/** 
	 * Return the arrival hour of the given stop index
	 * @param index the given stop index
	 * @return the arrival hour of the given stop index
	 */
	public int getArrivalHour(int index)
	{
		return orderedStops.get(index).getArrivalTime();
	}

	/** 
	 * Return the total distance of this route
	 * @return the total distance of this route
	 */
	public int getTotalDistance()
	{
		return totalDistance;
	}

	/** 
	 * Add a distance to the total path distance
	 * @param distance the distance to add
	 */
	public void addPathDistance(long distance)
	{
		totalDistance += distance;
	}

	/** 
	 * Return the total time spent in the bus by all the users
	 * @return the total time spent in the bus by all the users
	 */
	public int getTotalUserTime()
	{
		return totalUserTime;
	}

	/** 
	 * Add a time to the total user time
	 * @param timeInBus time to add 
	 */
	public void addTotalUserTime(int timeInBus)
	{
		this.totalUserTime += timeInBus;

	}

	/** 
	 * Set the bus start time 
	 * @param busStartTime the bus start time
	 */
	public void setBusStartTime(int busStartTime)
	{
		orderedStops.get(0).setArrivalTime(busStartTime);
	}

	/** 
	 * Add a location to this route
	 * @param location the location to add
	 * @param hour the hour where the bus arrives at the location
	 */
	public void addLocation(Location location, int hour)
	{
		orderedStops.add(new Stop(location, hour));

	}

	/** 
	 * Add an origin (take a person) using a  path
	 * @param path the path use to add the stop 
	 */
	protected void addOrigin(BusPath path)
	{
		orderedStops.add(new PickupStop(path.getOrigin(), path.getPerson(), path.getStartTakenTime(), path
				.getPickupTime()));
	}

	/** 
	 * Add a destination ( = deposit a person) using a path
	 * @param path the path used to add a stop to this route
	 */
	public void addDestination(BusPath path)
	{
		orderedStops.add(new DepositStop(path.getDestination(), path.getPerson(), path.getDepositTime(), path
				.getWishedDepositTime()));
		addDelay(path.getDelay());
		addAdvance(path.getAdvance());
		addTotalUserTime(path.getDuration());
		addExcessiveAdvance(path.getExcessiveAdvance());
	}

	/**
	 * Add a driver swap stop to this route using a path
	 * @param path the path used to add a the driver swap stop
	 */
	protected void addDriverSwap(BusPath path)
	{
		orderedStops.add(new DriverSwapStop(path.getDestination(), path.getDepositTime(), path.getDelay()));
		addDelay(Math.max(path.getDelay(), 0));
		addAdvance(path.getAdvance());
		addTotalUserTime(path.getDuration());
		addExcessiveAdvance(path.getExcessiveAdvance());
	}

	/**
	 * Add a stop to the stop list and move the dynamic path given using the given hour
	 * @param path the path used to add a stop
	 * @param hour the hour where the bus is at the stop
	 */
	public void addStop(DynamicPath path, int hour)
	{
		path.move(hour);
		if (path instanceof SwapConductorPath)
		{
			addDriverSwap(path);
		}
		else if (path.isGoingDestination())
		{
			addOrigin(path);
		}
		else if (path.isFinished())
		{
			addDestination(path);
		}
	}

	/** 
	 * Add advance to the total advance
	 * @param advance  the advance to add
	 */
	public void addAdvance(int advance)
	{
		totalAdvance += advance;
	}

	/**
	 * Add advance to the total excessive advance
	 * @param advance the advance to add
	 */
	public void addExcessiveAdvance(int advance)
	{
		totalExcessiveAdvance += advance;
	}

	/** 
	 * Return the last location visited by the bus
	 * @return the last location visited by the bus
	 */
	public Location getLastVisitedLocation()
	{
		return orderedStops.get(orderedStops.size() - 1).getLocation();
	}

	/** 
	 * Return the total advance of the route
	 * @return the total advance of the route
	 */
	public int getTotalAdvance()
	{
		return totalAdvance;
	}

	/** 
	 * Return the total excessive advance of the route
	 * @return the total excessive advance of this route
	 */
	public int getTotalExceedAdvance()
	{
		return totalExcessiveAdvance;
	}
	
	/**
	 * Return the destination of the specify stop if the given stop index concerns a pick upStop
	 * @param index the index of the pick up stop
	 * @return the location of the destination for the deposit stop or null if bad index given
	 */
	public Location getDestination(int index){
		Person origin = getPerson(index);
		for(int j = index ; j < getCountStops(); j++){
			if(isDepositStop(j) && getPerson(index).equals(origin))
				return getLocation(j);
		}
		return null;
	}
	

	/**
	 * Update the delay value
	 * @param delay The delay value
	 */
	protected void addDelay(int delay)
	{
		this.totalDelay += delay;
	}

	/**
	 * Return the stop at the given index
	 * @param index the stop idnex
	 * @return the stop 
	 */
	protected Stop getStop(int index)
	{
		return orderedStops.get(index);
	}

	/**
	 * Says if the stop at the given address is a pick up stop
	 * @param index the index of the stop
	 * @return <code>true</code> if it is a pick up stop, <code>false</code> otherwise
	 */
	public boolean isPickupStop(int index)
	{
		return getStop(index) instanceof PickupStop;
	}

	/**
	 * Says if the stop at the given address is a deposit stop
	 * @param index the index of the stop
	 * @return <code>true</code> if it is a deposit stop, <code>false</code> otherwise
	 */
	public boolean isDepositStop(int index)
	{
		return getStop(index) instanceof DepositStop;
	}

	/**
	 * Says if the stop at the given address is a driver swap stop
	 * @param index the index of the stop
	 * @return <code>true</code> if it is a driver swap stop, <code>false</code> otherwise
	 */
	public boolean isDriverSwapStop(int index)
	{
		return getStop(index) instanceof DriverSwapStop;
	}
}
