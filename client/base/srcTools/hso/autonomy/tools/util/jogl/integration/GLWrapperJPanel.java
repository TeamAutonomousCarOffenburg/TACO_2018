package hso.autonomy.tools.util.jogl.integration;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JPanel;

import hso.autonomy.tools.util.jogl.buffer.GLObjectBuffer;
import hso.autonomy.tools.util.jogl.integration.inputController.IGLInputController;
import hso.autonomy.tools.util.jogl.integration.inputController.impl.GLCameraInputController;
import hso.autonomy.tools.util.jogl.integration.renderer.IGLRenderer;
import hso.autonomy.tools.util.jogl.view.UniversalCamera;
import hso.autonomy.tools.util.jogl.view.ViewPort;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Implementation of a JPanel, wrapping an jogl openOG context. This class does
 * not provide any rendering directives directly. Instead rendering is done
 * entirely by {@link IGLRenderer} objects.<br>
 * <br>
 * This implementation supports two rendering modes, one called screen-mode and
 * one called scene-mode. The only difference between these two rendering modes
 * in terms of rendering is the coordinate system their objects are defined in.
 * The scene-mode coordinate system is user defined to match the rendered scene.
 * It is not directly related to pixel coordinates and therefore independent of
 * the screen it is rendered to. The screen-mode however objects are defined
 * with respect to a coordinate system relating directly to pixels of this
 * panel. Its origin is at the bottom left corner of this panel, while positive
 * x is the right and positive y upwards.<br>
 * <br>
 * To decouple the input from the screen renderer, there exists a separate
 * interface called {@link IGLInputController} for handling input. Due to
 * performance reasons and since there is usually only one user doing one thing,
 * user input is only forwarded to one specific input controller that has to be
 * selected.<br>
 * <br>
 * The camera is abstracted in the {@link UniversalCamera} class. It is in
 * charge of handling the scene coordinate system definition.
 *
 * @author Stefan Glaser
 */
public class GLWrapperJPanel extends JPanel implements GLEventListener
{
	// TODO: Create proper switch to turn debug mode on/off
	private static final boolean DEBUG = true;

	protected enum RenderingMode { INITIAL, SCENE_MODE, SCREEN_MODE }

	protected boolean init;

	protected UniversalCamera camera;

	protected final ViewPort viewPort;

	protected final GLObjectBuffer staticObjectBuffer;

	protected RenderingMode renderingMode;

	protected GLProfile glProfile;

	protected GLCapabilities glCapabilities;

	protected GLJPanel glCanvas;

	protected final ArrayList<IGLRenderer> sceneRenderer;

	protected IGLInputController activeInputController;

	protected final GLCameraInputController cameraInputController;

	protected GLU glu;

	protected GLUT glut;

	public GLWrapperJPanel(List<? extends IGLRenderer> sceneRendererList, UniversalCamera.CameraMode mode)
	{
		this(sceneRendererList, mode, true);
	}

	public GLWrapperJPanel(
			List<? extends IGLRenderer> sceneRendererList, UniversalCamera.CameraMode mode, boolean hasCameraController)
	{
		init = false;
		viewPort = new ViewPort();
		camera = new UniversalCamera(mode);
		staticObjectBuffer = new GLObjectBuffer();
		sceneRenderer = new ArrayList<>();
		if (sceneRendererList != null && sceneRendererList.size() > 0) {
			sceneRenderer.addAll(sceneRendererList);
		}

		// Create GL canvas
		glProfile = GLProfile.get(GLProfile.GL2);
		glCapabilities = new GLCapabilities(glProfile);
		glCapabilities.setSampleBuffers(true);
		glCapabilities.setNumSamples(8);
		glCanvas = new GLJPanel(glCapabilities);

		// Create camera input controller and add it to the glCanvas
		if (hasCameraController) {
			cameraInputController = new GLCameraInputController();
			cameraInputController.inputActivated(this);
			glCanvas.addKeyListener(cameraInputController);
			glCanvas.addMouseMotionListener(cameraInputController);
			glCanvas.addMouseWheelListener(cameraInputController);
			glCanvas.addMouseListener(cameraInputController);
		} else {
			cameraInputController = null;
		}

		// Panel properties
		setLayout(new BorderLayout());
		add(glCanvas, BorderLayout.CENTER);
		setMinimumSize(new Dimension(300, 225));
		setBackground(new Color(0.2f, 0.2f, 0.2f));

		// Register this panel as top level renderer
		glCanvas.addGLEventListener(this);
	}

	public boolean activateRendererInputController(IGLRenderer renderer)
	{
		if (renderer == null) {
			return activateInputController(null);
		} else {
			return activateInputController(renderer.getInputController());
		}
	}

	public boolean activateInputController(IGLInputController inputController)
	{
		if (inputController != activeInputController) {
			if (activeInputController != null) {
				glCanvas.removeKeyListener(activeInputController);
				glCanvas.removeMouseMotionListener(activeInputController);
				glCanvas.removeMouseWheelListener(activeInputController);
				glCanvas.removeMouseListener(activeInputController);
				activeInputController.inputDeactivated();
			}

			activeInputController = inputController;

			if (activeInputController != null) {
				activeInputController.inputActivated(this);
				glCanvas.addKeyListener(activeInputController);
				glCanvas.addMouseMotionListener(activeInputController);
				glCanvas.addMouseWheelListener(activeInputController);
				glCanvas.addMouseListener(activeInputController);
			}
		}

		return true;
	}

	public void enableCameraController()
	{
		if (cameraInputController != null) {
			cameraInputController.enable();
		}
	}

	public void disableCameraController()
	{
		if (cameraInputController != null) {
			cameraInputController.disable();
		}
	}

	public void zoomCameraFrame(int x, int y, float zoomFactor)
	{
		if (camera.getMode() != UniversalCamera.CameraMode.Camera2D) {
			return;
		}

		int halfPanelWidth = getWidth() / 2;
		int halfPanelHeight = getHeight() / 2;
		Vector3D cameraPos = camera.getPosition();

		double halfFrameWidth = (cameraPos.getZ() / 2) * halfPanelWidth / halfPanelHeight;
		double halfFrameHeight = cameraPos.getZ() / 2;

		double xScale = halfFrameWidth / halfPanelWidth;
		double yScale = halfFrameHeight / halfPanelHeight;

		// Since the only true reference between the camera frame and the panel is
		// the center, we have to relate mouse input transformations to the center
		// of both systems
		float xCenter = (float) ((x - halfPanelWidth) * xScale);
		float yCenter = (float) ((halfPanelHeight - y) * yScale);

		camera.zoom(xCenter, yCenter, zoomFactor);

		// Request repaint
		glCanvas.repaint();
	}

	public void moveCameraFrame(int x, int y)
	{
		int currentWidth = getWidth();
		int currentHeight = getHeight();
		Vector3D cameraPos = camera.getPosition();

		double frameWidth = cameraPos.getZ() * currentWidth / currentHeight;
		double frameHeight = cameraPos.getZ();

		double xScale = frameWidth / currentWidth;
		double yScale = frameHeight / currentHeight;

		camera.move(x * xScale, -y * yScale);

		// Request repaint
		glCanvas.repaint();
	}

	public GLU getGlu()
	{
		return glu;
	}

	public GLUT getGlut()
	{
		return glut;
	}

	public UniversalCamera getCamera()
	{
		return camera;
	}
	public void setCamera(UniversalCamera camera)
	{
		this.camera = camera;
	}

	public ViewPort getViewPort()
	{
		return viewPort;
	}

	public GLObjectBuffer getObjectBuffer()
	{
		return staticObjectBuffer;
	}

	public void repaintGL()
	{
		glCanvas.repaint();
	}

	@Override
	public void init(GLAutoDrawable drawable)
	{
		// System.out.println("INIT");

		// Reset rendering mode
		renderingMode = RenderingMode.INITIAL;

		if (DEBUG) {
			drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
		}

		GL2 gl = drawable.getGL().getGL2();

		gl.glEnable(GL2.GL_BLEND);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		// gl.glEnable(GL2.GL_POINT_SPRITE);
		// gl.glEnable(GL2.GL_POINT_SMOOTH);
		// gl.glHint(GL2.GL_POINT_SMOOTH_HINT, GL2.GL_NICEST);

		// Set background to half grey
		gl.glClearColor(0.2f, 0.2f, 0.2f, 1);

		// Load Identity matrix to model-view stack
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		// Initialize renderer
		for (int i = 0; i < sceneRenderer.size(); i++) {
			sceneRenderer.get(i).init(gl);
		}

		glu = new GLU();
		glut = new GLUT();

		init = true;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		// System.out.println("RESHAPE");

		// GL2 gl = drawable.getGL().getGL2();
		//
		// // Change to projection matrix.
		// gl.glMatrixMode(GL2.GL_PROJECTION);
		// gl.glLoadIdentity();
		//
		// // Perspective.
		// float widthHeightRatio = (float) width / (float) height;
		// // glu.gluPerspective(90, widthHeightRatio, 0.01, 100);
		// // gl.glFrustumf(-2f, 2f, -2f, 2f, 0.001f, 5f);
		// gl.glOrthof(-10, 10, -10 / widthHeightRatio, 10 / widthHeightRatio,
		// -100,
		// 100);
		//
		// // gl.glOrthof(-width / 100, width / 100, -height / 100, height / 100,
		// -1,
		// // 1);
		//
		// // Change back to model view matrix.
		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glLoadIdentity();
		// glu.gluLookAt(0, 0, 1, 0, 0, 0, 0, 1, 0);
		// // gl.glRotatef(30, 0, 0, 1);
		// // gl.glTranslatef(1, 1, 1);
		// // System.out.println("Hallo");

		// GL2 gl = drawable.getGL().getGL2();
		//
		// gl.glMatrixMode(GL2.GL_MODELVIEW);
		// gl.glLoadIdentity();
		// // gl.glViewport(0, 0, width, height);
		// int size = width > height ? width : height;
		// int xPos = (width - size) / 2;
		// int yPos = (height - size) / 2;
		// gl.glViewport(xPos, yPos, size, size);
		// // gl.glViewport(0, 0, size, size);

		GL2 gl = drawable.getGL().getGL2();

		viewPort.update(x, y, width, height);
		viewPort.applyTo(gl);
	}

	@Override
	public void display(GLAutoDrawable drawable)
	{
		if (!init) {
			return;
		}

		GL2 gl = drawable.getGL().getGL2();

		// TODO funktioniert, aber ist das richtig ?
		viewPort.applyTo(gl);

		// Reset rendering mode
		renderingMode = RenderingMode.INITIAL;
		activateSceneMode(gl);

		// Clear buffer
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		// Call render
		for (int i = 0; i < sceneRenderer.size(); i++) {
			sceneRenderer.get(i).render(gl, this);
		}
	}

	@Override
	public void dispose(GLAutoDrawable drawable)
	{
		GL2 gl = drawable.getGL().getGL2();

		for (int i = 0; i < sceneRenderer.size(); i++) {
			sceneRenderer.get(i).dispose(gl);
		}

		glu = null;
		glut = null;

		// Delete all buffered display lists (other GLWrapperPanels will have to
		// create all objects they need again, but since we do not keep track of
		// who is using which resource, this is the lazy programmer way of
		// avoiding memory wholes)
		staticObjectBuffer.deleteAllDisplayLists(gl);
	}

	public void activateSceneMode(GL2 gl)
	{
		if (renderingMode == RenderingMode.SCENE_MODE) {
			return;
		}

		renderingMode = RenderingMode.SCENE_MODE;
		camera.applyTo(gl, glu, this);
	}

	public void activateScreenMode(GL2 gl)
	{
		if (renderingMode == RenderingMode.SCREEN_MODE) {
			return;
		}

		renderingMode = RenderingMode.SCREEN_MODE;

		// Prepare projection stack matrix to match the screen size
		// (origin bottom left, positive x to the right, positive y upwards)
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, getWidth(), 0, getHeight(), -1, 1);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
}
