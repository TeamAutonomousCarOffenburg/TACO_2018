package hso.autonomy.tools.util.jogl.integration.renderer;

import javax.media.opengl.GL2;

import hso.autonomy.tools.util.jogl.integration.GLWrapperJPanel;
import hso.autonomy.tools.util.jogl.integration.inputController.IGLInputController;

/**
 * Interface for a general openGL renderer.
 *
 * @author Stefan Glaser
 */
public interface IGLRenderer {
	/**
	 * Called to initialize the renderer with the current openGL environment
	 * (Create display lists, vertex buffer objects, etc.).
	 *
	 * @param gl - the openGL environment
	 */
	void init(GL2 gl);

	/**
	 * Called to tell the renderer that it should render its content on the given
	 * openGL environment.
	 *
	 * @param gl - the openGL environment
	 * @param glPanel - the openGL wrapping panel
	 */
	void render(GL2 gl, GLWrapperJPanel glPanel);

	/**
	 * Called to release all resources which this renderer may have allocated in
	 * the graphics card (e.g. release display lists).
	 *
	 * @param gl - the openGL environment
	 */
	void dispose(GL2 gl);

	/**
	 * Retrieve the input controller of this screen renderer. Since not all
	 * screen renderer also need to handle input, the input controller can also
	 * be null.
	 *
	 * @return the input controller associated with this screen renderer
	 */
	IGLInputController getInputController();
}
