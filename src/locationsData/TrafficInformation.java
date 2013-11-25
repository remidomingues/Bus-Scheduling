/**
 * File : TrafficInformations.java
 *
 * Created on May 8, 2012, 4:45:06 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package locationsData;

import java.security.InvalidParameterException;
import java.util.Arrays;

/** 
 *  
 */
public class TrafficInformation
{
	// TODO Yoann : pour l instant j ai palie le probleme du sort en creant des
	// instances bidons de traffic coefficient. Voir si on peut pas ameliorer ca
	// d une maniere ou d un autre (utilisation de sous tableau ?, refaire un
	// sort a la main ? )

	/** The maximum value that a coefficient can have. */
	public static final float MAX_COEFFICIENT = 10;

	/** The minimum value that a coefficient can have */
	public static final float MIN_COEFFICIENT = 1;

	/**
	 * Store information about the different traffic coefficient existing 
	 * depending the hour
	 */
	class TrafficCoefficient implements Comparable<TrafficCoefficient>
	{
		/** Hour, in seconds, representing the beginning of the time frame 
		 * where the traffic coefficient is applicable*/
		int beginFrame;

		/** Hour, in seconds, representing the ending of the time frame 
		 * where the traffic coefficient is applicable*/
		int endFrame;

		/** The coefficient applicable to the traffic which represent the 
		 * rate of increasing duration of travels during the time frame.
		 * For example if coefficient equals 1.2 it means that the duration
		 *  will be 20% longer than in a normal situation. */
		float coefficient;

		/**
		 * Say if an object is equal to the current TrafficCoefficient
		 * @param obj the object to test
		 * @return true if both objects are equals, false if they are not.
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof TrafficCoefficient))
				return false;
			TrafficCoefficient tc = (TrafficCoefficient) obj;
			return beginFrame == tc.beginFrame && endFrame == tc.endFrame;
		}

		/** 
		 * Full constructor
		 * @param beginFrame hour, in seconds, representing the beginning of the time frame 
		 * where the traffic coefficient is applicable
		 * @param endFrame hour, in seconds, representing the ending of the time frame 
		 * where the traffic coefficient is applicable
		 * @param coefficient The coefficient applicable to the traffic which represent the 
		 * rate of increasing duration of travels during the time frame.
		 */
		public TrafficCoefficient(int beginFrame, int endFrame, float coefficient)
		{
			if (endFrame < beginFrame)
				throw new InvalidParameterException(
						"Invalid time frame: the end must be before the beginning (i.e. " + endFrame + " > "
								+ beginFrame + " ).");
			if (coefficient < MIN_COEFFICIENT)
				throw new IllegalArgumentException(
						"Coeficiente inválido : este deve ser superior ou igual à " + MIN_COEFFICIENT + ". (Invalid coefficient)");
			if (coefficient > MAX_COEFFICIENT)
				throw new IllegalArgumentException(
						"Coeficiente inválido : este deve ser inferior ou igual à " + MAX_COEFFICIENT + ". (Invalid coefficient)");
			this.beginFrame = beginFrame;
			this.endFrame = endFrame;
			this.coefficient = coefficient;
		}

		/**
		 * Compare an other TrafficInformation with the current one
		 * @param tc the other TrafficInformation to compare to
		 * @return 0 if both are equals, 1 if the current is lesser, -1 if the current is bigger
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(TrafficCoefficient tc)
		{
			return this.beginFrame - tc.beginFrame;
		}
	}

	/** Say if the array is sorted or not. Private attribute use only for the class management.*/
	private boolean sorted = false;
	/** Number of {@link TrafficCoefficient} in the array. Private attribute use only for the class management.*/
	private int logicSize = 0;
	/** Array of all {@link TrafficCoefficient}. */
	private TrafficCoefficient[] coefficients;

	/** 
	 * Default constructor.
	 */
	public TrafficInformation()
	{
		this(20);
	}

	/**
	 * Full constructor.
	 * @param coefficientCount the number of coefficient that will be add to the object.
	 * Please note that this class is not limited in traffic coefficient size. This constructor
	 * can just guarantee you a better speed if you know in advance how much coefficient you'll have.
	 */
	public TrafficInformation(int coefficientCount)
	{
		coefficients = new TrafficCoefficient[coefficientCount];
		Arrays.fill(coefficients, 0, coefficients.length, new TrafficCoefficient(23 * 3600 + 59 * 60,
				23 * 3600 + 59 * 60, 1));
	}

	/**
	 * Add a coefficient to this traffic information object.
	 * @param startFrame hour, in seconds, representing the beginning of the time frame 
	 * where the traffic coefficient is applicable
	 * @param endFrame hour, in seconds, representing the ending of the time frame 
	 * where the traffic coefficient is applicable
	 * @param coefficient The coefficient applicable to the traffic which represent the 
	 * rate of increasing duration of travels during the time frame.
	 */
	public void addCoefficient(int startFrame, int endFrame, float coefficient)
	{
		TrafficCoefficient tc = new TrafficCoefficient(startFrame, endFrame, coefficient);

		if (logicSize == coefficients.length)
		{
			resize();
		}

		coefficients[logicSize] = tc;
		logicSize++;
		sorted = false;
	}

	/** 
	 * Automatically increase the array size. Only use for the class management. 
	 */
	public void resize()
	{
		int newSize = coefficients.length + 10;
		TrafficCoefficient[] newTab = new TrafficCoefficient[newSize];

		for (int i = 0, size = coefficients.length ; i < size ; i++)
		{
			newTab[i] = coefficients[i];
		}
		Arrays.fill(newTab, coefficients.length, newSize - 1, new TrafficCoefficient(23 * 3600 + 59 * 60,
				23 * 3600 + 59 * 60, 1));
		coefficients = newTab;
	}

	/** 
	 * Sort the {@link TrafficCoefficient} array. Only use for the class management. 
	 */
	public void sort()
	{
		Arrays.sort(coefficients);
		sorted = true;
	}

	/**
	 * Return the coefficient corresponding the given hour. 
	 * @param hour the hour from where we want to know the traffic coefficient.
	 * @return the traffic coefficient.
	 */
	public float getCoefficient(int hour)
	{
		int start = 0, end = logicSize, middle;

		if (!sorted)
		{
			sort();
		}

		while ((end - start) > 1)
		{
			middle = (start + end) / 2;

			if (hour >= coefficients[middle].beginFrame && hour <= coefficients[middle].endFrame)
				return coefficients[middle].coefficient;

			if (coefficients[middle].beginFrame > hour)
			{
				end = middle;
			}
			else
			{
				start = middle;
			}
		}
		return 1;
	}
}
