package antColonyOptimization;

import locationsData.Path;

/**
 * Represents a bus path, so it add to a "classic" Path the data about duration, delay and advance that
 * can have the bus.
 */
public class BusPath extends Path
{
	/** Constant use to define what is considerate to be a normal advance in seconds */
	public static final int MAX_NORMAL_ADVANCE = 30 * 60; // 30 Minutes

	/** Time when the person is taken at the origin*/
	protected int pickupTime = 0;

	/** Time when the person is deposit at the destination */
	protected int depositTime = 0;

	/**
	 * Copy constructor
	 * @param p the path to copy
	 * @throws NullPointerException if one compulsory parameters is null 
	 * @throws IllegalArgumentException if the given time is invalid (bigger than the wished deposit time or not representing an hour)
	 */
	public BusPath(Path p) throws NullPointerException, IllegalArgumentException
	{
		super(p);
	}

	/**
	 * Return the time the person spent in the bus, equivalent to the difference time
	 *  between the deposit time and the taken time.
	 * @return  time the person spent in the bus
	 */
	public int getDuration()
	{
		return getDepositTime() - getPickupTime();
	}

	/**
	 * Return the delay of the path
	 * @return  the delay of the path
	 */
	public int getDelay()
	{
		return Math.max(getDepositTime() - getWishedDepositTime(), 0);
	}

	/**
	 * Return the advance of the path
	 * @return the advance of the path
	 */
	public int getAdvance()
	{
		return Math.max(getWishedDepositTime() - getDepositTime(), 0);
	}

	/**
	 * Return the excessive advance of the path, it means the time that exceed what is considered 
	 * to be a normal advance
	 * @return he excessive advance of the path
	 */
	public int getExcessiveAdvance()
	{
		return Math.max(getAdvance() - MAX_NORMAL_ADVANCE, 0);
	}

	/**
	 * Return the time, in seconds, when the bus take the person
	 * @return the time, in seconds, when the bus take the person
	 */
	public int getPickupTime()
	{
		return pickupTime;
	}

	/**
	 * Set the time when the bus take the person
	 * @param takenTime the time, in seconds, when the bus take the person
	 */
	public void setPickupTime(int takenTime)
	{
		this.pickupTime = takenTime;
	}

	/**
	 * Return time when the person is deposit to the destination
	 * @return time in seconds when the person is deposit to the destination
	 */
	public int getDepositTime()
	{
		return depositTime;
	}

	/**
	 * Set the time when the person is deposit to the destination
	 * @param depositTime time in seconds when the person is deposit to the destination
	 */
	public void setDepositTime(int depositTime)
	{
		this.depositTime = depositTime;
	}

}
