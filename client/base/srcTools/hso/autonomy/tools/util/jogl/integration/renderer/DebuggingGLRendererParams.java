package hso.autonomy.tools.util.jogl.integration.renderer;

import hso.autonomy.tools.util.imageBuffer.ImageFile;

public class DebuggingGLRendererParams
{
	private final ImageFile imageFile;

	private final String title;

	private final String description;

	private final boolean active;

	public DebuggingGLRendererParams(ImageFile imageFile, String title, String description, boolean active)
	{
		this.imageFile = imageFile;
		this.title = title;
		this.description = description;
		this.active = active;
	}

	public boolean isActive()
	{
		return active;
	}

	public String getDescription()
	{
		return description;
	}

	public String getTitle()
	{
		return title;
	}

	public ImageFile getImageFile()
	{
		return imageFile;
	}
}
