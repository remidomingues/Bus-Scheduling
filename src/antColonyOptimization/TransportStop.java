package antColonyOptimization;

import locationsData.Location;
import locationsData.Person;

/** 
 * Extends a stop and add it the fact that it is link to the transport (take or deposit) of a person
 */
public abstract class TransportStop extends Stop
{
	/**
	 * Full constructor
	 * @param location the location where the bus has to go
	 * @param person the person link with this location
	 * @param arrivalTime the time the bus arrive at destination
	 */
	public TransportStop(Location location, Person person, int arrivalTime)
	{
		super(location, arrivalTime);
		setPerson(person);
	}

	/** The person taken or deposit by the bus */
	private Person person;

	/**
	 *  Return the person value
	 * @return The person value
	 */
	public Person getPerson()
	{
		return person;
	}

	/**
	 * Set the person links with this stop
	 * @param person the person to set
	 */
	protected void setPerson(Person person)
	{
//		if (person == null)
//			throw new NullPointerException("You have to give a person related to a tranport stop.");
		this.person = person;
	}

	/**
	 * Return the time the bus needs to stay at the stop
	 * @return the time the bus needs to stay at the stop
	 */
	public int getDowntime()
	{
		return person.isWheelchaired() ? SolverData.getWheelchairTime() : SolverData.getBasicAdditionalTime();
	}

	/**
	 * @return a string describing the object
	 * @see antColonyOptimization.Stop#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder().append(getPerson().getName()).append(" :")
				.append(super.toString());

		return sb.toString();
	}
	
	
	/**  Contains information about a bus stop where the bus take a person
	 */
	static class PickupStop extends TransportStop
	{
		/** The minimum time from where the person wants to be taken. */
		protected int minimumTime;

		/** 
		 * Full constructor
		 * @param location the location where take the person
		 * @param person the person to take
		 * @param minimumTime minimum time from where the person can be taken
		 * @param arrivalTime the time the bus arrived at destination
		 */
		public PickupStop(Location location, Person person, int minimumTime, int arrivalTime)
		{
			super(location, person, arrivalTime);
			setMinimumTime(minimumTime);
		}

		/**
		* Return the difference between the real arrival time and the wished time.
		* @return the time difference for this bus stop
		*/
		public int getTimeDifference()
		{
			return getArrivalTime() - getMinimumTime();
		}

		/**
		 * Set the minimum time 
		 * @param time minimum time from where the person wants to be taken
		 */
		public void setMinimumTime(int time)
		{
			controlTime(time);
			this.minimumTime = time;
		}

		/**
		 * Return the minimum time from where the person want to be taken
		 * @return the minimum time from where the person want to be taken
		 */
		public int getMinimumTime()
		{
			return minimumTime;
		}

		/** 
		 * Clone this object
		 * @return the object clone
		 * @see antColonyOptimization.Stop#clone()
		 */
		@Override
		public Stop clone()
		{
			return new PickupStop(getLocation(), getPerson(), getMinimumTime(), getArrivalTime());
		}
	}

	/** 
	 *  Contains information about a bus stop where a bus deposit a person
	 */
	static class DepositStop extends TransportStop
	{
		/** Time in seconds when the person wants to be deposit */
		protected int wishedTime = 0;

		/**
		 * Full constructor
		 * @param location the location where the bus has to go to deposit the person
		 * @param person the person to deposit
		 * @param arrivalTime the time the bus arrive at destination
		 * @param wishedTime the wished time the bus should be at destination
		 */
		public DepositStop(Location location, Person person, int arrivalTime, int wishedTime)
		{
			super(location, person, arrivalTime);
			this.wishedTime = wishedTime;
		}

		/**
		* Return the difference between the real arrival time and the wished time.
		* @return the time difference for this bus stop
		*/
		public int getDelay()
		{
			return Math.max(getArrivalTime() - getWishedTime(), 0);
		}

		/**
		 * Return the time when the person wants to be deposit
		 * @return the time, in seconds, when the person wants to be deposit
		 */
		public int getWishedTime()
		{
			return wishedTime;
		}

		/** 
		 * Set the time when the person wants to be deposited
		 * @param wishedTime the time in seconds when the person wants to be deposited
		 */
		public void setWishedTime(int wishedTime)
		{
			controlTime(wishedTime);
			this.wishedTime = wishedTime;
		}

		/** 
		 * Clone this object
		 * @return the object clone
		 * @see antColonyOptimization.Stop#clone()
		 */
		@Override
		public Stop clone()
		{
			return new DepositStop(getLocation(), getPerson(), getArrivalTime(), getWishedTime());
		}
	}
	
}


