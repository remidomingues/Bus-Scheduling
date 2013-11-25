/**
 * File : Path.java
 *
 * Created on Apr 19, 2012, 2:31:16 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package locationsData;

/**
 * Represent a Path, including the person and the needs of this person about the path.
 */
public class Path
{
	/** The minimum hour authorized for the different time constraints */
	protected static final int MAX_HOUR = 24 * 3600 - 1;

	/** The minimum hour authorized for the different time constraints */
	protected static final int MIN_HOUR = 0 * 3600;

	/** If the user did'nt specify a time constraint */
	public static final int UNSPECIFIED_VALUE = -1;

	/** Path departure */
	protected Location origin;

	/** Path destination */
	protected Location destination;

	/** Time, in seconds, from when you can take the person */
	protected int minimumPickupTime;

	/** Time, in seconds, when the person wants to be deposit */
	protected int wishedDepositTime;

	/** Person transported by the bus on this path */
	protected Person person;

	/**
	 * Full constructor
	 * @param departure the origin location for the path
	 * @param destination the destination location of the path
	 * @param minPickupTime the time, represented in seconds, from where can start to take the person at the origin location
	 * @param wishedDepositTime the wished deposit time, represented in seconds, of the person to the destination
	 * @param person the person that need to do the path
	 * @throws NullPointerException if one compulsory parameters is null 
	 * @throws IllegalArgumentException if the given time is invalid (bigger than the wished deposit time or not representing an hour)
	 */
	public Path(Location departure, Location destination, int minPickupTime, int wishedDepositTime,
			Person person) throws NullPointerException, IllegalArgumentException
	{
		// We control that we have received the compulsory values
		if (departure == null || destination == null)
		{
			throw new NullPointerException("The departure and the destination locations are compulsory.");
		}
		this.origin = departure;
		this.destination = destination;
		this.person = person;
		setWishedDepositTime(wishedDepositTime);
		setMinPickupTime(minPickupTime);
	}

	/**
	 * Constructor if you only know the wished deposit time
	 * @param departure the origin location for the path
	 * @param destination the destination location of the path
	 * @param wishedDepositTime the wished deposit time, represented in seconds, of the person to the destination
	 * @param person the person that need to do the path
	 * @throws NullPointerException if one compulsory parameters is null 
	 * @throws IllegalArgumentException if the given time is invalid (bigger than the wished deposit time or not representing an hour)
	 */
	public Path(Location departure, Location destination, int wishedDepositTime, Person person)
			throws NullPointerException, IllegalArgumentException
	{
		this(departure, destination, UNSPECIFIED_VALUE, wishedDepositTime, person);
	}

	/**
	 * Copy constructor
	 * @param p The object to copy
	 * @throws NullPointerException if one compulsory parameters is null 
	 * @throws IllegalArgumentException if the given time is invalid (bigger than the wished deposit time or not representing an hour)
	 */
	public Path(Path p) throws NullPointerException, IllegalArgumentException
	{
		this(p.getOrigin(), p.getDestination(), p.getStartTakenTime(), p.getWishedDepositTime(), p
				.getPerson());
	}

	/**
	 * Return the departure location from where you take the person
	 * @return the departure location from where you take the person
	 */
	public Location getOrigin()
	{
		return origin;
	}

	/**
	 * Return the destination location, where you have to deposit the person
	 * @return the destination location, where you have to deposit the person
	 */
	public Location getDestination()
	{
		return destination;
	}

	/**
	 * Return the person transported on this path
	 * @return the person transported on this path
	 */
	public Person getPerson()
	{
		return person;
	}

	/**
	 * Return the time, in seconds, representing the start time you can take the person
	 * at the origin location.
	 * @return the time, in seconds, representing the start time you can take the person
	 * at the origin location.
	 */
	public int getStartTakenTime()
	{
		return minimumPickupTime;
	}

	/**
	 * Return the time, in seconds, representing the time before you have to deposit the person
	 * at the origin location.
	 * @return the time, in seconds, representing the time before you have to deposit the person
	 * at the origin location.
	 */
	public int getWishedDepositTime()
	{
		return wishedDepositTime;
	}

	/**
	 * Set the time when the person wants to be deposited
	 * @param wishedDepositTime time, in seconds, when the person wants to be deposit
	 * @throws IllegalArgumentException if the given time is invalid (bigger than the wished deposit time or not representing an hour)
	 */
	protected void setWishedDepositTime(int wishedDepositTime) throws IllegalArgumentException
	{
		// We control that the user said the wished deposit time value
		if (wishedDepositTime == UNSPECIFIED_VALUE)
			throw new IllegalArgumentException("O tempo mínimo de tomada e errado. (The minimal taken time is wrong)");
		// and if the value is correct
		if (wishedDepositTime <= MIN_HOUR || wishedDepositTime >= MAX_HOUR)
			throw new IllegalArgumentException(
					"O tempo mínimo de tomada e errado. (The minimal taken time is wrong)");
		this.wishedDepositTime = wishedDepositTime;
	}

	/**
	 * Set the minimal time from when you can the person
	 * @param startTakenTime the minimal time from when you can the person
	 * @throws IllegalArgumentException if the given time is invalid (bigger than the wished deposit time or not representing an hour)
	 */
	protected void setMinPickupTime(int startTakenTime) throws IllegalArgumentException
	{
		// we control if the start taken time value is correct if it is fill
		if (startTakenTime != UNSPECIFIED_VALUE)
		{
			if (startTakenTime <= MIN_HOUR || startTakenTime >= MAX_HOUR)
				throw new IllegalArgumentException(
						"O tempo mínimo de tomada e errado. (The minimal taken time is wrong)");
			if (startTakenTime >= wishedDepositTime)
				throw new IllegalArgumentException(
						"O tempo mínimo de tomada deve ser restrita inferior do tempo desejado do depósito. (The minimal taken time must be strictly inferior of the wished deposit time)");
		}
		this.minimumPickupTime = startTakenTime;
	}

	/** 
	 * Says if the path concerns a person in wheel chair
	 * @return true if the person transported has wheel chair, false otherwise
	 */
	public boolean isWheelChaired()
	{
		if (person != null)
			return person.isWheelchaired();
		else
			return false;
	}

	/**
	 * Says if the path has a minimal time from when you can the person or not
	 * @return <code>true</code> if the path has a minimal time, <code>false</code> otherwise
	 */
	public boolean hasOriginTimeConstraint()
	{
		return minimumPickupTime != UNSPECIFIED_VALUE;
	}

	/** 
	 * Return a string describing the path.
	 * @return a string describing the path.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(origin).append(" > ").append(destination);
		sb.append(isWheelChaired() ? "(wheelchair)" : "");
		sb.append("\n");
		return sb.toString();
	}
}
