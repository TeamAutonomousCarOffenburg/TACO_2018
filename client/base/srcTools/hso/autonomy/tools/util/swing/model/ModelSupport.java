package hso.autonomy.tools.util.swing.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

/**
 * An abstract class implementing the add/remove property change listener
 * methods of a Java Bean.
 *
 * @author Stefan Glaser
 */
public class ModelSupport implements IModel
{
	private transient SwingPropertyChangeSupport changeSupport;

	protected PropertyChangeSupport changeSupport()
	{
		if (changeSupport == null) {
			changeSupport = new SwingPropertyChangeSupport(this, true);
		}

		return changeSupport;
	}

	@Override
	public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport().addPropertyChangeListener(listener);
	}

	@Override
	public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
	{
		if (changeSupport != null) {
			changeSupport.removePropertyChangeListener(listener);
		}
	}

	@Override
	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		changeSupport().addPropertyChangeListener(propertyName, listener);
	}

	@Override
	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
	{
		if (changeSupport != null) {
			changeSupport.removePropertyChangeListener(propertyName, listener);
		}
	}

	/**
	 * Returns an array of all the <code>PropertyChangeListener</code>s added to
	 * this Model with {@link #addPropertyChangeListener(PropertyChangeListener)}
	 * or {@link #addPropertyChangeListener(String, PropertyChangeListener)}.
	 *
	 * @return all of the <code>PropertyChangeListener</code>s added or an empty
	 *         array if no listeners have been added
	 */
	public synchronized PropertyChangeListener[] getPropertyChangeListeners()
	{
		return changeSupport().getPropertyChangeListeners();
	}
}
