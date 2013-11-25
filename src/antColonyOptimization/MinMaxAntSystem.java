package antColonyOptimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import presentation.ProgressionEvent.ProgressionType;

import application.BusScheduling;

import locationsData.Location;
import locationsData.Path;
import locationsData.Person;

/**
 * Main class of the ant colony optimization algorithm. This one process the
 * search of a path as short as possible using Ant objects, using data and
 * parameters.
 */
public class MinMaxAntSystem
{
	/** Data used by the ant system */
	protected SolverData data;

	/** Parameters used by the solver */
	protected SolverParameters parameters;

	/** Set of all the paths the ants need to do */
	protected List<DynamicPath> paths;

	/** Array of the ants used to solve the problem*/
	protected Ant[] ants;

	/** The best solution found by the ants for the problem. */
	protected Partition bestPartition;

	/** The last partition found by the ants */
	protected Partition lastPartition;
	
	/** BusScheduling used to fire events */
	protected BusScheduling busScheduling;

	/**
	 * Constructor
	 * @param data data of the problem
	 * @param parameters parameters used by the solver
	 */
	public MinMaxAntSystem(SolverData data, SolverParameters parameters)
	{
		this.data = data;
		this.parameters = parameters;
		configurate();
	}

	/**
	 * Initialize and configure some of the parameters of the ant system solver.
	 */
	protected void configurate()
	{
		// Creation of the dynamic paths using the basic path in the solver data
		// We set a time frame when this is needed
		DynamicPath dynPath;
		paths = new ArrayList<DynamicPath>(data.getPaths().size());
		for (Path p : data.getPaths())
		{
			dynPath = new DynamicPath(p);
			if (!dynPath.hasOriginTimeConstraint())
			{
				dynPath.setMinPickupTime(dynPath.getWishedDepositTime()
						- (data.getDuration(dynPath.getOrigin(), dynPath.getDestination()) * SolverData.TIME_FRAME_COEFFICIENT));
			}
			paths.add(dynPath);
		}

		// Creation of the ants giving them a dynamic bus and a reference to the
		// paths set
		SwapConductorPath scp;
		ants = new Ant[data.getAllBus().size()];
		for (int i = 0 ; i < data.getAllBus().size() ; i++)
		{
			ants[i] = new Ant(data, new DynamicBus(data.getBus(i)), parameters, paths);

			scp = new SwapConductorPath(data.getBus(i).getDriverSwap(), 14 * 3600);
			ants[i].setSwapConductorPath(scp);
			paths.add(scp);
		}

		// Initialization of the pheromones matrix
		data.getDataMatrix().fillPheromones(parameters.getPheromoneAtStart());
	}

	/** 
	 * Return the best solution found by the ants for the problem
	 * @return the best solution found by the ants for the problem
	 */
	public Partition getBestPartition()
	{
		return bestPartition;
	}

	/**
	 * Search bus repartition and routes better as possible respecting the given constraints
	 */
	public void solve()
	{
		// Progression variables
		int cpt = 1, percent = 0;
		// The number of total iterations and the number of constructions for
		// each iterations
		int iterations = parameters.getIterationsNumber(), subIterations = parameters
				.getConstructionsNumber();
		// The position of the ants that is currently building a route
		int chosenAnt = 0;
		// Generate random integer in order to determine how much locations an
		// ant must done at each step.
		Random rand = new Random();

		for (int i = 0 ; i < iterations ; ++i)
		{
			for (int j = 0 ; j < subIterations ; ++j)
			{
				// Reinit of the partition
				lastPartition = new Partition(ants.length);

				// Build the first part of the route
				for (Ant a : ants)
				{
					a.startRoute();
				}
				// While there are path left, we build the routes by giving a
				// random number of
				// location to add to each ant.
				while (!allPathsDone())
				{
					ants[chosenAnt].buildSolution(rand.nextInt(4) + 1);
					chosenAnt = (chosenAnt + 1) % ants.length;
				}
				// Build the last part of the route
				for (Ant a : ants)
				{
					a.finishRoute();
					lastPartition.addRoute(a.getRoute());
				}
				// Once a construction is built we compare it to the best and
				// save it if t is better
				if (bestPartition == null || lastPartition.getTotalDelay() < bestPartition.getTotalDelay())
				{
					bestPartition = new Partition(lastPartition);
				}
				else if (lastPartition.getTotalDelay() == bestPartition.getTotalDelay())
				{
					if (lastPartition.getTotalUserTime() + lastPartition.getTotalAdvance() < bestPartition
							.getTotalUserTime() + bestPartition.getTotalAdvance())
					{
						bestPartition = new Partition(lastPartition);
					}
				}
				resetPaths();
			}
			// Updates the pheromone trails
			updatePheromones();
			// Evaporates the pheromones in the pheromones matrix
			evaporatePheromones();
			//Update the progression notification
			percent = updateProgression(cpt, percent, iterations);
			++cpt;
		}

		// Then we calculate and set the distances values to the routes
		for (Route route : bestPartition.getRoutes())
		{
			for (int i = 0 ; i < route.getCountStops() - 1 ; ++i)
			{
				route.addPathDistance(data.getDataMatrix().getDistance(route.getLocation(i),
						route.getLocation(i + 1)));
			}
		}

	}

	/**
	 * Set a given repartition as the best repartition
	 * @param repartition the partition to save
	 */
	protected void saveRepartition(Partition repartition)
	{
		bestPartition = new Partition(repartition);
	}

	/**
	 * Simulate the evaporation process of the pheromones put on paths
	 */
	protected void evaporatePheromones()
	{
		float coefMulti = 1 - parameters.getEvaporateRate();
		double min = parameters.getMinPheromones(), max = parameters.getMaxPheromones(), pheromones;

		for (Location origin : data.getLocations())
		{
			for (Location destination : data.getLocations())
			{
				pheromones = coefMulti * data.getPheromones(origin, destination);
				if (pheromones > max)
				{
					pheromones = max;
				}
				else if (pheromones < min)
				{
					pheromones = min;
				}
				data.setPheromones(origin, destination, pheromones);
			}
		}
	}

	/**
	 * Add pheromones to the paths taken by ants, depending on the path weight
	 */
	protected void updatePheromones()
	{
		Location start, end;
		double pheromoneValue = 0;

		for (Route route : bestPartition.getRoutes())
		{
			for (int i = 0 ; i < route.getCountStops() - 1 ; ++i)
			{
				start = route.getLocation(i);
				end = route.getLocation(i + 1);
				pheromoneValue = parameters.getPheromonesByAnt() / route.getTotalDuration();
				data.addPheromones(start, end, pheromoneValue);
			}
		}
	}

	/**
	 * Says if all paths are done yet or not
	 * @return true if all paths are done, false otherwise
	 */
	protected boolean allPathsDone()
	{
		for (DynamicPath p : paths)
		{
			if (!p.isFinished())
				return false;
		}
		return true;
	}

	/**
	 * Reset the dynamic paths and reset all of them on their origin position.
	 */
	public void resetPaths()
	{
		for (DynamicPath p : paths)
		{
			p.reset();
		}
	}

	/**
	 * Update the busScheduling value
	 * @param busScheduling The busScheduling value
	 */
	public void setBusScheduling(BusScheduling busScheduling)
	{
		this.busScheduling = busScheduling;
	}
	
	/**
	 * Update the process progression
	 * @param cpt The current step number
	 * @param percent The current progression percentage
	 * @param n Total number of steps
	 * @return The current progression percentage
	 */
	private int updateProgression(int cpt, int percent, int n)
	{
		int tmpPercent = 49 * cpt / n;
		if (tmpPercent != percent)
		{
			busScheduling.fireEvent(this, ProgressionType.INCREMENT, tmpPercent - percent);
			percent = tmpPercent;
		}
		return percent;
	}

	/** 
	 *  This class is used to make easier the implementation of the change conductor constraint.
	 */
	public static class SwapConductorPath extends DynamicPath
	{
		/**
		 * Full constructor
		 * @param location the location where swap the conductor
		 * @param swapTime the time where the conductor have to be swept
		 */
		public SwapConductorPath(Location location, int swapTime)
		{
			super(new Path(location, location, swapTime, new Person("", false)));
			setSwapTime(swapTime);
			state = PathState.ORIGIN;
		}

		/** 
		 * Move the path => says that the swap is done
		 * @param time time when you take or deposit the person
		 * @return 0 if origin point, the time of traveling in the bus if destination
		 * @see antColonyOptimization.DynamicPath#move(int)
		 */
		@Override
		public int move(int time)
		{
			setDepositTime(time);
			state = PathState.DONE;
			return getDuration();
		}

		/** 
		 * Return the time the bus needed to stop at the location
		 * @return the time the bus needed to stop at the location		 
		 * @see antColonyOptimization.DynamicPath#getStopDuration()
		 */
		@Override
		public int getStopDuration()
		{
			return SolverData.getDriverSwapTime();
		}

		/** 
		 * Return a positive time difference between wished deposit time and real deposit time
		 * @return a positive time difference between wished deposit time and real deposit time
		 * @see antColonyOptimization.BusPath#getDelay()
		 */
		@Override
		public int getDelay()
		{
			return getDepositTime() - getWishedDepositTime();
		}

		/** 
		 * @return 0 always
		 * @see antColonyOptimization.BusPath#getDuration()
		 */
		@Override
		public int getDuration()
		{
			return 0;
		}

		/** 
		 * @return null 
		 * @see locationsData.Path#getPerson()
		 */
		@Override
		public Person getPerson()
		{
			return null;
		}

		/** 
		 *  
		 * @see antColonyOptimization.DynamicPath#reset()
		 */
		@Override
		public void reset()
		{
			this.state = PathState.ORIGIN;
		}

		/** 
		 * @return always true
		 * @see antColonyOptimization.DynamicPath#isStarted()
		 */
		@Override
		public boolean isStarted()
		{
			return true;
		}

		/** 
		 * Reset the wished swap time 
		 * @param time the wished swap time
		 */
		public void setSwapTime(int time)
		{
			this.minimumPickupTime = time - SolverData.DRIVER_SWAP_TIME * SolverData.TIME_FRAME_COEFFICIENT;
			this.wishedDepositTime = time;
		}

		/** 
		 * @return a string describing the swap
		 * @see antColonyOptimization.DynamicPath#toString()
		 */
		@Override
		public String toString()
		{
			return "SWAP CONDUCTOR";
		}
	}
}
