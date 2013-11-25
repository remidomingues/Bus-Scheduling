/**
 * File : ProgressionEvent.java
 *
 * Created on May 17, 2012, 3:21:30 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package presentation;

import java.util.EventObject;

/**
 * Progression event used to add process progression
 */
public class ProgressionEvent extends EventObject
{
	/** Default version serial ID */
	private static final long serialVersionUID = 1L;

	/**
	 * Type of the progression value
	 */
	public enum ProgressionType
	{
		/** The value is an incrementation */
		INCREMENT,
		/** The value is a set */
		SET,
		/** The progression has been interrupted */
		INTERRUPT
	}

	/** Progression value type */
	private ProgressionType type;

	/** Progression value */
	private Integer value;

	/** Time used to increase the progression */
	private Long time;

	/**
	 * Constructor
	 * @param source Source object
	 * @param type Progression type
	 * @param value Progression value
	 */
	public ProgressionEvent(Object source, ProgressionType type, Integer value)
	{
		this(source, type, value, null);
	}

	/**
	 * Full constructor
	 * @param source Source object
	 * @param type Progression type
	 * @param value Progression value
	 * @param time Time used to increase the progression
	 */
	public ProgressionEvent(Object source, ProgressionType type, Integer value, Long time)
	{
		super(source);
		this.type = type;
		this.value = value;
		this.time = time;
	}

	/**
	 * Return the type value
	 * @return The type value
	 */
	public ProgressionType getType()
	{
		return type;
	}

	/**
	 * Return the time value
	 * @return The time value
	 */
	public Long getTime()
	{
		return time;
	}

	/**
	 * Return the value value
	 * @return The value value
	 */
	public Integer getValue()
	{
		return value;
	}

}
