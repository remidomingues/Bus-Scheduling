package locationsData;

/** Contains a person informations.
 */
public class Person
{
	/** true if the person has a wheel chair, false else */
	private boolean wheelchaired;

	/** Name */
	protected String name;

	/** Home phone number */
	private String homePhoneNumber;

	/** Personal phone number */
	private String personalPhoneNumber;

	/**
	 * Constructor
	 * @param name Home name
	 * @param homePhoneNumber Home phone number
	 * @param personalPhoneNumber Personal phone number
	 * @param wheelchaired true if the person has a wheel chair, false else
	 */
	public Person(String name, String homePhoneNumber, String personalPhoneNumber, boolean wheelchaired)
	{
		this.name = name;
		this.homePhoneNumber = homePhoneNumber;
		this.personalPhoneNumber = personalPhoneNumber;
		this.wheelchaired = wheelchaired;
	}

	/**
	 * Constructor
	 * @param name Person name
	 * @param wheelChaired true if the person has a wheel chair, false else
	 */
	public Person(String name, boolean wheelChaired)
	{
		this(name, null, null, wheelChaired);
	}

	/** Return the value of the attribute wheelchaired
	 * @return The wheelchaired value
	 */
	public boolean isWheelchaired()
	{
		return wheelchaired;
	}

	/**
	 * Return the personalPhoneNumber value
	 * @return The personalPhoneNumber value
	 */
	public String getPersonalPhoneNumber()
	{
		return personalPhoneNumber;
	}

	/**
	 * Return the homePhoneNumber value
	 * @return The homePhoneNumber value
	 */
	public String getHomePhoneNumber()
	{
		return homePhoneNumber;
	}

	/**
	 * Return the name value
	 * @return The name value
	 */
	public String getName()
	{
		return name;
	}
	
	/** 
	 * Says if an object is equals to the current person
	 * @param obj the object to compare with
	 * @return true if both objects are equals, false otherwise
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Person){
			return getName() == ((Person) obj).getName();
		}
		return false;
	}
}
