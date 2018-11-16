package hso.autonomy.tools.developer.bundles.developer.perspective;

import javax.swing.Icon;

import hso.autonomy.tools.util.bundleFramework.BundleContext;
import hso.autonomy.tools.util.imageBuffer.ImageFile;

/**
 * Base class for a view descriptor.
 *
 * @author Stefan Glaser
 */
public abstract class ViewDescriptorBase implements IViewDescriptor
{
	protected final BundleContext context;

	protected final String viewID;

	protected final String title;

	protected final Icon icon;

	public ViewDescriptorBase(BundleContext context, String viewID, String title, ImageFile imageFile)
	{
		this.context = context;
		this.viewID = viewID;
		this.title = title;
		this.icon = imageFile.getIcon();
	}

	@Override
	public String getViewID()
	{
		return viewID;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public Icon getIcon()
	{
		return icon;
	}

	@Override
	public String getDescription()
	{
		return "";
	}

	@Override
	public String getToolTip()
	{
		return "";
	}

	@Override
	public boolean acceptsObject(Object o)
	{
		return false;
	}

	@Override
	public boolean requiresObject()
	{
		return false;
	}

	@Override
	public boolean isManuallyOpenable()
	{
		return true;
	}
}
