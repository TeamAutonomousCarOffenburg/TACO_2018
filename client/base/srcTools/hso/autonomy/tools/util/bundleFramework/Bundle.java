package hso.autonomy.tools.util.bundleFramework;

public class Bundle
{
	private final BundleDescriptor descriptor;

	private final IBundleActivator bundleActivator;

	public Bundle(BundleDescriptor descriptor, IBundleActivator bundleActivator)
	{
		this.descriptor = descriptor;
		this.bundleActivator = bundleActivator;
	}

	public IBundleActivator getBundleActivator()
	{
		return bundleActivator;
	}

	public IBundleDescriptor getBundleDescriptor()
	{
		return descriptor;
	}

	@Override
	public String toString()
	{
		return descriptor.toString();
	}
}
