package hso.autonomy.tools.util.jogl.integration.renderer;

import javax.swing.Icon;

import hso.autonomy.tools.util.imageBuffer.ImageFile;
import hso.autonomy.tools.util.jogl.integration.inputController.IGLInputController;

/**
 * Base class for debugging view openGL renderer.
 *
 * @author Stefan Glaser
 */
public abstract class DebuggingGLRendererBase implements IDebuggingGLRenderer
{
	protected String rendererID;

	protected Icon icon;

	protected String title;

	protected String description;

	protected String toolTip;

	protected boolean active;

	protected boolean defaultTargetScene;

	public DebuggingGLRendererBase(
			String rendererID, ImageFile imageFile, String title, String description, boolean active)
	{
		this.rendererID = rendererID;
		if (imageFile != null) {
			this.icon = imageFile.getIcon();
		}
		this.title = title;
		this.description = description;
		this.toolTip = description;
		this.active = active;
		this.defaultTargetScene = true;
	}

	public DebuggingGLRendererBase(String rendererID, DebuggingGLRendererParams params)
	{
		this(rendererID, params.getImageFile(), params.getTitle(), params.getDescription(), params.isActive());
	}

	@Override
	public String getRendererID()
	{
		return rendererID;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getToolTip()
	{
		return toolTip;
	};

	@Override
	public boolean isActive()
	{
		return active;
	}

	@Override
	public void setActive(boolean active)
	{
		this.active = active;
	}

	@Override
	public IGLInputController getInputController()
	{
		return null;
	}
}
