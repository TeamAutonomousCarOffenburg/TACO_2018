package hso.autonomy.tools.util.jogl.buffer;

import java.util.HashMap;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * This class (currently) implements a buffer for openGL display lists.
 * Externally created display lists can be stored in this buffer for later use.
 * The display lists contained in this buffer can be accessed via a used defined
 * key. This way, display lists can be shared between different renderer.<br>
 * This class is mend to act as central buffer, such that resources allocated on
 * the graphics card can be easily shared and deleted.<br>
 * At the moment each gl-panel needs its own buffer because sharing the display
 * lists currently results in crashing the application. But later we may even
 * share display lists between different gl-panels to further restrict the
 * application footprint on the graphics card.
 *
 * @author Stefan Glaser
 */
public class GLObjectBuffer
{
	private HashMap<String, GLDisplayList> displayLists;

	public GLObjectBuffer()
	{
		displayLists = new HashMap<>();
	}

	public boolean addDisplayList(String key, GL2 gl, GLU glu, GLUT glut, IGLDisplayListRenderer listRenderer)
	{
		if (isKeyValid(key)) {
			return false;
		}

		displayLists.put(key, new GLDisplayList(gl, glu, glut, listRenderer));
		return true;
	}

	/**
	 * Check if there is a valid display list associated with the given key.
	 *
	 * @param key - the key to check
	 * @return true if there exists a display listID to the given key
	 */
	private boolean isKeyValid(String key)
	{
		return displayLists.containsKey(key);
	}

	/**
	 * Call the display list associated with the given key.
	 *
	 * @param gl - the openGL context
	 * @param key - the key
	 *
	 * @return false if there is no display list associated with the key, true
	 *         otherwise
	 */
	public boolean callDisplayList(GL2 gl, String key)
	{
		GLDisplayList list = displayLists.get(key);

		if (list == null) {
			System.err.println("No corresponding display list found: " + key);
			return false;
		}

		if (!list.isValid()) {
			System.err.println("Corresponding display list is invalid: " + key);
			return false;
		}

		displayLists.get(key).callList(gl);
		return true;
	}

	/**
	 * Delete the display list associated with the given key.
	 *
	 * @param gl - the openGL context
	 * @param key - the key to delete
	 */
	public void deleteDisplayList(GL2 gl, String key)
	{
		if (displayLists.containsKey(key)) {
			displayLists.get(key).delete(gl);
			displayLists.remove(key);
		}
	}

	/**
	 * Delete all display lists stored in this buffer.
	 *
	 * @param gl - the openGL context
	 */
	public void deleteAllDisplayLists(GL2 gl)
	{
		HashMap<String, GLDisplayList> oldMap = displayLists;
		displayLists = new HashMap<>();

		for (GLDisplayList list : oldMap.values()) {
			list.delete(gl);
		}
	}
}
