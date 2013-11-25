/**
 * File : DynamicBus.java
 *
 * Created on May 28, 2012, 9:35:36 AM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package antColonyOptimization;

import locationsData.Bus;
import locationsData.Person;

/** 
 *  Extends a bus to add him the management of the person in the bus and the capacity constraints
 */
class DynamicBus extends Bus
{
	/** Current number of person without wheelchairs in the bus */
	protected int currentBasics;
	/** Current number of person in wheelchairs in the bus */
	protected int currentWheelchaireds;

	/**
	 * Constructor by a normal bus.
	 * In fact this constructor improve a normal bus to a dynamic bus.
	 * @param bus the bus to make dynamic
	 */
	public DynamicBus(Bus bus)
	{
		super(bus.getId(), bus.getDrivers(), bus.getPhone(), bus.getWheelChairsSeats(), bus
				.getNonWheelChairsSeats(), bus.getOrigin(), bus.getDriverSwap(), bus.getMinBusStartTime());
	}

	/**
	 * Return the current number of persons without wheelchairs in the bus
	 * @return the current number of persons without wheelchairs in the bus
	 */
	public int getCurrentBasics()
	{
		return currentBasics;
	}

	/**
	 *  Return the current number of persons with a wheelchair in the bus
	 * @return the current number of persons with a wheelchair in the bus
	 */
	public int getCurrentWheelchaireds()
	{
		return currentWheelchaireds;
	}

	/**
	 * Increase the number of person with wheelchair in the bus by one
	 * @return true if it was possible to add, false otherwise
	 */
	protected boolean addWheelchair()
	{
		if (isWheelchairFull())
			return false;
		currentWheelchaireds++;
		return true;
	}

	/**
	 * Decrease the number of person in the bus with wheelchair by one
	 * @return true if it was possible to remove the person, false otherwise 
	 */
	protected boolean removeWheelchair()
	{
		if (currentWheelchaireds == 0)
			return false;
		currentWheelchaireds--;
		return true;
	}

	/**
	 * Increase the number of person without wheelchair in the bus by one
	 * @return true if it was possible to add, false otherwise
	 */
	protected boolean addBasic()
	{
		if (isBasicFull())
			return false;
		currentBasics++;
		return true;
	}

	/**
	 * Decrease the number of person in the bus without wheelchair by one
	 * @return true if it was possible to remove the person, false otherwise 
	 */
	protected boolean removeBasic()
	{
		if (currentBasics == 0)
			return false;
		currentBasics--;
		return true;
	}

	/**
	 * Says is there is no seats left for person in wheelchair
	 * @return true if the bus is full, false otherwise
	 */
	public boolean isWheelchairFull()
	{
		return getCurrentWheelchaireds() == getWheelChairsSeats();
	}

	/**
	 * Says is there is no seats left for person without wheelchair
	 * @return true if the bus is full, false otherwise
	 */
	public boolean isBasicFull()
	{
		return getCurrentBasics() == getNonWheelChairsSeats();
	}

	/**
	 * Add a person to the bus and adapt the current occupation depending if the person has wheelchair or not.
	 * @param person the person to add
	 * @return true if the person was null (not added) or was added, false if there is not enough place for this person
	 */
	public boolean addPerson(Person person)
	{
		if (person == null)
			return true;
		else if (person.isWheelchaired())
			return addWheelchair();
		else
			return addBasic();
	}

	/**
	 * Remove a person to the bus and adapt the current occupation depending if the person has wheelchair or not.
	 * @param person the person to remove
	 * @return true if the person was null (not removed) or was removed, false if the bus was already void.
	 */
	public boolean removePerson(Person person)
	{
		if (person == null)
			return true;
		else if (person.isWheelchaired())
			return removeWheelchair();
		else
			return removeBasic();
	}

	/**
	 * Says if a person can be added to the bus 
	 * @param person the person we want to add
	 * @return true if the person can be added, false otherwise.
	 */
	public boolean canBeAdded(Person person)
	{
		if (person == null)
			return true;
		else if (person.isWheelchaired())
			return !isWheelchairFull();
		else
			return !isBasicFull();
	}

}
