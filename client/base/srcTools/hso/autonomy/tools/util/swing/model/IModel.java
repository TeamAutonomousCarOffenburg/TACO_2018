package hso.autonomy.tools.util.swing.model;

import java.beans.PropertyChangeListener;

/**
 * A Collection of add/remove property change listener methods for a Java Bean.
 *
 * @author Stefan Glaser
 */
public interface IModel {
	/**
	 * Adds a <code>PropertyChange</code> listener.
	 *
	 * @param listener a <code>PropertyChangeListener</code> object
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Removes a <code>PropertyChange</code> listener.
	 *
	 * @param listener a <code>PropertyChangeListener</code> object
	 * @see #addPropertyChangeListener
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Adds a <code>PropertyChangeListener</code> listener to the specified
	 * property.
	 *
	 * @param listener a <code>PropertyChangeListener</code> object
	 */
	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

	/**
	 * Removes a <code>PropertyChangeListener</code> listener from the specified
	 * property.
	 *
	 * @param listener a <code>PropertyChangeListener</code> object
	 * @see #addPropertyChangeListener
	 */
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
