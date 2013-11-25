package locationsData;

import application.BusSchedulingException;

/**
 * Class containing informations about a location.
 */
public class Location
{
	/** Database location ID */
	private int id;
	
	/** Name of the location. It is not compulsory. */
	protected String name;

	/** Street */
	protected String street;

	/** Additional informations on the location (near to...). It is not compulsory.*/
	protected String additionalInformations;

	/** District */
	protected String district;

	/** Phone number. It is not compulsory. */
	private String phone;

	/** Latitude */
	private float latitude;

	/** Longitude */
	private float longitude;

	/** true if the coordinates was specified when the object was constructed, false else */
	private boolean coordinatesFilled = false;

	/**
	 *  Full constructor
	 * @param name name of the location.
	 * @param street name of the street of the location.
	 * @param additionalInformations additional information about the address of the location.
	 * @param district district of the location.
	 * @param phone phone number of the location
	 * @param latitude Latitude
	 * @param longitude Longitude
	 * @throws BusSchedulingException If an exception must be threw to the used 
	 */
	public Location(String name, String street, String district,
			String additionalInformations, String phone, float latitude, float longitude)
			throws BusSchedulingException
	{
		this(name, street, district, additionalInformations, phone);
		if (latitude != 0)
		{
			this.latitude = latitude;
		}
		if (longitude != 0)
		{
			this.longitude = longitude;
		}
		this.coordinatesFilled = true;
	}
	
	/**
	 * Copy constructor
	 * @param loc The location to copy
	 * @throws BusSchedulingException 
	 */
	public Location(Location loc) throws BusSchedulingException
	{
		this(loc.getName(), loc.getStreet(), loc.getDistrict(),
				loc.getAdditionalInformations(), loc.getPhone(),
				loc.getLatitude(), loc.getLongitude());
	}

	/**
	 * Full constructor
	 * @param name name of the location.
	 * @param street name of the street of the location.
	 * @param additionalInformations additional information about the adress of the location.
	 * @param district district of the location.
	 * @param phone phone number of the location
	 * @throws BusSchedulingException
	 */
	public Location(String name, String street, String district,
			String additionalInformations, String phone) throws BusSchedulingException
	{
		if (name != null && name.trim().length() != 0)
		{
			this.name = name.trim();
		}

		if (street == null || street.trim().length() == 0)
			throw new BusSchedulingException("A rua é obrigatória. (The street is compulsory)");
		this.street = street.trim();
		if (additionalInformations != null && additionalInformations.trim().length() != 0)
		{
			this.additionalInformations = additionalInformations.trim();
		}
		if (district == null || district.trim().length() == 0)
			throw new BusSchedulingException("O bairro é obrigatório. (The district is compulsory)");
		this.district = district.trim();
		if (phone != null && phone.trim().length() != 0)
		{
			this.phone = phone.trim();
		}
	}

	/**
	 * Return the id value
	 * @return The id value
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Update the id value
	 * @param id The id value
	 */
	public void setId(int id)
	{
		this.id = id;
	}

	/**
	 * Return the name of the location.
	 * If the name wasn't set at the creation of the location it will return
	 *  the street.
	 * @return the name of the location.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Return the latitude value
	 * @return The latitude value
	 */
	public float getLatitude()
	{
		return latitude;
	}

	/**
	 * Return the longitude value
	 * @return The longitude value
	 */
	public float getLongitude()
	{
		return longitude;
	}

	/**
	 * Return the coordinatesFilled value
	 * @return The coordinatesFilled value
	 */
	public boolean isCoordinatesFilled()
	{
		return coordinatesFilled;
	}

	/**
	 * Update the latitude value
	 * @param latitude The latitude value
	 */
	public void setLatitude(float latitude)
	{
		this.latitude = latitude;
	}

	/**
	 * Update the coordinatesFilled value
	 * @param coordinatesFilled The coordinatesFilled value
	 */
	public void setCoordinatesFilled(boolean coordinatesFilled)
	{
		this.coordinatesFilled = coordinatesFilled;
	}

	/**
	 * Update the longitude value
	 * @param longitude The longitude value
	 */
	public void setLongitude(float longitude)
	{
		this.longitude = longitude;
	}

	/**
	 * Return the street of the location.
	 * @return the street of the location.
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * Return additional information about the address or null if not set.
	 * @return additional information about the address or null if not set.
	 */
	public String getAdditionalInformations()
	{
		return additionalInformations;
	}

	/**
	 * Return the telephone number, or null if not set.
	 * Please note that the format of this phone number can change depending of the location.
	 * @return the telephone number, or null if not set.
	 */
	public String getPhone()
	{
		return phone;
	}

	/**
	 * Return the district, or null if not set.
	 * @return the telephone number, or null if not set.
	 */
	public String getDistrict()
	{
		return district;
	}
	
	/**
	 * Say if the latitude and longitude are known for the current location
	 * @return true is latitude and longitude are filled, false otherwise
	 */
	public boolean isGeocoded()
	{
		return latitude != 0 && longitude != 0;
	}

	/**
	 * Say if an object is equal to the current Location.
	 * Two locations if they have the same latitude and longitude,
	 * or if they are not geocoded if they have the same number, street and district.
	 * @param obj the object to test
	 * @return true if objects are equals, false otherwise
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null || !(obj instanceof Location))
			return false;
		Location loc = (Location) obj;
		if (this.isGeocoded() && loc.isGeocoded())
			return loc.latitude == latitude && loc.longitude == longitude;
		return loc.street.equals(street) && loc.district.equals(district);
	}

	/**
	 * Return the hash code of the Location
	 * @return the hash code of the Location
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		if (isGeocoded())
			return (int) (2 * latitude * 10000 + longitude * 10000);
		return street.hashCode();
	}

	/**
	 * Return the location address
	 * @return The location address
	 */
	public String getAddress()
	{
		StringBuilder s = new StringBuilder();
		if (getName() != null && getName() != "")
		{
			s.append(String.format("%s - ", getName()));
		}
		s.append(getStreet()).append(String.format(" - %s", getDistrict()));
		return s.toString();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return getAddress() + ", " + getLatitude() + ", " + getLongitude();
	}
}