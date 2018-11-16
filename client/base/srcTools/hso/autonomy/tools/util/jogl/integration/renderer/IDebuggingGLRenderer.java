package hso.autonomy.tools.util.jogl.integration.renderer;

import hso.autonomy.tools.util.swing.model.IDisplayableItem;

/**
 * Base interface for renderer extensions of debugging views.
 *
 * @author Stefan Glaser
 */
public interface IDebuggingGLRenderer extends IGLRenderer, IDisplayableItem {
	String getRendererID();

	boolean isActive();

	void setActive(boolean active);
}
