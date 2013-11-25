package antColonyOptimization;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import antColonyOptimization.MinMaxAntSystem.SwapConductorPath;

import locationsData.Location;

/** 
 *  Class simulating a work of an ant used by the min max ant system.
 */
class Ant
{
	/** Data used in order to know paths weight and pheromones */
	protected SolverData data;
	/** A reference to all the paths of the solver */
	protected List<DynamicPath> globalPaths;
	/**  The coefficient used by the ants to calculate probabilities */
	protected int visibilityCoef;
	/** The coefficient used by the ants to calculate probabilities */
	protected int pheromoneCoef;

	/** A dynamic bus use to help the ant work with constraints capacity */
	protected DynamicBus bus;
	/** The paths done by this ant. */
	protected Set<DynamicPath> chosenPaths;
	/** The special path used to perform the conductor swap */
	protected SwapConductorPath swap;
	/** Data of the path found by the ant */
	protected Route route;
	/** The provisional hour the ant start or continue his work;*/
	protected int hour;

	/** 
	 * Full constructor 
	 * @param data the data use to know duration and pheromones
	 * @param bus the bus linked to the ant
	 * @param parameters the solver parameters to determine coefficient
	 * @param globalPaths a reference to the list of global paths
	 */
	public Ant(SolverData data, DynamicBus bus, SolverParameters parameters, List<DynamicPath> globalPaths)
	{
		this.data = data;
		this.bus = bus;
		this.pheromoneCoef = parameters.getPheromoneCoef();
		this.visibilityCoef = parameters.getVisibilityCoef();
		this.globalPaths = globalPaths;
		this.chosenPaths = new HashSet<DynamicPath>();
	}

	/**
	 * Start the route by adding the origin bus location and set the start hour
	 */
	protected void startRoute()
	{
		init();
		// we get the start hour
		hour = bus.getMinBusStartTime();
		// We add the origin point
		swap.setSwapTime(hour + SolverData.getDriverWorkTime());
		route.addLocation(bus.getOrigin(), hour);
	}

	/**
	 * Finish the route by adding the start location as the final destination (cycle route) 
	 */
	protected void finishRoute()
	{
		// we finalize the route by adding the final destination
		Location chosenLoc = bus.getOrigin();
		hour += data.getDuration(route.getLastVisitedLocation(), chosenLoc);
		route.addLocation(chosenLoc, hour);
	}

	/**
	 * Initialize the ant 
	 */
	protected void init()
	{
		route = new Route();
		chosenPaths.clear();
		chosenPaths.add(swap);
	}

	/**
	 * Update the data value
	 * @param data The data value
	 */
	public void setData(SolverData data)
	{
		this.data = data;
	}

	/** 
	 * Return the value of the attribute travel
	 * @return The travel value
	 */
	public Route getRoute()
	{
		return route;
	}

	/** 
	 * Calculate the probability to take a specified path in function of path weight 
	 * and pheromones
	 * @param pheromones 
	 * @param visibility 
	 * @return The probability calculated
	 */
	protected double calculateProbability(double pheromones, int visibility)
	{
		return Math.pow(pheromones, pheromoneCoef) * Math.pow(1.0 / visibility, visibilityCoef);
	}

	/** Return the chosenPaths value
	 * @return The chosenPaths value
	 */
	public Set<DynamicPath> getChosenPaths()
	{
		return chosenPaths;
	}

	/** 
	 * Build a path under paths weight and pheromones constraints.
	 * @param locationsToConstruct number of locations to add to the current route
	 */
	public void buildSolution(int locationsToConstruct)
	{
		DynamicPath chosenPath; // represent the chosen path at each step
		Location chosenLoc; // represent the chosen location at each step
		boolean isGoingOrigin = false;
		float coeff;
		int duration;

		// While we find a path to do
		while ((chosenPath = searchDestinationLocation()) != null && locationsToConstruct > 0)
		{
			// We choose with probability a destination
			chosenLoc = chosenPath.getNext();
			isGoingOrigin = chosenPath.isGoingOrigin();
			// Then we calculate the duration modify by the traffic coefficient
			// We use an average between start time coefficient and arrival time coefficient
			duration = data.getDuration(route.getLastVisitedLocation(), chosenLoc);
			coeff = data.getTrafficCoefficient(hour);
			coeff += data.getTrafficCoefficient(hour + duration);
			coeff /= 2;
			duration *= coeff;
			// And we add the duration to the hour
			hour += duration;

			// Bus arrive early for departure, so it has to wait
			if ((chosenPath.isGoingOrigin()) && hour < chosenPath.getStartTakenTime())
			{
				hour = chosenPath.getStartTakenTime();
				// if this is the first path (1 because of the swap path )
				if(chosenPaths.size() == 1){ 
					route.setBusStartTime(hour - duration);
					swap.setSwapTime(hour - duration + SolverData.getDriverWorkTime());
				}
			}

			route.addStop(chosenPath, hour);
			if (isGoingOrigin)
			{
				bus.addPerson(chosenPath.getPerson());
			}
			else
			{
				bus.removePerson(chosenPath.getPerson());
			}
			hour += chosenPath.getStopDuration();

			chosenPaths.add(chosenPath);
			locationsToConstruct--;
		}
	}

	/**
	 * Set the swap conductor path
	 * @param scp the swap conductor path
	 */
	protected void setSwapConductorPath(SwapConductorPath scp)
	{
		this.swap = scp;
	}

	/** 
	 * Return the path having the next destination location
	 * @return the path having the next destination location
	 */
	protected DynamicPath searchDestinationLocation()
	{
		Location start = route.getLastVisitedLocation(), dest;
		double sumProba = 0, rand, sumRand = 0;
		int size = globalPaths.size(), i = 0, visibility, count = 0;
		double pheromones;
		double probas[] = new double[size];

		for (DynamicPath path : globalPaths)
		{
			if (path.isNotSarted() || !path.isFinished() && chosenPaths.contains(path))
			{
				// If we go to deposit or if we still have place to take the
				// person
				if (path.isGoingDestination() || bus.canBeAdded(path.getPerson()))
				{
					dest = path.getNext();
					pheromones = data.getPheromones(start, dest);
					visibility = data.getDuration(start, dest);

					// we consider the traffic consequences
					visibility *= data.getTrafficCoefficient(hour);

					// If we arrive too early we increase artificially the
					// duration
					if (visibility + hour < path.getStartTakenTime())
					{
						visibility += path.getStartTakenTime() - (visibility + hour);
					}

					probas[i] = calculateProbability(pheromones, visibility);

					if(probas[i] <= 0){
						probas[i] = 1E-320;
					}
					count++;
					sumProba += probas[i];
				}
			}
			i++;
		}

		if (count == 0)
		{
			return null;
		}
		rand = Math.random();
		i = 0;
		sumRand = probas[i] / sumProba;
		try
		{
			while (sumRand <= rand)
			{
				++i;
				sumRand += probas[i] / sumProba;
			}
			return globalPaths.get(i);
		}
		catch (Exception e)
		{
			for (DynamicPath p : globalPaths)
			{
				if (!p.isFinished())
					return p;
			}
			return null;
		}
	}

}
