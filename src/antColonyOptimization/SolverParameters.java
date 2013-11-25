package antColonyOptimization;

import java.util.ArrayList;
import java.util.List;

import locationsData.DataMatrix;
import locationsData.Location;

/** 
 * Contains parameters and methods to calculate parameters for the Min Max Ant System
 */
public class SolverParameters
{
	/** Number of solution to construct in just one normal iteration of the solver.
	 * Using to improve the result quality and can be assimilated to the
	 * number of ants in the case of a normal ACO. */
	protected int constructions_number = 200;

	/** Number of iterations used to find paths */
	protected int iterations_number = 250;

	/** Pheromone evaporate rate */
	protected float evaporate_rate = 0.02f;

	/** First coefficient used in order to calculate the relative importance between pheromones and path length 
	 * This coefficient is used to power pheromones value. */
	protected int pheromone_coef = 1;

	/** Second coefficient used in order to calculate the relative importance between pheromones and path length
	  * This coefficient is used to power path length value. */
	protected int visibility_coef = 2;

	/** Quantity of pheromones allowed per ant in order to put pheromones on the trail done */
	protected double pheromones_by_ant = 1;

	/** Number of pheromones to put on the trails at the start of the algorithm */
	protected double pheromones_at_start = 1;

	/** The distances matrix of the problem, only needed if you enable automatic parameters */
	protected DataMatrix matrix;

	/**	 Maximum amount of pheromones authorized on trails */
	protected double maxPheromones = 1;

	/**	 Minimum amount of pheromones authorized on trails */
	protected double minPheromones = 0;

	/** Path length found using nearest neighbor algorithm */
	protected int basicPathLength = -1;

	/**
	 * Constructor by matrix. Enable automatic parameters.
	 * Equivalent to SolverParameters(true, matrix).
	 * @param matrix the matrix used to calculate automatic parameters
	 */
	public SolverParameters(DataMatrix matrix)
	{
		this.matrix = matrix;
		updateAutomaticParameters();
	}

	/** 
	 *  Update all the following automatic parameters :
	 *   - constructions number
	 *   - initial pheromones amount
	 *   - max pheromone number
	 *   - min pheromones number
	 */
	public void updateAutomaticParameters()
	{
		this.constructions_number = calculateConstructionsNumber();
		this.pheromones_at_start = calculateInitialPheromones();
		this.minPheromones = calculateMinPheromones();
		this.maxPheromones = calculateMaxPheromones();
	}

	/**
	 * Calculate and return the initial pheromones amount adviced for the given problem
	 * @return the initial pheromones amount
	 */
	protected double calculateInitialPheromones()
	{
		return 1 / (this.getEvaporateRate() * getBasicPathLength());
	}

	/**
	 * Calculate and return the ants number
	 * @return  the ants number
	 */
	protected int calculateConstructionsNumber()
	{
		return matrix.getMatrixSize() > 300 ? 50 : 100;
	}

	/**
	 * Use informations of the solver parameters to calculate and return 
	 * the minimum amount of pheromones authorized on each trails. 
	 * @return the minimum amount of pheromones authorized on each trails. 
	 */
	protected double calculateMinPheromones()
	{
		double divided, divisor, averageSteps;
		// the average number of different choices available
		// to an ant each step while constructing a solution
		averageSteps = matrix.getMatrixSize() / 2;

		divided = calculateMaxPheromones() * (1 - Math.pow(0.05, 1 / getConstructionsNumber()));
		divisor = (averageSteps - 1) * Math.pow(0.05, 1 / getConstructionsNumber());

		return divided / divisor;
	}

	/**
	 * Use informations of the solver parameters to calculate and return 
	 * the maximum amount of pheromones authorized on each trails. 
	 * @return the maximum amount of pheromones authorized on each trails. 
	 */
	protected double calculateMaxPheromones()
	{
		return 1 / (this.getEvaporateRate() * getBasicPathLength());
	}
	
	/**
	 * Calculate and return the length of a path construct using the nearest-neighbor algorithm
	 * @return the length of a path
	 */
	protected int getBasicPathLength()
	{
		if (basicPathLength == -1)
		{
			List<Location> locations = new ArrayList<Location>(matrix.getLocations());
			int nbLocations = locations.size(), i, bestWay = Integer.MAX_VALUE;
			Location currentLoc = locations.get(0), nextLoc = locations.get(0), start = locations.get(0);
			locations.remove(start);

			for (i = 0 ; i < nbLocations - 1 ; ++i)
			{
				for (Location destination : locations)
				{
					if (matrix.getDuration(currentLoc, destination) < bestWay)
					{
						nextLoc = destination;
						bestWay = matrix.getDuration(currentLoc, destination);
					}
				}
				basicPathLength += bestWay;
				bestWay = Integer.MAX_VALUE;
				currentLoc = nextLoc;
				locations.remove(nextLoc);
			}
			basicPathLength += matrix.getDuration(currentLoc, start);
		}
		return basicPathLength;
	}

	/** 
	 * Setter for the maximum amount of pheromones authorized on trails
	 * @param max the maximum amount
	 */
	public void setMaxPheromones(double max)
	{
		if (max < 0)
			throw new IllegalArgumentException("Maximum pheromones must be a positive number.");
		if (max < this.minPheromones)
			throw new IllegalArgumentException("Maximum pheromones must be greater than minimum pheromones.");
		this.maxPheromones = max;
	}

	/** 
	 * Setter for the minimum amount of pheromones authorized on trails
	 * @param min the minimum amount
	 */
	public void setMinPheromones(double min)
	{
		if (min < 0)
			throw new IllegalArgumentException("Minimum pheromones must be a positive number.");
		if (min > this.maxPheromones)
			throw new IllegalArgumentException("Minimum pheromones must be lesser than maximum pheromones.");
		this.minPheromones = min;
	}

	/** 
	 * Return the minimum amount of pheromones authorized on trails
	 * @return the minimum amount of pheromones authorized on trails
	 */
	public double getMinPheromones()
	{
		return this.minPheromones;
	}

	/** 
	 * Return the maximum amount of pheromones authorized on trails
	 * @return the maximum amount of pheromones authorized on trails
	 */
	public double getMaxPheromones()
	{
		return this.maxPheromones;
	}

	/**
	 * Set the quantity of pheromones allowed per ant in order to put pheromones on the trail done
	 * @param quantity of pheromones by ant
	 */
	public void setPheromonesByAnt(double quantity)
	{
		if (quantity < 0)
			throw new IllegalArgumentException("The pheromone quantity by ant must be a positive integer");
		this.pheromones_by_ant = quantity;
	}

	/**
	 * Setter for the number of ants used in the algorithms.
	 * @param constructionsNumber number of ants
	 */
	public void setConstructionsNumber(int constructionsNumber)
	{
		if (constructionsNumber < 1)
			throw new IllegalArgumentException("The constructions number must be a strictly positive integer.");
		this.constructions_number = constructionsNumber;
	}

	/**
	 * Setter for the number of iterations
	 * @param number the number of iterations
	 */
	public void setIterationNumber(int number)
	{
		if (number < 1)
			throw new IllegalArgumentException("The iterations number must be a positive integer.");
		this.iterations_number = number;
	}

	/**
	 * Setter for the number of pheromones at start
	 * @param number number of initial pheromones
	 */
	public void setPheremonesAtStart(double number)
	{
		if (number < 0)
			throw new IllegalArgumentException("The number of pheromones at start must be a positive number.");
		this.pheromones_at_start = number;
	}

	/** 
	 * Setter for the pheromones coefficient
	 * @param coef coefficient
	 */
	public void setPheromoneCoef(int coef)
	{
		if (coef < 0)
			throw new IllegalArgumentException("The pheromones coefficient must be a positive integer.");
		this.pheromone_coef = coef;
	}

	/** 
	 * Setter for the visibility coefficient
	 * @param coef coefficient
	 */
	public void setVisibilityCoef(int coef)
	{
		if (coef < 0)
			throw new IllegalArgumentException("The visibility coefficient must be a positive integer.");
		this.visibility_coef = coef;
	}

	/** 
	 * Setter for the evaporate rate
	 * @param rate evaporate rate
	 */
	public void setEvaporateRate(float rate)
	{
		if (rate < 0)
			throw new IllegalArgumentException("The evaporate rate can't be negative.");
		this.evaporate_rate = rate;
	}

	/**
	 * Return the number of ants
	 * @return the number of ants
	 */
	public int getConstructionsNumber()
	{
		return this.constructions_number;
	}

	/**
	 * Return the evaporate rate
	 * @return the evaporate rate
	 */
	public float getEvaporateRate()
	{
		return this.evaporate_rate;
	}

	/**
	 * The number of iterations
	 * @return the number of iterations
	 */
	public int getIterationsNumber()
	{
		return this.iterations_number;
	}

	/**
	 * Return the coefficient of pheromone importance
	 * @return the coefficient of pheromone importance
	 */
	public int getPheromoneCoef()
	{
		return this.pheromone_coef;
	}

	/**
	 * Return the coefficient of visibility importance
	 * @return the coefficient of visibility importance
	 */
	public int getVisibilityCoef()
	{
		return this.visibility_coef;
	}

	/**
	 * Return the number of pheromones to put on the trails at the start of the algorithm
	 * @return the number of pheromones to put on the trails at the start of the algorithm
	 */
	public double getPheromoneAtStart()
	{
		return pheromones_at_start;
	}

	/**
	 * The quantity of pheromones allowed per ant in order to put pheromones on the trail done
	 * @return the quantity of pheromones allowed per ant in order to put pheromones on the trail done
	 */
	public double getPheromonesByAnt()
	{
		return pheromones_by_ant;
	}

	/** 
	 * Return a string describing the parameters of this class
	 * @return a string describing the parameters of this class
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("------ Solver Parameters ------\n");
		sb.append("Ants number: " + getConstructionsNumber() + "\n");
		sb.append("Iterations number: " + getIterationsNumber() + "\n");
		sb.append("Pheromones at start: " + getPheromoneAtStart() + "\n");
		sb.append("Pheromones by ants: " + getPheromonesByAnt() + "\n");
		sb.append("Evaporate rate: " + getEvaporateRate() + "\n");
		sb.append("Pheromone coefficient: " + getPheromoneCoef() + "\n");
		sb.append("Visibility coefficient: " + getVisibilityCoef() + "\n");
		sb.append("Min Pheromones: " + getMinPheromones() + "\n");
		sb.append("Max Pheromones: " + getMaxPheromones() + "\n");
		return sb.toString();
	}
}
