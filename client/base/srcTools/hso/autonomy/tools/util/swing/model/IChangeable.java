package hso.autonomy.tools.util.swing.model;

import javax.swing.event.ChangeListener;

/**
 * A copy of the add/remove change listener interface methods of a swing model.
 *
 * @author Stefan Glaser
 */
public interface IChangeable {
	/**
	 * Adds a <code>ChangeListener</code> to the model.
	 *
	 * @param l the listener to add
	 */
	void addChangeListener(ChangeListener l);

	/**
	 * Removes a <code>ChangeListener</code> from the model.
	 *
	 * @param l the listener to remove
	 */
	void removeChangeListener(ChangeListener l);
}
