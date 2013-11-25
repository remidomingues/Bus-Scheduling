/**
 * File : Repartition.java
 *
 * Created on May 28, 2012, 10:08:02 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package antColonyOptimization;

import java.util.ArrayList;
import java.util.List;

/** 
 *  Represent a bus partition. Contains as many routes as bus.
 */
public class Partition
{
	/** List of the routes */
	protected List<Route> routes;

	/**
	 * Normal constructor 
	 * @param size number of the bus/route that will be added to the partition
	 */
	public Partition(int size)
	{
		routes = new ArrayList<Route>(size);
	}

	/**
	 * Constructor by copy
	 * @param repartition the repartition to clone
	 */
	public Partition(Partition repartition)
	{
		this.routes = new ArrayList<Route>(repartition.routes.size());
		for (Route r : repartition.routes)
		{
			addRoute(new Route(r));
		}
	}

	/**
	 * Add a route to the routes list
	 * @param route the rotue to add
	 */
	public void addRoute(Route route)
	{
		routes.add(route);
	}

	/**
	 * Calculate and return the total delay which is the sum of the delay of each route.
	 * @return the total delay
	 */
	public int getTotalDelay()
	{
		int delay = 0;
		for (Route r : routes)
		{
			delay += r.getTotalDelay();
		}
		return delay;
	}

	/**
	 * Calculate and return the total duration which is the sum of the duration of each route.
	 * @return the total duration
	 */
	public int getTotalDuration()
	{
		int duration = 0;
		for (Route r : routes)
		{
			duration += r.getTotalDuration();
		}
		return duration;
	}

	/**
	 * Calculate and return the total user time which is the sum of the user time of each route.
	 * @return the total user time
	 */
	public int getTotalUserTime()
	{
		int userTime = 0;
		for (Route r : routes)
		{
			userTime += r.getTotalUserTime();
		}
		return userTime;
	}

	/**
	 * Calculate and return the total advance which is the sum of the advance of each route.
	 * @return the total advance
	 */
	public int getTotalAdvance()
	{
		int advance = 0;
		for (Route r : routes)
		{
			advance += r.getTotalAdvance();
		}
		return advance;
	}

	/**
	 * Calculate and return the total excessive advance which is the sum of the excessive advance of each route.
	 * @return the total excessive advance
	 */
	public int getTotalExcessiveAdvance()
	{
		int advance = 0;
		for (Route r : routes)
		{
			advance += r.getTotalExceedAdvance();
		}
		return advance;
	}
	
	/** 
	 * Return the routes list
	 * @return the routes list
	 */
	public List<Route> getRoutes()
	{
		return routes;
	}
}
