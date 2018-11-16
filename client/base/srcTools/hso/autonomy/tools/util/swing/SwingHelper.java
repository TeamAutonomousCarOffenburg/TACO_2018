package hso.autonomy.tools.util.swing;

import java.awt.Component;
import java.awt.Window;
import java.util.EventObject;

import javax.swing.JPopupMenu;

/**
 * A collection of static helper methods related to Swing.
 *
 * @author Stefan Glaser
 */
public class SwingHelper
{
	/**
	 * Retrieve the top-level Window instance of this component.<br>
	 * This method tries to find the top-level Window component by following the
	 * parent-hierarchy of the components involved. If a JPopupMenu is involved
	 * (which acts as a top level of the hierarchy without being a Window), this
	 * method continues to search for a Window in the popup-invoker hierarchy.
	 * <br>
	 * Known issues: This method fails if the popup menu's invoker is not set
	 * properly.
	 *
	 * @param component - a component of the Window of interest
	 * @return the top level Window instance which the component is currently
	 *         part of, or null if no top-level Window could be determined
	 */
	public static Window getTopLevelWindow(Component component)
	{
		if (component == null) {
			return null;
		} else if (component instanceof Window) {
			return (Window) component;
		} else if (component instanceof JPopupMenu) {
			return getTopLevelWindow(((JPopupMenu) component).getInvoker());
		} else {
			return getTopLevelWindow(component.getParent());
		}
	}

	/**
	 * Retrieve the top-level Window instance of the source of the given
	 * EventObject.<br>
	 * This method tries to find the top-level Window component by following the
	 * parent-hierarchy of the components involved. If a JPopupMenu is involved
	 * (which acts as a top level of the hierarchy without being a Window), this
	 * method continues to search for a Window in the popup-invoker hierarchy.
	 * <br>
	 * Known issues: This method fails if the popup menu's invoker is not set
	 * properly.
	 *
	 * @param event - the event of interest
	 * @return the top level Window instance which the source-component is
	 *         currently part of, or null if no top-level Window could be
	 *         determined
	 */
	public static Window getTopLevelWindow(EventObject event)
	{
		if (event == null) {
			return null;
		} else {
			Object source = event.getSource();

			if (source instanceof Component) {
				return getTopLevelWindow((Component) source);
			} else {
				return null;
			}
		}
	}
}
