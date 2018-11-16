package hso.autonomy.tools.util.jogl.buffer;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Wrapper class for openGL display lists.
 *
 * @author Stefan Glaser
 */
public class GLDisplayList
{
	private final int listID;

	private boolean valid;

	public GLDisplayList(GL2 gl, GLU glu, GLUT glut, IGLDisplayListRenderer renderer)
	{
		listID = gl.glGenLists(1);

		if (listID == 0) {
			valid = false;
		} else {
			valid = true;

			gl.glPushMatrix();
			{
				gl.glLoadIdentity();

				gl.glNewList(listID, GL2.GL_COMPILE);
				{
					// Render list content
					renderer.render(gl, glu, glut);
				}
				gl.glEndList();
			}
			gl.glPopMatrix();

			// System.out.println("++ Created DisplayList with ID: " + listID);
		}
	}

	public int getListID()
	{
		return listID;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void callList(GL2 gl)
	{
		if (valid) {
			gl.glCallList(listID);
		}
	}

	public void delete(GL2 gl)
	{
		if (valid) {
			valid = false;
			gl.glDeleteLists(listID, 1);

			// System.out.println("--- Deleted DisplayList with ID: " + listID);
		}
	}
}
