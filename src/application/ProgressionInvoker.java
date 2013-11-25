/**
 * File : ProgressionInvoker.java
 *
 * Created on May 28, 2012, 4:24:30 PM
 *
 * Authors : Rémi DOMINGUES & Yoann ALVAREZ
 */

package application;

import presentation.ProgressionListener;
import presentation.ProgressionEvent.ProgressionType;

/**
 *  
 */
public interface ProgressionInvoker
{
	/**
	 * Add a progression listener to the listeners list
	 * @param listener The listener to add
	 */
	public abstract void addProgressionListener(ProgressionListener listener);

	/**
	 * Remove a progression listener from the listeners list
	 * @param listener The listener to remove
	 */
	public abstract void removeProgressionListener(ProgressionListener listener);

	/**
	 * Fire a progression event to listeners
	 * @param source The source object
	 * @param type Progression type
	 * @param value Progression value
	 */
	public abstract void fireEvent(Object source, ProgressionType type, Integer value);

	/**
	 * Fire a progression event to listeners
	 * @param source The source object
	 * @param type Progression type
	 * @param value Progression value
	 * @param time Time to show progression
	 */
	public abstract void fireEvent(Object source, ProgressionType type, Integer value, Long time);
}
