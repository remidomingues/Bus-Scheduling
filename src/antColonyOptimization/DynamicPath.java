package antColonyOptimization;

import locationsData.Location;
import locationsData.Path;

/**
 * A bus path able to move, means that this class has a state  for the path 
 */
public class DynamicPath extends BusPath
{
	/**
	 * State of the path
	 */
	enum PathState
	{
		/** The origin is still to visit */
		ORIGIN,
		/** The destination is still to visit */
		DESTINATION,
		/** All destinations contained by the path have been visited */
		DONE
	}

	/** Path state. This one indicate the next location to visit */
	protected PathState state = PathState.ORIGIN;

	/**
	 * Full constructor
	 * @param path the path 
	 */
	public DynamicPath(Path path)
	{
		super(path);
	}

	/** 
	 * Set the time from when you can take the person
	 * @param startTakenTime the time from when you can take the person
	 * @see antColonyOptimization.BusPath#setPickupTime(int)
	 */
	@Override
	public void setMinPickupTime(int startTakenTime) throws IllegalArgumentException
	{
		super.setMinPickupTime(startTakenTime);
	}

	/**
	 * Reinitialize the path, setting the state to its default value
	 */
	public void reset()
	{
		state = PathState.ORIGIN;
	}

	/**
	 * Return the next location to visit, or null if all of them were visited
	 * @return The next location to visit, or null if all of them were visited
	 */
	public Location getNext()
	{
		if (state == PathState.ORIGIN)
			return getOrigin();
		else if (state == PathState.DESTINATION)
			return getDestination();
		else
			return null;
	}

	/**
	 * Change the path state value, simulating a move on this path
	 * and save the time of taken or deposit the person
	 * @param time time when you take or deposit the person
	 * @return 0 if origin point, the time of traveling in the bus if destination
	 */
	public int move(int time)
	{
		if (state == PathState.ORIGIN)
		{
			setPickupTime(time);
			state = PathState.DESTINATION;
		}
		else if (state == PathState.DESTINATION)
		{
			setDepositTime(time);
			state = PathState.DONE;
			return getDuration();
		}
		return 0;
	}

	/**
	 * Says if the next step for the bus is to go to the origin location
	 * @return <code>true</code> if the next step is the origin location, <code>false</code> otherwise
	 */
	public boolean isGoingOrigin()
	{
		return state == PathState.ORIGIN;
	}

	/**
	 * Says if the next step for the bus is to go to the destination location
	 * @return <code>true</code> if the next step is the destination location, <code>false</code> otherwise
	 */
	public boolean isGoingDestination()
	{
		return state == PathState.DESTINATION;
	}

	/**
	 * Says if the bus has finished his path
	 * @return <code>true</code> if the bus has done this path, <code>false</code> otherwise
	 */
	public boolean isFinished()
	{
		return state == PathState.DONE;
	}

	/**
	 * Says if this path is already started, meaning that one of location is already reached.
	 * A finished path is considered to be already started so this method will also returned true if the path is already done.
	 * @return true if the path is already started, false otherwise.
	 */
	public boolean isStarted()
	{
		return isFinished() || isGoingDestination();
	}

	/**
	 * Says if this path is not started yet. It means no locations was reached yet.
	 * @return true if the path is not started, false otherwise;
	 */
	public boolean isNotSarted(){
		return !isStarted();
	}

	/**
	 * Return the time the bus needed to stop at the location
	 * @return the time the bus needed to stop at the location
	 */
	public int getStopDuration()
	{
		if (getPerson() == null)
			return 0;
		if (isWheelChaired())
			return SolverData.getWheelchairTime();
		return SolverData.getBasicAdditionalTime();
	}
	
	/** 
	 * Set the wished deposit time value
	 * @param wishedDepositTime
	 * @throws IllegalArgumentException 
	 * @see locationsData.Path#setWishedDepositTime(int)
	 */
	@Override
	public void setWishedDepositTime(int wishedDepositTime) throws IllegalArgumentException
	{
		super.setWishedDepositTime(wishedDepositTime);
	}

	/** 
	 * @return a string describing this path
	 * @see locationsData.Path#toString()
	 */
	@Override
	public String toString()
	{
		return state.toString();
	}
}
