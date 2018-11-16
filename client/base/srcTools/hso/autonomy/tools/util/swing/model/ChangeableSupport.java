package hso.autonomy.tools.util.swing.model;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

/**
 * A copy of the state-listener methods of a swing model. The notification calls
 * to the listeners executed by {@link #fireStateChanged()} are ensured to run
 * in the event dispatching thread.
 *
 * @author Stefan Glaser
 */
public class ChangeableSupport implements IChangeable
{
	/**
	 * Only one <code>ChangeEvent</code> is needed per model instance since the
	 * event's only state is the source property. The source of events generated
	 * is always "this".
	 */
	protected transient ChangeEvent changeEvent = null;

	/** Stores the listeners on this model. */
	protected transient EventListenerList listenerList;

	private void createEventListenerList()
	{
		listenerList = new EventListenerList();
	}

	@Override
	public void addChangeListener(ChangeListener l)
	{
		if (listenerList == null) {
			createEventListenerList();
		}
		listenerList.add(ChangeListener.class, l);
	}

	@Override
	public void removeChangeListener(ChangeListener l)
	{
		if (listenerList != null) {
			listenerList.remove(ChangeListener.class, l);
		}
	}

	/**
	 * Returns an array of all the change listeners registered on this
	 * <code>Model</code>.
	 *
	 * @return all of this model's <code>ChangeListener</code>s or an empty array
	 *         if no change listeners are currently registered
	 *
	 * @see #addChangeListener
	 * @see #removeChangeListener
	 */
	public ChangeListener[] getChangeListeners()
	{
		if (listenerList == null) {
			createEventListenerList();
		}
		return listenerList.getListeners(ChangeListener.class);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is created lazily.
	 *
	 * @see EventListenerList
	 */
	protected void fireStateChanged()
	{
		if (listenerList == null) {
			return;
		}

		if (SwingUtilities.isEventDispatchThread()) {
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			// Process the listeners last to first, notifying
			// those that are interested in this event
			for (int i = listeners.length - 2; i >= 0; i -= 2) {
				if (listeners[i] == ChangeListener.class) {
					// Lazily create the event:
					if (changeEvent == null)
						changeEvent = new ChangeEvent(this);
					((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
				}
			}
		} else {
			SwingUtilities.invokeLater(this ::fireStateChanged);
		}
	}
}
